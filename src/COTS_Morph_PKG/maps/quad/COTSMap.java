package COTS_Morph_PKG.maps.quad;

import java.util.TreeMap;

import COTS_Morph_PKG.managers.mapManagers.mapPairManager;
import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.maps.quad.base.baseQuadMap;
import COTS_Morph_PKG.similarities.COTS_Similarity;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapCntlFlags;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_UI_Objects.IRenderInterface;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

/**
 * COTS map based on Jarek's paper
 * @author john
 *
 */
public class COTSMap extends baseQuadMap {
	
	/**
	 * data holding COTS map control values
	 */
	protected COTS_Similarity cots;
	/**
	 * currently set branch sharing strategy, for maps that use angle branching (i.e. COTS)
	 * 0 : do not share branching; 1: force all branching to be map A's branching; 2 : force all branching to be map B's branching; 3 : force all branching to be most recently edited map's branching
	 */
	protected int currBranchShareStrategy=0;
	/**
	 * whether this map should share branching
	 */
	protected boolean shouldShareBranching = false;	


	public COTSMap(COTS_MorphWin _win,  mapPairManager _mapMgr, myPointf[] _cntlPts, int _mapIdx, int _mapTypeIdx, int[][] _pClrs, mapUpdFromUIData _currUIVals,  boolean _isKeyFrame, String _mapTitle) {	
		super(_win, _mapMgr, _cntlPts,_mapIdx, _mapTypeIdx, _pClrs,_currUIVals, _isKeyFrame, _mapTitle);
		cots = new COTS_Similarity(mapTitle,basisVecs[0], basisVecs[2], basisVecs[1]);
		updateMapFromCntlPtVals_Indiv(resetMapUpdateFlags);
		edgePts = buildEdgePoints();
	}
	
	public COTSMap(String _mapTitle, COTSMap _otrMap) {
		super(_mapTitle,  _otrMap);
		cots = new COTS_Similarity(_mapTitle, _otrMap.cots);
		//updateMapFromCntlPtVals_Indiv(regMapUpdateFlags);
		edgePts = buildEdgePoints();
		//currBranchShareStrategy = _otrMap.currBranchShareStrategy;
		//shouldShareBranching = _otrMap.shouldShareBranching;
	}
	
	/**
	 * Instance-class specific initialization
	 */	
	@Override
	protected final void updateMapFromCntlPtVals_Indiv(mapCntlFlags flags) {
		//boolean reset = flags[0];
		//boolean optimizeAlpha = false;	//this is done to force the morphed maps to try to have angles close as possible to other map's angles
		//if(flags.length > 1) {optimizeAlpha= flags[1];}
		if(!isAbleToExec()) {return;}		//need this check since cots similarity not built before this is first called

		cots.deriveSimilarityFromCntlPts(cntlPts, flags);		
	}
	
	/**
	 * flags controls updates
	 * @param otrMap
	 * @param flags
	 */

	@Override
	public void updateMeWithMapVals(baseMap otrMap, mapCntlFlags flags) {
		if ((((COTSMap)otrMap).shouldShareBranching) || (flags.getCopyBranching())){
			cots.setBranching(((COTSMap)otrMap).cots.getBranching());
			updateMapFromOtrMapVals(flags);
		}
	}
	
	@Override
	protected boolean updateMapVals_FromUI_Indiv(mapUpdFromUIData upd) {		
		boolean hasBeenUpdated = false;		
			//update branch sharing strategy, if necessary
		hasBeenUpdated = updateBranchShareStrategy(upd.getBranchSharingStrategy()) ;

		return hasBeenUpdated;
	}
	
	@Override
	protected void registerMeToVals_PreIndiv(myVectorf dispBetweenMaps, float[] angleAndScale) {
		//this function is only called on registration copy map, so this map should never share branching
		shouldShareBranching = false;		
	}

	@Override
	public myPointf calcMapPt(float tx, float ty) {
		//tx interpolates between "vertical" edges, scale and angle, ty interpolates between "horizontal" edges, scale and angle
		return cots.mapPoint(cntlPts[0], tx, ty);
	}
	
	@Override
	protected final void setOtrMap_Indiv() {};
	@Override
	public myPointf getCenterPoint() {	return cots.getF();}
	
	/**
	 * update current branch sharing strategy
	 * @param _currBranchShareStrategy strategy for sharing angle branching, for cots maps.  
	 * 			0 : no branch sharing
	 * 			1 : force all branching to map A's branching
	 * 			2 : force all branching to map B's branching
	 * 			3 : force all branching to most recent edited map's branching
	 */
	private boolean updateBranchShareStrategy(int _currBranchShareStrategy) {
		if(currBranchShareStrategy == _currBranchShareStrategy) {return false;}
		currBranchShareStrategy = _currBranchShareStrategy;
		switch (currBranchShareStrategy) {
			case 0 : {shouldShareBranching = false; break;}
			case 1 : {shouldShareBranching = (mapIdx == 0); break;}
			case 2 : {shouldShareBranching = (mapIdx == 1); break;}
			case 3 : {shouldShareBranching = ((mapIdx == 0) || (mapIdx == 1)); break;}
			default : {return false;}
		}		
		return true;
	}
	
	
	/////////////////////
	// draw routines
	
	/**
	 * instance-specific point drawing
	 * @param pa
	 */
	@Override
	protected void _drawCntlPoints_Indiv(boolean isCurMap, int detail) {
		if(detail < COTS_MorphWin.drawMapDet_CntlPts_COV_F_IDX) {return;}
		pa.sphereDetail(5);
		pa.setStroke(polyColors[1], 255);
		mgr._drawPt(cots.getF(), sphereRad*1.5f);		
	}
	
	@Override
	protected final void _drawPointLabels_Indiv(int detail) {
		if(detail < COTS_MorphWin.drawMapDet_CntlPts_COV_F_IDX) {return;}
		win._drawLabelAtPt(cots.getF(),"Spiral Center : "+ cots.getF().toStrBrf(), 2.5f,-2.5f);		
	}
	
	@Override
	protected final void drawRightSideBarMenuTitle_Indiv() {
		if(null == otrMap) {return;}
		pa.pushMatrix();pa.pushStyle();
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 5.5f, "Shares branching on edit with other map ? ");
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Yellow, 255), 4.5f,  ""+shouldShareBranching);
		pa.popStyle();pa.popMatrix();
	}

	@Override
	protected float drawRightSideBarMenuDescr_Indiv(float yOff, float sideBarYDisp) {
		yOff = cots.drawRightSideBarMenuDescr(pa, yOff, sideBarYDisp);
		return yOff;
	}
	/**
	 * whether this map is ready to execute
	 * @return
	 */
	public final boolean isAbleToExec() {return (null!=cots);}
	/**
	 * set instance-specific flags - idx 0 is branching
	 * @param flags
	 */
	@Override
	public final void setFlags(boolean[] flags) {	 if(flags[0]) {	cots.setAllBranchingZero();}}


	/**
	 * manage mouse/map movement for child-class specific fields
	 */	
	@Override	
	protected final void findClosestCntlPt_Indiv(myPointf _mseLoc, myPointf _rayOrigin, myVectorf _rayDir, TreeMap<Float, myPointf> ptsByDist) {}	
	@Override
	protected final void mseDragInMap_Indiv(myVectorf defVec, myPointf mseClickIn3D_f, boolean isScale, boolean isRotation, boolean isTranslation, char key, int keyCode) {}
	@Override
	protected final void dilateMap_Indiv(float amt) {}
	@Override
	protected final void rotateMapInPlane_Indiv(float thet) {}
	@Override
	protected void moveMapInPlane_Indiv(myVectorf defVec) {}
	@Override
	protected void moveCntlPtInPlane_Indiv(myVectorf defVec) {}
	@Override
	protected final void mseRelease_Indiv() {	}

}//COTSMap

