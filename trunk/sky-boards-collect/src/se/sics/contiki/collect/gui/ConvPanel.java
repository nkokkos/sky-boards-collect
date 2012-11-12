package se.sics.contiki.collect.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import se.sics.contiki.collect.CollectServer;
import se.sics.contiki.collect.Node;
import se.sics.contiki.collect.Sensor;
import se.sics.contiki.collect.SensorData;
import se.sics.contiki.collect.Variable;
import se.sics.contiki.collect.Visualizer;


public class ConvPanel extends JPanel implements Visualizer, PropertyChangeListener {
  private static final long serialVersionUID = 1L;
  boolean active;
  String title;
  String category;
  
  Node selectedNode;
  Sensor[] sensors;
  SensorData data;
  ArrayList<XYSeriesCollection> seriesList;
  ArrayList<Function> functions;
  private Hashtable<String, ArrayList<JFormattedTextField>> fieldsTable;
  Properties calibrationConfig;

  JTabbedPane tabbedPane; // tabbedPanel
  JPanel mainPanel; // main Panel for each tab
  JFreeChart chart;
  ChartPanel chartPanel;
  XYSeriesCollection dataset;
  
  JButton buttonReset;
  JButton buttonFormula;
  JComboBox<String> comboBoxXOpt;
  GridBagConstraints c;
  public static final String delim = "\n";
  public final int DEF_MIN_X = 1;
  public final int DEF_MAX_X = 4096;
  public final int DEF_INC_X = 5;
  
  public ConvPanel(CollectServer server, String category, String title,Properties config){
    super(new BorderLayout());
    this.title=title;
    this.category=category;
    calibrationConfig=config;
    active=true;
    tabbedPane = new JTabbedPane();
    
    
    //fieldsTable.
  }

  public String getCategory() {
    return category;
  }

  public String getTitle() {
    return title;
  }

  public Component getPanel() {
    return this;
  }

  public void nodesSelected(Node[] node) {
    if (node.length>1) return;
    if (!active) return;
    
    selectedNode=node[0];
    sensors=selectedNode.getSensors();
    tabbedPane.removeAll();
    seriesList = new ArrayList<XYSeriesCollection>();
    functions = new ArrayList<Function>();
    fieldsTable  = new Hashtable<String, ArrayList<JFormattedTextField>> ();
    
    for (int i = 0; i < sensors.length; i++) {
      String sensorId = sensors[i].getId();
      Variable[] vars = sensors[i].getVars();
      mainPanel=new JPanel(new GridBagLayout());
      
      c=new GridBagConstraints();
      // add labels
      c.anchor=GridBagConstraints.LINE_START;
      c.gridx=c.gridy=0;
      c.gridwidth=2;
      c.weighty=0.5;
      mainPanel.add(new JLabel("Last ADC value: "+getLastADCValue(sensorId)),c);
      c.gridy++;
      c.gridwidth=1;
      mainPanel.add(new JLabel("Min. X:"),c);
      c.gridy++;
      mainPanel.add(new JLabel("Max. X:"),c);
      c.gridy++;
      mainPanel.add(new JLabel("Inc. X:"),c);
      
      for (int j = 0; j<vars.length;j++){
        c.gridy++;
        mainPanel.add(new JLabel(vars[j].getName() + ":"),c);
      }
      
      // Add fields
      ArrayList<JFormattedTextField> fieldList = 
          new ArrayList<JFormattedTextField>();
      c.fill=GridBagConstraints.HORIZONTAL;
      c.weightx=0.1;
      c.gridx=1;
      c.gridy=1;
      createLimitTextFields(i, "minx", DEF_MIN_X);
      c.gridy++;
      createLimitTextFields(i, "maxx", DEF_MAX_X);
      c.gridy++;
      createLimitTextFields(i, "inc", DEF_INC_X);
      int height=4;
      
      for (int j = 0; j<vars.length;j++){
        c.gridy++;
        height++;
        fieldList.add(createVarsTextFields(i, j));
      }
      fieldsTable.put(sensorId, fieldList);
      
      // Add buttons/combobox
      createComboBoxXOpt(sensors[i].getId());
      c.gridx=0;
      c.gridwidth=2;
      c.gridy++;
      height++;
      mainPanel.add(comboBoxXOpt,c);

      buttonReset = new JButton("Reset values");
      buttonReset.setName(sensorId+delim+i);
      buttonReset.addActionListener(new ButtonResetAction());
      c.gridy++;
      height++;
      //c.fill=GridBagConstraints.NONE;
      c.anchor=GridBagConstraints.CENTER;
      mainPanel.add(buttonReset,c);

      c.gridy++;
      height++;
      buttonFormula = new JButton("Show formulas");
      buttonFormula.setName(sensorId);
      buttonFormula.addActionListener(new ButtonFormulaAction());
      mainPanel.add(buttonFormula,c);
      
      // Add chart
      c.fill=GridBagConstraints.BOTH;
      c.gridx=2;
      c.gridy=0;
      c.weightx=1;
      c.weighty=0.5;
      c.gridheight=height;
      chartPanel = createChart(sensors[i].getId());
      mainPanel.add(chartPanel,c);
      
      tabbedPane.add(mainPanel, sensorId);
    }
    add(tabbedPane,BorderLayout.CENTER);
  }
  
  void createLimitTextFields(int idx, String s, int value) { 
    NumberFormat format = NumberFormat.getIntegerInstance();
    JFormattedTextField valueField = new JFormattedTextField(format);
    valueField.setValue(value);
    valueField.setColumns(10);
    valueField.setName(sensors[idx].getId() + delim + s);
    valueField.addPropertyChangeListener("value", this);
    mainPanel.add(valueField,c);
  }
  
  JFormattedTextField createVarsTextFields(int s, int v) {
    Variable[] vars = sensors[s].getVars();
    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMaximumFractionDigits(15);
    JFormattedTextField valueField = new JFormattedTextField(format);
    valueField.setValue(new Double(vars[v].getValue()));
    valueField.setColumns(10);
    valueField.setName(sensors[s].getId() + delim + vars[v].getName());
    valueField.addPropertyChangeListener("value", this);
    mainPanel.add(valueField,c);
    return valueField;
  }
  
  void createComboBoxXOpt(String sensorId) {
    String[] opt = {"adc value", "Sensor voltage(Vs)"};
    comboBoxXOpt = new JComboBox<String>();
    comboBoxXOpt.setModel(new DefaultComboBoxModel<String>(opt));
    comboBoxXOpt.setName(sensorId);
    comboBoxXOpt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        @SuppressWarnings("unchecked")
        JComboBox<String> cb = ((JComboBox<String>) e.getSource());
        String sensor = cb.getName();
        int idx = cb.getSelectedIndex();
        Function f;

        for (int i = 0; i < functions.size(); i++) {
          if (functions.get(i).getSensorId() == sensor) {
            f = functions.get(i);
            if (idx == 0) {
              f.setShowVs(false);
            } else if (idx == 1) {
              f.setShowVs(true);
            }
            updateChart(i);
          }
        }
      }
    });
  }

  @Override
  public void propertyChange(PropertyChangeEvent e) {
    int update = -1;
    Object source = e.getSource();
    StringTokenizer tokens = new StringTokenizer(
        ((JFormattedTextField) source).getName(), delim);
    String sensorStr = tokens.nextToken();
    String varStr = tokens.nextToken();
    Variable var;
    double newValue = ((Number) ((JFormattedTextField) source).getValue())
        .doubleValue();

    Sensor sensor = selectedNode.getNodeSensor(sensorStr);
    if ((var = sensor.getVar(varStr)) != null)
      if (var.getValue() == newValue)
        return;

    for (int i = 0; i < sensors.length; i++)
      if (sensors[i].getId().equals(sensorStr)) {
        if (varStr.equals("minx"))
          functions.get(i).setMinX((int) newValue);
        else if (varStr.equals("maxx"))
          functions.get(i).setMaxX((int) newValue);
        else if (varStr.equals("inc"))
          functions.get(i).setIncrement((int) newValue);
        else {
          sensors[i].setVar(varStr, newValue);
          updateConfig(sensors[i].getId(), varStr, newValue);
        }
        update = i;
      }

    if (update >= 0)
      updateChart(update);
  }

  private void updateChart(int chartIndex) {
    dataset = seriesList.get(chartIndex);
    dataset.removeAllSeries();
    Function conv=functions.get(chartIndex);
    XYSeries series = new XYSeries("Conversion function");
    for (int i = conv.getMinX(); i <= conv.getMaxX(); i += conv.getIncrement()) {
      series.add(conv.getX(i), conv.f(i));
    }
    dataset.addSeries(series);   
  }
  
  private ChartPanel createChart(String sensorId) {
    if (selectedNode.getSensorDataCount() == 0)
      return null;
    data = selectedNode.getSensorData(selectedNode.getSensorDataCount() - 1);
    Function conv = new Function(selectedNode.getNodeSensor(sensorId)) {
      protected double f(int value) {
        return sensor.getConv(value);
      }
    };
    functions.add(conv);
    return paintChart(conv);
  }
  
  private ChartPanel paintChart(Function conv) {
    XYSeries series = new XYSeries("Conversion function");
    for (int i = conv.getMinX(); i <= conv.getMaxX(); i += conv.getIncrement()) {
      series.add(conv.getX(i), conv.f(i));
    }

    dataset = new XYSeriesCollection();
    seriesList.add(dataset);
    dataset.addSeries(series);
    chart = ChartFactory.createXYLineChart("Conversion function", // Title
        conv.getxTag(), // x-axis Label
        conv.getyTag(), // y-axis Label
        dataset, // Dataset
        PlotOrientation.VERTICAL, // Plot Orientation
        true, // Show Legend
        true, // Use tooltips
        false // Configure chart to generate URLs?
        );
    return (chartPanel = new ChartPanel(chart));
  }
  
  String getLastADCValue(String sensor) {
    if (selectedNode.getLastSD()==null) return "";
    int lastValue = selectedNode.getLastValueOf(sensor);
    return Integer.toString(lastValue);
  }
  
  private JLabel getConversionImage(String sensorName) {
    if (selectedNode.getSensorDataCount() == 0)
      return null;
    String firmName = selectedNode.getFirmName();
    ImageIcon imgIcon = new ImageIcon("./images/" + firmName + "-" + sensorName
        + "-" + "formula.jpeg");
    JLabel imgLabel = new JLabel();
    imgLabel.setIcon(imgIcon);
    return imgLabel;
  }
  
  private class ButtonResetAction implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      String buttonName = ((Component) e.getSource()).getName();
      StringTokenizer tokens = new StringTokenizer(buttonName, delim);
      String sensor = tokens.nextToken();
      String index = tokens.nextToken();
      
      selectedNode.setDefaultValues(sensor);

      ArrayList<JFormattedTextField> TextFields = fieldsTable.get(sensor);
      ListIterator<JFormattedTextField> it=TextFields.listIterator();
      while (it.hasNext()){
        reset(it.next());
      }

      updateChart(Integer.parseInt(index));
    }

    public void reset(JFormattedTextField field) {
      String fieldName = field.getName();
      if (fieldName == "" || fieldName == null)
        return;
      StringTokenizer tokens = new StringTokenizer(field.getName(), delim);
      String sensorStr = tokens.nextToken();
      String varStr = tokens.nextToken();
      Variable var;

      Sensor sensor = selectedNode.getNodeSensor(sensorStr);
      if ((var = sensor.getVar(varStr)) != null) {
        ((JFormattedTextField) field).setValue(var.getValue());
        updateConfig(sensorStr, varStr, var.getValue());
      }
    }
  }

  public class ButtonFormulaAction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      String sensor = ((Component) e.getSource()).getName();
      JFrame imgFrame = new JFrame();
      imgFrame.add(getConversionImage(sensor));
      imgFrame.setTitle("Datasheet formulas");
      imgFrame.pack();
      imgFrame.setVisible(true);
    }
  }

  public void updateConfig(String sensor, String var, double newVal) {
    calibrationConfig.put(
        "var," + selectedNode.getID() + "," + sensor + "," + "" + var,
        String.valueOf(newVal));
  }

  private abstract class Function {
    protected Sensor sensor;
    private String xLabel = "adc_value";
    private String yLabel;
    private int minX = 1;
    private int maxX = 4096;
    private int increment = 5;
    private boolean hasVs = false;
    private String sensorId;
    private boolean showVs = false;

    Function(Sensor sensor) {
      this.sensor = sensor;
      this.yLabel = sensor.getUnits();
      this.sensorId = sensor.getId();
      this.hasVs = sensor.ADC12();
    }

    public Number getX(int i) {
      Sensor s;
      if (hasVs && showVs) {
        s = data.getNode().getNodeSensor(sensorId);
        double vRef = s.getVar("Vref").getValue();
        return ((double) i / (double) 4096) * vRef;
      } else
        return i;
    }

    abstract double f(int value);

    public void setMinX(int minX) {
      this.minX = minX;
    }

    public int getMinX() {
      return minX;
    }

    public void setMaxX(int maxX) {
      this.maxX = maxX;
    }

    public int getMaxX() {
      return maxX;
    }

    public void setIncrement(int increment) {
      this.increment = increment;
    }

    public int getIncrement() {
      return increment;
    }

    public String getxTag() {
      return xLabel;
    }

    public void setXTag(String lab) {
      xLabel = lab;
    }

    public String getyTag() {
      return yLabel;
    }

    public void setShowVs(boolean showVs) {
      if (!hasVs)
        return;
      if (showVs)
        setXTag("Voltage sensor (Vs)");
      else
        setXTag("adc_value");
      this.showVs = showVs;
    }

    public String getSensorId() {
      return sensorId;
    }
  }
  public void nodeAdded(Node node) {}
  public void nodeDataReceived(SensorData sensorData) {}
  public void clearNodeData() {}

}
