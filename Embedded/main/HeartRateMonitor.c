#include "ble.c"
#include <stdio.h>
#include "driver/adc.h"
#include "esp_adc/adc_oneshot.h"
#include "esp_adc/adc_continuous.h"
#include "esp_adc/adc_cali.h"
#include "esp_adc/adc_cali_scheme.h"
#include "soc/adc_channel.h"
#include "esp_log.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/queue.h"


#define ADC_CHANNEL ADC1_CHANNEL_6    //gpio34
#define BIT_WIDTH ADC_WIDTH_BIT_12    //12bit range - 0-4095
#define ADC_ATT ADC_ATTEN_DB_12         //12dB decrease

void analog_transmission(); //operates analog signal from sensor
void calibrate_adc();       //adc driver calibration

adc_cali_handle_t *handle;

void app_main(void)
{
    //xTaskCreate(analog_transmission, "transmisja", 2048, NULL, 5, NULL);
    bt_transmission();
}


//TODO MECHANIZMY SYNCHRONIZACJI WATKOW
void analog_transmission(){

    //set adc parameters
    adc1_config_width(ADC_WIDTH_BIT_12);
    adc1_config_channel_atten(ADC_CHANNEL, ADC_ATT);

    //main loop - read values and wait
    while(1){
        int data = adc1_get_raw(ADC_CHANNEL);
        printf("Puls: %d\n", data);
        vTaskDelay(10);
    }

}


