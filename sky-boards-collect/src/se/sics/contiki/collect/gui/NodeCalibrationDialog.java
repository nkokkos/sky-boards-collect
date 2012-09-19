/*
 * NodeCalibrationDialog
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 23 jul 2012
 */

package se.sics.contiki.collect.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import se.sics.contiki.collect.CollectServer;
import se.sics.contiki.collect.Configurable;
import se.sics.contiki.collect.Node;
import se.sics.contiki.collect.Sensor;
import se.sics.contiki.collect.SensorData;
import se.sics.contiki.collect.Variable;

public class NodeCalibrationDialog extends JFrame
    implements
      PropertyChangeListener,
      Configurable {

  private static final long serialVersionUID = 1L;
  public final int DEF_MIN_X = 1;
  public final int DEF_MAX_X = 4096;
  public final int DEF_INC_X = 5;

  Node node;
  Sensor[] sensors;
  SensorData data;
  Vector<JPanel> charts;
  Vector<Function> functions;
  private Hashtable<String, JPanel> fieldsTable;
  Properties calibrationConfig;

  JTabbedPane tabbedPane; // tabbedPanel
  JPanel mainPanel; // main Panel for each tab
  JFreeChart chart;
  ChartPanel chartPanel; // chartPanel in mainPanel

  JPanel varsPanel; // varsPanel in mainPanel
  JPanel labelPaneVars;
  JPanel fieldPaneVars;

  JButton buttonReset;
  JButton buttonFormula;
  JComboBox<String> comboBoxXOpt;
  int nodeType;

  /* do not use delim char in sensor/variable name */
  public static final char delim = '\n';

  public NodeCalibrationDialog(CollectServer server, String title,
      final Node node, Properties config) {
    super(title);
    fieldsTable = new Hashtable<String, JPanel>();

    calibrationConfig = config;
    charts = new Vector<JPanel>();
    functions = new Vector<Function>();
    this.node = node;
    sensors = node.getSensors();
    tabbedPane = new JTabbedPane();
    String sensorId;

    for (int i = 0; i < node.getSensorsCount(); i++) {
      sensorId = sensors[i].getId();
      mainPanel = new JPanel();
      mainPanel.setLayout(new FlowLayout());
      tabbedPane.add(mainPanel);
      tabbedPane.setTitleAt(i, sensorId + " sensor");

      varsPanel = new JPanel();
      varsPanel.setLayout(new BorderLayout());
      labelPaneVars = new JPanel(new GridLayout(0, 1));
      fieldPaneVars = new JPanel(new GridLayout(0, 1));
      fieldsTable.put(sensorId, fieldPaneVars);

      labelPaneVars.add(new JLabel("Last adc_value: "));
      fieldPaneVars.add(new JLabel(getLastADCValue(sensorId)));

      insertVarPaneSeparator();

      labelPaneVars.add(new JLabel("Min. X: "));
      labelPaneVars.add(new JLabel("Max. X: "));
      labelPaneVars.add(new JLabel("Inc. X: "));

      createLimitTextFields(i, "minx", DEF_MIN_X);
      createLimitTextFields(i, "maxx", DEF_MAX_X);
      createLimitTextFields(i, "inc", DEF_INC_X);

      insertVarPaneSeparator();

      Variable[] vars = sensors[i].getVars();

      for (int j = 0; j < vars.length; j++) {
        // create labels
        labelPaneVars.add(new JLabel(vars[j].getName() + ": "));
        // create fields
        createVarsTextFields(i, j);
      }

      chartPanel = createChart(sensors[i].getId());
      if (chartPanel==null) return;
      charts.add(chartPanel);

      createComboBoxXOpt(sensors[i].getId());

      buttonReset = new JButton("Reset values");
      buttonReset.setName(sensorId);
      buttonReset.addActionListener(new ButtonResetAction());

      buttonFormula = new JButton("Show formulas");
      buttonFormula.setName(sensorId);
      buttonFormula.addActionListener(new ButtonFormulaAction());

      insertVarPaneSeparator();
      labelPaneVars.add(new JLabel("X-axis:"));
      fieldPaneVars.add(comboBoxXOpt);

      insertVarPaneSeparator();
      labelPaneVars.add(new JLabel(""));
      fieldPaneVars.add(buttonReset);

      labelPaneVars.add(new JLabel(""));
      fieldPaneVars.add(buttonFormula);

      varsPanel.add(labelPaneVars, BorderLayout.CENTER);
      varsPanel.add(fieldPaneVars, BorderLayout.LINE_END);

      mainPanel.add(varsPanel);
      mainPanel.add(chartPanel);

      // TODO Add option "compare to->(Node with same firmware)"
    }
    this.getContentPane().add(tabbedPane);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    pack();
    setVisible(true);
  }

  private void insertVarPaneSeparator() {
    labelPaneVars.add(new JLabel(""));
    fieldPaneVars.add(new JLabel(""));
  }

  String getLastADCValue(String sensor) {
    if (node.getLastSD()==null) return "";
    int lastValue = node.getLastValueOf(sensor);
    return Integer.toString(lastValue);
  }

  void createLimitTextFields(int idx, String s, int value) {
    NumberFormat format = NumberFormat.getIntegerInstance();
    JFormattedTextField valueField = new JFormattedTextField(format);
    valueField.setValue(value);
    valueField.setColumns(10);
    valueField.setName(sensors[idx].getId() + delim + s);
    valueField.addPropertyChangeListener("value", this);
    fieldPaneVars.add(valueField);
  }

  void createVarsTextFields(int s, int v) {
    Variable[] vars = sensors[s].getVars();
    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMaximumFractionDigits(15);
    JFormattedTextField valueField = new JFormattedTextField(format);
    valueField.setValue(new Double(vars[v].getValue()));
    valueField.setColumns(10);
    valueField.setName(sensors[s].getId() + delim + vars[v].getName());
    valueField.addPropertyChangeListener("value", this);
    fieldPaneVars.add(valueField);
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
        ((JFormattedTextField) source).getName(), delim + "");
    String sensorStr = tokens.nextToken();
    String varStr = tokens.nextToken();
    Variable var;
    double newValue = ((Number) ((JFormattedTextField) source).getValue())
        .doubleValue();

    Sensor sensor = node.getNodeSensor(sensorStr);
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
    JPanel chartPanel = charts.get(chartIndex);
    chartPanel.removeAll();
    chartPanel.add(paintChart(functions.get(chartIndex)));
    chartPanel.updateUI();
  }

  private ChartPanel createChart(String sensorId) {
    if (node.getSensorDataCount() == 0)
      return null;
    data = node.getSensorData(node.getSensorDataCount() - 1);
    Function conv = new Function(node.getNodeSensor(sensorId)) {
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

    XYSeriesCollection dataset = new XYSeriesCollection();
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

  private JLabel getConversionImage(String sensorName) {
    if (node.getSensorDataCount() == 0)
      return null;
    String firmName = node.getFirmName();
    ImageIcon imgIcon = new ImageIcon("./images/" + firmName + "-" + sensorName
        + "-" + "formula.jpeg");
    JLabel imgLabel = new JLabel();
    imgLabel.setIcon(imgIcon);
    return imgLabel;
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

  private class ButtonResetAction implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      String sensor = ((Component) e.getSource()).getName();
      node.setDefaultValues(sensor);

      JPanel fieldPaneVars = fieldsTable.get(sensor);
      Component[] cArray = fieldPaneVars.getComponents();
      for (int i = 0; i < cArray.length; i++) {
        reset(cArray[i]);
      }
      for (int i = 0; i < sensors.length; i++)
        if (sensors[i].getId().equals(sensor))
          updateChart(i);

    }

    public void reset(Component field) {
      if (!(field instanceof JFormattedTextField))
        return;
      String fieldName = field.getName();
      if (fieldName == "" || fieldName == null)
        return;
      StringTokenizer tokens = new StringTokenizer(field.getName(), delim + "");
      String sensorStr = tokens.nextToken();
      String varStr = tokens.nextToken();
      Variable var;

      Sensor sensor = node.getNodeSensor(sensorStr);
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
        "var," + node.getID() + "," + sensor + "," + "" + var,
        String.valueOf(newVal));
  }

  @Override
  public void updateConfig(Properties config) {
    /*
     * for (int i=0;i<sensors.length;i++){ Variable[] vars=sensors[i].getVars();
     * for (int j=0;j<vars.length;j++) config.put("var," + node.getID() + "," +
     * sensors[i].getName() + "," + "" + vars[j].getName(), String.
     * valueOf(vars[j].getValue())); }
     */
  }
}
