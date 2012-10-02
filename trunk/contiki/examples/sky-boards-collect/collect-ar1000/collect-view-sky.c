 /**
 *   \file
 *        collect_view_arch_read_sensors implementation for AR1000 sensorboard
 *
 *   \author	 
 *        Eloy Díaz <eldial@gmail.com>  
 */

#include "collect-view.h"
#include "cc2420.h"
#include "leds.h"
#include "battery-sensor.h"
#include "AR1000.h"

#define MAX_WAIT 1000
enum{
  SKY,
  AR1000,
  DS1000,
};

enum {
  BATTERY_VOLTAGE_SENSOR,
  BATTERY_INDICATOR,
  CO_SENSOR,
  CO2_SENSOR,  
  DUST_SENSOR,
  RSSI_SENSOR,
  ETX1_SENSOR,
  ETX2_SENSOR,
  ETX3_SENSOR,
  SENSOR_BOARD,
};

/*---------------------------------------------------------------------------*/
void
collect_view_arch_read_sensors(struct collect_view_data_msg *msg)
{

  static int i = 0;
  static int j = 0;

  SENSORS_ACTIVATE(ar1000);
  SENSORS_ACTIVATE(battery_sensor);
  while (!ar1000.status(SENSORS_READY) && i<MAX_WAIT) 
    i++;
  while (!battery_sensor.status(SENSORS_READY) && j<MAX_WAIT) 
    j++;

  msg->sensors[BATTERY_VOLTAGE_SENSOR] = battery_sensor.value(0);
  msg->sensors[CO_SENSOR] = ar1000.value(SENSOR_CO);
  msg->sensors[CO2_SENSOR] = ar1000.value(SENSOR_CO2);
  msg->sensors[DUST_SENSOR] = ar1000.value(SENSOR_DUST);
  msg->sensors[SENSOR_BOARD] = AR1000;
	
  SENSORS_DEACTIVATE(ar1000);
  SENSORS_DEACTIVATE(battery_sensor);
}
