package unipi.jradar.core;

import org.apache.log4j.Logger;

import jssc.SerialPort;
import unipi.jradar.proxy.RadarControlProxy;

/**
 * This class represents the abstract engine behind backend.
 * 
 * @author federicosilvestri
 *
 */
public abstract class Engine implements RadarControlProxy {
	/**
	 *
	 * Status of system.
	 * 
	 * @author federicosilvestri
	 *
	 */
	public static enum Status {
		NOT_CONFIGURED, READY
	}

	/**
	 * Mode of system.
	 * 
	 * @author federicosilvestri
	 *
	 */
	public static enum Mode {
		AUTO, MANUAL
	};

	/**
	 * Current logger
	 */
	protected static final Logger log = Logger.getLogger(Engine.class);

	/**
	 * Current status of system.
	 */
	private Status status;

	/**
	 * Current mode of system.
	 */
	private Mode mode;

	/**
	 * Current port.
	 */
	protected SerialPort port;

	/**
	 * Current sensibility of height command.
	 */
	protected int heightSens;

	/**
	 * Current sensibility of angle command.
	 */
	protected int angleSens;

	protected Engine() {
		this.status = Status.NOT_CONFIGURED;
		this.port = null;
		this.mode = Mode.MANUAL;
	}

	/**
	 * Get all available ports of system.
	 * 
	 * @return an array of available ports.
	 */
	public abstract String[] getAvailablePorts();

	/**
	 * Shutdown the engine.
	 */
	public abstract void shutdown();

	/**
	 * Get system status.
	 * 
	 * @return Engine.Status object
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Set the system status.
	 * 
	 * @param status
	 *            the system status
	 */
	protected void setStatus(Status status) {
		this.status = status;
		SystemStatusNotifier.getInstance().updateStatus(status);
	}

	/**
	 * Get selected port.
	 * 
	 * @return String port identifier
	 */
	public String getCurrentPort() {
		if (port == null) {
			return null;
		}

		return port.getPortName();
	}

	/**
	 * Get the current mode of system.
	 * 
	 * @return Mode enumeration
	 */
	public Mode getMode() {
		return this.mode;
	}

	/**
	 * Set the mode of system
	 * 
	 * @param mode
	 *            mode to use
	 */
	public void setMode(Mode mode) {
		SystemStatusNotifier.getInstance().updateMode(mode);
	}

	public void setPort(String port) throws Exception {
		if (port == null) {
			throw new NullPointerException("You can't choose a null port!");
		}

		this.port = new SerialPort(port);
		SystemStatusNotifier.getInstance().updatePort(port);
	}

	/**
	 * Get sensibility of height command.
	 * 
	 * @return integer value
	 */
	public int getHeightSens() {
		return this.heightSens;
	}

	/**
	 * Get sensibility of angle command.
	 * 
	 * @return integer value
	 */
	public int getAngleSens() {
		return this.angleSens;
	}

}
