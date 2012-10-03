/*
 * S1087
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 16 sep 2012
 * 
 * Hamamatsu® S1087 light sensor
 * Visible Range (560 nm peak sensitivity wavelength)
 */
package se.sics.contiki.collect.sensor;
import se.sics.contiki.collect.Sensor;

public class S1087 extends Sensor {

  public S1087(String sensorID, String nodeID) {
    super(sensorID, nodeID);
    setUnits("Lx");
    setADC12(true);
    setConstants();
    setRoundDigits(2);
  }

  public double getConv(int value) {  
    double vRef = getValueOf("Vref");
    double v11 = getValueOf("v11");
    double R11 = getValueOf("R11");
    double Vs = ((double) value / (double) 4096) * vRef;
    return (v11 * 1000000 * (Vs / R11) * 1000);
  }

  @Override
  public void setConstants() {
    setVar("Vref", 2.5);
    setVar("R11", 100000);
    setVar("v11", 0.625);
  }
}
