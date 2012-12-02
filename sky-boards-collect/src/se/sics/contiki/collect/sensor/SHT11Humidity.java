/*
 * SHT11Temperature
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 16 sep 2012
 * 
 * Humidity Range: 0 ~ 100% RH
 * Humidity Resolution: 0.05 (typical)
 * Humidity Accuracy: ± 3 %RH (typical)
 */
package se.sics.contiki.collect.sensor;

import se.sics.contiki.collect.Sensor;

public class SHT11Humidity extends Sensor {

  double c1;
  double c2;
  double c3;
  double t1;
  double t2;

  public SHT11Humidity(String sensorID, String nodeID) {
    super(sensorID, nodeID);
    setUnits("%");
    configDefConstants();
    setConstants();
    setRoundDigits(2);
  }

  /*
   * Conversion based on this info:
   * http://www.advanticsys.com/wiki/index.php?title=Sensirion%C2%AE_SHT11
   */
  public double getConv(Double value) {
    Double temp = this.getAssociatedSensor().lastValue;
    double RHlinear = c1 + c2 * value + c3 * (value * value);
    // Temperature compensation:
    double v = (temp - 25) * (t1 + t2 * value) + RHlinear;
    if (v > 100) {
      return 100;
    }
    return v;
  }

  public void configDefConstants() {
    setVar("c1", -2.0468d);
    setVar("c2", 0.0367d);
    setVar("c3", -1.5955E-6d);
    setVar("t1", 0.01d);
    setVar("t2", 8E-5d);
  }

  public void setConstants() {
    c1 = getValueOf("c1");
    c2 = getValueOf("c2");
    c3 = getValueOf("c3");
    t1 = getValueOf("t1");
    t2 = getValueOf("t2");
  }

  public Sensor Clone() {
    Sensor copy = new SHT11Humidity(getId(), nodeID);
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
