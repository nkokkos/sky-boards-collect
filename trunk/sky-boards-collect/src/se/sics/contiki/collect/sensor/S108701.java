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

  public S108701(String sensorID, String nodeID,int aDCRes) {
    super(sensorID, nodeID);
    setUnits("Lx");
    setADC(aDCRes);
    setConstants();
    setRoundDigits(2);
  }

  public double getConv(Double value) {
    double vRef = getValueOf("Vref");
    double v12 = getValueOf("v12");
    double R12 = getValueOf("R12");
    double resolution=getADCResolution();
    double Vs = ((double) value / resolution) * vRef;
    return (v12 * 100000 * (Vs / R12) * 1000);
  }

  public void setConstants() {
    setVar("Vref", 2.5);
    setVar("R12", 100000);
    setVar("v12", 0.769);
  }

  public Sensor Clone() {
    Sensor copy=new S108701(getId(),nodeID,getADCResolution());
    Sensor a;
    copy.updateVars(this);
    if ((a=getAssociatedSensor())!=null)
      copy.setAssociatedSensor(a.Clone());
    copy.setRoundDigits(getRoundDigits());
    copy.setUnits(getUnits());
    return copy;
  }

}
