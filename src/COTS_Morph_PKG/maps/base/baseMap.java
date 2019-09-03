package COTS_Morph_PKG.maps.base;

import java.util.TreeMap;

import COTS_Morph_PKG.ui.base.COTS_MorphWin;
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
	 * from cov to point first clicked on in map space
	 */
	protected myVectorf currMseClkLocVec;
	/**
	 * point at center of cntrl points - "center" of map verts; control point currently being moved/modified by UI interaction
	 */
	protected myPointf cntlPtCOV, currMseModCntlPt;
		
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
	
		//text scale value for display
	private static final float txtSclVal = 1.25f;	
		//rotational UI mod scale value
	private static final float rotScl = .0025f;
		//draw scale in 3D
	private static final float mseDrag3DScl = 1.5f;
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
		gridColor = new int[4];
		System.arraycopy(polyColors[1], 0, gridColor, 0, gridColor.length);

		cntlPtCOV = new myPointf(0,0,0);
		currMseModCntlPt = null;
		currMseClkLocVec = null;
		
		//build ortho basis for map based on initial control points
		basisVecs = buildBasisVecs(myVectorf._cross(new myVectorf(_cntlPts[0], _cntlPts[1]), new myVectorf(_cntlPts[0], _cntlPts[3]))._normalize());
		//build display-only ortho frame
		buildOrthoFrame();
		//labels for points
		cntlPts = new myPointf[_cntlPts.length];
		origCntlPts = new myPointf[_cntlPts.length];
		cntlPtLbls = new String[_cntlPts.length];		
		for(int i=0;i<_cntlPts.length;++i) {	
			cntlPts[i]= new myPointf();
			origCntlPts[i] = new myPointf(_cntlPts[i]);
			cntlPtLbls[i]="" + ((char)(i+'A'));	
		}
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
		gridColor = new int[4];
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
		cntlPts = new myPointf[_otr.cntlPts.length];
		origCntlPts = new myPointf[cntlPts.length];
		cntlPtLbls = new String[cntlPts.length];		
		for(int i=0;i<cntlPts.length;++i) {	
			cntlPts[i]= new myPointf();
			origCntlPts[i] = new myPointf(_otr.origCntlPts[i]);
			cntlPtLbls[i]="" + ((char)(i+'A'));	
		}
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
		moveMapInPlane(dispBetweenMaps);
		updateMapFromCntlPtVals(false);
		rotateMapInPlane(angleAndScale[0]);
		updateMapFromCntlPtVals(false);
		dilateMap(angleAndScale[1]);
		updateMapFromCntlPtVals(false);
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
	public void updateMapVals(int _numCellsPerSide, int _branchSharingStrategy, boolean forceUpdate) {
		boolean changed = forceUpdate;
		//update cell count if neccessary
		changed = updateNumCellsPerSide(_numCellsPerSide) || changed;
		//update branch sharing strategy, if necessary
		changed = updateBranchShareStrategy(_branchSharingStrategy) || changed;
		//any instancing map-dependent quantities/updates that need to occur
		changed = updateMapVals_Indiv() || changed;
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
	protected abstract boolean updateMapVals_Indiv() ;	
	
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
	protected final void setCurrCntrlPtCOA() {
		cntlPtCOV.set(0,0,0);
		for(int i=0;i<cntlPts.length;++i) {		cntlPtCOV._add(cntlPts[i]);}
		cntlPtCOV._div(cntlPts.length);
		//find location for map title offest - directly above cntlPtCOA
		calcMapTitleOffset();
	}
	
	
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
		setCurrCntrlPtCOA();
	}//calcPolyPoints
	
	/**
	 * update this map's derived values without recalculating values
	 * @param reset
	 */
	protected final void updateMapFromOtrMapVals(boolean reset) {
		updateMapFromCntlPtVals_Indiv(reset);		
		//find center of control points
		setCurrCntrlPtCOA();
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
				polyPointTVals[i][j][1]=j/(1.0f*numCellsPerSide);				
			}
		}	
	}//buildPolyPointTVals
	

	///////////////////////
	// draw routines
	
	/**
	 * draw a point of a particular radius
	 * @param p point to draw
	 * @param rad radius of point
	 */
	protected void _drawPt(myPointf p, float rad) {
		pa.pushMatrix();pa.pushStyle();	
		pa.translate(p);
		pa.sphere(rad);
		pa.popStyle();pa.popMatrix();	
	}
		 
		
	/**
	 * draw the control points for this map
	 * @param pa
	 */
	private void _drawCntlPts(boolean isCurMap) {
		pa.sphereDetail(5);
		pa.stroke(0,0,0,255);
		for(myPointf p : cntlPts) {		_drawPt(p, (p.equals(currMseModCntlPt) && isCurMap ? 2.0f*sphereRad : sphereRad));}
		_drawPt(cntlPtCOV,sphereRad*1.5f);	
		//instance specific 
		_drawPoints_Indiv();
		
	}//_drawPoints
	
	
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
	 * draw deformed circles within cells
	 */	
	private void _drawPolyCircles() {
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
	
	public void drawMap_Texture(boolean isCurMap) {
		pa.pushMatrix();pa.pushStyle();	
		pa.noFill();
		pa.noStroke();
		for(int i=0;i<polyPointTVals.length-1;++i) {			
			for(int j=0;j<polyPointTVals[i].length-1;++j) {
				_drawPolyTexture(i,j);				
			}				
		}			
		pa.popStyle();pa.popMatrix();
	}
	
	public void drawMap_CntlPts(boolean isCurMap) {
		pa.pushMatrix();pa.pushStyle();	
		_drawCntlPts(isCurMap);
		pa.popStyle();pa.popMatrix();
	}
	
	public void drawMap_Fill(boolean isCurMap) {
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
		
	public void drawMap_Wf(boolean isCurMap) {
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
	
	/**
	 * draw orthogonal frame - red is normal to map plane
	 */
	public void drawOrthoFrame() {
		pa.pushMatrix();pa.pushStyle();	
			cntlPtCOV.showMeSphere(pa, 5.0f);
			pa.strokeWeight(3.0f);
			pa.stroke(255,0,0,255);
			pa.translate(cntlPtCOV);
			pa.line(myPointf.ZEROPT, orthoFrame[0]);
			pa.stroke(0,255,0,255);
			pa.strokeWeight(3.0f);
			pa.line(myPointf.ZEROPT, orthoFrame[1]);
			pa.stroke(0,0,255,255);
			pa.strokeWeight(3.0f);
			pa.line(myPointf.ZEROPT, orthoFrame[2]);
			for(int i=0;i<orthoFrame.length;++i) {	orthoFrame[i].showMeSphere(pa, 6.0f);}
		pa.popStyle();pa.popMatrix();				
	}//_drawOrthoFrame
	
	protected void _drawLabelAtPt_UnSetCam(myDispWindow animWin, myPointf p, String lbl, float xOff, float yOff) {
		pa.pushMatrix();pa.pushStyle();	
		pa.translate(p);
		animWin.unSetCamOrient();
		pa.scale(txtSclVal);
		pa.text(lbl, xOff,yOff,0); 
		pa.popStyle();pa.popMatrix();
	}

	protected void _drawLabelAtPt(myPointf p, String lbl, float xOff, float yOff) {
		pa.pushMatrix();pa.pushStyle();	
		pa.translate(p);
		pa.scale(txtSclVal);
		pa.text(lbl, xOff,yOff,0); 
		pa.popStyle();pa.popMatrix();
	}

	public void drawHeaderAndLabels_2D(boolean _drawLabels) {
		pa.pushMatrix();pa.pushStyle();
		pa.fill(0,0,0,255);
		pa.stroke(0,0,0,255);
		_drawLabelAtPt(mapTitleOffset, mapTitle, 0.0f, 0.0f);
		if(_drawLabels) {
			for(int i=0; i< cntlPts.length;++i) {	_drawLabelAtPt(cntlPts[i],cntlPtLbls[i] + "_"+mapIdx + " : " + cntlPts[i].toStrBrf(), 2.5f,-2.5f);}
			_drawLabelAtPt(cntlPtCOV,"Center of Cntrl Pts : " + cntlPtCOV.toStrBrf(), 2.5f,-2.5f); 
			_drawPointLabels_2D_Indiv();
		}
		pa.popStyle();pa.popMatrix();
	}//drawLabels_2D
	
	public void drawHeaderAndLabels_3D(boolean _drawLabels, myDispWindow animWin) {
		pa.pushMatrix();pa.pushStyle();
		pa.fill(0,0,0,255);
		pa.stroke(0,0,0,255);
		_drawLabelAtPt_UnSetCam( animWin, mapTitleOffset, mapTitle, 0.0f, 0.0f);
		if(_drawLabels) {
			for(int i=0; i< cntlPts.length;++i) {	_drawLabelAtPt_UnSetCam(animWin, cntlPts[i],cntlPtLbls[i] + "_"+mapIdx + " : " + cntlPts[i].toStrBrf(), 2.5f,-2.5f); }			
			_drawLabelAtPt_UnSetCam(animWin,cntlPtCOV,"Center of Cntrl Pts", 2.5f,-2.5f);
			_drawPointLabels_3D_Indiv(animWin);
		}
		pa.popStyle();pa.popMatrix();		
	}//drawLabels_3D
	
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
	protected abstract void _drawPoints_Indiv();
	protected abstract void _drawPointLabels_2D_Indiv();
	protected abstract void _drawPointLabels_3D_Indiv(myDispWindow animWin);

	
	////////////////////////
	// mouse interaction - need to determine which map control point of both maps is closest to mouse location

	
	/**
	 * returns the distance to the closest point to the mouse location
	 * @return
	 */
	public final Float findClosestCntlPt_2D(myPointf _mseLoc){
		currMseClkLocVec = new myVectorf(cntlPtCOV, _mseLoc);
		TreeMap<Float, Integer> res = new TreeMap<Float, Integer>();
		for(int i=0;i<cntlPts.length;++i) {			res.put(myPointf._SqrDist(cntlPts[i], _mseLoc), i);		}
		res.put(myPointf._SqrDist(cntlPtCOV, _mseLoc), -1);	
		Float leastKey = res.firstKey();
		Integer ptIDX = res.get(leastKey);
		currMseModCntlPt = (ptIDX >= 0) ? cntlPts[res.get(leastKey)] : cntlPtCOV;
		return leastKey;		
	}//findClosestCntlPt
	
	/**
	 * returns distance to closest point to passed line/ray
	 * @param _rayOrigin mouse point
	 * @param _rayDir unit direction vector in direction of mouse point into screen
	 * @return
	 */
	public final Float findClosestCntlPt_3D(myPoint _mseLoc, myPointf _rayOrigin, myVectorf _rayDir){
		currMseClkLocVec = new myVectorf(cntlPtCOV,new myPointf(_mseLoc.x,_mseLoc.y,_mseLoc.z));
		TreeMap<Float, Integer> res = new TreeMap<Float, Integer>();
		myVectorf vecToPt;
		float proj;
		for(int i=0;i<cntlPts.length;++i) {
			vecToPt = new myVectorf(_rayOrigin, cntlPts[i]);
			proj = vecToPt._dot(_rayDir);
			float dist = myVectorf._sub(vecToPt, myVectorf._mult(_rayDir, proj)).sqMagn;
			res.put(dist, i);					
		}
		
		vecToPt = new myVectorf(_rayOrigin, cntlPtCOV);
		proj = vecToPt._dot(_rayDir);
		float dist = myVectorf._sub(vecToPt, myVectorf._mult(_rayDir, proj)).sqMagn;
		res.put(dist, -1);	
		
		Float leastKey = res.firstKey();
		Integer ptIDX = res.get(leastKey);
		currMseModCntlPt = (ptIDX >= 0) ? cntlPts[res.get(leastKey)] : cntlPtCOV;		
		return leastKey;		
	}//findClosestCntlPt
	
	/**
	 * 0 needs to vanish, no negatives, either <1 which shrinks, or >1 which grows
	 * @param amt
	 */
	private final void dilateMap_MseDrag(myVectorf mseDragInWorld_f) {
		myVectorf currMseClkLocVecNorm = currMseClkLocVec._normalized();
		myVectorf deltaVec = currMseClkLocVecNorm._mult(currMseClkLocVecNorm._dot(mseDragInWorld_f)), dispVec = myVectorf._add(currMseClkLocVec, deltaVec);
		
		float amt = dispVec.sqMagn/currMseClkLocVec.sqMagn;
		//System.out.println("dilateMap : amt : " + amt + " deltavec : " + deltaVec.toStrBrf() + " mseDragInWorld_f : " + mseDragInWorld_f.toStrBrf() + " | currMseClkLocVec : " + currMseClkLocVec.toStrBrf());
		if(amt < .01f) {amt=.01f;}
		dilateMap(amt);
	}//dilateMap
	
	public final void dilateMap(float amt) {
		//scale all points toward COV
		for(int i=0;i<cntlPts.length;++i) {
			myVectorf COV_ToCntlPt = new myVectorf(cntlPtCOV, cntlPts[i]);
			cntlPts[i] = myPointf._add(cntlPtCOV, myVectorf._mult(COV_ToCntlPt, amt));
		}
	}

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
	
	}//rotateMapInPlane
	
	/**
	 * move the map in the plane by passed deflection vector
	 * @param defVec coplanar movement vector
	 */
	public final void moveMapInPlane(myVectorf defVec) {for(int i=0;i<cntlPts.length;++i) {	cntlPts[i]._add(defVec);	}	}
	
	
	public final void mseDrag_2D(int mouseX, int mouseY, float delX, float delY,myVector mseDragInWorld,char key, int keyCode) {
		boolean isScale = (key=='s') || (key=='S'), isRotation = (key=='r') || (key=='R');
		myVectorf mseDragInWorld_f = new myVectorf(mseDragInWorld);
		if(isScale || isRotation) {
			if(isScale) {				dilateMap_MseDrag(mseDragInWorld_f);			} 
			else {			rotateMapInPlane_MseDrag(new myPointf(mouseX, mouseY, 0), mseDragInWorld_f);		}	//isRotation		
		} else {							//cntl point movement
			if(currMseModCntlPt==null) {return;}
			myVectorf defVec = new myVectorf(delX, delY,0.0f);
			currMseModCntlPt._add(defVec);	
			if(currMseModCntlPt.equals(cntlPtCOV)) {moveMapInPlane(defVec);	}			
		}
		updateMapFromCntlPtVals(false);
	}
	
	public final void mseDrag_3D(myPoint mouseClickIn3D, myVector mseDragInWorld, char key, int keyCode) {	
		boolean isScale = (key=='s') || (key=='S'), isRotation = (key=='r') || (key=='R');
		myVectorf mseDragInWorld_f = new myVectorf(mseDragInWorld);
		myVectorf defVec = myVectorf._add(myVectorf._mult(this.basisVecs[1], mseDrag3DScl*mseDragInWorld_f._dot(basisVecs[1])), myVectorf._mult(this.basisVecs[2],mseDrag3DScl* mseDragInWorld_f._dot(basisVecs[2])));		
		
		if(isScale || isRotation) {
			if(isScale) {				dilateMap_MseDrag(defVec);			} 
			else {			rotateMapInPlane_MseDrag(new myPointf(mouseClickIn3D.x, mouseClickIn3D.y, mouseClickIn3D.z),  defVec);	}	//isRotation	
		} else {							//cntl point movement
			if(currMseModCntlPt==null) {return;}				
			//need to project mseDragInWorld to basisVecs[1] and [2], so we know that displacement will stay in plane
			//myVectorf defVec = myVectorf._add(myVectorf._mult(this.basisVecs[1], mseDragInWorld_f._dot(basisVecs[1])), myVectorf._mult(this.basisVecs[2], mseDragInWorld_f._dot(basisVecs[2])));		
			currMseModCntlPt._add(defVec);
			if(currMseModCntlPt.equals(cntlPtCOV)) {moveMapInPlane(defVec);	}	
		}
		updateMapFromCntlPtVals(false);
	}

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
	
	public final void setOtrMap(baseMap _otr) {		otrMap = _otr;}
	
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
