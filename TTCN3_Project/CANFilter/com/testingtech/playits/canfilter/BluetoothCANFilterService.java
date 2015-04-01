package com.testingtech.playits.canfilter;

import java.io.IOException;

import com.testingtech.playits.canfilter.bluetooth.ELMBluetooth;

public class BluetoothCANFilterService implements CANFilterService {

	private CANFilterLog canFilterLog = new CANFilterLog(
		      OpenXCCANFilterServiceMain.class.getSimpleName());
	private ELMBluetooth elmBluetooth;
	
	public BluetoothCANFilterService(String[] args) throws AddressInstantiationException {
		if (args.length < 3) {
			canFilterLog.logError(FilterLogMessages.WRONG_CMD_LINE_USAGE);
		      throw new AddressInstantiationException(
		          "Wrong number of arguments.");
		}
		elmBluetooth = new ELMBluetooth(args[2]); 
	}

	@Override
	public void startFilter() {
		try {
			elmBluetooth.init();
		} catch (IOException e) {
			canFilterLog.logError(FilterLogMessages.SOCKET_ERROR, e.getMessage());
		}
	}

}
