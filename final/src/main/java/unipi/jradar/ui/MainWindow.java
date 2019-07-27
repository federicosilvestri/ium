package unipi.jradar.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import unipi.jradar.App;
import unipi.jradar.util.MediaManager;

public class MainWindow extends JFrame implements WindowListener {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Internal reference to MediaManager class.
	 */
	private final MediaManager mediaManager;

	/**
	 * Menu panel.
	 */
	private MenuPanel menuPanel;

	/**
	 * Status panel.
	 */
	private StatusPanel statusPanel;

	/**
	 * Radar Panel Canvas.
	 */
	private RadarPanel radarPanel;

	/**
	 * Preferred dimensions.
	 */
	private static final Dimension PREFERRED_DIMENSION = new Dimension(500, 500);

	/**
	 * Minimum dimension.
	 */
	private static final Dimension MINIMUM_DIMENSION = new Dimension(400, 400);

	public MainWindow() {
		super("JRadar - IUM Project");
		mediaManager = MediaManager.getInstance();
		build();
		registerListeners();
		customize();
		setVisible(true);
	}

	private void customize() {
		try {
			Image icon;
			icon = ImageIO.read(mediaManager.locateResource("windowIcon.png"));
			setIconImage(icon);
		} catch (IOException e) {
			throw new RuntimeException("Cannot read image from Media Manager", e);
		}

		Toolkit.getDefaultToolkit().setDynamicLayout(false);

		setLocation(0, 0);
		setMinimumSize(MINIMUM_DIMENSION);
		setPreferredSize(PREFERRED_DIMENSION);
		pack();
	}

	private void build() {
		setLayout(new BorderLayout());

		menuPanel = new MenuPanel();
		setJMenuBar(menuPanel);

		radarPanel = new RadarPanel();
		add(radarPanel, BorderLayout.CENTER);

		statusPanel = new StatusPanel();
		add(statusPanel, BorderLayout.SOUTH);
	}

	private void registerListeners() {
		addWindowListener(this);
	}

	public void windowActivated(WindowEvent arg0) {
	}

	public void windowClosed(WindowEvent arg0) {
	}

	public void windowClosing(WindowEvent arg0) {
		App.getInstance().shutdown();
	}

	public void windowDeactivated(WindowEvent arg0) {
	}

	public void windowDeiconified(WindowEvent arg0) {
	}

	public void windowIconified(WindowEvent arg0) {
	}

	public void windowOpened(WindowEvent arg0) {
	}

	@Override
	public void dispose() {
		radarPanel.getAnimator().stop();
		super.dispose();
	}

}
