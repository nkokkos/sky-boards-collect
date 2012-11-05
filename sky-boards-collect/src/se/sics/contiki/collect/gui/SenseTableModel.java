/*
 * DataFeederSense
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 04 Oct 2012
 */
package se.sics.contiki.collect.gui;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class SenseTableModel extends AbstractTableModel {
  private static final long serialVersionUID = 1L;
  public final int defInitialCapacity = 15;
  private int keyCol = 2;

  private String[] columnNames = { "Node", "Sensor", "Feed Id.",
      "Values", "Send" };

  private Vector<SenseRow> data = 
      new Vector<SenseRow>(defInitialCapacity);
  
  //Unique key: feed Id
  private HashSet<String> keySet=new HashSet<String>(); 

  Vector<SenseRow> getData() {
    return data;
  }

  public int getColumnCount() {
    return columnNames.length;
  }

  public int getRowCount() {
    return getData().size();
  }

  public String getColumnName(int col) {
    return columnNames[col];
  }

  public Object getValueAt(int row, int col) {
    if (getData().size()>0){
      return getData().get(row).getField(col);
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
    if (col==keyCol){
      String oldValue=(String) getData().get(row).getField(col);
      if (oldValue!=value){
        keySet.remove(oldValue);
        keySet.add((String) value);
      }
    }  
    getData().get(row).setField(col, value);  
    fireTableCellUpdated(row, col);
  }
  
  public void addRow(String node, String sensor, String feedId, String conv,
      boolean send){
    if (!keySet.contains(feedId)){
      data.add(new SenseRow(node, sensor, feedId, conv, send));
      keySet.add(feedId);
      fireTableDataChanged();
    }
  }
}

class SenseRow {
  Vector<Object> row = new Vector<Object>(5);
  private static Object[] classes = {"", "", "", "", false};

  public SenseRow(String node, String sensor, String feedId, String conv,
      boolean send) {
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
  
  public static Class<? extends Object> getClass(int c){
    return classes[c].getClass();
  }
}