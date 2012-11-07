/*
 * SenseTableGUI
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 03 Oct 2012
 */

package se.sics.contiki.collect.gui;

import java.awt.Dimension;
import java.awt.Event;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

public class SenseTableGUI extends JTable {
  private static final long serialVersionUID = 1L;
  private DataFeederSense senseUI;
  private int selectedRow;

  public SenseTableGUI(DataFeederSense senseUI, SenseTableModel tableModel) {
    super(tableModel);
    this.senseUI = senseUI;
    setUpValuesColumn();
    setPreferredScrollableViewportSize(new Dimension(400, 250));
    setFillsViewportHeight(true);
    setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    setAutoCreateRowSorter(true);
    setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    setRowSelectionAllowed(true);
    setColumnSelectionAllowed(false);
    getSelectionModel().addListSelectionListener(new RowListener());
    getModel().addTableModelListener(new DataChangeListener());
  }

  public void setUpValuesColumn() {
    JComboBox<String> comboBox = new JComboBox<String>();
    comboBox.addItem("Raw");
    comboBox.addItem("Converted");
    TableColumn valuesCol = getColumnModel().getColumn(3);
    valuesCol.setCellEditor(new DefaultCellEditor(comboBox));

    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    renderer.setToolTipText("Click for combo box");
    valuesCol.setCellRenderer(renderer);
  }

  public void HandleSelectionEvent() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        int[] selectedRows = getSelectedRows();
        if (selectedRows.length == 0)
          return;
        selectedRow = selectedRows[0];
        SenseTableModel model = (SenseTableModel) getModel();
        int mRow = convertRowIndexToModel(selectedRow);

        String node = (String) model.getValueAt(mRow, SenseRow.IDX_NODE);
        String sensor = (String) model.getValueAt(mRow, SenseRow.IDX_SENSOR);
        String feedid = (String) model.getValueAt(mRow, SenseRow.IDX_FEEDID);
        String conv = (String) model.getValueAt(mRow, SenseRow.IDX_CONV);
        boolean send = (boolean) model.getValueAt(mRow, SenseRow.IDX_SEND);
        SenseRow sr = new SenseRow(node, sensor, feedid, conv, send);
        senseUI.updateFeedConfigPanel(sr);
      }
    });
  }

  public void HandleChangeEvent(int row, int col) {
    SwingUtilities.invokeLater(new ChangeEventHandler(row,col));
  }
  
  private class ChangeEventHandler implements Runnable{
    int row,col;
    ChangeEventHandler(int row, int col){
      this.row=row;
      this.col=col;
    }
    public void run() {
      SenseTableModel model = (SenseTableModel) getModel();
      int mRow = convertRowIndexToModel(row);
      switch (col) {
      case SenseRow.IDX_FEEDID:
        String id = (String) model.getValueAt(mRow, SenseRow.IDX_FEEDID);
        senseUI.updateFeedIdField(id);
        break;
      case SenseRow.IDX_CONV:
        String conv = (String) model.getValueAt(mRow, SenseRow.IDX_CONV);
        senseUI.updateConvComboBox(conv);
        break;
      }
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

  private class DataChangeListener implements TableModelListener {
    public void tableChanged(TableModelEvent e) {
      if (e.getType() != TableModelEvent.UPDATE)
        return;
      int col = e.getColumn();
      HandleChangeEvent(selectedRow, col);
    }
  }
}
