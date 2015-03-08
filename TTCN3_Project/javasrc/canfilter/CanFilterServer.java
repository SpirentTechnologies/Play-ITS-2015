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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Hashtable;

import org.json.JSONException;
import org.json.JSONObject;

public class CanFilterServer {
	private static char[] HEX_VAL = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

	private static final int DEFAULT_TCP_PORT = 7000;

	public static void main(String[] args) throws IOException {
		int portNumber = DEFAULT_TCP_PORT;
		String iface = "localhost";

		ServerSocket sock = null;

		try {
			// establish the socket
			InetAddress ifaceAddr = InetAddress.getByName(iface);
			sock = new ServerSocket();
			sock.setReuseAddress(true);
			sock.bind(new InetSocketAddress(ifaceAddr, portNumber));

			System.out.println("TCP echo server listening on " + iface + ":" + portNumber);

			/**
			 * listen for new connection requests. when a request arrives, service it
			 * and resume listening for more requests.
			 */
			while (true) {
				// now listen for connections
				Socket client = sock.accept();
				System.out.println("accepted connection from "+client);

				// service the connection
				ServiceConnection(client);
			}
		}
		catch (IOException ioe) {
			System.err.println("Error listening on port: "+portNumber+": "+ioe);
		}
		finally {
			if (sock != null)
				sock.close();
		}
	}

	public static void ServiceConnection(Socket client) {

		BufferedInputStream networkBin = null;
		OutputStream networkPout = null;
		
		
		JSONObject jsonObject = null;

		
		byte[] buf = new byte[512];

		try {
			/**
			 * get the input and output streams associated with the socket.
			 */
			networkBin = new BufferedInputStream(client
					.getInputStream());
			networkPout = client.getOutputStream();

			/**
			 * the following successively reads from the input stream and returns what
			 * was read. The loop terminates with ^D or the string "bye\r\n" from the
			 * input stream.
			 */
			while (true) {
				int read = networkBin.read(buf);
				if(read<0) {
					break;
				}

				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < read; i++) {
					int absVal = buf[i] & 0xFF; 
					sb.append("0x").append(HEX_VAL[absVal / 16]).append(HEX_VAL[absVal % 16]).append(" ");
				}
				
				System.out.println("read "+read+" bytes:");
				System.out.println(sb.toString());
				
				String request = sb.toString();
				try {
					jsonObject = new JSONObject(request);
					String requestType = jsonObject.getString("type");
					String requestData = jsonObject.getString("data");
					
					if (requestType == "info"){
						
						
					} else if (requestType == "get"){
						TableDataType reply = getFromDataTable(requestData);
						JSONObject replyJson = new JSONObject("{ \"type\":data, \"data\": " + reply.toString() +"}");
						String jsonString = replyJson.toString();
						byte[] b = jsonString.getBytes(Charset.forName("UTF-8"));
						
						System.out.println("Reply: " + jsonString);
						networkPout.write(b);
						
						networkPout.flush();
					}
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
//				networkPout.write(buf, 0, read);
//				networkPout.flush();        
			}
		}
		catch (IOException ioe) {
			System.err.println(ioe);
		}
		finally {
			try {
				if (networkBin != null)
					networkBin.close();
				if (networkPout != null)
					networkPout.close();
				if (client != null)
					client.close();
			}
			catch (IOException ioee) {
				System.err.println(ioee);
			}
		} // end try
	} // end ServiceConnection
	
	synchronized public static TableDataType getFromDataTable(String str){
		Hashtable<String, TableDataType> hashTable = CANFilterClient.getHashTable();
		return hashTable.get(str);
	}
}





