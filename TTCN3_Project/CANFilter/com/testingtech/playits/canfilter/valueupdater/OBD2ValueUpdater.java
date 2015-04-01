package com.testingtech.playits.canfilter.valueupdater;

import java.util.Date;
import java.util.Hashtable;

import com.testingtech.playits.canfilter.Car2XEntry;

public class OBD2ValueUpdater implements ValueUpdater {

	private Hashtable<String, Car2XEntry> car2xEntries;
	private static final String TWO_HEX_BYTES = "([0-9A-F]{2})+";

	public OBD2ValueUpdater(Hashtable<String, Car2XEntry> car2xEntries) {
		this.car2xEntries = car2xEntries;
	}

	@Override
	public void updateEntry(String key, Object value) {
		Car2XEntry car2xEntry = car2xEntries.get(key);
		String stringValue = calculateInput(value.toString());
		Object returnValue;
		try{
			returnValue = Float.parseFloat(stringValue);
		} catch (NumberFormatException e){
			returnValue = stringValue;
		}
			
		car2xEntry.setValue(returnValue);
		car2xEntry.setTimestamp(new Date().getTime());
	}

	/**
	 * @param response
	 *            The raw response
	 * @return the as possible calculated and converted reply see
	 *         http://en.wikipedia.org/wiki/OBD-II_PIDs If percentage wanted: 2
	 *         Hex-Bytes means 00 - FF = 0 - 255. To get the percentage:
	 *         Data/255*100
	 */
	public String calculateInput(String response) {
		Float result;
		int value2 = 0;
		if (!((response.replaceAll("\\s+", "")).matches(TWO_HEX_BYTES))
				|| response.length() < 6) {
			return response; // TODO choose other default value
		} else {
			String[] data = response.split("\\s"); // (response =
			// "41 0C 0C FC") 41,0C,0C,FC, data[0] = Bus, data[1] = Command
			int value1 = Integer.parseInt(data[2], 16);
			if (data.length > 3) {
				value2 = Integer.parseInt(data[3], 16);
			}
			switch (data[1]) {
			case "06":
			case "07":
			case "08":
			case "09":
			case "2D":
				result = (value1 - 128) * 100 / 128f;
				break;
			case "0A": // kPa
				result = value1 * 3f;
				break;
			case "0C": // ((A*256)+B)/4
				result = (value1 * 256 + value2) / 4f;
				break;
			case "33": // kPa
			case "0B":
			case "0D":
				result = (float) value1;
				break;
			case "0E": // relative to #1 cylinder
				result = (value1 - 128) / 2f;
				break;
			case "46":
			case "05":
			case "0F":
			case "5C":
				result = value1 - 40f;
				break;
			case "10":
				result = (value1 * 256 + value2) / 100f;
				break;
			case "21":
			case "1F":
			case "31":
			case "4D":
			case "4E":
			case "63": // torque Nm
				result = value1 * 256f + value2;
				break;
			case "22":
				result = (value1 * 256 + value2) * 0.079f;
				break;
			case "23":
				result = (value1 * 256 + value2) * 10f;
				break;
			case "42":
				result = (value1 * 256 + value2) / 1000f;
				break;
			case "43":
				result = (value1 * 256 + value2) * 100 / 255f;
				break;
			case "44":
				result = (value1 * 256 + value2) / 32768f;
				break;
			case "04":
			case "11":
			case "2C":
			case "2E":
			case "2F":
			case "45":
			case "47":
			case "48":
			case "49":
			case "4A":
			case "4B":
			case "4C":
			case "52":
				result = value1 * 100 / 255f;
				break;
			case "53":
				result = (value1 * 256 + value2) / 200f;
				break;
			case "5E": // engine_fuel_rate
				result = (value1 * 256 + value2) / 20f;
				break;
			case "61": // percentage torque
			case "62": // percentage torque
				result = value1 - 125f;
				break;
			default:
				return response;
			}
		}
		return Float.toString(result);
	}
}
