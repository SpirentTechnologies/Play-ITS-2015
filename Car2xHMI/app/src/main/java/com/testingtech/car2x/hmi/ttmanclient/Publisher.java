package com.testingtech.car2x.hmi.ttmanclient;

import com.testingtech.car2x.hmi.Logger;
import com.testingtech.car2x.hmi.TestRunnerActivity;
import com.testingtech.car2x.hmi.testcase.TestCase;
import com.testingtech.car2x.hmi.testcase.TestCaseProgress;
import com.testingtech.car2x.hmi.testcase.TestCaseVerdict;
import com.testingtech.car2x.hmi.testcase.Utils;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;

/**
 * Maps TTman client notifications to user interface feedback.
 */
public class Publisher {

  private final MessageFormat messageFormat;
  private final String progressFormatString;
  private final String verdictFormatString;
  private String currentTestCase;

  public Publisher() {
    this.messageFormat = new MessageFormat("\"stage:{0},timeWindow:{1}\"");
    this.progressFormatString =
        "publishProgress: testCase[%s], testCaseProgress[%s], timeWindow[%d]";
    this.verdictFormatString = "publishVerdict: testCase[%s], testCaseVerdict[%s]";
  }

  public void setCurrentTestCase(String testCase) {
    this.currentTestCase = testCase;
  }

  private boolean isCurrent(String testCase) {
    if (currentTestCase == null) {
      Logger.writeLog("PUBLISHER: Current test case has not been set.");
    }
    return this.currentTestCase.equals(testCase);
  }

  public void publishProgress(String testCaseName, String actionMessage) throws IOException {
    TestCaseProgress testCaseProgress = null;
    int timeWindow = -1;
    try {
      Object[] values = messageFormat.parse(actionMessage);
      int entryIndex = Integer.parseInt((String) values[0]);
      testCaseProgress = Utils.toTestCaseProgress(entryIndex);
      timeWindow = Integer.parseInt((String) values[1]);
    } catch (ParseException pex) {
        Logger.writeLog("PUBLISHER: MessageFormat error when parsing [" + actionMessage + "] " + pex.getMessage());
    }

    if (isCurrent(testCaseName) && testCaseName != null && testCaseProgress != null && timeWindow > -1) {
        Logger.writeLog(String.format(
                progressFormatString,
                testCaseName,
                testCaseProgress.name(),
                timeWindow
        ));
      // Update GUI
        if(testCaseProgress.ordinal() > 0) {
            Logger.writeLog("Publishing progress");
            TestRunnerActivity.guiUpdater.updateProgressBar(testCaseProgress.ordinal());
            TestRunnerActivity.guiUpdater.scrollToStage(testCaseProgress.ordinal() - 1);
            TestRunnerActivity.speakStageText(testCaseProgress.ordinal() - 1);
            Logger.writeLog("testCaseProgress: " + testCaseProgress.ordinal());
        }
    } else {
        Logger.writeLog("Validation failed...");
    }


  }

  public void publishVerdict(String testCaseName, String verdictLabel) throws IOException {
      TestCaseVerdict testCaseVerdict = Utils.toTestCaseVerdict(verdictLabel);

      if (isCurrent(testCaseName) && testCaseName != null && testCaseVerdict != null) {
          Logger.writeLog(String.format(
                  verdictFormatString,
                  testCaseName,
                  testCaseVerdict.name()
          ));
          // Update GUI
          Logger.writeLog("Publishing verdict");
          TestRunnerActivity.guiUpdater.updateProgressBar(99);  // 99 for max value
          TestRunnerActivity.guiUpdater.resetTestRunnerGui();
      } else {
          Logger.writeLog("Validation failed...");
      }
  }

}
