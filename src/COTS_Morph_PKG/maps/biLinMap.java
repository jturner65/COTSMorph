package COTS_Morph_PKG.maps;

import COTS_Morph_PKG.maps.base.baseMap;
import base_Utils_Objects.vectorObjs.myPointf;

public class biLinMap extends baseMap {

	public biLinMap(myPointf[] _cntlPts, int _mapIdx) {super(_cntlPts,_mapIdx);}

	@Override
	public myPointf calcMapPt(float tx, float ty) {	return new myPointf(new myPointf(cntlPts[0],tx,cntlPts[1]), ty, new myPointf(cntlPts[2],tx,cntlPts[3]));}
	
}
