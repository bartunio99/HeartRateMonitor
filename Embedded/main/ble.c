#include <stdint.h>
#include <string.h>
#include <stdbool.h>
#include <stdio.h>
#include "nvs.h"
#include "nvs_flash.h"

#include "esp_bt_main.h"
#include "esp_gatt_common_api.h"
#include "esp_log.h"
#include "freertos/FreeRTOS.h"
#include <inttypes.h>

#include "heart_sensor.h"
#include "heart_sensor.c"
#include "ble.h"

uint16_t heart_rate_service_uuid = 0x180D;
uint16_t heart_rate_char_uuid = 0x2A37;
esp_gatt_char_prop_t  heart_chart_prop = ESP_GATT_CHAR_PROP_BIT_READ | ESP_GATT_CHAR_PROP_BIT_NOTIFY;


//uuid uslugi
static uint8_t service_uuid[16] = {
    0x34, 0x12, 0xB1, 0xA3, 0xC1, 0x23, 0x45, 0x67,
    0x89, 0xAB, 0xCD, 0xEF, 0x01, 0x23, 0x45, 0x67
};


//struktura do profilu gatt
struct gatts_profile_inst {
    esp_gatts_cb_t gatts_cb;
    uint16_t gatts_if;
    uint16_t app_id;
    uint16_t conn_id;
    uint16_t service_handle;
    esp_gatt_srvc_id_t service_id;
    uint16_t char_handle;
    esp_bt_uuid_t char_uuid;
    esp_gatt_perm_t perm;
    esp_gatt_char_prop_t property;
    uint16_t descr_handle;
    esp_bt_uuid_t descr_uuid;
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
static esp_ble_adv_data_t adv_data = {
    .set_scan_rsp = false,
    .include_name = true,
    .include_txpower = false,
    .min_interval = 0x0006, //slave connection min interval, Time = min_interval * 1.25 msec
    .max_interval = 0x0010, //slave connection max interval, Time = max_interval * 1.25 msec
    .appearance = 0x00,
    .manufacturer_len = 0, //TEST_MANUFACTURER_DATA_LEN,
    .p_manufacturer_data =  NULL, //&test_manufacturer[0],
    .service_data_len = 0,
    .p_service_data = NULL,
    .service_uuid_len = sizeof(service_uuid),
    .p_service_uuid = service_uuid,
    .flag = (ESP_BLE_ADV_FLAG_GEN_DISC | ESP_BLE_ADV_FLAG_BREDR_NOT_SPT),
};

//profil gatt
static struct gatts_profile_inst gl_profile_tab[PROFILE_NUM] = {
    [PROFILE_A_APP_ID] = {
        .gatts_cb = gatt_profile_event_handler,
        .gatts_if = ESP_GATT_IF_NONE,       /* Not get the gatt_if, so initial is ESP_GATT_IF_NONE */
    },
};

static esp_attr_value_t ble_characteristics =
{
    .attr_max_len = sizeof(pulse),
    .attr_len     = sizeof(pulse),
    .attr_value   = (uint8_t*)&pulse
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

static void gatt_callback(esp_gatts_cb_event_t event, esp_gatt_if_t gatts_if, esp_ble_gatts_cb_param_t *param){

    //sprawdzenie czy gatt zostal zarejestrowany
    if (event == ESP_GATTS_REG_EVT) {
        if (param->reg.status == ESP_GATT_OK) {
            gl_profile_tab[param->reg.app_id].gatts_if = gatts_if;
        } else {
            ESP_LOGI(APP_TAG, "Reg app failed, app_id %04x, status %d",
                    param->reg.app_id,
                    param->reg.status);
            return;
        }
    }

    //wywolywanie callbacka wybranego profilu

    int idx;
    for (idx = 0; idx < PROFILE_NUM; idx++) {
        if (gatts_if == ESP_GATT_IF_NONE || gatts_if == gl_profile_tab[idx].gatts_if) {
            if (gl_profile_tab[idx].gatts_cb) {
                gl_profile_tab[idx].gatts_cb(event, gatts_if, param);
            }
        }

    
}}

static void gatt_profile_event_handler(esp_gatts_cb_event_t event, esp_gatt_if_t gatts_if, esp_ble_gatts_cb_param_t *param){
    esp_err_t ret;

    switch (event)
    {
        //rejestracja profilu
        case ESP_GATTS_REG_EVT:
            ESP_LOGI(APP_TAG, "REG_EVT");
            gl_profile_tab[PROFILE_A_APP_ID].service_id.is_primary = true;
            gl_profile_tab[PROFILE_A_APP_ID].service_id.id.inst_id = 0x00;
            gl_profile_tab[PROFILE_A_APP_ID].service_id.id.uuid.len = ESP_UUID_LEN_16;
            gl_profile_tab[PROFILE_A_APP_ID].service_id.id.uuid.uuid.uuid16 = heart_service_uuid;

            esp_ble_gap_set_device_name(DEVICE_NAME);
            ret = esp_ble_gap_config_adv_data(&adv_data);
            if (ret){
                ESP_LOGE(APP_TAG, "config adv data failed, error code = %x", ret);
            }

            ret = esp_ble_gap_start_advertising(&adv_params);
            esp_ble_gatts_create_service(gatts_if, &gl_profile_tab[PROFILE_A_APP_ID].service_id, 4);
            if(ret != ESP_OK){
            ESP_LOGE(APP_TAG, "Failed to configure advertising data: %s", esp_err_to_name(ret));
            }

            break;

        case ESP_GATTS_CONNECT_EVT:
            ret = esp_ble_gap_stop_advertising();
            if(ret){
                ESP_LOGE(APP_TAG, "%s zatrzymanie reklamowania nieudane, kod bledu = %x", __func__, ret);
            }
            ESP_LOGI(APP_TAG, "Polaczono z: %d", param->connect.conn_id);

            //zmiana paramterow polaczenia
            esp_ble_conn_update_params_t conn_params = {0};
            memcpy(conn_params.bda, param->connect.remote_bda, sizeof(esp_bd_addr_t));
            /* For the IOS system, please reference the apple official documents about the ble connection parameters restrictions. */
            conn_params.latency = 0;
            conn_params.max_int = 0x30;    // max_int = 0x30*1.25ms = 40ms
            conn_params.min_int = 0x10;    // min_int = 0x10*1.25ms = 20ms
            conn_params.timeout = 400;     // timeout = 400*10ms = 4000ms
            ESP_LOGI(APP_TAG, "ESP_GATTS_CONNECT_EVT, conn_id %d, remote %02x:%02x:%02x:%02x:%02x:%02x:",
                    param->connect.conn_id,
                    param->connect.remote_bda[0],
                    param->connect.remote_bda[1],
                    param->connect.remote_bda[2],
                    param->connect.remote_bda[3],
                    param->connect.remote_bda[4],
                    param->connect.remote_bda[5]
            );
            gl_profile_tab[PROFILE_A_APP_ID].conn_id = param->connect.conn_id;
            //start sent the update connection parameters to the peer device.
	        esp_ble_gap_update_conn_params(&conn_params);
            break;
        
        case ESP_GATTS_DISCONNECT_EVT:
            ESP_LOGI(APP_TAG, "ESP_GATTS_DISCONNECT_EVT, reason 0x%x", param->disconnect.reason);
            esp_ble_gap_start_advertising(&adv_params);  // Wznowienie reklamowania po rozłączeniu
            break;
           
        //utworzenie uslugi
        case ESP_GATTS_CREATE_EVT:
            ESP_LOGI(APP_TAG, "CREATE_SERVICE_EVT, status %d, service_handle %d", param->create.status, param->create.service_handle);
            gl_profile_tab[PROFILE_A_APP_ID].service_handle = param->create.service_handle;
            gl_profile_tab[PROFILE_A_APP_ID].char_uuid.len = ESP_UUID_LEN_16;
            gl_profile_tab[PROFILE_A_APP_ID].char_uuid.uuid.uuid16 = heart_char_uuid;

            esp_ble_gatts_start_service(gl_profile_tab[PROFILE_A_APP_ID].service_handle);

            esp_gatt_char_prop_t a_property = ESP_GATT_CHAR_PROP_BIT_READ | ESP_GATT_CHAR_PROP_BIT_WRITE | ESP_GATT_CHAR_PROP_BIT_NOTIFY;
            esp_err_t add_char_ret =
            esp_ble_gatts_add_char(gl_profile_tab[PROFILE_A_APP_ID].service_handle,
                                    &gl_profile_tab[PROFILE_A_APP_ID].char_uuid,
                                    ESP_GATT_PERM_READ | ESP_GATT_PERM_WRITE,
                                    a_property,
                                    &ble_characteristics,
                                    NULL);
            if (add_char_ret){
                ESP_LOGE(APP_TAG, "add char failed, error code =%x",add_char_ret);
            }

            break;

        //odczyt
        case ESP_GATTS_ADD_CHAR_EVT: {
            uint16_t length = 0;
            const uint8_t *prf_char;

            ESP_LOGI(APP_TAG, "ADD_CHAR_EVT, status %d,  attr_handle %d, service_handle %d",
                    param->add_char.status, param->add_char.attr_handle, param->add_char.service_handle);
            gl_profile_tab[PROFILE_A_APP_ID].char_handle = param->add_char.attr_handle;
            gl_profile_tab[PROFILE_A_APP_ID].descr_uuid.len = ESP_UUID_LEN_16;
            gl_profile_tab[PROFILE_A_APP_ID].descr_uuid.uuid.uuid16 = ESP_GATT_UUID_CHAR_CLIENT_CONFIG;
            esp_err_t get_attr_ret = esp_ble_gatts_get_attr_value(param->add_char.attr_handle, &length, &prf_char);
            if (get_attr_ret == ESP_FAIL){
                ESP_LOGE(APP_TAG, "ILLEGAL HANDLE");
            }
            ESP_LOGI(APP_TAG, "the gatts demo char length = %x", length);
            for(int i = 0; i < length; i++){
                ESP_LOGI(APP_TAG, "prf_char[%x] = %x",i,prf_char[i]);
            }
            esp_err_t add_descr_ret = esp_ble_gatts_add_char_descr(
                                    gl_profile_tab[PROFILE_A_APP_ID].service_handle,
                                    &gl_profile_tab[PROFILE_A_APP_ID].descr_uuid,
                                    ESP_GATT_PERM_READ | ESP_GATT_PERM_WRITE,
                                    NULL,NULL);
            if (add_descr_ret){
                ESP_LOGE(APP_TAG, "add char descr failed, error code = %x", add_descr_ret);
            }
            break;
        }

        case ESP_GATTS_ADD_CHAR_DESCR_EVT:
            ESP_LOGI(APP_TAG, "ADD_DESCR_EVT, status %d, attr_handle %d, service_handle %d",
                    param->add_char.status, param->add_char.attr_handle,
                    param->add_char.service_handle);
            break;
        
        case ESP_GATTS_READ_EVT:
            ESP_LOGI(APP_TAG, "GATT_READ_EVT, conn_id %d, trans_id %d, handle %d",
                param->read.conn_id, param->read.trans_id, param->read.handle);
            esp_gatt_rsp_t rsp;
            memset(&rsp, 0, sizeof(esp_gatt_rsp_t));
            
            pulse = returnPulse();
            uint8_t p1, p2;
            p1 = (pulse&0xFF);
            p2 = (pulse >> 8) & 0xFF;
            rsp.attr_value.handle = param->read.handle;
            uint8_t data[2];
            data[0] = p2;
            data[1] = p1;
            rsp.attr_value.len = sizeof(data);  // Ustaw długość na 2
            memcpy(rsp.attr_value.value, data, sizeof(data));  // Skopiuj dane

            esp_ble_gatts_send_response(gatts_if,
                            param->read.conn_id,
                            param->read.trans_id,
                            ESP_GATT_OK, &rsp);

            ESP_LOGI(APP_TAG, "Sending response: 0x%02X 0x%02X", data[0], data[1]);

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
        ESP_LOGE(APP_TAG, "%s inicjalizacja modulu bt nieudana, kod bledu = %x", __func__, ret);
        return;
    }

    //wlaczenie modulu bt jako ble
    ret = esp_bt_controller_enable(ESP_BT_MODE_BLE);
    if(ret){
        ESP_LOGE(APP_TAG, "%s wlaczenie modulu bt nieudane, kod bledu = %x", __func__, ret);
        return;
    }

    //inicjalizacja bluedroid
    ret = esp_bluedroid_init();
    if(ret){
        ESP_LOGE(APP_TAG, "%s inicjalizacja bluedraid nieudana, kod bledu = %x", __func__, ret);
        return;
    }

    //wlaczenie bluedroid
    ret = esp_bluedroid_enable();
    if(ret){
        ESP_LOGE(APP_TAG, "%s wlaczenie bluedroid nieudane, kod bledu = %x", __func__, ret);
        return;
    }

    //rejestracja profilu gap
    ret = esp_ble_gap_register_callback(gap_callback);
    if(ret){
        ESP_LOGE(APP_TAG, "%s rejestracja gap nieudana, kod bledu = %x", __func__, ret);
        return;
    }

    //rejestracja profilu gatt
    ret = esp_ble_gatts_register_callback(gatt_callback);
    if(ret){
        ESP_LOGE(APP_TAG, "%s rejestracja gatts nieudana, kod bledu = %x", __func__, ret);
        return;
    }

    //profil aplikacji
    ret = esp_ble_gatts_app_register(PROFILE_A_APP_ID);
    if(ret){
        ESP_LOGE(APP_TAG, "%s rejestracja gatts nieudana, kod bledu = %x", __func__, ret);
        return;
    }

    //ustawienie mtu - maximum transmission unit
    esp_err_t local_mtu_ret = esp_ble_gatt_set_local_mtu(500);
    if(local_mtu_ret){
        ESP_LOGE(APP_TAG, "%s ustawienie MTU nieudane, kod bledu = %x", __func__, ret);
        return;
    }

}

