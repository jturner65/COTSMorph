package COTS_Morph_PKG.similarities;

import COTS_Morph_PKG.similarities.base.baseSimilarity;
import COTS_Morph_PKG.transform.SpiralTransform;
import base_UI_Objects.IRenderInterface;
import base_UI_Objects.my_procApplet;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

public class COTS_Similarity extends baseSimilarity {

	public COTS_Similarity(myVectorf _n, myVectorf _I, myVectorf _J) {	super(_n, _I, _J);	}
	public COTS_Similarity(COTS_Similarity _otr) {	super(_otr);}
	
	
	/**
	 * how many transforms this similiarity consists of
	 */
	@Override
	protected final int getNumSpiralTransforms() { return 2;}
	
	@Override
	protected final void _reset_Indiv() {
	}

	/**
	 * use this function if this similarity is describing/used by a COTS map
	 * update the data for owning COTS based on passed control points
	 * @param cntlPts
	 * @param forceResetBranching whether branching state should be forced to be reset
	 */
	@Override
	public final void deriveSimilarityFromCntlPts(myPointf[] cntlPts, boolean forceResetBranching) {
		boolean [] flags = new boolean[] {forceResetBranching};
		trans[0].buildTransformation(cntlPts, flags);
		trans[1].buildTransformation(new myPointf[] {cntlPts[0],cntlPts[3],cntlPts[2],cntlPts[1]}, flags);
	}
	
	/**
	 * calc transformation point for given point and spiral quantities
	 * @param A base point to transform
	 * @param t time 
	 * @return
	 */	
	@Override			
	public final myPointf transformPoint(myPointf A, float t) {	return trans[0].transformPoint(A, t);}

	/**
	 * map point to 
	 * @param A corner of map
	 * @param tx interpolant along undeformed map x
	 * @param ty interpolant along underformed map y
	 * @param I ortho to norm, 'x' dir in COTS plane
	 * @param J ortho to norm, 'y' dir in COTS plane
	 * @return
	 */
	@Override
	public final myPointf mapPoint(myPointf A, float tx, float ty) {return trans[0].transformPoint(trans[1].transformPoint(A, tx), ty);}

	@Override
	protected float drawRightSideBarMenuDescr_Indiv(my_procApplet pa, float yOff, float sideBarYDisp) {		return yOff;	}
}
