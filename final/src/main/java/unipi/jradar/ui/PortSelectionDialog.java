package unipi.jradar.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import unipi.jradar.proxy.SystemProxy;
import unipi.jradar.util.MediaManager;

/**
 * Dialog for choosing the port.
 * 
 * @author federicosilvestri
 *
 */
public class PortSelectionDialog extends JDialog implements ActionListener {

	/**
	 * Default serial UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Combo box for listing all ports.
	 */
	private JComboBox<String> comboPorts;

	/**
	 * Confirm button to accept changes.
	 */
	private JButton confirmButton;

	/**
	 * Cancel button to cancel changes.
	 */
	private JButton cancelButton;

	/**
	 * Error label.
	 */
	private JLabel errorLabel;

	public PortSelectionDialog(Window mainWindow) {
		super(mainWindow, "Serial port selection");
		build();
		customize();
		setVisible(true);
	}

	private void build() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		JLabel colorLabel = new JLabel("Choose available port:");
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(colorLabel, gbc);
		/*
		 * now get all ports available by proxy.
		 */
		comboPorts = new JComboBox<>();
		String ports[] = SystemProxy.getInstance().getAvailablePorts();
		if (ports == null) {
			comboPorts.addItem("No ports available");
		} else {
			for (String port : ports) {
				comboPorts.addItem(port);
			}
		}
		add(comboPorts);
		gbc.gridwidth = 1;
		gbc.gridx = 1;
		gbc.gridy = 1;
		panel.add(comboPorts, gbc);

		JLabel spacer = new JLabel(" ");
		gbc.gridx = 0;
		gbc.gridy = 2;
		panel.add(spacer, gbc);
		confirmButton = new JButton("Ok");
		confirmButton.addActionListener(this);
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 3;
		panel.add(confirmButton, gbc);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		gbc.gridx = 1;
		gbc.gridy = 3;
		panel.add(cancelButton, gbc);
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 4;
		errorLabel = new JLabel("");
		errorLabel.setForeground(Color.RED);
		panel.add(errorLabel, gbc);
		add(panel);
	}

	private void customize() {
		setModal(true);
		try {
			setIconImage(ImageIO.read(MediaManager.getInstance().locateResource("windowIcon.png")));
		} catch (IOException e) {
			throw new RuntimeException("Cannot read icon image from Media Manager", e);
		}

		setLocation(this.getRootPane().getLocation());

		setResizable(false);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == cancelButton) {
			dispose();
		} else if (evt.getSource() == confirmButton) {
			/*
			 * to improve the application responsiveness we need to start a new thread for
			 * waiting the synchronous return of setPort method.
			 */
			// disable the button
			confirmButton.setEnabled(false);
			cancelButton.setEnabled(false);
			errorLabel.setText("Waiting for hardware...");
			pack();
			new Thread("ActionPerformedThread") {
				public void run() {
					try {
						SystemProxy.getInstance().setPort((String) comboPorts.getSelectedItem());
						dispose();
					} catch (Exception e) {
						errorLabel.setText(e.toString());
						confirmButton.setEnabled(true);
						cancelButton.setEnabled(true);
						pack();
					}
				}
			}.start();
		}
	}

}
