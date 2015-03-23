package canfilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Vector;

import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

public class ELMBluetooth implements DiscoveryListener {
  private static Object lock = new Object();

  private static Vector<RemoteDevice> remdevices = new Vector<RemoteDevice>();

  private static String connectionURL = null;

  /**
   * @param String[]
   */
  public static void main(String[] args) throws IOException,
      InterruptedException {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    ELMBluetooth obj = new ELMBluetooth();
    LocalDevice locdevice = LocalDevice.getLocalDevice();
    String add = locdevice.getBluetoothAddress();
    String friendlyName = locdevice.getFriendlyName();

    System.out.println("Local Bluetooth Address : " + add);
    System.out.println("" + "" + "Local Friendly name : " + friendlyName);

    DiscoveryAgent disAgent = locdevice.getDiscoveryAgent();
    System.out.println("********Locating Devices******");
    disAgent.startInquiry(DiscoveryAgent.GIAC, obj);
    try {

      synchronized (lock) {
        lock.wait();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    if (remdevices.size() <= 0) {
      System.out.println("No devices found");

    } else {
      int index = 0;
      for (int value = 0; value < remdevices.size(); value++) {
        RemoteDevice remoteDevice = (RemoteDevice) remdevices
            .elementAt(value);
        if ((remoteDevice.getFriendlyName(true) == "OBDII") 
            || (remoteDevice.getFriendlyName(true) == "ELM327")) { // TODO:
          // all
          // namend
          // this
          // way?
          index = value;
        }
      }

      RemoteDevice desDevice = (RemoteDevice) remdevices
          .elementAt(index); // index - 1
      UUID[] uuidset = new UUID[1];
      uuidset[0] = new UUID("1101", true);

      disAgent.searchServices(null, uuidset, desDevice, obj);
      try {
        synchronized (lock) {
          lock.wait();
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      if (connectionURL == null) {
        System.out.println("Device does not support SPP.");
      } else {
        System.out.println("Device supports SPP.");
        StreamConnection stConnect = (StreamConnection) Connector
            .open(connectionURL);
        OutputStream outStream = stConnect.openOutputStream();

        PrintWriter pwriter = new PrintWriter(new OutputStreamWriter(
            outStream));
        InputStream inStream = stConnect.openInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(
            inStream));

        System.out
            .println("Remember to set the Protocol to auto: AT SP 0  , to check AT DP");
        while (true) {
          System.out.println("Enter Command: ");
          String command = br.readLine();
          pwriter.write(command + "\r");
          pwriter.flush();
          System.out.println("command send: " + command);
          Thread.sleep(500);
          byte bytes = 0;
          StringBuilder res = new StringBuilder();
          String rawData = null;

          // read until '>' arrives
          while ((char) (bytes = (byte) in.read()) != '>') {
            res.append((char) bytes);
          }
          /*
           * Imagine the following response 41 0c 00 0d.
           * 
           * ELM sends strings!! So, ELM puts spaces between each
           * "byte". And pay attention to the fact that I've put the
           * word byte in quotes, because 41 is actually TWO bytes
           * (two chars) in the socket. So, we must do some more
           * processing..
           */
          rawData = res.toString().trim();

          /*
           * Data may have echo or informative text like "INIT BUS..."
           * or similar. The response ends with two carriage return
           * characters. So we need to take everything from the last
           * carriage return before those two (trimmed above).
           */
          rawData = rawData.substring(rawData.lastIndexOf(13) + 1);

          System.out.println("response: " + rawData);
        }
      }
    }
  }
//TODO necessary?
  /**
   * @param RemoteDevice
   * @param DeviceClass
   */
  public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
    if (!remdevices.contains(btDevice)) {
      remdevices.addElement(btDevice);
    }
  }

  @Override
  public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
    if (!(servRecord == null) && servRecord.length > 0) {
      connectionURL = servRecord[0].getConnectionURL(0, false);
    }

  }

  @Override
  public void serviceSearchCompleted(int transID, int respCode) {
    synchronized (lock) {
      lock.notify();
    }
  }

  @Override
  public void inquiryCompleted(int discType) {
    synchronized (lock) {
      lock.notify();
    }
    switch (discType) {
      case DiscoveryListener.INQUIRY_COMPLETED:
        System.out.println("Inquiry Completed");
        break;

      case DiscoveryListener.INQUIRY_TERMINATED:
        System.out.println("Inquiry Terminated");
        break;

      case DiscoveryListener.INQUIRY_ERROR:
        System.out.println("Inquiry Error");
        break;

      default:
        System.out.println("Unknown Response Code");
    }
  }
  /**
   * 
   * @param cmd
   * @return
   */
  public String getOBDIIKey(String cmd) {
    String result;
    switch (cmd) {
      case "vehicle_speed":
        result = "01 0D";
        break;
      case "engine_speed":
        result = "01 0C";
        break;
      case "steering_wheel_angle":
        result = "XX";
        break;
      case "torque_at_transmission":
        result = "XX";
        break;
      case "accelerator_pedal_position":
        result = "01 49"; // or 4A or 4B ?
        break;
      case "parking_brake_status":
        result = "XX";
        break;
      case "brake_pedal_status":
        result = "XX";
        break;
      case "transmission_gear_position":
        result = "XX";
        break;
      case "gear_lever_position":
        result = "XX";
        break;
      case "odometer":
        result = "XX";
        break;
      case "ignition_status":
        result = "XX";
        break;
      case "fuel_level":
        result = "01 2F";
        break;
      case "fuel_consumed_since_restart":
        result = "XX";
        break;
      case "door_status":
        result = "XX";
        break;
      case "headlamp_status":
        result = "XX";
        break;
      case "high_beam_status":
        result = "XX";
        break;
      case "windshield_wiper_status":
        result = "XX";
        break;
      case "latitude":
        result = "XX";
        break;
      case "longitude":
        result = "XX";
        break;
      case "battery_status":
        result = "AT RV";
        break;
      case "absolute_load":
        result = "01 43";
        break;
      case "runtime_since_last_start":
        result = "01 1F";
        break;
      case "timing_advanced": // relative to #1 cylinder
        result = "01 0E";
        break;
      case "engine_oil_temperature":
        result = "01 5C";
        break;
      case "drivers_demand_engine": // percentage torque
        result = "01 61";
        break;
      case "actual_engine": // percentage torque
        result = "01 62";
        break;
      case "engine_reference": // torque Nm
        result = "01 63";
        break;
      case "fuel_consumption_rate":
        result = "01 5E";
        break;
      case "barometric_pressure":
        result = "01 33";
        break;
      case "fuel_pressure":
        result = "01 0A";
        break;
      case "Intake_manifold_absolute_pressure":
        result = "01 0A";
        break;
      case "air_intake_temperature":
        result = "01 0F";
        break;
      case "ambient_air_temperature":
        result = "01 46";
        break;
      case "engine_coolant_temperature":
        result = "01 05";
        break;
      case "throttle_position":
        result = "01 11";
        break;
      default:
        result = "XX";
        break;
    }

    return result;
  }
  /**
   * 
   * @param type
   * @param reply
   * @return
   */
  public String convertOBD2ReplyToOpenXC(String type, String reply) {
    String result;

    String[] data = reply.split("\\s"); // cut first 4 bytes (response =
    // "41 0C 0C FC") 4,1,0,C,0,C,F,C
    int value1;
    int value2;
    int resultInt;
    switch (type) {
      case "vehicle_speed":
        value1 = Integer.parseInt(data[2] + data[3], 16);
        result = Integer.toString(value1);
        break;
      case "engine_speed": // ((A*256)+B)/4
        value1 = Integer.parseInt(data[2] + data[3], 16);
        value2 = Integer.parseInt(data[4] + data[5], 16);
        resultInt = ((value1 * 256) + value2) / 4;
        result = Integer.toString(resultInt);
        break;
      case "accelerator_pedal_position":
        result = (new Integer(
          Integer.parseInt(data[2] + data[3], 16) * 100 / 255))
          .toString();
        break;
      case "fuel_level":
        result = (new Integer(
          Integer.parseInt(data[2] + data[3], 16) * 100 / 255))
          .toString();
        break;
      case "battery_status": // 11.8V
        result = data[0] + data[1] + data[2] + data[3];
        break;
      case "absolute_load":
        value1 = Integer.parseInt(data[2] + data[3], 16);
        value2 = Integer.parseInt(data[4] + data[5], 16);
        resultInt = ((value1 * 256) + value2) * 100 / 255;
        result = Integer.toString(resultInt);
        break;
      case "runtime_since_last_start":
        value1 = Integer.parseInt(data[2] + data[3], 16);
        value2 = Integer.parseInt(data[4] + data[5], 16);
        resultInt = ((value1 * 256) + value2);
        result = Integer.toString(resultInt);
        break;
      case "timing_advanced": // relative to #1 cylinder
        result = Integer
          .toString((Integer.parseInt(data[2] + data[3], 16) - 128) / 2);
        break;
      case "engine_oil_temperature":
        result = Integer
          .toString(Integer.parseInt(data[2] + data[3], 16) - 40);
        break;
      case "drivers_demand_engine": // percentage torque
        result = Integer
          .toString(Integer.parseInt(data[2] + data[4], 16) - 125);
        break;
      case "actual_engine": // percentage torque
        result = Integer
          .toString(Integer.parseInt(data[2] + data[3], 16) - 125);
        break;
      case "engine_reference": // torque Nm
        value1 = Integer.parseInt(data[2] + data[3], 16);
        value2 = Integer.parseInt(data[4] + data[5], 16);
        resultInt = ((value1 * 256) + value2);
        result = Integer.toString(resultInt);
        break;
      case "fuel_consumption_rate": // engine_fuel_rate
        value1 = Integer.parseInt(data[2] + data[3], 16);
        value2 = Integer.parseInt(data[4] + data[5], 16);
        resultInt = ((value1 * 256) + value2) / 20;
        result = Integer.toString(resultInt);
        break;
      case "barometric_pressure": // kPa
        result = data[2];
        break;
      case "fuel_pressure": // kPa
        value1 = Integer.parseInt(data[2] + data[3], 16) * 3;
        result = Integer.toString(value1);
        break;
      case "Intake_manifold_absolute_pressure":
        result = data[2];
        break;
      case "air_intake_temperature":
        value1 = Integer.parseInt(data[2] + data[3], 16) - 40;
        result = Integer.toString(value1);
        break;
      case "ambient_air_temperature":
        value1 = Integer.parseInt(data[2] + data[3], 16) - 40;
        result = Integer.toString(value1);
        break;
      case "engine_coolant_temperature":
        value1 = Integer.parseInt(data[2] + data[3], 16) - 40;
        result = Integer.toString(value1);
        break;
      case "throttle_position":
        value1 = Integer.parseInt(data[2] + data[3], 16) * 100 / 255;
        result = Integer.toString(value1);
        break;
      default:
        result = reply;
        break;
    }

    return result;

  }

}