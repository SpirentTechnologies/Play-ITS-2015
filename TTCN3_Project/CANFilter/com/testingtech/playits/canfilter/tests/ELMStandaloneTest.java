package com.testingtech.playits.canfilter.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.testingtech.playits.canfilter.Elm327;


public class ELMStandaloneTest{
	public static void main(String[] args) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		Elm327 elm327 = new Elm327();
		System.out.println("Please choose:");
		System.out.println("[1] = Bluetooth  [2] = RS232  [3] = Exit");
		String command = br.readLine(); 
		switch (command) {
		case "1":
			elm327.usedConnection = elm327.BLUETOOTH;
			System.out.println("You choosed Bluetooth");
			elm327.init();
			System.out.println("Bluetooth Init takes about 20s..");
			break;
		case "2":
			elm327.usedConnection = elm327.RS232;
			System.out.println("You choosed RS232, please enter the used COM-Port(like COM3) or press Enter");
			String portname = br.readLine();
			if (portname.startsWith("COM")){
				elm327.init(portname);
			} else {
				elm327.init("");
			}
			break;
			
		case "3":
			return;
		default:
			break;
		}
		
		System.out.println("now displaying all converted supported PIDs:");
		elm327.initOpenXCToOBD2Map();
		List<String> supportedPids = elm327.getSupportedPIDs();
		for (String hexPids : supportedPids) {
			System.out.println(elm327.getKeyByValue(elm327.openXCToOBD2Map,hexPids));
		}
		
		System.out.println("Please enter one of the Strings above");
		System.out.println("To Exit simply enter 'EXIT");
		System.out.println("If there are Problems with ELM, run the Command AT Z");
		while(true){
			command = br.readLine(); 
			if (command.equals("EXIT")) {
				return;
			} else  {
				if (command.startsWith("01 ")){
					System.out.println("Entered Command: " + command + " = as OpenXCKey: " + elm327.getKeyByValue(elm327.openXCToOBD2Map, command));
				} else {
					System.out.println("Entered Command: " + command + " = as PID: " + elm327.openXCToOBD2Map.get(command));
				}
				String response = elm327.run(command);
				System.out.println("Response: " + response + " = Converted : " + elm327.convertOBD2Response(response));
			}
		}
		
	}
}