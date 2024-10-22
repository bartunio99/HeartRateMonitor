#include <stdio.h>
#include "driver/adc.h"
#include "esp_adc_cal.h"
#include "soc/adc_channel.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/queue.h"


#define ADC_CHANNEL ADC1_CHANNEL_6    //gpio34
#define BIT_WIDTH ADC_WIDTH_BIT_12    //12bit range - 0-4095
#define ADC_ATT ADC_ATTEN_DB_12         //12dB decrease

void analog_transmission();
void pair_device();
void bt_transmission();

void app_main(void)
{
    xTaskCreate(analog_transmission, "transmisja", 2048, NULL, 5, NULL);
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

