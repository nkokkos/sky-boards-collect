/*
 * GS02A
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 16 sep 2012
 * 
 * GS-02A CO sensor
 * Resistive-based technology
 */
package se.sics.contiki.collect.sensor;

import se.sics.contiki.collect.Sensor;

public class GS02A extends Sensor {

  double vRef;
  double RL;
  double Vcc;
  double R0;
  double case2_c1;
  double case2_c2;
  double case3_c1;
  double case3_c2;
  double up_limit;
  double low_limit;

  public GS02A(String sensorID, String nodeID, int aDCRes) {
    super(sensorID, nodeID);
    setUnits("ppm");
    setADC(aDCRes);
    configDefConstants();
    setConstants();
    setRoundDigits(2);
  }

  public double getConv(Double value) {
    double Vs = ((double) value / aDCResolution) * vRef;
    double sensitivity = ((Vcc / Vs) - 1) * RL / R0;

    if (sensitivity > up_limit)
      return 10;
    if (up_limit < sensitivity || sensitivity > low_limit)
      return (double) ((-case2_c1 * sensitivity) + case2_c2);
    return (double) ((-case3_c1 * sensitivity) + case3_c2);
  }

  public void configDefConstants() {
    setVar("Vref", 2.5);
    setVar("RL", 20000.0);
    setVar("R0", 880000.0);
    setVar("Vcc", 3.0);
    setVar("case2_c1", 300);
    setVar("case2_c2", 250);
    setVar("case3_c1", 3600);
    setVar("case3_c2", 1900);
    setVar("upper_limit", 0.8);
    setVar("lower_limit", 0.5);
  }

  public void setConstants() {
    vRef = getValueOf("Vref");
    RL = getValueOf("RL");
    Vcc = getValueOf("Vcc");
    R0 = getValueOf("R0");
    case2_c1 = getValueOf("case2_c1");
    case2_c2 = getValueOf("case2_c2");
    case3_c1 = getValueOf("case3_c1");
    case3_c2 = getValueOf("case3_c2");
    up_limit = getValueOf("upper_limit");
    low_limit = getValueOf("lower_limit");
  }

  public Sensor Clone() {
    Sensor copy = new GS02A(getId(), nodeID, getADCResolution());
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
