package com.testingtech.playits.canfilter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Hashtable;

import org.json.JSONException;

import com.testingtech.playits.canfilter.connector.OpenXCResourceConnector;

public class OpenXCCANFilterService implements CANFilterService {
	
  private CANFilterLog canFilterLog = new CANFilterLog(
      OpenXCCANFilterServiceMain.class.getSimpleName());
  SocketUtils socketUtils = new SocketUtils();
  private Hashtable<String, Car2XEntry> car2xEntries = new Hashtable<>();
  private RequestProcessor requestProcessor;
  private OpenXCResourceConnector resourceConnector;

  public OpenXCCANFilterService(String... args)
      throws AddressInstantiationException, IOException, JSONException {
    if (args.length < 4) {
      canFilterLog.logError(FilterLogMessages.WRONG_CMD_LINE_USAGE);
      throw new AddressInstantiationException(
          "Wrong number of arguments.");
    }
    createRequestProcessor(args[0], args[1]);
    createResourceConnector(args[2], args[3]);
  }

  private void createRequestProcessor(String... args)
      throws AddressInstantiationException, IOException, JSONException {
    ServerSocket serverSocket = socketUtils.createServerSocket(args[0],
        args[1]);
    requestProcessor = new RequestProcessor(serverSocket, car2xEntries);
  }

  private void createResourceConnector(String... args)
      throws AddressInstantiationException {
    InetSocketAddress address = socketUtils.createAddress(args[0], args[1]);
    resourceConnector = new OpenXCResourceConnector(car2xEntries, address);
  }

  @Override
  public void startFilter() {
    Thread resourceConnectorThread = new Thread(resourceConnector);
    try {
      while (requestProcessor.hasMoreRequests()) {
        requestProcessor.processNextRequest();
        if (!resourceConnectorThread.isAlive())
          resourceConnectorThread.start();
      }
    } catch (JSONException e) {
      canFilterLog.logError(FilterLogMessages.JSON_ERROR, e.getMessage());
    } catch (IOException e) {
      canFilterLog.logError(FilterLogMessages.SOCKET_ERROR,
          e.getMessage());
    }
    resourceConnector.disconnect();
  }
}
