package com.testingtech.playits.canfilter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Hashtable;

import org.json.JSONException;

import com.testingtech.playits.canfilter.connector.Elm327Connector;
import com.testingtech.playits.canfilter.connector.OpenXCResourceConnector;
import com.testingtech.playits.canfilter.connector.ResourceConnector;

public class CANFilterService {
  private boolean isUsingSimulator = true;
  private CANFilterLog canFilterLog = new CANFilterLog(
      CANFilterServiceMain.class.getSimpleName());
  SocketUtils socketUtils = new SocketUtils();
  private Hashtable<String, Car2XEntry> car2xEntries = new Hashtable<>();
  private String[] args;

  public CANFilterService(String... args) throws AddressInstantiationException {
    this.args = args;
    // host port simHost simPort true/false
    if (args.length == 5) {
      isUsingSimulator = Boolean.parseBoolean(args[4]);
    } else {
      canFilterLog.logError(FilterLogMessages.WRONG_CMD_LINE_USAGE);
      throw new AddressInstantiationException("Wrong number of arguments.");
    }
  }

  public void startFilter() throws AddressInstantiationException,
      JSONException, IOException {
    ServerSocket serverSocket = socketUtils.createServerSocket(args[0],
        args[1]);
    RequestProcessor requestProcessor = new RequestProcessor(serverSocket,
        car2xEntries);
    ResourceConnector resourceConnector = getResourceConnector();
    Thread resourceConnectorThread = new Thread(resourceConnector);
    while (requestProcessor.hasMoreRequests()) {
      requestProcessor.processNextRequest();
      if (!resourceConnectorThread.isAlive())
        resourceConnectorThread.start();
    }
    resourceConnector.disconnect();
  }

  private ResourceConnector getResourceConnector()
      throws AddressInstantiationException {
    ResourceConnector resourceConnector;
    if (isUsingSimulator) {
      InetSocketAddress address = socketUtils.createAddress(args[2],
          args[3]);
      resourceConnector = new OpenXCResourceConnector(car2xEntries,
          address);
    } else {
      resourceConnector = new Elm327Connector(car2xEntries);
    }
    return resourceConnector;
  }
}
