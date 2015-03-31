package com.testingtech.playits.canfilter.tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.testingtech.playits.canfilter.Elm327;

public class Elm327Test {
	Elm327 elm327 = new Elm327();
	private String line;

	@Before
	public void setUp() {
		line = "bank1_sensor3_oxygen_sensor_voltage=01 16";
	}
	
	
	
	@Test
	public void parsesOpenXCKey(){
		elm327.addKeyValuePair(line);
		assertEquals("01 16", elm327.openXCToOBD2Map.get("bank1_sensor3_oxygen_sensor_voltage"));
	}
	
}
