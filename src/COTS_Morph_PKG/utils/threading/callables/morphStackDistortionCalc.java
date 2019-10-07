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
	 * morph used to measure distortion
	 */
	protected baseMorph currDistMsrMorph;
	
	protected final int mapType;
	
	/**
	 * monitor progress
	 */
	private final int progressBnd;
	private static final float progAmt = .2f;
	protected double progress = -progAmt;

	public morphStackDistortionCalc(MessageObject _msgObj, int _stExIDX, int _endExIDX, int _thdIDX, int _distCalcType, baseMorph _currDistMsrMorph, int _mapType, baseMap[] _morphSliceAra,baseMap[][][] _allPolyMaps, float[][][][] _ttlDistPerCell) {
		msgObj = _msgObj;
		stIdx = _stExIDX;
		endIdx = _endExIDX;
		thdIDX= _thdIDX;
		mapType = _mapType;
		morphSliceAra = _morphSliceAra;
		allPolyMaps = _allPolyMaps;
		ttlDistPerCell = _ttlDistPerCell;
		currDistMsrMorph = _currDistMsrMorph;
		currDistMsrMorph.setMorphSlices(3);
		distCalcType = _distCalcType;
		int diff = (int) ((endIdx-stIdx) * progAmt);
		progressBnd = diff < 1 ? 1 : diff;
	}

	
	protected final void incrProgress(int idx, String task) {
		if(((idx-stIdx) % progressBnd) == 0) {		
			progress += progAmt;	
			msgObj.dispInfoMessage("morphStackDistortionCalc","incrProgress::thdIDX=" + String.format("%02d", thdIDX)+" ", "Progress performing " + task +" at : " + String.format("%.2f",progress));
		}
		if(progress > 1.0) {progress = 1.0;}
	}
	public final double getProgress() {	return progress;}	
	
	private void displayAvgPerMapDistortion(float[][][][] distsPerMap, int k) {
		String tmp = "\t map ["+k+"] : ";
		float ttlDistOnMap = 0.0f;
		int count = 0;
		for(int i=0;i<distsPerMap[k].length;++i) {for(int j=0;j<distsPerMap[k][i].length;++j) { for(int p=0;p<distsPerMap[k][i][j].length;++p) {		ttlDistOnMap+=distsPerMap[k][i][j][p];++count;	}}}
		ttlDistOnMap/=count;
		msgObj.dispInfoMessage("morphStackDistortionCalc::ThdIDX::"+thdIDX, "calculateMorphDistortion", tmp+ " Average per cell distortion on entire map @ layer "+k+" : "+ ttlDistOnMap);
	}

	/**
	 * for all polys on same map/slice, find array of i,j distances between adjacent polys
	 * @param mapPolys
	 * @param _k slice in morph slices
	 * @param _mapTypeIDX
	 * @param currDistMsrMorphs
	 * @return
	 */
	protected final float[][][] calcDistOnEntireMap(baseMap[][] mapPolys,int _sliceIDX, int _mapTypeIDX){
		float[][][] res = new float[mapPolys.length][mapPolys[0].length][3];
		int lastValIDX = mapPolys.length-1;
		
		//for all adjacent skip 1 polys, treat each poly as a new map of type maptype, build morph between both
		int[][] idxs = new int[][] {{-1,-2, -3},{-4,-5,-6},{-7,-8,-9}};
		
		//top and bottom row - horizontal dist i distortion
		for(int i=1;i<lastValIDX;++i) {//top and bottom row
			int id = 0;
			for(int k=-1;k<2;++k) {	idxs[id][0] = i+k;idxs[id][1]=0;idxs[id][2]=_sliceIDX;	++id;} id=0;
			res[i][0][0] = calcDistortion(idxs, mapPolys[i-1][0],mapPolys[i+1][0], mapPolys[i][0], _mapTypeIDX);
			for(int k=-1;k<2;++k) {	idxs[id][0] = i+k;idxs[id][1]=lastValIDX;idxs[id][2]=_sliceIDX;	++id;} id=0;
			res[i][lastValIDX][0] = calcDistortion(idxs, mapPolys[i-1][lastValIDX],mapPolys[i+1][lastValIDX], mapPolys[i][lastValIDX], _mapTypeIDX);	
		}
		//left and right side - vertical dist j distortion
		for(int j=1;j<lastValIDX;++j) {
			int id = 0;
			for(int k=-1;k<2;++k) {	idxs[id][0] = 0;idxs[id][1]=j+k;idxs[id][2]=_sliceIDX;	++id;} id=0;
			res[0][j][1] = calcDistortion(idxs, mapPolys[0][j-1],mapPolys[0][j+1], mapPolys[0][j], _mapTypeIDX);
			for(int k=-1;k<2;++k) {	idxs[id][0] = lastValIDX;idxs[id][1]=j+k;idxs[id][2]=_sliceIDX;	++id;} id=0;
			res[lastValIDX][j][1] = calcDistortion(idxs, mapPolys[lastValIDX][j-1],mapPolys[lastValIDX][j+1], mapPolys[lastValIDX][j], _mapTypeIDX);
		}
		//interior
		for(int i=1; i<lastValIDX;++i) {
			for(int j=1;j<lastValIDX;++j) {
				int id = 0; 
				for(int k=-1;k<2;++k) {	idxs[id][0] = i+k;idxs[id][1]=j;idxs[id][2]=_sliceIDX;	++id;} id=0;
				res[i][j][0] = calcDistortion(idxs, mapPolys[i-1][j],mapPolys[i+1][j], mapPolys[i][j], _mapTypeIDX);
				for(int k=-1;k<2;++k) {	idxs[id][1] = j+k;idxs[id][0]=i;idxs[id][2]=_sliceIDX;	++id;} id=0;
				res[i][j][1] = calcDistortion(idxs, mapPolys[i][j-1],mapPolys[i][j+1], mapPolys[i][j], _mapTypeIDX);
			}
		}
		return res;
	}
	
	/**
	 * calculate distortion between stPoly and endPoly - measure how far their average varies from middlePoly - distance
	 * @param idxs 3 x 3 array, idx 0 is st, end, or middle poly, idx 1 is i, j and k value
	 * @param stPoly start poly control points
	 * @param endPoly end poly control points
	 * @param middlePoly middle poly, to be compared to
	 * @param _mapTypeIDX type of map
	 * @param currDistMsrMorphs morph to use to calculate transformation
	 * @return distance between given middle map and morph map at t = .5f
	 */
	protected final float calcDistortion(int[][] idxs, baseMap stPoly, baseMap endPoly, baseMap middlePoly, int _mapTypeIDX) {
		//determine intermediate map
		currDistMsrMorph.setNewKeyFrameMaps(stPoly, endPoly);
		currDistMsrMorph.setMorphT(.5f);
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

//	
//	/**
//	 * find the distance between two maps' vertices
//	 * @param aMap
//	 * @param bMap
//	 * @return
//	 */
//	public final float findDistBetween2MapVerts(baseMap aMap, baseMap bMap) {
//		float res = findSqDistBetween2MapVerts(aMap, bMap);
//		return (float) Math.sqrt(res);
//	}
	
	
	
	@Override
	public Boolean call() throws Exception {
		switch(distCalcType) {
		case morphStackDistortionCalc_Runner.buildPolyCntlPtAraIDX 	:{	
			for(int k=stIdx;k<endIdx;++k) {		allPolyMaps[k]=morphSliceAra[k].buildPolyMaps();}
			break;}
		case morphStackDistortionCalc_Runner.mapWideDistCalcIDX 	:{		//calculate map distortion for rows and columns
			for(int k=stIdx;k<endIdx;++k) {		ttlDistPerCell[k] = calcDistOnEntireMap(allPolyMaps[k], k, mapType);	}			
			break;}
		case morphStackDistortionCalc_Runner.perMorphDistCalcIDX	:{		//calculate per-morph slice distortion along time dim
			baseMap stPoly, endPoly, midPoly;
			int[][] idxs = new int[][] {{-1,-2, -3},{-4,-5,-6},{-7,-8,-9}};
			//calc "lateral" distortion
			for(int k=stIdx;k<endIdx;++k) {	//for(int k=1;k<numMapSlices-1;++k) {		
				if((k==0) || (k==allPolyMaps.length-1)) {continue;}
				for(int i=0;i<allPolyMaps[k].length;++i) {
					for(int j=0;j<allPolyMaps[k][i].length;++j) {
						stPoly = allPolyMaps[k-1][i][j];
						endPoly = allPolyMaps[k+1][i][j];
						midPoly = allPolyMaps[k][i][j];
						int id=0;
						for(int l=-1;l<2;++l) {	idxs[id][0] = i;idxs[id][1]=j; idxs[id][2]=k+l;	++id;}
						ttlDistPerCell[k][i][j][2] = calcDistortion(idxs, stPoly,endPoly, midPoly, mapType);
					}
				}
			}	
			for(int k=stIdx;k<endIdx;++k) {	morphSliceAra[k].setDistCellColors(ttlDistPerCell[k]);	}
			break;}
		}
		
		
		return true;
	}

	
}//class morphStackDistortionCalc