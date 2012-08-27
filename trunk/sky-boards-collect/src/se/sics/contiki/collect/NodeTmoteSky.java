/*
 * NodeTmoteSky
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 15 jun 2012
 */

package se.sics.contiki.collect;

public class NodeTmoteSky extends Node implements SensorInfo {

	public NodeTmoteSky(String nodeID) {
		super(nodeID);
		addNodeSensors();
	}

	public NodeTmoteSky(String nodeID, String nodeName) {
		super(nodeID, nodeName);
	}

	public void addNodeSensors() {
		sensors.put(LIGHT1, new NodeSensor("Light1", LIGHT1, this.getID(), "Lx",
		    true));
		sensors.put(LIGHT2, new NodeSensor("Light2", LIGHT2, this.getID(), "Lx",
		    true));
		sensors.put(TEMPERATURE, new NodeSensor("Temperature", TEMPERATURE, this
		    .getID(), "Celsius", false));
		sensors.put(HUMIDITY, new NodeSensor("Humidity", HUMIDITY, this.getID(),
		    "%", false));
		setDefaultValues();
	}

	public void setDefaultValues() {
		setLight1Values();
		setLight2Values();
		setTemperatureValues();
		setHumidityValues();
	}

	public void setDefaultValues(String sensorName) {
		NodeSensor sensor = sensors.get(keyConv(sensorName));
		if (sensor == null)
			return;
		if (sensorName.equals("Light1")) {
			setLight1Values();
			return;
		}

		if (sensorName.equals("Light2")) {
			setLight2Values();
			return;
		}

		if (sensorName.equals("Temperature")) {
			setTemperatureValues();
			return;
		}

		if (sensorName.equals("Humidity")) {
			setHumidityValues();
		}

	}

	private void setHumidityValues() {
		NodeSensor s = sensors.get(HUMIDITY);
		s.setVar("c1", -2.0468);
		s.setVar("c2", 0.0367);
		s.setVar("c3", -1.5955E-6);
		s.setVar("t1", 0.01);
		s.setVar("t2", 8E-5);
		s.setVar("T", 25); // Only for calibration dialog.
	}

	private void setTemperatureValues() {
		NodeSensor s = sensors.get(TEMPERATURE);
		s.setVar("v1", 39.6);
		s.setVar("v2", 0.01);
	}

	private void setLight2Values() {
		NodeSensor s = sensors.get(LIGHT2);
		s.setVar("Vref", 2.5);
		s.setVar("R12", 100000);
		s.setVar("v12", 0.769);
	}

	private void setLight1Values() {
		NodeSensor s = sensors.get(LIGHT1);
		s.setVar("Vref", 2.5);
		s.setVar("R11", 100000);
		s.setVar("v11", 0.625);
	}
}
