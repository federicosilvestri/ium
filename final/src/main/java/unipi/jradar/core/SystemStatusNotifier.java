package unipi.jradar.core;

import java.util.HashSet;
import java.util.Set;

import unipi.jradar.core.Engine.Mode;
import unipi.jradar.core.Engine.Status;
import unipi.jradar.proxy.SystemStatusListener;

/**
 * This class is an implementation of Status Notifier for System.
 * 
 * @author federicosilvestri
 *
 */
public final class SystemStatusNotifier {

	private static SystemStatusNotifier instance;

	public synchronized static SystemStatusNotifier getInstance() {
		if (instance == null) {
			instance = new SystemStatusNotifier();
		}

		return instance;
	}

	private final Set<SystemStatusListener> registered;

	private SystemStatusNotifier() {
		registered = new HashSet<>();
	}

	public void register(SystemStatusListener sp) {
		if (sp == null) {
			throw new NullPointerException("You cannot pass null StatusProxyable instance!");
		}

		registered.add(sp);
	}

	synchronized void updateStatus(Status status) {
		Runnable e = () -> {
			for (SystemStatusListener sp : registered) {
				sp.statusUpdated(status);
			}
		};

		startThread(e);
	}

	synchronized void updatePort(String port) {
		Runnable e = () -> {
			for (SystemStatusListener sp : registered) {
				sp.portUpdated(port);
			}
		};
		startThread(e);
	}

	synchronized void updateMode(Mode mode) {
		Runnable e = () -> {
			for (SystemStatusListener sp : registered) {
				sp.modeUpdated(mode);
			}
		};
		startThread(e);
	}

	private void startThread(Runnable e) {
		Thread t = new Thread(e);
		t.setName("System Status Notifier Thread");
		t.setPriority(Thread.NORM_PRIORITY);
		t.start();
	}

}
