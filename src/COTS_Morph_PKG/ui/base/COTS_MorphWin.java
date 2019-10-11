package COTS_Morph_PKG.ui.base;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

import COTS_Morph_PKG.mapManager.mapPairManager;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
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
		gIDX_MorphTVal 					= 0,
		gIDX_MorphSpeed					= 1,
		gIDX_NumCellsPerSide    		= 2,
		gIDX_MapType					= 3,	
		gIDX_MorphType					= 4,			//overall morphtype to use - can be overridden		
		gIDX_MorphTypeOrient			= 5,			//morph types to use for each characteristic of morph frames
		gIDX_MorphTypeSize				= 6,
		gIDX_MorphTypeShape				= 7,
		gIDX_MorphTypeCOVPath			= 8,				
		gIDX_SetBrnchStrat				= 9,			//whether branching should be forced from Edit, forced from A, forced from B, or not shared
		gIDX_NumLineupFrames			= 10,			//# of frames to use for lineup
		gIDX_NumMorphSlices 			= 11,			//# of slices in morph
		gIDX_CntlPtDispDetail			= 12,			//how much detail the cntrol point display will show
		gIDX_MorphAnalysisMmmntsDetail 	= 13,
		gIDX_DistTestTransform			= 14,			//transformation to use to measure distortion
		gIDX_DistDimToShow				= 15,			//which distortion dimension should be colored
		gIDX_MorphDistMult 				= 16;			//distortion multiplier to use to control colors for 
	protected static final int numBaseCOTSWinUIObjs = 17;
	/**
	 * structure to facilitate communicating UI changes with functional code
	 */
	protected mapUpdFromUIData uiUpdateData;
	/**
	 * possible branch sharing strategies
	 */
	protected static final String[] branchShareStrategies = new String[] {"No Branch Sharing", "Force from A", "Force from B", "Force from Edit"};
	protected static final String[] cntlPtDispDetail = new String[] {"Cntl Pts Only", "Cntl Pts & COV", "Cntl Pts, COV & Edge Pts", "All Pts"};
	protected static final String[] analysisMmmntsDetail = new String[] {"Mean & STD", "First 4 moments", "Mean & STD + Min & Max", "4 Mmnts + Min & Max"};	
	protected static final String[] distDimToShow  = new String[] {"Map Rows", "Map Columns", "Morph Slices"};
	
	public static final int 
		drawMapDet_CntlPtsOnlyIDX  				= 0,
		drawMapDet_CntlPts_COV_IDX				= 1,
		drawMapDet_CntlPts_COV_EdgePts_IDX		= 2,
		drawMapDet_CntlPts_COV_EdgePts_F_IDX 	= 3;
	protected int drawMapDetail = 1;//start showing control points and cov
	/**
	 * all possible moments to display in traj analysis
	 */
	public static final String[][] allMmntDispLabels = new String[][] {
		{"MEAN","STD"},
		{"MEAN","STD","SKEW","KURT"},
		{"MEAN","STD", "MIN","MAX"},
		{"MEAN","STD","SKEW","KURT", "MIN","MAX"}
	};
	/**
	 * current index in allMmntDispLabels representing the moments to display in moments analysis,
	 */
	public int currMmntDispIDX = 1;
	/**
	 * current branch sharing strategy - set when UI changes, but only used if sent via UI button
	 */
	protected int currBranchShareStrat = 0;

	/**
	 * array of arrays holding map managers - 1 array with quad-spiral morph, 1 with regular morph; 1 manager for each type of possible map, each manages both keyframe maps any interactions between them
	 */
	protected mapPairManager[] mapManagers;
	
	/**
	 * currently selected map type
	 */
	protected int currMapTypeIDX = mapPairManager.COTSMapIDX;	
	
	
	//boolean priv flags
	public static final int 
		debugAnimIDX 						= 0,				//debug
		resetMapCrnrsIDX					= 1,
		
		resetMapCrnrs_0IDX					= 2,				//reset map corners to start positions
		resetMapCrnrs_1IDX					= 3,
				
		matchMapCrnrs_0IDX					= 4,				//match map corners - map 1 will get corners set from map 0
		matchMapCrnrs_1IDX					= 5,				//match map corners - map 0 will get corners set from map 1
		                        		
		findDiffFromAtoBIDX					= 6,				//find angle, scale and displacement from A to B
		findDiffFromBToAIDX					= 7,				//find angle, scale and displacement from B to A
		findBestOrRegDistIDX				= 8,				//find best distance between quad verts - potentially reassigning verts, or find registration distance
		
		calcMorphDistIDX					= 9,				//calculate the current morph distortion
		
		setCurrCOTSBranchShareStratIDX 		= 10,				//send currently set COTS branch sharing strategy to map managers and update
		
		resetAllBranchingIDX				= 11,
		resetMapBranch_0IDX					= 12,				//reset map's branching to 0
		resetMapBranch_1IDX					= 13,
		
		//////////////////////
		// drawing map flags
		drawMapIDX							= 14,				//draw map grid
		drawMap_CntlPtsIDX					= 15,				//draw map control poin
		drawMap_FillOrWfIDX					= 16,				//draw either filled checkerboards or wireframe for mapping grid
		drawMap_CellCirclesIDX 				= 17,				//draw inscribed circles within checkerboard cells
		drawMap_ImageIDX					= 18,				//draw the map's image
		drawMap_OrthoFrameIDX 				= 19, 				//draw orthogonal frame at map's center
		drawMap_CntlPtLblsIDX				= 20,				//draw labels for control points
		drawMap_RegCopyIDX					= 21,				//draw the registration copy map of the similarity between A and B
		drawMap_EdgeLinesIDX				= 22,				//draw the edge lines linking map A and B
		
		//////////////////////
		// calc/drawing morph flags
		//usePerFtrMorph_IDX				= 22,				//whether to use per feature morphs or global morphs
		drawMorph_MapIDX					= 23,				//draw morph frame
		drawMorph_SlicesIDX					= 24,
		drawMorph_Slices_FillOrWfIDX		= 25,				//draw either filled checkerboards or wireframe for morph slices
		drawMorph_Slices_RtSideInfoIDX  	= 26,				//draw info about morph slices on right side menu
		drawMorph_CntlPtTrajIDX 			= 27,				//show trajectory of COV and control pts
		drawMorph_FillOrWfIDX 				= 28,
		drawMorph_DistColorsIDX				= 29,
		//////////////////////
		// animating morph flags		
		sweepMapsIDX						= 30,				//sweep from one mapping to other mapping
		showTrajAnalysisWinIDX				= 31,				//show display of graphs of trajectory analysis - either this or oriented lineup should be shown, not both
		showMorphAnalysisGraphsIDX			= 32,
		showMrphStackDistAnalysisWinIDX		= 33,
		showMrphStackDistAnalysisGraphsIDX	= 34,
		showOrientedLineupIDX				= 35;				//show keyframe display of morph, all registered and displayed side by side
	
	protected static final int numBaseCOTSWinPrivFlags = 36;
	/**
	 * # of priv flags from base class and instancing class
	 */
	private int numPrivFlags;
	//array of corner vals to set/reset
	protected myPointf[][][] crnrs;
	/**
	 * colors for each of 2 maps' grids
	 */
	protected static final int[][][] mapGridColors = new int[][][] {
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
		{ "Curr Crnrs 0", "Curr Crnrs 1", "Curr Crnrs 2", "---" }, // row 3
		{ "Curr Crnrs 0", "Curr Crnrs 1", "Curr Crnrs 2", "Orig Crnrs" }, // row 2
		{ "---", "---", "---", "---" }, 
		{ "---", "---", "---", "---", "---" } 
	};

	/**
	 * images to use for each map
	 */
	public PImage[] textureImgs;
		
	public final boolean is3D;
	
	public COTS_MorphWin(my_procApplet _p, String _n, int _flagIdx, int[] fc, int[] sc, float[] rd, float[] rdClosed,String _winTxt, boolean _is3D) {
		super(_p, _n, _flagIdx, fc, sc, rd, rdClosed, _winTxt);
		is3D = _is3D;
	}
	
	public abstract String getWinName();
	
	@Override
	protected final void initMe() {
		crnrs = new myPointf[10][][];
		// capable of using right side menu
		setFlags(drawRightSideMenu, true);
		uiUpdateData = new mapUpdFromUIData(this); 
		// init specific sim flags
		initPrivFlags(numPrivFlags);
		//initially set to show maps
		setPrivFlags(drawMapIDX,true);
		setPrivFlags(drawMap_FillOrWfIDX,true);
		setPrivFlags(drawMap_CntlPtsIDX, true);
		setPrivFlags(drawMorph_FillOrWfIDX, true);
		//numCellsPerSide = (int) guiObjs[gIDX_NumCellsPerSide].getVal();
		//after flags are set but before they are forced
		buildUIUpdateStruct();
		
		//initialize the bounds for this map
		bndPts = getKeyFrameMapBndPts();
		//initialize all maps
		_initMapManagers();
		//initialize all morphs
		//_initMorphs();
		//updateMapsWithCurrMorphs();
		pa.setAllMenuBtnNames(menuBtnNames);

		initMe_Indiv();
	}//initMe
	/**
	 * has to be called after UI structs are built and set
	 */
	private void buildUIUpdateStruct() {		
		TreeMap<Integer, Integer> intValues = new TreeMap<Integer, Integer>();          

		intValues.put(gIDX_NumCellsPerSide, (int) guiObjs[gIDX_NumCellsPerSide].getVal()); 
		intValues.put(gIDX_MapType,	(int) guiObjs[gIDX_MapType].getVal()); 		
		intValues.put(gIDX_MorphType, (int) guiObjs[gIDX_MorphType].getVal()); 			
		intValues.put(gIDX_MorphTypeOrient, (int) guiObjs[gIDX_MorphTypeOrient].getVal()); 	
		intValues.put(gIDX_MorphTypeSize, (int) guiObjs[gIDX_MorphTypeSize].getVal()); 		
		intValues.put(gIDX_MorphTypeShape, (int) guiObjs[gIDX_MorphTypeShape].getVal()); 		
		intValues.put(gIDX_MorphTypeCOVPath, (int) guiObjs[gIDX_MorphTypeCOVPath].getVal()); 
		intValues.put(gIDX_SetBrnchStrat, (int) guiObjs[gIDX_SetBrnchStrat].getVal()); 	
		intValues.put(gIDX_NumLineupFrames,	(int) guiObjs[gIDX_NumLineupFrames].getVal()); 
		intValues.put(gIDX_NumMorphSlices, (int) guiObjs[gIDX_NumMorphSlices].getVal()); 
		intValues.put(gIDX_CntlPtDispDetail, (int) guiObjs[gIDX_CntlPtDispDetail].getVal()); 
		intValues.put(gIDX_MorphAnalysisMmmntsDetail, (int) guiObjs[gIDX_MorphAnalysisMmmntsDetail].getVal());  
		intValues.put(gIDX_DistTestTransform, (int) guiObjs[gIDX_DistTestTransform].getVal());  
		intValues.put(gIDX_DistDimToShow, (int) guiObjs[gIDX_DistDimToShow].getVal());  
		
		TreeMap<Integer, String> strValues = new TreeMap<Integer, String>();
		
		TreeMap<Integer, Float> floatValues = new TreeMap<Integer, Float>();
		
		floatValues.put(gIDX_MorphTVal, (float)guiObjs[gIDX_MorphTVal].getVal()); 	
		floatValues.put(gIDX_MorphSpeed, (float)guiObjs[gIDX_MorphSpeed].getVal());
		floatValues.put(gIDX_MorphDistMult, (float) Math.pow(Math.E,guiObjs[gIDX_MorphDistMult].getVal()));
	
		TreeMap<Integer, Boolean> boolValues = new TreeMap<Integer, Boolean>(); 	
		
		for(Integer i=0;i<this.numPrivFlags;++i) {	boolValues.put(i, getPrivFlags(i));}
		
		uiUpdateData.setAllVals(intValues, strValues, floatValues, boolValues); 
	}
	
	
	/**
	 * initialize all maps - only call once
	 */
	private void _initMapManagers() {
		textureImgs = new PImage[2];textureImgs[0]=pa.loadImage("faceImage_0.jpg");	textureImgs[1]=pa.loadImage("faceImage_1.jpg");		
		mapManagers = new mapPairManager[mapPairManager.mapTypes.length];
		
		for(int i=0;i<mapManagers.length;++i) {		
			mapManagers[i] = new mapPairManager(this, bndPts, textureImgs, uiUpdateData, i);
		}
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
				
		tmpBtnNamesArray.add(new Object[] { "Setting COTS Branch Strat", "Set COTS Branch Strat", setCurrCOTSBranchShareStratIDX });
		
		tmpBtnNamesArray.add(new Object[] { "Resetting All Branching", "Reset All Branching", resetAllBranchingIDX});	
		tmpBtnNamesArray.add(new Object[] { "Resetting Map 0 Branching", "Reset Map 0 Branching", resetMapBranch_0IDX});	
		tmpBtnNamesArray.add(new Object[] { "Resetting Map 1 Branching", "Reset Map 1 Branching", resetMapBranch_1IDX});

		tmpBtnNamesArray.add(new Object[] { "Finding Dist From A to B","Find Dist From A to B", findDiffFromAtoBIDX});	
		tmpBtnNamesArray.add(new Object[] { "Finding Dist From B to A","Find Dist From B to A", findDiffFromBToAIDX});	
		tmpBtnNamesArray.add(new Object[] { "Find Best Registration (may remap verts in copy)","Find Matching Vertex Registration", findBestOrRegDistIDX});
		tmpBtnNamesArray.add(new Object[] { "Calculating Current Morph Distortion","Calculate Current Morph Distortion",calcMorphDistIDX});

		tmpBtnNamesArray.add(new Object[] { "Showing Maps", "Show Maps",drawMapIDX});
		tmpBtnNamesArray.add(new Object[] { "Showing Registration Map", "Show Registration Maps",drawMap_RegCopyIDX});
		tmpBtnNamesArray.add(new Object[] { "Showing Cntl Pts", "Show Cntl Pts",drawMap_CntlPtsIDX});
		tmpBtnNamesArray.add(new Object[] { "Showing Cntl Pt Lbls", "Show Cntl Pt Lbls",drawMap_CntlPtLblsIDX});
		tmpBtnNamesArray.add(new Object[] { "Show Checkerboard Maps", "Show Wireframe Maps",drawMap_FillOrWfIDX});
		tmpBtnNamesArray.add(new Object[] { "Showing Cell Circles", "Show Cell Circles",drawMap_CellCirclesIDX});
		tmpBtnNamesArray.add(new Object[] { "Showing Edge Lines", "Show Edge Lines",drawMap_EdgeLinesIDX});
		tmpBtnNamesArray.add(new Object[] { "Showing Ortho Frame", "Show Ortho Frame",drawMap_OrthoFrameIDX});
		tmpBtnNamesArray.add(new Object[] { "Showing Map Image", "Show Map Image",drawMap_ImageIDX});
		
		tmpBtnNamesArray.add(new Object[] { "Running Morph Sweep", "Run Morph Sweep", sweepMapsIDX});
		
		tmpBtnNamesArray.add(new Object[] { "Showing Morph Map", "Show Morph Map",drawMorph_MapIDX});
		tmpBtnNamesArray.add(new Object[] { "Show Morph Checkerboard", "Show Morph Wireframe",drawMorph_FillOrWfIDX});
		tmpBtnNamesArray.add(new Object[] { "Showing Morph Slices", "Show Morph Slices",drawMorph_SlicesIDX});
		tmpBtnNamesArray.add(new Object[] { "Show Morph Slices CB", "Show Morph Slices WF",drawMorph_Slices_FillOrWfIDX});
		
		tmpBtnNamesArray.add(new Object[] { "Showing Distortion Colors", "Show Distortion Colors",drawMorph_DistColorsIDX});
		tmpBtnNamesArray.add(new Object[] { "Showing Morph Slice Info", "Show Morph Slice Info",drawMorph_Slices_RtSideInfoIDX});
		tmpBtnNamesArray.add(new Object[] { "Showing Morph Cntlpt Traj", "Show Morph Cntlpt Traj",drawMorph_CntlPtTrajIDX});
		
		tmpBtnNamesArray.add(new Object[] { "Showing Reg Map Lineup", "Show Reg Map Lineup", showOrientedLineupIDX});
		
		tmpBtnNamesArray.add(new Object[] { "Showing Traj Analysis", "Show Traj Analysis", showTrajAnalysisWinIDX});		
		//tmpBtnNamesArray.add(new Object[] { "Showing Distortion Analysis", "Show Distortion Analysis", showMrphStackDistAnalysisWinIDX});
		tmpBtnNamesArray.add(new Object[] { "Showing Traj Analysis Graphs", "Show Traj Analysis Graphs", showMorphAnalysisGraphsIDX});
		//tmpBtnNamesArray.add(new Object[] { "Showing Distortion Analysis Graphs", "Show Distortion Analysis Graphs", showMrphStackDistAnalysisGraphsIDX});
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
		tmpListObjVals.put(gIDX_MapType, mapPairManager.mapTypes);	
		tmpListObjVals.put(gIDX_MorphType, mapPairManager.morphTypes); 
		
		tmpListObjVals.put(gIDX_MorphTypeOrient, mapPairManager.cmpndMorphTypes); 
		tmpListObjVals.put(gIDX_MorphTypeSize, mapPairManager.cmpndMorphTypes); 
		tmpListObjVals.put(gIDX_MorphTypeShape, mapPairManager.cmpndMorphTypes); 
		tmpListObjVals.put(gIDX_MorphTypeCOVPath, mapPairManager.cmpndMorphTypes); 
		tmpListObjVals.put(gIDX_SetBrnchStrat, branchShareStrategies);
		tmpListObjVals.put(gIDX_CntlPtDispDetail, cntlPtDispDetail);
		tmpListObjVals.put(gIDX_MorphAnalysisMmmntsDetail, analysisMmmntsDetail);
		tmpListObjVals.put(gIDX_DistTestTransform, mapPairManager.morphTypes);
		tmpListObjVals.put(gIDX_DistDimToShow, distDimToShow);
		
		tmpUIObjArray.put(gIDX_MorphTVal,new Object[] { new double[] { 0.0, 1.0, 0.01 }, 0.5,"Progress of Morph", new boolean[] { false, false, true } }); 	
		tmpUIObjArray.put(gIDX_MorphSpeed,new Object[] { new double[] { 0.0, 2.0, 0.01 }, 1.0,"Speed of Morph Animation", new boolean[] { false, false, true } }); 	
		tmpUIObjArray.put(gIDX_NumCellsPerSide,new Object[] { new double[] { 2.0, 50.0, 1.0 }, 4.0, "# of Cells Per Grid Side", new boolean[]{true, false, true}}); 
		
		tmpUIObjArray.put(gIDX_SetBrnchStrat,new Object[] { new double[]{0.0, tmpListObjVals.get(gIDX_SetBrnchStrat).length-1, 1},0.0, "Branch Sharing Strategy", new boolean[]{true, true, true}});
		
		tmpUIObjArray.put(gIDX_MapType,new Object[] { new double[]{0.0, tmpListObjVals.get(gIDX_MapType).length-1, 1},1.0* currMapTypeIDX, "Map Type to Show", new boolean[]{true, true, true}}); 

		tmpUIObjArray.put(gIDX_MorphType,new Object[] { new double[]{0.0, tmpListObjVals.get(gIDX_MorphType).length-1, 1},1.0* mapPairManager.LERPMorphIDX, "Morph Type to Process", new boolean[]{true, true, true}});

		tmpUIObjArray.put(gIDX_MorphTypeOrient, new Object[] { new double[]{0.0, tmpListObjVals.get(gIDX_MorphTypeOrient).length-1, 1},1.0* mapPairManager.LERPMorphIDX, "Orientation Morph Type to Use", new boolean[]{true, true, true}});
		tmpUIObjArray.put(gIDX_MorphTypeSize, new Object[] { new double[]{0.0, tmpListObjVals.get(gIDX_MorphTypeSize).length-1, 1},1.0* mapPairManager.LERPMorphIDX, "Size Morph Type to Use", new boolean[]{true, true, true}});
		tmpUIObjArray.put(gIDX_MorphTypeShape, new Object[] { new double[]{0.0, tmpListObjVals.get(gIDX_MorphTypeShape).length-1, 1},1.0* mapPairManager.LERPMorphIDX, "Shape Morph Type to Use", new boolean[]{true, true, true}});
		tmpUIObjArray.put(gIDX_MorphTypeCOVPath,new Object[] { new double[]{0.0, tmpListObjVals.get(gIDX_MorphTypeCOVPath).length-1, 1},1.0* mapPairManager.LERPMorphIDX, "COV Path Morph Type to Use", new boolean[]{true, true, true}});
		
		tmpUIObjArray.put(gIDX_NumLineupFrames,new Object[] { new double[]{5.0, 20.0, 1.0},11.0, "# of Frames in Lineup", new boolean[]{true, false, true}}); 
		tmpUIObjArray.put(gIDX_NumMorphSlices,new Object[] { new double[]{5.0, 20.0, 1.0},11.0, "# of Slices in Morph", new boolean[]{true, false, true}}); 
		
		tmpUIObjArray.put(gIDX_CntlPtDispDetail,new Object[] { new double[]{0.0, tmpListObjVals.get(gIDX_CntlPtDispDetail).length-1, 1},1.0*drawMapDetail, "Cntl Pt Disp Detail", new boolean[]{true, true, true}});
		tmpUIObjArray.put(gIDX_MorphAnalysisMmmntsDetail,new Object[] { new double[]{0.0, tmpListObjVals.get(gIDX_MorphAnalysisMmmntsDetail).length-1, 1},1.0*currMmntDispIDX, "Traj Analysis Detail", new boolean[]{true, true, true}});
		tmpUIObjArray.put(gIDX_DistTestTransform,new Object[] { new double[]{0.0, tmpListObjVals.get(gIDX_DistTestTransform).length-1, 1},0.0, "Distortion Analysis Transform", new boolean[]{true, true, true}});
		tmpUIObjArray.put(gIDX_DistDimToShow,new Object[] { new double[]{0.0, tmpListObjVals.get(gIDX_DistDimToShow).length-1, 1},2.0, "Distortion Dimension to Show In Colors", new boolean[]{true, true, true}});
		
		tmpUIObjArray.put(gIDX_MorphDistMult,new Object[] { new double[] { -10.0, 20.0, 0.1 }, 0.0,"Distortion Mult Exponent (for Visualization)", new boolean[] { false, false, true } }); 	
		setupGUIObjsAras_Indiv(tmpUIObjArray, tmpListObjVals);
	}//setupGUIObjsAras
	protected abstract void setupGUIObjsAras_Indiv(TreeMap<Integer, Object[]> tmpUIObjArray, TreeMap<Integer, String[]> tmpListObjVals);
	
	//public final void setUIObj_FloatVals(int UIidx, float val) {guiObjs[UIidx].setVal(val);}
	
	
	@Override
	protected final void setUIWinVals(int UIidx) {
		float val = (float) guiObjs[UIidx].getVal();
		int ival = (int) val;
		switch (UIidx) {	
			case gIDX_MorphTVal : {			//morph value	
				if(checkAndSetFloatVal(UIidx, val)) {updateMapVals(); }
				break;}		
			case gIDX_MorphSpeed : {		//multiplier for animating morph
				if(checkAndSetFloatVal(UIidx, val)) {updateMapVals(); }
				break;}
			case gIDX_NumCellsPerSide : {	//# of cells per side for Map grid
				if(checkAndSetIntVal(UIidx, ival)) {updateMapVals();}
				break;}
			case gIDX_MapType : {
				if(checkAndSetIntVal(UIidx, ival)) {updateMapVals(); currMapTypeIDX = ival;}				
				break;}
			case gIDX_MorphType : {
				if(checkAndSetIntVal(UIidx, ival)) {updateMapVals();
					if(mapManagers[currMapTypeIDX].checkCurrMorphUsesReg()) {mapManagers[currMapTypeIDX].findDifferenceBetweenMaps(false, getPrivFlags(findBestOrRegDistIDX));}
				}						 	
				break;}
			case gIDX_MorphTypeOrient		: {		
				if(checkAndSetIntVal(UIidx, ival)) {updateMapVals();}
				break;}
			case gIDX_MorphTypeSize			: {		
				if(checkAndSetIntVal(UIidx, ival)) {updateMapVals();}
				break;}
			case gIDX_MorphTypeShape		: {		
				if(checkAndSetIntVal(UIidx, ival)) {updateMapVals();}
				break;}
			case gIDX_MorphTypeCOVPath		: {		
				if(checkAndSetIntVal(UIidx, ival)) {updateMapVals();}
				break;}		
			
			case gIDX_SetBrnchStrat : {
				if(currBranchShareStrat != ival) {currBranchShareStrat = ival;uiUpdateData.setIntValue(UIidx, ival);}
				//if(checkAndSetIntVal(gIDX_SetBrnchStrat, ival)) {updateMapVals();}
				//if(currBranchShareStrategy != ival) {	currBranchShareStrategy = ival; updateMapVals();}		
				break;}
			case gIDX_NumLineupFrames : {
				if(checkAndSetIntVal(UIidx, ival)) {updateMapVals();}
				//if(numLineupFrames != ival) {			numLineupFrames = ival; updateMapVals();	}	
				break;}
			case gIDX_NumMorphSlices	:{
				if(checkAndSetIntVal(UIidx, ival)) {updateMapVals();}
				break;}
			case gIDX_CntlPtDispDetail : {
				if(ival != drawMapDetail) {drawMapDetail=ival;}
				break;}				
			case gIDX_MorphAnalysisMmmntsDetail : {
				if(ival != currMmntDispIDX) {currMmntDispIDX=ival;}
				break;}	
			case gIDX_DistTestTransform : {
				//if(ival != currDistTransformIDX) {currDistTransformIDX=ival;uiUpdateData.setIntValue(UIidx, currDistTransformIDX);}
				if(checkAndSetIntVal(UIidx, ival)) {updateMapVals();}
				break;}
			case gIDX_DistDimToShow :{
				if(checkAndSetIntVal(UIidx, ival)) {updateMapVals();}
				break;}
			case gIDX_MorphDistMult : {
				if(checkAndSetFloatVal(UIidx, (float) Math.pow(10,val))) {updateMapVals(); }
				break;		}
			default : {setUIWinVals_Indiv(UIidx, val);}
		}

	}//setUIWinVals
	protected abstract void setUIWinVals_Indiv(int UIidx, float val);
	

	@Override
	public final void setPrivFlags(int idx, boolean val) {
		int flIDX = idx / 32, mask = 1 << (idx % 32);
		boolean oldVal = getPrivFlags(idx);
		privFlags[flIDX] = (val ? privFlags[flIDX] | mask : privFlags[flIDX] & ~mask);
		//this will update UI-to-maps com object
		if(null!=uiUpdateData) {if(checkAndSetBoolValue(idx, val)){ if((null!=mapManagers) && (mapManagers.length > 0)) {updateMapVals();}}}
		switch (idx) {// special actions for each flag
			case debugAnimIDX				: {		
				break;		}
			case resetMapCrnrsIDX			: {			
				if(val) {		resetAllMapCorners();	clearBtnNextFrame(idx);	}
				break;		}
			case resetMapCrnrs_0IDX			: {			
				if(val) {		resetMapCorners(0);		clearBtnNextFrame(idx);	}
				break;		}
			case resetMapCrnrs_1IDX			: {			
				if(val) {		resetMapCorners(1);		clearBtnNextFrame(idx);		}
				break;		}
			
			case matchMapCrnrs_0IDX			: {
				if(val) {		matchAllMapCorners(0,1);	clearBtnNextFrame(idx);	}
				break;}
			case matchMapCrnrs_1IDX			: {
				if(val) {		matchAllMapCorners(1,0);	clearBtnNextFrame(idx);	}
				break;}
			
			case findDiffFromAtoBIDX			: {
				if(val) {		mapManagers[currMapTypeIDX].setFromAndToCopyIDXs(0, 1); mapManagers[currMapTypeIDX].findDifferenceBetweenMaps(getPrivFlags(debugAnimIDX), getPrivFlags(findBestOrRegDistIDX));	clearBtnNextFrame(idx);}
				break;}
			case findDiffFromBToAIDX			: {
				if(val) {		mapManagers[currMapTypeIDX].setFromAndToCopyIDXs(1, 0); mapManagers[currMapTypeIDX].findDifferenceBetweenMaps(getPrivFlags(debugAnimIDX), getPrivFlags(findBestOrRegDistIDX));	clearBtnNextFrame(idx);}
				break;}					
			case findBestOrRegDistIDX 			: { 	
				if(val != oldVal) {
					mapManagers[currMapTypeIDX].findDifferenceBetweenMaps(getPrivFlags(debugAnimIDX), getPrivFlags(findBestOrRegDistIDX));
				}
				break;		}
			
			case calcMorphDistIDX 				:{
				if(val) {
					mapManagers[currMapTypeIDX].calculateMorphDistortion();
					clearBtnNextFrame(idx);}
				break;		}
			
			case setCurrCOTSBranchShareStratIDX 			: {				
				if(val) {	if(checkAndSetIntVal(gIDX_SetBrnchStrat, currBranchShareStrat)) {updateMapVals();}	clearBtnNextFrame(idx);	}	
				break;}			

			case resetAllBranchingIDX 			: {				
				if(val) {		mapManagers[currMapTypeIDX].resetAllBranching();					clearBtnNextFrame(idx);	}	
				break;}			
			case resetMapBranch_0IDX			: {			
				if(val) {		mapManagers[currMapTypeIDX].resetIndivMapBranching(0);		clearBtnNextFrame(idx);	}
				break;		}
			case resetMapBranch_1IDX			: {			
				if(val) {		mapManagers[currMapTypeIDX].resetIndivMapBranching(1);		clearBtnNextFrame(idx);		}
				break;		}
			
			case drawMapIDX						: {			break;		}
			case drawMap_CntlPtsIDX				: {			break;		}
			case drawMap_FillOrWfIDX			: { 		break;		}
			case drawMap_CellCirclesIDX 		: {			break;		}
			case drawMap_EdgeLinesIDX			: {			break;		}
			case drawMap_ImageIDX				: {			break;		}
			case drawMap_OrthoFrameIDX			: {			break;		}
			case drawMap_CntlPtLblsIDX			: {			break;		}
			case drawMap_RegCopyIDX				: {
				if (val) {			mapManagers[currMapTypeIDX].findDifferenceBetweenMaps(true, getPrivFlags(findBestOrRegDistIDX));}
				break;		}
			
			case drawMorph_DistColorsIDX		: {			break;		}
			case drawMorph_MapIDX				: {			break;		}	
			case drawMorph_SlicesIDX			: {			break;		}	
			case drawMorph_Slices_FillOrWfIDX	: {			break;		}	
			case drawMorph_Slices_RtSideInfoIDX	: {			break;		}	
			
			case drawMorph_CntlPtTrajIDX 		: {			break;		}
			case drawMorph_FillOrWfIDX			: {			break;		}
			case sweepMapsIDX					: {			break;		}
			case showOrientedLineupIDX			: {			
				if(val) {			
					setPrivFlags(showTrajAnalysisWinIDX, false);
					setPrivFlags(showMorphAnalysisGraphsIDX, false);
					setPrivFlags(showMrphStackDistAnalysisWinIDX, false);
					setPrivFlags(showMrphStackDistAnalysisGraphsIDX, false);
					mapManagers[currMapTypeIDX].buildOrientedLineup();	
				}
				break;		}
			case showTrajAnalysisWinIDX			: {
				if(val) {
					setPrivFlags(showOrientedLineupIDX, false);
					setPrivFlags(showMrphStackDistAnalysisWinIDX, false);
					setPrivFlags(showMrphStackDistAnalysisGraphsIDX, false);
				}
				break;		}
			case showMorphAnalysisGraphsIDX 			:{
				if(val) {
					setPrivFlags(showOrientedLineupIDX, false);
					setPrivFlags(showMrphStackDistAnalysisWinIDX, false);
					setPrivFlags(showMrphStackDistAnalysisGraphsIDX, false);
				}
				break;	}
			case showMrphStackDistAnalysisWinIDX		:{
				if(val) {
					setPrivFlags(showTrajAnalysisWinIDX, false);
					setPrivFlags(showMorphAnalysisGraphsIDX, false);
					setPrivFlags(showOrientedLineupIDX, false);
				}			
				break; }
			case showMrphStackDistAnalysisGraphsIDX  :{
				if(val) {
					setPrivFlags(showTrajAnalysisWinIDX, false);
					setPrivFlags(showMorphAnalysisGraphsIDX, false);
					setPrivFlags(showOrientedLineupIDX, false);
				}			
				break; }
			default 			: {setPrivFlags_Indiv(idx,val);}
		}
	}
	protected abstract void setPrivFlags_Indiv(int idx, boolean val);
	
	/**
	 * this will check if value is different than previous value, and if so will change it
	 * @param idx
	 * @param val
	 */
	protected final boolean checkAndSetBoolValue(int idx, boolean value) {if(!uiUpdateData.compareBoolValue(idx, value)) {uiUpdateData.setBoolValue(idx, value); return true;}return false;}
	protected final boolean checkAndSetIntVal(int idx, int value) {if(!uiUpdateData.compareIntValue(idx, value)) {uiUpdateData.setIntValue(idx, value);return true;}return false;}
	protected final boolean checkAndSetStrVal(int idx, String value) {if(!uiUpdateData.compareStringValue(idx, value)) {uiUpdateData.setStringValue(idx, value);return true;}return false;}
	protected final boolean checkAndSetFloatVal(int idx, float value) {if(!uiUpdateData.compareFloatValue(idx, value)) {uiUpdateData.setFloatValue(idx, value);return true;}return false;}
	
	/**
	 * these are called externally from mapUpdFromUI object from map manager/morph/map, to synchronize values that might change, like morph time
	 * @param idx
	 * @param value
	 */
	//public final void updateBoolValFromMapMgr(int idx, boolean value) {uiUpdateData.setBoolValue(idx, value);}
	public final void updateIntValFromMapMgr(int idx, int value) {guiObjs[idx].setVal(value);uiUpdateData.setIntValue(idx, value);}
	//public final void updateStrValFromMapMgr(int idx, String value) {uiUpdateData.setStringValue(idx, value);}
	public final void updateFloatValFromMapMgr(int idx, float value) {guiObjs[idx].setVal(value);uiUpdateData.setFloatValue(idx, value);}

	
	/**
	 * set all maps' default corner locations to be the current maps' corner locations
	 */
	protected final void saveCurrMapCrnrsAsGlblCrnrs(int idx) {		//only do for quads, or will break
		crnrs[idx] = mapManagers[currMapTypeIDX].getCurrMapCrnrsAsResetCrnrrs();
		for(int i=0;i<mapManagers.length;++i) {mapManagers[i].setBndPts(crnrs[idx]);	}
		for(int i=0;i<mapManagers.length;++i) {mapManagers[i].resetAllMapCorners();}
	}
	
	protected final void setCurrMapCrnrsAsGlblCrnrs(int idx) {
		if((idx < 0) || (idx > crnrs.length) || (crnrs[idx]==null)|| (crnrs[idx][0]==null)) {resetAllMapBaseCrnrs(); return;}
		for(int i=0;i<mapManagers.length;++i) {mapManagers[i].setBndPts(crnrs[idx]);	}
		for(int i=0;i<mapManagers.length;++i) {mapManagers[i].resetAllMapCorners();}
	}
	
	/**
	 * set all maps' default corner locations to original values
	 */
	protected final void resetAllMapBaseCrnrs() {
		for(int i=0;i<mapManagers.length;++i) {	mapManagers[i].resetBndPts();}
		for(int i=0;i<mapManagers.length;++i) {mapManagers[i].resetAllMapCorners();}
	}
	
	/**
	 * clear button next frame - to act like momentary switch.  will also clear UI object
	 * @param idx
	 */
	protected void clearBtnNextFrame(int idx) {addPrivBtnToClear(idx);		checkAndSetBoolValue(idx, false);}

	protected final void updateMapVals() {
		for(int i=0;i<mapManagers.length;++i) {mapManagers[i].updateMapMorphVals_FromUI(uiUpdateData);	}
		mapManagers[currMapTypeIDX].buildOrientedLineup();
	}
	
	/**
	 * reset all corners to beginning config
	 */	
	protected void resetAllMapCorners() {	mapManagers[currMapTypeIDX].resetAllMapCorners();}
	/**
	 * reset all instances of either "floor"/A or "ceiling"/B map
	 * @param mapIDX
	 */
	protected void resetMapCorners(int mapIDX) {		mapManagers[currMapTypeIDX].resetMapCorners(mapIDX);}
	/**
	 * match map destIDX corners to map srcIDX's corners
	 */
	protected void matchAllMapCorners(int srcIDX, int destIDX) {updateMapVals();mapManagers[currMapTypeIDX].matchAllMapCorners(srcIDX, destIDX);}//matchAllMapCorners	
	
	@Override
	protected final void setVisScreenDimsPriv() {	
		//for side-by-side lineup of registered frames
		for(int i=0;i<mapManagers.length;++i) {	mapManagers[i].setPopUpWins_RectDims();}
	}

	@Override
	protected final void setCustMenuBtnNames() {	}
	
	/**
	 * return an array of 
	 * @return
	 */
	public float[] getOrientedDims() {
		return new float[] {0, 0,curVisScrDims[0],100};
	}

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
//	@Override
//	protected final void drawMe(float animTimeMod) {
//		pa.pushMatrix();pa.pushStyle();
//		boolean debug = getPrivFlags(debugAnimIDX), showLbls = getPrivFlags(drawMap_CntlPtLblsIDX), drawCircles = getPrivFlags(drawMap_CellCirclesIDX);
//		boolean drawMorphMap = getPrivFlags(drawMorph_MapIDX), drawMorphSlices = getPrivFlags(drawMorph_SlicesIDX), drawCntlPts = getPrivFlags(drawMap_CntlPtsIDX);
//		boolean drawMap = getPrivFlags(drawMapIDX), drawMorphCntlPtTraj = getPrivFlags(drawMorph_CntlPtTrajIDX), drawCopy = getPrivFlags(drawMap_RegCopyIDX);
//		boolean _showDistColors = getPrivFlags(drawMorph_DistColorsIDX);
//		if(_showDistColors) {	mapManagers[currMapTypeIDX].checkIfMorphAnalysisDone();	}
//		//draw maps with dependenc on wireframe/filled setting
//		mapManagers[currMapTypeIDX].drawMaps_Main(debug, getPrivFlags(drawMapIDX), _showDistColors, getPrivFlags(drawMap_FillOrWfIDX), drawCircles, drawCopy);
//		//drawMaps_Aux(boolean drawTexture, boolean drawOrtho, boolean drawEdgeLines) {
//		mapManagers[currMapTypeIDX].drawMaps_Aux(debug, getPrivFlags(drawMap_ImageIDX), getPrivFlags(drawMap_OrthoFrameIDX), getPrivFlags(drawMap_EdgeLinesIDX), drawCntlPts, drawCopy, showLbls,drawMapDetail);	
//		
//		if(drawMorphCntlPtTraj) {		mapManagers[currMapTypeIDX].drawMorphedMap_CntlPtTraj(drawMapDetail);}		
//		
//		if(drawMorphMap || drawMorphSlices || drawCircles || getPrivFlags(drawMorph_CntlPtTrajIDX) || getPrivFlags(drawMorph_Slices_RtSideInfoIDX)) {		
//			mapManagers[currMapTypeIDX].drawAndAnimMorph(debug, animTimeMod, drawMap,
//					drawMorphMap, _showDistColors, getPrivFlags(drawMorph_FillOrWfIDX), 
//					drawMorphSlices, getPrivFlags(drawMorph_Slices_FillOrWfIDX), 
//					drawCircles, drawCntlPts, getPrivFlags(sweepMapsIDX), showLbls,drawMapDetail);	
//		}
//		
//		_drawMe_Indiv(animTimeMod);
//		pa.popStyle();pa.popMatrix();	
//	}
	
	@Override
	protected final void drawMe(float animTimeMod) {
		pa.pushMatrix();pa.pushStyle();
		//draw maps with dependenc on wireframe/filled setting
		mapManagers[currMapTypeIDX].drawMapsAndMorphs(animTimeMod, drawMapDetail);
		
		_drawMe_Indiv(animTimeMod);
		pa.popStyle();pa.popMatrix();	
	}
	protected abstract int[] getRowColSliceIDXs();

	protected abstract void _drawMe_Indiv(float animTimeMod);
	
	@Override
	public final void drawCustMenuObjs() {

	}
	protected float sideBarYDisp = 11.0f;
	@Override
	protected final void drawRightSideInfoBarPriv(float modAmtMillis) {
		float _yOff = yOff - 4;
		//start with yOff
		pa.pushMatrix();pa.pushStyle();
		pa.scale(1.05f);
		//draw map values
		_yOff = drawRightSideMaps(_yOff);
		
		pa.popStyle();pa.popMatrix();	
	}
	/**
	 * translated already to left top corner of visible screen, already in 2D
	 */
	@Override
	protected final void drawOnScreenStuffPriv(float modAmtMillis) {
		if(getPrivFlags(showOrientedLineupIDX)) {
			mapManagers[currMapTypeIDX].drawMaps_LineupFrames(getPrivFlags(drawMap_FillOrWfIDX), getPrivFlags(drawMap_CellCirclesIDX), getPrivFlags(drawMap_ImageIDX));
		} else if(getPrivFlags(showTrajAnalysisWinIDX) || getPrivFlags(showMorphAnalysisGraphsIDX)) { 
			mapManagers[currMapTypeIDX].drawMaps_MorphAnalysisWins(getPrivFlags(showTrajAnalysisWinIDX),getPrivFlags(showMorphAnalysisGraphsIDX), allMmntDispLabels[currMmntDispIDX],drawMapDetail,sideBarYDisp) ;
		} else if(getPrivFlags(showMrphStackDistAnalysisWinIDX) || getPrivFlags(showMrphStackDistAnalysisGraphsIDX)) { 
			mapManagers[currMapTypeIDX].drawMaps_MrphStackDistAnalysisWins(getPrivFlags(showMrphStackDistAnalysisWinIDX),getPrivFlags(showMrphStackDistAnalysisGraphsIDX), allMmntDispLabels[currMmntDispIDX],sideBarYDisp) ;
		}		
	}		
	
	/**
	 * draw map values on right side menu/display
	 * @param _yOff
	 * @return
	 */
	
	protected final float drawRightSideMaps(float _yOff) {
		_yOff = mapManagers[currMapTypeIDX].drawRightSideMaps(_yOff, sideBarYDisp,getPrivFlags(drawMorph_DistColorsIDX), getPrivFlags(drawMap_RegCopyIDX), getPrivFlags(drawMorph_MapIDX), getPrivFlags(drawMorph_Slices_RtSideInfoIDX));		
		return _yOff;
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
		boolean value = mapManagers[currMapTypeIDX].hndlMouseClickInMaps(mouseX, mouseY, mseClckInWorld, mseBtn, keyPressed);
		return value;
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
	
	private int updateCount = 0, maxUpdateForRefresh = 5;
	@Override
	protected final boolean hndlMouseDragIndiv(int mouseX, int mouseY, int pmouseX, int pmouseY, myPoint mouseClickIn3D, myVector mseDragInWorld, int mseBtn) {
		if(mapManagers[currMapTypeIDX].currMseModMap != null) {
	
			handleMapMseDrag(mouseX, mouseY, pmouseX, pmouseY, mouseClickIn3D, mseDragInWorld, mseBtn);
			++updateCount;
			//if((baseMapManager.CarrierSimRegTransIDX==currMorphTypeIDX) || (getPrivFlags(drawMap_RegCopyIDX))) {mapManagers[currMapTypeIDX].findDifferenceBetweenMaps(false);}
			if(updateCount == maxUpdateForRefresh) {				
				if((mapManagers[currMapTypeIDX].checkCurrMorphUsesReg()) || (getPrivFlags(drawMap_RegCopyIDX))) {					
					//msgObj.dispInfoMessage("COTS_MorphWin::"+this.name, "hndlMouseDragIndiv", "Start find map diff : " +updateCount);
					mapManagers[currMapTypeIDX].findDifferenceBetweenMaps(false, getPrivFlags(findBestOrRegDistIDX));
					//msgObj.dispInfoMessage("COTS_MorphWin::"+this.name, "hndlMouseDragIndiv", "Done find map diff");
					
				}
				updateCount = 0;
			}
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
		mapManagers[currMapTypeIDX].hndlMouseRelIndiv();
		if(getPrivFlags(drawMap_RegCopyIDX)) {mapManagers[currMapTypeIDX].findDifferenceBetweenMaps(false, getPrivFlags(findBestOrRegDistIDX));}
		if(getPrivFlags(showOrientedLineupIDX)) {mapManagers[currMapTypeIDX].buildOrientedLineup();	}
		mouseRelease_IndivMorphWin();
	}
	
	protected abstract void mouseRelease_IndivMorphWin();	
	@Override
	protected final void snapMouseLocs(int oldMouseX, int oldMouseY, int[] newMouseLoc) {}	
	@Override
	public final void handleSideMenuMseOvrDispSel(int btn, boolean val) {}	
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
					case 0: {	//set current map corners on both maps to be default corners for all maps in this world
						saveCurrMapCrnrsAsGlblCrnrs(0);
						resetButtonState();
						break;
					}
					case 1: {	//reset all map corners to be default bnd corners
						saveCurrMapCrnrsAsGlblCrnrs(1);
						resetButtonState();
						break;
					}
					case 2: {
						saveCurrMapCrnrsAsGlblCrnrs(2);
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
						setCurrMapCrnrsAsGlblCrnrs(0);
						resetButtonState();
						break;
					}
					case 1: {
						setCurrMapCrnrsAsGlblCrnrs(1);
						resetButtonState();
						break;
					}
					case 2: {
						setCurrMapCrnrsAsGlblCrnrs(2);
						resetButtonState();
						break;
					}
					case 3: {
						resetAllMapBaseCrnrs();
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