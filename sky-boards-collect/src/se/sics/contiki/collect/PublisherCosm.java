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

public class PublisherCosm extends Thread {
	final static String CosmServer = "http://api.cosm.com/v2/feeds/";

	private String APIkey;
	private String cosmFeed;
	private String cosmTitle = "Default Title";
	private final String cosmVersion = "1.0.0";
	Hashtable<String, String> feedTable;

	public PublisherCosm(Hashtable<String, String> feedTable, String key,
	    String cosmFeed) {
		APIkey = key;
		this.feedTable = feedTable;
		this.cosmFeed = cosmFeed;
	}

	public String getAPIkey() {
		return APIkey;
	}

	public void setAPIkey(String APIkey) {
		this.APIkey = APIkey;
	}

	public void setCosmFeed(String cosmFeed) {
		this.cosmFeed = cosmFeed;
	}

	public String constructMsgCosm(Hashtable<String, String> feedTable) {
		StringBuffer msg = new StringBuffer();
		char separator = ',';
		String value;
		for (Object key : feedTable.keySet()) {
			value = feedTable.get(key).replace(",", ".");// Set "." as decimal mark
			msg.append("{\"id\" : \"" + key + "\", " + "\"current_value\" :\""
			    + value + "\"}" + separator);
		}
		// remove last separator
		String msgbody = msg.toString();
		msgbody = msgbody.substring(0, msgbody.length() - 1);
		return "{\"title\":\"" + cosmTitle + "\"," + "\"version\":\"" + cosmVersion
		    + "\",\"datastreams\":[" + msgbody + "]}";
	}

	public void setCosmTitle(String cosmTitle) {
		this.cosmTitle = cosmTitle;
	}

	public String getCosmTitle() {
		return cosmTitle;
	}

	public String getCosmVersion() {
		return cosmVersion;
	}

	protected void logLine(String line, boolean stderr, Throwable e) {
		if (stderr) {
			System.err.println("Cosm Publisher@ " + line);
		} else {
			System.out.println("Cosm Publisher@ " + line);
		}
		if (e != null) {
			e.printStackTrace();
		}
	}

	public void run() {
		if (feedTable.isEmpty())
			return; // nothing to feed
		if (cosmTitle == null)
			return; // Title can't be blank
		if (cosmFeed == null)
			return;

		URL url = null;
		try {
			url = new URL(CosmServer + cosmFeed);
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
			urlConn.setRequestMethod("PUT");
		} catch (ProtocolException ex) {
			logLine("Unable to connect", false, ex);
			return;
		}

		urlConn.setRequestProperty("Content-Type", "application/json");
		urlConn.setRequestProperty("X-ApiKey", APIkey);
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

		String content = constructMsgCosm(feedTable);

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
			System.out.println("Cosm Publisher@ " + urlConn.getResponseCode() + " "
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
