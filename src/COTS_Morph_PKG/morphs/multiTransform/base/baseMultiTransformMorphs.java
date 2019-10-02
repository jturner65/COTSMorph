package COTS_Morph_PKG.morphs.multiTransform.base;

import COTS_Morph_PKG.mapManager.mapPairManager;
import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.morphs.base.baseMorph;
import COTS_Morph_PKG.similarities.base.baseTransformer;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

public abstract class baseMultiTransformMorphs extends baseMorph {
	/**
	 * similarities that will act as transformations
	 */
	protected baseTransformer[] transforms;
	/**
	 * arrays of corresponding edges between key frames
	 */	
	protected myPointf[][] crnrPtAras;


	public baseMultiTransformMorphs(COTS_MorphWin _win, mapPairManager _mapMgr, String _morphTitle) {	super(_win, _mapMgr, _morphTitle);}


	/**
	 * this will perform initialization of morph-specific data before initial morph calc is performed, from base class ctor
	 */	
	@Override
	public void _endCtorInit() {		
		crnrPtAras = getCornerPtAras();
		transforms = new baseTransformer[crnrPtAras.length];
		if(crnrPtAras.length == 0) {return;}
		
		for(int i=0;i<transforms.length;++i) {			transforms[i] = buildSimilarity(i);		}
		
	}	
	/**
	 * get corner points in appropriate format to be used by this morph
	 * @return
	 */
	protected abstract myPointf[][] getCornerPtAras();
	protected abstract baseTransformer buildSimilarity(int idx);
	
	
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
	protected final void _calcMorphWithSingleSim(myPointf[] Apts, myPointf[] Bpts, myPointf[] destPts, boolean useLerp, float tA, float tB) {
		if(useLerp) {
			for(int i=0;i<Apts.length;++i) {	destPts[i]=  myPointf._add(myPointf._mult(Apts[i], tA), myPointf._mult(Bpts[i], tB));}	//forces to lerp calc - only use if not yet properly initialized		
		} else {
			for(int i=0;i<Apts.length;++i) {		
				myPointf res = transforms[0].transformPoint( Apts[i],tB);
				//add transformation
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
		mapCalcsAfterCntlPointsSet("this::resetAllBranching_Indiv", true, false);
	}


}
