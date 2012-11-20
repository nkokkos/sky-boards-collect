/*
 * CosmTableModel
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 14 Oct 2012
 */
package se.sics.contiki.collect.gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Properties;

import javax.swing.table.AbstractTableModel;

public class CosmTableModel extends AbstractTableModel {
  private static final long serialVersionUID = 1L;
  public final int defInitialCapacity = 20;
  private int keyCol = 2;
  private int titleCol = 3;
  private String[] columnNames = { "Node", "Datastream ID's", "Feed ID",
      "Feed title", "Values", "Send" };
  private ArrayList<CosmRow> data = new ArrayList<CosmRow>(defInitialCapacity);
  // Unique key: feed Id
  private HashSet<String> keySet = new HashSet<String>();
  Properties configFile;
  private Hashtable<String, ArrayList<Integer>> nodeRowIndex = new Hashtable<String, ArrayList<Integer>>();

  public CosmTableModel(Properties configFile){
    this.configFile = configFile;
  }
  public ListIterator<CosmRow> getListIterator() {
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
    return CosmRow.getClass(c);
  }

  public boolean isCellEditable(int row, int col) {
    if (col == 0)
      return false;
    else
      return true;
  }

  public void setValueAt(Object value, int row, int col) {
    if (col == keyCol) {
      if (keySet.contains(value))
        return;
      if (!CosmDataFeeder.isValidFeedID((String) value))
        return;
      String oldValue = (String) data.get(row).getField(col);
      if (!oldValue.equals(value)) {
        keySet.remove(oldValue);
        deleteFromConfigFile(oldValue);
        keySet.add((String) value);
      }
    } else if (col == titleCol) {
      if (!CosmDataFeeder.isValidFeedTitle((String) value))
        return;
    }

    data.get(row).setField(col, value);
    fireTableCellUpdated(row, col);
  }

  private void deleteFromConfigFile(String key) {
    configFile.remove("feedcosm," + key);
  }

  public void addRow(String nodeId, Hashtable<String, String> dataStreams,
      String feedId, String feedTitle, String conv, boolean send) {
    if (!keySet.contains(feedId)) {
      int row = data.size();
      data.add(new CosmRow(nodeId, dataStreams, feedId, feedTitle, conv, send));
      keySet.add(feedId);
      addToNodeRowIndex(nodeId, row);
      fireTableRowsInserted(row, row);
    }
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
      String key = (String) (data.get(row).getField(CosmRow.IDX_FEEDID));
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
    CosmRow row;
    for (int i = 0; i < data.size(); i++) {
      row = data.get(i);
      addToNodeRowIndex((String) row.getField(SenseRow.IDX_NODE), i);
    }
  }

  public ArrayList<CosmRow> getRows(String nodeId) {
    ArrayList<Integer> nodeRows = nodeRowIndex.get(nodeId);
    ArrayList<CosmRow> rows = new ArrayList<CosmRow>();
    for (int i = 0; i < nodeRows.size(); i++) {
      rows.add(data.get(nodeRows.indexOf(i)));
    }
    return rows;
  }

  public ArrayList<Integer> getRowsOfIndex(String nodeId) {
    return nodeRowIndex.get(nodeId);
  }
}

class CosmRow {
  ArrayList<Object> row;
  private static Object[] classes = { "", new Hashtable<String, String>(), "",
      "", "", true };
  public static final int IDX_NODE = 0;
  public static final int IDX_DATASTREAMS = 1;
  public static final int IDX_FEEDID = 2;
  public static final int IDX_FEEDTITLE = 3;
  public static final int IDX_CONV = 4;
  public static final int IDX_SEND = 5;

  public CosmRow(String node, Hashtable<String, String> dataStreams,
      String feedId, String feedTitle, String conv, boolean send) {
    row = new ArrayList<Object>(6);
    row.add(node);
    row.add(dataStreams);
    row.add(feedId);
    row.add(feedTitle);
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
}