package CANPortFilter;

import com.testingtech.ttcn.tri.PortFilter;
import com.testingtech.ttcn.tri.extension.PortFilterProvider;

public class CANPortFilterProvider implements PortFilterProvider {

	@Override
	public PortFilter getPortFilter() {
		// TODO Auto-generated method stub
		return new CANPortFilter();
	}

}
