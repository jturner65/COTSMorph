package COTS_Morph_PKG.maps.triangular;

import java.util.TreeMap;

import COTS_Morph_PKG.managers.mapManagers.mapPairManager;
import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.maps.triangular.base.baseTriangleMap;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapCntlFlags;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

public class BiLinTriPolyMap extends baseTriangleMap {

	public BiLinTriPolyMap(COTS_MorphWin _win, mapPairManager _mapMgr, myPointf[] _cntlPts, int _mapIdx, int _mapTypeIDX, int[][] _pClrs, mapUpdFromUIData _currUIVals,  boolean _isKeyFrame, String _mapTitle) {
		super(_win, _mapMgr, _cntlPts, _mapIdx, _mapTypeIDX, _pClrs, _currUIVals, _isKeyFrame, _mapTitle);
	}

	public BiLinTriPolyMap(String _mapTitle, BiLinTriPolyMap _otr) {	super(_mapTitle, _otr);}

	@Override
	protected boolean updateMapVals_FromUI_Indiv(mapUpdFromUIData upd) {
		boolean hasBeenUpdated = false;		
		return hasBeenUpdated;
	}

	@Override
	public void updateMeWithMapVals(baseMap otrMap, mapCntlFlags flags) {
	}

	/**
	 * Instance-class specific initialization
	 */	
	@Override
	protected final void updateMapFromCntlPtVals_Indiv(mapCntlFlags flags) {
		boolean reset = flags.getResetBranching();
	}
	@Override
	protected void registerMeToVals_PreIndiv(myVectorf dispBetweenMaps, float[] angleAndScale) {}
	
	@Override
	public final float calcTtlSurfaceArea() {	
		return mgr.calcAreaOfPolyInPlane(cntlPts, distPlanarPt, basisVecs[0]);
	}//calcTtlSurfaceArea	
	
	/**
	 * Return array of all morph-relevant cntl/info points for this map.
	 * Call if morph map -after-  morph is calced.  
	 * Include COV and possibly F point, if COTS or other spiral-based map
	 * @return
	 */
	@Override
	public final void getAllMorphCntlPts_Indiv(myPointf[] res) {
		res[cntlPts.length]= new myPointf(this.cntlPtCOV);
	};
	@Override
	public final int getNumAllMorphCntlPts() {	return cntlPts.length + 1;};
	/**
	 * instance specific values should be added here
	 * @param map
	 */
	@Override
	public final void getTrajAnalysisKeys_Indiv(TreeMap<String, Integer> map) {
		int numTtlPts = getNumAllMorphCntlPts();
		map.put(COV_Label, numTtlPts-1);
	}

		
	/////////////////////
	// draw routines
	@Override
	protected float drawRtSdMenuDescr_Indiv(float yOff, float sideBarYDisp) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void _drawCntlPoints_Indiv(boolean isCurMap, int detail) {

	}

	@Override
	protected void _drawPointLabels_Indiv(int detail) {

	}
	
	/**
	 * set instance-specific flags
	 * @param flags
	 */
	@Override
	public final void setFlags(boolean[] flags) {};


	@Override
	protected void findClosestCntlPt_Indiv(myPointf _mseLoc, myPointf _rayOrigin, myVectorf _rayDir, TreeMap<Float, myPointf> ptsByDist) {}
	@Override
	protected void dilateMap_Indiv(float amt) {}
	@Override
	protected void rotateMapInPlane_Indiv(float thet) {}
	@Override
	protected void moveMapInPlane_Indiv(myVectorf defVec) {}
	@Override
	protected void moveCntlPtInPlane_Indiv(myVectorf defVec) {}
	@Override
	protected void mseDragInMap_Indiv(myVectorf defVec, myPointf mseClickIn3D_f, boolean isScale, boolean isRotation,  boolean isTranslation,char key, int keyCode) {	}

	@Override
	protected void mseRelease_Indiv() {	}

	@Override
	protected void setOtrMap_Indiv() {	}

}
