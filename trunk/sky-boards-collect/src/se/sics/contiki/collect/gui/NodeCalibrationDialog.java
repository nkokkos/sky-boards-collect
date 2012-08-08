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
import javax.swing.ImageIcon;
import javax.swing.JButton;
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

import se.sics.contiki.collect.Configurable;
import se.sics.contiki.collect.Node;
import se.sics.contiki.collect.NodeAR1000;
import se.sics.contiki.collect.NodeCM5000;
import se.sics.contiki.collect.NodeDS1000;
import se.sics.contiki.collect.NodeSensor;
import se.sics.contiki.collect.SensorData;
import se.sics.contiki.collect.SensorInfo;
import se.sics.contiki.collect.Variable;

public class NodeCalibrationDialog extends JFrame 
								   implements PropertyChangeListener,
								   Configurable{

	private static final long serialVersionUID = 1L;
	private final int DEF_MIN_X = 1;
	private final int DEF_MAX_X = 5000;
	private final int DEF_INC_X = 5;
	
	Node node;
    NodeSensor[] sensors;
    SensorData data;
    Vector<JPanel> charts;
    Vector<Function> functions;
    private Hashtable<String,JPanel> fieldsTable;
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
	JButton buttonAutoCal;


    /*do not use delim char in sensor/variable name */
    public static final char delim = '\n';

    public NodeCalibrationDialog(String title, final Node node,Properties config){
    	super(title);
    	fieldsTable = new Hashtable<String,JPanel>();
    	
    	if (node.getSensorsCount()==0)
    		return; // Sink node or unknown firmware
    	
    	calibrationConfig=config;
        charts=new Vector<JPanel>();
        functions=new Vector<Function>();
        this.node=node;
        sensors=node.getNodeSensors();
        tabbedPane = new JTabbedPane();
        String sensorName;

        for (int i=0;i<node.getSensorsCount();i++)
        {
        	sensorName=sensors[i].getName();
        	mainPanel=new JPanel();
        	mainPanel.setLayout(new FlowLayout());
        	tabbedPane.add(mainPanel);
        	tabbedPane.setTitleAt(i, sensorName+" sensor");
        	
    		varsPanel=new JPanel();
    		varsPanel.setLayout(new BorderLayout());
        	labelPaneVars = new JPanel(new GridLayout(0,1));
        	fieldPaneVars = new JPanel(new GridLayout(0,1));
         	fieldsTable.put(sensorName, fieldPaneVars);
        	
         	labelPaneVars.add(new JLabel("Last adc_value: "));
         	fieldPaneVars.add(new JLabel(getLastADCValue(sensorName)));

            insertVarPaneSeparator();
         	
        	labelPaneVars.add(new JLabel("Min. X: "));
        	labelPaneVars.add(new JLabel("Max. X: "));
        	labelPaneVars.add(new JLabel("Inc. X: "));
        	
        	createLimitTextFields(i, "minx",DEF_MIN_X);
        	createLimitTextFields(i, "maxx",DEF_MAX_X);
        	createLimitTextFields(i, "inc", DEF_INC_X);
        	
        	insertVarPaneSeparator();
            
        	Variable[] vars=sensors[i].getVars();
            
        	for (int j=0;j<vars.length;j++){	
        		// create labels
        		labelPaneVars.add(new JLabel(vars[j].getName()+": "));
        		// create fields
        		createVarsTextFields(i, j);
        	}
            
            chartPanel=createChart(sensors[i].getSensorId());
            charts.add(chartPanel);
            
            buttonReset=new JButton("Reset values");
            buttonReset.setName(sensorName);
            buttonReset.addActionListener(new ButtonResetAction());
            
            buttonFormula=new JButton("View conversion\n expressions");
            buttonFormula.setName(sensorName);
            buttonFormula.addActionListener(new ButtonFormulaAction());
            
            buttonAutoCal=new JButton("Auto Calibration");
            buttonFormula.setName(sensorName);
            buttonAutoCal.addActionListener(new ButtonAutoCalAction());
            buttonAutoCal.setToolTipText("Autocalibrate (TODO)");//TODO
            
            insertVarPaneSeparator(); 
            labelPaneVars.add(new JLabel(""));
            fieldPaneVars.add(buttonReset);
            labelPaneVars.add(new JLabel(""));
            fieldPaneVars.add(buttonFormula);
            labelPaneVars.add(new JLabel(""));
            fieldPaneVars.add(buttonAutoCal);
            
            varsPanel.add(labelPaneVars, BorderLayout.CENTER); 
            varsPanel.add(fieldPaneVars, BorderLayout.LINE_END);


            
            mainPanel.add(varsPanel); 
        	mainPanel.add(chartPanel);
        	//mainPanel.add(getConversionImage(sensors[i].getName()));

            //TODO Add option "compare to->(Node with same firmware)"      	
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

	String getLastADCValue(String sensor){
    	int SENSOR=node.keyConv(sensor);
    	SensorData sd=node.getLastSD();
    	int lastValue=sd.getValue(SENSOR);
    	return Integer.toString(lastValue);
    }
    
    void createLimitTextFields(int idx, String s, int value){
		NumberFormat format=NumberFormat.getIntegerInstance();
		JFormattedTextField valueField = new JFormattedTextField(format);
        valueField.setValue(value);
        valueField.setColumns(10);
        valueField.setName(sensors[idx].getName()+delim+s);
        valueField.addPropertyChangeListener("value",this);       
        fieldPaneVars.add(valueField);
    }
    
    void createVarsTextFields(int s, int v)
    {
    	Variable[] vars=sensors[s].getVars();
		NumberFormat format=NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(15);
		JFormattedTextField valueField = new JFormattedTextField(format);
        valueField.setValue(new Double(vars[v].getValue()));
        valueField.setColumns(10);
        valueField.setName(sensors[s].getName()+delim+vars[v].getName());
        valueField.addPropertyChangeListener("value",this);       
        fieldPaneVars.add(valueField);
    }

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		int update=-1;
		Object source = e.getSource();
		StringTokenizer tokens=new StringTokenizer
		(((JFormattedTextField) source).getName(),delim+"");
		String sensorStr=tokens.nextToken();
		String varStr=tokens.nextToken();
		Variable var;
		double newValue=((Number)
			((JFormattedTextField) source).getValue()).doubleValue();
		
		
		NodeSensor sensor=node.getNodeSensor(sensorStr);
		if ((var=sensor.getVar(varStr))!=null)
			if (var.getValue()==newValue) return;

		   
		for (int i = 0; i < sensors.length;i++)
			if (sensors[i].getName().equals(sensorStr)){
				if (varStr.equals("minx")) 
					functions.get(i).setMinX((int)newValue);
				else if (varStr.equals("maxx")) 
					functions.get(i).setMaxX((int)newValue);
				else if (varStr.equals("inc")) 
					functions.get(i).setIncrement((int)newValue);
				else {
					sensors[i].setVar(varStr, newValue);
					updateConfig(sensors[i].getName(),
							varStr,newValue);
				}
				update=i;
			}
				   	   	
		if (update>=0)
			updateChart(update);
	}
	
	private void updateChart(int chartIndex){
		JPanel chartPanel=charts.get(chartIndex);
		chartPanel.removeAll();
		chartPanel.add(paintChart(functions.get(chartIndex)));
		chartPanel.updateUI();
	}

	private ChartPanel createChart(int sensorId)
	{
		if (node.getSensorDataCount()==0) return null;
		data=node.getSensorData(node.getSensorDataCount()-1);
		Function conv=createFunction(sensorId);
		functions.add(conv);
		return paintChart(conv);
	}
	
	
	private ChartPanel paintChart(Function conv){
        // Create a simple XY chart
        XYSeries series = new XYSeries("Conversion function");
        for (int i = conv.getMinX(); i <= conv.getMaxX(); i+=conv.getIncrement())
        {
            series.add(i,conv.f(i));
        }
        

        // Add the series to your data set
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        // Generate the graph
        chart = ChartFactory.createXYLineChart(
            "CONV (adc_value)",        // Title
            conv.getxTag(),            // x-axis Label
            conv.getyTag(),            // y-axis Label
            dataset,                   // Dataset
            PlotOrientation.VERTICAL,  // Plot Orientation
            true,                      // Show Legend
            true,                      // Use tooltips
            false                      // Configure chart to generate URLs?
            );
        return (chartPanel = new ChartPanel(chart));
	}
	
	private Function createFunction(int sensorId)
	{
		
		switch(data.getType()){
		case SensorInfo.CM5000:		
			return createCM5000F(sensorId);
			
		case SensorInfo.AR1000:
			return createAR1000F(sensorId);
			
		case SensorInfo.DS1000:
			return createDS1000F(sensorId);
			
		case SensorInfo.EM1000:break; //TODO
		case SensorInfo.EX1000:break; //TODO
		}
		return null;
	}
	
	private Function createCM5000F(int sensorId)
	{
		Function conv = null;
		data=new SensorData(data.getNode());
		switch (sensorId){
			case NodeCM5000.LIGHT1:
				conv=new Function(data,DEF_MIN_X,DEF_MAX_X,DEF_INC_X,"lx"){
					protected double f(int value)
					{
						return data.getLight1(value);
					}
				};break;
			case NodeCM5000.LIGHT2: 
				conv=new Function(data,DEF_MIN_X,DEF_MAX_X,DEF_INC_X, "lx"){
					protected double f(int value)
					{
						return data.getLight2(value);
					}
				};break;
			case NodeCM5000.TEMPERATURE:
				conv=new Function(data,DEF_MIN_X,DEF_MAX_X,DEF_INC_X, "Celsius"){
					protected double f(int value)
					{
						return data.getTemperatureCM5000(value);
					}
				};break;
			case NodeCM5000.HUMIDITY:
				conv=new Function(data,DEF_MIN_X,DEF_MAX_X,DEF_INC_X,"Humidity %"){
					protected double f(int value)
					{
						return data.getHumidity(value);
					}
				};break;		
			}
		return conv;
	}
	
	private Function createAR1000F(int sensorId)
	{
		Function conv=null;
		data=new SensorData(data.getNode());
		switch(sensorId){
			case NodeAR1000.CO:
				conv=new Function(data,DEF_MIN_X,DEF_MAX_X,DEF_INC_X,"ppm"){
					protected double f(int value)
					{
						return data.getCO(value);
					}
				};break;
			case NodeAR1000.CO2:
				conv=new Function(data,DEF_MIN_X,DEF_MAX_X,DEF_INC_X,"ppm"){
					protected double f(int value)
					{
						return data.getCO2(value);
					}
				};break;
			case NodeAR1000.DUST:
				conv=new Function(data,DEF_MIN_X,DEF_MAX_X,DEF_INC_X,"mg/m^3"){
					protected double f(int value)
					{
						return data.getDust(value);
					}
				};break;
			}
		return conv;
	}
	
	private Function createDS1000F(int sensorId){
		Function conv=null;
		data=new SensorData(data.getNode());
		switch(sensorId){
			case NodeDS1000.CO:
				conv=new Function(data,DEF_MIN_X,DEF_MAX_X,DEF_INC_X,"ppm"){
					protected double f(int value)
					{
						return data.getCO(value);
					}
				};break;
			case NodeDS1000.CO2:
				conv=new Function(data,DEF_MIN_X,DEF_MAX_X,DEF_INC_X,"ppm"){
					protected double f(int value)
					{
						return data.getCO2(value);
					}
				};break;
			case NodeDS1000.TEMPERATURE:
				conv=new Function(data,DEF_MIN_X,DEF_MAX_X,DEF_INC_X,"Celsius"){
					protected double f(int value)
					{
						return data.getTemperatureDS1000(value);
					}
				};break;
			}
		return conv;
	}
	
	private JLabel getConversionImage(String sensorName){
		
		String firmName="";
		if (node.getSensorDataCount()==0) return null;
		data=node.getSensorData(node.getSensorDataCount()-1);
		
		switch(data.getType())
		{
			case SensorInfo.CM5000:
				firmName="CM5000"; break;
			case SensorInfo.AR1000:
				firmName="AR1000"; break;
			case SensorInfo.DS1000:
				firmName="DS1000"; break;
			case SensorInfo.EM1000:break; //TODO
			case SensorInfo.EX1000:break; //TODO
		}
		
        ImageIcon imgIcon = new ImageIcon("./images/"+
        		firmName+"-"+sensorName+"-"+"formula.jpeg");
        JLabel imgLabel =new JLabel();
        imgLabel.setIcon(imgIcon);
        return imgLabel;
	}
	
	
	private abstract class Function
	{
		SensorData data;
		private String xLabel="adc_value";
		private String yLabel;
	    private int minX=1;
	    private int maxX=10000;
	    private int increment=5;
		Function(SensorData data, int min, int max, int inc, String units)
		{
			this.data=data;
			setMinX(min);
			setMaxX(max);
			setIncrement(inc);
			this.yLabel=units;
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

		public String getyTag() {
			return yLabel;
		}
	}
	
	private class ButtonResetAction 
	implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			String sensor=((Component) e.getSource()).getName();
			
			int t=node.getLastSD().getType();
			switch(t){
			case SensorInfo.CM5000: 
				((NodeCM5000) node).setDefaultValues(sensor);
				break;
			case SensorInfo.DS1000: 
				((NodeDS1000) node).setDefaultValues(sensor);	
				break;
			case SensorInfo.AR1000: 
				((NodeAR1000) node).setDefaultValues(sensor);
				break;
			}
			JPanel fieldPaneVars=fieldsTable.get(sensor);
			Component[] cArray=fieldPaneVars.getComponents();
			for (int i=0;i<cArray.length;i++){
				reset(cArray[i]);
			}
			for (int i = 0; i < sensors.length;i++)
				if (sensors[i].getName().equals(sensor))
					updateChart(i);
				
		}
		
		public void reset(Component field)
		{
			if (!(field instanceof JFormattedTextField)) return;
			String fieldName=field.getName();
			if (fieldName=="" || fieldName==null) return;
			StringTokenizer tokens=
				new StringTokenizer(field.getName(),delim+"");
			String sensorStr=tokens.nextToken();
			String varStr=tokens.nextToken();
			Variable var;
			
			NodeSensor sensor=node.getNodeSensor(sensorStr);
			if ((var=sensor.getVar(varStr))!=null){
				((JFormattedTextField) field).setValue(var.getValue());
				updateConfig(sensorStr,varStr,var.getValue());
			}
		}
	}
	
	public class ButtonFormulaAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String sensor=((Component) e.getSource()).getName();
			JFrame imgFrame=new JFrame();
			imgFrame.add(getConversionImage(sensor));
			imgFrame.pack();
			imgFrame.setVisible(true);
		}

	}

	public class ButtonAutoCalAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub

		}

	}


	public void updateConfig(String sensor, String var, double newVal) {
		calibrationConfig.put("var,"+node.getID()+","+sensor+"," +
		""+var, String.valueOf(newVal));	
	}

	@Override
	public void updateConfig(Properties config) {
		// TODO Instead of updateConfig(String sensor, String var, double newVal)
		// Do the same as in DataFeederCosm
	}
}
