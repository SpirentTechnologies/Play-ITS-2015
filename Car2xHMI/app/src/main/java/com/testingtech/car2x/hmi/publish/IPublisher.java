package com.testingtech.car2x.hmi.publish;

import java.io.IOException;

public interface IPublisher {

  public void publishProgress(String testCaseName, String actionMessage) throws IOException;

  public void publishVerdict(String testCaseName, String verdictName) throws IOException;

}
