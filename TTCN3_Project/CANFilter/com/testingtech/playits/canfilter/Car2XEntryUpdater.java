package com.testingtech.playits.canfilter;

import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Car2XEntryUpdater {

	private CANFilterLog canFilterLog = new CANFilterLog(
			Car2XEntryUpdater.class.getSimpleName());

	private Hashtable<String, Car2XEntry> car2xEntries = new Hashtable<>();

	public Car2XEntryUpdater(Hashtable<String, Car2XEntry> car2xEntries) {
		this.car2xEntries = car2xEntries;
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

		// timeoutResponder.addTimer(key, interval);
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
		// timeoutResponder.removeTimer(key);
	}
}
