package COTS_Morph_PKG.morphs;

import COTS_Morph_PKG.managers.mapManagers.mapPairManager;
import COTS_Morph_PKG.managers.morphManagers.base.baseMorphManager;
import COTS_Morph_PKG.morphs.base.baseSimpleMorph;
import COTS_Morph_PKG.similarities.CarrierSimilarity;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

public class DualCarrierSimMorph extends baseSimpleMorph {
	/**
	 * similarity that will act as carrier
	 */
	protected CarrierSimilarity[] carriers;
	
	/**
	 * per side cntl point edges, in order, of each poly
	 */	
	protected myPointf[][] diagPtAras;

	public DualCarrierSimMorph(COTS_MorphWin _win, baseMorphManager _morphMgr, mapPairManager _mapMgr, String _morphTitle) {super(_win, _morphMgr, _mapMgr, _morphTitle);}
	
	@Override
	public void initCalcMorph_Indiv(float tA, float tB) {
		if(null==carriers) {return;}
		for(int i=0;i<carriers.length;++i) {
			carriers[i].deriveSimilarityFromCntlPts(diagPtAras[1], mapFlags[mapUpdateNoResetIDX]);
		}			
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
		
	}

	/**
	 * this will perform initialization of morph-specific data before initial morph calc is performed, from base class ctor
	 */	
	@Override
	public final void _endCtorInit() {	
		myPointf[] mapADiag = mapA.getCntlPtDiagonal(), mapAOffDiag =  mapA.getCntlPtOffDiagonal();
		myPointf[] mapBDiag = mapB.getCntlPtDiagonal(), mapBOffDiag =  mapB.getCntlPtOffDiagonal();
		diagPtAras = new myPointf[2][];
		diagPtAras[0] = new myPointf[] {mapADiag[0],mapADiag[1],mapBDiag[0],mapBDiag[1]};
		diagPtAras[1] = new myPointf[] {mapAOffDiag[0],mapAOffDiag[1],mapBOffDiag[0],mapBOffDiag[1]};
		carriers = new CarrierSimilarity[2];
		for(int i=0;i<carriers.length;++i) {
			carriers[i] = new CarrierSimilarity(morphTitle+"_"+i, mapA.basisVecs[0],mapA.basisVecs[2],mapA.basisVecs[1]);
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
		for(int i=0;i<Apts.length;++i) {		
			destPts[i]= (null==carriers) ? 
					myPointf._add(new myPointf(Apts[i]),myVectorf._mult(normDispTimeVec, tB)) : 
					myPointf._add(myPointf._mult(carriers[i%2].transformPoint(Apts[i], tB), tA),myPointf._mult(carriers[i%2].transformPoint(Bpts[i], tA), tB));
			//destPts[i]= myPointf._add(res, myVectorf._mult(normDispTimeVec, tB));//calcMorph_Point(tA, Apts[i], tB, Bpts[i]);	
//			destPts[i]= (null==carrier) ? 
//					myPointf._add(new myPointf(Apts[i]),myVectorf._mult(normDispTimeVec, tB)) : 
//					myPointf._add(myPointf._mult(carrier.transformPoint(Apts[i],tB), tA),myPointf._mult(carrier.transformPoint(Bpts[i],tA), tB));
		}
	}

	@Override
	public float drawMorphRtSdMenuDescr_Indiv(float yOff, float sideBarYDisp) {
		for(int i=0;i<carriers.length;++i) {
			yOff+= carriers[i].drawRightSideBarMenuDescr(pa, yOff, sideBarYDisp);
		}
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
		for(int i=0;i<carriers.length;++i) {
			carriers[i].setAllBranchingZero();
		}
		
	}



}//class DualCarrierSimMorph
