/*
 * CosmPublisher
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 30 jul 2012
 */

package se.sics.contiki.collect;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Hashtable;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

public class PublisherSense extends Thread {

	final static String SenseServer = "http://api.sen.se/events/";

	private String APIkey;
	Hashtable<String, String> feedTable;

	public PublisherSense(Hashtable<String, String> feedTable, String key) {
		APIkey = key;
		this.feedTable = feedTable;
	}

	public String getAPIkey() {
		return APIkey;
	}

	public void setAPIkey(String APIkey) {
		this.APIkey = APIkey;
	}

	public String constructMsgOpenSense(Hashtable<String, String> feedTable) {
		StringBuffer msg = new StringBuffer();
		char separator = ',';
		String value;
		for (Object key : feedTable.keySet()) {
			value = feedTable.get(key).replace(",", ".");// Set "." as decimal mark
			msg.append("{\"feed_id\" : " + key + ", " + "\"value\" : \"" + value
			    + "\"}" + separator);
		}
		// remove last separator
		String msgbody = msg.toString();
		msgbody = msgbody.substring(0, msgbody.length() - 1);
		return "[" + msgbody + "]";
	}

	protected void logLine(String line, boolean stderr, Throwable e) {
		if (stderr) {
			System.err.println("sen.se Publisher@ " + line);
		} else {
			System.out.println("sen.se Publisher@ " + line);
		}
		if (e != null) {
			e.printStackTrace();
		}
	}

	public void run() {
		if (feedTable.isEmpty())
			return; // nothing to feed

		URL url = null;
		try {
			url = new URL(SenseServer);
		} catch (MalformedURLException ex) {
			logLine("Bad URL", false, ex);
			return;
		}

		HttpURLConnection urlConn = null;
		try {
			// URL connection channel.
			urlConn = (HttpURLConnection) url.openConnection();
		} catch (IOException ex) {
			logLine("Unable to open connection", false, ex);
			return;
		}

		urlConn.setDoInput(true);
		urlConn.setDoOutput(true);
		urlConn.setUseCaches(false);

		try {
			urlConn.setRequestMethod("POST");
		} catch (ProtocolException ex) {
			logLine("Unable to connect", false, ex);
			return;
		}

		urlConn.setRequestProperty("Content-Type", "application/json");
		urlConn.setRequestProperty("sense_key", APIkey);
		urlConn.setRequestProperty("User-Agent", "Contiki collect-view");

		try {
			urlConn.connect();
		} catch (IOException ex) {
			logLine("Unable to connect", false, ex);
			return;
		}

		DataOutputStream output = null;
		BufferedReader input = null;

		try {
			output = new DataOutputStream(urlConn.getOutputStream());
		} catch (IOException ex) {
			logLine("Unable to get output stream", false, ex);
			return;
		}

		String content = constructMsgOpenSense(feedTable);

		try {
			output.writeBytes(content);
			output.flush();
			output.close();
		} catch (IOException ex) {
			logLine("Problem with output stream", false, ex);
			return;
		}

		String str = null;
		try {
			System.out.println("sen.se Publisher@ " + urlConn.getResponseCode() + " "
			    + urlConn.getResponseMessage());
			input = new BufferedReader(
			    new InputStreamReader(urlConn.getInputStream()));
			while (null != ((str = input.readLine()))) {
				System.out.println(str);
			}
			input.close();
		} catch (IOException ex) {
			logLine("Something went wrong", false, ex);
		}
	}
}
