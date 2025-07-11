package COTS_Morph_PKG.morph.base;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;

import COTS_Morph_PKG.map.base.Base_PolyMap;
import COTS_Morph_PKG.map.registration.mapRegDist;
import COTS_Morph_PKG.mapManager.mapPairManager;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import COTS_Morph_PKG.utils.controlFlags.morphCntlFlags;
import COTS_Morph_PKG.utils.threading.runners.morphStackDistortionCalc_Runner;
import base_Math_Objects.interpolants.base.InterpolantBehavior;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;
import base_Render_Interface.IRenderInterface;
import base_StatsTools.analysis.floatTrajAnalyzer;
import base_StatsTools.analysis.myPointfTrajAnalyzer;
import base_UI_Objects.GUI_AppManager;
import base_UI_Objects.windowUI.base.Base_DispWindow;

/**
 * class holding common functionality to morph between two image maps
 * @author john
 *
 */
public abstract class baseMorph {
    
    public IRenderInterface ri;
    public COTS_MorphWin win;    
    /**
     * current map manager, managing key frames of a specific type that this morph is working on
     */
    protected final mapPairManager mapMgr;
    /**
     * application manager
     */
    public static GUI_AppManager AppMgr;
    /**
     * type index of this morph
     */
    public final int morphTypeIDX;
    /**
     * maps this morph is working on
     */
    protected Base_PolyMap mapA, mapB;
    /**
     * current morph map - will be same type as passed maps
     */
    protected Base_PolyMap curMorphMap;
    
    /**
     * current time in morph
     */
    protected float morphT;
    
    /**
     * solid representation of morphs
     */
    protected TreeMap<Float, Base_PolyMap>[] morphSliceAras;
    protected static final int 
        equalDist_MorphSlicesIDX = 0,
        equalRawT_MorphSlicesIDX = 1;
    protected static final int numMorphSliceAras = 2;
    /**
     * thread runner for distortion calculation
     */
    protected morphStackDistortionCalc_Runner  distCalcRunner;
    /**
     * morph slice ara idx to use for display
     */
    protected int curMorphSliceAraIDX = 0;
    
    protected int numMorphSlices = 8;

    public final String morphTitle;
    /**
     * vector to add to displacement to manage morphing between frames in 3D
     */
    protected myVectorf normDispTimeVec;

    protected morphCntlFlags[] mapFlags;
    protected static final int 
        mapUpdateNoResetIDX     = 0;
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
     * threading constructions - allow map manager to own its own threading executor
     */
    protected ExecutorService th_exec;    //to access multithreading - instance from calling program
    protected final int numUsableThreads;        //# of threads usable by the application

    //currently calculated distortion cell colors
    protected float[][][][] distCellColors;    
    
    
    protected float[] 
            minCellDistVals = new float[3],            //minimum seen distortion in any cell 
            maxCellDistVals = new float[3];            //max seen distortion in any cell

    /**
     * 
     * @param _win
     * @param _mapMgr
     * @param _morphTitle
     */
    public baseMorph(COTS_MorphWin _win, mapPairManager _mapMgr, Base_PolyMap _mapA, Base_PolyMap _mapB,int _morphTypeIDX, String _morphTitle) {
        win=_win; ri=Base_DispWindow.ri;AppMgr = Base_DispWindow.AppMgr;morphTitle=_morphTitle;mapMgr=_mapMgr;morphTypeIDX=_morphTypeIDX;
        initMorphSliceAras();
        mapA = _mapA;
        mapB = _mapB;    
        th_exec = mapMgr.getTh_Exec();
        numUsableThreads = mapMgr.getNumUsableThreads();
        //(mapPairManager _mapMgr, ExecutorService _th_exec, boolean _canMT, int _numThds, int _numWorkUnits)
        initDistCalcRunners();
        _ctorFinalize(true);
    }
    

    public baseMorph(baseMorph _otr) {//copy ctor
        win=_otr.win; ri=Base_DispWindow.ri;morphTitle=_otr.morphTitle+"_cpy";mapMgr=_otr.mapMgr;morphTypeIDX=_otr.morphTypeIDX;
        initMorphSliceAras();
        mapA = getCopyOfMap(_otr.mapA, "cpyOfMapA");
        mapB = getCopyOfMap(_otr.mapB, "cpyOfMapB");
        th_exec = mapMgr.getTh_Exec();
        numUsableThreads = mapMgr.getNumUsableThreads();
        //(mapPairManager _mapMgr, ExecutorService _th_exec, boolean _canMT, int _numThds, int _numWorkUnits)
        initDistCalcRunners();
        _ctorFinalize(true);        
    }
    @SuppressWarnings("unchecked")
    private void initMorphSliceAras() {
        morphSliceAras = new TreeMap[numMorphSliceAras];
        for(int i=0;i<morphSliceAras.length;++i) {    morphSliceAras[i] = new TreeMap<Float, Base_PolyMap>();}
    }
    
    private void initDistCalcRunners() {
//        distCalcRunners = new morphStackDistortionCalc_Runner[numMorphSliceAras];
//        for(int i=0;i<morphSliceAras.length;++i) {    distCalcRunners[i] = new morphStackDistortionCalc_Runner( mapMgr, th_exec, true, numUsableThreads, 1);}
        
        distCalcRunner = new morphStackDistortionCalc_Runner( mapMgr, th_exec, true, numUsableThreads, 1);
    }
    
    /**
     * currently called only from distortion calculation
     * @param _mapA
     * @param _mapB
     */
    public void setNewKeyFrameMaps(Base_PolyMap _mapA, Base_PolyMap _mapB, boolean setAllInitVals) {
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
        currUIVals = new mapUpdFromUIData(mapMgr.getCurrUIVals());
        normDispTimeVec = new myVectorf(mapA.getCOV(), mapB.getCOV());
        normDispTimeVec = myVectorf._mult(mapA.basisVecs[0], normDispTimeVec._dot(mapA.basisVecs[0]));    
        //areaTrajMaps = new TreeMap<Float, Float>();
        mapFlags = new morphCntlFlags[numMapFlags];
        for(int i=0;i<mapFlags.length;++i) {
            mapFlags[i] = new morphCntlFlags(this);
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
        
    private final void _morphColors(Base_PolyMap _curMorphMap, float tA, float tB) {
        //calculate checker board and grid color morphs
        int[][] aPlyClrs = mapA.getPolyColors(),bPlyClrs = mapB.getPolyColors(), curPlyClrs = new int[aPlyClrs.length][aPlyClrs[0].length];
        for(int i=0;i<aPlyClrs.length;++i) {for(int j=0;j<aPlyClrs[i].length;++j) {    curPlyClrs[i][j] = calcMorph_Integer(tA,aPlyClrs[i][j],tB,bPlyClrs[i][j]);}}
        _curMorphMap.setPolyColors(curPlyClrs);        
        int[] aGridClr = mapA.getGridColor(), bGridClr = mapB.getGridColor(), curGridClrs = new int[aGridClr.length];
        for(int i=0;i<aGridClr.length;++i) {curGridClrs[i] = calcMorph_Integer(tA,aGridClr[i],tB,bGridClr[i]);}
        _curMorphMap.setGridColor(curGridClrs);
    }
    
    /**
     * this should be called whenever map A's values have changed (??) this is in place to address weird morph branching stuff TODO
     */
    //private float oldMorphT = 0.0f;
    public final void updateMorphMapWithMapVals() {    updateMorphMapWithMapVals(true);}
    public final void updateMorphMapWithMapVals(boolean calcMorph) {
        //mapMgr.msgObj.dispInfoMessage("baseMorph::"+morphTitle, "updateMorphMapWithMapVals", "Before updateMeWithMapVals");// :  curMorphMap :  "+ curMorphMap.toString());
        
//        curMorphMap.updateMeWithMapVals(mapA,mapFlags[mapUpdateNoResetIDX]);
//        if(calcMorph) {
//            calcMorph();
//        }
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
//        } else if(morphT == 1.0f) {
//            curMorphMap = getCopyOfMap(mapB,mapB.mapTitle + "_currMorphMap_"+morphTitle +" @ t="+String.format("%2.3f", morphT)); 
        } else {//if(morphT != oldMorphT){

            curMorphMap = getCopyOfMap(curMorphMap,mapA.mapTitle + "_currMorphMap_"+morphTitle +" @ t="+String.format("%2.3f", morphT)); 
        }
        //if(morphT == .5f) {    System.out.println(this.morphTitle+" : calc morph @ t== .5f");}
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
    
    protected final void _calcMorphOnMap(Base_PolyMap _curMorphMap, boolean _calcColors, float t) {
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
    
    public TreeMap<Float, Base_PolyMap> buildLineupOfFrames(int _numFrames) {
        return _buildArrayOfMorphMaps_Even(new TreeMap<Float, Base_PolyMap>(), _numFrames, "_Lineup_Frames");
    }
    
    /**
     * Called by morphCntlFlags upon debug being set/cleared
     * @param val
     */
    public void handleMapCntlDebug(boolean val) {
        //TODO
    }
    
    /**
     * return relevant control/info points for trajectory based on current time, from passed morph map
     * @param _curMorphMap = current map to morph and get contrl point locs from
     * @param tA
     * @param tB
     * @return
     */
    protected final myPointf[] getMorphMapTrajPts(Base_PolyMap _curMorphMap, float tA, float tB) {
        //update map with current morph calc
        calcMorphAndApplyToMap(_curMorphMap, tA, tB);
        //get relevant points from map, based on what kind of map, to build trajectories from
        myPointf[] newPts = _curMorphMap.getAllMorphCntlPts();        
        return newPts;
    }    
    
    /**
     * use currently set t value to calculate morph and apply to passed morph map
     */
    protected abstract void calcMorphAndApplyToMap(Base_PolyMap _curMorphMap, float tA, float tB);
    
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
    
//    /**
//     * calculate morph stack distortion
//     */
//    public final void calculateMorphDistortion(baseMorph currDistMsrMorph) {
//        setMorphSliceAra();
//        
//        currDistMsrMorph.setMorphSlices(3);
//        
//        morphSliceAraDistColorsSet = false;
//        morphMapDistColorsSet = false;
//        canResetMapFlags = false;
//        //launch thread to calc distortion in background
//        
//        distCalcRunners[morphSliceAraForDistCalcIDX].setAllInitMapVals(currDistMsrMorph, morphSliceAras[morphSliceAraForDistCalcIDX].values().toArray(new baseMap[0]));
//        th_exec.submit(distCalcRunners[morphSliceAraForDistCalcIDX]);
//
//        mapMgr.msgObj.dispInfoMessage("baseMorph", "calculateMorphDistortion",  "Launched distortion calc");
//    }//calculateMorphDistortion
//    
//    /**
//     * called after morph distortion calc thread is completed
//     * @return
//     */
//
//    public final float updateMapValsFromDistCalc() {
//        distCellColors[morphSliceAraForDistCalcIDX] = distCalcRunners[morphSliceAraForDistCalcIDX].getTtlDistPerCell();
//        mapA.setDistCellColors(distCellColors[morphSliceAraForDistCalcIDX][0]);
//        mapB.setDistCellColors(distCellColors[morphSliceAraForDistCalcIDX][distCellColors.length-1]);
//        minCellDistVals = distCalcRunners[morphSliceAraForDistCalcIDX].getMinDistPerCell();
//        maxCellDistVals = distCalcRunners[morphSliceAraForDistCalcIDX].getMaxDistPerCell();
//        canResetMapFlags = true;
//        setMorphMapAndSliceColors();        
//        return distCalcRunners[morphSliceAraForDistCalcIDX].getTtlDistForEntireMrphStck();
//    }
//    
//    private boolean morphMapDistColorsSet = false, morphSliceAraDistColorsSet = false, canResetMapFlags = false;
//    protected void setMorphMapAndSliceColors() {
//        TreeMap<Float, baseMap> morphSliceAra = morphSliceAras[morphSliceAraForDistCalcIDX];
//        
//        if((distCellColors != null) &&(distCellColors[morphSliceAraForDistCalcIDX] != null) && (morphSliceAra.size()==distCellColors[morphSliceAraForDistCalcIDX].length)) {
//            if(!morphSliceAraDistColorsSet) {    
//                int kIdx = 0;
//                for(float key : morphSliceAra.keySet()) {morphSliceAra.get(key).setDistCellColors(distCellColors[morphSliceAraForDistCalcIDX][kIdx++]);}            
//                if(canResetMapFlags) {morphSliceAraDistColorsSet = true;}
//            }
//            if(!morphMapDistColorsSet) {            
//                int kIDX = (int)((distCellColors[morphSliceAraForDistCalcIDX].length-1) * morphT);
//                //if(kIDX >= distCellColors.length-1) {
//                    curMorphMap.setDistCellColors(distCellColors[morphSliceAraForDistCalcIDX][kIDX]);
//    //                    } else {
//    //                        //curMorphMap.setDistCellColors(distCalcRunner.calcMorphMapDist(curMorphMap, kIDX));
//    //                        curMorphMap.setDistCellColors(distCalcRunner.calcMorphMapDist(morphSliceAra[kIDX], morphSliceAra[kIDX+1],curMorphMap));
//    //                    }
//                if(canResetMapFlags) {morphMapDistColorsSet=true;}
//            }
//        }
//        
//    }
//    

    /**
     * return the appropriate extra start frame and end frame copies depending on type of animation
     * @param animType int idx of animation/interpolant behavior
     * @return
     */
    protected Base_PolyMap[][][] getKFPolyMapsForCurMorphAnimType(Base_PolyMap[] morphSliceAra, int animType){
        Base_PolyMap[][][] res = new Base_PolyMap[2][][];
        InterpolantBehavior animBehavior = InterpolantBehavior.getEnumFromValue(animType);
        //morphSliceAra[k].buildPolyMaps();
        int stIdx, endIdx;
        switch (animBehavior) {
            case pingPong : {
                stIdx = 1;
                endIdx = morphSliceAra.length-2;
                break;
            }        
            case oneWayFwdLoop : {
                stIdx = morphSliceAra.length-1;
                endIdx = morphSliceAra.length-2;
                break;
            }        
            case oneWayBkwdLoop :{
                stIdx = 1;
                endIdx = 0;
                break;
            }        
            case pingPongStop : 
            case oneWayFwdStopLoop : 
            case oneWayBkwdStopLoop :{//morphSliceAra[k].buildPolyMaps();
                stIdx = 0;
                endIdx = morphSliceAra.length-1;
                break;
            }    
            default        :{        
                stIdx = 1;
                endIdx = morphSliceAra.length-2;    
                break;
            }
        }//switch
        
        res[0] = morphSliceAra[stIdx].buildPolyMaps();
        res[1] = morphSliceAra[endIdx].buildPolyMaps();        
        return res;
        
    }
    
    
    
    /**
     * calculate morph stack distortion
     */
    public final void calculateMorphDistortion(baseMorph currDistMsrMorph, int animType) {
        setMorphSliceAra();
        
        currDistMsrMorph.setMorphSlices(3);
        
        morphSliceAraDistColorsSet = false;
        morphMapDistColorsSet = false;
        canResetMapFlags = false;
        Base_PolyMap[] morphSliceAra = morphSliceAras[curMorphSliceAraIDX].values().toArray(new Base_PolyMap[0]);
        Base_PolyMap[][][] kfPolyMaps = getKFPolyMapsForCurMorphAnimType(morphSliceAra, animType);
        
        //launch thread to calc distortion in background
        distCalcRunner.setAllInitMapVals(currDistMsrMorph, morphSliceAra, kfPolyMaps[0],kfPolyMaps[1]);
//        distCalcRunners[equalDist_MorphSlicesIDX].setAllInitMapVals(currDistMsrMorph, morphSliceAras[equalDist_MorphSlicesIDX].values().toArray(new baseMap[0]));
//        distCalcRunners[equalRawT_MorphSlicesIDX].setAllInitMapVals(currDistMsrMorph, morphSliceAras[equalRawT_MorphSlicesIDX].values().toArray(new baseMap[0]));
        //th_exec.submit(distCalcRunner);
        th_exec.submit(distCalcRunner);

        mapMgr.msgObj.dispInfoMessage("baseMorph", "calculateMorphDistortion",  "Launched distortion calc");
    }//calculateMorphDistortion
    
    /**
     * called after morph distortion calc thread is completed
     * @return
     */

//    public final float updateMapValsFromDistCalc() {
//        distCellColors = distCalcRunners[curMorphSliceAraIDX].getTtlDistPerCell();
//        mapA.setDistCellColors(distCellColors[0]);
//        mapB.setDistCellColors(distCellColors[distCellColors.length-1]);
//        minCellDistVals = distCalcRunners[curMorphSliceAraIDX].getMinDistPerCell();
//        maxCellDistVals = distCalcRunners[curMorphSliceAraIDX].getMaxDistPerCell();
//        canResetMapFlags = true;
//        setMorphMapAndSliceColors();        
//        return distCalcRunners[curMorphSliceAraIDX].getTtlDistForEntireMrphStck();
//    }
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
        TreeMap<Float, Base_PolyMap> morphSliceAra = morphSliceAras[curMorphSliceAraIDX];
        
        if((distCellColors != null) &&(distCellColors != null) && (morphSliceAra.size()==distCellColors.length)) {
            if(!morphSliceAraDistColorsSet) {    
                int kIdx = 0;
                for(float key : morphSliceAra.keySet()) {morphSliceAra.get(key).setDistCellColors(distCellColors[kIdx++]);}            
                if(canResetMapFlags) {morphSliceAraDistColorsSet = true;}
            }
            if(!morphMapDistColorsSet) {            
                int kIDX = (int)((distCellColors.length-1) * morphT);
                //if(kIDX >= distCellColors.length-1) {
                    curMorphMap.setDistCellColors(distCellColors[kIDX]);
    //                    } else {
    //                        //curMorphMap.setDistCellColors(distCalcRunner.calcMorphMapDist(curMorphMap, kIDX));
    //                        curMorphMap.setDistCellColors(distCalcRunner.calcMorphMapDist(morphSliceAra[kIDX], morphSliceAra[kIDX+1],curMorphMap));
    //                    }
                if(canResetMapFlags) {morphMapDistColorsSet=true;}
            }
        }
        
    }
    
    /**
     * call only once
     */
    protected final void initTrajAnalyzers() {
        trajAnalyzers.clear();        //1 per control point
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
        for(String key : trajAnalyzers.keySet()) {        res.put(key, new ArrayList<myPointf>());    }
        
        myPointf[][] tmpCnltPtAra;
        //int idx = 0;
        for(Float t : _cntlPtTrajs.keySet()) {
            tmpCnltPtAra = _cntlPtTrajs.get(t);    //this is all cntl points at this time    
            for(String key : trajAnalyzeKeys.keySet()) {
                res.get(key).add(new myPointf(tmpCnltPtAra[0][trajAnalyzeKeys.get(key)]));    //add copy of control point from trajectory
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
            Base_PolyMap tmpMap = getCopyOfMap(null,mapA.mapTitle +"_MorphCntlPtTraj");
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
    
    
    public final void updateMorphVals_FromUI(mapUpdFromUIData upd) {
        currUIVals.setAllVals(upd);//can't use the same mapUpdFromUIData everywhere because we compare differences
        curMorphSliceAraIDX = currUIVals.getCurMorphSliceAraIDX();
        
        //morphSliceAraForDistCalcIDX = currUIVals.getMorphSliceAraForDistIDX();
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
        ri.pushMatState();        
        for(String key : trajAnalyzers.keySet()) {//per control point    
            if(key.equals(Base_PolyMap.COV_Label) && (dispDetail < COTS_MorphWin.drawMapDet_CntlPts_COV_IDX)){continue;}
            if(key.equals(Base_PolyMap.SpiralCtrLbl) && (dispDetail < COTS_MorphWin.drawMapDet_CntlPts_COV_EdgePts_F_IDX)){continue;}
            trajAnalyzers.get(key).drawAnalyzerData(ri, mmntDispLabels,trajWinDims, "Cntl Pt Traj : " + key);
        }
        areaTrajAnalyzer.drawAnalyzerData(ri, mmntDispLabels,trajWinDims, "Area :");
        ri.popMatState();
        
    }
    
    /**
     * draw all traj analysis graphs
     * @param trajWinDims array of float dims - width,height of window, y value where window starts, y value to displace every line
     */
    public final void drawTrajAnalyzerGraphs(String[] mmntDispLabels, int dispDetail, float[] trajWinDims) {
        //float yDisp = trajWinDims[3];
        ri.pushMatState();        
        for(String key : trajAnalyzers.keySet()) {//per control point
            if(key.equals(Base_PolyMap.COV_Label) && (dispDetail < COTS_MorphWin.drawMapDet_CntlPts_COV_IDX)){continue;}
            if(key.equals(Base_PolyMap.SpiralCtrLbl) && (dispDetail < COTS_MorphWin.drawMapDet_CntlPts_COV_EdgePts_F_IDX)){continue;}
            trajAnalyzers.get(key).drawAnalyzerGraphs(ri, mmntDispLabels,trajWinDims, "Cntl Pt Traj : " + key);
        }
        areaTrajAnalyzer.drawAnalyzerGraphs(ri, mmntDispLabels,trajWinDims, "Area :");
        ri.popMatState();
        
    }

    
    public final float drawMapRtSdMenuDescr(float yOff, float sideBarYDisp) {        
        ri.pushMatState();
            AppMgr.showMenuTxt_Green( morphTitle);
            AppMgr.showMenuTxt_White(" Morph Frame @ Time : ");
            AppMgr.showMenuTxt_Yellow( String.format(Base_PolyMap.strPointDispFrmt8,morphT));
        ri.popMatState();
    
        yOff += sideBarYDisp;
        ri.translate(10.0f,sideBarYDisp, 0.0f);        
        yOff = curMorphMap.drawRtSdMenuDescr(yOff, sideBarYDisp, false, true);
        return yOff;
    }
    public final float drawMorphSliceRtSdMenuDescr(float yOff, float sideBarYDisp) {
        if(null!=morphSliceAras[curMorphSliceAraIDX]) {
            float modYAmt = sideBarYDisp*.9f;
            yOff += modYAmt;
            //ri.translate(10.0f,modYAmt, 0.0f);        
            for(float key : morphSliceAras[curMorphSliceAraIDX].keySet()) {
                yOff = morphSliceAras[curMorphSliceAraIDX].get(key).drawRtSdMenuDescr(yOff, modYAmt, true, false);
            }
        }
        return yOff;
    }
    
    public final float drawMorphTitle(float yOff, float sideBarYDisp) {
        AppMgr.showOffsetText(0,IRenderInterface.gui_Cyan, morphTitle + " Morph : ");
        yOff += sideBarYDisp;
        ri.translate(0.0f, sideBarYDisp, 0.0f);
        return yOff;
    }
    /**
     * draw the summaries of distortion values on right side menu, highlighting the dimension of the distortion currently being displayed
     * @param vals
     */
    private final void _drawRtSideMenuDistSummaries(float[] vals,  String label, int curIdx) {
        ri.pushMatState();
            AppMgr.showMenuTxt_White( label + " :[");
            for(int i=0;i<vals.length-1;++i) {
                AppMgr.showOffsetText_RightSideMenu(ri.getClr((i==curIdx ? IRenderInterface.gui_Yellow : IRenderInterface.gui_Cyan), 255), String.format(Base_PolyMap.strPointDispFrmt85,vals[i])+", ");
            }
            AppMgr.showOffsetText_RightSideMenu(ri.getClr((vals.length-1==curIdx ? IRenderInterface.gui_Yellow : IRenderInterface.gui_Cyan), 255), String.format(Base_PolyMap.strPointDispFrmt85,vals[vals.length-1]));
            AppMgr.showMenuTxt_White("]");
        ri.popMatState();    
    }
    
    
    public final float drawDistortionRtSideMenuMinMax(float yOff, float sideBarYDisp, int curIdx) {        
        _drawRtSideMenuDistSummaries(maxCellDistVals,"Max Dist",  curIdx);
        yOff += sideBarYDisp;
        ri.translate(0.0f,sideBarYDisp, 0.0f);    
        
        _drawRtSideMenuDistSummaries(minCellDistVals,"Min Dist",  curIdx);
        yOff += sideBarYDisp;
        ri.translate(0.0f,sideBarYDisp, 0.0f);                

        return yOff;
    }
    
    public final float drawMorphRtSdMenuDescr(float yOff, float sideBarYDisp, float _morphSpeed) {//, String[] _scopeList) {
        ri.pushMatState();
            AppMgr.showMenuTxt_White("Morph Between :");
            AppMgr.showMenuTxt_Yellow( mapA.mapTitle);
            AppMgr.showMenuTxt_White(" and");
            AppMgr.showMenuTxt_Yellow( mapB.mapTitle);
        ri.popMatState();        
        yOff += sideBarYDisp;
        ri.translate(0.0f,sideBarYDisp, 0.0f);        
        
        ri.pushMatState();
            AppMgr.showMenuTxt_White("Currently at time :");
            AppMgr.showMenuTxt_Yellow( String.format(Base_PolyMap.strPointDispFrmt8,morphT));
            AppMgr.showMenuTxt_White(" | Speed :");
            AppMgr.showMenuTxt_Yellow( String.format(Base_PolyMap.strPointDispFrmt8,_morphSpeed));
        ri.popMatState();        
        
        yOff += sideBarYDisp;
        ri.translate(0.0f,sideBarYDisp, 0.0f);            
        yOff = drawMorphRtSdMenuDescr_Indiv(yOff, sideBarYDisp);    
        return yOff;
    }    
    public abstract float drawMorphRtSdMenuDescr_Indiv(float yOff, float sideBarYDisp);    
    public final void drawMorphedMap_CntlPts(int _detail) {        curMorphMap.drawMap_CntlPts(false, _detail);    }
    
    protected Base_PolyMap getCopyOfMap(Base_PolyMap cpyMap, String fullCpyName) {
        if(null==cpyMap) {cpyMap = mapA;}
        Base_PolyMap resMap = mapMgr.buildCopyMapOfPassedMapType(cpyMap, fullCpyName);
        //resMap.updateMeWithMapVals(cpyMap,mapFlags[mapCopyNoResetIDX]);
        return resMap;
    }
    

    /**
     * builds cntl point trajectories using current morph
     */
    public final void drawMorphedMap_CntlPtTraj(int _detail) {
        ri.pushMatState();    
        ri.setSphereDetail(5);
        ri.setStroke(0,0,0,255);
    
        ri.setStrokeWt(1.0f);
        for(float t = 0.01f;t<=1.0f;t+=.01f) {
            //float tA = 1.0f-t, tB = t;
            //initCalcMorph_Indiv(tA, tB);
            myPointf[][] cntlPts = cntlPtTrajs.get(t);
            myPointf[][][] edgePts = edgePtTrajs.get(t);
            //idx 5 is cov, idx 6 is ctr pt
            if(null != cntlPts) {
                for(int i = 0;i<cntlPts[0].length-2;++i) {        mapMgr._drawPt(cntlPts[1][i], 2.0f); ri.drawLine(cntlPts[0][i], cntlPts[1][i]);}
            }
            //following not nested in case their values change
            if(_detail >= COTS_MorphWin.drawMapDet_CntlPts_COV_IDX) {
                int k=cntlPts[0].length-2;
                mapMgr._drawPt(cntlPts[1][k], 2.0f); ri.drawLine(cntlPts[0][k], cntlPts[1][k]);                
            }
            if(_detail >= COTS_MorphWin.drawMapDet_CntlPts_COV_EdgePts_IDX) {
                for(int i=0;i<edgePts[0].length;++i) {    for(int j=0;j<edgePts[0][i].length;++j) {    ri.drawLine(edgePts[0][i][j], edgePts[1][i][j]);}}
            }
            if(_detail >= COTS_MorphWin.drawMapDet_CntlPts_COV_EdgePts_F_IDX) {
                int k=cntlPts[0].length-1;
                mapMgr._drawPt(cntlPts[1][k], 2.0f); ri.drawLine(cntlPts[0][k], cntlPts[1][k]);
            }
        }        
        ri.popMatState();    
    }
    
    public final void drawCurrMorphedMap(boolean _showDistColors, boolean _isFill, boolean _drawMap, boolean _drawCircles) {
        _drawMorphMap(curMorphMap, _showDistColors,_isFill, _drawMap, _drawCircles);
    }
    
    public final void drawMorphSlices( boolean _showDistColors, boolean _isFill, boolean _drawMorphSliceMap, boolean _drawCircles, boolean _drawCntlPts, boolean _showLabels, int _detail) {
        TreeMap<Float, Base_PolyMap> morphSliceAra = morphSliceAras[curMorphSliceAraIDX];
        if(_drawMorphSliceMap) {
            if(_showDistColors && morphSliceAraDistColorsSet) {    for(Float t : morphSliceAra.keySet()) {        morphSliceAra.get(t).drawMap_DistColor( currUIVals.getMorphDistMult(), currUIVals.getDistDimToShow());    }} 
            else if(_isFill) {    for(Float t : morphSliceAra.keySet()) {        morphSliceAra.get(t).drawMap_Fill();}} 
            else {            for(Float t : morphSliceAra.keySet()) {        morphSliceAra.get(t).drawMap_Wf();}}
        }
        if(_drawCircles) {
            if((!_drawMorphSliceMap) &&_isFill) {    for(Float t : morphSliceAra.keySet()) {        morphSliceAra.get(t).drawMap_PolyCircles_Fill();}} 
            else {            for(Float t : morphSliceAra.keySet()) {        morphSliceAra.get(t).drawMap_PolyCircles_Wf();}}
        
        }

        if(_drawCntlPts) {
            for(Float t : morphSliceAra.keySet()) {
                Base_PolyMap map = morphSliceAra.get(t);
                map.drawMap_CntlPts(false, _detail);
                map.drawHeaderAndLabels(_showLabels, _detail);
            }
        }    
    }
    
    protected final void _drawMorphMap(Base_PolyMap _map, boolean _showDistColors, boolean _isFill, boolean _drawMap, boolean _drawCircles) {
        if(_drawMap) {
            if(_showDistColors && morphSliceAraDistColorsSet) {_map.drawMap_DistColor( currUIVals.getMorphDistMult(), currUIVals.getDistDimToShow());    }
            else if(_isFill) {    _map.drawMap_Fill();}
            else {            _map.drawMap_Wf();}    
        }
        if(_drawCircles) {
            if((!_drawMap) && _isFill) {    _map.drawMap_PolyCircles_Fill();}    
            else {            _map.drawMap_PolyCircles_Wf();}        
        }
    }

    public final void drawHeaderAndLabels(boolean _drawLabels, int _detail) {                            curMorphMap.drawHeaderAndLabels(_drawLabels,_detail);}
    
    /**
     * this will draw instancing morph-specific data on screen 
     */
    public abstract void drawMorphSpecificValues(boolean debug, boolean drawCntlPts, boolean showLbls);

    
    /**
     * this will return an array of k, i,j control point arrays, where i and j are map column and row, and k is slice idx
     * @return
     */
    public final Base_PolyMap[][][] buildAllSliceCellMaps(TreeMap<Float, Base_PolyMap> sliceAra){        
        Base_PolyMap[][][] res = new Base_PolyMap[numMorphSlices][][];
        int i = 0;
        for(Float key : sliceAra.keySet()) {res[i++] = sliceAra.get(key).buildPolyMaps();}        
        return res;
    }
    
    
    /////////////////////////
    // setters/getters
    
    public final void setMorphT(float _t) {            morphT=_t;                calcMorph();}    
    //public final void setMorphScope(int _mScope) {    morphScope = _mScope;    calcMorph();}
    
    public final void setMorphSlices(int _num) {
        if(_num < 3) {_num =3;}//allow no fewer than 3 slices
        int oldNumMorphSlices = numMorphSlices; 
        numMorphSlices=_num;
        if(oldNumMorphSlices != numMorphSlices) {setMorphSliceAra();}
    }

    protected final void setMorphSliceAra() {
        morphSliceAras[equalRawT_MorphSlicesIDX] = _buildArrayOfMorphMaps_Fade(morphSliceAras[equalRawT_MorphSlicesIDX],numMorphSlices, "_MrphSlcFade");
        morphSliceAras[equalDist_MorphSlicesIDX] = _buildArrayOfMorphMaps_Even(morphSliceAras[equalDist_MorphSlicesIDX],numMorphSlices, "_MrphSlcEven");
        //morphSliceAra = buildArrayOfMorphMaps(numMorphSlices, "_MrphSlc");
        morphSliceAraDistColorsSet = false;
        setMorphMapAndSliceColors();
    }//setMorphMapAra()    
    
    
    private final TreeMap<Float, Base_PolyMap> _buildArrayOfMorphMaps_Fade(TreeMap<Float, Base_PolyMap> morphMaps, int numMaps, String _name) {
        Base_PolyMap tmpMap = getCopyOfMap(null, mapA.mapTitle);    
        morphMaps.clear();
        //TreeMap<Float, baseMap> morphMaps = new TreeMap<Float, baseMap>();
        //want incr so that i get numMaps back
        Float tIncr = 1.0f/(numMaps-1.0f);  //getCurrAnimatorInterpolant
        Float _rawt = 0.0f, t;
        for (int i=0;i<numMaps-1;++i) {    
            t = mapMgr.getCurrAnimatorInterpolant(_rawt);
            tmpMap = getCopyOfMap(tmpMap, mapA.mapTitle +_name + " @ t="+String.format("%2.3f", t));
            _calcMorphOnMap(tmpMap, true, t);
            morphMaps.put(_rawt, tmpMap);        
            _rawt+=tIncr;
        }
        _rawt=1.0f;
        t = mapMgr.getCurrAnimatorInterpolant(_rawt);
        //tmpMap = mapMgr.buildCopyMapOfPassedMapType(mapA, "Morph @ t="+String.format("%2.3f", t));
        tmpMap = getCopyOfMap(tmpMap, mapA.mapTitle +_name + " @ t="+String.format("%2.3f", t));
        //
        _calcMorphOnMap(tmpMap, true,_rawt);    
        morphMaps.put(_rawt, tmpMap);                
        return morphMaps;
    }

    private final TreeMap<Float, Base_PolyMap> _buildArrayOfMorphMaps_Even(TreeMap<Float, Base_PolyMap> morphMaps, int numMaps, String _name) {
        Base_PolyMap tmpMap = getCopyOfMap(null, mapA.mapTitle);    
        morphMaps.clear();
        //TreeMap<Float, baseMap> morphMaps = new TreeMap<Float, baseMap>();
        //want incr so that i get numMaps back
        Float tIncr = 1.0f/(numMaps-1.0f);  //getCurrAnimatorInterpolant
        Float t=0.0f;
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
    
    public Base_PolyMap getCurMorphMap() {return curMorphMap;}    
    public myPointf[] getCurMorphMap_CntlPts() {return curMorphMap.getCntlPts();}
    
    public final int getNumMorphSlices() {
        //mapMgr.msgObj.dispInfoMessage("baseMorph", "getNumMorphSlices", "NumMorphSlices var : " + numMorphSlices + " | Size of morph slices ara : " + this.morphSliceAra.size());
        return numMorphSlices;
    }
    
    public mapPairManager getMapPairManager() {return mapMgr;}

    public final String toStringEdge(myPointf[] e) {
        return "0:["+e[0].toStrCSV(Base_PolyMap.strPointDispFrmt8)+"] | 1:["+e[1].toStrCSV(Base_PolyMap.strPointDispFrmt8)+"]";
    }
    

}//class baseMorph
