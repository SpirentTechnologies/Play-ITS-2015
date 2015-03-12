package com.testingtech.car2x.hmi.messages;

import java.util.Date;

/**
 * Messages sent to the client socket containing progress feedback concerning a test case.
 */
public class StatusMessage extends Message {

    private static final long serialVersionUID = -1216127552974401911L;

    public final TestCaseProgress progress;
    public int value;

    public StatusMessage(TestCase testCaseId, Date date, TestCaseProgress progress, int value) {
        super(testCaseId, date);
        this.progress = progress;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format(
                "%s, statusType:[%s], statusValue:[%d]",
                super.toString(),
                progress,
                value
        );
    }

}
