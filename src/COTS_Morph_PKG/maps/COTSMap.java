package COTS_Morph_PKG.maps;

import java.util.TreeMap;

import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.similarities.COTS_Similarity;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_UI_Objects.windowUI.myDispWindow;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

/**
 * COTS map based on Jarek's paper
 * @author john
 *
 */
public class COTSMap extends baseMap {
	
	/**
	 * data holding COTS map control values
	 */
	protected COTS_Similarity cots;
	

	public COTSMap(COTS_MorphWin _win, myPointf[] _cntlPts, int _mapIdx, int _mapTypeIdx, int[][] _pClrs, int _numCellPerSide, String _mapTitle) {	
		super(_win,_cntlPts,_mapIdx, _mapTypeIdx, _pClrs,_numCellPerSide, _mapTitle);
		cots = new COTS_Similarity(basisVecs[0], basisVecs[2], basisVecs[1]);
		updateMapFromCntlPtVals_Indiv( true);
	}
	public COTSMap(COTSMap _otrMap) {
		super(_otrMap);
		cots = new COTS_Similarity(_otrMap.cots);
		updateMapFromCntlPtVals_Indiv( true);
	}
	
	/**
	 * Instance-class specific initialization
	 */	
	@Override
	protected final void updateMapFromCntlPtVals_Indiv(boolean reset) {
		if(null==cots) {return;}		//need this check since cots similarity not built before this is first called

		cots.deriveSimilarityFromCntlPts(cntlPts, reset);		
	}
	

	@Override
	public void updateMeWithMapVals(baseMap otrMap) {
		cots.setBranching(((COTSMap)otrMap).cots.getBranching());
		updateMapFromOtrMapVals(false);
	}
	
	@Override
	public myPointf calcMapPt(float tx, float ty) {
		//tx interpolates between "vertical" edges, scale and angle, ty interpolates between "horizontal" edges, scale and angle
		return cots.mapPoint(cntlPts[0], tx, ty);
	}
	
	@Override
	protected final void setOtrMap_Indiv() {};
	@Override
	public myPointf getCenterPoint() {
		return cots.getF();
	}
	
	@Override
	protected boolean updateMapVals_FromUI_Indiv(mapUpdFromUIData upd) {		boolean hasBeenUpdated = false;		return hasBeenUpdated;}
	/**
	 * build a set of edge points around the edge of this map
	 */
	protected final myPointf[][] buildEdgePoints() {
		 myPointf[][] ePts = new myPointf[cntlPts.length][numCellsPerSide];
		 if(null==cots) {return ePts;}
		 for(int j=0;j<ePts[0].length;++j) { 
			 float t = (1.0f*j)/ePts[0].length;
			 ePts[0][j] = calcMapPt(t, 0.0f);
			 ePts[1][j] = calcMapPt(1.0f, t);
			 ePts[2][j] = calcMapPt(1.0f-t, 1.0f);
			 ePts[3][j] = calcMapPt(0.0f, 1.0f-t);
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
		pa.sphereDetail(5);
		pa.setStroke(polyColors[1], 255);
		_drawPt(cots.getF(), sphereRad*1.5f);		
	}
	
	@Override
	protected final void _drawPointLabels_Indiv() {
		win._drawLabelAtPt(cots.getF(),"Spiral Center : "+ cots.getF().toStrBrf(), 2.5f,-2.5f);		
	}
	
	
	@Override
	protected float drawRightSideBarMenuDescr_Indiv(float yOff, float sideBarYDisp) {
		yOff = cots.drawRightSideBarMenuDescr(pa, yOff, sideBarYDisp);
		return yOff;
	}

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

}//COTSMap

