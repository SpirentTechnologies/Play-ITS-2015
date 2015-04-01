package com.testingtech.playits.canfilter.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Scanner;

import com.testingtech.playits.canfilter.CANFilterLog;
import com.testingtech.playits.canfilter.Car2XEntry;
import com.testingtech.playits.canfilter.FilterLogMessages;
import com.testingtech.playits.canfilter.SocketUtils;

/**
 * Connects to the openXC simulator and updates values as long as there are
 * updated requested (at least one entry exists).
 */
public class OpenXCResourceConnector implements ResourceConnector {

	private static final String ASCII_0 = String.valueOf((char) 0);
	private CANFilterLog canFilterLog = CANFilterLog.getLog(OpenXCResourceConnector.class.getSimpleName());
	Socket socket = new Socket();
	private Scanner scanner;
	private InetSocketAddress address;

	public OpenXCResourceConnector(InetSocketAddress address,
			Hashtable<String, Car2XEntry> car2xEntries) throws IOException {
		this.address = address;
	}

	@Override
	public void disconnect() {
		canFilterLog.logInfo(FilterLogMessages.SOCKET_CLOSE);
		SocketUtils.close(scanner);
		SocketUtils.close(socket);
	}

	@Override
	public void connect() throws IOException {
		socket.connect(address);
		canFilterLog.logInfo(FilterLogMessages.OPENXC_CONNECTION,
				address.getHostName(), String.valueOf(address.getPort()));
		scanner = createScanner();
	}
	
	private Scanner createScanner() {
		Scanner scanner = null;
		try {
			scanner = new Scanner(socket.getInputStream())
			.useDelimiter(ASCII_0);
		} catch (IOException ioe) {
			canFilterLog.logError(FilterLogMessages.SOCKET_CONNECT,
					ioe.getMessage());
		}
		return scanner;
	}

	public Scanner getScanner() {
		return scanner;
	}
}
