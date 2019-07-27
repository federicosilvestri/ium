package unipi.jradar.proxy;

import unipi.jradar.core.RadarData;

/**
 * This interface notifies that a Radar Data is available.
 * 
 * @author federicosilvestri
 *
 */
public interface RadarDataListener {
	/**
	 * A Radar Data is available
	 * 
	 * @param radarData
	 *            radar data
	 */
	public void newDataAvailable(RadarData radarData);
}
