package com.testingtech.car2x.hmi.messages;

import java.util.Date;

/**
 * Messages sent to the client socket on error occurrence.
 */
public class ErrorMessage extends Message {

    private static final long serialVersionUID = -1458395504540052362L;

    // TODO add severity level
    public final String errorMessage;


    public ErrorMessage(TestCase testCaseId, Date date, String errorMessage) {
        super(testCaseId, date);
        this.errorMessage = errorMessage;
    }

    /**
     public ErrorMessage(TestCase testCaseId, Date date, boolean success, String errorMessage) {
     super(testCaseId, date);
     this.success = success;
     this.errorMessage = errorMessage;
     }
     */

    @Override
    public String toString() {
        return String.format(
                "%s, errorMessage:[%s]",
                super.toString(),
                errorMessage
        );
    }

}
