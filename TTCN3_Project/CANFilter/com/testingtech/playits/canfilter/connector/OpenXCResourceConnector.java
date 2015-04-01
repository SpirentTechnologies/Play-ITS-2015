package com.testingtech.playits.canfilter.connector;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Hashtable;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

import com.testingtech.playits.canfilter.CANFilterLog;
import com.testingtech.playits.canfilter.Car2XEntry;
import com.testingtech.playits.canfilter.FilterLogMessages;
import com.testingtech.playits.canfilter.valueupdater.OpenXCValueUpdater;

public class OpenXCResourceConnector implements ResourceConnector {
	
	private Hashtable<String, Car2XEntry> car2xEntries;
	private InetSocketAddress address;
	private CANFilterLog canFilterLog = new CANFilterLog(
			OpenXCResourceConnector.class.getSimpleName());
	private boolean isDisconnected;
	
	public OpenXCResourceConnector(Hashtable<String, Car2XEntry> car2xEntries, InetSocketAddress addess) {
		this.car2xEntries = car2xEntries;
		this.address = addess;
	}

	/**
	 * Connects to the openXC simulator and updates values as long as there are
	 * updated requested (at least one entry exists).
	 */
	public void run() {
		isDisconnected = false;
		OpenXCValueUpdater openXCEntryUpdater = new OpenXCValueUpdater(car2xEntries);
		Socket socket = null;
		Scanner scanner = null;
		try {
			socket = new Socket();
			canFilterLog.logInfo(FilterLogMessages.OPENXC_CONNECTION,
					address.getHostName(), String.valueOf(address.getPort()));
			socket.connect(address);

			scanner = new Scanner(socket.getInputStream()).useDelimiter(String
					.valueOf((char) 0));

			while (!isDisconnected) {
				JSONObject jsonObject = new JSONObject(scanner.next());
				openXCEntryUpdater.updateEntry(jsonObject.getString("name"), jsonObject
						.get("name"));
			}
		} catch (SocketTimeoutException e) {
			canFilterLog.logError(FilterLogMessages.SOCKET_TIMEOUT,
					address.getHostName(), String.valueOf(address.getPort()), e.getMessage());
		} catch (IOException ioe) {
			canFilterLog.logError(FilterLogMessages.SOCKET_CONNECT,
					address.getHostName(), String.valueOf(address.getPort()), ioe.getMessage());
		} catch (JSONException e) {
			canFilterLog.logError(FilterLogMessages.JSON_ERROR, e.getMessage());
		} finally {
			canFilterLog.logInfo(FilterLogMessages.NO_MORE_ENTRIES);
			close(scanner);
			close(socket);
		}
	}
	
	private void close(Closeable socket) {
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			canFilterLog.logError(FilterLogMessages.SOCKET_ERROR,
					e.getMessage());
		}
	}

	@Override
	public void disconnect() {
		isDisconnected = true;
	}
}
