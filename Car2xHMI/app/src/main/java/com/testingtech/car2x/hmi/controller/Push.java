package com.testingtech.car2x.hmi.controller;

import java.io.IOException;

/**
 * TODO Controller should verify that the started test case is the same as the target started
 * by the TTmanCommunicator thread.
 * TODO deal with IOException
 * Ensure verdict and test case ID are checked.
 */
public interface Push {

  public void pushAcknowledgement(
          final String testCaseName, final String command
  ) throws IOException;

  public void pushProgress(
          final String testCaseName, final String actionMessage
  ) throws IOException;

  public void pushVerdict(
          final String testCaseName, final String verdictName
  ) throws IOException;

}
