package com.testingtech.playits.canfilter;

import java.util.Arrays;
import java.util.List;

import com.testingtech.playits.canfilter.rs232.ELMRS232;

public class RS232CANFilterService implements CANFilterService {

	private CANFilterLog canFilterLog = new CANFilterLog(
			RS232CANFilterService.class.getSimpleName());
	private ELMRS232 elmrs232;
	
	public RS232CANFilterService(String[] args) throws AddressInstantiationException {
		
		if (args.length < 7)  {
			canFilterLog.logError(FilterLogMessages.WRONG_CMD_LINE_USAGE);
		      throw new AddressInstantiationException(
		          "Wrong number of arguments.");
		}
		List<String> serialPortParams = Arrays.asList(args).subList(2, args.length);
		elmrs232 = new ELMRS232(serialPortParams);
	}

	@Override
	public void startFilter() {
		elmrs232.init();
	}

}
