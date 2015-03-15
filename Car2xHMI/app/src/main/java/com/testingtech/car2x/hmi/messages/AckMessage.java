package com.testingtech.car2x.hmi.messages;

import java.util.Date;

/**
 * Messages sent to the client socket in response to a ControlMessage.
 */
public class AckMessage extends Message {

    private static final long serialVersionUID = -5734370360142692570L;

    public final TestCaseCommand command;

    public AckMessage(TestCase testCaseId, Date date, TestCaseCommand command) {
        super(testCaseId, date);
        this.command = command;
    }

    @Override
    public String toString() {
        return String.format(
                "%s, command:[%s]",
                super.toString(),
                command
        );
    }

}
