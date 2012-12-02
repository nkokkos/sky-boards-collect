/*
 * SH300DC
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 16 sep 2012
 * 
 * SH-300-DC CO2 sensor
 * Voltage output based 
 */
package se.sics.contiki.collect.sensor;

import se.sics.contiki.collect.Sensor;

public class SH300DC extends Sensor {

  double vRef;
  double c1;
  double c2;

  public SH300DC(String sensorID, String nodeID, int aDCRes) {
    super(sensorID, nodeID);
    setUnits("ppm");
    setADC(aDCRes);
    configDefConstants();
    setConstants();
    setRoundDigits(2);
  }

  public double getConv(Double value) {
    double Vs = ((double) value / aDCResolution) * vRef;
    return (Vs * c1) - c2;
  }

  public void configDefConstants() {
    setVar("Vref", 2.5);
    setVar("c1", 1000.0);
    setVar("c2", 200.0);
  }

  public void setConstants() {
    vRef = getValueOf("Vref");
    c1 = getValueOf("c1");
    c2 = getValueOf("c2");
  }

  public Sensor Clone() {
    Sensor copy = new SH300DC(getId(), nodeID, getADCResolution());
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
