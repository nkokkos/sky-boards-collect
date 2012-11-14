/*
 * CosmTableGUI
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 14 Oct 2012
 */

package se.sics.contiki.collect.gui;

import java.awt.Dimension;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

public class CosmTableGUI extends JTable {
  private static final long serialVersionUID = 1L;
  private int[] selectedRows;

  public CosmTableGUI(CosmTableModel tableModel) {
    super(tableModel);
    setUpValuesColumn();
    setUpDataStreamsColumn();
    setPreferredScrollableViewportSize(new Dimension(400, 250));
    setFillsViewportHeight(true);
    setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    setAutoCreateRowSorter(true);
    setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    setRowSelectionAllowed(true);
    setColumnSelectionAllowed(false);
    getSelectionModel().addListSelectionListener(new RowListener());
  }

  public void setUpDataStreamsColumn() {
    JComboBox<String> comboBox = new JComboBox<String>();
    comboBox.addItem("Default");
    comboBox.addItem("Custom");
    TableColumn valuesCol = getColumnModel().getColumn(1);
    valuesCol.setCellEditor(new DefaultCellEditor(comboBox));
    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    valuesCol.setCellRenderer(renderer);
  }
  
  public void setUpValuesColumn() {
    JComboBox<String> comboBox = new JComboBox<String>();
    comboBox.addItem("Raw");
    comboBox.addItem("Converted");
    TableColumn valuesCol = getColumnModel().getColumn(4);
    valuesCol.setCellEditor(new DefaultCellEditor(comboBox));
    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    valuesCol.setCellRenderer(renderer);
  }

  public void HandleSelectionEvent() {
    selectedRows = getSelectedRows();
    for (int i=0;i<selectedRows.length;i++){
      selectedRows[i]=convertRowIndexToModel(selectedRows[i]);
    }
  }

  private class RowListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent event) {
      if (event.getValueIsAdjusting()) {
        return;
      }
      HandleSelectionEvent();
    }
  }
}
