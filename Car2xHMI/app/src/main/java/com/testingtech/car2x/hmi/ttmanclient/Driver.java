/**
 * HMI service application in charge of controlling test cases
 * via TTman and pushing event information (such as test case
 * progress) to the HMI client.
 *
 * Current variant does not communicate with the TTman server. Instead, it responds to the HMI
 * using pre-defined messages.
 *
 */
package com.testingtech.car2x.hmi.ttmanclient;

import com.testingtech.car2x.hmi.Logger;
import com.testingtech.car2x.hmi.ttmanclient.Publisher;
import com.testingtech.car2x.hmi.ttmanclient.NotificationHandler;
import com.testingtech.car2x.hmi.ttmanclient.TestCaseRunner;

import java.io.IOException;

public class Driver {

    private Publisher publisher;
    private NotificationHandler notificationHandler;
    private TestCaseRunner testCaseRunner;
    private static Thread thread;

    public Driver(String testCase){
        Logger.writeLog("DRIVER: Starting");
        try {
            publisher = new Publisher();
            notificationHandler = new NotificationHandler(publisher);
            testCaseRunner = new TestCaseRunner(notificationHandler);
            start(testCase);
        }catch(IOException ioe){
            ioe.printStackTrace(Logger.writer);
            Logger.writer.flush();
        }
    }

    public void start(String testCase) {
        if(testCaseRunner != null) {
            testCaseRunner.setCurrentTestCase(testCase);
            publisher.setCurrentTestCase(testCase);
            thread = new Thread(testCaseRunner);
            thread.start();
            Logger.writeLog("DRIVER: Started");
        } else{
            Logger.writeLog("Test case could not be started: testCaseRunner is null");
        }
    }

    public void interrupt() {
        thread.getThreadGroup().interrupt();
        thread.interrupt();
    }

}