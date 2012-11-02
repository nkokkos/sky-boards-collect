/*
 * DataFeederSense
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 28 jul 2012
 */

package se.sics.contiki.collect.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import se.sics.contiki.collect.Configurable;
import se.sics.contiki.collect.Node;
import se.sics.contiki.collect.Sensor;
import se.sics.contiki.collect.PublisherSense;
import se.sics.contiki.collect.SensorData;
import se.sics.contiki.collect.Visualizer;

public class DataFeederSense extends JPanel implements Visualizer,
    Configurable {

  private static final long serialVersionUID = 1L;
  String category;
  JPasswordField keyField;
  JComboBox<String> comboBoxNode;
  JComboBox<String> comboBoxSensor;
  JComboBox<String> comboBoxRaw;
  JButton startButton;
  JButton stopButton;
  JButton setButton;
  JButton removeButton;
  boolean doFeed = false;
  boolean feedRaw = true;
  private JPanel panel;
  String feedingNode;
  String feedingSensor;
  JPanel fieldPaneVars;
  Properties config;
  JTextField feedIdField;
  JLabel statusLabel;
  PublisherSense publisher;
  JTextArea logArea;

  private Hashtable<String, Node> nodes = new Hashtable<String, Node>();

  public DataFeederSense(String category, Properties config) {
    this.config = config;
    panel = new JPanel(new BorderLayout());
    this.category = category;

    keyField = new JPasswordField();
    keyField.setColumns(30);

    comboBoxNode = new JComboBox<String>();
    comboBoxNode.setModel(new DefaultComboBoxModel<String>());
    comboBoxNode.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (comboBoxNode.getItemCount() == 0)
          return;
        int idx = comboBoxNode.getSelectedIndex();
        feedingNode = comboBoxNode.getItemAt(idx).toString();
        Node n = nodes.get(feedingNode);
        Sensor[] sensors = n.getSensors();
        Vector<String> sensorNames = new Vector<String>();
        for (int i = 0; i < sensors.length; i++)
          sensorNames.add(sensors[i].getId());
        comboBoxSensor
            .setModel(new DefaultComboBoxModel<String>(sensorNames));
        feedingSensor = comboBoxSensor.getItemAt(0).toString();
        loadFeedIDvalue();
      }
    });

    comboBoxSensor = new JComboBox<String>();
    comboBoxSensor.setModel(new DefaultComboBoxModel<String>());
    comboBoxSensor.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (comboBoxNode.getItemCount() == 0)
          return;
        int idx = comboBoxSensor.getSelectedIndex();
        feedingSensor = comboBoxSensor.getItemAt(idx).toString();
        loadFeedIDvalue();
      }
    });

    String[] opt = { "Raw", "Converted" };
    comboBoxRaw = new JComboBox<String>();
    comboBoxRaw.setModel(new DefaultComboBoxModel<String>(opt));
    comboBoxRaw.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int idx = comboBoxRaw.getSelectedIndex();
        String how = comboBoxRaw.getItemAt(idx).toString();
        if (how.equals("Raw"))
          feedRaw = true;
        else if (how.equals("Converted"))
          feedRaw = false;
      }
    });

    startButton = new JButton("Start Feeding");
    startButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String apikey = arrayToString(keyField.getPassword());
        if (apikey == null || "".equals(apikey)) {
          JOptionPane.showMessageDialog(null, "Missing API Key", "Error",
              JOptionPane.ERROR_MESSAGE);
          return;
        }
        doFeed = true;
        statusLabel.setText("Status: Feeding");
      }
    });

    stopButton = new JButton("Stop Feeding ");
    stopButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doFeed = false;
        statusLabel.setText("Status: Not feeding");
      }
    });

    setButton = new JButton("Set");
    setButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        storeFeedIDvalue();
      }
    });

    removeButton = new JButton("Remove");
    removeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        removeFeedIDvalue();
      }
    });

    feedIdField = new JTextField();
    feedIdField.setText(null);
    feedIdField.setColumns(11);

    statusLabel = new JLabel("Status: Not feeding");

    JPanel cosmPanel = new JPanel(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.gridx=0;
    c.gridy=0;
    c.gridwidth=1;
    c.weightx=0.5;
    c.fill=GridBagConstraints.NONE;
    c.anchor=GridBagConstraints.LINE_END;
    cosmPanel.add(new JLabel("API key "),c);

    c.gridx=1;
    c.gridy=0;
    c.gridwidth=2;
    c.fill=GridBagConstraints.HORIZONTAL;
    cosmPanel.add(keyField,c);
    
    c.gridx=0;
    c.gridy=1;
    c.gridwidth=3;
    c.fill=GridBagConstraints.HORIZONTAL;
    cosmPanel.add(new JSeparator(),c);
    
    c.gridx=0;
    c.gridy=2;
    c.gridwidth=1;
    c.fill=GridBagConstraints.NONE;
    c.anchor=GridBagConstraints.CENTER;
    cosmPanel.add(new JLabel("Node"),c);
    
    c.gridx=1;
    c.gridy=2;
    c.gridwidth=1;
    c.fill=GridBagConstraints.NONE;
    c.anchor=GridBagConstraints.CENTER;
    cosmPanel.add(new JLabel("Sensor"),c);
    
    c.gridx=0;
    c.gridy=3;
    c.gridwidth=1;
    c.fill=GridBagConstraints.HORIZONTAL;
    cosmPanel.add(comboBoxNode,c);
    
    c.gridx=1;
    c.gridy=3;
    c.gridwidth=1;
    c.fill=GridBagConstraints.HORIZONTAL;
    cosmPanel.add(comboBoxSensor,c);
    
    c.gridx=2;
    c.gridy=3;
    c.gridwidth=1;
    c.fill=GridBagConstraints.NONE;
    c.anchor=GridBagConstraints.CENTER;
    cosmPanel.add(removeButton,c);
    
    c.gridx=0;
    c.gridy=4;
    c.gridwidth=1;
    c.fill=GridBagConstraints.NONE;
    c.anchor=GridBagConstraints.CENTER;
    cosmPanel.add(new JLabel("Send values"),c);
    
    c.gridx=1;
    c.gridy=4;
    c.gridwidth=1;
    c.fill=GridBagConstraints.NONE;
    c.anchor=GridBagConstraints.CENTER;
    cosmPanel.add(new JLabel("Feed ID"),c);
    
    c.gridx=0;
    c.gridy=5;
    c.gridwidth=1;
    c.fill=GridBagConstraints.HORIZONTAL;
    cosmPanel.add(comboBoxRaw,c);
    
    c.gridx=1;
    c.gridy=5;
    c.gridwidth=1;
    c.fill=GridBagConstraints.HORIZONTAL;
    cosmPanel.add(feedIdField, c);
    
    c.gridx=2;
    c.gridy=5;
    c.gridwidth=1;
    c.fill=GridBagConstraints.NONE;
    c.anchor=GridBagConstraints.CENTER;
    cosmPanel.add(setButton, c);
    
    c.gridx=0;
    c.gridy=6;
    c.gridwidth=3;
    c.fill=GridBagConstraints.HORIZONTAL;
    cosmPanel.add(new JSeparator(),c);
      
   
    c.gridx=1;
    c.gridy=8;
    c.gridwidth=1;
    c.fill=GridBagConstraints.HORIZONTAL;
    c.anchor=GridBagConstraints.CENTER;
    cosmPanel.add(startButton, c);
    
    c.gridx=2;
    c.gridy=8;
    c.gridwidth=1;
    c.fill=GridBagConstraints.HORIZONTAL;
    c.anchor=GridBagConstraints.CENTER;
    cosmPanel.add(stopButton, c);
    
    panel.add(cosmPanel,BorderLayout.NORTH);
  }
  
  private void setConstraints(GridBagConstraints c, int gridx, int gridy, 
      int gridwidth, int fill, int anchor){
    c.gridx=gridx;
    c.gridy=gridy;
    c.gridwidth=gridwidth;
    c.fill=fill;
    c.anchor=anchor;
  }

  @Override
  public void clearNodeData() {
    if (!isVisible())
      return;
    nodes.clear();
    comboBoxNode.removeAllItems();
    comboBoxSensor.removeAllItems();
    feedIdField.setText(null);
  }

  public String getCategory() {
    return category;
  }

  public String getTitle() {
    return "sen.se Feeder";
  }

  public Component getPanel() {
    return panel;
  }

  @Override
  public void nodeAdded(Node node) {
	if (!isVisible()) return;
	String nodeID=node.getID();
	if (nodes.get(nodeID)!=null) return;
	SensorData sd = node.getLastSD();
	if (sd == null) return; // unknown node type
	nodes.put(nodeID, node);
	comboBoxNode.setModel(new DefaultComboBoxModel<String>(getSortedNodeList()));
	comboBoxNode.setSelectedItem(new String(nodeID));
  }
  
  private Vector<String> getSortedNodeList(){
	Vector<Node> list = new Vector<Node>();
    for (Object key : nodes.keySet()) {
      list.add(nodes.get(key));   
    }
    Node[] nodeList=list.toArray(new Node[0]); 
    Arrays.sort(nodeList); 
    return toStringList(nodeList);
  }
  
  private Vector<String> toStringList(Node[] nodeList){
    Vector<String> list = new Vector<String>();
	for (int i=0;i<nodeList.length;i++)
      list.add(nodeList[i].getID());
    return list;
  }

  @Override
  public void nodeDataReceived(SensorData sensorData) {
    if (!isVisible())
      return;
    Hashtable<String, String> feedTable = new Hashtable<String, String>();
    if (nodes.get(sensorData.getNodeID()) == null) {
      nodeAdded(sensorData.getNode());
    }

    if (doFeed) {
      Node n = sensorData.getNode();
      fillFeedTable(n, sensorData, feedTable);
      String key = arrayToString(keyField.getPassword());
      PublisherSense publisher = new PublisherSense(feedTable, key, this);
      publisher.start();
    }
  }

  void fillFeedTable(Node n, SensorData sd, Hashtable<String, String> feedTable) {
    Sensor[] sensors = n.getSensors();
    for (int i = 0; i < sensors.length; i++) {
      putValue(sensors[i].getId(), n, sd, feedTable);
    }
  }

  private void putValue(String sensorId, Node n, SensorData sd,
      Hashtable<String, String> feedTable) {

    String value;
    String id;
    Sensor s = n.getNodeSensor(sensorId);
    if ((id = s.getFeedID()) != null) {
      if (feedRaw) {
        value = Integer.toString(n.getLastValueOf(sensorId));
      } else {
        value = n.getRoundedConvOf(sensorId);
      }
      feedTable.put(id, value);
    }
  }

  @Override
  public void nodesSelected(Node[] node) {
    // Ignore
  }

  public void loadFeedIDvalue() {
    Node n;
    Sensor s;
    if (feedingNode == null || feedingSensor == null)
      return;
    if ((n = nodes.get(feedingNode)) == null)
      return;
    if ((s = n.getNodeSensor(feedingSensor)) == null)
      return;

    feedIdField.setText(s.getFeedID());
  }

  public void storeFeedIDvalue() {
    Node n;
    Sensor s;
    if (feedingNode == null || feedingSensor == null)
      return;
    if ((n = nodes.get(feedingNode)) == null)
      return;
    if ((s = n.getNodeSensor(feedingSensor)) == null)
      return;

    String id = feedIdField.getText();
    if (isValidFeedID(id))
      s.setFeedID(feedIdField.getText());
    else
      feedIdField.setText("Invalid feed ID");
  }

  public void removeFeedIDvalue() {
    Node n;
    Sensor s;
    if (feedingNode == null || feedingSensor == null)
      return;
    if ((n = nodes.get(feedingNode)) == null)
      return;
    if ((s = n.getNodeSensor(feedingSensor)) == null)
      return;

    s.setFeedID(null);
    feedIdField.setText(null);
  }

  public static boolean isValidFeedID(String id) {
    if (id == null || id.equals("") || !isInteger(id))
      return false;
    return true;
  }

  public static boolean isInteger(String str) {
    try {
      Integer.parseInt(str);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public static String arrayToString(char[] a) {
    StringBuffer result = new StringBuffer();
    if (a.length > 0) {
      result.append(a[0]);
      for (int i = 1; i < a.length; i++) {
        result.append(a[i]);
      }
    }
    return result.toString();
  }
  
  public void addResponseLine(final String text) {
	SwingUtilities.invokeLater(new Runnable() {
    public void run() {
    String current = logArea.getText();
    int len = current.length();
    if (len > 4096) {
      current = current.substring(len - 4096);
    }
    current = len > 0 ? (current + '\n' + text) : text;
    logArea.setText(current);
    logArea.setCaretPosition(current.length());
    }
   });
  }

  public void updateConfig(Properties config) {
    String id;
    for (Object key : nodes.keySet()) {
      Node n = nodes.get(key);
      Sensor[] sensors = n.getSensors();
      for (int i = 0; i < sensors.length; i++) {
        if ((id = sensors[i].getFeedID()) != null) {
          config.setProperty("feedsense," + n.getID() + ","
              + sensors[i].getId(), id);
        } else
          config.remove("feedsense," + n.getID() + "," + sensors[i].getId());
      }
    }
  }
}