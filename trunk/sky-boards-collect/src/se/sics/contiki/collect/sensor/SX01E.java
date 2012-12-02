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
  
  double vRef;
  double c1;
  double c2;

  public SX01E(String sensorID, String nodeID, int aDCRes) {
    super(sensorID, nodeID);
    setUnits("mg/m^3");
    setADC(aDCRes);
    configDefConstants();
    setConstants();
    setRoundDigits(4);
  }

  public double getConv(Double value) {
    double Vs = ((double) value / aDCResolution) * vRef;
    return (Vs * c1) + c2;
  }

  public void configDefConstants() {
    setVar("Vref", 2.5);
    setVar("c1", 0.228);
    setVar("c2", 0.03);
  }
  
  public void setConstants() {
    vRef = getValueOf("Vref");
    c1 = getValueOf("c1");
    c2 = getValueOf("c2");
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
    copy.setConstants();
    return copy;
  }
}
