package unipi.jradar.core;

/**
 * This object represent an hardware request to Arduino.
 * 
 * @author federicosilvestri
 *
 */
class HardwareRequest {

	/**
	 * The action "moving".
	 */
	public static final int MOVE_REQUEST = 0;

	/**
	 * The action "read".
	 */
	public static final int READ_REQUEST = 1;

	/**
	 * The format of the command. It should be
	 */
	public static final String COMMAND_FORMAT = "%d;%d;%d;\n";

	/**
	 * The action of request.
	 */
	private final int action;

	/**
	 * Height of sensor.
	 */
	private final int heigth;

	/**
	 * Angle of sensor.
	 */
	private final int angle;

	/**
	 * Create a new hardware request.
	 * 
	 * @param angle
	 *            angle of sensor
	 * @param heigth
	 *            height of sensor.
	 */
	public HardwareRequest(int action, int heigth, int angle) {
		super();

		this.action = action;
		this.angle = angle;
		this.heigth = heigth;
	}

	@Override
	public String toString() {
		return "HardwareRequest [action=" + action + ", heigth=" + heigth + ", angle=" + angle + "]";
	}

	/**
	 * Get the request string.
	 * 
	 * @return string request
	 */
	public String getRequest() {
		String command = String.format(COMMAND_FORMAT, action, heigth, angle);
		return command;
	}

}
