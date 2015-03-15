package com.testingtech.car2x.hmi.messages;

import java.io.Serializable;
import java.util.Date;

/**
 * Base class of all socket messages.
 */
public abstract class Message implements Serializable {

    private static final long serialVersionUID = -2609347609614186025L;

    public final TestCase testCaseId;
    public final Date date;

    public Message(TestCase testCaseId, Date date) {
        this.testCaseId = testCaseId;
        this.date = date;
    }

    @Override
    public String toString() {
        return String.format(
                "testCaseId:[%s], date:[%tF %tT]",
                testCaseId,
                date,
                date
        );
    }

}