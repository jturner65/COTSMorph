package COTS_Morph_PKG.morphs;

import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.morphs.base.baseMorph;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import base_Utils_Objects.vectorObjs.myPointf;

public class LERPMorph extends baseMorph {
	public LERPMorph(COTS_MorphWin _win, baseMap _a, baseMap _b, baseMap _morphMap, int _morphScope) {
		super(_win,_a,_b,_morphMap, _morphScope, "LERP");
	}
	
	@Override
	protected final int calcMorph_Integer(float tA, int AVal, float tB, int BVal) { return (int) ((tA*AVal) + (tB*BVal));}
	@Override
	protected float calcMorph_Float(float tA, float AVal, float tB, float BVal) {		return (tA*AVal) + (tB*BVal);}
	@Override
	protected double calcMorph_Double(float tA, double AVal, float tB, double BVal) {		return (tA*AVal) + (tB*BVal);}
	@Override
	protected myPointf calcMorph_Point(float tA, myPointf AVal, float tB, myPointf BVal) {
		return myPointf._add(myPointf._mult(AVal, tA), myPointf._mult(BVal, tB));
	}
	
	/**
	 * any morph code that needs to be executed before any morph/inteprolation occurs
	 */
	@Override
	protected void initCalcMorph_Indiv(float tA, float tB) {		
	}
	

	@Override
	protected float drawMorphRtSdMenuDescr_Indiv(float yOff, float sideBarYDisp) {
		// TODO Auto-generated method stub
		return yOff;
	}

}//class LERPMorph
