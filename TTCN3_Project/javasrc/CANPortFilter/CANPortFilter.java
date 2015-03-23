package CANPortFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.etsi.ttcn.tri.TriAddress;
import org.etsi.ttcn.tri.TriComponentId;
import org.etsi.ttcn.tri.TriMessage;
import org.etsi.ttcn.tri.TriPortId;
import org.etsi.ttcn.tri.TriStatus;

import com.testingtech.ttcn.tri.PortFilter;

public class CANPortFilter extends PortFilter {

	private static final long serialVersionUID = -3492721938799363917L;

	/**
	 * 
	 * @param tsiPortId
	 *            Identifies the system port.
	 * @param SUTAddress
	 *            Indicates an internal address of the SUT.
	 * @param componentId
	 *            Identifies the target component.
	 * @param receivedMessage
	 *            Contains the encoded value that is processed by the by the
	 *            receive statement.
	 */
	public void triEnqueueMsg(TriPortId tsiPortId, TriAddress SUTAddress, TriComponentId componentId,
			TriMessage receivedMessage) {

		File speedFile = new File("resource/Speed");
		BufferedReader bufferedReader = null;
		String speed = null;

		// create a new file input stream
		try {
			bufferedReader = new BufferedReader(new FileReader(speedFile));
		} catch (FileNotFoundException e) {
			System.err.println("Error! File " + speedFile.getAbsoluteFile() + " not found.");
			e.printStackTrace();
		}

		// read content from file input stream
		try {
			speed = bufferedReader.readLine();
		} catch (IOException e) {
			System.err.println("Error while reading file.");
			e.printStackTrace();
		}

		System.out.println("PortFilter " + this + " sends following outgoing message: " + speed);

		receivedMessage.setEncodedMessage(speed.getBytes());
		// FIXME: What is this if-clause for?
		// do not enqueue zero length messages
		if (receivedMessage.getEncodedMessage().length > 0) {
			super.triEnqueueMsg(tsiPortId, SUTAddress, componentId, receivedMessage);
		}
	}

	/**
	 * This method is called when the test case executes a send statement.
	 * 
	 * @param componentId
	 *            Identifies the sending component.
	 * @param tsiPortId
	 *            Identifies the system port.
	 * @param address
	 *            Target of the send statement.
	 * @param sendMessage
	 *            The message to be send.
	 */
	public TriStatus triSend(TriComponentId componentId, TriPortId tsiPortId, TriAddress address, TriMessage sendMessage) {
		System.out.println("PortFilter " + this + " receives following outgoing message: "
				+ new String(sendMessage.getEncodedMessage()));

		// message can be send out without any modification
		return super.triSend(componentId, tsiPortId, address, sendMessage);
	}

}
