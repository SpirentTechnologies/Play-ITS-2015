package canfilter;

import java.net.Socket;
import java.util.Hashtable;
import java.util.Timer;

public class TimeoutResponder {

	private Socket socket;
	private Hashtable<String, Timer> timers = new Hashtable<>();
	private Hashtable<String, Car2XEntry> car2xEntries;

	public TimeoutResponder(Socket socket, Hashtable<String, Car2XEntry> car2xEntries) {
		this.socket = socket;
		this.car2xEntries = car2xEntries;
	}

	public void addTimer(String key, int interval) {
		if (timers.contains(key))
			timers.get(key).cancel();
		Timer timer = new Timer();
		timers.put(key, timer);
		timer.schedule(new ResponderTask(key, car2xEntries.get(key), socket),
				interval, interval);
	}

	public void removeTimer(String key) {
		timers.get(key).cancel();
		timers.remove(key);
	}
}