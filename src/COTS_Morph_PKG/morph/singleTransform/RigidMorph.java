package COTS_Morph_PKG.morph.singleTransform;

import COTS_Morph_PKG.map.base.Base_PolyMap;
import COTS_Morph_PKG.mapManager.mapPairManager;
import COTS_Morph_PKG.morph.singleTransform.base.baseSingleTransformMorph;
import COTS_Morph_PKG.transformer.spiral.Reg_SpiralSimWithTranslation;
import COTS_Morph_PKG.transformer.spiral.base.baseSpiralTransformer;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_Math_Objects.vectorObjs.floats.myPointf;

/**
 * rigid morph is composed only of rotations and translations (always unit scaling)
 * @author john
 *
 */
public class RigidMorph extends baseSingleTransformMorph {

	public RigidMorph(COTS_MorphWin _win, mapPairManager _mapMgr, Base_PolyMap _mapA, Base_PolyMap _mapB,int _morphTypeIDX,  String _morphTitle) {super(_win, _mapMgr,_mapA, _mapB, _morphTypeIDX,_morphTitle);}
	public RigidMorph(RigidMorph _otr) {super(_otr);}
	@Override
	protected final baseSpiralTransformer buildSimilarity() {
		return new Reg_SpiralSimWithTranslation(morphTitle,mapA.basisVecs[0],mapA.basisVecs[2],mapA.basisVecs[1]);		
	};
	

	@Override
	protected void updateMorphValsFromUI_Indiv(mapUpdFromUIData upd) {
	}
	@Override
	public void mapCalcsAfterCntlPointsSet_Indiv(String _calledFrom) {
		if(null==transform) {return;}
		mapRegDistCalc.setMapsAndCalc(mapA, mapB, false, false);
		
		myPointf mapPtA = new myPointf(mapA.getCOV()),mapPtB = new myPointf(mapB.getCOV());
		//scale is forced to 1.0 due to rigid morph
		transform.deriveSimilarityFromCntlPts(new myPointf[] {mapRegDistCalc.getDispBetweenMaps(), new myPointf(mapRegDistCalc.getAngle(),1.0f,0.0f),mapPtA,mapPtB},mapFlags[mapUpdateNoResetIDX]);
	}//mapCalcsAfterCntlPointsSet_Indiv

	@Override
	public final int calcMorph_Integer(float tA, int AVal, float tB, int BVal) { return (int) ((tA*AVal) + (tB*BVal));}
	@Override
	public float calcMorph_Float(float tA, float AVal, float tB, float BVal) {		return (tA*AVal) + (tB*BVal);}
	@Override
	public final double calcMorph_Double(float tA, double AVal, float tB, double BVal) {		return (tA*AVal) + (tB*BVal);}
	

	@Override
	public void drawMorphSpecificValues(boolean debug, boolean drawCntlPts, boolean showLbls) {
		pa.pushMatState();	
		pa.setFill(0,0,0,255);
		pa.setStroke(0,0,0,255);
		pa.setStrokeWt(1.0f);
		
		pa.popMatState();	
	}

}// class RigidMorph
