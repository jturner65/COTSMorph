package COTS_Morph_PKG.managers.morphManagers;

import COTS_Morph_PKG.managers.mapManagers.mapPairManager;
import COTS_Morph_PKG.managers.morphManagers.base.baseMorphManager;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;

/**
 * class to manage a single morph to be used across all features of the animation
 * @author john
 *
 */
public class GlobalMorphMgr extends baseMorphManager {

	public GlobalMorphMgr(COTS_MorphWin _win, mapPairManager _mapMgr) {
		super(_win, _mapMgr, 1);
		
	}

}//class GlobalMorphMgr
