package CANPortFilter;

import com.testingtech.ttcn.tri.PortFilter;
import com.testingtech.ttcn.tri.extension.PortFilterProvider;

/**
 * 
 * @author Christian Damm, Sascha Kretzschmann
 * This class is necessary to
 *         manipulate the communication between the TE and the SUT
 * 
 */
public class CANPortFilterProvider implements PortFilterProvider {

	@Override
	public PortFilter getPortFilter() {
		return new CANPortFilter();
	}

}
