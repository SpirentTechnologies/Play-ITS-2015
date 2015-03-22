package canfilter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import org.apache.commons.lang3.StringUtils;

public class ELMBluetooth implements DiscoveryListener {
	private static Object lock = new Object();

	private static Vector remdevices = new Vector();

	private static String connectionURL = null;
	private static final String RESPONSE_TERMINALCHAR = ">";

	public static void main(String args[]) throws IOException, InterruptedException {
		BufferedReader b = new BufferedReader(new InputStreamReader(System.in));

		ELMBluetooth obj = new ELMBluetooth();
		LocalDevice locdevice = LocalDevice.getLocalDevice();
		String add = locdevice.getBluetoothAddress();
		String friendly_name = locdevice.getFriendlyName();

		System.out.println("Local Bluetooth Address : " + add);
		System.out.println("" + "" + "Local Friendly name : " + friendly_name);

		DiscoveryAgent dis_agent = locdevice.getDiscoveryAgent();
		System.out.println("********Locating Devices******");
		dis_agent.startInquiry(DiscoveryAgent.GIAC, obj);
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
			for (int i = 0; i < remdevices.size(); i++) {
				RemoteDevice remote_device = (RemoteDevice) remdevices
						.elementAt(i);
				if (remote_device.getFriendlyName(true) == "OBDII") { // TODO: all namend this way?
					index = i;
				}
				// System.out.println((i+1)+".)"+remote_device.getFriendlyName(true)+" "+remote_device.getBluetoothAddress());
			}
			// System.out.println("Choose Device to establish SPP");
			// int index=Integer.parseInt(b.readLine());

			RemoteDevice des_device = (RemoteDevice) remdevices
					.elementAt(index); // index - 1
			UUID[] uuidset = new UUID[1];
			uuidset[0] = new UUID("1101", true);

			dis_agent.searchServices(null, uuidset, des_device, obj);
			try {
				synchronized (lock) {
					lock.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (connectionURL == null) {
				System.out.println("Device does not support SPP.");
			} else {
				System.out.println("Device supports SPP.");
				StreamConnection st_connect = (StreamConnection) Connector
						.open(connectionURL);
				OutputStream outStream = st_connect.openOutputStream();

				PrintWriter pWriter = new PrintWriter(new OutputStreamWriter(
						outStream));
				InputStream inStream = st_connect.openInputStream();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						inStream));

				System.out
						.println("Remember to set the Protocol to auto: AT SP 0  , to check AT DP");
				while (true) {
					final StringBuilder buffer = new StringBuilder(256);
					String reply;
					System.out.println("Enter Command: ");
					String command = b.readLine();
					pWriter.write(command + "\r");
					pWriter.flush();
					System.out.println("command send: " + command);
					Thread.sleep(500);
					byte bytes = 0;
					StringBuilder res = new StringBuilder();
					String rawData = null;

					// read until '>' arrives
					while ((char) (bytes = (byte) in.read()) != '>')
						res.append((char) bytes);

					/*
					 * Imagine the following response 41 0c 00 0d.
					 * 
					 * ELM sends strings!! So, ELM puts spaces between each
					 * "byte". And pay attention to the fact that I've put the
					 * word byte in quotes, because 41 is actually TWO bytes
					 * (two chars) in the socket. So, we must do some more
					 * processing..
					 */
					rawData = res.toString().trim();

					/*
					 * Data may have echo or informative text like "INIT BUS..."
					 * or similar. The response ends with two carriage return
					 * characters. So we need to take everything from the last
					 * carriage return before those two (trimmed above).
					 */
					rawData = rawData.substring(rawData.lastIndexOf(13) + 1);

					System.out.println("response: " + rawData);
				}
			}
		}
	}

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

}