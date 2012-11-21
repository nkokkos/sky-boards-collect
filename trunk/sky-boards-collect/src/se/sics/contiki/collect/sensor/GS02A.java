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

  public GS02A(String sensorID, String nodeID) {
    super(sensorID, nodeID);
    setUnits("ppm");
    setADC12(true);
    setConstants();
    setRoundDigits(2);
  }

  @Override
  public double getConv(Double value) {
    double vRef = getValueOf("Vref");
    double RL = getValueOf("RL");
    double Vcc = getValueOf("Vcc");
    double R0 = getValueOf("R0");
    double case2_v1 = getValueOf("case2_v1");
    double case2_v2 = getValueOf("case2_v2");
    double case3_v1 = getValueOf("case3_v1");
    double case3_v2 = getValueOf("case3_v2");
    double up_limit = getValueOf("upper_limit");
    double low_limit = getValueOf("lower_limit");

    double Vs = ((double) value / (double) 4096) * vRef;
    double sensitivity = ((Vcc / Vs) - 1) * RL / R0;

    if (sensitivity > up_limit)
      return 10;
    if (up_limit < sensitivity || sensitivity > low_limit)
      return (double) ((-case2_v1 * sensitivity) + case2_v2);
    return (double) ((-case3_v1 * sensitivity) + case3_v2); // sensitivity<=lower limit
  }

  @Override
  public void setConstants() {
    setVar("Vref", 2.5);
    setVar("RL", 20000.0);
    setVar("R0", 880000.0);
    setVar("Vcc", 3.0);
    setVar("case2_v1", 300);
    setVar("case2_v2", 250);
    setVar("case3_v1", 3600);
    setVar("case3_v2", 1900);
    setVar("upper_limit",0.8);
    setVar("lower_limit",0.5);
  }

}
