package COTS_Morph_PKG.utils.runners;

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
public class morphStackDistortionCalc_Runner extends myThreadRunner {
	public final mapPairManager mapMgr;
	/**
	 * these are all the map slices in the morph stack
	 */
	protected baseMap[] morphSliceAra;
	/**
	 * output : these are all the control points for the current morphstack
	 */
	protected baseMap[][][] allPolyMaps;
	/**
	 * output : scalar value of distortion at each cell k,i,j, with final index being direction of distortion measure
	 */
	protected float[][][][] ttlDistPerCell;
	/**
	 * output : morph used to measure distortion
	 */
	protected baseMorph currDistMsrMorph;
	/**
	 * map type being calculated
	 */
	protected final int mapType;
	
	/**
	 * whether the distortion type to calculate is i/j distortion on map, or between morph slices
	 */
	protected int distCalcType;
	public static final int 
		buildPolyCntlPtAraIDX = 0,
		mapWideDistCalcIDX = 1,
		perMorphDistCalcIDX = 2;
	
	//protected int numMapSlices,numCols,numRows;
	
	public morphStackDistortionCalc_Runner(mapPairManager _mapMgr, ExecutorService _th_exec, boolean _canMT, int _numThds, int _numWorkUnits, int _mapType) {
		super(_mapMgr.msgObj, _th_exec, _canMT, _numThds, _numWorkUnits);
		mapMgr=_mapMgr;mapType=_mapType;

	}
	
	/**
	 * setup initial map values, before runMe is called
	 * @param _currDistMsrMorph
	 * @param _allPolyMaps
	 */
	public final void setAllInitMapVals( baseMorph _currDistMsrMorph, baseMap[] _morphSliceAra) {	
		currDistMsrMorph = _currDistMsrMorph;
		currDistMsrMorph.setMorphSlices(3);
		morphSliceAra  = _morphSliceAra;
		int numMapSlices = morphSliceAra.length;
		int numCellsPerSide = morphSliceAra[0].getNumCellsPerSide();
		setNumWorkUnits(numMapSlices);
		allPolyMaps = new baseMap[numMapSlices][][];
		ttlDistPerCell = new float[numMapSlices][numCellsPerSide][numCellsPerSide][3];
	}
	/**
	 * call before runme, to set the type of calculation to execute
	 * @param _typ
	 */
	public final void setDistCalcType(int _typ) {distCalcType = _typ;}
	
	public final float[][][][] getTtlDistPerCell(){return ttlDistPerCell;}
	
	public final baseMap[][][] getAllPolyMaps(){return allPolyMaps;}
	
	/**
	 * add per-thread callable to be launched
	 * baseMap[][][] _allPolyMaps, float[][][][] _ttlDistPerCell, float[][][] _avgDistPerCell
	 */
	@Override
	protected void execPerPartition(List<Callable<Boolean>> ExMappers, int dataSt, int dataEnd, int pIdx, int ttlParts) {
		ExMappers.add(new morphStackDistortionCalc(msgObj, dataSt, dataEnd, pIdx, distCalcType, mapMgr.buildCopyMorphOfPassedType(currDistMsrMorph),mapType,morphSliceAra,allPolyMaps,ttlDistPerCell));		
	}
	
	//approx # of calcs per partition/thread - for this calc this should be maps so 1 per thread
	@Override
	protected int getNumPerPartition() {		return 1;	}
	//code to execute single-threaded execution
	@Override
	protected void runMe_Indiv_ST() {
		morphStackDistortionCalc rnr = new morphStackDistortionCalc(msgObj, 0, morphSliceAra.length, 0, distCalcType, mapMgr.buildCopyMorphOfPassedType(currDistMsrMorph),mapType,morphSliceAra,allPolyMaps,ttlDistPerCell);
		try {			rnr.call();		} 
		catch (Exception e) {			e.printStackTrace();		}
	}
	//code to execute at the end of the mt or st execution
	@Override
	protected void runMe_Indiv_End() {

	}

}//class morphStackDistortionCalc_Runner


