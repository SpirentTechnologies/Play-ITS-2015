package com.testingtech.playits.canfilter.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.testingtech.playits.canfilter.bluetooth.ELMBluetoothConnector;
import com.testingtech.playits.canfilter.rs232.ELMRS232Connector;
import com.testingtech.playits.canfilter.valueupdater.OBD2ValueUpdater;

public class ELMStandaloneTest {
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		ELMRS232Connector rs232Connector = new ELMRS232Connector(new ArrayList<String>());
		ELMBluetoothConnector bluetoothConnector = new ELMBluetoothConnector("COM4");
		OBD2ValueUpdater obd2ValueUpdater;
		System.out.println("Please choose:");
		System.out.println("[1] = Bluetooth  [2] = RS232  [3] = Exit");
		String command = br.readLine();
		switch (command) {
		case "1":
			System.out.println("You choose Bluetooth");
			System.out.println("Bluetooth Init takes about 20s..");
			bluetoothConnector.connect();
			break;
		case "2":
			System.out
					.println("You choose RS232, please enter the used COM-Port(like COM3) or press Enter");
			String portname = br.readLine();
			if (portname.startsWith("COM")) {
				rs232Connector.connect();
			}		
			break;

		case "3":
			return;
		default:
			return;
		}
		System.out.println("now displaying all converted supported PIDs:");
//		elm327.initOpenXCToOBD2Map();
//		List<String> supportedPids = elm327.getSupportedPIDs();
//		for (String hexPids : supportedPids) {
//			System.out.println(elm327.getKeyByValue(elm327.openXCToOBD2Map,
//					hexPids) + " = " + hexPids);
//		}

		System.out.println("Please enter one of the Strings above");
		System.out.println("To Exit simply enter 'EXIT");
		System.out
				.println("If there are Problems with ELM, run the Command AT Z");
		while (true) {
			command = br.readLine();
			if (command.equals("EXIT")) {
				return;
			} else {
				if (command.startsWith("01")) {
//					System.out.println("Entered Command: "
//							+ command
//							+ " = as OpenXCKey: "
//							+ elm327.getKeyByValue(elm327.openXCToOBD2Map,
//									command));
				} else {
//					System.out.println("Entered Command: " + command
//							+ " = as PID: "
//							+ elm327.openXCToOBD2Map.get(command));
				}
//				String response = elm327.run(command);
//				System.out.println("Response: " + response + " = Converted : "
//						+ obd2ValueUpdater.calculateInput(response));
			}
		}

	}
}