package com.testingtech.car2x.hmi.controller;

import com.testingtech.car2x.hmi.appserver.SocketCommunicator;
import com.testingtech.car2x.hmi.messages.AckMessage;
import com.testingtech.car2x.hmi.messages.ProgressMessage;
import com.testingtech.car2x.hmi.messages.TestCaseCommand;
import com.testingtech.car2x.hmi.messages.TestCase;
import com.testingtech.car2x.hmi.messages.TestCaseProgress;
import com.testingtech.car2x.hmi.messages.TestCaseVerdict;
import com.testingtech.car2x.hmi.messages.VerdictMessage;
import java.io.IOException;
import java.util.Date;

/**
 * Maps incoming TTman client notifications to App client messages.
 *
 * TODO Too much code duplication. Move all pushXXX into one method and the toXXX into one as well.
 * TODO implement error messages along with exception handling
 */
public class Controller implements Push {

  private final SocketCommunicator socketComm;

  public Controller() throws IOException {
    this.socketComm = new SocketCommunicator();
  }

  @Override
  public void pushAcknowledgement(String testCaseName, String commandLabel) throws IOException {
    TestCase testCase = toTestCase(testCaseName);
    TestCaseCommand testCaseCommand = toTestCaseCommand(commandLabel);
    if (testCase != null && testCaseCommand != null) {
      socketComm.sendMessage(new AckMessage(
          testCase, new Date(), testCaseCommand
      ));
    }
  }

  @Override
  public void pushProgress(String testCaseName, String progressLabel) throws IOException {
    TestCase testCase = toTestCase(testCaseName);
    TestCaseProgress testCaseProgress = toTestCaseProgress(progressLabel);
    if (testCase != null && testCaseProgress != null) {
      socketComm.sendMessage(new ProgressMessage(
          testCase, new Date(), testCaseProgress
      ));
    }
  }

  @Override
  public void pushVerdict(String testCaseName, String verdictLabel) throws IOException {
    TestCase testCase = toTestCase(testCaseName);
    TestCaseVerdict testCaseVerdict = toTestCaseVerdict(verdictLabel);
    if (testCase != null && testCaseVerdict != null) {
      socketComm.sendMessage(new VerdictMessage(
          testCase, new Date(), testCaseVerdict
      ));
    }
  }

  private TestCase toTestCase(String testCaseName) {
    try {
      return TestCase.valueOf(testCaseName);
    } catch (IllegalArgumentException iae) {
      return null;
    }
  }

  private TestCaseCommand toTestCaseCommand(String commandLabel) {
    try {
      return TestCaseCommand.valueOf(commandLabel);
    } catch (IllegalArgumentException iae) {
      return null;
    }
  }

  private TestCaseProgress toTestCaseProgress(String progressLabel) {
    try {
      return TestCaseProgress.valueOf(progressLabel);
    } catch (IllegalArgumentException iae) {
      return null;
    }
  }

  private TestCaseVerdict toTestCaseVerdict(String verdictLabel) {
    try {
      return TestCaseVerdict.valueOf(verdictLabel);
    } catch (IllegalArgumentException iae) {
      return null;
    }
  }

}
