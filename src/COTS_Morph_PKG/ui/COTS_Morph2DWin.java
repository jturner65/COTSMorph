package COTS_Morph_PKG.ui;

import java.util.ArrayList;
import java.util.TreeMap;

import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import base_UI_Objects.my_procApplet;
import base_Utils_Objects.vectorObjs.myPoint;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVector;

public class COTS_Morph2DWin extends COTS_MorphWin {
	
	private int _numPrivButtons = numBaseCOTSWinPrivFlags + 0;

	
	public COTS_Morph2DWin(my_procApplet _p, String _n, int _flagIdx, int[] fc, int[] sc, float[] rd, float[] rdClosed,String _winTxt, boolean _canDrawTraj) {
		super(_p, _n, _flagIdx, fc, sc, rd, rdClosed, _winTxt, _canDrawTraj);
		
		super.initThisWin(_canDrawTraj, true, false);
	}
	@Override
	protected final void initMe_Indiv() {
	
	}//initMe


	/**
	 * return the initial bounds for the maps in the world space
	 * @return 2-d array of 4 points - first idx is map idx, 2nd idx is 4 points
	 */
	protected final myPointf[][] get2MapBndPts(){
		myPointf[][] bndPts = new myPointf[2][4];
		//width of area per map
		float widthPerMap = .5f*rectDim[2], 
				halfWidth = .5f*widthPerMap;
		float size = rectDim[3] * .4f,
				halfSize = .5f * size;

		
		float minX =rectDim[0]+ halfWidth - halfSize, minY = (rectDim[1]+.5f*rectDim[3]) - .5f*size;		
		float maxX = minX + size, maxY = minY + size;
		
		bndPts[0] = new myPointf[]{ new myPointf(minX, minY,0),
									new myPointf(maxX, minY,0),
									new myPointf(minX, maxY,0),
									new myPointf(maxX, maxY,0)};
		bndPts[1] = new myPointf[]{ new myPointf(minX + widthPerMap, minY,0),
									new myPointf(maxX + widthPerMap, minY,0),
									new myPointf(minX + widthPerMap, maxY,0),
									new myPointf(maxX + widthPerMap, maxY,0)};
			
		return bndPts;
	}

	@Override
	protected final int initAllPrivBtns_Indiv(ArrayList<Object[]> tmpBtnNamesArray) {
		
		
		return _numPrivButtons;
	}//initAllPrivBtns

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
		if(getPrivFlags(drawMapIDX)) {	for(int i=0;i<maps[0].length;++i) {maps[0][i].drawLabels_2D(pa);}}
		
	}
	

	
	////////////////////////
	// keyboard and mouse

	
	@Override
	protected boolean hndlMouseMoveIndiv(int mouseX, int mouseY, myPoint mseClckInWorld) {
		
		return false;
	}

	@Override
	protected boolean hndlMouseClickIndiv(int mouseX, int mouseY, myPoint mseClckInWorld, int mseBtn) {
		
		return false;
	}

	@Override
	protected boolean hndlMouseDragIndiv(int mouseX, int mouseY, int pmouseX, int pmouseY, myPoint mouseClickIn3D,
			myVector mseDragInWorld, int mseBtn) {
		
		return false;
	}

	@Override
	protected void snapMouseLocs(int oldMouseX, int oldMouseY, int[] newMouseLoc) {
		

	}

	@Override
	protected void hndlMouseRelIndiv() {
		

	}


	@Override
	protected final myPoint getMsePtAs3DPt(myPoint mseLoc) {	return new myPoint(mseLoc.x, mseLoc.y, 0);}	


}
