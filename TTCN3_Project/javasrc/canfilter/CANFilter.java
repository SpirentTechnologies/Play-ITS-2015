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

import org.json.JSONException;
import org.json.JSONObject;

import sun.security.util.Length;




public class CANFilter {
	private static final String DEFAULT_TCP_SERVER_HOST = "localhost";
	private static final int DEFAULT_TCP_SERVER_PORT = 50001;

	public static void main(String[] args) throws IOException {
		String serverHost = DEFAULT_TCP_SERVER_HOST;
		int serverPort = DEFAULT_TCP_SERVER_PORT;

		System.out.println("TCPClient started");

		Socket sock = null;

		try {
			// establish the socket
			sock = new Socket();
			sock.setSoLinger(true, 0);
			sock.connect(new InetSocketAddress(serverHost, serverPort), 50001);
			System.out.println("TCPClient connected to  " + serverHost + ":"
					+ serverPort + " on local port " + sock.getLocalPort());

			Scanner inputStream = new Scanner(sock.getInputStream()).useDelimiter("}");
			// echo back any message received
			
			JSONObject jsonObject = null;
			
			String str = new String();
			while (true) {
				
				str = inputStream.next() + "}";
				str = str.substring(1); //delete first " "

				
//				System.out.println(str);
				
				try {
					jsonObject = new JSONObject(str);
					String name = jsonObject.getString("name"); // get the name from data.
					Object value = jsonObject.get("value");
					String valueAsString = "";
							
					if (value instanceof Double)
						valueAsString = new Double((double) value).toString();
					else if (value instanceof Boolean)
						valueAsString = new Boolean((boolean) value).toString();
					else valueAsString = (String) value;
					
					System.out.println(valueAsString);
							 
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

//				System.out.println(str);
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
