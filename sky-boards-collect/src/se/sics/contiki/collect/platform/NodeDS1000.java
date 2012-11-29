/*
 * NodeDS1000
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 15 jun 2012
 */

package se.sics.contiki.collect.platform;

import se.sics.contiki.collect.Node;
import se.sics.contiki.collect.SensorInfo;
import se.sics.contiki.collect.sensor.*;

public class NodeDS1000 extends Node implements SensorInfo {

  public NodeDS1000(String nodeID) {
    super(nodeID);
    init();
  }

  public NodeDS1000(String nodeID, String nodeName) {
    super(nodeID, nodeName);
    init();
  }

  @Override
  public void init() {
    setPlatformADCResolution();
    addSensors();
    mapMsgFormat();
    setNodeType(); 
  }

  @Override
  public void addSensors() {
    String nodeID = this.getID();
    sensors.put(CO_SENSOR,
        new GS02A(CO_SENSOR, nodeID, PLATFORM_ADC_RESOLUTION));
    sensors.put(CO2_SENSOR, new SH300DC(CO2_SENSOR, nodeID,
        PLATFORM_ADC_RESOLUTION));
    sensors.put(TEMPERATURE_SENSOR, new NTC103F397F(TEMPERATURE_SENSOR, nodeID,
        PLATFORM_ADC_RESOLUTION));
  }

  @Override
  public void mapMsgFormat() {
    dataMsgMapping.put(CO_SENSOR, CO);
    dataMsgMapping.put(CO2_SENSOR, CO2);
    dataMsgMapping.put(TEMPERATURE_SENSOR, TEMPERATURE);
  }

  @Override
  public void setNodeType() {
    type = "DS1000";
  }

  @Override
  public void setPlatformADCResolution() {
    PLATFORM_ADC_RESOLUTION = 4096;
  }

  @Override
  public void copySensorsFrom(Node n) {
    sensors.put(CO_SENSOR, n.getNodeSensor(CO_SENSOR).Clone());
    sensors.put(CO2_SENSOR, n.getNodeSensor(CO2_SENSOR).Clone());
    sensors.put(TEMPERATURE_SENSOR, n.getNodeSensor(TEMPERATURE_SENSOR).Clone());
  }
}
