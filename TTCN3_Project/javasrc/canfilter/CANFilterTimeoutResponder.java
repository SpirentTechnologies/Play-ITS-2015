package canfilter;


import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
public class CANFilterTimeoutResponder extends Thread {
	

	private Hashtable<String, TableDataType> hashTable;
	private InetSocketAddress address;
	public Hashtable<String, Integer> intervallTable;
	
	public CANFilterTimeoutResponder(Hashtable<String, TableDataType> hashTable, Hashtable<String, Integer> intervallTable ,InetSocketAddress address) {
		this.hashTable = hashTable;
		this.address = address;
		this.intervallTable = intervallTable;
	}
	
	   public void run() {
		   while(true){
			   long currentTime = new Date().getTime() /100;
//			   Hashtable<Integer, String> table
			   Enumeration<String> enumKey = intervallTable.keys();
			   while(enumKey.hasMoreElements()) {
			       String key = enumKey.nextElement();
			       int interval = intervallTable.get(key);
			       if(currentTime % (interval/100) == 0){
			    	   TableDataType tableEntry = hashTable.get(key);
			           sendData(tableEntry);
			           
			       }
			   
			   }
		   
		   }
	   }
	   
}