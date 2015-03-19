package canfilter;

import javax.sound.sampled.Port;

import org.apache.commons.lang3.StringUtils;

import jssc.SerialPort;
import jssc.SerialPortException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ELM327 {

	  
	  private String portName;	//cfg
	  private Long commandTimeout; //cfg
	
	  private final SerialPort serialPort = new SerialPort(portName);
	  
	  private static final String RESPONSE_OK = "OK";
	  private static final String RESPONSE_TERMINALCHAR = ">";
	  
	  
	  
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public void initateConnection(){
		 try {
			 //Page 7 Manual elm327.pdf
			serialPort.setParams(38400, 8, 1, 0, true, true);
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}
	
	
	  /**
	   * Reads responses line by line from the buffer until {@value #RESPONSE_TERMINALCHAR} comes or
	   * timeout occurs.
	   *
	   * @return a list of lines in response, never null and never empty list.
	   * @throws PortCommunicationException - if there was no response or prompt was missing.
	   */
	public List<String> readResponse() {

	    try {
	      final StringBuilder buffer = new StringBuilder(256);
	      final long start = System.currentTimeMillis();
	      final long timeout = 3000;
	      while (true) {
	        if (start + timeout < System.currentTimeMillis()) {
	          System.out.println("Timeout %dms occured."); 
	        }
	        if (serialPort.getInputBufferBytesCount() == 0) {
	          Thread.yield();
	          continue;
	        }
	        final String string = serialPort.readString();
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
	   * @param command - command and it's arguments.
	   * @throws PortCommunicationException
	   */
	  public void writeln(String... command) {
		    try {
		      for (String commandPart : command) {
		        serialPort.writeString(commandPart);
		      }
		      serialPort.writeString("\r\n");
		    } catch (SerialPortException e) {
		      throw new RuntimeException(e);
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
		      throw new RuntimeException("Command unsuccessful! Response: " + response);
		    }
		  }
	  
	  /**
	   * Executes an AT command and returns an answer.
	   *
	   * @param command - an AT command to execute.
	   * @return an answer of the command.
	   */
	  public String at(final String command) {
	    writeln("AT", command);
	    return readResponse().get(0);
	  }
	  
	  /**
	   * Sends ATE signal and sets the command echo on/off
	   *
	   * @param on
	   */
	  public void setEcho(final boolean on){
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
	  public void reset()  {
	    final String response = at("Z");
	    // with echo on the first line will be ATZ (sent command)
	    // another line will be a device type identification.
	    if (response == null) {
	      throw new RuntimeException("Command unsuccessful! Response: " + response);
	    }
	  }


	  /**
	   * Closes the port.
	   */
//	  @Override
	  public void close() {
	    try {
	      serialPort.closePort();
	    } catch (SerialPortException e) {
	      throw new IllegalStateException("Cannot close the port.", e);
	    }
	  }
	
	  public static boolean[] convertHexToBooleanArray(final String... line) {
		    final boolean[] bools = new boolean[line.length * 8];
		    int k = 0;
		    for (int i = 0; i < line.length; i++) {
		      String hex = line[i];
		      int val = Integer.parseInt(hex, 16);
		      String binary = StringUtils.leftPad(Integer.toBinaryString(val), 8, '0');
		      for (int j = 0; j < binary.length(); j++) {
		        bools[k++] = binary.charAt(j) == '1';
		      }
		    }
		    return bools;
		  }


		  public static int toInteger(boolean... bools) {
		    int base = 1;
		    int index = bools.length - 1;
		    int value = 0;
		    while(index >= 0) {
		      value += bools[index] ? base : 0;
		      base *= 2;
		      index--;
		    }
		    return value;
		  }

}

