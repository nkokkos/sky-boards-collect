/*
 * SenseTableGUI
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 03 Oct 2012
 */

package se.sics.contiki.collect.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import se.sics.contiki.collect.Node;

public class SenseTableGUI extends JTable {
  private static final long serialVersionUID = 1L;
  private int[] selectedRows;

  public SenseTableGUI(SenseTableModel tableModel) {
    super(tableModel);
    setUpValuesColumn();
    //setUpSendColumn();
    setPreferredScrollableViewportSize(new Dimension(400, 250));
    setFillsViewportHeight(true);
    setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    setAutoCreateRowSorter(true);
    setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    setRowSelectionAllowed(true);
    setColumnSelectionAllowed(false);
    getSelectionModel().addListSelectionListener(new RowListener());
  }

  public void setUpValuesColumn() {
    JComboBox<String> comboBox = new JComboBox<String>();
    comboBox.addItem("Raw");
    comboBox.addItem("Converted");
    TableColumn valuesCol = getColumnModel().getColumn(SenseRow.IDX_CONV);
    valuesCol.setCellEditor(new DefaultCellEditor(comboBox));
    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    valuesCol.setCellRenderer(renderer);
  }
  
  public void setUpSendColumn(){
    TableColumn sendCol = getColumnModel().getColumn(SenseRow.IDX_SEND);
    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    
    JPopupMenu popupMenu = new JPopupMenu();
    JMenuItem item = new JMenuItem("Check all");
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SenseTableModel model = (SenseTableModel) getModel();
        int size = model.getRowCount();
        for (int i=0;i<size;i++){
          model.setValueAt(true, i, SenseRow.IDX_SEND);
        }
      }
    });
    popupMenu.add(item);
    
    item = new JMenuItem("Uncheck all");
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SenseTableModel model = (SenseTableModel) getModel();
        int size = model.getRowCount();
        for (int i=0;i<size;i++){
          model.setValueAt(false, i, SenseRow.IDX_SEND);
        }
      }
    });
    popupMenu.add(item);
    renderer.setComponentPopupMenu(popupMenu);
    sendCol.setCellRenderer(renderer);
  }

  public String getToolTipText(MouseEvent e) {
    java.awt.Point p = e.getPoint();
    int row = rowAtPoint(p);
    int col = columnAtPoint(p);

    if (row < 0 || col < 0)
      return null;

    switch (col) {
    case SenseRow.IDX_NODE:
      return (String) getValueAt(row, col);
    case SenseRow.IDX_SENSOR:
      return (String) getValueAt(row, col);
    case SenseRow.IDX_FEEDID:
      return (String) getValueAt(row, col);
    case SenseRow.IDX_CONV:
      return (String) getValueAt(row, col);
    }
    return null;
  }

  public void HandleSelectionEvent() {
    selectedRows = getSelectedRows();
    for (int i = 0, n=selectedRows.length; i < n ; i++) {
      selectedRows[i] = convertRowIndexToModel(selectedRows[i]);
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

  public void selectRows(Node[] node) {

    if (node==null||getRowCount()==0)
      return;
    SenseTableModel model = (SenseTableModel) getModel();
    ArrayList<Integer> rows;
    ListIterator<Integer> rowsIt;
    removeRowSelectionInterval(0, model.getRowCount()-1);

    for (int i = 0, n=node.length; i < n ; i++) {
      if ((rows = model.getRowsOf(node[i].getID())) != null) {
        rowsIt = rows.listIterator();
        while (rowsIt.hasNext()) {
          int row = convertRowIndexToView((int) rowsIt.next());
          addRowSelectionInterval(row, row);
        }
      }
    }
  }
}
