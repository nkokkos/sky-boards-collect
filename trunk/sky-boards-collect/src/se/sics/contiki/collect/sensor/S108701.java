/*
 * S108701
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 16 sep 2012
 * 
 * Hamamatsu® S1087 light sensor
 * Visible & Infrared Range (960 nm peak sensitivity wavelength)
 */
package se.sics.contiki.collect.sensor;
import se.sics.contiki.collect.Sensor;

public class S108701 extends Sensor {

  double vRef;
  double k12;
  double R12;
  double c1;
  double c2;
  
  public S108701(String sensorID, String nodeID,int aDCRes) {
    super(sensorID, nodeID);
    setUnits("Lx");
    setADC(aDCRes);
    configDefConstants();
    setConstants();
    setRoundDigits(2);
  }

  public double getConv(Double value) {
    double Vs = ((double) value / aDCResolution) * vRef;
    return (k12 * c1 * (Vs / R12) * c2);
  }

  public void configDefConstants() {
    setVar("Vref", 2.5);
    setVar("R12", 100000);
    setVar("k12", 0.769);
    setVar("c1", 10e5);
    setVar("c2", 1000);
  }
  
  public void setConstants(){
    vRef = getValueOf("Vref");
    k12 = getValueOf("k12");
    R12 = getValueOf("R12");
    c1 = getValueOf("c1");
    c2 = getValueOf("c2");
  }

  public Sensor Clone() {
    Sensor copy=new S108701(getId(),nodeID,getADCResolution());
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
