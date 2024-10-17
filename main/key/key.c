#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "driver/gpio.h"
#include "esp_log.h"

#include "key.h"
#include "ble_ctl.h"

#define KEY_UNLOCK_PIN  3
#define KEY_LOCK_PIN    4
#define KEY_POWER_PIN   5

#define KEY_PRESS_STATE 0

static int lock_state = 0;

#define TAG "lock"

void key_init(void)
{
    gpio_config_t io_conf = {
        .mode = GPIO_MODE_OUTPUT,
        .pin_bit_mask = 1ULL << KEY_UNLOCK_PIN
    };
    gpio_config(&io_conf);

    io_conf.pin_bit_mask = 1ULL << KEY_LOCK_PIN;
    gpio_config(&io_conf);

    io_conf.pin_bit_mask = 1ULL << KEY_POWER_PIN;
    gpio_config(&io_conf);

    gpio_set_level(KEY_UNLOCK_PIN, !KEY_PRESS_STATE);
    gpio_set_level(KEY_LOCK_PIN, !KEY_PRESS_STATE);
    gpio_set_level(KEY_POWER_PIN, !KEY_PRESS_STATE);
}

static void key_press(int key)
{
    gpio_set_level(key, KEY_PRESS_STATE);

    vTaskDelay(500 / portTICK_PERIOD_MS);

    gpio_set_level(key, !KEY_PRESS_STATE);
}

void set_unlock(void)
{
    ESP_LOGI(TAG, "%s", __func__);

    key_press(KEY_UNLOCK_PIN);
    lock_state = KEY_UNLOCK;

    notify_state_to_app(KEY_UNLOCK);
}

void set_lock(void)
{
    ESP_LOGI(TAG, "%s", __func__);

    key_press(KEY_LOCK_PIN);
    lock_state = KEY_LOCK;

    notify_state_to_app(KEY_LOCK);
}

void set_power(void)
{
    ESP_LOGI(TAG, "%s", __func__);

    key_press(KEY_POWER_PIN);

    vTaskDelay(300 / portTICK_PERIOD_MS);

    key_press(KEY_POWER_PIN);

    lock_state = KEY_POWER;

    notify_state_to_app(KEY_POWER);
}

int get_lock_state(void)
{
    return lock_state;
}
