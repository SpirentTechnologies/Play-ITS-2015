package com.testingtech.playits.canfilter;

public class Car2XEntry {
  // Attributes
  private String obd2key = "0D"; // default value
  private String car2XValue;
  private boolean event;
  private long timestamp;

  /**
   * Backup key for communicating with ELM327
   * @return hexadecimal obd-2 key
   */
  public String getObd2key() {
    return this.obd2key;
  }

  /**
   * Date in millis according to the last time 
   * car2XValue and event where updated.
   * @return a timestamp
   */
  public long getTimestamp() {
    return this.timestamp;
  }

  /**
   * Each key has a car2XValue, respresented as String
   * @return first value of a given key
   */
  public String getcar2XValue() {
    return this.car2XValue;
  }

  /**
   * Optional value representing true or false in case of an event
   * @return true or false coded as String
   */
  public boolean getevent() {
    return this.event;
  }

  /**
   * Name of car2XValue read from openXC simulator 
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
   * Date of last update of car2XValue and event
   * @param timestamp milliseconds.
   */
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * Actual value of car2x entry.
   * @param car2XValue String or String representation of a number
   */
  public void setcar2XValue(String car2XValue) {
    this.car2XValue = car2XValue;
  }

  /**
   * Optional value representing state of the key
   * @param str Either true or false represented as a String
   */
  public void setevent(boolean event) {
    this.event = event;
  }
}