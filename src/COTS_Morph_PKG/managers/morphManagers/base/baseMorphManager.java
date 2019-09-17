package COTS_Morph_PKG.managers.morphManagers.base;

import java.util.TreeMap;

import COTS_Morph_PKG.managers.mapManagers.mapPairManager;
import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.morphs.CarrierSimDiagMorph;
import COTS_Morph_PKG.morphs.CarrierSimTransformMorph;
import COTS_Morph_PKG.morphs.DualCarrierSimMorph;
import COTS_Morph_PKG.morphs.LERPMorph;
import COTS_Morph_PKG.morphs.LogPolarMorph;
import COTS_Morph_PKG.morphs.QuadKeyEdgeSpiralMorph;
import COTS_Morph_PKG.morphs.base.baseMorph;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_UI_Objects.my_procApplet;
import base_UI_Objects.windowUI.myDispWindow;
/**
 * class to manage the execution of morph objects
 * @author john
 *
 */
public abstract class baseMorphManager {
	/**
	 * map used to represent current status of morph
	 */
	public final baseMap[] morphMaps;
	/**
	 * current ui values describing features of the map
	 */
	protected mapUpdFromUIData currUIVals;
	/**
	 * types of morphs supported
	 */
	public static final String[] morphTypes = new String[] {
		"LERP",						//linearly interpolate control points
		"Carrier Sim using Diagonal",
		"Carrier Sim w/Transformation",
		"Dual Carrier Similarity",
		"Key edge->Key edge Spiral",
		"Log Polar"			
	};
	
	//need an index per morph type
	public static final int
		LERPMorphIDX			= 0,
		CarrierSimDiagIDX		= 1,
		CarrierSimRegTransIDX	= 2,
		DualCarrierSimIDX		= 3,
		QuadSpiralEdgeIDS		= 4,
		LogPolarMorphIDX 		= 5;
	
	/**
	 * array holding morphs
	 */
	protected baseMorph[][] morphs;
	/**
	 * this is the index of the current type of morph - this is the 2nd index in the morphs array
	 */
	protected int currMorphTypeIDX;

	/**
	 * this map holds a sequence of baseMaps keyed by time, oriented to frame A, to be displayed in the lineup
	 */
	protected TreeMap<Float, baseMap>[] lineUpMorphMaps;
	
	public static my_procApplet pa;
	public COTS_MorphWin win;
	//owning map manager
	public mapPairManager mapMgr;
	//CURRENTLY NOT BEING USED
	public baseMorphManager(COTS_MorphWin _win, mapPairManager _mapMgr, int _numSimultaneousMorphs) {
		win=_win; pa=myDispWindow.pa;mapMgr=_mapMgr;
		currUIVals = new mapUpdFromUIData(mapMgr.currUIVals);
		morphs = new baseMorph[_numSimultaneousMorphs][];
		morphMaps = new baseMap[_numSimultaneousMorphs];
		lineUpMorphMaps = new TreeMap[_numSimultaneousMorphs];
		for(int i=0;i<morphs.length;++i) {
			morphs[i]=_initMorphsAra();
			morphMaps[i] = mapMgr.buildCopyMapOfPassedMapType(mapMgr.maps[0], mapMgr.maps[0].mapTitle+"_Morph_"+i);
			lineUpMorphMaps[i] = new TreeMap<Float, baseMap>();
		}
		System.out.println("Currently not being used anywhere!");		
	}
	
	/**
	 * initialize all morphs - only call once
	 */
	private baseMorph[] _initMorphsAra() {
		baseMorph[] tmpMorphs = new baseMorph[morphTypes.length];
		tmpMorphs[LERPMorphIDX] = new LERPMorph(win,this, mapMgr, morphTypes[LERPMorphIDX]); 
		tmpMorphs[CarrierSimDiagIDX] = new CarrierSimDiagMorph(win,this,mapMgr, morphTypes[CarrierSimDiagIDX]); 		
		tmpMorphs[CarrierSimRegTransIDX] = new CarrierSimTransformMorph(win,this,mapMgr, morphTypes[CarrierSimRegTransIDX]); 
		tmpMorphs[DualCarrierSimIDX] = new DualCarrierSimMorph(win,this,mapMgr, morphTypes[DualCarrierSimIDX]); 
		tmpMorphs[QuadSpiralEdgeIDS] = new QuadKeyEdgeSpiralMorph(win,this,mapMgr,morphTypes[DualCarrierSimIDX]); 
		tmpMorphs[LogPolarMorphIDX] = new LogPolarMorph(win,this, mapMgr, morphTypes[LogPolarMorphIDX]);	
		return tmpMorphs;
	}
	

	/**
	 * build oriented lineup of specific # of frames (default 5) where each frame is registered to keyframe A, and then displayed side-by-side
	 */
	public void buildOrientedLineup() {
		for(int i=0;i<morphs.length;++i) {
			TreeMap<Float, baseMap> rawMorphMaps = morphs[i][currMorphTypeIDX].buildLineupOfFrames(currUIVals.getNumLineupFrames()); 
			//win.getMsgObj().dispInfoMessage("mapManager::"+name, "buildOrientedLineup", "# of morph maps: " + rawMorphMaps.size() + " lineup num requested :" +currUIVals.numLineupFrames + " maps types : " + maps[0].mapTitle+" | " + maps[1].mapTitle);
			lineUpMorphMaps[i].clear();
			for(Float t : rawMorphMaps.keySet()) {
				baseMap tmpMorphMap = rawMorphMaps.get(t);
				lineUpMorphMaps[i].put(t, mapMgr.calcDifferenceBetweenMaps(tmpMorphMap, mapMgr.maps[0]));
			}		
		}
	}//buildOrientedLineup
	
	
	public final void updateMapValsFromUI(mapUpdFromUIData upd) {
		currUIVals.setAllVals(upd);
		currMorphTypeIDX = currUIVals.getCurrMorphTypeIDX();

		for(int i=0;i<morphMaps.length;++i) {morphMaps[i].updateMapVals_FromUI(currUIVals);}
	}
	
	public final void resetAllMapCorners() {
		for(int i=0;i<morphMaps.length;++i) {morphMaps[i].resetCntlPts(mapMgr.bndPts[0]);}
	}
	
	public final void hndlMouseRelIndiv() {
		for(int i=0;i<morphMaps.length;++i) {morphMaps[i].mseRelease();}
	}
}//class baseMorphManager
