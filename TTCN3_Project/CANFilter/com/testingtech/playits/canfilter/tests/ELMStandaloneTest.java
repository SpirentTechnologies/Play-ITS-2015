package com.testingtech.playits.canfilter.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;

import com.testingtech.playits.canfilter.Car2XEntry;
import com.testingtech.playits.canfilter.bluetooth.ELMBluetoothConnector;
import com.testingtech.playits.canfilter.connector.Elm327Connector;
import com.testingtech.playits.canfilter.rs232.ELMRS232Connector;
import com.testingtech.playits.canfilter.valueupdater.OBD2ValueUpdater;

public class ELMStandaloneTest {
	private static Hashtable<String, Car2XEntry> car2xEntries = new Hashtable<String, Car2XEntry>();
	private static Hashtable<String, String> openXCToOBD2Map = new Hashtable<String, String>();

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		Elm327Connector connector;
//		ELMRS232Connector rs232Connector = new ELMRS232Connector(new ArrayList<String>());
//		ELMBluetoothConnector bluetoothConnector = new ELMBluetoothConnector("COM4");
		System.out.println("Please choose:");
		System.out.println("[1] = Bluetooth  [2] = RS232  [3] = Exit");
		String command = br.readLine();
		switch (command) {
		case "1":
			System.out.println("You choose Bluetooth");
			System.out.println("Bluetooth Init takes about 20s..");
			connector = new ELMBluetoothConnector("OBDII"); 
			connector.connect();
			break;
		case "2":
			System.out
					.println("You choose RS232, please enter the used COM-Port(like COM3) or press Enter");
			String portname = br.readLine();
			connector = new ELMRS232Connector(Arrays.asList(portname,"9600","0","8","1"));
			if (portname.startsWith("COM")) {
				connector.connect();
			}		
			break;

		case "3":
			return;
		default:
			return;
		}
		OBD2ValueUpdater obd2ValueUpdater = new OBD2ValueUpdater(connector, car2xEntries);
		initOpenXCToOBD2Map();
		System.out.println("now displaying all converted supported PIDs:");
		
		List<String> supportedPids = connector.getSupportedPIDs();
		for (String hexPid : supportedPids) {
			System.out.println(openXCToOBD2Map.get(hexPid)  + " = " + hexPid);
		}

		System.out.println("Please enter one of the Strings above");
		System.out.println("To Exit simply enter 'EXIT");
		System.out
				.println("If there are Problems with ELM, run the Command AT Z");
		while (true) {
			command = br.readLine();
			if (command.equals("EXIT")) {
				return;
			} else {
				if (command.startsWith("01 ")) {
					System.out.println("Entered Command: "
							+ command
							+ " = as OpenXCKey: "
							+ openXCToOBD2Map.get(command.substring(3)));
				}
				else {
					System.out.println("Entered Command: "
							+ command
							+ " = as OpenXCKey: "
							+ openXCToOBD2Map.get(command));
				}
					
				String response = connector.run(command);
				System.out.println("Response: " + response + " = Converted : "
						+ obd2ValueUpdater.calculateInput(response));
			}
		}

	}
	
	/**
	 * Initializes a bidirectional mapping from openXC key to obd2 key.
	 */
	private static void initOpenXCToOBD2Map() {
		File file = new File("resources\\openXCToOBD2Map.txt");
		try {
			Scanner scanner = new Scanner(file);
			while (scanner.hasNext()) {
				addKeys(scanner.nextLine().split("="));
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void addKeys(String[] keys) {
		String openXCkey = keys[0];
		String obd2key = keys[1];
		openXCToOBD2Map.put(openXCkey, obd2key);
		openXCToOBD2Map.put(obd2key, openXCkey);
	}
}
