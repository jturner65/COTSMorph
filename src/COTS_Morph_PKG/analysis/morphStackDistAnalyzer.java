package COTS_Morph_PKG.analysis;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;

import COTS_Morph_PKG.analysis.base.baseAnalyzer;
import COTS_Morph_PKG.analysis.stats.base.baseProbSummary;
import COTS_Morph_PKG.map.base.baseMap;
import COTS_Morph_PKG.mapManager.mapPairManager;
import COTS_Morph_PKG.morph.base.baseMorph;
import COTS_Morph_PKG.utils.runners.morphStackDistortionCalc_Runner;
import base_UI_Objects.my_procApplet;
import base_Utils_Objects.vectorObjs.myPointf;

public class morphStackDistAnalyzer extends baseAnalyzer {
	/**
	 * owning map manager
	 */
	public final mapPairManager mapMgr;
	public final int mapType;
	/**
	 * threading constructions - allow map manager to own its own threading executor
	 */
	protected ExecutorService th_exec;	//to access multithreading - instance from calling program
	protected final int numUsableThreads;		//# of threads usable by the application
	
	/**
	 * these are all the control points for the current morphstack
	 */
	protected baseMap[][][] allPolyMaps;
	/**
	 * scalar value of distortion at each cell k,i,j, with final index being direction of distortion measure
	 */
	public float[][][][] ttlDistPerCell;

	/**
	 * avg distortion in each cell
	 */
	protected float[][][] avgDistPerCell;
	/**
	 * average distortion across entire morphstack
	 */
	protected float ttlDistForEntireMrphStck;
	/**
	 * thread runner for distortion calculation
	 */
	protected morphStackDistortionCalc_Runner  distCalcRunner;
	
	//private TreeMap<String, myVectorfTrajAnalyzer> distVecAnalyzers;

	public morphStackDistAnalyzer(mapPairManager _mapMgr) {
		super();
		mapMgr=_mapMgr;mapType = mapMgr.mapType;
		th_exec = mapMgr.getTh_Exec();
		numUsableThreads = mapMgr.getNumUsableThreads();
		//(mapPairManager _mapMgr, ExecutorService _th_exec, boolean _canMT, int _numThds, int _numWorkUnits, int _mapType)
		distCalcRunner = new morphStackDistortionCalc_Runner(mapMgr, th_exec, true, numUsableThreads, 1, mapType);
		
	}
	
	public float[][][] getAvgDistPerCell(){return avgDistPerCell;}
	public float[][][][] getTtlDistPerCell(){return ttlDistPerCell;}
	public float getTtlDistForEntireMrphStck() {return ttlDistForEntireMrphStck;}
	
	/**
	 * calculate the distortion in the current morph
	 * @param _currDistTransformIDX index in morph list for transformatio to use to calculate distortion
	 */

	public final void calculateAllDistortions( baseMorph _currDistMsrMorph, baseMap[] morphSliceAra) {	
		baseMorph currDistMsrMorph = _currDistMsrMorph;
		currDistMsrMorph.setMorphSlices(3);
		
		distCalcRunner.setAllInitMapVals(currDistMsrMorph, morphSliceAra);
		distCalcRunner.setDistCalcType(morphStackDistortionCalc_Runner.buildPolyCntlPtAraIDX);
		distCalcRunner.runMe();
		allPolyMaps = distCalcRunner.getAllPolyMaps();
		mapMgr.msgObj.dispInfoMessage("morphStackDistAnalyzer", "calculateAllDistortions",  "allPolyMaps has : " + allPolyMaps.length + " slices with : "+ allPolyMaps[0].length +" columns and " + allPolyMaps[0][0].length + " rows ");
		
		distCalcRunner.setDistCalcType(morphStackDistortionCalc_Runner.mapWideDistCalcIDX);
		distCalcRunner.runMe();
		
		distCalcRunner.setDistCalcType(morphStackDistortionCalc_Runner.perMorphDistCalcIDX);
		distCalcRunner.runMe();

		ttlDistPerCell = distCalcRunner.getTtlDistPerCell();
		int numMapSlices = allPolyMaps.length,
				numCols = allPolyMaps[0].length,
				numRows = allPolyMaps[0][0].length;//,		
		
		//aggregate all distortions
		avgDistPerCell = new float[numMapSlices][numCols][numRows]; 
		ttlDistForEntireMrphStck = 0.0f;
		int count = 0;
		for(int k=0;k<numMapSlices;++k) {
			for(int i=0;i<numCols;++i) {
				for(int j=0;j<numRows;++j) {
					avgDistPerCell[k][i][j] = (ttlDistPerCell[k][i][j][0] + ttlDistPerCell[k][i][j][1] +ttlDistPerCell[k][i][j][2])/3.0f;
					ttlDistForEntireMrphStck +=avgDistPerCell[k][i][j];++count;
				}
			}
			displayAvgPerMapDistortion(ttlDistPerCell, k);	
		}
		ttlDistForEntireMrphStck/=count;
		
	}//calculateMorphDistortion
	
	
//	/**
//	 * calculate the distortion in the current morph
//	 * @param _currDistTransformIDX index in morph list for transformatio to use to calculate distortion
//	 */
//
//	public final void calculateAllDistortions( baseMorph _currDistMsrMorph, baseMap[][][] _allPolyMaps) {	
//		currDistMsrMorph = _currDistMsrMorph;
//		currDistMsrMorph.setMorphSlices(3);
//		allPolyMaps = _allPolyMaps;
//		int numMapSlices = allPolyMaps.length,
//			numCols = allPolyMaps[0].length,
//			numRows = allPolyMaps[0][0].length;//,
//			//numPts = allPolyCntlPts[0][0][0].length;
//		//measure per-map distortions @ each i,j
//		//allPolyCntrLocs = new myPointf[numMapSlices][numCols][numRows];
//		//for(int k=0;k<numMapSlices;++k) {for(int i=0;i<numCols;++i) {for(int j=0;j<numRows;++j) {allPolyCntrLocs[k][i][j] = myPointf._average(allPolyMaps[k][i][j]);}}}
//		ttlDistPerCell = new float[numMapSlices][numCols][numRows][3];
//			
//			//calcluate for map
//		for(int k=0;k<numMapSlices;++k) {			
//			ttlDistPerCell[k] = calcDistOnEntireMap(allPolyMaps[k], k, mapType, currDistMsrMorph);
//			
//		}
//	
//		baseMap stPoly, endPoly, midPoly;
//		int[][] idxs = new int[][] {{-1,-2, -3},{-4,-5,-6},{-7,-8,-9}};
//		//calc "lateral" distortion
//		for(int k=1;k<numMapSlices-1;++k) {			
//			int count = 0;
//			for(int i=0;i<numCols;++i) {
//				for(int j=0;j<numRows;++j) {
//					stPoly = allPolyMaps[k-1][i][j];
//					endPoly = allPolyMaps[k+1][i][j];
//					midPoly = allPolyMaps[k][i][j];
//					int id=0;
//					for(int l=-1;l<2;++l) {	idxs[id][0] = i;idxs[id][1]=j; idxs[id][2]=k+l;	++id;}
//					ttlDistPerCell[k][i][j][2] = calcDistortion(idxs, stPoly,endPoly, midPoly, mapType, currDistMsrMorph);
//				}
//			}
//		}
//		
//		
//		//aggregate all distortions
//		avgDistPerCell = new float[numMapSlices][numCols][numRows]; 
//		ttlDistForEntireMrphStck = 0.0f;
//		int count = 0;
//		for(int k=0;k<numMapSlices;++k) {
//			int countPerSlice = 0;
//			float ttlDistsForSlice = 0.0f;
//			displayAvgPerMapDistortion(ttlDistPerCell, k);
//			for(int i=0;i<numCols;++i) {
//				for(int j=0;j<numRows;++j) {
//					avgDistPerCell[k][i][j] = (ttlDistPerCell[k][i][j][0] + ttlDistPerCell[k][i][j][1] +ttlDistPerCell[k][i][j][2])/3.0f;
//					//mapMgr.win.getMsgObj().dispInfoMessage("morphStackDistAnalyzer", "calculateMorphDistortion", "\tDist per cell "+i +","+j+" on map "+k+" : "+ distPerCell[k][i][j] + " vector dist magn : " + ttlDistVecs[k][i][j].magn);
//					ttlDistForEntireMrphStck +=avgDistPerCell[k][i][j];++count;
//					ttlDistsForSlice +=ttlDistPerCell[k][i][j][2];++countPerSlice;
//					
//				}
//			}
//			ttlDistsForSlice/=countPerSlice;
//			mapMgr.win.getMsgObj().dispInfoMessage("morphStackDistAnalyzer", "calculateMorphDistortion", "\tAverage Total distortion over adjacent slices on map @ layer "+k+" : "+ ttlDistsForSlice);
//		}
//		ttlDistForEntireMrphStck/=count;
//		
//	}//calculateMorphDistortion
	
	private void displayAvgPerMapDistortion(float[][][][] distsPerMap, int k) {
		String tmp = "\t map ["+k+"] : ";
		float[] ttlDistOnMap = new float[distsPerMap[0][0][0].length];
		int count = 0;
		for(int i=0;i<distsPerMap[k].length;++i) {
			for(int j=0;j<distsPerMap[k][i].length;++j) { 
				for(int p=0;p<distsPerMap[k][i][j].length;++p) {			ttlDistOnMap[p]+=distsPerMap[k][i][j][p];++count;	}
			}
		}
		for(int i=0;i<ttlDistOnMap.length-1;++i) {ttlDistOnMap[i]/=count;	}
		mapMgr.win.getMsgObj().dispInfoMessage("morphStackDistAnalyzer", "calculateMorphDistortion", tmp+ " Average per cell distortion on entire map @ layer "+k+": across rows : "+ ttlDistOnMap[0] + " | across cols : "+ ttlDistOnMap[1] +" |  across slices : "+ ttlDistOnMap[2]);
	}
	
	/**
	 * for all polys on same map/slice, find array of i,j distances between adjacent polys
	 * @param mapPolys
	 * @param _k slice in morph slices
	 * @param _mapTypeIDX
	 * @param currDistMsrMorphs
	 * @return
	 */
	protected final float[][][] calcDistOnEntireMap(baseMap[][] mapPolys,int _sliceIDX, int _mapTypeIDX, baseMorph currDistMsrMorph){
		float[][][] res = new float[mapPolys.length][mapPolys[0].length][3];
		int lastValIDX = mapPolys.length-1;
		
		//for all adjacent skip 1 polys, treat each poly as a new map of type maptype, build morph between both
		int[][] idxs = new int[][] {{-1,-2, -3},{-4,-5,-6},{-7,-8,-9}};
		
		//top and bottom row - horizontal dist i distortion
		for(int i=1;i<lastValIDX;++i) {//top and bottom row
			int id = 0;
			for(int k=-1;k<2;++k) {	idxs[id][0] = i+k;idxs[id][1]=0;idxs[id][2]=_sliceIDX;	++id;} id=0;
			res[i][0][0] = calcDistortion(idxs, mapPolys[i-1][0],mapPolys[i+1][0], mapPolys[i][0], _mapTypeIDX, currDistMsrMorph);
			for(int k=-1;k<2;++k) {	idxs[id][0] = i+k;idxs[id][1]=lastValIDX;idxs[id][2]=_sliceIDX;	++id;} id=0;
			res[i][lastValIDX][0] = calcDistortion(idxs, mapPolys[i-1][lastValIDX],mapPolys[i+1][lastValIDX], mapPolys[i][lastValIDX], _mapTypeIDX, currDistMsrMorph);	
		}
		//left and right side - vertical dist j distortion
		for(int j=1;j<lastValIDX;++j) {
			int id = 0;
			for(int k=-1;k<2;++k) {	idxs[id][0] = 0;idxs[id][1]=j+k;idxs[id][2]=_sliceIDX;	++id;} id=0;
			res[0][j][1] = calcDistortion(idxs, mapPolys[0][j-1],mapPolys[0][j+1], mapPolys[0][j], _mapTypeIDX, currDistMsrMorph);
			for(int k=-1;k<2;++k) {	idxs[id][0] = lastValIDX;idxs[id][1]=j+k;idxs[id][2]=_sliceIDX;	++id;} id=0;
			res[lastValIDX][j][1] = calcDistortion(idxs, mapPolys[lastValIDX][j-1],mapPolys[lastValIDX][j+1], mapPolys[lastValIDX][j], _mapTypeIDX, currDistMsrMorph);
		}
		//interior
		for(int i=1; i<lastValIDX;++i) {
			for(int j=1;j<lastValIDX;++j) {
				int id = 0; 
				for(int k=-1;k<2;++k) {	idxs[id][0] = i+k;idxs[id][1]=j;idxs[id][2]=_sliceIDX;	++id;} id=0;
				res[i][j][0] = calcDistortion(idxs, mapPolys[i-1][j],mapPolys[i+1][j], mapPolys[i][j], _mapTypeIDX, currDistMsrMorph);
				for(int k=-1;k<2;++k) {	idxs[id][1] = j+k;idxs[id][0]=i;idxs[id][2]=_sliceIDX;	++id;} id=0;
				res[i][j][1] = calcDistortion(idxs, mapPolys[i][j-1],mapPolys[i][j+1], mapPolys[i][j], _mapTypeIDX, currDistMsrMorph);
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
	protected final float calcDistortion(int[][] idxs, baseMap stPoly, baseMap endPoly, baseMap middlePoly, int _mapTypeIDX, baseMorph currDistMsrMorph) {
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
	public void analyzeTrajectory(ArrayList pts, String name) {}
	
	/**
	 * 
	 */
	@Override
	public final void drawAnalyzerData(my_procApplet pa, String[] mmntDispLabels, float[] trajWinDims, String _notUsed) {
		pa.pushMatrix();pa.pushStyle();	
		

		
		pa.popStyle();pa.popMatrix();
		
	}//_drawAnalyzerData
	@Override
	public final void drawAnalyzerGraphs(my_procApplet pa, String[] mmntDispLabels, float[] trajWinDims, String _notUsed) {
		pa.pushMatrix();pa.pushStyle();	
	

		pa.popStyle();pa.popMatrix();
	}//_drawAnalyzerData
	
	

	@Override
	protected void drawSingleSummary(my_procApplet pa, String[] mmntDispLabels, baseProbSummary smryRaw,float txtLineYDisp, float ltrMult) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void drawSingleSmryGraph(my_procApplet pa, String[] mmntDispLabels, baseProbSummary smryRaw,float txtLineYDisp, float ltrMult) {
		// TODO Auto-generated method stub

	}

}
