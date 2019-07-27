package unipi.jradar.ui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.apache.log4j.Logger;

import unipi.jradar.core.SystemStatusNotifier;
import unipi.jradar.core.Engine.Mode;
import unipi.jradar.core.Engine.Status;
import unipi.jradar.proxy.RadarControlProxy;
import unipi.jradar.proxy.SystemProxy;
import unipi.jradar.proxy.SystemStatusListener;

/**
 * This class is the class that controls the key of system.
 * 
 * @author federicosilvestri
 *
 */
class RadarKeyListener implements KeyListener, SystemStatusListener {

	/**
	 * Internal logger.
	 */
	private final static Logger log = Logger.getLogger(RadarKeyListener.class);

	/**
	 * Instance of Radar Proxy
	 */
	private final RadarControlProxy radarProxy;

	/**
	 * Enabled or not enabled.
	 */
	private boolean enabled;

	RadarKeyListener() {
		radarProxy = SystemProxy.getInstance().getRadar();
		enabled = false;
		// registering to System Status Listener
		SystemStatusNotifier.getInstance().register(this);
	}

	private void radarProxyAction(int action) {
		if (enabled) {
			switch (action) {
			case KeyEvent.VK_LEFT:
				radarProxy.goLeft();
				break;
			case KeyEvent.VK_RIGHT:
				radarProxy.goRight();
				break;
			case KeyEvent.VK_UP:
				radarProxy.goUp();
				break;
			case KeyEvent.VK_DOWN:
				radarProxy.goDown();
				break;
			case KeyEvent.VK_SPACE:
				radarProxy.read();
				break;
			case KeyEvent.VK_DELETE:
			case KeyEvent.VK_ESCAPE:
				radarProxy.resetMemory();
				break;

			}
		}
	}

	private void radarProxySensAction(int action) {
		switch (action) {
		case KeyEvent.VK_LEFT:
			radarProxy.changeAngleSens(-1);
			break;
		case KeyEvent.VK_RIGHT:
			radarProxy.changeAngleSens(+1);
			break;
		case KeyEvent.VK_UP:
			radarProxy.changeHeightSens(+1);
			break;
		case KeyEvent.VK_DOWN:
			radarProxy.changeHeightSens(-1);
			break;
		}

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.isShiftDown()) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_UP:
			case KeyEvent.VK_DOWN:
				radarProxySensAction(e.getKeyCode());
				e.consume();
				break;

			}
		} else {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_UP:
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_SPACE:
			case KeyEvent.VK_DELETE:
				radarProxyAction(e.getKeyCode());
				e.consume();
				break;

			}
		}

		// System.out.println(e.getKeyCode());

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (e.getKeyChar() == 'M') {
			radarProxy.changeMemorySize(+1);
		} else if (e.getKeyChar() == 'm') {
			radarProxy.changeMemorySize(-1);
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnable(boolean enable) {
		this.enabled = enable;
	}

	@Override
	public void statusUpdated(Status status) {
		if (status == Status.READY) {
			this.enabled = true;
			log.debug("Enabling the keyboard, status changed");
		} else {
			this.enabled = false;
			log.debug("Disabling the keyboard, status changed");
		}
	}

	@Override
	public void portUpdated(String port) {
		// do nothing
	}

	@Override
	public void modeUpdated(Mode mode) {
		// do nothing
	}

}
