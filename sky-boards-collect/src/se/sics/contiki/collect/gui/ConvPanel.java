/*
 * NodeCalibrationDialog
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 23 jul 2012
 */
package se.sics.contiki.collect.gui;


import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import se.sics.contiki.collect.CollectServer;
import se.sics.contiki.collect.Node;
import se.sics.contiki.collect.Sensor;
import se.sics.contiki.collect.SensorData;
import se.sics.contiki.collect.Visualizer;

public class ConvPanel extends JPanel implements Visualizer {
  private static final long serialVersionUID = 1L;
  private CollectServer server;
  private boolean selected;
  private String title;
  private String category;

  private Node selectedNode;
  private String selectedNodeID;
  private Node workingCopyNode; // working copy of the node
  private Properties config;

  private JTabbedPane tabbedPane; // tabbedPanel
  TabChangeListener tabChangeListener;

  public ConvPanel(CollectServer server, String category, String title,
      Properties config) {
    super(new BorderLayout());
    this.title = title;
    this.category = category;
    this.server = server;
    this.config = config;
    selected = false;
    tabbedPane = new JTabbedPane();
    tabChangeListener = new TabChangeListener();
  }

  private class TabChangeListener implements ChangeListener {
    public void stateChanged(ChangeEvent e) {
      // This has to be done because sensors can
      // have dependent expressions (eg: Temperature
      // compensation in SHT11 Humidity sensor).
      // See SHT11Humidity.java and SHT11Temperature.java
      SensorAdjustPanel sap=(SensorAdjustPanel) tabbedPane.getSelectedComponent();
      if (sap==null)
        return;
      if (sap.isSensorDependent())
        sap.updateChart();
    }
  }

  public String getCategory() {
    return category;
  }

  public String getTitle() {
    return title;
  }

  public Component getPanel() {
    return this;
  }

  public void setSelected(boolean isSelected) {
    selected = isSelected;
  }

  private void copySelectedNode() {
    
    SensorData sd = selectedNode.getLastSD();
    if (sd == null){
      return;
    }
    int nodeType = sd.getType();
    String id = selectedNode.getID();
    workingCopyNode = server.createNode(id, nodeType);
    workingCopyNode.copySensorsFrom(selectedNode);
    workingCopyNode.addSensorData(selectedNode.getLastSD());
  }

  public void nodesSelected(Node[] node) {

    if (node==null)
      return;

    // Do not paint
    // components in these cases
    if (!selected || node == null || node.length > 1
        || node[0].getID().equals(selectedNodeID))
      return;
    tabbedPane.removeAll();
    tabbedPane.removeChangeListener(tabChangeListener);
    selectedNode = node[0];
    selectedNodeID = selectedNode.getID();
    if (node[0].getSensorDataCount()==0){return;}
    copySelectedNode();
    Sensor[] sensors = workingCopyNode.getSensors();
    
    for (int i = 0; i < sensors.length; i++) {
      Sensor sensor = sensors[i];
      JPanel pane = new SensorAdjustPanel(workingCopyNode, sensor,
          config,this);
      tabbedPane.add(pane, sensor.getId());
    }
    add(tabbedPane, BorderLayout.CENTER);
    updateUI();
    tabbedPane.addChangeListener(tabChangeListener);
  }

  public void nodeAdded(Node node) {/* ignore */
  }

  public void nodeDataReceived(SensorData sensorData) {
    // update last raw value received
    if (!selected || sensorData.getNode().getID() != selectedNode.getID())
      return;

    for (int i = 0; i < tabbedPane.getTabCount(); i++) {
      ((SensorAdjustPanel) tabbedPane.getComponentAt(0))
          .addSensorData(sensorData);
    }
  }
  
  public void updateChanges(Sensor s){
    Sensor appSensor=selectedNode.getNodeSensor(s.getId());
    appSensor.updateVars(s);
    server.updateChart(s.getId());
  }

  public void clearNodeData() {/* ignore */
  }
}
