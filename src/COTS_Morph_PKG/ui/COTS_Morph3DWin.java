package COTS_Morph_PKG.ui;

import java.util.ArrayList;
import java.util.TreeMap;

import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import base_UI_Objects.my_procApplet;
import base_Utils_Objects.vectorObjs.myPoint;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVector;
import base_Utils_Objects.vectorObjs.myVectorf;

public class COTS_Morph3DWin  extends COTS_MorphWin {
	
	private int _numPrivButtons = numBaseCOTSWinPrivFlags + 0;

		//drag scale in 3D
	private static final float mseDrag3DScl = 1.5f;
	
	public COTS_Morph3DWin(my_procApplet _p, String _n, int _flagIdx, int[] fc, int[] sc, float[] rd, float[] rdClosed,String _winTxt) {
		super(_p, _n, _flagIdx, fc, sc, rd, rdClosed, _winTxt);		
		super.initThisWin(false);
	}
	
	@Override
	protected final void initMe_Indiv() {
		setFlags(drawMseEdge,true);
		setFlags(showRightSideMenu, true);
	}//initMe

	/**
	 * return the initial bounds for the maps in the world space
	 * @return 2-d array of 4 points - first idx is map idx, 2nd idx is 4 points
	 */
	protected final myPointf[][] getKeyFrameMapBndPts(){		
		myPointf[][] bndPts = new myPointf[2][4];
					
		float minX = pa.cubeBnds[0][0], minY = pa.cubeBnds[0][1], minZ = pa.cubeBnds[0][2];
		float maxX = pa.cubeBnds[0][0] + pa.cubeBnds[1][0], maxY = pa.cubeBnds[0][1] + pa.cubeBnds[1][1], maxZ = pa.cubeBnds[0][2] + pa.cubeBnds[1][2];
		
		
		bndPts[0] = new myPointf[]{ new myPointf(minX, maxY+1.0f, maxZ-1.0f),
									new myPointf(minX, minY, maxZ),
									new myPointf(minX, minY, minZ),
									new myPointf(minX, maxY, minZ)};
		bndPts[1] = new myPointf[]{ new myPointf(maxX, maxY-1.0f, maxZ-1.0f),
									new myPointf(maxX, minY, maxZ),
									new myPointf(maxX, minY, minZ),
									new myPointf(maxX, maxY, minZ)};
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
		
	}
	
	/**
	 * called by maps
	 * @param p
	 * @param lbl
	 * @param xOff
	 * @param yOff
	 */
	public final void _drawLabelAtPt(myPointf p, String lbl, float xOff, float yOff) {
		pa.pushMatrix();pa.pushStyle();	
		pa.translate(p);
		unSetCamOrient();
		pa.scale(txtSclVal);
		pa.text(lbl, xOff,yOff,0); 
		pa.popStyle();pa.popMatrix();
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
		myVectorf defVec = myVectorf._add(myVectorf._mult(currMseModMap.basisVecs[1], mseDrag3DScl*mseDragInWorld_f._dot(currMseModMap.basisVecs[1])), myVectorf._mult(currMseModMap.basisVecs[2],mseDrag3DScl* mseDragInWorld_f._dot(currMseModMap.basisVecs[2])));		
		myPointf mseClickIn3D_f = new myPointf(mouseClickIn3D.x, mouseClickIn3D.y, mouseClickIn3D.z);
			
		currMseModMap.mseDragInMap(defVec, mseClickIn3D_f,keyPressed,keyCodePressed);
		//currMseModMap.mseDrag_3D(mouseClickIn3D,mseDragInWorld,keyPressed,keyCodePressed);
	}

	
	@Override
	protected final void mouseRelease_IndivMorphWin(){
	}


	@Override
	protected final myPoint getMsePtAs3DPt(myPoint mseLoc) {		return new myPoint(mseLoc);	}

	

}//class COTS_Morph3DWin
