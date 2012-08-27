/*
 * NodeSensor
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 22 jun 2012
 */

package se.sics.contiki.collect;
import java.util.Vector;

public class NodeSensor {
	
	private String sensorName;
	private Vector<Variable> vars;
	private int id;
	private String feedID;
	private String units;
	private boolean hasVoltage;
	
	public NodeSensor(String name, int id, String nodeID, String units, boolean hasVoltage){
		 sensorName=name;
		 vars = new Vector<Variable>();
		 this.id=id;
		 feedID=null;
		 this.setUnits(units);
		 this.setHasVoltage(hasVoltage);
	}
	
	public String getFeedID(){
		return feedID;
	}
	
	public void setFeedID(String id){
		feedID=id;
	}
	
	public String getName(){
		return sensorName;
	}
	public int getSensorId(){
		return id;
	}
	
	public void setVar(String name, double value){
		int i=0;
		
		while (i<vars.size()){
			if (vars.get(i).getName().equals(name)){
				vars.get(i).setValue(value);
				return;
			}
			i++;
		}
	
		Variable var1 = new Variable(name, value);
		vars.add(var1);
	}
	
	public Variable[] getVars(){		
		Variable[] vars_array = new Variable[vars.size()];
		for (int i = 0; i<vars.size();i++){
			vars_array[i]=vars.get(i);
		}
		return vars_array;
	}
	
	public Variable getVar(String name){
		int i=0;
		
		while (i<vars.size()){
			if (vars.get(i).getName().equals(name)){
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

	public void setHasVoltage(boolean hasVoltage) {
	  this.hasVoltage = hasVoltage;
  }

	public boolean hasVoltage() {
	  return hasVoltage;
  }
}
