package com.testingtech.playits.canfilter.connector;

import java.io.IOException;

public interface ResourceConnector {

	void disconnect();
	void connect() throws IOException;
}
