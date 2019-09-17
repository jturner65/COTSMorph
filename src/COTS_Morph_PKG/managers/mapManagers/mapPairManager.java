package COTS_Morph_PKG.managers.mapManagers;


import java.util.TreeMap;

import COTS_Morph_PKG.managers.morphManagers.base.baseMorphManager;
import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.maps.quad.BiLinMap;
import COTS_Morph_PKG.maps.quad.COTSMap;
import COTS_Morph_PKG.maps.triangular.BiLinTriPolyMap;
import COTS_Morph_PKG.maps.triangular.PointNormTriPolyMap;
import COTS_Morph_PKG.morphs.CarrierSimDiagMorph;
import COTS_Morph_PKG.morphs.CarrierSimTransformMorph;
import COTS_Morph_PKG.morphs.DualCarrierSimMorph;
import COTS_Morph_PKG.morphs.LERPMorph;
import COTS_Morph_PKG.morphs.LogPolarMorph;
import COTS_Morph_PKG.morphs.QuadKeyEdgeSpiralMorph;
import COTS_Morph_PKG.morphs.base.baseMorph;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_UI_Objects.IRenderInterface;
import base_UI_Objects.my_procApplet;
import base_UI_Objects.windowUI.myDispWindow;
import base_Utils_Objects.vectorObjs.myPoint;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;
import processing.core.PImage;

/**
 * this class will manage a pair of maps that are acting as key frames, controlling access to the maps
 * this class will serve to isolate and manage map->map interactions, including but not limited to morphing
 * there will be one map manager of each type for every type of map
 * @author john
 *
 */
public class mapPairManager {
	/**
	 * descriptive name of this map manager
	 */
	public final String name;
	/**
	 * the maps this mapManager will manage
	 */
	public final baseMap[] maps;
	/**
	 * the types of maps this manager owns
	 */
	public final int mapType;
	/**
	 * current ui values describing features of the map
	 */
	public mapUpdFromUIData currUIVals;
	/**
	 * map being currently modified by mouse interaction - only a ref to a map, or null
	 */
	public baseMap currMseModMap;	
	/**
	 * current morph being executed
	 */
	protected int currMorphTypeIDX;
	/**
	 * copy map to display if being copied or similarity mapped - only a copy of either mapA or mapB
	 */
	public baseMap copyMap;
	/**
	 * map used to represent current status of morph
	 */
	public final baseMap morphMap;
	/**
	 * idx's of maps to use for similarity mapping/registration between maps
	 */
	protected int fromMapIDX = -1, toMapIDX = -1;
	/**
	 * bounds for the key frame maps managed by this
	 */
	public final myPointf[][] bndPts;
	
	/**
	 * types of maps supported
	 */
	public static final String[] mapTypes = new String[] {
		"Triangle",
		"Point Normal Triangle",
		"Bilinear",
		"COTS",			
	};
	//need an index per map type
	public static final int
		triangleMapIDX		= 0,
		ptNormTriMapIDX		= 1,
		bilinearMapIDX		= 2,
		COTSMapIDX		 	= 3;	
	
	/**
	 * array holding morphs
	 */
	protected baseMorph[] morphs;

	public static my_procApplet pa;
	public COTS_MorphWin win;
	/**
	 * array holding upper left corner x,y, width, height of rectangle to use for oriented lineup images
	 */
	protected float[] lineupRectDims;
	protected float perLineupImageWidth;
	protected TreeMap<Float, baseMap> lineUpMorphMaps;
	
	/**
	 * morph animation variables
	 */
	private float morphProgress = 0.5f, morphSpeed = 1.0f;
	
	/**
	 * colors for each of 2 maps' grids
	 */
	protected static final int[][][] mapGridColors = new int[][][] {
		{{90,0,222,255},{0,225,10,255}},		//map grid 0 
		{{255,200,0,255},{255,0,0,255}}		//map grid 1
	};

	public mapPairManager(COTS_MorphWin _win, myPointf[][] _bndPts, PImage[] _txtrImages, mapUpdFromUIData _currUIVals, int _mapType) {
		win=_win; pa=myDispWindow.pa;
		name = win.getWinName()+"::Mgr of " + mapTypes[_mapType] + " maps";
		// necessary info to build map
		bndPts = new myPointf[_bndPts.length][];
		for(int i=0;i<bndPts.length;++i) {bndPts[i]=new myPointf[_bndPts[i].length];	for(int j=0;j<bndPts[i].length;++j) {		bndPts[i][j]=new myPointf(_bndPts[i][j]);	}}
		currUIVals = new mapUpdFromUIData(_currUIVals);
		setLineupRectDims();
		//build maps
		maps = new baseMap[2];
		for(int j=0;j<maps.length;++j) {	maps[j] = buildKeyFrameMapOfPassedType(_mapType,j, "");}		
		
		morphMap = buildCopyMapOfPassedMapType(maps[0], maps[0].mapTitle+"_Morph"); 
		morphMap.setImageToMap(_txtrImages[0]);
		copyMap = buildCopyMapOfPassedMapType(maps[0], maps[0].mapTitle+"_Copy");
		
		for(int j=0;j<maps.length;++j) {	maps[j].setImageToMap(_txtrImages[j]);	maps[j].setOtrMap(maps[(j+1)%maps.length]);}
		//for building registration copy
		fromMapIDX = 0;
		toMapIDX = 1;
		lineUpMorphMaps = new TreeMap<Float, baseMap>();
		mapType=_mapType;
		_initMorphs();
	}//ctor
	/**
	 * initialize all morphs - only call once
	 */
	private void _initMorphs() {
		morphs = new baseMorph[baseMorphManager.morphTypes.length];
		morphs[baseMorphManager.LERPMorphIDX] = new LERPMorph(win,null,this,baseMorphManager.morphTypes[baseMorphManager.LERPMorphIDX]); 
		morphs[baseMorphManager.CarrierSimDiagIDX] = new CarrierSimDiagMorph(win,null,this,baseMorphManager.morphTypes[baseMorphManager.CarrierSimDiagIDX]); 		
		morphs[baseMorphManager.CarrierSimRegTransIDX] = new CarrierSimTransformMorph(win,null,this,baseMorphManager.morphTypes[baseMorphManager.CarrierSimRegTransIDX]); 
		morphs[baseMorphManager.DualCarrierSimIDX] = new DualCarrierSimMorph(win,null,this,baseMorphManager.morphTypes[baseMorphManager.DualCarrierSimIDX]); 
		morphs[baseMorphManager.QuadSpiralEdgeIDS] = new QuadKeyEdgeSpiralMorph(win,null,this,baseMorphManager.morphTypes[baseMorphManager.DualCarrierSimIDX]); 
		morphs[baseMorphManager.LogPolarMorphIDX] = new LogPolarMorph(win,null,this,baseMorphManager.morphTypes[baseMorphManager.LogPolarMorphIDX]);	
	}
	
	public final baseMap buildCopyMapOfPassedMapType(baseMap oldMap, String _mapName) {	
		baseMap map;
		switch (oldMap.mapTypeIDX) {
			case triangleMapIDX		: {	map = new BiLinTriPolyMap(_mapName,(BiLinTriPolyMap)oldMap);		break;}
			case ptNormTriMapIDX	: {	map = new PointNormTriPolyMap(_mapName,(PointNormTriPolyMap)oldMap);		break;}
			case bilinearMapIDX 	: {	map = new BiLinMap(_mapName,(BiLinMap)oldMap);		break;}
			case COTSMapIDX 		: {	map = new COTSMap(_mapName,(COTSMap)oldMap); 		break;}
			default		:{
				win.getMsgObj().dispErrorMessage("mapManager", "buildMapOfPassedType", "Error : Unable to duplicate passed map "+ oldMap.mapTitle + " due to unknown map type : " + oldMap.mapTypeIDX + " : "  +mapTypes[oldMap.mapTypeIDX] + ". Returning null.");
				return null;
			}
		}
		return map;
	}//buildCopyMapOfPassedMapType		
	
	public final baseMap buildKeyFrameMapOfPassedType(int _mapType, int _mapValIdx, String _mapNameSuffix) {
		String mapName = mapTypes[_mapType] + ( _mapNameSuffix.length() == 0 ? "":  " " + _mapNameSuffix) + " Map"+ _mapValIdx;
		switch (_mapType) {
			case triangleMapIDX			:{
				myPointf[] triBndPts = new myPointf[3];
				for(int i=0;i<triBndPts.length;++i) {		triBndPts[i]=bndPts[_mapValIdx][i];		}
				return new BiLinTriPolyMap(win, this, triBndPts, _mapValIdx, _mapType, mapGridColors[_mapValIdx], currUIVals,true, mapName);}
			case ptNormTriMapIDX		: {	
				myPointf[] triBndPts = new myPointf[3];
				for(int i=0;i<triBndPts.length;++i) {		triBndPts[i]=bndPts[_mapValIdx][i];		}
				return new PointNormTriPolyMap(win, this,triBndPts, _mapValIdx, _mapType,mapGridColors[_mapValIdx], currUIVals,true, mapName);}
			case bilinearMapIDX 	: {	return new BiLinMap(win,this, bndPts[_mapValIdx], _mapValIdx, _mapType,mapGridColors[_mapValIdx], currUIVals,true, mapName);}
			case COTSMapIDX 		: {	return new COTSMap(win,this,  bndPts[_mapValIdx], _mapValIdx, _mapType,mapGridColors[_mapValIdx], currUIVals,true, mapName);}
			default		:{
				win.getMsgObj().dispErrorMessage("mapManager", "buildMapOfPassedType", "Error : Unable to build requested map due to unknown map type : " + _mapType + " : "  +mapTypes[_mapType] + ". Returning null.");
				return null;
			}			
		}
	}
	
	//////////////
	// map comparison and map/morph processing
	/**
	 * register "from" map to "to" map, and build a copy
	 * @param setCopyMap
	 * @param dispMod
	 */
	public final void findDifferenceBetweenMaps(boolean dispMod) {		
		baseMap fromMap = maps[fromMapIDX];
		baseMap toMap = maps[toMapIDX];
		copyMap = calcDifferenceBetweenMaps(fromMap, toMap, dispMod);
	}//findDifferenceBetweenMaps
	
	
	public final baseMap calcDifferenceBetweenMaps(baseMap fromMap, baseMap toMap) {return calcDifferenceBetweenMaps(fromMap, toMap, false);}	
	private baseMap calcDifferenceBetweenMaps(baseMap fromMap, baseMap toMap, boolean dispMod) {
		myVectorf dispBetweenMaps = new myVectorf();
		float[] angleAndScale = new float[2];
		toMap.findDifferenceToMe(fromMap, dispBetweenMaps, angleAndScale);
		if(dispMod) {
			win.getMsgObj().dispInfoMessage("mapManager", "findDiffBetweenMaps", "Distance " + fromMap.mapTitle + " -> " + toMap.mapTitle + " | Displacement of COV : " +  dispBetweenMaps.toStrBrf() + " | Angle between Maps : " + angleAndScale[0] + " | Geometric Means Scale :" + angleAndScale[1]);
		}
		//System.out.println("Building copy of from map : " + fromMap.mapTitle);
		baseMap tmpMap = buildCopyMapOfPassedMapType(fromMap, fromMap.mapTitle+"_DiffMap");
		tmpMap.registerMeToVals(dispBetweenMaps, angleAndScale);
		//System.out.println("Done Building copy of from map : " + fromMap.mapTitle);
		return tmpMap;	
	}	
	/**
	 * build oriented lineup of specific # of frames (default 5) where each frame is registered to keyframe A, and then displayed side-by-side
	 */
	public final void buildOrientedLineup() {
		TreeMap<Float, baseMap> rawMorphMaps = morphs[currMorphTypeIDX].buildLineupOfFrames(currUIVals.getNumLineupFrames()); 
		//win.getMsgObj().dispInfoMessage("mapManager::"+name, "buildOrientedLineup", "# of morph maps: " + rawMorphMaps.size() + " lineup num requested :" +currUIVals.numLineupFrames + " maps types : " + maps[0].mapTitle+" | " + maps[1].mapTitle);
		lineUpMorphMaps.clear();
		for(Float t : rawMorphMaps.keySet()) {
			baseMap tmpMorphMap = rawMorphMaps.get(t);
			lineUpMorphMaps.put(t, calcDifferenceBetweenMaps(tmpMorphMap, maps[0]));
		}		
	}

	//////////////
	// draw routines
	
	/**
	 * draw a point of a particular radius
	 * @param p point to draw
	 * @param rad radius of point
	 */
	public void _drawPt(myPointf _p, float _rad) {
		pa.pushMatrix();pa.pushStyle();	
		pa.translate(_p);
		pa.sphere(_rad);
		pa.popStyle();pa.popMatrix();	
	}
	
	public void _drawVec(myPointf _p, myPointf _pEnd, int[] _strkClr, float _rad) {
		pa.pushMatrix();pa.pushStyle();	
		pa.stroke(_strkClr[0],_strkClr[1],_strkClr[2],255);
		pa.line(_p.x, _p.y,_p.z,_pEnd.x, _pEnd.y,_pEnd.z);
		pa.translate(_pEnd);
		pa.sphere(_rad);
		pa.popStyle();pa.popMatrix();	
	}
	
	/**
	 * main map drawing - dependent on wireframe/filled flag
	 * @param fillOrWf
	 * @param drawMap
	 * @param drawCircles
	 * @param drawCopy
	 */
	public final void drawMaps_Main(boolean debug, boolean drawMap, boolean fillOrWf, boolean drawCircles, boolean drawCopy) {
		if(drawMap) {
			if(fillOrWf) {		for(int i=0;i<maps.length;++i) {maps[i].drawMap_Fill();}}
			else {				for(int i=0;i<maps.length;++i) {maps[i].drawMap_Wf();}}
		}
		if(drawCircles) {
			if(fillOrWf) {		for(int i=0;i<maps.length;++i) {maps[i].drawMap_PolyCircles_Fill();}}		
			else {				for(int i=0;i<maps.length;++i) {maps[i].drawMap_PolyCircles_Wf();}}					
		}
		if(drawCopy) {			if(fillOrWf) {copyMap.drawMap_Fill();	} else {copyMap.drawMap_Wf();}}
	}//drawMaps_Main
	
	/**
	 * draw maps with no wireframe/filled dependence
	 * @param drawTexture
	 * @param drawOrtho
	 * @param drawEdgeLines
	 */
	public final void drawMaps_Aux(boolean debug, boolean drawTexture, boolean drawOrtho, boolean drawEdgeLines, boolean drawCntlPts, boolean showLbls, int _detail) {
		if(drawTexture)	{		for(int i=0;i<maps.length;++i) {maps[i].drawMap_Texture();}}
		if(drawOrtho) {			for(int i=0;i<maps.length;++i) {maps[i].drawOrthoFrame();}}
		if(drawEdgeLines) {		maps[0].drawMap_EdgeLines();}
		if(drawCntlPts) {
			int curModMapIDX = (null==currMseModMap ? -1 : currMseModMap.mapIdx);
			for(int i=0;i<maps.length;++i) {maps[i].drawMap_CntlPts(i==curModMapIDX, _detail);}
		}
		for(int i=0;i<maps.length;++i) {	maps[i].drawHeaderAndLabels(showLbls,_detail);}
	}//drawMaps_Aux
	
	/**
	 * manage morph display and evolution
	 * @param animTimeMod
	 */
	protected float morphSign = 1.0f;

	public final void drawAndAnimMorph(boolean debug, float animTimeMod,boolean drawMorphMap, boolean morphMapFillOrWf,  boolean drawSlices, boolean morphSlicesFillOrWf,boolean drawCircles, boolean drawCntlPts, boolean sweepMaps, boolean showLbls, int _detail) {
		morphs[currMorphTypeIDX].setMorphT(morphProgress);//sets t value and calcs morph
		pa.pushMatrix();pa.pushStyle();	
			pa.fill(0,0,0,255);
			pa.stroke(0,0,0,255);
			pa.strokeWeight(1.0f);
			//any instancing-morph-specific data
			morphs[currMorphTypeIDX].drawMorphSpecificValues(debug,drawCntlPts, showLbls);
		pa.popStyle();pa.popMatrix();	
		
		if(drawSlices) {
			morphs[currMorphTypeIDX].drawMorphSlices(morphSlicesFillOrWf, drawCircles, drawCntlPts, showLbls, _detail);			
		}
		if(drawMorphMap) {
			morphs[currMorphTypeIDX].drawMorphedMap(morphMapFillOrWf, drawCircles);	
			if(drawCntlPts){morphs[currMorphTypeIDX].drawMorphedMap_CntlPts(_detail);}
			morphs[currMorphTypeIDX].drawHeaderAndLabels(showLbls,_detail);
		}
		if(sweepMaps) {
			morphProgress += (morphSign * (animTimeMod * morphSpeed));			
			if(morphProgress > 1.0f) {morphProgress = 1.0f;morphSign = -1.0f;} else if (morphProgress < 0.0f) {	morphProgress = 0.0f;	morphSign = 1.0f;}		
			currUIVals.setMorphProgress(morphProgress);
		}
	}

	/**
	 * draw oriented frames of map morph, side by side, at bottom of screen - should be called by drawHeader_Priv, which is already 2d
	 * @param fillOrWf
	 */
	public final void drawMaps_LineupFrames(boolean fillOrWf, boolean drawCircles, boolean drawTexture) {
		pa.pushMatrix();pa.pushStyle();	
		pa.setStroke(new int[] {0, 0,0}, 255);
		pa.setStrokeWt(2.0f);
		pa.setFill(new int[] {245, 255,232},255);
		pa.translate(0.0f,win.rectDim[3]-perLineupImageWidth,0.0f);
		pa.rect(lineupRectDims[0],lineupRectDims[1],lineupRectDims[2],lineupRectDims[3]);		
		
		pa.setFill(new int[] {0, 222,232},255);
		for(Float t : lineUpMorphMaps.keySet()) {
			pa.pushMatrix();pa.pushStyle();
				pa.translate(10.0f, 10.0f, 0.0f);
				pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Black, 255), 6.0f, "t = " + String.format(baseMap.strPointDispFrmt6,t));
			pa.popStyle();pa.popMatrix();
			baseMap tmpMorphMap = lineUpMorphMaps.get(t);

			pa.pushMatrix();pa.pushStyle();
				pa.translate(.5f*perLineupImageWidth, .5f*perLineupImageWidth, 0.0f);
				tmpMorphMap.drawMap_LineUp(true, drawCircles, drawTexture, perLineupImageWidth);
				//pa.ellipse(0.0f, 0.0f, .45f*perLineupImageWidth, .45f*perLineupImageWidth);
			pa.popStyle();pa.popMatrix();	
			
			pa.translate(perLineupImageWidth, 0.0f, 0.0f);
			pa.line(0.0f,lineupRectDims[1], 0.0f, 0.0f, perLineupImageWidth+ lineupRectDims[1], 0.0f );
		}			
		pa.popStyle();pa.popMatrix();	
	}
	
	public final void drawMorphedMap_CntlPtTraj(int _detail) {
		morphs[currMorphTypeIDX].drawMorphedMap_CntlPtTraj(_detail);
	}
	
	protected final float _drawRightSideMorphMap(float _yOff, float sideBarYDisp) {
		pa.translate(-10.0f, sideBarYDisp, 0.0f);	
		pa.showOffsetText(0,IRenderInterface.gui_Cyan, "Current Morph Map : ");
		pa.translate(10.0f, sideBarYDisp, 0.0f);					
		_yOff = morphs[currMorphTypeIDX].drawMapRtSdMenuDescr(_yOff, sideBarYDisp);
		pa.translate(-10.0f, 0.0f, 0.0f);
		return _yOff;
	}
	protected final float _drawRightSideMorphSlices(float _yOff, float sideBarYDisp) {
		pa.translate(-10.0f, sideBarYDisp, 0.0f);	
		pa.showOffsetText(0,IRenderInterface.gui_Cyan, "Morph Slices : ");
		pa.translate(10.0f, sideBarYDisp, 0.0f);					
		_yOff = morphs[currMorphTypeIDX].drawMorphSliceRtSdMenuDescr(_yOff, sideBarYDisp);
		pa.translate(-10.0f, 0.0f, 0.0f);
		return _yOff;
	}

	
	public final float drawRightSideMaps(float _yOff, float sideBarYDisp, boolean drawRegCopy, boolean drawMorph,  boolean drawMorphSlicesRtSideInfo) {
		pa.showOffsetText(0,IRenderInterface.gui_Cyan,  "Current Maps : " + mapTypes[mapType] + " Maps : ");
		
		pa.translate(10.0f, sideBarYDisp, 0.0f);		
		for(int i=0; i<maps.length;++i) {			_yOff = maps[i].drawRightSideBarMenuDescr(_yOff, sideBarYDisp, true, true);		}
		if(drawRegCopy) {_yOff = copyMap.drawRightSideBarMenuDescr(_yOff, sideBarYDisp, true, true);}
		if(drawMorph) {			_yOff = _drawRightSideMorphMap(_yOff, sideBarYDisp);	}		
		pa.translate(-10.0f, sideBarYDisp, 0.0f);	
		pa.showOffsetText(0,IRenderInterface.gui_Cyan, "Current Morph : ");
		pa.translate(10.0f, sideBarYDisp, 0.0f);		
		//_yOff += sideBarYDisp;
		pa.showOffsetText(0,IRenderInterface.gui_Green, morphs[currMorphTypeIDX].morphTitle + " Morph : ");
		//_yOff += sideBarYDisp;
		pa.translate(10.0f, sideBarYDisp, 0.0f);
		_yOff = morphs[currMorphTypeIDX].drawMorphRtSdMenuDescr(_yOff, sideBarYDisp,morphSpeed);
		if(drawMorphSlicesRtSideInfo) {	_yOff = _drawRightSideMorphSlices(_yOff, sideBarYDisp);}

		return _yOff;
	}
	

	
	/**
	 * draw current morph values on right side menu/display
	 * @param _yOff
	 * @return
	 */	
	protected final float drawCurrentMorph(float _yOff, float sideBarYDisp, float morphSpeed) {// , String[] morphScopes) {
		pa.showOffsetText(0,IRenderInterface.gui_Cyan, morphs[currMorphTypeIDX].morphTitle + " Morph : ");
		_yOff += sideBarYDisp;
		pa.translate(10.0f, sideBarYDisp, 0.0f);
		_yOff = morphs[currMorphTypeIDX].drawMorphRtSdMenuDescr(_yOff, sideBarYDisp, morphSpeed);//,morphScopes);
		return _yOff;
	}
	
	///////////////
	// setters/getters
	
	public final void setLineupRectDims() {
		lineupRectDims = win.getOrientedDims();
		perLineupImageWidth = lineupRectDims[2]/(1.0f*currUIVals.getNumLineupFrames());
		lineupRectDims[3] = perLineupImageWidth;
	}
	
	public final void setFromAndToCopyIDXs(int _fromIdx, int _toIdx) {
		fromMapIDX = _fromIdx;		toMapIDX = _toIdx;
		
	}
	
	/**
	 * this will set whether the morphing mechanism will use per-freature morphs or a single global morph
	 * @param isPerFeature
	 */
	public final void setGlobalOrPerFeatureMorphs(boolean isPerFeature) {	
		
		
		
		
		
	}//setGlobalOrPerFeatureMorphs	
	
	/**
	 * this will reset branching on all maps that use branching
	 */
	public final void resetAllBranching() {
		boolean[] flags = new boolean[] {true};			//idx 0 is branching
		for (int i=0;i<maps.length;++i) {	maps[i].setFlags(new boolean[] {true});}
		for (int i=0;i<morphs.length;++i) {morphs[i].resetAllBranching();}		
	}
	
	public final void resetBranchingMap(int mapIDX) {
		
		
	}
	
	public final void updateMapValsFromUI(mapUpdFromUIData upd) {
		currUIVals.setAllVals(upd);
		currMorphTypeIDX = currUIVals.getCurrMorphTypeIDX();
		morphProgress = currUIVals.getMorphProgress();
        
		morphSpeed = currUIVals.getMorphSpeed(); 
		
		setLineupRectDims();
		for(int i=0;i<this.morphs.length;++i) {	morphs[i].setMorphSlices(currUIVals.getNumMorphSlices());}
		morphMap.updateMapVals_FromUI(currUIVals);
		for(int j=0;j<maps.length;++j) {	maps[j].updateMapVals_FromUI(currUIVals);}
		morphs[currMorphTypeIDX].mapCalcsAfterCntlPointsSet(name + "::updateMapValsFromUI");
	}
	
	/**
	 * this will return true if the current morph type uses registration - currently only CarrierSimRegTransIDX
	 * @return
	 */
	public final boolean checkCurrMorphUsesReg() {
		return baseMorphManager.CarrierSimRegTransIDX==currMorphTypeIDX;
	}
	
	////////////////////
	// mouse/keyboard ui interaction 
	
	/**
	 * reset corners of all maps
	 */	
	public final void resetAllMapCorners() {
		morphMap.resetCntlPts(bndPts[0]);
		for(int j=0;j<maps.length;++j) {		maps[j].resetCntlPts(bndPts[j]);	}	
		morphs[currMorphTypeIDX].mapCalcsAfterCntlPointsSet(name + "::resetAllMapCorners");
		
	}	
	/**
	 * reset all instances of either "floor"/A or "ceiling"/B map
	 * @param mapIDX
	 */
	public final void resetMapCorners(int mapIDX) {	maps[mapIDX].resetCntlPts(bndPts[mapIDX]);morphs[currMorphTypeIDX].mapCalcsAfterCntlPointsSet(name + "::resetMapCorners");}
	/**
	 * match map destIDX corners to map srcIDX's corners
	 */
	public final void matchAllMapCorners(int srcIDX, int destIDX) {
		myPointf[] rawPts0 = maps[srcIDX].getCntlPts(), newPts = new myPointf[rawPts0.length];
		for(int j=0;j<rawPts0.length;++j) {	newPts[j] = myPointf._add(myPointf._sub(rawPts0[j], bndPts[srcIDX][j]), bndPts[destIDX][j]);}			
		maps[destIDX].resetCntlPts(newPts);	
		morphs[currMorphTypeIDX].mapCalcsAfterCntlPointsSet(name + "::matchAllMapCorners");
				
	}//matchAllMapCorners	

	//minimum distance for click to be considered within range of control point
	public static final float minSqClickDist = 2500.0f;
	
	/**
	 * determine whether a relevant mouse click has occurred in the maps this mgr manages
	 * @param mouseX
	 * @param mouseY
	 * @param mseClckInWorld
	 * @param mseBtn
	 * @param keyPressed
	 * @return
	 */
	public final boolean hndlMouseClickInMaps(int mouseX, int mouseY, myPoint mseClckInWorld, int mseBtn, char keyPressed) {
		//check every map for closest control corner to click location
		TreeMap<Float,baseMap>  mapDists = new TreeMap<Float,baseMap>();
		//msgObj.dispInfoMessage("COTS_Morph3DWin", "hndlMouseClickIndiv", "Mouse button pressed : " + mseBtn + " Key Pressed : " + keyPressed + " Key Coded : " + keyCodePressed);
		//get a point on ray through mouse location in world
		myPointf _rayOrigin = pa.c.getMseLoc_f();
		myVectorf _rayDir = pa.c.getEyeToMouseRay_f();
		myPointf mseLocInWorld_f = win.getMouseClkPtInWorld(mseClckInWorld,mouseX,mouseY);
		
		for(int j=0;j<maps.length;++j) {	
			mapDists.put(maps[j].findClosestCntlPt(mseLocInWorld_f, _rayOrigin, _rayDir), maps[j]);
		}
		Float minSqDist = mapDists.firstKey();
		if((minSqDist < minSqClickDist) || (keyPressed=='s') || (keyPressed=='S')  || (keyPressed=='r') || (keyPressed=='R')|| (keyPressed=='t') || (keyPressed=='T'))  {
			currMseModMap = mapDists.get(minSqDist);
			return true;
		}
		currMseModMap = null;
		return false;
	}

	
	/**
	 * handle mouse drag on map
	 * @param defVec
	 * @param mseClickIn3D_f
	 * @param key
	 * @param keyCode
	 */
	public final boolean mseDragInMap(myVectorf defVec, myPointf mseClickIn3D_f,  char key, int keyCode) {	
		if(currMseModMap == null) {return false;}
		boolean isScale = (key=='s') || (key=='S'), isRotation = (key=='r') || (key=='R'), isTranslation = (key=='t')||(key=='T');
		boolean performFinalIndiv = true;
		if(isScale || isRotation || isTranslation) {
			if(isScale) {								currMseModMap.dilateMap_MseDrag(defVec);			} 
			else if(isRotation) {						currMseModMap.rotateMapInPlane_MseDrag(mseClickIn3D_f, defVec);		}	//isRotation
			else if(isTranslation) {					currMseModMap.moveMapInPlane(defVec);}
		} else {							//cntl point movement			
			performFinalIndiv = currMseModMap.mseDragPickedCntlPt(defVec);
		}
		if(performFinalIndiv) {		//some editing happened, so finalize
			currMseModMap.mseDragInMap_Post(defVec,mseClickIn3D_f,isScale, isRotation, isTranslation, key, keyCode);	
			morphs[currMorphTypeIDX].mapCalcsAfterCntlPointsSet(name + "::mseDragInMap");
		}
		return performFinalIndiv;
	}//mseDragInMap
	
	
	public final void hndlMouseRelIndiv() {
		morphMap.mseRelease();
		for(int i=0;i<maps.length;++i) {	maps[i].mseRelease();}
		morphs[currMorphTypeIDX].mapCalcsAfterCntlPointsSet(name + "::hndlMouseRelIndiv");
		currMseModMap = null;
	}	
	

}//class mapManager
