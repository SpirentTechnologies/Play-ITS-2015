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

import java.io.Closeable;
import java.io.IOException;
import java.lang.Thread.State;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class CANFilterService {

  private static final int DEFAULT_SERVER_PORT = 7000;
  private static final String DEFAULT_SERVER_HOST = "localhost";

  private static Hashtable<String, Car2XEntry> car2xEntries = new Hashtable<>();
  private static Thread entryUpdaterThread;
  private static Car2XEntryUpdater entryUpdater;
  private static TimeoutResponder timeoutResponder;
  static CANFilterLog filterLog = new CANFilterLog();

  /**
   * Provides TCP server functionality to handle JSON requests. Upon receiving
   * a start request, an entry updater thread which updates openXC / obd2 hash
   * table entries is started. Parallel timeout responder tasks periodically
   * send back updated JSON values from the hash table over a client socket.
   * 
   * @param args
   *            host, port
   */
  public static void main(String[] args) {
    String host;
    int port;
    
    if (args.length != 2) {
      filterLog.logCommandLineUsage();
      host = DEFAULT_SERVER_HOST;
      port = DEFAULT_SERVER_PORT;
    } else {
      host = PatternMatchUtils.matchHost(args[0], DEFAULT_SERVER_HOST);
      port = PatternMatchUtils.matchPortNumber(args[1], DEFAULT_SERVER_PORT);
    }

    InetSocketAddress address = new InetSocketAddress(host, port);
    Socket socket = null;
    ServerSocket serverSocket = null;
    try {
      serverSocket = createServerSocket(address);
      filterLog.logStartListening(serverSocket.getInetAddress().getHostName(), serverSocket.getLocalPort());
      
      socket = serverSocket.accept();
      timeoutResponder = new TimeoutResponder(socket, car2xEntries);
      entryUpdater = new Car2XEntryUpdater(car2xEntries);
      JSONTokener jsonTokener = new JSONTokener(socket.getInputStream());
      while (jsonTokener.more()) {
        try {
          Object nextValue = jsonTokener.nextValue();
          if (nextValue instanceof JSONObject) {
            filterLog.logIncomingRequest(nextValue.toString());
            // TODO maybe add a kill request to exit loop
            handleRequest((JSONObject) nextValue);
          } else {
            filterLog.logJSONError(nextValue.toString());
          }
        } catch (JSONException e) {
          filterLog.logJSONError(e.getMessage());
        }
      }

    } catch (IOException e) {
      filterLog.logSocketError(e.getMessage());
    } catch (JSONException e) {
      filterLog.logJsonErrror();
    } finally {
      filterLog.logNoMoreRequests();
      close(socket);
      close(serverSocket);
    }
  }

  private static void close(Closeable socket) {
    try {
      if (socket != null) {
        socket.close();
      }
    } catch (IOException e) {
      filterLog.logSocketClose(e.getMessage());
    }
  }

  private static ServerSocket createServerSocket(InetSocketAddress address)
      throws IOException, SocketException {
    ServerSocket serverSocket = new ServerSocket();
    serverSocket.setReuseAddress(true);
    serverSocket.bind(address);
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
      filterLog.logUnsupportedRequest();
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
    filterLog.logEntryAddition(key, interval);
    car2xEntries.put(key, new Car2XEntry());

    if (entryUpdaterThread == null
        || entryUpdaterThread.getState() != State.RUNNABLE) {
      filterLog.logStartEntryUpdater();
      entryUpdaterThread = new Thread(entryUpdater);
      entryUpdaterThread.start();
    }
    timeoutResponder.addTimer(key, interval);
  }

  private static void removeEntries(JSONArray jsonArray) throws JSONException {
    for (int index = 0; index < jsonArray.length(); index++) {
      JSONObject jsonObject = jsonArray.getJSONObject(index);
      removeEntry(jsonObject.getString("key"));
    }
  }

  private static void removeEntry(String key) {
    filterLog.logEntryRemoval(key);
    car2xEntries.remove(key);
    timeoutResponder.removeTimer(key);
  }
}
