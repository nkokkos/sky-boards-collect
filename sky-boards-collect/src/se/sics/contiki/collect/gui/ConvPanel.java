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
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.UIDefaults;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

import se.sics.contiki.collect.CollectServer;
import se.sics.contiki.collect.Node;
import se.sics.contiki.collect.Sensor;
import se.sics.contiki.collect.SensorData;
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
  private ArrayList<JFreeChart> chartList;
  private ArrayList<XYSeriesCollection> seriesList;
  private ArrayList<Function> functions;
  private ArrayList<JLabel> xLabelList;
  private ArrayList<JLabel> yLabelList;
  private Hashtable<String, ArrayList<JFormattedTextField>> fieldsTable;
  private Properties calibrationConfig;

  private JTabbedPane tabbedPane; // tabbedPanel
  private XYSeriesCollection dataset;

  public final int DEF_MIN_X = 1;
  public int DEF_MAX_X;
  public final int DEF_INC_X = 5;
  public final String TOOL_TIP_RESET_BT = "Reset to default values";
  public final String TOOL_TIP_SAVE_BT = "Confirm changes and update charts";
  public final String TOOL_TIP_FORM_BT = "Display sensor conversions expressions";
  public final String TOOL_TIP_LAST_LABEL = "Last raw or ADC value received from the node";

  private final Color DOM_HIGHLIGHT_COLOR = Color.BLUE;
  private final Color RANGE_HIGHLIGHT_COLOR = new Color(0, 153, 0);
  private final Color LAST_HIGHLIGHT_COLOR = new Color(102, 0, 0);
  private Color COLOR_JAVA_SEPARATOR = new Color(0, 100, 0);
  private Color COLOR_JAVA_DEF = new Color(238, 238, 238);

  public ConvPanel(CollectServer server, String category, String title,
      Properties config) {
    super(new BorderLayout());
    this.title = title;
    this.category = category;
    this.server = server;
    calibrationConfig = config;
    active = false;
    tabbedPane = new JTabbedPane();
    UIDefaults defaults = javax.swing.UIManager.getDefaults();
    COLOR_JAVA_DEF = defaults.getColor("Panel.background");
    COLOR_JAVA_SEPARATOR = defaults.getColor("Separator.foreground");
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

  public void setActive(boolean isActive) {
    this.active = isActive;
  }

  public void nodesSelected(Node[] node) {
    if (node == null || node.length > 1)
      return;

    // Do not waste resources painting
    // components unless this is the selected tab
    if (!active)
      return;

    selectedNode = node[0];
    DEF_MAX_X = selectedNode.PLATFORM_ADC_RESOLUTION;
    sensors = selectedNode.getSensors();
    tabbedPane.removeAll();
    seriesList = new ArrayList<XYSeriesCollection>();
    chartList = new ArrayList<JFreeChart>();
    functions = new ArrayList<Function>();
    xLabelList = new ArrayList<JLabel>();
    yLabelList = new ArrayList<JLabel>();
    fieldsTable = new Hashtable<String, ArrayList<JFormattedTextField>>();

    for (int i = 0; i < sensors.length; i++) {
      Sensor sensor = sensors[i];
      String sensorId = sensor.getId();
      Object[] vars = sensor.getVarsNames();
      JPanel mainPanel = new JPanel(new GridBagLayout());

      GridBagConstraints c = new GridBagConstraints();
      // add labels
      c.anchor = GridBagConstraints.LINE_START;
      c.gridx = c.gridy = 0;
      c.gridwidth = 2;
      c.weighty = 0.5;
      c.insets.left = 5;
      JLabel label = new JLabel("Last Raw: " + getLastADCValue(sensorId));
      label.setToolTipText(TOOL_TIP_LAST_LABEL);
      label.setForeground(LAST_HIGHLIGHT_COLOR);
      mainPanel.add(label, c);

      JLabel l = new JLabel();
      l.setForeground(DOM_HIGHLIGHT_COLOR);
      c.gridx = 0;
      c.gridy++;
      xLabelList.add(l);
      mainPanel.add(l, c);

      l = new JLabel();
      l.setForeground(RANGE_HIGHLIGHT_COLOR);
      c.gridy++;
      yLabelList.add(l);
      mainPanel.add(l, c);

      c.gridy++;
      c.gridwidth = 1;
      mainPanel.add(new JLabel("Min. X "), c);

      c.gridy++;
      mainPanel.add(new JLabel("Max. X "), c);

      c.gridy++;
      mainPanel.add(new JLabel("Inc. X "), c);

      c.gridy++;
      c.gridwidth = 2;
      c.fill = GridBagConstraints.HORIZONTAL;
      mainPanel.add(new JSeparator(), c);

      c.gridwidth = 1;
      for (int j = 0; j < vars.length; j++) {
        String var = (String) vars[j];
        c.gridy++;
        mainPanel.add(new JLabel(var + " "), c);
      }
      c.insets.left = 0;
      // Add fields
      ArrayList<JFormattedTextField> fieldList = new ArrayList<JFormattedTextField>();
      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridx = 1;
      c.gridy = 3;
      mainPanel.add(createLimitTextFields("minx", DEF_MIN_X), c);

      c.gridy++;
      mainPanel.add(createLimitTextFields("maxx", DEF_MAX_X), c);

      c.gridy++;
      mainPanel.add(createLimitTextFields("inc", DEF_INC_X), c);

      c.gridy++;// for the JSeparator
      int height = 6;

      for (int j = 0; j < vars.length; j++) {
        String var = (String) vars[j];
        c.gridy++;
        height++;
        JFormattedTextField tf = createVarsTextFields(var,
            sensor.getValueOf(var));
        fieldList.add(tf);
        mainPanel.add(tf, c);
      }

      c.insets.left = 5;
      // Add buttons/combobox
      c.gridx = 0;
      c.gridwidth = 2;
      c.gridy++;
      mainPanel.add(createComboBoxXOpt(sensorId), c);

      c.gridy++;
      mainPanel.add(
          createButton("Reset values", TOOL_TIP_RESET_BT, sensorId,
              new ButtonResetAction()), c);

      c.gridy++;
      mainPanel.add(
          createButton("Show formulas", TOOL_TIP_FORM_BT, sensorId,
              new ButtonFormulaAction()), c);
      height += 3;

      // Add chart
      c.fill = GridBagConstraints.BOTH;
      c.gridx = 2;
      c.gridy = 1;
      c.weightx = 1;
      c.gridwidth = 2;
      c.weighty = 0.5;
      c.gridheight = height - 1;
      ChartPanel chartPanel = createChart(sensorId);
      mainPanel.add(chartPanel, c);

      fieldsTable.put(sensorId, fieldList);
      tabbedPane.add(mainPanel, sensorId);

      // Add Slider
      c.fill = GridBagConstraints.BOTH;
      c.anchor = GridBagConstraints.CENTER;
      c.gridx = 2;
      c.gridy = height;
      c.gridheight = 1;
      JSlider sld = configSlider();
      //setSliderPos(getLastADCDnumValue(sensorId), sld, chartPanel.getChart());
      mainPanel.add(sld, c);

    }
    add(tabbedPane, BorderLayout.CENTER);
    updateUI();
  }

  private JSlider configSlider() {
    JSlider sld = new JSlider(0, 1000, 500);
    sld.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        int i = tabbedPane.getSelectedIndex();
        JFreeChart chart = chartList.get(i);
        Function func = functions.get(i);
        int pos = ((JSlider) e.getSource()).getValue();
        XYPlot plot = (XYPlot) chart.getPlot();
        ValueAxis domainAxis = plot.getDomainAxis();
        Range rangeAxis = domainAxis.getRange();
        Double d = domainAxis.getLowerBound() + ((double) pos / 1000D)
            * rangeAxis.getLength();
        plot.setDomainCrosshairValue(d);
        d = func.domainRealValue(d);
        plot.setRangeCrosshairValue(func.f(d));
      }
    });
    return sld;
  }
  
/*
  private void setSliderPos(double x, JSlider sld, JFreeChart chart) {
    XYPlot plot = (XYPlot) chart.getPlot();
    ValueAxis domainAxis = plot.getDomainAxis();
    Range rangeAxis = domainAxis.getRange();
    Double lower = domainAxis.getLowerBound();
    Double axisLenght = rangeAxis.getLength();
    Double pos = ((x - lower) / axisLenght) * sld.getMaximum();
    if (pos > 1000)
      pos = 1000D;
    else if (pos < 0)
      pos = 0D;
    sld.setValue((int)Math.floor(pos));
    sld.updateUI();
  }
  */
  
  private JFormattedTextField createLimitTextFields(String fieldName, int value) {
    NumberFormat format = NumberFormat.getIntegerInstance();
    JFormattedTextField valueField = new JFormattedTextField(format);
    valueField.setValue(value);
    valueField.setColumns(10);
    valueField.setName(fieldName);
    valueField.addPropertyChangeListener("value", this);
    return valueField;
  }

  private JFormattedTextField createVarsTextFields(String varName, double value) {
    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMaximumFractionDigits(15);
    JFormattedTextField valueField = new JFormattedTextField(format);
    valueField.setValue(value);
    valueField.setColumns(10);
    valueField.setName(varName);
    valueField.addPropertyChangeListener("value", this);
    return valueField;
  }

  private JButton createButton(String text, String tip, String name,
      ActionListener Listener) {
    JButton b = new JButton(text);
    b.setToolTipText(tip);
    b.setName(name);
    b.addActionListener(Listener);
    return b;
  }

  private JComboBox<String> createComboBoxXOpt(String sensorId) {
    String[] opt = { "adc value", "Sensor voltage(Vs)" };
    JComboBox<String> comboBoxXOpt = new JComboBox<String>();
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
    return comboBoxXOpt;
  }

  public void propertyChange(PropertyChangeEvent e) {
    Object source = e.getSource();
    String varName = ((JFormattedTextField) source).getName();
    double newValue = ((Number) ((JFormattedTextField) source).getValue())
        .doubleValue();
    int i = tabbedPane.getSelectedIndex();
    String sensorId = sensors[i].getId();

    if (varName.equals("minx"))
      functions.get(i).setMinX((int) newValue);
    else if (varName.equals("maxx"))
      functions.get(i).setMaxX((int) newValue);
    else if (varName.equals("inc"))
      functions.get(i).setIncrement((int) newValue);
    else {
      Sensor sensor = selectedNode.getNodeSensor(sensorId);
      sensor.setVar(varName, newValue);
      updateConfig(sensor.getId(), varName, newValue);
    }
    updateChart(i);
  }

  private void updateChart(int chartIndex) {
    dataset = seriesList.get(chartIndex);
    dataset.removeAllSeries();
    Function conv = functions.get(chartIndex);
    XYSeries series = new XYSeries("Conversion function (" + conv.getSensorId()
        + ")");
    genSerie(conv, series);
    dataset.addSeries(series);
    JFreeChart chart = chartList.get(chartIndex);
    XYPlot plot = (XYPlot) chart.getPlot();
    plot.setRangeCrosshairValue(conv.f(plot.getDomainCrosshairValue()));
    server.UpdateChart(conv.getSensorId());
  }

  private ChartPanel createChart(String sensorId) {
    if (selectedNode.getSensorDataCount() == 0)
      return new ChartPanel(null);
    data = selectedNode.getLastSD();
    Function conv = new Function(selectedNode.getNodeSensor(sensorId),
        selectedNode.PLATFORM_ADC_RESOLUTION) {
      protected double f(Double value) {
        return sensor.getConv(value);
      }
    };
    functions.add(conv);

    return paintChart(conv);
  }

  private ChartPanel paintChart(Function conv) {
    String sensorId = conv.getSensorId();
    XYSeries series = new XYSeries("Conversion function (" + sensorId + ")");
    genSerie(conv, series);
    dataset = new XYSeriesCollection();
    seriesList.add(dataset);
    dataset.addSeries(series);
    String title = "Conversion function (Node " + selectedNode.getID() + ")";
    JFreeChart chart = ChartFactory.createXYLineChart(title, // Title
        conv.getxTag(), // x-axis Label
        conv.getyTag(), // y-axis Label
        dataset, PlotOrientation.VERTICAL, //
        false, // Show Legend
        true, // Use tooltips
        false // Configure chart to generate URLs?
        );
    XYPlot plot = chart.getXYPlot();
    plot.getDomainAxis().setLabelPaint(DOM_HIGHLIGHT_COLOR);
    plot.getDomainAxis().setTickLabelPaint(DOM_HIGHLIGHT_COLOR);
    plot.getRangeAxis().setLabelPaint(RANGE_HIGHLIGHT_COLOR);
    plot.getRangeAxis().setTickLabelPaint(RANGE_HIGHLIGHT_COLOR);
    plot.setBackgroundPaint(Color.WHITE);
    plot.setDomainGridlinePaint(Color.GRAY);
    plot.setRangeGridlinePaint(Color.GRAY);
    chart.setBackgroundPaint(COLOR_JAVA_DEF);
    chart.setBorderPaint(COLOR_JAVA_SEPARATOR);

    plot.setOutlineStroke(new BasicStroke(1.0f));
    plot.setOutlinePaint(COLOR_JAVA_SEPARATOR);

    BasicStroke s = (BasicStroke) plot.getDomainGridlineStroke();
    BasicStroke stroke = new BasicStroke(1.0f, s.getEndCap(),
        BasicStroke.JOIN_BEVEL, 100.0f, s.getDashArray(), s.getDashPhase());

    Double x = getLastADCDnumValue(sensorId);
    plot.setDomainCrosshairValue(x, true);
    plot.setDomainCrosshairStroke(stroke);
    plot.setDomainCrosshairVisible(true);
    plot.setDomainCrosshairLockedOnData(true);
    plot.setDomainCrosshairPaint(DOM_HIGHLIGHT_COLOR);

    Double y = conv.f(x);
    plot.setRangeCrosshairValue(y, true);
    plot.setRangeCrosshairStroke(stroke);
    plot.setRangeCrosshairVisible(true);
    plot.setRangeCrosshairLockedOnData(true);
    plot.setRangeCrosshairPaint(RANGE_HIGHLIGHT_COLOR);

    ValueMarker valueMarker = new ValueMarker(x);
    valueMarker.setPaint(LAST_HIGHLIGHT_COLOR);
    valueMarker.setLabel("Last received");
    valueMarker.setLabelOffsetType(LengthAdjustmentType.EXPAND);
    valueMarker.setStroke(new BasicStroke(0.5f));
    valueMarker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
    valueMarker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
    
    //TODO Two Domain Axis instead of ( Vs / ADC) dropbox
    NumberAxis  va = new NumberAxis("Sensor Voltage");
    
    plot.setDomainAxis(va);

    plot.addDomainMarker(valueMarker, Layer.FOREGROUND);

    chart.addProgressListener(new ChartProgressListener() {
      public void chartProgress(ChartProgressEvent e) {
        if (e.getType() == ChartProgressEvent.DRAWING_FINISHED) {
          int i = tabbedPane.getSelectedIndex();
          Sensor s = selectedNode.getNodeSensor(tabbedPane.getTitleAt(i));
          XYPlot plot = (XYPlot) e.getChart().getPlot();
          Double x = plot.getDomainCrosshairValue();
          Double y = plot.getRangeCrosshairValue();
          JLabel xL = xLabelList.get(i);
          JLabel yL = yLabelList.get(i);
          xL.setText("x     = " + round(x, 2));
          yL.setText("f(x) = " + round(y, s.getRoundDigits()) + " ("
              + s.getUnits() + ")");
        }
      }
    });
    chartList.add(chart);
    return (new ChartPanel(chart));
  }

  private void genSerie(Function conv, XYSeries series) {
    for (int i = conv.getMinX(); i <= conv.getMaxX(); i += conv.getIncrement()) {
      series.add(conv.getX(i), conv.f((double) i));
    }
  }

  String getLastADCValue(String sensor) {
    if (selectedNode.getLastSD() == null)
      return "";
    Double lastValue = selectedNode.getLastValueOf(sensor);
    return Double.toString(lastValue);
  }

  Double getLastADCDnumValue(String sensor) {
    if (selectedNode.getLastSD() == null)
      return Double.NaN;
    return selectedNode.getLastValueOf(sensor);
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
      String sensor = ((Component) e.getSource()).getName();
      int i = tabbedPane.getSelectedIndex();
      selectedNode.setDefaultValues(sensor);
      ArrayList<JFormattedTextField> TextFields = fieldsTable.get(sensor);
      ListIterator<JFormattedTextField> it = TextFields.listIterator();
      while (it.hasNext()) {
        reset(it.next(), i);
      }
      updateChart(i);
    }

    public void reset(JFormattedTextField field, int i) {
      String varName = field.getName();
      String sensorId = sensors[i].getId();
      Sensor sensor = selectedNode.getNodeSensor(sensorId);
      Double value = sensor.getValueOf(varName);
      ((JFormattedTextField) field).setValue(value);
      // default values do not need to be stored
      removeFromConfig(sensorId, varName, value);
    }
  }

  public class ButtonFormulaAction implements ActionListener {
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
   * values do not need to be stored (they are set on start up)
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
    private int maxX;
    private int increment = 5;// increase for better GUI performance
    private String sensorId;
    private boolean showVs = false;
    private int aDCresolution;
    public static final int DEF_MAX_X = 10000;

    Function(Sensor sensor, int aDCresolution) {
      this.sensor = sensor;
      maxX = sensor.getADCResolution();
      if (maxX<=0) maxX=DEF_MAX_X;
          
      yLabel = sensor.getUnits();
      sensorId = sensor.getId();
      this.aDCresolution = aDCresolution;
    }

    public Number getX(int i) {
      Sensor s;
      if (showVs) {
        s = data.getNode().getNodeSensor(sensorId);
        double vRef = s.getValueOf("Vref");
        return ((double) i / (double) aDCresolution) * vRef;
      } else
        return i;
    }

    public double domainRealValue(double V) {
      if (showVs) {
        double vRef = sensor.getValueOf("Vref");
        V = (V * aDCresolution) / vRef;
      }
      return V;
    }

    abstract double f(Double value);

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
      if (!sensor.ADC()) {
        showVs = false;
        return;
      }
      Sensor s = data.getNode().getNodeSensor(sensorId);
      if (s.getValueOf("Vref") == Double.NaN) {
        showVs = false;
        return;
      }

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

  static String round(double d, int digits) {
    NumberFormat frm = NumberFormat.getInstance();
    frm.setMaximumFractionDigits(digits);
    frm.setRoundingMode(RoundingMode.UP);
    return frm.format(d);
  }

  public void nodeAdded(Node node) {/* ignore */
  }

  public void nodeDataReceived(SensorData sensorData) {/* ignore */
  }

  public void clearNodeData() {/* ignore */
  }
}
