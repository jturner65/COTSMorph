package COTS_Morph_PKG.morphs;

import COTS_Morph_PKG.managers.mapManagers.mapPairManager;
import COTS_Morph_PKG.managers.morphManagers.base.baseMorphManager;
import COTS_Morph_PKG.morphs.base.baseSimpleMorph;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_Utils_Objects.vectorObjs.myPointf;

public class LERPMorph extends baseSimpleMorph {
	public LERPMorph(COTS_MorphWin _win, baseMorphManager _morphMgr, mapPairManager _mapMgr, String _morphTitle) {super(_win, _morphMgr, _mapMgr, _morphTitle);}
	
	
	/**
	 * any morph code that needs to be executed before any morph/inteprolation occurs
	 */
	@Override
	public void initCalcMorph_Indiv(float tA, float tB) {	}
	
	@Override
	protected void updateMorphValsFromUI_Indiv(mapUpdFromUIData upd) {}

	/**
	 * this will perform initialization of morph-specific data before initial morph calc is performed, from base class ctor
	 */	
	@Override
	public void _endCtorInit() {	}
	
	@Override
	public final int calcMorph_Integer(float tA, int AVal, float tB, int BVal) { return (int) ((tA*AVal) + (tB*BVal));}
	@Override
	public float calcMorph_Float(float tA, float AVal, float tB, float BVal) {		return (tA*AVal) + (tB*BVal);}
	@Override
	public double calcMorph_Double(float tA, double AVal, float tB, double BVal) {		return (tA*AVal) + (tB*BVal);}
	/**
	 * calcluate this morph algorithm between Apts and Bpts, putting result in destPts
	 * @param Apts
	 * @param Bpts
	 * @param destPts
	 * @param tA
	 * @param tB
	 */
	@Override
	public final void calcMorphBetweenTwoSetsOfCntlPoints(myPointf[] Apts, myPointf[] Bpts, myPointf[] destPts, float tA, float tB) {
		for(int i=0;i<Apts.length;++i) {				destPts[i]=  myPointf._add(myPointf._mult(Apts[i], tA), myPointf._mult(Bpts[i], tB));}//calcMorph_Point(tA, Apts[i], tB, Bpts[i]);	}
	}
	
	/**
	 * this function will conduct calculations between the two keyframe maps, if such calcs are used, whenever either is modified.  this is morph dependent
	 * @param _calledFrom : string denoting who called this method.  For debugging
	 */
	@Override
	public void mapCalcsAfterCntlPointsSet_Indiv(String _calledFrom) {	}

	@Override
	public float drawMorphRtSdMenuDescr_Indiv(float yOff, float sideBarYDisp) {
		return yOff;
	}
	/**
	 * this will draw instancing morph-specific data on screen 
	 */
	@Override
	public void drawMorphSpecificValues(boolean debug, boolean _isFill, boolean _drawCircles) {
		pa.pushMatrix();pa.pushStyle();	
		pa.fill(0,0,0,255);
		pa.stroke(0,0,0,255);
		pa.strokeWeight(1.0f);
		
		pa.popStyle();pa.popMatrix();	
	}
	@Override
	public void resetAllBranching() {	}

}//class LERPMorph
