 /**
 *		\file
 *						Test program and usage example of the AR1000 driver
 * 						
 *
 *		\author	 
 *						Eloy Díaz 	  
 */
#include "contiki.h"
#include "lib/sensors.h"
#include "dev/sky-sensors.h"
#include "dev/AR1000.h"
#include <stdio.h> 

void print_values();

/*---------------------------------------------------------------------------*/
PROCESS(ar1000_test, "AR1000 Test");
AUTOSTART_PROCESSES(&ar1000_test);
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(ar1000_test, ev, data)
{
  PROCESS_BEGIN();

	static struct etimer et;
	static int i=0;
	
	printf ("\n\nrunning SENSORS_ACTIVATE(ar1000). Values should slightly change\n");
  SENSORS_ACTIVATE(ar1000);
	while (i<20)	{
		etimer_set(&et, CLOCK_SECOND * 1);
  	PROCESS_WAIT_UNTIL(etimer_expired(&et));
		print_values();
		i++;
	}i=0;

	printf ("\n\nrunning SENSORS_DEACTIVATE(ar1000). Values should be fixed now\n");
  SENSORS_DEACTIVATE(ar1000);
	while (i<10)	{
		etimer_set(&et, CLOCK_SECOND * 1);
  	PROCESS_WAIT_UNTIL(etimer_expired(&et));
		print_values();
		i++;
	}i=0;

	printf ("\n\nrunning SENSORS_ACTIVATE(ar1000). Values should slightly change\n");
  SENSORS_ACTIVATE(ar1000);
	while (i<20)	{
		etimer_set(&et, CLOCK_SECOND * 1);
  	PROCESS_WAIT_UNTIL(etimer_expired(&et));
		print_values();
		i++;
	}i=0;

	printf ("\nTest complete\n");
  PROCESS_END();
	 
}

void print_values(){
		printf("CO2:%u,DUST:%u,CO:%u\n", \
		ar1000.value(SENSOR_CO2), \
		ar1000.value(SENSOR_DUST), \
		ar1000.value(SENSOR_CO)); 
}
/*---------------------------------------------------------------------------*/
