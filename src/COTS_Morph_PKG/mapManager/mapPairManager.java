package COTS_Morph_PKG.mapManager;


import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import COTS_Morph_PKG.map.base.Base_PolyMap;
import COTS_Morph_PKG.map.quad.BiLinMap;
import COTS_Morph_PKG.map.quad.COTSMap;
import COTS_Morph_PKG.map.registration.mapRegDist;
import COTS_Morph_PKG.map.triangular.BiLinTriPolyMap;
import COTS_Morph_PKG.map.triangular.PointNormTriPolyMap;
import COTS_Morph_PKG.morph.CompoundMorph;
import COTS_Morph_PKG.morph.base.baseMorph;
import COTS_Morph_PKG.morph.multiTransform.DualCarrierSimMorph;
import COTS_Morph_PKG.morph.multiTransform.LogPolarMorph;
import COTS_Morph_PKG.morph.multiTransform.QuadKeyEdgeSpiralMorph;
import COTS_Morph_PKG.morph.singleTransform.AffineMorph;
import COTS_Morph_PKG.morph.singleTransform.CarrierSimDiagMorph;
import COTS_Morph_PKG.morph.singleTransform.LERPMorph;
import COTS_Morph_PKG.morph.singleTransform.RigidMorph;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.GUI_AppManager;
import base_UI_Objects.windowUI.base.Base_DispWindow;
import base_Utils_Objects.io.messaging.MessageObject;
import base_Math_Objects.interpolants.base.Base_Interpolant;
import base_Math_Objects.interpolants.base.InterpolantTypes;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;
import processing.core.PImage;

/**
 * this class will manage a pair of maps that are acting as key frames, controlling access to the maps
 * this class will serve to isolate and manage map->map interactions, including but not limited to morphing
 * there will be one map manager for each type of map
 * @author john
 *
 */
public class mapPairManager {

	public static IRenderInterface pa;
	public COTS_MorphWin win;	
	public static GUI_AppManager AppMgr;
	/**
	 * descriptive name of this map manager
	 */
	public final String name;
	/**
	 * the maps this mapManager will manage
	 */
	private final Base_PolyMap[] maps;
	/**
	 * the types of maps this manager owns
	 */
	public final int mapType;
	/**
	 * current ui values describing features of the map
	 */
	protected mapUpdFromUIData currUIVals;
	/**
	 * map being currently modified by mouse interaction - only a ref to a map, or null
	 */
	public Base_PolyMap currMseModMap;
	/**
	 * idx's of maps to use for similarity mapping/registration between maps,
	 * current morph being executed
	 */
	protected int fromMapIDX = -1, toMapIDX = -1, currMorphTypeIDX;
	/**
	 * bounds for the key frame maps managed by this
	 */
	public myPointf[][] bndPts;
	public final myPointf[][] origBndPts;
	/**
	 * structure to manage registration functionality and distance calculation
	 */
	public mapRegDist mapRegDistCalc;
	
	/**
	 * types of morphs supported
	 */
	public static final String[] morphTypes = new String[] {
		"LERP",						//linearly interpolate control points
		"Log Polar",
		"Rigid",
		"Affine",
		"Carrier Sim using Diagonal",
		"Dual Carrier Similarity",
		"Key edge->Key edge Spiral",
		"Compound Morph"
	};
	/**
	 * morph types available to compound morph - CompoundMorphIDX should not be available
	 */
	public static final String[] cmpndMorphTypes = new String[] {
		"LERP",						//linearly interpolate control points
		"Log Polar",			
		"Rigid",
		"Affine",
		"Carrier Sim using Diagonal",
		"Dual Carrier Similarity",
		"Key edge->Key edge Spiral",
	};
	
	
	public static final int compareMorphSameIDX = 0;
	
	//need an index per morph type
	public static final int
		LERPMorphIDX			= 0,
		LogPolarMorphIDX 		= 1,
		RigidMorphIDX			= 2,
		AffineMorphIDX			= 3,
		CarrierSimDiagIDX		= 4,
		DualCarrierSimIDX		= 5,
		QuadSpiralEdgeIDX		= 6,
		CompoundMorphIDX		= 7;	
	
	/**
	 * types of maps supported
	 */
	public static final String[] mapTypes = new String[] {
		"Triangle",
		"Pt-Norm Tri",
		"Bilinear",
		"COTS",	
		"Bary BiLin pt D",	
		"Bary COTS pt D"
	};
	//need an index per map type
	public static final int
		triangleMapIDX			= 0,
		ptNormTriMapIDX			= 1,
		bilinearMapIDX			= 2,
		COTSMapIDX		 		= 3,
		BaryBiLinQuadMapIDX		= 4,
		BaryCOTSQuadMapIDX		= 5;	

	/**
	 * array holding morphs
	 */
	protected baseMorph[] morphs;
	/**
	 * array holding morphs to use for distortion measurements
	 */
	protected baseMorph[] distMsrMorphs;

	public MessageObject msgObj;
	/**
	 * array holding upper left corner x,y, width, height of rectangle to use for oriented lineup images
	 */
	protected float[] lineupRectDims;
	protected float perLineupImageWidth;
	protected TreeMap<Float, Base_PolyMap> lineUpMorphMaps;
	
	/**
	 * array holding upper left corner x,y, width, height of rectangle to use for displaying graphs of trajectory analysis
	 */
	protected float[] trajAnalysisRectDims;
	protected float perTrajAnalysisImageWidth;
	
	/**
	 * array holding upper left corner x,y, width, height of rectangle to use for displaying graphs of morph stack distortion analysis
	 */
	protected float[] mStckDistAnalysisRectDims;
	protected float perMStckDistAnalysisImageWidth;
	/**
	 * threading constructions - allow map manager to own its own threading executor
	 */
	protected ExecutorService th_exec;	//to access multithreading - instance from calling program
	protected final int numUsableThreads;		//# of threads usable by the application

	/**
	 * morph animation variables
	 */
	private float //morphProgress = 0.5f, 
			morphSpeed = 1.0f;
	/**
	 * interplants to be used for animation
	 */
	private Base_Interpolant[] animators;
	/**
	 * index of current animator being used
	 */
	private int curAnimatorIDX = InterpolantTypes.linear.getVal();
	
	/**
	 * colors for each of 2 maps' grids
	 */
	protected static final int[][][] mapGridColors = new int[][][] {
		{{90,0,222,255},{0,225,10,255}},		//map grid 0 
		{{255,200,0,255},{255,0,0,255}}			//map grid 1
	};

	/**
	 * whether or not the current UI values are reflected in the current distortion calculation
	 */
	protected boolean morphStackAnalysisDone = false;

	public mapPairManager(COTS_MorphWin _win, myPointf[][] _bndPts, PImage[] _txtrImages, mapUpdFromUIData _currUIVals, int _mapType) {
		win=_win; pa=Base_DispWindow.ri;  AppMgr= Base_DispWindow.AppMgr; msgObj=win.getMsgObj();
				
		//for building registration copy
		fromMapIDX = 0;
		toMapIDX = 1;
		lineUpMorphMaps = new TreeMap<Float, Base_PolyMap>();
		mapType=_mapType;
		name = win.getWinName()+"::Mgr of " + mapTypes[mapType] + " maps";
		// necessary info to build map
		origBndPts = new myPointf[_bndPts.length][];
		for(int i=0;i<origBndPts.length;++i) {origBndPts[i]=new myPointf[_bndPts[i].length];	for(int j=0;j<origBndPts[i].length;++j) {		origBndPts[i][j]=new myPointf(_bndPts[i][j]);	}}
		bndPts = new myPointf[_bndPts.length][];
		for(int i=0;i<bndPts.length;++i) {bndPts[i]=new myPointf[_bndPts[i].length];	for(int j=0;j<bndPts[i].length;++j) {		bndPts[i][j]=new myPointf(_bndPts[i][j]);	}}
		//setBndPts(origBndPts);
		
		currUIVals = new mapUpdFromUIData(_currUIVals);		
		animators = new Base_Interpolant[InterpolantTypes.getNumVals()];
		for(int i=0;i<animators.length;++i) {
			animators[i] = Base_Interpolant.buildInterpolant(InterpolantTypes.getVal(i), 0.5f, 0.5f);
		}
		
		//want # of usable background threads.  Leave 2 for primary process (and potential draw loop)
		numUsableThreads = Runtime.getRuntime().availableProcessors() - 2;
		th_exec = Executors.newCachedThreadPool();// this is performing much better even though it is using all available threads
		
		//build maps
		maps = new Base_PolyMap[2];
		for(int j=0;j<maps.length;++j) {	maps[j] = buildKeyFrameMapOfPassedType(mapType,j,  bndPts[j],"");}		
		mapRegDistCalc = new mapRegDist(this, maps[0],maps[1]);
		
		for(int j=0;j<maps.length;++j) {	maps[j].setImageToMap(_txtrImages[j]);	maps[j].setOtrMap(maps[(j+1)%maps.length]);}

		morphs = new baseMorph[morphTypes.length];
		for(int i=0;i<morphs.length;++i) {	morphs[i]=buildMorph(i, maps[0],maps[1]);}
		
		distMsrMorphs = new baseMorph[morphTypes.length];
		for(int i=0;i<distMsrMorphs.length;++i) {	distMsrMorphs[i]=buildMorph(i, maps[0],maps[1]);}
		
		
		setPopUpWins_RectDims();
		updateMapMorphVals_FromUI(currUIVals);
	}//ctor
	
	/**
	 * call from morphs to determine what current animator will yield for passed raw t value
	 * @param _rawt raw, linearly-evolved t value from 0 to 1
	 * @return 
	 */
	public float getCurrAnimatorInterpolant(float _rawt) {
		return animators[curAnimatorIDX].calcInterpolant(_rawt);
	}
	
//	public void setBndPts(myPointf[][] _bndPts) {
//		bndPts = new myPointf[_bndPts.length][];
//		for(int i=0;i<bndPts.length;++i) {bndPts[i]=new myPointf[_bndPts[i].length];	for(int j=0;j<bndPts[i].length;++j) {		bndPts[i][j]=new myPointf(_bndPts[i][j]);	}}
//	}
	public void setBndPts(myPointf[][] _bndPts) {
		for(int i=0;i<bndPts.length;++i) {for(int j=0;j<bndPts[i].length;++j) {		bndPts[i][j].set(_bndPts[i][j]);	}}
	}
	/**
	 * call to reset all maps' original base points - used for reset
	 */
	public void resetBndPts() {setBndPts(origBndPts);}
	
	
	public baseMorph buildMorph(int typeIDX, Base_PolyMap _mapA, Base_PolyMap _mapB) {
		switch (typeIDX) {
			case LERPMorphIDX 		: {		return new LERPMorph(win,this,_mapA, _mapB, typeIDX, morphTypes[typeIDX]); 		}
			case RigidMorphIDX  	: {		return new RigidMorph(win,this,_mapA, _mapB, typeIDX, morphTypes[typeIDX]); 		}
			case AffineMorphIDX 	: {		return new AffineMorph(win,this,_mapA, _mapB, typeIDX, morphTypes[typeIDX]);}
			case CarrierSimDiagIDX 	: {		return new CarrierSimDiagMorph(win,this,_mapA, _mapB, typeIDX, morphTypes[typeIDX]);	}
			case DualCarrierSimIDX 	: {		return new DualCarrierSimMorph(win,this,_mapA, _mapB, typeIDX, morphTypes[typeIDX]); }
			case QuadSpiralEdgeIDX 	: {		return new QuadKeyEdgeSpiralMorph(win,this,_mapA, _mapB, typeIDX, morphTypes[typeIDX]);}
			case LogPolarMorphIDX 	: {		return new LogPolarMorph(win,this,_mapA, _mapB, typeIDX,  morphTypes[typeIDX]);}
			case CompoundMorphIDX 	: {		return new CompoundMorph(win,this,_mapA, _mapB, typeIDX,  morphTypes[typeIDX]);}
		
			default : {
				msgObj.dispInfoMessage("mapPairManager", "buildMorph", "Unknown morph type idx : " + typeIDX + ". Returning null.");
				return null;
			}
		}
	}//buildMorph
	
	public synchronized final baseMorph buildCopyMorphOfPassedType(baseMorph oldMorph) {
		int typeIDX = oldMorph.morphTypeIDX;
		switch (typeIDX) {
			case LERPMorphIDX 		: {		return new LERPMorph((LERPMorph)oldMorph); 		}
			case RigidMorphIDX  	: {		return new RigidMorph((RigidMorph)oldMorph); 		}
			case AffineMorphIDX 	: {		return new AffineMorph((AffineMorph)oldMorph);}
			case CarrierSimDiagIDX 	: {		return new CarrierSimDiagMorph((CarrierSimDiagMorph)oldMorph);	}
			case DualCarrierSimIDX 	: {		return new DualCarrierSimMorph((DualCarrierSimMorph)oldMorph); }
			case QuadSpiralEdgeIDX 	: {		return new QuadKeyEdgeSpiralMorph((QuadKeyEdgeSpiralMorph)oldMorph);}
			case LogPolarMorphIDX 	: {		return new LogPolarMorph((LogPolarMorph)oldMorph);}
			case CompoundMorphIDX 	: {		return new CompoundMorph((CompoundMorph)oldMorph);}
		
			default : {
				msgObj.dispInfoMessage("mapPairManager", "buildCopyMorphOfPassedType", "Unknown morph type idx : " + typeIDX + ". Returning null.");
				return null;
			}
		}
		
	}//buildCopyMorphOfPassedType

	/**
	 * build a map of the specified map type
	 * @param _mapType type of map : Bilin, cots, etc
	 * @param _mapValIdx : idx of map, either 0 or 1
	 * @param _cntlPts : control points to use as initial points for map
	 * @param _mapNameSuffix : descriptive string to add to end of map namme
	 * @return
	 */	
	public synchronized final Base_PolyMap buildKeyFrameMapOfPassedType(int _mapType, int _mapValIdx, myPointf[] _cntlPts, String _mapNameSuffix) {
		String mapName = mapTypes[_mapType] + ( _mapNameSuffix.length() == 0 ? "":  " " + _mapNameSuffix) + " Map"+ _mapValIdx;
		switch (_mapType) {
			case triangleMapIDX			:{
				myPointf[] triBndPts = new myPointf[3];
				for(int i=0;i<triBndPts.length;++i) {		triBndPts[i]=_cntlPts[i];		}
				return new BiLinTriPolyMap(win, this, triBndPts, _mapValIdx, _mapType, mapGridColors[_mapValIdx], currUIVals,true, mapName);}
			case ptNormTriMapIDX		: {	
				myPointf[] triBndPts = new myPointf[3];
				for(int i=0;i<triBndPts.length;++i) {		triBndPts[i]=_cntlPts[i];		}
				return new PointNormTriPolyMap(win, this,triBndPts, _mapValIdx, _mapType, mapGridColors[_mapValIdx], currUIVals,true, mapName);}
			case bilinearMapIDX 		: {	return new BiLinMap(win,this, _cntlPts, _mapValIdx, _mapType,mapGridColors[_mapValIdx], currUIVals,true, false, mapName);}
			case COTSMapIDX 			: {	return new COTSMap(win,this,  _cntlPts, _mapValIdx, _mapType,mapGridColors[_mapValIdx], currUIVals,true, false, mapName);}
		    case BaryBiLinQuadMapIDX	: { return new BiLinMap(win,this, _cntlPts, _mapValIdx, _mapType,mapGridColors[_mapValIdx], currUIVals,true, true, mapName);}  
		    case BaryCOTSQuadMapIDX		: { return new COTSMap(win,this,  _cntlPts, _mapValIdx, _mapType,mapGridColors[_mapValIdx], currUIVals,true, true, mapName);}  
			default		:{
				msgObj.dispErrorMessage("mapManager", "buildMapOfPassedType", "Error : Unable to build requested map due to unknown map type : " + _mapType + " : "  +mapName + ". Returning null.");
				return null;
			}			
		}
	}
	
	public final Base_PolyMap buildCopyMapOfPassedMapType(Base_PolyMap oldMap, String _mapName) {	
		Base_PolyMap map;
		switch (oldMap.mapTypeIDX) {
			case triangleMapIDX			: {	map = new BiLinTriPolyMap(_mapName,(BiLinTriPolyMap)oldMap);		break;}
			case ptNormTriMapIDX		: {	map = new PointNormTriPolyMap(_mapName,(PointNormTriPolyMap)oldMap);		break;}
			case bilinearMapIDX 		: {	map = new BiLinMap(_mapName,(BiLinMap)oldMap);		break;}
			case COTSMapIDX 			: {	map = new COTSMap(_mapName,(COTSMap)oldMap); 		break;}
			case BaryBiLinQuadMapIDX	: {	map = new BiLinMap(_mapName,(BiLinMap)oldMap);		break;} 
			case BaryCOTSQuadMapIDX		: {	map = new COTSMap(_mapName,(COTSMap)oldMap); 		break;} 
			default		:{
				msgObj.dispErrorMessage("mapManager", "buildMapOfPassedType", "Error : Unable to duplicate passed map "+ oldMap.mapTitle + " due to unknown map type : " + oldMap.mapTypeIDX + " : "  +mapTypes[oldMap.mapTypeIDX] + ". Returning null.");
				return null;
			}
		}
		return map;
	}//buildCopyMapOfPassedMapType		

	
	//////////////
	// map comparison and map/morph processing

	/**
	 * takes array of points and calculates the area of the poly described by them in the plane described by the normal n
	 * @param pts array of points making up poly
	 * @param n unit normal of plane points live in
	 * @return
	 */
	public final float calcAreaOfPolyInPlane(myPointf[] pts, myPointf planarPt, myVectorf n) {
		float res = 0.0f;
		myVectorf U = new myVectorf(),V= new myVectorf();
		for(int i=1;i<pts.length;++i) {
			U.set(planarPt, pts[i-1]);
			V.set(planarPt, pts[i]);
			res +=  myVectorf._mixProd(n,U, V);		//signed area projected on normal axis
		}
		U.set(planarPt,pts[pts.length-1]);
		V.set(planarPt,pts[0]);
		res +=  myVectorf._mixProd(n,U, V);		//signed area projected on normal axis
		res/=2.0f;
		return res;
		
	}

	/**
	 * calculate center of mass of poly described by passed point array
	 * center of mass is not necessarily centroid of array of passed points
	 * each area weights COM Calc  : (individual triangle coms * triangle areas)/total area
	 * @param pts points to take COM of
	 * @param thirdPt a third point in the plane of these points, to use as a pivot point for the triangles used to build COM
	 * @param n normal to plane of points
	 * @return COM point
	 */
	public final myPointf calcCOM(myPointf[] pts, myPointf thirdPt, myVectorf n){
		myPointf COM = new myPointf(), triCOM;
		float ttlArea = 0.0f, triArea = 0.0f;
		myVectorf U = new myVectorf(),V= new myVectorf();
		for(int i = 1; i< pts.length;++i) {
			U.set(thirdPt,pts[i-1]);
			V.set(thirdPt,pts[i]);
			
			triCOM = myPointf._average(thirdPt, pts[i-1],pts[i]);
			triArea = myVectorf._mixProd(n,U, V);		//signed area projected on normal axis
			COM._add(myPointf._mult(triCOM, triArea));
			ttlArea += triArea;			
		}
		U.set(thirdPt,pts[pts.length-1]);
		V.set(thirdPt,pts[0]);
		triCOM = myPointf._average(thirdPt, pts[pts.length-1],pts[0]);
		triArea = myVectorf._mixProd(n,U, V);		//signed area projected on normal axis
		COM._add(myPointf._mult(triCOM, triArea));
		ttlArea += triArea;
		myPointf Ct = myPointf._mult(COM, 1.0f/ttlArea);
		return Ct;
	}//calcCOM
	
	/**
	 * register "from" map to "to" map, and build a copy
	 * @param dispMod
	 */
	public final void findDifferenceBetweenMaps(boolean dispMod, boolean findBestDist) {	
		mapRegDistCalc.setMapsAndCalc(maps[fromMapIDX], maps[toMapIDX], dispMod, findBestDist);
	}//findDifferenceBetweenMaps
	
	/**
	 * calculate the distortion in the current morph
	 * @param _currDistTransformIDX index in morph list for transformatio to use to calculate distortion
	 */

	public boolean morphCalcLaunched = false;
	private int maxDispCounter = 30, curDispCounter =0;
	public final void calculateMorphDistortion() {
//		if((mapType==triangleMapIDX) || ( mapType==ptNormTriMapIDX)) {	
//			//need to find better way of calculating triangle map cell corner points so indexing isn't broken
//			msgObj.dispInfoMessage("mapPairManager", "calculateMorphDistortion",  "Calculating morph distortion for maps of type " + mapTypes[mapType] + " not yet supported.");
//			return;
//		}
			//this is index in morph list for type of morph to use to measure distortion
		if(!morphCalcLaunched) {
			curDispCounter = 0;
			int currDistTransformIDX = currUIVals.getCurrDistTransformIDX();		
			baseMorph currDistMsrMorph = distMsrMorphs[currDistTransformIDX];
			currDistMsrMorph.updateMorphVals_FromUI(this.currUIVals);
			morphCalcLaunched = true;
			morphs[currMorphTypeIDX].calculateMorphDistortion(currDistMsrMorph, currUIVals.getCurMorphAnimTypeIDX());
			msgObj.dispInfoMessage("mapPairManager", "calculateMorphDistortion",  "Waiting to finish.");	
		} else {
			++curDispCounter;
			if(curDispCounter % maxDispCounter == 0) {
				msgObj.dispInfoMessage("mapPairManager", "calculateMorphDistortion",  "Already calculating Morph distortion, please wait for completion.");
				curDispCounter=0;
			}			
		}
	
		
	}//calculateMorphDistortion

	/**
	 * called by distortion runner
	 */
	public final void finishedDistortionCalc() {
		float ttlDistForEntireMorph = morphs[currMorphTypeIDX].updateMapValsFromDistCalc();
		morphStackAnalysisDone = true;
		morphCalcLaunched = false;
		msgObj.dispInfoMessage("mapPairManager", "finishedDistortionCalc",  "Finished calculating current morph distortion : Total average distortion across entire morph : " +ttlDistForEntireMorph +" for maps of type " + mapTypes[mapType] +" with "+ currUIVals.getNumCellsPerSide() +" cells per side and morph of type " +morphTypes[currMorphTypeIDX] +" with " + currUIVals.getNumMorphSlices() + " slices, using transformation : " + morphTypes[currUIVals.getCurrDistTransformIDX()]);	
	}


	/**
	 * build oriented lineup of specific # of frames (default 5) where each frame is registered to keyframe A, and then displayed side-by-side
	 */
	public final void buildOrientedLineup() {
		TreeMap<Float, Base_PolyMap> rawMorphMaps = morphs[currMorphTypeIDX].buildLineupOfFrames(currUIVals.getNumLineupFrames()); 
		//msgObj.dispInfoMessage("mapManager::"+name, "buildOrientedLineup", "# of morph maps: " + rawMorphMaps.size() + " lineup num requested :" +currUIVals.numLineupFrames + " maps types : " + maps[0].mapTitle+" | " + maps[1].mapTitle);
		lineUpMorphMaps.clear();
		for(Float t : rawMorphMaps.keySet()) {
			Base_PolyMap tmpMorphMap = rawMorphMaps.get(t);
			lineUpMorphMaps.put(t, mapRegDistCalc.calcDifferenceBetweenMaps(tmpMorphMap, maps[0]));
		}	
		
	}

	
	//getter/setter/convenience funcs to check for whether mt capable, and to return # of usable threads (total host threads minus some reserved for processing)
	public int getNumUsableThreads() {return numUsableThreads;}
	public ExecutorService getTh_Exec() {return th_exec;}
	
	/**
	 * called at beginning of draw if showing morph colors : check if we need to recalculate distortion due to UI changes
	 */
	public void checkIfMorphAnalysisDone() {	if(!morphStackAnalysisDone) {calculateMorphDistortion();}}
	
	public mapUpdFromUIData getCurrUIVals() {return currUIVals;}
	
	//////////////
	// draw routines
	
	public final void drawMapsAndMorphs(float animTimeMod, int drawMapDetail) {
		pa.pushMatState();
		boolean debug = currUIVals.getIsDebug(), showLbls = currUIVals.getFlag(COTS_MorphWin.drawMap_CntlPtLblsIDX), drawCircles = currUIVals.getFlag(COTS_MorphWin.drawMap_CellCirclesIDX);
		boolean drawMorphMap = currUIVals.getFlag(COTS_MorphWin.drawMorph_MapIDX), drawMorphSlices = currUIVals.getFlag(COTS_MorphWin.drawMorph_SlicesIDX), drawCntlPts = currUIVals.getFlag(COTS_MorphWin.drawMap_CntlPtsIDX);
		boolean drawMap = currUIVals.getFlag(COTS_MorphWin.drawMapIDX), drawMorphCntlPtTraj = currUIVals.getFlag(COTS_MorphWin.drawMorph_CntlPtTrajIDX), drawCopy = currUIVals.getFlag(COTS_MorphWin.drawMap_RegCopyIDX);
		boolean _showDistColors = currUIVals.getFlag(COTS_MorphWin.drawMorph_DistColorsIDX);
		if(_showDistColors) {	checkIfMorphAnalysisDone();	}
		//draw maps with dependenc on wireframe/filled setting
		drawMaps_Main(debug, currUIVals.getFlag(COTS_MorphWin.drawMapIDX), _showDistColors, currUIVals.getFlag(COTS_MorphWin.drawMap_FillOrWfIDX), drawCircles, drawCopy);
		//drawMaps_Aux(boolean drawTexture, boolean drawOrtho, boolean drawEdgeLines) {
		drawMaps_Aux(debug, currUIVals.getFlag(COTS_MorphWin.drawMap_ImageIDX), currUIVals.getFlag(COTS_MorphWin.drawMap_OrthoFrameIDX), currUIVals.getFlag(COTS_MorphWin.drawMap_EdgeLinesIDX), drawCntlPts, drawCopy, showLbls,drawMapDetail);	
		
		if(drawMorphCntlPtTraj) {		drawMorphedMap_CntlPtTraj(drawMapDetail);}		
		
		if(drawMorphMap || drawMorphSlices || drawCircles || currUIVals.getFlag(COTS_MorphWin.drawMorph_CntlPtTrajIDX) || currUIVals.getFlag(COTS_MorphWin.drawMorph_Slices_RtSideInfoIDX)) {		
			drawAndAnimMorph(debug, animTimeMod, drawMap,
					drawMorphMap, _showDistColors, currUIVals.getFlag(COTS_MorphWin.drawMorph_FillOrWfIDX), 
					drawMorphSlices, currUIVals.getFlag(COTS_MorphWin.drawMorph_Slices_FillOrWfIDX), 
					drawCircles, drawCntlPts, currUIVals.getFlag(COTS_MorphWin.sweepMapsIDX), showLbls,drawMapDetail);	
		}
		pa.popMatState();	
	}
	
	/**
	 * draw a point of a particular radius
	 * @param p point to draw
	 * @param rad radius of point
	 */
	public void _drawPt(myPointf _p, float _rad) {
		pa.pushMatState();	
		pa.translate(_p);
		pa.drawSphere(_rad);
		pa.popMatState();	
	}
	
	public void _drawVec(myPointf _p, myPointf _pEnd, int[] _strkClr, float _rad) {
		pa.pushMatState();	
		pa.setStroke(_strkClr[0],_strkClr[1],_strkClr[2],255);
		pa.drawLine(_p.x, _p.y,_p.z,_pEnd.x, _pEnd.y,_pEnd.z);
		pa.translate(_pEnd);
		pa.drawSphere(_rad);
		pa.popMatState();	
	}
	
	
	/**
	 * main map drawing - dependent on wireframe/filled flag
	 * @param fillOrWf
	 * @param drawMap
	 * @param drawCircles
	 * @param drawCopy
	 */
	public final void drawMaps_Main(boolean debug, boolean drawMap, boolean _showDistColors,boolean fillOrWf, boolean drawCircles, boolean drawCopy) {
		if(drawMap) {
			if(fillOrWf) {		
				if(_showDistColors){	for(int i=0;i<maps.length;++i) {maps[i].drawMap_DistColor( currUIVals.getMorphDistMult(), currUIVals.getDistDimToShow());	}}
				else {					for(int i=0;i<maps.length;++i) {maps[i].drawMap_Fill();}}
			} else {					for(int i=0;i<maps.length;++i) {maps[i].drawMap_Wf();}}
		}
		if(drawCircles) {
			if((!drawMap) && (fillOrWf)) {		for(int i=0;i<maps.length;++i) {	maps[i].drawMap_PolyCircles_Fill();	}}		
			else {								for(int i=0;i<maps.length;++i) {	maps[i].drawMap_PolyCircles_Wf();}}					
		}
		if(drawCopy) {			mapRegDistCalc.drawMaps_Main(fillOrWf);}
	}//drawMaps_Main
	
	/**
	 * draw maps with no wireframe/filled dependence
	 * @param drawTexture
	 * @param drawOrtho
	 * @param drawEdgeLines
	 */
	public final void drawMaps_Aux(boolean debug, boolean drawTexture, boolean drawOrtho, boolean drawEdgeLines, boolean drawCntlPts, boolean drawCopy, boolean showLbls, int _detail) {
		if(drawCopy) {			mapRegDistCalc.drawMaps_Aux(drawTexture, drawOrtho, drawCntlPts,showLbls, _detail);}
		if(drawTexture)	{		for(int i=0;i<maps.length;++i) {maps[i].drawMap_Texture();}}
		if(drawOrtho) {			for(int i=0;i<maps.length;++i) {maps[i].drawOrthoFrame();}}
		if(drawEdgeLines) {		maps[0].drawMap_EdgeLines();}
		if(drawCntlPts) {
			int curModMapIDX = (null==currMseModMap ? -1 : currMseModMap.mapIdx);
			for(int i=0;i<maps.length;++i) {maps[i].drawMap_CntlPts(i==curModMapIDX, _detail);}
		}
		for(int i=0;i<maps.length;++i) {	maps[i].drawHeaderAndLabels(showLbls,_detail);}
		
	}//drawMaps_Aux
	
	/**
	 * manage morph display and evolution
	 * @param animTimeMod
	 */
	//protected float morphSign = 1.0f;

	public final void drawAndAnimMorph(boolean debug, float animTimeMod, boolean drawMap, boolean drawMorphMap, boolean _showDistColors, boolean morphMapFillOrWf,  boolean drawSlices, boolean morphSlicesFillOrWf,boolean drawCircles, boolean drawCntlPts, boolean sweepMaps, boolean showLbls, int _detail) {
		//morphs[currMorphTypeIDX].setMorphT(morphProgress);//sets t value and calcs morph
		morphs[currMorphTypeIDX].setMorphT(animators[curAnimatorIDX].getValue());//sets t value and calcs morph
		pa.pushMatState();	
			pa.setFill(0,0,0,255);
			pa.setStroke(0,0,0,255);
			pa.setStrokeWt(1.0f);
			//any instancing-morph-specific data
			morphs[currMorphTypeIDX].drawMorphSpecificValues(debug,drawCntlPts, showLbls);
		pa.popMatState();	
		
		if(drawSlices) {
			morphs[currMorphTypeIDX].drawMorphSlices((!drawMorphMap && _showDistColors), morphSlicesFillOrWf, drawMap, drawCircles, drawCntlPts, showLbls, _detail);			
		}
		if(drawMorphMap) {
			morphs[currMorphTypeIDX].drawCurrMorphedMap(_showDistColors, morphMapFillOrWf, drawMap, drawCircles);	
			if(drawCntlPts){morphs[currMorphTypeIDX].drawMorphedMap_CntlPts(_detail);}
			morphs[currMorphTypeIDX].drawHeaderAndLabels(showLbls,_detail);
		}
		if(sweepMaps) {
			evolveAllAnimators(animTimeMod);
//			morphProgress += (morphSign * (animTimeMod * morphSpeed));			
//			if(morphProgress > 1.0f) {morphProgress = 1.0f;morphSign = -1.0f;} else if (morphProgress < 0.0f) {	morphProgress = 0.0f;	morphSign = 1.0f;}	
			currUIVals.setMorphProgress(animators[curAnimatorIDX].getValue());
		}
		
	}
	
	public final void drawMaps_MorphAnalysisWins(boolean drawTrajAnalysis, boolean drawAnalysisGraphs, String[] mmntDispLabels, int dispDetail, float sideBarYDisp) {
		int alpha = 120;
		int mod=0;
		if(dispDetail < COTS_MorphWin.drawMapDet_CntlPts_COV_IDX) { mod+=2;}
		else if(dispDetail < COTS_MorphWin.drawMapDet_CntlPts_COV_EdgePts_F_IDX){ mod+=1;}
		recalcTrajAnalysisDims(mod);
		pa.pushMatState();	
		pa.setStroke(0,0,0, 255);
		pa.setStrokeWt(2.0f);
		int numWinBarsToDraw = 1;
		pa.translate(0.0f,win.rectDim[3],0.0f);//perTrajAnalysisImageWidth up from bottom
		if(drawAnalysisGraphs) {
			pa.pushMatState();	
			pa.setFill(255, 235,255,alpha);
			pa.translate(0.0f,-perTrajAnalysisImageWidth * numWinBarsToDraw,0.0f);//perTrajAnalysisImageWidth up from bottom
			pa.drawRect(new float[]{trajAnalysisRectDims[0],trajAnalysisRectDims[1],trajAnalysisRectDims[2],trajAnalysisRectDims[3]});	
			morphs[currMorphTypeIDX].drawTrajAnalyzerGraphs(mmntDispLabels,  dispDetail, new float[] {perTrajAnalysisImageWidth,perTrajAnalysisImageWidth,trajAnalysisRectDims[1], sideBarYDisp});
			++numWinBarsToDraw;
			pa.popMatState();
		}
		if(drawTrajAnalysis) {
			pa.pushMatState();	
			pa.setFill(235, 252,255,alpha);
			pa.translate(0.0f,-perTrajAnalysisImageWidth * numWinBarsToDraw,0.0f);//perTrajAnalysisImageWidth up from bottom
			pa.drawRect(new float[]{trajAnalysisRectDims[0],trajAnalysisRectDims[1],trajAnalysisRectDims[2],trajAnalysisRectDims[3]});	
			morphs[currMorphTypeIDX].drawTrajAnalyzerData(mmntDispLabels,  dispDetail, new float[] {perTrajAnalysisImageWidth,perTrajAnalysisImageWidth,trajAnalysisRectDims[1], sideBarYDisp});			
			pa.popMatState();			
		}
		pa.popMatState();
	}

	public final void drawMaps_MrphStackDistAnalysisWins(boolean drawTrajAnalysis, boolean drawAnalysisGraphs, String[] mmntDispLabels, float sideBarYDisp) {
		int alpha = 120;
		recalcMrphStckAnalysisDims(0);
		pa.pushMatState();	
		pa.setStroke(0,0,0, 255);
		pa.setStrokeWt(2.0f);
		int numWinBarsToDraw = 1;
		pa.translate(0.0f,win.rectDim[3],0.0f);
		if(drawAnalysisGraphs) {
			pa.pushMatState();	
			pa.setFill(255, 235,255,alpha);
			pa.translate(0.0f,-perMStckDistAnalysisImageWidth * numWinBarsToDraw,0.0f);//perTrajAnalysisImageWidth up from bottom
			pa.drawRect(new float[]{mStckDistAnalysisRectDims[0],mStckDistAnalysisRectDims[1],mStckDistAnalysisRectDims[2],mStckDistAnalysisRectDims[3]});	
			//mrphStackDistAnalyzer.drawAnalyzerGraphs(pa, mmntDispLabels, new float[] {perMStckDistAnalysisImageWidth,perMStckDistAnalysisImageWidth,mStckDistAnalysisRectDims[1], sideBarYDisp}, "");
			++numWinBarsToDraw;
			pa.popMatState();
		}
		if(drawTrajAnalysis) {
			pa.pushMatState();	
			pa.setFill(235, 252,255,alpha);
			pa.translate(0.0f,-perMStckDistAnalysisImageWidth * numWinBarsToDraw,0.0f);//perTrajAnalysisImageWidth up from bottom
			pa.drawRect(new float[]{mStckDistAnalysisRectDims[0],mStckDistAnalysisRectDims[1],mStckDistAnalysisRectDims[2],mStckDistAnalysisRectDims[3]});
			//mrphStackDistAnalyzer.drawAnalyzerData(pa, mmntDispLabels, new float[] {perMStckDistAnalysisImageWidth,perMStckDistAnalysisImageWidth,mStckDistAnalysisRectDims[1], sideBarYDisp}, "");
			pa.popMatState();			
		}
		pa.popMatState();
	}//drawMaps_MrphStackDistAnalysisWins


	/**
	 * draw oriented frames of map morph, side by side, at bottom of screen - should be called by drawHeader_Priv, which is already 2d
	 * @param fillOrWf
	 */
	public final void drawMaps_LineupFrames(boolean fillOrWf, boolean drawCircles, boolean drawTexture) {
		pa.pushMatState();	
		pa.setStroke(0,0,0, 255);
		pa.setStrokeWt(2.0f);
		pa.setFill(245, 255,232,255);
		pa.translate(0.0f,win.rectDim[3]-perLineupImageWidth,0.0f);
		pa.drawRect(new float[]{lineupRectDims[0],lineupRectDims[1],lineupRectDims[2],lineupRectDims[3]});		
		
		//pa.setFill(new int[] {0, 222,232},255);
		for(Float t : lineUpMorphMaps.keySet()) {
			pa.pushMatState();
				pa.translate(10.0f, 10.0f, 0.0f);
				AppMgr.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Black, 255), 6.0f, "t = " + String.format(Base_PolyMap.strPointDispFrmt6,t));
			pa.popMatState();
			Base_PolyMap tmpMorphMap = lineUpMorphMaps.get(t);

			pa.pushMatState();
				pa.translate(.5f*perLineupImageWidth, .5f*perLineupImageWidth, 0.0f);
				tmpMorphMap.drawMap_LineUp(true, drawCircles, drawTexture, perLineupImageWidth);
				//pa.ellipse(0.0f, 0.0f, .45f*perLineupImageWidth, .45f*perLineupImageWidth);
			pa.popMatState();	
			
			pa.translate(perLineupImageWidth, 0.0f, 0.0f);
			pa.drawLine(0.0f,lineupRectDims[1], 0.0f, 0.0f, perLineupImageWidth+ lineupRectDims[1], 0.0f );
		}			
		pa.popMatState();	
	}
	
	public final void drawMorphedMap_CntlPtTraj(int _detail) {
		morphs[currMorphTypeIDX].drawMorphedMap_CntlPtTraj(_detail);
	}
	
	protected final float _drawRightSideMorphMap(float _yOff, float sideBarYDisp) {
		pa.translate(-10.0f, sideBarYDisp, 0.0f);	
		AppMgr.showOffsetText(0,IRenderInterface.gui_Cyan, "Current Morph Map : ");
		pa.translate(10.0f, sideBarYDisp, 0.0f);					
		_yOff = morphs[currMorphTypeIDX].drawMapRtSdMenuDescr(_yOff, sideBarYDisp);
		pa.translate(-10.0f, 0.0f, 0.0f);
		return _yOff;
	}
	protected final float _drawRightSideMorphSlices(float _yOff, float sideBarYDisp) {
		pa.translate(-10.0f, sideBarYDisp, 0.0f);	
		AppMgr.showOffsetText(0,IRenderInterface.gui_Cyan, "Morph Slices : ");
		pa.translate(10.0f, sideBarYDisp, 0.0f);					
		_yOff = morphs[currMorphTypeIDX].drawMorphSliceRtSdMenuDescr(_yOff, sideBarYDisp);
		pa.translate(-10.0f, 0.0f, 0.0f);
		return _yOff;
	}
	
	public final float drawRightSideMaps(float _yOff, float sideBarYDisp, boolean _showDistClrs, boolean drawRegCopy, boolean drawMorph,  boolean drawMorphSlicesRtSideInfo) {
		AppMgr.showOffsetText(0,IRenderInterface.gui_Cyan,  "Current Maps : " + mapTypes[mapType] + " Maps : ");
		
		pa.translate(10.0f, sideBarYDisp, 0.0f);		
		for(int i=0; i<maps.length;++i) {			_yOff = maps[i].drawRtSdMenuDescr(_yOff, sideBarYDisp, true, true);		}
		if(drawRegCopy) {_yOff = mapRegDistCalc.drawRightSideMaps(_yOff, sideBarYDisp);}
		//if(drawRegCopy) {_yOff = copyMap.drawRtSdMenuDescr(_yOff, sideBarYDisp, true, true);}
		if(drawMorph) {			_yOff = _drawRightSideMorphMap(_yOff, sideBarYDisp);	}		
		pa.translate(-10.0f, sideBarYDisp, 0.0f);	
		if(_showDistClrs) {
			AppMgr.showOffsetText(0,IRenderInterface.gui_Cyan, "Current Distortion Mins/Maxs : ");
			pa.translate(10.0f, sideBarYDisp, 0.0f);	
			_yOff = morphs[currMorphTypeIDX].drawDistortionRtSideMenuMinMax(_yOff, sideBarYDisp,  currUIVals.getDistDimToShow());
			pa.translate(-10.0f, sideBarYDisp, 0.0f);	
		}
		AppMgr.showOffsetText(0,IRenderInterface.gui_Cyan, "Current Morph : ");
		pa.translate(10.0f, sideBarYDisp, 0.0f);		
		//_yOff += sideBarYDisp;
		AppMgr.showOffsetText(0,IRenderInterface.gui_Green, morphs[currMorphTypeIDX].morphTitle + " Morph : ");
		//_yOff += sideBarYDisp;
		pa.translate(10.0f, sideBarYDisp, 0.0f);
		_yOff = morphs[currMorphTypeIDX].drawMorphRtSdMenuDescr(_yOff, sideBarYDisp,morphSpeed);
		if(drawMorphSlicesRtSideInfo) {	_yOff = _drawRightSideMorphSlices(_yOff, sideBarYDisp);}

		return _yOff;
	}	
	/**
	 * draw current morph values on right side menu/display
	 * @param _yOff
	 * @return
	 */	
	protected final float drawCurrentMorph(float _yOff, float sideBarYDisp, float morphSpeed) {// , String[] morphScopes) {
		_yOff = morphs[currMorphTypeIDX].drawMorphTitle(_yOff, sideBarYDisp);
		pa.translate(10.0f,0.0f,0.0f);
		_yOff = morphs[currMorphTypeIDX].drawMorphRtSdMenuDescr(_yOff, sideBarYDisp, morphSpeed);//,morphScopes);
		return _yOff;
	}
	
	///////////////
	// setters/getters
	/**
	 * set up dimensions for lineup window and traj analysis window
	 */
	public final void setPopUpWins_RectDims() {
		//
		lineupRectDims = win.getOrientedDims();
		perLineupImageWidth = lineupRectDims[2]/(1.0f*currUIVals.getNumLineupFrames());
		lineupRectDims[3] = perLineupImageWidth;
		//traj analysis window
		recalcTrajAnalysisDims(0);
		//morph stack distortion analysis
		recalcMrphStckAnalysisDims(0);

	}
	
	private final void recalcTrajAnalysisDims(int mod) {
		//traj analysis window
		trajAnalysisRectDims = win.getOrientedDims();
		perTrajAnalysisImageWidth = trajAnalysisRectDims[2]/(1.0f* morphs[currMorphTypeIDX].getNumAnalysisBoxes() - mod);
		trajAnalysisRectDims[3] = perTrajAnalysisImageWidth;
	}
	
	private final void recalcMrphStckAnalysisDims(int mod) {
		mStckDistAnalysisRectDims = win.getOrientedDims();
		perMStckDistAnalysisImageWidth = mStckDistAnalysisRectDims[2]/5.0f;
		mStckDistAnalysisRectDims[3] = perMStckDistAnalysisImageWidth;
	}
	
	public final void setFromAndToCopyIDXs(int _fromIdx, int _toIdx) {	fromMapIDX = _fromIdx;		toMapIDX = _toIdx;	}
	
	/**
	 * this will reset branching on all maps that use branching
	 */
	public final void resetAllBranching() {
		for (int i=0;i<maps.length;++i) {	maps[i].setFlags(new boolean[] {true});}
		for (int i=0;i<morphs.length;++i) {morphs[i].resetAllBranching();}		
	}//resetAllBranching
	
	public final void resetIndivMapBranching(int idx) {
		maps[idx].setFlags(new boolean[] {true});
	}//resetIndivMapBranching
	
	public final void updateMapMorphVals_FromUI(mapUpdFromUIData upd) {
		boolean shouldRecalcMorphAnalysis=  upd.uiForMorphDistAnalysisChanged(currUIVals);
		currUIVals.setAllVals(upd);//can't use the same mapUpdFromUIData everywhere because we compare differences
		currMorphTypeIDX = currUIVals.getCurrMorphTypeIDX();
		
		curAnimatorIDX = currUIVals.getCurrAnimatorIDX();
		
		setAllAnimatorVals(currUIVals.getMorphProgress(), currUIVals.getCurMorphAnimTypeIDX());
		
		morphSpeed = currUIVals.getMorphSpeed(); 
		if(shouldRecalcMorphAnalysis) {
			//msgObj.dispInfoMessage("mapPairManager::"+this.name, "updateMapValsFromUI", "UI Update forces morph stack analysis recalc");
			morphStackAnalysisDone = false;		//set to recalculate on draw, if ui elements have changed that impact this
		} 
		else {
			//msgObj.dispInfoMessage("mapPairManager::"+this.name, "updateMapValsFromUI", "UI Update does not force morph stack analysis recalc");
		}
		setPopUpWins_RectDims();
		for(int i=0;i<this.morphs.length;++i) {	morphs[i].updateMorphVals_FromUI(upd);}
		
		for(int i=0;i<this.distMsrMorphs.length;++i) {distMsrMorphs[i].updateMorphVals_FromUI(upd);}
		for(int j=0;j<maps.length;++j) {	maps[j].updateMapVals_FromUI(currUIVals);}
		morphs[currMorphTypeIDX].mapCalcsAfterCntlPointsSet(name + "::updateMapValsFromUI", true, true);
	}//updateMapMorphVals_FromUI
	
	/**
	 * set all animators to passed value
	 * @param _t morph progress/time
	 * @param _animBehaviorIDX type of animation behavior to engage in (ping-pong, ping-pong w/stop, etc) see base_Math_Objects.interpolants.InterpolantBehavior
	 */
	private void setAllAnimatorVals(float _t, int _animBehaviorIDX) {		for(int i=0;i<animators.length;++i) {	animators[i].setValue(_t); animators[i].setAnimBehavior(_animBehaviorIDX);}	 	}
	/**
	 * evolve all animators from draw function
	 * @param animTimeMod
	 */
	private void evolveAllAnimators(float animTimeMod) {
		float delta = animTimeMod * morphSpeed;
		for(int i=0;i<animators.length;++i) {	animators[i].evolveInterpolant(delta);}
	}
		
	/**
	 * this will return true if the current morph type uses registration - currently only CarrierSimRegTransIDX
	 * @return
	 */
	public final boolean checkCurrMorphUsesReg() {		return AffineMorphIDX==currMorphTypeIDX;	}	
	
	////////////////////
	// mouse/keyboard ui interaction 
	
	/**
	 * reset corners of all maps
	 */	
	public final void resetAllMapCorners() {
		for(int j=0;j<maps.length;++j) {		maps[j].resetCntlPts(bndPts[j]);	}	
		for (int i=0;i<morphs.length;++i) {		morphs[i].resetAllBranching();}		
		morphs[currMorphTypeIDX].mapCalcsAfterCntlPointsSet(name + "::resetAllMapCorners", true, true);		
		morphStackAnalysisDone = false;
	}	
	/**
	 * use this to record state of current map corners
	 * @return
	 */
	public final myPointf[][] getCurrMapCrnrsAsResetCrnrrs() {
		myPointf[][] res = new myPointf[2][];
		for(int i=0;i<res.length;++i) {
			res[i] = new myPointf[4];
			myPointf[] mapICntlPts = maps[i].getCntlPts_Copy();
			for(int j=0;j<mapICntlPts.length;++j) {				res[i][j]=mapICntlPts[j];		}
			if(mapICntlPts.length < res[i].length) {		//if not a quad
				for(int j=mapICntlPts.length; j<res[i].length;++j) {res[i][j] = new myPointf(origBndPts[i][j]);}
			}
		}
		return res;
	}
	
	/**
	 * reset all instances of either "floor"/A or "ceiling"/B map
	 * @param mapIDX
	 */
	public final void resetMapCorners(int mapIDX) {	
		if(mapIDX==0) {for (int i=0;i<morphs.length;++i) {morphs[i].resetCurMorphBranching();}		}//if map A is reset then branching needs to be reset as well for morphs
		maps[mapIDX].resetCntlPts(bndPts[mapIDX]);morphs[currMorphTypeIDX].mapCalcsAfterCntlPointsSet(name + "::resetMapCorners", true, true);
		morphStackAnalysisDone = false;
	}
	
	/**
	 * match map destIDX corners to map srcIDX's corners
	 */
	public final void matchAllMapCorners(int srcIDX, int destIDX) {
		myPointf[] rawPts0 = maps[srcIDX].getCntlPts(), newPts = new myPointf[rawPts0.length];
		for(int j=0;j<rawPts0.length;++j) {	newPts[j] = myPointf._add(myPointf._sub(rawPts0[j], bndPts[srcIDX][j]), bndPts[destIDX][j]);}			
		maps[destIDX].resetCntlPts(newPts);	
		morphs[currMorphTypeIDX].mapCalcsAfterCntlPointsSet(name + "::matchAllMapCorners", true, true);
		morphStackAnalysisDone = false;
	}//matchAllMapCorners	

	//minimum distance for click to be considered within range of control point
	public static final float minSqClickDist = 2500.0f;
	
	/**
	 * determine whether a relevant mouse click has occurred in the maps this mgr manages
	 * @param mouseX
	 * @param mouseY
	 * @param mseClckInWorld
	 * @param mseBtn
	 * @param keyPressed
	 * @return
	 */
	public final boolean hndlMouseClickInMaps(int mouseX, int mouseY, myPoint mseClckInWorld, int mseBtn, char keyPressed) {
		//check every map for closest control corner to click location
		TreeMap<Float,Base_PolyMap>  mapDists = new TreeMap<Float,Base_PolyMap>();
		//msgObj.dispInfoMessage("COTS_Morph3DWin", "hndlMouseClickIndiv", "Mouse button pressed : " + mseBtn + " Key Pressed : " + keyPressed + " Key Coded : " + keyCodePressed);
		//get a point on ray through mouse location in world
		myPointf _rayOrigin = AppMgr.getMseLoc_f();
		myVectorf _rayDir = AppMgr.getEyeToMouseRay_f();
		myPointf mseLocInWorld_f = win.getMouseClkPtInWorld(mseClckInWorld,mouseX,mouseY);
		
		for(int j=0;j<maps.length;++j) {	
			mapDists.put(maps[j].findClosestCntlPt(mseLocInWorld_f, _rayOrigin, _rayDir), maps[j]);
		}
		Float minSqDist = mapDists.firstKey();
		if((minSqDist < minSqClickDist) || (keyPressed=='s') || (keyPressed=='S')  || (keyPressed=='r') || (keyPressed=='R')|| (keyPressed=='t') || (keyPressed=='T'))  {
			currMseModMap = mapDists.get(minSqDist);
			return true;
		}
		currMseModMap = null;
		return false;
	}
	
	
	/**
	 * handle mouse drag on map
	 * @param defVec
	 * @param mseClickIn3D_f
	 * @param key
	 * @param keyCode
	 */
	public final boolean mseDragInMap(myVectorf defVec, myPointf mseClickIn3D_f,  char key, int keyCode) {	
		if(currMseModMap == null) {return false;}
		boolean isScale = (key=='s') || (key=='S'), isRotation = (key=='r') || (key=='R'), isTranslation = (key=='t')||(key=='T');
		boolean performFinalIndiv = true;
		if(isScale || isRotation || isTranslation) {
			if(isScale) {								currMseModMap.dilateMap_MseDrag(defVec);			} 
			else if(isRotation) {						currMseModMap.rotateMapInPlane_MseDrag(mseClickIn3D_f, defVec);		}	//isRotation
			else if(isTranslation) {					currMseModMap.moveMapInPlane(defVec);}
		} else {							//cntl point movement			
			performFinalIndiv = currMseModMap.mseDragPickedCntlPt(defVec);
		}
		if(performFinalIndiv) {		//some editing happened, so finalize
			currMseModMap.mseDragInMap_Post(defVec,mseClickIn3D_f,isScale, isRotation, isTranslation, key, keyCode);	
			morphs[currMorphTypeIDX].mapCalcsAfterCntlPointsSet(name + "::mseDragInMap", false, ( currUIVals.getFlag(COTS_MorphWin.drawMorph_MapIDX)));
		}
		return performFinalIndiv;
	}//mseDragInMap
	
	
	public final void hndlMouseRelIndiv() {
		for(int i=0;i<maps.length;++i) {	maps[i].mseRelease();}
		if(currMseModMap != null) {
			morphStackAnalysisDone = false;	//any changes to either mouse map require distortion to be recalculated
			morphs[currMorphTypeIDX].mapCalcsAfterCntlPointsSet(name + "::hndlMouseRelIndiv", true, true);
			currMseModMap = null;
		}
	}

	public void hndlMouseRel_Indiv() {
		// TODO Auto-generated method stub
		
	}	
	

}//class mapManager
