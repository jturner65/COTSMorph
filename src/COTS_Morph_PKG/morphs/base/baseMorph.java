package COTS_Morph_PKG.morphs.base;

import java.util.ArrayList;

import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import base_UI_Objects.IRenderInterface;
import base_UI_Objects.my_procApplet;
import base_UI_Objects.windowUI.myDispWindow;
import base_Utils_Objects.vectorObjs.myPointf;

/**
 * class holding common functionality to morph between two image maps
 * @author john
 *
 */
public abstract class baseMorph {
	/**
	 * maps this morph is working on
	 */
	protected baseMap mapA, mapB;
	
	/**
	 * current time in morph
	 */
	protected float morphT;
	
	/**
	 * scope of morph interpolation : 0 is only control points; 1 is control points and internal variables
	 */
	protected int morphScope;
	/**
	 * current morph map - will be same type as passed maps
	 */
	private baseMap curMorphMap;
	
	/**
	 * solid representation of morphs
	 */
	protected baseMap[] morphMapArray;
	protected boolean buildMorphMapAra = false;
	
	protected my_procApplet pa;
	protected COTS_MorphWin win;
	public final String morphTitle;

	public baseMorph(COTS_MorphWin _win, baseMap _a, baseMap _b, baseMap _morphMap, int _morphScope, String _morphTitle) {
		win=_win; pa=myDispWindow.pa;morphTitle=_morphTitle;
		morphT=.5f;
		morphScope = _morphScope;
		setMaps(_a,_b,_morphMap);
	}
	
	public final void setMaps(baseMap _a, baseMap _b, baseMap _morphMap) {
		mapA = _a;
		mapB = _b;		
		curMorphMap = _morphMap;
		if(buildMorphMapAra) {setMorphMapAra();}
		calcMorph();
	}
	
	protected final void setMorphMapAra() {
		baseMap tmpMap;
		ArrayList<baseMap> mapAra = new ArrayList<baseMap>();
		for (float t = 0.0f; t <=1.0f; t+= .1f) {
			tmpMap = win.buildCopyMapOfPassedMapType(mapA, "Morph @ t="+String.format("%2d", t));
			float tA = 1.0f-t, tB = t;
			//initial code for morph, if necessary
			initCalcMorph_Indiv(tA, tB);
			//morph colors
			_morphColors(tmpMap, tA, tB);
			//calculate geometry morph
			//myPointf[] delPts = 
			calcMorphOfDeltaCntlPts(tmpMap, tA, tB);	
			//update map with point deltas
			//if(null!=delPts) {tmpMap.updateCntlPts(delPts);}
			mapAra.add(tmpMap);
		
		}
		morphMapArray = mapAra.toArray(new baseMap[0]);
	}//setMorphMapAra()
	
	
	public final void setMorphT(float _t) {
		morphT=_t;
		calcMorph();		
	}
	
	public final void setMorphScope(int _mScope) {
		morphScope = _mScope;
		calcMorph();	
	}
	
	private final void _morphColors(baseMap _curMorphMap, float tA, float tB) {
		//calculate checker board and grid color morphs
		int[][] aPlyClrs = mapA.getPolyColors(),bPlyClrs = mapB.getPolyColors(), curPlyClrs = new int[aPlyClrs.length][aPlyClrs[0].length];
		for(int i=0;i<aPlyClrs.length;++i) {for(int j=0;j<aPlyClrs[i].length;++j) {	curPlyClrs[i][j] = calcMorph_Integer(tA,aPlyClrs[i][j],tB,bPlyClrs[i][j]);}}
		_curMorphMap.setPolyColors(curPlyClrs);		
		int[] aGridClr = mapA.getGridColor(), bGridClr = mapB.getGridColor(), curGridClrs = new int[aGridClr.length];
		for(int i=0;i<aGridClr.length;++i) {curGridClrs[i] = calcMorph_Integer(tA,aGridClr[i],tB,bGridClr[i]);}
		_curMorphMap.setGridColor(curGridClrs);
	}
	
	/**
	 * use currently set t value to calculate morph
	 */
	protected final void calcMorph() {
		//update morph map with map a's vals, 
		curMorphMap.updateMeWithMapVals(mapA);
		float tA = 1.0f-morphT, tB = morphT;
		//initial code for morph, if necessary
		initCalcMorph_Indiv(tA, tB);
		//morph colors
		_morphColors(curMorphMap, tA, tB);
		//calculate geometry morph - find delta to use with control points
		//myPointf[] delPts = 
		calcMorphOfDeltaCntlPts(curMorphMap, tA, tB);	
		//update map with point deltas
		//if(null!=delPts) {curMorphMap.updateCntlPts(delPts);}
		//any global post-morph calc		
	}
	
	protected abstract void initCalcMorph_Indiv(float tA, float tB);
	
	protected abstract int calcMorph_Integer(float tA, int AVal, float tB, int BVal);	
	protected abstract float calcMorph_Float(float tA, float AVal, float tB, float BVal);
	protected abstract double calcMorph_Double(float tA, double AVal, float tB, double BVal);
	protected abstract myPointf calcMorph_Point(float tA, myPointf AVal, float tB, myPointf BVal);
	
	/**
	 * use currently set t value to calculate morph
	 */
	protected final myPointf[] calcMorphOfDeltaCntlPts(baseMap _curMorphMap, float tA, float tB) {
		myPointf[] aCntlPts = mapA.getCntlPts(), bCntlPts = mapB.getCntlPts(), morphCntlPts = _curMorphMap.getCntlPts();
		myPointf[] newPts = new myPointf[aCntlPts.length];
		myPointf[] delPts = new myPointf[aCntlPts.length];
		for(int i=0;i<aCntlPts.length;++i) {	
			newPts[i]= calcMorph_Point(tA, aCntlPts[i], tB, bCntlPts[i]);//myPointf._add(myPointf._mult(aCntlPts[i], tA), myPointf._mult(bCntlPts[i], tB));
			delPts[i] = myPointf._sub(newPts[i], morphCntlPts[i]);
		}
		_curMorphMap.updateCntlPts(delPts);
		newPts[newPts.length-2] = _curMorphMap.getCOV();
		newPts[newPts.length-1] = _curMorphMap.getCenterPoint();		
		return newPts;
	}

	

	//////////////////////////////
	// draw routines	
	
	public final float drawMapRtSdMenuDescr(float yOff, float sideBarYDisp) {
		if(null == curMorphMap) {return yOff;}	
		
		pa.pushMatrix();pa.pushStyle();
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Green, 255), 6.5f, morphTitle);
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 5.6f, " Morph Frame @ Time : ");
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Yellow, 255), 6.5f, String.format("%.4f",morphT));
		pa.popStyle();pa.popMatrix();
	
		
		//pa.showOffsetText(0,IRenderInterface.gui_White, morphTitle+" Morph Frame @ Time : " +  String.format("%.4f",morphT));
		yOff += sideBarYDisp;
		pa.translate(10.0f,sideBarYDisp, 0.0f);		
		yOff = curMorphMap.drawRightSideBarMenuDescr(yOff, sideBarYDisp, false);
		
		
		return yOff;
	}
	public final float drawMorphRtSdMenuDescr(float yOff, float sideBarYDisp, float _morphSpeed, String[] _scopeList) {
		pa.pushMatrix();pa.pushStyle();
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Green, 255), 6.0f, morphTitle);
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 6.5f, "  Morph Between :");
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Yellow, 255), 6.5f, mapA.mapTitle);
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 6.5f, " and");
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Yellow, 255), 6.5f, mapB.mapTitle);
		pa.popStyle();pa.popMatrix();		
		yOff += sideBarYDisp;
		pa.translate(0.0f,sideBarYDisp, 0.0f);		
		
		pa.pushMatrix();pa.pushStyle();
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 6.0f, "Currently at time :");
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Yellow, 255), 6.5f, String.format("%.4f",morphT));
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 6.5f, " | Speed :");
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Yellow, 255), 6.5f, String.format("%.4f",_morphSpeed));
		pa.popStyle();pa.popMatrix();		
		
		yOff += sideBarYDisp;
		pa.translate(0.0f,sideBarYDisp, 0.0f);		
		
		pa.pushMatrix();pa.pushStyle();
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 6.0f, "Morph Scope : ");
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Yellow, 255), 6.5f, _scopeList[morphScope]);
		pa.popStyle();pa.popMatrix();		
		
		yOff += sideBarYDisp;
		pa.translate(0.0f,sideBarYDisp, 0.0f);	
		
		yOff = drawMorphRtSdMenuDescr_Indiv(yOff, sideBarYDisp);
		
		return yOff;
	}
	
	protected abstract float drawMorphRtSdMenuDescr_Indiv(float yOff, float sideBarYDisp);
	
	public final void drawMorphedMap_CntlPts() {
		curMorphMap.drawMap_CntlPts(false);
	}
	
	/**
	 * draw a point of a particular radius
	 * @param p point to draw
	 * @param rad radius of point
	 */
	protected void _drawPt(myPointf p, float rad) {
		pa.pushMatrix();pa.pushStyle();	
		pa.translate(p);
		pa.sphere(rad);
		pa.popStyle();pa.popMatrix();	
	}


	public final void drawMorphedMap_CntlPtTraj() {
		baseMap tmpMap = win.buildCopyMapOfPassedMapType(mapA, "MorphTmp");
		pa.pushMatrix();pa.pushStyle();	
		pa.sphereDetail(5);
		pa.stroke(0,0,0,255);
		myPointf[] cntlPtsOld = calcMorphOfDeltaCntlPts(tmpMap,1.0f, 0.0f);
		pa.strokeWeight(1.0f);
		for(float t = 0.01f;t<=1.0f;t+=.01f) {
			float tA = 1.0f-t, tB = t;
			initCalcMorph_Indiv(tA, tB);
			myPointf[] cntlPts = calcMorphOfDeltaCntlPts(tmpMap,tA, tB);
			//idx 5 is cov, idx 6 is ctr pt
			if(null != cntlPts) {
				for(int i = 0;i<cntlPts.length;++i) {		_drawPt(cntlPts[i], 2.0f); pa.line(cntlPtsOld[i], cntlPts[i]);}
			}
			cntlPtsOld = cntlPts;			
		}
		
		pa.popStyle();pa.popMatrix();	
	}


	
	public final void drawMorphedMap(boolean _isFill, boolean _drawCircles) {
		if(_isFill) {	curMorphMap.drawMap_Fill(false);}
		else {			curMorphMap.drawMap_Wf(false);}	
		if(_drawCircles) {
			if(_isFill) {	curMorphMap.drawMap_PolyCircles_Fill();}	
			else {			curMorphMap.drawMap_PolyCircles_Wf();}		
		}
	}

	public final void drawHeaderAndLabels_2D(boolean _drawLabels) {							curMorphMap.drawHeaderAndLabels_2D(_drawLabels);}
	public final void drawHeaderAndLabels_3D(boolean _drawLabels, myDispWindow animWin) {	curMorphMap.drawHeaderAndLabels_3D(_drawLabels,animWin);}
	
}//class baseMorph
