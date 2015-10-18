package com.testingtech.car2x.hmi.ttmanclient;

import com.testingtech.tworkbench.ttman.server.api.IExecutionHandler;
import com.testingtech.tworkbench.ttman.server.api.ITERequest;
import com.testingtech.tworkbench.ttman.server.api.ITEResponse;
import com.testingtech.tworkbench.ttman.server.api.Job;
import com.testingtech.tworkbench.ttman.server.api.Parameter;
import com.testingtech.tworkbench.ttman.server.api.TestCase;
import com.testingtech.tworkbench.ttman.server.api.TestCaseStatus;
import com.testingtech.tworkbench.ttman.server.impl.StatusImpl;
import com.testingtech.tworkbench.ttman.server.impl.TEMessageRequest;
import com.testingtech.tworkbench.ttman.server.impl.TEResponse;

import java.io.IOException;
import java.util.List;

public final class NotificationHandler implements IExecutionHandler {

    private Publisher publisher;
    private String testCaseName;

    public NotificationHandler(Publisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public final void testCaseFinished(final Job job, final TestCase testCase,
                                       final TestCaseStatus testCaseStatus, final List<Parameter> parameter)
            throws IOException {
        final String currentTestCaseName = testCase.getName();
        final String verdict = testCaseStatus.getVerdictKind().getString();
        this.publisher.publishVerdict(currentTestCaseName, verdict);
    }

    /**
     * Notifies that the test case has started by return the stage string value of 0, denoting that
     * the test case is in the initialization stage.
     */
    @Override
    public final void testCaseStarted(final Job job, final TestCase testCase,
                                      final List<Parameter> parameter) throws IOException {
        this.testCaseName = testCase.getName();
        this.publisher.publishProgress(this.testCaseName, "\"stage:0,timeWindow:0\"");
    }

    @Override
    public final ITEResponse teRequest(final ITERequest request) throws IOException {
        switch (request.getKind()) {
            case message: {
                final String message = ((TEMessageRequest) request).getMessage();
                this.publisher.publishProgress(this.testCaseName, message);
                return new TEResponse(StatusImpl.OK, null, null);
            }
            default:
                throw new UnsupportedOperationException("Unsupported ITERequest Kind received.");
        }
    }

    @Override
    public final void serverShutdown() throws IOException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public final void controlFinished(final Job arg0, final String arg1,
                                      final List<TestCaseStatus> arg2) throws IOException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public final void controlStarted(final Job arg0, final String arg1) throws IOException {
        throw new UnsupportedOperationException("Not supported.");
    }

}
