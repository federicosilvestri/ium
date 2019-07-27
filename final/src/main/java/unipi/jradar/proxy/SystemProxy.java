package unipi.jradar.proxy;

import org.apache.log4j.Logger;

import unipi.jradar.core.Engine;
import unipi.jradar.core.Engine.Mode;
import unipi.jradar.core.Engine.Status;

/**
 * This class is used only by UI to communicate with backend. UI -> backend
 * 
 * @author federicosilvestri
 *
 */
public final class SystemProxy {

	/**
	 * Private logger.
	 */
	private static final Logger log = Logger.getLogger(SystemProxy.class);

	/**
	 * Singleton instance of System.
	 */
	private static SystemProxy instance;

	/**
	 * Get the instance of system.
	 * 
	 * @return SystemProxy Instance
	 */
	public static SystemProxy getInstance() {
		if (instance == null) {
			throw new RuntimeException("SystemProxy is not initialized yet!");
		}

		return instance;
	}

	public static void create(Engine engine) {
		if (instance != null) {
			throw new RuntimeException("SystemProxy is already initialized!");
		}

		instance = new SystemProxy(engine);
	}

	/**
	 * Private instance of engine.
	 */
	private final Engine engine;

	private SystemProxy(Engine engine) {
		this.engine = engine;
	}

	public String[] getAvailablePorts() {
		return engine.getAvailablePorts();
	}

	public Status getStatus() {
		return engine.getStatus();
	}

	public String getPort() {
		return engine.getCurrentPort();
	}

	public Mode getMode() {
		return engine.getMode();
	}

	public void setMode(Mode mode) {
		log.info("Setting mode " + mode);
		engine.setMode(mode);
	}

	public void setPort(String port) throws Exception {
		log.info("Setting port " + port);
		engine.setPort(port);

	}

	public RadarControlProxy getRadar() {
		return engine;
	}

	public int getHeightSens() {
		return engine.getHeightSens();
	}

	public int getAngleSens() {
		return engine.getAngleSens();
	}

}
