package COTS_Morph_PKG;


import COTS_Morph_PKG.ui.COTS_Morph2DWin;
import COTS_Morph_PKG.ui.COTS_Morph3DWin;
import base_UI_Objects.*;
import base_UI_Objects.windowUI.base.myDispWindow;
import base_UI_Objects.windowUI.sidebar.mySideBarMenu;
/**
 * Experiment with self organizing maps in applications related to graphics and geometry
 * 
 * John Turner
 * 
 */
public class COTS_MorphMain extends GUI_AppManager {
	//project-specific variables
	public String prjNmLong = "Morphing between two COTS-mapped Quads", prjNmShrt = "COTS_Morph";
	
	private final int
		showUIMenu = 0,
		showCOTS_2DMorph = 1,
		showCOTS_3DMorph = 2						
		;
	public final int numVisFlags = 3;
	
	//idx's in dispWinFrames for each window - 0 is always left side menu window
	private static final int
		dispCOTS_2DMorph = 1,
		dispCOTS_3DMorph = 2
		;
																		//set array of vector values (sceneFcsVals) based on application
	private final int[] bground = new int[]{220,244,244,255};		//bground color	
	
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
	
	
	/**
	 * set smoothing level based on renderer
	 * @param smthLvl 0 == no smoothing,  	int: either 2, 3, 4, or 8 depending on the renderer
	 */
	@Override
	protected void setSmoothing() {	pa.setSmoothing(8);}
	
	/**
	 * whether or not we want to restrict window size on widescreen monitors
	 * 
	 * @return 0 - use monitor size regardless
	 * 			1 - use smaller dim to be determine window 
	 * 			2+ - TBD
	 */
	@Override
	protected int setAppWindowDimRestrictions() {	return 1;}	
	
	//instance-specific setup code
	protected void setup_Indiv() {		
		setBkgrnd();
	}	
	@Override
	public void setBkgrnd(){((my_procApplet)pa).background(bground[0],bground[1],bground[2],bground[3]);}//setBkgrnd	
	/**
	 * determine which main flags to show at upper left of menu 
	 */
	@Override
	protected void initMainFlags_Indiv() {
		setMainFlagToShow_debugMode(true);
		setMainFlagToShow_saveAnim(true); 
		setMainFlagToShow_runSim(false);
		setMainFlagToShow_singleStep(false);
		setMainFlagToShow_showRtSideMenu(true);
	}
	
	@Override
	//build windows here
	protected void initAllDispWindows() {
	
		showInfo = true;
		//includes 1 for menu window (never < 1) - always have same # of visFlags as myDispWindows
		int numWins = numVisFlags;		
		//titles and descs, need to be set before sidebar menu is defined
		String[] _winTitles = new String[]{"","2D COTS Morph","3D COTS Morph"},//,"SOM Map UI"},
				_winDescr = new String[] {"","Display 2 COTS patches and the morph between them","Display 2 COTS patches in 3D and the morph between them"};
		initWins(numWins,_winTitles, _winDescr);
		//call for menu window
		buildInitMenuWin(showUIMenu);
		//menu bar init
		int wIdx = dispMenuIDX,fIdx=showUIMenu;
		dispWinFrames[wIdx] = buildSideBarMenu(wIdx, fIdx, new String[]{"Load/Save Map Configuration","Save Curr Keyframes' Default Corners...","Set All Keyframes' Default Corners...","Functions 4"}, new int[] {3,4,4,4}, 5, true, true);//new COTS_MorphSideBarMenu(this, winTitles[wIdx], fIdx, winFillClrs[wIdx], winStrkClrs[wIdx], winRectDimOpen[wIdx], winRectDimClose[wIdx], winDescr[wIdx]);	
		//instanced window dimensions when open and closed - only showing 1 open at a time
		float[] _dimOpen  =  new float[]{menuWidth, 0, pa.getWidth()-menuWidth, pa.getHeight()}, _dimClosed  =  new float[]{menuWidth, 0, hideWinWidth, pa.getHeight()};	
		//setInitDispWinVals : use this to define the values of a display window
		//int _winIDX, 
		//float[] _dimOpen, float[] _dimClosed  : dimensions opened or closed
		//String _ttl, String _desc 			: window title and description
		//boolean[] _dispFlags 					: 
		//   flags controlling display of window :  idxs : 0 : canDrawInWin; 1 : canShow3dbox; 2 : canMoveView; 3 : dispWinIs3d
		//int[] _fill, int[] _strk, 			: window fill and stroke colors
		//int _trajFill, int _trajStrk)			: trajectory fill and stroke colors, if these objects can be drawn in window (used as alt color otherwise)
		//specify windows that cannot be shown simultaneously here
		initXORWins(new int[]{showCOTS_2DMorph,showCOTS_3DMorph},new int[]{dispCOTS_2DMorph,dispCOTS_3DMorph});

		wIdx = dispCOTS_2DMorph; fIdx= showCOTS_2DMorph;
		setInitDispWinVals(wIdx, _dimOpen, _dimClosed,new boolean[]{false,false,true,false}, new int[]{210,220,250,255},new int[]{255,255,255,255},new int[]{180,180,180,255},new int[]{100,100,100,255}); 
		dispWinFrames[wIdx] = new COTS_Morph2DWin(pa, this, winTitles[wIdx], fIdx, winFillClrs[wIdx], winStrkClrs[wIdx], winRectDimOpen[wIdx], winRectDimClose[wIdx], winDescr[wIdx]);		

		//3d window
		wIdx = dispCOTS_3DMorph; fIdx= showCOTS_3DMorph;
		setInitDispWinVals(wIdx, _dimOpen, _dimClosed,new boolean[]{false,true,true,true}, new int[]{220,244,244,255},new int[]{0,0,0,255},new int[]{180,180,180,255},new int[]{100,100,100,255}); 
		dispWinFrames[wIdx] = new COTS_Morph3DWin(pa, this, winTitles[wIdx], fIdx, winFillClrs[wIdx], winStrkClrs[wIdx], winRectDimOpen[wIdx], winRectDimClose[wIdx], winDescr[wIdx]);		
	
	}//	initVisOnce_Priv
	
	
	@Override
	//called from base class, once at start of program after vis init is called - set initial windows to show - always show UI Menu
	protected void initOnce_Indiv(){
		//which objects to initially show
		setVisFlag(showUIMenu, true);					//show input UI menu	
		//setVisFlag(showSpereAnimRes, true);
		setVisFlag(showCOTS_3DMorph, true);
		
	}//	initOnce
	
	@Override
	//called multiple times, whenever re-initing
	protected void initProgram_Indiv(){	}//initProgram	
	@Override
	protected void initVisProg_Indiv() {}		


	@Override
	protected String getPrjNmLong() {return prjNmLong;}

	@Override
	protected String getPrjNmShrt() {		return prjNmShrt;	}

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
	public String[] getMouseOverSelBtnNames() {
		return new String[0]; 
	}
	
	@Override
	protected void handleKeyPress(char key, int keyCode) {
		switch (key){
			case ' ' : {toggleSimIsRunning(); break;}							//run sim
			case 'f' : {dispWinFrames[curFocusWin].setInitCamView();break;}					//reset camera
			case 'a' :
			case 'A' : {toggleSaveAnim();break;}						//start/stop saving every frame for making into animation
			case 's' :
			case 'S' : {break;}//save(getScreenShotSaveName(prjNmShrt));break;}//save picture of current image			
			default : {	}
		}//switch	
	}

	@Override
	//gives multiplier based on whether shift, alt or cntl (or any combo) is pressed
	public double clickValModMult(){return ((altIsPressed() ? .1 : 1.0) * (shiftIsPressed() ? 10.0 : 1.0));}	
	//keys/criteria are present that means UI objects are modified by set values based on clicks (as opposed to dragging for variable values)
	//to facilitate UI interaction non-mouse computers, set these to be single keys
	@Override
	public boolean isClickModUIVal() {
		//TODO change this to manage other key settings for situations where multiple simultaneous key presses are not optimal or conventient
		return altIsPressed() || shiftIsPressed();		
	}
	
	@Override
	//these tie using the UI buttons to modify the window in with using the boolean tags - PITA but currently necessary
	public void handleShowWin(int btn, int val, boolean callFlags){//display specific windows - multi-select/ always on if sel
		if(!callFlags){//called from setflags - only sets button state in UI to avoid infinite loop
			setMenuBtnState(mySideBarMenu.btnShowWinIdx,btn, val);
		} else {//called from clicking on buttons in UI
		
			//val is btn state before transition 
			boolean bVal = (val == 1?  false : true);
			//each entry in this array should correspond to a clickable window, not counting menu
			setVisFlag(btn+1, bVal);
		}
	}//handleShowWin
	
	@Override
	//get the ui rect values of the "master" ui region (another window) -> this is so ui objects of one window can be made, clicked, and shown displaced from those of the parent windwo
	public float[] getUIRectVals(int idx){
		//this.pr("In getUIRectVals for idx : " + idx);
		switch(idx){
		case dispMenuIDX 				: { return new float[0];}			//idx 0 is parent menu sidebar
		case dispCOTS_2DMorph				: { return dispWinFrames[dispMenuIDX].uiClkCoords;}
		case dispCOTS_3DMorph				: { return dispWinFrames[dispMenuIDX].uiClkCoords;}
		default :  return dispWinFrames[dispMenuIDX].uiClkCoords;
		}
	}	
	
	@SuppressWarnings("unused")
	private float[] getMaxUIClkCoords() {
		float[] res = new float[] {0.0f,0.0f,0.0f,0.0f}, tmpCoords;
		for (int winIDX : winDispIdxXOR) {
			tmpCoords = dispWinFrames[winIDX].uiClkCoords;
			for(int i=0;i<tmpCoords.length;++i) {
				if(res[i]<tmpCoords[i]) {res[i]=tmpCoords[i];}
			}
		}
		return res;
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
			case showUIMenu 	    : { dispWinFrames[dispMenuIDX].setFlags(myDispWindow.showIDX,val);    break;}											//whether or not to show the main ui window (sidebar)			
			case showCOTS_2DMorph	: {setWinFlagsXOR(dispCOTS_2DMorph, val);break;}
			case showCOTS_3DMorph	: {setWinFlagsXOR(dispCOTS_3DMorph, val);break;}
			default : {break;}
		}
	}//setFlags  


	@Override
	public int[] getClr_Custom(int colorVal, int alpha) {	return new int[] {255,255,255,alpha};}

	
}//class SOM_GeometryMain
