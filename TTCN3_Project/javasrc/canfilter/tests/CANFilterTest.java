package canfilter.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Hashtable;
import java.util.Scanner;

import org.junit.Test;

import canfilter.TableDataType;

public class CANFilterTest {

	@Test
	public void getXcSimDataTest() {
		String serverHost = "localhost";
		int serverPort = 50001;
		Socket sock = null;
		try {
			// establish the socket
			sock = new Socket();
			sock.setSoLinger(true, 0);
			sock.connect(new InetSocketAddress(serverHost, serverPort), 50001);
			Scanner inputStream = new Scanner(sock.getInputStream()).useDelimiter("}");
			String str = new String();
			str = inputStream.next() + "}";
			assertTrue(str.startsWith("{") || str.endsWith("}"));
		} catch (SocketTimeoutException e) {
			fail("Timeout could not connect to " + serverHost+ ":" + serverPort);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (sock != null) {
				try {
					sock.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	@Test
	public void putToHashTableTest(){
		Hashtable<String, TableDataType> hashTable = new Hashtable<String, TableDataType>();
		TableDataType tabledata = new TableDataType("vehicle_speed", "0x07","217"," false");
		hashTable.put(tabledata.getOpenxckey(), tabledata);
        assertTrue(tabledata.equals(hashTable.get("vehicle_speed")));  
	}
	
	@Test
	public void receiveMessageAsServerTest(){
		int portNumber = 50005;
		String iface = "localhost";
		ServerSocket sockS = null;
		Socket sockC = null;
		try {
			// establish the socket
			InetAddress ifaceAddr = InetAddress.getByName(iface);
			sockS = new ServerSocket();
			sockC = new Socket();
			sockS.setReuseAddress(true);
			sockS.bind(new InetSocketAddress(ifaceAddr, portNumber));
			sockC.connect(new InetSocketAddress(iface, portNumber), portNumber);
			assertTrue(!sockC.isClosed());
		}
		catch (IOException ioe) {
			System.err.println("Error listening on port: "+portNumber+": "+ioe);
		}
		finally {
			if (sockS != null)
				try {
					sockS.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

}
