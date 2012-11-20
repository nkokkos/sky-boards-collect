/*
 * NodeCalibrationDialog
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 23 jul 2012
 */
package se.sics.contiki.collect.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.UIDefaults;

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


public class ConvPanel extends JPanel implements Visualizer,
    PropertyChangeListener {
  private static final long serialVersionUID = 1L;
  private CollectServer server;
  private boolean active;
  private String title;
  private String category;

  private Node selectedNode;
  private Sensor[] sensors;
  private SensorData data;
  private ArrayList<XYSeriesCollection> seriesList;
  private ArrayList<Function> functions;
  private Hashtable<String, ArrayList<JFormattedTextField>> fieldsTable;
  private Properties calibrationConfig;

  private JTabbedPane tabbedPane; // tabbedPanel
  private JPanel mainPanel; // main Panel for each tab
  private JFreeChart chart;
  private ChartPanel chartPanel;
  private XYSeriesCollection dataset;

  private JButton buttonReset;
  private JButton buttonFormula;
  private JComboBox<String> comboBoxXOpt;
  private GridBagConstraints c;
  private static final String delim = "\n";
  public final int DEF_MIN_X = 1;
  public final int DEF_MAX_X = 4096;
  public final int DEF_INC_X = 5;
  public final String TOOL_TIP_RESET_BT = "Reset to default values.";
  public final String TOOL_TIP_SAVE_BT = "Confirm changes and update charts.";
  public final String TOOL_TIP_FORM_BT = "Display sensor conversions expressions.";
  
  private final Color RAW_HIGHLIGH_COLOR=new Color(0,51,0);
  private Color JAVA_SEPARATOR=new Color(0,100,0);
  private Color JAVA_DEF=new Color(238,238,238);

  public ConvPanel(CollectServer server, String category, String title,
      Properties config) {
    super(new BorderLayout());
    this.title = title;
    this.category = category;
    this.server = server;
    calibrationConfig = config;
    active = true;
    tabbedPane = new JTabbedPane();
    UIDefaults defaults = javax.swing.UIManager.getDefaults();
    JAVA_DEF=defaults.getColor("Panel.background");
    JAVA_SEPARATOR=defaults.getColor("Separator.foreground");
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
    if (node == null || node.length > 1)
      return;
    if (!active)
      return;

    selectedNode = node[0];
    sensors = selectedNode.getSensors();
    tabbedPane.removeAll();
    seriesList = new ArrayList<XYSeriesCollection>();
    functions = new ArrayList<Function>();
    fieldsTable = new Hashtable<String, ArrayList<JFormattedTextField>>();

    for (int i = 0; i < sensors.length; i++) {
      String sensorId = sensors[i].getId();
      Variable[] vars = sensors[i].getVars();
      mainPanel = new JPanel(new GridBagLayout());

      c = new GridBagConstraints();
      // add labels
      c.anchor = GridBagConstraints.LINE_START;
      c.gridx = c.gridy = 0;
      c.gridwidth = 2;
      c.weighty = 0.5;
      c.insets.left=5;
      JLabel label=new JLabel("Last Raw/ADC value: " + getLastADCValue(sensorId));
      label.setForeground(RAW_HIGHLIGH_COLOR);
      mainPanel.add(label, c);
      c.gridy++;
      c.gridwidth = 1;
      mainPanel.add(new JLabel("Min. X "), c);
      c.gridy++;
      mainPanel.add(new JLabel("Max. X "), c);
      c.gridy++;
      mainPanel.add(new JLabel("Inc. X "), c);
      c.gridy++;
      c.gridwidth=2;
      c.fill = GridBagConstraints.HORIZONTAL;
      mainPanel.add(new JSeparator(), c);
      c.gridwidth=1;
      for (int j = 0; j < vars.length; j++) {
        c.gridy++;
        mainPanel.add(new JLabel(vars[j].getName() + " "), c);
      }
      c.insets.left=0;
      // Add fields
      ArrayList<JFormattedTextField> fieldList = new ArrayList<JFormattedTextField>();
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 0;
      c.gridx = 1;
      c.gridy = 1;
      createLimitTextFields(i, "minx", DEF_MIN_X);
      c.gridy++;
      createLimitTextFields(i, "maxx", DEF_MAX_X);
      c.gridy++;
      createLimitTextFields(i, "inc", DEF_INC_X);
      c.gridy++;//for the JSeparator
      int height = 5;

      for (int j = 0; j < vars.length; j++) {
        c.gridy++;
        height++;
        fieldList.add(createVarsTextFields(i, j));
      }
      fieldsTable.put(sensorId, fieldList);
      c.insets.left=5;
      // Add buttons/combobox
      createComboBoxXOpt(sensors[i].getId(), c);
      createButton(buttonReset, "Reset values", TOOL_TIP_RESET_BT, sensorId
          + delim + i, new ButtonResetAction(), c);
      createButton(buttonFormula, "Show formulas", TOOL_TIP_FORM_BT, sensorId,
          new ButtonFormulaAction(), c);
      height += 3;

      
      // Add chart
      c.fill = GridBagConstraints.BOTH;
      c.gridx = 3;
      c.gridy = 0;
      c.weightx = 1;
      c.weighty = 0.5;
      c.gridheight = height;
      chartPanel = createChart(sensors[i].getId());
      mainPanel.add(chartPanel, c);
      
      tabbedPane.add(mainPanel, sensorId);
    }
    add(tabbedPane, BorderLayout.CENTER);
    updateUI();
  }

  void createLimitTextFields(int idx, String s, int value) {
    NumberFormat format = NumberFormat.getIntegerInstance();
    JFormattedTextField valueField = new JFormattedTextField(format);
    valueField.setValue(value);
    valueField.setColumns(10);
    valueField.setName(sensors[idx].getId() + delim + s);
    valueField.addPropertyChangeListener("value", this);
    mainPanel.add(valueField, c);
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
    mainPanel.add(valueField, c);
    return valueField;
  }

  void createButton(JButton b, String text, String tip, String name,
      ActionListener Listener, GridBagConstraints c) {
    b = new JButton(text);
    b.setToolTipText(tip);
    b.setName(name);
    b.addActionListener(Listener);
    c.gridy++;
    c.fill = GridBagConstraints.HORIZONTAL;
    mainPanel.add(b, c);
  }

  void createComboBoxXOpt(String sensorId, GridBagConstraints c) {
    String[] opt = { "adc value", "Sensor voltage(Vs)" };
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
    c.gridx = 0;
    c.gridwidth = 2;
    c.gridy++;
    mainPanel.add(comboBoxXOpt, c);
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
    Function conv = functions.get(chartIndex);
    XYSeries series = new XYSeries("Conversion function ("+conv.getSensorId()+")");
    genSerie(conv, series);
    dataset.addSeries(series);
    server.UpdateChart(conv.getSensorId());
  }

  private ChartPanel createChart(String sensorId) {
    if (selectedNode.getSensorDataCount() == 0)
      return new ChartPanel(null);
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
    XYSeries series = new XYSeries("Conversion function ("+conv.getSensorId()+")");  
    genSerie(conv, series);
    dataset = new XYSeriesCollection();
    seriesList.add(dataset);
    dataset.addSeries(series);
    String title="Conversion function (Node "
        + selectedNode.getID()+")";
    chart = ChartFactory.createXYLineChart(title, // Title
        conv.getxTag(), // x-axis Label
        conv.getyTag(), // y-axis Label
        dataset, // Dataset
        PlotOrientation.VERTICAL, // Plot Orientation
        true, // Show Legend
        true, // Use tooltips
        false // Configure chart to generate URLs?
        );
    
    chart.getXYPlot().getDomainAxis().setTickLabelPaint(RAW_HIGHLIGH_COLOR);
    chart.getXYPlot().getDomainAxis().setLabelPaint(RAW_HIGHLIGH_COLOR);
    chart.getXYPlot().setBackgroundPaint(Color.WHITE);
    chart.getXYPlot().setDomainGridlinePaint(Color.GRAY);
    chart.getXYPlot().setRangeGridlinePaint(Color.GRAY);
    chart.setBackgroundPaint(JAVA_DEF);
    chart.setBorderPaint(JAVA_SEPARATOR);
    chart.getXYPlot().setOutlineStroke(new BasicStroke(1.0f));
    chart.getXYPlot().setOutlinePaint(JAVA_SEPARATOR);

    return (chartPanel = new ChartPanel(chart));
  }

  private void genSerie(Function conv, XYSeries series) {
    for (int i = conv.getMinX(); i <= conv.getMaxX(); i += conv.getIncrement()) {
      series.add(conv.getX(i), conv.f(i));
    }
  }

  String getLastADCValue(String sensor) {
    if (selectedNode.getLastSD() == null)
      return "";
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
      ListIterator<JFormattedTextField> it = TextFields.listIterator();
      while (it.hasNext()) {
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
        // default values do not need to be stored
        removeFromConfig(sensorStr, varStr, var.getValue());
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
      imgFrame.setLocationRelativeTo((Component) e.getSource());
      imgFrame.pack();
      imgFrame.setVisible(true);
    }
  }

  public void updateConfig(String sensor, String var, double newVal) {
    String key = "var," + selectedNode.getID() + "," + sensor + "," + "" + var;
    String value = calibrationConfig.getProperty(key);
    String newValue = String.valueOf(newVal);
    if (newValue.equals(value))
      return;
    calibrationConfig.put(key, newValue);
  }

  /**
   * This method is called when resetting default constants values, as default
   * values do not need to be stored.
   */
  public void removeFromConfig(String sensor, String var, double newVal) {
    String key = "var," + selectedNode.getID() + "," + sensor + "," + "" + var;
    calibrationConfig.remove(key);
  }

  private abstract class Function {
    protected Sensor sensor;
    private String xLabel = "Raw value";
    private String yLabel;
    private int minX = 1;
    private int maxX = 4096;
    private int increment = 5;// increase for better GUI performance
    private boolean hasVs = false;
    private String sensorId;
    private boolean showVs = false;

    Function(Sensor sensor) {
      this.sensor = sensor;
      yLabel = sensor.getUnits();
      sensorId = sensor.getId();
      hasVs = sensor.ADC12();
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

  public void nodeAdded(Node node) {/* ignore */
  }

  public void nodeDataReceived(SensorData sensorData) {/* ignore */
  }

  public void clearNodeData() {/* ignore */
  }
}
