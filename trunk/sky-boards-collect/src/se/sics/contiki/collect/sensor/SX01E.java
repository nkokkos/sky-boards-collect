/*
 * SX01E
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 16 sep 2012
 * 
 * SX-01E Dust (particle concentration) sensor
 * Linear output voltage
 */

package se.sics.contiki.collect.sensor;
import se.sics.contiki.collect.Sensor;

public class SX01E extends Sensor {

  public SX01E(String sensorID, String nodeID, int aDCRes) {
    super(sensorID, nodeID);
    setUnits("mg/m^3");
    setADC(aDCRes);
    setConstants();
    setRoundDigits(4);
  }

  public double getConv(Double value) {
    double vRef = getValueOf("Vref");
    double v1 = getValueOf("v1");
    double v2 = getValueOf("v2");
    double resolution=getADCResolution();
    double Vs = ((double) value / resolution) * vRef;

    return (Vs * v1) + v2;
  }

  public void setConstants() {
    setVar("Vref", 2.5);
    setVar("v1", 0.228);
    setVar("v2", 0.03);
  }
  
  public Sensor Clone() {
    Sensor copy=new SX01E(getId(),nodeID,getADCResolution());
    Sensor a;
    copy.updateVars(this);
    if ((a=getAssociatedSensor())!=null)
      copy.setAssociatedSensor(a.Clone());
    copy.setRoundDigits(getRoundDigits());
    copy.setUnits(getUnits());
    copy.lastValue = lastValue;
    return copy;
  }
}
