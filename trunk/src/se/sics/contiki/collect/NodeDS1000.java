/*
 * NodeDS1000
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 15 jun 2012
 */

package se.sics.contiki.collect;

public class NodeDS1000 extends Node implements SensorInfo{

	public NodeDS1000(String nodeID) {
		super(nodeID);
		addNodeSensors();	
	}

	public NodeDS1000(String nodeID, String nodeName) {
		super(nodeID, nodeName);
	}

	public void addNodeSensors(){
		sensors.put(CO, new NodeSensor("CO",
				CO,this.getID()));
		sensors.put(CO2, new NodeSensor("CO2", 
				CO2,this.getID()));
		sensors.put(TEMPERATURE, new NodeSensor("Temperature", 
				TEMPERATURE,this.getID()));
		setDefaultValues();
	}
	
	public void setDefaultValues(){
		setCOValues();
		setCO2Values();
		setTemperatureValues();
	}
	
	public void setDefaultValues(String sensorName)
	{
		NodeSensor sensor=sensors.get(this.keyConv(sensorName));
		if (sensor==null) return;
		if (sensorName.equals("CO")){
			setCOValues();
			return;
		}
		
		if (sensorName.equals("CO2")){
			setCO2Values();
			return;
		}
		
		if (sensorName.equals("Temperature")){
			setTemperatureValues();
		}		
	}
	
	private void setCOValues()
	{
		NodeSensor s=sensors.get(CO);
		s.setVar("Vref", 2.5);
		s.setVar("RL", 20000.0);
		s.setVar("R0", 280000.0);
		s.setVar("Vcc", 3.0);
		s.setVar("case2_v1", 300);
		s.setVar("case2_v2", 250);
		s.setVar("case3_v1", 3600);
		s.setVar("case3_v2", 1900);
	}
	
	private void setCO2Values()
	{
		NodeSensor s=sensors.get(CO2);
		s.setVar("Vref", 2.5);
		s.setVar("v1", 1000.0);
		s.setVar("v2", 200.0);
	}
	
	private void setTemperatureValues()
	{
		NodeSensor s=sensors.get(TEMPERATURE);
		s.setVar("Vref", 2.5);
		s.setVar("Vcc", 3.0);
		s.setVar("RT", 10000.0);
		s.setVar("T", 298.15);
		s.setVar("beta", 3970.0);
	}

}
