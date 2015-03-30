package com.testingtech.playits.canfilter;

public final class CANFilterLog {

	private String componentName;

	public CANFilterLog(String componentName) {
		this.componentName = componentName;
	}
	
	public void logInfo(FilterLogMessages FilterLogMessages, String... args) {
		String infoMessage = "["+ componentName + "] ";
		switch (FilterLogMessages) {
		case INCOMING_REQUEST:
			infoMessage += "Incoming request: ";
			break;
		case STOPP:
			infoMessage += "No more requests, stopping service.";
			break;
		case START:
			infoMessage += "Listening for requests on ";
			break;
		case ENTRY_REMOVED:
			infoMessage += "Removing entry ";
			break;
		case ENTRY_ADDED:
			infoMessage += "Adding entry ";
			break;
		case OPENXC_CONNECTION:
			infoMessage += "Connecting to openXC simulator at ";
			break;
		case NO_MORE_ENTRIES:
			infoMessage += "No more entries to update. Shutting down. ";
			break;
		case SENDING_RESPONSE:
			infoMessage += "Sending response: ";
			break;
		case STARTING_RESPONSE:
			infoMessage += "Starting response of ";
			break;
		case STOPPING_RESPONSE:
			infoMessage += "Stopping response of ";
			break;
		default:
			break;
		}
		for (String arg : args) {
			infoMessage += arg + " : ";
		}
		System.out.println(infoMessage);
	}

	public void logError(FilterLogMessages FilterLogMessages, String... args) {
		String errorMessage = "["+ componentName + "] ";
		switch (FilterLogMessages) {
		case JSON_ERROR:
			errorMessage += "Error while reading json objects from input stream. ";
			break;
		case REQUEST_ERROR:
			errorMessage += "Unsupported request ";
			break;
		case UNSUPPORTED_REQUEST:
			errorMessage += "Could not process json request. ";
			break;
		case WRONG_CMD_LINE_USAGE:
			errorMessage += "Usage: java CANFilterService <Server host>  <Server Port Number> ";
			break;
		case SOCKET_ERROR:
			errorMessage += "Could not close socket. ";
			break;
		case SOCKET_TIMEOUT:
			errorMessage += "Timeout: Could not connect to ";
			break;
		case SOCKET_CONNECT:
			errorMessage += "Could not connect to ";
			break;	
		case SENDING_RESPONSE:
			errorMessage += "Error while sending response for ";
			break;	
		default:
			break;
		}
		for (String arg : args) {
			errorMessage += arg + " : ";
		}
		System.err.println(errorMessage);
	}

}
