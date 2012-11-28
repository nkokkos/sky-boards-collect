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
  private String SelectedNodeID;
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

  private void copySelectedNode(Node n) {
    selectedNode = n;
    SelectedNodeID = selectedNode.getID();
    SensorData sd = selectedNode.getLastSD();
    if (sd == null)
      return;
    int nodeType = sd.getType();
    String id = selectedNode.getID();
    workingCopyNode = server.createNode(id, nodeType);
    workingCopyNode.copySensorsFrom(selectedNode);
    workingCopyNode.addSensorData(selectedNode.getLastSD());
  }

  public void nodesSelected(Node[] node) {

    // Do not waste time painting
    // components in these cases
    if (!selected || node == null || node.length > 1
        || node[0].getID().equals(SelectedNodeID))
      return;

    tabbedPane.removeChangeListener(tabChangeListener);
    copySelectedNode(node[0]);
    Sensor[] sensors = workingCopyNode.getSensors();
    tabbedPane.removeAll();

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
