package COTS_Morph_PKG.morphs;

import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.morphs.base.baseMorph;
import base_Utils_Objects.vectorObjs.myPointf;

public class LERPMorph extends baseMorph {
	public LERPMorph(baseMap _a, baseMap _b, baseMap _morphMap) {
		super(_a,_b,_morphMap);
	}
	
	@Override
	protected final int calcMorph_Int(float tA, int AVal, float tB, int BVal) { return (int) ((tA*AVal) + (tB*BVal));};

	
	/**
	 * use currently set t value to calculate morph
	 */
	@Override
	protected void calcMorph_Indiv(float tA, float tB) {
		//lerp between control points of mapA and mapB
		myPointf[] aCntlPts = mapA.getScl_CntlPts(tA), bCntlPts = mapB.getScl_CntlPts(tB);
		myPointf[] delPts = new myPointf[aCntlPts.length];
		boolean notTest = false;
		if(notTest) {
			for(int i=0;i<delPts.length;++i) {	delPts[i]= myPointf._add(aCntlPts[i], bCntlPts[i]);}
			curMorphMap.setCntlPts(delPts);
			//curMorphMap.moveCntlPts(delPts);
		} else {
			myPointf[] morphCntlPts = curMorphMap.getCntlPts();
			for(int i=0;i<delPts.length;++i) {	delPts[i]= myPointf._sub( myPointf._add(aCntlPts[i], bCntlPts[i]),morphCntlPts[i]);}
			//System.out.println(""+curMorphMap.mapIdx + "|"+ at + "|" + bt);
			curMorphMap.addCntlPts(delPts);
		}
		
		
	}

}//class LERPMorph
