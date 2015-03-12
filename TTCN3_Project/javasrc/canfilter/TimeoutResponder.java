package canfilter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TimeoutResponder extends Thread {

	private Hashtable<String, Car2XEntry> car2xValues;
	private Socket client;

	public TimeoutResponder(Hashtable<String, Car2XEntry> car2xValues,
			Socket client) {
		this.car2xValues = car2xValues;
	}

	public void run() {
		while (!car2xValues.isEmpty()) {
			long currentTime = new Date().getTime() / 100;
			Enumeration<String> enumKey = car2xValues.keys();
			while (enumKey.hasMoreElements()) {
				String key = enumKey.nextElement();
				Car2XEntry car2xEntry = car2xValues.get(key);
				if (currentTime % (car2xEntry.getInterval() / 100) == 0) {
					Car2XEntry tableEntry = car2xValues.get(key);
					sendData(key, tableEntry);
				}
			}
		}
	}

	private void sendData(String key, Car2XEntry tableEntry) {
		JSONObject response = new JSONObject();
		try {
			response.put("Timestamp", tableEntry.getTimestamp());
			response.put("OpenXCKey", key);
			response.put("OBD2Key", tableEntry.getObd2key());
			response.put("ValueA", tableEntry.getValueA());
			response.put("ValueB", tableEntry.getValueB());
			printOut(response);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void printOut(JSONObject jsonObject) {
		String jsonAsString = jsonObject.toString();
		byte[] bytes = jsonAsString.getBytes(Charset.forName("UTF-8"));
		OutputStream outputStream;
		try {
			outputStream = client.getOutputStream();
			outputStream.write(bytes);
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}