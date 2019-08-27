package COTS_Morph_PKG.morphs;

import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.morphs.base.baseMorph;

/**
 * this will use a carrier similiarity to propagate the morph from frame a to frame b
 * @author john
 *
 */
public class CarrierSimMorph extends baseMorph {
	
	public CarrierSimMorph(baseMap _a, baseMap _b, baseMap _morphMap) {
		super(_a,_b,_morphMap);
	}
	
	@Override
	protected final int calcMorph_Integer(float tA, int AVal, float tB, int BVal) { return (int) ((tA*AVal) + (tB*BVal));}
	@Override
	protected float calcMorph_Float(float tA, float AVal, float tB, float BVal) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected double calcMorph_Double(float tA, double AVal, float tB, double BVal) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void calcMorph_Indiv(float tA, float tB) {
		// TODO Auto-generated method stub
		
	}

	
	
}//class CarrierSimMorph
