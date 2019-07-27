package unipi.jradar;

import org.apache.log4j.Logger;

import unipi.jradar.core.Engine;
import unipi.jradar.core.EngineImpl;
import unipi.jradar.proxy.SystemProxy;
import unipi.jradar.ui.MainWindow;
import unipi.jradar.util.MediaManager;

/**
 * This is the main class of the application. It manages all events related to
 * bootstrap and shutdown.
 *
 */
public final class App {
	/**
	 * Logger of class.
	 */
	private static final Logger log = Logger.getLogger(App.class);

	/**
	 * Singleton instance of application.
	 */
	private static App instance;

	/**
	 * Get the instance of application.
	 * 
	 * @return App instance
	 */
	public static App getInstance() {
		if (instance == null) {
			instance = new App();
		}

		return instance;
	}

	public static void main(String[] args) {
		App.getInstance();
	}

	/**
	 * The MainWindow object.
	 */
	private MainWindow mainWindow;

	/**
	 * The backend engine.
	 */
	private Engine engine;

	/**
	 * Instance the App class.
	 */
	public App() {
		log.info("Starting JRadar Application");
		log.info("Executing tests");
		executeTests();
		log.info("Tests are OK!");
		startBackend();
		startGUI();
		log.info("Started JRadar Application");
	}

	private void executeTests() {
		try {
			MediaManager.getInstance().test();
		} catch (Exception e) {
			throw new RuntimeException("Application cannot be started because main tests are failed", e);
		}

	}

	private void startBackend() {
		log.info("Starting backend");
		engine = new EngineImpl();
		SystemProxy.create(engine);
		log.info("Backend started");
	}

	public void startGUI() {
		log.info("Starting GUI");
		// setting global parameter
		System.setProperty("sun.awt.noerasebackground", "true");
		mainWindow = new MainWindow();
		log.info("GUI started");
	}

	public void shutdown() {
		log.info("Received a shutdown function invocation");
		if (mainWindow.isActive()) {
			mainWindow.dispose();
			mainWindow = null;
		}
		log.info("GUI is now destroyed");

		log.info("Stopping engine");
		engine.shutdown();
		log.info("Engine is now stopped");

		log.info("Application is terminated");
	}

}
