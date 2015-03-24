package com.testingtech.playits.canfilter;

public class Car2XEntry {
  // Attributes
  private String obd2key = "0D"; // default value
  private String valueA;
  private String valueB;
  private long timestamp;

  /**
   * Backup key for communicating with ELM327
   * @return hexadecimal obd-2 key
   */
  public String getObd2key() {
    return obd2key;
  }

  /**
   * Date in millis according to the last time 
   * valueA and valueB where updated.
   * @return a timestamp
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * Each key has a valueA, respresented as String
   * @return first value of a given key
   */
  public String getValueA() {
    return valueA;
  }

  /**
   * Optional value representing true or false in case of an event
   * @return true or false coded as String
   */
  public String getValueB() {
    return valueB;
  }

  /**
   * Name of valueA read from openXC simulator 
   * @param key two byte hexadecimal String
   */
  public void setOpenxckey(String key) {
    this.obd2key = key;
  }

  /**
   * Backup key for communicating with ELM327
   * @param key two byte hexadecimal String
   */
  public void setObd2key(String key) {
    this.obd2key = key;
  }

  /**
   * Date of last update of valueA and valueB
   * @param timestamp milliseconds.
   */
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * Actual value of car2x entry.
   * @param valueA String or String representation of a number
   */
  public void setValueA(String valueA) {
    this.valueA = valueA;
  }

  /**
   * Optional value representing state of the key
   * @param str Either true or false represented as a String
   */
  public void setValueB(String str) {
    this.valueB = str;
  }
}