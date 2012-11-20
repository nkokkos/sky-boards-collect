/*
 * SenseTableModel
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 04 Oct 2012
 */
package se.sics.contiki.collect.gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class SenseTableModel extends AbstractTableModel {
  private static final long serialVersionUID = 1L;
  public final int defInitialCapacity = 20;
  private int keyCol = 2;
  private String[] columnNames = { "Node", "Sensor", "Feed ID", "Value", "Send" };
  private Vector<SenseRow> data = new Vector<SenseRow>(defInitialCapacity);
  // Unique key: feed Id
  private HashSet<String> keySet = new HashSet<String>();
  Properties configFile;
  private Hashtable<String, ArrayList<Integer>> nodeRowIndex = new Hashtable<String, ArrayList<Integer>>();

  public SenseTableModel(Properties configFile) {
    this.configFile = configFile;
  }

  public ListIterator<SenseRow> getListIterator() {
    return data.listIterator();
  }

  public int getColumnCount() {
    return columnNames.length;
  }

  public int getRowCount() {
    return data.size();
  }

  public String getColumnName(int col) {
    return columnNames[col];
  }

  public Object getValueAt(int row, int col) {
    if (data.size() > 0) {
      return data.get(row).getField(col);
    }
    return null;
  }

  public Class<? extends Object> getColumnClass(int c) {
    return SenseRow.getClass(c);
  }

  public boolean isCellEditable(int row, int col) {
    if (col < 2) {
      return false;
    } else {
      return true;
    }
  }

  public void setValueAt(Object value, int row, int col) {
    if (col == keyCol) {
      if (keySet.contains(value))
        return;
      if (!SenseDataFeeder.isValidFeedID((String) value))
        return;
      String oldValue = (String) data.get(row).getField(col);
      if (!oldValue.equals(value)) {
        keySet.remove(oldValue);
        deleteFromConfigFile(oldValue);
        keySet.add((String) value);
      }
    }
    data.get(row).setField(col, value);
    fireTableCellUpdated(row, col);
  }

  private void deleteFromConfigFile(String key) {
    configFile.remove("feedsense," + key);
  }

  public void addRow(String nodeId, String sensor, String feedId, String conv,
      boolean send) {
    if (keySet.contains(feedId))
      return;

    int row = data.size();
    data.add(new SenseRow(nodeId, sensor, feedId, conv, send));
    keySet.add(feedId);
    addToNodeRowIndex(nodeId, row);
    fireTableRowsInserted(row, row);
  }

  private void addToNodeRowIndex(String nodeId, int rowIndex) {
    ArrayList<Integer> nodeRows;

    if ((nodeRows = nodeRowIndex.get(nodeId)) == null) {
      nodeRows = new ArrayList<Integer>();
      nodeRowIndex.put(nodeId, nodeRows);
    }
    nodeRows.add(rowIndex);
  }

  public ArrayList<String> deleteRows(int[] rows) {
    ArrayList<String> delList = new ArrayList<String>();
    for (int i = rows.length - 1; i >= 0; i--) {
      int row = rows[i];
      String key = (String) (data.get(row).getField(SenseRow.IDX_FEEDID));
      keySet.remove(key);
      delList.add(key);
      data.remove(row);
    }
    remakeNodeRowIndex();
    fireTableRowsDeleted(rows[0], rows[rows.length - 1]);
    return delList;
  }

  private void remakeNodeRowIndex() {
    nodeRowIndex.clear();
    SenseRow row;
    for (int i = 0; i < data.size(); i++) {
      row = data.get(i);
      addToNodeRowIndex((String) row.getField(SenseRow.IDX_NODE), i);
    }
  }

  public ArrayList<SenseRow> getRows(String nodeId) {
    ArrayList<Integer> nodeRows = nodeRowIndex.get(nodeId);
    ArrayList<SenseRow> rows = new ArrayList<SenseRow>();
    for (int i = 0; i < nodeRows.size(); i++) {
      rows.add(data.get(nodeRows.indexOf(i)));
    }
    return rows;
  }

  public ArrayList<Integer> getRowsOfIndex(String nodeId) {
    return nodeRowIndex.get(nodeId);
  }
}

class SenseRow {
  Vector<Object> row;
  private static Object[] classes = { "", "", "", "", true };
  public static final int IDX_NODE = 0;
  public static final int IDX_SENSOR = 1;
  public static final int IDX_FEEDID = 2;
  public static final int IDX_CONV = 3;
  public static final int IDX_SEND = 4;

  public SenseRow(String node, String sensor, String feedId, String conv,
      boolean send) {
    row = new Vector<Object>(5);
    row.add(node);
    row.add(sensor);
    row.add(feedId);
    row.add(conv);
    row.add(send);
  }

  public Object getField(int index) {
    return row.get(index);
  }

  public void setField(int index, Object value) {
    row.set(index, value);
  }

  public static Class<? extends Object> getClass(int c) {
    return classes[c].getClass();
  }

  public SenseRow clone() {
    return new SenseRow((String) row.get(0), (String) row.get(1),
        (String) row.get(2), (String) row.get(3), (boolean) row.get(4));
  }
}