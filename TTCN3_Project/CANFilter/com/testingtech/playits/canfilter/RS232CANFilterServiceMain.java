package com.testingtech.playits.canfilter;

public class RS232CANFilterServiceMain {

	public static void main(String[] args) {
		try {
			new RS232CANFilterService(args).startFilter();
		} catch (AddressInstantiationException e) {
			// TODO 
		}
	}
}
