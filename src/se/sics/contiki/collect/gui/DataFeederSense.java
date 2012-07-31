/*
 * DataFeederOpenSense
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 28 jul 2012
 */

package se.sics.contiki.collect.gui;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import se.sics.contiki.collect.Configurable;
import se.sics.contiki.collect.Node;
import se.sics.contiki.collect.NodeSensor;
import se.sics.contiki.collect.PublisherSense;
import se.sics.contiki.collect.SensorData;
import se.sics.contiki.collect.SensorInfo;
import se.sics.contiki.collect.Visualizer;

public class DataFeederSense extends JPanel implements Visualizer, SensorInfo, Configurable{
	
	private static final long serialVersionUID = 1L;
	String category;
	JPasswordField keyField;
	JTextField responseField; // TODO
	JComboBox comboBoxNode;
	JComboBox comboBoxSensor;
	JComboBox comboBoxRaw;
	JButton startButton;
	JButton stopButton;
	JButton setButton;
	JButton removeButton;
	boolean doFeed=false;
	boolean feedRaw=true;
	private JPanel panel;
	String feedingNode;
	String feedingSensor;
	JPanel fieldPaneVars;
	Properties config;
	JTextField field;
	JLabel statusLabel;
	PublisherSense publisher;
	
	private Hashtable<String,Node> nodes = new Hashtable<String,Node>();
	
	public DataFeederSense(String category, Properties config){
		this.config=config;
		panel = new JPanel(new BorderLayout());
		this.category=category;
			
    	keyField=new JPasswordField();
    	keyField.setColumns(30);

		comboBoxNode = new JComboBox();
		comboBoxNode.setModel(new DefaultComboBoxModel());
		comboBoxNode.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (comboBoxNode.getItemCount()==0) return;
				int idx=comboBoxNode.getSelectedIndex();
				feedingNode=comboBoxNode.getItemAt(idx).toString();
				Node n=nodes.get(feedingNode);
				NodeSensor[] sensors=n.getNodeSensors();
				Vector<String> sensorNames=new Vector<String>();
				for (int i=0;i<sensors.length;i++)
					sensorNames.add(sensors[i].getName());
				comboBoxSensor.setModel(new DefaultComboBoxModel(
						sensorNames.toArray()));
				feedingSensor=comboBoxSensor.getItemAt(0).toString();
				loadFeedIDvalue();			
			}});
		
		comboBoxSensor = new JComboBox();
		comboBoxSensor.setModel(new DefaultComboBoxModel());
		comboBoxSensor.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (comboBoxNode.getItemCount()==0) return;
				int idx=comboBoxSensor.getSelectedIndex();
				feedingSensor=comboBoxSensor.getItemAt(idx).toString();
				loadFeedIDvalue();
			}});
		
		String[] opt= {"Raw","Converted"};
		comboBoxRaw = new JComboBox();
		comboBoxRaw.setModel(new DefaultComboBoxModel(opt));
		comboBoxRaw.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int idx=comboBoxRaw.getSelectedIndex();
				String how=comboBoxRaw.getItemAt(idx).toString();
				if (how.equals("Raw")) feedRaw=true;
				else if (how.equals("Converted")) feedRaw=false;
			}});
			
    	responseField=new JTextField();
    	responseField.setEditable(false);
    	
    	startButton=new JButton("Start Feeding");
    	startButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				doFeed=true;
				statusLabel.setText("Status: Feeding");
			}});
    	
    	stopButton=new JButton("Stop Feeding");
    	stopButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				doFeed=false;
				statusLabel.setText("Status: Not feeding");
			}});
    	
    	setButton=new JButton("Set");
    	setButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				storeFeedIDvalue();
			}});
    	
    	removeButton=new JButton("Remove");
    	removeButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				removeFeedIDvalue();
			}});
    	
    	field = new JTextField();
    	field.setText(null);
    	field.setColumns(11);

    	statusLabel=new JLabel("Status: Not feeding");
    		
    	JPanel controlPanel = new JPanel(new GridLayout(0,1));
    	controlPanel.add(new JLabel(""));
    	
    	JPanel keyPanel= new JPanel();
    	keyPanel.add(new JLabel("sen.se API Key:"));
    	keyPanel.add(keyField);
        controlPanel.add(keyPanel);

        controlPanel.add(new JLabel(""));
        controlPanel.add(new JSeparator());

        JPanel feedPanel= new JPanel();
        feedPanel.add(new JLabel("Node:"));
        feedPanel.add(comboBoxNode);
        feedPanel.add(new JLabel("Sensor:"));
        feedPanel.add(comboBoxSensor);
        feedPanel.add(new JLabel("Feed ID:"));
        feedPanel.add(field);
        JPanel feedControl = new JPanel();
        feedControl.add(removeButton);
        feedControl.add(setButton);
          
        controlPanel.add(feedPanel);
        controlPanel.add(feedControl);
        controlPanel.add(new JLabel(""));
        controlPanel.add(new JSeparator());       
        
        JPanel startPanel=new JPanel();
        startPanel.add(new JLabel("Send "));
        startPanel.add(comboBoxRaw);
        startPanel.add(new JLabel("values.  "));
        startPanel.add(startButton);
        startPanel.add(stopButton);
        controlPanel.add(startPanel);
        
        JPanel statusPanel=new JPanel(); 
        statusPanel.add(statusLabel);
        
        controlPanel.add(statusPanel);
        controlPanel.add(new JLabel(""));
        controlPanel.add(new JSeparator());

        panel.add(controlPanel, BorderLayout.NORTH);
	}

	@Override
	public void clearNodeData() {
		if (!isVisible()) return;
		nodes.clear();
		comboBoxNode.removeAllItems();	
		comboBoxSensor.removeAllItems();
		field.setText(null);
	}

	public String getCategory() {
	    return category;
	  }

	  public String getTitle() {
	    return "sen.se Feeder";
	  }

	  public Component getPanel() {
	    return panel;
	  }

	@Override
	public void nodeAdded(Node node) {
		if (!isVisible()) return;
		SensorData sd=node.getLastSD();
		if (sd==null) return; // unknown node type
		
		nodes.put(node.getID(), node);
		comboBoxNode.setModel(new DefaultComboBoxModel(nodes.keySet().toArray()));
	}

	@Override
	public void nodeDataReceived(SensorData sensorData) {
		if (!isVisible()) return;
		Hashtable<String,String> feedTable = new Hashtable<String,String>();
		if (nodes.get(sensorData.getNode())==null){
			nodeAdded(sensorData.getNode());
		}
		
		if (doFeed){
			Node n=sensorData.getNode();
			int t=n.getLastSD().getType();
			switch(t){
			case CM5000:			
				fillFeedTableCM5000(n,sensorData,feedTable);
				break;
			case AR1000:
				fillFeedTableAR1000(n,sensorData,feedTable);
				break;
			case DS1000:
				fillFeedTableDS1000(n,sensorData,feedTable);
				break;
			}
			
			String key=arrayToString(keyField.getPassword());
			PublisherSense publisher=new PublisherSense(feedTable,key);
			publisher.start();
		}	
	}
	
	void fillFeedTableCM5000(Node n,SensorData sd,
			Hashtable<String,String> feedTable){
		
		putValue(LIGHT1,n,sd,feedTable);
		putValue(LIGHT2,n,sd,feedTable);
		putValue(TEMPERATURE,n,sd,feedTable);
		putValue(HUMIDITY,n,sd,feedTable);
		
	}
	void fillFeedTableAR1000(Node n,SensorData sd,
			Hashtable<String,String> feedTable){
		
		putValue(CO,n,sd,feedTable);
		putValue(CO2,n,sd,feedTable);
		putValue(DUST,n,sd,feedTable);
		
	}
	void fillFeedTableDS1000(Node n,SensorData sd,
			Hashtable<String,String> feedTable){
		
		putValue(CO,n,sd,feedTable);
		putValue(CO2,n,sd,feedTable);
		putValue(TEMPERATURE,n,sd,feedTable);
	}
	
	private void putValue(int sensorId,Node n,SensorData sd,
			Hashtable<String,String> feedTable){
		
		String value;
		String id;
		NodeSensor s=n.getNodeSensor(sensorId);
		if ((id=s.getFeedID())!=null){
			if (feedRaw){
				value=Integer.toString(sd.getValue(sensorId));
			}
			else {
				value=round(getConverted(sensorId,sd));
			}
				
			feedTable.put(id, value);
		}
	}

	private double getConverted(int sensorId, SensorData sd){
		switch(sd.getType()){
			case CM5000: 
				switch(sensorId){
				case LIGHT1: return sd.getLight1();
				case LIGHT2: return sd.getLight2();
				case HUMIDITY: return sd.getHumidity();
				case TEMPERATURE: return sd.getTemperatureCM5000();
				}break;
			case DS1000: 				
				switch(sensorId){
				case CO: return sd.getCO();
				case CO2: return sd.getCO2();
				case TEMPERATURE: return sd.getTemperatureDS1000();
				}break;
			case AR1000:
				switch(sensorId){
				case CO: return sd.getCO();
				case CO2: return sd.getCO2();
				case DUST: return sd.getDust();
				}
		}
		return 0.0;
	}
    private String round(double d){
    	//TODO round digits selected by user
    	int digits=4;
    	
    	NumberFormat frm=NumberFormat.getInstance();
    	frm.setMaximumFractionDigits(digits);
    	frm.setRoundingMode(RoundingMode.UP);
    	return frm.format(d);
    }

	@Override
	public void nodesSelected(Node[] node) {
		// Ignore
	}
	
	public void loadFeedIDvalue(){
		Node n;
		NodeSensor s;
		if (feedingNode == null || feedingSensor==null) return;
		if ((n=nodes.get(feedingNode))==null) return;
		if ((s=n.getNodeSensor(feedingSensor))==null) return;
		
		field.setText(s.getFeedID());
	}
	
	public void storeFeedIDvalue(){
		Node n;
		NodeSensor s;
		if (feedingNode == null || feedingSensor==null) return;
		if ((n=nodes.get(feedingNode))==null) return;
		if ((s=n.getNodeSensor(feedingSensor))==null) return;
		
		String id=field.getText();
		if (isValidFeedID(id))
			s.setFeedID(field.getText());	
		else field.setText("Invalid feed ID");
	}
	
	public void removeFeedIDvalue(){
		Node n;
		NodeSensor s;
		if (feedingNode == null || feedingSensor==null) return;
		if ((n=nodes.get(feedingNode))==null) return;
		if ((s=n.getNodeSensor(feedingSensor))==null) return;
		
		s.setFeedID(null);
		field.setText(null);
	}
	
	public static boolean isValidFeedID(String id){
		if (id==null || id.equals("") || !isInteger(id))
			return false;
		
		return true;	
	}
	
	public static boolean isInteger(String str) {
        try {
        	Integer.parseInt(str);
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
	}
	
	public static String arrayToString(char[] a) {
	    StringBuffer result = new StringBuffer();
	    if (a.length > 0) {
	        result.append(a[0]);
	        for (int i=1; i<a.length; i++) {
	            result.append(a[i]);
	        }
	    }
	    return result.toString();
	}
	
	public void updateConfig(Properties config) {
		String id;
		for(Object key: nodes.keySet()) {
			Node n=nodes.get(key);
			NodeSensor[] sensors=n.getNodeSensors();
			for (int i=0;i<sensors.length;i++){
				if ((id=sensors[i].getFeedID())!=null){
					config.setProperty("feedsense,"+n.getID()+","+
										sensors[i].getName(), id);
				}
				else config.remove("feedsense,"+n.getID()+","+
										sensors[i].getName());					
			}
		}	
	}

}
