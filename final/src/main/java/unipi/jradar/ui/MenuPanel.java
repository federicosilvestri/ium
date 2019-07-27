package unipi.jradar.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;

import unipi.jradar.core.Engine.Mode;
import unipi.jradar.core.Engine.Status;
import unipi.jradar.core.SystemStatusNotifier;
import unipi.jradar.proxy.SystemProxy;
import unipi.jradar.proxy.SystemStatusListener;

class MenuPanel extends JMenuBar implements SystemStatusListener, ActionListener {

	/**
	 * Default serial UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Configuration menu.
	 */
	private JMenu configurationMenu;

	/**
	 * Port configuration menu
	 */
	private JMenuItem portConfigurationItem;

	/**
	 * Mode menu.
	 */
	private JMenu modeMenu;

	/**
	 * Mode auto selection item.
	 */
	private JRadioButtonMenuItem modeAutoSelectionItem;

	/**
	 * Mode auto selection item.
	 */
	private JRadioButtonMenuItem modeManualSelectionItem;

	/**
	 * Group for mode selection.
	 */
	private ButtonGroup modeSelectionGroup;

	/**
	 * Help menu.
	 */
	private JMenu helpMenu;

	MenuPanel() {
		build();
		customize();
		registerListeners();
		/*
		 * registering to StatusProxy
		 */
		SystemStatusNotifier.getInstance().register(this);
	}

	private void build() {
		configurationMenu = new JMenu("Configuration");
		portConfigurationItem = new JMenuItem("Port");

		configurationMenu.add(portConfigurationItem);
		add(configurationMenu);

		modeMenu = new JMenu("Mode");
		modeSelectionGroup = new ButtonGroup();
		modeAutoSelectionItem = new JRadioButtonMenuItem("Auto");
		modeManualSelectionItem = new JRadioButtonMenuItem("Manual");
		modeSelectionGroup.add(modeAutoSelectionItem);
		modeSelectionGroup.add(modeManualSelectionItem);
		modeMenu.add(modeAutoSelectionItem);
		modeMenu.add(modeManualSelectionItem);

		/*
		 * activating the current mode
		 */
		switch (SystemProxy.getInstance().getMode()) {
		case AUTO:
			modeAutoSelectionItem.setSelected(true);
			break;
		case MANUAL:
			modeManualSelectionItem.setSelected(true);
			break;
		default:
			throw new RuntimeException("Invalid mode detected");
		}

		add(modeMenu);

		helpMenu = new JMenu("?");
		add(helpMenu);
	}

	private void customize() {
		// TODO Auto-generated method stub

	}

	private void registerListeners() {
		portConfigurationItem.addActionListener(this);
		modeAutoSelectionItem.addActionListener(this);
		modeManualSelectionItem.addActionListener(this);
	}

	@Override
	public void statusUpdated(Status status) {
	}

	@Override
	public void portUpdated(String port) {
	}

	@Override
	public void modeUpdated(Mode mode) {
		if (mode == Mode.MANUAL) {
			modeManualSelectionItem.setEnabled(true);
		} else if (mode == Mode.AUTO) {
			modeAutoSelectionItem.setEnabled(true);
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == portConfigurationItem) {
			new PortSelectionDialog((JFrame) SwingUtilities.getWindowAncestor(this));
		} else if (event.getSource() == modeAutoSelectionItem) {
			SystemProxy.getInstance().setMode(Mode.AUTO);
		} else if (event.getSource() == modeManualSelectionItem) {
			SystemProxy.getInstance().setMode(Mode.MANUAL);
		}
	}

}
