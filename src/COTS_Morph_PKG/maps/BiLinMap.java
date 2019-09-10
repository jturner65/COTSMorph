package COTS_Morph_PKG.maps;

import java.util.TreeMap;

import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_UI_Objects.windowUI.myDispWindow;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

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
	 * build a set of edge points around the edge of this map
	 */
	protected final myPointf[][] buildEdgePoints() {
		 myPointf[][] ePts = new myPointf[cntlPts.length][numCellsPerSide];
		 myPointf A, B;
		 myVectorf AB;
		 for(int i=0;i<ePts.length;++i) {
			 A = cntlPts[i];
			 B = cntlPts[(i+1)%cntlPts.length];
			 AB = new myVectorf(A,B);
			 for(int j=0;j<numCellsPerSide;++j) { ePts[i][j] = myPointf._add(A,(1.0f*j)/numCellsPerSide, AB);}
		 }
		 return ePts;
	}

	/////////////////////
	// draw routines
	
	/**
	 * instance-specific point drawing
	 * @param pa
	 */
	@Override
	protected void _drawCntlPoints_Indiv(boolean isCurMap) {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected float drawRightSideBarMenuDescr_Indiv(float yOff, float sideBarYDisp) {
		return yOff;
	}


	@Override
	protected boolean updateMapVals_FromUI_Indiv(mapUpdFromUIData upd) {	boolean hasBeenUpdated = false;		return hasBeenUpdated;}
	@Override
	protected final void _drawPointLabels_Indiv() {	}
	@Override
	public void updateMeWithMapVals(baseMap otrMap) {
		
	}
	@Override
	protected final void setOtrMap_Indiv() {};
	@Override
	public myPointf getCenterPoint() {return cntlPtCOV;}
	
	/**
	 * manage mouse/map movement for child-class specific fields
	 */	
	@Override	
	protected final void findClosestCntlPt_Indiv(myPointf _mseLoc, myPointf _rayOrigin, myVectorf _rayDir, TreeMap<Float, myPointf> ptsByDist) {}	
	@Override
	protected final void mseDragInMap_Indiv(myVectorf defVec, myPointf mseClickIn3D_f,boolean isScale,boolean isRotation,  char key, int keyCode) {}
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
