package com.testingtech.car2x.hmi.ttmanclient;

import android.util.Log;

import com.testingtech.car2x.hmi.Globals;
import com.testingtech.car2x.hmi.testcase.TestCase;
import com.testingtech.car2x.hmi.testcase.TestCaseVerdict;
import com.testingtech.car2x.hmi.testcase.Utils;
import com.testingtech.tworkbench.ttman.server.api.Credentials;
import com.testingtech.tworkbench.ttman.server.api.ExecuteTestCaseJob;
import com.testingtech.tworkbench.ttman.server.api.ExecutionServerFactory;
import com.testingtech.tworkbench.ttman.server.api.IExecutionServer;
import com.testingtech.tworkbench.ttman.server.api.JobStatus;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.io.IOException;
import java.net.InetAddress;

public class TestCaseRunner implements Runnable, ITestCaseRunner {

  private final NotificationHandler notificationHandler;
  private IExecutionServer client;
  private TestCase currentTestCase;
  private TestCaseVerdict verdict;

  public TestCaseRunner(NotificationHandler handler) throws IOException {
    this.notificationHandler = handler;
    this.initServerConnection();
    this.initTestSuite();
  }

  /**
   * Initialize TTman server connection using the file based configuration parameters.
   */
  private void initServerConnection() throws IOException {

      String clientAddress = "";
      final String clientPortString = "10280";
      final int clientPort = Integer.parseInt(clientPortString);

      final String serverAddress = "192.168.87.148";//Globals.serverIp;
      final String serverPortString = "10279";
      final int serverPort;

      try{
          Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
          while(en.hasMoreElements()) {
              NetworkInterface intf = en.nextElement();
              if (intf.getName().equalsIgnoreCase("eth0") ||
                      intf.getName().equalsIgnoreCase("wlan0")) {
                  Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                  while(enumIpAddr.hasMoreElements()) {
                      InetAddress inetAddress = enumIpAddr.nextElement();
                      String hostAddress = inetAddress.getHostAddress();
                      if(!hostAddress.contains(":")) {
                          clientAddress = hostAddress;
                      }
                  }
              }
          }
          Log.i("TESTCASERUNNER", "Host address " + clientAddress);
      } catch (SocketException se) {
          se.printStackTrace();
      }

      if (serverPortString == null) {
          serverPort = IExecutionServer.DEFAULT_SERVER_PORT_NUMBER;
      } else {
          serverPort = Integer.parseInt(serverPortString);
      }

      final String user = "user";
      final String password = "password";
      final Credentials credentials = new Credentials(user, password);

      this.client = new ExecutionServerFactory().
              createClient(InetAddress.getByName(clientAddress), clientPort);
      this.client.connect(serverAddress, serverPort, credentials, this.notificationHandler);
  }

  private void initTestSuite() throws IOException {
    final String testProject = "TTCN3_Project";
    final String testFile = "clf/Car2X_Testcases.clf";

    this.client.loadTestSuiteFromFile(testProject, testFile);
  }

  @Override
  public void setCurrentTestCase(TestCase testCase) {
    this.currentTestCase = testCase;
  }

  /**
   * Starts an execution job that runs a test case. Waits for this thread to complete and finally
   * sets the resulting verdict for later use.
   *
   */
  @Override
  public void run() {
    final ExecuteTestCaseJob execJob;
    try {
      String testCaseModule = "Car2X_Testcases";
      execJob = client.executeTestCase(testCaseModule, this.currentTestCase.name(), null);

      Log.i("TESTCASERUNNER", "Waiting for test case execution");
      boolean executionDone = false;
      while (! executionDone) {
        // sleep until server finished job execution
        // status updates are sent in the mean time through the handler interface
        try {
          execJob.join();
        } catch (InterruptedException e) {
          Log.e("TESTCASERUNNER", e.getMessage());
        }
        JobStatus jobStatus = execJob.getStatus();
        executionDone = (jobStatus == JobStatus.CANCELLED) || (jobStatus == JobStatus.FINISHED);
      }
      Log.i("TESTCASERUNNER", "End of test case execution");

      String verdictLabel = execJob.getTestCaseStatus().getVerdictKind().getString();
      this.verdict = Utils.toTestCaseVerdict(verdictLabel);

    } catch (IOException ioex) {
      Log.e("TESTCASERUNNER", ioex.getMessage());
    }
  }

  @Override
  public TestCaseVerdict getVerdict() {
    return this.verdict;
  }

  @Override
  public void closeServerConnection() {
    try {
      this.client.disconnect();
    } catch (IOException ioex) {
      Log.w("TESTCASERUNNER", ioex.getMessage());
    }
  }

}
