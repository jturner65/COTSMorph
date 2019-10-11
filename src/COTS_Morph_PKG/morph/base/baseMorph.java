package COTS_Morph_PKG.morph.base;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;

import COTS_Morph_PKG.analysis.floatTrajAnalyzer;
import COTS_Morph_PKG.analysis.myPointfTrajAnalyzer;
import COTS_Morph_PKG.analysis.registration.mapRegDist;
import COTS_Morph_PKG.map.base.baseMap;
import COTS_Morph_PKG.mapManager.mapPairManager;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapCntlFlags;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import COTS_Morph_PKG.utils.runners.morphStackDistortionCalc_Runner;
import base_UI_Objects.IRenderInterface;
import base_UI_Objects.my_procApplet;
import base_UI_Objects.windowUI.myDispWindow;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

/**
 * class holding common functionality to morph between two image maps
 * @author john
 *
 */
public abstract class baseMorph {
	/**
	 * current map manager, managing key frames of a specific type that this morph is working on
	 */
	protected final mapPairManager mapMgr;
	/**
	 * type index of this morph
	 */
	public final int morphTypeIDX;
	/**
	 * maps this morph is working on
	 */
	protected baseMap mapA, mapB;
	/**
	 * current morph map - will be same type as passed maps
	 */
	protected baseMap curMorphMap;
	
	/**
	 * current time in morph
	 */
	protected float morphT;
	
	/**
	 * solid representation of morphs
	 */
	protected TreeMap<Float, baseMap> morphSliceAra;
	protected int numMorphSlices = 8;
	
	public my_procApplet pa;
	public COTS_MorphWin win;
	public final String morphTitle;
	/**
	 * vector to add to displacement to manage morphing between frames in 3D
	 */
	protected myVectorf normDispTimeVec;

	protected mapCntlFlags[] mapFlags;
	protected static final int 
		mapUpdateNoResetIDX 	= 0;
	protected static final int numMapFlags = 1;
	
	/**
	 * set of all control points for each time slice, including COV and center/F, if present
	 */
	protected TreeMap<Float, myPointf[][]> cntlPtTrajs;
	/**
	 * list of areas, sorted by time in morph traj
	 */
	protected ArrayList<Float> areaTrajs;
	
	/**
	 * set of all edge points for each time slice
	 */
	protected TreeMap<Float, myPointf[][][]> edgePtTrajs;
	/**
	 * class to perform registrations and reg-related calculations, if necessary for a particular morph
	 */
	protected mapRegDist mapRegDistCalc;
	/**
	 * analyzers for each morph trajectory - keyed by traj name
	 */
	protected TreeMap<String, myPointfTrajAnalyzer> trajAnalyzers;
	protected floatTrajAnalyzer areaTrajAnalyzer;
	/**
	 * map holding name of traj component and index in cntl point ara
	 */
	protected TreeMap<String, Integer> trajAnalyzeKeys;
	
	protected boolean reCalcTrajsAndAnalysis;
	
		//ref to UI object from map manager
	protected mapUpdFromUIData currUIVals;
	/**
	 * thread runner for distortion calculation
	 */
	protected morphStackDistortionCalc_Runner  distCalcRunner;
	/**
	 * threading constructions - allow map manager to own its own threading executor
	 */
	protected ExecutorService th_exec;	//to access multithreading - instance from calling program
	protected final int numUsableThreads;		//# of threads usable by the application

	//currently calculated distortion cell colors
	protected float[][][][] distCellColors;
	
	protected float[] 
			minCellDistVals = new float[3],			//minimum seen distortion in any cell 
			maxCellDistVals = new float[3];			//max seen distortion in any cell

	/**
	 * 
	 * @param _win
	 * @param _mapMgr
	 * @param _morphTitle
	 */
	public baseMorph(COTS_MorphWin _win, mapPairManager _mapMgr, baseMap _mapA, baseMap _mapB,int _morphTypeIDX, String _morphTitle) {
		win=_win; pa=myDispWindow.pa;morphTitle=_morphTitle;mapMgr=_mapMgr;morphTypeIDX=_morphTypeIDX;
		mapA = _mapA;
		mapB = _mapB;	
		th_exec = mapMgr.getTh_Exec();
		numUsableThreads = mapMgr.getNumUsableThreads();
		//(mapPairManager _mapMgr, ExecutorService _th_exec, boolean _canMT, int _numThds, int _numWorkUnits)
		distCalcRunner = new morphStackDistortionCalc_Runner(mapMgr, th_exec, true, numUsableThreads, 1);
		_ctorFinalize(true);
	}
	
	public baseMorph(baseMorph _otr) {//copy ctor
		win=_otr.win; pa=myDispWindow.pa;morphTitle=_otr.morphTitle+"_cpy";mapMgr=_otr.mapMgr;morphTypeIDX=_otr.morphTypeIDX;
		mapA = getCopyOfMap(_otr.mapA, "cpyOfMapA");
		mapB = getCopyOfMap(_otr.mapB, "cpyOfMapB");
		th_exec = mapMgr.getTh_Exec();
		numUsableThreads = mapMgr.getNumUsableThreads();
		//(mapPairManager _mapMgr, ExecutorService _th_exec, boolean _canMT, int _numThds, int _numWorkUnits)
		distCalcRunner = new morphStackDistortionCalc_Runner( mapMgr, th_exec, true, numUsableThreads, 1);
		_ctorFinalize(true);		
	}
	
	/**
	 * currently called only from distortion calculation
	 * @param _mapA
	 * @param _mapB
	 */
	public void setNewKeyFrameMaps(baseMap _mapA, baseMap _mapB, boolean setAllInitVals) {
		mapA = _mapA;
		mapB = _mapB;	
		_ctorFinalize(setAllInitVals);
	}
	/**
	 * finalize construction of morph
	 * @param isFullFunctionMorph whether this is a full morph, or only one to be used for comparison calculations
	 */
	private void _ctorFinalize(boolean isFullFunctionMorph) {
		morphT=.5f;
		curMorphMap = getCopyOfMap(mapA,mapA.mapTitle + "_currMorphMap_"+morphTitle +" @ t="+String.format("%2.3f", morphT)); 
		curMorphMap.setImageToMap(mapA.getImageToMap());		
		currUIVals = new mapUpdFromUIData(mapMgr.currUIVals);
		normDispTimeVec = new myVectorf(mapA.getCOV(), mapB.getCOV());
		normDispTimeVec = myVectorf._mult(mapA.basisVecs[0], normDispTimeVec._dot(mapA.basisVecs[0]));	
		//areaTrajMaps = new TreeMap<Float, Float>();
		mapFlags = new mapCntlFlags[numMapFlags];
		for(int i=0;i<mapFlags.length;++i) {
			mapFlags[i] = new mapCntlFlags();
			mapFlags[i].setOptimizeAlpha(true); 
			mapFlags[i].setCopyBranching(true);
		}

		//initialize essential data before calcMorph is called
		mapRegDistCalc = new mapRegDist(this.mapMgr, mapA, mapB);
		_endCtorInit();
		mapCalcsAfterCntlPointsSet(morphTitle + "::ctor",false, false);	
		if(isFullFunctionMorph) {
			areaTrajs = new ArrayList<Float>();
			cntlPtTrajs = new TreeMap<Float, myPointf[][]>();
			edgePtTrajs = new TreeMap<Float, myPointf[][][]>();
			trajAnalyzers = new TreeMap<String, myPointfTrajAnalyzer>();
			trajAnalyzeKeys = mapA.getTrajAnalysisKeys();
			reCalcTrajsAndAnalysis = true;
			initTrajAnalyzers();
			setMorphSliceAra();
			calcMorph();		
		}
	}
	
	
	/**
	 * this will perform initialization of morph-specific data before initial morph calc is performed, from base class ctor
	 */
	protected abstract void _endCtorInit();
		
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
	 * this should be called whenever map A's values have changed (??)
	 */
	private float oldMorphT = 0.0f;
	public final void updateMorphMapWithMapVals() {	updateMorphMapWithMapVals(true);}
	public final void updateMorphMapWithMapVals(boolean calcMorph) {
		//mapMgr.msgObj.dispInfoMessage("baseMorph::"+morphTitle, "updateMorphMapWithMapVals", "Before updateMeWithMapVals");// :  curMorphMap :  "+ curMorphMap.toString());
		
//		curMorphMap.updateMeWithMapVals(mapA,mapFlags[mapUpdateNoResetIDX]);
//		if(calcMorph) {
//			calcMorph();
//		}
		//mapMgr.msgObj.dispInfoMessage("baseMorph::"+morphTitle, "updateMorphMapWithMapVals", "After updateMeWithMapVals :  curMorphMap :  "+ curMorphMap.toString());
	}
	
	/**
	 * this is only to calcuate the morph for comparison purposes - use t == .5f for most comparisons
	 * Note this will not build control point trajectories, slices, or other components used in the full morph
	 */
	public final void calcMorphCompare(float t) {_calcMorphOnMap(curMorphMap, false, t);}
	
	
	/**
	 * issue  : branching is flaking out with curMorphMap.  Problem is that old alpha values are not properly tracked.  
	 * 
	 * Forcing curMorphMap to have mapA's branching at t==0 is a temporary work around
	 */
	
	
	/**
	 * use currently set t value to calculate morph
	 */
	protected final void calcMorph() {
		if(morphT == 0.0f) {
			curMorphMap = getCopyOfMap(mapA,mapA.mapTitle + "_currMorphMap_"+morphTitle +" @ t="+String.format("%2.3f", morphT)); 
//		} else if(morphT == 1.0f) {
//			curMorphMap = getCopyOfMap(mapB,mapB.mapTitle + "_currMorphMap_"+morphTitle +" @ t="+String.format("%2.3f", morphT)); 
		} else if(morphT != oldMorphT){

			curMorphMap = getCopyOfMap(curMorphMap,mapA.mapTitle + "_currMorphMap_"+morphTitle +" @ t="+String.format("%2.3f", morphT)); 
		}
		//if(morphT == .5f) {	System.out.println(this.morphTitle+" : calc morph @ t== .5f");}
		//update morph map with map a's vals, 
		//curMorphMap.updateMeWithMapVals(mapA,mapFlags[mapUpdateNoResetIDX]);
		//if(morphT != oldMorphT) {
		//curMorphMap = getCopyOfMap(curMorphMap,mapA.mapTitle + "_currMorphMap_"+morphTitle +" @ t="+String.format("%2.3f", morphT)); 
		//}
		//if(morphT == 0.0f) {updateMorphMapWithMapVals();}
		//manage slices
		setMorphSliceAra();	
		
		//mapFlags[mapUpdateNoResetIDX].setOptimizeAlpha(morphT != oldMorphT);
		
		_calcMorphOnMap(curMorphMap, true, morphT);
		morphMapDistColorsSet = false;
		setMorphMapAndSliceColors();
		//oldMorphT = morphT;
		//mapFlags[mapUpdateNoResetIDX].setOptimizeAlpha(true);
		
		buildCntlPointTrajs();
	}	
	
//	/**
//	 * use currently set t value to calculate morph
//	 */
//	protected final void calcMorph() {
//		//update morph map with map a's vals, 
//		//curMorphMap.updateMeWithMapVals(mapA,mapFlags[mapUpdateNoResetIDX]);
////		if (morphT != oldMorphT) {
////			baseMap tmpMap = getCopyOfMap(mapA, mapA.mapTitle);	
////			curMorphMap = getCopyOfMap(tmpMap,mapA.mapTitle + "_currMorphMap_"+morphTitle +" @ t="+String.format("%2.3f", morphT)); 
////		}
//	
//		if(morphT == 0.0f) {updateMorphMapWithMapVals(false);}
//		//manage slices
//		setMorphSliceAra();	
//		
//		mapFlags[mapUpdateNoResetIDX].setOptimizeAlpha(morphT != oldMorphT);
//		
//		_calcMorphOnMap(curMorphMap, true, morphT);
//		
//		oldMorphT = morphT;
//		mapFlags[mapUpdateNoResetIDX].setOptimizeAlpha(true);
//		
//		buildCntlPointTrajs();
//		
//	}	
	
	protected final void _calcMorphOnMap(baseMap _curMorphMap, boolean _calcColors, float t) {
		float tA = 1.0f-t, tB = t;
		//initial code for morph, if necessary - assume control points have changed
		//Shouldn't be necessary here because any changes should have caused this to be processed already via mapCalcsAfterCntlPointsSet_Indiv
		//initCalcMorph_Indiv(tA, tB);
		//morph colors
		if(_calcColors) {_morphColors(_curMorphMap, tA, tB);}
		
		calcMorphAndApplyToMap(_curMorphMap, tA, tB);	
	}
	
	//public abstract void initCalcMorph_Indiv(float tA, float tB);
	
	public abstract int calcMorph_Integer(float tA, int AVal, float tB, int BVal);	
	public abstract float calcMorph_Float(float tA, float AVal, float tB, float BVal);
	public abstract double calcMorph_Double(float tA, double AVal, float tB, double BVal);
	
	public TreeMap<Float, baseMap> buildLineupOfFrames(int _numFrames) {
		return buildArrayOfMorphMaps(_numFrames, "_Lineup_Frames");
	}
	
	/**
	 * return relevant control/info points for trajectory based on current time, from passed morph map
	 * @param _curMorphMap = current map to morph and get contrl point locs from
	 * @param tA
	 * @param tB
	 * @return
	 */
	protected final myPointf[] getMorphMapTrajPts(baseMap _curMorphMap, float tA, float tB) {
		//update map with current morph calc
		calcMorphAndApplyToMap(_curMorphMap, tA, tB);
		//get relevant points from map, based on what kind of map, to build trajectories from
		myPointf[] newPts = _curMorphMap.getAllMorphCntlPts();		
		return newPts;
	}	
	
	/**
	 * use currently set t value to calculate morph and apply to passed morph map
	 */
	public abstract void calcMorphAndApplyToMap(baseMap _curMorphMap, float tA, float tB);
	
	/**
	 * calcluate this morph algorithm between Apts and Bpts, putting result in destPts
	 * @param Apts
	 * @param Bpts
	 * @param destPts
	 * @param tA
	 * @param tB
	 */
	public abstract void calcMorphBetweenTwoSetsOfCntlPoints(myPointf[] Apts, myPointf[] Bpts, myPointf[] destPts, float tA, float tB);

	/**
	 * this function will conduct calculations between the two keyframe maps, if such calcs are used, whenever either is modified.  this is morph dependent
	 */
	//public final void mapCalcsAfterCntlPointsSet(String _calledFrom) {
	public final void mapCalcsAfterCntlPointsSet(String _calledFrom, boolean reBuildNow, boolean updCurMorphMap) {
		reCalcTrajsAndAnalysis = true;
		mapCalcsAfterCntlPointsSet_Indiv(_calledFrom);
		if(reBuildNow) {buildCntlPointTrajs();}
		if(updCurMorphMap) {//call this if mapA's values have changed, and currMorphMap needs to get reassigned possible branching info
			updateMorphMapWithMapVals();
		}
		//mapCalcsAfterCntlPointsSet_Indiv(_calledFrom);
	};
	public abstract void mapCalcsAfterCntlPointsSet_Indiv(String _calledFrom);
	
	/**
	 * calculate morph stack distortion
	 */
	public final void calculateMorphDistortion(baseMorph currDistMsrMorph) {
		setMorphSliceAra();
		
		currDistMsrMorph.setMorphSlices(3);
		
		morphSliceAraDistColorsSet = false;
		morphMapDistColorsSet = false;
		canResetMapFlags = false;
		//launch thread to calc distortion in background
		distCalcRunner.setAllInitMapVals(currDistMsrMorph, morphSliceAra.values().toArray(new baseMap[0]));
		th_exec.submit(distCalcRunner);

		mapMgr.msgObj.dispInfoMessage("baseMorph", "calculateMorphDistortion",  "Launched distortion calc");
	}//calculateMorphDistortion
	
	/**
	 * called after morph thread is completed
	 * @return
	 */

	public final float updateMapValsFromDistCalc() {
		distCellColors = distCalcRunner.getTtlDistPerCell();
		mapA.setDistCellColors(distCellColors[0]);
		mapB.setDistCellColors(distCellColors[distCellColors.length-1]);
		minCellDistVals = distCalcRunner.getMinDistPerCell();
		maxCellDistVals = distCalcRunner.getMaxDistPerCell();
		canResetMapFlags = true;
		setMorphMapAndSliceColors();		
		return distCalcRunner.getTtlDistForEntireMrphStck();
	}
	
	private boolean morphMapDistColorsSet = false, morphSliceAraDistColorsSet = false, canResetMapFlags = false;
	protected void setMorphMapAndSliceColors() {
		if((distCellColors != null) && (morphSliceAra.size()==distCellColors.length)) {
			if(!morphSliceAraDistColorsSet) {	
				int kIdx = 0;
				for(float key : morphSliceAra.keySet()) {morphSliceAra.get(key).setDistCellColors(distCellColors[kIdx++]);}			
				if(canResetMapFlags) {morphSliceAraDistColorsSet = true;}
			}
			if(!morphMapDistColorsSet) {			
				int kIDX = (int)((distCellColors.length-1) * morphT);
				//if(kIDX >= distCellColors.length-1) {
					curMorphMap.setDistCellColors(distCellColors[kIDX]);
//				} else {
//					curMorphMap.setDistCellColors(distCalcRunner.calcMorphMapDist(curMorphMap, kIDX));
//				}
				if(canResetMapFlags) {morphMapDistColorsSet=true;}
			}
		}
	}
	
	/**
	 * call only once
	 */
	protected final void initTrajAnalyzers() {
		trajAnalyzers.clear();		//1 per control point
		for(String key : trajAnalyzeKeys.keySet()) {
			trajAnalyzers.put(key, new myPointfTrajAnalyzer());
		}
		//analyzer for areas
		areaTrajAnalyzer = new floatTrajAnalyzer();
	}
	
	/**
	 * build analysis component for per-control point trajectory analysis
	 */
	public final void analyzeMorph() {
		//analyze cntlPtTrajs

		TreeMap<String, ArrayList<myPointf>> morphTrajCntlPtArrays = buildMapOfMorphTrajCntlPtsToAnalyze(cntlPtTrajs);
		
		//for(int i=0;i<trajCntlPtArrays.length;++i) {
		for(String keyType : morphTrajCntlPtArrays.keySet()) {
			//System.out.println("analyzeMorphTrajs  map name : " + mapA.mapTitle+ " name : " + keyType+ " for : " + this.morphTitle + " # Pts : " +trajCntlPtArrays.get(keyType).size() +" morphTrajAnalyzer::analyzeTrajectory : ");
			trajAnalyzers.get(keyType).analyzeTrajectory(morphTrajCntlPtArrays.get(keyType),keyType);
		}			
		//ArrayList<Float> areaTrajs = buildMapOfMorphAreasToAnalyze();
		areaTrajAnalyzer.analyzeTrajectory(areaTrajs,"Areas");
	}//analyzeMorphTrajs()
	
	/**
	 * take map of cntl point traj 2d arrays (beginning and ending (first idx, 0 or 1) points for 
	 * segments of trajectory for each control point (2nd idx)), keyed by time, and build map of type-keyed cntl point arrays, with points in order
	 * @param cntlPtTrajs 
	 * @return
	 */
	protected TreeMap<String, ArrayList<myPointf>> buildMapOfMorphTrajCntlPtsToAnalyze(TreeMap<Float, myPointf[][]> _cntlPtTrajs){
		TreeMap<String, ArrayList<myPointf>> res = new TreeMap<String, ArrayList<myPointf>>();//first idx is # of cntl point types, 2nd is # of points in traj
		if(_cntlPtTrajs.size() == 0) { return res;}
		for(String key : trajAnalyzers.keySet()) {		res.put(key, new ArrayList<myPointf>());	}
		
		myPointf[][] tmpCnltPtAra;
		//int idx = 0;
		for(Float t : _cntlPtTrajs.keySet()) {
			tmpCnltPtAra = _cntlPtTrajs.get(t);	//this is all cntl points at this time	
			for(String key : trajAnalyzeKeys.keySet()) {
				res.get(key).add(new myPointf(tmpCnltPtAra[0][trajAnalyzeKeys.get(key)]));	//add copy of control point from trajectory
			}
		}		
		return res;
	}
	
	public final int getNumAnalysisBoxes() {
		int numCntlPts = mapA.getNumAllMorphCntlPts();
		//add 1 for area
		numCntlPts++;
		
		return numCntlPts;//all control points + COV and F point, if exists. if no F point then duplicates COV. TODO make this not occur if map doesn't have F point
	}
	/**
	 * build cntl point trajs
	 */
	protected final void buildCntlPointTrajs() {
		if(reCalcTrajsAndAnalysis) {
			cntlPtTrajs.clear();
			edgePtTrajs.clear();
			//areaTrajMaps.clear();
			areaTrajs.clear();
			baseMap tmpMap = getCopyOfMap(null,mapA.mapTitle +"_MorphCntlPtTraj");
			_calcMorphOnMap(tmpMap, false, 0.0f);
			myPointf[] cntlPtsOld = tmpMap.getAllMorphCntlPts();
			//myPointf[] cntlPtsOld = getMorphMapTrajPts(tmpMap,1.0f, 0.0f);//includes cov and possibly center/f
			//areaTrajMaps.put(0.0f, tmpMap.calcTtlSurfaceArea());
			areaTrajs.add(tmpMap.calcTtlSurfaceArea());
			myPointf[][] tmpCntlPtAra, edgePtsOld = mapA.getEdgePts();
			myPointf[][][] tmpEdgePtAra;
			for(float t = 0.01f;t<=1.0f;t+=.01f) {
				//float tA = 1.0f-t, tB = t;
				//initCalcMorph_Indiv(tA, tB);
				_calcMorphOnMap(tmpMap, false, t);
				myPointf[] cntlPts = tmpMap.getAllMorphCntlPts();
				//myPointf[] cntlPts = getMorphMapTrajPts(tmpMap,tA, tB);
				
				areaTrajs.add(tmpMap.calcTtlSurfaceArea());
				//idx 5 is cov, idx 6 is ctr pt
				if(null != cntlPts) {	
					tmpCntlPtAra = new myPointf[2][];
					tmpCntlPtAra[0]=new myPointf[cntlPts.length];
					tmpCntlPtAra[1]=new myPointf[cntlPts.length];
					for(int i =0;i<cntlPts.length;++i) {
						tmpCntlPtAra[0][i] = new myPointf(cntlPtsOld[i]);
						tmpCntlPtAra[1][i] = new myPointf(cntlPts[i]);						
					}
					cntlPtTrajs.put(t, tmpCntlPtAra);
				}
				//only for polys with edge points
				myPointf[][] edgePts = tmpMap.getEdgePts();
				tmpEdgePtAra = new myPointf[2][][];
				tmpEdgePtAra[0]=new myPointf[edgePts.length][];
				tmpEdgePtAra[1]=new myPointf[edgePts.length][];
				//build 2 d array of edge point (corners of polys along edges of map)
				for(int i=0;i<edgePts.length;++i) {
					tmpEdgePtAra[0][i]=new myPointf[edgePts[i].length];
					tmpEdgePtAra[1][i]=new myPointf[edgePts[i].length];
					for(int j=0;j<edgePts[i].length;++j) {
						tmpEdgePtAra[0][i][j]=new myPointf(edgePtsOld[i][j]);
						tmpEdgePtAra[1][i][j]=new myPointf(edgePts[i][j]);
					}
				}
				edgePtTrajs.put(t, tmpEdgePtAra);
				cntlPtsOld = cntlPts;
				edgePtsOld = edgePts;
			}		
			analyzeMorph();
			reCalcTrajsAndAnalysis = false;
		}

	}//buildCntlPointTrajs()
	
	
	public final void updateMorphValsFromUI(mapUpdFromUIData upd) {
		//currUIVals.setAllVals(upd);
		currUIVals = upd;
		curMorphMap.updateMapVals_FromUI(upd);
		setMorphSlices(upd.getNumMorphSlices());
		updateMorphValsFromUI_Indiv(upd);
	}
	protected abstract void updateMorphValsFromUI_Indiv(mapUpdFromUIData upd);

	//////////////////////////////
	// draw routines	
	/**
	 * draw all traj analysis data
	 * @param trajWinDims array of float dims - width,height of window, y value where window starts, y value to displace every line
	 */
	public final void drawTrajAnalyzerData(String[] mmntDispLabels, int dispDetail, float[] trajWinDims) {
		//float yDisp = trajWinDims[3];
		pa.pushMatrix();pa.pushStyle();		
		for(String key : trajAnalyzers.keySet()) {//per control point	
			if(key.equals(baseMap.COV_Label) && (dispDetail < COTS_MorphWin.drawMapDet_CntlPts_COV_IDX)){continue;}
			if(key.equals(baseMap.SpiralCtrLbl) && (dispDetail < COTS_MorphWin.drawMapDet_CntlPts_COV_EdgePts_F_IDX)){continue;}
			trajAnalyzers.get(key).drawAnalyzerData(pa, mmntDispLabels,trajWinDims, "Cntl Pt Traj : " + key);
		}
		areaTrajAnalyzer.drawAnalyzerData(pa, mmntDispLabels,trajWinDims, "Area :");
		pa.popStyle();pa.popMatrix();
		
	}
	
	/**
	 * draw all traj analysis graphs
	 * @param trajWinDims array of float dims - width,height of window, y value where window starts, y value to displace every line
	 */
	public final void drawTrajAnalyzerGraphs(String[] mmntDispLabels, int dispDetail, float[] trajWinDims) {
		//float yDisp = trajWinDims[3];
		pa.pushMatrix();pa.pushStyle();		
		for(String key : trajAnalyzers.keySet()) {//per control point
			if(key.equals(baseMap.COV_Label) && (dispDetail < COTS_MorphWin.drawMapDet_CntlPts_COV_IDX)){continue;}
			if(key.equals(baseMap.SpiralCtrLbl) && (dispDetail < COTS_MorphWin.drawMapDet_CntlPts_COV_EdgePts_F_IDX)){continue;}
			trajAnalyzers.get(key).drawAnalyzerGraphs(pa, mmntDispLabels,trajWinDims, "Cntl Pt Traj : " + key);
		}
		areaTrajAnalyzer.drawAnalyzerGraphs(pa, mmntDispLabels,trajWinDims, "Area :");
		pa.popStyle();pa.popMatrix();
		
	}

	
	public final float drawMapRtSdMenuDescr(float yOff, float sideBarYDisp) {		
		pa.pushMatrix();pa.pushStyle();
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Green, 255), 6.5f, morphTitle);
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 5.6f, " Morph Frame @ Time : ");
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Yellow, 255), 6.5f, String.format(baseMap.strPointDispFrmt8,morphT));
		pa.popStyle();pa.popMatrix();
	
		yOff += sideBarYDisp;
		pa.translate(10.0f,sideBarYDisp, 0.0f);		
		yOff = curMorphMap.drawRtSdMenuDescr(yOff, sideBarYDisp, false, true);
		return yOff;
	}
	public final float drawMorphSliceRtSdMenuDescr(float yOff, float sideBarYDisp) {
		if(null!=morphSliceAra) {
			float modYAmt = sideBarYDisp*.9f;
			yOff += modYAmt;
			//pa.translate(10.0f,modYAmt, 0.0f);		
			for(float key : morphSliceAra.keySet()) {
				yOff = morphSliceAra.get(key).drawRtSdMenuDescr(yOff, modYAmt, true, false);
			}
		}
		return yOff;
	}
	
	public final float drawMorphTitle(float yOff, float sideBarYDisp) {
		pa.showOffsetText(0,IRenderInterface.gui_Cyan, morphTitle + " Morph : ");
		yOff += sideBarYDisp;
		pa.translate(0.0f, sideBarYDisp, 0.0f);
		return yOff;
	}
	/**
	 * draw the summaries of distortion values on right side menu, highlighting the dimension of the distortion currently being displayed
	 * @param vals
	 */
	private final void _drawRtSideMenuDistSummaries(float[] vals,  String label, int curIdx) {
		pa.pushMatrix();pa.pushStyle();
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 5.2f, label + " :[");
			for(int i=0;i<vals.length-1;++i) {
				pa.showOffsetText_RightSideMenu(pa.getClr((i==curIdx ? IRenderInterface.gui_Yellow : IRenderInterface.gui_Cyan), 255), 7.0f, String.format(baseMap.strPointDispFrmt85,vals[i])+", ");
			}
			pa.showOffsetText_RightSideMenu(pa.getClr((vals.length-1==curIdx ? IRenderInterface.gui_Yellow : IRenderInterface.gui_Cyan), 255), 7.0f, String.format(baseMap.strPointDispFrmt85,vals[vals.length-1]));
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 5.2f, "]");
		pa.popStyle();pa.popMatrix();	
	}
	
	
	public final float drawDistortionRtSideMenuMinMax(float yOff, float sideBarYDisp, int curIdx) {		
		_drawRtSideMenuDistSummaries(maxCellDistVals,"Max Dist",  curIdx);
		yOff += sideBarYDisp;
		pa.translate(0.0f,sideBarYDisp, 0.0f);	
		
		_drawRtSideMenuDistSummaries(minCellDistVals,"Min Dist",  curIdx);
		yOff += sideBarYDisp;
		pa.translate(0.0f,sideBarYDisp, 0.0f);				

		return yOff;
	}
	
	public final float drawMorphRtSdMenuDescr(float yOff, float sideBarYDisp, float _morphSpeed) {//, String[] _scopeList) {
		pa.pushMatrix();pa.pushStyle();
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 6.2f, "Morph Between :");
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Yellow, 255), 6.8f, mapA.mapTitle);
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 6.2f, " and");
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Yellow, 255), 6.8f, mapB.mapTitle);
		pa.popStyle();pa.popMatrix();		
		yOff += sideBarYDisp;
		pa.translate(0.0f,sideBarYDisp, 0.0f);		
		
		pa.pushMatrix();pa.pushStyle();
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 6.0f, "Currently at time :");
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Yellow, 255), 6.5f, String.format(baseMap.strPointDispFrmt8,morphT));
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 6.5f, " | Speed :");
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Yellow, 255), 6.5f, String.format(baseMap.strPointDispFrmt8,_morphSpeed));
		pa.popStyle();pa.popMatrix();		
		
		yOff += sideBarYDisp;
		pa.translate(0.0f,sideBarYDisp, 0.0f);			
		yOff = drawMorphRtSdMenuDescr_Indiv(yOff, sideBarYDisp);	
		return yOff;
	}	
	public abstract float drawMorphRtSdMenuDescr_Indiv(float yOff, float sideBarYDisp);	
	public final void drawMorphedMap_CntlPts(int _detail) {		curMorphMap.drawMap_CntlPts(false, _detail);	}
	
	protected baseMap getCopyOfMap(baseMap cpyMap, String fullCpyName) {
		if(null==cpyMap) {cpyMap = mapA;}
		baseMap resMap = mapMgr.buildCopyMapOfPassedMapType(cpyMap, fullCpyName);
		//resMap.updateMeWithMapVals(cpyMap,mapFlags[mapCopyNoResetIDX]);
		return resMap;
	}
	

	/**
	 * builds cntl point trajectories using current morph
	 */
	public final void drawMorphedMap_CntlPtTraj(int _detail) {
		pa.pushMatrix();pa.pushStyle();	
		pa.sphereDetail(5);
		pa.stroke(0,0,0,255);
	
		pa.strokeWeight(1.0f);
		for(float t = 0.01f;t<=1.0f;t+=.01f) {
			//float tA = 1.0f-t, tB = t;
			//initCalcMorph_Indiv(tA, tB);
			myPointf[][] cntlPts = cntlPtTrajs.get(t);
			myPointf[][][] edgePts = edgePtTrajs.get(t);
			//idx 5 is cov, idx 6 is ctr pt
			if(null != cntlPts) {
				for(int i = 0;i<cntlPts[0].length-2;++i) {		mapMgr._drawPt(cntlPts[1][i], 2.0f); pa.line(cntlPts[0][i], cntlPts[1][i]);}
			}
			//following not nested in case their values change
			if(_detail >= COTS_MorphWin.drawMapDet_CntlPts_COV_IDX) {
				int k=cntlPts[0].length-2;
				mapMgr._drawPt(cntlPts[1][k], 2.0f); pa.line(cntlPts[0][k], cntlPts[1][k]);				
			}
			if(_detail >= COTS_MorphWin.drawMapDet_CntlPts_COV_EdgePts_IDX) {
				for(int i=0;i<edgePts[0].length;++i) {	for(int j=0;j<edgePts[0][i].length;++j) {	pa.line(edgePts[0][i][j], edgePts[1][i][j]);}}
			}
			if(_detail >= COTS_MorphWin.drawMapDet_CntlPts_COV_EdgePts_F_IDX) {
				int k=cntlPts[0].length-1;
				mapMgr._drawPt(cntlPts[1][k], 2.0f); pa.line(cntlPts[0][k], cntlPts[1][k]);
			}
		}		
		pa.popStyle();pa.popMatrix();	
	}
	
	public final void drawCurrMorphedMap(boolean _showDistColors, boolean _isFill, boolean _drawMap, boolean _drawCircles) {
		_drawMorphMap(curMorphMap, _showDistColors,_isFill, _drawMap, _drawCircles);
	}
	
	public final void drawMorphSlices( boolean _showDistColors, boolean _isFill, boolean _drawMorphSliceMap, boolean _drawCircles, boolean _drawCntlPts, boolean _showLabels, int _detail) {
		if(_drawMorphSliceMap) {
			if(_showDistColors && morphSliceAraDistColorsSet) {	for(Float t : morphSliceAra.keySet()) {		morphSliceAra.get(t).drawMap_DistColor( currUIVals.getMorphDistMult(), currUIVals.getDistDimToShow());	}} 
			else if(_isFill) {	for(Float t : morphSliceAra.keySet()) {		morphSliceAra.get(t).drawMap_Fill();}} 
			else {			for(Float t : morphSliceAra.keySet()) {		morphSliceAra.get(t).drawMap_Wf();}}
		}
		if(_drawCircles) {
			if((!_drawMorphSliceMap) &&_isFill) {	for(Float t : morphSliceAra.keySet()) {		morphSliceAra.get(t).drawMap_PolyCircles_Fill();}} 
			else {			for(Float t : morphSliceAra.keySet()) {		morphSliceAra.get(t).drawMap_PolyCircles_Wf();}}
		
		}

		if(_drawCntlPts) {
			for(Float t : morphSliceAra.keySet()) {
				baseMap map = morphSliceAra.get(t);
				map.drawMap_CntlPts(false, _detail);
				map.drawHeaderAndLabels(_showLabels, _detail);
			}
		}	
	}
	
	protected final void _drawMorphMap(baseMap _map, boolean _showDistColors, boolean _isFill, boolean _drawMap, boolean _drawCircles) {
		if(_drawMap) {
			if(_showDistColors && morphSliceAraDistColorsSet) {_map.drawMap_DistColor( currUIVals.getMorphDistMult(), currUIVals.getDistDimToShow());	}
			else if(_isFill) {	_map.drawMap_Fill();}
			else {			_map.drawMap_Wf();}	
		}
		if(_drawCircles) {
			if((!_drawMap) && _isFill) {	_map.drawMap_PolyCircles_Fill();}	
			else {			_map.drawMap_PolyCircles_Wf();}		
		}
	}

	public final void drawHeaderAndLabels(boolean _drawLabels, int _detail) {							curMorphMap.drawHeaderAndLabels(_drawLabels,_detail);}
	
	/**
	 * this will draw instancing morph-specific data on screen 
	 */
	public abstract void drawMorphSpecificValues(boolean debug, boolean drawCntlPts, boolean showLbls);

	
	/**
	 * this will return an array of k, i,j control point arrays, where i and j are map column and row, and k is slice idx
	 * @return
	 */
	public final baseMap[][][] buildAllSliceCellMaps(){
		
		baseMap[][][] res = new baseMap[numMorphSlices][][];
		int i = 0;
		for(Float key : morphSliceAra.keySet()) {res[i++] = morphSliceAra.get(key).buildPolyMaps();}
		
		
		return res;
	}
	
	
	/////////////////////////
	// setters/getters
	
	public final void setMorphT(float _t) {			morphT=_t;				calcMorph();}	
	//public final void setMorphScope(int _mScope) {	morphScope = _mScope;	calcMorph();}
	
	public final void setMorphSlices(int _num) {
		if(_num < 3) {_num =3;}//allow no fewer than 3 slices
		int oldNumMorphSlices = numMorphSlices; 
		numMorphSlices=_num;
		if(oldNumMorphSlices != numMorphSlices) {setMorphSliceAra();}
	}

	protected final void setMorphSliceAra() {
		morphSliceAra = buildArrayOfMorphMaps(numMorphSlices, "_MrphSlc");
		morphSliceAraDistColorsSet = false;
		setMorphMapAndSliceColors();
	}//setMorphMapAra()	
	
	protected final TreeMap<Float, baseMap> buildArrayOfMorphMaps(int numMaps, String _name) {
		baseMap tmpMap = getCopyOfMap(null, mapA.mapTitle);	
		TreeMap<Float, baseMap> morphMaps = new TreeMap<Float, baseMap>();
		//want incr so that i get numMaps back
		Float tIncr = 1.0f/(numMaps-1.0f);
		Float t = 0.0f;
		for (int i=0;i<numMaps-1;++i) {		
			tmpMap = getCopyOfMap(tmpMap, mapA.mapTitle +_name + " @ t="+String.format("%2.3f", t));
			_calcMorphOnMap(tmpMap, true, t);
			morphMaps.put(t, tmpMap);		
			t+=tIncr;
		}
		t=1.0f;
		//tmpMap = mapMgr.buildCopyMapOfPassedMapType(mapA, "Morph @ t="+String.format("%2.3f", t));
		tmpMap = getCopyOfMap(tmpMap, mapA.mapTitle +_name + " @ t="+String.format("%2.3f", t));
		//
		_calcMorphOnMap(tmpMap, true,t);	
		morphMaps.put(t, tmpMap);				
		return morphMaps;
	}

	public final void resetCurMorphBranching() {
		curMorphMap.setFlags(new boolean[] {true});
	}
	
	/**
	 * resets branching if any morphs maintain branching values on their own
	 */
	public final void resetAllBranching() {
		resetCurMorphBranching();
		resetAllBranching_Indiv();
	};
	public abstract void resetAllBranching_Indiv();
	
	public baseMap getCurMorphMap() {return curMorphMap;}	
	public myPointf[] getCurMorphMap_CntlPts() {return curMorphMap.getCntlPts();}
	
	public final int getNumMorphSlices() {
		//mapMgr.msgObj.dispInfoMessage("baseMorph", "getNumMorphSlices", "NumMorphSlices var : " + numMorphSlices + " | Size of morph slices ara : " + this.morphSliceAra.size());
		return numMorphSlices;
	}
	
	public mapPairManager getMapPairManager() {return mapMgr;}

	public final String toStringEdge(myPointf[] e) {
		return "0:["+e[0].toStrCSV(baseMap.strPointDispFrmt8)+"] | 1:["+e[1].toStrCSV(baseMap.strPointDispFrmt8)+"]";
	}
	

}//class baseMorph
