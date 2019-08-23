package COTS_Morph_PKG.morphs;

import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.morphs.base.baseMorph;

public class CarrierSimMorph extends baseMorph {

	public CarrierSimMorph(baseMap _a, baseMap _b, baseMap _morphMap) {
		super(_a,_b,_morphMap);
	}
	
	@Override
	protected final int calcMorph_Int(float tA, int AVal, float tB, int BVal) { return (int) ((tA*AVal) + (tB*BVal));};

	@Override
	protected void calcMorph_Indiv(float tA, float tB) {
		// TODO Auto-generated method stub
		
	}

	
}//class CarrierSimMorph
