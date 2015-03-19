package com.testingtech.car2x.hmi.appserver;

import java.util.Date;
import com.testingtech.car2x.hmi.messages.TestCaseCommand;
import com.testingtech.car2x.hmi.messages.ControlMessage;
import com.testingtech.car2x.hmi.messages.ProgressMessage;
import com.testingtech.car2x.hmi.messages.TestCaseProgress;
import com.testingtech.car2x.hmi.messages.VerdictMessage;
import com.testingtech.car2x.hmi.messages.TestCase;
import com.testingtech.car2x.hmi.messages.TestCaseVerdict;

/**
 * Services app requests concerning the starting or stopping of a test case.
 *
 * TODO change behavior so server automatically listens for a new connection
 * once the previous connection has been closed or has timed out.
 *
 * TODO move to controller
 */
public class AppService implements Runnable {

	@Override
	public void run() {
		try {
			SocketCommunicator communicator = new SocketCommunicator();
			communicator.connectionInit();
			ControlMessage controlMessage = communicator.getRequest();
			if (controlMessage.command.compareTo(TestCaseCommand.START) == 0) {
				// simulating test progress
				communicator.sendMessage(new ProgressMessage(
						TestCase.TC_VEHICLE_SPEED_OVER_50, new Date(),
						TestCaseProgress.STAGE1));
				Thread.sleep(4000);
				communicator.sendMessage(new ProgressMessage(
						TestCase.TC_VEHICLE_SPEED_OVER_50, new Date(),
						TestCaseProgress.STAGE2));
				Thread.sleep(4000);
				communicator.sendMessage(new ProgressMessage(
						TestCase.TC_VEHICLE_SPEED_OVER_50, new Date(),
						TestCaseProgress.STAGE3));
				Thread.sleep(4000);
				communicator.sendMessage(new ProgressMessage(
						TestCase.TC_VEHICLE_SPEED_OVER_50, new Date(),
						TestCaseProgress.STAGE4));
				Thread.sleep(4000);
				communicator.sendMessage(new VerdictMessage(
						TestCase.TC_VEHICLE_SPEED_OVER_50, new Date(),
						TestCaseVerdict.PASS));
			} else {
				System.out.println("Command is unsupported. Terminating...");
			}
			System.out.println("Closing connections");
			communicator.closeConnService();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}