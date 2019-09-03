package COTS_Morph_PKG.maps;

import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import base_UI_Objects.windowUI.myDispWindow;
import base_Utils_Objects.vectorObjs.myPointf;

public class BiLinMap extends baseMap {

	public BiLinMap(COTS_MorphWin _win, myPointf[] _cntlPts, int _mapIdx, int _mapTypeIdx, int[][] _pClrs, int _numCellPerSide, String _mapTitle) {super(_win,_cntlPts,_mapIdx, _mapTypeIdx, _pClrs,_numCellPerSide, _mapTitle);}
	public BiLinMap(BiLinMap _otrMap) {super(_otrMap);}
	/**
	 * Instance-class specific initialization
	 */	
	@Override
	protected final void updateMapFromCntlPtVals_Indiv(boolean reset) {
	}

	@Override
	public myPointf calcMapPt(float tx, float ty) {	return new myPointf(new myPointf(cntlPts[0],tx,cntlPts[1]), ty, new myPointf(cntlPts[3],tx,cntlPts[2]));}
	/**
	 * instance-specific point drawing
	 * @param pa
	 */
	@Override
	protected void _drawPoints_Indiv() {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected float drawRightSideBarMenuDescr_Indiv(float yOff, float sideBarYDisp) {
		return yOff;
	}

	@Override
	protected void mseRelease_Indiv() {	}

	@Override
	protected boolean updateMapVals_Indiv() {	boolean hasBeenUpdated = false;		return hasBeenUpdated;}
	@Override
	protected final void _drawPointLabels_2D_Indiv() {	}
	@Override
	protected final void _drawPointLabels_3D_Indiv(myDispWindow animWin) {	}
	@Override
	public void updateMeWithMapVals(baseMap otrMap) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public myPointf getCenterPoint() {return cntlPtCOV;}

}//class biLinMap
