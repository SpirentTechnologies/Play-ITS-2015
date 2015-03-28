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

import com.testingtech.car2x.hmi.publish.Publisher;
import com.testingtech.car2x.hmi.testcase.TestCase;
import com.testingtech.car2x.hmi.ttmanclient.NotificationHandler;
import com.testingtech.car2x.hmi.ttmanclient.TestCaseRunner;

import java.io.IOException;

public class Driver implements Runnable{

  public void run() {

      try {
          Publisher publisher = new Publisher();
          NotificationHandler notificationHandler = new NotificationHandler(publisher);
          TestCaseRunner testCaseRunner = new TestCaseRunner(notificationHandler);

          testCaseRunner.setCurrentTestCase(TestCase.TC_VEHICLE_SPEED_SIMULATED);
          publisher.setCurrentTestCase(TestCase.TC_VEHICLE_SPEED_SIMULATED);

          new Thread(testCaseRunner).start();
      }catch(IOException ioe){
          Log.e("DRIVER", ioe.getMessage());
          ioe.printStackTrace();
      }


  }

}