package canfilter;

import java.net.InetSocketAddress;
import java.util.Hashtable;

public class CANFilter {

	static Hashtable<String, TableDataType> hashTable = new Hashtable<String, TableDataType>();


	public static void main(String[] args) {
		// args contain hosts (localhost) and ports (e.g. 50001) 
		// TODO filter out hosts and addresses correctly
		
		InetSocketAddress serverSocketAddress = new InetSocketAddress(args[0], new Integer(args[1]).intValue());
		InetSocketAddress clientSocketAddress = new InetSocketAddress(args[2], new Integer(args[3]).intValue());
		
		Thread serverThread = new CANFilterServer(hashTable, serverSocketAddress);
		Thread clientThread = new CANFilterClient(hashTable, clientSocketAddress);
		
		serverThread.run();
		clientThread.run();
		
		while (true) {
			try {
				serverThread.join();
				clientThread.join();
				break;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
