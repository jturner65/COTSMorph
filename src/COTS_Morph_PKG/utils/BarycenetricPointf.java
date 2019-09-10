package COTS_Morph_PKG.utils;

import COTS_Morph_PKG.maps.base.baseMap;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

/**
 * manages the calculation of barycentric coordinates for a point
 * @author john
 *
 */
public class BarycenetricPointf extends myPointf {
	public baseMap ownr;
	/**
	 * normalized bary coords of point within first 3 cntl points of ownr map
	 */
	public float[] pointNBC;

	public BarycenetricPointf (baseMap _ownr){ 										super(0,0,0);		ownr = _ownr;pointNBC = calcNormBaryCoords();}	
	public BarycenetricPointf (baseMap _ownr, myPointf _pt) {						super(_pt);			ownr = _ownr;pointNBC = calcNormBaryCoords();}	
	public BarycenetricPointf (baseMap _ownr, float _x, float _y, float _z) {		super(_x,_y,_z);	ownr = _ownr;pointNBC = calcNormBaryCoords();}
	public BarycenetricPointf (baseMap _ownr, double _x, double _y, double _z){		super(_x,_y,_z);	ownr = _ownr;pointNBC = calcNormBaryCoords();}
	public BarycenetricPointf (baseMap _ownr, myPointf A, myVectorf B) {			super(A,B);			ownr = _ownr;pointNBC = calcNormBaryCoords();}
	public BarycenetricPointf (baseMap _ownr, myPointf A, float s, myPointf B) {	super(A,s,B);		ownr = _ownr;pointNBC = calcNormBaryCoords();}
	
	/**
	 * update point when it's control points have been changed in any way
	 */
	public final void udpatePoint() {	set(calcPointFromNormBaryCoords());}
	
	
	public final void movePoint(myVectorf dispVec) {
		
	}
	
	/**
	 * this will calculate normalized barycentric coordinates (u, v, w) for pt w/respect to first 3 control points of poly
	 * @param pt
	 * @return
	 */
	public final float[] calcNormBaryCoords() {myPointf[] cntlPts = ownr.getCntlPts(); return calcNormBaryCoords(cntlPts);}
	/**
	 * find point described within this point's current owning control points with current normalized bary coords
	 * @return
	 */
	public final myPointf calcPointFromNormBaryCoords() {	myPointf[] cntlPts = ownr.getCntlPts();return myPointf.calcPointFromNormBaryCoords(cntlPts,pointNBC);}

}//class BarycentricCoords
