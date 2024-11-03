#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "driver/gpio.h"
#include "esp_log.h"

#define BEEP_PIN  1

void beep_init(void)
{
    gpio_config_t io_conf = {
        .mode = GPIO_MODE_OUTPUT,
        .pin_bit_mask = 1ULL << BEEP_PIN
    };
    gpio_config(&io_conf);

    gpio_set_level(BEEP_PIN, 0);
}

void beep_long(void)
{
	gpio_set_level(BEEP_PIN, 1);
	vTaskDelay(500 / portTICK_PERIOD_MS);
	gpio_set_level(BEEP_PIN, 0);
}

void beep_short(void)
{
	gpio_set_level(BEEP_PIN, 1);
	vTaskDelay(200 / portTICK_PERIOD_MS);
	gpio_set_level(BEEP_PIN, 0);
}

void beep_unlock(void)
{
	beep_short();

	vTaskDelay(400 / portTICK_PERIOD_MS);

	beep_long();
}

void beep_lock(void)
{
	beep_long();

	vTaskDelay(400 / portTICK_PERIOD_MS);

	beep_short();
}

void beep_power(void)
{
	beep_long();

	vTaskDelay(400 / portTICK_PERIOD_MS);

	beep_short();

	vTaskDelay(400 / portTICK_PERIOD_MS);

	beep_long();
}

void beep_mode(uint8_t mode)
{
	switch (mode) {
		case 1:
			beep_unlock();
			break;
		case 2:
			beep_lock();
			break;
		case 3:
			beep_power();
			break;
		default:
			break;	
	}
}


