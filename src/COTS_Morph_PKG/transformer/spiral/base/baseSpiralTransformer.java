package COTS_Morph_PKG.transformer.spiral.base;

import COTS_Morph_PKG.transform.SpiralTransform;
import COTS_Morph_PKG.transform.base.baseTransform;
import COTS_Morph_PKG.transformer.base.baseTransformer;
import COTS_Morph_PKG.utils.controlFlags.base.Base_ControlFlags;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;

/**
 * this class describes a proper similarity transformation driven by one or more spiral transforms 
 * @author john
 *
 */
public abstract class baseSpiralTransformer extends baseTransformer {
	
	public baseSpiralTransformer(String _name, myVectorf _n, myVectorf _I, myVectorf _J) {		super(_name,_n,_I,_J);}
	
	public baseSpiralTransformer(String _name, baseSpiralTransformer _otr) {		super(_name, _otr);	}//copy ctor
	
	/**
	 * build array of appropriate transformations
	 * @return
	 */
	@Override
	protected final baseTransform[] initTransform() {
		SpiralTransform[] res = new SpiralTransform[getNumSpiralTransforms()];
		for(int i=0;i<res.length;++i) {	
			String transName = name + "_trans_"+i;
			res[i]=new SpiralTransform(transName, norm,I,J);
		}
		return res ;
	}
	
	/**
	 * build array of appropriate transformations
	 * @return
	 */
	@Override
	protected final baseTransform[] initTransformFromCopy(baseTransformer _otr){
		baseTransform[] otrTrans = _otr.getTrans();
		SpiralTransform[] res = new SpiralTransform[otrTrans.length];
		for(int i=0;i<otrTrans.length;++i) {	res[i]=new SpiralTransform(name, (SpiralTransform) otrTrans[i]);	}
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
	public abstract void deriveSimilarityFromCntlPts(myPointf[] cntlPts, Base_ControlFlags flags);	
	
	/////////////////////////
	// getters/setters
	//public final void setResetAllBranching(boolean _reset) {for (int i=0;i<trans.length;++i) {trans[i].setBranching(0);}}	
	public final void setAllBranchingZero() {for (int i=0;i<trans.length;++i) {((SpiralTransform) trans[i]).setBranching(0.0f);}}//setBranching
	public final void setBranching(float[] brnchOffset) {for (int i=0;i<trans.length;++i) {((SpiralTransform) trans[i]).setBranching(brnchOffset[i]);}}//setBranching

	public final float[] getBranching() {
		float[] resAra = new float[trans.length];
		for (int i=0;i<trans.length;++i) {resAra[i] = ((SpiralTransform) trans[i]).getBranching();}
		return resAra;
	}
	
	public final float[] getAnglesAndBranching() {
		float[] anglesAndBrVals = new float[trans.length*2];
		for (int i=0;i<trans.length;++i) {
			float[] _tmpTVals = ((SpiralTransform) trans[i]).getAnglesAndBranching();
			anglesAndBrVals[i]= _tmpTVals[0];
			anglesAndBrVals[i+trans.length] = _tmpTVals[1];
		}
		return anglesAndBrVals;
	}
	

	public final myPointf getF() {return getF(0);}
	public final myPointf getF(int idx) {return ((SpiralTransform) trans[idx]).getCenterPoint();}
	
	public final float[] getScales() {
		float[] scales = new float[trans.length];
		for (int i=0;i<trans.length;++i) {	scales[i]=((SpiralTransform) trans[i]).get_Scale();}
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