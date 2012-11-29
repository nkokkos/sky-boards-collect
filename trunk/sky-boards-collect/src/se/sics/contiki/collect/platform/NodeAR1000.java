/*
 * NodeAR1000
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 15 jun 2012
 */

package se.sics.contiki.collect.platform;

import se.sics.contiki.collect.sensor.*;
import se.sics.contiki.collect.Node;
import se.sics.contiki.collect.SensorInfo;

public class NodeAR1000 extends Node implements SensorInfo {

  public NodeAR1000(String nodeID) {
    super(nodeID);
    init();
  }

  public NodeAR1000(String nodeID, String nodeName) {
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
    sensors.put(DUST_SENSOR, new SX01E(DUST_SENSOR, nodeID,
        PLATFORM_ADC_RESOLUTION));
  }

  @Override
  public void mapMsgFormat() {
    dataMsgMapping.put(CO_SENSOR, CO);
    dataMsgMapping.put(CO2_SENSOR, CO2);
    dataMsgMapping.put(DUST_SENSOR, DUST);
  }

  @Override
  public void setNodeType() {
    type = "AR1000";
  }

  @Override
  public void setPlatformADCResolution() {
    PLATFORM_ADC_RESOLUTION = 4096;
  }

  @Override
  public void copySensorsFrom(Node n) {
    sensors.put(CO_SENSOR, n.getNodeSensor(CO_SENSOR).Clone());
    sensors.put(CO2_SENSOR, n.getNodeSensor(CO2_SENSOR).Clone());
    sensors.put(DUST_SENSOR, n.getNodeSensor(DUST_SENSOR).Clone());
  }
}
