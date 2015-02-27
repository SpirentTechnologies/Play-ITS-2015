package CANPortFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.etsi.ttcn.tri.TriAddress;
import org.etsi.ttcn.tri.TriComponentId;
import org.etsi.ttcn.tri.TriMessage;
import org.etsi.ttcn.tri.TriPortId;
import org.etsi.ttcn.tri.TriStatus;

import sun.misc.IOUtils;

import com.testingtech.ttcn.tri.PortFilter;
import com.testingtech.util.HexViewer;

public class CANPortFilter extends PortFilter {
	
	File speed = new File("../../resource/Speed");
	FileInputStream fis = null;

	/**
	 * 
	 */
	private static final long serialVersionUID = -3492721938799363917L;

	public void triEnqueueMsg(TriPortId tsiPortId, TriAddress sutAddress,
			TriComponentId componentId, TriMessage receivedMessage) {
		

		byte filecontent[] = new byte[(int)speed.length()];
		
		try {
			fis = new FileInputStream(speed);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			fis.read(filecontent);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.out.println("PortFilter " + this
				+ " receives following incoming message: "
				+ HexViewer.hexString(filecontent));

		receivedMessage.setEncodedMessage(filecontent);
		if (receivedMessage.getEncodedMessage().length > 0) { // do not enqueue
																// zero length
																// messages
			super.triEnqueueMsg(tsiPortId, sutAddress, componentId,
					receivedMessage);
		}

	}

	public TriStatus triSend(TriComponentId componentId, TriPortId tsiPortId,
			TriAddress address, TriMessage sendMessage) {
		System.out.println("PortFilter " + this
				+ " receives following outgoing message: "
				+ HexViewer.hexString(sendMessage.getEncodedMessage()));
		//checkValue(sendMessage)
		// message can be send out without any modification
		return super.triSend(componentId, tsiPortId, address, sendMessage);
	}

}
