/**
 * Base class of all socket messages.
 */
package com.testingtech.car2xhmi.messages;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {

  private static final long serialVersionUID = -1475042464034501344L;
  public final String testCaseId;
  public final Date date;

  public Message(String testCaseId, Date date) {
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