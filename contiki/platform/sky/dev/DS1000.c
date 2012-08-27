/*
 * Copyright (c) 2005-2012, Swedish Institute of Computer Science
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
 * 
 */


 /**
 *		\file
 *						Contiki driver for the attachable sky sensor board DS1000.
 * 						Check ~/contiki/platform/sky/dev/sky-sensors.c to see how the 
 *						ADC12 is configured.
 *
 *		\author	 
 *						Eloy Díaz 	  
 */

#include "contiki.h"
#include "lib/sensors.h"
#include "dev/sky-sensors.h"
#include "dev/DS1000.h"

#include <io.h>

/**
* SH-300-DC ---> ADC3  (CO2)
* PSX-01E   ---> ADC4  (Temperature)
* GS-02A    ---> ADC5  (CO)
*/
#define INPUT_CHANNEL      ((1 << INCH_3) | (1 << INCH_4) | (1 << INCH_5))

/**
* Voltage reference ~ 2.5
* See MSP420 User's guide and sky-sensors.c
* for more details.
*/
#define INPUT_REFERENCE     SREF_1

#define CO2_MEM		    ADC12MEM3
#define TEMP_MEM  	  ADC12MEM4
#define CO_MEM		    ADC12MEM5

const struct sensors_sensor ds1000;
/*---------------------------------------------------------------------------*/
static int
value(int type)
{
  switch(type) {
	  case SENSOR_CO2:
	    return CO2_MEM;
	  case SENSOR_TEMP:
	    return TEMP_MEM;
	  case SENSOR_CO:
	    return CO_MEM;
  }
  return 0;
}
/*---------------------------------------------------------------------------*/
static int
status(int type)
{
  return sky_sensors_status(INPUT_CHANNEL, type);
}
/*---------------------------------------------------------------------------*/
static int
configure(int type, int c)
{
	return sky_sensors_configure(INPUT_CHANNEL, INPUT_REFERENCE, type, c);
}
/*---------------------------------------------------------------------------*/
SENSORS_SENSOR(ds1000, "ds1000", value, configure, status);
