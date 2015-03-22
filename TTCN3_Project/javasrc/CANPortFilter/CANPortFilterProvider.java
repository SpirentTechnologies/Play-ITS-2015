package CANPortFilter;

import com.testingtech.ttcn.tri.PortFilter;
import com.testingtech.ttcn.tri.extension.PortFilterProvider;

// FIXME: This whole class does nothing except returning an object of type CANPportFilter(). So: Why is it necessary?
public class CANPortFilterProvider implements PortFilterProvider {

	@Override
	public PortFilter getPortFilter() {
		// TODO Auto-generated method stub
		return new CANPortFilter();
	}

}
