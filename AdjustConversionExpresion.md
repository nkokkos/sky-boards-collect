# Introduction #

An example on how to calibre DS1000.Temperature sensor using the _adjust conversion expressions tool_.



# Step 1 #

First we must have a **reference device** that we know it shows the right temperature, for example our home thermometer. In this example, **real value is 30º.**

So, we start a network collect, and our temperature values from node 3 are not the same as our reference device:

![https://sky-boards-collect.googlecode.com/svn/wiki/img/imgcal1.jpg](https://sky-boards-collect.googlecode.com/svn/wiki/img/imgcal1.jpg)


# Step 2 #

We need to know **ADC\_VALUE**. This is the output from ADC12 (Analog-to-digital 12 bit converter) that a node sends to application, and represents analog voltage from the sensor converted by ADC12 to a digital representation. Then within the Java application, conversion formulas are applied to this value to obtain the real physical magnitude. Conversion formulas are very related to sensor/ADC12 circuit properties. The adc\_value is shown in the tool's dialog:

# Step 3 #

**In this example adc\_value was 2349**

Then we have CONVERSION(2349)=24, and this is wrong according to our reference thermometer, because it should be CONVERSION(2349)=30.

![https://sky-boards-collect.googlecode.com/svn/wiki/img/imgcal3.jpg](https://sky-boards-collect.googlecode.com/svn/wiki/img/imgcal3.jpg)

  * Select node 3 and go to "Tools->Adjust conversion expressions>Temperature sensor"

  * Change values until **CONVERSION(2349)=30**

**You should change preferably Vcc and/or Vref**, if your batteries have some time (do not use cheap batteries :) or you don't know which Vref is using the firmware to configure ADC12 input reference. If your mote is USB-powered, you will likely have to make change Vcc. In light and CO sensors other changes are possible.

By trial and error method we found a good value (you could also try a more advanced method based in ecuations resolution). In this example if we change Vcc to 2.66 it gives the good conversion

![https://sky-boards-collect.googlecode.com/svn/wiki/img/imgcal4.jpg](https://sky-boards-collect.googlecode.com/svn/wiki/img/imgcal4.jpg)

![https://sky-boards-collect.googlecode.com/svn/wiki/img/imgcal5.jpg](https://sky-boards-collect.googlecode.com/svn/wiki/img/imgcal5.jpg)

**NOTE:** MinX, MaxX and IncX values are only intended to define the range in which conversion function is generated, its value doesn't affect conversion.

Now, close adjust conversion expressions dialog and chart will be updated to the right values:

![https://sky-boards-collect.googlecode.com/svn/wiki/img/imgcal6.jpg](https://sky-boards-collect.googlecode.com/svn/wiki/img/imgcal6.jpg)