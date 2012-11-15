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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Hashtable;

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
import javax.swing.table.TableColumn;

public class CosmTableGUI extends JTable {
  private static final long serialVersionUID = 1L;
  private int[] selectedRows;
  protected static final int DATASTREAMS_COL = 1;
  protected static final int VALUES_TYPE_COL = 4;

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
    TableColumn valuesCol = getColumnModel().getColumn(DATASTREAMS_COL);
    valuesCol.setCellEditor(new DataStreamCellEditor());
  }

  public void setUpValuesColumn() {
    JComboBox<String> comboBox = new JComboBox<String>();
    comboBox.addItem("Raw");
    comboBox.addItem("Converted");
    TableColumn valuesCol = getColumnModel().getColumn(VALUES_TYPE_COL);
    valuesCol.setCellEditor(new DefaultCellEditor(comboBox));
    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    valuesCol.setCellRenderer(renderer);
  }

  public void HandleSelectionEvent() {
    selectedRows = getSelectedRows();
    for (int i = 0; i < selectedRows.length; i++) {
      selectedRows[i] = convertRowIndexToModel(selectedRows[i]);
    }
  }
  
  public String getToolTipText(MouseEvent e) {
    //TODO 
    /*
     * NO API TO SET TOOLTIP DURATION. TRY THIS:
     * http://www.eclipsezone.com/eclipse/forums/t65353.html
     */
    String tip = null;
    java.awt.Point p = e.getPoint();
    int rowIndex = rowAtPoint(p);
    int colIndex = columnAtPoint(p);
    
    if (rowIndex<0 || colIndex<0) return null;
    
    int col = convertColumnIndexToModel(colIndex);
    int row = convertRowIndexToModel(rowIndex);

    if (col == DATASTREAMS_COL) {
      @SuppressWarnings("unchecked")
      Hashtable<String, String> dataStreams=(Hashtable<String, String>)getValueAt(row, col);
      StringBuilder tipText = new StringBuilder();
      tipText.append("<html>"); 
      tipText.append("Current Datastreams IDs:<br>"); 
      for (Object key:dataStreams.keySet()){
        String sensor=(String)key;
        tipText.append(sensor+" sensor: "+dataStreams.get(key)+"<br>");
      }
      tipText.append("</html>");
      tip=tipText.toString();
    } else {
        //Just in case
        tip = super.getToolTipText(e);
    }
    return tip;
}

  private class RowListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent event) {
      if (event.getValueIsAdjusting()) {
        return;
      }
      HandleSelectionEvent();
    }
  }

  private class DataStreamCellEditor extends AbstractCellEditor
  implements TableCellEditor, ActionListener {
    private static final long serialVersionUID = 1L;
    
    Hashtable<String,String> dataStreams;
    JButton button;
    ArrayList<JTextField> textFields;
    
    protected static final String EDIT = "edit";
    protected static final String CANCEL = "cancel";
    protected static final String OK = "ok";

    public DataStreamCellEditor(){
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
      if (e.getActionCommand().equals(EDIT)){
        createDatastreamsDialog().setVisible(true);
        fireEditingStopped();
      }
    }
    
    private JDialog createDatastreamsDialog(){
      JDialog dialog = new JDialog();
      dialog.setModalityType(ModalityType.APPLICATION_MODAL);
      dialog.setTitle("Datastreams configuration");
      JPanel pane = new JPanel(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      textFields = new ArrayList<JTextField>();
      c.weightx=0.1;
      c.weighty=0.1;
      c.gridy=0;
      
      for (Object key: dataStreams.keySet()){
        String sensor=key.toString();
        c.gridx=0;
        c.weightx=0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_END;
        pane.add(new JLabel(sensor),c);
        c.gridx=1;
        c.weightx=0.1;
        c.fill = GridBagConstraints.HORIZONTAL;
        JTextField textField = new JTextField();
        textField.setText(dataStreams.get(sensor));
        pane.add(textField,c);
        textFields.add(textField);
        c.gridy++;
      }
      JButton cancel=new JButton("Cancel");
      cancel.addActionListener(this);
      cancel.setActionCommand(CANCEL);
      JButton ok = new JButton ("OK");
      ok.addActionListener(this);
      ok.setActionCommand(OK);
      JPanel buttonPanel=new JPanel();
      buttonPanel.add(ok);
      buttonPanel.add(cancel);
      c.gridx=0;
      c.gridwidth=2;
      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.CENTER;
      pane.add(buttonPanel,c); 
      dialog.setContentPane(pane);
      dialog.pack();
      dialog.setLocationRelativeTo(button);
      return dialog;
    }
    
    @SuppressWarnings("unchecked")
    public Component getTableCellEditorComponent(JTable table, Object value,
        boolean isSelected, int row, int column) {
      dataStreams=(Hashtable<String, String>) value;
      return button;
    }
  }
 
}
