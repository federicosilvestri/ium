package unipi.jradar.core;

import org.apache.log4j.Logger;

/**
 * The class represents the request to be sent to Arduino Hardware.
 * 
 * @author federicosilvestri
 *
 */
class HardwareResponse {
	/**
	 * Internal Logger.
	 */
	private final static Logger log = Logger.getLogger(HardwareResponse.class);
	/**
	 * The size of response data.
	 */
	static final int RESPONSE_DATA_SIZE = 4;
	/**
	 * 
	 * Start character of data.
	 */
	static final String RESPONSE_START_CHAR = "A";
	/**
	 * End character of data-
	 */
	static final String RESPONSE_ENDING_CHAR = "B";
	/**
	 * The index of status.
	 */
	static final int RESPONSE_STATUS_INDEX = 0;
	/**
	 * The index of height.
	 */
	static final int RESPONSE_H_INDEX = 2;
	/**
	 * The index of angle.
	 */
	static final int RESPONSE_ANGLE_INDEX = 3;
	/**
	 * The index of value.
	 */
	static final int RESPONSE_VALUE_INDEX = 1;
	/**
	 * The response status code.
	 */
	static final int RESPONSE_STATUS_OK = 4;
	/**
	 * The response status invalid.
	 */
	static final int RESPONSE_STATUS_INVALID = 3;

	/**
	 * Response status.
	 */
	final int status;
	/**
	 * Response height.
	 */
	final int h;
	/**
	 * Response angle.
	 */
	final int angle;
	/**
	 * Response value.
	 */
	final double value;

	/**
	 * Create a new hardware request.
	 * 
	 * @param status
	 *            the status of response
	 * @param h
	 *            the height
	 * @param angle
	 *            the angle of response
	 * @param value
	 *            the value of response
	 */
	HardwareResponse(int status, int h, int angle, double value) {
		super();
		this.status = status;
		this.h = h;
		this.angle = angle;
		this.value = value;
	}

	static HardwareResponse parseResponse(String response) throws Exception {
		if (response == null || response.length() == 0) {
			throw new Exception("Response is empty");
		}

		if (!(response.startsWith(RESPONSE_START_CHAR) && response.endsWith(RESPONSE_ENDING_CHAR))) {
			log.warn("Received invalid response from hardware");
			throw new Exception("Cannot parse the data");
		}

		String cleaned = response.substring(1);
		cleaned = cleaned.substring(0, cleaned.length() - 1);
		String pieces[] = cleaned.split(";");

		if (pieces.length != RESPONSE_DATA_SIZE) {
			log.error("Received invalid data from hardware. Array of data is not matching the right size.");
			log.error(response);
			throw new Exception("Cannot parse the data");
		}

		// applying the data
		try {
			int status = Integer.parseInt(pieces[RESPONSE_STATUS_INDEX]);
			int currentAngle = Integer.parseInt(pieces[RESPONSE_ANGLE_INDEX]);
			int currentHeight = Integer.parseInt(pieces[RESPONSE_H_INDEX]);
			double value = Double.parseDouble(pieces[RESPONSE_VALUE_INDEX]);

			return new HardwareResponse(status, currentHeight, currentAngle, value);
		} catch (NumberFormatException ex) {
			log.error("Received invalid data from hardware. Cannot convert string to number.");
			throw new Exception("Cannot parse the data");
		}
	}

	@Override
	public String toString() {
		return "HardwareResponse [status=" + status + ", h=" + h + ", angle=" + angle + ", value=" + value + "]";
	}

}
