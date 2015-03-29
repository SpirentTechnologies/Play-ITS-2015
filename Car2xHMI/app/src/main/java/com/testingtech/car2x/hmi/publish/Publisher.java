package com.testingtech.car2x.hmi.publish;

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
public class Publisher implements IPublisher {

  private final MessageFormat messageFormat;
  private final String progressFormatString;
  private final String verdictFormatString;
  private TestCase currentTestCase;

  public Publisher() {
    this.messageFormat = new MessageFormat("\"stage:{0},timeWindow:{1}\"");
    this.progressFormatString =
        "publishProgress: testCase[%s], testCaseProgress[%s], timeWindow[%d]";
    this.verdictFormatString = "publishVerdict: testCase[%s], testCaseVerdict[%s]";
  }

  public void setCurrentTestCase(TestCase testCase) {
    this.currentTestCase = testCase;
  }

  private boolean isCurrent(TestCase testCase) {
    if (currentTestCase == null) {
      TestRunnerActivity.writeLog("PUBLISHER: Current test case has not been set.");
    }
    return this.currentTestCase.compareTo(testCase) == 0;
  }

  @Override
  public void publishProgress(String testCaseName, String actionMessage) throws IOException {
    TestCase testCase = Utils.toTestCase(testCaseName);
    TestCaseProgress testCaseProgress = null;
    int timeWindow = -1;
    try {
      Object[] values = messageFormat.parse(actionMessage);
      int entryIndex = Integer.parseInt((String) values[0]);
      testCaseProgress = Utils.toTestCaseProgress(entryIndex);
      timeWindow = Integer.parseInt((String) values[1]);
    } catch (ParseException pex) {
        TestRunnerActivity.writeLog("PUBLISHER: MessageFormat error when parsing [" + actionMessage + "] " + pex.getMessage());
    }

    if (isCurrent(testCase) && testCase != null && testCaseProgress != null && timeWindow > -1) {
        TestRunnerActivity.writeLog(String.format(
          progressFormatString,
          testCase.name(),
          testCaseProgress.name(),
          timeWindow
      ));
      // Update GUI
        if(testCaseProgress.ordinal() > 1) {
            TestRunnerActivity.writeLog("Publishing progress");
            TestRunnerActivity.guiUpdater.updateProgressBar(testCaseProgress.ordinal());
            TestRunnerActivity.guiUpdater.scrollToStage(testCaseProgress.ordinal() - 1);
            TestRunnerActivity.speakStageText(testCaseProgress.ordinal() - 1);
        }
    } else {
        TestRunnerActivity.writeLog("Validation failed...");
    }


  }

  @Override
  public void publishVerdict(String testCaseName, String verdictLabel) throws IOException {
      TestCase testCase = Utils.toTestCase(testCaseName);
      TestCaseVerdict testCaseVerdict = Utils.toTestCaseVerdict(verdictLabel);

      if (isCurrent(testCase) && testCase != null && testCaseVerdict != null) {
          TestRunnerActivity.writeLog(String.format(
                  verdictFormatString,
                  testCase.name(),
                  testCaseVerdict.name()
          ));
          // Update GUI
          TestRunnerActivity.writeLog("Publishing verdict");
          TestRunnerActivity.guiUpdater.updateProgressBar(99);  // 99 for max value
          TestRunnerActivity.guiUpdater.resetTestRunnerGui();
      } else {
          TestRunnerActivity.writeLog("Validation failed...");
      }
  }

}
