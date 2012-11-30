/*
 * Sensor
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 22 jun 2012
 */

package se.sics.contiki.collect;

import java.util.LinkedHashMap;
import java.util.Set;

public abstract class Sensor {

  private LinkedHashMap<String, Double> vars;
  private String id;
  private String units;
  private boolean ADC;
  private int roundDigits;
  private int aDCResolution;
  public String nodeID;
  public double lastValue;
  private Sensor associatedSensor;

  public Sensor(String sensorID, String nodeID) {
    vars = new LinkedHashMap<String, Double>();
    this.id = sensorID;
    aDCResolution = -1;
    ADC = false;
    roundDigits = 2;
    units = "";
    this.nodeID=nodeID;
  }
  
  public void updateVars(Sensor s){
    vars.clear();
    String[] varNames=(String[]) s.getVarsNames();
    for (int i=0,n=varNames.length;i<n;i++){
      String var=varNames[i];
      vars.put(var, s.getValueOf(var));
    }
  }
  
  public String getId() {
    return id;
  }

  public void setVar(String name, double value) {
    vars.put(name, value);
  }

  public Object[] getVarsNames() {
    Set<String> s=vars.keySet();
    return s.toArray();
  }

  public void setUnits(String units) {
    this.units = units;
  }

  public String getUnits() {
    return units;
  }

  public void setADC(int aDCResolution) {
    this.ADC = true;
    this.aDCResolution=aDCResolution;
  }

  public boolean ADC() {
    return ADC;
  }

  public int getADCResolution() {
    return aDCResolution;
  }
  
  public double getValueOf(String var){
    Double value=vars.get(var);
    if (value==null) return Double.NaN;
    return value;
  }
  
  public void setRoundDigits(int digits){
    if(digits<1) digits=1;
    else if (digits>8) digits=8;
    roundDigits=digits;
  }
 
  public int getRoundDigits(){
    return roundDigits;
  }
  
  public void setAssociatedSensor(Sensor s){
    associatedSensor=s;
  }
  
  public Sensor getAssociatedSensor(){
    return associatedSensor;
  }
  
  public abstract double getConv(Double value);  
  public abstract void setConstants();
  public abstract Sensor Clone();
}
