package COTS_Morph_PKG.similarities.base;

import COTS_Morph_PKG.transform.SpiralTransform;
import base_UI_Objects.my_procApplet;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

/**
 * this class is a struct that describes a proper similarity transformation as contained in a spiral
 * @author john
 *
 */
public abstract class baseSimilarity {
	/**
	 * basis of plane similarity is working in
	 */
	protected myVectorf norm, I, J;
	/**
	 * array of spiral transformations (idxed U,V,...)
	 */
	protected SpiralTransform[] trans;

	
	public baseSimilarity(myVectorf _n, myVectorf _I, myVectorf _J) {	
		norm=new myVectorf(_n);
		I=new myVectorf(_I);
		J=new myVectorf(_J);
		trans = initTransform();
	}
	
	public baseSimilarity(baseSimilarity _otr) {	
		norm=new myVectorf(_otr.norm);
		I=new myVectorf(_otr.I);
		J=new myVectorf(_otr.J);
		trans = new SpiralTransform[_otr.trans.length];  
		for(int i=0;i<_otr.trans.length;++i) {	trans[i]=new SpiralTransform(_otr.trans[i]);}
	}//copy ctor

	protected final SpiralTransform[] initTransform() {
		SpiralTransform[] res = new SpiralTransform[getNumSpiralTransforms()];
		for(int i=0;i<res.length;++i) {	res[i]=new SpiralTransform(norm,I,J);}
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
	 * @param forceResetBranching whether branching state should be forced to be reset
	 */	
	public abstract void deriveSimilarityFromCntlPts(myPointf[] cntlPts, boolean forceResetBranching);

				
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
	public abstract myPointf transformPoint(myPointf A, float t);			
	/**
	 * 2D mapping of transformation of point A
	 * @param A corner of map
	 * @param tx interpolant along undeformed map x
	 * @param ty interpolant along underformed map y
	 * @return
	 */
	public abstract myPointf mapPoint(myPointf A, float tx, float ty);
		
	

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
		for (int i=0;i<trans.length;++i) { yOff += trans[i].drawRightSideBarMenuDescr(pa, yOff, sideBarYDisp, transLbls[i]);}
		yOff = drawRightSideBarMenuDescr_Indiv(pa, yOff, sideBarYDisp);
		return yOff;
	}
	protected abstract float drawRightSideBarMenuDescr_Indiv(my_procApplet pa, float yOff, float sideBarYDisp);
	
	/////////////////////////
	// getters/setters
	public final myVectorf getNorm() {return norm;}
	public final void setResetBranching(boolean _reset) {for (int i=0;i<trans.length;++i) {trans[i].setResetBranching(_reset);}}	
	public final void setBranching(float[] brnchOffset) {for (int i=0;i<trans.length;++i) {trans[i].setBranching(brnchOffset[i]);}}//setBranching

	public final float[] getBranching() {
		float[] resAra = new float[trans.length];
		for (int i=0;i<trans.length;++i) {resAra[i] = trans[i].getBranching();}
		return resAra;
	}
	
	public final float[] getAnglesAndBranching() {
		float[] transVals = new float[trans.length*2];
		for (int i=0;i<trans.length;++i) {
			float[] _tmpTVals = trans[i].getAnglesAndBranching();
			transVals[i]= _tmpTVals[0];
			transVals[i+trans.length] = _tmpTVals[1];
		}
		return transVals;
	}
	

	public final myPointf getF() {return getF(0);}
	public final myPointf getF(int idx) {return trans[idx].getCenterPoint();}
	public final String getDebugStr() {
		String dbgStr = "";
		for (int i=0;i<trans.length-1;++i) { dbgStr+= transLbls[i]+ ": " + trans[i].getDebugStr() + " | ";}
		int i=trans.length-1;
		dbgStr+= transLbls[i]+ ": " + trans[i].getDebugStr();
		return dbgStr;
	}
	
	
}//class COTSData