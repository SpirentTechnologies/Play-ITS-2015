package com.testingtech.playits.canfilter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class RequestProcessor {

  private Socket socket;
  private CANFilterLog canFilterLog = new CANFilterLog(
      RequestProcessor.class.getSimpleName());
  private JSONTokener jsonTokener;
  // private Car2XEntryUpdater car2xEntryUpdater;
  private TimeoutResponder timeoutResponder;
  private Hashtable<String, Car2XEntry> car2xEntries;

  public RequestProcessor(ServerSocket serverSocket,
      Hashtable<String, Car2XEntry> car2xEntries) throws IOException,
      JSONException {
    this.car2xEntries = car2xEntries;
    acceptRequests(serverSocket);
    // car2xEntryUpdater = new Car2XEntryUpdater(car2xEntries);
    timeoutResponder = new TimeoutResponder(socket, car2xEntries);
  }

  private void acceptRequests(ServerSocket serverSocket) throws IOException,
      JSONException {
    socket = serverSocket.accept();
    jsonTokener = new JSONTokener(socket.getInputStream());
  }

  public boolean hasMoreRequests() throws JSONException {
    return jsonTokener.more();
  }

  public void processNextRequest() throws JSONException, IOException {
    Object nextValue = jsonTokener.nextValue();
    if (nextValue instanceof JSONObject) {
      canFilterLog.logInfo(FilterLogMessages.INCOMING_REQUEST,
          nextValue.toString());
      // TODO maybe add a kill request to exit loop
      processRequest((JSONObject) nextValue);
    } else {
      canFilterLog.logError(FilterLogMessages.JSON_ERROR,
          nextValue.toString());
    }
  }

  private void processRequest(JSONObject request) throws IOException,
      JSONException {
    JSONArray data = request.getJSONArray("reqData");
    switch (request.getString("reqType")) {
    case "start":
      // TODO add loop
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

  public void addEntries(JSONArray jsonArray) throws JSONException {
    for (int index = 0; index < jsonArray.length(); index++) {
      JSONObject jsonObject = jsonArray.getJSONObject(index);
      addEntry(jsonObject.getString("key"), jsonObject.getInt("interval"));
    }
  }

  private void addEntry(String key, int interval) {
    canFilterLog.logInfo(FilterLogMessages.ENTRY_ADDED, key);
    car2xEntries.put(key, new Car2XEntry());
    timeoutResponder.addTimer(key, interval);
  }

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
}
