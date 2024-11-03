#include "esp_bt.h"
#include "esp_gap_ble_api.h"
#include "esp_gatts_api.h"
#include "esp_gatt_defs.h"

#ifndef BLE_H
#define BLE_H

#define APP_TAG "Pulsometr"
#define PROFILE_NUM      1              //ile profili
#define PROFILE_A_APP_ID 0
#define DEVICE_NAME "Pulsometr"  // Nazwa reklamowanego urządzenia
#define heart_service_uuid 0x180D
#define heart_char_uuid 0x2A37
#define GATTS_DEMO_CHAR_VAL_LEN_MAX 0x40

static void gap_callback(esp_gap_ble_cb_event_t event, esp_ble_gap_cb_param_t *param);
static void gatt_callback(esp_gatts_cb_event_t event, esp_gatt_if_t gatts_if, esp_ble_gatts_cb_param_t *param);
static void gatt_profile_event_handler(esp_gatts_cb_event_t event, esp_gatt_if_t gatts_if, esp_ble_gatts_cb_param_t *param);
void bt_transmission();

#endif