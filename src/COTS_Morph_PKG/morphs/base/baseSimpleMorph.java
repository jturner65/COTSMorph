package COTS_Morph_PKG.morphs.base;

import COTS_Morph_PKG.managers.mapManagers.mapPairManager;
import COTS_Morph_PKG.managers.morphManagers.base.baseMorphManager;
import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import base_Utils_Objects.vectorObjs.myPointf;

/**
 * class that composes and applies a single morph algorithm for all 4 distinguishing features of map
 * @author john
 *
 */
public abstract class baseSimpleMorph extends baseMorph {

	public baseSimpleMorph(COTS_MorphWin _win, baseMorphManager _morphMgr, mapPairManager _mapMgr, String _morphTitle) {
		super(_win, _morphMgr, _mapMgr, _morphTitle);
	}

	/**
	 * use currently set t value to calculate morph and apply to passed morph map
	 */
	@Override
	public void calcMorphAndApplyToMap(baseMap _curMorphMap, float tA, float tB) {
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


}
