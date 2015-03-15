package com.testingtech.car2x.hmi.messages;

import java.util.Date;

/**
 * Messages sent to the client socket containing the test case verdict.
 */
public class VerdictMessage extends Message {

    private static final long serialVersionUID = -8599864379194721903L;

    public final TestCaseVerdict verdict;

    public VerdictMessage(TestCase testCaseId, Date date, TestCaseVerdict verdict) {
        super(testCaseId, date);
        this.verdict = verdict;
    }

    @Override
    public String toString() {
        return String.format(
                "%s, verdict:[%s]",
                super.toString(),
                verdict
        );
    }

}
