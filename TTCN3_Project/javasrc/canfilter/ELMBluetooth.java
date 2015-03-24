package canfilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
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
  
  public static Hashtable<String, String> openXCToOBD2Map = new Hashtable<String, String>();
  
  private static Hashtable<String, Car2XEntry> car2xEntries = new Hashtable<>();
  /**
   * @param String[]
   */
  public static void main(String[] args) throws IOException,
      InterruptedException {
    initOpenXCToOBD2Map();
    initCar2XEntries();
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
          if (checkIfHexOrAT(command) || command.equals("")) {
            pwriter.write(command + "\r");
            pwriter.flush();
            System.out.println("command send: " + command + " = " 
                + getKeyByValue(openXCToOBD2Map,command));
          } else if (car2xEntries.containsKey(command)) {
            pwriter.write(car2xEntries.get(command).getObd2key() + "\r");
            pwriter.flush();
            System.out.println("command send: " + car2xEntries.get(command).getObd2key() 
                + " = " + command);
          }
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
          
          
          if (checkIfHexOrAT(command) || command.equals("")) {
            if (getKeyByValue(openXCToOBD2Map,command) != null) {
              System.out.println("response: " + rawData + " = " 
                  + convertOBD2ReplyToOpenXC( getKeyByValue(openXCToOBD2Map,command), rawData));
            } else {
              System.out.println("response: " + rawData + "    (not used in any Testcase)");
        	}
          } else if(car2xEntries.containsKey(command)) {
            System.out.println("response:" + rawData + " = " 
               + convertOBD2ReplyToOpenXC(command, rawData));
          }

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
  public static void initOpenXCToOBD2Map() {  
    //TODO Error-Handling if there is no OBD2 Key(here XX)
    openXCToOBD2Map.put("vehicle_speed", "01 0D");
    openXCToOBD2Map.put("engine_speed", "01 0C");
    openXCToOBD2Map.put("steering_wheel_angle", "XX");
    openXCToOBD2Map.put("torque_at_transmission", "XX");
    openXCToOBD2Map.put("accelerator_pedal_position", "01 49"); //or 4A or 4B ?
    openXCToOBD2Map.put("parking_brake_status", "XX");
    openXCToOBD2Map.put("brake_pedal_status", "XX");
    openXCToOBD2Map.put("transmission_gear_position", "XX");
    openXCToOBD2Map.put("gear_lever_position", "XX");
    openXCToOBD2Map.put("odometer", "XX");
    openXCToOBD2Map.put("ignition_status", "XX");
    openXCToOBD2Map.put("fuel_level", "01 2F");
    openXCToOBD2Map.put("fuel_consumed_since_restart", "XX");
    openXCToOBD2Map.put("door_status", "XX");
    openXCToOBD2Map.put("headlamp_status", "XX");
    openXCToOBD2Map.put("high_beam_status", "XX");
    openXCToOBD2Map.put("windshield_wiper_status", "XX");
    openXCToOBD2Map.put("latitude", "XX");
    openXCToOBD2Map.put("longitude", "XX");
    openXCToOBD2Map.put("battery_status", "AT RV");
    openXCToOBD2Map.put("absolute_load", "01 43");
    openXCToOBD2Map.put("runtime_since_last_start", "01 1F");
    openXCToOBD2Map.put("timing_advanced", "01 0E"); // relative to #1 cylinder
    openXCToOBD2Map.put("engine_oil_temperature", "01 5C");
    openXCToOBD2Map.put("drivers_demand_engine", "01 61"); // percentage torque
    openXCToOBD2Map.put("actual_engine", "01 62"); // percentage torque
    openXCToOBD2Map.put("engine_reference", "01 63");  // torque Nm
    openXCToOBD2Map.put("fuel_consumption_rate", "01 5E");
    openXCToOBD2Map.put("barometric_pressure", "01 33");
    openXCToOBD2Map.put("fuel_pressure", "01 0A");
    openXCToOBD2Map.put("Intake_manifold_absolute_pressure", "01 0A");
    openXCToOBD2Map.put("air_intake_temperature", "01 0F");
    openXCToOBD2Map.put("ambient_air_temperature", "01 46");
    openXCToOBD2Map.put("engine_coolant_temperature", "01 05");
    openXCToOBD2Map.put("throttle_position", "01 11");
  }
  
  /**
   * 
   */
  public static void initCar2XEntries() {
    car2xEntries.put("vehicle_speed", new Car2XEntry(0));
    car2xEntries.put("engine_speed", new Car2XEntry(0));
    car2xEntries.put("steering_wheel_angle", new Car2XEntry(0));
    car2xEntries.put("torque_at_transmission", new Car2XEntry(0));
    car2xEntries.put("accelerator_pedal_position", new Car2XEntry(0));
    car2xEntries.put("parking_brake_status", new Car2XEntry(0));
    car2xEntries.put("brake_pedal_status", new Car2XEntry(0));
    car2xEntries.put("transmission_gear_position", new Car2XEntry(0));
    car2xEntries.put("gear_lever_position", new Car2XEntry(0));
    car2xEntries.put("odometer", new Car2XEntry(0));
    car2xEntries.put("ignition_status", new Car2XEntry(0));
    car2xEntries.put("fuel_level", new Car2XEntry(0));
    car2xEntries.put("fuel_consumed_since_restart", new Car2XEntry(0));
    car2xEntries.put("door_status", new Car2XEntry(0));
    car2xEntries.put("headlamp_status", new Car2XEntry(0));
    car2xEntries.put("high_beam_status", new Car2XEntry(0));
    car2xEntries.put("windshield_wiper_status", new Car2XEntry(0));
    car2xEntries.put("latitude", new Car2XEntry(0));
    car2xEntries.put("longitude", new Car2XEntry(0));
    car2xEntries.put("battery_status", new Car2XEntry(0));
    car2xEntries.put("absolute_load", new Car2XEntry(0));
    car2xEntries.put("runtime_since_last_start", new Car2XEntry(0));
    car2xEntries.put("timing_advanced", new Car2XEntry(0));
    car2xEntries.put("engine_oil_temperature", new Car2XEntry(0));
    car2xEntries.put("drivers_demand_engine", new Car2XEntry(0));
    car2xEntries.put("actual_engine", new Car2XEntry(0));
    car2xEntries.put("engine_reference", new Car2XEntry(0));
    car2xEntries.put("fuel_consumption_rate", new Car2XEntry(0));
    car2xEntries.put("barometric_pressure", new Car2XEntry(0));
    car2xEntries.put("fuel_pressure", new Car2XEntry(0));
    car2xEntries.put("Intake_manifold_absolute_pressure", new Car2XEntry(0));
    car2xEntries.put("air_intake_temperature", new Car2XEntry(0));
    car2xEntries.put("ambient_air_temperature", new Car2XEntry(0));
    car2xEntries.put("engine_coolant_temperature", new Car2XEntry(0));
    car2xEntries.put("throttle_position", new Car2XEntry(0)); 

    Enumeration<String> enumKey = car2xEntries.keys();
    while(enumKey.hasMoreElements()) {
      String key = enumKey.nextElement();
      car2xEntries.get(key).setObds2key(openXCToOBD2Map.get(key));
    }
  }
  /**
   * 
   * @param type
   * @param reply
   * @return
   */
  public static String convertOBD2ReplyToOpenXC(String type, String reply) {
    String result;
    if (reply.equals("NO DATA") || reply.equals("OK") || reply.startsWith("BUS")) {
      result = reply;
    } else {
     String[] data = reply.split("\\s"); // cut first 4 bytes (response =
    	// "41 0C 0C FC") 41,0C,0C,FC
    	int value1;
    	int value2;
    	int resultInt;
    	switch (type) {
    	case "vehicle_speed":
    		value1 = Integer.parseInt(data[2], 16);
    		result = Integer.toString(value1);
    		break;
    	case "engine_speed": // ((A*256)+B)/4
    		value1 = Integer.parseInt(data[2], 16);
    		value2 = Integer.parseInt(data[3], 16);
    		resultInt = ((value1 * 256) + value2) / 4;
    		result = Integer.toString(resultInt);
    		break;
    	case "accelerator_pedal_position":
    		result = (new Integer(
    				Integer.parseInt(data[2], 16) * 100 / 255))
    				.toString();
    		break;
    	case "fuel_level":
    		result = (new Integer(
    				Integer.parseInt(data[2], 16) * 100 / 255))
    				.toString();
    		break;
    	case "battery_status": // 11.8V
    		result = reply;
    		break;
    	case "absolute_load":
    		value1 = Integer.parseInt(data[2], 16);
    		value2 = Integer.parseInt(data[3], 16);
    		resultInt = ((value1 * 256) + value2) * 100 / 255;
    		result = Integer.toString(resultInt);
    		break;
    	case "runtime_since_last_start":
    		value1 = Integer.parseInt(data[2], 16);
    		value2 = Integer.parseInt(data[3], 16);
    		resultInt = ((value1 * 256) + value2);
    		result = Integer.toString(resultInt);
    		break;
    	case "timing_advanced": // relative to #1 cylinder
    		result = Integer
    		.toString((Integer.parseInt(data[2], 16) - 128) / 2);
    		break;
    	case "engine_oil_temperature":
    		result = Integer
    		.toString(Integer.parseInt(data[2], 16) - 40);
    		break;
    	case "drivers_demand_engine": // percentage torque
    		result = Integer
    		.toString(Integer.parseInt(data[2], 16) - 125);
    		break;
    	case "actual_engine": // percentage torque
    		result = Integer
    		.toString(Integer.parseInt(data[2], 16) - 125);
    		break;
    	case "engine_reference": // torque Nm
    		value1 = Integer.parseInt(data[2], 16);
    		value2 = Integer.parseInt(data[3], 16);
    		resultInt = ((value1 * 256) + value2);
    		result = Integer.toString(resultInt);
    		break;
    	case "fuel_consumption_rate": // engine_fuel_rate
    		value1 = Integer.parseInt(data[2], 16);
    		value2 = Integer.parseInt(data[3], 16);
    		resultInt = ((value1 * 256) + value2) / 20;
    		result = Integer.toString(resultInt);
    		break;
    	case "barometric_pressure": // kPa
    		result = Integer.toString(Integer.parseInt(data[2], 16));
    		break;
    	case "fuel_pressure": // kPa
    		value1 = Integer.parseInt(data[2], 16) * 3;
    		result = Integer.toString(value1);
    		break;
    	case "Intake_manifold_absolute_pressure":
    		result = Integer.toString(Integer.parseInt(data[2], 16));
    		break;
    	case "air_intake_temperature":
    		value1 = Integer.parseInt(data[2], 16) - 40;
    		result = Integer.toString(value1);
    		break;
    	case "ambient_air_temperature":
    		value1 = Integer.parseInt(data[2], 16) - 40;
    		result = Integer.toString(value1);
    		break;
    	case "engine_coolant_temperature":
    		value1 = Integer.parseInt(data[2], 16) - 40;
    		result = Integer.toString(value1);
    		break;
    	case "throttle_position":
    		value1 = Integer.parseInt(data[2], 16) * 100 / 255;
    		result = Integer.toString(value1);
    		break;
    	default:
    		result = reply;
    		break;
    	}
    }

    return result;

  }
  
  public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
	    for (Entry<T, E> entry : map.entrySet()) {
	        if (value.equals(entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
  
  public static boolean checkIfHexOrAT(String input){
	  String rawData = input.replaceAll("\\s", "");
	    if (rawData.matches("([0-9A-F]{2})+") || (rawData.startsWith("AT"))) {
	    	return true;
	    } else {
	    	return false;
	    }	  
  }

}