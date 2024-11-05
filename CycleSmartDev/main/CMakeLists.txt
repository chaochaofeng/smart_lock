file(GLOB_RECURSE rc522srcs rc522/*.c)
file(GLOB_RECURSE keysrcs key/*.c)
file(GLOB_RECURSE blesrcs ble/*.c)
file(GLOB_RECURSE beepsrcs beep/*.c)

idf_component_register(SRCS "smart-lock.c" 
                        "${rc522srcs}" "${keysrcs}" "${blesrcs}" "${beepsrcs}"
                    INCLUDE_DIRS "." "rc522" "key" "ble" "beep"
                    REQUIRES bt nvs_flash driver)
