package com.testingtech.car2x.hmi.messages;

import java.util.Date;

/**
 * Messages received from the client socket containing information regarding controlling of test
 * cases.
 */
public class ControlMessage extends Message {

    private static final long serialVersionUID = 6458145498447577207L;

    public final TestCaseCommand command;

    public ControlMessage(TestCase testCaseId, Date date, TestCaseCommand command) {
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
