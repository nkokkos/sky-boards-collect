/*
 * NodeAR1000
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 15 jun 2012
 */

/**
 * 
 */
package se.sics.contiki.collect;

public class NodeAR1000 extends Node implements SensorInfo {

	public int type = AR1000;

	public NodeAR1000(String nodeID) {
		super(nodeID);
		addNodeSensors();
	}

	public NodeAR1000(String nodeID, String nodeName) {
		super(nodeID, nodeName);
	}

	public void addNodeSensors() {
		sensors.put(CO, new NodeSensor("CO", CO, this.getID(), "ppm", true));
		sensors.put(CO2, new NodeSensor("CO2", CO2, this.getID(), "ppm", true));
		sensors.put(DUST, new NodeSensor("Dust", DUST, this.getID(), "mg/m^3",
		    false));
		setDefaultValues();
	}

	public void setDefaultValues() {
		setCOValues();
		setCO2Values();
		setDustValues();
	}

	public void setDefaultValues(String sensorName) {
		NodeSensor sensor = sensors.get(keyConv(sensorName));
		if (sensor == null)
			return;
		if (sensorName.equals("CO")) {
			setCOValues();
			return;
		}
		if (sensorName.equals("CO2")) {
			setCO2Values();
			return;
		}
		if (sensorName.equals("Dust")) {
			setDustValues();
		}
	}

	private void setCOValues() {
		NodeSensor s = sensors.get(CO);
		s.setVar("Vref", 2.5);
		s.setVar("RL", 20000.0);
		s.setVar("R0", 280000.0);
		s.setVar("Vcc", 3.0);
		s.setVar("case2_v1", 300);
		s.setVar("case2_v2", 250);
		s.setVar("case3_v1", 3600);
		s.setVar("case3_v2", 1900);
	}

	private void setCO2Values() {
		NodeSensor s = sensors.get(CO2);
		s.setVar("Vref", 2.5);
		s.setVar("v1", 1000.0);
		s.setVar("v2", 200.0);
	}

	private void setDustValues() {
		NodeSensor s = sensors.get(DUST);
		s.setVar("Vref", 2.5);
		s.setVar("v1", 0.228);
		s.setVar("v2", 0.03);
	}
}
