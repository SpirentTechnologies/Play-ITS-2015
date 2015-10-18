package com.testingtech.playits.canfilter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

public class ResponderTask extends TimerTask {

	private String key;
	private Car2XEntry car2xEntry;
	private Socket socket;
	private CANFilterLog canFilterLog = new CANFilterLog(ResponderTask.class.getSimpleName());

	private JSONObject response = new JSONObject();

	/**
	 * Periodically sends a key-value pair encoded as a JSON String over a
	 * socket's output stream.
	 * 
	 * @param key
	 *            openXC key
	 * @param car2xEntry
	 *            corresponding value entry
	 * @param socket
	 *            TCP response channel
	 */
	public ResponderTask(String key, Car2XEntry car2xEntry, Socket socket) {
		this.key = key;
		this.car2xEntry = car2xEntry;
		this.socket = socket;
		createResponseTemplate(key, car2xEntry);
	}

	private void createResponseTemplate(String key, Car2XEntry car2xEntry) {
		try {
			response.put("OpenXCKey", key);
			response.put("OBD2Key", car2xEntry.getOBD2key());
		} catch (JSONException e) {
			canFilterLog.logError(FilterLogMessages.JSON_ERROR,
					"Cannot create response object.");
		}
	}

	@Override
	public void run() {
		try {
			JSONObject response = updateResponse();
			canFilterLog.logInfo(FilterLogMessages.SENDING_RESPONSE,
					response.toString());
			sendJSONObject(response);
		} catch (JSONException e) {
			canFilterLog.logError(FilterLogMessages.JSON_ERROR, e.getMessage());
		}
	}

	private JSONObject updateResponse() throws JSONException {
		// INFO: omits key if value is empty
		response.put("car2XValue", createValue());
		response.put("eventValue", car2xEntry.getEvent());
		response.put("respTimestamp", car2xEntry.getTimestamp());
		return response;
	}

	private JSONObject createValue() throws JSONException {
		JSONObject valueObject = new JSONObject();
		Object value = car2xEntry.getValue();
		if (value instanceof String) {
			valueObject.put("stringValue", value.toString());
		} else if (value instanceof Double) {
			valueObject.put("floatValue", new Float((double) value));
		} else if (value instanceof Integer) {
			valueObject.put("floatValue", new Float((int) value));
		} else if (value instanceof Boolean) {
			valueObject.put("booleanValue", (boolean) value);
		} else {
			throw new JSONException(
					value
							+ " is not supported. The entry may not yet have been updated / initialized.");
		}
		return valueObject;
	}

	private void sendJSONObject(JSONObject jsonObject) {
		String jsonAsString = jsonObject.toString();
		byte[] bytes = jsonAsString.getBytes(Charset.forName("UTF-8"));
		try {
			sendBytes(bytes);
		} catch (IOException e) {
			canFilterLog.logError(FilterLogMessages.SENDING_RESPONSE, key,
					e.getMessage());
			cancel();
		}
	}

	private void sendBytes(byte[] bytes) throws IOException {
		OutputStream outputStream = socket.getOutputStream();
		outputStream.write(bytes);
		outputStream.flush();
	}
}
