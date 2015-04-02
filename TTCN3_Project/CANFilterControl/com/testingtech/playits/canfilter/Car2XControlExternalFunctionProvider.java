package com.testingtech.playits.canfilter;

import java.io.IOException;

import org.etsi.ttcn.tci.BooleanValue;
import org.etsi.ttcn.tci.CharstringValue;
import org.etsi.ttcn.tci.IntegerValue;

import com.testingtech.ttcn.annotation.ExternalFunction;
import com.testingtech.ttcn.tri.AnnotationsExternalFunctionPlugin;
import com.testingtech.util.UUID;

@ExternalFunction.Definitions(Car2XControlExternalFunctionProvider.class)
public class Car2XControlExternalFunctionProvider extends
    AnnotationsExternalFunctionPlugin {

  private static Process process;
  private static String processId;

  /**
   * Starts a CAN filter service process with specified host and port
   * 
   * @param host
   *            host name
   * @param portNumber
   *            port
   * @return process id
   */
  @ExternalFunction(name = "start_Filter", module = "Car2X_Control")
  public CharstringValue start_Filter(CharstringValue host,
      IntegerValue portNumber) {
    return start_Filter(
        // CANFilterService.class.getCanonicalName()
        "com.testingtech.playits.canfilter.CANFilterService",
        new String[] { host.getString(),
            String.valueOf(portNumber.getInt()) });
  }

  private CharstringValue start_Filter(String mainClass, String[] parameters) {
    if (process != null && process.isAlive()) {
      return newCharstringValue(processId);
    }
    processId = UUID.randomUUID().toString();

    logInfo("Starting " + mainClass + " with parameters: \""
        + join(parameters, "\" \"") + "\"");

    try {
      process = Runtime.getRuntime().exec("java -classpath build/CANFilter;libs/java-json.jar "
              + mainClass + " " + join(parameters, " "));

      new StreamForwarder(process.getInputStream(), System.out)
          .start();
      new StreamForwarder(process.getErrorStream(), System.err)
          .start();
    } catch (IOException e) {
      logError("Could not start CAN filter service: " + e.getMessage());
      return newCharstringValue("");
    }

    return newCharstringValue(processId);
  }

  private String join(String[] strings, String delimiter) {
    if (strings.length == 0) {
      return "";
    }

    StringBuilder sb = new StringBuilder();

    sb.append(strings[0]);
    for (int index = 1; index < strings.length; index++) {
      sb.append(delimiter).append(strings[index]);
    }

    return sb.toString();
  }

  /**
   * Stops the CAN filter service process.
   * @return true (service could successfully be stopped)
   */
  @ExternalFunction(name = "stop_Filter", module = "Car2X_Control")
  public BooleanValue stop_Filter() {
    logInfo("Stopping CAN service filter.");

    if (process != null) {
      process.destroy();
      try {
        process.waitFor();
      } catch (InterruptedException e) {
        // nothing to do just continue
      }
    }
    return newBooleanValue(true);
  }
}
