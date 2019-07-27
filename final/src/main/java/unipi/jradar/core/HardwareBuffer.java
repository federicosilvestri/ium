package unipi.jradar.core;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class represents the hardware buffer that filters and stores temporary
 * the data received by Arduino.
 * 
 * @author federicosilvestri
 *
 */
class HardwareBuffer {

	/**
	 * The queue of available data.
	 */
	private LinkedBlockingQueue<String> finishedQueue;

	/**
	 * Current buffer
	 */
	private String buffer;

	/**
	 * Create a new Hardware Buffer.
	 */
	HardwareBuffer() {
		finishedQueue = new LinkedBlockingQueue<>();
		buffer = null;
	}

	/**
	 * It checks if string is syntactically correct.
	 * 
	 * @param command
	 *            the command
	 * @return true if it's correct, false if not
	 */
	public static boolean checkString(String command) {
		if (!command.contains(HardwareResponse.RESPONSE_ENDING_CHAR)
				&& !command.contains(HardwareResponse.RESPONSE_START_CHAR)) {
			return true;
		}

		int balance = 0;
		for (int i = 0; i < command.length(); i++) {
			char currentChar = command.charAt(i);

			if (("" + currentChar).equals(HardwareResponse.RESPONSE_START_CHAR)) {
				balance += 1;
			} else if (("" + currentChar).equals(HardwareResponse.RESPONSE_ENDING_CHAR)) {
				balance -= 1;
			}

			if (Math.abs(balance) > 1) {
				return false;
			}
		}

		if (balance == 0) {
			return (command.charAt(command.length() - 1) + "").equals(HardwareResponse.RESPONSE_ENDING_CHAR);
		}

		return balance == 1;
	}

	private String cleanPiece(String piece) {
		String clean = piece.replaceAll("[\\r\\n]+", "");

		return clean;
	}

	private void offerEntirePiece(String piece) {
		// we know that string starts with A and finishes with B.
		// We know that the string is syntactically corrected.
		do {
			int startIndex = piece.indexOf(HardwareResponse.RESPONSE_START_CHAR);
			int endIndex = piece.indexOf(HardwareResponse.RESPONSE_ENDING_CHAR);

			if (endIndex == -1) {
				// the command is incomplete
				updateBuffer(piece);
				return;
			}

			String command = piece.substring(startIndex, endIndex + 1);
			piece = piece.substring(endIndex + 1);
			finishedQueue.add(command);
		} while (piece.length() > 0);
	}

	/**
	 * Update the buffer with a new piece of data.
	 * 
	 * @param piece
	 *            piece of data
	 */
	public synchronized void updateBuffer(String piece) {
		if (piece == null || piece.length() == 0) {
			// do nothing.
		}

		piece = cleanPiece(piece);

		// check again the size of piece.
		if (piece.length() == 0) {
			return;
		}

		if (piece.startsWith(HardwareResponse.RESPONSE_START_CHAR)
				&& piece.contains(HardwareResponse.RESPONSE_ENDING_CHAR)) {
			// this is a complete command, put it in the queue
			if (!HardwareBuffer.checkString(piece)) {
				throw new RuntimeException("Incorrect piece of command, check string not passed: " + piece);
			}
			offerEntirePiece(piece);
		} else if (piece.startsWith(HardwareResponse.RESPONSE_START_CHAR)) {
			// create a new piece and put it in the buffer
			if (!HardwareBuffer.checkString(piece)) {
				throw new RuntimeException("Incorrect piece of command, check string not passed: " + piece);
			}
			buffer = new String(piece);
		} else if (piece.endsWith(HardwareResponse.RESPONSE_ENDING_CHAR)) {
			// concatenate it to current buffer and push it to the queue
			offerEntirePiece(buffer + piece);
			buffer = null;
		} else if (buffer == null) {
			throw new RuntimeException("Buffer is null, and we have not received the start character.");
		} else {
			// we have a piece in the middle
			buffer = buffer + piece;
		}
	}

	/**
	 * Get the status of data.
	 * 
	 * @return true if one or more data is available, false if not.
	 */
	public boolean isReady() {
		return !finishedQueue.isEmpty();
	}

	/**
	 * Get the buffered data.
	 * 
	 * @return String data
	 */
	public String getResponse() {
		if (!isReady()) {
			throw new RuntimeException("No response available");
		}

		try {
			return finishedQueue.take();
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted exception during taking the response from buffer!", e);
		}
	}
}
