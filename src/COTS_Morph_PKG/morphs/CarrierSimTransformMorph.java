package COTS_Morph_PKG.morphs;

import COTS_Morph_PKG.managers.mapManagers.mapPairManager;
import COTS_Morph_PKG.managers.morphManagers.base.baseMorphManager;
import COTS_Morph_PKG.morphs.base.baseSimpleMorph;
import COTS_Morph_PKG.similarities.SpiralSimilarityWithTranslation;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

public class CarrierSimTransformMorph extends baseSimpleMorph {
	/**
	 * similarity that will act as transformation
	 */
	protected SpiralSimilarityWithTranslation transform;


	public CarrierSimTransformMorph(COTS_MorphWin _win, baseMorphManager _morphMgr, mapPairManager _mapMgr, String _morphTitle) {super(_win, _morphMgr, _mapMgr,  _morphTitle);}
	/**
	 * this will perform initialization of morph-specific data before initial morph calc is performed, from base class ctor
	 */	
	@Override
	public void _endCtorInit() {	
		transform = new SpiralSimilarityWithTranslation(morphTitle,mapA.basisVecs[0],mapA.basisVecs[2],mapA.basisVecs[1]);		
	}
	

	@Override
	public void initCalcMorph_Indiv(float tA, float tB) {
		if(null==transform) {return;}
		myVectorf dispBetweenMaps = new myVectorf();
		float[] angleAndScale = new float[2];
		mapB.findDifferenceToMe(mapA, dispBetweenMaps, angleAndScale);
		
		myPointf mapPtA = new myPointf(mapA.getCOV()),mapPtB = new myPointf(mapB.getCOV());

		transform.deriveSimilarityFromCntlPts(new myPointf[] {dispBetweenMaps, new myPointf(angleAndScale[0],angleAndScale[1],0.0f),mapPtA,mapPtB},mapFlags[mapUpdateNoResetIDX]);
	}
	
	@Override
	protected void updateMorphValsFromUI_Indiv(mapUpdFromUIData upd) {
	}

	/**
	 * this function will conduct calculations between the two keyframe maps, if such calcs are used, whenever either is modified.  this is morph dependent
	 * @param _calledFrom : string denoting who called this method.  For debugging
	 */
	@Override
	public void mapCalcsAfterCntlPointsSet_Indiv(String _calledFrom) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public final int calcMorph_Integer(float tA, int AVal, float tB, int BVal) { return (int) ((tA*AVal) + (tB*BVal));}
	@Override
	public float calcMorph_Float(float tA, float AVal, float tB, float BVal) {		return (tA*AVal) + (tB*BVal);}
	@Override
	public final double calcMorph_Double(float tA, double AVal, float tB, double BVal) {		return (tA*AVal) + (tB*BVal);}
	
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
		for(int i=0;i<Apts.length;++i) {		
			myPointf res = (null==transform) ? new myPointf( Apts[i]) : transform.transformPoint( Apts[i],tB);			
			destPts[i]= myPointf._add(res, myVectorf._mult(normDispTimeVec, tB));//calcMorph_Point(tA, Apts[i], tB, Bpts[i]);		
		}
	}

	@Override
	public float drawMorphRtSdMenuDescr_Indiv(float yOff, float sideBarYDisp) {	
		transform.drawRightSideBarMenuDescr(pa, yOff, sideBarYDisp);
		yOff += sideBarYDisp;
		pa.translate(0.0f,sideBarYDisp, 0.0f);	
		
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
	public void resetAllBranching() {
		transform.setAllBranchingZero();
		
	}

}//class CarrierSimTransformMorph
