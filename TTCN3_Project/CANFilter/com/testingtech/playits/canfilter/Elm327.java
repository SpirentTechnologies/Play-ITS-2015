package com.testingtech.playits.canfilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import com.testingtech.playits.canfilter.bluetooth.ELMBluetooth;
import com.testingtech.playits.canfilter.rs232.ELMRS232;

public class Elm327 {
	private static final String TWO_HEX_BYTES = "([0-9A-F]{2})+";
	public final int RS232 = 0;
	public final int BLUETOOTH = 1;
	public int usedConnection;

	public Hashtable<String, String> openXCToOBD2Map = new Hashtable<String, String>();

	public Hashtable<String, Car2XEntry> car2xEntries = new Hashtable<>();
	private ELMBluetooth elmBluetooth;
	private ELMRS232 elmRS232;

	public Elm327() {
		elmBluetooth = new ELMBluetooth();
		elmRS232 = new ELMRS232();
	}

	/**
	 * 
	 * @param cmd
	 * @return see http://en.wikipedia.org/wiki/OBD-II_PIDs adds possible
	 *         available PID Codes
	 */
	public void initOpenXCToOBD2Map() {
		File file = new File("resources\\openXCToOBD2Map.txt");
		try {
			Scanner scanner = new Scanner(file); //.useDelimiter("\n")
			while (scanner.hasNext()) {
				addKeyValuePair(scanner.nextLine());
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void addKeyValuePair(String line) {
		String[] nextLine = line.split("=");
		openXCToOBD2Map.put(nextLine[0], nextLine[1]);
	}

	/**
	 * 
	 * @param map
	 * @param value
	 * @return
	 */
	public <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (value.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * 
	 * @param input
	 *            the String to check
	 * @return true if hex or starts with AT primarily to check the Command
	 *         sending to ELM327
	 */
	public boolean checkIfHexOrAT(String input) {
		String rawData = input.replaceAll("\\s", "");
		return rawData.matches(TWO_HEX_BYTES) || (rawData.startsWith("AT"));
	}

	/**
	 * @param response
	 *            The raw response
	 * @return the as possible calculated and converted reply see
	 *         http://en.wikipedia.org/wiki/OBD-II_PIDs If percentage wanted: 2
	 *         Hex-Bytes means 00 - FF = 0 - 255. To get the percentage:
	 *         Data/255*100
	 */
	public String convertOBD2Response(String response) {
		Float result;
		int value2 = 0;
		if (!((response.replaceAll("\\s+", "")).matches(TWO_HEX_BYTES)) || response.length() < 6) {
			return response;
		} else {
			String[] data = response.split("\\s"); // (response =
			// "41 0C 0C FC") 41,0C,0C,FC, data[0] = Bus, data[1] = Command
			int value1 = Integer.parseInt(data[2], 16);
			if (data.length > 3){
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

	/**
	 * 
	 * @return ArrayList<String> with all supported 01 PIDs (e.g.
	 *         " 01 00"," 01 1F") Example: 41 00 98 18 00 01 =
	 *         10011000000110000000000000000001 41 20 00 01 80 01 =
	 *         00000000000000011000000000000001 41 40 C0 80 00 00 =
	 *         11000000100000000000000000000000 -> last Bit 0 -> no more PIDs =
	 *         all supported PIDs = 00,04,05,0C,0D,1F,2F,31,3F,40,41,(48)
	 *         [04,05,0C,0D,21,22,30,31,42,49]
	 * 
	 */
	public List<String> getSupportedPIDs() {
		List<String> pids = new ArrayList<String>();
		for (int i = 0; i < 8; i += 2) {
			String binaryPIDs = getBinaryPIDs(String.valueOf(i) + "0");
			if (binaryPIDs.length() > 1) {
				pids.addAll(getPIDs(binaryPIDs, i));
				if (binaryPIDs.charAt(binaryPIDs.length() - 1) == '0') {
					break;
				}
			}
		}
		return pids;
	}

	private Collection<? extends String> getPIDs(String binaryPIDs, int range) {
		List<String> pids = new ArrayList<String>();
		for (int i = 0; i < binaryPIDs.length(); i++) {
			if (binaryPIDs.charAt(i) == '1') {
				pids.add("01 " + intPositionInBinaryField2Hex(i, range));
			}
		}
//		doubleCheck(pids); //TODO ConcurrentModificationException..
		return pids;
	}

	/**
	 * 
	 * @param hexadecimalPIDs
	 * @return the given hex String to Binary String
	 */
	private String getBinaryPIDs(String range) {
		String hexadecimalPIDs = getHexadecimalPIDs(range);
		String binaryPIDs = "";
		if (hexadecimalPIDs.matches(TWO_HEX_BYTES)) {
			binaryPIDs = new BigInteger(hexadecimalPIDs, 16).toString(2);
			while (binaryPIDs.length() < hexadecimalPIDs.length()) {
				// fill leading Zeros
				binaryPIDs = "0" + binaryPIDs;
			}
		}
		return binaryPIDs;
	}

	/**
	 * 
	 * @param i position in binary field
	 * @return hex value of that position
	 */
	private String intPositionInBinaryField2Hex(int i, int range) {
		String hex = Integer.toHexString(i + 1); //TODO change to i ?
		hex = hex.toUpperCase();
		if (hex.length() == 1) {
			//F -> 0F, or F-> 2F
			hex = Integer.toString(range) + hex; 
		} else if (hex.length() == 2){
			//1F -> 3F
			hex = Integer.toString(range+1) + hex.charAt(1);
		}
		return hex;
	}

	/**
	 * Get the raw pids, extract the data itself, cuts the whitespaces and
	 * returns it
	 * 
	 * @param range
	 *            e.g. 20 --> 01 20
	 * @return the supported 01 PIDs for the given range
	 */
	private String getHexadecimalPIDs(String range) {
		String rawResponse = getRawPIDs(range);
		// delete whitespaces
		rawResponse = rawResponse.replaceAll("\\s+", "");
		// cut the first 4 Bytes
		rawResponse = rawResponse.substring(4);
		return rawResponse;
	}

	/**
	 * Gets the raw supported PIDs e.g. 01 00, 01 20, 01 40..
	 * 
	 * @param range
	 * @return
	 */
	private String getRawPIDs(String range) {
		String rawResponse = "";
		if (usedConnection == BLUETOOTH) {
			rawResponse = elmBluetooth.run("01 " + range);
		} else if (usedConnection == RS232) {
			rawResponse = elmRS232.run("01 " + range);
		}
		return rawResponse;
	}

	/**
	 * Checks whether PIDs really give a correct response.
	 * 
	 * @param pids
	 */
	private void doubleCheck(List<String> pids) {
		String doubleCheck;
		for (String pid : pids) {
			doubleCheck = run(pid);
			if (!doubleCheck.matches(TWO_HEX_BYTES)) {
				pids.remove(pid);
			}
		}
	}

	public void init() throws IOException {
		elmBluetooth.init();
	}
	
	public void init(String portname) throws IOException {
		elmRS232.init(portname);
	}

	/**
	 * runs the given Command using the predefined ELM-Connection
	 * 
	 * @param cmd
	 * @return
	 */
	public String run(String cmd) {
		String response = "";
		
		if(cmd != null){
			if(openXCToOBD2Map.contains(cmd)){ //PID or AT Command directly 
				if (usedConnection == BLUETOOTH || cmd.startsWith("AT")) {
					response = elmBluetooth.run(cmd);
				} else if (usedConnection == RS232) {
					response = elmRS232.run(cmd);
				}
				return response;
			} else if (openXCToOBD2Map.containsKey(cmd)){ //openXCKey
				if (usedConnection == BLUETOOTH) {
					response = elmBluetooth.run(openXCToOBD2Map.get(cmd));
				} else if (usedConnection == RS232) {
					response = elmRS232.run(openXCToOBD2Map.get(cmd));
				}
				return response;
			} else {
				return "Please enter a vaild Command!";
			}
		} else {
			return "no Command entered!";
		}
	}
}