package com.testingtech.playits.canfilter;

public class BluetoothCANFilterServiceMain {

	public static void main(String[] args) {
		try {
			new BluetoothCANFilterService(args).startFilter();
		} catch (AddressInstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
