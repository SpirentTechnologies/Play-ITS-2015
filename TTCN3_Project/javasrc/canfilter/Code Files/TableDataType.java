package canfilter;
import java.util.Date;
public class TableDataType {
	//Attributes
	private String openxckey;
	private String obd2key;
	private String value1;
	private String value2;
	private  long timestamp;
	//Constructor
	public TableDataType(String openxckey, String obd2key, String value1, String value2){
		this.openxckey = openxckey;
		this.obd2key = obd2key;
		this.value1 = value1;
		this.value2 = value2;
		this.timestamp = new Date().getTime();
	}	
	
	public TableDataType(String openxckey, String obd2key, String value1) {
		this(openxckey, obd2key, value1, "");
		this.timestamp = new Date().getTime();
	}

	public String getOpenxckey() {
		return openxckey;
	}
	
	public String getObd2key() {
		return obd2key;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public String getValue1() {
		return value1;
	}
	
	public String getValue2() {
		return value2;
	}
	
	public void setOpenxckey(String str){
		this.obd2key = str;
	}
	
	public void setObds2key(String str){
		this.obd2key = str;
	}
	
	public void setTimestamp(long ts){
		this.timestamp = ts;
	}
	
	public void setValue1(String str){
		this.value1 = str;
	}
	
	public void setValue2(String str){
		this.value2 = str;
	}
}