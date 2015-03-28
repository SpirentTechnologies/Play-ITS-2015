/*
 * Sources: 
 * https://github.com/dmatej/java-cardiag
 * https://github.com/pires/obd-java-api
 * http://stackoverflow.com/questions/18431424/unable-to-communicate-with-elm327-bluetooth
 */

package canfilter;

import javax.sound.sampled.Port;

import org.apache.commons.lang3.StringUtils;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.io.*;
import java.util.Vector;


public class ELM327 {
	private static int RS232 = 0;
	private static int BLUETOOTH = 1;
	private static int usedConnection;
	private static final String RESPONSE_OK = "OK";
	private static final String RESPONSE_TERMINALCHAR = ">";

//	// Bluetooth Variables
//	private static Object lock = new Object();
//	private static Vector remdevices = new Vector();
//	private static String connectionURL = null;

	// Serial Variables:
	static Socket sock = null;
	static Hashtable<String, String> commandHashTable = new Hashtable<String, String>();

	private static String portName = "COM4";
	private static Long commandTimeout = 3000l;
	private static String string;

	private final static SerialPort serialPort = new SerialPort(portName);


	public static void main(String[] args) throws IOException, SerialPortException, InterruptedException {
//		initConnection(args[0]);
//		initCommandHashTable();
		initConnection("rs232");
		
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while(true){
			
			System.out.println("Enter Command: ");
	        String command = br.readLine(); 
	        if (command == "EXIT"){
	        	serialPort.closePort();
	        } else {
	        	serialPort.writeString(command +"\r");
	        	Thread.sleep(200);
	        	System.out.println(getResponse());
	        	
	        	if (serialPort.getInputBufferBytesCount() == 0) {
					Thread.yield();
					continue;
				}
	        	
	        }
		}

	}

	
	public static String getResponse() throws SerialPortException{
        StringBuilder res = new StringBuilder();
        String rawData = null;
        byte[] input = null;

        // read until '>' arrives
//        while ((char) (bytes = (byte) in.read()) != '>') {
//          res.append((char) bytes);
//        }
        
        input = serialPort.readBytes();
        for (int i = 0; i < input.length; i++) {
			if(((char)input[i]) != '>'){
				res.append((char) input[i]);
			}
		}
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
        
        return rawData;
	}
	public static void initConnection(String type) {
		if (type == "rs232") {
			usedConnection = RS232;
			try {
				String[] portNames = SerialPortList.getPortNames();
				for (int i = 0; i < portNames.length; i++) {
					System.out.println(portNames[i]);
				}

				// Page 7 Manual elm327.pdf
				serialPort.openPort();
				serialPort.setParams(SerialPort.BAUDRATE_9600, //38400 OR 9600
									SerialPort.DATABITS_8,
			                        SerialPort.STOPBITS_1,
			                        SerialPort.PARITY_NONE);
			} catch (SerialPortException e) {
				e.printStackTrace();
			}
		} else if (type == "bluetooth") {
			usedConnection = BLUETOOTH;
			sock = new Socket();
			// TODO implement Bluetooth version
		} else {
			System.out.println("please choose rs232 or bluetooth");
		}

	}

	/**
	 * Reads responses line by line from the buffer until
	 * {@value #RESPONSE_TERMINALCHAR} comes or timeout occurs.
	 *
	 * @return a list of lines in response, never null and never empty list.
	 * @throws PortCommunicationException
	 *             - if there was no response or prompt was missing.
	 */
	public static List<String> readResponse() {

		try {
			final StringBuilder buffer = new StringBuilder(256);
			final long start = System.currentTimeMillis();
			while (true) {
				if (start + commandTimeout < System.currentTimeMillis()) {
					System.out.println("Timeout %dms occured.");
				}

				if (usedConnection == RS232) {
					if (serialPort.getInputBufferBytesCount() == 0) {
						Thread.yield();
						continue;
					}
				} else if (usedConnection == BLUETOOTH) {
					// TODO
				}

				String string = "";
				if (usedConnection == RS232) {
					string = serialPort.readString();
				} else if (usedConnection == BLUETOOTH) {
					// TODO
					string = "";
				}

				buffer.append(string).append('\r');
				if (StringUtils.endsWith(string, RESPONSE_TERMINALCHAR)) {
					buffer.setLength(buffer.length() - 2);
					break;
				}
			}
			final String response = buffer.toString();
			if (response == null || response.trim().isEmpty()) {
				System.out.println("Retrieved no response from the port.");
			}
			final String[] lines = StringUtils.split(response, '\r');
			final List<String> responses = new ArrayList<String>(lines.length);
			for (String line : lines) {
				final String trimmed = StringUtils.trimToNull(line);
				if (trimmed != null) {
					responses.add(trimmed);
				}
			}

			return responses;
		} catch (SerialPortException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Writes command and arguments to the port.
	 *
	 * @param command
	 *            - command and it's arguments.
	 * @throws PortCommunicationException
	 */
	public void writeln(String... command) {
		if (usedConnection == RS232) {
			try {
				for (String commandPart : command) {
					serialPort.writeString(commandPart);
				}
				serialPort.writeString("\r\n");
			} catch (SerialPortException e) {
				throw new RuntimeException(e);
			}
		} else if (usedConnection == BLUETOOTH) {
			// TODO
		}
	}

	/**
	 * @param value
	 * @return 1 for true, 0 for false.
	 */
	protected final String translate(final boolean value) {
		return value ? "1" : "0";
	}

	/**
	 * Checks for OK in port response.
	 *
	 */
	protected void checkOkResponse() {
		final List<String> response = readResponse();
		if (response == null || response.isEmpty()) {
			throw new RuntimeException("No response.");
		}
		final int resultCodeIndex = response.size() - 1;
		if (!RESPONSE_OK.equalsIgnoreCase(response.get(resultCodeIndex))) {
			throw new RuntimeException("Command unsuccessful! Response: "
					+ response);
		}
	}

	/**
	 * Executes an AT command and returns an answer.
	 *
	 * @param command
	 *            - an AT command to execute.
	 * @return an answer of the command.
	 */
	public String executeCommand(final String command) {
		writeln("AT", command);
		return readResponse().get(0);
	}

	/**
	 * Sends ATE signal and sets the command echo on/off
	 *
	 * @param on
	 */
	public void setEcho(final boolean on) {
		writeln("ATE", translate(on));
		checkOkResponse();
	}

	/**
	 * Sends ATL signal and sets the line termination on/off
	 *
	 * @param on
	 */
	public void setLineTermination(final boolean on) {
		writeln("ATL", translate(on));
		checkOkResponse();
	}

	/**
	 * Sends AT Z command, resets the communication.
	 *
	 */
	public void reset() {
		final String response = executeCommand("Z");
		// with echo on the first line will be ATZ (sent command)
		// another line will be a device type identification.
		if (response == null) {
			throw new RuntimeException("Command unsuccessful! Response: "
					+ response);
		}
	}

	/**
	 * Closes the port.
	 */
	// @Override
	public void close() {
		if (usedConnection == RS232) {
			try {
				serialPort.closePort();
			} catch (SerialPortException e) {
				throw new IllegalStateException("Cannot close the port.", e);
			}
		} else if (usedConnection == BLUETOOTH) {

		}

	}

}
