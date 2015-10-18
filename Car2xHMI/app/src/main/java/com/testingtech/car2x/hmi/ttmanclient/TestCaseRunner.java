package com.testingtech.car2x.hmi.ttmanclient;

import com.testingtech.car2x.hmi.Globals;
import com.testingtech.car2x.hmi.Logger;
import com.testingtech.car2x.hmi.PropertyReader;
import com.testingtech.car2x.hmi.testcases.TestCaseVerdict;
import com.testingtech.car2x.hmi.testcases.Utils;
import com.testingtech.tworkbench.ttman.server.api.ExecuteTestCaseJob;
import com.testingtech.tworkbench.ttman.server.api.JobStatus;
import com.testingtech.tworkbench.ttman.server.impl.client.RemoteExecutionClient;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestCaseRunner implements Runnable {

    private RemoteExecutionClient client;
    private String currentTestCase;
    private TestCaseVerdict verdict;
    private DateFormat tlzDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private ExecuteTestCaseJob execJob;

    public void setCurrentTestCase(String testCase) {
        this.currentTestCase = testCase;
    }

    public TestCaseRunner(RemoteExecutionClient client) {
        this.client = client;
    }

    /**
     * Starts an execution job that runs a test case. Waits for this thread to complete and finally
     * sets the resulting verdict for later use.
     */

    private void saveTLZ(String dateTime) {
        try {
            Logger.writeLog("TESTCASERUNNER: Saving TLZ");
//            File tlzPath = Globals.mainActivity.getFilesDir();
            File tlzPath = Globals.mainActivity.getExternalFilesDir(null);
            client.saveLog(new File(tlzPath, MessageFormat.format("{0}_{1}.tlz", this.currentTestCase, dateTime)));
        } catch (IOException ioex) {
            Logger.writeLog("TESTCASERUNNER: Error while saving TLZ: " + ioex.getMessage());
        }
    }

    @Override
    public void run() {
        String dateTime = tlzDateFormat.format(new Date());
        try {
            String testCaseModule = PropertyReader.readProperty("ttw.testcase.module");
            execJob = this.client.executeTestCase(testCaseModule, this.currentTestCase, null);
            Logger.writeLog("TESTCASERUNNER: Waiting for test case execution");
            boolean executionDone = false;
            while (!executionDone) {
                // sleep until server finished job execution
                // status updates are sent in the mean time through the handler interface
                try {
                    execJob.join();
                } catch (InterruptedException e) {
                    Logger.writeLog("TESTCASERUNNER: Test case execution was interrupted: " + e.getMessage());
                }
                JobStatus jobStatus = execJob.getStatus();
                executionDone = (jobStatus == JobStatus.CANCELLED) || (jobStatus == JobStatus.FINISHED);
            }
            Logger.writeLog("TESTCASERUNNER: End of test case execution");
            String verdictLabel = execJob.getTestCaseStatus().getVerdictKind().getString();
            this.verdict = Utils.toTestCaseVerdict(verdictLabel);
        } catch (IOException ioex) {
            Logger.writeLog("TESTCASERUNNER: Error while executing test case: " + ioex.getMessage());
        } finally {
            saveTLZ(dateTime);
        }
    }

    public void stopExecution() {
        try {
            client.stopExecution(execJob);
        } catch (IOException ioex) {
            Logger.writeLog("TESTCASERUNNER: Error while stopping job execution. " + ioex.getMessage());
        }
    }
}
