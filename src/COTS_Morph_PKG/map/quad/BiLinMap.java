package COTS_Morph_PKG.map.quad;

import java.util.TreeMap;

import COTS_Morph_PKG.map.base.baseMap;
import COTS_Morph_PKG.map.quad.base.baseQuadMap;
import COTS_Morph_PKG.mapManager.mapPairManager;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import COTS_Morph_PKG.utils.controlFlags.base.Base_ControlFlags;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;

public class BiLinMap extends baseQuadMap {

	public BiLinMap(COTS_MorphWin _win,  mapPairManager _mapMgr, myPointf[] _cntlPts, int _mapIdx, int _mapTypeIdx, int[][] _pClrs, mapUpdFromUIData _currUIVals,  boolean _isKeyFrame, boolean _isBaryQuad, String _mapTitle) {
		super(_win, _mapMgr, _cntlPts,_mapIdx, _mapTypeIdx, _pClrs,_currUIVals, _isKeyFrame, _isBaryQuad, _mapTitle);
	}
	public BiLinMap(String _mapTitle, BiLinMap _otrMap) {super(_mapTitle, _otrMap);}

	
	/**
	 * Instance-class specific initialization
	 */	
	@Override
	protected final void _updateQuadMapFromCntlPtVals_Indiv(Base_ControlFlags flags) {
		//boolean reset = flags.getResetBranching();
	}
	@Override
	protected boolean updateMapVals_FromUI_Indiv(mapUpdFromUIData upd) {	boolean hasBeenUpdated = false;		return hasBeenUpdated;}
	@Override
	public void _updateMeWithQuadMapVals(baseMap otrMap, Base_ControlFlags flags) {
		
	}
	
	@Override
	public myPointf calcMapPt(float tx, float ty) {	return new myPointf(new myPointf(cntlPts[0],tx,cntlPts[1]), ty, new myPointf(cntlPts[3],tx,cntlPts[2]));}
	@Override
	protected void registerMeToVals_PreIndiv(myVectorf dispBetweenMaps, float[] angleAndScale) {}
	
	@Override
	public final float calcTtlSurfaceArea() {	
		return mgr.calcAreaOfPolyInPlane(cntlPts,distPlanarPt, basisVecs[0]);
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
	
	/**
	 * instance-specific point drawing
	 * @param pa
	 */
	@Override
	protected void _drawCntlPoints_Indiv(boolean isCurMap, int detail) {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected float _drawQuadMapRtSdMenuDescr_Indiv(float yOff, float sideBarYDisp) {
		return yOff;
	}
	@Override
	protected final void _drawPointLabels_Indiv(int detail) {	}
	@Override
	protected final void drawRtSdMenuTitle_Indiv() {}

	
	/**
	 * set instance-specific flags
	 * @param flags
	 */
	@Override
	public final void setFlags(boolean[] flags) {};
	@Override
	protected final void _setOtrQuadMap_Indiv() {};
	
	/**
	 * whether this map is ready to execute
	 * @return
	 */
	public final boolean isAbleToExec() {return true;}
	
	/**
	 * manage mouse/map movement for child-class specific fields
	 */	
	@Override	
	protected final void findClosestCntlPt_Indiv(myPointf _mseLoc, myPointf _rayOrigin, myVectorf _rayDir, TreeMap<Float, myPointf> ptsByDist) {}	
	@Override
	protected final void mseDragInMap_Indiv(myVectorf defVec, myPointf mseClickIn3D_f,boolean isScale,boolean isRotation, boolean isTranslation, char key, int keyCode) {}
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

}//class biLinMap
