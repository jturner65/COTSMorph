package COTS_Morph_PKG.morphs.base;

import COTS_Morph_PKG.managers.mapManagers.mapPairManager;
import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.similarities.CarrierSimilarity;
import COTS_Morph_PKG.similarities.base.baseSimilarity;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

/**
 * class that composes and applies a single morph algorithm for all 4 distinguishing features of map
 * @author john
 *
 */
public abstract class baseSimpleMorph extends baseMorph {
	/**
	 * similarity that will act as transformation
	 */
	protected baseSimilarity[] transforms;
	/**
	 * arrays of corresponding edges between key frames
	 */	
	protected myPointf[][] crnrPtAras;

	public baseSimpleMorph(COTS_MorphWin _win, mapPairManager _mapMgr, String _morphTitle) {
		super(_win, _mapMgr, _morphTitle);
	}

	/**
	 * this will perform initialization of morph-specific data before initial morph calc is performed, from base class ctor
	 */	
	@Override
	public void _endCtorInit() {			
		crnrPtAras = getDiagPtsAras();
		transforms = new baseSimilarity[crnrPtAras.length];
		if(crnrPtAras.length == 0) {return;}
		
		for(int i=0;i<transforms.length;++i) {
			//transforms[i] = new CarrierSimilarity(morphTitle+"_"+i, mapA.basisVecs[0],mapA.basisVecs[2],mapA.basisVecs[1]);
			transforms[i] = buildSimilarity(i);//, mapA.basisVecs[0],mapA.basisVecs[2],mapA.basisVecs[1]);
		}
		
	}	
	protected abstract myPointf[][] getDiagPtsAras();
	protected abstract baseSimilarity buildSimilarity(int idx);
	
	
	/**
	 * use currently set t value to calculate morph and apply to passed morph map
	 */
	@Override
	public final void calcMorphAndApplyToMap(baseMap _curMorphMap, float tA, float tB) {
		myPointf[] aCntlPts = mapA.getCntlPts(), bCntlPts = mapB.getCntlPts(); 
		myPointf[] newPts = new myPointf[aCntlPts.length];
		
		//put morph results into newPts
		calcMorphBetweenTwoSetsOfCntlPoints(aCntlPts, bCntlPts, newPts, tA, tB);
		_curMorphMap.setCntlPts(newPts, mapFlags[mapUpdateNoResetIDX]);
//		for(int i=0;i<aCntlPts.length;++i) {	
//			delPts[i] = myPointf._sub(newPts[i], morphCntlPts[i]);	//performing this to make sure we have COV properly calculated
//		}
//		_curMorphMap.updateCntlPts(delPts);
		
//		//rebuild pts array to include other points
//		if(retPts) {			newPts = _curMorphMap.getAllMorphCntlPts();		}
//		newPts[newPts.length-2] = _curMorphMap.getCOV();
//		newPts[newPts.length-1] = _curMorphMap.getCenterPoint();	//F point for COTS, otherwise re-maps COV	
//		return newPts;
	}//calcMorphOfDeltaCntlPts	

	/**
	 * use this function to build morphed points for single-transform morphs.  2x transform morphs should not use this
	 * 
	 * @param Apts
	 * @param Bpts
	 * @param destPts
	 * @param useLerp
	 * @param tA
	 * @param tB
	 */
	protected final void _calcMorphWithSingleSim(myPointf[] Apts, myPointf[] Bpts, myPointf[] destPts, boolean useLerp, float tA, float tB) {
		if(useLerp) {
			for(int i=0;i<Apts.length;++i) {		
//				myPointf res = new myPointf( Apts[i]);			
//				destPts[i]= myPointf._add(res, myVectorf._mult(normDispTimeVec, tB));//calcMorph_Point(tA, Apts[i], tB, Bpts[i]);		
				destPts[i]=  myPointf._add(myPointf._mult(Apts[i], tA), myPointf._mult(Bpts[i], tB));}//calcMorph_Point(tA, Apts[i], tB, Bpts[i]);				}
			
		} else {
			for(int i=0;i<Apts.length;++i) {		
				myPointf res = transforms[0].transformPoint( Apts[i],tB);			
				destPts[i]= myPointf._add(res, myVectorf._mult(normDispTimeVec, tB));//calcMorph_Point(tA, Apts[i], tB, Bpts[i]);		
			}
		}	
	}
	
	@Override
	public float drawMorphRtSdMenuDescr_Indiv(float yOff, float sideBarYDisp) {
		for(int i=0;i<transforms.length;++i) {
			transforms[i].drawRightSideBarMenuDescr(pa, yOff, sideBarYDisp);
			yOff += sideBarYDisp;
			pa.translate(0.0f,sideBarYDisp, 0.0f);	
		}		
		return yOff;
	}
	@Override
	public void resetAllBranching_Indiv() {
		for(int i=0;i<transforms.length;++i) {transforms[i].setAllBranchingZero();}
		mapCalcsAfterCntlPointsSet("self::resetAllBranching_Indiv", true, false);
	}


}
