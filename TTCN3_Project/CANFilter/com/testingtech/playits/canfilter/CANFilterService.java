/*
 * ----------------------------------------------------------------------------
 *  (C) Copyright Testing Technologies.  All Rights Reserved.
 *
 *  All copies of this program, whether in whole or in part, and whether
 *  modified or not, must display this and all other embedded copyright
 *  and ownership notices in full.
 *
 *  See the file COPYRIGHT for details of redistribution and use.
 *
 *  You should have received a copy of the COPYRIGHT file along with
 *  this file; if not, write to the Testing Technologies,
 *  Michaelkirchstr. 17/18, 10179 Berlin, Germany.
 *
 *  TESTING TECHNOLOGIES DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS
 *  SOFTWARE. IN NO EVENT SHALL TESTING TECHNOLOGIES BE LIABLE FOR ANY
 *  SPECIAL, DIRECT, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 *  WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN
 *  AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
 *  ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 *  THIS SOFTWARE.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND,
 *  EITHER EXPRESSED OR IMPLIED, INCLUDING ANY KIND OF IMPLIED OR
 *  EXPRESSED WARRANTY OF NON-INFRINGEMENT OR THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.
 * ----------------------------------------------------------------------------
 */
package com.testingtech.playits.canfilter;

import java.io.IOException;
import java.lang.Thread.State;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Hashtable;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CANFilterService {

  private static final int DEFAULT_SERVER_PORT = 7070;
  private static final String DEFAULT_SERVER_HOST = "localhost";
  
  private static Hashtable<String, Car2XEntry> car2xEntries = new Hashtable<>();
  private static Car2xEntryUpdater entryUpdater;
  private static TimeoutResponder timeoutResponder;

  /**
   * If provided with host and port name, starts entry updater to update a 
   * hash table with openXC simulator values and timeout responder timer 
   * tasks to periodically send back updates values from said hash table.
   * @param args host, port
   */
  public static void main(String[] args) {
    String host = DEFAULT_SERVER_HOST;
    int port = DEFAULT_SERVER_PORT;
    
    if (args.length != 2) {
      System.err.println("Usage: java CANFilterService <Server host>  <Server Port Number>");
      System.err.println("  ... using default server host "+ DEFAULT_SERVER_HOST + " default server port " + DEFAULT_SERVER_PORT);
    }
    else {
      host = args[0];
      port = extractPortNumber(args, 1, DEFAULT_SERVER_PORT); 
    }
    
    InetSocketAddress address = new InetSocketAddress(host, port);

    Socket socket = null;
    ServerSocket serverSocket = null;
    Scanner scanner = null;
    timeoutResponder = new TimeoutResponder(socket, car2xEntries);
    try {
      serverSocket = createServerSocket(address);
      while (true) {
        socket = serverSocket.accept();
        scanner = new Scanner(socket.getInputStream());
        JSONObject request = new JSONObject(scanner.next());
        handleRequest(request);
        scanner.close();
      }

    } catch (IOException ioe) {
      System.err.println("Error listening on port: " + address.getPort()
          + ": " + ioe);
    } catch (JSONException e) {
      e.printStackTrace();
    } finally {
      if (serverSocket != null) {
        try {
          serverSocket.close();
        } catch (IOException e) {
          System.err.println("Could not close server socket: " + e.getMessage());
        }
      }
      if (scanner != null) {
        scanner.close();
      }
    }
  }

  private static ServerSocket createServerSocket(InetSocketAddress address)
      throws IOException, SocketException {
    ServerSocket serverSocket = new ServerSocket();
    serverSocket.setReuseAddress(true);
    serverSocket.bind(address);
    System.out.println("TCP echo server listening on "
        + address.getHostName() + ":" + address.getPort());
    return serverSocket;
  }

  private static void handleRequest(JSONObject request) throws IOException,
      JSONException {
    JSONArray data = request.getJSONArray("reqData");
    switch (request.getString("reqType")) {
      case "start":
        addEntries(data);
        break;
      case "stop":
        removeEntries(data);
        break;
      default:
        System.err.println("Unsupported request");
        break;
    }
  }

  private static void addEntries(JSONArray jsonArray) throws JSONException {
    for (int index = 0; index < jsonArray.length(); index++) {
      JSONObject jsonObject = jsonArray.getJSONObject(index);
      addEntry(jsonObject.getString("key"), jsonObject.getInt("interval"));
    }
  }

  private static void addEntry(String key, int interval) {
    car2xEntries.put(key, new Car2XEntry());
    if (entryUpdater == null || entryUpdater.getState() != State.RUNNABLE) {
      entryUpdater = new Car2xEntryUpdater(car2xEntries);
      entryUpdater.start();
    }
    timeoutResponder.addTimer(key, interval);
  }

  private static void removeEntries(JSONArray jsonArray) throws JSONException {
    for (int index = 0; index < jsonArray.length(); index++) {
      removeEntry(jsonArray.getString(index));
    }
  }

  private static void removeEntry(String key) {
    car2xEntries.remove(key);
    timeoutResponder.removeTimer(key);
  }
  
  private static int extractPortNumber(String[] args, int pos, int defaultValue) {
    int portNumber = defaultValue;

    try {
      if (args.length > pos) {
        portNumber = Integer.parseInt(args[pos]);
      }
    } catch (NumberFormatException e) {
      System.err.println("Wrong port format (" + args[pos] + ") using " + defaultValue);
    }
    
    return portNumber;
  }
}
