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

  public SH300DC(String sensorID, String nodeID,int aDCRes) {
    super(sensorID, nodeID);
    setUnits("ppm");
    setADC(aDCRes);
    setConstants();
    setRoundDigits(2);
  }

  public double getConv(Double value) {
    double vRef = getValueOf("Vref");
    double v1 = getValueOf("v1");
    double v2 = getValueOf("v2");
    double resolution=getADCResolution();
    double Vs = ((double) value / resolution) * vRef;
    return (Vs * v1) - v2;
  }

  public void setConstants() {
    setVar("Vref", 2.5);
    setVar("v1", 1000.0);
    setVar("v2", 200.0);
  }

  public Sensor Clone() {
    Sensor copy=new SH300DC(getId(),nodeID,getADCResolution());
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
