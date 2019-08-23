package COTS_Morph_PKG.maps;

import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import base_UI_Objects.windowUI.myDispWindow;
import base_Utils_Objects.vectorObjs.myPointf;

public class biLinMap extends baseMap {

	public biLinMap(COTS_MorphWin _win, myPointf[] _cntlPts, int _mapIdx, int[][] _pClrs, int _numCellPerSide) {super(_win,_cntlPts,_mapIdx,_pClrs,_numCellPerSide, "Bilinear Map");}
	/**
	 * Instance-class specific initialization
	 */	
	protected final void updateCntlPtVals_Indiv(boolean reset) {
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
	protected void mseRelease_Indiv() {	}

	@Override
	protected boolean updateMapVals_Indiv(boolean hasBeenUpdated) {
		// TODO Auto-generated method stub
		return hasBeenUpdated;
	}
	@Override
	protected final void _drawPointLabels_2D_Indiv() {
	}
	@Override
	protected final void _drawPointLabels_3D_Indiv(myDispWindow animWin) {
		
	}


}//class biLinMap
