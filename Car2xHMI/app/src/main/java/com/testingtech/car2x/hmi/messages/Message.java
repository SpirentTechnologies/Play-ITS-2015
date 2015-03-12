/**
 * Base class of all socket messages.
 */
package com.testingtech.car2x.hmi.messages;

import java.io.Serializable;
import java.util.Date;

public abstract class Message implements Serializable {

    private static final long serialVersionUID = -1475042464034501344L;

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