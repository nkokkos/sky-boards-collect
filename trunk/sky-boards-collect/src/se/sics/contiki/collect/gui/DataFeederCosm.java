/*
 * DataFeederCosm
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 28 jul 2012
 */

package se.sics.contiki.collect.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
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
import se.sics.contiki.collect.PublisherCosm;
import se.sics.contiki.collect.Sensor;
import se.sics.contiki.collect.SensorData;
import se.sics.contiki.collect.Visualizer;

public class DataFeederCosm extends JPanel implements Visualizer, Configurable {

  private static final long serialVersionUID = 1L;
  String category;
  JPasswordField keyField;
  JButton startButton;
  JButton stopButton;
  JButton addButton;
  JButton deleteButton;
  private JPanel panel;
  boolean doFeed = false;
  JLabel statusLabel;
  PublisherCosm publisher;
  JTextArea logArea;
  CosmTableGUI cosmTableGUI;
  CosmTableModel cosmTableModel;

  private Hashtable<String, Node> nodes = new Hashtable<String, Node>();

  public DataFeederCosm(String category, Properties config) {
    panel = new JPanel(new BorderLayout());
    this.category = category;

    keyField = new JPasswordField();
    keyField.setColumns(30);

    startButton = new JButton("Start Feeding");
    startButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String apikey = arrayToString(keyField.getPassword());
        if (apikey == null || "".equals(apikey)) {
          JOptionPane.showMessageDialog(startButton, "Missing API Key",
              "Error", JOptionPane.ERROR_MESSAGE);
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

    addButton = new JButton("Add");
    addButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            (new DialogAdd(nodes)).setLocationRelativeTo(addButton);
          }
        });
      }
    });

    deleteButton = new JButton("Delete");
    deleteButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        deleteSelectedRows();
      }
    });

    cosmTableModel = new CosmTableModel();
    cosmTableGUI = new CosmTableGUI(cosmTableModel);

    logArea = new JTextArea();
    logArea.setEditable(false);
    logArea.setPreferredSize(new Dimension(400, 250));

    statusLabel = new JLabel("Status: Not feeding");
    JPanel cosmPanel = new JPanel(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
    c.weightx = 0;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.LINE_START;
    c.insets = new Insets(10, 20, 10, 0);
    cosmPanel.add(new JLabel("API key "), c);

    c.gridx = 1;
    c.gridy = 0;
    c.gridwidth = 1;
    c.weightx = 0.5;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = new Insets(10, 0, 10, 20);
    keyField.setToolTipText("Copy your Cosm API key here");
    cosmPanel.add(keyField, c);

    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 4;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(0, 0, 0, 0);
    cosmPanel.add(new JSeparator(), c);

    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = 4;
    c.weighty = 0.5;
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.CENTER;
    c.insets = new Insets(10, 20, 5, 20);
    cosmPanel.add(new JScrollPane(cosmTableGUI), c);

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
    cosmPanel.add(remsetPanel, c);

    c.gridx = 0;
    c.gridy = 4;
    c.gridwidth = 4;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(0, 0, 0, 0);
    cosmPanel.add(new JSeparator(), c);

    c.gridx = 0;
    c.gridy = 5;
    c.gridwidth = 4;
    c.weighty = 0.5;
    c.fill = GridBagConstraints.BOTH;
    c.insets = new Insets(10, 20, 5, 20);
    logArea.setText("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
    cosmPanel.add(new JScrollPane(logArea), c);

    c.gridx = 0;
    c.gridy = 6;
    c.gridwidth = 4;
    c.weighty = 0;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.CENTER;
    JPanel startStopPanel = new JPanel();
    startStopPanel.add(startButton);
    startStopPanel.add(stopButton);
    c.insets = new Insets(5, 20, 10, 20);
    cosmPanel.add(startStopPanel, c);

    panel.add(cosmPanel, BorderLayout.CENTER);
    loadConfig(config);
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
    return "Cosm Feeder";
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
    if (!isVisible())
      return;
    Hashtable<String, String> feedTable = new Hashtable<String, String>();
    if (nodes.get(sensorData.getNodeID()) == null) {
      nodeAdded(sensorData.getNode());
      return;
    }

    if (doFeed) {
      Node n = sensorData.getNode();
      fillFeedTable(n, sensorData, feedTable);
      String key = arrayToString(keyField.getPassword());
      PublisherCosm publisher = new PublisherCosm(feedTable, key, n.getFeedID(),this);
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
  }

  @Override
  public void nodesSelected(Node[] node) {
    // ignore for now
  }

  public void deleteSelectedRows() {
    int[] selectedRows = cosmTableGUI.getSelectedRows();
    if (selectedRows.length == 0)
      return;
    int opt = JOptionPane.showConfirmDialog(deleteButton, "Delete "
        + selectedRows.length + " row(s)?", "Confirm delete",
        JOptionPane.YES_NO_OPTION);
    if (opt == JOptionPane.YES_OPTION) {
      cosmTableModel.deleteRow(selectedRows);
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

  /**
   * Configuration line format (key=value)
   * 
   * feedcosm,<feedId>,<datastream> = <node>,<feedTitle>,<conv>,<send>
   * 
   */
  public void updateConfig(Properties config) {/*
    ListIterator<CosmRow> li = ((CosmTableModel) cosmTableGUI.getModel())
        .getListIterator();
    while (li.hasNext()) {
      CosmRow sr = li.next();
      StringBuilder value = new StringBuilder();
      String key = "feedcosm," + (String) sr.getField(CosmRow.IDX_FEEDID);
      value.append(sr.getField(CosmRow.IDX_NODE) + ","
          + sr.getField(CosmRow.IDX_SENSOR) + ","
          + sr.getField(CosmRow.IDX_CONV) + ","
          + sr.getField(CosmRow.IDX_SEND));
      config.setProperty(key, value.toString());
    }
  */}

  public void loadConfig(Properties config) {/*
    for (Object k : config.keySet()) {
      String key=k.toString();
      if (((String) key).startsWith("feedcosm")) {
        String[] SKey=key.split(",");
        String[] SVal=config.getProperty(key).split(",");
        boolean send=Boolean.parseBoolean(SVal[3]);
        cosmTableModel.addRow(SVal[0], SVal[1], SKey[1], SVal[2], send);
      }
    }
  */}

  private class DialogAdd extends JFrame {
    private static final long serialVersionUID = 1L;
    private JComboBox<String> comboBoxNode;
    private JComboBox<String> comboBoxRaw;
    private Hashtable<String,String> dataStreams;
    JTextField feedIdField;
    JTextField feedTitleField;
    String feedConv = "Converted";
    String feedingNode;
    JButton OKbutton;
    JPanel pane;

    public DialogAdd(final Hashtable<String, Node> nodes) {
      super();
      pane = new JPanel();
      pane.setLayout(new GridBagLayout());
      pane.setOpaque(true);

      feedIdField = new JTextField();
      feedTitleField = new JTextField();
      dataStreams = new Hashtable<String,String>();
      OKbutton = new JButton("Add");
      OKbutton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String id = feedIdField.getText();
          String title= feedTitleField.getText();
          if (isValidFeedID(id)) {
            cosmTableModel.addRow(feedingNode, dataStreams, id, title,feedConv,
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
          /* DEBUG LINES */

          /* DEBUG LINES */
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
          for (int i = 0; i < sensors.length; i++){
            String sensorId=sensors[i].getId();
            dataStreams.put(sensorId, sensorId);
          }
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
      c.anchor = GridBagConstraints.LINE_START;
      pane.add(new JLabel("Node"), c);

      c.gridx = 1;
      c.gridy = 0;
      c.weightx = 0.1;
      c.fill = GridBagConstraints.HORIZONTAL;
      pane.add(comboBoxNode, c);
      
      c.gridx = 0;
      c.gridy = 1;
      c.weightx = 0;
      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.LINE_START;
      pane.add(new JLabel("Feed identifier "), c);

      c.gridx = 1;
      c.gridy = 1;
      c.weightx = 0.1;
      c.fill = GridBagConstraints.HORIZONTAL;
      pane.add(feedIdField, c);
      
      c.gridx = 0;
      c.gridy = 2;
      c.weightx = 0;
      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.LINE_START;
      pane.add(new JLabel("Feed Title"), c);

      c.gridx = 1;
      c.gridy = 2;
      c.weightx = 0.1;
      c.fill = GridBagConstraints.HORIZONTAL;
      pane.add(feedTitleField, c);
      
      c.gridx = 0;
      c.gridy = 3;
      c.weightx = 0;
      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.LINE_START;
      pane.add(new JLabel("Send values"), c);

      c.gridx = 1;
      c.gridy = 3;
      c.weightx = 0.1;
      c.fill = GridBagConstraints.HORIZONTAL;
      pane.add(comboBoxRaw, c);

      c.gridx = 0;
      c.gridy = 4;
      c.gridwidth = 2;
      c.weightx = 0.1;
      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.CENTER;
      JPanel groupPanel = new JPanel();
      groupPanel.add(OKbutton);
      groupPanel.add(cancelButton);
      pane.add(groupPanel, c);

      setContentPane(pane);
      setPreferredSize(new Dimension(300, 250));
      // this.setDefaultCloseOperation();
      setVisible(true);
      setTitle("Feed configuration");
      pack();

      if (comboBoxNode.getItemCount() > 0)
        comboBoxNode.setSelectedIndex(0);
    }

    public boolean isValidFeedID(String id) {
      if (id == null || id.equals("") || !isInteger(id)
          || Integer.valueOf(id) < 0)
        return false;
      return true;
    }

    public boolean isInteger(String str) {
      try {
        Integer.parseInt(str);
        return true;
      } catch (NumberFormatException e) {
        return false;
      }
    }

    void closeWindow() {
      this.setEnabled(false);
      this.dispose();
    }
  }
}
