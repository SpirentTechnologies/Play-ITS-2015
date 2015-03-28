package com.testingtech.car2x.hmi.publish;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

import com.testingtech.car2x.hmi.Globals;
import com.testingtech.car2x.hmi.R;
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
      Log.e("PUBLISHER", "Current test case has not been set.");
    }
    return this.currentTestCase.compareTo(testCase) == 0;
  }

  @Override
  public void publishProgress(String testCaseName, String actionMessage) throws IOException {
    System.out.println("PUBLISH PROGRESS!");
    TestCase testCase = Utils.toTestCase(testCaseName);
    TestCaseProgress testCaseProgress = null;
    int timeWindow = -1;
    try {
      Object[] values = messageFormat.parse(actionMessage);
      int entryIndex = Integer.parseInt((String) values[0]);
      testCaseProgress = Utils.toTestCaseProgress(entryIndex);
      timeWindow = Integer.parseInt((String) values[1]);
    } catch (ParseException pex) {
      Log.e("PUBLISHER", "MessageFormat error when parsing [" + actionMessage + "] ", pex);
    }

    if (isCurrent(testCase) && testCase != null && testCaseProgress != null && timeWindow > -1) {
      System.out.println(String.format(
          progressFormatString,
          testCase.name(),
          testCaseProgress.name(),
          timeWindow
      ));
      // Update GUI here
        new Test(2).execute();
    }

  }

  @Override
  public void publishVerdict(String testCaseName, String verdictLabel) throws IOException {
    TestCase testCase = Utils.toTestCase(testCaseName);
    TestCaseVerdict testCaseVerdict = Utils.toTestCaseVerdict(verdictLabel);

    if (isCurrent(testCase) && testCase != null && testCaseVerdict != null) {
      System.out.println(String.format(
          verdictFormatString,
          testCase.name(),
          testCaseVerdict.name()
      ));
      // Update GUI here
    }
  }

    class Test extends AsyncTask<Void, Integer, Void>{

        private int num;

        public Test(int num){
            this.num = num;
        }

        @Override
        protected Void doInBackground(Void... p){
            publishProgress(num);
            return null;
        }

        protected void onProgressUpdate(Integer... h){
            ProgressBar p = (ProgressBar) Globals.view.findViewById(R.id.progressbar);
            p.setProgress(num / 5);
            super.onProgressUpdate();
        }
    }


}
