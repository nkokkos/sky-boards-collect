/*
 * SensorAdjustPanel
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 28 Nov 2012
 */

package se.sics.contiki.collect.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
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
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

import se.sics.contiki.collect.Node;
import se.sics.contiki.collect.Sensor;
import se.sics.contiki.collect.SensorData;

public class SensorAdjustPanel extends JPanel {

  private static final long serialVersionUID = 1L;
  // Logic
  private Sensor sensor;
  public String sensorId;
  private String selectedVar;
  private Node sensorNode;
  private boolean autoRange;
  private boolean saveChanges;
  Properties config;
  ConvPanel convPanel;
  
  // JFreeChart
  private JFreeChart chart;
  private ChartPanel chartPanel;
  private XYSeriesCollection dataset;

  // GUI
  private Function function;
  private JTextPane crossHairTextPane;
  private JFormattedTextField selectedVarField;
  private JLabel lastRawLabel;
  private JSlider slider;
  private JTextPane intervalPane;
  private JSpinner spinner;
  private JCheckBox autoRangeCheckBox;
  private JCheckBox saveCheckBox;
  private JComboBox<Object> varsComboBox;

  // constants
  public final int DEF_MIN_X = 1;
  public final int DEF_INC_X = 1; // increase for better GUI performance
  public final String TOOL_TIP_RESET_BT = "Reset all sensor's conversion expressions constants to default values";
  private final String TOOL_TIP_FORM_BT = "Display sensor's conversions expressions";
  private final String TOOL_TIP_LAST_LABEL = "Last raw or ADC value received from the node";
  private final String TOOL_TIP_CLICK_CHANGE = "Click to change";
  private final String TOOL_TIP_SAVE_CHECK = "Check the box to auto-save changes in constants and update application's charts";
  private final String INTERVAL_SELECTION_TITLE = "Domain interval. Use carefully. Increse step for large intervals.";
  private final Color COLOR_DOM_HIGHLIGHT = Color.BLUE;
  private final Color COLOR_RANGE_HIGHLIGHT = new Color(0, 153, 0);
  private final Color COLOR_LAST_HIGHLIGHT = new Color(102, 0, 0);
  private Color COLOR_MOUSE_ON;
  private Color COLOR_BACKGROUND;

  functionGenerator fc;

  public SensorAdjustPanel(Node n, Sensor s, Properties config, ConvPanel convPanel) {
    sensor = s;
    sensorNode = n;
    this.config = config;
    this.convPanel = convPanel;
    UIDefaults defaults = javax.swing.UIManager.getDefaults();
    COLOR_BACKGROUND = defaults.getColor("Panel.background");
    COLOR_MOUSE_ON = defaults.getColor("Button.shadow");

    sensorId = sensor.getId();
    setupCrossHairTextPane();
    setupLastRawLabel();
    setupChart();
    setupSlider();
    setupIntervalPane();
    setupSpinner();
    setupAutoRangeCheckbox();
    setupVarsComboBox();
    setupValueField();
    setupSaveChangesCheckBox();
    setDomainIntervalText(intervalPane, DEF_MIN_X, function.getMaxX());

    GridBagConstraints c = new GridBagConstraints();
    setLayout(new GridBagLayout());// ..totally :)

    // crosshair status text pane
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 3;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(0, 0, 10, 0);
    add(crossHairTextPane, c);

    // label of last ADC value
    c.gridx = 3;
    c.gridwidth = 2;
    add(lastRawLabel, c);

    // chart
    c.fill = GridBagConstraints.BOTH;
    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 1;
    c.gridwidth = 5;
    c.weighty = 0.5;
    c.weightx = 0.5;
    add(chartPanel, c);

    // Slider
    c.gridy++;
    c.weighty = 0;
    c.fill = GridBagConstraints.HORIZONTAL;
    add(slider, c);

    // domain label
    c.gridy++;
    c.gridwidth = 1;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = new Insets(0, 20, 0, 0);
    add(new JLabel("Domain: "), c);

    // interval setup pane
    c.gridx++;
    c.weightx = 0.5;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.CENTER;
    c.insets = new Insets(0, 5, 0, 5);
    add(intervalPane, c);

    // step label
    c.gridx++;
    c.weightx = 0;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.LINE_END;
    add(new JLabel("Step: "), c);

    // step's spinner
    c.gridx++;
    c.weightx = 0.5;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(0, 0, 0, 20);
    add(spinner, c);

    // auto range checkbox
    c.gridwidth = 1;
    c.gridx++;
    c.weightx = 0;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = new Insets(5, 0, 0, 0);
    add(autoRangeCheckBox, c);

    // constant label
    c.gridy++;
    c.gridwidth = 1;
    c.gridx = 0;
    c.weightx = 0;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = new Insets(5, 20, 0, 0);
    add(new JLabel("Constant: "), c);

    // add combo box
    c.gridx++;
    c.weightx = 0.1;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.LINE_START;
    c.insets = new Insets(5, 5, 0, 5);
    add(varsComboBox, c);

    // value label
    c.gridx++;
    c.weightx = 0;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = new Insets(5, 5, 0, 0);
    add(new JLabel("value: "), c);

    // text field of selected var
    c.gridx++;
    c.gridwidth = 1;
    c.weightx = 0.5;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5, 0, 0, 20);
    add(selectedVarField, c);

    // save changes check box
    c.gridwidth = 1;
    c.gridx++;
    c.weightx = 0;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = new Insets(5, 0, 0, 0);
    add(saveCheckBox, c);

    // buttons
    c.gridwidth = 5;
    c.gridx = 0;
    c.gridy++;
    c.weightx = 0.1;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.CENTER;
    c.insets = new Insets(5, 5, 5, 5);
    add(setupButtonPanel(), c);
  }

  private void setupCrossHairTextPane() {
    crossHairTextPane = new JTextPane();
    crossHairTextPane.setBorder(LineBorder.createBlackLineBorder());
    crossHairTextPane.setEditable(false);
    crossHairTextPane.setBackground(COLOR_BACKGROUND);
    StyledDocument doc = crossHairTextPane.getStyledDocument();
    Style def = StyleContext.getDefaultStyleContext().getStyle(
        StyleContext.DEFAULT_STYLE);
    doc.addStyle("regular", def);
    Style style = doc.addStyle("x", null);
    StyleConstants.setForeground(style, COLOR_DOM_HIGHLIGHT);
    style = doc.addStyle("y", null);
    StyleConstants.setForeground(style, COLOR_RANGE_HIGHLIGHT);
  }

  private void setupLastRawLabel() {
    lastRawLabel = new JLabel("Last Raw: " + String.valueOf(getLastADCValue()),
        JLabel.CENTER);
    lastRawLabel.setBorder(LineBorder.createBlackLineBorder());
    lastRawLabel.setToolTipText(TOOL_TIP_LAST_LABEL);
    lastRawLabel.setForeground(COLOR_LAST_HIGHLIGHT);
  }

  private void setupChart() {
    if (sensorNode.getSensorDataCount() == 0)
      chartPanel = new ChartPanel(null);

    function = new Function(sensor) {
      protected double f(Double value) {
        return sensor.getConv(value);
      }
    };

    dataset = new XYSeriesCollection();
    dataset.addSeries(new XYSeries(""));
    String title = "Conversion function (Node " + sensorNode.getID() + " "+ sensor.getId() + ")";
    chart = ChartFactory.createXYLineChart(title, // Title
        function.getxTag(), // x-axis Label
        function.getyTag(), // y-axis Label
        dataset, PlotOrientation.VERTICAL, //
        false, // Show Legend
        true, // Use tooltips
        false // Configure chart to generate URLs?
        );
    chart.setTitle(new TextTitle(title,new Font("",Font.BOLD,14)));
    XYPlot plot = chart.getXYPlot();

    ValueAxis va = plot.getDomainAxis();
    va.setLabelPaint(COLOR_DOM_HIGHLIGHT);
    va.setTickLabelPaint(COLOR_DOM_HIGHLIGHT);

    va = plot.getRangeAxis();
    va.setLabelPaint(COLOR_RANGE_HIGHLIGHT);
    va.setTickLabelPaint(COLOR_RANGE_HIGHLIGHT);

    plot.setBackgroundPaint(Color.WHITE);
    plot.setDomainGridlinePaint(Color.GRAY);
    plot.setRangeGridlinePaint(Color.GRAY);
    chart.setBackgroundPaint(COLOR_BACKGROUND);
    chart.setBorderPaint(COLOR_MOUSE_ON);

    plot.setOutlineStroke(new BasicStroke(1.0f));
    plot.setOutlinePaint(COLOR_MOUSE_ON);

    BasicStroke s = (BasicStroke) plot.getDomainGridlineStroke();
    BasicStroke stroke = new BasicStroke(1.0f, s.getEndCap(),
        BasicStroke.JOIN_BEVEL, 100.0f, s.getDashArray(), s.getDashPhase());

    Double x = getLastADCValue();
    plot.setDomainCrosshairValue(x, true);
    plot.setDomainCrosshairStroke(stroke);
    plot.setDomainCrosshairVisible(true);
    plot.setDomainCrosshairLockedOnData(true);
    plot.setDomainCrosshairPaint(COLOR_DOM_HIGHLIGHT);

    Double y = function.f(x);
    plot.setRangeCrosshairValue(y, true);
    plot.setRangeCrosshairStroke(stroke);
    plot.setRangeCrosshairVisible(true);
    plot.setRangeCrosshairLockedOnData(true);
    plot.setRangeCrosshairPaint(COLOR_RANGE_HIGHLIGHT);

    addValueMarker(x, plot);

    chart.addProgressListener(new ChartProgressListener() {
      public void chartProgress(ChartProgressEvent e) {
        if (e.getType() == ChartProgressEvent.DRAWING_FINISHED) {
          StyledDocument doc = crossHairTextPane.getStyledDocument();
          XYPlot plot = (XYPlot) e.getChart().getPlot();
          Double x = plot.getDomainCrosshairValue();
          Double y = plot.getRangeCrosshairValue();
          try {
            doc.remove(0, doc.getLength());
            String xValue = String.valueOf(x.intValue());
            String yValue = round(y, sensor.getRoundDigits());
            String a = "   f ( ";
            String b = " ) = ";
            String c = " " + sensor.getUnits();
            String statusText = a + xValue + b + yValue + c;
            doc.insertString(0, statusText, doc.getStyle("main"));
            doc.setCharacterAttributes(a.length(), xValue.length(),
                doc.getStyle("x"), false);
            doc.setCharacterAttributes(
                a.length() + xValue.length() + b.length(),
                yValue.length() + c.length(), doc.getStyle("y"), false);
          } catch (BadLocationException e1) {
            e1.printStackTrace();
          }
        }
      }
    });
    chartPanel = new ChartPanel(chart);
    updateChart();
  }

  private void addValueMarker(double x, XYPlot plot) {
    ValueMarker valueMarker = new ValueMarker(x);
    valueMarker.setPaint(COLOR_LAST_HIGHLIGHT);
    valueMarker.setLabel("Last received");
    valueMarker.setLabelOffsetType(LengthAdjustmentType.EXPAND);
    valueMarker.setStroke(new BasicStroke(0.5f));
    valueMarker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
    valueMarker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
    plot.addDomainMarker(valueMarker, Layer.FOREGROUND);
  }

  private void setupSlider() {
    slider = new JSlider(0, 1000, 500);
    slider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        int pos = ((JSlider) e.getSource()).getValue();
        XYPlot plot = (XYPlot) chart.getPlot();
        ValueAxis domainAxis = plot.getDomainAxis();
        Range rangeAxis = domainAxis.getRange();
        Double d = domainAxis.getLowerBound() + ((double) pos / 1000D)
            * rangeAxis.getLength();
        plot.setDomainCrosshairValue(d);
        plot.setRangeCrosshairValue(function.f(d));
      }
    });
  }

  private void setupIntervalPane() {
    intervalPane = new JTextPane();
    intervalPane.setEditable(false);
    intervalPane.setSelectionColor(null);
    intervalPane.setToolTipText(TOOL_TIP_CLICK_CHANGE);
    intervalPane.setBackground(COLOR_BACKGROUND);

    intervalPane.addMouseListener(new domainIntervalPaneMouseListener(
        intervalPane));
  }

  private void setupSpinner() {
    SpinnerModel model = new SpinnerNumberModel(DEF_INC_X, 1, 500, 1);
    spinner = new JSpinner(model);
    spinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (!fcFinished())
          return;
        int inc = (int) spinner.getValue();
        function.setStep(inc);
        updateChart();
      }
    });
  }

  private void setupAutoRangeCheckbox() {
    autoRangeCheckBox = new JCheckBox("Auto Range");
    autoRangeCheckBox.setToolTipText(TOOL_TIP_SAVE_CHECK);
    autoRangeCheckBox.setSelected(true);
    autoRange=true;
    autoRangeCheckBox
        .setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    autoRangeCheckBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent arg0) {
        autoRange = autoRangeCheckBox.isSelected();
        if (!fcFinished())
          return;
        updateChart();
      }
    });
  }

  private void setupVarsComboBox() {
    Object[] varNames = sensor.getVarsNames();
    varsComboBox = new JComboBox<Object>(varNames);
    varsComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectedVar = (String) varsComboBox.getSelectedItem();
        Double v = new Double(sensor.getValueOf(selectedVar));
        selectedVarField.setValue(v);
      }
    });
  }

  private void setupValueField() {
    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMaximumFractionDigits(20);
    selectedVarField = new JFormattedTextField(format);
    selectedVarField.addPropertyChangeListener("value",
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent e) {
            if (!fcFinished())
              return;
            double newValue = ((Number) selectedVarField.getValue())
                .doubleValue();
            sensor.setVar(selectedVar, newValue);
            if (saveChanges) {
              updateConfig(selectedVar, newValue);
            }
            updateChart();
          }
        });
    if (varsComboBox.getItemCount() > 0)
      varsComboBox.setSelectedIndex(0);
    else
      selectedVarField.setValue(Double.NaN);
  }

  public void updateConfig(String var, double newVal) {
    String key = "var," + sensor.nodeID + "," + sensorId + "," + "" + var;
    String value = config.getProperty(key);
    String newValue = String.valueOf(newVal);
    if (newValue.equals(value))
      return;
    config.put(key, newValue);
    convPanel.updateChanges(sensor);
  }

  private void setupSaveChangesCheckBox() {
    saveCheckBox = new JCheckBox("Save changes");
    saveCheckBox.setToolTipText(TOOL_TIP_SAVE_CHECK);
    saveCheckBox.setSelected(false);
    saveChanges = false;
    saveCheckBox.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    saveCheckBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent arg0) {
        saveChanges = saveCheckBox.isSelected();
      }
    });
  }

  private JPanel setupButtonPanel() {
    JPanel p = new JPanel();
    p.add(createButton("Show Formulas", TOOL_TIP_FORM_BT, sensorId,
        new ButtonFormulaAction()));
    p.add(createButton("Reset All", TOOL_TIP_RESET_BT, sensorId,
        new ButtonResetAction()));
    return p;
  }

  private class ButtonResetAction implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (!fcFinished())
        return;
      sensor.setConstants();
      if (saveChanges){
        Object[] vars = sensor.getVarsNames();
        for (int i = 0, n=vars.length; i < n; i++) {
          removeFromConfig((String) vars[i]);
        }
        convPanel.updateChanges(sensor);
      }
      updateChart();
      varsComboBox.setSelectedIndex(0);
    }
  }

  /**
   * This method is called when resetting default constants values, as default
   * values do not need to be stored (they are set on start up)
   */
  public void removeFromConfig(String var) {
    String key = "var," + sensor.nodeID + "," + sensorId + "," + "" + var;
    config.remove(key);
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

  private JLabel getConversionImage(String sensorName) {
    String firmName = sensorNode.getFirmName();
    ImageIcon imgIcon = new ImageIcon("./images/" + firmName + "-" + sensorName
        + "-" + "formula.jpeg");
    JLabel imgLabel = new JLabel();
    imgLabel.setIcon(imgIcon);
    return imgLabel;
  }

  private JButton createButton(String text, String tip, String name,
      ActionListener Listener) {
    JButton b = new JButton(text);
    b.setToolTipText(tip);
    b.setName(name);
    b.addActionListener(Listener);
    return b;
  }

  static String round(double d, int digits) {
    NumberFormat frm = NumberFormat.getInstance();
    frm.setMaximumFractionDigits(digits);
    frm.setRoundingMode(RoundingMode.UP);
    return frm.format(d);
  }

  public Double getLastADCValue() {
    if (sensorNode.getLastSD() == null)
      return Double.NaN;
    return sensorNode.getLastValueOf(sensorId);
  }

  public void addSensorData(SensorData sensorData) {
    sensorNode.addSensorData(sensorData);
    XYPlot plot = chart.getXYPlot();
    plot.clearDomainMarkers();
    double last=getLastADCValue();
    addValueMarker(last, plot);
    lastRawLabel.setText("Last Raw: " + String.valueOf(last));
  }

  public boolean isSensorDependent() {
    return (sensor.getAssociatedSensor() != null);
  }

  private class domainIntervalPaneMouseListener implements MouseListener {
    JTextPane pane;
    public static final int INTERVAL_WARNING_T = 100000;

    domainIntervalPaneMouseListener(JTextPane pane) {
      super();
      this.pane = pane;
    }

    public void mouseClicked(MouseEvent e) {

      if (!fcFinished())
        return;
      NumberFormat format = NumberFormat.getIntegerInstance();
      JFormattedTextField lowerField = new JFormattedTextField(format);
      JFormattedTextField upperField = new JFormattedTextField(format);
      lowerField.setColumns(6);
      int lowerValue = function.getMinX();
      int upperValue = function.getMaxX();
      lowerField.setValue(lowerValue);
      upperField.setColumns(6);
      upperField.setValue(upperValue);
      JPanel intervalPanel = new JPanel();
      intervalPanel.add(new JLabel("Lower value:"));
      intervalPanel.add(lowerField);
      intervalPanel.add(Box.createHorizontalStrut(10));
      intervalPanel.add(new JLabel("Upper value:"));
      intervalPanel.add(upperField);

      int result = JOptionPane.showConfirmDialog(pane, intervalPanel,
          INTERVAL_SELECTION_TITLE, JOptionPane.OK_CANCEL_OPTION);
      if (result == JOptionPane.OK_OPTION) {
        int newUpper = (int) ((Number) upperField.getValue()).doubleValue();
        int newLower = (int) ((Number) lowerField.getValue()).doubleValue();
        if (newLower >= newUpper || newLower <= 0 || newUpper <= 0)
          return;
        if (newUpper != upperValue)
          function.setMaxX(newUpper);
        if (newLower != lowerValue)
          function.setMinX(newLower);
        if (newUpper - newLower >= INTERVAL_WARNING_T) {
          Object[] options = { "Proceed", "Do not proceed"};
          int n = JOptionPane.showOptionDialog(pane,
              "Your interval is too big. This could lead to application problems.\n"
                  + "Make sure you have increased the function's step before proceeding",
              "Warning!", JOptionPane.YES_NO_CANCEL_OPTION,
              JOptionPane.WARNING_MESSAGE, null, options, options[1]);

          if ("Do not proceed".equals(options[n])) {
            return;
          }
        }
        updateChart();
        setDomainIntervalText(pane, newLower, newUpper);
      }
    }

    public void mouseEntered(MouseEvent e) {
      pane.setBorder(LineBorder.createGrayLineBorder());
      pane.setBackground(COLOR_MOUSE_ON);
    }

    public void mouseExited(MouseEvent e) {
      pane.setBorder(null);
      pane.setBackground(COLOR_BACKGROUND);
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }
  }

  public void setDomainIntervalText(JTextPane tf, int lower, int upper) {
    tf.setText(" [ " + String.valueOf(lower) + ", " + String.valueOf(upper)
        + " ] ");
  }

  /**
   * PRIVATE CLASS Function
   */
  private abstract class Function {
    protected Sensor sensor;
    private String xLabel = "Raw value";
    private String yLabel;
    private int minX = DEF_MIN_X;
    private int maxX;
    private int step = DEF_INC_X;
    private int DEF_MAX_X = 7000;

    public Function(Sensor sensor) {
      this.sensor = sensor;
      maxX = sensor.getADCResolution();
      if (maxX <= 0)
        maxX = DEF_MAX_X;

      yLabel = sensor.getUnits();
      sensorId = sensor.getId();
    }

    public Function(Function f) {
      sensor = f.sensor.Clone();
      maxX = f.getMaxX();
      minX = f.getMinX();
      step = f.getStep();
      xLabel = f.getxTag();
      yLabel = f.getyTag();
      sensorId = sensor.getId();
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

    public void setStep(int increment) {
      this.step = increment;
    }

    public int getStep() {
      return step;
    }

    public String getxTag() {
      return xLabel;
    }

    public String getyTag() {
      return yLabel;
    }
  }

  /**
   * PRIVATE CLASS functionGenerator
   */
  private class functionGenerator extends Thread {
    SensorAdjustPanel gui;
    boolean finished;
    XYSeries series;
    Function function;

    functionGenerator(SensorAdjustPanel sac, XYSeries s, Function f) {
      gui = sac;
      finished = false;
      series = s;
      function = new Function(f) {
        protected double f(Double value) {
          return sensor.getConv(value);
        }
      };
    }

    void addSeries() {
      if (gui == null) {
        return;
      }
      gui.addSeriesToChart(series);
    }

    boolean Finished() {
      return finished;
    }

    public void run() {
      int min = function.getMinX();
      int max = function.getMaxX();
      int step = function.getStep();

      for (int i = min; i <= max; i += step) {
        series.add(i, function.f((double) i));
      }
      addSeries();
      finished = true;
    }
  }

  public boolean fcFinished() {
    if (fc == null)
      return true;
    if (!fc.Finished()) {
      return false;
    }
    return true;
  }

  public void updateChart() {
    XYSeries series = new XYSeries("");
    fc = new functionGenerator(this, series, function);
    fc.start();
  }

  // called by the functionGenerator Thread
  private void addSeriesToChart(XYSeries series) {
    dataset.removeAllSeries();
    dataset.addSeries(series);
    XYPlot plot = (XYPlot) chart.getPlot();
    plot.setRangeCrosshairValue(function.f(plot.getDomainCrosshairValue()));
    if (autoRange) {
      plot.getDomainAxis().setAutoRange(true);
      plot.getRangeAxis().setAutoRange(true);
    }
  }
}
