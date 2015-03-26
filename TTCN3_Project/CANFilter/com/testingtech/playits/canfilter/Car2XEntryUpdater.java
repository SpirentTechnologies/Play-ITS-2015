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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

public class Car2XEntryUpdater extends Thread {

	private static final String DEFAULT_SIMULATOR_HOST = "localhost";
	private static final int DEFAULT_SIMULATOR_PORT = 50001;

	private Hashtable<String, Car2XEntry> car2xEntries;
	private InetSocketAddress address;

	/**
	 * Connects to the openXC simulator and updates openXC respectively obd2
	 * values within a hash table provided as a parameter.
	 * 
	 * @param car2xEntries
	 */
	public Car2XEntryUpdater(Hashtable<String, Car2XEntry> car2xEntries) {
		this.car2xEntries = car2xEntries;
		this.address = new InetSocketAddress(DEFAULT_SIMULATOR_HOST,
				DEFAULT_SIMULATOR_PORT);
	}

	/**
	 * Connects to the openXC simulator and updates values as long as there are
	 * updated requested (at least one entry exists).
	 */
	public void run() {
		Socket socket = null;
		Scanner scanner = null;
		try {
			socket = new Socket();
			System.out
					.println("[EntryUpdater] Connecting to openXC simulator at "
							+ address.getHostName()
							+ " on local port "
							+ address.getPort());
			socket.connect(address);

			scanner = new Scanner(socket.getInputStream()).useDelimiter(String
					.valueOf((char) 0));

			while (car2xEntries.size() > 0) {
				parseString(scanner.next());
			}
		} catch (SocketTimeoutException e) {
			System.out.println("[EntryUpdater] Timeout: Could not connect to "
					+ address.getHostName() + ":" + address.getPort());
		} catch (IOException ioe) {
			System.err.println("[EntryUpdater] Could not connect to "
					+ address.getHostName() + " on port " + address.getPort());
		} finally {
			System.out
					.println("[EntryUpdater] No more entries to update. Shutting down.");
			close(scanner);
			close(socket);
		}
	}

	private void close(Scanner scanner) {
		if (scanner != null) {
			scanner.close();
		}
	}

	private void close(Socket socket) {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				System.out.println("[EntryUpdater] Could not close socket: "
						+ e.getMessage());
			}
		}
	}

	private void parseString(String jsonString) {
		// TODO enable to debug
//		 System.out.println("[EntryUpdater] Incoming object: " + jsonString);
		if (jsonString.contains("parking_brake_status"))
				System.out.println(jsonString);
		try {
			JSONObject jsonObject = new JSONObject(jsonString);
			Car2XEntry car2xEntry = car2xEntries.get(jsonObject
					.getString("name"));
			if (car2xEntry != null) {
				updateEntry(car2xEntry, jsonObject);
			}
		} catch (JSONException jsone) {
			System.err.println("[EntryUpdater] Could not parse json string: "
					+ jsonString);
		}
	}

	// TODO add obd2 key
	private void updateEntry(Car2XEntry car2xEntry, JSONObject jsonObject)
			throws JSONException {
		car2xEntry.setTimestamp(new Date().getTime());
		car2xEntry.setValue(jsonObject.get("value"));
		try {
			car2xEntry.setEvent(jsonObject.getBoolean("event"));
		} catch (JSONException jsonException) {
			// ignore missing event, because not every incoming
			// JSON object has an event
		}
	}
}
