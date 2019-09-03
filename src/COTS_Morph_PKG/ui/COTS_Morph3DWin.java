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
	protected final myPointf[][] get2MapBndPts(){
		myPointf[][] bndPts = new myPointf[2][4];
		
//		//boundary regions for enclosing cube - given as min and difference of min and max
//		public float[][] cubeBnds = new float[][]{//idx 0 is min, 1 is diffs
//			new float[]{-gridDimX/2.0f,-gridDimY/2.0f,-gridDimZ/2.0f},//mins
//			new float[]{gridDimX,gridDimY,gridDimZ}};			//diffs
			
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
	protected final void _drawMe_Indiv(float animTimeMod, boolean showLbls){
		for(int i=0;i<maps[0].length;++i) {maps[currMapTypeIDX][i].drawHeaderAndLabels_3D( showLbls, this);}
		if(getPrivFlags(drawMorph_MapIDX)) {	morphs[currMorphTypeIDX].drawHeaderAndLabels_3D( showLbls, this);}
	}
	
	
	
	////////////////////////
	// keyboard and mouse

	@Override
	protected final boolean hndlMouseMoveIndiv(int mouseX, int mouseY, myPoint mseClckInWorld) {
		
		return false;
	}
	
	private myPointf _rayOrigin;
	private myVectorf _rayDir;

	@Override
	protected final boolean hndlMouseClickIndiv(int mouseX, int mouseY, myPoint mseClckInWorld, int mseBtn) {
		//check every map for closest control corner to click location
		TreeMap<Float,baseMap>  mapDists = new TreeMap<Float,baseMap>();
		//msgObj.dispInfoMessage("COTS_Morph3DWin", "hndlMouseClickIndiv", "Mouse button pressed : " + mseBtn + " Key Pressed : " + keyPressed + " Key Coded : " + keyCodePressed);
		//get a point on ray through mouse location in world
		_rayOrigin = pa.c.getMseLoc_f();
		_rayDir = pa.c.getEyeToMouseRay_f();
		//myVectorf _rayDir = new myVectorf(pa.c.getMse2DtoMse3DinWorld(focusTar));
		for(int j=0;j<maps[currMapTypeIDX].length;++j) {	
			mapDists.put(maps[currMapTypeIDX][j].findClosestCntlPt_3D(mseClckInWorld, _rayOrigin, _rayDir), maps[currMapTypeIDX][j]);
		}
		Float minSqDist = mapDists.firstKey();
		if((minSqDist < minSqClickDist) || (keyPressed=='s') || (keyPressed=='S')  || (keyPressed=='r') || (keyPressed=='R'))  {
			currMseModMap = mapDists.get(minSqDist);
			return true;
		}
		currMseModMap = null;
		return false;
	}

	@Override
	protected final boolean hndlMouseDragIndiv(int mouseX, int mouseY, int pmouseX, int pmouseY, myPoint mouseClickIn3D, myVector mseDragInWorld, int mseBtn) {
		if(currMseModMap != null) {
			currMseModMap.mseDrag_3D(mouseClickIn3D,mseDragInWorld,keyPressed,keyCodePressed);
			return true;
		}
		return false;
	}
	
	@Override
	protected final void mouseRelease_IndivMorphWin(){
		_rayOrigin = null;
		_rayDir = null;
	}


	@Override
	protected final myPoint getMsePtAs3DPt(myPoint mseLoc) {		return new myPoint(mseLoc);	}

	

}//class COTS_Morph3DWin
