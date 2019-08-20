package COTS_Morph_PKG.ui.base;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

import COTS_Morph_PKG.maps.COTSMap;
import COTS_Morph_PKG.maps.biLinMap;
import COTS_Morph_PKG.maps.mobiusMap;
import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.morphs.base.baseMorph;
import base_UI_Objects.my_procApplet;
import base_UI_Objects.drawnObjs.myDrawnSmplTraj;
import base_UI_Objects.windowUI.myDispWindow;
import base_Utils_Objects.io.MsgCodes;
import base_Utils_Objects.vectorObjs.myPoint;
import base_Utils_Objects.vectorObjs.myPointf;

public abstract class COTS_MorphWin extends myDispWindow {
	
	//ui vars
	public static final int
		gIDX_TimeVal 			= 0,
		gIDX_NumCellsPerSide    = 1,
		gIDX_MapType			= 2,
		gIDX_MorphType			= 3;

	protected static final int numBaseCOTSWinUIObjs = 4;
	
	/**
	 * currently selected map type
	 */
	protected int currMapTypeIDX = 0;
	/**
	 * currently selected morph type
	 */
	protected int currMorphTypeIDX = 0;
	/**
	 * currently set # of cells per side in grid
	 */
	protected int numCellsPerSide = 8;
	
	
	//boolean priv flags
	public static final int 
		debugAnimIDX 			= 0,				//debug
		resetMapCrnrsIDX		= 1,
		drawMapIDX				= 2,				//draw mappings
		drawMapIDXFillOrWfIDX	= 3,
		drawCntlPtLblsIDX		= 4,
		sweepMapsIDX			= 5;				//sweep from one mapping to other mapping
	protected static final int numBaseCOTSWinPrivFlags = 6;
	
	/**
	 * types of maps supported
	 */
	protected static final String[] mapTypes = new String[] {
		"Bilinear",
	//	"Mobius",
		"COTS",			
	};
	//need an index per map type
	public static final int
		bilinearMapIDX		= 0,
		//mobiusMapIDX	 	= 1,
		COTSMapIDX		 	= 1;
	
	/**
	 * array holding maps
	 */
	protected baseMap[][] maps;

	/**
	 * types of morphs supported
	 */
	protected static final String[] morphTypes = new String[] {
		"Drawn Line",
		"Minkowski"			
	};
	/**
	 * array holding morphs
	 */
	protected baseMorph[] morphs;
	//need an index per morph type
	public static final int
		drawnLineMorphIDX 		= 0,
		minkMorphIDX			= 1;
	
	/**
	 * map being currently modified by mouse interaction
	 */
	protected baseMap currMseModMap;
	//minimum distance for click to be considered within range of control point
	protected final float minSqClickDist = 2500.0f;
	
	/**
	 * colors for each of 2 maps' grids
	 */
	protected final int[][][] mapGridColors = new int[][][] {
		{{255,200,0,255},{90,0,222,255}},		//map grid 0
		{{55,255,10,255},{255,0,0,255}}		//map grid 1
	};

	/**
	 * # of priv flags from base class and instancing class
	 */
	private int numPrivFlags;

	public String[][] menuBtnNames = new String[][] { // each must have literals for every button defined in side bar
		// menu, or ignored
		{ "---", "---", "---"}, // row 1
		{ "---", "---", "---", "---" }, // row 3
		{ "---", "---", "---", "---" }, // row 2
		{ "---", "---", "---", "---" }, 
		{ "---", "---", "---", "---", "---" } };

	
	public COTS_MorphWin(my_procApplet _p, String _n, int _flagIdx, int[] fc, int[] sc, float[] rd, float[] rdClosed,String _winTxt, boolean _canDrawTraj) {
		super(_p, _n, _flagIdx, fc, sc, rd, rdClosed, _winTxt, _canDrawTraj);
	}
	
	@Override
	protected final void initMe() {
		// capable of using right side menu
		setFlags(drawRightSideMenu, true);
		// init specific sim flags
		initPrivFlags(numPrivFlags);
		//initially set to show maps
		setPrivFlags(drawMapIDX,true);
		setPrivFlags(drawMapIDXFillOrWfIDX,true);
		numCellsPerSide = (int) guiObjs[gIDX_NumCellsPerSide].getVal();
		
		maps = new baseMap[mapTypes.length][];
		for(int i=0;i<maps.length;++i) {maps[i] = new baseMap[2];}
		myPointf[][] bndPts = get2MapBndPts();
		for(int i=0;i<maps[bilinearMapIDX].length;++i) {maps[bilinearMapIDX][i] = new biLinMap(bndPts[i], i,mapGridColors[i], numCellsPerSide);}
		//for(int i=0;i<maps[mobiusMapIDX].length;++i) {maps[mobiusMapIDX][i] = new mobiusMap(bndPts[i], i,mapGridColors[i], numCellsPerSide);}
		for(int i=0;i<maps[COTSMapIDX].length;++i) {maps[COTSMapIDX][i] = new COTSMap(bndPts[i], i,mapGridColors[i], numCellsPerSide);}
		
		
		morphs = new baseMorph[morphTypes.length];
		
		
		
		currMseModMap = null;
	
		pa.setAllMenuBtnNames(menuBtnNames);

		initMe_Indiv();
	}//initMe
	protected abstract void initMe_Indiv();
	
	/**
	 * return the initial bounds for the maps in the world space
	 * @return 2-d array of 4 points - first idx is map idx, 2nd idx is 4 points
	 */
	protected abstract myPointf[][] get2MapBndPts();

	@Override
	public final void initAllPrivBtns(ArrayList<Object[]> tmpBtnNamesArray) {

		// add an entry for each button, in the order they are wished to be displayed
		// true tag, false tag, btn IDX
		tmpBtnNamesArray.add(new Object[] { "Debugging", "Debug", debugAnimIDX });
		tmpBtnNamesArray.add(new Object[] { "Resetting Maps", "Reset Maps", resetMapCrnrsIDX });
		tmpBtnNamesArray.add(new Object[] { "Showing Maps", "Show Maps",drawMapIDX});
		tmpBtnNamesArray.add(new Object[] { "Show Filled Maps", "Show Wireframe Maps",drawMapIDXFillOrWfIDX});
		tmpBtnNamesArray.add(new Object[] { "Showing Cntl Pt Lbls", "Show Cntl Pt Lbls",drawCntlPtLblsIDX});
		tmpBtnNamesArray.add(new Object[] { "Running Sweep", "Run Sweeps", sweepMapsIDX});
		
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
		
		tmpUIObjArray.put(gIDX_TimeVal,new Object[] { new double[] { 0.0, 1.0, 0.001 }, 0.5,"Progress of Morph", new boolean[] { false, false, true } }); 		
		tmpUIObjArray.put(gIDX_NumCellsPerSide,new Object[] { new double[] { 2.0, 100.0, 1.0 }, 8.0, "# of Cells Per Grid Side", new boolean[]{true, false, true}}); 
		tmpUIObjArray.put(gIDX_MapType,new Object[] { new double[]{0.0, tmpListObjVals.get(gIDX_MapType).length-1, 1},0.0, "Map Type to Show", new boolean[]{true, true, true}}); 
		tmpUIObjArray.put(gIDX_MorphType,new Object[] { new double[]{0.0, tmpListObjVals.get(gIDX_MorphType).length-1, 1},0.0, "Morph Type to Process", new boolean[]{true, true, true}}); 
				
		setupGUIObjsAras_Indiv(tmpUIObjArray, tmpListObjVals);
	}//setupGUIObjsAras
	protected abstract void setupGUIObjsAras_Indiv(TreeMap<Integer, Object[]> tmpUIObjArray, TreeMap<Integer, String[]> tmpListObjVals);
	
	@Override
	protected final void setUIWinVals(int UIidx) {
		float val = (float) guiObjs[UIidx].getVal();
		int ival = (int) val;
		switch (UIidx) {	
			case gIDX_TimeVal : {			//morph value			
				break;}			
			case gIDX_NumCellsPerSide : {	//# of cells per side for Map grid
				if(numCellsPerSide != ival) {
					numCellsPerSide = ival;
					updateMapVals();
				}
				break;}
			case gIDX_MapType : {
				if(currMapTypeIDX != ival) {
					currMapTypeIDX = ival;
					updateCurrentMapsAndMorph();
				}				
				break;}
			case gIDX_MorphType : {
				if(currMorphTypeIDX != ival) {
					currMorphTypeIDX = ival;
					updateCurrentMapsAndMorph();
				}				
				
				break;}
			default : {setUIWinVals_Indiv(UIidx, val);}
		}

	}//setUIWinVals
	protected abstract void setUIWinVals_Indiv(int UIidx, float val);
	
	protected final void updateMapVals() {
		for(int i=0;i<maps.length;++i) {for(int j=0;j<maps[i].length;++j) {		maps[i][j].updateMapVals(numCellsPerSide, false);}}
	}
	
	/**
	 * called whenever selected map or morph is changed
	 */
	protected final void updateCurrentMapsAndMorph() {
		
		
	}


	@Override
	public final void setPrivFlags(int idx, boolean val) {
		int flIDX = idx / 32, mask = 1 << (idx % 32);
		privFlags[flIDX] = (val ? privFlags[flIDX] | mask : privFlags[flIDX] & ~mask);
		switch (idx) {// special actions for each flag
			case debugAnimIDX			: {			break;		}
			case resetMapCrnrsIDX		: {			
				if(val) {					
					resetAllMapCorners();
					addPrivBtnToClear(resetMapCrnrsIDX);
				}
				break;		}
			case drawMapIDX				: {			break;		}
			case drawMapIDXFillOrWfIDX	: { 		break;		}
			case sweepMapsIDX			: {			break;		}
			default 			: {setPrivFlags_Indiv(idx,val);}
		}
	}
	protected abstract void setPrivFlags_Indiv(int idx, boolean val);

	protected void resetAllMapCorners() {
		myPointf[][] bndPts = get2MapBndPts();
		for(int i=0;i<maps.length;++i) {for(int j=0;j<maps[i].length;++j) {		maps[i][j].setCntlPts(bndPts[j], numCellsPerSide);	}}		
	}
	
	
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
		if(getPrivFlags(drawMapIDX)) {	
			int curModMapIDX = (null==currMseModMap ? -1 : currMseModMap.mapIdx);
			if(getPrivFlags(drawMapIDXFillOrWfIDX)) {	for(int i=0;i<maps[currMapTypeIDX].length;++i) {maps[currMapTypeIDX][i].drawMap_Fill(pa, i==curModMapIDX);}}
			else {										for(int i=0;i<maps[currMapTypeIDX].length;++i) {maps[currMapTypeIDX][i].drawMap_Wf(pa, i==curModMapIDX);}}
		}
		_drawMe_Indiv(animTimeMod,getPrivFlags(drawCntlPtLblsIDX));
	}
	
	protected abstract void _drawMe_Indiv(float animTimeMod, boolean showLbls);
	
	@Override
	public final void drawCustMenuObjs() {
		

	}

	@Override
	protected final void drawRightSideInfoBarPriv(float modAmtMillis) {
		

	}

	@Override
	protected final void drawOnScreenStuffPriv(float modAmtMillis) {
		

	}
	
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