package COTS_Morph_PKG.ui.base;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import COTS_Morph_PKG.mapManager.mapPairManager;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_Math_Objects.interpolants.base.InterpolantBehavior;
import base_Math_Objects.interpolants.base.InterpolantTypes;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.GUI_AppManager;
import base_UI_Objects.renderer.ProcessingRenderer;
import base_UI_Objects.windowUI.base.Base_DispWindow;
import base_UI_Objects.windowUI.drawnTrajectories.DrawnSimpleTraj;
import base_UI_Objects.windowUI.uiData.UIDataUpdater;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;
import base_Utils_Objects.io.messaging.MsgCodes;
import processing.core.PImage;

public abstract class COTS_MorphWin extends Base_DispWindow {
    
    //ui vars
    public static final int
        gIDX_MorphTVal                     = 0,
        gIDX_MorphSpeed                    = 1,
        gIDX_MorphTValType                = 2,            //type of interpolant to use for morph animation
        gIDX_NumCellsPerSide            = 3,
        gIDX_MapType                    = 4,    
        gIDX_MorphType                    = 5,            //overall morphtype to use - can be overridden        
        gIDX_MorphTypeOrient            = 6,            //morph types to use for each characteristic of morph frames
        gIDX_MorphTypeSize                = 7,
        gIDX_MorphTypeShape                = 8,
        gIDX_MorphTypeCOVPath            = 9,                
        gIDX_MorphAnimType                = 10,            //type of morphing animation to execute - ping pong, ping pong with stop, 1-way forward loop, 1 way backward loop
        gIDX_SetBrnchStrat                = 11,            //whether branching should be forced from Edit, forced from A, forced from B, or not shared
        gIDX_NumLineupFrames            = 12,            //# of frames to use for lineup
        gIDX_NumMorphSlices             = 13,            //# of slices in morph
        gIDX_MorphSliceDispType            = 14,            //type of morph slice to display - evenly spaced or "faded"
        gIDX_CntlPtDispDetail            = 15,            //how much detail the cntrol point display will show
        gIDX_MorphAnalysisMmmntsDetail     = 16,
        gIDX_DistTestTransform            = 17,            //transformation to use to measure distortion
        gIDX_DistDimToShow                = 18,            //which distortion dimension should be colored
        gIDX_MorphDistMult                 = 19;            //distortion multiplier to use to control colors for 
    protected static final int numBaseCOTSWinUIObjs = 20;
    /**
     * possible branch sharing strategies
     */
    protected static final String[] branchShareStrategies = new String[]{"No Branch Sharing", "Force from A", "Force from B", "Force from Edit"};
    /**
     * control point detail to display
     */
    protected static final String[] cntlPtDispDetail = new String[]{"Cntl Pts Only", "Cntl Pts & COV", "Cntl Pts, COV & Edge Pts", "All Pts"};
    /**
     * values to display in control point trajectory analysis
     */
    protected static final String[] analysisMmmntsDetail = new String[]{"Mean & STD", "First 4 moments", "Mean & STD + Min & Max", "4 Mmnts + Min & Max"};    
    /**
     * which dimension to display for distortion 
     */
    protected static final String[] distDimToShow  = new String[]{"Map Rows", "Map Columns", "Morph Slices"};
    /**
     * whether to show morph slices evenly spaced in actual t, or evenly spaced in input of function of t interpolant (non-linear) (if one is used)
     */
    protected static final String[] morphSliceType  = new String[]{"Evenly Spaced", "Fade Spacing"};
    //protected static final String[] morphAnimType = new String[]{"Ping-pong", "Ping-pong w/stop", "1-way fwd loop", "1-way bckwd loop"};

    //these should be in order of increasing detail
    public static final int 
        drawMapDet_CntlPts_IDX                  = 0,
        drawMapDet_CntlPts_COV_IDX                = 1,
        drawMapDet_CntlPts_COV_EdgePts_IDX        = 2,
        drawMapDet_CntlPts_COV_EdgePts_F_IDX     = 3;
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
        //debug is idx 0
        resetMapCrnrsIDX                    = 1,
        
        resetMapCrnrs_0IDX                    = 2,                //reset map corners to start positions
        resetMapCrnrs_1IDX                    = 3,
                
        matchMapCrnrs_0IDX                    = 4,                //match map corners - map 1 will get corners set from map 0
        matchMapCrnrs_1IDX                    = 5,                //match map corners - map 0 will get corners set from map 1
                                        
        findDiffFromAtoBIDX                    = 6,                //find angle, scale and displacement from A to B
        findDiffFromBToAIDX                    = 7,                //find angle, scale and displacement from B to A
        findBestOrRegDistIDX                = 8,                //find best distance between quad verts - potentially reassigning verts, or find registration distance
        
        calcMorphDistIDX                    = 9,                //calculate the current morph distortion
        
        setCurrCOTSBranchShareStratIDX         = 10,                //send currently set COTS branch sharing strategy to map managers and update
        
        resetAllBranchingIDX                = 11,
        resetMapBranch_0IDX                    = 12,                //reset map's branching to 0
        resetMapBranch_1IDX                    = 13,
        
        //////////////////////
        // drawing map flags
        drawMapIDX                            = 14,                //draw map grid
        drawMap_CntlPtsIDX                    = 15,                //draw map control poin
        drawMap_FillOrWfIDX                    = 16,                //draw either filled checkerboards or wireframe for mapping grid
        drawMap_CellCirclesIDX                 = 17,                //draw inscribed circles within checkerboard cells
        drawMap_ImageIDX                    = 18,                //draw the map's image
        drawMap_OrthoFrameIDX                 = 19,                 //draw orthogonal frame at map's center
        drawMap_CntlPtLblsIDX                = 20,                //draw labels for control points
        drawMap_RegCopyIDX                    = 21,                //draw the registration copy map of the similarity between A and B
        drawMap_EdgeLinesIDX                = 22,                //draw the edge lines linking map A and B
        
        //////////////////////
        // calc/drawing morph flags
        //usePerFtrMorph_IDX                = 22,                //whether to use per feature morphs or global morphs
        drawMorph_MapIDX                    = 23,                //draw morph frame
        drawMorph_SlicesIDX                    = 24,
        drawMorph_Slices_FillOrWfIDX        = 25,                //draw either filled checkerboards or wireframe for morph slices
        drawMorph_Slices_RtSideInfoIDX      = 26,                //draw info about morph slices on right side menu
        drawMorph_CntlPtTrajIDX             = 27,                //show trajectory of COV and control pts
        drawMorph_FillOrWfIDX                 = 28,
        drawMorph_DistColorsIDX                = 29,
        //////////////////////
        // animating morph flags        
        sweepMapsIDX                        = 30,                //sweep from one mapping to other mapping
        showTrajAnalysisWinIDX                = 31,                //show display of graphs of trajectory analysis - either this or oriented lineup should be shown, not both
        showMorphAnalysisGraphsIDX            = 32,
        showMrphStackDistAnalysisWinIDX        = 33,
        showMrphStackDistAnalysisGraphsIDX    = 34,
        showOrientedLineupIDX                = 35;                //show keyframe display of morph, all registered and displayed side by side
    
    protected static final int numBaseCOTSWinPrivFlags = 36;
    /**
     * # of priv flags from base class and instancing class
     */
    //private int numPrivFlags;
    //array of corner vals to set/reset
    protected myPointf[][][] crnrs;
    /**
     * colors for each of 2 maps' grids
     */
    protected static final int[][][] mapGridColors = new int[][][] {
        {{90,0,222,255},{0,225,10,255}},        //map grid 0
        {{255,200,0,255},{255,0,0,255}}        //map grid 1
    };
    
        //text scale value for display
    protected static final float txtSclVal = 1.25f;    
        //bounds for the key frame maps in this window
    public myPointf[][] bndPts;
    
    /**
     * images to use for each map
     */
    public PImage[] textureImgs;
        
    public final boolean is3D;
    
    public COTS_MorphWin(IRenderInterface _p,  GUI_AppManager _AppMgr, int _winIdx, boolean _is3D) {
        super(_p, _AppMgr, _winIdx);
        is3D = _is3D;
    }
    
    public abstract String getWinName();
    
    /**
     * Initialize any UI control flags appropriate for all boids window application
     */
    protected final void initDispFlags() {
        // capable of using right side menu
        dispFlags.setHasRtSideMenu(true);    
        initDispFlags_Indiv();
    }
    
    /**
     * Initialize any UI control flags appropriate for specific instanced boids window
     */
    protected abstract void initDispFlags_Indiv();
    
    
    @Override
    protected final void initMe() {
        crnrs = new myPointf[10][][];
        //initialize the bounds for this map
        bndPts = getKeyFrameMapBndPts();
        //initialize all maps
        _initMapManagers();
        //initialize all morphs
        //_initMorphs();
        //updateMapsWithCurrMorphs();
        initMe_Indiv();
    }//initMe
    
    
    /**
     * set initial values for private flags for instancing window - set before initMe is called
     */

    @Override
    protected int[] getFlagIDXsToInitToTrue() {
        return new int[] {drawMapIDX,drawMap_FillOrWfIDX,drawMap_CntlPtsIDX, drawMorph_FillOrWfIDX};
    }
    
    /**
     * build this instancing window class's specific UI->to->calc object
     * @return
     */
    protected final UIDataUpdater buildUIDataUpdateObject() {return new mapUpdFromUIData(this); };    
        
    /**
     * initialize all maps - only call once
     */
    private void _initMapManagers() {
        textureImgs = new PImage[2];
        textureImgs[0]=((ProcessingRenderer) ri).loadImage("faceImage_0.jpg");    
        textureImgs[1]=((ProcessingRenderer) ri).loadImage("faceImage_1.jpg");        
        mapManagers = new mapPairManager[mapPairManager.mapTypes.length];
        
        for(int i=0;i<mapManagers.length;++i) {        
            mapManagers[i] = new mapPairManager(this, bndPts, textureImgs, (mapUpdFromUIData) getUIDataUpdater(), i);
        }
    }

    protected abstract void initMe_Indiv();
    
    /**
     * return the initial bounds for the maps in the world space
     * @return 2-d array of 4 points - first idx is map idx, 2nd idx is 4 points
     */
    protected abstract myPointf[][] getKeyFrameMapBndPts();
    
    /**
     * Build all UI objects to be shown in left side bar menu for this window. This is the first child class function called by initThisWin
     * @param tmpUIObjMap : map of GUIObj_Params, keyed by unique string, with values describing the UI object
     *             - The object IDX                   
     *          - A double array of min/max/mod values                                                   
     *          - The starting value                                                                      
     *          - The label for object                                                                       
     *          - The object type (GUIObj_Type enum)
     *          - A boolean array of behavior configuration values : (unspecified values default to false)
     *               idx 0: value is sent to owning window,  
     *               idx 1: value is sent on any modifications (while being modified, not just on release), 
     *               idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
     *          - A boolean array of renderer format values :(unspecified values default to false) - Behavior Boolean array must also be provided!
     *                 - Should be multiline
     *                 - One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
     *                 - Force this object to be on a new row/line (For side-by-side layouts)
     *                 - Text should be centered (default is false)
     *                 - Object should be rendered with outline (default for btns is true, for non-buttons is false)
     *                 - Should have ornament
     *                 - Ornament color should match label color  
     */
    @Override
    protected final void setupGUIObjsAras(LinkedHashMap<String, GUIObj_Params> tmpUIObjMap){            
//        //keyed by object idx (uiXXXIDX), entries are lists of values to use for list select ui objects    
        
        var floatObjConfig = uiMgr.buildGUIObjConfigFlags(false, false);
        floatObjConfig.setIsUsedByWindow(true);
        floatObjConfig.setUpdateWindowWhileMod(true);
        
        tmpUIObjMap.put("gIDX_MorphTVal", uiMgr.uiObjInitAra_Float(gIDX_MorphTVal, new double[] { 0.0, 1.0, 0.01 }, 0.5,"Progress of Morph", floatObjConfig));     
        tmpUIObjMap.put("gIDX_MorphSpeed", uiMgr.uiObjInitAra_Float(gIDX_MorphSpeed, new double[] { 0.0, 2.0, 0.01 }, 1.0,"Speed of Morph Animation"));     
        tmpUIObjMap.put("gIDX_MorphTValType", uiMgr.uiObjInitAra_List(gIDX_MorphTValType, 1.0*InterpolantTypes.linear.getVal(), "Morph Animation Interpolant Type : ", InterpolantTypes.getListOfTypes()));
        
        tmpUIObjMap.put("gIDX_NumCellsPerSide", uiMgr.uiObjInitAra_Int(gIDX_NumCellsPerSide, new double[] { 2.0, 50.0, 1.0 }, 4.0, "# of Cells Per Grid Side")); 
        // customized object configuration
        var listObjConfig = uiMgr.buildGUIObjConfigFlags(false, false);
        listObjConfig.setIsUsedByWindow(true);
        listObjConfig.setUpdateWindowWhileMod(false);
        listObjConfig.setDontUpdateOwner(true);
        
        tmpUIObjMap.put("gIDX_SetBrnchStrat", uiMgr.uiObjInitAra_List(gIDX_SetBrnchStrat, 0.0, "Branch Sharing Strategy", branchShareStrategies, listObjConfig));
        
        tmpUIObjMap.put("gIDX_MapType", uiMgr.uiObjInitAra_List(gIDX_MapType, 1.0* currMapTypeIDX, "Map Type to Show", mapPairManager.mapTypes)); 

        tmpUIObjMap.put("gIDX_MorphType", uiMgr.uiObjInitAra_List(gIDX_MorphType, 1.0* mapPairManager.LERPMorphIDX, "Morph Type to Process", mapPairManager.morphTypes));

        tmpUIObjMap.put("gIDX_MorphTypeOrient", uiMgr.uiObjInitAra_List(gIDX_MorphTypeOrient, 1.0* mapPairManager.LERPMorphIDX, "Orientation Morph Type to Use", mapPairManager.cmpndMorphTypes));
        tmpUIObjMap.put("gIDX_MorphTypeSize", uiMgr.uiObjInitAra_List(gIDX_MorphTypeSize, 1.0* mapPairManager.LERPMorphIDX, "Size Morph Type to Use", mapPairManager.cmpndMorphTypes));
        tmpUIObjMap.put("gIDX_MorphTypeShape", uiMgr.uiObjInitAra_List(gIDX_MorphTypeShape, 1.0* mapPairManager.LERPMorphIDX, "Shape Morph Type to Use", mapPairManager.cmpndMorphTypes));
        tmpUIObjMap.put("gIDX_MorphTypeCOVPath", uiMgr.uiObjInitAra_List(gIDX_MorphTypeCOVPath, 1.0* mapPairManager.LERPMorphIDX, "COV Path Morph Type to Use", mapPairManager.cmpndMorphTypes));
        
        tmpUIObjMap.put("gIDX_MorphAnimType", uiMgr.uiObjInitAra_List(gIDX_MorphAnimType, 1.0* InterpolantBehavior.pingPong.getVal(), "Morph Animation Type : ", InterpolantBehavior.getListOfTypes()));
        
        tmpUIObjMap.put("gIDX_NumLineupFrames", uiMgr.uiObjInitAra_Int(gIDX_NumLineupFrames, new double[]{5.0, 20.0, 1.0},11.0, "# of Frames in Lineup")); 
        tmpUIObjMap.put("gIDX_NumMorphSlices", uiMgr.uiObjInitAra_Int(gIDX_NumMorphSlices, new double[]{5.0, 20.0, 1.0},11.0, "# of Slices in Morph")); 
        
        tmpUIObjMap.put("gIDX_MorphSliceDispType", uiMgr.uiObjInitAra_List(gIDX_MorphSliceDispType, 0.0, "Morph Slice Spacing to Show", morphSliceType));        
        
        tmpUIObjMap.put("gIDX_CntlPtDispDetail", uiMgr.uiObjInitAra_List(gIDX_CntlPtDispDetail, 1.0*drawMapDetail, "Cntl Pt Disp Detail", cntlPtDispDetail));
        tmpUIObjMap.put("gIDX_MorphAnalysisMmmntsDetail", uiMgr.uiObjInitAra_List(gIDX_MorphAnalysisMmmntsDetail, 1.0*currMmntDispIDX, "Traj Analysis Detail", analysisMmmntsDetail));
        tmpUIObjMap.put("gIDX_DistTestTransform", uiMgr.uiObjInitAra_List(gIDX_DistTestTransform, 0.0, "Distortion Analysis Transform", mapPairManager.morphTypes));
        tmpUIObjMap.put("gIDX_DistDimToShow", uiMgr.uiObjInitAra_List(gIDX_DistDimToShow, 2.0, "Distortion Dimension to Show In Colors", distDimToShow));
        
        tmpUIObjMap.put("gIDX_MorphDistMult", uiMgr.uiObjInitAra_Float(gIDX_MorphDistMult, new double[] {-10.0, 20.0, 0.1 }, 0.0,"Distortion Mult Exponent (for Visualization)"));     
        //tmpUIObjMap.put("gIDX_MorphSliceTypeForDist", uiMgr.uiObjInitAra_List( ,     morphSliceType.length-1, 1},0.0, "Morph Slice Spacing For Dist Calc"));
        setupGUIObjsAras_Indiv(tmpUIObjMap);
    }//setupGUIObjsAras
    
    /**
     * Build UI button objects to be shown in left side bar menu for this window.  This is the first child class function called by initThisWin
     * @param firstIdx : the first index to use in the map/as the objIdx
     * @param tmpUIBoolSwitchObjMap : map of GUIObj_Params to be built containing all flag-backed boolean switch definitions, keyed by sequential value == objId
     *                 the first element is the object index
     *                 the second element is true label
     *                 the third element is false label
     *                 the final element is integer flag idx 
     */
    @Override
    protected final void setupGUIBoolSwitchAras(int firstIdx, LinkedHashMap<String, GUIObj_Params> tmpUIBoolSwitchObjMap) {        
        //add an entry for each button, in the order they are wished to be displayed
        int idx=firstIdx;
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.buildDebugButton(idx++,"Debugging", "Enable Debug"));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Resetting Maps", "Reset Maps", resetMapCrnrsIDX));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Resetting Map 0", "Reset Map 0", resetMapCrnrs_0IDX));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Resetting Map 1", "Reset Map 1", resetMapCrnrs_1IDX));
            
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Matching Map 1 to Map 0 Crnrs", "Match Map 1 to Map 0 Crnrs", matchMapCrnrs_0IDX));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Matching Map 0 to Map 1 Crnrs", "Match Map 0 to Map 1 Crnrs", matchMapCrnrs_1IDX));
                
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Setting COTS Branch Strat", "Set COTS Branch Strat", setCurrCOTSBranchShareStratIDX));
        
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Resetting All Branching", "Reset All Branching", resetAllBranchingIDX));    
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Resetting Map 0 Branching", "Reset Map 0 Branching", resetMapBranch_0IDX));    
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Resetting Map 1 Branching", "Reset Map 1 Branching", resetMapBranch_1IDX));

        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Finding Dist From A to B","Find Dist From A to B", findDiffFromAtoBIDX));    
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Finding Dist From B to A","Find Dist From B to A", findDiffFromBToAIDX));    
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Find Best Registration (may remap verts in copy)","Find Matching Vertex Registration", findBestOrRegDistIDX));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Calculating Current Morph Distortion","Calculate Current Morph Distortion",calcMorphDistIDX));

        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Showing Maps", "Show Maps",drawMapIDX));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Showing Registration Map", "Show Registration Maps",drawMap_RegCopyIDX));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Showing Cntl Pts", "Show Cntl Pts",drawMap_CntlPtsIDX));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Showing Cntl Pt Lbls", "Show Cntl Pt Lbls",drawMap_CntlPtLblsIDX));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Show Checkerboard Maps", "Show Wireframe Maps",drawMap_FillOrWfIDX));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Showing Cell Circles", "Show Cell Circles",drawMap_CellCirclesIDX));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Showing Edge Lines", "Show Edge Lines",drawMap_EdgeLinesIDX));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Showing Ortho Frame", "Show Ortho Frame",drawMap_OrthoFrameIDX));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Showing Map Image", "Show Map Image",drawMap_ImageIDX));
        
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Running Morph Sweep", "Run Morph Sweep", sweepMapsIDX));
        
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Showing Morph Map", "Show Morph Map",drawMorph_MapIDX));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Show Morph Checkerboard", "Show Morph Wireframe",drawMorph_FillOrWfIDX));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Showing Morph Slices", "Show Morph Slices",drawMorph_SlicesIDX));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Show Morph Slices CB", "Show Morph Slices WF",drawMorph_Slices_FillOrWfIDX));
        
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Showing Distortion Colors", "Show Distortion Colors",drawMorph_DistColorsIDX));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Showing Morph Slice Info", "Show Morph Slice Info",drawMorph_Slices_RtSideInfoIDX));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Showing Morph Cntlpt Traj", "Show Morph Cntlpt Traj",drawMorph_CntlPtTrajIDX));
        
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Showing Reg Map Lineup", "Show Reg Map Lineup", showOrientedLineupIDX));
        
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Showing Traj Analysis", "Show Traj Analysis", showTrajAnalysisWinIDX));        
        //tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Showing Distortion Analysis", "Show Distortion Analysis", showMrphStackDistAnalysisWinIDX));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Showing Traj Analysis Graphs", "Show Traj Analysis Graphs", showMorphAnalysisGraphsIDX));
        //tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx, "button_"+idx++, "Showing Distortion Analysis Graphs", "Show Distortion Analysis Graphs", showMrphStackDistAnalysisGraphsIDX));
        
        setupGUIBoolSwitchAras_Indiv(idx, tmpUIBoolSwitchObjMap);
    }//setupGUIBoolSwitchAras
    
    /**
     * Build all UI objects to be shown in left side bar menu for this window. This is the first child class function called by initThisWin
     * @param tmpUIObjMap : map of GUIObj_Params, keyed by unique string, with values describing the UI object
     *             - The object IDX                   
     *          - A double array of min/max/mod values                                                   
     *          - The starting value                                                                      
     *          - The label for object                                                                       
     *          - The object type (GUIObj_Type enum)
     *          - A boolean array of behavior configuration values : (unspecified values default to false)
     *               idx 0: value is sent to owning window,  
     *               idx 1: value is sent on any modifications (while being modified, not just on release), 
     *               idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
     *          - A boolean array of renderer format values :(unspecified values default to false) - Behavior Boolean array must also be provided!
     *                 - Should be multiline
     *                 - One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
     *                 - Force this object to be on a new row/line (For side-by-side layouts)
     *                 - Text should be centered (default is false)
     *                 - Object should be rendered with outline (default for btns is true, for non-buttons is false)
     *                 - Should have ornament
     *                 - Ornament color should match label color 
     */
    protected abstract void setupGUIObjsAras_Indiv(LinkedHashMap<String, GUIObj_Params> tmpUIObjMap);

    /**
     * Build all UI buttons to be shown in left side bar menu for this window. This is for instancing windows to add to button region
     * @param firstIdx : the first index to use in the map/as the objIdx
     * @param tmpUIBoolSwitchObjMap : map of GUIObj_Params to be built containing all flag-backed boolean switch definitions, keyed by sequential value == objId
     *                 the first element is the object index
     *                 the second element is true label
     *                 the third element is false label
     *                 the final element is integer flag idx 
     */
    protected abstract void setupGUIBoolSwitchAras_Indiv(int firstIdx, LinkedHashMap<String, GUIObj_Params> tmpUIBoolSwitchObjMap);
    
    /**
     * Called if int-handling guiObjs_Numeric[UIidx] (int or list) has new data which updated UI adapter. 
     * Intended to support custom per-object handling by owning window.
     * Only called if data changed!
     * @param UIidx Index of gui obj with new data
     * @param val float value of new data
     * @param oldVal float value of old data in UIUpdater
     */
    @Override
    protected final void setUI_IntValsCustom(int UIidx, int ival, int oldVal) {
        switch(UIidx){    
            case gIDX_MorphTValType         : {break;}
            case gIDX_NumCellsPerSide         : {break;}    //# of cells per side for Map grid
            case gIDX_MapType                 : { 
                currMapTypeIDX = ival;    
                break;}
            case gIDX_MorphType : {
                if(mapManagers[currMapTypeIDX].checkCurrMorphUsesReg()) {
                    mapManagers[currMapTypeIDX].findDifferenceBetweenMaps(false, uiMgr.getPrivFlag(findBestOrRegDistIDX));
                }                                         
                break;}
            case gIDX_MorphTypeOrient        : {break;}
            case gIDX_MorphTypeSize            : {break;}
            case gIDX_MorphTypeShape        : {break;}
            case gIDX_MorphTypeCOVPath        : {break;}        
            case gIDX_MorphAnimType            : {break;}        
            case gIDX_SetBrnchStrat : {
                //TODO currently branch strat will actually change and be sent to maps whenever UI changes
                //Need to change fundamental mechanism in Base_DispWindow to handle this better.                
                
                currBranchShareStrat = ival;
                
                //if(checkAndSetIntVal(gIDX_SetBrnchStrat, ival)) {updateMapVals();}
                //if(currBranchShareStrategy != ival) {    currBranchShareStrategy = ival; updateMapVals();}        
                break;}
            case gIDX_NumLineupFrames         : {break;}
            case gIDX_NumMorphSlices        : {break;}
            case gIDX_MorphSliceDispType    : {break;}            
            case gIDX_CntlPtDispDetail         : {
                drawMapDetail = ival;
                break;}                
            case gIDX_MorphAnalysisMmmntsDetail : {
                currMmntDispIDX = ival;
                break;}    
            case gIDX_DistTestTransform     : {break;}
            case gIDX_DistDimToShow         : {break;}
            
            default : {
                boolean found = setUI_IntValsCustom_Indiv(UIidx, ival);
                if (!found) {
                    msgObj.dispWarningMessage(className, "setUI_IntValsCustom", "No int-defined gui object mapped to idx :"+UIidx);
                }
                break;
            }                
        }
    }//setUI_IntValsCustom
    protected abstract boolean setUI_IntValsCustom_Indiv(int UIidx, int ival);

    /**
     * Called if float-handling guiObjs_Numeric[UIidx] has new data which updated UI adapter.  
     * Intended to support custom per-object handling by owning window.
     * Only called if data changed!
     * @param UIidx Index of gui obj with new data
     * @param val float value of new data
     * @param oldVal float value of old data in UIUpdater
     */
    @Override
    protected final void setUI_FloatValsCustom(int UIidx, float val, float oldVal) {
        switch(UIidx){        
        case gIDX_MorphTVal     : {break;}        
        case gIDX_MorphSpeed     : {break;}    
        case gIDX_MorphDistMult : {break;}
        default : {
            boolean found = setUI_FloatValsCustom_Indiv(UIidx, val);
            if (!found) {
                msgObj.dispWarningMessage(className, "setUI_FloatValsCustom", "No float-defined gui object mapped to idx :"+UIidx);
            }
            break;}
        }                
    }//setUI_FloatValsCustom    
    protected abstract boolean setUI_FloatValsCustom_Indiv(int UIidx, float val);

    /**
     * UI code-level Debug mode functionality. Called only from flags structure
     * @param val
     */
    @Override
    protected final void handleDispFlagsDebugMode_Indiv(boolean val) {}
    
    /**
     * Application-specific Debug mode functionality (application-specific). Called only from privflags structure
     * @param val
     */
    @Override
    protected final void handlePrivFlagsDebugMode_Indiv(boolean val) {    }
    
    /**
     * Handle application-specific flag setting
     */
    @Override
    public void handlePrivFlags_Indiv(int idx, boolean val, boolean oldVal){
        switch (idx) {// special actions for each flag
            case resetMapCrnrsIDX            : {            
                if(val) {        resetAllMapCorners();    clearSwitchNextFrame(idx);    }
                break;        }
            case resetMapCrnrs_0IDX            : {            
                if(val) {        resetMapCorners(0);        clearSwitchNextFrame(idx);    }
                break;        }
            case resetMapCrnrs_1IDX            : {            
                if(val) {        resetMapCorners(1);        clearSwitchNextFrame(idx);        }
                break;        }            
            case matchMapCrnrs_0IDX            : {
                if(val) {        matchAllMapCorners(0,1);    clearSwitchNextFrame(idx);    }
                break;}
            case matchMapCrnrs_1IDX            : {
                if(val) {        matchAllMapCorners(1,0);    clearSwitchNextFrame(idx);    }
                break;}
            
            case findDiffFromAtoBIDX            : {
                if(val) {        mapManagers[currMapTypeIDX].setFromAndToCopyIDXs(0, 1); mapManagers[currMapTypeIDX].findDifferenceBetweenMaps(uiMgr.getPrivFlagIsDebug(), uiMgr.getPrivFlag(findBestOrRegDistIDX));    clearSwitchNextFrame(idx);}
                break;}
            case findDiffFromBToAIDX            : {
                if(val) {        mapManagers[currMapTypeIDX].setFromAndToCopyIDXs(1, 0); mapManagers[currMapTypeIDX].findDifferenceBetweenMaps(uiMgr.getPrivFlagIsDebug(), uiMgr.getPrivFlag(findBestOrRegDistIDX));    clearSwitchNextFrame(idx);}
                break;}                    
            case findBestOrRegDistIDX             : {     
                if(val != oldVal) {
                    mapManagers[currMapTypeIDX].findDifferenceBetweenMaps(uiMgr.getPrivFlagIsDebug(), uiMgr.getPrivFlag(findBestOrRegDistIDX));
                }
                break;        }
            
            case calcMorphDistIDX                 :{
                if(val) {
                    mapManagers[currMapTypeIDX].calculateMorphDistortion();
                    clearSwitchNextFrame(idx);}
                break;        }
            
            case setCurrCOTSBranchShareStratIDX             : {                
                if(val) {    if(checkAndSetIntVal(gIDX_SetBrnchStrat, currBranchShareStrat)) {updateCalcObjUIVals();}    clearSwitchNextFrame(idx);    }    
                break;}            

            case resetAllBranchingIDX             : {                
                if(val) {        mapManagers[currMapTypeIDX].resetAllBranching();                    clearSwitchNextFrame(idx);    }    
                break;}            
            case resetMapBranch_0IDX            : {            
                if(val) {        mapManagers[currMapTypeIDX].resetIndivMapBranching(0);        clearSwitchNextFrame(idx);    }
                break;        }
            case resetMapBranch_1IDX            : {            
                if(val) {        mapManagers[currMapTypeIDX].resetIndivMapBranching(1);        clearSwitchNextFrame(idx);        }
                break;        }
            
            case drawMapIDX                        : {            break;        }
            case drawMap_CntlPtsIDX                : {            break;        }
            case drawMap_FillOrWfIDX            : {         break;        }
            case drawMap_CellCirclesIDX         : {            break;        }
            case drawMap_EdgeLinesIDX            : {            break;        }
            case drawMap_ImageIDX                : {            break;        }
            case drawMap_OrthoFrameIDX            : {            break;        }
            case drawMap_CntlPtLblsIDX            : {            break;        }
            case drawMap_RegCopyIDX                : {
                if (val) {            mapManagers[currMapTypeIDX].findDifferenceBetweenMaps(true, uiMgr.getPrivFlag(findBestOrRegDistIDX));}
                break;        }
            
            case drawMorph_DistColorsIDX        : {            break;        }
            case drawMorph_MapIDX                : {            break;        }    
            case drawMorph_SlicesIDX            : {            break;        }    
            case drawMorph_Slices_FillOrWfIDX    : {            break;        }    
            case drawMorph_Slices_RtSideInfoIDX    : {            break;        }    
            
            case drawMorph_CntlPtTrajIDX         : {            break;        }
            case drawMorph_FillOrWfIDX            : {            break;        }
            case sweepMapsIDX                    : {            break;        }
            case showOrientedLineupIDX            : {            
                if(val) {            
                    uiMgr.setPrivFlag(showTrajAnalysisWinIDX, false);
                    uiMgr.setPrivFlag(showMorphAnalysisGraphsIDX, false);
                    uiMgr.setPrivFlag(showMrphStackDistAnalysisWinIDX, false);
                    uiMgr.setPrivFlag(showMrphStackDistAnalysisGraphsIDX, false); 
                    mapManagers[currMapTypeIDX].buildOrientedLineup();    
                }
                break;        }
            case showTrajAnalysisWinIDX            : {
                if(val) {
                    uiMgr.setPrivFlag(showOrientedLineupIDX, false);
                    uiMgr.setPrivFlag(showMrphStackDistAnalysisWinIDX, false);
                    uiMgr.setPrivFlag(showMrphStackDistAnalysisGraphsIDX, false);
                }
                break;        }
            case showMorphAnalysisGraphsIDX             :{
                if(val) {
                    uiMgr.setPrivFlag(showOrientedLineupIDX, false);
                    uiMgr.setPrivFlag(showMrphStackDistAnalysisWinIDX, false);
                    uiMgr.setPrivFlag(showMrphStackDistAnalysisGraphsIDX, false);
                }
                break;    }
            case showMrphStackDistAnalysisWinIDX        :{
                if(val) {
                    uiMgr.setPrivFlag(showTrajAnalysisWinIDX, false);
                    uiMgr.setPrivFlag(showMorphAnalysisGraphsIDX, false);
                    uiMgr.setPrivFlag(showOrientedLineupIDX, false);
                }            
                break; }
            case showMrphStackDistAnalysisGraphsIDX  :{
                if(val) {
                    uiMgr.setPrivFlag(showTrajAnalysisWinIDX, false);
                    uiMgr.setPrivFlag(showMorphAnalysisGraphsIDX, false);
                    uiMgr.setPrivFlag(showOrientedLineupIDX, false);
                }            
                break; }
            default             : {setPrivFlags_Indiv(idx,val);}
        }
    }
    protected abstract void setPrivFlags_Indiv(int idx, boolean val);
    

    /**
     * set all maps' default corner locations to be the current maps' corner locations
     */
    protected final void saveCurrMapCrnrsAsGlblCrnrs(int idx) {        //only do for quads, or will break
        crnrs[idx] = mapManagers[currMapTypeIDX].getCurrMapCrnrsAsResetCrnrrs();
        for(int i=0;i<mapManagers.length;++i) {mapManagers[i].setBndPts(crnrs[idx]);    }
        for(int i=0;i<mapManagers.length;++i) {mapManagers[i].resetAllMapCorners();}
    }
    
    protected final void setCurrMapCrnrsAsGlblCrnrs(int idx) {
        if((idx < 0) || (idx > crnrs.length) || (crnrs[idx]==null)|| (crnrs[idx][0]==null)) {resetAllMapBaseCrnrs(); return;}
        for(int i=0;i<mapManagers.length;++i) {mapManagers[i].setBndPts(crnrs[idx]);    }
        for(int i=0;i<mapManagers.length;++i) {mapManagers[i].resetAllMapCorners();}
    }
    
    /**
     * set all maps' default corner locations to original values
     */
    protected final void resetAllMapBaseCrnrs() {
        for(int i=0;i<mapManagers.length;++i) {mapManagers[i].resetBndPts();}
        for(int i=0;i<mapManagers.length;++i) {mapManagers[i].resetAllMapCorners();}
    }
    
    @Override
    protected final void updateCalcObjUIVals() {
        if((null==mapManagers)||(mapManagers.length==0)) {return;}
        for(int i=0;i<mapManagers.length;++i) {mapManagers[i].updateMapMorphVals_FromUI((mapUpdFromUIData) getUIDataUpdater());    }
        mapManagers[currMapTypeIDX].buildOrientedLineup();
    }
    
    /**
     * reset all corners to beginning config
     */    
    protected void resetAllMapCorners() {mapManagers[currMapTypeIDX].resetAllMapCorners();}
    /**
     * reset all instances of either "floor"/A or "ceiling"/B map
     * @param mapIDX
     */
    protected void resetMapCorners(int mapIDX) {mapManagers[currMapTypeIDX].resetMapCorners(mapIDX);}
    /**
     * match map destIDX corners to map srcIDX's corners
     */
    protected void matchAllMapCorners(int srcIDX, int destIDX) {updateCalcObjUIVals();mapManagers[currMapTypeIDX].matchAllMapCorners(srcIDX, destIDX);}//matchAllMapCorners    
    
    @Override
    protected final void setVisScreenDimsPriv() {    
        if (mapManagers == null) {return;}
        //for side-by-side lineup of registered frames
        for(int i=0;i<mapManagers.length;++i) {    mapManagers[i].setPopUpWins_RectDims();}
    }

    @Override
    protected final void setCustMenuBtnLabels() {    }
    
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
    protected void setCamera_Indiv(float[] camVals) {
        // No custom camera handling
        setCameraBase(camVals);
    }//setCameraIndiv
    
    @Override
    protected final void drawMe(float animTimeMod) {
        ri.pushMatState();
        //draw maps with dependenc on wireframe/filled setting
        mapManagers[currMapTypeIDX].drawMapsAndMorphs(animTimeMod, drawMapDetail);
        
        _drawMe_Indiv(animTimeMod);
        ri.popMatState();    
    }
    protected abstract int[] getRowColSliceIDXs();

    protected abstract void _drawMe_Indiv(float animTimeMod);
    
    @Override
    public final void drawCustMenuObjs(float animTimeMod) {}

    @Override
    protected final void drawRightSideInfoBarPriv(float modAmtMillis) {
        float[] rtSideYOffVals = AppMgr.getRtSideYOffVals();
        //start with yOff
        ri.pushMatState();
        ri.scale(1.05f);
        //draw map values
        drawRightSideMaps(rtSideYOffVals);
        
        ri.popMatState();    
    }
    /**
     * translated already to left top corner of visible screen, already in 2D
     */
    @Override
    protected final void drawOnScreenStuffPriv(float modAmtMillis) {
        if(uiMgr.getPrivFlag(showOrientedLineupIDX)) {
            mapManagers[currMapTypeIDX].drawMaps_LineupFrames(uiMgr.getPrivFlag(drawMap_FillOrWfIDX), uiMgr.getPrivFlag(drawMap_CellCirclesIDX), uiMgr.getPrivFlag(drawMap_ImageIDX));
        } else if(uiMgr.getPrivFlag(showTrajAnalysisWinIDX) || uiMgr.getPrivFlag(showMorphAnalysisGraphsIDX)) { 
            mapManagers[currMapTypeIDX].drawMaps_MorphAnalysisWins(uiMgr.getPrivFlag(showTrajAnalysisWinIDX),uiMgr.getPrivFlag(showMorphAnalysisGraphsIDX), allMmntDispLabels[currMmntDispIDX],drawMapDetail,getTextHeightOffset()) ;
        } else if(uiMgr.getPrivFlag(showMrphStackDistAnalysisWinIDX) || uiMgr.getPrivFlag(showMrphStackDistAnalysisGraphsIDX)) { 
            mapManagers[currMapTypeIDX].drawMaps_MrphStackDistAnalysisWins(uiMgr.getPrivFlag(showMrphStackDistAnalysisWinIDX),uiMgr.getPrivFlag(showMrphStackDistAnalysisGraphsIDX), allMmntDispLabels[currMmntDispIDX],getTextHeightOffset()) ;
        }        
    }        
    
    /**
     * draw map values on right side menu/display
     * @param _yOff
     * @return
     */
    
    protected final float drawRightSideMaps(float[] rtSideYOffVals) {
        rtSideYOffVals[0] = mapManagers[currMapTypeIDX].drawRightSideMaps(rtSideYOffVals[0], rtSideYOffVals[1],uiMgr.getPrivFlag(drawMorph_DistColorsIDX), uiMgr.getPrivFlag(drawMap_RegCopyIDX), uiMgr.getPrivFlag(drawMorph_MapIDX), uiMgr.getPrivFlag(drawMorph_Slices_RtSideInfoIDX));        
        return rtSideYOffVals[0];
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
    protected final boolean simMe(float modAmtSec) {    return false;}
    @Override
    protected final void stopMe() {}
    
    ////////////////////////
    // keyboard and mouse
    
    //move without click
    @Override
    protected final boolean hndlMouseMove_Indiv(int mouseX, int mouseY, myPoint mseClckInWorld) {        
        return false;
    }

    @Override
    protected final boolean hndlMouseClick_Indiv(int mouseX, int mouseY, myPoint mseClckInWorld, int mseBtn) {
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
    protected final boolean hndlMouseDrag_Indiv(int mouseX, int mouseY, int pmouseX, int pmouseY, myPoint mouseClickIn3D, myVector mseDragInWorld, int mseBtn) {
        if(mapManagers[currMapTypeIDX].currMseModMap != null) {
    
            handleMapMseDrag(mouseX, mouseY, pmouseX, pmouseY, mouseClickIn3D, mseDragInWorld, mseBtn);
            ++updateCount;
            //if((baseMapManager.CarrierSimRegTransIDX==currMorphTypeIDX) || (uiMgr.getPrivFlag(drawMap_RegCopyIDX))) {mapManagers[currMapTypeIDX].findDifferenceBetweenMaps(false);}
            if(updateCount == maxUpdateForRefresh) {                
                if((mapManagers[currMapTypeIDX].checkCurrMorphUsesReg()) || (uiMgr.getPrivFlag(drawMap_RegCopyIDX))) {                    
                    //msgObj.dispInfoMessage("COTS_MorphWin::"+this.name, "hndlMouseDragIndiv", "Start find map diff : " +updateCount);
                    mapManagers[currMapTypeIDX].findDifferenceBetweenMaps(false, uiMgr.getPrivFlag(findBestOrRegDistIDX));
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
    protected boolean handleMouseWheel_Indiv(int ticks, float mult) {        return false;    }
    @Override
    protected final void hndlMouseRel_Indiv() {        
        mapManagers[currMapTypeIDX].hndlMouseRel_Indiv();
        if(uiMgr.getPrivFlag(drawMap_RegCopyIDX)) {mapManagers[currMapTypeIDX].findDifferenceBetweenMaps(false, uiMgr.getPrivFlag(findBestOrRegDistIDX));}
        if(uiMgr.getPrivFlag(showOrientedLineupIDX)) {mapManagers[currMapTypeIDX].buildOrientedLineup();    }
        mouseRelease_IndivMorphWin();
    }
    
    protected abstract void mouseRelease_IndivMorphWin();    
    @Override
    protected final void snapMouseLocs(int oldMouseX, int oldMouseY, int[] newMouseLoc) {}    
    @Override
    public final void handleSideMenuMseOvrDispSel(int btn, boolean val) {}    
    /**
     * type is row of buttons (1st idx in curCustBtn array) 2nd idx is btn
     * @param funcRow idx for button row
     * @param btn idx for button within row (column)
     * @param label label for this button (for display purposes)
     */
    @Override
    protected final void launchMenuBtnHndlr(int funcRow, int btn, String label){
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
                        msgObj.dispMessage(className+"(COTS_MorphWin)", "launchMenuBtnHndlr", "Unknown Functions 1 btn : " + btn, MsgCodes.warning2);
                        break;
                    }
                }
                break;
            } // row 1 of menu side bar buttons
    
            case 1: {// row 2 of menu side bar buttons
                switch (btn) {
                    case 0: {    //set current map corners on both maps to be default corners for all maps in this world
                        saveCurrMapCrnrsAsGlblCrnrs(0);
                        resetButtonState();
                        break;
                    }
                    case 1: {    //reset all map corners to be default bnd corners
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
                        msgObj.dispMessage(className+"(COTS_MorphWin)", "launchMenuBtnHndlr", "Unknown Functions 2 btn : " + btn, MsgCodes.warning2);
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
                        msgObj.dispMessage(className+"(COTS_MorphWin)", "launchMenuBtnHndlr", "Unknown Functions 3 btn : " + btn,
                                MsgCodes.warning2);
                        resetButtonState();
                        break;
                    }
                }
                break;
            } // row 3 of menu side bar buttons
            case 3: {// row 3 of menu side bar buttons
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
                        msgObj.dispMessage(className+"(COTS_MorphWin)", "launchMenuBtnHndlr", "Unknown Functions 4 btn : " + btn, MsgCodes.warning2);
                        resetButtonState();
                        break;
                    }
                }
                break;
            } // row 3 of menu side bar buttons
            default : {
                msgObj.dispWarningMessage(className+"(COTS_MorphWin)","launchMenuBtnHndlr","Clicked Unknown Btn row : " + funcRow +" | Btn : " + btn);
                break;
            }
        }
    }
    @Override
    protected final void handleSideMenuDebugSelEnable(int btn) {
        switch (btn) {
            case 0: {                break;            }
            case 1: {                break;            }
            case 2: {                break;            }
            case 3: {                break;            }
            case 4: {                break;            }
            case 5: {                break;            }
            default: {
                msgObj.dispMessage(className+"(COTS_MorphWin)", "handleSideMenuDebugSelEnable", "Unknown Debug btn : " + btn,MsgCodes.warning2);
                break;
            }
        }
    }
    
    @Override
    protected final void handleSideMenuDebugSelDisable(int btn) {
        switch (btn) {
            case 0: {                break;            }
            case 1: {                break;            }
            case 2: {                break;            }
            case 3: {                break;            }
            case 4: {                break;            }
            case 5: {                break;            }
        default: {
            msgObj.dispMessage(className+"(COTS_MorphWin)", "handleSideMenuDebugSelDisable", "Unknown Debug btn : " + btn,MsgCodes.warning2);
            break;
            }
        }
    }

    
    @Override
    protected final void endShiftKey_Indiv() {}
    @Override
    protected final void endAltKey_Indiv() {}
    @Override
    protected final void endCntlKey_Indiv() {}
    
    ///////////////////////
    // deprecated file io stuff
    @Override
    public final void hndlFileLoad(File file, String[] vals, int[] stIdx) {}
    @Override
    public final ArrayList<String> hndlFileSave(File file) {        return null;}
    @Override
    protected final String[] getSaveFileDirNamesPriv() {return null;    }
    
    ////////////////////
    // drawn trajectory stuff
    @Override
    protected final void initDrwnTraj_Indiv() {}
    @Override
    protected final void addSScrToWin_Indiv(int newWinKey) {}
    @Override
    protected final void addTrajToScr_Indiv(int subScrKey, String newTrajKey) {}
    @Override
    protected final void delSScrToWin_Indiv(int idx) {}
    @Override
    protected final void delTrajToScr_Indiv(int subScrKey, String newTrajKey) {}
    @Override
    public void processTraj_Indiv(DrawnSimpleTraj drawnTraj) {
    }


}