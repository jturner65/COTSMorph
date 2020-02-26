package COTS_Morph_PKG.ui;

import java.util.ArrayList;
import java.util.TreeMap;

import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import base_JavaProjTools_IRender.base_Render_Interface.IRenderInterface;
import base_UI_Objects.GUI_AppManager;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_Math_Objects.vectorObjs.floats.myVectorf;

public class COTS_Morph2DWin extends COTS_MorphWin {
	
	private int _numPrivButtons = numBaseCOTSWinPrivFlags + 0;

	public COTS_Morph2DWin(IRenderInterface _p,  GUI_AppManager _AppMgr, String _n, int _flagIdx, int[] fc, int[] sc, float[] rd, float[] rdClosed,String _winTxt) {
		super(_p, _AppMgr, _n, _flagIdx, fc, sc, rd, rdClosed, _winTxt, false);		
		super.initThisWin(false);
	}
	@Override
	public final String getWinName() {return "COTS_Morph2DWin";}

	
	@Override
	protected final void initMe_Indiv() {	
	}//initMe
	/**
	 * return the initial bounds for the maps in the world space
	 * @return 2-d array of 4 points - first idx is map idx, 2nd idx is 4 points
	 */
	protected final myPointf[][] getKeyFrameMapBndPts(){
		myPointf[][] bndPts = new myPointf[2][4];
		//width of area per map
		float widthPerMap = .5f*rectDim[2], 	halfWidth = .5f*widthPerMap;
		float size = rectDim[3] * .35f,			halfSize = .5f * size;
		
		float minX =rectDim[0]+ halfWidth - halfSize, minY = (rectDim[1]+.5f*rectDim[3]) - .5f*size - 150.0f;		
		float maxX = minX + size, maxY = minY + size;
		
		bndPts[0] = new myPointf[]{ new myPointf(minX+.1f, minY-.1f,0),
									new myPointf(maxX, minY,0),
									new myPointf(maxX, maxY,0),
									new myPointf(minX, maxY,0)};
		
		bndPts[1] = new myPointf[]{ new myPointf(minX + widthPerMap-.1f, minY-.2f,0),
									new myPointf(maxX + widthPerMap-.2f, minY+.1f,0),
									new myPointf(maxX + widthPerMap, maxY,0),
									new myPointf(minX + widthPerMap+.2f, maxY,0)};
			
		return bndPts;
	}

	@Override
	protected final int initAllPrivBtns_Indiv(ArrayList<Object[]> tmpBtnNamesArray) {
		
		
		return _numPrivButtons;
	}//initAllPrivBtns
	/**
	 * which indexes are for rows, columns and slices
	 */
	protected final int[] getRowColSliceIDXs() {
		return new int[] {1,2,3};
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

	@Override
	protected void setUIWinVals_Indiv(int UIidx, float val) {
		switch (UIidx) {	
		}

	}//setUIWinVals



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
		
		
	}//_drawMe_Indiv
	
	
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
		pa.scale(txtSclVal);
		pa.showText(lbl, xOff,yOff,0); 
		pa.popMatState();
	}

		
	////////////////////////
	// keyboard and mouse

	@Override
	public final Float findDistToPtOrRay(myPointf _pt0, myPointf _pt, myPointf _notUsedPt, myVectorf _notUsedVec) {		return myPointf._SqrDist(_pt0, _pt);	};
	@Override
	public final myPointf getMouseClkPtInWorld(myPoint mseClckInWorld,int mouseX, int mouseY) {return new myPointf(mouseX,mouseY,0);}

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
		//myVectorf mseDragInWorld_f = new myVectorf(mseDragInWorld);
		myVectorf defVec = new myVectorf(1.0f*(mouseX-pmouseX), 1.0f*(mouseY-pmouseY),0.0f);
		myPointf mseClickIn3D_f = new myPointf(mouseX, mouseY, 0);
		//mapManagers[currMapTypeIDX].currMseModMap.mseDragInMap(defVec, mseClickIn3D_f,keyPressed,keyCodePressed);
		boolean changed = mapManagers[currMapTypeIDX].mseDragInMap(defVec, mseClickIn3D_f,keyPressed,keyCodePressed);
		//currMseModMap.mseDrag_2D(mouseX, mouseY, 1.0f*(mouseX-pmouseX), 1.0f*(mouseY-pmouseY),mseDragInWorld,keyPressed,keyCodePressed);
		if(changed) {
			if(getPrivFlags(showOrientedLineupIDX)) {mapManagers[currMapTypeIDX].buildOrientedLineup();	}
		}
	}
	
	@Override
	protected final void mouseRelease_IndivMorphWin(){}

	@Override
	protected final myPoint getMsePtAs3DPt(myPoint mseLoc) {	return new myPoint(mseLoc.x, mseLoc.y, 0);}


}//class COTS_Morph2DWin
