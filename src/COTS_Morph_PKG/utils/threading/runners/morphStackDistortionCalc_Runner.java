package COTS_Morph_PKG.utils.threading.runners;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import COTS_Morph_PKG.map.base.baseMap;
import COTS_Morph_PKG.mapManager.mapPairManager;
import COTS_Morph_PKG.morph.base.baseMorph;
import COTS_Morph_PKG.utils.threading.callables.morphStackDistortionCalc;
import base_Utils_Objects.threading.runners.myThreadRunner;
/**
 * measure distortion in a multi-threaded execution, for speed concerns; 1 thread per map/slice
 * @author john
 *
 */
public class morphStackDistortionCalc_Runner extends myThreadRunner implements Runnable{
	public final mapPairManager mapMgr;
	/**
	 * these are all the map slices in the morph stack
	 */
	protected baseMap[] morphSliceAra;
	/**
	 * output : these are all the control points for the current morphstack
	 */
	protected baseMap[][][] allPolyMaps;
	//special frames  for start and end poly maps to be used for key frame animation distortion calculation in time direction based on animation motion
	protected baseMap[][] kfStartPolyMap, kfEndPolyMap;
	/**
	 * output : scalar value of distortion at each cell k,i,j, with final index being direction of distortion measure
	 */
	protected float[][][][] ttlDistPerCell;
	//max and min (non zero)  distortion measured across rows, columns, slices
	protected float[] maxDistPerCell,minDistPerCell;
	/**
	 * avg distortion in each cell
	 */
	protected float[][][] avgDistPerCell;

	/**
	 * output : morph used to measure distortion
	 */
	protected baseMorph currDistMsrMorph;
	protected baseMorph[] perThdDistMsrMorphs;
	/**
	 * map type being calculated
	 */
	//protected final int mapType;
	
	protected float ttlDistForEntireMrphStck;
	
	/**
	 * whether the distortion type to calculate is i/j distortion on map, or between morph slices
	 */
	protected int distCalcType;
	public static final int 
		buildPolyCntlPtAraIDX = 0,
		mapWideDistCalcIDX = 1,
		perMorphDistCalcIDX = 2;

	public morphStackDistortionCalc_Runner(mapPairManager _mapMgr, ExecutorService _th_exec, boolean _canMT, int _numThds, int _numWorkUnits) {
		super(_th_exec, _canMT, _numThds, _numWorkUnits);
		perThdDistMsrMorphs = new baseMorph[numUsableThreads];
		mapMgr=_mapMgr;
	}
	
	private morphStackDistortionCalc morphMapRnr;
	/**
	 * setup initial map values, before runMe is called
	 * @param _currDistMsrMorph
	 * @param _allPolyMaps
	 */
	public final void setAllInitMapVals(baseMorph _currDistMsrMorph, baseMap[] _morphSliceAra, baseMap[][] _kfStartPolyMap, baseMap[][] _kfEndPolyMap) {	
		currDistMsrMorph = _currDistMsrMorph;
		currDistMsrMorph.setMorphSlices(3);
		morphSliceAra  = _morphSliceAra;
		setNumWorkUnits(morphSliceAra.length);
		int numActualThds = getNumPartitions();
		perThdDistMsrMorphs = new baseMorph[numActualThds];
		for(int i=0;i<perThdDistMsrMorphs.length;++i) {
			perThdDistMsrMorphs[i] = mapMgr.buildCopyMorphOfPassedType(currDistMsrMorph);
		}
		int numMapSlices = morphSliceAra.length;
		int numCellsPerSide = morphSliceAra[0].getNumCellsPerSide();
		setNumWorkUnits(numMapSlices);
		allPolyMaps = new baseMap[numMapSlices][][];
		ttlDistPerCell = new float[numMapSlices][numCellsPerSide][numCellsPerSide][3];
		avgDistPerCell = new float[numMapSlices][numCellsPerSide][numCellsPerSide]; 
		
		//start and end key frame poly maps, for use at actual keyframes if this calc operates on them (for slice distortion)
		kfStartPolyMap = _kfStartPolyMap;
		kfEndPolyMap = _kfEndPolyMap;		
		morphMapRnr = new morphStackDistortionCalc(msgObj, 0, morphSliceAra.length, 0, distCalcType, perThdDistMsrMorphs[0],morphSliceAra,allPolyMaps,ttlDistPerCell, avgDistPerCell, kfStartPolyMap, kfEndPolyMap);
	}
	/**
	 * call before runme, to set the type of calculation to execute
	 * @param _typ
	 */
	public final void setCalcFuncType(int _typ) {distCalcType = _typ;}
	
	public final float[][][][] getTtlDistPerCell(){return ttlDistPerCell;}
	public final float[][][] getAvgDistPerCell(){return avgDistPerCell;}
	public final float getTtlDistForEntireMrphStck() {return ttlDistForEntireMrphStck;}
	public final float[] getMaxDistPerCell() {return maxDistPerCell;}
	public final float[] getMinDistPerCell() {return minDistPerCell;}
	
	public final baseMap[][][] getAllPolyMaps(){return allPolyMaps;}
	
	
	/**
	 * add per-thread callable to be launched
	 * baseMap[][][] _allPolyMaps, float[][][][] _ttlDistPerCell, float[][][] _avgDistPerCell
	 */
	@Override
	protected void execPerPartition(List<Callable<Boolean>> ExMappers, int dataSt, int dataEnd, int pIdx, int ttlParts) {
		//mapMgr.msgObj.dispInfoMessage("morphStackDistAnalyzer", "execPerPartition",  "Instancing mapper for pIdx : " + pIdx + " perThdDistMsrMorphs size : " + perThdDistMsrMorphs.length);
		ExMappers.add(new morphStackDistortionCalc(msgObj, dataSt, dataEnd, pIdx, distCalcType,perThdDistMsrMorphs[pIdx],morphSliceAra,allPolyMaps,ttlDistPerCell, avgDistPerCell, kfStartPolyMap, kfEndPolyMap));		
	}
	
	//approx # of calcs per partition/thread - for this calc this should be maps so 1 per thread
	@Override
	protected int getNumPerPartition() {		return 2;	}
	//code to execute single-threaded execution
	@Override
	protected void runMe_Indiv_ST() {
		morphStackDistortionCalc rnr = new morphStackDistortionCalc(msgObj, 0, morphSliceAra.length, 0, distCalcType, perThdDistMsrMorphs[0],morphSliceAra,allPolyMaps,ttlDistPerCell, avgDistPerCell, kfStartPolyMap, kfEndPolyMap);
		try {			rnr.call();		} 
		catch (Exception e) {			e.printStackTrace();		}
	}
	//code to execute at the end of the mt or st execution
	@Override
	protected void runMe_Indiv_End() {

	}
	
	public float[][][] calcMorphMapDist(baseMap stMap, baseMap endMap, baseMap curMap) {
		return morphMapRnr.calcAllDistOnPassedMap(stMap, endMap, curMap);
	}
	
	
	private void displayAvgPerMapDistortion(float[][][][] distsPerMap, int k) {
		String tmp = "\t map ["+k+"] : ";
		float[] ttlDistOnMap = new float[distsPerMap[0][0][0].length];
		int count = 0;
		for(int i=0;i<distsPerMap[k].length;++i) {
			for(int j=0;j<distsPerMap[k][i].length;++j) { 
				for(int p=0;p<distsPerMap[k][i][j].length;++p) {			ttlDistOnMap[p]+=distsPerMap[k][i][j][p];++count;	}
			}
		}
		for(int i=0;i<ttlDistOnMap.length;++i) {ttlDistOnMap[i]/=1.0f*count;	}
		msgObj.dispInfoMessage("morphStackDistortionCalc_Runner", "calculateMorphDistortion", tmp+ " Average per cell distortion on entire map @ layer "+k+" count : " +count + " : across rows : "+ ttlDistOnMap[0] + " | across cols : "+ ttlDistOnMap[1] +" |  across slices : "+ ttlDistOnMap[2]);
	}	

	@Override
	public void run() {
		
		//NOTE : syntax errors here will just hang with no output or dump
		//build arrays of i,j,k polys from control points
		setCalcFuncType(morphStackDistortionCalc_Runner.buildPolyCntlPtAraIDX);
		//mapMgr.msgObj.dispInfoMessage("morphStackDistAnalyzer", "calculateAllDistortions",  "Calling task buildPolyCntlPtAraIDX");
		runMe();
		//mapMgr.msgObj.dispInfoMessage("morphStackDistAnalyzer", "calculateAllDistortions",  "Finished task buildPolyCntlPtAraIDX");
		//build distortion per map in i/j dir		
		setCalcFuncType(morphStackDistortionCalc_Runner.mapWideDistCalcIDX);
		//mapMgr.msgObj.dispInfoMessage("morphStackDistAnalyzer", "calculateAllDistortions",  "Calling task mapWideDistCalcIDX");
		runMe();		
		//mapMgr.msgObj.dispInfoMessage("morphStackDistAnalyzer", "calculateAllDistortions",  "Finished task mapWideDistCalcIDX");
		//build distortion per slice in k dir
		setCalcFuncType(morphStackDistortionCalc_Runner.perMorphDistCalcIDX);
		//mapMgr.msgObj.dispInfoMessage("morphStackDistAnalyzer", "calculateAllDistortions",  "Calling task perMorphDistCalcIDX");
		runMe();
		//mapMgr.msgObj.dispInfoMessage("morphStackDistAnalyzer", "calculateAllDistortions",  "Finished task perMorphDistCalcIDX");

		msgObj.dispInfoMessage("morphStackDistAnalyzer", "calculateAllDistortions",  "allPolyMaps has : " + allPolyMaps.length + " slices with : "+ allPolyMaps[0].length +" columns and " + allPolyMaps[0][0].length + " rows ");

		maxDistPerCell = new float[] {Float.MIN_VALUE,Float.MIN_VALUE,Float.MIN_VALUE};
		minDistPerCell = new float[] {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE};
		int numMapSlices = allPolyMaps.length,
				numCols = allPolyMaps[0].length,
				numRows = allPolyMaps[0][0].length;//,		
		
		//aggregate all distortions
		ttlDistForEntireMrphStck = 0.0f;
		int count = 0;
		for(int k=0;k<numMapSlices;++k) {
			for(int i=0;i<numCols;++i) {
				for(int j=0;j<numRows;++j) {
					float[] distAtLoc = ttlDistPerCell[k][i][j];
					for(int dim=0;dim<3;++dim) {
						maxDistPerCell[dim]=( distAtLoc[dim] > maxDistPerCell[dim] ? distAtLoc[dim] : maxDistPerCell[dim]);
						//find minimum non-zero distortion
						minDistPerCell[dim]=( ((distAtLoc[dim] > 0) && distAtLoc[dim] < minDistPerCell[dim]) ? distAtLoc[dim] : minDistPerCell[dim]);
						ttlDistForEntireMrphStck +=distAtLoc[dim];++count;
					}
					//ttlDistForEntireMrphStck +=avgDistPerCell[k][i][j];++count;
				}
			}
			displayAvgPerMapDistortion(ttlDistPerCell, k);	
		}
		ttlDistForEntireMrphStck/=count;
		msgObj.dispInfoMessage("morphStackDistortionCalc_Runner", "Call","Completed calculation and assignment for distortion.");
		//call finishing routine in mapMgr
		mapMgr.finishedDistortionCalc();
		
	}

}//class morphStackDistortionCalc_Runner


