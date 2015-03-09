/**
 * Messages sent to the client socket containing information regarding
 * the status of test cases.
 */
package com.testingtech.car2xhmi.messages;

import java.util.Date;

public class StatusMessage extends Message {

  private static final long serialVersionUID = -1216127552974401911L;
  // TODO both should actually be ENUMs
  public final String statusType;
  public final int statusValue;

  public StatusMessage(String testCaseId, Date date, String statusType, int statusValue) {
    super(testCaseId, date);
    this.statusType = statusType;
    this.statusValue = statusValue;
  }

  @Override
  public String toString() {
    return String.format(
        "%s, statusType:[%s], statusValue:[%s]",
        super.toString(),
        statusType,
        statusValue
    );
  }

}
