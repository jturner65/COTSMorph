package COTS_Morph_PKG;


import java.util.HashMap;

import COTS_Morph_PKG.ui.COTS_Morph2DWin;
import COTS_Morph_PKG.ui.COTS_Morph3DWin;
import base_UI_Objects.GUI_AppManager;
import base_Utils_Objects.io.messaging.MsgCodes;
/**
 * Experiment with self organizing maps in applications related to graphics and geometry
 * 
 * John Turner
 * 
 */
public class COTS_MorphMain extends GUI_AppManager {
    //project-specific variables
    public final String prjNmShrt = "COTS_Morph";
    public final String prjNmLong = "Morphing between two COTS-mapped Quads";
    public final String projDesc = "Morphing between two COTS-mapped Quads in 2D and 3D.";
    
    public String authorString = "John Turner";

    public final int numVisFlags = 3;
    
    //idx's in dispWinFrames for each window - 0 is always left side menu window
    private static final int
        dispCOTS_2DMorph = 1,
        dispCOTS_3DMorph = 2
        ;
    /**
     * # of visible windows including side menu (always at least 1 for side menu)
     */
    private static final int numVisWins = 3;

    private final int[] bground = new int[]{220,244,244,255};        //bground color    

    private boolean useSphereBKGnd = false;
    
    private String bkSkyBox = "bkgrndTex.jpg";
    /**
     * fraction of height popup som win should use
     */
    public final float PopUpWinOpenFraction = .20f;
    
///////////////
//CODE STARTS
///////////////    
    //////////////////////////////////////////////// code
    
    //needs main to run project - do not modify this code in any way
    public static void main(String[] passedArgs) {        
        COTS_MorphMain me = new COTS_MorphMain();
        COTS_MorphMain.invokeProcessingMain(me, passedArgs);
    }//main    
    
    protected COTS_MorphMain(){super();}

    @Override
    protected boolean showMachineData() {return true;}
    /**
     * Set various relevant runtime arguments in argsMap
     * @param _passedArgs command-line arguments
     */
    @Override
    protected HashMap<String,Object> setRuntimeArgsVals(HashMap<String, Object> _passedArgsMap) {
        return  _passedArgsMap;
    }
    
    
    /**
     * Called in pre-draw initial setup, before first init
     * potentially override setup variables on per-project basis.
     * Do not use for setting background color or Skybox anymore.
     *      (Current settings in ProcessingRenderer)     
     *      strokeCap(PROJECT);
     *      textSize(txtSz);
     *      textureMode(NORMAL);            
     *      rectMode(CORNER);    
     *      sphereDetail(4);     * 
     */
    @Override
    protected void setupAppDims_Indiv() {}
    @Override
    protected boolean getUseSkyboxBKGnd(int winIdx) {    return useSphereBKGnd;}
    @Override
    protected String getSkyboxFilename(int winIdx) {    return bkSkyBox;}
    @Override
    protected int[] getBackgroundColor(int winIdx) {return bground;}
    @Override
    protected int getNumDispWindows() {    return numVisWins;    }

    /**
     * set smoothing level based on renderer
     * @param smthLvl 0 == no smoothing,      int: either 2, 3, 4, or 8 depending on the renderer
     */
    @Override
    public void setSmoothing() {    ri.setSmoothing(8);}
    
    /**
     * whether or not we want to restrict window size on widescreen monitors
     * 
     * @return 0 - use monitor size regardless
     *             1 - use smaller dim to be determine window 
     *             2+ - TBD
     */
    @Override
    protected int setAppWindowDimRestrictions() {    return 1;}    
    
    @Override
    public String getPrjNmShrt() {return prjNmShrt;}
    @Override
    public String getPrjNmLong() {return prjNmLong;}
    @Override
    public String getPrjDescr() {return projDesc;}
    
    /**
     * Set minimum level of message object console messages to display for this application. If null then all messages displayed
     * @return
     */
    @Override
    protected final MsgCodes getMinConsoleMsgCodes() {return null;}
    /**
     * Set minimum level of message object log messages to save to log for this application. If null then all messages saved to log.
     * @return
     */
    @Override
    protected final MsgCodes getMinLogMsgCodes() {return null;}

    /**
     * determine which main flags to show at upper left of menu 
     */
    @Override
    protected void initBaseFlags_Indiv() {
        setBaseFlagToShow_debugMode(true);
        setBaseFlagToShow_saveAnim(true); 
        setBaseFlagToShow_runSim(false);
        setBaseFlagToShow_singleStep(false);
        setBaseFlagToShow_showRtSideMenu(true);        
        setBaseFlagToShow_showStatusBar(true);
        setBaseFlagToShow_showDrawableCanvas(false);
    }
    
    @Override
    //build windows here
    protected void initAllDispWindows() {
        showInfo = true;
        //titles and descs, need to be set before sidebar menu is defined
        String[] _winTitles = new String[]{"","2D COTS Morph","3D COTS Morph"},//,"SOM Map UI"},
                _winDescr = new String[]{"","Display 2 COTS patches and the morph between them","Display 2 COTS patches in 3D and the morph between them"};

        //instanced window dims when open and closed - only showing 1 open at a time - and init cam vals
        float[][] _floatDims  = getDefaultWinAndCameraDims();    

        //menu bar init
        String[] menuBtnTitles = new String[]{
                "Load/Save Map Configuration",
                "Save Curr Keyframes' Default Corners...",
                "Set All Keyframes' Default Corners...",
                "Functions 4"};
        String[][] menuBtnNames = new String[][] { 
            // each must have literals for every button defined in side bar
            // menu, or ignored
            { "---", "---", "---"}, // row 1
            { "Curr Crnrs 0", "Curr Crnrs 1", "Curr Crnrs 2", "---" }, // row 2
            { "Curr Crnrs 0", "Curr Crnrs 1", "Curr Crnrs 2", "Orig Crnrs" }, // row 3
            { "---", "---", "---", "---" }
        };        
        String [] dbgBtns = {"Debug 0", "Debug 1", "Debug 2", "Debug 3","Debug 4"};
        buildSideBarMenu(_winTitles, menuBtnTitles, menuBtnNames, dbgBtns, true, true);    

        //define windows
        /**
         *  _winIdx The index in the various window-descriptor arrays for the dispWindow being set
         *  _title string title of this window
         *  _descr string description of this window
         *  _dispFlags Essential flags describing the nature of the dispWindow for idxs : 
         *         0 : dispWinIs3d, 
         *         1 : canDrawInWin; 
         *         2 : canShow3dbox (only supported for 3D); 
         *         3 : canMoveView
         *  _floatVals an array holding float arrays for 
         *                 rectDimOpen(idx 0),
         *                 rectDimClosed(idx 1),
         *                 initCameraVals(idx 2)
         *  _intClrVals and array holding int arrays for
         *                 winFillClr (idx 0),
         *                 winStrkClr (idx 1),
         *                 winTrajFillClr(idx 2),
         *                 winTrajStrkClr(idx 3),
         *                 rtSideFillClr(idx 4),
         *                 rtSideStrkClr(idx 5)
         *  _sceneCenterVal center of scene, for drawing objects (optional)
         *  _initSceneFocusVal initial focus target for camera (optional)
         */
        initXORWins(new int[]{dispCOTS_2DMorph,dispCOTS_3DMorph},new int[]{dispCOTS_2DMorph,dispCOTS_3DMorph});

        int wIdx = dispCOTS_2DMorph;
        setInitDispWinVals(wIdx, _winTitles[wIdx], _winDescr[wIdx], getDfltBoolAra(false), _floatDims,
                new int[][] {new int[]{210,220,250,255}, new int[]{255,255,255,255},
                    new int[]{180,180,180,255}, new int[]{100,100,100,255},
                    new int[]{0,0,0,200},new int[]{255,255,255,255}});
                
        setDispWindow(wIdx, new COTS_Morph2DWin(ri, this, wIdx));        

        //3d window
        wIdx = dispCOTS_3DMorph;
        setInitDispWinVals(wIdx, _winTitles[wIdx], _winDescr[wIdx], getDfltBoolAra(true), _floatDims,        
                new int[][] {new int[]{220,244,244,255},new int[]{0,0,0,255},
                    new int[]{180,180,180,255},new int[]{100,100,100,255},
                    new int[]{0,0,0,200},new int[]{255,255,255,255}});        
        
        setDispWindow(wIdx, new COTS_Morph3DWin(ri, this, wIdx));        
    
    }//    initVisOnce_Priv
    
    
    @Override
    //called from base class, once at start of program after vis init is called - set initial windows to show - always show UI Menu
    protected void initOnce_Indiv(){
        setWinVisFlag(dispCOTS_3DMorph, true);
        setShowStatusBar(true);
        
    }//    initOnce
    
    @Override
    //called multiple times, whenever re-initing
    protected void initProgram_Indiv(){    }//initProgram    

    /**
     * Individual extending Application Manager post-drawMe functions
     * @param modAmtMillis
     * @param is3DDraw
     */
    @Override
    protected void drawMePost_Indiv(float modAmtMillis, boolean is3DDraw) {}
    
    
    //////////////////////////////////////////////////////
    /// user interaction
    //////////////////////////////////////////////////////    
    //key is key pressed
    //keycode is actual physical key pressed == key if shift/alt/cntl not pressed.,so shift-1 gives key 33 ('!') but keycode 49 ('1')

    /**
     * present an application-specific array of mouse over btn names 
     * for the selection of the desired mouse over text display - if is length 0 or null, will not be displayed
     */
    @Override
    public String[] getMouseOverSelBtnLabels() {
        return new String[0]; 
    }
    
    @Override
    protected void handleKeyPress(char key, int keyCode) {
        switch (key){
            case ' ' : {toggleSimIsRunning(); break;}                            //run sim
            case 'f' : {getCurFocusDispWindow().setInitCamView();break;}                    //reset camera
            case 'a' :
            case 'A' : {toggleSaveAnim();break;}                        //start/stop saving every frame for making into animation
            case 's' :
            case 'S' : {break;}//save(getScreenShotSaveName(prjNmShrt));break;}//save picture of current image            
            default : {    }
        }//switch    
    }

    //keys/criteria are present that means UI objects are modified by set values based on clicks (as opposed to dragging for variable values)
    //to facilitate UI interaction non-mouse computers, set these to be single keys
    @Override
    public boolean isClickModUIVal() {
        //TODO change this to manage other key settings for situations where multiple simultaneous key presses are not optimal or conventient
        return altIsPressed() || shiftIsPressed();        
    }
        
    @Override
    //get the ui rect values of the "master" ui region (another window) -> this is so ui objects of one window can be made, clicked, and shown displaced from those of the parent windwo
    public float[] getUIRectVals_Indiv(int idx, float[] menuClickDim){
        //this.pr("In getUIRectVals for idx : " + idx);
        switch(idx){
        case dispCOTS_2DMorph    : { return menuClickDim;}
        case dispCOTS_3DMorph    : { return menuClickDim;}
        default                 :  return menuClickDim;
        }
    }    
    
    //////////////////////////////////////////
    /// graphics and base functionality utilities and variables
    //////////////////////////////////////////
    
    /**
     * return the number of visible window flags for this application
     * @return
     */
    @Override
    public int getNumVisFlags() {return numVisFlags;}
    @Override
    //address all flag-setting here, so that if any special cases need to be addressed they can be
    protected void setVisFlag_Indiv(int idx, boolean val ){
        switch (idx){
            case dispCOTS_2DMorph    : {setWinFlagsXOR(dispCOTS_2DMorph, val);break;}
            case dispCOTS_3DMorph    : {setWinFlagsXOR(dispCOTS_3DMorph, val);break;}
            default : {break;}
        }
    }//setFlags  
    
}//class SOM_GeometryMain
