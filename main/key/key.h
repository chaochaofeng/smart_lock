#ifndef _KEY_H
#define _KEY_H

enum {
    KEY_UNLOCK = 1,
    KEY_LOCK,
    KEY_POWER,
};

void key_init(void);
void set_unlock(void);
void set_lock(void);
void set_power(void);
int get_lock_state(void);

#endif
