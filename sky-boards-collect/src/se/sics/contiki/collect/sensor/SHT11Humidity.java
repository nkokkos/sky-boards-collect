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
  
  SHT11Temperature tempSensor;

  public SHT11Humidity(String sensorID, String nodeID) {
    super(sensorID, nodeID);
    setUnits("%");
    setADC12(false);
    setConstants();
    setRoundDigits(2);
  }
  
  public void setAssociatedTempSensor(Sensor s)
  {
    this.tempSensor=(SHT11Temperature) s;
  }

  @Override
  public double getConv(int value) {
    /*
     * Conversion based on this info:
     * http://www.advanticsys.com/wiki/index.php?title=Sensirion%C2%AE_SHT11
     */
    double c1 = getValueOf("c1");
    double c2 = getValueOf("c2");
    double c3 = getValueOf("c3");
    double t1 = getValueOf("t1");
    double t2 = getValueOf("t2");
    double temp = tempSensor.lastTemp;
        
    double RHlinear = c1 + c2 * value + c3 * (value * value);
    // Temperature compensation:
    double v = (temp - 25) * (t1 + t2 * value) + RHlinear;
    if (v > 100) {
      return 100;
    }
    return v;
  }

  @Override
  public void setConstants() {
    setVar("c1", -2.0468);
    setVar("c2", 0.0367);
    setVar("c3", -1.5955E-6);
    setVar("t1", 0.01);
    setVar("t2", 8E-5);
  }
}
