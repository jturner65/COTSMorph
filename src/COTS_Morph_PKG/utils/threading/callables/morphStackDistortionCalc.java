package COTS_Morph_PKG.utils.threading.callables;

import java.util.concurrent.Callable;

import COTS_Morph_PKG.map.base.baseMap;
import COTS_Morph_PKG.morph.base.baseMorph;
import COTS_Morph_PKG.utils.runners.morphStackDistortionCalc_Runner;
import base_Utils_Objects.io.MessageObject;
import base_Utils_Objects.vectorObjs.myPointf;

public class morphStackDistortionCalc implements Callable<Boolean>{
	/**
	 * message object
	 */
	protected final MessageObject msgObj;
	/**
	 * start and end index in array of data, thread index, # of 
	 */
	protected final int stIdx, endIdx, thdIDX;	
	/**
	 * whether the distortion type to calculate is i/j distortion on map, or between morph slices
	 */
	protected final int distCalcType;
	
	/////////	refs to map arrays and output data
	/**
	 * these are all the map slices in the morph stack
	 */
	protected baseMap[] morphSliceAra;

	/**
	 * these are all the control points for the current morphstack
	 */
	protected baseMap[][][] allPolyMaps;
	/**
	 * output : scalar value of distortion at each cell k,i,j, with final index being direction of distortion measure
	 */
	public float[][][][] ttlDistPerCell;
	/**
	 * avg distortion in each cell
	 */
	public float[][][] avgDistPerCell;

	/**
	 * morph used to measure distortion
	 */
	protected baseMorph currDistMsrMorph;
		
	/**
	 * monitor progress
	 */
	private int progressBnd;
	private static final float progAmt = .2f;
	protected double progress = -progAmt;

	public morphStackDistortionCalc(MessageObject _msgObj, int _stExIDX, int _endExIDX, int _thdIDX, int _distCalcType, baseMorph _currDistMsrMorph, baseMap[] _morphSliceAra,baseMap[][][] _allPolyMaps, float[][][][] _ttlDistPerCell, float[][][] _avgDistPerCell) {
		msgObj = _msgObj;
		stIdx = _stExIDX;
		endIdx = _endExIDX;
		thdIDX= _thdIDX;
		morphSliceAra = _morphSliceAra;
		avgDistPerCell = _avgDistPerCell;
		allPolyMaps = _allPolyMaps;
		ttlDistPerCell = _ttlDistPerCell;
		currDistMsrMorph = _currDistMsrMorph;
		currDistMsrMorph.setMorphSlices(3);
		distCalcType = _distCalcType;		
		
	}
	
	protected final void incrProgress(int idx, String task) {
		if((idx % progressBnd) == 0) {		
			progress += progAmt;	
			msgObj.dispInfoMessage("morphStackDistortionCalc","incrProgress::thdIDX=" + String.format("%02d", thdIDX)+" ", "Progress performing " + task +" at : " + String.format("%.2f",progress));
		}
		if(progress > 1.0) {progress = 1.0;}
	}
	public final double getProgress() {	return progress;}	
	
//	private void displayAvgPerMapDistortion(float[][][][] distsPerMap, int k) {
//		String tmp = "\t map ["+k+"] : ";
//		float ttlDistOnMap = 0.0f;
//		int count = 0;
//		for(int i=0;i<distsPerMap[k].length;++i) {for(int j=0;j<distsPerMap[k][i].length;++j) { for(int p=0;p<distsPerMap[k][i][j].length;++p) {		ttlDistOnMap+=distsPerMap[k][i][j][p];++count;	}}}
//		ttlDistOnMap/=count;
//		msgObj.dispInfoMessage("morphStackDistortionCalc::ThdIDX::"+thdIDX, "calculateMorphDistortion", tmp+ " Average per cell distortion on entire map @ layer "+k+" : "+ ttlDistOnMap);
//	}

	/**
	 * for all polys on same map/slice, find array of i,j distances between adjacent polys
	 * @param mapPolys
	 * @return
	 */
	protected final float[][][] calcDistOnEntireMap(baseMap[][] mapPolys){
		float[][][] res = new float[mapPolys.length][mapPolys[0].length][3];
		int lastValIDX = mapPolys.length-1;
		
		
		for(int i=1;i<lastValIDX;++i) {
			//top and bottom rows
			res[i][0][0] = calcDistortion(mapPolys[i-1][0],mapPolys[i+1][0], mapPolys[i][0]);
			res[i][lastValIDX][0] = calcDistortion(mapPolys[i-1][lastValIDX],mapPolys[i+1][lastValIDX], mapPolys[i][lastValIDX]);	
			//first and last columns
			res[0][i][1] = calcDistortion(mapPolys[0][i-1],mapPolys[0][i+1], mapPolys[0][i]);
			res[lastValIDX][i][1] = calcDistortion(mapPolys[lastValIDX][i-1],mapPolys[lastValIDX][i+1], mapPolys[lastValIDX][i]);
		}

		//interior
		for(int i=1; i<lastValIDX;++i) {
			for(int j=1;j<lastValIDX;++j) {
				res[i][j][0] = calcDistortion(mapPolys[i-1][j],mapPolys[i+1][j], mapPolys[i][j]);
				res[i][j][1] = calcDistortion(mapPolys[i][j-1],mapPolys[i][j+1], mapPolys[i][j]);
			}
		}
		return res;
	}

	/**
	 * calculate distortion between stPoly and endPoly - measure how far their average varies from middlePoly - distance
	 * @param stPoly start poly control points
	 * @param endPoly end poly control points
	 * @param middlePoly middle poly, to be compared to
	 * @param _mapTypeIDX type of map
	 * @param currDistMsrMorphs morph to use to calculate transformation
	 * @return distance between given middle map and morph map at t = .5f
	 */
	public final float calcDistortion(baseMap stPoly, baseMap endPoly, baseMap middlePoly) {
		//determine intermediate map
		currDistMsrMorph.setNewKeyFrameMaps(stPoly, endPoly, false);
//		currDistMsrMorph.getNumMorphSlices();
//		currDistMsrMorph.setMorphT(.5f);
		currDistMsrMorph.calcMorphCompare(.5f);
		//use intermediate morph map to be comparator to intermediate actual map
		baseMap currMorphMap = currDistMsrMorph.getCurMorphMap();
		//calculate distortion as square distance between corresponding vertices
		float res = findSqDistBetween2MapVerts(middlePoly, currMorphMap);
		//float res = findDistBetween2MapVerts(middlePoly, currMorphMap);
		return res;
	}
	
	/**
	 * calculate all distortion on curMap @ time t
	 * @param stMap	keyframe map previous
	 * @param endMap keyframe map next
	 * @param curMap
	 * @param t
	 * @return
	 */
	public final float[][][] calcAllDistOnPassedMap(baseMap stMap, baseMap endMap, baseMap curMap){
		baseMap[][] stPolyMaps = stMap.buildPolyMaps(), endPolyMaps = endMap.buildPolyMaps(); 
		baseMap[][] curMapPolys = curMap.buildPolyMaps(); 
		float t = (curMap.getCurMorphTVal() - stMap.getCurMorphTVal())/(endMap.getCurMorphTVal() - stMap.getCurMorphTVal());
		//calculate row and column distortion
		float[][][] res = calcDistOnEntireMap(curMapPolys);
		for(int i=0;i<stPolyMaps.length;++i) {
			for(int j=0;j<stPolyMaps[i].length;++j) {
				res[i][j][2] = calcDistortionAtTime(stPolyMaps[i][j],endPolyMaps[i][j], curMapPolys[i][j], t);
			}
		}	
		return res;
	}
	
	
	/**
	 * calculate distortion between stPoly and endPoly - measure how far their average varies from middlePoly - distance
	 * @param stPoly start poly control points
	 * @param endPoly end poly control points
	 * @param middlePoly middle poly, to be compared to
	 * @param currDistMsrMorphs morph to use to calculate transformation
	 * @return distance between given middle map and morph map at t = .5f
	 */
	public final float calcDistortionAtTime(baseMap stPoly, baseMap endPoly, baseMap middlePoly, float t) {
		//determine intermediate map
		currDistMsrMorph.setNewKeyFrameMaps(stPoly, endPoly, false);
//		currDistMsrMorph.getNumMorphSlices();
//		currDistMsrMorph.setMorphT(.5f);
		currDistMsrMorph.calcMorphCompare(t);
		//use intermediate morph map to be comparator to intermediate actual map
		baseMap currMorphMap = currDistMsrMorph.getCurMorphMap();
		//calculate distortion as square distance between corresponding vertices
		float res = findSqDistBetween2MapVerts(middlePoly, currMorphMap);
		//float res = findDistBetween2MapVerts(middlePoly, currMorphMap);
		return res;
	}
	
	/**
	 * find the square distance between two maps' vertices
	 * @param aMap
	 * @param bMap
	 * @return
	 */
	public final float findSqDistBetween2MapVerts(baseMap aMap, baseMap bMap) {
		float res = 0.0f;
		myPointf[] aCntlPts = aMap.getCntlPts(), bCntlPts = bMap.getCntlPts();
		for(int i=0;i<aCntlPts.length;++i) {res += myPointf._SqrDist(aCntlPts[i], bCntlPts[i]);}
		return res;
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
	
	private void calcPerMorphStackDist() {
		int incr = 0;
		baseMap stPoly, endPoly, midPoly;
		//calc "lateral" distortion
		for(int k=stIdx;k<endIdx;++k) {	//for(int k=1;k<numMapSlices-1;++k) {		
			if((k==0) || (k==allPolyMaps.length-1)) {continue;}
			for(int i=0;i<allPolyMaps[k].length;++i) {
				for(int j=0;j<allPolyMaps[k][i].length;++j) {
					stPoly = allPolyMaps[k-1][i][j];
					endPoly = allPolyMaps[k+1][i][j];
					midPoly = allPolyMaps[k][i][j];
					ttlDistPerCell[k][i][j][2] = calcDistortion(stPoly,endPoly, midPoly);
					incrProgress(++incr, "Build PerMorph Dist Calc");
				}
			}					
		}		
	}
	
	@Override
	public Boolean call() throws Exception {
		int diff = (int) ((endIdx-stIdx) * progAmt);
		progressBnd = diff < 1 ? 1 : diff;
		switch(distCalcType) {
			case morphStackDistortionCalc_Runner.buildPolyCntlPtAraIDX 	:{	
				for(int k=stIdx;k<endIdx;++k) {		allPolyMaps[k]=morphSliceAra[k].buildPolyMaps(); }//incrProgress(incr++, "Build Poly CntlPt Aras");}
				break;}
			case morphStackDistortionCalc_Runner.mapWideDistCalcIDX 	:{		//calculate map distortion for rows and columns
				for(int k=stIdx;k<endIdx;++k) {		ttlDistPerCell[k] = calcDistOnEntireMap(allPolyMaps[k]); }//incrProgress(incr++, "Calc Map Wide Dist");	}			
				break;}
			case morphStackDistortionCalc_Runner.perMorphDistCalcIDX	:{		//calculate per-morph slice distortion along time dim
				diff = (int) ( (allPolyMaps[stIdx].length *allPolyMaps[stIdx][0].length * (endIdx-stIdx)) * progAmt);
				progressBnd = diff < 1 ? 1 : diff;
				
				calcPerMorphStackDist();
				
				for(int k=stIdx;k<endIdx;++k) {	morphSliceAra[k].setDistCellColors(ttlDistPerCell[k]);	}
				//calc average distortion per cell
				for(int k=stIdx;k<endIdx;++k) {	
					for(int i=0;i<ttlDistPerCell[k].length;++i) {
						for(int j=0;j<ttlDistPerCell[k][i].length;++j) {
							avgDistPerCell[k][i][j] = (ttlDistPerCell[k][i][j][0] + ttlDistPerCell[k][i][j][1] +ttlDistPerCell[k][i][j][2])/3.0f;						
						}
					}
				}			
				break;}
		}		
		
		return true;
	}

	
}//class morphStackDistortionCalc