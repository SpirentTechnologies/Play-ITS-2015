package com.testingtech.playits.canfilter.bluetooth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
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

import com.testingtech.playits.canfilter.Car2XEntry;

/**
 * 
 * @author Benjamin Kodera, Christian Kühling
 *
 */
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
    String rawData = null;

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
            .println("If Problems occur, run AT Z");
        System.out.println(getSupportedPIDs(inStream, pwriter));
        while (true) {
          System.out.println("Enter Command: ");
          String command = br.readLine(); 
          if (checkIfHexOrAT(command) || command.equals("")) {
            rawData = run(command,in,pwriter);
            System.out.println("command send: " + command + " = " 
                + getKeyByValue(openXCToOBD2Map,command));
          } else if (car2xEntries.containsKey(command)) {
            rawData = run(car2xEntries.get(command).getOBD2key(),in,pwriter);
            System.out.println("command send: " + car2xEntries.get(command).getOBD2key() 
                + " = " + command);
          }
         
          
          
          if (checkIfHexOrAT(command) || command.equals("")) {
            if (getKeyByValue(openXCToOBD2Map,command) != null) {
              System.out.println("response: " + rawData + " = " 
                  + convertOBD2Reply( rawData));
            } else {
              System.out.println("response: " + rawData + "    (not used in any Testcase)");
        	}
          } else if(car2xEntries.containsKey(command)) {
            System.out.println("response:" + rawData + " = " 
               + convertOBD2Reply( rawData));
          }

        }
      }
    }
  }
  /**
   * @param RemoteDevice
   * @param DeviceClass
   * using javax.bluetooth import
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
   * see http://en.wikipedia.org/wiki/OBD-II_PIDs
   * adds possible available PID Codes
   */
  public static void initOpenXCToOBD2Map() {  
    //TODO Error-Handling if there is no OBD2 Key(here 00)

    openXCToOBD2Map.put("steering_wheel_angle", "00");
    openXCToOBD2Map.put("torque_at_transmission", "00");    
    openXCToOBD2Map.put("parking_brake_status", "00");
    openXCToOBD2Map.put("brake_pedal_status", "00");
    openXCToOBD2Map.put("transmission_gear_position", "00");
    openXCToOBD2Map.put("gear_lever_position", "00");
    openXCToOBD2Map.put("odometer", "00");
    openXCToOBD2Map.put("ignition_status", "00");
    openXCToOBD2Map.put("fuel_consumed_since_restart", "00");
    openXCToOBD2Map.put("door_status", "00");
    openXCToOBD2Map.put("headlamp_status", "00");
    openXCToOBD2Map.put("high_beam_status", "00");
    openXCToOBD2Map.put("windshield_wiper_status", "00");
    openXCToOBD2Map.put("latitude", "00");
    openXCToOBD2Map.put("longitude", "00");
    
    openXCToOBD2Map.put("battery_status", "AT RV"); //AT Command

    openXCToOBD2Map.put("pid_supported_0120", "01 00");
    openXCToOBD2Map.put("pid_supported_2140", "01 20");
    openXCToOBD2Map.put("pid_supported_4160", "01 40");
    openXCToOBD2Map.put("pid_supported_6180", "01 60");
    openXCToOBD2Map.put("pid_supported_81A0", "01 80");
    openXCToOBD2Map.put("pid_supported_A1C0", "01 A0");
    openXCToOBD2Map.put("pid_supported_C1E0", "01 C0");
    openXCToOBD2Map.put("monitor_status_since_dtc_cleared", "01 01");
    openXCToOBD2Map.put("freeze_dtc", "01 02");
    openXCToOBD2Map.put("fuel_system_status", "01 03");
    openXCToOBD2Map.put("calculated_engine_load", "01 04");
    openXCToOBD2Map.put("engine_coolant_temperature", "01 05");
    openXCToOBD2Map.put("short_term_fuel_bank1", "01 06");
    openXCToOBD2Map.put("long_termin_fuel_bank1", "01 07");
    openXCToOBD2Map.put("short_term_fuel_bank2", "01 08");
    openXCToOBD2Map.put("long_termin_fuel_bank2", "01 09");
    openXCToOBD2Map.put("fuel_pressure", "01 0A");
    openXCToOBD2Map.put("Intake_manifold_absolute_pressure", "01 0B");
    openXCToOBD2Map.put("engine_speed", "01 0C");
    openXCToOBD2Map.put("engine_rpm", "01 0C");
    openXCToOBD2Map.put("vehicle_speed", "01 0D");
    openXCToOBD2Map.put("timing_advanced", "01 0E"); // relative to #1 cylinder
    openXCToOBD2Map.put("air_intake_temperature", "01 0F");
    openXCToOBD2Map.put("maf_air_flow_rate", "01 10");
    openXCToOBD2Map.put("throttle_position", "01 11");
    openXCToOBD2Map.put("commanded_secondary_air_status", "01 12");
    openXCToOBD2Map.put("oxygen_sensor_present", "01 13"); //or 1D ?
    openXCToOBD2Map.put("bank1_sensor1_oxygen_sensor_voltage", "01 14");
    openXCToOBD2Map.put("bank1_sensor2_oxygen_sensor_voltage", "01 15");
    openXCToOBD2Map.put("bank1_sensor3_oxygen_sensor_voltage", "01 16");
    openXCToOBD2Map.put("bank1_sensor4_oxygen_sensor_voltage", "01 17");
    openXCToOBD2Map.put("bank2_sensor1_oxygen_sensor_voltage", "01 18");
    openXCToOBD2Map.put("bank2_sensor2_oxygen_sensor_voltage", "01 19");
    openXCToOBD2Map.put("bank2_sensor3_oxygen_sensor_voltage", "01 1A");
    openXCToOBD2Map.put("bank2_sensor4_oxygen_sensor_voltage", "01 1B");
    openXCToOBD2Map.put("obd_standard_this_vehicle_conforms_to", "01 1C");
    openXCToOBD2Map.put("auxiliary_input_status", "01 1E");
    openXCToOBD2Map.put("runtime_since_last_start", "01 1F");
    openXCToOBD2Map.put("distance_traveled_with_malfunction_indicator_lamp_on", "01 21");
    openXCToOBD2Map.put("fuel_rail_pressure_manifold_vacuum", "01 22");
    openXCToOBD2Map.put("fuel_rail_pressure_direct_inject", "01 23");
    openXCToOBD2Map.put("o2s1_wr_lambda_equivalent_ratio_voltage", "01 24");
    openXCToOBD2Map.put("o2s2_wr_lambda_equivalent_ratio_voltage", "01 25");
    openXCToOBD2Map.put("o2s3_wr_lambda_equivalent_ratio_voltage", "01 26");
    openXCToOBD2Map.put("o2s4_wr_lambda_equivalent_ratio_voltage", "01 27");
    openXCToOBD2Map.put("o2s5_wr_lambda_equivalent_ratio_voltage", "01 28");
    openXCToOBD2Map.put("o2s6_wr_lambda_equivalent_ratio_voltage", "01 29");
    openXCToOBD2Map.put("o2s7_wr_lambda_equivalent_ratio_voltage", "01 2A");
    openXCToOBD2Map.put("o2s8_wr_lambda_equivalent_ratio_voltage", "01 2B");
    openXCToOBD2Map.put("commanded_egr", "01 2C");
    openXCToOBD2Map.put("egr_error", "01 2D");
    openXCToOBD2Map.put("commanded_evaporative_purge", "01 2E");
    openXCToOBD2Map.put("fuel_level", "01 2F"); //fuel_level_input
    openXCToOBD2Map.put("num_of_warmups_since_codes_cleared", "01 30");
    openXCToOBD2Map.put("distance_traveled_since_codes_cleared", "01 31");
    openXCToOBD2Map.put("evap_system_vapor_pressure", "01 32");
    openXCToOBD2Map.put("barometric_pressure", "01 33");
    openXCToOBD2Map.put("o2s1_wr_lambda_equivalent_ratio_current", "01 34");
    openXCToOBD2Map.put("o2s2_wr_lambda_equivalent_ratio_current", "01 35");
    openXCToOBD2Map.put("o2s3_wr_lambda_equivalent_ratio_current", "01 36");
    openXCToOBD2Map.put("o2s4_wr_lambda_equivalent_ratio_current", "01 37");
    openXCToOBD2Map.put("o2s5_wr_lambda_equivalent_ratio_current", "01 38");
    openXCToOBD2Map.put("o2s6_wr_lambda_equivalent_ratio_current", "01 39");
    openXCToOBD2Map.put("o2s7_wr_lambda_equivalent_ratio_current", "01 3A");
    openXCToOBD2Map.put("o2s8_wr_lambda_equivalent_ratio_current", "01 3B");
    openXCToOBD2Map.put("catalyst_temperature_bank1_sensor1", "01 3C");
    openXCToOBD2Map.put("catalyst_temperature_bank1_sensor2", "01 3D");
    openXCToOBD2Map.put("catalyst_temperature_bank2_sensor1", "01 3E");
    openXCToOBD2Map.put("catalyst_temperature_bank2_sensor2", "01 3F");
    openXCToOBD2Map.put("monitor_status_this_drive_cycle", "01 41");
    openXCToOBD2Map.put("control_module_voltage", "01 42");
    openXCToOBD2Map.put("absolute_load", "01 43");
    openXCToOBD2Map.put("fuel_air_commanded_equivalence_ratio", "01 44");
    openXCToOBD2Map.put("relative_throttle_position", "01 45");
    openXCToOBD2Map.put("ambient_air_temperature", "01 46");
    openXCToOBD2Map.put("absolute_throttle_position_b", "01 47");
    openXCToOBD2Map.put("absolute_throttle_position_c", "01 48");
    openXCToOBD2Map.put("absolute_throttle_position_d", "01 49");
    openXCToOBD2Map.put("accelerator_pedal_position", "01 49"); //openxc
    openXCToOBD2Map.put("absolute_throttle_position_e", "01 4A");
    openXCToOBD2Map.put("absolute_throttle_position_f", "01 4B");
    openXCToOBD2Map.put("commanded_throttle_actuator", "01 4C");
    openXCToOBD2Map.put("time_run_with_mil_on", "01 4D");
    openXCToOBD2Map.put("time_since_trouble_codes_cleared", "01 4E");
    openXCToOBD2Map.put("maximum_value__equivalence_ratio_oxygen_sensor"
    		+"_voltage_oxygen_sensor current_ and_intake_manifold_absolute_pressure", "01 4F");
    openXCToOBD2Map.put("maximum_value_for_air_flow_rate_from_mass_air_flow_sensor", "01 50");
    openXCToOBD2Map.put("fuel_type", "01 51");
    
    openXCToOBD2Map.put("ethanol_fuel_percent", "01 52");
    openXCToOBD2Map.put("absolute_evap_system_vapor_pressure", "01 53");
    openXCToOBD2Map.put("evap_system_vapor_pressure", "01 54");
    openXCToOBD2Map.put("short_term_secondary_oxygen_sensor_trim_bank1_and_bank3", "01 55");
    openXCToOBD2Map.put("long_term_secondary_oxygen_sensor_trim_bank1_and_bank3", "01 56");
    openXCToOBD2Map.put("short_term_secondary_oxygen_sensor_trim_bank2_and_bank4", "01 57");
    openXCToOBD2Map.put("long_term_secondary_oxygen_sensor_trim_bank2_and_bank4", "01 58");  
    
    openXCToOBD2Map.put("Fuel_rail_pressure_absolute", "01 59");
    openXCToOBD2Map.put("relative_accelerator_pedal_position", "01 5A");
    openXCToOBD2Map.put("hybrid_battery_pack_remaining_life", "01 5B");
    openXCToOBD2Map.put("engine_oil_temperature", "01 5C");
    openXCToOBD2Map.put("fuel_injection_timing", "01 5D");
    openXCToOBD2Map.put("fuel_consumption_rate", "01 5E"); //openXC
    openXCToOBD2Map.put("engine_fuel_rate", "01 5E");
    openXCToOBD2Map.put("emission_requirements_to_which_vehicle_is_designed", "01 5F");
    openXCToOBD2Map.put("drivers_demand_engine_percent_torque", "01 61");
    openXCToOBD2Map.put("actual_engine_percentage_torque", "01 62");
    openXCToOBD2Map.put("engine_reference_torque", "01 63");  // Nm
    openXCToOBD2Map.put("engine_percent_torque_data", "01 64");
    openXCToOBD2Map.put("auxiliary_input_output_supported", "01 65");
    openXCToOBD2Map.put("mass_air_flow_sensor", "01 66");
    openXCToOBD2Map.put("engine_coolant_temperature", "01 67");
    openXCToOBD2Map.put("intake_air_temperature_sensor", "01 68");
    openXCToOBD2Map.put("commanded_egr_and_egr_error", "01 69");
    openXCToOBD2Map.put("commanded_diesel_intake_air_flow_control_and_relative_intake"
    		+ "_air_flow position", "01 6A");
    openXCToOBD2Map.put("exhaust_gas_recirculation_temperature", "01 6B");
    openXCToOBD2Map.put("commanded_throttle_actuator_control_and_relative"
    		+ "_throttle_position", "01 6C");
    openXCToOBD2Map.put("Fuel_pressure_control_system", "01 6D");
    openXCToOBD2Map.put("injection_pressure_control_system", "01 6E");
    openXCToOBD2Map.put("turbocharger_compressor_inlet_pressure", "01 6F");
    openXCToOBD2Map.put("boost_pressure_control", "01 70");
    openXCToOBD2Map.put("variable_geometry_turbo_control", "01 71");
    openXCToOBD2Map.put("wastegate_control", "01 72");
    openXCToOBD2Map.put("exhaust_pressure", "01 73");
    openXCToOBD2Map.put("turbocharger_rpm", "01 74");
    openXCToOBD2Map.put("turbocharger_temperature", "01 75");
    openXCToOBD2Map.put("turbocharger_temperature", "01 76");
    openXCToOBD2Map.put("charge_air_cooler_temperature", "01 77");
    openXCToOBD2Map.put("exhaust_gas_temperature_bank1", "01 78");
    openXCToOBD2Map.put("exhaust_gas_temperature_bank2", "01 79");
    openXCToOBD2Map.put("diesel_particulate_filter", "01 7A"); //AND 7B?
    openXCToOBD2Map.put("diesel_particulate_filter_temperature", "01 7C");
    openXCToOBD2Map.put("nox_nte_control_area_status", "01 7D");
    openXCToOBD2Map.put("pm_nte_control_area_status", "01 7E");
    openXCToOBD2Map.put("engine_run_time", "01 7F");
    openXCToOBD2Map.put("engine_run_time_for_auxiliary"
    		+ "_emissions_control_device", "01 81"); //AND 82?
    openXCToOBD2Map.put("nox_sensor", "01 83");
    openXCToOBD2Map.put("manifold_surface_temperature", "01 84");
    openXCToOBD2Map.put("nox_reagent_system", "01 85");
    openXCToOBD2Map.put("particulate_matter_sensor", "01 86");
    openXCToOBD2Map.put("intake_manifold_absolute_pressure", "01 87");
    //+Manufacturespecific PIDs ?
  }
  
  /**
   * For Testing only: adds some openXCKeys to value-Hashtable
   */
  public static void initCar2XEntries() {
    car2xEntries.put("vehicle_speed", new Car2XEntry());
    car2xEntries.put("engine_speed", new Car2XEntry());
    car2xEntries.put("steering_wheel_angle", new Car2XEntry());
    car2xEntries.put("torque_at_transmission", new Car2XEntry());
    car2xEntries.put("accelerator_pedal_position", new Car2XEntry());
    car2xEntries.put("parking_brake_status", new Car2XEntry());
    car2xEntries.put("brake_pedal_status", new Car2XEntry());
    car2xEntries.put("transmission_gear_position", new Car2XEntry());
    car2xEntries.put("gear_lever_position", new Car2XEntry());
    car2xEntries.put("odometer", new Car2XEntry());
    car2xEntries.put("ignition_status", new Car2XEntry());
    car2xEntries.put("fuel_level", new Car2XEntry());
    car2xEntries.put("fuel_consumed_since_restart", new Car2XEntry());
    car2xEntries.put("door_status", new Car2XEntry());
    car2xEntries.put("headlamp_status", new Car2XEntry());
    car2xEntries.put("high_beam_status", new Car2XEntry());
    car2xEntries.put("windshield_wiper_status", new Car2XEntry());
    car2xEntries.put("latitude", new Car2XEntry());
    car2xEntries.put("longitude", new Car2XEntry());
    car2xEntries.put("battery_status", new Car2XEntry());
    car2xEntries.put("absolute_load", new Car2XEntry());
    car2xEntries.put("runtime_since_last_start", new Car2XEntry());
    car2xEntries.put("timing_advanced", new Car2XEntry());
    car2xEntries.put("engine_oil_temperature", new Car2XEntry());
    car2xEntries.put("drivers_demand_engine", new Car2XEntry());
    car2xEntries.put("actual_engine", new Car2XEntry());
    car2xEntries.put("engine_reference", new Car2XEntry());
    car2xEntries.put("fuel_consumption_rate", new Car2XEntry());
    car2xEntries.put("barometric_pressure", new Car2XEntry());
    car2xEntries.put("fuel_pressure", new Car2XEntry());
    car2xEntries.put("Intake_manifold_absolute_pressure", new Car2XEntry());
    car2xEntries.put("air_intake_temperature", new Car2XEntry());
    car2xEntries.put("ambient_air_temperature", new Car2XEntry());
    car2xEntries.put("engine_coolant_temperature", new Car2XEntry());
    car2xEntries.put("throttle_position", new Car2XEntry()); 

    Enumeration<String> enumKey = car2xEntries.keys();
    while(enumKey.hasMoreElements()) {
      String key = enumKey.nextElement();
      car2xEntries.get(key).setOBD2key(openXCToOBD2Map.get(key));
    }
  }
 
  /**
   * 
   * @param map
   * @param value
   * @return
   */
  public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
	    for (Entry<T, E> entry : map.entrySet()) {
	        if (value.equals(entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
  /**
   * 
   * @param input the String to check
   * @return true if hex or starts with AT
   * primarily to check the Command sending to ELM327
   */
  public static boolean checkIfHexOrAT(String input){
	  String rawData = input.replaceAll("\\s", "");
	    if (rawData.matches("([0-9A-F]{2})+") || (rawData.startsWith("AT"))) {
	    	return true;
	    } else {
	    	return false;
	    }	  
  }
   
  /**
   * @param reply The raw Reply
   * @return the as possible calculated and converted reply
   * see http://en.wikipedia.org/wiki/OBD-II_PIDs
   * If percentage wanted: 2 Hex-Bytes means 00 - FF = 0 - 255.
   * To get the percentage: Data/255*100 
   */
  public static String convertOBD2Reply(String reply) {
	    String result;
	    if (!((reply.replaceAll("\\s+","")).matches("([0-9A-F]{2})+"))){
	      result = reply;
	    } else {
	    	String[] data = reply.split("\\s"); // (response =
	    	// "41 0C 0C FC") 41,0C,0C,FC, data[0] = Bus, data[1] = Command
	    	int value1;
	    	int value2;
	    	int resultInt;
	    	float resultFloat;
	    	switch (data[1]) {
	    	case "04":
	    		resultFloat = (float) (new Integer(
	    				Integer.parseInt(data[2], 16) * 100));
	    		result = Float.toString(resultFloat/255);
	    		break;
	    	case "05":
	    		value1 = Integer.parseInt(data[2], 16) - 40;
	    		result = Integer.toString(value1);
	    		break;
	    	case "06":
	    		value1 = (Integer.parseInt(data[2],16) -128) * 100/128;
	    		result = Integer.toString(value1);
	    		break;
	    	case "07":
	    		value1 = (Integer.parseInt(data[2],16) -128) * 100/128;
	    		result = Integer.toString(value1);
	    		break;
	    	case "08":
	    		value1 = (Integer.parseInt(data[2],16) -128) * 100/128;
	    		result = Integer.toString(value1);
	    		break;
	    	case "09":
	    		value1 = (Integer.parseInt(data[2],16) -128) * 100/128;
	    		result = Integer.toString(value1);
	    		break;
	    	case "0A": // kPa
	    		value1 = Integer.parseInt(data[2], 16) * 3;
	    		result = Integer.toString(value1);
	    		break;
	    	case "0B":
	    		result = Integer.toString(Integer.parseInt(data[2], 16));
	    		break;
	    	case "0C": // ((A*256)+B)/4
	    		value1 = Integer.parseInt(data[2], 16);
	    		value2 = Integer.parseInt(data[3], 16);
	    		resultInt = ((value1 * 256) + value2) / 4;
	    		result = Integer.toString(resultInt);
	    		break;
	    	case "0D":
	    		value1 = Integer.parseInt(data[2], 16);
	    		result = Integer.toString(value1);
	    		break;
	    	case "0E": // relative to #1 cylinder
	    		result = Integer
	    		.toString((Integer.parseInt(data[2], 16) - 128) / 2);
	    		break;
	    	case "0F":
	    		value1 = Integer.parseInt(data[2], 16) - 40;
	    		result = Integer.toString(value1);
	    		break;
	    	case "10":
	    		value1 = Integer.parseInt(data[2], 16);
	    		value2 = Integer.parseInt(data[3], 16);
	    		resultInt = (((value1 * 256) + value2)/100);
	    		result = Integer.toString(resultInt);
	    		break;
	    	case "11":
	    		resultFloat = (float) (new Integer(
	    				Integer.parseInt(data[2], 16) * 100));
	    		result = Float.toString(resultFloat/255);
	    		break;
	    	case "1F":
	    		value1 = Integer.parseInt(data[2], 16);
	    		value2 = Integer.parseInt(data[3], 16);
	    		resultInt = ((value1 * 256) + value2);
	    		result = Integer.toString(resultInt);
	    		break;
	    	case "21":
	    		value1 = Integer.parseInt(data[2], 16);
	    		value2 = Integer.parseInt(data[3], 16);
	    		resultInt = ((value1 * 256) + value2);
	    		result = Integer.toString(resultInt);
	    		break;
	    	case "22":
	    		value1 = Integer.parseInt(data[2], 16);
	    		value2 = Integer.parseInt(data[3], 16);
	    		resultFloat = (((value1 * 256) + value2)*0.079f);
	    		result = Float.toString(resultFloat);
	    		break;
	    	case "23":
	    		value1 = Integer.parseInt(data[2], 16);
	    		value2 = Integer.parseInt(data[3], 16);
	    		resultInt = (((value1 * 256) + value2)*10);
	    		result = Integer.toString(resultInt);
	    		break;
	    	case "2C":
	    		value1 = Integer.parseInt(data[2], 16);
	    		resultFloat = ((value1*100)/255);
	    		result = Float.toString(resultFloat);
	    		break;
	    	case "2D":
	    		value1 = Integer.parseInt(data[2], 16);
	    		resultFloat = ((value1-128)*(100/128));
	    		result = Float.toString(resultFloat);
	    		break;
	    	case "2E":
	    		resultFloat = (float) (new Integer(
	    				Integer.parseInt(data[2], 16) * 100));
	    		result = Float.toString(resultFloat/255);
	    		break;
	    	case "2F":
	    		resultFloat = (float) (new Integer(
	    				Integer.parseInt(data[2], 16) * 100));
	    		result = Float.toString(resultFloat/255);
	    		break;
	    	case "31":
	    		value1 = Integer.parseInt(data[2], 16);
	    		value2 = Integer.parseInt(data[3], 16);
	    		resultInt = (value1*256)+value2;
	    		result = Integer.toString(resultInt);
	    		break;
	    	case "33": // kPa
	    		result = Integer.toString(Integer.parseInt(data[2], 16));
	    		break;
	    	case "42":
	    		value1 = Integer.parseInt(data[2], 16);
	    		value2 = Integer.parseInt(data[3], 16);
	    		resultFloat = ((value1*256)+value2)/1000;
	    		result = Float.toString(resultFloat);
	    		break;
	    	case "43":
	    		value1 = Integer.parseInt(data[2], 16);
	    		value2 = Integer.parseInt(data[3], 16);
	    		resultInt = ((value1 * 256) + value2) * 100;
	    		resultFloat = (float)resultInt/255;
	    		result = Float.toString(resultFloat);
	    		break;
	    	case "44":
	    		value1 = Integer.parseInt(data[2], 16);
	    		value2 = Integer.parseInt(data[3], 16);
	    		resultFloat = ((value1*256)+value2)/32768;
	    		result = Float.toString(resultFloat);
	    		break;
	    	case "45":
	    		resultFloat = (float) (new Integer(
	    				Integer.parseInt(data[2], 16) * 100));
	    		result = Float.toString(resultFloat/255);
	    		break;
	    	case "46":
	    		value1 = Integer.parseInt(data[2], 16) - 40;
	    		result = Integer.toString(value1);
	    		break;
	    	case "47":
	    		resultFloat = (float) (new Integer(
	    				Integer.parseInt(data[2], 16) * 100));
	    		result = Float.toString(resultFloat/255);
	    		break;
	    	case "48":
	    		resultFloat = (float) (new Integer(
	    				Integer.parseInt(data[2], 16) * 100));
	    		result = Float.toString(resultFloat/255);
	    		break;
	    	case "49":
	    		resultFloat = (float) (new Integer(
	    				Integer.parseInt(data[2], 16) * 100));
	    		result = Float.toString(resultFloat/255);
	    		break;
	    	case "4A":
	    		resultFloat = (float) (new Integer(
	    				Integer.parseInt(data[2], 16) * 100));
	    		result = Float.toString(resultFloat/255);
	    		break;
	    	case "4B":
	    		resultFloat = (float) (new Integer(
	    				Integer.parseInt(data[2], 16) * 100));
	    		result = Float.toString(resultFloat/255);
	    		break;
	    	case "4C":
	    		resultFloat = (float) (new Integer(
	    				Integer.parseInt(data[2], 16) * 100));
	    		result = Float.toString(resultFloat/255);
	    		break;
	    	case "4D":
	    		value1 = Integer.parseInt(data[2]);
	    		value2 = Integer.parseInt(data[3]);
	    		resultInt = (value1*256)+value2;
	    		result = Integer.toString(resultInt);
	    		break;
	    	case "4E":
	    		value1 = Integer.parseInt(data[2]);
	    		value2 = Integer.parseInt(data[3]);
	    		resultInt = (value1*256)+value2;
	    		result = Integer.toString(resultInt);
	    		break;
	    	case "52":
	    		resultFloat = (float) (new Integer(
	    				Integer.parseInt(data[2], 16) * 100));
	    		result = Float.toString(resultFloat/255);
	    		break;
	    	case "53":
	    		value1 = Integer.parseInt(data[2]);
	    		value2 = Integer.parseInt(data[3]);
	    		resultFloat = ((value1*256) + value2)/200;
	    		result = Float.toString(resultFloat);
	    		break;
	    	case "5C":
	    		result = Integer
	    		.toString(Integer.parseInt(data[2], 16) - 40);
	    		break;
	    	case "5E": // engine_fuel_rate
	    		value1 = Integer.parseInt(data[2], 16);
	    		value2 = Integer.parseInt(data[3], 16);
	    		resultInt = ((value1 * 256) + value2) / 20;
	    		result = Integer.toString(resultInt);
	    		break;
	    	case "61": // percentage torque
	    		result = Integer
	    		.toString(Integer.parseInt(data[2], 16) - 125);
	    		break;
	    	case "62": // percentage torque
	    		result = Integer
	    		.toString(Integer.parseInt(data[2], 16) - 125);
	    		break;
	    	case "63": // torque Nm
	    		value1 = Integer.parseInt(data[2], 16);
	    		value2 = Integer.parseInt(data[3], 16);
	    		resultInt = ((value1 * 256) + value2);
	    		result = Integer.toString(resultInt);
	    		break;
	    	default:
	    		result = reply;
	    		break;
	    	}
	    }

	    return result;

	  }
  
 
  /**
   * 
   * @param in Inputstream
   * @param pwriter Outputstream
   * @return ArrayList<String> with all supported 01 PIDs (e.g. " 01 00"," 01 1F") 
   * Example: 
   * 41 00 98 18 00 01 = 10011000000110000000000000000001
   * 41 20 00 01 80 01 = 00000000000000011000000000000001
   * 41 40 C0 80 00 00 = 11000000100000000000000000000000 -> last Bit 0 -> no more PIDs
   * = all supported PIDs = 00,03,04,0B,0C,1F,2F,30,3F,40,41,(48)
   * 
   * 00!, (03), 04!,[05,] (0B), 0C!,[0D,] 1F!, 20!, 21!, 30!, 40!, 41!, (48)
   * 
   */
  public static List<String> getSupportedPIDs(BufferedReader in, PrintWriter pwriter){
	  List<String> results = new ArrayList<String>();
	  String hexInput;
	  String binInput;
	  String doubleCheck;
	  hexInput = run("01 00", in, pwriter);
	  //delete whitespaces 
	  hexInput=hexInput.replaceAll("\\s+","");
	  //cut the first 4 Bytes
	  hexInput = hexInput.substring(4); 
	  
	  if (hexInput.matches("([0-9A-F]{2})+")){
		  binInput = new BigInteger(hexInput, 16).toString(2);
		  while (binInput.length() < hexInput.length()){
			  //fill leading Zeros
			  binInput = "0"+binInput;
		  }
		  for (int i = 0; i < binInput.length(); i++) {
			  char c = binInput.charAt(i);
			  if (c == '1'){
				  String hex = Integer.toHexString(i+1);
				  hex=hex.toUpperCase();
				  if(hex.length() == 1){
					  hex = "0"+hex;
				  }
				  results.add("01 " + hex);
			  }
		  }			
		  if (binInput.charAt(binInput.length()-1) == '1'){ //if there are more supported PIDs
			  getMoreSupportedPIDs(results, 0 ,in, pwriter);
		  }
	  }
	  
	  //check whether pids really give correct response
	  for (String result : results) {
		doubleCheck = run(result,in,pwriter);
		doubleCheck = doubleCheck.replaceAll("\\s+","");
		if (!doubleCheck.matches("([0-9A-F]{2})+")){
			results.remove(result);
		}
	}
	  return results;
  }
  
  /**
   * 
   * @param results ArrayList
   * @param cmd last used command(00,20,40..)
   * @param in Inputstream
   * @param pwriter Outputstream
   * Recursively adds remaining supported PIDs to result
   */
  private static void getMoreSupportedPIDs(List<String> results, Integer cmd,
		BufferedReader in, PrintWriter pwriter) {
	  Integer cmdRange = cmd + 20;
	  Integer cmdRange2 = cmd + 30;
	  String newCmd = "01 " + cmdRange.toString();
	  String newCmd2 = cmdRange2.toString();
	  String hexInput;
	  String binInput;
	  hexInput = run(newCmd, in, pwriter);
	  //delete whitespaces 
	  hexInput = hexInput.replaceAll("\\s+","");
	  //cut the first 4 Bytes
	  hexInput = hexInput.substring(4);
	  if (hexInput.matches("([0-9A-F]{2})+")){
		  binInput = new BigInteger(hexInput, 16).toString(2);
		  while (binInput.length() < hexInput.length()){
			  //fill leading Zeros
			  binInput = "0"+binInput;
		  }
		  for (int i = 0; i < binInput.length(); i++) {
			  char c = binInput.charAt(i);
			  if (c == '1'){
				  String hex = Integer.toHexString(i+1);
				  hex=hex.toUpperCase();
				  if(hex.length() == 1){ //F = 3F
					  hex = newCmd.charAt(3) + hex;
				  } else { //1F but really is 3F
					  hex = Character.toString(newCmd2.charAt(0)) + hex.charAt(1);
				  }
				  results.add("01 " + hex);
			  }
		  }			
		  if (binInput.charAt(binInput.length()-1) == '1'){ //if there are more supported PIDs
			  getMoreSupportedPIDs(results, cmd + 20  ,in, pwriter);
		  }
	  }
	  
}
  
  /**
   * 
   * @param command the command to run
   * @param in Inputstream
   * @param pwriter Outputstream
   * @return raw reply
   */
public static String run(String command, BufferedReader in, PrintWriter pwriter) {
	  pwriter.write(command + "\r");
      pwriter.flush();
      
      try {
		Thread.sleep(200);
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
      byte bytes = 0;
      StringBuilder res = new StringBuilder();
      String rawData = null;

      // read until '>' arrives
      try {
		while ((char) (bytes = (byte) in.read()) != '>') {
		    res.append((char) bytes);
		  }
	} catch (IOException e) {
		e.printStackTrace();
	}
       //ELM sends like this: 41 0F 05 with whitespaces
      rawData = res.toString().trim();

      //no "WAITING" or "INIT BUS", just the data itself
      rawData = rawData.substring(rawData.lastIndexOf(13) + 1);
      
      return rawData;
  }

/**
 * 
 * @param type The expected type of the reply
 * @param reply The raw Reply
 * @return the as possible calculated and converted reply
 * see http://en.wikipedia.org/wiki/OBD-II_PIDs
 * If percentage wanted: 2 Hex-Bytes means 00 - FF = 0 - 255.
 * To get the percentage: Data/255*100 
 * works but will be replaced by convertOBD2Reply
 */
public static String convertOBD2ReplyToOpenXC(String type, String reply) {
  String result;
  String replyTrimmend = reply.replaceAll("\\s+","");
  if (replyTrimmend.equals("NO DATA") || replyTrimmend.equals("OK") || replyTrimmend.startsWith("BUS")) {
    result = reply;
  } else {
   String[] data = reply.split("\\s"); // cut first 4 bytes (response =
  	// "41 0C 0C FC") 41,0C,0C,FC
  	int value1;
  	int value2;
  	int resultInt;
  	float resultFloat;
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
  		resultFloat = (float) (new Integer(
  				Integer.parseInt(data[2], 16) * 100));
  		result = Float.toString(resultFloat/255);
  		break;
  	case "fuel_level":
  		resultFloat = (float) (new Integer(
  				Integer.parseInt(data[2], 16) * 100));
  		result = Float.toString(resultFloat/255);
  		break;
  	case "battery_status": // 11.8V
  		result = reply;
  		break;
  	case "absolute_load":
  		value1 = Integer.parseInt(data[2], 16);
  		value2 = Integer.parseInt(data[3], 16);
  		resultInt = ((value1 * 256) + value2) * 100;
  		resultFloat = (float)resultInt/255;
  		result = Float.toString(resultFloat);
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
  		resultFloat = (float) (new Integer(
  				Integer.parseInt(data[2], 16) * 100));
  		result = Float.toString(resultFloat/255);
  		break;
  	default:
  		result = reply;
  		break;
  	}
  }

  return result;

}

}