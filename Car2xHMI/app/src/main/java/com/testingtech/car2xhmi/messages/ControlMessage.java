/**
 * Messages received from the client socket containing information regarding
 * controlling of test cases.
 */
package com.testingtech.car2xhmi.messages;

import java.util.Date;

public class ControlMessage extends Message {

  private static final long serialVersionUID = 6458145498447577207L;
  // TODO make command an ENUM
  public final String command;

  public ControlMessage(String testCaseId, Date date, String command) {
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
