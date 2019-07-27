package unipi.jradar.core;

/**
 * This class represents the RadarData created by Engine.
 * 
 * @author federicosilvestri
 *
 */
public class RadarData {

	public static final int MAX_HEIGHT = 600;

	/**
	 * The altitude of radar sensor.
	 */
	public final double altitude;
	/**
	 * The angle of radar sensor.
	 */
	public final double angle;
	/**
	 * The value received by sonar.
	 */
	public final double value;

	/**
	 * Create a new radar data.
	 * 
	 * @param altitude
	 *            the altitude daya
	 * @param angle
	 *            angle data
	 * @param value
	 *            value data
	 */
	public RadarData(double altitude, double angle, double value) {
		super();
		this.altitude = altitude;
		this.angle = angle;

		if (value >= SystemConstant.MAX_VALUE) {
			this.value = SystemConstant.MAX_VALUE;
		} else {
			this.value = value;
		}
	}

	/**
	 * Get the real eight of system.
	 * 
	 * @return double that represents the real heigth.
	 */
	public double getRealAltitude() {
		double piece1 = Math.pow(SystemConstant.L, 2)
				- (SystemConstant.R * Math.sin(SystemConstant.THETA_STEP * this.altitude + SystemConstant.THETA_0));
		double piece2 = (SystemConstant.R
				* Math.cos(SystemConstant.THETA_STEP * this.altitude + SystemConstant.THETA_0));

		double res = Math.sqrt(piece1) - piece2 + SystemConstant.H;

		return res;
	}

}
