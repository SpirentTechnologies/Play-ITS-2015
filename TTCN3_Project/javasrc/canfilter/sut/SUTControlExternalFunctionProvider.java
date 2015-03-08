package canfilter.sut;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.etsi.ttcn.tci.BooleanValue;
import org.etsi.ttcn.tci.CharstringValue;
import org.etsi.ttcn.tci.IntegerValue;

import com.testingtech.ttcn.annotation.ExternalFunction;
import com.testingtech.ttcn.tri.AnnotationsExternalFunctionPlugin;
import com.testingtech.util.UUID;

@ExternalFunction.Definitions(SUTControlExternalFunctionProvider.class)
public class SUTControlExternalFunctionProvider extends
		AnnotationsExternalFunctionPlugin {

	private ProcessMap processMap = ProcessMap.getInstance();

	// external function start_Server_SUT(in charstring host, in integer
	// portNumber) return charstring;
	@ExternalFunction(name = "start_Server_SUT", module = "SUT_Control")
	public CharstringValue start_Server_SUT(CharstringValue host,
			IntegerValue portNumber) {
		return start_SUT(
				"canfilter.CANFilterServer",
				new String[] { host.getString(),
						String.valueOf(portNumber.getInt()) },
				"TCP server listening on ");
	}

	// external function start_Client_SUT(in charstring host, in integer
	// portNumber) return charstring;
	@ExternalFunction(name = "start_Client_SUT", module = "SUT_Control")
	public CharstringValue start_Client_SUT(CharstringValue host,
			IntegerValue portNumber) {
		return start_SUT(
				"canfilter.CANFilterClient",
				new String[] { host.getString(),
						String.valueOf(portNumber.getInt()) }, null);
	}
	
	//external function stop_SUT(in charstring sutId) return boolean;
		@ExternalFunction(name = "stop_SUT", module = "SUT_Control")
		public BooleanValue stop_SUT(CharstringValue sutId) {
			logInfo("Stopping SUT " + sutId.getString());
			
			Process p = processMap.get(sutId.getString());
			if (p != null) {
				p.destroy();
				try {
					p.waitFor();
				} catch (InterruptedException e) {
					// nothing to do just continue
				}
				processMap.remove(sutId.getString());
			}

			return newBooleanValue(true);
		}

	private CharstringValue start_SUT(String mainClass, String[] parameters,
			String blockUntilMessage) {
		String processId = UUID.randomUUID().toString();

		logInfo("Starting " + mainClass + " with parameters: \""
				+ join(parameters, "\" \"") + "\"");

		try {
			final Process p = Runtime.getRuntime().exec(
					"java -classpath build/SUT " + mainClass + " "
							+ join(parameters, " "));
			processMap.put(processId, p);

			if (blockUntilMessage != null) {
				waitForMessage(blockUntilMessage, p.getInputStream(),
						System.out);
			}

			new StreamForwarder(p.getInputStream(), System.out).start();
			new StreamForwarder(p.getErrorStream(), System.err).start();
		} catch (IOException e) {
			logError("Could not start SUT: " + e.getMessage());
			return newCharstringValue("");
		}

		return newCharstringValue(processId);
	}

	/**
	 * Wait until a line of output from in matches blockUntilMessage. Forward
	 * read lines to out.
	 */
	private void waitForMessage(String blockUntilMessage, InputStream in,
			PrintStream out) {
		BufferedReader r = new BufferedReader(new InputStreamReader(in));

		String line;
		try {
			while ((line = r.readLine()) != null) {
				out.println(line);

				if (line.startsWith(blockUntilMessage)) {
					break;
				}
			}
		} catch (IOException e) {
		}
	}

	private String join(String[] strings, String delimiter) {
		if (strings.length == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder();

		sb.append(strings[0]);
		for (int i = 1; i < strings.length; i++) {
			sb.append(delimiter).append(strings[i]);
		}

		return sb.toString();
	}
}

class ProcessMap {
	private static ProcessMap instance = null;
	private Map<String, Process> processMap = null;

	private ProcessMap() {
		processMap = new HashMap<String, Process>();
	}

	public static ProcessMap getInstance() {
		if (instance == null) {
			instance = new ProcessMap();
		}

		return instance;
	}

	public void put(String key, Process p) {
		processMap.put(key, p);
	}

	public Process get(String key) {
		return processMap.get(key);
	}

	public boolean containsKey(String key) {
		return processMap.containsKey(key);
	}

	public void remove(String key) {
		processMap.remove(key);
	}
}

class StreamForwarder extends Thread {
	private final InputStream in;
	private final PrintStream out;

	public StreamForwarder(InputStream in, PrintStream out) {
		this.in = in;
		this.out = out;
	}

	public void run() {
		BufferedReader r = new BufferedReader(new InputStreamReader(in));

		String line;
		try {
			while ((line = r.readLine()) != null) {
				out.println(line);
			}
		} catch (IOException e) {
		}
	}
}
