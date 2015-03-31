package com.testingtech.playits.canfilter.connector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import com.testingtech.playits.canfilter.Car2XEntry;
import com.testingtech.playits.canfilter.bluetooth.ELMBluetooth;
import com.testingtech.playits.canfilter.rs232.ELMRS232;
import com.testingtech.playits.canfilter.valueupdater.OBD2ValueUpdater;

public class Elm327Connector implements ResourceConnector {
	private static final String TWO_HEX_BYTES = "([0-9A-F]{2})+";
	public final int RS232 = 0;
	public final int BLUETOOTH = 1;
	public int usedConnection;

	public Hashtable<String, String> openXCToOBD2Map = new Hashtable<String, String>();

	public Hashtable<String, Car2XEntry> car2xEntries = new Hashtable<>();
	private ELMBluetooth elmBluetooth;
	private ELMRS232 elmRS232;
	private boolean isDisconnected;

	public Elm327Connector(Hashtable<String, Car2XEntry> car2xEntries) {
		this.car2xEntries = car2xEntries;
		initOpenXCToOBD2Map();
		elmBluetooth = new ELMBluetooth();
		elmRS232 = new ELMRS232();
	}
	
	@Override
	public void run() {
		OBD2ValueUpdater obd2ValueUpdater = new OBD2ValueUpdater(car2xEntries);
		try {
			init(); // or init(...);
			// TODO getSupportedPIDs();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		while(!isDisconnected) {
		  // TODO start periodic run() command
			Enumeration<String> openXCkeys = car2xEntries.keys();
			while (openXCkeys.hasMoreElements()) {
				String openXCkey = openXCkeys.nextElement();
				String obd2Cmd = openXCToOBD2Map.get(openXCkey);
				String rawResponse = run(obd2Cmd);
				obd2ValueUpdater.updateEntry(openXCkey, rawResponse);
			}
		}
	}

	/**
	 * 
	 * @param cmd
	 * @return see http://en.wikipedia.org/wiki/OBD-II_PIDs adds possible
	 *         available PID Codes
	 */
	private void initOpenXCToOBD2Map() {
		File file = new File("resources\\openXCToOBD2Map.txt");
		try {
			Scanner scanner = new Scanner(file);
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
	 * 
	 * @return ArrayList<String> with all supported 01 PIDs (e.g.:) 00_20 = 98
	 *         18 00 01 -> 10011000000110000000000000000001 =
	 *         01,04,05,(12=)0C,(13=)0D, (32=)20 20_40 = 00 01 80 01 ->
	 *         00000000000000011000000000000001 = (16=)30,(17=)31,(32=)40 40_60
	 *         = C0 80 00 00 -> 11000000100000000000000000000000 =
	 *         (1=)41,(2=)42,(9=)49
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
	 * @param i
	 *            position in binary field
	 * @return hex value of that position
	 */
	private String intPositionInBinaryField2Hex(int i, int range) {
		String hex = Integer.toHexString(i + 1);
		hex = hex.toUpperCase();
		if (hex.length() == 1) {
			// F -> 0F, or F-> 2F
			hex = Integer.toString(range) + hex;
		} else if (hex.length() == 2) {
			// 1F -> 3F
			hex = Integer.toString(range + 1) + hex.charAt(1);
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

		if (cmd != null) {
			if (openXCToOBD2Map.contains(cmd)) { // PID or AT Command directly
				if (usedConnection == BLUETOOTH || cmd.startsWith("AT")) {
					response = elmBluetooth.run(cmd);
				} else if (usedConnection == RS232) {
					response = elmRS232.run(cmd);
				}
				return response;
			} else if (openXCToOBD2Map.containsKey(cmd)) { // openXCKey
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

	@Override
	public void disconnect() {
		isDisconnected = true;
	}
}