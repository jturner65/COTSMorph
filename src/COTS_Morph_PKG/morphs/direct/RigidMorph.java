package COTS_Morph_PKG.morphs.direct;

import COTS_Morph_PKG.managers.mapManagers.mapPairManager;
import COTS_Morph_PKG.morphs.base.baseSimpleMorph;
import COTS_Morph_PKG.similarities.Reg_SpiralSimWithTranslation;
import COTS_Morph_PKG.similarities.base.baseSimilarity;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

/**
 * rigid morph is composed only of rotations and translations (always unit scaling)
 * @author john
 *
 */
public class RigidMorph extends baseSimpleMorph {

	public RigidMorph(COTS_MorphWin _win, mapPairManager _mapMgr, String _morphTitle) {super(_win, _mapMgr, _morphTitle);}
	
	@Override
	protected final baseSimilarity buildSimilarity(int i) {
		return new Reg_SpiralSimWithTranslation(morphTitle+"_"+i,mapA.basisVecs[0],mapA.basisVecs[2],mapA.basisVecs[1]);		
	};
	
	@Override
	protected final myPointf[][] getDiagPtsAras(){
		myPointf[][] res = new myPointf[1][];
		myPointf[] mapADiag = mapA.getCntlPtDiagonal(),mapBDiag = mapB.getCntlPtDiagonal(); 
		res[0] = new myPointf[] {mapADiag[0],mapADiag[1],mapBDiag[0],mapBDiag[1]};
		return res;
	}

	@Override
	protected void updateMorphValsFromUI_Indiv(mapUpdFromUIData upd) {
	}
	@Override
	public void mapCalcsAfterCntlPointsSet_Indiv(String _calledFrom) {
		if(null==transforms) {return;}
		mapRegDistCalc.setMapsAndCalc(mapA, mapB, false, false);
//		
//		
//		myVectorf dispBetweenMaps = new myVectorf();
//		float[] angleAndScale = new float[2];
//		mapB.findDifferenceToMe(mapA, dispBetweenMaps, angleAndScale);
		
		myPointf mapPtA = new myPointf(mapA.getCOV()),mapPtB = new myPointf(mapB.getCOV());
		//scale is forced to 1.0 due to rigid morph
		transforms[0].deriveSimilarityFromCntlPts(new myPointf[] {mapRegDistCalc.getDispBetweenMaps(), new myPointf(mapRegDistCalc.getAngle(),1.0f,0.0f),mapPtA,mapPtB},mapFlags[mapUpdateNoResetIDX]);
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
	public void calcMorphBetweenTwoSetsOfCntlPoints(myPointf[] Apts, myPointf[] Bpts, myPointf[] destPts, float tA,	float tB) {
		_calcMorphWithSingleSim(Apts,Bpts,destPts,(null==transforms), tA, tB);
		//		if(null==transforms) {
//			for(int i=0;i<Apts.length;++i) {		
//				myPointf res = new myPointf( Apts[i]);			
//				destPts[i]= myPointf._add(res, myVectorf._mult(normDispTimeVec, tB));//calcMorph_Point(tA, Apts[i], tB, Bpts[i]);		
//			}
//			
//		} else {
//			for(int i=0;i<Apts.length;++i) {		
//				myPointf res = transforms[0].transformPoint( Apts[i],tB);			
//				destPts[i]= myPointf._add(res, myVectorf._mult(normDispTimeVec, tB));//calcMorph_Point(tA, Apts[i], tB, Bpts[i]);		
//			}
//		}	
	}

	@Override
	public void drawMorphSpecificValues(boolean debug, boolean drawCntlPts, boolean showLbls) {
		pa.pushMatrix();pa.pushStyle();	
		pa.fill(0,0,0,255);
		pa.stroke(0,0,0,255);
		pa.strokeWeight(1.0f);
		
		pa.popStyle();pa.popMatrix();	
	}

}// class RigidMorph
