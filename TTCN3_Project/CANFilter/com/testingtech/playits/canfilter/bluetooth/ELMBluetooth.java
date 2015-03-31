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

/**
 * 
 * @author Benjamin Kodera, Christian Kühling
 *
 */
public class ELMBluetooth implements DiscoveryListener {

	private Object lock = new Object();

	private Vector<RemoteDevice> remdevices = new Vector<RemoteDevice>();

	private String connectionURL = null;

	public BufferedReader br;
	public PrintWriter pwriter;
	public BufferedReader in;

	 ELMBluetooth obj;

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
	 * Inits the Bluetooth Connection
	 * 
	 * @throws IOException
	 */
	public void init() throws IOException {
		//TODO Fix(hangs @ Inquiry complete)
		br = new BufferedReader(new InputStreamReader(System.in));
		obj = new ELMBluetooth();
		LocalDevice locdevice = LocalDevice.getLocalDevice();
	    DiscoveryAgent disAgent = locdevice.getDiscoveryAgent();
	    System.out.println("********Locating Devices******");
	    disAgent.startInquiry(DiscoveryAgent.GIAC, obj);
		try {
			synchronized (lock) {
				System.out.println("one");
				lock.wait();
				System.out.println("two");
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
				if ((remoteDevice.getFriendlyName(true) == "OBDII")
						|| (remoteDevice.getFriendlyName(true) == "ELM327")) {
					index = value;
				}
			}

			RemoteDevice desDevice = (RemoteDevice) remdevices.elementAt(index); // index
																					// -
																					// 1
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
				// TODO Error Handling ("Device does not support SPP.");
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
	 * 
	 * @param command
	 *            the command to run
	 * @param in
	 *            Inputstream
	 * @param pwriter
	 *            Outputstream
	 * @return raw reply
	 */
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
		String rawReply = null;

		// read until '>' arrives
		try {
			while ((char) (bytes = (byte) in.read()) != '>') {
				res.append((char) bytes);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// ELM sends like this: 41 0F 05 with whitespaces
		rawReply = res.toString().trim();
		// no "WAITING" or "INIT BUS", just the data itself
		rawReply = rawReply.substring(rawReply.lastIndexOf(13) + 1);
		return rawReply;
	}

}