package COTS_Morph_PKG.maps;

import java.util.TreeMap;

import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_UI_Objects.windowUI.myDispWindow;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;
import processing.core.PConstants;

public class TriangleBiLinMap extends baseMap {
	/**
	 * normals at each control point - used to set other map's cntl point location
	 */
	protected myVectorf[] cntlPtNorms;
	/**
	 * points to display on UI for enpoints of control point normal vectors
	 */
	protected myPointf[] cntlPtNormEndPts;
	
	public myPointf[] otrCntlPts;
	
		//array of labels to use for control points
	protected String[] cntlPtNormLbls;	
	
	/**
	 * editable Q point
	 */
	public myPointf Qpt;
	public float[] qPointNBC;
	
	protected final float normDispVecOffset = 50.0f;
	
	public TriangleBiLinMap(COTS_MorphWin _win, myPointf[] _cntlPts, int _mapIdx, int _mapTypeIDX, int[][] _pClrs,int _numCellPerSide, String _mapTitle) {
		super(_win, _cntlPts, _mapIdx, _mapTypeIDX, _pClrs, _numCellPerSide, _mapTitle);
		 _initCntlPtNormVecs();
	}

	public TriangleBiLinMap(TriangleBiLinMap _otr) {
		super(_otr);	
		_initCntlPtNormVecs();
	}
	
	private void _initCntlPtNormVecs() {
		if(cntlPtNorms != null) {return;}
		Qpt = new myPointf(cntlPts[0], .5f, cntlPtCOV);
		qPointNBC = calcNormBaryCoords(Qpt);	
		cntlPtNorms = new myVectorf[cntlPts.length];
		cntlPtNormLbls = new String[cntlPtNorms.length];
		cntlPtNormEndPts = new myPointf[cntlPtNorms.length];
		otrCntlPts = new myPointf[cntlPtNorms.length];
		for(int i=0;i<cntlPtNorms.length;++i) {	
			cntlPtNorms[i] = new myVectorf();
			cntlPtNormLbls[i]="" + ((char)(i+'U'));	
			cntlPtNormEndPts[i] = new myPointf();
			otrCntlPts[i] = new myPointf();
		}	
	}
	
	/**
	 * only call if otrMap has been set
	 */
	protected void _setCntlPtNorms() {
		if(otrMap == null) {return;}
		myPointf[] otrCntlPts = otrMap.getCntlPts();
		if(cntlPtNorms == null) {			_initCntlPtNormVecs();		}
		for(int i=0;i<cntlPtNorms.length;++i) {	
			cntlPtNorms[i].set(cntlPts[i], otrCntlPts[i]);
			cntlPtNorms[i]._normalize();
			cntlPtNormEndPts[i].set(myPointf._add(cntlPts[i], normDispVecOffset, cntlPtNorms[i]));
		}
	}
	
	@Override
	protected void updateMapFromCntlPtVals_Indiv(boolean reset) {
		//first move other map's control points
		if(otrMap == null) {			return;		}
		//myPointf[] otrMapCntlPts = otrMap.getCntlPts();
		for(int i=0;i<cntlPts.length;++i) {
			myPointf pt = otrMap.findPointInMyPlane(cntlPts[i], cntlPtNorms[i]);
			otrCntlPts[i].set(pt);
		}
		otrMap.updateMeWithMapVals(this);
	}

	
	@Override
	public void updateMeWithMapVals(baseMap otrMap) {
		for(int i=0;i<cntlPts.length;++i) {		cntlPts[i].set(((TriangleBiLinMap)otrMap).otrCntlPts[i]);		}
		Qpt.set(calcPointFromNormBaryCoords(((TriangleBiLinMap)otrMap).qPointNBC));
		qPointNBC = calcNormBaryCoords(Qpt);	
		_setCntlPtNorms();
		//find center of control points
		setCurrCntrlPtCOV();		
	}

	@Override
	protected boolean updateMapVals_FromUI_Indiv(mapUpdFromUIData upd) {
		boolean hasBeenUpdated = false;		
		return hasBeenUpdated;
	}
	
	/**
	 * not used for this map
	 */
	@Override
	public myPointf calcMapPt(float tx, float ty) {	return null;}
	
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
	
	@Override
	protected void _drawCntlPoints_Indiv(boolean isCurMap) {
		for(int i=0;i<cntlPtNorms.length;++i) {
			myPointf p = cntlPts[i];
			_drawVec(p, cntlPtNormEndPts[i], new int[] {0,0,0,255}, (p.equals(currMseModCntlPt) && isCurMap ? 2.0f*sphereRad : sphereRad));
		}
		pa.pushMatrix();pa.pushStyle();
		if(mapIdx==0) {		pa.stroke(255, 0 ,255,255);	} else {pa.stroke( 0, 255,255,255);}
		
		pa.setStrokeWt(1.0f);
		for(int i=0;i<otrCntlPts.length;++i) {
			myPointf p = otrCntlPts[i];
			_drawPt(p, sphereRad);
		}
		pa.popStyle();pa.popMatrix();
		
		pa.pushMatrix();pa.pushStyle();	
		pa.stroke(255,255,0,255);
		pa.setStrokeWt(1.0f);		
		_drawPt(Qpt, sphereRad);
		if((otrMap != null) && (mapIdx == 0)) {
			pa.line(((TriangleBiLinMap)otrMap).Qpt, Qpt);
		}
		pa.popStyle();pa.popMatrix();
	}
	
	@Override
	public void drawMap_Fill() {
		pa.pushMatrix();pa.pushStyle();	
		pa.stroke(255,255,255,255);
		pa.setStrokeWt(1.0f);
		pa.setFill(polyColors[1], polyColors[1][3]);
		_drawCntlPtPoly();
		pa.popStyle();pa.popMatrix();
	}//drawMap_Fill
	@Override
	//ignore circles for this map type
	public void drawMap_PolyCircles_Fill() {	drawMap_Fill();}
	
	@Override	
	public void drawMap_Wf() {
		pa.pushMatrix();pa.pushStyle();	
		pa.noFill();
		pa.setStroke(gridColor, gridColor[3]);
		pa.setStrokeWt(1.5f);	
		_drawCntlPtPoly();
		pa.popStyle();pa.popMatrix();
	}//drawMap_Wf
	@Override
	//ignore circles for this map type
	public void drawMap_PolyCircles_Wf() {		drawMap_Wf();	}	
	@Override
	//ignore texture - use wire frame
	public void drawMap_Texture() {				drawMap_Wf();}

	@Override
	protected void _drawPointLabels_Indiv() {
		for(int i=0;i<cntlPtNorms.length;++i) {
			myPointf p = cntlPtNormEndPts[i];
			win._drawLabelAtPt(p,   cntlPtNormLbls[i]+ "_"+mapIdx + " : " + cntlPtNorms[i].toStrBrf(), 2.5f,-2.5f);
		}
		win._drawLabelAtPt(Qpt, (mapIdx == 0 ? "P":"Q") + " : [" +String.format("%.3f",qPointNBC[0]) +","+String.format("%.3f",qPointNBC[1])+ "," + String.format("%.3f",qPointNBC[2])+"] : " + Qpt.toStrBrf(),  2.5f,-2.5f);
	}
	@Override
	protected float drawRightSideBarMenuDescr_Indiv(float yOff, float sideBarYDisp) {
		return yOff;
	}

	
	/////////////////////
	// set/get	
	@Override
	protected final void setOtrMap_Indiv() {_setCntlPtNorms();	}
	@Override
	public myPointf getCenterPoint() {return cntlPtCOV;}
	
	/**
	 * manage mouse/map drag movement for child-class specific fields
	 */
	@Override	
	protected final void findClosestCntlPt_Indiv(myPointf _mseLoc, myPointf _rayOrigin, myVectorf _rayDir, TreeMap<Float, myPointf> ptsByDist) {
		for(int i=0;i<cntlPtNormEndPts.length;++i) {		ptsByDist.put(win.findDistToPtOrRay(_mseLoc, cntlPtNormEndPts[i],_rayOrigin,_rayDir), cntlPtNormEndPts[i]);}	
		ptsByDist.put(win.findDistToPtOrRay(_mseLoc, Qpt, _rayOrigin, _rayDir), Qpt);
	}	
	/**
	 * after control points are moved, make sure they are normalized
	 */
	@Override
	protected void mseDragInMap_Indiv(myVectorf defVec, myPointf mseClickIn3D_f,boolean isScale,boolean isRotation, char key, int keyCode) {
		for(int i=0;i<cntlPtNorms.length;++i) {	
			cntlPtNorms[i].set(cntlPts[i], cntlPtNormEndPts[i]);
			cntlPtNorms[i]._normalize();
			cntlPtNormEndPts[i].set(myPointf._add(cntlPts[i], normDispVecOffset, cntlPtNorms[i]));
		}	
		qPointNBC = calcNormBaryCoords(Qpt);		
	}
	/**
	 * update control point normals upon editing, as well as editable q point and bound points
	 */
	private void updateCntlPtNormEndPts() {
		for(int i=0;i<cntlPtNormEndPts.length;++i) {	cntlPtNormEndPts[i].set(myPointf._add(cntlPts[i], normDispVecOffset, cntlPtNorms[i]));}
		//recalculate Qpt
		Qpt.set(calcPointFromNormBaryCoords(qPointNBC));
	}
	
	/**
	 * move normals appropriately for dilation
	 */
	@Override
	protected void dilateMap_Indiv(float amt) {	updateCntlPtNormEndPts();}	
	/**
	 * move normals appropriately for rotation
	 */
	@Override
	protected void rotateMapInPlane_Indiv(float thet) {updateCntlPtNormEndPts();}
	/**
	 * move normals appropriately 
	 */
	@Override
	protected void moveMapInPlane_Indiv(myVectorf defVec) {updateCntlPtNormEndPts();}
	
	@Override
	protected void moveCntlPtInPlane_Indiv(myVectorf defVec) {
		for(int i=0;i<cntlPts.length;++i) {	if(currMseModCntlPt.equals(cntlPts[i])) {updateCntlPtNormEndPts(); return;}	}		//if moving control point, move vector and vector end point along with it
	}
	
	
	@Override
	protected void mseRelease_Indiv() {
	}


}//class TriangleBiLinMap
