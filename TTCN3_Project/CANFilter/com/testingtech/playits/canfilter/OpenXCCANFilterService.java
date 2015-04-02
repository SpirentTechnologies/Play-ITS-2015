/*
 * ----------------------------------------------------------------------------
 *  (C) Copyright Testing Technologies.  All Rights Reserved.
 *
 *  All copies of this program, whether in whole or in part, and whether
 *  modified or not, must display this and all other embedded copyright
 *  and ownership notices in full.
 *
 *  See the file COPYRIGHT for details of redistribution and use.
 *
 *  You should have received a copy of the COPYRIGHT file along with
 *  this file; if not, write to the Testing Technologies,
 *  Michaelkirchstr. 17/18, 10179 Berlin, Germany.
 *
 *  TESTING TECHNOLOGIES DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS
 *  SOFTWARE. IN NO EVENT SHALL TESTING TECHNOLOGIES BE LIABLE FOR ANY
 *  SPECIAL, DIRECT, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 *  WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN
 *  AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
 *  ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 *  THIS SOFTWARE.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND,
 *  EITHER EXPRESSED OR IMPLIED, INCLUDING ANY KIND OF IMPLIED OR
 *  EXPRESSED WARRANTY OF NON-INFRINGEMENT OR THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.
 * ----------------------------------------------------------------------------
 */
package com.testingtech.playits.canfilter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Hashtable;

import org.json.JSONException;

import com.testingtech.playits.canfilter.connector.OpenXCResourceConnector;
import com.testingtech.playits.canfilter.valueupdater.OpenXCValueUpdater;

public class OpenXCCANFilterService {

	static CANFilterLog canFilterLog = CANFilterLog
			.getLog(OpenXCCANFilterService.class.getSimpleName());
	static Hashtable<String, Car2XEntry> car2xEntries = new Hashtable<>();

	/**
	 * Provides TCP server functionality to handle JSON requests. Upon receiving
	 * a start request, an entry updater thread which updates openXC / obd2 hash
	 * table entries is started. Parallel timeout responder tasks periodically
	 * send back updated JSON values from the hash table over a client socket.
	 * 
	 * @param args
	 *            host, port
	 */
	public static void main(String[] args) {
		try {
			InetSocketAddress address = SocketUtils.createAddress(args[2],
					args[3]);
			OpenXCResourceConnector openXCResourceConnector = new OpenXCResourceConnector(
					address, car2xEntries);
			InetSocketAddress serverAddress = SocketUtils.createAddress(
					args[0], args[1]);
			OpenXCValueUpdater openXCValueUpdater = new OpenXCValueUpdater(
					openXCResourceConnector, car2xEntries);
			new CANFilterService(serverAddress, openXCResourceConnector,
					car2xEntries, openXCValueUpdater).startFilter();
		} catch (JSONException e) {
			canFilterLog.logError(FilterLogMessages.JSON_ERROR);
		} catch (IOException e) {
			canFilterLog.logError(FilterLogMessages.REQUEST_ERROR,
					e.getMessage());
		} catch (AddressInstantiationException e) {
			canFilterLog.logError(e.getMessage());
		}
	}
}
