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
package canfilter;

import java.io.IOException;
import java.lang.Thread.State;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CANFilterService {

	private static final int DEFAULT_TCP_PORT = 7000;
	int portNumber = DEFAULT_TCP_PORT;
	String iface = "localhost";
	static Hashtable<String, Car2XEntry> car2xEntries = new Hashtable<>();

	public static void main(String[] args) {

		InetSocketAddress address = new InetSocketAddress(args[0], new Integer(
				args[1]).intValue());

		Socket client = null;

		ServerSocket sock = null;
		Scanner scanner = null;

		try {
			// establish the socket
			sock = new ServerSocket();
			sock.setReuseAddress(true);
			sock.bind(address);

			System.out.println("TCP echo server listening on "
					+ address.getHostName() + ":" + address.getPort());

			TimeoutResponder timeoutResponder = null;
			Car2xEntryUpdater canSimulatorReader = null;

			/**
			 * listen for new connection requests. when a request arrives,
			 * service it and resume listening for more requests.
			 */
			while (true) {
				client = sock.accept();
				scanner = new Scanner(client.getInputStream());
				JSONObject jsonObject = new JSONObject(scanner.next());
				JSONArray data = jsonObject.getJSONArray("reqData");
				switch (jsonObject.getString("reqType")) {
				case "start":
					addEntry(data);
					if (canSimulatorReader == null || canSimulatorReader.getState() != State.RUNNABLE) {
						canSimulatorReader = new Car2xEntryUpdater(car2xEntries);
						canSimulatorReader.start();
					}
					if (timeoutResponder == null || timeoutResponder.getState() != State.RUNNABLE) {
						timeoutResponder = new TimeoutResponder(car2xEntries, client);
						timeoutResponder.start();
					}
					break;
				case "stop":
					removeEntry(data);
					break;
				default:
					System.err.println("Unsupported request");
					break;
				}
			}

		} catch (IOException ioe) {
			System.err.println("Error listening on port: " + address.getPort()
					+ ": " + ioe);
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			if (sock != null)
				try {
					sock.close();
				} catch (IOException e) {
				}
			scanner.close();
		}
	}

	synchronized public Car2XEntry[] getFromDataTable(String[] str) {
		Car2XEntry[] stringArray = new Car2XEntry[str.length];
		for (int i = 0; i < str.length; i++) {
			stringArray[i] = car2xEntries.get(str);
		}
		return stringArray;
	}

	static void addEntry(JSONArray jsonArray) {
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject obj;
			try {
				obj = jsonArray.getJSONObject(i);
				Car2XEntry car2xEntry = new Car2XEntry(obj.getInt("interval"));
				car2xEntries.put(obj.getString("key"), car2xEntry);
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
	}

	static void removeEntry(JSONArray jsonArray) {
		for (int i = 0; i < jsonArray.length(); i++) {
			try {
				car2xEntries.remove(jsonArray.getString(i));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

}
