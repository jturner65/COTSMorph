package COTS_Morph_PKG.maps.base;

import java.util.TreeMap;

import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_UI_Objects.IRenderInterface;
import base_UI_Objects.my_procApplet;
import base_UI_Objects.windowUI.myDispWindow;
import base_Utils_Objects.MyMathUtils;
import base_Utils_Objects.vectorObjs.myPoint;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVector;
import base_Utils_Objects.vectorObjs.myVectorf;
import processing.core.PConstants;
import processing.core.PImage;

/**
 * base class describing a mapping
 * @author john
 *
 */
public abstract class baseMap {
	/**
	 * array of corner cntl points in current state;array of original control points - initial configuration
	 */
	protected myPointf[] cntlPts,origCntlPts;
	/**
	 * equal spaced points around polygon edges
	 */
	protected myPointf[][] edgePts;
	/**
	 * from cov to point first clicked on in map space
	 */
	protected myVectorf currMseClkLocVec;
	/**
	 * point at center of cntrl points - "center" of map verts; control point currently being moved/modified by UI interaction
	 */
	protected myPointf cntlPtCOV,origCntlPtCOV, currMseModCntlPt;		
	/**
	 * poly corner t values - recalced when cntlpts move - idx 1 is x idx, idx2 is y idx
	 */
	protected float[][][] polyPointTVals;	
	/**
	 * # of cells per side in grid
	 */
	protected int numCellsPerSide = -1;
	/**
	 * reference to other map, if this is a keyframe map, null otherwise
	 */
	protected baseMap otrMap;
	/**
	 * map index for this map , either 0, 1 or 2(if morph map); idx of type of map, from win defs for type of instancing class
	 */
	public final int mapIdx, mapTypeIDX;
	/**
	 * image to deform/distort for map
	 */
	protected PImage imageToMap;	
	/**
	 * currently set branch sharing strategy, for maps that use angle branching (i.e. COTS)
	 * 0 : do not share branching; 1: force all branching to be map A's branching; 2 : force all branching to be map B's branching; 3 : force all branching to be most recently edited map's branching
	 */
	protected int currBranchShareStrategy;
	/**
	 * whether this map should share branching
	 */
	protected boolean shouldShareBranching = false;	
	/**
	 * coplanar ortho frame for map idx 0 == norm; idx 1 == 'y' axis, idx 2 == 'x' axis
	 */
	public myVectorf[] basisVecs;

		//rotational UI mod scale value
	private static final float rotScl = .0025f;
		//# of total points for edge of map
	private static final float numTtlPtsPerEdge = 300.0f;
		//base radius of drawn sphere
	protected static final float sphereRad = 5.0f;
		//# of subdivisions per poly for checkerboard
	protected float subDivPerPoly;
		//array of 2 poly colors
	protected int[][] polyColors;
		//color for grid ISO lines
	protected int[] gridColor;

	protected my_procApplet pa;
	protected COTS_MorphWin win;
		//title of map for display	
	public String mapTitle;
		//where to print map title above map
	protected myPointf mapTitleOffset;
	public final float mapTtlXOff;	
		//array of labels to use for control points
	protected final String[] cntlPtLbls;	
		//display ortho frame
	private myPointf[] orthoFrame;
	
	public baseMap(COTS_MorphWin _win, myPointf[] _cntlPts, int _mapIdx, int _mapTypeIDX, int[][] _pClrs, int _numCellPerSide, String _mapTitle) {
		win=_win; pa=myDispWindow.pa;
		mapTypeIDX = _mapTypeIDX;
		mapIdx = _mapIdx;
		mapTitle = _mapTitle;
		mapTtlXOff = myDispWindow.yOff*mapTitle.length()*.25f;
		mapTitleOffset = new myPointf(0,0,0);
		polyColors = new int[_pClrs.length][];
		for(int i=0;i<polyColors.length;++i) {
			polyColors[i]=new int[_pClrs[i].length];
			System.arraycopy(_pClrs[i], 0, polyColors[i], 0, polyColors[i].length);
		}
		gridColor = new int[polyColors[1].length];
		System.arraycopy(polyColors[1], 0, gridColor, 0, gridColor.length);

		cntlPtCOV = new myPointf(0,0,0);
		currMseModCntlPt = null;
		currMseClkLocVec = null;
		
		//build ortho basis for map based on initial control points
		basisVecs = buildBasisVecs(myVectorf._cross(new myVectorf(_cntlPts[0], _cntlPts[1]), new myVectorf(_cntlPts[0], _cntlPts[_cntlPts.length-1]))._normalize());
		//build display-only ortho frame
		buildOrthoFrame();
		//labels for points
		origCntlPtCOV = new myPointf();
		cntlPts = new myPointf[_cntlPts.length];
		origCntlPts = new myPointf[cntlPts.length];
		cntlPtLbls = new String[cntlPts.length];
		for(int i=0;i<cntlPts.length;++i) {	
			cntlPts[i]= new myPointf();
			origCntlPts[i] = new myPointf(_cntlPts[i]);
			cntlPtLbls[i]="" + ((char)(i+'A'));
			origCntlPtCOV._add(origCntlPts[i]);
		}
		origCntlPtCOV._div(origCntlPts.length);
		//set control points and initialize 
		setCntlPts(_cntlPts,_numCellPerSide);

	}//ctor	
	
	public baseMap(baseMap _otr) {
		win=_otr.win; pa=myDispWindow.pa;
		mapIdx = _otr.mapIdx;
		mapTypeIDX = _otr.mapTypeIDX;
		mapTitle = _otr.mapTitle + " cpy" ;
		mapTtlXOff = _otr.mapTtlXOff;
		mapTitleOffset = new myPointf(_otr.mapTitleOffset);
		polyColors = new int[_otr.polyColors.length][];
		for(int i=0;i<polyColors.length;++i) {
			polyColors[i]=new int[_otr.polyColors[i].length];
			System.arraycopy(_otr.polyColors[i], 0, polyColors[i], 0, polyColors[i].length);
		}
		gridColor = new int[polyColors[1].length];
		System.arraycopy(polyColors[1], 0, gridColor, 0, gridColor.length);

		cntlPtCOV = new myPointf(_otr.cntlPtCOV);
		currMseModCntlPt = null;
		currMseClkLocVec = null;
		
		//build ortho basis for map based on initial control points
		basisVecs = new myVectorf[_otr.basisVecs.length];
		for(int i=0;i<basisVecs.length;++i) {basisVecs[i]=new myVectorf(_otr.basisVecs[i]);}
		//build display-only ortho frame
		buildOrthoFrame();
		//points and labels for points
		origCntlPtCOV = new myPointf();
		cntlPts = new myPointf[_otr.cntlPts.length];
		origCntlPts = new myPointf[cntlPts.length];
		cntlPtLbls = new String[cntlPts.length];	
		for(int i=0;i<cntlPts.length;++i) {	
			cntlPts[i]= new myPointf();
			origCntlPts[i] = new myPointf(_otr.origCntlPts[i]);
			cntlPtLbls[i]="" + ((char)(i+'A'));	
			origCntlPtCOV._add(origCntlPts[i]);
		}
		origCntlPtCOV._div(origCntlPts.length);
		this.setOtrMap(_otr.otrMap);
		//set control points and initialize 
		setCntlPts(_otr.cntlPts,_otr.numCellsPerSide);
		
	}//copy ctor
	

	
	/**
	 * register another map to this map - this will transform the passed map to be as close as possible to this map and return the changes as parameters
	 * @param otrMap
	 */	
	public void findDifferenceToMe(baseMap otrMap, myVectorf dispBetweenMaps, float[] angleAndScale) {  
		myPointf A = cntlPtCOV, B = otrMap.cntlPtCOV; 
		myVectorf AP, BP,rBP;
		float sin = 0.0f, cos = 0.0f;
		//geometric avg of vector lengths from cov to cntlpts
		double scl = 1.0;
		for(int i=0;i<cntlPts.length;++i) {
		  AP = new myVectorf(A, cntlPts[i]);
		  BP = new myVectorf(B, otrMap.cntlPts[i]);
		  rBP = BP.rotMeAroundAxis(otrMap.basisVecs[0], MyMathUtils.halfPi_f);
		  cos += AP._dot(BP);
		  sin += AP._dot(rBP);
		  scl *= AP.magn/BP.magn;
		}
		angleAndScale[0] = (float) Math.atan2(sin,cos);
		dispBetweenMaps.set(new myVectorf(B,A));
		angleAndScale[1] = (float) Math.pow(scl, 1.0/cntlPts.length);
	}//findDifferenceToMe
	
	/**
	 * move this map to the passed values
	 * @param dispBetweenMaps
	 * @param angleAndScale
	 */
	public void registerMeToVals(myVectorf dispBetweenMaps, float[] angleAndScale) {
		shouldShareBranching = false;
		updateMapFromCntlPtVals(false);
		moveMapInPlane(dispBetweenMaps);		updateMapFromCntlPtVals(false);
		rotateMapInPlane(angleAndScale[0]);		updateMapFromCntlPtVals(false);
		dilateMap(angleAndScale[1]);			updateMapFromCntlPtVals(false);
	}
	
	
	/**
	 * call every time control points change programmatically (not are moved, but set/forced to some value)
	 * @param _cntlPts
	 * @param _numCellPerSide
	 */
	public void setCntlPts(myPointf[] _cntlPts) {setCntlPts(_cntlPts, false, numCellsPerSide);}
	public void setCntlPts(myPointf[] _cntlPts, int _numCellsPerSide) {setCntlPts(_cntlPts, true, _numCellsPerSide);}
	private void setCntlPts(myPointf[] _cntlPts, boolean reset, int _numCellsPerSide) {
		updateNumCellsPerSide(_numCellsPerSide);
		for(int i=0;i<cntlPts.length;++i) {		cntlPts[i].set(_cntlPts[i]);}
		updateMapFromCntlPtVals(reset);
	}	
	
	/**
	 * update control points from morph
	 */
	public void updateCntlPts(myPointf[] _delCntlPts) {		
		for(int i=0;i<cntlPts.length;++i) {		
			cntlPts[i]._add(_delCntlPts[i]);
			updateMapFromCntlPtVals(false);
		}
	}
	/**
	 * update map values from UI (non-cntl point values) and recalc poly points if necessary or forced
	 * @param _numCellsPerSide
	 * @param _branchSharingStrategy strategy for sharing angle branching, for cots maps.  
	 * 			0 : no branch sharing
	 * 			1 : force all branching to map A's branching
	 * 			2 : force all branching to map B's branching
	 * 			3 : force all branching to most recent edited map's branching
	 * @param forceUpdate : force poly point recalc
	 */
	 public void updateMapVals_FromUI(mapUpdFromUIData upd) {
		boolean changed = upd.forceUpdate();
			//update cell count if neccessary
		changed = updateNumCellsPerSide(upd.numCellsPerSide) || changed;
			//update branch sharing strategy, if necessary
		changed = updateBranchShareStrategy(upd.branchSharingStrategy) || changed;
			//any instancing map-dependent quantities/updates that need to occur
		changed = updateMapVals_FromUI_Indiv(upd) || changed;
			//if any changes occur, update all map values accordingly
		if(changed) {
			updateMapFromCntlPtVals(false);			
		}
	}	
	
	/**
	 * update the # of cells per side, and all other comparable quantities 
	 * @param _numCellsPerSide
	 * @return if changed or not
	 */
	private boolean updateNumCellsPerSide(int _numCellsPerSide) {
		if(numCellsPerSide == _numCellsPerSide) {return false;}
		numCellsPerSide = _numCellsPerSide;
		//subDivPerPoly = (int) ((numCellsPerSide -1)/(1.0*numTtlPtsPerEdge)) + 1;
		subDivPerPoly = numCellsPerSide/numTtlPtsPerEdge;
		//rebuild poly t values
		buildPolyPointTVals();
		return true;
	}
	
	private boolean updateBranchShareStrategy(int _currBranchShareStrategy) {
		if(currBranchShareStrategy == _currBranchShareStrategy) {return false;}
		currBranchShareStrategy = _currBranchShareStrategy;
		switch (currBranchShareStrategy) {
			case 0 : {shouldShareBranching = false; break;}
			case 1 : {shouldShareBranching = (mapIdx == 0); break;}
			case 2 : {shouldShareBranching = (mapIdx == 1); break;}
			case 3 : {shouldShareBranching = ((mapIdx == 0) || (mapIdx == 1)); break;}
		}		
		return true;
	}

	/**
	 * instance specific updates
	 * @param hasBeenUpdated whether this map has been updated already
	 * @return hasBeenUpdated, possibly changed to true if this method causes update
	 */
	protected abstract boolean updateMapVals_FromUI_Indiv(mapUpdFromUIData upd) ;	
	
	/**
	 * calculate mapped point given offets - tx and ty should be between 0 and 1
	 * @param tx
	 * @param ty
	 * @return
	 */
	public abstract myPointf calcMapPt(float tx, float ty);	
	
	/**
	 * calc center of area point and map title offset
	 */
	protected final void setCurrCntrlPtCOV() {
		cntlPtCOV.set(0,0,0);
		for(int i=0;i<cntlPts.length;++i) {		cntlPtCOV._add(cntlPts[i]);}
		cntlPtCOV._div(cntlPts.length);
		//find location for map title offest - directly above cntlPtCOA
		calcMapTitleOffset();
		//find edge points around polygon
		edgePts = buildEdgePoints();
	}
	
	protected abstract myPointf[][] buildEdgePoints();
	
	/**
	 * build all grid verts based on current control corner point positions
	 * @return
	 */
	protected final void updateMapFromCntlPtVals(boolean reset) {
		//instance-specifics
		updateMapFromCntlPtVals_Indiv( reset);	
		if((null != otrMap) && (shouldShareBranching)) {//share this map's branching with OTR map
			//force update of other map
			otrMap.updateMeWithMapVals(this);
		}
		//find center of control points
		setCurrCntrlPtCOV();
	}//calcPolyPoints
	
	/**
	 * update this map's derived values without recalculating values
	 * @param reset
	 */
	protected final void updateMapFromOtrMapVals(boolean reset) {
		updateMapFromCntlPtVals_Indiv(reset);		
		//find center of control points
		setCurrCntrlPtCOV();
	}
		
	public abstract void updateMeWithMapVals(baseMap otrMap);	
	
	/**
	 * Instance-class specific initialization
	 * @param reset whether internal state of inheriting map should be reset, if there is any
	 */
	protected abstract void updateMapFromCntlPtVals_Indiv(boolean reset);
	
	/**
	 * precalculate the tx,ty values for the grid poly bounds - only necessary when numCellsPerSide changes
	 */
	protected final void buildPolyPointTVals() {
		polyPointTVals = new float[numCellsPerSide+1][numCellsPerSide+1][2];			
		float tx;		
		for(int i=0;i<polyPointTVals.length;++i) {
			tx = i/(1.0f*numCellsPerSide);
			for(int j=0;j<polyPointTVals[i].length;++j) {
				polyPointTVals[i][j][0]=tx;
				polyPointTVals[i][j][1]=j/(1.0f*numCellsPerSide);	//ty			
			}
		}	
		edgePts = buildEdgePoints();
	}//buildPolyPointTVals
	

	///////////////////////
	// draw routines
	
	/**
	 * draw a point of a particular radius
	 * @param p point to draw
	 * @param rad radius of point
	 */
	protected void _drawPt(myPointf _p, float _rad) {
		pa.pushMatrix();pa.pushStyle();	
		pa.translate(_p);
		pa.sphere(_rad);
		pa.popStyle();pa.popMatrix();	
	}
	
	protected void _drawVec(myPointf _p, myPointf _pEnd, int[] _strkClr, float _rad) {
		pa.pushMatrix();pa.pushStyle();	
		pa.stroke(_strkClr[0],_strkClr[1],_strkClr[2],255);
		pa.line(_p.x, _p.y,_p.z,_pEnd.x, _pEnd.y,_pEnd.z);
		pa.translate(_pEnd);
		pa.sphere(_rad);
		pa.popStyle();pa.popMatrix();	
	}
		 
		
	/**
	 * draw the control points for this map
	 * @param pa
	 */
	private void _drawCntlPts(boolean isCurMap) {
		pa.sphereDetail(5);
		pa.stroke(0,0,0,255);
		for(int i=0;i<cntlPts.length;++i) {	myPointf p = cntlPts[i];_drawPt(p, (p.equals(currMseModCntlPt) && isCurMap ? 2.0f*sphereRad : sphereRad));}
		_drawPt(cntlPtCOV,sphereRad*1.5f);	
		//instance specific 
		_drawCntlPoints_Indiv(isCurMap);
		
	}//_drawPoints
	
	/**
	 * draw polygon around control points
	 */
	protected final void _drawCntlPtPoly() {
		pa.beginShape();
		for(int i=0;i<cntlPts.length;++i) {		pa.vertex(cntlPts[i].x,cntlPts[i].y,cntlPts[i].z);	}
		pa.endShape(PConstants.CLOSE);	
	}
	
	protected final void _drawPoly(int i, int j) {
		myPointf pt;
		float tx, ty = polyPointTVals[i][j][1];
		pa.beginShape();
		
		pa.normal(basisVecs[0].x, basisVecs[0].y, basisVecs[0].z);
		for (tx = polyPointTVals[i][j][0]; tx<polyPointTVals[i+1][j][0];tx+=subDivPerPoly) {
			pt = calcMapPt(tx, ty);
			pa.vertex(pt.x,pt.y,pt.z);
		}
		tx = polyPointTVals[i+1][j][0];					
		for (ty = polyPointTVals[i+1][j][1]; ty<polyPointTVals[i+1][j+1][1];ty+=subDivPerPoly) {
			pt = calcMapPt(tx, ty);
			pa.vertex(pt.x,pt.y,pt.z);
		}
		
		ty = polyPointTVals[i+1][j+1][1];
		for (tx = polyPointTVals[i+1][j+1][0]; tx>polyPointTVals[i][j+1][0];tx-=subDivPerPoly) {
			pt = calcMapPt(tx, ty);
			pa.vertex(pt.x,pt.y,pt.z);
		}
		tx = polyPointTVals[i][j+1][0];	
		for (ty = polyPointTVals[i][j+1][1]; ty>polyPointTVals[i][j][1];ty-=subDivPerPoly) {
			pt = calcMapPt(tx, ty);
			pa.vertex(pt.x,pt.y,pt.z);
		}
		pa.endShape(PConstants.CLOSE);	
	}
	
	protected final void _drawPolyTexture(int i, int j) {
		myPointf pt;
		float tx, ty = polyPointTVals[i][j][1];
		
		pa.beginShape();
		pa.texture(imageToMap);
		pa.normal(basisVecs[0].x, basisVecs[0].y, basisVecs[0].z);
		for (tx = polyPointTVals[i][j][0]; tx<polyPointTVals[i+1][j][0];tx+=subDivPerPoly) {
			pt = calcMapPt(tx, ty);
			pa.vertex(pt.x,pt.y,pt.z, tx, ty);
		}
		tx = polyPointTVals[i+1][j][0];					
		for (ty = polyPointTVals[i+1][j][1]; ty<polyPointTVals[i+1][j+1][1];ty+=subDivPerPoly) {
			pt = calcMapPt(tx, ty);
			pa.vertex(pt.x,pt.y,pt.z, tx, ty);
		}
		
		ty = polyPointTVals[i+1][j+1][1];
		for (tx = polyPointTVals[i+1][j+1][0]; tx>polyPointTVals[i][j+1][0];tx-=subDivPerPoly) {
			pt = calcMapPt(tx, ty);
			pa.vertex(pt.x,pt.y,pt.z, tx, ty);
		}
		tx = polyPointTVals[i][j+1][0];	
		for (ty = polyPointTVals[i][j+1][1]; ty>polyPointTVals[i][j][1];ty-=subDivPerPoly) {
			pt = calcMapPt(tx, ty);
			pa.vertex(pt.x,pt.y,pt.z, tx, ty);
		}
		pa.endShape(PConstants.CLOSE);	
	}
	
	/**
	 * this will draw lines linking both maps at corresponding points
	 */
	protected final void _drawLineBtwnMapEdges() {
		if((null==edgePts) ||(edgePts.length==0)|| (null==edgePts[0])){return;}
		int clrIdx = 0;
		for(int i=0;i<edgePts.length;++i) {
			clrIdx = i % 2;
			pa.setStroke(polyColors[clrIdx], polyColors[clrIdx][3]);
			for(int j=0;j<edgePts[i].length;++j) {
				myPointf a = edgePts[i][j];
				myPointf b = otrMap.edgePts[i][j];
				pa.line(a,b);
				//pa.line(edgePts[i][j], otrMap.edgePts[i][j]);
			}
		}		
	}
	
	
	/**
	 * draw deformed circles within cells
	 */	
	protected void _drawPolyCircles() {
		pa.pushMatrix();pa.pushStyle();	
		pa.setStrokeWt(2.0f);
		pa.noFill();
		float r = 1.f/(1.0f*numCellsPerSide), tx, ty, halfR = r/2.0f;
		float circInterp = .12f;
		myPointf pt;
		for(int i=0;i<polyPointTVals.length-1;++i) {
			float ri = r*i;
			for(int j=0;j<polyPointTVals[i].length-1;++j) {
				float rj = r * j;
				pa.beginShape();
			    for(float u=0; u<MyMathUtils.twoPi_f; u+=circInterp) {
			    	tx=(float) (halfR + ri + halfR*Math.cos(u));
			    	ty=(float) (halfR + rj + halfR*Math.sin(u));
					pt = calcMapPt(tx, ty);
					pa.vertex(pt.x,pt.y,pt.z);			    
			    }
				pa.endShape(PConstants.CLOSE);	
			}	
		}
		pa.popStyle();pa.popMatrix();
	}//_drawMapCircles
	
	public void drawMap_Texture() {
		pa.pushMatrix();pa.pushStyle();	
		pa.noFill();
		pa.noStroke();
		for(int i=0;i<polyPointTVals.length-1;++i) {for(int j=0;j<polyPointTVals[i].length-1;++j) {		_drawPolyTexture(i,j);		}}			
		pa.popStyle();pa.popMatrix();
	}
	
	public void drawMap_CntlPts(boolean isCurMap) {
		pa.pushMatrix();pa.pushStyle();	
		_drawCntlPts(isCurMap);
		pa.popStyle();pa.popMatrix();
	}
	
	public void drawMap_Fill() {
		pa.pushMatrix();pa.pushStyle();	
		pa.stroke(255,255,255,255);
		pa.setStrokeWt(1.0f);
		int clrIdx = 0;
		
		for(int i=0;i<polyPointTVals.length-1;++i) {
			clrIdx = i % 2;
			for(int j=0;j<polyPointTVals[i].length-1;++j) {
				pa.setFill(polyColors[clrIdx], polyColors[clrIdx][3]);
				_drawPoly(i,j);		
				clrIdx = (clrIdx + 1) % 2;
			}				
		}	
		pa.popStyle();pa.popMatrix();
	}//drawMap_Fill
	
	public void drawMap_PolyCircles_Fill() {
		pa.pushMatrix();pa.pushStyle();	
		pa.stroke(255,255,255,255);
		pa.setStrokeWt(1.0f);
		_drawPolyCircles();
		pa.popStyle();pa.popMatrix();
	}
		
	public void drawMap_Wf() {
		pa.pushMatrix();pa.pushStyle();	
		pa.noFill();
		pa.setStroke(gridColor, gridColor[3]);
		pa.setStrokeWt(1.5f);	
		for(int i=0;i<polyPointTVals.length-1;++i) {for(int j=0;j<polyPointTVals[i].length-1;++j) {_drawPoly(i,j);}}	
		pa.popStyle();pa.popMatrix();
	}//drawMap_Wf
	
	public void drawMap_PolyCircles_Wf() {
		pa.pushMatrix();pa.pushStyle();	
		pa.noFill();
		pa.setStroke(gridColor, gridColor[3]);
		pa.setStrokeWt(1.5f);	
		_drawPolyCircles();
		pa.popStyle();pa.popMatrix();		
	}
	
	public void drawMap_EdgeLines() {
		if(null==otrMap) {return;}
		pa.pushMatrix();pa.pushStyle();	
		pa.noFill();
		pa.setStrokeWt(1.5f);	
		//draw lines from this map's edges to other map's edges
		_drawLineBtwnMapEdges();
		pa.popStyle();pa.popMatrix();		
	}
	
	/**
	 * draw orthogonal frame - red is normal to map plane
	 */
	public void drawOrthoFrame() {
		pa.pushMatrix();pa.pushStyle();	
			cntlPtCOV.showMeSphere(pa, 5.0f);
			pa.strokeWeight(3.0f);
			pa.translate(cntlPtCOV);
			pa.stroke(255,0,0,255);
			pa.line(0,0,0, orthoFrame[0].x, orthoFrame[0].y, orthoFrame[0].z);
			pa.stroke(0,255,0,255);
			pa.line(0,0,0, orthoFrame[1].x, orthoFrame[1].y, orthoFrame[1].z);
			pa.stroke(0,0,255,255);
			pa.line(0,0,0, orthoFrame[2].x, orthoFrame[2].y, orthoFrame[2].z);
			for(int i=0;i<orthoFrame.length;++i) {	orthoFrame[i].showMeSphere(pa, 6.0f);}
		pa.popStyle();pa.popMatrix();				
	}//_drawOrthoFrame
	

	public void drawHeaderAndLabels(boolean _drawLabels) {
		pa.pushMatrix();pa.pushStyle();
		pa.fill(0,0,0,255);
		pa.stroke(0,0,0,255);
		win._drawLabelAtPt(mapTitleOffset, mapTitle, 0.0f, 0.0f);
		if(_drawLabels) {
			for(int i=0; i< cntlPts.length;++i) {	win._drawLabelAtPt(cntlPts[i],cntlPtLbls[i] + "_"+mapIdx + " : " + cntlPts[i].toStrBrf(), 2.5f,-2.5f);}
			win._drawLabelAtPt(cntlPtCOV,"Center of Cntrl Pts : " + cntlPtCOV.toStrBrf(), 2.5f,-2.5f); 
			_drawPointLabels_Indiv();
		}
		pa.popStyle();pa.popMatrix();
	}//drawHeaderAndLabels
	

	/**
	 * draw right side map description
	 */
	public final float drawRightSideBarMenuDescr(float yOff, float sideBarYDisp, boolean showTitle) {
		if(showTitle) {
			pa.pushMatrix();pa.pushStyle();
				pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Green, 255), 6.5f, mapTitle);
				pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 4.5f, " | # Cells Per Side : ");
				pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Green, 255), 6.5f, ""+numCellsPerSide);
				if (otrMap!=null) {
					pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 5.5f, " | Other Map : ");
					pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Green, 255), 6.5f, otrMap.mapTitle);
				}

			pa.popStyle();pa.popMatrix();
			yOff += sideBarYDisp;			pa.translate(0.0f,sideBarYDisp, 0.0f);
			pa.pushMatrix();pa.pushStyle();
				pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 5.5f, "Shares branching on edit with other map ? ");
				pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Yellow, 255), 4.5f,  ""+shouldShareBranching);
			pa.popStyle();pa.popMatrix();
		} else {
			pa.pushMatrix();pa.pushStyle();
				pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 4.5f, "# Cells Per Side : ");
				pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Green, 255), 6.5f, ""+numCellsPerSide);
			pa.popStyle();pa.popMatrix();
		}
		yOff += sideBarYDisp;		pa.translate(20.0f,sideBarYDisp, 0.0f);
		pa.pushMatrix();pa.pushStyle();
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 5.5f, "Centroid :  ");
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_LightCyan, 255), 4.5f,  cntlPtCOV.toStrBrf());
		pa.popStyle();pa.popMatrix();
		yOff += sideBarYDisp;		pa.translate(0.0f,sideBarYDisp, 0.0f);
		for(int i=0;i<cntlPts.length;++i) {
			pa.pushMatrix();pa.pushStyle();
				pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 5.5f, "Cntl Pt " + i + " : ");
				pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_LightCyan, 255), 4.5f, cntlPts[i].toStrBrf());
			pa.popStyle();pa.popMatrix();
			yOff += sideBarYDisp;			pa.translate(0.0f,sideBarYDisp, 0.0f);
		}		
		pa.translate(-20.0f,sideBarYDisp, 0.0f);
		
		yOff = drawRightSideBarMenuDescr_Indiv(yOff,sideBarYDisp);
		yOff += sideBarYDisp;		pa.translate(0.0f,sideBarYDisp, 0.0f);	
		
		return yOff;
	}
	protected abstract float drawRightSideBarMenuDescr_Indiv(float yOff, float sideBarYDisp);
	
	/**
	 * instance-specific point drawing
	 * @param pa
	 */
	protected abstract void _drawCntlPoints_Indiv(boolean isCurMap);
	protected abstract void _drawPointLabels_Indiv();

	
	/**
	 * 3D only : find point in this map's plane where passed line intersects
	 * @param _rayOrigin point origin of parametric line eq
	 * @param _rayDir direction of line - assumed to be unit length
	 * @return point of intersection in this plane
	 */
	public myPointf findPointInMyPlane(myPointf _rayOrigin, myVectorf _rayDir) {
		myVectorf dispVec = new myVectorf(otrMap.origCntlPtCOV,origCntlPtCOV);
		myVectorf _unitDispVec = dispVec._normalized();
		float cosThet = _rayDir._dot(_unitDispVec);// sinThet = (float) Math.sqrt(1-(cosThet * cosThet));
		if(cosThet == 0) {System.out.println("zero denom");return new myPointf();}//degenerate - ray is coplanar with plane of map
		//parallel component of _rayDir is dispVec, want to calculate perp component
		float newMag = dispVec.magn/cosThet;		
		myVectorf rayDirMult = myVectorf._mult(_rayDir, newMag);
		myPointf resPt = myPointf._add(_rayOrigin, 1.0f, rayDirMult);	
		return 	resPt;
	}
	  
	/**
	 * this will calculate normalized barycentric coordinates for pt w/respect to first 3 control points of poly
	 * @param pt
	 * @return
	 */
	public float[] calcNormBaryCoords(myPointf pt) {return pt.calcNormBaryCoords(cntlPts);}
	public myPointf calcPointFromNormBaryCoords(float[] coords) {return myPointf.calcPointFromNormBaryCoords(cntlPts,coords);}

	
	////////////////////////
	// mouse interaction - need to determine which map control point of both maps is closest to mouse location

	
	/**
	 * returns distance to closest point to passed line/ray or _mseLoc point, depending on whether 3d or 2d
	 * @param _mseLoc mouse cick location
	 * @param _rayOrigin origin of click ray, if 3d (zero vec in 2d)
	 * @param _rayDir direction of click ray, if 3d (FORWARD vec in 2d)
	 * @return
	 */
	public final Float findClosestCntlPt(myPointf _mseLoc, myPointf _rayOrigin, myVectorf _rayDir){
		currMseClkLocVec = new myVectorf(cntlPtCOV, _mseLoc);
		TreeMap<Float, myPointf> ptsByDist = new TreeMap<Float, myPointf>();
		for(int i=0;i<cntlPts.length;++i) {		ptsByDist.put(win.findDistToPtOrRay(_mseLoc, cntlPts[i],_rayOrigin,_rayDir), cntlPts[i]);}		
		ptsByDist.put(win.findDistToPtOrRay(_mseLoc, cntlPtCOV, _rayOrigin,_rayDir), cntlPtCOV);	
		findClosestCntlPt_Indiv( _mseLoc,_rayOrigin, _rayDir,ptsByDist);
		Float leastKey = ptsByDist.firstKey();
		currMseModCntlPt = ptsByDist.get(leastKey);
		return leastKey;		
	}//findClosestCntlPt
	
	protected abstract void findClosestCntlPt_Indiv(myPointf _mseLoc, myPointf _rayOrigin, myVectorf _rayDir, TreeMap<Float, myPointf> ptsByDist);
	
	/**
	 * 0 needs to vanish, no negatives, either <1 which shrinks, or >1 which grows
	 * @param amt
	 */
	private final void dilateMap_MseDrag(myVectorf mseDragInWorld_f) {
		myVectorf currMseClkLocVecNorm = currMseClkLocVec._normalized();
		myVectorf deltaVec = currMseClkLocVecNorm._mult(currMseClkLocVecNorm._dot(mseDragInWorld_f)), dispVec = myVectorf._add(currMseClkLocVec, deltaVec);
		
		float amt = dispVec.sqMagn/currMseClkLocVec.sqMagn;
		//System.out.println("dilateMap : amt : " + amt + " deltavec : " + deltaVec.toStrBrf() + " mseDragInWorld_f : " + mseDragInWorld_f.toStrBrf() + " | currMseClkLocVec : " + currMseClkLocVec.toStrBrf());
		if(amt < .0f) {amt=.01f;}		//TODO need different mechanism to bound dilation
		dilateMap(amt);
	}//dilateMap
	
	private final float minDilate = 10.0f;
	public final void dilateMap(float amt) {
		//scale all points toward COV
		for(int i=0;i<cntlPts.length;++i) {
			myVectorf COV_ToCntlPt = new myVectorf(cntlPtCOV, cntlPts[i]);
			myVectorf dispVec = myVectorf._mult(COV_ToCntlPt, amt);
			if(dispVec.magn < minDilate) {dispVec._mult(minDilate/dispVec.magn);}
			cntlPts[i] = myPointf._add(cntlPtCOV, dispVec);
		}
		dilateMap_Indiv(amt);
	}
	protected abstract void dilateMap_Indiv(float amt);

	private final void rotateMapInPlane_MseDrag(myPointf clckPt, myVectorf deltaVec) {		
		myVectorf vecToPoint = new myVectorf(cntlPtCOV,clckPt)._normalize();
		myVectorf rotVec = myVectorf._cross(vecToPoint, deltaVec);		
		rotateMapInPlane(rotVec._dot(basisVecs[0])*rotScl);
		
	}//rotateMapInPlane
	
	/**
	 * callable internally or by UI
	 * @param thet angle to rotate, in radians
	 */
	public final void rotateMapInPlane(float thet) {
		//rotate all control points in plane by thet around basisVecs[0]
		for(int i=0;i<cntlPts.length;++i) {
			myVectorf COV_ToCntlPt = new myVectorf(cntlPtCOV, cntlPts[i]),
					rotVec = COV_ToCntlPt.rotMeAroundAxis(basisVecs[0], thet);
			cntlPts[i] = myPointf._add(cntlPtCOV, rotVec);			
		}
		rotateMapInPlane_Indiv(thet);
	}//rotateMapInPlane
	protected abstract void rotateMapInPlane_Indiv(float thet);
	
	/**
	 * move the map in the plane by passed deflection vector
	 * @param defVec coplanar movement vector
	 */
	public final void moveMapInPlane(myVectorf defVec) {for(int i=0;i<cntlPts.length;++i) {	cntlPts[i]._add(defVec);}	moveMapInPlane_Indiv(defVec);}
	protected abstract void moveMapInPlane_Indiv(myVectorf defVec);
	protected abstract void moveCntlPtInPlane_Indiv(myVectorf defVec);
		
	/**
	 * handle mouse drag on map
	 * @param defVec
	 * @param mseClickIn3D_f
	 * @param key
	 * @param keyCode
	 */
	public final void mseDragInMap(myVectorf defVec, myPointf mseClickIn3D_f,  char key, int keyCode) {		
		boolean isScale = (key=='s') || (key=='S'), isRotation = (key=='r') || (key=='R');
		if(isScale || isRotation) {
			if(isScale) {				dilateMap_MseDrag(defVec);			} 
			else {			rotateMapInPlane_MseDrag(mseClickIn3D_f, defVec);		}	//isRotation		
		} else {							//cntl point movement
			if(currMseModCntlPt==null) {return;}	
			currMseModCntlPt._add(defVec);	
			if(currMseModCntlPt.equals(cntlPtCOV)) {moveMapInPlane(defVec);	}	
			else {moveCntlPtInPlane_Indiv(defVec);}
		}
		mseDragInMap_Indiv(defVec,mseClickIn3D_f,isScale, isRotation, key, keyCode);			
		updateMapFromCntlPtVals(false);
	}//mseDragInMap
	
	protected abstract void mseDragInMap_Indiv(myVectorf defVec, myPointf mseClickIn3D_f,boolean isScale,boolean isRotation,  char key, int keyCode);
	
	/**
	 * code for whenever mouse release is executed
	 */
	public final void mseRelease() {
		updateMapFromCntlPtVals(false);
		currMseModCntlPt = null;
		currMseClkLocVec = null;
		mseRelease_Indiv();
	}
	
	protected abstract void mseRelease_Indiv();

	
	/////////////////////////
	// setters/getters
	
	public final baseMap getOtrMap() {return otrMap;}
	public final void setOtrMap(baseMap _otr) {		otrMap = _otr;  setOtrMap_Indiv();}
	protected abstract void setOtrMap_Indiv();
	
	public final int getNumCellsPerSide() {			return numCellsPerSide;}
	public final int[][] getPolyColors() {			return polyColors;}
	public final int[] getGridColor() {				return gridColor;}	
	public final myPointf getCOV() {				return cntlPtCOV;}
	
	/**
	 * if map has center point i.e. spiral, this will return it, otherwise it returns the COV
	 * @return
	 */
	public abstract myPointf getCenterPoint();
	
	
	public final myPointf[] getCntlPts() {			return cntlPts;}
	public final myPointf[] getCntlPts_Copy() {			
		myPointf[] cpyPts = new myPointf[cntlPts.length];
		for(int i=0;i<cpyPts.length;++i) {	cpyPts[i]= new myPointf(cntlPts[i]);}		
		return cpyPts;}
	public final myPointf[] getCntlPtDiagonal() {	return new myPointf[] {cntlPts[0],cntlPts[2]};}

	public final void setPolyColors(int[][] _pClrs) {polyColors = _pClrs;}
	public final void setGridColor(int[] _gClr) {gridColor = _gClr;}	
	
	public final void setImageToMap(PImage _img) {	imageToMap=_img;	}	
	
	
	protected final void printOutAllCntlPts() {
		String debug = "";
	    for(int i=0;i<cntlPts.length;++i) {	    	debug +="\n\t"+cntlPts[i].toStrBrf();	    }
	    System.out.println(debug);
	}
	/**
	 * calc appropriate location for map title, based on map deformation
	 */
	protected void calcMapTitleOffset() {
		myPointf tmpCtrPt = new myPointf(cntlPts[0],.5f,cntlPts[1]);
		myVectorf ttlOff = new myVectorf(cntlPtCOV,tmpCtrPt);
		myVectorf ttlOffNorm = ttlOff._normalized(),
				ttlToA = new myVectorf(tmpCtrPt, cntlPts[0]), 
				ttlToB = new myVectorf(tmpCtrPt, cntlPts[1]);
		//length of projection of difference between a and ctr pt between a and b on vector from COA to ctr pt between a and b
		float distA = ttlToA._dot(ttlOffNorm), distB = ttlToB._dot(ttlOffNorm), dist = (distA > distB ? distA : distB);
		
		myVectorf ttlToACoLin = myVectorf._mult(ttlOffNorm, dist),
				ttlPerp = myVectorf._sub(ttlToA, ttlToACoLin)._normalize()._mult(mapTtlXOff);
		mapTitleOffset.set(myVectorf._add(myVectorf._add(myVectorf._add(cntlPtCOV ,ttlOff), myVectorf._mult(ttlOffNorm, 1.0f*(dist + 20.0f))), ttlPerp));
	
	}
	
	/**
	 * build orthonormal basis from the passed normal (unit)
	 * @param tmpNorm : normal 
	 * @return ortho basis
	 */
	protected myVectorf[] buildBasisVecs(myVectorf tmpNorm) {
		myVectorf[] basisVecs = new myVectorf[3];
		//build basis vectors
		basisVecs[0] = tmpNorm._normalized();
		if(basisVecs[0]._dot(myVectorf.FORWARD) == 1.0f) {//if planeNorm is in x direction means plane is y-z, so y axis will work as basis
			basisVecs[1] = new myVectorf(myVectorf.UP);
			basisVecs[1]._normalize();
			basisVecs[2] = new myVectorf(myVectorf.RIGHT);
			basisVecs[2]._normalize();		
		
			//System.out.println(mapTitle+" Using up");
		} else {
			basisVecs[1] = basisVecs[0]._cross(myVectorf.FORWARD);
			basisVecs[1]._normalize();

			basisVecs[2] = basisVecs[1]._cross(basisVecs[0]);
			basisVecs[2]._normalize();		
	
		}
		return basisVecs;
	}

	/**
	 * build ortho frame to display basis vecs
	 */
	private void buildOrthoFrame() {
		//display-only ortho frame
		orthoFrame = new myPointf[basisVecs.length];		
		for(int i=0;i<orthoFrame.length;++i) {orthoFrame[i]= myPointf._add(myPointf.ZEROPT, 200.0f, basisVecs[i]);}		
	}

	
}//class baseMap
