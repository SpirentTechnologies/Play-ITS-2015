package com.testingtech.playits.canfilter.bluetooth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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

import com.testingtech.playits.canfilter.connector.Elm327Connector;

public class ELMBluetoothConnector extends Elm327Connector implements
		DiscoveryListener {

	private static final String DEFAULT_DEVICE_NAME = "OBDII"; // may also be
																// named CBT
	private static Object lock = new Object();
	private static Vector<RemoteDevice> remdevices = new Vector<RemoteDevice>();
	private static String connectionURL = null;

	public BufferedReader br;
	private static PrintWriter pwriter;
	private static BufferedReader in;

	ELMBluetoothConnector obj;

	private String deviceName;

	public ELMBluetoothConnector(String deviceName) {
		this.deviceName = deviceName;
	}

	@Override
	public void connect() throws IOException {
		initBluetooth();
		super.connect();
	}

	/**
	 * @param RemoteDevice
	 * @param DeviceClass
	 *            using javax.bluetooth import
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
	 * Initializes the Bluetooth connection with a specified ELM327 device.
	 * 
	 * @throws IOException
	 */
	private void initBluetooth() throws IOException {
		br = new BufferedReader(new InputStreamReader(System.in));
		obj = new ELMBluetoothConnector(deviceName);
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
			System.out.println(("available Bluetooth Device:"));
			for (int value = 0; value < remdevices.size(); value++) {
				RemoteDevice remoteDevice = (RemoteDevice) remdevices
						.elementAt(value);
				System.out.println(remoteDevice.getFriendlyName(false));
				// TODO check if all elm327 are named this way
				String name = remoteDevice.getFriendlyName(true);
				if (name.equals(deviceName) || name.equals(DEFAULT_DEVICE_NAME)) {
					index = value;
				}
			}

			RemoteDevice desDevice = (RemoteDevice) remdevices.elementAt(index);
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
				System.out.println("Device does not support SPP");
			} else {
				System.out.println("Device supports SPP.");
				StreamConnection stConnect = (StreamConnection) Connector
						.open(connectionURL);
				OutputStream outStream = stConnect.openOutputStream();

				pwriter = new PrintWriter(new OutputStreamWriter(outStream));
				InputStream inStream = stConnect.openInputStream();
				in = new BufferedReader(new InputStreamReader(inStream));
			}
		}
	}

	/**
	 * Runs an AT / OBD2 command. The response consists of a hexadecimal string
	 * representation of the value corresponding to the command sent.
	 * 
	 * @param command
	 *            the command to run
	 * @param in
	 *            Inputstream
	 * @param pwriter
	 *            Outputstream
	 * @return raw reply
	 */
	@Override
	public String run(String command) {
		pwriter.write(command + "\r");
		pwriter.flush();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		byte bytes = 0;
		StringBuilder res = new StringBuilder();

		// read until '>' arrives
		try {
			while ((char) (bytes = (byte) in.read()) != '>') {
				res.append((char) bytes);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// ELM sends like this: 41 0F 05 with whitespaces
		String rawResponse = res.toString().trim();
		// no "WAITING" or "INIT BUS", just the data itself
		rawResponse = rawResponse.substring(rawResponse.lastIndexOf(13) + 1);
		return rawResponse;
	}

	@Override
	public void disconnect() {
		super.disconnect();
	}
}