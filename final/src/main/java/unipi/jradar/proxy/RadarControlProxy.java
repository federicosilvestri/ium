package unipi.jradar.proxy;

/**
 * This interface represents the control of radar.
 * 
 * @author federicosilvestri
 *
 */
public interface RadarControlProxy {
	/**
	 * Go left with radar.
	 */
	public void goLeft();

	/**
	 * Go right with radar.
	 */
	public void goRight();

	/**
	 * Go up with radar.
	 */
	public void goUp();

	/**
	 * Go down with radar.
	 */
	public void goDown();

	/**
	 * Set the height of system.
	 * 
	 * @param heigth
	 *            integer value of height.
	 */
	public void setHeight(int heigth);

	/**
	 * Set the angle of system.
	 * 
	 * @param heigth
	 *            integer value of angle
	 */
	public void setAngle(int heigth);

	/**
	 * Trigger sensor read.
	 */
	public void read();

	/**
	 * Set the sensibility of height command.
	 * 
	 * @param sens
	 *            integer that represents how many height change in a command
	 *            execution
	 */
	public void changeHeightSens(int sens);

	/**
	 * Set the sensibility of angle command.
	 * 
	 * @param sens
	 *            integer that represents how many angle change in a command
	 *            execution
	 */
	public void changeAngleSens(int sens);

	/**
	 * Set the size of memory
	 * 
	 * @param size
	 *            integer number that represents how many object store
	 */
	public void changeMemorySize(int size);

	/**
	 * Reset the radar memory.
	 */
	public void resetMemory();
}
