package com.testingtech.playits.canfilter;

public class Car2XEntry {
  // Attributes
  private String obd2key = "0D"; // default value
  private Object value;
  private boolean event;
  private long timestamp;

  /**
   * Backup key for communicating with ELM327
   * @return hexadecimal obd-2 key
   */
  public String getOBD2key() {
    return obd2key;
  }

  /**
   * Date in milliseconds according to the last time 
   * valueA and valueB where updated.
   * @return a timestamp
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * Each entry has a value (float, string or boolean)
   * @return value of a given key
   */
  public Object getValue() {
    return value;
  }

  /**
   * Optional value representing whether an event for this value has occurred
   * or not. By default, value is false
   * @return true or false
   */
  public boolean getEvent() {
    return event;
  }

  /**
   * Name of valueA read from openXC simulator 
   * @param key two byte hexadecimal String
   */
  public void setOpenXCkey(String key) {
    this.obd2key = key;
  }

  /**
   * Backup key for communicating with ELM327
   * @param key two byte hexadecimal String
   */
  public void setOBD2key(String key) {
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
   * @param value String or String representation of a number
   */
  public void setValue(Object value) {
    this.value = value;
  }

  /**
   * Optional value representing state of the key
   * @param event Either true or false represented as a String
   */
  public void setEvent(boolean event) {
    this.event = event;
  }
}