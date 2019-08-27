package COTS_Morph_PKG.morphs;

import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.morphs.base.baseMorph;

public class LogPolarMorph extends baseMorph {

	public LogPolarMorph(baseMap _a, baseMap _b, baseMap _morphMap) {
		super(_a,_b,_morphMap);
	}
	@Override
	protected final int calcMorph_Integer(float tA, int AVal, float tB, int BVal) { return (int) ((tA*AVal) + (tB*BVal));};
	@Override
	protected float calcMorph_Float(float tA, float AVal, float tB, float BVal) {		return (tA*AVal) + (tB*BVal);}
	@Override
	protected double calcMorph_Double(float tA, double AVal, float tB, double BVal) {		return (tA*AVal) + (tB*BVal);}

	/**
	 * use currently set t value to calculate morph
	 */
	protected final void calcMorph_Indiv(float tA, float tB) {
		
		
	}
	
}//class drawnLineMorph
