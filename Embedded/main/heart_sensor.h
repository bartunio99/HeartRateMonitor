#include <stdio.h>
#include <stdint.h> 

#ifndef HEART_SENSOR_H
#define HEART_SENSOR_H

#define ADC_CHANNEL ADC1_CHANNEL_6    //gpio34
#define BIT_WIDTH ADC_WIDTH_BIT_12    //12bit range - 0-4095
#define ADC_ATT ADC_ATTEN_DB_12         //12dB decrease

void analog_transmission();
uint16_t returnPulse();

#endif