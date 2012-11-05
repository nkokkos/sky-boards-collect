/*
 * DataFeederSense
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 04 Oct 2012
 */
package se.sics.contiki.collect.gui;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class SenseTableModel extends AbstractTableModel {
  private static final long serialVersionUID = 1L;
  public final int defInitialCapacity = 15;

  private String[] columnNames = { "Node", "Sensor", "Feed Id.",
      "Values", "Send" };

  private Vector<SenseRow> data = 
      new Vector<SenseRow>(defInitialCapacity);

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
    return getData().get(row).getField(col);
  }

  /*
   * JTable uses this method to determine the default renderer/ editor for each
   * cell. If we didn't implement this method, then the last column would
   * contain text ("true"/"false"), rather than a check box.
   */
  public Class<? extends Object> getColumnClass(int c) {
    return getValueAt(0, c).getClass();
  }

  /*
   * Don't need to implement this method unless your table's editable.
   */
  public boolean isCellEditable(int row, int col) {
    // Note that the data/cell address is constant,
    // no matter where the cell appears on screen.
    if (col < 2) {
      return false;
    } else {
      return true;
    }
  }

  /*
   * Don't need to implement this method unless your table's data can change.
   */
  public void setValueAt(Object value, int row, int col) {
    getData().get(row).setField(col, value);
    fireTableCellUpdated(row, col);
  }
  
  public void addRow(String node, String sensor, String feedId, String conv,
      boolean send){
    data.add(new SenseRow(node, sensor, feedId, conv, send));
    fireTableDataChanged();
  }
}

class SenseRow {
  Vector<Object> row = new Vector<Object>(5);

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
}