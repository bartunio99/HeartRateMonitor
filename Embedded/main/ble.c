#include <stdint.h>
#include <string.h>
#include <stdbool.h>
#include <stdio.h>
#include "nvs.h"
#include "nvs_flash.h"

#include "esp_bt.h"
#include "esp_gap_ble_api.h"
#include "esp_gattc_api.h"
#include "esp_gatt_defs.h"
#include "esp_bt_main.h"
#include "esp_gatt_common_api.h"
#include "esp_log.h"
#include "freertos/FreeRTOS.h"

#define GATTC_TAG "Pulsometr"
#define REMOTE_SERVICE_UUID        0x00FF
#define REMOTE_NOTIFY_CHAR_UUID    0xFF01
#define PROFILE_NUM      1              //ile profili
#define PROFILE_A_APP_ID 0
#define INVALID_HANDLE   0
#define DEVICE_NAME "Pulsometr"  // Nazwa reklamowanego urządzenia

static const char remote_device_name[] = "Pulsometr";

static void gap_callback(esp_gap_ble_cb_event_t event, esp_ble_gap_cb_param_t *param);

//uuid uslugi
static uint8_t service_uuid[16] = {
    0x34, 0x12, 0xB1, 0xA3, 0xC1, 0x23, 0x45, 0x67,
    0x89, 0xAB, 0xCD, 0xEF, 0x01, 0x23, 0x45, 0x67
};

//struktura do profilu gatt
struct gattc_profile_inst {
    esp_gattc_cb_t gattc_cb;
    uint16_t gattc_if;
    uint16_t app_id;
    uint16_t conn_id;
    uint16_t service_start_handle;
    uint16_t service_end_handle;
    uint16_t char_handle;
    esp_bd_addr_t remote_bda;
};

//parametry reklamowania
esp_ble_adv_params_t adv_params = {
        .adv_int_min = 0x20,
        .adv_int_max = 0x40,
        .adv_type = ADV_TYPE_IND,
        .own_addr_type = BLE_ADDR_TYPE_PUBLIC,
        .channel_map = ADV_CHNL_ALL,
        .adv_filter_policy = ADV_FILTER_ALLOW_SCAN_ANY_CON_ANY,
    };

//dane reklamowe urzadzenia
esp_ble_adv_data_t adv_data = {
    .set_scan_rsp = false,
    .include_name = true,
    .include_txpower = true,
    .min_interval = 0x100,
    .max_interval = 0x200,
    .appearance = 0x00,
    .manufacturer_len = 0,
    .service_data_len = 0,
    .service_uuid_len = sizeof(service_uuid),
    .p_service_uuid = service_uuid,
    .p_manufacturer_data = NULL,
    .p_service_data = NULL,
};



static void gap_callback(esp_gap_ble_cb_event_t event, esp_ble_gap_cb_param_t *param){

    switch (event) {

        case ESP_GAP_BLE_ADV_DATA_SET_COMPLETE_EVT:
            ESP_LOGI("BLE", "Advertising data set complete.");
            
            break;
        case ESP_GAP_BLE_ADV_START_COMPLETE_EVT:
            ESP_LOGI("BLE", "Advertising started.");
            break;
        case ESP_GAP_BLE_ADV_STOP_COMPLETE_EVT:
            ESP_LOGI("BLE", "Advertising stopped.");
            break;
        default:
            break;
    }

}

void bt_transmission(){

    //inicjalizacja nieulotnej pamieci flash - do zapisywania ssid i hasla np
    esp_err_t ret  = nvs_flash_init();
    if(ret == ESP_ERR_NVS_NO_FREE_PAGES || ret == ESP_ERR_NVS_NEW_VERSION_FOUND){       
        ESP_ERROR_CHECK(nvs_flash_erase());     //zwalnia pamiec
        ret = nvs_flash_init();
    }
    ESP_ERROR_CHECK(ret);

    ESP_ERROR_CHECK(esp_bt_controller_mem_release(ESP_BT_MODE_CLASSIC_BT));

    //inicjalizacja kontrolera bt - warstwa lacza danych - niewidoczny dla innych urzadzen
    esp_bt_controller_config_t bt_cft = BT_CONTROLLER_INIT_CONFIG_DEFAULT();
    ret = esp_bt_controller_init(&bt_cft);
    if(ret){
        ESP_LOGE(GATTC_TAG, "%s inicjalizacja modulu bt nieudana, kod bledu = %x", __func__, ret);
        return;
    }

    //wlaczenie modulu bt jako ble
    ret = esp_bt_controller_enable(ESP_BT_MODE_BLE);
    if(ret){
        ESP_LOGE(GATTC_TAG, "%s wlaczenie modulu bt nieudane, kod bledu = %x", __func__, ret);
        return;
    }

    //inicjalizacja bluedroid
    ret = esp_bluedroid_init();
    if(ret){
        ESP_LOGE(GATTC_TAG, "%s inicjalizacja bluedraid nieudana, kod bledu = %x", __func__, ret);
        return;
    }

    //wlaczenie bluedroid
    ret = esp_bluedroid_enable();
    if(ret){
        ESP_LOGE(GATTC_TAG, "%s wlaczenie bluedroid nieudane, kod bledu = %x", __func__, ret);
        return;
    }

    //nadanie nazwy urzadzenia
    ret = esp_ble_gap_set_device_name(DEVICE_NAME);
        if(ret != ESP_OK){
        ESP_LOGE(GATTC_TAG, "%s nazwanie modulu nieudane, kod bledu = %x", __func__, ret);
        return;
    }

    //rejestracja profilu gap
    ret = esp_ble_gap_register_callback(gap_callback);
    if(ret){
        ESP_LOGE(GATTC_TAG, "%s rejestracja gap nieudana, kod bledu = %x", __func__, ret);
        return;
    }

    ret = esp_ble_gap_start_advertising(&adv_params);
    if(ret != ESP_OK){
        ESP_LOGE(GATTC_TAG, "Failed to configure advertising data: %s", esp_err_to_name(ret));
    }
}

