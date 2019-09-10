package COTS_Morph_PKG.similarities;


import COTS_Morph_PKG.similarities.base.baseSimilarity;
import COTS_Morph_PKG.transform.SpiralTransform;
import base_UI_Objects.my_procApplet;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

public class CarrierSimilarity extends baseSimilarity {

	public CarrierSimilarity(myVectorf _n, myVectorf _I, myVectorf _J) {
		super(_n, _I, _J);
	}

	public CarrierSimilarity(CarrierSimilarity _otr) {
		super(_otr);
	}
	
	/**
	 * how many transforms this similiarity consists of
	 */
	@Override
	protected final int getNumSpiralTransforms() { return 2;}

	
	@Override
	protected final void _reset_Indiv() {
	}
	
	/**
	 * use this function if this is a carrier similarity
	 * @param cntlPts end points, in order, of edge on map a  (diagonal a->c, map 0, diagonal a->c map 1) idx 0->2, 1->3
	 * @param forceResetBranching whether branching reset should be forced
	 */
	@Override
	public final void deriveSimilarityFromCntlPts(myPointf[] cntlPts, boolean forceResetBranching) {
		myPointf[] e0 = new myPointf[] {cntlPts[0],cntlPts[1]},
				e1 = new myPointf[] {cntlPts[2],cntlPts[3]},
				e0Ortho = new myPointf[] {e0[0],e1[0]},
				e1Ortho = new myPointf[] {e0[1],e1[1]};
		boolean [] flags = new boolean[] {forceResetBranching};
		
		//e1 end points, in order, of edge on map a  (diagonal a->c) 
		//e2 end points, in order, of edge on map b  (diagonal a->c) 
		trans[0].buildTransformation(e0, e1, flags);
		trans[1].buildTransformation(e0Ortho, e1Ortho, flags);
		
		
//	    mv = spiralScale(e1[0],e1[1],e2[0],e2[1]); 
//	    mu = spiralScale(e1[0],e2[0],e1[1],e2[1]);	
//		old_au = au;
//		old_av = av;
//		av = spiralAngle(e1[0],e1[1],e2[0],e2[1]); 
//	    au = spiralAngle(e1[0],e2[0],e1[1],e2[1]); 
//	    au_BranchDisp = 0.0f;
//	    av_BranchDisp = 0.0f;
//	    F = spiralCenter(mu,au,e1[1],e2[1]);
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
