package unipi.jradar.proxy;

import unipi.jradar.core.Engine.Mode;
import unipi.jradar.core.Engine.Status;

/**
 * This is an interface for listening events related to system.
 * 
 * @author federicosilvestri
 *
 */
public interface SystemStatusListener {
	/**
	 * The status is updated.
	 * 
	 * @param status
	 *            new status
	 */
	public void statusUpdated(Status status);

	/**
	 * Port is updated
	 * 
	 * @param port
	 *            updated port
	 */
	public void portUpdated(String port);

	/**
	 * Mode is updated.
	 * 
	 * @param mode
	 *            updated mode
	 */
	public void modeUpdated(Mode mode);
}
