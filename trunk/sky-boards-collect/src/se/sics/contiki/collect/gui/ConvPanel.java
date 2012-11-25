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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.UIDefaults;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
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
  private Node workingCopyNode; // working copy of the node
  private Sensor[] sensors;
  private ArrayList<JFreeChart> chartList;
  private ArrayList<XYSeriesCollection> seriesList;
  private ArrayList<Function> functions;
  private ArrayList<JTextPane> crossHairStatusList;
  private  ArrayList<JFormattedTextField> fieldList;
  private Properties calibrationConfig;

  private JTabbedPane tabbedPane; // tabbedPanel
  TabChangeListener tabChangeListener;
  private XYSeriesCollection dataset;

  public final int DEF_MIN_X = 1;
  public int DEF_MAX_X;
  public final int DEF_INC_X = 5;
  public final String TOOL_TIP_RESET_BT = "Reset to default values";
  public final String TOOL_TIP_SAVE_BT = "Confirm changes and update charts taking new values as input";
  public final String TOOL_TIP_FORM_BT = "Display sensor conversions expressions";
  public final String TOOL_TIP_LAST_LABEL = "Last raw or ADC value received from the node";

  private final Color COLOR_DOM_HIGHLIGHT = Color.BLUE;
  private final Color COLOR_RANGE_HIGHLIGHT = new Color(0, 153, 0);
  private final Color COLOR_LAST_HIGHLIGHT = new Color(102, 0, 0);
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
    tabChangeListener = new TabChangeListener();
  }

  private class TabChangeListener implements ChangeListener {
    public void stateChanged(ChangeEvent e) {
      // This has to be done because sensors can
      // have dependent expressions (eg: Temperature
      // compensation in SHT11 Humidity sensor).
      // See SHT11Humidity.java and SHT11Temperature.java
      updateChart(tabbedPane.getSelectedIndex());
    }
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

  private void copySelectedNode(Node n) {
    selectedNode = n;
    int nodeType = selectedNode.getLastSD().getType();
    String id = selectedNode.getID();
    workingCopyNode = server.createNode(id, nodeType);
    workingCopyNode.copySensorVarsFrom(selectedNode);
  }

  public void nodesSelected(Node[] node) {
    if (node == null || node.length > 1)
      return;

    // Do not waste resources painting
    // components unless this class is the selected tab
    if (!active)
      return;
    tabbedPane.removeChangeListener(tabChangeListener);

    copySelectedNode(node[0]);

    DEF_MAX_X = selectedNode.PLATFORM_ADC_RESOLUTION;
    sensors = selectedNode.getSensors();
    tabbedPane.removeAll();
    seriesList = new ArrayList<XYSeriesCollection>();
    chartList = new ArrayList<JFreeChart>();
    functions = new ArrayList<Function>();
    crossHairStatusList = new ArrayList<JTextPane>();
    fieldList = new ArrayList<JFormattedTextField>();
    GridBagConstraints c = new GridBagConstraints();

    for (int i = 0; i < sensors.length; i++) {
      Sensor sensor = sensors[i];
      String sensorId = sensor.getId();
      Object[] vars = sensor.getVarsNames();
      JPanel mainPanel = new JPanel(new GridBagLayout());
      // Add crosshair status text pane
      JTextPane status = new JTextPane();
      status.setBorder(LineBorder.createBlackLineBorder());
      status.setEditable(false);
      //status.setBackground(COLOR_JAVA_DEF);
      StyledDocument doc = status.getStyledDocument();
      Style def = StyleContext.getDefaultStyleContext().getStyle(
          StyleContext.DEFAULT_STYLE);
      doc.addStyle("regular", def);
      Style s = doc.addStyle("x", null);
      StyleConstants.setForeground(s, COLOR_DOM_HIGHLIGHT);
      s = doc.addStyle("y", null);
      StyleConstants.setForeground(s, COLOR_RANGE_HIGHLIGHT);
      c.gridx = 0;
      c.gridy=0;
      c.gridwidth = 4;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.insets = new Insets(0,0,10,0);
      crossHairStatusList.add(status);
      mainPanel.add(status, c);
      // Add chart
      c.fill = GridBagConstraints.BOTH;
      c.gridx = 0;
      c.gridy = 1;
      c.weightx = 1;
      c.gridwidth = 4;
      c.weighty = 0.5;
      c.weightx = 0.5;
      ChartPanel chartPanel = createChart(sensorId);
      mainPanel.add(chartPanel, c);
      // Add Slider
      c.gridy++;
      c.weighty = 0;
      c.fill = GridBagConstraints.HORIZONTAL;
      JSlider sld = configSlider();
      mainPanel.add(sld, c);

      // Add controls
      c.gridy++;
      c.gridwidth=1;
      c.gridx=1;
      c.weightx=0;
      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.LINE_END;
      c.insets = new Insets(5,20,0,0);
      mainPanel.add(new JLabel("Constant: "),c);

      JComboBox<Object> varsComboBox = new JComboBox<Object>(vars);
      if (vars.length>0)
        varsComboBox.setSelectedIndex(0);
      varsComboBox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          int i=tabbedPane.getSelectedIndex();
          @SuppressWarnings("unchecked")
          String var=(String) ((JComboBox<Object>)e.getSource()).getSelectedItem();
          Sensor s=sensors[i];
          fieldList.get(i).setValue(s.getValueOf(var));
          fieldList.get(i).repaint();
        }
      });
      c.gridx++;
      c.weightx=0;
      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.CENTER;
      c.insets = new Insets(5,0,0,5);
      mainPanel.add(varsComboBox,c);

      NumberFormat format = NumberFormat.getNumberInstance();
      format.setMaximumFractionDigits(15);
      JFormattedTextField valueField = new JFormattedTextField(format);
      valueField.setValue(532D);
      //valueField.setColumns(12);
      //valueField.addPropertyChangeListener("value", this);
      fieldList.add(valueField);
      c.gridx++;
      c.gridwidth=1;
      c.weightx=0.1;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.insets = new Insets(5,0,0,20);
      mainPanel.add(valueField,c);
      
      c.gridy++;
      c.gridx=0;
      c.gridwidth=4;
      c.fill = GridBagConstraints.HORIZONTAL;
      JPanel buttonPanel = new JPanel();
      buttonPanel.add(createButton("Reset", TOOL_TIP_RESET_BT, sensorId,
          new ButtonResetAction()));
      buttonPanel.add(createButton("Confirm", TOOL_TIP_SAVE_BT,
          sensorId, new ButtonSaveChangesAction()));
      buttonPanel.add(createButton("Show", TOOL_TIP_FORM_BT, sensorId,
          new ButtonFormulaAction()));
      c.insets = new Insets(5,20,0,20);
      mainPanel.add(buttonPanel,c);

      tabbedPane.add(mainPanel, sensorId);
    }
    add(tabbedPane, BorderLayout.CENTER);
    updateUI();
    tabbedPane.addChangeListener(tabChangeListener);
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

  private JFormattedTextField createLimitTextFields(String fieldName, int value) {
    NumberFormat format = NumberFormat.getIntegerInstance();
    JFormattedTextField valueField = new JFormattedTextField(format);
    valueField.setValue(value);
    valueField.setName(fieldName);
    valueField.addPropertyChangeListener("value", this);
    return valueField;
  }

  private JFormattedTextField createVarsTextFields(String varName, double value) {
    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMaximumFractionDigits(15);
    JFormattedTextField valueField = new JFormattedTextField(format);
    valueField.setValue(value);
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
      Sensor sensor = workingCopyNode.getNodeSensor(sensorId);
      sensor.setVar(varName, newValue);
      // updateConfig(sensor.getId(), varName, newValue);
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
  }

  private ChartPanel createChart(String sensorId) {
    if (selectedNode.getSensorDataCount() == 0)
      return new ChartPanel(null);

    Sensor s = workingCopyNode.getNodeSensor(sensorId);
    Function conv = new Function(s, selectedNode.PLATFORM_ADC_RESOLUTION) {
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
    plot.getDomainAxis().setLabelPaint(COLOR_DOM_HIGHLIGHT);
    plot.getDomainAxis().setTickLabelPaint(COLOR_DOM_HIGHLIGHT);
    plot.getRangeAxis().setLabelPaint(COLOR_RANGE_HIGHLIGHT);
    plot.getRangeAxis().setTickLabelPaint(COLOR_RANGE_HIGHLIGHT);
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
    plot.setDomainCrosshairPaint(COLOR_DOM_HIGHLIGHT);

    Double y = conv.f(x);
    plot.setRangeCrosshairValue(y, true);
    plot.setRangeCrosshairStroke(stroke);
    plot.setRangeCrosshairVisible(true);
    plot.setRangeCrosshairLockedOnData(true);
    plot.setRangeCrosshairPaint(COLOR_RANGE_HIGHLIGHT);

    ValueMarker valueMarker = new ValueMarker(x);
    valueMarker.setPaint(COLOR_LAST_HIGHLIGHT);
    valueMarker.setLabel("Last received");
    valueMarker.setLabelOffsetType(LengthAdjustmentType.EXPAND);
    valueMarker.setStroke(new BasicStroke(0.5f));
    valueMarker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
    valueMarker.setLabelTextAnchor(TextAnchor.TOP_LEFT);

    // TODO Two Domain Axis instead of ( Vs / ADC) dropbox
    // NumberAxis va = new NumberAxis("Sensor Voltage");

    // plot.setDomainAxis(1, va);

    // plot.addDomainMarker(valueMarker, Layer.FOREGROUND);

    chart.addProgressListener(new ChartProgressListener() {
      public void chartProgress(ChartProgressEvent e) {
        if (e.getType() == ChartProgressEvent.DRAWING_FINISHED) {
          int i = tabbedPane.getSelectedIndex();
          JTextPane status = crossHairStatusList.get(i);
          Sensor s = sensors[i];
          StyledDocument doc = status.getStyledDocument();
          XYPlot plot = (XYPlot) e.getChart().getPlot();
          Double x = plot.getDomainCrosshairValue();
          Double y = plot.getRangeCrosshairValue();
          try {
            System.out.println(doc.getEndPosition());
            doc.remove(0, doc.getLength());
            String xValue = round(x, 2);
            String yValue = round(y, s.getRoundDigits());
            String a = "   f ( ";
            String b = " ) = ";
            String c = " "+s.getUnits();
            String statusText = a + xValue + b + yValue + c;
            doc.insertString(0, statusText, doc.getStyle("main"));
            doc.setCharacterAttributes(a.length(), xValue.length(),
                doc.getStyle("x"), false);
            doc.setCharacterAttributes(
                a.length() + xValue.length() + b.length(), yValue.length()+c.length(),
                doc.getStyle("y"), false);
          } catch (BadLocationException e1) {
            e1.printStackTrace();
          }
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
      return "No data";
    Double lastValue = selectedNode.getLastValueOf(sensor);
    return Double.toString(lastValue);
  }

  Double getLastADCDnumValue(String sensor) {
    if (selectedNode.getLastSD() == null)
      return Double.NaN;
    return selectedNode.getLastValueOf(sensor);
  }

  private JLabel getConversionImage(String sensorName) {
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
      workingCopyNode.setDefaultValues(sensor);
      /*ArrayList<JFormattedTextField> TextFields = fieldsTable.get(sensor);
      ListIterator<JFormattedTextField> it = TextFields.listIterator();
      while (it.hasNext()) {
        reset(it.next(), i);
      }
      updateChart(i);*/
    }

    public void reset(JFormattedTextField field, int i) {
      String varName = field.getName();
      String sensorId = sensors[i].getId();
      Sensor sensor = workingCopyNode.getNodeSensor(sensorId);
      Double value = sensor.getValueOf(varName);
      ((JFormattedTextField) field).setValue(value);
      // default values do not need to be stored
      // removeFromConfig(sensorId, varName, value);
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

  public class ButtonSaveChangesAction implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      // copy changes in workingcopy to selectednode instance
      // server.updateChart(conv.getSensorId());
      // for every value update variable value on disk ONLY IF , the value of
      // workingcopy != the value of selected node copy
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
      if (maxX <= 0)
        maxX = DEF_MAX_X;

      yLabel = sensor.getUnits();
      sensorId = sensor.getId();
      this.aDCresolution = aDCresolution;
    }

    public Number getX(int i) {
      if (showVs) {
        double vRef = sensor.getValueOf("Vref");
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
      if (sensor.getValueOf("Vref") == Double.NaN) {
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
