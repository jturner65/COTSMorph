package COTS_Morph_PKG.morphs.carrier;

import COTS_Morph_PKG.managers.mapManagers.mapPairManager;
import COTS_Morph_PKG.morphs.base.baseSimpleMorph;
import COTS_Morph_PKG.similarities.Reg_SpiralSimWithTranslation;
import COTS_Morph_PKG.similarities.base.baseSimilarity;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

public class CarrierSimTransformMorph extends baseSimpleMorph {

	public CarrierSimTransformMorph(COTS_MorphWin _win, mapPairManager _mapMgr, String _morphTitle) {super(_win, _mapMgr,  _morphTitle);}
	@Override
	protected final baseSimilarity buildSimilarity(int i) {
		return new Reg_SpiralSimWithTranslation(morphTitle+"_"+i,mapA.basisVecs[0],mapA.basisVecs[2],mapA.basisVecs[1]);		
	};

	@Override
	protected void updateMorphValsFromUI_Indiv(mapUpdFromUIData upd) {
	}
	
	@Override
	protected final myPointf[][] getDiagPtsAras(){
		myPointf[][] res = new myPointf[1][];
		myPointf[] mapADiag = mapA.getCntlPtDiagonal(),mapBDiag = mapB.getCntlPtDiagonal(); 
		res[0] = new myPointf[] {mapADiag[0],mapADiag[1],mapBDiag[0],mapBDiag[1]};
		return res;
	}

	/**
	 * this function will conduct calculations between the two keyframe maps, if such calcs are used, whenever either is modified.  this is morph dependent
	 * @param _calledFrom : string denoting who called this method.  For debugging
	 */
	@Override
	public void mapCalcsAfterCntlPointsSet_Indiv(String _calledFrom) {
		if(null==transforms) {return;}
		myVectorf dispBetweenMaps = new myVectorf();
		float[] angleAndScale = new float[2];
		mapB.findDifferenceToMe(mapA, dispBetweenMaps, angleAndScale);
		
		myPointf mapPtA = new myPointf(mapA.getCOV()),mapPtB = new myPointf(mapB.getCOV());

		transforms[0].deriveSimilarityFromCntlPts(new myPointf[] {dispBetweenMaps, new myPointf(angleAndScale[0],angleAndScale[1],0.0f),mapPtA,mapPtB},mapFlags[mapUpdateNoResetIDX]);
	}//mapCalcsAfterCntlPointsSet_Indiv


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
		_calcMorphWithSingleSim(Apts,Bpts,destPts,(null==transforms), tA, tB);
//		if(null==transform) {
//			for(int i=0;i<Apts.length;++i) {		
//				myPointf res = new myPointf( Apts[i]);			
//				destPts[i]= myPointf._add(res, myVectorf._mult(normDispTimeVec, tB));//calcMorph_Point(tA, Apts[i], tB, Bpts[i]);		
//			}
//			
//		} else {
//			for(int i=0;i<Apts.length;++i) {		
//				myPointf res = transform.transformPoint( Apts[i],tB);			
//				destPts[i]= myPointf._add(res, myVectorf._mult(normDispTimeVec, tB));//calcMorph_Point(tA, Apts[i], tB, Bpts[i]);		
//			}
//		}
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

}//class CarrierSimTransformMorph
