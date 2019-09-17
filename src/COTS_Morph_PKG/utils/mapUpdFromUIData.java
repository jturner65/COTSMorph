package COTS_Morph_PKG.utils;

import java.util.TreeMap;

import COTS_Morph_PKG.ui.base.COTS_MorphWin;

/**
 * structure holding UI-derived/modified data used to update maps
 * @author john
 *
 */
public class mapUpdFromUIData {
	private final COTS_MorphWin win;
	/**
	 * map to hold UI-driven int values, using the UI object idx's as keys
	 */
	private TreeMap<Integer, Integer> intValues;
	/**
	 * map to hold UI-driven int values, using the UI object idx's as keys
	 */
	private TreeMap<Integer, String> strValues;
	/**
	 * map to hold UI-driven int values, using the UI object idx's as keys
	 */
	private TreeMap<Integer, Float> floatValues;
	/**
	 * map to hold UI-driven boolean values, using the UI object flags' idx's as keys 
	 */
	private TreeMap<Integer, Boolean> boolValues;

	/**
	 * 
	 * @param ints : idx 0 : numCellsPerSide; idx 1 : branchSharingStrategy
	 * @param bools : idx 0 : forceUpdate;
	 */
	public mapUpdFromUIData(COTS_MorphWin _win) {	win=_win;	initMaps();}
	public mapUpdFromUIData(COTS_MorphWin _win, TreeMap<Integer, Integer> _iVals, TreeMap<Integer, String> _sVals, TreeMap<Integer, Float> _fVals,TreeMap<Integer, Boolean> _bVals) {
		win=_win;
		initMaps();
		setAllVals(_iVals, _sVals, _fVals, _bVals);
	}
	
	public mapUpdFromUIData(mapUpdFromUIData _otr) {
		win=_otr.win;
		initMaps();
		setAllVals(_otr.intValues,_otr.strValues,_otr.floatValues,_otr.boolValues);
	}
	
	private void initMaps() {
		intValues = new TreeMap<Integer, Integer>();
		strValues = new TreeMap<Integer, String>();
		floatValues = new TreeMap<Integer, Float>(); 
		boolValues = new TreeMap<Integer, Boolean>();
	}
	
	public void setAllVals(mapUpdFromUIData _otr) {setAllVals(_otr.intValues,_otr.strValues,_otr.floatValues,_otr.boolValues);}
	public void setAllVals(TreeMap<Integer, Integer> _intValues, TreeMap<Integer, String> _strValues, TreeMap<Integer, Float> _floatValues,TreeMap<Integer, Boolean> _boolValues) {
		for(Integer key : _intValues.keySet()) {intValues.put(key, _intValues.get(key));}
		for(Integer key : _strValues.keySet()) {strValues.put(key, _strValues.get(key));}
		for(Integer key : _floatValues.keySet()) {floatValues.put(key, _floatValues.get(key));}
		for(Integer key : _boolValues.keySet()) {boolValues.put(key, _boolValues.get(key));}
	}

	
	public boolean compareIntValue(Integer idx, Integer value) {	return (intValues.get(idx) != null) && (intValues.get(idx).equals(value));	}
	public boolean compareStringValue(Integer idx, String value) {	return (strValues.get(idx) != null) && (strValues.get(idx).equals(value));	}
	public boolean compareFloatValue(Integer idx, Float value) {	return (floatValues.get(idx) != null) && (floatValues.get(idx).equals(value));	}
	public boolean compareBoolValue(Integer idx, Boolean value) {	return (boolValues.get(idx) != null) && (boolValues.get(idx).equals(value));	}
	
	public void setIntValue(Integer idx, Integer value){	intValues.put(idx,value);	}
	public void setStringValue(Integer idx, String value){	strValues.put(idx,value);	}
	public void setFloatValue(Integer idx, Float value){	floatValues.put(idx,value);	}
	public void setBoolValue(Integer idx, Boolean value){	boolValues.put(idx,value);	}
	
	/**
	 * access ints
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
	
	/**
	 * access bools
	 */
	public boolean forceUpdate() {return false;}//TODO update this if desired to have UI able to force map to update cntl point values;  boolValues.get(COTS_MorphWin.<flag idx>);}
	
	/**
	 * access floats
	 */
	public float getMorphProgress() {return floatValues.get(COTS_MorphWin.gIDX_MorphTVal);}
	public float getMorphSpeed() {return floatValues.get(COTS_MorphWin.gIDX_MorphSpeed);}
	/**
	 * set morph progress and update win ui object
	 * @param _prog
	 */
	public void setMorphProgress(float _prog) {floatValues.put(COTS_MorphWin.gIDX_MorphTVal,_prog);	win.setUIObj_FloatVals(COTS_MorphWin.gIDX_MorphTVal,_prog);			}
	/**
	 * set morph speed and update win ui object
	 * @param _prog
	 */
	public void setMorphSpeed(float _speed) {floatValues.put(COTS_MorphWin.gIDX_MorphSpeed,_speed);	win.setUIObj_FloatVals(COTS_MorphWin.gIDX_MorphTVal,_speed);		}

}//class mapUpdateFromUIData
