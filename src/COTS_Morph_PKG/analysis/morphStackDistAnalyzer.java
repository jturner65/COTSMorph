package COTS_Morph_PKG.analysis;

import java.util.ArrayList;

import COTS_Morph_PKG.analysis.base.baseAnalyzer;
import COTS_Morph_PKG.analysis.prob.base.baseProbSummary;
import COTS_Morph_PKG.map.base.baseMap;
import COTS_Morph_PKG.mapManager.mapPairManager;
import COTS_Morph_PKG.morph.base.baseMorph;
import base_UI_Objects.my_procApplet;

public class morphStackDistAnalyzer extends baseAnalyzer {
	/**
	 * owning map manager
	 */
	public final mapPairManager mapMgr;
	public final int mapType;
	
	/**
	 * these are all the control points for the current morphstack
	 */
	private baseMap[][][] allPolyMaps;
	/**
	 * scalar value of distortion at each cell k,i,j, with final index being direction of distortion measure
	 */
	public float[][][][] ttlDistPerCell;

	/**
	 * avg distortion in each cell
	 */
	private float[][][] avgDistPerCell;
	/**
	 * average distortion across entire morphstack
	 */
	private float ttlDistForEntireMrphStck;
	/**
	 * morph used to measure distortion
	 */
	private baseMorph currDistMsrMorph;
	
	//private TreeMap<String, myVectorfTrajAnalyzer> distVecAnalyzers;

	public morphStackDistAnalyzer(mapPairManager _mapMgr) {
		super();
		mapMgr=_mapMgr;mapType = mapMgr.mapType;
	}
	
	public float[][][][] getTtlDistPerCell(){return ttlDistPerCell;}
	public float getTtlDistForEntireMrphStck() {return ttlDistForEntireMrphStck;}
	
	/**
	 * calculate the distortion in the current morph
	 * @param _currDistTransformIDX index in morph list for transformatio to use to calculate distortion
	 */

	public final void calculateAllDistortions( baseMorph _currDistMsrMorph, baseMap[][][] _allPolyMaps) {	
		currDistMsrMorph = _currDistMsrMorph;
		currDistMsrMorph.setMorphSlices(3);
		allPolyMaps = _allPolyMaps;
		int numMapSlices = allPolyMaps.length,
			numCols = allPolyMaps[0].length,
			numRows = allPolyMaps[0][0].length;//,
			//numPts = allPolyCntlPts[0][0][0].length;
		//measure per-map distortions @ each i,j
		//allPolyCntrLocs = new myPointf[numMapSlices][numCols][numRows];
		//for(int k=0;k<numMapSlices;++k) {for(int i=0;i<numCols;++i) {for(int j=0;j<numRows;++j) {allPolyCntrLocs[k][i][j] = myPointf._average(allPolyMaps[k][i][j]);}}}
		ttlDistPerCell = new float[numMapSlices][numCols][numRows][3];
			//calcluate for map
		for(int k=0;k<numMapSlices;++k) {			
			ttlDistPerCell[k] = calcDistOnEntireMap(allPolyMaps[k], k, mapType, currDistMsrMorph);
			displayAvgPerMapDistortion(ttlDistPerCell, k);
		}
	
		baseMap stPoly, endPoly, midPoly;
		int[][] idxs = new int[][] {{-1,-2, -3},{-4,-5,-6},{-7,-8,-9}};
		//calc "lateral" distortion
		for(int k=1;k<numMapSlices-1;++k) {			
			float ttlDistsForSlice = 0.0f;
			int count = 0;
			for(int i=0;i<numCols;++i) {
				for(int j=0;j<numRows;++j) {
					stPoly = allPolyMaps[k-1][i][j];
					endPoly = allPolyMaps[k+1][i][j];
					midPoly = allPolyMaps[k][i][j];
					int id=0;
					for(int l=-1;l<2;++l) {	idxs[id][0] = i;idxs[id][1]=j; idxs[id][2]=k+l;	++id;}
					ttlDistPerCell[k][i][j][2] = calcDistortion(idxs, stPoly,endPoly, midPoly, mapType, currDistMsrMorph);
					ttlDistsForSlice +=ttlDistPerCell[k][i][j][2];++count;
				}
			}
			ttlDistsForSlice/=count;
			mapMgr.win.getMsgObj().dispInfoMessage("morphStackDistAnalyzer", "calculateMorphDistortion", "\tAverage Total distortion over adjacent slices on map @ layer "+k+" : "+ ttlDistsForSlice);
		}
		//aggregate all distortions
		avgDistPerCell = new float[numMapSlices][numCols][numRows]; 
		ttlDistForEntireMrphStck = 0.0f;
		int count = 0;
		for(int k=0;k<numMapSlices;++k) {
			for(int i=0;i<numCols;++i) {
				for(int j=0;j<numRows;++j) {
					avgDistPerCell[k][i][j] = (ttlDistPerCell[k][i][j][0] + ttlDistPerCell[k][i][j][1] +ttlDistPerCell[k][i][j][2])/3.0f;
					//mapMgr.win.getMsgObj().dispInfoMessage("morphStackDistAnalyzer", "calculateMorphDistortion", "\tDist per cell "+i +","+j+" on map "+k+" : "+ distPerCell[k][i][j] + " vector dist magn : " + ttlDistVecs[k][i][j].magn);
					ttlDistForEntireMrphStck +=avgDistPerCell[k][i][j];
					++count;
				}
			}
		}
		ttlDistForEntireMrphStck/=count;
		
	}//calculateMorphDistortion
	
	private void displayAvgPerMapDistortion(float[][][][] distsPerMap, int k) {
		String tmp = "\t map ["+k+"] : ";
		float ttlDistOnMap = 0.0f;
		int count = 0;
		for(int i=0;i<distsPerMap[k].length;++i) {for(int j=0;j<distsPerMap[k][i].length;++j) { for(int p=0;p<distsPerMap[k][i][j].length;++p) {		ttlDistOnMap+=distsPerMap[k][i][j][p];++count;	}}}
		ttlDistOnMap/=count;
		mapMgr.win.getMsgObj().dispInfoMessage("morphStackDistAnalyzer", "calculateMorphDistortion", tmp+ " Average per cell distortion on entire map @ layer "+k+" : "+ ttlDistOnMap);
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
		
		currDistMsrMorph.setNewKeyFrameMaps(stPoly, endPoly);
		currDistMsrMorph.setMorphT(.5f);
		baseMap currMorphMap = currDistMsrMorph.getCurMorphMap();
		
		float res = findSqDistBetween2MapVerts(middlePoly, currMorphMap);
		//float res = findDistBetween2MapVerts(middlePoly, currMorphMap);
		return res;
	}
	
	
	@Override
	public void analyzeTrajectory(ArrayList pts, String name) {}
	
	
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
