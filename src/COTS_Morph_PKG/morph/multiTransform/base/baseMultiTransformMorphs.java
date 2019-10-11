package COTS_Morph_PKG.morph.multiTransform.base;

import COTS_Morph_PKG.map.base.baseMap;
import COTS_Morph_PKG.mapManager.mapPairManager;
import COTS_Morph_PKG.morph.base.baseMorph;
import COTS_Morph_PKG.transformer.spiral.base.baseSpiralTransformer;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_Utils_Objects.vectorObjs.myPointf;

public abstract class baseMultiTransformMorphs extends baseMorph {
	/**
	 * similarities that will act as transformations
	 */
	protected baseSpiralTransformer[] transforms;
	/**
	 * arrays of corresponding edges between key frames
	 */	
	protected myPointf[][] crnrPtAras;


	public baseMultiTransformMorphs(COTS_MorphWin _win, mapPairManager _mapMgr, baseMap _mapA, baseMap _mapB,int _morphTypeIDX,  String _morphTitle) {super(_win, _mapMgr,_mapA, _mapB, _morphTypeIDX,_morphTitle);}
	public baseMultiTransformMorphs(baseMultiTransformMorphs _otr) {super(_otr);}

	/**
	 * this will perform initialization of morph-specific data before initial morph calc is performed, from base class ctor
	 */	
	@Override
	protected final void _endCtorInit() {		
		crnrPtAras = getCornerPtAras();
		if((null==crnrPtAras) || (crnrPtAras.length == 0)) {return;}
		transforms = new baseSpiralTransformer[crnrPtAras.length];		
		for(int i=0;i<transforms.length;++i) {			transforms[i] = buildSimilarity(i);		}		
	}	
	/**
	 * get corner points in appropriate format to be used by this morph
	 * @return
	 */
	protected abstract myPointf[][] getCornerPtAras();
	protected abstract baseSpiralTransformer buildSimilarity(int idx);
	
	@Override
	protected final void updateMorphValsFromUI_Indiv(mapUpdFromUIData upd) {
	}

	
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
		_curMorphMap.setCurMorphTVal(tB);
	}//calcMorphAndApplyToMap	
	
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
		if(transformsAreBad()) {
			for(int i=0;i<Apts.length;++i) {	destPts[i]=  myPointf._add(myPointf._mult(Apts[i], tA), myPointf._mult(Bpts[i], tB));}	//forces to lerp calc - only use if not yet properly initialized
		} else {	
			_calcMorphCntlPoints_MultiTransIndiv(Apts, Bpts, destPts, tA, tB);
		}
	}//calcMorphBetweenTwoSetsOfPoints
	
	/**
	 * calcluate this morph algorithm between Apts and Bpts, putting result in destPts
	 * @param Apts
	 * @param Bpts
	 * @param destPts
	 * @param tA
	 * @param tB
	 */
	protected abstract void _calcMorphCntlPoints_MultiTransIndiv(myPointf[] Apts, myPointf[] Bpts, myPointf[] destPts, float tA, float tB);
	
	protected abstract boolean transformsAreBad();
	
	@Override
	public float drawMorphRtSdMenuDescr_Indiv(float yOff, float sideBarYDisp) {
		if(transformsAreBad()) {return yOff;}
		for(int i=0;i<transforms.length;++i) {
			transforms[i].drawRightSideBarMenuDescr(pa, yOff, sideBarYDisp);
			yOff += sideBarYDisp;	pa.translate(0.0f,sideBarYDisp, 0.0f);	
		}		
		return yOff;
	}
	@Override
	public void resetAllBranching_Indiv() {
		if(transformsAreBad()) {return;}
		for(int i=0;i<transforms.length;++i) {transforms[i].setAllBranchingZero();}
		mapCalcsAfterCntlPointsSet("this::resetAllBranching_Indiv", true, false);
	}


}//class baseMultiTransformMorphs
