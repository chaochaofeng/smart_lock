package com.example.cyclesmart

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cyclesmart.ui.theme.CycleSmartTheme
import java.util.UUID
import java.util.concurrent.Semaphore


public class CycleBluetooth {
    public var connect_state = false
    public var finddev = 0
    public var findgattserver = false
    public var notify = false
}

class MainActivity : ComponentActivity(), View.OnClickListener {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var connectedDevice: BluetoothDevice? = null
    private lateinit var gattService: BluetoothGattService
    private lateinit var gattCharacteristic: BluetoothGattCharacteristic

    private val tAG = "haha"
    private val serveruiid =   "0000180f-0000-1000-8000-00805f9b34fb"
    private val characteristicuuid = "0000ff00-0000-1000-8000-00805f9b34fb"
    private val charactercfg = "00002902-0000-1000-8000-00805f9b34fb"
    private val myDevAdder = "54:32:04:47:F9:7E"
    private var lockstate = 0 //1: unlock 2:lock 3:power
    private val semaphore = Semaphore(0)
    private lateinit var mGatt: BluetoothGatt
    private lateinit var cycleble: CycleBluetooth

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_lock -> {
                when (lockstate) {
                    1 -> {
                        lockstate = 2
                        findViewById<ImageButton>(R.id.button_lock).setImageResource(R.drawable.lock_icon)
                        val value = "cls".toByteArray()
                        writeCharacteristicVal(value)
                        Log.d("haha", "lockstate lock")
                    }
                    2-> {
                        lockstate = 1
                        findViewById<ImageButton>(R.id.button_lock).setImageResource(R.drawable.unlock_icon)
                        val value = "opl".toByteArray()
                        writeCharacteristicVal(value)
                        Log.d("haha", "lockstate unlock")
                    }
                    3 ->
                        Toast.makeText(this, "请先关闭电源", Toast.LENGTH_SHORT).show()
                    else -> {
                        Toast.makeText(this, "press pwr 连接蓝牙", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            R.id.button_pwr -> {
                when (lockstate) {
                    1 -> {
                        Log.d("haha", "pwr up")
                        lockstate = 3
                        findViewById<Button>(R.id.button_pwr).setBackgroundResource(R.drawable.shape_circle)
                        val value = "pwr".toByteArray()
                        writeCharacteristicVal(value)
                    }
                    2 ->
                        Toast.makeText(this, "请先解锁再上电", Toast.LENGTH_SHORT).show()
                    3 -> {
                        lockstate = 1
                        findViewById<Button>(R.id.button_pwr).setBackgroundResource(R.drawable.shape_circle_gray)
                        val value = "opl".toByteArray()
                        writeCharacteristicVal(value)
                        Log.d("haha", "pwr down")
                    }
                    else -> {
                        Toast.makeText(this, "正在连接蓝牙", Toast.LENGTH_SHORT).show()

                        cycleble.finddev = 0
                        startBluetoothDiscovery()
                    }
                }
            }
        }
    }

    private fun updateButtonUi(state:Int) {
        when(state) {
            1-> {
                findViewById<Button>(R.id.button_pwr).setBackgroundResource(R.drawable.shape_circle_gray)
                findViewById<ImageButton>(R.id.button_lock).setImageResource(R.drawable.unlock_icon)
                findViewById<ImageButton>(R.id.button_lock).setBackgroundResource(R.drawable.shape_circle)
            }
            2->{
                findViewById<Button>(R.id.button_pwr).setBackgroundResource(R.drawable.shape_circle_gray)
                findViewById<ImageButton>(R.id.button_lock).setImageResource(R.drawable.lock_icon)
                findViewById<ImageButton>(R.id.button_lock).setBackgroundResource(R.drawable.shape_circle)
            }
            3-> {
                findViewById<Button>(R.id.button_pwr).setBackgroundResource(R.drawable.shape_circle)
                findViewById<ImageButton>(R.id.button_lock).setImageResource(R.drawable.unlock_icon)
                findViewById<ImageButton>(R.id.button_lock).setBackgroundResource(R.drawable.shape_circle)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CycleSmartTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
        setContentView(R.layout.activity_main)

        findViewById<ImageButton>(R.id.button_lock).setOnClickListener(this)
        findViewById<Button>(R.id.button_pwr).setOnClickListener(this)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (!bluetoothAdapter.isEnabled) {
            Log.d(tAG, "please open bluetooth")
            return
        }

        // 请求位置权限 (蓝牙扫描需要)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT), 2)
            Thread.sleep(1000)
        }

        cycleble = CycleBluetooth()
        cycleble.connect_state = false
        cycleble.finddev = 0
        cycleble.findgattserver = false
        cycleble.notify = false

        startBluetoothDiscovery()
        Log.d(tAG, "start bluetooth scan")
        //mainThread()
    }

    /*
    private fun mainThread() {

        val thread = Thread {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
            }
            val handler = Handler(Looper.getMainLooper())

            while (true) {
                semaphore.acquire()
                if (cycleble.finddev == 1)
                    continue

                Thread.sleep(10000)

                if (cycleble.finddev == 0) {
                    bluetoothAdapter.cancelDiscovery()
                    Thread.sleep(10000)

                }

                bluetoothAdapter.cancelDiscovery()

                if (cycleble.finddev == 1 && !cycleble.connect_state) {
                    Log.d(tAG, "start connect device")
                    connectedDevice?.let { connectToDevice(it) }
                    Thread.sleep(5000)
                }
            }
        }.start()
    }
*/
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                val rssi: Short = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)

                if (ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    if (device != null) {

                        if (device.name != null) {
                            Log.d(tAG, "find: " + device.name + " mac: " + device.getAddress() + " rssi: " + rssi)
                        }

                        if (device.getAddress() == myDevAdder) {
                            bluetoothAdapter.cancelDiscovery()

                            connectedDevice = device

                            connectToDevice(device)

                            cycleble.finddev = 1

                            semaphore.release()
                        }
                    }
                    else {
                        Log.d(tAG, "device is null")
                    }
                }
            }
        }
    }

    private fun startBluetoothDiscovery() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }

        Log.d(tAG, "start scan")

        bluetoothAdapter.startDiscovery()
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
    }

    private fun gattClose(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        mGatt.disconnect()
        mGatt.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)

        if (cycleble.connect_state) {
            gattClose()
            cycleble.connect_state = false
        }
    }

    // 蓝牙设备连接
    private fun connectToDevice(device: BluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        mGatt = device.connectGatt(this, false, gattCallback)
    }
    // 设备连接回调
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("haha", "Bluetooth has connected")
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                val ret = mGatt.discoverServices()
                if (!ret) {
                    Log.d(tAG, "Services discovered failed");
                }

                cycleble.connect_state = true
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("Bluetooth", "Disconnected from GATT server.")

                cycleble.connect_state = false

                lockstate = 0

                findViewById<ImageButton>(R.id.button_lock).setBackgroundResource(R.drawable.shape_circle_gray)
                findViewById<Button>(R.id.button_pwr).setBackgroundResource(R.drawable.shape_circle_gray)

                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    Toast.makeText(applicationContext, "断开连接", Toast.LENGTH_SHORT).show()
                }

                bluetoothAdapter.cancelDiscovery()

                if (status == 133) {
                    cycleble.finddev = 0
                    gattClose()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("Bluetooth", "Services discovered.")
                // 查找特征并操作
                Log.d("Bluetooth", gatt.toString() + " size: " + gatt.services.size)

                for (service in gatt.services) {
                    Log.d(tAG, "type:" + service.type + " uuid: " + service.uuid.toString())
                }
                val service: BluetoothGattService = gatt.getService(UUID.fromString(serveruiid))
                if (service != null) {
                    cycleble.findgattserver = true
                    gattCharacteristic = service.getCharacteristic(UUID.fromString(characteristicuuid))
                    if (gattCharacteristic != null) {
                        // 开始读取特征
                        if (ActivityCompat.checkSelfPermission(
                                applicationContext,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return
                        }

                        gatt.readCharacteristic(gattCharacteristic)
                    }
                }
            }
        }

        private fun updateLockState(str:String) {
            Log.d("Bluetooth", "Characteristic read: $str")

            when(str) {
                "cls" -> lockstate = 2
                "opl" -> lockstate = 1
                "pwr" -> lockstate = 3
            }

            val handler = Handler(Looper.getMainLooper())
            handler.post {
                updateButtonUi(lockstate)
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val value = characteristic.value
                val stastr = String(value)

                updateLockState(stastr)
            }


            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            if (!cycleble.notify) {
                gatt.setCharacteristicNotification(gattCharacteristic, true)

                val descriptor: BluetoothGattDescriptor =
                    gattCharacteristic.getDescriptor(UUID.fromString(charactercfg))
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                gatt.writeDescriptor(descriptor)
                Log.d(tAG, "enable bluetooth descriptor")
                cycleble.notify = true
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            gatt.readCharacteristic(gattCharacteristic)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("Bluetooth", "Characteristic written successfully.")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun writeCharacteristicVal(value: ByteArray) {
        if (!cycleble.findgattserver)
            return

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mGatt.writeCharacteristic(gattCharacteristic, value, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CycleSmartTheme {
        Greeting("Android")
    }
}
