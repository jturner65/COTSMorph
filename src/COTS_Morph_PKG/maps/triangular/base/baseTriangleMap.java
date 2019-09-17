package COTS_Morph_PKG.maps.triangular.base;

import COTS_Morph_PKG.managers.mapManagers.mapPairManager;
import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapCntlFlags;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

/**
 * base class for triangle maps, that provides overrides for functions in baseMap that don't make sense for triangles
 * @author john
 *
 */
public abstract class baseTriangleMap extends baseMap {

	public baseTriangleMap(COTS_MorphWin _win, mapPairManager _mapMgr, myPointf[] _cntlPts, int _mapIdx, int _mapTypeIDX, int[][] _pClrs, mapUpdFromUIData _currUIVals,  boolean _isKeyFrame, String _mapTitle) {
		super(_win, _mapMgr,_cntlPts, _mapIdx, _mapTypeIDX, _pClrs, _currUIVals, _isKeyFrame, _mapTitle);		
	}

	public baseTriangleMap(String _mapTitle, baseTriangleMap _otr) {		super(_mapTitle, _otr);			}
	
	/**
	 * not used for triangle maps - shouldn't be called - returns null to flag as problem
	 */
	@Override
	public final myPointf calcMapPt(float tx, float ty) {	return null;}
	/**
	 * precalculate the tx,ty values for the grid poly bounds - not used for triangle maps
	 */
	@Override
	protected final void buildPolyPointTVals() {}

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
	}//buildEdgePointsdc
	
	/**
	 * draw a minimized lineup picture with appropriate settings
	 */
	@Override	
	protected final void drawMap_LineUp_Indiv(boolean fillOrWf, boolean notUsed1, boolean notUsed2) {
		drawMap_Fill();	
	}//drawMap_LineUp_Indiv

	@Override
	public final void drawMap_Fill() {
		pa.pushMatrix();pa.pushStyle();	
		pa.stroke(255,255,255,255);
		pa.setStrokeWt(1.0f);
		pa.setFill(polyColors[1], polyColors[1][3]);
		_drawCntlPtPoly();
		pa.popStyle();pa.popMatrix();
	}//drawMap_Fill
	@Override
	//ignore circles for this map type
	public final void drawMap_PolyCircles_Fill() {	drawMap_Fill();}
	
	@Override	
	public final void drawMap_Wf() {
		pa.pushMatrix();pa.pushStyle();	
		pa.noFill();
		pa.setStroke(gridColor, gridColor[3]);
		pa.setStrokeWt(1.5f);	
		_drawCntlPtPoly();
		pa.popStyle();pa.popMatrix();
	}//drawMap_Wf
	@Override
	//ignore circles for this map type
	public final void drawMap_PolyCircles_Wf() {		drawMap_Wf();	}	
	@Override
	//ignore texture - use wire frame
	public final void drawMap_Texture() {				drawMap_Wf();}
	
	@Override
	protected final void drawRightSideBarMenuTitle_Indiv() {}
	@Override
	public final myPointf getCenterPoint() {return cntlPtCOV;}

}
