package COTS_Morph_PKG.morphs;

import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.morphs.base.baseMorph;
import base_Utils_Objects.vectorObjs.myPointf;

public class LERPMorph extends baseMorph {
	public LERPMorph(baseMap _a, baseMap _b, baseMap _morphMap) {
		super(_a,_b,_morphMap);
	}
	
	@Override
	protected final int calcMorph_Integer(float tA, int AVal, float tB, int BVal) { return (int) ((tA*AVal) + (tB*BVal));}
	@Override
	protected float calcMorph_Float(float tA, float AVal, float tB, float BVal) {		return (tA*AVal) + (tB*BVal);}
	@Override
	protected double calcMorph_Double(float tA, double AVal, float tB, double BVal) {		return (tA*AVal) + (tB*BVal);}
	
	/**
	 * use currently set t value to calculate morph
	 */
	@Override
	protected void calcMorph_Indiv(float tA, float tB) {
		//lerp between control points of mapA and mapB
		myPointf[] aCntlPts = mapA.getScl_CntlPts(tA), bCntlPts = mapB.getScl_CntlPts(tB);
		myPointf[] delPts = new myPointf[aCntlPts.length];
		for(int i=0;i<delPts.length;++i) {	delPts[i]= myPointf._add(aCntlPts[i], bCntlPts[i]);}
		curMorphMap.setCntlPts(delPts);
			//curMorphMap.moveCntlPts(delPts);
	
		
	}

}//class LERPMorph
