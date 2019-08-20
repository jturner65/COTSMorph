package COTS_Morph_PKG.maps.base;

import java.util.TreeMap;

import base_UI_Objects.my_procApplet;
import base_UI_Objects.windowUI.myDispWindow;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVector;
import base_Utils_Objects.vectorObjs.myVectorf;
import processing.core.PConstants;

/**
 * base class describing a mapping
 * @author john
 *
 */
public abstract class baseMap {
	/**
	 * array of corner cntl points
	 */
	protected myPointf[] cntlPts;
	/**
	 * array of labels to use for control points
	 */
	protected String[] cntlPtLbls;	
	/**
	 * control point currently being moved/modified by UI interaction
	 */
	protected myPointf currMseModCntlPt;
	/**
	 * point at center of cntrl points - "center" of map
	 */
	protected myPointf cntlPtCOA;
	
	
	/**
	 * poly points - recalced when cntlpts move - idx 1 is x idx, idx2 is y idx
	 */
	protected myPointf[][] polyPoints;
	
	/**
	 * array of 2 poly colors
	 */
	protected int[][] polyColors;
	/**
	 * color for grid lines
	 */
	protected int[] gridColor;
	
	/**
	 * # of cells per side in grid
	 */
	protected int numCellsPerSide = 8;
	
	protected static final float sphereRad = 5.0f;
	/**
	 * map index for this map , either 0 or 1
	 */
	public final int mapIdx;
	
	/**
	 * title of map for display
	 */
	protected final String mapTitle;
	/**
	 * where to print map title above map
	 */
	protected myPointf mapTitleOffset;
	protected final float mapTtlXOff;
	
	/**
	 * coplanar ortho frame for map
	 */
	protected myVectorf[] basisVecs;
	protected myPointf[] orthoFrame;
	
	//text scale value for display
	protected final float txtSclVal = 1.25f;
	
	public baseMap(myPointf[] _cntlPts, int _mapIdx, int[][] _pClrs, int _numCellPerSide, String _mapTitle) {
		mapIdx = _mapIdx;
		currMseModCntlPt = null;
		mapTitle = _mapTitle + " "+ mapIdx;
		mapTtlXOff = myDispWindow.yOff*mapTitle.length()*.25f;
		cntlPtCOA = new myPointf(0,0,0);
		mapTitleOffset = new myPointf(0,0,0);
		
		setCntlPts(_cntlPts,_numCellPerSide);
		polyColors = new int[_pClrs.length][];
		for(int i=0;i<polyColors.length;++i) {
			polyColors[i]=new int[_pClrs[i].length];
			System.arraycopy(_pClrs[i], 0, polyColors[i], 0, polyColors[i].length);
		}
		gridColor = new int[4];
		System.arraycopy(polyColors[1], 0, gridColor, 0, gridColor.length);
		
	}//ctor
	/**
	 * call every time control points actually change
	 * @param _cntlPts
	 * @param _numCellPerSide
	 */
	public void setCntlPts(myPointf[] _cntlPts, int _numCellPerSide) {
		numCellsPerSide = _numCellPerSide;
		cntlPts = new myPointf[_cntlPts.length];
		cntlPtLbls = new String[cntlPts.length];		
		for(int i=0;i<cntlPts.length;++i) {		
			cntlPts[i]=_cntlPts[i];
			cntlPtLbls[i]="" + ((char)(i+'A'));	
		}
		basisVecs = buildBasisVecs(myVectorf._cross(new myVectorf(cntlPts[0], cntlPts[1]), new myVectorf(cntlPts[0], cntlPts[3]))._normalize());
		//initial poly points
		calcPolyPoints(true);
	}
	
	/**
	 * update map values and recalc poly points if necessary or forced
	 * @param _numCellsPerSide
	 * @param forceUpdate : force poly point recalc
	 */
	public void updateMapVals(int _numCellsPerSide, boolean forceUpdate) {
		boolean changed = forceUpdate;
		if(_numCellsPerSide != numCellsPerSide) {
			numCellsPerSide=_numCellsPerSide;	
			changed = true;
		}
		
		changed = updateMapVals_Indiv(changed);
		//initial poly points
		if(changed) {
			calcPolyPoints(false);
		}
	}
	/**
	 * instance specific updates
	 * @param hasBeenUpdated whether this map has been updated already
	 * @return hasBeenUpdated, possibly changed to true if this method causes update
	 */
	protected abstract boolean updateMapVals_Indiv(boolean hasBeenUpdated) ;
	
	
	
	//for disp only
	protected final void rebuildOrthoFrame() {
		orthoFrame = new myPointf[3];		
		for(int i=0;i<orthoFrame.length;++i) {orthoFrame[i]= myPointf._add(cntlPtCOA, 200.0f, basisVecs[i]);}		
	}
	
	/**
	 * calculate mapped point given offets - tx and ty should be between 0 and 1
	 * @param tx
	 * @param ty
	 * @return
	 */
	public abstract myPointf calcMapPt(float tx, float ty);
	
	
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
		
			System.out.println(mapTitle+" Using up");
		} else {
			basisVecs[1] = basisVecs[0]._cross(myVectorf.FORWARD);
			basisVecs[1]._normalize();

			basisVecs[2] = basisVecs[1]._cross(basisVecs[0]);
			basisVecs[2]._normalize();		
	
		}
		return basisVecs;
	}

	
	/**
	 * calc center of area point and map title offset
	 */
	protected final void setCurrCntrlPtCOA() {
		cntlPtCOA.set(0,0,0);
		for(int i=0;i<cntlPts.length;++i) {		cntlPtCOA._add(cntlPts[i]);}
		cntlPtCOA._div(cntlPts.length);
		//find location for map title offest - directly above cntlPtCOA
		myPointf tmpCtrPt = new myPointf(cntlPts[0],.5f,cntlPts[1]);
		
		myVectorf ttlOff = new myVectorf(cntlPtCOA,tmpCtrPt);
		myVectorf ttlOffNorm = ttlOff._normalized(),
				ttlToA = new myVectorf(tmpCtrPt, cntlPts[0]), 
				ttlToB = new myVectorf(tmpCtrPt, cntlPts[1]);
		//length of projection of difference between a and ctr pt between a and b on vector from COA to ctr pt between a and b
		float distA = ttlToA._dot(ttlOffNorm), distB = ttlToB._dot(ttlOffNorm), dist = (distA > distB ? distA : distB);
		
		myVectorf ttlToACoLin = myVectorf._mult(ttlOffNorm, dist),
				ttlPerp = myVectorf._sub(ttlToA, ttlToACoLin)._normalize()._mult(mapTtlXOff);
		mapTitleOffset.set(myVectorf._add(myVectorf._add(myVectorf._add(cntlPtCOA ,ttlOff), myVectorf._mult(ttlOffNorm, 1.0f*(dist + 20.0f))), ttlPerp));
		rebuildOrthoFrame();
	}
	
	
	/**
	 * build all grid verts based on current control corner point positions
	 * @return
	 */
	protected final void calcPolyPoints(boolean reset) {
		//instance-specifics
		updateCntlPtVals(reset);		
		//find center of control points
		setCurrCntrlPtCOA();
		
		polyPoints = new myPointf[numCellsPerSide+1][numCellsPerSide+1];
		for(int i=0;i<polyPoints.length;++i) {
			float tx = i/(1.0f*numCellsPerSide);
			for(int j=0;j<polyPoints[i].length;++j) {	polyPoints[i][j] = calcMapPt(tx, j/(1.0f*numCellsPerSide));}
		}		
		
	}//calcPolyPoints
	/**
	 * Instance-class specific initialization
	 */
	protected abstract void updateCntlPtVals(boolean reset);
	
	protected final void printOutAllCntlPts() {
		String debug = "";
	    for(int i=0;i<cntlPts.length;++i) {
	    	debug +="\n\t"+cntlPts[i].toStrBrf();
	    }
	    System.out.println(debug);
	}

	
	///////////////////////
	// draw routines
	
	protected void _drawPt(my_procApplet pa, myPointf p, float rad) {
		pa.pushMatrix();pa.pushStyle();	
		pa.translate(p);
		pa.sphere(rad);
		pa.popStyle();pa.popMatrix();	
	}
		 
		
	/**
	 * draw the control points for this map
	 * @param pa
	 */
	private void _drawPoints(my_procApplet pa, boolean isCurMap) {
		//drawOrthoFrame(pa);
		pa.sphereDetail(5);
		pa.stroke(0,0,0,255);
		for(myPointf p : cntlPts) {		_drawPt(pa,p, (p.equals(currMseModCntlPt) && isCurMap ? 2.0f*sphereRad : sphereRad));}
		_drawPt(pa,cntlPtCOA,sphereRad*1.5f);	
		//instance specific 
		_drawPoints_Indiv(pa);
		
	}//_drawPoints
	/**
	 * instance-specific point drawing
	 * @param pa
	 */
	protected abstract void _drawPoints_Indiv(my_procApplet pa);
	
	public void drawMap_Fill(my_procApplet pa, boolean isCurMap) {
		pa.pushMatrix();pa.pushStyle();	
		pa.stroke(255,255,255,255);
		pa.setStrokeWt(1.0f);
		//polyPoints = calcPolyPoints();
		int clrIdx = 0;
		for(int i=0;i<polyPoints.length-1;++i) {
			clrIdx = i % 2;
			for(int j=0;j<polyPoints[i].length-1;++j) {
				pa.setFill(polyColors[clrIdx], polyColors[clrIdx][3]);
				pa.beginShape();
				pa.gl_vertex(polyPoints[i][j]);
				pa.gl_vertex(polyPoints[i+1][j]);
				pa.gl_vertex(polyPoints[i+1][j+1]);
				pa.gl_vertex(polyPoints[i][j+1]);
				pa.endShape(PConstants.CLOSE);				
				clrIdx = (clrIdx + 1) % 2;
			}				
		}	
		_drawPoints(pa,isCurMap);
		pa.popStyle();pa.popMatrix();
	}//drawMap_Fill
		
	public void drawMap_Wf(my_procApplet pa, boolean isCurMap) {
		pa.pushMatrix();pa.pushStyle();	
		pa.noFill();
		pa.setStroke(gridColor, gridColor[3]);
		pa.setStrokeWt(2.0f);
		//polyPoints = calcPolyPoints();
		int clrIdx = 0;
		for(int i=0;i<polyPoints.length-1;++i) {
			clrIdx = i % 2;
			for(int j=0;j<polyPoints[i].length-1;++j) {
				pa.beginShape();
				pa.gl_vertex(polyPoints[i][j]);
				pa.gl_vertex(polyPoints[i+1][j]);
				pa.gl_vertex(polyPoints[i+1][j+1]);
				pa.gl_vertex(polyPoints[i][j+1]);
				pa.endShape(PConstants.CLOSE);				
				clrIdx = (clrIdx + 1) % 2;
			}				
		}	
		_drawPoints(pa,isCurMap);
		pa.popStyle();pa.popMatrix();
	}//drawMap_Wf
	
	private void drawOrthoFrame(my_procApplet pa) {
		pa.pushMatrix();pa.pushStyle();	
		pa.strokeWeight(3.0f);
		pa.stroke(255,0,0,255);
		pa.line(cntlPtCOA, orthoFrame[0]);
		pa.pushMatrix();pa.pushStyle();	
		pa.fill(0,0,0,255);
		pa.translate(myPointf._add(cntlPtCOA,orthoFrame[0]));
		//pa.text("Ortho 0", 0, 0);
		pa.popStyle();pa.popMatrix();
		pa.stroke(0,255,0,255);
		pa.strokeWeight(3.0f);
		pa.line(cntlPtCOA, orthoFrame[1]);
		pa.stroke(0,0,255,255);
		pa.strokeWeight(3.0f);
		pa.line(cntlPtCOA, orthoFrame[2]);
		cntlPtCOA.showMeSphere(pa, 5.0f);
		for(int i=0;i<orthoFrame.length;++i) {	orthoFrame[i].showMeSphere(pa, 5.0f);}
		pa.popStyle();pa.popMatrix();				
	}//_drawOrthoFrame
	
	private void _drawLabelAtPt_UnSetCamp(my_procApplet pa, myDispWindow animWin, myPointf p, String lbl, float xOff, float yOff) {
		pa.pushMatrix();pa.pushStyle();	
		pa.translate(p);
		animWin.unSetCamOrient();
		pa.scale(txtSclVal);
		pa.text(lbl, xOff,yOff,0); 
		pa.popStyle();pa.popMatrix();
	}

	private void _drawLabelAtPt(my_procApplet pa, myPointf p, String lbl, float xOff, float yOff) {
		pa.pushMatrix();pa.pushStyle();	
		pa.translate(p);
		pa.scale(txtSclVal);
		pa.text(lbl, xOff,yOff,0); 
		pa.popStyle();pa.popMatrix();
	}

	public void drawHeaderAndLabels_2D(my_procApplet pa, boolean _drawLabels) {
		pa.pushMatrix();pa.pushStyle();
		pa.fill(0,0,0,255);
		pa.stroke(0,0,0,255);
		_drawLabelAtPt(pa, mapTitleOffset, mapTitle, 0.0f, 0.0f);
		if(_drawLabels) {
			for(int i=0; i< cntlPts.length;++i) {
				_drawLabelAtPt(pa,cntlPts[i],cntlPtLbls[i] + "_"+mapIdx + " : " + cntlPts[i].toStrBrf(), 2.5f,-2.5f);
			}
			_drawLabelAtPt(pa,cntlPtCOA,"Center of Cntrl Pts", 2.5f,-2.5f); 
		}
		pa.popStyle();pa.popMatrix();
	}//drawLabels_2D
	
	public void drawHeaderAndLabels_3D(my_procApplet pa, boolean _drawLabels, myDispWindow animWin) {
		pa.pushMatrix();pa.pushStyle();
		pa.fill(0,0,0,255);
		pa.stroke(0,0,0,255);
		_drawLabelAtPt_UnSetCamp(pa, animWin, mapTitleOffset, mapTitle, 0.0f, 0.0f);
		if(_drawLabels) {
			for(int i=0; i< cntlPts.length;++i) {
				_drawLabelAtPt_UnSetCamp(pa, animWin, cntlPts[i],cntlPtLbls[i] + "_"+mapIdx + " : " + cntlPts[i].toStrBrf(), 2.5f,-2.5f); 
			}			
			_drawLabelAtPt_UnSetCamp(pa, animWin,cntlPtCOA,"Center of Cntrl Pts", 2.5f,-2.5f);
		}
		pa.popStyle();pa.popMatrix();		
	}//drawLabels_3D
	
	////////////////////////
	// mouse interaction - need to determine which map control point of both maps is closest to mouse location
	
	/**
	 * returns the distance to the closest point to the mouse location
	 * @return
	 */
	public final Float findClosestCntlPt_2D(myPointf _mseLoc){
		TreeMap<Float, Integer> res = new TreeMap<Float, Integer>();
		for(int i=0;i<cntlPts.length;++i) {			res.put(myPointf._SqrDist(cntlPts[i], _mseLoc), i);		}
		Float leastKey = res.firstKey();
		currMseModCntlPt = cntlPts[res.get(leastKey)];
		return leastKey;		
	}//findClosestCntlPt
	
	/**
	 * returns distance to closest point to passed line/ray
	 * @param _rayOrigin mouse point
	 * @param _rayDir unit direction vector in direction of mouse point into screen
	 * @return
	 */
	public final Float findClosestCntlPt_3D(myPointf _rayOrigin, myVectorf _rayDir){
		TreeMap<Float, Integer> res = new TreeMap<Float, Integer>();
		myVectorf vecToPt;
		float proj;
		for(int i=0;i<cntlPts.length;++i) {
			vecToPt = new myVectorf(_rayOrigin, cntlPts[i]);
			proj = vecToPt._dot(_rayDir);
			float dist = myVectorf._sub(vecToPt, myVectorf._mult(_rayDir, proj)).sqMagn;
			res.put(dist, i);					
		}
		Float leastKey = res.firstKey();
		currMseModCntlPt = cntlPts[res.get(leastKey)];
		return leastKey;		
	}//findClosestCntlPt
	
	public final void mseDrag_2D(float delX, float delY) {	
		if(currMseModCntlPt==null) {return;}
		//printOutAllCntlPts();
		currMseModCntlPt._add(delX, delY,0.0f);	
		calcPolyPoints(false);
	}
	
	public final void mseDrag_3D(myVector mseDragInWorld) {	
		if(currMseModCntlPt==null) {return;}
		
		myVectorf mseDragInWorld_f = new myVectorf(mseDragInWorld);
		//need to project mseDragInWorld to basisVecs[1] and [2], so we know that displacement will stay in plane
		currMseModCntlPt.set(myPointf._add(currMseModCntlPt, mseDragInWorld_f._dot(this.basisVecs[1]), this.basisVecs[1], mseDragInWorld_f._dot(this.basisVecs[2]), this.basisVecs[2]));
		calcPolyPoints(false);
	}

	/**
	 * code for whenever mouse release is executed
	 */
	public final void mseRelease() {
		calcPolyPoints(false);
		currMseModCntlPt = null;
		mseRelease_Indiv();
	}
	
	protected abstract void mseRelease_Indiv();

}//class baseMap
