/*
 * Copyright (c) 2008, Swedish Institute of Computer Science.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 *
 * -----------------------------------------------------------------
 *
 * SensorData
 *
 * Authors : Joakim Eriksson, Niclas Finne
 * Created : 3 jul 2008
 *
 * Modified by Eloy Díaz, 30 jul 2012
 */

package se.sics.contiki.collect;
import java.util.Arrays;

/**
 *
 */
public class SensorData implements SensorInfo {

  private final Node node;
  private final int[] values;
  private final long nodeTime;
  private final long systemTime;
  private int seqno;
  private boolean isDuplicate;

  public SensorData(Node node, int[] values, long systemTime) {
    this.node = node;
    this.values = values;
    this.nodeTime = ((values[TIMESTAMP1] << 16) + values[TIMESTAMP2]) * 1000L;
    this.systemTime = systemTime;
    this.seqno = values[SEQNO];
  }
  
  public SensorData(Node node) {
	  this.node = node;
	  values = null;
	  nodeTime=0;
	  systemTime=0;
  }

  public Node getNode() {
    return node;
  }

  public String getNodeID() {
    return node.getID();
  }

  public boolean isDuplicate() {
    return isDuplicate;
  }

  public void setDuplicate(boolean isDuplicate) {
    this.isDuplicate = isDuplicate;
  }

  public int getSeqno() {
    return seqno;
  }

  public void setSeqno(int seqno) {
    this.seqno = seqno;
  }

  public int getValue(int index) {
    return values[index];
  }

  public int getValueCount() {
    return values.length;
  }

  public long getNodeTime() {
    return nodeTime;
  }

  public long getSystemTime() {
    return systemTime;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (systemTime > 0L) {
      sb.append(systemTime).append(' ');
    }
    for (int i = 0, n = values.length; i < n; i++) {
      if (i > 0) sb.append(' ');
      sb.append(values[i]);
    }
    return sb.toString();
  }

  public static SensorData parseSensorData(CollectServer server, String line) {
    return parseSensorData(server, line, 0);
  }

  public static SensorData parseSensorData(CollectServer server, String line, long systemTime) {
    String[] components = line.trim().split("[ \t]+");
    // Check if COOJA log
    if (components.length == VALUES_COUNT + 2 && components[1].startsWith("ID:")) {
      if (!components[2].equals("" + VALUES_COUNT)) {
        // Ignore non sensor data
        return null;
      }
      try {
        systemTime = Long.parseLong(components[0]);
        components = Arrays.copyOfRange(components, 2, components.length);
      } catch (NumberFormatException e) {
        // First column does not seem to be system time
      }

    } else if (components[0].length() > 8) {
      // Sensor data prefixed with system time
      try {
        systemTime = Long.parseLong(components[0]);
        components = Arrays.copyOfRange(components, 1, components.length);
      } catch (NumberFormatException e) {
        // First column does not seem to be system time
      }
    }
    if (components.length != SensorData.VALUES_COUNT) {
      return null;
    }
    // Sensor data line (probably)
    int[] data = parseToInt(components);
    if (data == null || data[0] != VALUES_COUNT) {
      System.err.println("Failed to parse data line: '" + line + "'");
      return null;
    }
    String nodeID = mapNodeID(data[NODE_ID]);
    Node node = server.addNode(nodeID,data[SENSOR_BOARD]);
    return new SensorData(node, data, systemTime);
  }

  public static String mapNodeID(int nodeID) {
    return "" + (nodeID & 0xff) + '.' + ((nodeID >> 8) & 0xff);
  }

  private static int[] parseToInt(String[] text) {
    try {
      int[] data = new int[text.length];
      for (int i = 0, n = data.length; i < n; i++) {
        data[i] = Integer.parseInt(text[i]);
      }
      return data;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public double getCPUPower() {
    return (values[TIME_CPU] * POWER_CPU) / (values[TIME_CPU] + values[TIME_LPM]);
  }

  public double getLPMPower() {
    return (values[TIME_LPM] * POWER_LPM) / (values[TIME_CPU] + values[TIME_LPM]);
  }

  public double getListenPower() {
    return (values[TIME_LISTEN] * POWER_LISTEN) / (values[TIME_CPU] + values[TIME_LPM]);
  }

  public double getTransmitPower() {
    return (values[TIME_TRANSMIT] * POWER_TRANSMIT) / (values[TIME_CPU] + values[TIME_LPM]);
  }

  public double getAveragePower() {
    return (values[TIME_CPU] * POWER_CPU + values[TIME_LPM] * POWER_LPM
    + values[TIME_LISTEN] * POWER_LISTEN + values[TIME_TRANSMIT] * POWER_TRANSMIT)
    / (values[TIME_CPU] + values[TIME_LPM]);
  }

  public long getPowerMeasureTime() {
    return (1000L * (values[TIME_CPU] + values[TIME_LPM])) / TICKS_PER_SECOND;
  }

  public double getRadioIntensity() {
    return values[RSSI];
  }

  public double getLatency() {
    return values[LATENCY] / 32678.0;
  }


  public String getBestNeighborID() {
    return values[BEST_NEIGHBOR] > 0 ? mapNodeID(values[BEST_NEIGHBOR]): null;
  }

  public double getBestNeighborETX() {
    return values[BEST_NEIGHBOR_ETX] / 8.0;
  }
  
  public double getBatteryVoltage() {
    return values[BATTERY_VOLTAGE] * 2 * 2.5 / 4096.0;
  }

  public double getBatteryIndicator() {
    return values[BATTERY_INDICATOR];
  }  
  
  public int getType()
  {
	  return values[SENSOR_BOARD];
  }
  
  public double getHumidity() {
		/* 
		 * Conversion based on this info:
		 * 
		 * http://www.advanticsys.com/wiki/index.php?title=Sensirion%C2%AE_SHT11
		 * 
		 * 12 bit values:
		 * c1=-2.0468 	c2=0.0367 	c3=-1.5955E-6
		 * t1=0.01 	t2=0.00008
		 * 
		 * RHlinear = c1 + c2 · SORH + c3 · SORH^2
		 * RHtrue = (T°C - 25) ·(t1 + t2 · SORH) + RHlinear
		 */
		NodeSensor humSensor=node.getNodeSensor(HUMIDITY);
		double c1=humSensor.getVar("c1").getValue();
		double c2=humSensor.getVar("c2").getValue();
		double c3=humSensor.getVar("c3").getValue();
		double t1=humSensor.getVar("t1").getValue();
		double t2=humSensor.getVar("t2").getValue();
		int adc_val=values[HUMIDITY];
		 
		double RHlinear=c1+c2*adc_val+c3*(adc_val*adc_val);
		// Temperature compensation:
		double v=(this.getTemperatureCM5000()-25)*(t1+t2*adc_val)+RHlinear;
		if(v > 100) {
			return 100;
		}
		return v;
	}
	public double getHumidity(int value) {
		NodeSensor humSensor=node.getNodeSensor(HUMIDITY);
		double c1=humSensor.getVar("c1").getValue();
		double c2=humSensor.getVar("c2").getValue();
		double c3=humSensor.getVar("c3").getValue();
		double t1=humSensor.getVar("t1").getValue();
		double t2=humSensor.getVar("t2").getValue();
		double temp=humSensor.getVar("T").getValue();
		int adc_val=value;
		 
		double RHlinear=c1+c2*adc_val+c3*(adc_val*adc_val);
		// Temperature compensation:
		double v=(temp-25)*(t1+t2*adc_val)+RHlinear;
		if(v > 100) {
			return 100;
		}
		return v;
	}

	public double getLight1() {
		NodeSensor lightSensor=node.getNodeSensor(LIGHT1);
		double vRef=lightSensor.getVar("Vref").getValue();
		double v11=lightSensor.getVar("v11").getValue();
		double R11=lightSensor.getVar("R11").getValue();
		double Vs=((double)values[LIGHT2]/(double)4096)*vRef;
		return (v11*1000000*(Vs/R11)*1000);
	}
	public double getLight1(int value) {

		NodeSensor lightSensor=node.getNodeSensor(LIGHT1);
		double vRef=lightSensor.getVar("Vref").getValue();
		double v11=lightSensor.getVar("v11").getValue();
		double R11=lightSensor.getVar("R11").getValue();
		double Vs=((double)value/(double)4096)*vRef;
		return (v11*1000000*(Vs/R11)*1000);
	}

	public double getLight2() {
		NodeSensor lightSensor=node.getNodeSensor(LIGHT2);
		double vRef=lightSensor.getVar("Vref").getValue();
		double v12=lightSensor.getVar("v12").getValue();
		double R12=lightSensor.getVar("R12").getValue();
		double Vs=((double)values[LIGHT2]/(double)4096)*vRef;
		return (v12*100000*(Vs/R12)*1000);
	}
	public double getLight2(int value) {

		NodeSensor lightSensor=node.getNodeSensor(LIGHT2);
		double vRef=lightSensor.getVar("Vref").getValue();
		double v12=lightSensor.getVar("v12").getValue();
		double R12=lightSensor.getVar("R12").getValue();
		double Vs=((double)value/(double)4096)*vRef;
		return (v12*100000*(Vs/R12)*1000);
	}

	

	public double getCO() {
		NodeSensor COSensor=node.getNodeSensor(CO);
		
		double vRef=COSensor.getVar("Vref").getValue();
		double RL=COSensor.getVar("RL").getValue();
		double Vcc=COSensor.getVar("Vcc").getValue();
		double R0=COSensor.getVar("R0").getValue();
		double case2_v1=COSensor.getVar("case2_v1").getValue();
		double case2_v2=COSensor.getVar("case2_v2").getValue();
		double case3_v1=COSensor.getVar("case3_v1").getValue();
		double case3_v2=COSensor.getVar("case3_v2").getValue();
		  
		double Vs=((double)values[CO]/(double)4096)*vRef;
		double sensitivity=((Vcc/Vs)-1)*RL/R0;;
		 	  
		if (sensitivity>0.8) return 10.0;
		if (0.8<sensitivity || sensitivity>0.5) 
			return (double)((-case2_v1*sensitivity)+case2_v2);
		return (double)((-case3_v1*sensitivity)+case3_v2); //sensitivity<=0,5
	}
	public double getCO(int value)
	{
		NodeSensor COSensor=node.getNodeSensor(CO);
		
		double vRef=COSensor.getVar("Vref").getValue();
		double RL=COSensor.getVar("RL").getValue();
		double Vcc=COSensor.getVar("Vcc").getValue();
		double R0=COSensor.getVar("R0").getValue();
		double case2_v1=COSensor.getVar("case2_v1").getValue();
		double case2_v2=COSensor.getVar("case2_v2").getValue();
		double case3_v1=COSensor.getVar("case3_v1").getValue();
		double case3_v2=COSensor.getVar("case3_v2").getValue();
		  
		double Vs=((double)value/(double)4096)*vRef;
		double sensitivity=((Vcc/Vs)-1)*RL/R0;;
		 	  
		if (sensitivity>0.8) return 10.0;
		if (0.8<sensitivity || sensitivity>0.5) 
			return (double)((-case2_v1*sensitivity)+case2_v2);
		return (double)((-case3_v1*sensitivity)+case3_v2); //sensitivity<=0,5
	}
	  
	public double getCO2() {
		NodeSensor CO2Sensor=node.getNodeSensor(CO2);
		double vRef=CO2Sensor.getVar("Vref").getValue();
		double v1=CO2Sensor.getVar("v1").getValue();
		double v2=CO2Sensor.getVar("v2").getValue();
		
		double Vs=((double)values[CO2]/(double)4096)*vRef;
		return (Vs*v1)-v2;
	}
	
	public double getCO2(int value)
	{
		NodeSensor CO2Sensor=node.getNodeSensor(CO2);
		double vRef=CO2Sensor.getVar("Vref").getValue();
		double v1=CO2Sensor.getVar("v1").getValue();
		double v2=CO2Sensor.getVar("v2").getValue();
		
		double Vs=((double)value/(double)4096)*vRef;
		return (Vs*v1)-v2;
	}
	  
	public double getDust() {
		NodeSensor DustSensor=node.getNodeSensor(DUST);
		double vRef=DustSensor.getVar("Vref").getValue();
		double v1=DustSensor.getVar("v1").getValue();
		double v2=DustSensor.getVar("v2").getValue();
		double Vs=((double)values[DUST]/(double)4096)*vRef;
		
		return (Vs*v1)+v2;
	}
	
	public double getDust(int value)
	{
		NodeSensor DustSensor=node.getNodeSensor(DUST);
		double vRef=DustSensor.getVar("Vref").getValue();
		double v1=DustSensor.getVar("v1").getValue();
		double v2=DustSensor.getVar("v2").getValue();
		double Vs=((double)value/(double)4096)*vRef;
		
		return (Vs*v1)+v2;
	}

	public double getTemperatureDS1000() {
		NodeSensor tempSensor=node.getNodeSensor(TEMPERATURE);
		double vRef=tempSensor.getVar("Vref").getValue();
		double Vcc=tempSensor.getVar("Vcc").getValue();
		double RT=tempSensor.getVar("RT").getValue();
		double T=tempSensor.getVar("T").getValue();
		double beta=tempSensor.getVar("beta").getValue();
		double K=272.15;
		//Thermistor resistance R0
		double Vs=((double)values[TEMPERATURE]/(double)4096)*vRef;
		double R0= ( (Vcc-Vs) * RT )/Vs;
		double ln_param=(double)RT/(double)R0;
		return ((-beta/(Math.log(ln_param)-(beta/T))))-K; 
	}
	
	public double getTemperatureDS1000(int value) {
		NodeSensor tempSensor=node.getNodeSensor(TEMPERATURE);
		double vRef=tempSensor.getVar("Vref").getValue();
		double Vcc=tempSensor.getVar("Vcc").getValue();
		double RT=tempSensor.getVar("RT").getValue();
		double T=tempSensor.getVar("T").getValue();
		double beta=tempSensor.getVar("beta").getValue();
		double K=272.15;
		//Thermistor resistance R0
		double Vs=((double)value/(double)4096)*vRef;
		double R0= ( (Vcc-Vs) * RT )/Vs;
		double ln_param=(double)RT/(double)R0;
		return ((-beta/(Math.log(ln_param)-(beta/T))))-K; 
	}
	public double getTemperatureCM5000() {
		NodeSensor tempSensor=node.getNodeSensor(TEMPERATURE);
		double v1=tempSensor.getVar("v1").getValue();
		double v2=tempSensor.getVar("v2").getValue();

		return -v1 + v2 * values[TEMPERATURE];

	}
	public double getTemperatureCM5000(int value) {
		NodeSensor tempSensor=node.getNodeSensor(TEMPERATURE);
		double v1=tempSensor.getVar("v1").getValue();
		double v2=tempSensor.getVar("v2").getValue();

		return -v1 + v2 * value;
	}

}
