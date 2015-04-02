package com.testingtech.playits.canfilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class RequestProcessor {

	private Socket socket;
	private CANFilterLog canFilterLog = CANFilterLog
			.getLog(RequestProcessor.class.getSimpleName());
	private JSONTokener jsonTokener;
	private TimeoutResponder timeoutResponder;
	private Hashtable<String, Car2XEntry> car2xEntries;
	private ServerSocket serverSocket;
	private Hashtable<String, String> openXCToOBD2Map = new Hashtable<String, String>();

	public RequestProcessor(ServerSocket serverSocket,
			Hashtable<String, Car2XEntry> car2xEntries) throws IOException,
			JSONException {
		this.serverSocket = serverSocket;
		this.car2xEntries = car2xEntries;
		initOpenXCToOBD2Map();
		acceptRequests();
		timeoutResponder = new TimeoutResponder(socket, car2xEntries);
	}

	private void acceptRequests() throws IOException, JSONException {
		socket = serverSocket.accept();
		jsonTokener = new JSONTokener(socket.getInputStream());
	}

	/**
	 * Blocks until there are more requests or the socket has been closed.
	 * @return true if the stream has not yet been closes or there is another request coming in
	 * @throws JSONException Thrown if there is an invalid request incoming.
	 */
	public boolean hasMoreRequests() throws JSONException {
		return jsonTokener.more();
	}

	/**
	 * Processes a JSON request. Supported are start and stop requests, <br>
	 * e.g.
	 * {"reqData":[{"interval":500,"key":"ignition_status"}],"reqType":"start"}.
	 * Either adds or removes entries in resp. from the car2X values table to
	 * observe.
	 * 
	 * @throws JSONException
	 *             Thrown if the request does not conform to a valid JSON format
	 */
	public void processNextRequest() throws JSONException {
		Object nextValue = jsonTokener.nextValue();
		if (nextValue instanceof JSONObject) {
			canFilterLog.logInfo(FilterLogMessages.INCOMING_REQUEST,
					nextValue.toString());
			processRequest((JSONObject) nextValue);
		} else {
			canFilterLog.logError(FilterLogMessages.JSON_ERROR,
					nextValue.toString());
		}
	}

	private void processRequest(JSONObject request) throws JSONException {
		JSONArray data = request.getJSONArray("reqData");
		switch (request.getString("reqType")) {
		case "start":
			addEntries(data);
			break;
		case "stop":
			removeEntries(data);
			break;
		default:
			canFilterLog.logError(FilterLogMessages.UNSUPPORTED_REQUEST);
			break;
		}
	}

	/**
	 * Adds car2X values, identified by an openXC keys, read from a JSON array
	 * to the car2X values table.
	 * 
	 * @param jsonArray
	 *            A JSON array containing keys and intervals for further
	 *            observation.
	 * @throws JSONException
	 *             Thrown if JSON objects within the array do not conform to a
	 *             valid JSON format.
	 */
	public void addEntries(JSONArray jsonArray) throws JSONException {
		for (int index = 0; index < jsonArray.length(); index++) {
			JSONObject jsonObject = jsonArray.getJSONObject(index);
			addEntry(jsonObject.getString("key"), jsonObject.getInt("interval"));
		}
	}

	private void addEntry(String key, int interval) {
		canFilterLog.logInfo(FilterLogMessages.ENTRY_ADDED, key);
		String obd2Key = openXCToOBD2Map.get(key);
		if (obd2Key == null) {
			obd2Key = "NONE";
		}
		car2xEntries.put(key, new Car2XEntry(obd2Key));
		timeoutResponder.addTimer(key, interval);
	}

	/**
	 * Removes car2X values, identified by an openXC keys, read from a JSON
	 * array from the car2X values table.
	 * 
	 * @param jsonArray
	 *            A JSON array containing keys as identifiers to remove car2X
	 *            values from the car2X values table.
	 * @throws JSONException
	 *             Thrown if JSON objects within the array do not conform to a
	 *             valid JSON format.
	 */
	public void removeEntries(JSONArray jsonArray) throws JSONException {
		for (int index = 0; index < jsonArray.length(); index++) {
			JSONObject jsonObject = jsonArray.getJSONObject(index);
			removeEntry(jsonObject.getString("key"));
		}
	}

	private void removeEntry(String key) {
		canFilterLog.logInfo(FilterLogMessages.ENTRY_REMOVED, key);
		car2xEntries.remove(key);
		timeoutResponder.removeTimer(key);
	}

	/**
	 * Closes the server connection and clears the all entries from the car2X
	 * values table.
	 */
	public void shutdown() {
		removeAllEntries();
		canFilterLog.logInfo(FilterLogMessages.SOCKET_CLOSE);
		SocketUtils.close(serverSocket);
		SocketUtils.close(socket);
	}

	private void removeAllEntries() {
		Enumeration<String> openXCkeys = car2xEntries.keys();
		while (openXCkeys.hasMoreElements()) {
			String openXCkey = openXCkeys.nextElement();
			removeEntry(openXCkey);
		}
	}

	/**
	 * Initializes a bidirectional mapping from openXC key to obd2 key.
	 */
	private void initOpenXCToOBD2Map() {
		File file = new File("resources\\openXCToOBD2Map.txt");
		try {
			Scanner scanner = new Scanner(file);
			while (scanner.hasNext()) {
				addKeys(scanner.nextLine().split("="));
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void addKeys(String[] keys) {
		String openXCkey = keys[0];
		String obd2key = keys[1];
		openXCToOBD2Map.put(openXCkey, obd2key);
		openXCToOBD2Map.put(obd2key, openXCkey);
	}

}