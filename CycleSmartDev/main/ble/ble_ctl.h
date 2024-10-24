#ifndef _BLE_CTL_H_
#define _BLE_CTL_H_

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

struct ble_info {
    bool is_connected;
    bool notify_state;
};

/* Attributes State Machine */
enum
{
    IDX_SVC,
    IDX_CHAR_A,
    IDX_CHAR_VAL_A,
    IDX_CHAR_CFG_A,

    IDX_CHAR_B,
    IDX_CHAR_VAL_B,

    IDX_CHAR_C,
    IDX_CHAR_VAL_C,

    HRS_IDX_NB,
};

void ble_ctl_init(void);
void notify_state_to_app(int notify_state);
void set_state_to_gatt(int state);
void ble_get_state_from_gatt(void);
#endif