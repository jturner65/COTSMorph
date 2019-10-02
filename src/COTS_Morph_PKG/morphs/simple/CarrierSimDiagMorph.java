package COTS_Morph_PKG.morphs.simple;

import COTS_Morph_PKG.mapManager.mapPairManager;
import COTS_Morph_PKG.morphs.simple.base.baseSimpleMorph;
import COTS_Morph_PKG.similarities.CarrierTransformer;
import COTS_Morph_PKG.similarities.base.baseTransformer;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_Utils_Objects.vectorObjs.myPointf;

/**
 * this will use a carrier similiarity to propagate the morph from frame a to frame b
 * @author john
 *
 */
public class CarrierSimDiagMorph extends baseSimpleMorph {

	
	public CarrierSimDiagMorph(COTS_MorphWin _win, mapPairManager _mapMgr, String _morphTitle) {super(_win, _mapMgr,  _morphTitle);}

	@Override
	protected final baseTransformer buildSimilarity() {
		return new CarrierTransformer(morphTitle,mapA.basisVecs[0],mapA.basisVecs[2],mapA.basisVecs[1]);		
	};
	
	@Override
	protected void updateMorphValsFromUI_Indiv(mapUpdFromUIData upd) {}

	@Override
	protected final myPointf[] getCornerPtAra(){
		myPointf[] mapADiag = mapA.getCntlPtDiagonal(),mapBDiag = mapB.getCntlPtDiagonal(); 
		return new myPointf[] {mapADiag[0],mapADiag[1],mapBDiag[0],mapBDiag[1]};
	}


	/**
	 * this function will conduct calculations between the two keyframe maps, if such calcs are used, whenever either is modified.  this is morph dependent
	 * @param _calledFrom : string denoting who called this method.  For debugging
	 */
	@Override
	public void mapCalcsAfterCntlPointsSet_Indiv(String _calledFrom) {			
		if(null==transform) {return;};
		//myPointf[] mapADiag = mapA.getCntlPtDiagonal(),mapBDiag = mapB.getCntlPtDiagonal();		
		//carrier.deriveSimilarityFromCntlPts(new myPointf[] {mapADiag[0],mapADiag[1],mapBDiag[0],mapBDiag[1]}, true);		
		transform.deriveSimilarityFromCntlPts(crnrPtAra, mapFlags[mapUpdateNoResetIDX]);		
	}

	@Override
	public int calcMorph_Integer(float tA, int AVal, float tB, int BVal) { return (int) ((tA*AVal) + (tB*BVal));}
	@Override
	public float calcMorph_Float(float tA, float AVal, float tB, float BVal) {		return (tA*AVal) + (tB*BVal);}
	@Override
	public double calcMorph_Double(float tA, double AVal, float tB, double BVal) {		return (tA*AVal) + (tB*BVal);}
	
//	/**
//	 * calcluate this morph algorithm between Apts and Bpts, putting result in destPts
//	 * @param Apts
//	 * @param Bpts
//	 * @param destPts
//	 * @param tA
//	 * @param tB
//	 */
//	@Override
//	public void calcMorphBetweenTwoSetsOfCntlPoints(myPointf[] Apts, myPointf[] Bpts, myPointf[] destPts, float tA, float tB) {
//		_calcMorphWithSingleSim(Apts,Bpts,destPts,(null==transform), tA, tB);
//
//	}

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


}//class CarrierSimMorph
