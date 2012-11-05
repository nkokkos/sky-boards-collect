/*
 * DataFeederSense
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 03 Oct 2012
 */

package se.sics.contiki.collect.gui;

import java.awt.Dimension;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

public class SenseTableGUI extends JTable {
  private static final long serialVersionUID = 1L;
  

  public SenseTableGUI(SenseTableModel tableModel) {
    super(tableModel);
    setUpValuesColumn();
    setPreferredScrollableViewportSize(new Dimension(400, 250));
    setFillsViewportHeight(true);
    setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    setAutoCreateRowSorter(true);
    }

  public void setUpValuesColumn() {
    JComboBox<String> comboBox = new JComboBox<String>();
    comboBox.addItem("Raw");
    comboBox.addItem("Converted");
    TableColumn valuesCol=this.getColumnModel().getColumn(3);
    setCellEditor(new DefaultCellEditor(comboBox));

    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    renderer.setToolTipText("Click for combo box");
    valuesCol.setCellRenderer(renderer);
  }
}


