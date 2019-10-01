package COTS_Morph_PKG.utils;

import COTS_Morph_PKG.managers.mapManagers.mapPairManager;
import COTS_Morph_PKG.maps.base.baseMap;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

/**
 * this class acts as an augmented struct to hold and calculate registration distances between fromMap and toMap
 * @author john
 *
 */
public class mapRegDist {
	public final mapPairManager mapMgr;
	/**
	 * from and to maps for current registration
	 */
	public baseMap fromMap, toMap;
	/**
	 * copy map
	 */
	private baseMap copyMap;
	/**
	 * best distance between from man and to map
	 */
	private myVectorf dispBetweenMaps;
	private float[] angleAndScale;

	
	public mapRegDist(mapPairManager _mapMgr, baseMap _fromMap, baseMap _toMap) {
		mapMgr = _mapMgr;		
		fromMap =_fromMap;
		toMap = _toMap;
		dispBetweenMaps = new myVectorf();
		angleAndScale = new float[2];
		copyMap = mapMgr.buildCopyMapOfPassedMapType(fromMap, fromMap.mapTitle+"_Copy");	
	}
	
	/**
	 * set from map and to map for distance calc
	 * @param _fromMap
	 * @param _toMap
	 * @param dispMod
	 * @param findClosestDist : possibly rotate map to re-align cntl points
	 * @param updateCopyMap
	 */
	public void setMapsAndCalc(baseMap _fromMap, baseMap _toMap) {
		fromMap =_fromMap;
		toMap = _toMap;		
		performCalc(false, false);
		
	}
	public void setMapsAndCalc(baseMap _fromMap, baseMap _toMap, boolean dispMod, boolean findClosestDist) {
		fromMap =_fromMap;
		toMap = _toMap;		
		performCalc(dispMod, findClosestDist);
		
	}//setMapsAndCalc
	
	public void performCalc(boolean dispMod, boolean findClosestDist) {
		dispBetweenMaps = new myVectorf();
		angleAndScale = new float[2];
		
		if(findClosestDist) {
			copyMap = findBestDifferenceBetweenMaps(fromMap, toMap, dispMod);			
		} else {
			copyMap = calcDifferenceBetweenMaps(fromMap, toMap, dispMod);
		}
				
	}
	/**
	 * need this to return map 
	 * @param fromMap
	 * @param toMap
	 * @return
	 */
	public final baseMap calcDifferenceBetweenMaps(baseMap fromMap, baseMap toMap) {return calcDifferenceBetweenMaps(fromMap, toMap, false);}	
	private baseMap calcDifferenceBetweenMaps(baseMap fromMap, baseMap toMap, boolean dispMod) {
		dispBetweenMaps = new myVectorf();
		angleAndScale = new float[2];
		toMap.findDifferenceToMe(fromMap, dispBetweenMaps, angleAndScale);
		if(dispMod) {
			mapMgr.win.getMsgObj().dispInfoMessage("mapRegDist", "calcDifferenceBetweenMaps", "Distance " + fromMap.mapTitle + " -> " + toMap.mapTitle + " | Displacement of COV : " +  dispBetweenMaps.toStrBrf() + " | Angle between Maps : " + angleAndScale[0] + " | Geometric Means Scale :" + angleAndScale[1]);
		}
		//System.out.println("Building copy of from map : " + fromMap.mapTitle);
		baseMap tmpMap = mapMgr.buildCopyMapOfPassedMapType(fromMap, fromMap.mapTitle+"_DiffMap");
		tmpMap.registerMeToVals(dispBetweenMaps, angleAndScale);
		//System.out.println("Done Building copy of from map : " + fromMap.mapTitle);
		return tmpMap;	
	}	
	
	
	public float findSqDistBetween2MapVerts(baseMap aMap, baseMap bMap) {
		float res = 0.0f;
		myPointf[] aCntlPts = aMap.getCntlPts(), bCntlPts = bMap.getCntlPts();
		for(int i=0;i<aCntlPts.length;++i) {res += myPointf._SqrDist(aCntlPts[i], bCntlPts[i]);}
		return res;
	}
	
	public baseMap findBestDifferenceBetweenMaps(baseMap fromMap, baseMap toMap, boolean dispMod) {
		baseMap bestMap = null;
		float tmpDistBetweenMaps, minDistBetweenMaps = 9999999999.9f;
		dispBetweenMaps = new myVectorf();
		angleAndScale = new float[2];
		for(int i=0;i<fromMap.getNumCntlPts();++i) {
			myVectorf tmpDispBetweenMaps = new myVectorf();
			float[] tmpAngleAndScale = new float[2];
			toMap.findDifferenceToMe(fromMap, i, tmpDispBetweenMaps, tmpAngleAndScale);
			baseMap tmpMap = mapMgr.buildCopyMapOfPassedMapType(fromMap, fromMap.mapTitle+"_DiffMap");
			tmpMap.shiftCntlPtIDXs(i);
			tmpMap.registerMeToVals(tmpDispBetweenMaps, tmpAngleAndScale);
			tmpDistBetweenMaps = findSqDistBetween2MapVerts(tmpMap, toMap);
			if(dispMod) {
				mapMgr.win.getMsgObj().dispInfoMessage("mapRegDist", "findBestDifferenceBetweenMaps", "Distance " + fromMap.mapTitle + " -> " + toMap.mapTitle + " == " + tmpDistBetweenMaps+" for i : "+ i +" | Displacement of COV : " +  tmpDispBetweenMaps.toStrBrf() + " | Angle between Maps : " + tmpAngleAndScale[0] + " | Geometric Means Scale :" + tmpAngleAndScale[1]);
			}
			if(tmpDistBetweenMaps < minDistBetweenMaps) {
				minDistBetweenMaps = tmpDistBetweenMaps;
				dispBetweenMaps = tmpDispBetweenMaps;
				angleAndScale = tmpAngleAndScale;
				bestMap = tmpMap;
			}
		}
		return bestMap;
	}
	
	public final void drawMaps_Main(boolean fillOrWf) {
		if(fillOrWf) {copyMap.drawMap_Fill();	} else {copyMap.drawMap_Wf();}
	}//drawMaps_Main
	
	public final void drawMaps_Aux(boolean drawTexture, boolean drawOrtho, boolean drawCntlPts,boolean showLbls, int _detail) {
		if(drawTexture)	{		copyMap.drawMap_Texture();}
		if(drawOrtho) {			copyMap.drawOrthoFrame();}
		if(drawCntlPts) {		copyMap.drawMap_CntlPts(false, _detail);}
		copyMap.drawHeaderAndLabels(showLbls,_detail);		
	}
	
	
	public final float drawRightSideMaps(float _yOff, float sideBarYDisp) {
		return copyMap.drawRtSdMenuDescr(_yOff, sideBarYDisp, true, true);
	}
	
	
	public myVectorf getDispBetweenMaps() {return dispBetweenMaps;}
	public float getAngle() {return angleAndScale[0];}
	public float getScale() {return angleAndScale[1];}
	public float[] getAngleAndScale() {return angleAndScale;}
	public baseMap getCopyMap() {return copyMap;}
}//class mapRegDist
