package unipi.jradar.core;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;

import unipi.jradar.proxy.RadarDataListener;

/**
 * This class is an implementation of Status Notifier for System.
 * 
 * @author federicosilvestri
 *
 */
public final class RadarDataNotifier {

	/**
	 * Max Threads for notifier.
	 */
	private static final int MAX_THREADS = 3;

	/**
	 * Internal logger.
	 */
	private static final Logger log = Logger.getLogger(RadarDataNotifier.class);

	private static RadarDataNotifier instance;

	public synchronized static RadarDataNotifier getInstance() {
		if (instance == null) {
			instance = new RadarDataNotifier();
		}

		return instance;
	}

	private final Set<RadarDataListener> registered;
	private final ThreadPoolExecutor threadPool;

	private RadarDataNotifier() {
		registered = new HashSet<>();
		threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		threadPool.setMaximumPoolSize(MAX_THREADS);
	}

	public void register(RadarDataListener rdl) {
		if (rdl == null) {
			throw new NullPointerException("You cannot pass null StatusProxyable instance!");
		}

		registered.add(rdl);
	}

	synchronized void updateRadarData(RadarData data) {
		/*
		 * we need to do it asynchronously so lets create a new thread.
		 */
		Runnable e = () -> {
			for (RadarDataListener rdl : registered) {
				rdl.newDataAvailable(data);
			}
		};

		// submitting to thread pool
		threadPool.submit(e);
	}

	void shutdown() {
		log.info("Radar Data Notifier has received a shut off signal");
		threadPool.shutdown();
		log.info("Radar Data Notifier is now shut off");
	}

	public void unregister(AutoThread autoThread) {
		this.registered.remove(autoThread);
	}

}
