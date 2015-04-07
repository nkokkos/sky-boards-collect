**[CollectView](https://github.com/contiki-os/contiki/tree/master/tools/collect-view)** is a Contiki OS Java tool that makes possible to program and deploy a Contiki wireless sensor network and collect loads of information without even having to start programming. This project adds some new features.

For further information about Contiki, please refer to Contiki [website](http://www.contiki-os.org/) and [wiki](http://wiki.contiki-os.org/).

# Features #

  * Extended application design to allow support for any (defined) platform so that all defined platforms can work together on same network with Contiki OS firmware. Platform definition is straightforward. Currently defined platforms:
    * Tmote sky
    * AR1000 (CO, CO<sub>2</sub> & dust particle concentration sensorboard)
    * DS1000 (Temperature, CO & CO<sub>2</sub> sensorboard)

  * Data feeding to cosm and sense.
  * Software calibration tool ([this wiki page](http://code.google.com/p/sky-boards-collect/wiki/AdjustConversionExpresion) explains the concept).
  * Last values visualization on sensor map.
  * Load any firmware to motes (placed under /firmware folder).

[Screenshots](http://code.google.com/p/sky-boards-collect/wiki/Screenshots) can be found on Wiki pages.

[Here](http://code.google.com/p/sky-boards-collect/source/browse/trunk/contiki/examples/sky-boards-collect/cooja-test/) is a Cooja example.


# Install #

  1. [Download](https://sky-boards-collect.googlecode.com/files/sky-boards-collect-1.6b.zip) zip file
  1. Unzip to some directory
  1. Open command prompt/Unix shell, cd to application directory and type in:
```
java -jar sky-boards-collect.jar
```

If you encounter any problem programming or connecting to nodes, then try:
  * Check that files in application directory `/tools` are set as executable
  * Run application as admin/root


# Drivers and Firmware #

Because of Contiki OS great driver interface design, writing a new driver for sky platform is pretty much straightforward if one doesn't want to change ADC12 configuration (sky-sensors.c)

### AR1000, DS1000 and CO1000 Sensor board Contiki OS Driver ###
Drivers source files have been added to Downloads. Follow README instructions. After installation, please run test program to check that everything is OK. One can see how to use the driver in test source file.

CO1000 driver has not been tested on CO1000 Hardware, but it will probably work if data sheet is correct.

sky-boards-collect doesn't support CO1000 sensors for now.

### Firmware ###

  * http://code.google.com/p/sky-boards-collect/source/browse/trunk/contiki/examples/sky-boards-collect/collect-ar1000/

  * http://code.google.com/p/sky-boards-collect/source/browse/trunk/contiki/examples/sky-boards-collect/collect-ds1000/


# Roadmap #

  * Define EM1000, SE1000, CO1000 sensorboards.

# About #

This work was part of a final year project about WSN - Contiki OS - `CollectView`