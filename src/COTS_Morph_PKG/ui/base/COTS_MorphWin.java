package COTS_Morph_PKG.ui.base;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

import COTS_Morph_PKG.maps.biLinMap;
import COTS_Morph_PKG.maps.base.baseMap;
import base_UI_Objects.my_procApplet;
import base_UI_Objects.drawnObjs.myDrawnSmplTraj;
import base_UI_Objects.windowUI.myDispWindow;
import base_Utils_Objects.io.MsgCodes;
import base_Utils_Objects.vectorObjs.myPointf;

public abstract class COTS_MorphWin extends myDispWindow {
	
	//ui vars
	public static final int
		gIDX_TimeVal 			= 0,
		gIDX_MapType			= 1;

	protected static final int numBaseCOTSWinUIObjs = 2;
	
	/**
	 * array holding maps
	 */
	protected baseMap[][] maps;
	//boolean priv flags
	public static final int 
		debugAnimIDX 			= 0,				//debug
		rebuildCOTSIDX			= 1,
		drawMapIDX				= 2,				//draw mappings
		drawMapIDXFillOrWf		= 3,
		sweepMapsIDX			= 4;				//sweep from one mapping to other mapping
	protected static final int numBaseCOTSWinPrivFlags = 5;
	
	protected static final String[] mapTypes = new String[] {
		"Bilinear",
		"Mobius",
		"COTS",			
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
		maps = new baseMap[mapTypes.length][];
		for(int i=0;i<maps.length;++i) {maps[i] = new baseMap[2];}
		myPointf[][] bndPts = get2MapBndPts();
		for(int i=0;i<maps[0].length;++i) {maps[0][i] = new biLinMap(bndPts[i], i);}

	
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
		tmpBtnNamesArray.add(new Object[] { "Rebuilding", "Rebuild", rebuildCOTSIDX });
		tmpBtnNamesArray.add(new Object[] { "Showing Maps", "Show Maps",drawMapIDX});
		tmpBtnNamesArray.add(new Object[] { "Show Filled Maps", "Show Wireframe Maps",drawMapIDXFillOrWf});
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
		
		tmpUIObjArray.put(gIDX_TimeVal,new Object[] { new double[] { 0.0, 1.0, 0.001 }, 0.5,"Progress of Morph", new boolean[] { false, false, true } }); 
		tmpUIObjArray.put(gIDX_MapType,new Object[] { new double[]{0.0, tmpListObjVals.get(gIDX_MapType).length-1, 1},0.0, "Map Type to Show" ,new boolean[]{true, true, true}}); 
		setupGUIObjsAras_Indiv(tmpUIObjArray, tmpListObjVals);
	}//setupGUIObjsAras

	protected abstract void setupGUIObjsAras_Indiv(TreeMap<Integer, Object[]> tmpUIObjArray, TreeMap<Integer, String[]> tmpListObjVals);
	
	@Override
	protected final void setUIWinVals(int UIidx) {
		float val = (float) guiObjs[UIidx].getVal();
		//int ival = (int) val;
		switch (UIidx) {	
			case gIDX_TimeVal : {			
				break;}			//morph value
			default : {setUIWinVals_Indiv(UIidx, val);}
		}

	}//setUIWinVals
	protected abstract void setUIWinVals_Indiv(int UIidx, float val);


	@Override
	public final void setPrivFlags(int idx, boolean val) {
		int flIDX = idx / 32, mask = 1 << (idx % 32);
		privFlags[flIDX] = (val ? privFlags[flIDX] | mask : privFlags[flIDX] & ~mask);
		switch (idx) {// special actions for each flag
			case debugAnimIDX		: {			break;		}
			case rebuildCOTSIDX		: {			break;		}
			case drawMapIDX			: {			break;		}
			case drawMapIDXFillOrWf	: { 		break;		}
			case sweepMapsIDX		: {			break;		}
			default 			: {setPrivFlags_Indiv(idx,val);}
		}
	}
	protected abstract void setPrivFlags_Indiv(int idx, boolean val);

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
			if(getPrivFlags(drawMapIDXFillOrWf)) {	for(int i=0;i<maps[0].length;++i) {maps[0][i].drawMap_Fill(pa);}}
			else {									for(int i=0;i<maps[0].length;++i) {maps[0][i].drawMap_Wf(pa);}}
		}
		_drawMe_Indiv(animTimeMod);
	}
	
	protected abstract void _drawMe_Indiv(float animTimeMod);
	
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