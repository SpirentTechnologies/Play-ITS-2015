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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class CANFilterServer extends Thread {
	private static char[] HEX_VAL = { '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private static final int DEFAULT_TCP_PORT = 7000;
	int portNumber = DEFAULT_TCP_PORT;
	String iface = "localhost";

	private Hashtable<String, TableDataType> hashTable;
	private Hashtable<String, Integer> intervallTable;
	private InetSocketAddress address;

	public CANFilterServer(Hashtable<String, TableDataType> hashTable, InetSocketAddress address) {
		this.hashTable = hashTable;
		this.address = address;
	}

	public void run() {
		
		//Test SQL Get
		System.out.println(getFromDatabase("vehicle_speed"));

		ServerSocket sock = null;

		try {
			// establish the socket
			sock = new ServerSocket();
			sock.setReuseAddress(true);
			sock.bind(address);

			System.out.println("TCP echo server listening on " + iface + ":"
					+ portNumber);

			/**
			 * listen for new connection requests. when a request arrives,
			 * service it and resume listening for more requests.
			 */
			while (true) {
				// now listen for connections
				Socket client = sock.accept();
				System.out.println("accepted connection from " + client);

				// service the connection
				ServiceConnection(client);
			}
		} catch (IOException ioe) {
			System.err.println("Error listening on port: " + portNumber + ": "
					+ ioe);
		} finally {
			if (sock != null)
				try {
					sock.close();
				} catch (IOException e) {
				}
		}
	}

	public void ServiceConnection(Socket client) {

		Pattern pattern = Pattern
				.compile("\\{\"type\": \"(\\w+)\", \"data\": \\[(\"\\w+\",?)+\\]\\}");
		BufferedInputStream networkBin = null;
		OutputStream networkPout = null;

		JSONObject jsonObject = null;

		byte[] buf = new byte[512];

		try {
			/**
			 * get the input and output streams associated with the socket.
			 */
			networkBin = new BufferedInputStream(client.getInputStream());
			networkPout = client.getOutputStream();

			/**
			 * the following successively reads from the input stream and
			 * returns what was read. The loop terminates with ^D or the string
			 * "bye\r\n" from the input stream.
			 */
			while (true) {
				int read = networkBin.read(buf);
				if (read < 0) {
					break;
				}

				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < read; i++) {
					int absVal = buf[i] & 0xFF;
					sb.append("0x").append(HEX_VAL[absVal / 16])
							.append(HEX_VAL[absVal % 16]).append(" ");
				}

				System.out.println("read " + read + " bytes:");
				System.out.println(sb.toString());

				String request = sb.toString();
				try {

//					Pattern pattern = Pattern
//							.compile("\\{\"type\": \"(\\w+)\", \"data\": \"?(\\w+|[-+]?[0-9]*\\.?[0-9]+)\"?\\}");
					
//					Matcher matcher = pattern.matcher(request);
//
//					if (matcher.find()) {
//						// get type and first data element
//						//type
//						String requestType = matcher.group(1);
//						//data
//						String requestDataString = matcher.group(2);
//						//cut "[" and "]"
//						requestDataString = requestDataString.substring(0, requestDataString.length()-2);
//						//remove additional ""
//						requestDataString = requestDataString.replace("\"", "");
//						//convert to StringArray
//						String[] requestData = requestDataString.split(",");
//
//						// if more than one requested element (immernoch unschön, weil's nicht intuitiv ist)
//						while (matcher.find()) {
//							TableDataType tableDataType = hashTable.get(matcher.group());
//							printOut(networkPout, tableDataType.getValue1());
//						}
						
						JSONObject jo = new JSONObject(request);
						String requestType = jo.getString("reqType");
						JSONArray requestData = jo.getJSONArray("reqData");

						if (requestType == "stop") {
							stopGettingData(requestData);
							// TODO: abklären wir hier reagiert werden soll

							// stop receiving this particular data out of
							// sim-elm327

							// ggf sende bestätigung

						} else if (requestType == "start") {
							addToIntervallTable(requestData);
							
							// from now on receive accordingly data from
							// sim/ELM327
//							TableDataType[] reply = getFromDataTable(requestData);
//
//							for (int i = 0; i < reply.length; i++) {
//								JSONObject replyJson = new JSONObject(
//										"{ \"type\":data, \"data\": "
//												+ reply[i].toString() + "}");
//								String jsonString = replyJson.toString();
//								printOut(networkPout, jsonString);
//							}
						} else if (requestType == "info") {
							// TODO: abklären wir hier reagiert werden soll
						}
//					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// networkPout.write(buf, 0, read);
				// networkPout.flush();
			}
		} catch (IOException ioe) {
			System.err.println(ioe);
		} finally {
			try {
				if (networkBin != null)
					networkBin.close();
				if (networkPout != null)
					networkPout.close();
				if (client != null)
					client.close();
			} catch (IOException ioee) {
				System.err.println(ioee);
			}
		} // end try
	} // end ServiceConnection

	private void printOut(OutputStream networkPout, String jsonString)
			throws IOException {
		byte[] b = jsonString.getBytes(Charset
				.forName("UTF-8"));
		System.out.println("Reply: " + jsonString);
		networkPout.write(b);
		networkPout.flush();
	}

	synchronized public TableDataType[] getFromDataTable(String[] str) {
		TableDataType[] stringArray = new TableDataType[str.length];
		for (int i = 0; i < str.length; i++) {
			stringArray[i] = hashTable.get(str);
		}
		return stringArray;
	}
	
	
	synchronized private void addToIntervallTable(JSONArray  ja){
		for (int i = 0; i < ja.length(); i++) {
			JSONObject obj;
			try {
				obj = ja.getJSONObject(i);
				String name = obj.getString("key");
				int intervall = obj.getInt("interval");
				intervallTable.put(name, intervall);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	synchronized private void stopGettingData(JSONArray ja){
		for (int i = 0; i < ja.length(); i++) {
			try {
				intervallTable.remove(ja.getString(i));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	private TableDataType getFromDatabase(String str){
		TableDataType result = new TableDataType("","","");
		  Connection connect = null;
		  Statement statement = null;
		  ResultSet resultSet = null;
		  
		    try {
	      // This will load the MySQL driver, each DB has its own driver
	      Class.forName("com.mysql.jdbc.Driver");
	      // Setup the connection with the DB
	      connect = DriverManager
	          .getConnection("jdbc:mysql://localhost/feedback?"
	              + "user=car2xuser&password=car2x");

	      // Statements allow to issue SQL queries to the database
	      statement = connect.createStatement();
	      
	      // Result set get the result of the SQL query
	      resultSet = statement
	          .executeQuery("select * from valuetable.car2xvalues where openxckey = " + str + "\"");
	      result.setObds2key(resultSet.getString("obds2key"));
	      result.setOpenxckey(resultSet.getString("openXCkey"));
	      result.setValue1(resultSet.getString("valuea"));
	      result.setValue2(resultSet.getString("valueb"));
	      result.setTimestamp(resultSet.getLong("timestamp"));
	      
	      
		    } catch (Exception e) {
		        try {
					throw e;
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		    }
//		    } finally {
//		      close();
//		    }
//	      
	      
	      return result;
	}
}
