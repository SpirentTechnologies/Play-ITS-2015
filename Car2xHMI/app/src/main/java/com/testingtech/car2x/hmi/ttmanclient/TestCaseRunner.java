package com.testingtech.car2x.hmi.ttmanclient;

import android.util.Log;

import com.testingtech.car2x.hmi.Globals;
import com.testingtech.car2x.hmi.TestRunnerActivity;
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
      InetAddress clientIp = getOwnIp();
      TestRunnerActivity.writeLog("TESTCASERUNNER: Host address " + clientIp.getHostAddress());

      final String user = "user";
      final String password = "password";
      final Credentials credentials = new Credentials(user, password);

      this.client = new ExecutionServerFactory().createClient(clientIp, Globals.clientPort);
      this.client.connect(Globals.serverIp, Globals.serverPort, credentials, this.notificationHandler);
  }

    private InetAddress getOwnIp(){
        try{
            Enumeration<NetworkInterface> enumNetwork = NetworkInterface.getNetworkInterfaces();
            while(enumNetwork.hasMoreElements()) {
                NetworkInterface netInterface = enumNetwork.nextElement();
                if (netInterface.getName().equalsIgnoreCase("eth0") ||
                        netInterface.getName().equalsIgnoreCase("wlan0")) {
                    Enumeration<InetAddress> enumIpAddr = netInterface.getInetAddresses();
                    while(enumIpAddr.hasMoreElements()) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if(!inetAddress.getHostAddress().contains(":")) {
                            return inetAddress;
                        }
                    }
                }
            }
        } catch (SocketException se) {
            se.printStackTrace(TestRunnerActivity.writer);
            TestRunnerActivity.writer.flush();
        }
        return null;
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

      TestRunnerActivity.writeLog("TESTCASERUNNER: Waiting for test case execution");
      boolean executionDone = false;
      while (! executionDone) {
        // sleep until server finished job execution
        // status updates are sent in the mean time through the handler interface
        try {
          execJob.join();
        } catch (InterruptedException e) {
            TestRunnerActivity.writeLog("TESTCASERUNNER:" + e.getMessage());
        }
        JobStatus jobStatus = execJob.getStatus();
        executionDone = (jobStatus == JobStatus.CANCELLED) || (jobStatus == JobStatus.FINISHED);
      }
        TestRunnerActivity.writeLog("TESTCASERUNNER: End of test case execution");

      String verdictLabel = execJob.getTestCaseStatus().getVerdictKind().getString();
      this.verdict = Utils.toTestCaseVerdict(verdictLabel);

    } catch (IOException ioex) {
        TestRunnerActivity.writeLog("TESTCASERUNNER: " + ioex.getMessage());
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
        TestRunnerActivity.writeLog("TESTCASERUNNER: " + ioex.getMessage());
    }
  }

}
