package se.sics.contiki.collect.sensor;
/*
 * GS02A
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 16 sep 2012
 * 
 * GS-02A CO sensor
 * Resistive-based technology
 */
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
  public double getConv(int value) {
    double vRef = getValueOf("Vref");
    double RL = getValueOf("RL");
    double Vcc = getValueOf("Vcc");
    double R0 = getValueOf("R0");
    double case2_v1 = getValueOf("case2_v1");
    double case2_v2 = getValueOf("case2_v2");
    double case3_v1 = getValueOf("case3_v1");
    double case3_v2 = getValueOf("case3_v2");

    double Vs = ((double) value / (double) 4096) * vRef;
    double sensitivity = ((Vcc / Vs) - 1) * RL / R0;

    if (sensitivity > 0.8)
      return 10.0;
    if (0.8 < sensitivity || sensitivity > 0.5)
      return (double) ((-case2_v1 * sensitivity) + case2_v2);
    return (double) ((-case3_v1 * sensitivity) + case3_v2); // sensitivity<=0,5
  }

  @Override
  public void setConstants() {
    setVar("Vref", 2.5);
    setVar("RL", 20000.0);
    setVar("R0", 280000.0);
    setVar("Vcc", 3.0);
    setVar("case2_v1", 300);
    setVar("case2_v2", 250);
    setVar("case3_v1", 3600);
    setVar("case3_v2", 1900);
  }

}
