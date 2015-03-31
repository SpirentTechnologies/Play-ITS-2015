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
  @ExternalFunction(name = "startFilter", module = "Car2X_Control")
  public CharstringValue startFilter(CharstringValue host,
      IntegerValue portNumber, CharstringValue simulatorHost,
      IntegerValue simulatorPortNumber) {
    return startFilter(
        "com.testingtech.playits.canfilter.CANFilterServiceMain",
        new String[] { host.getString(),
            String.valueOf(portNumber.getInt()),
            simulatorHost.getString(),
            String.valueOf(simulatorPortNumber.getInt()) });
  }

  private CharstringValue startFilter(String mainClass, String[] parameters) {
    if (process != null && process.isAlive()) {
      return newCharstringValue(processId);
    }
    processId = UUID.randomUUID().toString();

    logInfo("Starting " + mainClass + " with parameters: \""
        + join(parameters, "\" \"") + "\"");

    String pathSeparator = System.getProperty("path.separator");
    String execCommand = "java -classpath build/CANFilter" + pathSeparator
            + "libs/java-json.jar " + mainClass + " "
            + join(parameters, " "); 
    try {

      process = Runtime.getRuntime().exec(execCommand);

      new StreamForwarder(process.getInputStream(), System.out).start();
      new StreamForwarder(process.getErrorStream(), System.err).start();
    } catch (IOException e) {
      logError("Could not start CAN filter service: " + e.getMessage());
      processId = "";
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
   * 
   * @return true (service could successfully be stopped)
   */
  @ExternalFunction(name = "stopFilter", module = "Car2X_Control")
  public BooleanValue stopFilter() {
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
