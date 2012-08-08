/*
 * Copyright (c) 2012, Swedish Institute of Computer Science.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 */ 


/**
 *		\file
 *						collect_view_arch_read_sensors function for AR1000 sensor board
 *
 *		\author	 
 *						Eloy Díaz 	  
 */
#include "collect-view.h"
#include "dev/cc2420.h"
#include "dev/leds.h"
#include "dev/battery-sensor.h"
#include "dev/AR1000.h"
#include "io.h"

enum{
	CM5000,
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

  	SENSORS_ACTIVATE(ar1000);
 		SENSORS_ACTIVATE(battery_sensor);
		printf ("Reading sensors\n");

		msg->sensors[BATTERY_VOLTAGE_SENSOR] = battery_sensor.value(0);
  	msg->sensors[CO_SENSOR] = ar1000.value(SENSOR_CO);
		msg->sensors[CO2_SENSOR] = ar1000.value(SENSOR_CO2);
		msg->sensors[DUST_SENSOR] = ar1000.value(SENSOR_DUST);
		msg->sensors[SENSOR_BOARD] = AR1000;
		
		SENSORS_DEACTIVATE(ar1000);
  	SENSORS_DEACTIVATE(battery_sensor);

}




