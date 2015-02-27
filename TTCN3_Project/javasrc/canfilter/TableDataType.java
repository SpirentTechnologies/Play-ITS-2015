package canfilter;
import java.util.Date;
public class TableDataType {
	//Attributes
	String openxckey;
	String obd2key;
	String value1;
	String value2;
	long timestamp;
	//Constructor
	TableDataType(String openxckey, String obd2key, String value1, String value2){
		this.openxckey = openxckey;
		this.obd2key = obd2key;
		this.value1 = value1;
		this.value2 = value2;
		this.timestamp = new Date().getTime();
	}	
}