package com.testingtech.playits.canfilter.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class StreamForwarder extends Thread {
  private final InputStream in;
  private final PrintStream out;

  /**
   * Redirects an input stream to a print stream
   * @param in
   * @param out
   */
  public StreamForwarder(InputStream in, PrintStream out) {
    this.in = in;
    this.out = out;
  }

  /**
   * As long as there is a line to be read from the input stream,
   * forward it to the print stream
   */
  public void run() {
    BufferedReader bufferedReader = new BufferedReader(
        new InputStreamReader(in));

    String line;
    try {
      while ((line = bufferedReader.readLine()) != null) {
        out.println(line);
      }
    } catch (IOException e) {
      System.err.println("[StreamForwarder] An error occurred: " + e.getMessage());
    }
  }
}
