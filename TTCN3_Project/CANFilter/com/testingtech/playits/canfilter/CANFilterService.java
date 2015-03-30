/*
 * ----------------------------------------------------------------------------
 *  (C) Copyright Testing Technologies.  All Rights Reserved.
 *
 *  All copies of this program, whether in whole or in part, and whether
 *  modified or not, must display this and all other embedded copyright
 *  and ownership notices in full.
 *
 *  See the file COPYRIGHT for details of redistribution and use.
 *
 *  You should have received a copy of the COPYRIGHT file along with
 *  this file; if not, write to the Testing Technologies,
 *  Michaelkirchstr. 17/18, 10179 Berlin, Germany.
 *
 *  TESTING TECHNOLOGIES DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS
 *  SOFTWARE. IN NO EVENT SHALL TESTING TECHNOLOGIES BE LIABLE FOR ANY
 *  SPECIAL, DIRECT, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 *  WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN
 *  AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
 *  ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 *  THIS SOFTWARE.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND,
 *  EITHER EXPRESSED OR IMPLIED, INCLUDING ANY KIND OF IMPLIED OR
 *  EXPRESSED WARRANTY OF NON-INFRINGEMENT OR THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.
 * ----------------------------------------------------------------------------
 */
package com.testingtech.playits.canfilter;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class CANFilterService {

	private static final int DEFAULT_SERVER_PORT = 7000;
	private static final String DEFAULT_SERVER_HOST = "localhost";

	private static Hashtable<String, Car2XEntry> car2xEntries = new Hashtable<>();
	private static Car2XEntryUpdater entryUpdater;
	private static TimeoutResponder timeoutResponder;
	static CANFilterLog canFilterLog = new CANFilterLog(
			CANFilterService.class.getSimpleName());

	/**
	 * Provides TCP server functionality to handle JSON requests. Upon receiving
	 * a start request, an entry updater thread which updates openXC / obd2 hash
	 * table entries is started. Parallel timeout responder tasks periodically
	 * send back updated JSON values from the hash table over a client socket.
	 * 
	 * @param args
	 *            host, port
	 */
	public static void main(String[] args) {
		Socket socket = null;
		ServerSocket serverSocket = null;
		try {
			serverSocket = createServerSocket(args);
			socket = serverSocket.accept();
			timeoutResponder = new TimeoutResponder(socket, car2xEntries);
			entryUpdater = new Car2XEntryUpdater(car2xEntries);
			JSONTokener jsonTokener = new JSONTokener(socket.getInputStream());
			while (jsonTokener.more()) {
				try {
					Object nextValue = jsonTokener.nextValue();
					if (nextValue instanceof JSONObject) {
						canFilterLog.logInfo(
								FilterLogMessages.INCOMING_REQUEST,
								nextValue.toString());
						// TODO maybe add a kill request to exit loop
						handleRequest((JSONObject) nextValue);
					} else {
						canFilterLog.logError(FilterLogMessages.JSON_ERROR,
								nextValue.toString());
					}
				} catch (JSONException e) {
					canFilterLog.logError(FilterLogMessages.JSON_ERROR);
				}
			}

		} catch (IOException e) {
			canFilterLog.logError(FilterLogMessages.SOCKET_ERROR);
		} catch (JSONException e) {
			canFilterLog.logError(FilterLogMessages.JSON_ERROR);
		} finally {
			canFilterLog.logInfo(FilterLogMessages.STOPP);
			close(socket);
			close(serverSocket);
		}
	}

	final static String HOST_PATTERN = "(localhost|(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[0-9]{1,2})(\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[0-9]{1,2})){3})";
	final static String PORT_NUMBER_PATTERN = "(6553[0-5]|655[0-2]\\d|65[0-4]\\d\\d|6[0-4]\\d{3}|[1-5]\\d{4}|[1-9]\\d{0,3}|0)";
	
	private static InetSocketAddress createAddress(String[] args) {
		String host;
		int port;
		if (args.length != 2) {
			canFilterLog.logError(FilterLogMessages.WRONG_CMD_LINE_USAGE);
			host = DEFAULT_SERVER_HOST;
			port = DEFAULT_SERVER_PORT;
		} else {
			host = args[0].matches(HOST_PATTERN) ? args[0] : DEFAULT_SERVER_HOST;
			port = args[1].matches(PORT_NUMBER_PATTERN) ? Integer.valueOf(args[1]) : DEFAULT_SERVER_PORT;
		}
		return new InetSocketAddress(host, port);
	}

	private static void close(Closeable socket) {
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			canFilterLog.logError(FilterLogMessages.SOCKET_ERROR,
					e.getMessage());
		}
	}

	private static ServerSocket createServerSocket(String[] args)
			throws IOException, SocketException {
		ServerSocket serverSocket = new ServerSocket();
		serverSocket.setReuseAddress(true);
		InetSocketAddress address = createAddress(args);
		serverSocket.bind(address);
		canFilterLog.logInfo(FilterLogMessages.START, address.getHostName(), String.valueOf(address.getPort()));
		return serverSocket;
	}

	private static void handleRequest(JSONObject request) throws IOException,
			JSONException {
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

	private static void addEntries(JSONArray jsonArray) throws JSONException {
		for (int index = 0; index < jsonArray.length(); index++) {
			JSONObject jsonObject = jsonArray.getJSONObject(index);
			addEntry(jsonObject.getString("key"), jsonObject.getInt("interval"));
		}
	}

	private static void addEntry(String key, int interval) {
		canFilterLog.logInfo(FilterLogMessages.ENTRY_ADDED, key);
		car2xEntries.put(key, new Car2XEntry());

		if (!entryUpdater.isRunning()) {
			new Thread(entryUpdater).start();
		}
		timeoutResponder.addTimer(key, interval);
	}

	private static void removeEntries(JSONArray jsonArray) throws JSONException {
		for (int index = 0; index < jsonArray.length(); index++) {
			JSONObject jsonObject = jsonArray.getJSONObject(index);
			removeEntry(jsonObject.getString("key"));
		}
	}

	private static void removeEntry(String key) {
		canFilterLog.logInfo(FilterLogMessages.ENTRY_REMOVED, key);
		car2xEntries.remove(key);
		timeoutResponder.removeTimer(key);
	}
}
