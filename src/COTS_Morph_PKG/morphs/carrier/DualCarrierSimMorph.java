package COTS_Morph_PKG.morphs.carrier;

import COTS_Morph_PKG.managers.mapManagers.mapPairManager;
import COTS_Morph_PKG.morphs.base.baseSimpleMorph;
import COTS_Morph_PKG.similarities.CarrierSimilarity;
import COTS_Morph_PKG.similarities.base.baseSimilarity;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

public class DualCarrierSimMorph extends baseSimpleMorph {

	public DualCarrierSimMorph(COTS_MorphWin _win, mapPairManager _mapMgr, String _morphTitle) {super(_win, _mapMgr, _morphTitle);}

	@Override
	protected final baseSimilarity buildSimilarity(int i) {
		return new CarrierSimilarity(morphTitle+"_"+i,mapA.basisVecs[0],mapA.basisVecs[2],mapA.basisVecs[1]);		
	};
		
	@Override
	protected final myPointf[][] getDiagPtsAras(){
		myPointf[][] res = new myPointf[2][];
		myPointf[] mapADiag = mapA.getCntlPtDiagonal(),mapBDiag = mapB.getCntlPtDiagonal(); 
		res[0] = new myPointf[] {mapADiag[0],mapADiag[1],mapBDiag[0],mapBDiag[1]};
		myPointf[] mapAOffDiag =  mapA.getCntlPtOffDiagonal(), mapBOffDiag =  mapB.getCntlPtOffDiagonal();
		res[1] = new myPointf[] {mapAOffDiag[0],mapAOffDiag[1],mapBOffDiag[0],mapBOffDiag[1]};
		return res;
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
		if(null==transforms) {return;}
		for(int i=0;i<transforms.length;++i) {
			transforms[i].deriveSimilarityFromCntlPts(crnrPtAras[i], mapFlags[mapUpdateNoResetIDX]);
		}					
	}


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
		if(null==transforms) {
			for(int i=0;i<Apts.length;++i) {	destPts[i]=myPointf._add(new myPointf(Apts[i]),myVectorf._mult(normDispTimeVec, tB));}
		} else {
			int carrierIdx = 0;
			for(int i=0;i<Apts.length;++i) {	
				carrierIdx = i%2;
				destPts[i]=  myPointf._add(myPointf._mult(transforms[carrierIdx].transformPoint(Apts[i], tB), tA),myPointf._mult(transforms[carrierIdx].transformPoint(Bpts[i], tA), tB));
				//destPts[i]= myPointf._add(res, myVectorf._mult(normDispTimeVec, tB));//calcMorph_Point(tA, Apts[i], tB, Bpts[i]);	
	//			destPts[i]= (null==carrier) ? 
	//					myPointf._add(new myPointf(Apts[i]),myVectorf._mult(normDispTimeVec, tB)) : 
	//					myPointf._add(myPointf._mult(carrier.transformPoint(Apts[i],tB), tA),myPointf._mult(carrier.transformPoint(Bpts[i],tA), tB));
			}
		}
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


}//class DualCarrierSimMorph
