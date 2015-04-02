package com.testingtech.playits.canfilter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Hashtable;

import org.json.JSONException;

import com.testingtech.playits.canfilter.connector.ResourceConnector;

public class CANFilterService {

	private RequestProcessor requestProcessor;
	SocketUtils socketUtils = new SocketUtils();
	private ResourceConnector resourceConnector;
	CANFilterLog canFilterLog = CANFilterLog
			.getLog(OpenXCCANFilterService.class.getSimpleName());
	private Runnable valueUpdater;

	public CANFilterService(InetSocketAddress serverAddress,
			ResourceConnector resourceConnector,
			Hashtable<String, Car2XEntry> car2xEntries, Runnable valueUpdater)
			throws AddressInstantiationException, IOException, JSONException {
		requestProcessor = createRequestProcessor(serverAddress, car2xEntries);
		this.resourceConnector = resourceConnector;
		this.valueUpdater = valueUpdater;
	}

	private RequestProcessor createRequestProcessor(
			InetSocketAddress serverAddress,
			Hashtable<String, Car2XEntry> car2xEntries)
			throws AddressInstantiationException, IOException, JSONException {
		ServerSocket serverSocket = SocketUtils
				.createServerSocket(serverAddress);
		RequestProcessor requestProcessor = new RequestProcessor(serverSocket,
				car2xEntries);
		return requestProcessor;
	}

	public void startFilter() {
		try {
			Thread valueUpdaterThread = null;
			resourceConnector.connect();

			while (requestProcessor.hasMoreRequests()) {
				requestProcessor.processNextRequest();
				if (valueUpdaterThread == null || !valueUpdaterThread.isAlive()) {
					valueUpdaterThread = new Thread(valueUpdater);
					valueUpdaterThread.start();
				}
			}
		} catch (JSONException e) {
			canFilterLog.logError(FilterLogMessages.JSON_ERROR, e.getMessage());
		} catch (IOException e) {
			canFilterLog.logError(FilterLogMessages.SOCKET_ERROR,
					e.getMessage());
		}
		requestProcessor.shutdown();
		resourceConnector.disconnect();
	}
}
