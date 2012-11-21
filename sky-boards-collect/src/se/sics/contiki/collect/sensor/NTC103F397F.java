/*
 * NTC103F397F
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 16 sep 2012
 * 
 * NTC-103F397F Temperature Sensor
 * NTC Thermistor Technology 
 */
package se.sics.contiki.collect.sensor;
import se.sics.contiki.collect.Sensor;

public class NTC103F397F extends Sensor {

  public NTC103F397F(String sensorID, String nodeID) {
    super(sensorID, nodeID);
    setUnits("Celsius");
    setADC12(true);
    setConstants();
    setRoundDigits(2);
  }

  @Override
  public double getConv(Double value) {
    double vRef = getValueOf("Vref");
    double Vcc = getValueOf("Vcc");
    double RT = getValueOf("RT");
    double T = getValueOf("T");
    double beta = getValueOf("beta");
    double K = 272.15;
    // Thermistor resistor R0
    double Vs = ((double) value / (double) 4096) * vRef;
    double R0 = ((Vcc - Vs) * RT) / Vs;
    double ln_param = (double) RT / (double) R0;
    return ((-beta / (Math.log(ln_param) - (beta / T)))) - K;
  }

  @Override
  public void setConstants() {
    setVar("Vref", 2.5);
    setVar("Vcc", 3.0);
    setVar("RT", 10000.0);
    setVar("T", 298.15);
    setVar("beta", 3970.0);
  }
}
