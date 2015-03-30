package com.testingtech.playits.canfilter;

import java.net.Socket;
import java.util.Hashtable;
import java.util.Timer;

public class TimeoutResponder {

  private Hashtable<String, Timer> timers = new Hashtable<>();
  private Hashtable<String, Car2XEntry> car2xEntries;
  private Socket socket;
  static CANFilterLog canFilterLog = new CANFilterLog(
		  TimeoutResponder.class.getSimpleName());

  /**
   * Starts and stops tasks that periodically send openXC / obd-2 values 
   * from a hash table over a socket.
   * 
   * @param socket
   *            TCP recipient socket of JSON responses coming from a hash
   *            table.
   * @param car2xEntries hash table of openXC keys and corresponding values
   */
  public TimeoutResponder(Socket socket,
      Hashtable<String, Car2XEntry> car2xEntries) {
    this.socket = socket;
    this.car2xEntries = car2xEntries;
  }

  /**
   * Starts a timer task for a given entry in a hash table specified by a key.
   * The task is periodically repeated at a given interval.
   * 
   * @param key
   *            openXC key
   * @param interval
   *            duration between two responses from a timer task in
   *            milliseconds
   */
  public void addTimer(String key, int interval) {
    Timer timer = timers.get(key);
    if (timer != null) {
      timer.cancel();
    }
    timer = new Timer();
    timers.put(key, timer);
    canFilterLog.logInfo(FilterLogMessages.STARTING_RESPONSE, key);
    // TODO set 2nd parameter to zero if immediate response is desired
    timer.schedule(new ResponderTask(key, car2xEntries.get(key), socket),
        interval, interval);
  }

  /**
   * Stops a timer task from sending a hash table entry.
   * 
   * @param key
   *            openXC key
   */
  public void removeTimer(String key) {
	canFilterLog.logInfo(FilterLogMessages.STOPPING_RESPONSE, key);
    Timer timer = timers.get(key);
    if (timer != null) {
      timer.cancel();
      timers.remove(key);
    }
  }
}