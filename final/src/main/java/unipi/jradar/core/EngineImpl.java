package unipi.jradar.core;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 * This class represents the Engine of communication between local machine and
 * Arduino.
 * 
 * @author federicosilvestri
 *
 */
public class EngineImpl extends Engine implements SerialPortEventListener {
	private int currentHeight;
	private int currentAngle;
	private final HardwareBuffer hw;
	private final RadarDataContainer rdc;
	private AutoThread autoThread;
	private int memorySize;
	private final int MAX_MEMORY_SIZE = 200;
	private final int H_STEP = 1;
	private final int THETA_STEP = 1;
	private final int MIN_H_SENS = 1;
	private final int MAX_H_SENS = 50;
	private final int MIN_THETA_SENS = 1;
	private final int MAX_THETA_SENS = 10;

	public EngineImpl() {
		super();
		executeLocalTests();
		currentHeight = SystemConstant.MIN_H;
		currentAngle = SystemConstant.MIN_ANGLE;
		hw = new HardwareBuffer();
		rdc = RadarDataContainer.getInstance();
		this.memorySize = RadarDataContainer.DEFAULT_MEMORY_SIZE;
		this.angleSens = MAX_THETA_SENS / 2;
		this.heightSens = MAX_H_SENS / 2;
	}

	private void executeLocalTests() {
		// trying to get ports
		String[] ports = getAvailablePorts();

		if (ports.length == 0) {
			log.error("No RXTX ports available!");
		}

	}

	public String[] getAvailablePorts() {
		/*
		 * scanning available ports
		 */
		return SerialPortList.getPortNames();
	}

	@Override
	public void setMode(Mode mode) {
		if (getStatus() == Status.READY) {
			switch (mode) {
			case AUTO:
				if (autoThread != null) {
					autoThread.setStop();
				}
				autoThread = new AutoThread(this, this.heightSens, this.angleSens);
				autoThread.start();
				break;
			case MANUAL:
				if (autoThread != null) {
					autoThread.setStop();
					autoThread = null;
				}
				break;
			default:
				throw new RuntimeException("The mode enumeration is out of bound!");
			}
		}

		super.setMode(mode);
	}

	@Override
	public void setPort(String port) throws Exception {
		// close current port
		closePorts();
		super.setPort(port);
		initialize();
	}

	private void closePorts() {
		if (port != null) {
			try {
				port.removeEventListener();
				port.closePort();
			} catch (SerialPortException e) {
				log.error(e);
			}
		}
	}

	private void initialize() throws Exception {
		// check if it's already initialized
		if (this.getStatus() != Status.NOT_CONFIGURED) {
			throw new RuntimeException("Status cannot be != NOT_CONFIGURED");
		}

		// // trying to initialize the port
		port.openPort();
		port.setParams(SystemConstant.BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		port.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
		port.addEventListener(this, SerialPort.MASK_RXCHAR);

		super.setStatus(Status.READY);
	}

	private void writeData(int h, int theta) {
		HardwareRequest hr = new HardwareRequest(HardwareRequest.MOVE_REQUEST, h, theta);
		log.info("SENT COMMAND:" + hr);
		try {
			port.writeString(hr.getRequest());
		} catch (SerialPortException e) {
			log.error("Cannot write to serial port", e);
		}
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		if (event.isRXCHAR() && event.getEventValue() > 0) {
			try {
				String receivedData = port.readString(event.getEventValue());
				// log.debug(receivedData);
				hw.updateBuffer(receivedData);
				if (hw.isReady()) {
					updateEngine(hw.getResponse());
				}
			} catch (SerialPortException ex) {
				System.out.println("Error in receiving string from SerialPort: " + ex);
			} catch (Exception e) {
				log.info("Cannot parse data sent by hardware", e);
			}
		}
	}

	private void updateEngine(String response) {
		/*
		 * Parsing the response from hardware.
		 */
		HardwareResponse hr = null;
		try {
			hr = HardwareResponse.parseResponse(response);
		} catch (Exception e) {
			log.warn("Cannot parse data from buffer");
			return;
		}

		if (hr.status != HardwareResponse.RESPONSE_STATUS_OK) {
			// hardware status error.
			return;
		}

		log.debug("RESPONSE:" + hr);

		/*
		 * Updating the current status
		 */
		this.currentAngle = hr.angle;
		this.currentHeight = hr.h;

		// creating a new radar data
		RadarData rd = new RadarData(hr.h, hr.angle, hr.value);

		// adding the data to container
		rdc.pushData(rd);
	}

	@Override
	public void goLeft() {
		if (currentAngle <= SystemConstant.MIN_ANGLE) {
			currentAngle = SystemConstant.MIN_ANGLE;
		} else {
			currentAngle -= this.angleSens;
		}

		writeData(currentHeight, currentAngle);
	}

	@Override
	public void goRight() {
		if (currentAngle >= SystemConstant.MAX_ANGLE) {
			currentAngle = SystemConstant.MAX_ANGLE;
		} else {
			currentAngle += this.angleSens;
		}

		writeData(currentHeight, currentAngle);

	}

	@Override
	public void goUp() {
		if (currentHeight >= SystemConstant.MAX_H) {
			currentHeight = SystemConstant.MAX_H;
		} else {
			currentHeight += this.heightSens;
		}
		writeData(currentHeight, currentAngle);
	}

	@Override
	public void goDown() {
		if (currentHeight <= SystemConstant.MIN_H) {
			currentHeight = SystemConstant.MIN_H;
		} else {
			currentHeight -= this.heightSens;
		}
		writeData(currentHeight, currentAngle);
	}

	@Override
	public void read() {
		HardwareRequest hr = new HardwareRequest(HardwareRequest.READ_REQUEST, 0, 0);
		try {
			port.writeString(hr.getRequest());
		} catch (SerialPortException e) {
			log.error("Cannot write to serial port", e);
		}
	}

	@Override
	public void shutdown() {
		if (this.getStatus() == Status.READY) {
			closePorts();
		}

		// shutting down the RDC
		rdc.shutdown();

		this.setStatus(Status.NOT_CONFIGURED);
	}

	@Override
	public void setHeight(int height) {
		if (height < SystemConstant.MIN_H || height > SystemConstant.MAX_H) {
			throw new RuntimeException("Invalid height set!");
		}

		this.currentHeight = height;
		writeData(currentHeight, currentAngle);
	}

	@Override
	public void setAngle(int angle) {
		if (angle < SystemConstant.MIN_ANGLE || angle > SystemConstant.MAX_ANGLE) {
			throw new RuntimeException("Invalid angle set!");
		}

		this.currentAngle = angle;
		writeData(currentHeight, currentAngle);
	}

	@Override
	public void changeHeightSens(int sens) {
		if (sens > 0) {
			this.heightSens += this.H_STEP;
		} else if (sens < 0) {
			this.heightSens -= this.H_STEP;
		}

		if (this.heightSens > MAX_H_SENS) {
			this.heightSens = MAX_H_SENS;
		} else if (this.heightSens < MIN_H_SENS) {
			this.heightSens = MIN_H_SENS;
		}

	}

	@Override
	public void changeAngleSens(int sens) {
		if (sens > 0) {
			this.angleSens += this.THETA_STEP;
		} else if (sens < 0) {
			this.angleSens -= this.THETA_STEP;
		}

		if (this.angleSens < MIN_THETA_SENS) {
			this.angleSens = MIN_THETA_SENS;
		} else if (this.angleSens > MAX_THETA_SENS) {
			this.angleSens = MAX_THETA_SENS;
		}
	}

	@Override
	public void changeMemorySize(int size) {
		if (size < 0) {
			this.memorySize -= 1;
		} else if (size > 0) {
			this.memorySize += 1;
		}

		if (this.memorySize <= 0) {
			this.memorySize = 1;
		} else if (this.memorySize > MAX_MEMORY_SIZE) {
			this.memorySize = MAX_MEMORY_SIZE;
		}

		rdc.setMemory(this.memorySize);
	}

	@Override
	public void resetMemory() {
		rdc.resetMemory();

	}

}
