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

  public SH300DC(String sensorID, String nodeID) {
    super(sensorID, nodeID);
    setUnits("ppm");
    setADC12(true);
    setConstants();
    setRoundDigits(2);
  }

  @Override
  public double getConv(Double value) {
    double vRef = getValueOf("Vref");
    double v1 = getValueOf("v1");
    double v2 = getValueOf("v2");

    double Vs = ((double) value / (double) 4096) * vRef;
    return (Vs * v1) - v2;
  }

  @Override
  public void setConstants() {
    setVar("Vref", 2.5);
    setVar("v1", 1000.0);
    setVar("v2", 200.0);
  }

}
