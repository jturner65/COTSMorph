package COTS_Morph_PKG.similarities.base;

import COTS_Morph_PKG.transform.SpiralTransform;
import base_UI_Objects.my_procApplet;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

public abstract class SpiralSimilarity extends baseSimilarity {
	/**
	 * array of spiral transformations (idxed U,V,...)
	 */
	protected SpiralTransform[] trans;
	public SpiralSimilarity(myVectorf _n, myVectorf _I, myVectorf _J) {
		super(_n, _I, _J);
		trans = initTransform();
	}
	
	protected abstract SpiralTransform[] initTransform();
	public SpiralSimilarity(SpiralSimilarity _otr) {
		super(_otr);
		trans = new SpiralTransform[_otr.trans.length];  
		for(int i=0;i<_otr.trans.length;++i) {	trans[i]=new SpiralTransform(_otr.trans[i]);}
	}
	
	public final void reset() {	
		for(int i=0;i<trans.length;++i) {	trans[i].reset();}
		_reset_Indiv(); 
	}
	protected abstract void _reset_Indiv();
	
	/**
	 * call after recalculating the center point - this will translate the center point by passed displacement
	 * @param _disp
	 */
	public final void translateSpiralCtrPtByVec(myVectorf _disp) {
		for(int i=0;i<trans.length;++i) {	trans[i].translateCenterPointByVec(_disp);}
	}
	
	/**
	 * cots map point
	 * @param A corner of map
	 * @param tx interpolant along undeformed map x
	 * @param ty interpolant along underformed map y
	 * @param I ortho to norm, 'x' dir in COTS plane
	 * @param J ortho to norm, 'y' dir in COTS plane
	 * @return
	 */
	@Override
	public final myPointf mapPoint(myPointf A, float tx, float ty) {
		return trans[0].transformPoint(trans[1].transformPoint(A, tx), ty);
	}

	
	//////////////////////////
	// draw routines
	/**
	 * configre and draw this similiarity's quantities on right side display
	 * @param pa
	 * @param yOff
	 * @param sideBarYDisp
	 * @return
	 */
	private static final String[] transLbls = {"U","V","W","X","Y","Z"};
	@Override
	public float drawRightSideBarMenuDescr(my_procApplet pa, float yOff, float sideBarYDisp) {
		for (int i=0;i<trans.length;++i) { yOff += trans[i].drawRightSideBarMenuDescr(pa, yOff, sideBarYDisp, transLbls[i]);}
		return yOff;
	}
	
	/////////////////////////
	// getters/setters
	public final void setResetBranching(boolean _reset) {
		for (int i=0;i<trans.length;++i) {trans[i].setResetBranching(_reset);}
	}
	
	public final void setBranching(float[] brnchOffset) {
		for (int i=0;i<trans.length;++i) {trans[i].setBranching(brnchOffset[i]);}
	}//setBranching

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
	@Override
	public final String getDebugStr() {
		String dbgStr = "";
		for (int i=0;i<trans.length-1;++i) { dbgStr+= transLbls[i]+ ": " + trans[i].getDebugStr() + " | ";}
		int i=trans.length-1;
		dbgStr+= transLbls[i]+ ": " + trans[i].getDebugStr();
		return dbgStr;
	}
	

}//class SpiralSimilarity
