package unipi.jradar.util;

import java.io.File;
import java.net.URL;

import org.apache.log4j.Logger;

import unipi.jradar.App;

/**
 * This class represents the Media Manager application.
 * 
 * @author federicosilvestri
 *
 */
public final class MediaManager {

	/**
	 * Logger.
	 */
	private static final Logger log = Logger.getLogger(App.class);

	/**
	 * Singleton instance.
	 */
	private static MediaManager instance;

	/**
	 * Get the instance of class.
	 * 
	 * @return MediaManager instance
	 */
	public static MediaManager getInstance() {
		if (instance == null) {
			instance = new MediaManager();
		}

		return instance;
	}

	/**
	 * The path of media files.
	 */
	private static final String RESOURCES_PATH = "unipi/jradar/media/";

	/**
	 * Internal class loader.
	 */
	private final ClassLoader classLoader;

	private MediaManager() {
		classLoader = this.getClass().getClassLoader();
		log.debug("Media Manager is now instanced.");
	}

	/**
	 * 
	 * Warning: it does not work with Windows.
	 * 
	 * @param name
	 *            the name of the resource
	 * @return the URL of resource
	 */
	public URL locateResource(String name) {
		if (name == null) {
			throw new NullPointerException("You can't pass an invalid resource name!");
		}

		String resourceFullPath = RESOURCES_PATH + name;
		URL resourceURL = classLoader.getResource(resourceFullPath);
		final String regex = "^([^\\/])*/";

		while (resourceURL == null && resourceFullPath.contains("/")) {
			resourceFullPath = resourceFullPath.replaceFirst(regex, "");
			resourceURL = classLoader.getResource(resourceFullPath);
			log.warn("Resource " + name + " not found... trying with path=" + resourceFullPath);
		}

		if (resourceURL == null) {
			throw new RuntimeException("Your resource named \"" + name + "\" is not found in path!");
		}

		return resourceURL;
	}

	public File getResource(String name) {
		URL resourceURL = this.locateResource(name);
		return new File(resourceURL.getFile());
	}

	public void test() throws Exception {
		/*
		 * Trying to locate a test resource.
		 */
		if (getResource("test.txt") == null) {
			throw new Exception("Cannot get the test file!");
		}
	}

}
