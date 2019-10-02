package COTS_Morph_PKG.similarities.base;

import COTS_Morph_PKG.transform.SpiralTransform;
import COTS_Morph_PKG.utils.mapCntlFlags;
import base_UI_Objects.my_procApplet;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

/**
 * this class describes a proper similarity transformation driven by one or more spiral transforms 
 * @author john
 *
 */
public abstract class baseTransformer {
	public final String name;
	/**
	 * basis of plane similarity is working in
	 */
	protected myVectorf norm, I, J;
	/**
	 * array of spiral transformations (idxed U,V,...)
	 */
	protected SpiralTransform[] trans;

	
	public baseTransformer(String _name, myVectorf _n, myVectorf _I, myVectorf _J) {	
		name=_name;
		norm=new myVectorf(_n);
		I=new myVectorf(_I);
		J=new myVectorf(_J);
		trans = initTransform();
	}
	
	public baseTransformer(String _name, baseTransformer _otr) {
		name=_name;
		norm=new myVectorf(_otr.norm);
		I=new myVectorf(_otr.I);
		J=new myVectorf(_otr.J);
		trans = new SpiralTransform[_otr.trans.length];  
		for(int i=0;i<_otr.trans.length;++i) {	trans[i]=new SpiralTransform(_name, _otr.trans[i]);}
	}//copy ctor

	protected final SpiralTransform[] initTransform() {
		SpiralTransform[] res = new SpiralTransform[getNumSpiralTransforms()];
		for(int i=0;i<res.length;++i) {	
			String transName = name + "_trans_"+i;
			res[i]=new SpiralTransform(transName, norm,I,J);
		}
		return res ;
	}
	
	/**
	 * how many similarity transformations this similiarity calculator should consist of
	 * @return
	 */
	protected abstract int getNumSpiralTransforms();

	/**
	 * reset similarity values
	 */
	public final void reset() {	for(int i=0;i<trans.length;++i) {	trans[i].reset();}	_reset_Indiv(); }
	protected abstract void _reset_Indiv();
	
	/**
	 * use this function if this similarity is describing/used by a COTS map
	 * update the data for owning COTS based on passed control points
	 * @param cntlPts
	 * @param flags : specific control flags passed from owning map
	 */	
	public abstract void deriveSimilarityFromCntlPts(myPointf[] cntlPts, mapCntlFlags flags);

				
	/**
	 * this will project the passed vector to the map plane
	 * @param vec
	 * @return
	 */
	public myVectorf projVecToMapPlane(myVectorf vec) {return myVectorf._add(myVectorf._mult(I, vec._dot(I)), myVectorf._mult(J, vec._dot(J)));}
	
	/**
	 * this will project the passed scalar quantities, as if they were the x,y coords of a vector, to the map plane spanned by I,J
	 * @param vec
	 * @return
	 */
	public myVectorf projVecToMapPlane(float u, float v) {return myVectorf._add(myVectorf._mult(I, u), myVectorf._mult(J, v));}	
	

	/**
	 * calc 1D transformation point for given point
	 * @param A base point to transform
	 * @param t time 
	 * @return
	 */	
	public final myPointf transformPoint(myPointf A, float t) {return transformPoint(A, 0, t);};			
	public abstract myPointf transformPoint(myPointf A, int transformIDX, float t);			
	/**
	 * 2D mapping of transformation of point A
	 * @param A corner of map
	 * @param tx interpolant along undeformed map x
	 * @param ty interpolant along underformed map y
	 * @return
	 */
	public final myPointf mapPoint(myPointf A, float tx, float ty) {return mapPoint(A, new int[] {0,1}, tx, ty);}
	public abstract myPointf mapPoint(myPointf A, int[] transformIDX, float tx, float ty);

	//////////////////////////
	// draw routines
	private static final String[] transLbls = {"U","V","W","X","Y","Z"};
	/**
	 * configure and draw this similiarity's quantities on right side display
	 * @param pa
	 * @param yOff
	 * @param sideBarYDisp
	 * @return
	 */
	public final float drawRightSideBarMenuDescr(my_procApplet pa, float yOff, float sideBarYDisp) {
		return drawRightSideBarMenuDescr(pa, yOff, sideBarYDisp, transLbls);
//		for (int i=0;i<trans.length;++i) { yOff += trans[i].drawRightSideBarMenuDescr(pa, yOff, sideBarYDisp, transLbls[i]);}
//		yOff = drawRightSideBarMenuDescr_Indiv(pa, yOff, sideBarYDisp);
//		return yOff;
	}
	public final float drawRightSideBarMenuDescr(my_procApplet pa, float yOff, float sideBarYDisp, String[] _transLbls) {
		for (int i=0;i<trans.length;++i) { yOff += trans[i].drawRightSideBarMenuDescr(pa, yOff, sideBarYDisp, _transLbls[i]);}
		yOff = drawRightSideBarMenuDescr_Indiv(pa, yOff, sideBarYDisp);
		return yOff;
	}
	protected abstract float drawRightSideBarMenuDescr_Indiv(my_procApplet pa, float yOff, float sideBarYDisp);
	
	/////////////////////////
	// getters/setters
	public final myVectorf getNorm() {return norm;}
	//public final void setResetAllBranching(boolean _reset) {for (int i=0;i<trans.length;++i) {trans[i].setBranching(0);}}	
	public final void setAllBranchingZero() {for (int i=0;i<trans.length;++i) {trans[i].setBranching(0.0f);}}//setBranching
	public final void setBranching(float[] brnchOffset) {for (int i=0;i<trans.length;++i) {trans[i].setBranching(brnchOffset[i]);}}//setBranching

	public final float[] getBranching() {
		float[] resAra = new float[trans.length];
		for (int i=0;i<trans.length;++i) {resAra[i] = trans[i].getBranching();}
		return resAra;
	}
	
	public final float[] getAnglesAndBranching() {
		float[] anglesAndBrVals = new float[trans.length*2];
		for (int i=0;i<trans.length;++i) {
			float[] _tmpTVals = trans[i].getAnglesAndBranching();
			anglesAndBrVals[i]= _tmpTVals[0];
			anglesAndBrVals[i+trans.length] = _tmpTVals[1];
		}
		return anglesAndBrVals;
	}
	

	public final myPointf getF() {return getF(0);}
	public final myPointf getF(int idx) {return trans[idx].getCenterPoint();}
	
	public final float[] getScales() {
		float[] scales = new float[trans.length];
		for (int i=0;i<trans.length;++i) {	scales[i]=trans[i].get_Scale();}
		return scales;
	}
	
	public final String getDebugStr() {
		String dbgStr = "";
		for (int i=0;i<trans.length-1;++i) { dbgStr+= transLbls[i]+ ": " + trans[i].getDebugStr() + " | ";}
		int i=trans.length-1;
		dbgStr+= transLbls[i]+ ": " + trans[i].getDebugStr();
		return dbgStr;
	}
	
	
}//class COTSData