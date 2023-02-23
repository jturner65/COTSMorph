package COTS_Morph_PKG.ui;

import java.util.ArrayList;
import java.util.TreeMap;

import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.GUI_AppManager;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_Math_Objects.vectorObjs.floats.myVectorf;

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
	protected final void initMe_Indiv() {
	}//initMe

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
					
		float minX = AppMgr.cubeBnds[0][0], minY = AppMgr.cubeBnds[0][1], minZ = AppMgr.cubeBnds[0][2];
		float maxX = AppMgr.cubeBnds[0][0] + AppMgr.cubeBnds[1][0], maxY = AppMgr.cubeBnds[0][1] + AppMgr.cubeBnds[1][1], maxZ = AppMgr.cubeBnds[0][2] + AppMgr.cubeBnds[1][2];
		
		
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

	@Override
	protected final int initAllPrivBtns_Indiv(ArrayList<Object[]> tmpBtnNamesArray) {
		
		
		return _numPrivButtons;
	}//initAllPrivBtns
	
	/**
	 * which indexes are for rows, columns and slices
	 */
	@Override
	protected final int[] getRowColSliceIDXs() {
		return new int[] {3,-1,-2};
	}

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
	protected void setupGUIObjsAras_Indiv(TreeMap<Integer, Object[]> tmpUIObjArray, TreeMap<Integer, String[]> tmpListObjVals) {
		
	}
	
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
	protected final void _drawMe_Indiv(float animTimeMod){		
		
	}
	
	/**
	 * called by maps
	 * @param p
	 * @param lbl
	 * @param xOff
	 * @param yOff
	 */
	public final void _drawLabelAtPt(myPointf p, String lbl, float xOff, float yOff) {
		pa.pushMatState();	
		pa.translate(p);
		unSetCamOrient();
		pa.scale(txtSclVal);
		pa.showText(lbl, xOff,yOff,0); 
		pa.popMatState();
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
			if(privFlags.getFlag(showOrientedLineupIDX)) {mapManagers[currMapTypeIDX].buildOrientedLineup();	}
		}
	}
	
	@Override
	protected final void mouseRelease_IndivMorphWin(){
	}
	@Override
	protected final myPoint getMsePtAs3DPt(myPoint mseLoc) {		return new myPoint(mseLoc);	}

	

}//class COTS_Morph3DWin
