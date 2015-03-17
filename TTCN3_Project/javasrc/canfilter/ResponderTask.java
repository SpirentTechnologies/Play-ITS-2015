package canfilter;

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

	public ResponderTask(String key, Car2XEntry car2xEntry, Socket socket) {
		this.key = key;
		this.car2xEntry = car2xEntry;
		this.socket = socket;
	}

	@Override
	public void run() {
		if (car2xEntry.getTimestamp() > 0) {
			try {
				JSONObject response = createResponse();
				send(response);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private JSONObject createResponse() throws JSONException {
		JSONObject response = new JSONObject();
		response.put("Timestamp", car2xEntry.getTimestamp());
		response.put("OpenXCKey", key);
		response.put("OBD2Key", car2xEntry.getObd2key());
		response.put("ValueA", car2xEntry.getValueA());
		response.put("ValueB", car2xEntry.getValueB());
		return response;
	}

	private void send(JSONObject jsonObject) {
		String jsonAsString = jsonObject.toString();
		byte[] bytes = jsonAsString.getBytes(Charset.forName("UTF-8"));
		OutputStream outputStream;
		try {
			outputStream = socket.getOutputStream();
			outputStream.write(bytes);
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
