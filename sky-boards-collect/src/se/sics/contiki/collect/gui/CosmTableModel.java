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

import javax.swing.table.AbstractTableModel;

public class CosmTableModel extends AbstractTableModel {
  private static final long serialVersionUID = 1L;
  public final int defInitialCapacity = 20;
  private int keyCol = 2;

  private String[] columnNames = { "Node", "Datastreams Id.", "Feed Id.", "Feed title", "Values",
      "Send" };

  private ArrayList<CosmRow> data = new ArrayList<CosmRow>(defInitialCapacity);

  // Unique key: feed Id
  private HashSet<String> keySet = new HashSet<String>();

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
      String oldValue = (String) data.get(row).getField(col);
      if (oldValue != value) {
        keySet.remove(oldValue);
        keySet.add((String) value);
      }
    }
    data.get(row).setField(col, value);
    fireTableCellUpdated(row, col);
  }

  public void addRow(String node, Hashtable<String,String> dataStreams, 
      String feedId, String feedTitle, String conv, boolean send) {
    if (!keySet.contains(feedId)) {
      int row = data.size();
      data.add(new CosmRow(node, dataStreams, feedId, feedTitle, conv, send));
      keySet.add(feedId);
      fireTableRowsInserted(row, row);
    }
  }

  public void deleteRow(int[] rows) {
    for (int i=rows.length-1;i>=0;i--) {
      int row=rows[i];
      String key=(String) (data.get(row).getField(CosmRow.IDX_FEEDID));
      keySet.remove(key);
      data.remove(row);
    }
    fireTableRowsDeleted(rows[0],rows[rows.length-1]);
  }
}

class CosmRow {
  ArrayList<Object> row;
  private static Object[] classes = { "", new Hashtable<String,String>(), "", "", "", true };
  public static final int IDX_NODE = 0;
  public static final int IDX_DATASTREAMS = 1;
  public static final int IDX_FEEDID = 2;
  public static final int IDX_FEEDTITLE = 3;
  public static final int IDX_CONV = 4;
  public static final int IDX_SEND = 5;

  public CosmRow(String node, Hashtable<String,String> dataStreams, String feedId, String feedTitle,
      String conv, boolean send) {
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