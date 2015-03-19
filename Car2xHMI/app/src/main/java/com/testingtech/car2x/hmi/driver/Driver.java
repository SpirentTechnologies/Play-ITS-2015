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

import com.testingtech.car2x.hmi.appserver.AppService;

public class Driver {

  public static void start() {

    System.out.println("Starting app thread");
    Thread appThread = new Thread(new AppService());
    appThread.start();
    System.out.println("App thread is in state: " + appThread.getState());

  }

}