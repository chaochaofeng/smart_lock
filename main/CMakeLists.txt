file(GLOB_RECURSE rc522srcs rc522/*.c)

idf_component_register(SRCS "smart-lock.c" "${rc522srcs}"
                    INCLUDE_DIRS "." "rc522"
                    REQUIRES bt nvs_flash driver)
