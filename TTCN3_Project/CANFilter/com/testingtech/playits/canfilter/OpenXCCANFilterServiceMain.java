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

import org.json.JSONException;

public class OpenXCCANFilterServiceMain {

	static CANFilterLog canFilterLog = new CANFilterLog(
			OpenXCCANFilterServiceMain.class.getSimpleName());
	
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
			new OpenXCCANFilterService(args).startFilter();
		} catch (JSONException e) {
			canFilterLog.logError(FilterLogMessages.JSON_ERROR);
		} catch (IOException e) {
			canFilterLog.logError(FilterLogMessages.REQUEST_ERROR, e.getMessage());
		} catch (AddressInstantiationException e) {
			//TODO log error
		} 
	}
}
