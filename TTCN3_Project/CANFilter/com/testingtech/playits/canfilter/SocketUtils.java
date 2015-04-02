package com.testingtech.playits.canfilter;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class SocketUtils {

  private static CANFilterLog canFilterLog = CANFilterLog.getLog(SocketUtils.class.getSimpleName());

  final static String HOST_PATTERN = "(localhost|(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[0-9]{1,2})(\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[0-9]{1,2})){3})";
  final static String PORT_NUMBER_PATTERN = "(6553[0-5]|655[0-2]\\d|65[0-4]\\d\\d|6[0-4]\\d{3}|[1-5]\\d{4}|[1-9]\\d{0,3}|0)";

  public static InetSocketAddress createAddress(String... args)
      throws AddressInstantiationException {
    String host;
    int portNumber;
    if (args.length != 2) {
      canFilterLog.logError(FilterLogMessages.WRONG_CMD_LINE_USAGE);
      throw new AddressInstantiationException("Wrong number of arguments");
    } else {
      if (args[0].matches(HOST_PATTERN)) {
        host = args[0];
      } else {
        throw new AddressInstantiationException("Invalid host name");
      }
      if (args[1].matches(PORT_NUMBER_PATTERN)) {
        portNumber = Integer.parseInt(args[1]);
      } else
        throw new AddressInstantiationException("Invalid port number");
    }
    return new InetSocketAddress(host, portNumber);
  }

  public static ServerSocket createServerSocket(InetSocketAddress address)
      throws AddressInstantiationException {
    ServerSocket serverSocket = null;
    try {
      serverSocket = new ServerSocket();
      serverSocket.setReuseAddress(true);
      serverSocket.bind(address);
      canFilterLog.logInfo(FilterLogMessages.START,
          address.getHostName(), String.valueOf(address.getPort()));
    } catch (IOException e) {
      canFilterLog.logError(FilterLogMessages.SOCKET_ERROR);
    }
    return serverSocket;
  }

  public static void close(Closeable socket) {
    try {
      if (socket != null) {
        socket.close();
      }
    } catch (IOException e) {
      canFilterLog.logError(FilterLogMessages.SOCKET_ERROR,
          e.getMessage());
    }
  }
}
