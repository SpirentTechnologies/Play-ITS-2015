/**
 * Messages sent to the client socket containing test case progress.
 */
package com.testingtech.car2x.hmi.messages;

import java.util.Date;

public class ProgressMessage extends Message {

    private static final long serialVersionUID = -2654835295802782772L;

    public final TestCaseProgress progress;

    public ProgressMessage(TestCase testCaseId, Date date, TestCaseProgress progress) {
        super(testCaseId, date);
        this.progress = progress;
    }

    @Override
    public String toString() {
        return String.format("%s, progress:[%s]",
                super.toString(),
                progress
        );
    }

}
