package com.testingtech.playits.canfilter.tests;

import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.testingtech.playits.canfilter.Car2XEntry;

@RunWith(Parameterized.class)
public class CANFilterTest {

	private Hashtable<String, Car2XEntry> car2xValues = new Hashtable<String, Car2XEntry>();
	private int duration;
	private int expectedTimeouts;
	private long startTime;
	private long currentTime;

	public void setUp() {
		car2xValues.put("vehicle_speed", new Car2XEntry());
		car2xValues.put("door_status", new Car2XEntry());
		car2xValues.put("engine_speed", new Car2XEntry());
		car2xValues.put("headlamp_status", new Car2XEntry());
		car2xValues.put("hanbreak_status", new Car2XEntry());
	}

	public CANFilterTest(int duration, int expectedTimeouts) {
		setUp();
		startTime = new Date().getTime();
		currentTime = startTime;
		this.duration = duration;
		car2xValues.get("vehicle_speed");
		this.expectedTimeouts = expectedTimeouts;
	}

	@Parameters
	public static List<Object[]> data() {
		return Arrays.asList(new Object[][] { 
				{ 499, 500, 0 }, { 999, 500, 1 }, { 1000, 500, 2 },
				{ 1499, 1500, 0 }, { 1500, 1500, 1 }, { 1501, 1500, 1 }, { 3000, 1500, 2 },
				});
	}

}