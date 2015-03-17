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

import canfilter.CANFilterService;

import com.testingtech.ttcn.annotation.ExternalFunction;
import com.testingtech.ttcn.tri.AnnotationsExternalFunctionPlugin;
import com.testingtech.util.UUID;

@ExternalFunction.Definitions(SUTControlExternalFunctionProvider.class)
public class SUTControlExternalFunctionProvider extends
		AnnotationsExternalFunctionPlugin {

	private ProcessMap processMap = ProcessMap.getInstance();

	@ExternalFunction(name = "start_Filter", module = "Car2X_Control")
	public CharstringValue start_Filter(CharstringValue host,
			IntegerValue portNumber) {
		return start_Filter(
				CANFilterService.class.getCanonicalName(),
				new String[] { host.getString(),
						String.valueOf(portNumber.getInt()) });
	}

	private CharstringValue start_Filter(String mainClass, String[] parameters) {
		String processId = UUID.randomUUID().toString();

		logInfo("Starting " + mainClass + " with parameters: \""
				+ join(parameters, "\" \"") + "\"");

		try {
			final Process p = Runtime.getRuntime().exec(
					"java -classpath build/SUT " + mainClass + " "
							+ join(parameters, " "));
			processMap.put(processId, p);

			new StreamForwarder(p.getInputStream(), System.out).start();
			new StreamForwarder(p.getErrorStream(), System.err).start();
		} catch (IOException e) {
			logError("Could not start SUT: " + e.getMessage());
			return newCharstringValue("");
		}

		return newCharstringValue(processId);
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

	@ExternalFunction(name = "stop_Filter", module = "Car2X_Control")
	public BooleanValue stop_Filter(CharstringValue sutId) {
		logInfo("Stopping Filter " + sutId.getString());

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
