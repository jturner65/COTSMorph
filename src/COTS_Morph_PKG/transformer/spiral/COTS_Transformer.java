package COTS_Morph_PKG.transformer.spiral;

import COTS_Morph_PKG.transformer.spiral.base.baseSpiralTransformer;
import COTS_Morph_PKG.utils.mapCntlFlags;
import base_UI_Objects.my_procApplet;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;
/**
 * calculate COTS transformation : spiral of spiral
 * @author john
 *
 */
public class COTS_Transformer extends baseSpiralTransformer {

	public COTS_Transformer(String _name, myVectorf _n, myVectorf _I, myVectorf _J) {	super(_name + "_COTS_Sim",_n, _I, _J);	}
	public COTS_Transformer(String _name, COTS_Transformer _otr) {	super(_name + "_COTS_Sim_Cpy",_otr);}
	
	
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
	public final void deriveSimilarityFromCntlPts(myPointf[] cntlPts, mapCntlFlags flags) {
		myPointf[] e0 = new myPointf[] {cntlPts[0],cntlPts[1]},
				e1 = new myPointf[] {cntlPts[3],cntlPts[2]},
				e0Ortho = new myPointf[] {e0[0],e1[0]},
				e1Ortho = new myPointf[] {e0[1],e1[1]};
		trans[0].buildTransformation(e0,e1, flags);	
		//flags.setDebug(true);//set to true to debug branching
		trans[1].buildTransformation(e0Ortho, e1Ortho, flags);
		//flags.setDebug(false);//set to true to debug branching
	}
	
	/**
	 * calc transformation point for given point and spiral quantities
	 * @param A base point to transform
	 * @param t time 
	 * @return
	 */	
	@Override			
	public final myPointf transformPoint(myPointf A, int transformIDX, float t) {	return trans[transformIDX].transformPoint(A, t);}

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
	public final myPointf mapPoint(myPointf A, int[] transformIDXs, float tx, float ty) {return trans[transformIDXs[0]].transformPoint(trans[transformIDXs[1]].transformPoint(A, tx), ty);}

	@Override
	protected float drawRightSideBarMenuDescr_Indiv(my_procApplet pa, float yOff, float sideBarYDisp) {		return yOff;	}
}
