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

  double vRef;
  double k11;
  double R11;
  double c1;
  double c2;
  
  public S1087(String sensorID, String nodeID, int aDCRes) {
    super(sensorID, nodeID);
    setUnits("Lx");
    setADC(aDCRes);
    configDefConstants();
    setConstants();
    setRoundDigits(2);
  }

  public double getConv(Double value) {  
    double Vs = ((double) value / aDCResolution) * vRef;
    return (k11 * c1 * (Vs / R11) * c2);
  }

  @Override
  public void configDefConstants() {
    setVar("Vref", 2.5);
    setVar("R11", 100000);
    setVar("k11", 0.625);
    setVar("c1", 10e6);
    setVar("c2", 1000);
  }
  
  public void setConstants(){
    vRef = getValueOf("Vref");
    k11 = getValueOf("k11");
    R11 = getValueOf("R11");
    c1 = getValueOf("c1");
    c2 = getValueOf("c2");
  }
  
  public Sensor Clone() {
    Sensor copy=new S1087(getId(),nodeID,getADCResolution());
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
