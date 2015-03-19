/**
 * Connection service for the Android application used
 * by the tester.
 * Information is shared using a socket.
 *
 * TODO use proto buf to convert data before sending it
 * over streams.
 * TODO move output information to a logger after the prototype is done
 * TODO create a Communicator interface to support different means of communication.
 */
package com.testingtech.car2x.hmi.appserver;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import com.testingtech.car2x.hmi.messages.ControlMessage;
import com.testingtech.car2x.hmi.messages.Message;

public class SocketCommunicator {

  private final ServerSocket serverSocket;
  private Socket clientSocket;
  private ObjectInputStream ois;
  private ObjectOutputStream oos;

  /*
   * TODO change backlog to 1
   */
  public SocketCommunicator() throws IOException {
    this.serverSocket = new ServerSocket(30000, 5);
  }

  /**
   * Blocks until a request is received, then initializes
   * the input and output streams.
   *
   * The input stream is initialized first, as the constructor otherwise hangs
   * since it waits for the header of the associated output stream of the
   * client to arrive.
   */
  public void connectionInit() throws IOException {
    System.out.println("Waiting for connections");
    this.clientSocket = serverSocket.accept();
    System.out.println(
        "Received connection request from "
        + this.clientSocket.getInetAddress().getHostName()
    );
    this.ois = new ObjectInputStream(this.clientSocket.getInputStream());
    this.oos = new ObjectOutputStream(this.clientSocket.getOutputStream());
  }

  /**
   * Extracts client requests from the stream.
   * Only ControlMessage types are expected here.
   *
   * Method blocks while the stream is empty.
   */
  public ControlMessage getRequest() throws IOException, ClassNotFoundException {
    ControlMessage request = (ControlMessage) ois.readObject();
    System.out.println("Received from client: " + request);
    return request;
  }

  /**
   * Pushes messages to the client. Used to inform the client
   * on test case progress.
   */
  public void sendMessage(Message message) throws IOException {
    System.out.println("Sending to client: " + message);
    oos.writeObject(message);
    oos.flush();
  }

  public void closeConnService() throws IOException {
    this.clientSocket.close();
    this.serverSocket.close();
  }

}