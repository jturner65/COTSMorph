package COTS_Morph_PKG.ui.base;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

import COTS_Morph_PKG.maps.COTSMap;
import COTS_Morph_PKG.maps.TriangleBiLinMap;
import COTS_Morph_PKG.CarrierSimTransformMorph;
import COTS_Morph_PKG.maps.BiLinMap;
import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.morphs.CarrierSimDiagMorph;
import COTS_Morph_PKG.morphs.DualCarrierSimMorph;
import COTS_Morph_PKG.morphs.LERPMorph;
import COTS_Morph_PKG.morphs.LogPolarMorph;
import COTS_Morph_PKG.morphs.PointNormalMorph;
import COTS_Morph_PKG.morphs.base.baseMorph;
import COTS_Morph_PKG.similarities.SpiralSimilarityWithTranslation;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_UI_Objects.IRenderInterface;
import base_UI_Objects.my_procApplet;
import base_UI_Objects.drawnObjs.myDrawnSmplTraj;
import base_UI_Objects.windowUI.myDispWindow;
import base_Utils_Objects.io.MsgCodes;
import base_Utils_Objects.vectorObjs.myPoint;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVector;
import base_Utils_Objects.vectorObjs.myVectorf;
import processing.core.PImage;

public abstract class COTS_MorphWin extends myDispWindow {
	
	//ui vars
	public static final int
		gIDX_MorphTVal 			= 0,
		gIDX_MorphSpeed			= 1,
		gIDX_NumCellsPerSide    = 2,
		gIDX_MapType			= 3,
		gIDX_MorphType			= 4,
		gIDX_SetBrnchOff		= 5//,			//whether branching should be forced from Edit, forced from A, forced from B, or not shared
		//gIDX_MorphScope			= 6;			//whether animation interpolation only operates on control points, or whether it also works on internal values (such as cots fixed point, angle and scale)
		;
	protected static final int numBaseCOTSWinUIObjs = 6;
	
	//boolean priv flags
	public static final int 
		debugAnimIDX 			= 0,				//debug
		resetMapCrnrsIDX		= 1,
		
		resetMapCrnrs_0IDX		= 2,
		resetMapCrnrs_1IDX		= 3,
		
		matchMapCrnrs_0IDX		= 4,				//match map corners - map 1 will get corners set from map 0
		matchMapCrnrs_1IDX		= 5,				//match map corners - map 0 will get corners set from map 1
		
		findDiffFromAtoBIDX		= 6,				//find angle, scale and displacement from A to B
		findDiffFromBToAIDX		= 7,				//find angle, scale and displacement from B to A
		
		//////////////////////
		// drawing map flags
		drawMapIDX				= 8,				//draw map grid
		drawMap_CntlPtsIDX		= 9,				//draw map control poin
		drawMap_FillOrWfIDX		= 10,				//draw either filled checkerboards or wireframe for mapping grid
		drawMap_CellCirclesIDX 	= 11,				//draw inscribed circles within checkerboard cells
		drawMap_ImageIDX		= 12,				//draw the map's image
		drawMap_OrthoFrameIDX 	= 13, 				//draw orthogonal frame at map's center
		drawMap_CntlPtLblsIDX	= 14,				//draw labels for control points
		drawMap_CopyIDX			= 15,				//draw the copy map of the similarity between A and B
		drawMap_EdgeLinesIDX	= 16,				//draw the edge lines linking map A and B
		
		//////////////////////
		// drawing morph flags
		drawMorph_MapIDX		= 17,				//draw morph frame
		drawMorph_CntlPtTrajIDX = 18,				//show trajectory of COV and control pts
		drawMorph_FillOrWfIDX 	= 19,
		
		//////////////////////
		// animating morph flags		
		sweepMapsIDX			= 20;				//sweep from one mapping to other mapping
	protected static final int numBaseCOTSWinPrivFlags = 21;
	/**
	 * # of priv flags from base class and instancing class
	 */
	private int numPrivFlags;
	
	/**
	 * types of maps supported
	 */
	protected static final String[] mapTypes = new String[] {
		"Triangle",
		"Bilinear",
		"COTS",			
	};
	//need an index per map type
	public static final int
		triangleMapIDX		= 0,
		bilinearMapIDX		= 1,
		COTSMapIDX		 	= 2;	
	/**
	 * array holding maps
	 */
	protected baseMap[][] maps;
	/**
	 * idx's of maps to use for similarity mapping
	 */
	protected int fromMapIDX = -1, toMapIDX = -1;
	/**
	 * copy map to display if being copied or similarity mapped
	 */
	protected baseMap copyMap;
	
	//morph map in progress
	protected baseMap[] morphMaps;

	/**
	 * types of morphs supported
	 */
	protected static final String[] morphTypes = new String[] {
		"Point-Normal",
		"LERP",						//linearly interpolate control points
		"Carrier Sim Diag",
		"Carrier Sim Reg Trans",
		"Dual Carrier Similarity",
		"Log Polar"			
	};
	
	//need an index per morph type
	public static final int
		PointNormalIDX			= 0,
		LERPMorphIDX			= 1,
		CarrierSimDiagIDX		= 2,
		CarrierSimRegTransIDX	= 3,
		DualCarrierSimIDX		= 4,
		LogPolarMorphIDX 		= 5;
	
	/**
	 * array holding morphs
	 */
	protected baseMorph[] morphs;
	
	protected static final String[] morphScopes = new String[] {"Cntl Points Only", "Cntl Pts and Internal Vars"};
	protected static final String[] branchShareStrategies = new String[] {"No Branch Sharing", "Force from A", "Force from B", "Force from Edit"};
	
	/**
	 * map being currently modified by mouse interaction
	 */
	protected baseMap currMseModMap;
	//minimum distance for click to be considered within range of control point
	protected final float minSqClickDist = 2500.0f;
	/**
	 * morph animation variables
	 */
	protected float morphProgress = 0.5f, sign = 1.0f, morphSpeed = 1.0f;
	/**
	 * currently selected map type
	 */
	protected int currMapTypeIDX = COTSMapIDX;
	/**
	 * currently selected morph type
	 */
	protected int currMorphTypeIDX = LERPMorphIDX;
	/**
	 * scope of interpolation - either control points only (0), or internal variables along with control points (1)
	 */
	protected int currMorphScope = 1;
	/**
	 * method to share branches - 0 : no branch sharing, 1 : use map 0, 2 : use map 1, 3 : use most recent edit
	 */
	protected int currBranchShareStrategy = 0;
	/**
	 * currently set # of cells per side in grid
	 */
	protected int numCellsPerSide = 4;	
	/**
	 * colors for each of 2 maps' grids
	 */
	protected final int[][][] mapGridColors = new int[][][] {
		{{90,0,222,255},{0,225,10,255}},		//map grid 0
		{{255,200,0,255},{255,0,0,255}}		//map grid 1
	};
	
		//text scale value for display
	protected static final float txtSclVal = 1.25f;	
		//bounds for the key frame maps in this window
	public myPointf[][] bndPts;


	public String[][] menuBtnNames = new String[][] { // each must have literals for every button defined in side bar
		// menu, or ignored
		{ "---", "---", "---"}, // row 1
		{ "---", "---", "---", "---" }, // row 3
		{ "---", "---", "---", "---" }, // row 2
		{ "---", "---", "---", "---" }, 
		{ "---", "---", "---", "---", "---" } 
	};

	/**
	 * images to use for each map
	 */
	public PImage[] textureImgs;
		
	
	public COTS_MorphWin(my_procApplet _p, String _n, int _flagIdx, int[] fc, int[] sc, float[] rd, float[] rdClosed,String _winTxt) {
		super(_p, _n, _flagIdx, fc, sc, rd, rdClosed, _winTxt);
	}
	
	@Override
	protected final void initMe() {
		// capable of using right side menu
		setFlags(drawRightSideMenu, true);
		// init specific sim flags
		initPrivFlags(numPrivFlags);
		//initially set to show maps
		setPrivFlags(drawMapIDX,true);
		setPrivFlags(drawMap_FillOrWfIDX,true);
		setPrivFlags(drawMap_CntlPtsIDX, true);
		setPrivFlags(drawMorph_FillOrWfIDX, true);
		numCellsPerSide = (int) guiObjs[gIDX_NumCellsPerSide].getVal();
		//initialize the bounds for this map
		bndPts = getKeyFrameMapBndPts();
		//initialize all maps
		_initMaps();
		//initialize all morphs
		_initMorphs();
		
		currMseModMap = null;
	
		pa.setAllMenuBtnNames(menuBtnNames);

		initMe_Indiv();
	}//initMe
	/**
	 * initialize all maps - only call once
	 */
	private void _initMaps() {
		textureImgs = new PImage[2];
		textureImgs[0]=pa.loadImage("faceImage_0.jpg");
		textureImgs[1]=pa.loadImage("faceImage_1.jpg");
		
		maps = new baseMap[mapTypes.length][];
		for(int i=0;i<maps.length;++i){
			maps[i] = new baseMap[2];
			for(int j=0;j<maps[i].length;++j) {maps[i][j] = buildMapOfPassedType(i,j, "");}
		}
		fromMapIDX = 0;
		toMapIDX = 1;
		morphMaps = new baseMap[mapTypes.length];
		for(int i=0;i<morphMaps.length;++i) {	morphMaps[i]=buildMapOfPassedType(i,0, "Morph"); copyMap = buildCopyMapOfPassedMapType(maps[i][0], "Copy");}
				
		for(int i=0;i<maps.length;++i) {
			morphMaps[i].setImageToMap(textureImgs[0]);
			for(int j=0;j<maps[i].length;++j) {		maps[i][j].setImageToMap(textureImgs[j]);	maps[i][j].setOtrMap(maps[i][(j+1)%maps[i].length]);}
		}	
	}
	
	
	public baseMap buildCopyMapOfPassedMapType(baseMap mapA, String _mapNameSuffix) {	
		baseMap map;
		switch (mapA.mapTypeIDX) {
			case triangleMapIDX		: {	map = new TriangleBiLinMap((TriangleBiLinMap)mapA);		break;}
			case bilinearMapIDX 	: {	map = new BiLinMap((BiLinMap)mapA);		break;}
			case COTSMapIDX 		: {	map = new COTSMap((COTSMap)mapA); 		break;}
			default		:{
				msgObj.dispErrorMessage("COTS_MorphWin", "buildMapOfPassedType", "Error : Unable to duplicate passed map "+ mapA.mapTitle + " due to unknown map type : " + mapA.mapTypeIDX + " : "  +mapTypes[mapA.mapTypeIDX] + ". Returning null.");
				return null;
			}
		}
		map.mapTitle += ( _mapNameSuffix.length() == 0 ? "":  " " + _mapNameSuffix);
		return map;
	}//buildCopyMapOfPassedMapType		
	
	public baseMap buildMapOfPassedType(int _mapType, int _mapValIdx, String _mapNameSuffix) {
		//myPointf[][] bndPts = getKeyFrameMapBndPts();
		switch (_mapType) {
			case triangleMapIDX		: {	
				myPointf[] triBndPts = new myPointf[3];
				for(int i=0;i<triBndPts.length;++i) {		triBndPts[i]=bndPts[_mapValIdx][i];		}
				return new TriangleBiLinMap(this, triBndPts, _mapValIdx, _mapType,mapGridColors[_mapValIdx], numCellsPerSide, mapTypes[_mapType] + ( _mapNameSuffix.length() == 0 ? "":  " " + _mapNameSuffix) + " Map"+ _mapValIdx );}
			case bilinearMapIDX 	: {	return new BiLinMap(this, bndPts[_mapValIdx], _mapValIdx, _mapType,mapGridColors[_mapValIdx], numCellsPerSide, mapTypes[_mapType] + ( _mapNameSuffix.length() == 0 ? "":  " " + _mapNameSuffix) + " Map"+ _mapValIdx );}
			case COTSMapIDX 		: {	return new COTSMap(this, bndPts[_mapValIdx], _mapValIdx, _mapType,mapGridColors[_mapValIdx], numCellsPerSide,  mapTypes[_mapType] + ( _mapNameSuffix.length() == 0 ? "":  " " + _mapNameSuffix) + " Map"+ _mapValIdx);}
			default		:{
				msgObj.dispErrorMessage("COTS_MorphWin", "buildMapOfPassedType", "Error : Unable to build requested map due to unknown map type : " + _mapType + " : "  +mapTypes[_mapType] + ". Returning null.");
				return null;
			}			
		}
	}
		
	/**
	 * initialize all morphs - only call once
	 */
	private void _initMorphs() {
		morphs = new baseMorph[morphTypes.length];
		morphs[PointNormalIDX] = new PointNormalMorph(this,maps[currMapTypeIDX][0],  maps[currMapTypeIDX][1], morphMaps[currMapTypeIDX],currMorphScope);
		morphs[LERPMorphIDX] = new LERPMorph(this,maps[currMapTypeIDX][0],  maps[currMapTypeIDX][1], morphMaps[currMapTypeIDX],currMorphScope); 
		morphs[CarrierSimDiagIDX] = new CarrierSimDiagMorph(this,maps[currMapTypeIDX][0],  maps[currMapTypeIDX][1], morphMaps[currMapTypeIDX],currMorphScope); 		
		morphs[CarrierSimRegTransIDX] = new CarrierSimTransformMorph(this,maps[currMapTypeIDX][0],  maps[currMapTypeIDX][1], morphMaps[currMapTypeIDX],currMorphScope); 
		morphs[DualCarrierSimIDX] = new DualCarrierSimMorph(this,maps[currMapTypeIDX][0],  maps[currMapTypeIDX][1], morphMaps[currMapTypeIDX],currMorphScope); 
		morphs[LogPolarMorphIDX] = new LogPolarMorph(this,maps[currMapTypeIDX][0],  maps[currMapTypeIDX][1], morphMaps[currMapTypeIDX],currMorphScope);		
	}
	
	protected abstract void initMe_Indiv();
	
	/**
	 * return the initial bounds for the maps in the world space
	 * @return 2-d array of 4 points - first idx is map idx, 2nd idx is 4 points
	 */
	protected abstract myPointf[][] getKeyFrameMapBndPts();

	@Override
	public final void initAllPrivBtns(ArrayList<Object[]> tmpBtnNamesArray) {

		// add an entry for each button, in the order they are wished to be displayed
		// true tag, false tag, btn IDX
		tmpBtnNamesArray.add(new Object[] { "Debugging", "Debug", debugAnimIDX });
		tmpBtnNamesArray.add(new Object[] { "Resetting Maps", "Reset Maps", resetMapCrnrsIDX });
		tmpBtnNamesArray.add(new Object[] { "Resetting Map 0", "Reset Map 0", resetMapCrnrs_0IDX });
		tmpBtnNamesArray.add(new Object[] { "Resetting Map 1", "Reset Map 1", resetMapCrnrs_1IDX });
		tmpBtnNamesArray.add(new Object[] { "Matching Map 1 to Map 0 Crnrs", "Match Map 1 to Map 0 Crnrs", matchMapCrnrs_0IDX });
		tmpBtnNamesArray.add(new Object[] { "Matching Map 0 to Map 1 Crnrs", "Match Map 0 to Map 1 Crnrs", matchMapCrnrs_1IDX });
		
		tmpBtnNamesArray.add(new Object[] { "Finding Dist From A to B","Find Dist From A to B", findDiffFromAtoBIDX});	
		tmpBtnNamesArray.add(new Object[] { "Finding Dist From B to A","Find Dist From B to A", findDiffFromBToAIDX});			
		
		tmpBtnNamesArray.add(new Object[] { "Showing Maps", "Show Maps",drawMapIDX});
		tmpBtnNamesArray.add(new Object[] { "Showing Registration Map", "Show Registration Maps",drawMap_CopyIDX});
		tmpBtnNamesArray.add(new Object[] { "Showing Ortho Frame", "Show Ortho Frame",drawMap_OrthoFrameIDX});
		tmpBtnNamesArray.add(new Object[] { "Showing Cntl Pts", "Show Cntl Pts",drawMap_CntlPtsIDX});
		tmpBtnNamesArray.add(new Object[] { "Showing Cntl Pt Lbls", "Show Cntl Pt Lbls",drawMap_CntlPtLblsIDX});
		tmpBtnNamesArray.add(new Object[] { "Show Checkerboard Maps", "Show Wireframe Maps",drawMap_FillOrWfIDX});
		tmpBtnNamesArray.add(new Object[] { "Showing Cell Circles", "Show Cell Circles",drawMap_CellCirclesIDX});
		tmpBtnNamesArray.add(new Object[] { "Showing Edge Lines", "Show Edge Lines",drawMap_EdgeLinesIDX});
		tmpBtnNamesArray.add(new Object[] { "Showing Map Image", "Show Map Image",drawMap_ImageIDX});
		
		tmpBtnNamesArray.add(new Object[] { "Showing Morph Map", "Show Morph Map",drawMorph_MapIDX});
		tmpBtnNamesArray.add(new Object[] { "Show Morph Checkerboard", "Show Morph Wireframe",drawMorph_FillOrWfIDX});
		tmpBtnNamesArray.add(new Object[] { "Showing Morph Cntlpt Traj", "Show Morph Cntlpt Traj",drawMorph_CntlPtTrajIDX});
		
		tmpBtnNamesArray.add(new Object[] { "Running Morph Sweep", "Run Morph Sweep", sweepMapsIDX});
		
		//instance-specific buttons
		numPrivFlags = initAllPrivBtns_Indiv(tmpBtnNamesArray);
		
	}//initAllPrivBtns	
	protected abstract int initAllPrivBtns_Indiv(ArrayList<Object[]> tmpBtnNamesArray);

	/**
	 * init ui objects from maps, keyed by ui object idx, with value being data
	 * @param tmpUIObjArray : map of object data, keyed by UI object idx, with array values being :                    
	 *           the first element double array of min/max/mod values                                                   
	 *           the 2nd element is starting value                                                                      
	 *           the 3rd elem is label for object                                                                       
	 *           the 4th element is boolean array of {treat as int, has list values, value is sent to owning window}    
	 * @param tmpListObjVals
	 */
	@Override
	protected final void setupGUIObjsAras(TreeMap<Integer, Object[]> tmpUIObjArray, TreeMap<Integer, String[]> tmpListObjVals) {
		tmpListObjVals.put(gIDX_MapType, mapTypes);		
		tmpListObjVals.put(gIDX_MorphType, morphTypes);
		//tmpListObjVals.put(gIDX_MorphScope, morphScopes);
		tmpListObjVals.put(gIDX_SetBrnchOff, branchShareStrategies);
		
		tmpUIObjArray.put(gIDX_MorphTVal,new Object[] { new double[] { 0.0, 1.0, 0.01 }, 1.0* morphProgress,"Progress of Morph", new boolean[] { false, false, true } }); 	
		tmpUIObjArray.put(gIDX_MorphSpeed,new Object[] { new double[] { 0.0, 2.0, 0.01 }, 1.0* morphSpeed,"Speed of Morph Animation", new boolean[] { false, false, true } }); 	
		tmpUIObjArray.put(gIDX_NumCellsPerSide,new Object[] { new double[] { 2.0, 100.0, 1.0 }, 1.0* numCellsPerSide, "# of Cells Per Grid Side", new boolean[]{true, false, true}}); 
		
		tmpUIObjArray.put(gIDX_SetBrnchOff,new Object[] { new double[]{0.0, tmpListObjVals.get(gIDX_SetBrnchOff).length-1, 1},1.0* currBranchShareStrategy, "Branch Sharing Strategy", new boolean[]{true, true, true}});
		tmpUIObjArray.put(gIDX_MapType,new Object[] { new double[]{0.0, tmpListObjVals.get(gIDX_MapType).length-1, 1},1.0* currMapTypeIDX, "Map Type to Show", new boolean[]{true, true, true}}); 
		
		tmpUIObjArray.put(gIDX_MorphType,new Object[] { new double[]{0.0, tmpListObjVals.get(gIDX_MorphType).length-1, 1},1.0* currMorphTypeIDX, "Morph Type to Process", new boolean[]{true, true, true}}); 
		//tmpUIObjArray.put(gIDX_MorphScope,new Object[] { new double[]{0.0, tmpListObjVals.get(gIDX_MorphScope).length-1, 1},1.0* currMorphScope, "Scope of Morph", new boolean[]{true, true, true}}); 
		
		setupGUIObjsAras_Indiv(tmpUIObjArray, tmpListObjVals);
	}//setupGUIObjsAras
	protected abstract void setupGUIObjsAras_Indiv(TreeMap<Integer, Object[]> tmpUIObjArray, TreeMap<Integer, String[]> tmpListObjVals);
	
	@Override
	protected final void setUIWinVals(int UIidx) {
		float val = (float) guiObjs[UIidx].getVal();
		int ival = (int) val;
		switch (UIidx) {	
			case gIDX_MorphTVal : {			//morph value	
				if(val != morphProgress) {				morphProgress = val;}
				break;}		
			case gIDX_MorphSpeed : {		//multiplier for animating morph
				if(val != morphSpeed) {					morphSpeed = val;}
				break;}
			case gIDX_NumCellsPerSide : {	//# of cells per side for Map grid
				if(numCellsPerSide != ival) {			numCellsPerSide = ival;	updateMapVals();}
				break;}
			case gIDX_MapType : {
				if(currMapTypeIDX != ival) {			currMapTypeIDX = ival;	updateMorphsWithCurrMaps();}				
				break;}
			case gIDX_MorphType : {
				if(currMorphTypeIDX != ival) {			
					currMorphTypeIDX = ival;
					if(CarrierSimRegTransIDX==currMorphTypeIDX) {findDifferenceBetweenMaps(true, false);}
				}						 	
				break;}
			case gIDX_SetBrnchOff : {
				if(currBranchShareStrategy != ival) {	currBranchShareStrategy = ival; updateMapVals();}		
				break;}
//			case gIDX_MorphScope : {
//				if(currMorphScope != ival) {			currMorphScope = ival;	updateMorphsWithMorphScope();}	
//				break;}
			default : {setUIWinVals_Indiv(UIidx, val);}
		}

	}//setUIWinVals
	protected abstract void setUIWinVals_Indiv(int UIidx, float val);
	
	protected final void updateMapVals() {
		mapUpdFromUIData upd = new mapUpdFromUIData(new int[] {numCellsPerSide, currBranchShareStrategy},new boolean[] {false});
		for(int i=0;i<maps.length;++i) {
			morphMaps[i].updateMapVals_FromUI(upd);
			for(int j=0;j<maps[i].length;++j) {		maps[i][j].updateMapVals_FromUI(upd);}			
		}
	}
	
	/**
	 * called whenever selected map or morph is changed
	 */
	protected final void updateMorphsWithCurrMaps() {		
		for(int i=0;i<morphs.length;++i) {morphs[i].setMaps( maps[currMapTypeIDX][0],  maps[currMapTypeIDX][1], morphMaps[currMapTypeIDX]);}
	}
//	protected final void updateMorphsWithMorphScope() {
//		for(int i=0;i<morphs.length;++i) {morphs[i].setMorphScope(currMorphScope);}
//	}

	@Override
	public final void setPrivFlags(int idx, boolean val) {
		int flIDX = idx / 32, mask = 1 << (idx % 32);
		privFlags[flIDX] = (val ? privFlags[flIDX] | mask : privFlags[flIDX] & ~mask);
		switch (idx) {// special actions for each flag
			case debugAnimIDX				: {			break;		}
			case resetMapCrnrsIDX			: {			
				if(val) {		resetAllMapCorners();	addPrivBtnToClear(resetMapCrnrsIDX);		}
				break;		}
			case resetMapCrnrs_0IDX			: {			
				if(val) {		resetMapCorners(0);		addPrivBtnToClear(resetMapCrnrs_0IDX);		}
				break;		}
			case resetMapCrnrs_1IDX			: {			
				if(val) {		resetMapCorners(1);		addPrivBtnToClear(resetMapCrnrs_1IDX);		}
				break;		}
			case matchMapCrnrs_0IDX			: {
				if(val) {		matchAllMapCorners(0,1);	addPrivBtnToClear(matchMapCrnrs_0IDX);	}
				break;}
			case matchMapCrnrs_1IDX			: {
				if(val) {		matchAllMapCorners(1,0);	addPrivBtnToClear(matchMapCrnrs_1IDX);	}
				break;}
			
			case findDiffFromAtoBIDX			: {
				if(val) {		fromMapIDX = 0; toMapIDX = 1;findDifferenceBetweenMaps(true, true);	addPrivBtnToClear(findDiffFromAtoBIDX);	}
				break;}
			case findDiffFromBToAIDX			: {
				if(val) {		fromMapIDX = 1; toMapIDX = 0;findDifferenceBetweenMaps(true, true);	addPrivBtnToClear(findDiffFromBToAIDX);	}
				break;}
						
			case drawMapIDX					: {			break;		}
			case drawMap_CntlPtsIDX			: {			break;		}
			case drawMap_FillOrWfIDX		: { 		break;		}
			case drawMap_CellCirclesIDX 	: {			break;		}
			case drawMap_EdgeLinesIDX		: {			break;		}
			case drawMap_ImageIDX			: {			break;		}
			case drawMap_OrthoFrameIDX		: {			break;		}
			case drawMap_CntlPtLblsIDX		: {			break;		}
			case drawMap_CopyIDX			: {
				if (val) {findDifferenceBetweenMaps(true, true);}
				break;		}
			case drawMorph_MapIDX			: {			break;		}	
			case drawMorph_CntlPtTrajIDX 	: {			break;		}
			case drawMorph_FillOrWfIDX		: {			break;		}
			case sweepMapsIDX				: {			break;		}
			default 			: {setPrivFlags_Indiv(idx,val);}
		}
	}
	protected abstract void setPrivFlags_Indiv(int idx, boolean val);

	public void findDifferenceBetweenMaps(boolean setCopyMap, boolean dispMod) {
		
		baseMap fromMap = maps[currMapTypeIDX][fromMapIDX];
		baseMap toMap = maps[currMapTypeIDX][toMapIDX];
		
		myVectorf dispBetweenMaps = new myVectorf();
		float[] angleAndScale = new float[2];
		toMap.findDifferenceToMe(fromMap, dispBetweenMaps, angleAndScale);
		if(dispMod) {
			msgObj.dispInfoMessage("COTS_MorphWin", "findDiffBetweenMaps", "Distance " + fromMap.mapTitle + " -> " + toMap.mapTitle + " | Displacement of COV : " +  dispBetweenMaps.toStrBrf() + " | Angle between Maps : " + angleAndScale[0] + " | Geometric Means Scale :" + angleAndScale[1]);
		}
		if(setCopyMap) {		
			copyMap = buildCopyMapOfPassedMapType(fromMap, "Copy");
			copyMap.registerMeToVals(dispBetweenMaps, angleAndScale);
		}
	}
	
	/**
	 * reset all instances of either "floor"/A or "ceiling"/B map
	 */	
	protected void resetAllMapCorners() {
		for(int i=0;i<morphMaps.length;++i) {			morphMaps[i].setCntlPts(bndPts[0], numCellsPerSide);		}
		for(int i=0;i<maps.length;++i) {			for(int j=0;j<maps[i].length;++j) {		maps[i][j].setCntlPts(bndPts[j], numCellsPerSide);	}		}			
	}
	/**
	 * reset all instances of either "floor"/A or "ceiling"/B map
	 * @param mapIDX
	 */
	protected void resetMapCorners(int mapIDX) {	for(int i=0;i<maps.length;++i) {	maps[i][mapIDX].setCntlPts(bndPts[mapIDX], numCellsPerSide);}}
	/**
	 * match map destIDX corners to map srcIDX's corners
	 */
	protected void matchAllMapCorners(int srcIDX, int destIDX) {
		//myPointf[][] bndPts = getKeyFrameMapBndPts();
		for(int i=0;i<maps.length;++i) {
			myPointf[] rawPts0 = maps[i][srcIDX].getCntlPts(), newPts = new myPointf[rawPts0.length];
			for(int j=0;j<rawPts0.length;++j) {	newPts[j] = myPointf._add(myPointf._sub(rawPts0[j], bndPts[srcIDX][j]), bndPts[destIDX][j]);}			
			maps[i][destIDX].setCntlPts(newPts, numCellsPerSide);	
		}		
	}//matchAllMapCorners	
	
	@Override
	protected final void setVisScreenDimsPriv() {
		

	}

	@Override
	protected final void setCustMenuBtnNames() {	}

	/////////////////////////////
	// draw routines
	@Override
	protected final void setCameraIndiv(float[] camVals) {
		// , float rx, float ry, float dz are now member variables of every window
		pa.camera(camVals[0], camVals[1], camVals[2], camVals[3], camVals[4], camVals[5], camVals[6], camVals[7], camVals[8]);
		// puts origin of all drawn objects at screen center and moves forward/away by dz
		pa.translate(camVals[0], camVals[1], (float) dz);
		setCamOrient();
	}
	@Override
	protected final void drawMe(float animTimeMod) {
		pa.pushMatrix();pa.pushStyle();
		int curModMapIDX = (null==currMseModMap ? -1 : currMseModMap.mapIdx);
		if(getPrivFlags(drawMapIDX)) {				
			if(getPrivFlags(drawMap_FillOrWfIDX)) {		for(int i=0;i<maps[currMapTypeIDX].length;++i) {maps[currMapTypeIDX][i].drawMap_Fill();}}
			else {										for(int i=0;i<maps[currMapTypeIDX].length;++i) {maps[currMapTypeIDX][i].drawMap_Wf();}}
		}
		if(getPrivFlags(drawMap_CellCirclesIDX)) {
			if(getPrivFlags(drawMap_FillOrWfIDX)) {		for(int i=0;i<maps[currMapTypeIDX].length;++i) {maps[currMapTypeIDX][i].drawMap_PolyCircles_Fill();}}		
			else {										for(int i=0;i<maps[currMapTypeIDX].length;++i) {maps[currMapTypeIDX][i].drawMap_PolyCircles_Wf();}}			
		}
		if(getPrivFlags(drawMap_CopyIDX)) {			
			if(getPrivFlags(drawMap_FillOrWfIDX)) { copyMap.drawMap_Fill();	}	
			else {									copyMap.drawMap_Wf();	}		
		}
		if(getPrivFlags(drawMap_EdgeLinesIDX)) {		maps[currMapTypeIDX][0].drawMap_EdgeLines();}
		
		if(getPrivFlags(drawMap_ImageIDX)) {			for(int i=0;i<maps[currMapTypeIDX].length;++i) {maps[currMapTypeIDX][i].drawMap_Texture();}}
		if(getPrivFlags(drawMap_CntlPtsIDX)){			for(int i=0;i<maps[currMapTypeIDX].length;++i) {maps[currMapTypeIDX][i].drawMap_CntlPts(i==curModMapIDX);}}
		if(getPrivFlags(drawMap_OrthoFrameIDX)) {		for(int i=0;i<maps[currMapTypeIDX].length;++i) {maps[currMapTypeIDX][i].drawOrthoFrame();}}
		if(getPrivFlags(drawMorph_CntlPtTrajIDX)) {		morphs[currMorphTypeIDX].drawMorphedMap_CntlPtTraj();}
		if(getPrivFlags(drawMorph_MapIDX)) {
			morphs[currMorphTypeIDX].setMorphT(morphProgress);
			morphs[currMorphTypeIDX].drawMorphedMap(getPrivFlags(drawMorph_FillOrWfIDX), getPrivFlags(drawMap_CellCirclesIDX));	
			if(getPrivFlags(drawMap_CntlPtsIDX)){morphs[currMorphTypeIDX].drawMorphedMap_CntlPts();}
		}
		if(getPrivFlags(sweepMapsIDX)) {
			morphProgress += (sign * (animTimeMod * morphSpeed));
			guiObjs[gIDX_MorphTVal].setVal(morphProgress);
			if(morphProgress > 1.0f) {morphProgress = 1.0f;sign = -1.0f;} else if (morphProgress < 0.0f) {	morphProgress = 0.0f;	sign = 1.0f;}
		}
		boolean showLbls = getPrivFlags(drawMap_CntlPtLblsIDX);
		for(int i=0;i<maps[currMapTypeIDX].length;++i) {maps[currMapTypeIDX][i].drawHeaderAndLabels(showLbls);}
		if(getPrivFlags(drawMorph_MapIDX)) {			morphs[currMorphTypeIDX].drawHeaderAndLabels(showLbls);}
		_drawMe_Indiv(animTimeMod);
		pa.popStyle();pa.popMatrix();	
	}
	
	protected abstract void _drawMe_Indiv(float animTimeMod);
	
	@Override
	public final void drawCustMenuObjs() {

	}

	@Override
	protected final void drawRightSideInfoBarPriv(float modAmtMillis) {
		float _yOff = yOff - 4;
		//start with yOff
		pa.pushMatrix();pa.pushStyle();
		pa.scale(1.05f);
		//draw map values
		_yOff = drawRightSideMaps(_yOff);
		//draw current morph values
		_yOff = drawCurrentMorph(_yOff);
		
		pa.popStyle();pa.popMatrix();	
	}
	/**
	 * draw map values on right side menu/display
	 * @param _yOff
	 * @return
	 */
	protected float sideBarYDisp = 12.0f;
	protected final float drawRightSideMaps(float _yOff) {
		pa.showOffsetText(0,IRenderInterface.gui_Cyan, mapTypes[currMapTypeIDX] + " Maps : ");
		_yOff += sideBarYDisp;
		pa.translate(10.0f, sideBarYDisp, 0.0f);		
		for(int i=0; i<maps[currMapTypeIDX].length;++i) {
			_yOff = maps[currMapTypeIDX][i].drawRightSideBarMenuDescr(_yOff, sideBarYDisp, true);
		}
		_yOff = copyMap.drawRightSideBarMenuDescr(_yOff, sideBarYDisp, true);
		if(getPrivFlags(drawMorph_MapIDX)) {
			_yOff = morphs[currMorphTypeIDX].drawMapRtSdMenuDescr(_yOff, sideBarYDisp);
		}		
		return _yOff;
	}
	/**
	 * draw current morph values on right side menu/display
	 * @param _yOff
	 * @return
	 */
	
	protected final float drawCurrentMorph(float _yOff) {
		pa.showOffsetText(0,IRenderInterface.gui_Cyan, morphTypes[currMorphTypeIDX] + " Morph : ");
		_yOff += sideBarYDisp;
		pa.translate(10.0f, sideBarYDisp, 0.0f);
		_yOff = morphs[this.currMorphTypeIDX].drawMorphRtSdMenuDescr(_yOff, sideBarYDisp,morphSpeed,morphScopes);
		return _yOff;
	}
	
	@Override
	protected final void drawOnScreenStuffPriv(float modAmtMillis) {
		

	}
		
	public abstract void _drawLabelAtPt(myPointf p, String lbl, float xOff, float yOff);	
	
	/////////////////////////////
	// window control
	@Override
	protected final void resizeMe(float scale) {}
	@Override
	protected final void showMe() {}
	@Override
	protected final void closeMe() {}
	@Override
	protected final boolean simMe(float modAmtSec) {	return false;}
	@Override
	protected final void stopMe() {}
	
	////////////////////////
	// keyboard and mouse
	
	//move without click
	@Override
	protected final boolean hndlMouseMoveIndiv(int mouseX, int mouseY, myPoint mseClckInWorld) {
		
		return false;
	}

	@Override
	protected final boolean hndlMouseClickIndiv(int mouseX, int mouseY, myPoint mseClckInWorld, int mseBtn) {
		//check every map for closest control corner to click location
		TreeMap<Float,baseMap>  mapDists = new TreeMap<Float,baseMap>();
		//msgObj.dispInfoMessage("COTS_Morph3DWin", "hndlMouseClickIndiv", "Mouse button pressed : " + mseBtn + " Key Pressed : " + keyPressed + " Key Coded : " + keyCodePressed);
		//get a point on ray through mouse location in world
		myPointf _rayOrigin = pa.c.getMseLoc_f();
		myVectorf _rayDir = pa.c.getEyeToMouseRay_f();
		myPointf mseLocInWorld_f = getMouseClkPtInWorld(mseClckInWorld,mouseX,mouseY);
		
		for(int j=0;j<maps[currMapTypeIDX].length;++j) {	
			mapDists.put(maps[currMapTypeIDX][j].findClosestCntlPt(mseLocInWorld_f, _rayOrigin, _rayDir), maps[currMapTypeIDX][j]);
		}
		Float minSqDist = mapDists.firstKey();
		if((minSqDist < minSqClickDist) || (keyPressed=='s') || (keyPressed=='S')  || (keyPressed=='r') || (keyPressed=='R'))  {
			currMseModMap = mapDists.get(minSqDist);
			return true;
		}
		currMseModMap = null;
		return false;
	}
	/**
	 * function to find location in map world of mouse click
	 * @param mseClckInWorld
	 * @param mouseX
	 * @param mouseY
	 * @return
	 */
	public abstract myPointf getMouseClkPtInWorld(myPoint mseClckInWorld,int mouseX, int mouseY);
	/**
	 * called by maps - finds distance to either passed point (in 2D) or passed ray cast (in 3D)
	 * @param _pt0
	 * @param _pt
	 * @param _rayOrigin
	 * @param _rayDir
	 * @return
	 */
	public abstract Float findDistToPtOrRay(myPointf _pt0, myPointf _pt, myPointf _rayOrigin, myVectorf _rayDir);	
	
	@Override
	protected final boolean hndlMouseDragIndiv(int mouseX, int mouseY, int pmouseX, int pmouseY, myPoint mouseClickIn3D, myVector mseDragInWorld, int mseBtn) {
		if(currMseModMap != null) {
			handleMapMseDrag(mouseX, mouseY, pmouseX, pmouseY, mouseClickIn3D, mseDragInWorld, mseBtn);
			if((CarrierSimRegTransIDX==currMorphTypeIDX) || (getPrivFlags(drawMap_CopyIDX))) {findDifferenceBetweenMaps(true, false);}
			return true;
		}
		return false;
	}
	
	/**
	 * handle map-specific mouse drag interaction
	 * @param mouseX mouse x
	 * @param mouseY mouse y
	 * @param pmouseX previous mouse x
	 * @param pmouseY previous mouse y
	 * @param mouseClickIn3D 3d location of mouse 
	 * @param mseDragInWorld displacement vector of mouse, in plane of screen normal
	 * @param mseBtn which button was pressed
	 */
	protected abstract void handleMapMseDrag(int mouseX, int mouseY, int pmouseX, int pmouseY, myPoint mouseClickIn3D, myVector mseDragInWorld, int mseBtn);

	@Override
	protected final void hndlMouseRelIndiv() {
		for(int i=0;i<maps.length;++i) {for(int j=0;j<maps[i].length;++j) {	maps[i][j].mseRelease();}}
		currMseModMap = null;
		mouseRelease_IndivMorphWin();
	}
	
	protected abstract void mouseRelease_IndivMorphWin();
	
	@Override
	protected final void snapMouseLocs(int oldMouseX, int oldMouseY, int[] newMouseLoc) {}
	
	@Override
	public final void handleSideMenuMseOvrDispSel(int btn, boolean val) {
		

	}
	
	@Override
	protected final void launchMenuBtnHndlr(int funcRow, int btn) {
		msgObj.dispMessage("COTS_MorphWin", "launchMenuBtnHndlr", "Begin requested action : Click Functions "+(funcRow+1)+" in " + name + " : btn : " + btn, MsgCodes.info4);
		switch (funcRow) {
			case 0: {// row 1 of menu side bar buttons
				// {"Gen Training Data", "Save Training data","Load Training Data"}, //row 1
				switch (btn) {
					case 0: {
						resetButtonState();
						break;
					}
					case 1: {
						resetButtonState();
						break;
					}
					case 2: {
						resetButtonState();
						break;
					}
					default: {
						msgObj.dispMessage("COTS_MorphWin", "launchMenuBtnHndlr", "Unknown Functions 1 btn : " + btn, MsgCodes.warning2);
						break;
					}
				}
				break;
			} // row 1 of menu side bar buttons
	
			case 1: {// row 2 of menu side bar buttons
				switch (btn) {
					case 0: {
						resetButtonState();
						break;
					}
					case 1: {
						resetButtonState();
						break;
					}
					case 2: {
						resetButtonState();
						break;
					}
					case 3: {// show/hide som Map UI
						resetButtonState();
						break;
					}
					default: {
						msgObj.dispMessage("COTS_MorphWin", "launchMenuBtnHndlr", "Unknown Functions 2 btn : " + btn, MsgCodes.warning2);
						resetButtonState();
						break;
					}
				}
				break;
			} // row 2 of menu side bar buttons
			case 2: {// row 3 of menu side bar buttons
				switch (btn) {
					case 0: {
						resetButtonState();
						break;
					}
					case 1: {
						resetButtonState();
						break;
					}
					case 2: {
						resetButtonState();
						break;
					}
					case 3: {
						resetButtonState();
						break;
					}
					default: {
						msgObj.dispMessage("COTS_MorphWin", "launchMenuBtnHndlr", "Unknown Functions 3 btn : " + btn,
								MsgCodes.warning2);
						resetButtonState();
						break;
					}
				}
				break;
			} // row 3 of menu side bar buttons
			case 3: {// row 3 of menu side bar buttons
				switch (btn) {
					case 0:
					case 1:
					case 2:
					case 3: {// load all training data, default map config, and build map
						resetButtonState();
						break;
					}
					default: {
						msgObj.dispMessage("COTS_MorphWin", "launchMenuBtnHndlr", "Unknown Functions 4 btn : " + btn, MsgCodes.warning2);
						resetButtonState();
						break;
					}
				}
				break;
			} // row 3 of menu side bar buttons
		}
		msgObj.dispMessage("COTS_MorphWin", "launchMenuBtnHndlr", "End requested action (multithreaded actions may still be working) : Click Functions "+(funcRow+1)+" in " + name + " : btn : " + btn, MsgCodes.info4);
	}

	@Override
	public final void handleSideMenuDebugSel(int btn, int val) {
		msgObj.dispMessage("COTS_MorphWin", "handleSideMenuDebugSel","Click Debug functionality in " + name + " : btn : " + btn, MsgCodes.info4);
		switch (btn) {
			case 0: {
				resetButtonState();
				break;
			}
			case 1: {
				resetButtonState();
				break;
			}
			case 2: {
				resetButtonState();
				break;
			}
			case 3: {// show current mapdat status
				resetButtonState();
				break;
			}
			case 4: {
				resetButtonState();
				break;
			}
			default: {
				msgObj.dispMessage("COTS_MorphWin", "handleSideMenuDebugSel", "Unknown Debug btn : " + btn,MsgCodes.warning2);
				resetButtonState();
				break;
			}
		}
		msgObj.dispMessage("COTS_MorphWin", "handleSideMenuDebugSel", "End Debug functionality selection.",MsgCodes.info4);
	}
	
	@Override
	protected final void endShiftKeyI() {}
	@Override
	protected final void endAltKeyI() {}
	@Override
	protected final void endCntlKeyI() {}
	
	///////////////////////
	// deprecated file io stuff
	@Override
	public final void hndlFileLoad(File file, String[] vals, int[] stIdx) {}
	@Override
	public final ArrayList<String> hndlFileSave(File file) {		return null;}
	@Override
	protected final String[] getSaveFileDirNamesPriv() {return null;	}
	
	////////////////////
	// drawn trajectory stuff
	@Override
	protected final void initDrwnTrajIndiv() {}
	@Override
	protected final void addSScrToWinIndiv(int newWinKey) {}
	@Override
	protected final void addTrajToScrIndiv(int subScrKey, String newTrajKey) {}
	@Override
	protected final void delSScrToWinIndiv(int idx) {}
	@Override
	protected final void delTrajToScrIndiv(int subScrKey, String newTrajKey) {}
	@Override
	protected final void processTrajIndiv(myDrawnSmplTraj drawnTraj) {}


}