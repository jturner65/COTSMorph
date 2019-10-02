package COTS_Morph_PKG.morph.singleTransform;

import COTS_Morph_PKG.map.base.baseMap;
import COTS_Morph_PKG.mapManager.mapPairManager;
import COTS_Morph_PKG.morph.singleTransform.base.baseSingleTransformMorph;
import COTS_Morph_PKG.transformer.spiral.Reg_SpiralSimWithTranslation;
import COTS_Morph_PKG.transformer.spiral.base.baseSpiralTransformer;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_Utils_Objects.vectorObjs.myPointf;

public class AffineMorph extends baseSingleTransformMorph {

	public AffineMorph(COTS_MorphWin _win, mapPairManager _mapMgr, baseMap _mapA, baseMap _mapB, String _morphTitle) {super(_win, _mapMgr,_mapA, _mapB,  _morphTitle);}
	@Override
	protected final baseSpiralTransformer buildSimilarity() {
		return new Reg_SpiralSimWithTranslation(morphTitle,mapA.basisVecs[0],mapA.basisVecs[2],mapA.basisVecs[1]);		
	};

	@Override
	protected void updateMorphValsFromUI_Indiv(mapUpdFromUIData upd) {
	}

	/**
	 * this function will conduct calculations between the two keyframe maps, if such calcs are used, whenever either is modified.  this is morph dependent
	 * @param _calledFrom : string denoting who called this method.  For debugging
	 */
	@Override
	public void mapCalcsAfterCntlPointsSet_Indiv(String _calledFrom) {
		if(null==transform) {return;}
		mapRegDistCalc.setMapsAndCalc(mapA, mapB, false, false);
		
		myPointf mapPtA = new myPointf(mapA.getCOV()),mapPtB = new myPointf(mapB.getCOV());

		transform.deriveSimilarityFromCntlPts(new myPointf[] {mapRegDistCalc.getDispBetweenMaps(), new myPointf(mapRegDistCalc.getAngle(),mapRegDistCalc.getScale(),0.0f),mapPtA,mapPtB},mapFlags[mapUpdateNoResetIDX]);
	}//mapCalcsAfterCntlPointsSet_Indiv


	@Override
	public final int calcMorph_Integer(float tA, int AVal, float tB, int BVal) { return (int) ((tA*AVal) + (tB*BVal));}
	@Override
	public float calcMorph_Float(float tA, float AVal, float tB, float BVal) {		return (tA*AVal) + (tB*BVal);}
	@Override
	public final double calcMorph_Double(float tA, double AVal, float tB, double BVal) {		return (tA*AVal) + (tB*BVal);}
	
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

}//class CarrierSimTransformMorph
