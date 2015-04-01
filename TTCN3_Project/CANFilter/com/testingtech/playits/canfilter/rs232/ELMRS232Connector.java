package com.testingtech.playits.canfilter.rs232;

import java.io.IOException;
import java.util.List;

import jssc.SerialPort;
import jssc.SerialPortException;

import com.testingtech.playits.canfilter.connector.Elm327Connector;

public class ELMRS232Connector extends Elm327Connector {

	int baudRate;
	int parity;
	int dataBits;
	int stopBits;
	
	private SerialPort serialPort;

	public ELMRS232Connector(List<String> rs232Params) {
		serialPort = new SerialPort(rs232Params.get(0));
		baudRate = Integer.parseInt(rs232Params.get(1));
		parity = Integer.parseInt(rs232Params.get(2));
		dataBits = Integer.parseInt(rs232Params.get(3));
		stopBits = Integer.parseInt(rs232Params.get(4));
	}
	
	/**
	 * Runs the given command, waits 200ms and returns the raw Reply without
	 * "WAITING" etc.
	 * 
	 * @param command
	 * @return raw Reply
	 */
	@Override
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
	private String getResponse() {
		StringBuilder res = new StringBuilder();
		String rawResponse = null;
		byte[] input = null;

		try {
			input = serialPort.readBytes();
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < input.length; i++) {
			if ((char) input[i] != '>') {
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
	 * Opens the serialPort Connection
	 * @throws IOException 
	 */
	@Override
	public void connect() throws IOException {
		initSerialPort();
		super.connect();
	}

	private void initSerialPort() {
		try {
			serialPort.openPort();
			serialPort.setParams(baudRate,
					dataBits, stopBits, parity);
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void disconnect() {
		try {
			serialPort.closePort();
		} catch (SerialPortException e) {
			throw new IllegalStateException("Cannot close the port.", e);
		}
		super.disconnect();
	}

}
