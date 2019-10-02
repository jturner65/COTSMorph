package COTS_Morph_PKG.analysis.map;


import COTS_Morph_PKG.map.base.baseMap;
import COTS_Morph_PKG.mapManager.mapPairManager;
import base_UI_Objects.my_procApplet;
import base_Utils_Objects.vectorObjs.myPointf;

/**
 * this class will take a map and analyze its values (i.e. distortion)
 * @author john
 *
 */
public abstract class baseMapAnalyzer {
	public final int ID;
	protected static int idGen = 0;
	protected my_procApplet pa;
	protected baseMap ownrMap;
	protected mapPairManager mapMgr;
	

	public baseMapAnalyzer(baseMap _ownrMap) {
		ID = idGen++;
		ownrMap=_ownrMap; pa=baseMap.pa; mapMgr = ownrMap.getMapManager();
	}
	
	/**
	 * find the square distance between two maps
	 * @param aMap
	 * @param bMap
	 * @return
	 */
	public float findSqDistBetween2MapVerts(baseMap aMap, baseMap bMap) {
		float res = 0.0f;
		myPointf[] aCntlPts = aMap.getCntlPts(), bCntlPts = bMap.getCntlPts();
		for(int i=0;i<aCntlPts.length;++i) {res += myPointf._SqrDist(aCntlPts[i], bCntlPts[i]);}
		return res;
	}

	

}//class baseMapAnalyzer
