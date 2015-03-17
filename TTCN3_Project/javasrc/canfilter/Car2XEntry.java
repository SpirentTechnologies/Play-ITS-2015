package canfilter;

public class Car2XEntry {
	// Attributes
	private String obd2key;
	private String valueA;
	private String valueB;
	private long timestamp;

	public String getObd2key() {
		return obd2key;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getValueA() {
		return valueA;
	}

	public String getValueB() {
		return valueB;
	}

	public void setOpenxckey(String str) {
		this.obd2key = str;
	}

	public void setObds2key(String str) {
		this.obd2key = str;
	}

	public void setTimestamp(long ts) {
		this.timestamp = ts;
	}

	public void setValueA(String str) {
		this.valueA = str;
	}

	public void setValueB(String str) {
		this.valueB = str;
	}
}