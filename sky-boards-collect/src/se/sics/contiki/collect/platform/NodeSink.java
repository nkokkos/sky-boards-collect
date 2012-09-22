package se.sics.contiki.collect.platform;


import se.sics.contiki.collect.Node;
import se.sics.contiki.collect.SensorInfo;

public class NodeSink extends Node implements SensorInfo {

  public NodeSink(String nodeID) {
    super(nodeID);
    init();
  }
  
  public NodeSink(String nodeID, String nodeName){
    super(nodeID,nodeName);
    init();
  }
  
  @Override
  public void init() {
    addSensors();
    mapMsgFormat();
    setNodeType();
  }
  
  @Override
  public void addSensors() {}

  @Override
  public void mapMsgFormat() {}

  @Override
  public void setNodeType() {
    type="Sink";    
  }
}
