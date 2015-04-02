package com.testingtech.playits.canfilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.etsi.ttcn.tci.BooleanValue;
import org.etsi.ttcn.tci.CharstringValue;
import org.etsi.ttcn.tci.RecordValue;
import org.etsi.ttcn.tci.UnionValue;

import com.testingtech.ttcn.annotation.ExternalFunction;
import com.testingtech.ttcn.tci.util.ValueUtils;
import com.testingtech.ttcn.tri.AnnotationsExternalFunctionPlugin;
import com.testingtech.util.UUID;

@ExternalFunction.Definitions(Car2XControlExternalFunctionProvider.class)
public class Car2XControlExternalFunctionProvider extends
    AnnotationsExternalFunctionPlugin {

  private static Process process;
  private static String processId;

  /**
   * Starts a CAN filter service process with a specified filter configuration.
   * 
   * @param RecordValue server host (Charstring), 
   * server port (IntegerValue) and a connection configuration (UnionValue)
   * @return process identifier
   */
  @ExternalFunction(name = "startFilter", module = "Car2X_Control")
  public CharstringValue startFilter(RecordValue filterCfg) {
    String host = ValueUtils.getStringValue(filterCfg.getField("host"));
    String portNumber = ValueUtils.getStringValue(filterCfg
        .getField("portNumber"));
    List<String> args = new ArrayList<String>(Arrays.asList(host,
        String.valueOf(portNumber)));

    UnionValue canConfig = (UnionValue) filterCfg.getField("canConfig");
    String selectedVariant = canConfig.getPresentVariantName();
    RecordValue selectedValue = (RecordValue) canConfig
        .getVariant(selectedVariant);

    String mainClass = "";
    switch (selectedVariant) {
    case "simulator":
      mainClass = "com.testingtech.playits.canfilter.OpenXCCANFilterService";
      createOpenXCArguments(selectedValue, args);
      break;
    case "elmBluetooth":
      mainClass = "com.testingtech.playits.canfilter.BluetoothCANFilterService";
      args.add(ValueUtils.getStringValue(selectedValue
          .getField("deviceName")));
      break;
    case "elmRS232":
      mainClass = "com.testingtech.playits.canfilter.RS232CANFilterService";
      createRS232Arguments(selectedValue, args);
      break;
    default:
      break;
    }
    return startFilter(mainClass, args.toArray(new String[0]));
  }

  private void createOpenXCArguments(RecordValue selectedValue,
      List<String> args) {
    args.add(ValueUtils.getStringValue(selectedValue.getField("host")));
    args.add(ValueUtils.getStringValue(selectedValue.getField("portNumber")));
  }

  private void createRS232Arguments(RecordValue selectedValue,
      List<String> args) {
    args.add(ValueUtils.getStringValue(selectedValue.getField("comPort")));
    args.add(ValueUtils.getStringValue(selectedValue.getField("baudRate")));
    args.add(ValueUtils.getStringValue(selectedValue.getField("parity")));
    args.add(ValueUtils.getStringValue(selectedValue.getField("dataBits")));
    args.add(ValueUtils.getStringValue(selectedValue.getField("stopBits")));
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
