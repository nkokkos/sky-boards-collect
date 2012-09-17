package se.sics.contiki.collect.sensor;
/*
 * S108701
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 16 sep 2012
 * 
 * Hamamatsu® S1087 light sensor
 * Visible & Infrared Range (960 nm peak sensitivity wavelength)
 */
import se.sics.contiki.collect.Sensor;

public class S108701 extends Sensor {

  public S108701(String sensorID, String nodeID) {
    super(sensorID, nodeID);
    setUnits("Lx");
    setADC12(true);
    setConstants();
    setRoundDigits(2);
  }

  public double getConv(int value) {
    double vRef = getValueOf("Vref");
    double v12 = getValueOf("v12");
    double R12 = getValueOf("R12");
    double Vs = ((double) value / (double) 4096) * vRef;
    return (v12 * 100000 * (Vs / R12) * 1000);
  }

  @Override
  public void setConstants() {
    setVar("Vref", 2.5);
    setVar("R12", 100000);
    setVar("v12", 0.769);
  }

}
