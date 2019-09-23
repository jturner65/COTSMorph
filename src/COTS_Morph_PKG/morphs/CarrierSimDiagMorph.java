package COTS_Morph_PKG.morphs;

import COTS_Morph_PKG.managers.mapManagers.mapPairManager;
import COTS_Morph_PKG.managers.morphManagers.base.baseMorphManager;
import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.morphs.base.baseMorph;
import COTS_Morph_PKG.similarities.CarrierSimilarity;
import COTS_Morph_PKG.transform.SpiralTransform;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

/**
 * this will use a carrier similiarity to propagate the morph from frame a to frame b
 * @author john
 *
 */
public class CarrierSimDiagMorph extends baseMorph {
	/**
	 * similarity that will act as carrier
	 */
	protected CarrierSimilarity carrier;
	
	/**
	 * per side cntl point edges, in order, of each poly
	 */	
	protected myPointf[] edgePtAra;

	
	public CarrierSimDiagMorph(COTS_MorphWin _win, baseMorphManager _morphMgr, mapPairManager _mapMgr, String _morphTitle) {super(_win, _morphMgr, _mapMgr,  _morphTitle);}
	/**
	 * this will perform initialization of morph-specific data before initial morph calc is performed, from base class ctor
	 */	
	@Override
	protected final void _endCtorInit() {	
		myPointf[] mapADiag = mapA.getCntlPtDiagonal();
		myPointf[] mapBDiag = mapB.getCntlPtDiagonal();	
		edgePtAra = new myPointf[] {mapADiag[0],mapADiag[1],mapBDiag[0],mapBDiag[1]};				
		carrier = new CarrierSimilarity(morphTitle, mapA.basisVecs[0],mapA.basisVecs[2],mapA.basisVecs[1]);
	}
	
	
	/**
	 * any morph code that needs to be executed before any morph/inteprolation occurs - update the similarity control points
	 */
	@Override
	protected final void initCalcMorph_Indiv(float tA, float tB) {
		if(null==carrier) {return;}
		//myPointf[] mapADiag = mapA.getCntlPtDiagonal(),mapBDiag = mapB.getCntlPtDiagonal();		
		//carrier.deriveSimilarityFromCntlPts(new myPointf[] {mapADiag[0],mapADiag[1],mapBDiag[0],mapBDiag[1]}, true);		
		carrier.deriveSimilarityFromCntlPts(edgePtAra, mapFlags[mapUpdateNoResetIDX]);		
	}
	/**
	 * this function will conduct calculations between the two keyframe maps, if such calcs are used, whenever either is modified.  this is morph dependent
	 * @param _calledFrom : string denoting who called this method.  For debugging
	 */
	@Override
	protected void mapCalcsAfterCntlPointsSet_Indiv(String _calledFrom) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected int calcMorph_Integer(float tA, int AVal, float tB, int BVal) { return (int) ((tA*AVal) + (tB*BVal));}
	@Override
	protected float calcMorph_Float(float tA, float AVal, float tB, float BVal) {		return (tA*AVal) + (tB*BVal);}
	@Override
	protected double calcMorph_Double(float tA, double AVal, float tB, double BVal) {		return (tA*AVal) + (tB*BVal);}
	
	/**
	 * calcluate this morph algorithm between Apts and Bpts, putting result in destPts
	 * @param Apts
	 * @param Bpts
	 * @param destPts
	 * @param tA
	 * @param tB
	 */
	@Override
	protected void calcMorphBetweenTwoSetsOfCntlPoints(myPointf[] Apts, myPointf[] Bpts, myPointf[] destPts, float tA, float tB) {
		for(int i=0;i<Apts.length;++i) {		
			myPointf res = (null==carrier) ? new myPointf(Apts[i]) : carrier.transformPoint(Apts[i],tB);	
			destPts[i]= myPointf._add(res, myVectorf._mult(normDispTimeVec, tB));//calcMorph_Point(tA, Apts[i], tB, Bpts[i]);	
		}
	}

//	@Override
//	protected myPointf calcMorph_Point(float tA, myPointf AVal, float tB, myPointf BVal) {
//		if(null==carrier) {return new myPointf(AVal);}
//		myPointf res = carrier.transformPoint(AVal,tB);
//		//return carrier.mapPoint(AVal,tA, tB);                                                                                                    
//		//return myPointf._add(myPointf._mult(carrier.transformPoint(AVal,tB), tA),myPointf._mult(carrier.transformPoint(BVal,tA), tB));		 
//		return myPointf._add(res, myVectorf._mult(normDispTimeVec, tB));
//	}

	@Override
	protected float drawMorphRtSdMenuDescr_Indiv(float yOff, float sideBarYDisp) {
		carrier.drawRightSideBarMenuDescr(pa, yOff, sideBarYDisp);
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
		carrier.setAllBranchingZero();
		
	}




}//class CarrierSimMorph
