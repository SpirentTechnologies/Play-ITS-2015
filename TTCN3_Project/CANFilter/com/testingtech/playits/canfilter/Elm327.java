package com.testingtech.playits.canfilter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom.output.EscapeStrategy;

import com.testingtech.playits.canfilter.bluetooth.ELMBluetooth;
import com.testingtech.playits.canfilter.rs232.ELMRS232;

public class Elm327 {
  private static final String TWO_HEX_BYTES = "([0-9A-F]{2})+";
  private static int RS232 = 0;
  private static int BLUETOOTH = 1;
  private static int usedConnection;

  public static Hashtable<String, String> openXCToOBD2Map = new Hashtable<String, String>();

  public static Hashtable<String, Car2XEntry> car2xEntries = new Hashtable<>();

  /**
   * 
   * @param cmd
   * @return see http://en.wikipedia.org/wiki/OBD-II_PIDs adds possible
   *         available PID Codes
   */
  public static void initOpenXCToOBD2Map() {
    // TODO Error-Handling if there is no OBD2 Key(here 00)

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

    openXCToOBD2Map.put("battery_status", "AT RV"); // AT Command

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
    openXCToOBD2Map.put("timing_advanced", "01 0E"); // relative to #1
    // cylinder
    openXCToOBD2Map.put("air_intake_temperature", "01 0F");
    openXCToOBD2Map.put("maf_air_flow_rate", "01 10");
    openXCToOBD2Map.put("throttle_position", "01 11");
    openXCToOBD2Map.put("commanded_secondary_air_status", "01 12");
    openXCToOBD2Map.put("oxygen_sensor_present", "01 13"); // or 1D ?
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
    openXCToOBD2Map
        .put("distance_traveled_with_malfunction_indicator_lamp_on",
            "01 21");
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
    openXCToOBD2Map.put("fuel_level", "01 2F"); // fuel_level_input
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
    openXCToOBD2Map.put("accelerator_pedal_position", "01 49"); // openxc
    openXCToOBD2Map.put("absolute_throttle_position_e", "01 4A");
    openXCToOBD2Map.put("absolute_throttle_position_f", "01 4B");
    openXCToOBD2Map.put("commanded_throttle_actuator", "01 4C");
    openXCToOBD2Map.put("time_run_with_mil_on", "01 4D");
    openXCToOBD2Map.put("time_since_trouble_codes_cleared", "01 4E");
    openXCToOBD2Map
        .put("maximum_value__equivalence_ratio_oxygen_sensor"
            + "_voltage_oxygen_sensor current_ and_intake_manifold_absolute_pressure",
            "01 4F");
    openXCToOBD2Map.put(
        "maximum_value_for_air_flow_rate_from_mass_air_flow_sensor",
        "01 50");
    openXCToOBD2Map.put("fuel_type", "01 51");
    openXCToOBD2Map.put("ethanol_fuel_percent", "01 52");
    openXCToOBD2Map.put("absolute_evap_system_vapor_pressure", "01 53");
    openXCToOBD2Map.put("evap_system_vapor_pressure", "01 54");
    openXCToOBD2Map.put(
        "short_term_secondary_oxygen_sensor_trim_bank1_and_bank3",
        "01 55");
    openXCToOBD2Map.put(
        "long_term_secondary_oxygen_sensor_trim_bank1_and_bank3",
        "01 56");
    openXCToOBD2Map.put(
        "short_term_secondary_oxygen_sensor_trim_bank2_and_bank4",
        "01 57");
    openXCToOBD2Map.put(
        "long_term_secondary_oxygen_sensor_trim_bank2_and_bank4",
        "01 58");
    openXCToOBD2Map.put("Fuel_rail_pressure_absolute", "01 59");
    openXCToOBD2Map.put("relative_accelerator_pedal_position", "01 5A");
    openXCToOBD2Map.put("hybrid_battery_pack_remaining_life", "01 5B");
    openXCToOBD2Map.put("engine_oil_temperature", "01 5C");
    openXCToOBD2Map.put("fuel_injection_timing", "01 5D");
    openXCToOBD2Map.put("fuel_consumption_rate", "01 5E"); // openXC
    openXCToOBD2Map.put("engine_fuel_rate", "01 5E");
    openXCToOBD2Map.put(
        "emission_requirements_to_which_vehicle_is_designed", "01 5F");
    openXCToOBD2Map.put("drivers_demand_engine_percent_torque", "01 61");
    openXCToOBD2Map.put("actual_engine_percentage_torque", "01 62");
    openXCToOBD2Map.put("engine_reference_torque", "01 63"); // Nm
    openXCToOBD2Map.put("engine_percent_torque_data", "01 64");
    openXCToOBD2Map.put("auxiliary_input_output_supported", "01 65");
    openXCToOBD2Map.put("mass_air_flow_sensor", "01 66");
    openXCToOBD2Map.put("engine_coolant_temperature", "01 67");
    openXCToOBD2Map.put("intake_air_temperature_sensor", "01 68");
    openXCToOBD2Map.put("commanded_egr_and_egr_error", "01 69");
    openXCToOBD2Map.put(
        "commanded_diesel_intake_air_flow_control_and_relative_intake"
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
    openXCToOBD2Map.put("diesel_particulate_filter", "01 7A"); // AND 7B?
    openXCToOBD2Map.put("diesel_particulate_filter_temperature", "01 7C");
    openXCToOBD2Map.put("nox_nte_control_area_status", "01 7D");
    openXCToOBD2Map.put("pm_nte_control_area_status", "01 7E");
    openXCToOBD2Map.put("engine_run_time", "01 7F");
    openXCToOBD2Map.put("engine_run_time_for_auxiliary"
        + "_emissions_control_device", "01 81"); // AND 82?
    openXCToOBD2Map.put("nox_sensor", "01 83");
    openXCToOBD2Map.put("manifold_surface_temperature", "01 84");
    openXCToOBD2Map.put("nox_reagent_system", "01 85");
    openXCToOBD2Map.put("particulate_matter_sensor", "01 86");
    openXCToOBD2Map.put("intake_manifold_absolute_pressure", "01 87");
    // +Manufacturespecific PIDs ?
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
   * @param input
   *            the String to check
   * @return true if hex or starts with AT primarily to check the Command
   *         sending to ELM327
   */
  public static boolean checkIfHexOrAT(String input) {
    String rawData = input.replaceAll("\\s", "");
    return rawData.matches(TWO_HEX_BYTES) || (rawData.startsWith("AT"));
  }

  /**
   * @param response
   *            The raw response
   * @return the as possible calculated and converted reply see
   *         http://en.wikipedia.org/wiki/OBD-II_PIDs If percentage wanted: 2
   *         Hex-Bytes means 00 - FF = 0 - 255. To get the percentage:
   *         Data/255*100
   */
  public static String convertOBD2Response(String response) {
    Float result;
    if (!((response.replaceAll("\\s+", "")).matches(TWO_HEX_BYTES))) {
      return response;
    } else {
      String[] data = response.split("\\s"); // (response =
      // "41 0C 0C FC") 41,0C,0C,FC, data[0] = Bus, data[1] = Command
      int value1 = Integer.parseInt(data[2], 16);
      int value2 = Integer.parseInt(data[3], 16);
      switch (data[1]) {
      case "06":
      case "07":
      case "08":
      case "09":
      case "2D":
        result = (value1 - 128) * 100 / 128f;
        break;
      case "0A": // kPa
        result = value1 * 3f;
        break;
      case "0C": // ((A*256)+B)/4
    	  result = (value1 * 256 + value2) / 4f;
        break;
      case "33": // kPa
      case "0B":
      case "0D":
        result = (float) value1;
        break;
      case "0E": // relative to #1 cylinder
        result = (value1 - 128) / 2f;
        break;
      case "46":
      case "05":
      case "0F":
      case "5C":
        result = value1 - 40f;
        break;
      case "10":
    	  result = (value1 * 256 + value2) / 100f;
    	  break;
      case "21":
      case "1F":
      case "31":
      case "4D":
      case "4E":
      case "63": // torque Nm
    	  result = value1 * 256f + value2;
        break;
      case "22":
        result = (value1 * 256 + value2) * 0.079f;
        break;
      case "23":
        result = (value1 * 256 + value2) * 10f;
        break;
      case "42":
        result = (value1 * 256 + value2) / 1000f;
        break;
      case "43":
        result = (value1 * 256 + value2) * 100 / 255f;
        break;
      case "44":
        result = (value1 * 256 + value2) / 32768f;
        break;
      case "04":
      case "11":
      case "2C":
      case "2E":
      case "2F":
      case "45":
      case "47":
      case "48":
      case "49":
      case "4A":
      case "4B":
      case "4C":
      case "52":
        result = value1 * 100 / 255f;
        break;
      case "53":
        result = (value1 * 256 + value2) / 200f;
        break;
      case "5E": // engine_fuel_rate
    	  result = (value1 * 256 + value2) / 20f;
        break;
      case "61": // percentage torque
      case "62": // percentage torque
        result = value1 - 125f;
        break;
      default:
        return response;
      }
    }
    return Float.toString(result);
  }

  /**
   * 
   * @return ArrayList<String> with all supported 01 PIDs (e.g.
   *         " 01 00"," 01 1F") Example: 41 00 98 18 00 01 =
   *         10011000000110000000000000000001 41 20 00 01 80 01 =
   *         00000000000000011000000000000001 41 40 C0 80 00 00 =
   *         11000000100000000000000000000000 -> last Bit 0 -> no more PIDs =
   *         all supported PIDs = 00,04,05,0C,0D,1F,2F,31,3F,40,41,(48)
   *         [04,05,0C,0D,21,22,30,31,42,49]
   * 
   */
  public static List<String> getSupportedPIDs() {
    List<String> pids = new ArrayList<String>();
    for (int i = 0; i < 8; i += 2) {
      String binaryPIDs = getBinaryPIDs(String.valueOf(i) + "0");
      if (binaryPIDs.length() > 1) {
        pids.addAll(getPIDs(binaryPIDs));
        if (binaryPIDs.charAt(binaryPIDs.length() - 1) == '0') {
          break;
      }
    }
    }
    return pids;
  }

  private static Collection<? extends String> getPIDs(String binaryPIDs) {
    List<String> pids = new ArrayList<String>();
    for (int i = 0; i < binaryPIDs.length(); i++) { 
        if (binaryPIDs.charAt(i) == '1') {
          pids.add("01 " + intPositionInBinaryField2Hex(i+1));
        }
      }
      doubleCheck(pids);
    return pids;
  }

  /**
   * 
   * @param hexadecimalPIDs
   * @return the given hex String to Binary String
   */
  private static String getBinaryPIDs(String range) {
	  String hexadecimalPIDs = getHexadecimalPIDs(range);
	  String binaryPIDs = "";
    if (hexadecimalPIDs.matches(TWO_HEX_BYTES)) {
      binaryPIDs = new BigInteger(hexadecimalPIDs, 16).toString(2);
      while (binaryPIDs.length() < hexadecimalPIDs.length()) {
        // fill leading Zeros
        binaryPIDs = "0" + binaryPIDs;
      }
    }
    return binaryPIDs;
  }

  /**
   * 
   * @param i position in binary field
   * @return hex value of that position
   */
  private static String intPositionInBinaryField2Hex(int i) {
    String hex = Integer.toHexString(i + 1);
    hex = hex.toUpperCase();
    if (hex.length() == 1) {
      hex = "0" + hex;
    }
    return hex;
  }
  
  /**
   * Get the raw pids, extract the data itself, cuts the whitespaces and returns it
   * @param range e.g. 20 --> 01 20
   * @return the supported 01 PIDs for the given range
   */
  private static String getHexadecimalPIDs(String range) {
    String rawResponse = getRawPIDs(range);
    // delete whitespaces
    rawResponse = rawResponse.replaceAll("\\s+", "");
    // cut the first 4 Bytes
    rawResponse = rawResponse.substring(4);
    return rawResponse;
  }

/**
 * Gets the raw supported PIDs
 * e.g. 01 00, 01 20, 01 40..
 * @param range
 * @return
 */
private static String getRawPIDs(String range) {
	String rawResponse = "";
    if (usedConnection == BLUETOOTH) {
      rawResponse = ELMBluetooth.run("01 " + range);
    } else if (usedConnection == RS232) {
      rawResponse = ELMRS232.run("01 " + range);
    }
	return rawResponse;
}

  /**
   * Checks whether PIDs really give a correct response.
   * 
   * @param pids
   */
  private static void doubleCheck(List<String> pids) {
    String doubleCheck;
    for (String pid : pids) {
      doubleCheck = run(pid);
      if (!doubleCheck.matches(TWO_HEX_BYTES)) {
        pids.remove(pid);
      }
    }
  }
  
  /**
   * runs the given Command using the predefined ELM-Connection
   * @param cmd
   * @return
   */
  private static String run(String cmd) {
    String doubleCheck = "";
    if (usedConnection == BLUETOOTH) {
      doubleCheck = ELMBluetooth.run(cmd);
    } else if (usedConnection == RS232) {
      doubleCheck = ELMRS232.run(cmd);
    }
    return doubleCheck.replaceAll("\\s+", "");
  }
}