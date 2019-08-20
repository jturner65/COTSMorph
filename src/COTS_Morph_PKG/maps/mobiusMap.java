package COTS_Morph_PKG.maps;

import COTS_Morph_PKG.maps.base.baseMap;
import base_UI_Objects.my_procApplet;
import base_Utils_Objects.vectorObjs.myPointf;

public class mobiusMap extends baseMap {

	public mobiusMap(myPointf[] _cntlPts, int _mapIdx, int[][] _pClrs, int _numCellPerSide) {	super(_cntlPts, _mapIdx, _pClrs,_numCellPerSide, "Mobius Map");}
	/**
	 * Instance-class specific initialization
	 */	
	protected final void updateCntlPtVals(boolean reset) {
		
		
	}

	@Override
	public myPointf calcMapPt(float tx, float ty) {
		// TODO Auto-generated method stub
		return new myPointf();
	}
	/**
	 * instance-specific point drawing
	 * @param pa
	 */
	@Override
	protected void _drawPoints_Indiv(my_procApplet pa) {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void mseRelease_Indiv() {	}
	@Override
	protected boolean updateMapVals_Indiv(boolean hasBeenUpdated) {
		// TODO Auto-generated method stub
		return hasBeenUpdated;
	}


}
