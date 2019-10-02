package COTS_Morph_PKG.morphs.simple.base;

import COTS_Morph_PKG.mapManager.mapPairManager;
import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.morphs.base.baseMorph;
import COTS_Morph_PKG.similarities.base.baseTransformer;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapRegDist;
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
	protected baseTransformer transform;
	/**
	 * arrays of corresponding edges between key frames
	 */	
	protected myPointf[] crnrPtAra;
	


	public baseSimpleMorph(COTS_MorphWin _win, mapPairManager _mapMgr, String _morphTitle) {super(_win, _mapMgr, _morphTitle);}

	/**
	 * this will perform initialization of morph-specific data before initial morph calc is performed, from base class ctor
	 */	
	@Override
	public void _endCtorInit() {		
		crnrPtAra = getCornerPtAra();
		if(crnrPtAra.length == 0) {return;}
		transform = buildSimilarity();	
	}	
	/**
	 * get corner points in appropriate format to be used by this morph
	 * @return
	 */
	protected abstract myPointf[] getCornerPtAra();
	protected abstract baseTransformer buildSimilarity();
	
	
	/**
	 * use currently set t value to calculate morph and apply to passed morph map
	 */
	@Override
	public final void calcMorphAndApplyToMap(baseMap _curMorphMap, float tA, float tB) {
		myPointf[] aCntlPts = mapA.getCntlPts(), bCntlPts = mapB.getCntlPts(); 
		myPointf[] newPts = new myPointf[aCntlPts.length];
		
		//put morph results into newPts
		calcMorphBetweenTwoSetsOfCntlPoints(aCntlPts, bCntlPts, newPts, tA, tB);
		//set curmorphmap's control points to be those calculated
		_curMorphMap.setCntlPts(newPts, mapFlags[mapUpdateNoResetIDX]);
	}//calcMorphAndApplyToMap	

	/**
	 * use this function to build morphed points for single-transform morphs.  2 and more transform morphs should not use this
	 * just a common function, used by multiple morphs
	 * 
	 * @param Apts corner points from A map
	 * @param Bpts corner points from B map
	 * @param destPts morph points go here
	 * @param useLerp whether or not to use Lerp - usually only if not intialized, or if lerp morph
	 * @param tA 1 - morph time
	 * @param tB morph time
	 */
	@Override
	public final void calcMorphBetweenTwoSetsOfCntlPoints(myPointf[] Apts, myPointf[] Bpts, myPointf[] destPts, float tA, float tB) {
		if(null==transform) {
			for(int i=0;i<Apts.length;++i) {	destPts[i]=  myPointf._add(myPointf._mult(Apts[i], tA), myPointf._mult(Bpts[i], tB));}	//forces to lerp calc - only use if not yet properly initialized		
		} else {
			for(int i=0;i<Apts.length;++i) {		
				myPointf res = transform.transformPoint( Apts[i],tB);
				//add transformation
				destPts[i]= myPointf._add(res, myVectorf._mult(normDispTimeVec, tB));//calcMorph_Point(tA, Apts[i], tB, Bpts[i]);		
			}
		}	
	}
	
	@Override
	public final float drawMorphRtSdMenuDescr_Indiv(float yOff, float sideBarYDisp) {
		if(transform==null) {return yOff;}
		transform.drawRightSideBarMenuDescr(pa, yOff, sideBarYDisp);
		yOff += sideBarYDisp;
		pa.translate(0.0f,sideBarYDisp, 0.0f);	
		return yOff;
	}
	@Override
	public final void resetAllBranching_Indiv() {
		if(transform==null) {return;}
		transform.setAllBranchingZero();
		mapCalcsAfterCntlPointsSet("this::resetAllBranching_Indiv", true, false);
	}


}//class baseSimpleMorph
