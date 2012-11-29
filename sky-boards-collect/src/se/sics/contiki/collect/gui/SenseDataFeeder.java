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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import se.sics.contiki.collect.Configurable;
import se.sics.contiki.collect.Node;
import se.sics.contiki.collect.PublisherSense;
import se.sics.contiki.collect.Sensor;
import se.sics.contiki.collect.SensorData;
import se.sics.contiki.collect.Visualizer;

public class SenseDataFeeder extends JPanel implements Visualizer, Configurable {

  private static final long serialVersionUID = 1L;
  String category;
  JPasswordField keyField;
  JButton addButton;
  JButton deleteButton;
  private JPanel panel;
  boolean doFeed = false;
  JLabel statusLabel;
  PublisherSense publisher;
  JTextArea logArea;
  SenseTableGUI senseTableGUI;
  SenseTableModel senseTableModel;
  Properties config;
  String apiKey;

  private Hashtable<String, Node> nodes = new Hashtable<String, Node>();

  public SenseDataFeeder(String category, Properties config) {
    panel = new JPanel(new BorderLayout());
    this.category = category;
    this.config = config;
    keyField = new JPasswordField();
    keyField.setColumns(30);

    addButton = new JButton("Add");
    addButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (nodes.isEmpty())
          return;
        JDialog dialog = new DialogAdd(nodes);
        dialog.setLocationRelativeTo(addButton);
        dialog.setVisible(true);
      }
    });

    deleteButton = new JButton("Delete");
    deleteButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        deleteSelectedRows();
      }
    });

    senseTableModel = new SenseTableModel(config);
    senseTableGUI = new SenseTableGUI(senseTableModel);

    logArea = new JTextArea(10,40);
    logArea.setEditable(false);

    statusLabel = new JLabel("Status: Not feeding");
    JPanel sensePanel = new JPanel(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
    c.weightx = 0;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.LINE_START;
    c.insets = new Insets(10, 20, 10, 0);
    sensePanel.add(new JLabel("API key "), c);

    c.gridx = 1;
    c.gridy = 0;
    c.gridwidth = 1;
    c.weightx = 0.5;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = new Insets(10, 0, 10, 20);
    keyField.setToolTipText("Copy your sen.se API key here");
    sensePanel.add(keyField, c);

    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 4;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(0, 0, 0, 0);
    sensePanel.add(new JSeparator(), c);

    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = 4;
    c.weighty = 0.5;
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.CENTER;
    c.insets = new Insets(10, 20, 5, 20);
    sensePanel.add(new JScrollPane(senseTableGUI), c);

    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth = 4;
    c.weighty = 0;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.CENTER;
    JPanel remsetPanel = new JPanel();
    remsetPanel.add(addButton);
    remsetPanel.add(deleteButton);
    c.insets = new Insets(5, 20, 10, 20);
    sensePanel.add(remsetPanel, c);

    c.gridx = 0;
    c.gridy = 4;
    c.gridwidth = 4;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(0, 0, 0, 0);
    sensePanel.add(new JSeparator(), c);

    c.gridx = 0;
    c.gridy = 5;
    c.gridwidth = 4;
    c.weighty = 0.5;
    c.fill = GridBagConstraints.BOTH;
    c.insets = new Insets(10, 20, 10, 20);
    sensePanel.add(new JScrollPane(logArea), c);

    panel.add(sensePanel, BorderLayout.CENTER);
  }

  @Override
  public void clearNodeData() {
    if (!isVisible())
      return;
    nodes.clear();
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
    if (!isVisible())
      return;
    String nodeID = node.getID();
    if (nodes.get(nodeID) != null)
      return;
    SensorData sd = node.getLastSD();
    if (sd == null)
      return; // unknown node type
    nodes.put(nodeID, node);
  }

  private Vector<String> getSortedNodeList() {
    Vector<Node> list = new Vector<Node>();
    for (Object key : nodes.keySet()) {
      list.add(nodes.get(key));
    }
    Node[] nodeList = list.toArray(new Node[0]);
    Arrays.sort(nodeList);
    return toStringList(nodeList);
  }

  private Vector<String> toStringList(Node[] nodeList) {
    Vector<String> list = new Vector<String>();
    for (int i = 0; i < nodeList.length; i++)
      list.add(nodeList[i].getID());
    return list;
  }

  @Override
  public void nodeDataReceived(SensorData sensorData) {
    
    if (nodes.isEmpty())
      return;
    
    if (nodes.get(sensorData.getNodeID()) == null) {
      nodeAdded(sensorData.getNode());
      return;
    }

    SenseRow row;
    Hashtable<String, String> feedTable = new Hashtable<String, String>();
    Node node = sensorData.getNode();
    String nodeId=node.getID();
    if (!getAPIKey())
      return;
    ArrayList<SenseRow> FeedRows = senseTableModel.getRows(nodeId);
    if (FeedRows==null)
      return;
    ListIterator<SenseRow> it = FeedRows.listIterator();
    PublisherSense publisher;

    while (it.hasNext()) {
      row = (SenseRow) it.next();
      if ((boolean) row.getField(SenseRow.IDX_SEND)) {
        putValue(node, row, feedTable);
      }
    }
    publisher = new PublisherSense(feedTable, apiKey, this);
    publisher.setFeedingNode(nodeId);
    publisher.start();
  }

  private void putValue(Node node, SenseRow row,
      Hashtable<String, String> feedTable) {
    String value = "";
    String sensorId = (String) row.getField(SenseRow.IDX_SENSOR);

    if (row.getField(SenseRow.IDX_CONV).equals("Converted")) {
      value = Double.toString(node.getLastValueOf(sensorId));
    } else if (row.getField(SenseRow.IDX_CONV).equals("Raw")) {
      value = node.getRoundedConvOf(sensorId);
    }
    feedTable.put((String) row.getField(SenseRow.IDX_FEEDID), value);
  }
  
  private boolean  getAPIKey(){
    apiKey = arrayToString(keyField.getPassword());
    if (apiKey == null || "".equals(apiKey)) {
      return false;
    }
    return true;
  }

  @Override
  public void nodesSelected(Node[] node) {
    senseTableGUI.selectRows(node);
  }

  public void deleteSelectedRows() {
    int[] selectedRows = senseTableGUI.getSelectedRows();
    if (selectedRows.length == 0)
      return;
    int opt = JOptionPane.showConfirmDialog(deleteButton, "Delete "
        + selectedRows.length + " row(s)?", "Confirm delete",
        JOptionPane.YES_NO_OPTION);
    if (opt == JOptionPane.YES_OPTION) {
      ArrayList<String> delList;
      delList = senseTableModel.deleteRows(selectedRows);
      for (int i = 0; i < delList.size(); i++) {
        config.remove("feedsense," + delList.get(i));
      }
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
        if (logArea.getText().length()>4096) 
          logArea.setText("");
        logArea.append(text+"\n");
        logArea.setCaretPosition(logArea.getText().length());
      }
    });
  }

  /**
   * Configuration line format (key=value)
   * 
   * feedsense,<feedId> = <node>,<sensor>,<conv>,<send>
   * 
   */
  public void updateConfig(Properties config) {
    ListIterator<SenseRow> li = ((SenseTableModel) senseTableGUI.getModel())
        .getListIterator();
    while (li.hasNext()) {
      SenseRow sr = li.next();
      StringBuilder value = new StringBuilder();
      String key = "feedsense," + (String) sr.getField(SenseRow.IDX_FEEDID);
      value.append(sr.getField(SenseRow.IDX_NODE) + ","
          + sr.getField(SenseRow.IDX_SENSOR) + ","
          + sr.getField(SenseRow.IDX_CONV) + ","
          + sr.getField(SenseRow.IDX_SEND));
      config.setProperty(key, value.toString());
    }
  }

  public void loadConfigLine(String key, String value) {
    String[] SKey = key.split(",");
    String[] SVal = value.split(",");
    boolean send = Boolean.parseBoolean(SVal[3]);
    senseTableModel.addRow(SVal[0], SVal[1], SKey[1], SVal[2], send);
  }

  private class DialogAdd extends JDialog {
    private static final long serialVersionUID = 1L;
    private JComboBox<String> comboBoxNode;
    private JComboBox<String> comboBoxSensor;
    private JComboBox<String> comboBoxRaw;
    JTextField feedIdField;
    String feedConv = "Converted";
    String feedingNode;
    String feedingSensor;
    JButton OKbutton;
    JPanel pane;

    public DialogAdd(final Hashtable<String, Node> nodes) {
      super();
      pane = new JPanel();
      pane.setLayout(new GridBagLayout());
      pane.setOpaque(true);

      feedIdField = new JTextField();
      OKbutton = new JButton("Add");
      OKbutton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String id = feedIdField.getText();
          if (isValidFeedID(id)) {
            senseTableModel.addRow(feedingNode, feedingSensor, id, feedConv,
                true);
            closeWindow();
          } else
            JOptionPane.showMessageDialog(pane, "Invalid feed ID", "Error",
                JOptionPane.ERROR_MESSAGE);
        }
      });
      JButton cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          closeWindow();
        }
      });
      comboBoxNode = new JComboBox<String>();
      comboBoxNode.setModel(new DefaultComboBoxModel<String>(
          getSortedNodeList()));
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
          comboBoxSensor.setSelectedIndex(0);
          feedingSensor = comboBoxSensor.getItemAt(0).toString();
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
        }
      });

      String[] opt = { "Converted", "Raw" };
      comboBoxRaw = new JComboBox<String>();
      comboBoxRaw.setModel(new DefaultComboBoxModel<String>(opt));
      comboBoxRaw.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          int idx = comboBoxRaw.getSelectedIndex();
          feedConv = comboBoxRaw.getItemAt(idx).toString();
        }
      });

      GridBagConstraints c = new GridBagConstraints();
      c.gridx = 0;
      c.gridy = 0;
      c.weightx = 0;
      c.weighty = 0.1;
      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.LINE_END;
      c.insets = new Insets(10, 0, 0, 0);
      pane.add(new JLabel("Node"), c);

      c.gridx = 1;
      c.gridy = 0;
      c.weightx = 0.1;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.insets = new Insets(10, 5, 0, 10);
      pane.add(comboBoxNode, c);

      c.gridx = 0;
      c.gridy = 1;
      c.weightx = 0;
      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.LINE_END;
      c.insets = new Insets(10, 0, 0, 0);
      pane.add(new JLabel("Sensor"), c);

      c.gridx = 1;
      c.gridy = 1;
      c.weightx = 0.1;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.insets = new Insets(10, 5, 0, 10);
      pane.add(comboBoxSensor, c);

      c.gridx = 0;
      c.gridy = 2;
      c.weightx = 0;
      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.LINE_END;
      c.insets = new Insets(10, 0, 0, 0);
      pane.add(new JLabel("Send values"), c);

      c.gridx = 1;
      c.gridy = 2;
      c.weightx = 0.1;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.insets = new Insets(10, 5, 0, 10);
      pane.add(comboBoxRaw, c);

      c.gridx = 0;
      c.gridy = 3;
      c.weightx = 0;
      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.LINE_END;
      c.insets = new Insets(10, 10, 0, 0);
      pane.add(new JLabel("Feed identifier"), c);

      c.gridx = 1;
      c.gridy = 3;
      c.weightx = 0.1;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.insets = new Insets(10, 5, 0, 10);
      pane.add(feedIdField, c);

      c.gridx = 0;
      c.gridy = 4;
      c.gridwidth = 2;
      c.weightx = 0.1;
      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.CENTER;
      JPanel groupPanel = new JPanel();
      groupPanel.add(OKbutton);
      groupPanel.add(cancelButton);
      c.insets = new Insets(10, 0, 10, 0);
      pane.add(groupPanel, c);

      setContentPane(pane);
      if (comboBoxNode.getItemCount() > 0)
        comboBoxNode.setSelectedIndex(0);
      pack();
      setTitle("Feed configuration");
      setModalityType(ModalityType.APPLICATION_MODAL);
    }

    void closeWindow() {
      dispose();
    }
  }

  public static boolean isValidFeedID(String id) {
    if (id == null || id.equals("") || !isInteger(id)
        || Integer.valueOf(id) < 0)
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
}
