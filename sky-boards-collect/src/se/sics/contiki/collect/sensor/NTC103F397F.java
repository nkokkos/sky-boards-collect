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

  double vRef;
  double Vcc;
  double RT;
  double T;
  double beta;
  double K = 272.15;

  public NTC103F397F(String sensorID, String nodeID, int aDCRes) {
    super(sensorID, nodeID);
    setUnits("Celsius");
    setADC(aDCRes);
    configDefConstants();
    setConstants();
    setRoundDigits(2);
  }

  @Override
  public double getConv(Double value) {
    // Thermistor resistor R0
    double Vs = ((double) value / aDCResolution) * vRef;
    double R0 = ((Vcc - Vs) * RT) / Vs;
    double ln_param = (double) RT / (double) R0;
    return ((-beta / (Math.log(ln_param) - (beta / T)))) - K;
  }

  @Override
  public void configDefConstants() {
    setVar("Vref", 2.5);
    setVar("Vcc", 3.0);
    setVar("RT", 10000.0);
    setVar("T", 298.15);
    setVar("beta", 3970.0);
  }

  public void setConstants() {
    vRef = getValueOf("Vref");
    Vcc = getValueOf("Vcc");
    RT = getValueOf("RT");
    T = getValueOf("T");
    beta = getValueOf("beta");
  }

  public Sensor Clone() {
    Sensor copy = new NTC103F397F(getId(), nodeID, getADCResolution());
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
