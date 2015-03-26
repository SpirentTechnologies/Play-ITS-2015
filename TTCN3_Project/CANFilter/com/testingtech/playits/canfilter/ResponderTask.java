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
  }

  @Override
  public void run() {
    try {
      JSONObject response = createResponse();
      System.out.println("[ResponderTask] Sending " + response);
      sendJSONObject(response);
    } catch (JSONException e) {
      System.out.println("[ResponderTask] Error creating JSON response. "
          + e.getMessage());
    }
  }

  private JSONObject createResponse() throws JSONException {
	// INFO: omits key if value is empty 
    JSONObject response = new JSONObject();
    response.put("OpenXCKey", key);
    response.put("OBD2Key", car2xEntry.getOBD2key());
    response.put("car2XValue", car2xEntry.getValue());
    response.put("eventValue", car2xEntry.getEvent());
    response.put("respTimestamp", car2xEntry.getTimestamp());
    return response;
  }

  private void sendJSONObject(JSONObject jsonObject) {
    String jsonAsString = jsonObject.toString();
    byte[] bytes = jsonAsString.getBytes(Charset.forName("UTF-8"));
    try {
      sendBytes(bytes);
    } catch (IOException e) {
      System.err
          .println("[ResponderTask] Error while sending response for "
              + key + ": " + e.getMessage());
      cancel();
    }
  }

  private void sendBytes(byte[] bytes) throws IOException {
    OutputStream outputStream = socket.getOutputStream();
    outputStream.write(bytes);
    outputStream.flush();
  }
}
