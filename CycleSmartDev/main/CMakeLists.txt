file(GLOB_RECURSE rc522srcs rc522/*.c)
file(GLOB_RECURSE keysrcs key/*.c)
file(GLOB_RECURSE blesrcs ble/*.c)

idf_component_register(SRCS "smart-lock.c" 
                        "${rc522srcs}" "${keysrcs}" "${blesrcs}"
                    INCLUDE_DIRS "." "rc522" "key" "ble"
                    REQUIRES bt nvs_flash driver)
