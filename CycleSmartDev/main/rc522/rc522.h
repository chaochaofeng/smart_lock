#ifndef _RC522_H
#define _RC522_H

#include "driver/gpio.h"

#define MF522_RST_PIN                    GPIO_NUM_8
  
#define MF522_MISO_PIN                   GPIO_NUM_2
#define MF522_MOSI_PIN                   GPIO_NUM_7

#define MF522_SCK_PIN                    GPIO_NUM_6
#define MF522_NSS_PIN                    GPIO_NUM_10

#define LED_PIN                          GPIO_NUM_12  

#define RST_H                            gpio_set_level(MF522_RST_PIN, 1)
#define RST_L                            gpio_set_level(MF522_RST_PIN, 0)

#define MOSI_H                           gpio_set_level(MF522_MOSI_PIN, 1)
#define MOSI_L                           gpio_set_level(MF522_MOSI_PIN, 0)

#define SCK_H                            gpio_set_level(MF522_SCK_PIN, 1)
#define SCK_L                            gpio_set_level(MF522_SCK_PIN, 0)

#define NSS_H                            gpio_set_level(MF522_NSS_PIN, 1)
#define NSS_L                            gpio_set_level(MF522_NSS_PIN, 0)

#define READ_MISO                        gpio_get_level(MF522_MISO_PIN)

#define LED_ON                           gpio_set_level(LED_PIN, 1)
#define LED_OFF                          gpio_set_level(LED_PIN, 0)

#endif
