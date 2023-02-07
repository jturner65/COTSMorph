package COTS_Morph_PKG.morph.singleTransform.base;

import COTS_Morph_PKG.map.base.Base_PolyMap;
import COTS_Morph_PKG.mapManager.mapPairManager;
import COTS_Morph_PKG.morph.base.baseMorph;
import COTS_Morph_PKG.transformer.spiral.base.baseSpiralTransformer;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;

/**
 * class that composes and applies a single morph algorithm for all 4 distinguishing features of map
 * @author john
 *
 */
public abstract class baseSingleTransformMorph extends baseMorph {
	/**
	 * similarity that will act as transformation
	 */
	protected baseSpiralTransformer transform;
	/**
	 * arrays of corresponding edges between key frames
	 */	
	protected myPointf[] crnrPtAra;

	public baseSingleTransformMorph(COTS_MorphWin _win, mapPairManager _mapMgr, Base_PolyMap _mapA, Base_PolyMap _mapB,int _morphTypeIDX,  String _morphTitle) {super(_win, _mapMgr,_mapA, _mapB, _morphTypeIDX,_morphTitle);}	
	public baseSingleTransformMorph(baseSingleTransformMorph _otr) {		super(_otr);}
	
	/**
	 * this will perform initialization of morph-specific data before initial morph calc is performed, from base class ctor
	 */	
	@Override
	protected final void _endCtorInit() {		
		transform = buildSimilarity();	
		crnrPtAra = getCornerPtAra();
	}	
	/**
	 * get corner points in appropriate format to be used by this morph
	 * @return
	 */
	protected final myPointf[] getCornerPtAra(){
		myPointf[] mapADiag = mapA.getCntlPtDiagonal(),mapBDiag = mapB.getCntlPtDiagonal(); 
		return new myPointf[] {mapADiag[0],mapADiag[1],mapBDiag[0],mapBDiag[1]};
	}

	protected abstract baseSpiralTransformer buildSimilarity();
	
	
	/**
	 * use currently set t value to calculate morph and apply to passed morph map
	 */
	@Override
	protected final void calcMorphAndApplyToMap(Base_PolyMap _curMorphMap, float tA, float tB) {
		myPointf[] aCntlPts = mapA.getCntlPts(), bCntlPts = mapB.getCntlPts(); 
		myPointf[] newPts = new myPointf[aCntlPts.length];
		
		//put morph results into newPts
		calcMorphBetweenTwoSetsOfCntlPoints(aCntlPts, bCntlPts, newPts, tA, tB);
		//set curmorphmap's control points to be those calculated
		_curMorphMap.setCntlPts(newPts, mapFlags[mapUpdateNoResetIDX]);
		_curMorphMap.setCurMorphTVal(tB);
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
