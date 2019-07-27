package unipi.jradar.core;

import unipi.jradar.proxy.RadarControlProxy;
import unipi.jradar.proxy.RadarDataListener;

/**
 * This class is the thread that manages the command to system. s
 * 
 * @author federicosilvestri
 *
 */
class AutoThread extends Thread implements RadarDataListener {

	/**
	 * The stop variable.
	 */
	private boolean stop;

	private double h;
	private double theta;
	private final RadarControlProxy radar;
	private boolean hReverse;
	private boolean thetaReverse;

	AutoThread(RadarControlProxy radar, int hIncrement, int thetaInrement) {
		h = SystemConstant.MIN_H;
		theta = SystemConstant.MIN_ANGLE;
		this.radar = radar;

		setName("AutoThread for AUTO Mode");
		setPriority(Thread.MIN_PRIORITY);
	}

	@Override
	public synchronized void start() {
		this.stop = false;
		RadarDataNotifier.getInstance().register(this);
		super.start();
	}

	public void setStop() {
		this.stop = true;
		RadarDataNotifier.getInstance().unregister(this);
	}

	private void updateH() {
		if (hReverse) {
			if (h <= SystemConstant.MIN_H) {
				hReverse = false;
				radar.goUp();
			} else {
				radar.goDown();
			}
		} else {
			if (h >= SystemConstant.MAX_H) {
				hReverse = true;
				radar.goDown();
			} else {
				radar.goUp();
			}
		}
	}

	@Override
	public void run() {
		// initialize
		radar.setAngle(SystemConstant.MIN_ANGLE);
		radar.setHeight(SystemConstant.MIN_H);
		
		while (!(stop || Thread.interrupted())) {
			// update
			if (thetaReverse) {
				if (theta <= SystemConstant.MIN_ANGLE) {
					thetaReverse = false;
					// radar.goRight();
					updateH();
				} else {
					radar.goLeft();
				}
			} else {
				if (theta >= SystemConstant.MAX_ANGLE) {
					thetaReverse = true;
					// radar.goLeft();
					updateH();
				} else {
					radar.goRight();
				}
			}

			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void newDataAvailable(RadarData radarData) {
		this.theta = radarData.angle;
		this.h = radarData.altitude;
	}

}