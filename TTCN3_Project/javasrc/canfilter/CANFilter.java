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
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import sun.security.util.Length;

import java.util.Hashtable;

public class CANFilter {
	private static final String DEFAULT_TCP_SERVER_HOST = "localhost";
	private static final int DEFAULT_TCP_SERVER_PORT = 50001;

	public static void main(String[] args) throws IOException {
		String serverHost = DEFAULT_TCP_SERVER_HOST;
		int serverPort = DEFAULT_TCP_SERVER_PORT;

		System.out.println("TCPClient started");

		Socket sock = null;
		
		Hashtable hashTable = new Hashtable();

		try {
			// establish the socket
			sock = new Socket();
			sock.setSoLinger(true, 0);
			sock.connect(new InetSocketAddress(serverHost, serverPort), 50001);
			System.out.println("TCPClient connected to  " + serverHost + ":"
					+ serverPort + " on local port " + sock.getLocalPort());

			Scanner inputStream = new Scanner(sock.getInputStream())
					.useDelimiter("}");
			// echo back any message received

			int counter = 1000;
			long startTime = System.currentTimeMillis();
			String str = new String();
			while (true) {
				if (counter == 1000) {
					startTime = System.currentTimeMillis();
				}
				counter--;
				str = inputStream.next() + "}";
				if (!(str.substring(0, 1).equals("{"))) {
					str = str.substring(1); // delete first " "
				}

				TableDataType tabledata = stringReceive(str);
//				tabledata = stringReceive(str);
//				regExpReceive(str);
				if (counter == 0) {
					long stopTime = System.currentTimeMillis();
					long elapsedTime = stopTime - startTime;
					System.out.println(elapsedTime);
					counter = 1000;
				}
				
				hashTable.put(tabledata.openxckey, tabledata);
				
//				System.out.println(hashTable.get("vehicle_speed"));
//				System.out.println(hashTable);
				System.out.println(hashTable.size());

			}

		} catch (SocketTimeoutException e) {
			System.out.println("Timeout could not connect to " + serverHost
					+ ":" + serverPort);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (sock != null) {
				sock.close();
			}
		}
	}

	private static void regExpReceive(String str) {
		Pattern pattern = Pattern.compile("[\\w\\.\\-]+");
		Matcher matcher = pattern.matcher(str);

		if (matcher.find()) {
			int groupCount = matcher.groupCount();
			System.out.println(groupCount);
			for (int i = 0; i < groupCount; i++) {
				System.out.print(matcher.group(i) + ", ");
			}
		}
		// System.out.println();
		// String name = matcher.group(2);
		// String value = matcher.group(4);
		// String event = matcher.group(6);

	}

	private static TableDataType stringReceive(String str) {
		// System.out.println(str);
		
		JSONObject jsonObject = null;
		
		String valueAsString = "";
		String eventAsString = "";
		String name = "";
		try {
			jsonObject = new JSONObject(str);
			name = jsonObject.getString("name"); // get the name from data.
			Object value = jsonObject.get("value");
			
			
			if (jsonObject.has("event")){
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

		// System.out.println(str);
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