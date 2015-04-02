package com.testingtech.playits.canfilter.connector;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class Elm327Connector implements ResourceConnector {
	private static final String TWO_HEX_BYTES = "([0-9A-F]{2})+";
	private List<String> supportedPIDs;
	private boolean isConnected;

	@Override
	public void connect() throws IOException {
		isConnected = true;
		supportedPIDs = getSupportedPIDs();
	}
	
	@Override
	public void disconnect() {
		isConnected = true;
	}
	
	public boolean isConnected() {
		return isConnected;
	}
	
	public boolean isSupported(String obd2cmd) {
		return supportedPIDs.contains(obd2cmd);
	}
	
	public abstract String run(String cmd); 
	
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
		String rawResponse = run("01 " + range);
		// delete whitespaces
		rawResponse = rawResponse.replaceAll("\\s+", "");
		// cut the first 4 Bytes
		rawResponse = rawResponse.substring(4);
		return rawResponse;
	}

}