package COTS_Morph_PKG.utils;

import java.util.TreeMap;

import COTS_Morph_PKG.mapManager.mapPairManager;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import base_UI_Objects.windowUI.base.base_UpdateFromUIData;

/**
 * structure holding UI-derived/modified data used to update maps
 * @author john
 *
 */
public class mapUpdFromUIData extends base_UpdateFromUIData {

	/**
	 * 
	 * @param ints : idx 0 : numCellsPerSide; idx 1 : branchSharingStrategy
	 * @param bools : idx 0 : forceUpdate;
	 */
	public mapUpdFromUIData(COTS_MorphWin _win) {super(_win);}
	public mapUpdFromUIData(COTS_MorphWin _win, TreeMap<Integer, Integer> _iVals, TreeMap<Integer, Float> _fVals,TreeMap<Integer, Boolean> _bVals) {
		super(_win,_iVals,_fVals,_bVals);
	}
	
	public mapUpdFromUIData(mapUpdFromUIData _otr) {
		super(_otr);
	}
	
	/**
	 * access app-specific ints
	 */
	public int getNumCellsPerSide() {return intValues.get(COTS_MorphWin.gIDX_NumCellsPerSide);}
	public int getBranchSharingStrategy() {return intValues.get(COTS_MorphWin.gIDX_SetBrnchStrat);}
	public int getNumLineupFrames() {return intValues.get(COTS_MorphWin.gIDX_NumLineupFrames);}	
	public int getNumMorphSlices() {return intValues.get(COTS_MorphWin.gIDX_NumMorphSlices);}	
	
	public int getCurrMorphTypeIDX() {return intValues.get(COTS_MorphWin.gIDX_MorphType);}
	public int getCurrMorphType_OrientIDX() {return intValues.get(COTS_MorphWin.gIDX_MorphTypeOrient);}
	public int getCurrMorphType_SizeIDX() {return intValues.get(COTS_MorphWin.gIDX_MorphTypeSize);}
	public int getCurrMorphType_ShapeIDX() {return intValues.get(COTS_MorphWin.gIDX_MorphTypeShape);}
	public int getCurrMorphType_COVPathIDX() {return intValues.get(COTS_MorphWin.gIDX_MorphTypeCOVPath);}
	
	public int getCurrDistTransformIDX() {return intValues.get(COTS_MorphWin.gIDX_DistTestTransform);}
	public int getDistDimToShow() {return intValues.get(COTS_MorphWin.gIDX_DistDimToShow);}
	
	public int getCurrAnimatorIDX() {return intValues.get(COTS_MorphWin.gIDX_MorphTValType);}
	public int getCurMorphSliceAraIDX() {return intValues.get(COTS_MorphWin.gIDX_MorphSliceDispType);}
	
	public int getCurMorphAnimTypeIDX() {return intValues.get(COTS_MorphWin.gIDX_MorphAnimType);}
	//public int getMorphSliceAraForDistIDX() {return intValues.get(COTS_MorphWin.gIDX_MorphSliceTypeForDist);}
	/**
	 * 
	 * access bools
	 */
	public boolean forceUpdate() {return false;}//TODO update this if desired to have UI able to force map to update cntl point values;  boolValues.get(COTS_MorphWin.<flag idx>);}
	
	/**
	 * check if any UI values have changed that should force a distortion recalculation
	 * @param _otr
	 * @return
	 */
	public boolean uiForMorphDistAnalysisChanged(mapUpdFromUIData _otr) {
		
		boolean checkCmpnd = false;
		if((getCurrMorphTypeIDX() == mapPairManager.CompoundMorphIDX)) {
			checkCmpnd = ((getCurrMorphType_OrientIDX() != _otr.getCurrMorphType_OrientIDX()) || 
					(getCurrMorphType_SizeIDX() != _otr.getCurrMorphType_SizeIDX()) || 
					(getCurrMorphType_ShapeIDX() != _otr.getCurrMorphType_ShapeIDX()) || 
					(getCurrMorphType_COVPathIDX() != _otr.getCurrMorphType_COVPathIDX()));
		}
		
		return ((getNumCellsPerSide() != _otr.getNumCellsPerSide()) 
				|| (getNumMorphSlices() != _otr.getNumMorphSlices()) 
				|| (getCurrMorphTypeIDX() != _otr.getCurrMorphTypeIDX()) 
				|| (getCurMorphAnimTypeIDX() != _otr.getCurMorphAnimTypeIDX()) 
				|| checkCmpnd				
				|| (getCurrAnimatorIDX() != _otr.getCurrAnimatorIDX())
				|| (getCurMorphSliceAraIDX() != _otr.getCurMorphSliceAraIDX())
				|| (getCurrDistTransformIDX() != _otr.getCurrDistTransformIDX())) ;		
	}
	
	/**
	 * access floats
	 */
	
	public float getMorphProgress() {return floatValues.get(COTS_MorphWin.gIDX_MorphTVal);}
	public float getMorphSpeed() {return floatValues.get(COTS_MorphWin.gIDX_MorphSpeed);}
	public float getMorphDistMult() {return floatValues.get(COTS_MorphWin.gIDX_MorphDistMult);}
	/**
	 * set morph progress and update win ui object from morph object
	 * @param _prog
	 */
	public void setMorphProgress(float _prog) {floatValues.put(COTS_MorphWin.gIDX_MorphTVal,_prog);	win.updateFloatValFromExecCode(COTS_MorphWin.gIDX_MorphTVal,_prog);}
	/**
	 * set morph speed and update win ui object from morph object
	 * @param _prog
	 */
	public void setMorphSpeed(float _speed) {floatValues.put(COTS_MorphWin.gIDX_MorphSpeed,_speed);	win.updateFloatValFromExecCode(COTS_MorphWin.gIDX_MorphSpeed,_speed);		}


}//class mapUpdateFromUIData
