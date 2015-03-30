package com.testingtech.playits.canfilter.rs232;

import jssc.SerialPort;
import jssc.SerialPortException;

import java.net.Socket;
import java.util.Hashtable;

/**
 * 
 * @author Benjamin Kodera, Christian Kühling
 *
 */
public class ELMRS232 {
	static Socket sock = null;
	static Hashtable<String, String> commandHashTable = new Hashtable<String, String>();

	private String portName = "COM4";

	private SerialPort serialPort = new SerialPort(portName);

	/**
	 * Runs the given command, waits 200ms and returns the raw Reply without
	 * "WAITING" etc.
	 * 
	 * @param command
	 * @return raw Reply
	 */
	public String run(String command) {
		try {
			serialPort.writeString(command + "\r");
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return getResponse();
	}

	/**
	 * Listen to the serial port and returns raw data
	 * 
	 * @return raw data without INIT, WAITING etc
	 */
	public String getResponse() {
		StringBuilder res = new StringBuilder();
		String rawResponse = null;
		byte[] input = null;

		try {
			input = serialPort.readBytes();
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < input.length; i++) {
			if (((char) input[i]) != '>') {
				res.append((char) input[i]);
			}
		}
		// ELM sends like this: 41 0F 05 with whitespaces
		rawResponse = res.toString().trim();
		// no "WAITING" or "INIT BUS", just the data itself
		rawResponse = rawResponse.substring(rawResponse.lastIndexOf(13) + 1);
		return rawResponse;
	}

	/**
	 * Inits the serialPort Connection
	 */
	public void init(String portname) {
		try {
			if (!(portname == "")) {
				serialPort = new SerialPort(portname);
			}
			// Page 7 Manual elm327.pdf
			serialPort.openPort();
			serialPort.setParams(
					SerialPort.BAUDRATE_9600, // 38400 OR 9600
					SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Closes the port.
	 */
	// @Override
	public void close() {
		try {
			serialPort.closePort();
		} catch (SerialPortException e) {
			throw new IllegalStateException("Cannot close the port.", e);
		}
	}

}
