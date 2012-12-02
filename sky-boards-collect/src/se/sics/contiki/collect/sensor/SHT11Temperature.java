/*
 * SHT11Temperature
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 16 sep 2012
 * 
 * SHT11 temperature sensor
 * Temperature Range: -40 ~ 123.8 °C
 * Temperature Resolution: : ± 0.01(typical)
 * Temperature Accuracy: ± 0.4 °C (typical)
 */
package se.sics.contiki.collect.sensor;

import se.sics.contiki.collect.Sensor;

public class SHT11Temperature extends Sensor {

  double c1;
  double c2;

  public SHT11Temperature(String sensorID, String nodeID) {
    super(sensorID, nodeID);
    setUnits("Celsius");
    configDefConstants();
    setConstants();
    setRoundDigits(2);
  }

  public double getConv(Double value) {
    lastValue = -c1 + c2 * value;
    return lastValue;
  }

  public void configDefConstants() {
    setVar("c1", 39.6);
    setVar("c2", 0.01);
  }

  public void setConstants() {
    c1 = getValueOf("c1");
    c2 = getValueOf("c2");
  }

  public Sensor Clone() {
    Sensor copy = new SHT11Temperature(getId(), nodeID);
    Sensor a;
    copy.updateVars(this);
    if ((a = getAssociatedSensor()) != null)
      copy.setAssociatedSensor(a.Clone());
    copy.setRoundDigits(getRoundDigits());
    copy.setUnits(getUnits());
    copy.lastValue = lastValue;
    copy.setConstants();
    return copy;
  }
}
