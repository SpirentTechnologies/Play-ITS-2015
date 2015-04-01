package com.testingtech.playits.canfilter.bluetooth;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Hashtable;

import org.json.JSONException;

import com.testingtech.playits.canfilter.AddressInstantiationException;
import com.testingtech.playits.canfilter.CANFilterLog;
import com.testingtech.playits.canfilter.CANFilterService;
import com.testingtech.playits.canfilter.Car2XEntry;
import com.testingtech.playits.canfilter.FilterLogMessages;
import com.testingtech.playits.canfilter.OpenXCCANFilterService;
import com.testingtech.playits.canfilter.SocketUtils;
import com.testingtech.playits.canfilter.valueupdater.OBD2ValueUpdater;

public class BluetoothCANFilterService {

	static CANFilterLog canFilterLog = CANFilterLog
			.getLog(OpenXCCANFilterService.class.getSimpleName());
	static Hashtable<String, Car2XEntry> car2xEntries = new Hashtable<>();

	public static void main(String[] args) {
		try {
			InetSocketAddress serverAddress = SocketUtils.createAddress(
					args[0], args[1]);
			ELMBluetoothConnector elmBluetoothConnector = new ELMBluetoothConnector(
					args[2]);
			OBD2ValueUpdater obd2ValueUpdater = new OBD2ValueUpdater(
					elmBluetoothConnector, car2xEntries);
			new CANFilterService(serverAddress, elmBluetoothConnector,
					car2xEntries, obd2ValueUpdater).startFilter();
		} catch (AddressInstantiationException e) {
			canFilterLog.logError(e.getMessage());
		} catch (IOException e) {
			canFilterLog.logError(FilterLogMessages.SOCKET_ERROR, e.getMessage());
		} catch (JSONException e) {
			canFilterLog.logError(FilterLogMessages.JSON_ERROR);
		}
	}

}
