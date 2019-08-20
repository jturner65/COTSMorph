package COTS_Morph_PKG.morphs.base;

import COTS_Morph_PKG.maps.base.baseMap;

/**
 * class holding common functionality to morph between two image maps
 * @author john
 *
 */
public abstract class baseMorph {
	/**
	 * maps this morph is working on
	 */
	protected baseMap mapA, mapB;
	
	/**
	 * current time in
	 */
	protected float t;

	public baseMorph() {
		t=.5f;
	}
	
	public final void setMaps(baseMap _a, baseMap _b) {
		mapA = _a;
		mapB = _b;		
		calcMorph();
	}
	
	public final void setMorphT(float _t) {
		t=_t;
		calcMorph();
	}
	
	/**
	 * use currently set t value to calculate morph
	 */
	protected abstract void calcMorph();
	
	

}//class baseMorph
