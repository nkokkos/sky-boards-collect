/*
 * Sensor
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 22 jun 2012
 */

package se.sics.contiki.collect;

import java.util.Vector;

public abstract class Sensor {

  private Vector<Variable> vars;
  private String id;
  private String units;
  private boolean adc12;
  private int roundDigits;

  public Sensor(String sensorID, String nodeID) {
    vars = new Vector<Variable>();
    this.id = sensorID;
  }

  public String getId() {
    return id;
  }

  public void setVar(String name, double value) {
    int i = 0;

    while (i < vars.size()) {
      if (vars.get(i).getName().equals(name)) {
        vars.get(i).setValue(value);
        return;
      }
      i++;
    }

    Variable var1 = new Variable(name, value);
    vars.add(var1);
  }

  public Variable[] getVars() {
    Variable[] vars_array = new Variable[vars.size()];
    for (int i = 0; i < vars.size(); i++) {
      vars_array[i] = vars.get(i);
    }
    return vars_array;
  }

  public Variable getVar(String name) {
    int i = 0;

    while (i < vars.size()) {
      if (vars.get(i).getName().equals(name)) {
        return vars.get(i);
      }
      i++;
    }
    return null;
  }

  public void setUnits(String units) {
    this.units = units;
  }

  public String getUnits() {
    return units;
  }

  public void setADC12(boolean b) {
    this.adc12 = b;
  }

  public boolean ADC12() {
    return adc12;
  }
  
  public double getValueOf(String var){
    return getVar(var).getValue();
  }
  
  public void setRoundDigits(int digits){
    if(digits<1) digits=1;
    else if (digits>8) digits=8;
    roundDigits=digits;
  }
 
  public int getRoundDigits(){
    return roundDigits;
  }
  
  public abstract double getConv(Double value);  
  public abstract void setConstants();
  
}
