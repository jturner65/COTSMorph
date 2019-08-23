package COTS_Morph_PKG.maps.base;

import base_Utils_Objects.vectorObjs.myPointf;

/**
 * this class is a struct that describes a COTS configuration
 * @author john
 *
 */
public class COTSData {
	protected float t;
	/**
	 * Spiral scaling factors
	 */
	protected float mu = 1.0f, mv = 1.0f;
	/**
	 * spiral angles
	 */
	protected float au = 0.0f, av = 0.0f;
	protected float old_au = au, old_av = av;
	/**
	 * spiral center
	 */
	protected myPointf F;
	
	public COTSData() {}
		
	public final float get_mu() {return mu;}
	public final float get_mv() {return mv;}

	public final float get_au() {return au;}
	public final float get_av() {return av;}
	public final myPointf getF() {return F;}
	
}//class COTSData