#include "ble.c"
#include "ble.h"
#include "heart_sensor.h"
#include <stdio.h>

#include "esp_log.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/queue.h"

//todo synchronizacja watkow i ogarniecie burdelu w ble.c oraz headery
void app_main(void)
{
    bt_transmission();
    xTaskCreate(analog_transmission, "transmisja", 2048, NULL, 5, NULL);
}
