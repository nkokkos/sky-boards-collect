/*
 * CosmTableGUI
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 14 Oct 2012
 */

package se.sics.contiki.collect.gui;

import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.ListIterator;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import se.sics.contiki.collect.Node;

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
    TableColumn col = getColumnModel().getColumn(CosmRow.IDX_DATASTREAMS);
    col.setCellEditor(new DataStreamCellEditor());
    col.setCellRenderer(new DataStreamsCellRenderer());
  }

  public void setUpValuesColumn() {
    JComboBox<String> comboBox = new JComboBox<String>();
    comboBox.addItem("Raw");
    comboBox.addItem("Converted");
    TableColumn valuesCol = getColumnModel().getColumn(CosmRow.IDX_CONV);
    valuesCol.setCellEditor(new DefaultCellEditor(comboBox));
    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    valuesCol.setCellRenderer(renderer);
  }

  @SuppressWarnings("unchecked")
  public String getToolTipText(MouseEvent e) {
    java.awt.Point p = e.getPoint();
    int row = rowAtPoint(p);
    int col = columnAtPoint(p);

    if (row < 0 || col < 0)
      return null;

    switch (col) {
    case CosmRow.IDX_NODE:
      return (String) getValueAt(row, col);
    case CosmRow.IDX_DATASTREAMS:
      return consDatastreamsToolTip((Hashtable<String, String>) 
          getValueAt(row,col));
    case CosmRow.IDX_FEEDID:
      return (String) getValueAt(row, col);
    case CosmRow.IDX_FEEDTITLE:
      return (String) getValueAt(row, col);
    case CosmRow.IDX_CONV:
      return (String) getValueAt(row, col);
    }
    return null;
  }

  private String consDatastreamsToolTip(Hashtable<String, String> dataStreams) {
    StringBuilder tipText = new StringBuilder();
    tipText
        .append("<html><table>"
            + "<tr><th><u>Sensor</u></th><th><u>Datastream ID</u></th></tr>");
    for (Object key : ((Hashtable<String, String>) dataStreams).keySet()) {
      String sensor = (String) key;
      String dstrm = ((Hashtable<String, String>) dataStreams).get(key);
      tipText.append("<tr><td>" + sensor + "</td><td>" + dstrm + "</td></tr>");
    }
    tipText.append("</table></html>");
    return tipText.toString();
  }

  private class RowListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent event) {
      if (event.getValueIsAdjusting()) {
        return;
      }
      HandleSelectionEvent();
    }
  }

  public void HandleSelectionEvent() {
    selectedRows = getSelectedRows();
    for (int i = 0; i < selectedRows.length; i++) {
      selectedRows[i] = convertRowIndexToModel(selectedRows[i]);
    }
  }
  
  public void selectRows(Node[] node) {
    if (node==null||getRowCount()==0)
      return;
    CosmTableModel model = (CosmTableModel) getModel();
    ArrayList<Integer> rows;
    ListIterator<Integer> rowsIt;
    
    removeRowSelectionInterval(0, model.getRowCount()-1);

    for (int i = 0; i < node.length; i++) {
      if ((rows = model.getRowsOfIndex(node[i].getID())) != null) {
        rowsIt = rows.listIterator();
        while (rowsIt.hasNext()) {
          int row = convertRowIndexToView((int) rowsIt.next());
          addRowSelectionInterval(row, row);
        }
      }
    }
  }

  private class DataStreamsCellRenderer extends JButton implements
      TableCellRenderer {
    private static final long serialVersionUID = 1L;

    public DataStreamsCellRenderer() {
      super("Edit");
    }

    public Component getTableCellRendererComponent(JTable table,
        Object dataStreams, boolean isSelected, boolean hasFocus, int row,
        int col) {
      return this;
    }
  }

  private class DataStreamCellEditor extends AbstractCellEditor implements
      TableCellEditor, ActionListener {
    private static final long serialVersionUID = 1L;

    Hashtable<String, String> dataStreams;
    JButton button;
    ArrayList<JTextField> textFields;
    JDialog dialog;

    protected static final String EDIT = "edit";
    protected static final String CANCEL = "cancel";
    protected static final String OK = "ok";

    public DataStreamCellEditor() {
      super();
      button = new JButton();
      button.setActionCommand(EDIT);
      button.addActionListener(this);
      button.setBorderPainted(false);
    }

    @Override
    public Object getCellEditorValue() {
      return dataStreams;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (e.getActionCommand().equals(EDIT)) {
        dialog = createDatastreamsDialog();
        dialog.setVisible(true);
        fireEditingStopped();
        return;
      } else if (e.getActionCommand().equals(OK)) {
        int i = 0;
        for (Object key : dataStreams.keySet()) {
          dataStreams.put((String) key, textFields.get(i).getText());
          i++;
        }
      }
      dialog.dispose();
    }

    private JDialog createDatastreamsDialog() {
      JDialog dialog = new JDialog();
      dialog.setModalityType(ModalityType.APPLICATION_MODAL);
      dialog.setTitle("Datastream IDs configuration");
      JPanel pane = new JPanel(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      textFields = new ArrayList<JTextField>();
      c.weightx = 0.1;
      c.weighty = 0.1;
      c.gridy = 0;
      c.insets = new Insets(5, 5, 0, 5);
      for (Object key : dataStreams.keySet()) {
        String sensor = key.toString();
        c.gridx = 0;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        pane.add(new JLabel(sensor), c);
        c.gridx = 1;
        c.weightx = 0.1;
        c.fill = GridBagConstraints.HORIZONTAL;
        JTextField textField = new JTextField();
        textField.setText(dataStreams.get(sensor));
        pane.add(textField, c);
        textFields.add(textField);
        c.gridy++;
      }

      JButton cancel = new JButton("Cancel");
      cancel.addActionListener(this);
      cancel.setActionCommand(CANCEL);
      JButton ok = new JButton("OK");
      ok.addActionListener(this);
      ok.setActionCommand(OK);
      JPanel buttonPanel = new JPanel();
      buttonPanel.add(ok);
      buttonPanel.add(cancel);
      c.gridx = 0;
      c.gridwidth = 2;
      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.CENTER;
      c.insets = new Insets(0, 0, 0, 0);
      pane.add(buttonPanel, c);
      dialog.setContentPane(pane);
      dialog.pack();
      dialog.setLocationRelativeTo(button);
      dialog.setModalityType(ModalityType.APPLICATION_MODAL);
      return dialog;
    }

    @SuppressWarnings("unchecked")
    public Component getTableCellEditorComponent(JTable table, Object value,
        boolean isSelected, int row, int column) {
      dataStreams = (Hashtable<String, String>) value;
      return button;
    }
  }
}
