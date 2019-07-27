package unipi.jradar.core;

import jssc.SerialPort;

/**
 * This class represents the physics and non-physics system constants.
 * 
 * @author federicosilvestri
 *
 */
public class SystemConstant {
	/**
	 * Start angle.
	 */
	public static final double THETA_0 = 0.628;

	/**
	 * The angle that stepper does in one step.
	 */
	public static final double THETA_STEP = 0.002908;

	/**
	 * The size of radius.
	 */
	public static final double R = 2.5;

	/**
	 * The size of length.
	 */
	public static final double L = 6.2;

	/**
	 * The size of heigth.
	 */
	public static final double H = 6.0;

	/**
	 * The standard increment of height.
	 */
	public static final int HEIGHT_INCREMENT = 50;

	/**
	 * The standard increment of angle.
	 */
	public static final int ANGLE_INCREMENT = 10;

	/**
	 * The minimum height.
	 */
	public static final int MIN_H = 0;

	/**
	 * The maximum height.
	 */
	public static final int MAX_H = 600;

	/**
	 * The maximum angle.
	 */
	public static final int MAX_ANGLE = 180;

	/**
	 * The minimum angle.
	 */
	public static final int MIN_ANGLE = 0;

	/**
	 * Baud rate of port.
	 */
	static final int BAUD_RATE = SerialPort.BAUDRATE_57600;

	public static final double MAX_VALUE = 200;
}
