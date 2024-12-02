#include "heart_sensor.h"
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

uint16_t pulse;

void analog_transmission(){

    //set adc parameters
    adc1_config_width(ADC_WIDTH_BIT_12);
    adc1_config_channel_atten(ADC_CHANNEL, ADC_ATT);

    //main loop - read values and wait
    while(1){
        uint16_t data = adc1_get_raw(ADC_CHANNEL);
        pulse = data;
        //printf("Puls: %d\n", data);
        vTaskDelay(10);
    }

}

uint16_t returnPulse(){
    return pulse;
}