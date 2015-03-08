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
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

public class CANFilterClient extends Thread {
	
	private Hashtable<String, TableDataType> hashTable;

	private static final String DEFAULT_TCP_SERVER_HOST = "localhost";
	private static final int DEFAULT_TCP_SERVER_PORT = 50001;
	private InetSocketAddress address;

	public CANFilterClient(Hashtable<String, TableDataType> hashTable, InetSocketAddress address) {
		this.hashTable = hashTable;
		this.address = address;
	}

	public void run() {

		System.out.println("TCPClient started");

		Socket sock = null;
		Scanner scanner = null;

		try {
			// establish the socket
			sock = new Socket();
			sock.setSoLinger(true, 0);
			sock.connect(address);
			System.out.println("TCPClient connected to host : "
					+ address.getHostName() + " on local port " + address.getPort());

			scanner = new Scanner(sock.getInputStream()).useDelimiter("}");

			while (true) {
				TableDataType tabledata = regExpReceive(scanner.next() + "}");
				addToDataTable(tabledata);
				System.out.println(hashTable);
			}

		} catch (SocketTimeoutException e) {
			System.out.println("Timeout could not connect to " + address.getHostName()
					+ ":" + address.getPort());
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (sock != null)
				try {
					sock.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (scanner != null)
				scanner.close();
		}
	}

	private static TableDataType regExpReceive(String str) {
		Pattern pattern = Pattern
				.compile("\\{\"name\": \"(\\w+)\", \"value\": \"?(\\w+|[-+]?[0-9]*\\.?[0-9]+)\"?\\}");
		Pattern eventPattern = Pattern
				.compile("\\{\"name\": \"(\\w+)\", \"value\": \"?(\\w+|[-+]?[0-9]*\\.?[0-9]+)\"?, \"event\": (true|false)\\}");

		Matcher matcher = pattern.matcher(str);

		if (matcher.find()) {
			// group(1) = name, group(2) = value
			return new TableDataType(matcher.group(1), "", matcher.group(2));
		} else {
			matcher = eventPattern.matcher(str);
			if (matcher.find()) {
				// group(1) = name, group(2) = value, group(3) = event
				return new TableDataType(matcher.group(1), "",
						matcher.group(2), matcher.group(3));
			}
		}
		return null; // etwas unschön, besser ne Exception
	}

	synchronized public void addToDataTable(TableDataType tabledata) {
		hashTable.put(tabledata.getOpenxckey(), tabledata);
	}

	private static TableDataType stringReceive(String str) { // kann eigentlich
																// raus
		// System.out.println(str);

		JSONObject jsonObject = null;

		String valueAsString = "";
		String eventAsString = "";
		String name = "";
		try {
			jsonObject = new JSONObject(str);
			name = jsonObject.getString("name");
			Object value = jsonObject.get("value");
			if (jsonObject.has("event")) {
				Object event = jsonObject.get("event");
				if (event instanceof Double)
					eventAsString = new Double((double) event).toString();
				else if (event instanceof Boolean)
					eventAsString = new Boolean((boolean) event).toString();
				else
					eventAsString = (String) event;
			}
			if (value instanceof Double)
				valueAsString = new Double((double) value).toString();
			else if (value instanceof Boolean)
				valueAsString = new Boolean((boolean) value).toString();
			else
				valueAsString = (String) value;
			// System.out.println(name + ":" + valueAsString);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new TableDataType(name, "", valueAsString, eventAsString);
	}

	private static String bufferToString(byte[] buffer) {
		String str = "";
		try {
			str = new String(buffer, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;
	}
}
