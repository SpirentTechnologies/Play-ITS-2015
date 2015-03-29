/**
 * HMI service application in charge of controlling test cases
 * via TTman and pushing event information (such as test case
 * progress) to the HMI client.
 *
 * Current variant does not communicate with the TTman server. Instead, it responds to the HMI
 * using pre-defined messages.
 *
 */
package com.testingtech.car2x.hmi.driver;

import android.util.Log;

import com.testingtech.car2x.hmi.TestRunnerActivity;
import com.testingtech.car2x.hmi.publish.Publisher;
import com.testingtech.car2x.hmi.testcase.TestCase;
import com.testingtech.car2x.hmi.ttmanclient.NotificationHandler;
import com.testingtech.car2x.hmi.ttmanclient.TestCaseRunner;

import java.io.IOException;

public class Driver implements Runnable{

    private TestCase testCase;

    public Driver(TestCase testCase){
        this.testCase = testCase;
    }

  public void run() {

      try {
          TestRunnerActivity.writeLog("DRIVER: Starting");
          Publisher publisher = new Publisher();
          NotificationHandler notificationHandler = new NotificationHandler(publisher);
          TestCaseRunner testCaseRunner = new TestCaseRunner(notificationHandler);

          testCaseRunner.setCurrentTestCase(testCase);
          publisher.setCurrentTestCase(testCase);

          new Thread(testCaseRunner).start();
      }catch(IOException ioe){
          ioe.printStackTrace(TestRunnerActivity.writer);
          TestRunnerActivity.writer.flush();
      }


  }

}