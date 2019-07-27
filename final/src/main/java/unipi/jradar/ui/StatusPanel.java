package unipi.jradar.ui;

import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import unipi.jradar.core.Engine.Mode;
import unipi.jradar.core.Engine.Status;
import unipi.jradar.core.SystemStatusNotifier;
import unipi.jradar.proxy.SystemProxy;
import unipi.jradar.proxy.SystemStatusListener;

class StatusPanel extends JPanel implements SystemStatusListener {

	/**
	 * Default serial UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The number of total labels
	 */
	private static final int TOTAL_LABELS = 3;

	/**
	 * SystemProxy object
	 */
	private final SystemProxy systemProxy;

	private JLabel currentStatusLabel;
	private JLabel currentStatus;
	private JLabel currentPortLabel;
	private JLabel currentPort;
	private JLabel currentModeLabel;
	private JLabel currentMode;

	StatusPanel() {
		systemProxy = SystemProxy.getInstance();
		build();
		customize();
		/*
		 * registering to proxy
		 */
		SystemStatusNotifier.getInstance().register(this);
	}

	private JLabel makeLabel(String name) {
		JLabel label = new JLabel(name);
		label.setFont(new Font("Verdana", Font.BOLD, 12));
		return label;
	}

	private void build() {
		/*
		 * setting the right layout for status bar
		 */
		setLayout(new GridLayout(1, TOTAL_LABELS * 3 - 1));

		/*
		 * building components
		 */
		currentStatusLabel = makeLabel("Hardware Status");
		currentStatus = new JLabel(systemProxy.getStatus().name());
		currentPortLabel = makeLabel("Port");
		currentPort = new JLabel(systemProxy.getPort());
		currentModeLabel = makeLabel("Mode");
		currentMode = new JLabel(systemProxy.getMode().name());

		/*
		 * adding components to panel
		 */
		add(currentStatusLabel);
		add(currentStatus);
		add(new JSeparator(JSeparator.VERTICAL));
		add(currentPortLabel);
		add(currentPort);
		add(new JSeparator(JSeparator.VERTICAL));
		add(currentModeLabel);
		add(currentMode);
	}

	private void customize() {
		// TODO Auto-generated method stub

	}

	@Override
	public void statusUpdated(Status status) {
		currentStatus.setText(status.name());
	}

	@Override
	public void portUpdated(String port) {
		currentPort.setText(port);
	}

	@Override
	public void modeUpdated(Mode mode) {
		currentMode.setText(mode.name());
	}

}
