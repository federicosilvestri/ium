package unipi.jradar.core;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * This class is the Radar Data Container. It contains the data sent by radar.
 * 
 * @author federicosilvestri
 *
 */
public class RadarDataContainer {

	/**
	 * Internal logger.
	 */
	private static final Logger log = Logger.getLogger(RadarDataContainer.class);

	/**
	 * Singleton instance.
	 */
	private static RadarDataContainer instance;

	/**
	 * Get instance of container.
	 * 
	 * @return singleton instance of radar data container.
	 */
	public static RadarDataContainer getInstance() {
		if (instance == null) {
			instance = new RadarDataContainer();
			log.info("Radar Data Container is now instanced!");
		}

		return instance;
	}

	/**
	 * Maximum number of radar data to keep in memory
	 */
	final static int DEFAULT_MEMORY_SIZE = 10;

	/**
	 * The store of container.
	 */
	private List<RadarData> store;

	/**
	 * Memory size.
	 */
	private int memorySize;

	/**
	 * Radar Data Notifier.
	 */
	private final RadarDataNotifier rdn;

	private RadarDataContainer() {
		store = Collections.synchronizedList(new LinkedList<RadarData>());
		rdn = RadarDataNotifier.getInstance();
		memorySize = DEFAULT_MEMORY_SIZE;
	}

	void pushData(RadarData data) {
		if (store.size() >= memorySize) {
			store.remove(0);
		}

		store.add(store.size(), data);

		// notify the available data
		rdn.updateRadarData(data);
	}

	public List<RadarData> getList() {
		return new LinkedList<>(this.store);
	}

	public RadarData getLast() {
		return this.store.get(0);
	}

	void resetMemory() {
		this.store.clear();
	}

	void shutdown() {
		/*
		 * I've nothing to do, my only responsibility is the RDN.
		 */
		rdn.shutdown();

	}

	void setMemory(int size) {
		this.memorySize = size;

	}

	public int getMemorySize() {
		return this.memorySize;
	}

	public int getSize() {
		return this.store.size();
	}
}
