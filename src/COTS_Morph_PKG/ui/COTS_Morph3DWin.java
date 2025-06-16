package COTS_Morph_PKG.ui;

import java.util.TreeMap;

import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.GUI_AppManager;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;

public class COTS_Morph3DWin  extends COTS_MorphWin {
	
	private int _numPrivButtons = numBaseCOTSWinPrivFlags + 0;

		//drag scale in 3D
	private static final float mseDrag3DScl = 1.5f;
	
	public COTS_Morph3DWin(IRenderInterface _p,  GUI_AppManager _AppMgr,  int _winIdx) {
		super(_p, _AppMgr, _winIdx, true);		
		super.initThisWin(false);
	}
	@Override
	public final String getWinName() {return "COTS_Morph3DWin";}
	
	@Override
	protected final void initMe_Indiv() {}//initMe

	@Override
	protected void initDispFlags_Indiv() {
		dispFlags.setDrawMseEdge(true);
		dispFlags.setShowRtSideMenu(true);
	}

	/**
	 * return the initial bounds for the maps in the world space
	 * @return 2-d array of 4 points - first idx is map idx, 2nd idx is 4 points
	 */
	protected final myPointf[][] getKeyFrameMapBndPts(){		
		myPointf[][] bndPts = new myPointf[2][4];
		float[][] cubeBnds = AppMgr.get3dCubeBnds();
		float minX = cubeBnds[0][0], minY = cubeBnds[0][1], minZ = cubeBnds[0][2];
		float maxX = cubeBnds[0][0] + cubeBnds[1][0], maxY = cubeBnds[0][1] + cubeBnds[1][1], maxZ = cubeBnds[0][2] + cubeBnds[1][2];
		
		
		bndPts[0] = new myPointf[]{ new myPointf(minX, maxY+1.0f, maxZ-1.0f),
									new myPointf(minX, minY-1.0f, maxZ-2.0f),
									new myPointf(minX, minY+2.0f, minZ-1.0f),
									new myPointf(minX, maxY-2.0f, minZ-2.0f)};
		
		bndPts[1] = new myPointf[]{ new myPointf(maxX, maxY-1.0f, maxZ-1.0f),
									new myPointf(maxX, minY-1.0f, maxZ+1.0f),
									new myPointf(maxX, minY-2.0f, minZ+2.0f),
									new myPointf(maxX, maxY-2.0f, minZ-1.0f)};
		return bndPts;
	}
	
	/**
	 * Retrieve the total number of defined privFlags booleans (application-specific state bools and interactive buttons)
	 */
	@Override
	public int getTotalNumOfPrivBools(){		return _numPrivButtons;	}//initAllPrivBtns
	
	/**
	 * Build all UI objects to be shown in left side bar menu for this window.  This is the first child class function called by initThisWin
	 * @param tmpUIObjMap : map of GUIObj_Params, keyed by unique string, with values describing the UI object
	 * 			- The object IDX                   
	 *          - A double array of min/max/mod values                                                   
	 *          - The starting value                                                                      
	 *          - The label for object                                                                       
	 *          - The object type (GUIObj_Type enum)
	 *          - A boolean array of behavior configuration values : (unspecified values default to false)
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
	 *          - A boolean array of renderer format values :(unspecified values default to false)
	 *           	idx 0: whether multi-line(stacked) or not                                                  
	 *              idx 1: if true, build prefix ornament                                                      
	 *              idx 2: if true and prefix ornament is built, make it the same color as the text fill color.
	 */
	@Override
	protected final void setupGUIObjsAras_Indiv(TreeMap<String, GUIObj_Params> tmpUIObjMap) {}

	/**
	 * Build all UI buttons to be shown in left side bar menu for this window. This is for instancing windows to add to button region
	 * USE tmpUIBoolSwitchObjMap.size() for start idx
	 * @param tmpUIBoolSwitchObjMap : map of GUIObj_Params to be built containing all flag-backed boolean switch definitions, keyed by sequential value == objId
	 * 				the first element is the object index
	 * 				the second element is true label
	 * 				the third element is false label
	 * 				the final element is integer flag idx 
	 */
	@Override
	protected final void setupGUIBoolSwitchAras_Indiv(TreeMap<String, GUIObj_Params> tmpUIBoolSwitchObjMap) {}
	
	/**
	 * which indexes are for rows, columns and slices
	 */
	@Override
	protected final int[] getRowColSliceIDXs() {		return new int[] {3,-1,-2};	}
	
	/**
	 * Check class-specific int/list ui objs upon population.  returns true if found
	 */
	@Override
	protected boolean setUI_IntValsCustom_Indiv(int UIidx, int ival) {return false;}
	/**
	 * Check class-specific float ui objs upon population.  returns true if found
	 */
	@Override
	protected boolean setUI_FloatValsCustom_Indiv(int UIidx, float val) {return false;}

	@Override
	public void setPrivFlags_Indiv(int idx, boolean val) {
		switch (idx) {// special actions for each flag
			default : {}
		}
	}

	/////////////////////////////
	// draw routines
	@Override
	protected final void _drawMe_Indiv(float animTimeMod){}
	
	/**
	 * called by maps
	 * @param p
	 * @param lbl
	 * @param xOff
	 * @param yOff
	 */
	public final void _drawLabelAtPt(myPointf p, String lbl, float xOff, float yOff) {
		ri.pushMatState();	
		ri.translate(p);
		unSetCamOrient();
		ri.scale(txtSclVal);
		ri.showText(lbl, xOff,yOff,0); 
		ri.popMatState();
	}
	
	////////////////////////
	// keyboard and mouse
	
	@Override
	public final Float findDistToPtOrRay(myPointf _notUsedPt, myPointf _pt, myPointf _rayOrigin, myVectorf _rayDir) {		
		myVectorf vecToPt = new myVectorf(_rayOrigin, _pt);
		float proj = vecToPt._dot(_rayDir);		
		return myVectorf._sub(vecToPt, myVectorf._mult(_rayDir, proj)).sqMagn;
	}
	@Override
	public final myPointf getMouseClkPtInWorld(myPoint mseClckInWorld,int mouseX, int mouseY) {return new myPointf(mseClckInWorld.x, mseClckInWorld.y, mseClckInWorld.z);}

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
	@Override
	protected final void handleMapMseDrag(int mouseX, int mouseY, int pmouseX, int pmouseY, myPoint mouseClickIn3D, myVector mseDragInWorld, int mseBtn) {
		myVectorf mseDragInWorld_f = new myVectorf(mseDragInWorld);
		myVectorf[] basisVecs = mapManagers[currMapTypeIDX].currMseModMap.basisVecs;
		myVectorf defVec = myVectorf._add(myVectorf._mult(basisVecs[1], mseDrag3DScl*mseDragInWorld_f._dot(basisVecs[1])), myVectorf._mult(basisVecs[2],mseDrag3DScl* mseDragInWorld_f._dot(basisVecs[2])));		
		myPointf mseClickIn3D_f = new myPointf(mouseClickIn3D.x, mouseClickIn3D.y, mouseClickIn3D.z);
		//mapManagers[currMapTypeIDX].currMseModMap.mseDragInMap(defVec, mseClickIn3D_f,keyPressed,keyCodePressed);
		boolean changed = mapManagers[currMapTypeIDX].mseDragInMap(defVec, mseClickIn3D_f,keyPressed,keyCodePressed);
		if(changed) {
			if(uiMgr.getPrivFlag(showOrientedLineupIDX)) {mapManagers[currMapTypeIDX].buildOrientedLineup();	}
		}
	}
	
	@Override
	protected final void mouseRelease_IndivMorphWin(){}
	@Override
	protected final myPoint getMsePtAs3DPt(myPoint mseLoc) {		return new myPoint(mseLoc);	}	

}//class COTS_Morph3DWin
