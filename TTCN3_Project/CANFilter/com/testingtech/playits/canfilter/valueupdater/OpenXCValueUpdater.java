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
package com.testingtech.playits.canfilter.valueupdater;

import java.util.Date;
import java.util.Hashtable;

import org.json.JSONException;

import com.testingtech.playits.canfilter.CANFilterLog;
import com.testingtech.playits.canfilter.Car2XEntry;
import com.testingtech.playits.canfilter.FilterLogMessages;

public class OpenXCValueUpdater implements ValueUpdater {

	private Hashtable<String, Car2XEntry> car2xEntries;
	private CANFilterLog canFilterLog = new CANFilterLog(
			OpenXCValueUpdater.class.getSimpleName());

	/**
	 * Connects to the openXC simulator and updates openXC respectively obd2
	 * values within a hash table provided as a parameter.
	 * 
	 * @param car2xEntries
	 */
	public OpenXCValueUpdater(Hashtable<String, Car2XEntry> car2xEntries) {
		this.car2xEntries = car2xEntries;
	}
	
	@Override
	public void updateEntry(String key, Object value) {
		// TODO enable to debug
//		 System.out.println("[EntryUpdater] Incoming object: " + response);
		try {
			Car2XEntry car2xEntry = car2xEntries.get(key);
			if (car2xEntry != null) {
				updateEntry(car2xEntry, value);
			}
		} catch (JSONException jsone) {
			canFilterLog.logError(FilterLogMessages.JSON_ERROR);
		}
	}

	// TODO add obd2 key
	private void updateEntry(Car2XEntry car2xEntry, Object value)
			throws JSONException {
		car2xEntry.setTimestamp(new Date().getTime());
		car2xEntry.setValue(value);
		
		// TODO no more event value
//		try {
//			car2xEntry.setEvent(jsonObject.getBoolean("event"));
//		} catch (JSONException jsonException) {
//			// ignore missing event, because not every incoming
//			// JSON object has an event
//		}
	}
}
