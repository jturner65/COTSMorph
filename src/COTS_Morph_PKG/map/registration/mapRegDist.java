package COTS_Morph_PKG.map.registration;

import COTS_Morph_PKG.map.base.baseMap;
import COTS_Morph_PKG.mapManager.mapPairManager;
import base_Math_Objects.MyMathUtils;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;

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
	 * displacement vector, angle and scale difference between the two maps
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
	 * register "from" map to "to" map - this will transform the from map to be as close as possible to the "to" map and return the changes as parameters
	 * forces correspondence between vertices in both maps  - will orient maps to have there vertices closest to one another
	 * @param dispBetweenMaps displacement vector between maps
	 * @param angleAndScale array holding angle (idx 0) and scale (idx 1)
	 */
	protected void findDifferenceBetweenMaps(myVectorf dispBetweenMaps, float[] angleAndScale) {
		myPointf A = toMap.getCOV(), B = fromMap.getCOV();
		myPointf[] cntlPtsA = toMap.getCntlPts(), cntlPtsB = fromMap.getCntlPts();
		
		myVectorf AP, BP,rBP;
		float sin = 0.0f, cos = 0.0f;
		//geometric avg of vector lengths from cov to cntlpts
		double scl = 1.0;
		for(int i=0;i<cntlPtsA.length;++i) {
		  AP = new myVectorf(A, cntlPtsA[i]);
		  BP = new myVectorf(B, cntlPtsB[i]);
		  scl *= AP.magn/BP.magn;
		  //don't need to normalize since atan2 takes care of this (arctan(y/x))
		  rBP = BP.rotMeAroundAxis(fromMap.basisVecs[0], MyMathUtils.HALF_PI_F);
		  cos += AP._dot(BP);
		  sin += AP._dot(rBP);
		}
		angleAndScale[0] = (float) Math.atan2(sin,cos);
		dispBetweenMaps.set(new myVectorf(B,A));
		angleAndScale[1] = (float) Math.pow(scl, 1.0/cntlPtsA.length);
	}
	/**
	 * register "from" map to "to" map - this will transform the from map to be as close as possible to the "to" map and return the changes as parameters
	 * @param _idxOffset offset in cntl point array - use this to orient other control points for closer fit
	 * @param dispBetweenMaps displacement vector between maps
	 * @param angleAndScale array holding angle (idx 0) and scale (idx 1)
	 */
	protected void findDifferenceBetweenMaps(int _idxOffset, myVectorf dispBetweenMaps, float[] angleAndScale) {  
		myPointf A = toMap.getCOV(), B = fromMap.getCOV();
		myPointf[] cntlPtsA = toMap.getCntlPts(), cntlPtsB = fromMap.getCntlPts();
		
		myVectorf AP, BP,rBP;
		float sin = 0.0f, cos = 0.0f;
		//geometric avg of vector lengths from cov to cntlpts
		double scl = 1.0;
		for(int i=0;i<cntlPtsA.length;++i) {
		  AP = new myVectorf(A, cntlPtsA[(i+_idxOffset)%cntlPtsA.length]);
		  BP = new myVectorf(B, cntlPtsB[i]);
		  scl *= AP.magn/BP.magn;
		  rBP = BP.rotMeAroundAxis(fromMap.basisVecs[0], MyMathUtils.HALF_PI_F);
		  cos += AP._dot(BP);
		  sin += AP._dot(rBP);
		}
		angleAndScale[0] = (float) Math.atan2(sin,cos);
		dispBetweenMaps.set(new myVectorf(B,A));
		angleAndScale[1] = (float) Math.pow(scl, 1.0/cntlPtsA.length);
	}//findDifferenceToMe
	
	
	
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
		//toMap.findDifferenceToMe(fromMap, dispBetweenMaps, angleAndScale);
		findDifferenceBetweenMaps(dispBetweenMaps, angleAndScale);
		if(dispMod) {
			mapMgr.win.getMsgObj().dispInfoMessage("mapRegDist", "calcDifferenceBetweenMaps", "Distance " + fromMap.mapTitle + " -> " + toMap.mapTitle + " | Displacement of COV : " +  dispBetweenMaps.toStrBrf() + " | Angle between Maps : " + angleAndScale[0] + " | Geometric Means Scale :" + angleAndScale[1]);
		}
		baseMap tmpMap = mapMgr.buildCopyMapOfPassedMapType(fromMap, fromMap.mapTitle+"_DiffMap");
		tmpMap.registerMeToVals(dispBetweenMaps, angleAndScale);
		return tmpMap;	
	}	
	
	
	/**
	 * find the distance between two maps' vertices
	 * @param aMap
	 * @param bMap
	 * @return
	 */
	public final float findDistBetween2MapVerts(baseMap aMap, baseMap bMap) {
		float res = 0.0f;
		myPointf[] aCntlPts = aMap.getCntlPts(), bCntlPts = bMap.getCntlPts();
		for(int i=0;i<aCntlPts.length;++i) {res += myPointf._SqrDist(aCntlPts[i], bCntlPts[i]);}
		return (float) Math.sqrt(res);
	}
	
	public float findSqDistBetween2MapVerts(baseMap aMap, baseMap bMap) {
		float res = 0.0f;
		myPointf[] aCntlPts = aMap.getCntlPts(), bCntlPts = bMap.getCntlPts();
		for(int i=0;i<aCntlPts.length;++i) {res += myPointf._SqrDist(aCntlPts[i], bCntlPts[i]);}
		return res;
	}
	/**
	 * finds correspondence between maps verts that preserves orientation of verts and has minimum distance between maps (i.e. compares rotated version of to map with from map)
	 * @param fromMap
	 * @param toMap
	 * @param dispMod
	 * @return
	 */
	public baseMap findBestDifferenceBetweenMaps(baseMap fromMap, baseMap toMap, boolean dispMod) {
		baseMap bestMap = null;
		float tmpDistBetweenMaps, minDistBetweenMaps = 9999999999.9f;
		dispBetweenMaps = new myVectorf();
		angleAndScale = new float[2];
		int numCntlPts = fromMap.getNumCntlPts();
		for(int i=0;i<numCntlPts;++i) {
			myVectorf tmpDispBetweenMaps = new myVectorf();
			float[] tmpAngleAndScale = new float[2];
			//toMap.findDifferenceToMe(fromMap, i, tmpDispBetweenMaps, tmpAngleAndScale);
			findDifferenceBetweenMaps(i, tmpDispBetweenMaps, tmpAngleAndScale);
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
