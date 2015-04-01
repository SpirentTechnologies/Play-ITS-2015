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

import com.testingtech.car2x.hmi.Globals;
import com.testingtech.car2x.hmi.Logger;
import com.testingtech.car2x.hmi.ttmanclient.Publisher;
import com.testingtech.car2x.hmi.ttmanclient.NotificationHandler;
import com.testingtech.car2x.hmi.ttmanclient.TestCaseRunner;

import java.io.IOException;

public class Driver implements Runnable {

    private Publisher publisher;
    private NotificationHandler notificationHandler;
    private TestCaseRunner testCaseRunner;
    private Thread thread;

    @Override
    public void run() {
        Logger.writeLog("DRIVER: Starting");
        try {
            publisher = new Publisher();
            notificationHandler = new NotificationHandler(publisher);
            testCaseRunner = new TestCaseRunner(notificationHandler);
            startTestCase();
        }catch(IOException ioe){
            ioe.printStackTrace(Logger.writer);
            Logger.writer.flush();
        }
    }

    public void startTestCase() {
        if(testCaseRunner != null) {
            testCaseRunner.setCurrentTestCase(Globals.currentTestCase);
            publisher.setCurrentTestCase(Globals.currentTestCase);
            Thread runnerThread = new Thread(testCaseRunner);
            runnerThread.start();
            Logger.writeLog("DRIVER: Test case started");
        } else{
            Logger.writeLog("Cannot start test case: testCaseRunner is null");
        };
    }

    public void interrupt() {
        if(thread != null) {
            thread.getThreadGroup().interrupt();
            thread.interrupt();
        }else{
            Logger.writeLog("Test could not be interrupted: thread is null");
        }
    }
}