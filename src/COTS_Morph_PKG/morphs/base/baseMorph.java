package COTS_Morph_PKG.morphs.base;

import java.util.TreeMap;

import COTS_Morph_PKG.managers.mapManagers.mapPairManager;
import COTS_Morph_PKG.managers.morphManagers.base.baseMorphManager;
import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapCntlFlags;
import base_UI_Objects.IRenderInterface;
import base_UI_Objects.my_procApplet;
import base_UI_Objects.windowUI.myDispWindow;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

/**
 * class holding common functionality to morph between two image maps
 * @author john
 *
 */
public abstract class baseMorph {
	/**
	 * current map manager, managing key frames of a specific type that this morph is working on
	 */
	protected mapPairManager mapMgr;
	/**
	 * current map manager, managing key frames of a specific type that this morph is working on
	 */
	//protected baseMorphManager morphMgr;
	/**
	 * maps this morph is working on
	 */
	protected final baseMap mapA, mapB;
	/**
	 * current morph map - will be same type as passed maps
	 */
	private baseMap curMorphMap;
	
	/**
	 * current time in morph
	 */
	protected float morphT;
	
	/**
	 * solid representation of morphs
	 */
	protected TreeMap<Float, baseMap> morphSliceAra;
	protected int numMorphSlices = 8;
	
	protected my_procApplet pa;
	protected COTS_MorphWin win;
	public final String morphTitle;
	/**
	 * vector to add to displacement to manage morphing between frames in 3D
	 */
	protected myVectorf normDispTimeVec;

	protected mapCntlFlags[] mapFlags;
	protected static final int 
		mapUpdateNoResetIDX 	= 0;
	protected static final int numMapFlags = 1;
	
	/**
	 * set of all control points for each time slice
	 */
	protected TreeMap<Float, myPointf[][]> cntlPtTrajs;
	
	/**
	 * set of all edge points for each time slice
	 */
	protected TreeMap<Float, myPointf[][][]> edgePtTrajs;
	

	public baseMorph(COTS_MorphWin _win, baseMorphManager _morphMgr, mapPairManager _mapMgr, String _morphTitle) {
		win=_win; pa=myDispWindow.pa;morphTitle=_morphTitle;mapMgr=_mapMgr;//morphMgr=_morphMgr;
		morphT=.5f;
		mapA = mapMgr.maps[0];
		mapB = mapMgr.maps[1];	
		curMorphMap = mapMgr.morphMap;	
		normDispTimeVec = new myVectorf(mapA.getCOV(), mapB.getCOV());
		normDispTimeVec = myVectorf._mult(mapA.basisVecs[0], normDispTimeVec._dot(mapA.basisVecs[0]));	
		cntlPtTrajs = new TreeMap<Float, myPointf[][]>();
		edgePtTrajs = new TreeMap<Float, myPointf[][][]>();
		mapFlags = new mapCntlFlags[numMapFlags];
		for(int i=0;i<mapFlags.length;++i) {
			mapFlags[i] = new mapCntlFlags();
			mapFlags[i].setOptimizeAlpha(true);
			mapFlags[i].setCopyBranching(true);
		}

		//initialize essential data before calcMorph is called
		_endCtorInit();
		mapCalcsAfterCntlPointsSet(morphTitle + "::ctor");	

		setMorphSliceAra();
		calcMorph();		
	}
	/**
	 * this will perform initialization of morph-specific data before initial morph calc is performed, from base class ctor
	 */
	protected abstract void _endCtorInit();
		
	private final void _morphColors(baseMap _curMorphMap, float tA, float tB) {
		//calculate checker board and grid color morphs
		int[][] aPlyClrs = mapA.getPolyColors(),bPlyClrs = mapB.getPolyColors(), curPlyClrs = new int[aPlyClrs.length][aPlyClrs[0].length];
		for(int i=0;i<aPlyClrs.length;++i) {for(int j=0;j<aPlyClrs[i].length;++j) {	curPlyClrs[i][j] = calcMorph_Integer(tA,aPlyClrs[i][j],tB,bPlyClrs[i][j]);}}
		_curMorphMap.setPolyColors(curPlyClrs);		
		int[] aGridClr = mapA.getGridColor(), bGridClr = mapB.getGridColor(), curGridClrs = new int[aGridClr.length];
		for(int i=0;i<aGridClr.length;++i) {curGridClrs[i] = calcMorph_Integer(tA,aGridClr[i],tB,bGridClr[i]);}
		_curMorphMap.setGridColor(curGridClrs);
	}
	
	/**
	 * use currently set t value to calculate morph
	 */
	protected final void calcMorph() {
		//update morph map with map a's vals, 
		curMorphMap.updateMeWithMapVals(mapA,mapFlags[mapUpdateNoResetIDX]);
		//manage slices
		setMorphSliceAra();
		float tA = 1.0f-morphT, tB = morphT;
		
		_calcMorphOnMap(curMorphMap, true, tA, tB);
		buildCntlPointTrajs();
	}
	
	
	protected final void _calcMorphOnMap(baseMap _curMorphMap, boolean _calcColors, float tA, float tB) {
		//initial code for morph, if necessary
		initCalcMorph_Indiv(tA, tB);
		//morph colors
		if(_calcColors) {_morphColors(_curMorphMap, tA, tB);}
		//calculate geometry morph - find delta to use with control points
		//myPointf[] delPts = 
		calcMorphDeltaOfCntlPts(_curMorphMap, tA, tB);	
	}
	
	protected abstract void initCalcMorph_Indiv(float tA, float tB);
	
	protected abstract int calcMorph_Integer(float tA, int AVal, float tB, int BVal);	
	protected abstract float calcMorph_Float(float tA, float AVal, float tB, float BVal);
	protected abstract double calcMorph_Double(float tA, double AVal, float tB, double BVal);
	
	public TreeMap<Float, baseMap> buildLineupOfFrames(int _numFrames) {
		return buildArrayOfMorphMaps(_numFrames, "_Lineup_Frames");
	}
	
	
	/**
	 * use currently set t value to calculate morph
	 */
	protected final myPointf[] calcMorphDeltaOfCntlPts(baseMap _curMorphMap, float tA, float tB) {
		myPointf[] aCntlPts = mapA.getCntlPts(), bCntlPts = mapB.getCntlPts(), morphCntlPts = _curMorphMap.getCntlPts(),delPts = new myPointf[aCntlPts.length];
		myPointf[] newPts = new myPointf[aCntlPts.length+2];

		//put morph results into newPts
		calcMorphBetweenTwoSetsOfCntlPoints(aCntlPts, bCntlPts, newPts, tA, tB);
		_curMorphMap.setCntlPts(newPts, mapFlags[mapUpdateNoResetIDX]);
//		for(int i=0;i<aCntlPts.length;++i) {	
//			delPts[i] = myPointf._sub(newPts[i], morphCntlPts[i]);	//performing this to make sure we have COV properly calculated
//		}
//		_curMorphMap.updateCntlPts(delPts);
		newPts[newPts.length-2] = _curMorphMap.getCOV();
		newPts[newPts.length-1] = _curMorphMap.getCenterPoint();		
		return newPts;
	}//calcMorphOfDeltaCntlPts	
	
	/**
	 * calcluate this morph algorithm between Apts and Bpts, putting result in destPts
	 * @param Apts
	 * @param Bpts
	 * @param destPts
	 * @param tA
	 * @param tB
	 */
	protected abstract void calcMorphBetweenTwoSetsOfCntlPoints(myPointf[] Apts, myPointf[] Bpts, myPointf[] destPts, float tA, float tB);
	
	protected final myPointf[][] calcMorphBetweenTwoSetsOfPoints_2DAra(myPointf[][] Apts, myPointf[][] Bpts, float tA, float tB) {
		myPointf[][] destPts = new myPointf[Apts.length][];
		for(int i=0;i<Apts.length;++i) {
			destPts[i]=new myPointf[Apts[i].length];
			calcMorphBetweenTwoSetsOfCntlPoints(Apts[i], Bpts[i], destPts[i], tA, tB);
		}
		return destPts;
	}
	
	/**
	 * this function will conduct calculations between the two keyframe maps, if such calcs are used, whenever either is modified.  this is morph dependent
	 */
	public abstract void mapCalcsAfterCntlPointsSet(String _calledFrom);
	

	//////////////////////////////
	// draw routines	
	
	public final float drawMapRtSdMenuDescr(float yOff, float sideBarYDisp) {
		if(null == curMorphMap) {return yOff;}	
		
		pa.pushMatrix();pa.pushStyle();
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Green, 255), 6.5f, morphTitle);
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 5.6f, " Morph Frame @ Time : ");
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Yellow, 255), 6.5f, String.format(baseMap.strPointDispFrmt8,morphT));
		pa.popStyle();pa.popMatrix();
	
		yOff += sideBarYDisp;
		pa.translate(10.0f,sideBarYDisp, 0.0f);		
		yOff = curMorphMap.drawRightSideBarMenuDescr(yOff, sideBarYDisp, false, true);
		return yOff;
	}
	public final float drawMorphSliceRtSdMenuDescr(float yOff, float sideBarYDisp) {
		if(null!=morphSliceAra) {
			float modYAmt = sideBarYDisp*.9f;
			yOff += modYAmt;
			pa.translate(10.0f,modYAmt, 0.0f);		
			for(float key : morphSliceAra.keySet()) {
				yOff = morphSliceAra.get(key).drawRightSideBarMenuDescr(yOff, modYAmt, true, false);
			}
		}
		return yOff;
	}
	public final float drawMorphRtSdMenuDescr(float yOff, float sideBarYDisp, float _morphSpeed) {//, String[] _scopeList) {
		pa.pushMatrix();pa.pushStyle();
			//pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Green, 255), 6.0f, morphTitle);
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 6.5f, "Morph Between :");
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Yellow, 255), 6.5f, mapA.mapTitle);
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 6.5f, " and");
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Yellow, 255), 6.5f, mapB.mapTitle);
		pa.popStyle();pa.popMatrix();		
		yOff += sideBarYDisp;
		pa.translate(0.0f,sideBarYDisp, 0.0f);		
		
		pa.pushMatrix();pa.pushStyle();
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 6.0f, "Currently at time :");
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Yellow, 255), 6.5f, String.format(baseMap.strPointDispFrmt8,morphT));
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 6.5f, " | Speed :");
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Yellow, 255), 6.5f, String.format(baseMap.strPointDispFrmt8,_morphSpeed));
		pa.popStyle();pa.popMatrix();		
		
		yOff += sideBarYDisp;
		pa.translate(0.0f,sideBarYDisp, 0.0f);			
		yOff = drawMorphRtSdMenuDescr_Indiv(yOff, sideBarYDisp);	
		return yOff;
	}	
	protected abstract float drawMorphRtSdMenuDescr_Indiv(float yOff, float sideBarYDisp);	
	public final void drawMorphedMap_CntlPts(int _detail) {		curMorphMap.drawMap_CntlPts(false, _detail);	}
	
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
	
	protected baseMap getCopyOfMap(baseMap cpyMap, String fullCpyName) {
		if(null==cpyMap) {cpyMap = mapA;}
		baseMap resMap = mapMgr.buildCopyMapOfPassedMapType(cpyMap, fullCpyName);
		//resMap.updateMeWithMapVals(cpyMap,mapFlags[mapCopyNoResetIDX]);
		return resMap;
	}
	
	protected final void buildCntlPointTrajs() {
		cntlPtTrajs.clear();
		edgePtTrajs.clear();
		baseMap tmpMap = getCopyOfMap(null,mapA.mapTitle +"_MorphCntlPtTraj");
		myPointf[] cntlPtsOld = calcMorphDeltaOfCntlPts(tmpMap,1.0f, 0.0f);
		myPointf[][] tmpCntlPtAra;
		myPointf[][][] tmpEdgePtAra;
		myPointf[][] edgePtsOld = mapA.getEdgePts();
		
		pa.strokeWeight(1.0f);
		for(float t = 0.01f;t<=1.0f;t+=.01f) {
			float tA = 1.0f-t, tB = t;
			initCalcMorph_Indiv(tA, tB);
			myPointf[] cntlPts = calcMorphDeltaOfCntlPts(tmpMap,tA, tB);
			myPointf[][] edgePts = tmpMap.getEdgePts();
			//idx 5 is cov, idx 6 is ctr pt
			if(null != cntlPts) {	
				tmpCntlPtAra = new myPointf[2][];
				tmpCntlPtAra[0]=new myPointf[cntlPts.length];
				tmpCntlPtAra[1]=new myPointf[cntlPts.length];
				for(int i =0;i<cntlPts.length;++i) {
					tmpCntlPtAra[0][i] = new myPointf(cntlPtsOld[i]);
					tmpCntlPtAra[1][i] = new myPointf(cntlPts[i]);						
				}
				cntlPtTrajs.put(t, tmpCntlPtAra);
			}
			tmpEdgePtAra = new myPointf[2][][];
			tmpEdgePtAra[0]=new myPointf[edgePts.length][];
			tmpEdgePtAra[1]=new myPointf[edgePts.length][];
			for(int i=0;i<edgePts.length;++i) {
				tmpEdgePtAra[0][i]=new myPointf[edgePts[i].length];
				tmpEdgePtAra[1][i]=new myPointf[edgePts[i].length];
				for(int j=0;j<edgePts[i].length;++j) {
					tmpEdgePtAra[0][i][j]=new myPointf(edgePtsOld[i][j]);
					tmpEdgePtAra[1][i][j]=new myPointf(edgePts[i][j]);
				}
			}
			edgePtTrajs.put(t, tmpEdgePtAra);
			cntlPtsOld = cntlPts;
			edgePtsOld = edgePts;
		}		
	}
	
	/**
	 * builds cntl point trajectories using current morph
	 */
	public final void drawMorphedMap_CntlPtTraj(int _detail) {
		pa.pushMatrix();pa.pushStyle();	
		pa.sphereDetail(5);
		pa.stroke(0,0,0,255);
	
		pa.strokeWeight(1.0f);
		for(float t = 0.01f;t<=1.0f;t+=.01f) {
			float tA = 1.0f-t, tB = t;
			initCalcMorph_Indiv(tA, tB);
			myPointf[][] cntlPts = cntlPtTrajs.get(t);
			myPointf[][][] edgePts = edgePtTrajs.get(t);
			//idx 5 is cov, idx 6 is ctr pt
			if(null != cntlPts) {
				for(int i = 0;i<cntlPts[0].length-2;++i) {		_drawPt(cntlPts[1][i], 2.0f); pa.line(cntlPts[0][i], cntlPts[1][i]);}
			}
			if(_detail >= COTS_MorphWin.drawMapDet_CntlPts_COV_IDX) {
				int k=cntlPts[0].length-2;
				_drawPt(cntlPts[1][k], 2.0f); pa.line(cntlPts[0][k], cntlPts[1][k]);				
			}
			if(_detail >= COTS_MorphWin.drawMapDet_CntlPts_COV_F_IDX) {
				int k=cntlPts[0].length-1;
				_drawPt(cntlPts[1][k], 2.0f); pa.line(cntlPts[0][k], cntlPts[1][k]);
			}
			if(_detail >= COTS_MorphWin.drawMapDet_ALL_IDX) {
				for(int i=0;i<edgePts[0].length;++i) {	for(int j=0;j<edgePts[0][i].length;++j) {	pa.line(edgePts[0][i][j], edgePts[1][i][j]);}}
			}
		}		
		pa.popStyle();pa.popMatrix();	
	}
	
	public final void drawMorphedMap(boolean _isFill, boolean _drawCircles) {
		_drawMorphMap(curMorphMap, _isFill, _drawCircles);
	}
	
	public final void drawMorphSlices(boolean _isFill, boolean _drawCircles, boolean _drawCntlPts, boolean _showLabels, int _detail) {
		for(Float t : morphSliceAra.keySet()) {
			_drawMorphMap(morphSliceAra.get(t), _isFill, _drawCircles);
		}	
		if(_drawCntlPts) {
			for(Float t : morphSliceAra.keySet()) {
				baseMap map = morphSliceAra.get(t);
				map.drawMap_CntlPts(false, _detail);
				map.drawHeaderAndLabels(_showLabels, _detail);
			}
		}
	
	}
	
	protected final void _drawMorphMap(baseMap _map, boolean _isFill, boolean _drawCircles) {
		if(_isFill) {	_map.drawMap_Fill();}
		else {			_map.drawMap_Wf();}	
		if(_drawCircles) {
			if(_isFill) {	_map.drawMap_PolyCircles_Fill();}	
			else {			_map.drawMap_PolyCircles_Wf();}		
		}
	}

	public final void drawHeaderAndLabels(boolean _drawLabels, int _detail) {							curMorphMap.drawHeaderAndLabels(_drawLabels,_detail);}
	
	/**
	 * this will draw instancing morph-specific data on screen 
	 */
	public abstract void drawMorphSpecificValues(boolean debug, boolean drawCntlPts, boolean showLbls);

	
	/////////////////////////
	// setters/getters
	
	public final void setMorphT(float _t) {			morphT=_t;				calcMorph();}	
	//public final void setMorphScope(int _mScope) {	morphScope = _mScope;	calcMorph();}
	
	public final void setMorphSlices(int _num) {
		int oldNumMorphSlices = numMorphSlices; 
		numMorphSlices=_num;
		if(oldNumMorphSlices != numMorphSlices) {setMorphSliceAra();}
	}

	protected final void setMorphSliceAra() {
		morphSliceAra = buildArrayOfMorphMaps(numMorphSlices, "_MorphSlice");
	}//setMorphMapAra()	
	
	protected final TreeMap<Float, baseMap> buildArrayOfMorphMaps(int numMaps, String _name) {
		baseMap tmpMap = getCopyOfMap(null, mapA.mapTitle);	
		TreeMap<Float, baseMap> morphMaps = new TreeMap<Float, baseMap>();
		//want incr so that i get numMaps back
		Float tIncr = 1.0f/(numMaps-1.0f);
		Float t = 0.0f;
		for (int i=0;i<numMaps-1;++i) {		
			tmpMap = getCopyOfMap(tmpMap, mapA.mapTitle +_name + " @ t="+String.format("%2.3f", t));			
			float tA = 1.0f-t, tB = t;
			_calcMorphOnMap(tmpMap, true, tA, tB);
			morphMaps.put(t, tmpMap);		
			t+=tIncr;
		}
		t=1.0f;
		//tmpMap = mapMgr.buildCopyMapOfPassedMapType(mapA, "Morph @ t="+String.format("%2.3f", t));
		tmpMap = getCopyOfMap(tmpMap, mapA.mapTitle +_name + " @ t="+String.format("%2.3f", t));
		//
		_calcMorphOnMap(tmpMap, true, 0.0f, 1.0f);	
		morphMaps.put(t, tmpMap);				
		return morphMaps;
	}

	/**
	 * resets branching if any morphs maintain branching values on their own
	 */
	public abstract void resetAllBranching();

	public final String toStringEdge(myPointf[] e) {
		return "0:["+e[0].toStrCSV(baseMap.strPointDispFrmt8)+"] | 1:["+e[1].toStrCSV(baseMap.strPointDispFrmt8)+"]";
	}
	

}//class baseMorph
