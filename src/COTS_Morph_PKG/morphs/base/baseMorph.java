package COTS_Morph_PKG.morphs.base;

import COTS_Morph_PKG.maps.base.baseMap;
import base_UI_Objects.windowUI.myDispWindow;

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
	 * current time in
	 */
	protected float t;
	
	/**
	 * current morph map - will be same type as passed maps
	 */
	protected baseMap curMorphMap;

	public baseMorph(baseMap _a, baseMap _b, baseMap _morphMap) {
		t=.5f;
		setMaps(_a,_b,_morphMap);
	}
	
	public final void setMaps(baseMap _a, baseMap _b, baseMap _morphMap) {
		mapA = _a;
		mapB = _b;		
		curMorphMap = _morphMap;
		calcMorph();
	}
	
	public final void setMorphT(float _t) {
		t=_t;
		calcMorph();		
	}
	
	
	/**
	 * use currently set t value to calculate morph
	 */
	protected final void calcMorph() {
		//calculate checker board and grid color morphs
		float at = 1.0f-t, bt = t;
		int[][] aPlyClrs = mapA.getPolyColors(),bPlyClrs = mapB.getPolyColors(), curPlyClrs = new int[aPlyClrs.length][aPlyClrs[0].length];
		for(int i=0;i<aPlyClrs.length;++i) {for(int j=0;j<aPlyClrs[i].length;++j) {	curPlyClrs[i][j] = calcMorph_Int(at,aPlyClrs[i][j],bt,bPlyClrs[i][j]);}}
		curMorphMap.setPolyColors(curPlyClrs);		
		int[] aGridClr = mapA.getGridColor(), bGridClr = mapB.getGridColor(), curGridClrs = new int[aGridClr.length];
		for(int i=0;i<aGridClr.length;++i) {curGridClrs[i]=calcMorph_Int(at,aGridClr[i],bt,bGridClr[i]);}
		curMorphMap.setGridColor(curGridClrs);
		
		//calculate geometry morph
		calcMorph_Indiv(at, bt);	
		//any global post-morph calc
		
	};
	protected abstract int calcMorph_Int(float tA, int AVal, float tB, int BVal);
	protected abstract void calcMorph_Indiv(float tA, float tB);
	
	public final void drawMorphedMap(boolean _isFill, boolean _drawCircles) {
		if(_isFill) {	curMorphMap.drawMap_Fill(false, _drawCircles);}
		else {			curMorphMap.drawMap_Wf(false, _drawCircles);}	
	}

	public final void drawHeaderAndLabels_2D(boolean _drawLabels) {							curMorphMap.drawHeaderAndLabels_2D(_drawLabels);}
	public final void drawHeaderAndLabels_3D(boolean _drawLabels, myDispWindow animWin) {	curMorphMap.drawHeaderAndLabels_3D(_drawLabels,animWin);}
	
}//class baseMorph
