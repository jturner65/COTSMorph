package COTS_Morph_PKG.similarities;

import COTS_Morph_PKG.similarities.base.baseTransformer;
import COTS_Morph_PKG.utils.mapCntlFlags;
import base_UI_Objects.my_procApplet;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

/**
 * transformer for calculating single spiral transformation
 * @author john
 *
 */
public class SpiralTransformer extends baseTransformer {

	public SpiralTransformer(String _name, myVectorf _n, myVectorf _I, myVectorf _J) {
		super(_name+"_SpiralSim", _n, _I, _J);
	}

	public SpiralTransformer(String _name, baseTransformer _otr) {
		super(_name+"_SpiralSim_Cpy", _otr);
	}

	@Override
	protected int getNumSpiralTransforms() {	return 1;}

	@Override
	protected void _reset_Indiv() {}

	@Override
	public void deriveSimilarityFromCntlPts(myPointf[] cntlPts, mapCntlFlags flags) {
		myPointf[] e0 = new myPointf[] {cntlPts[0],cntlPts[1]},
				e1 = new myPointf[] {cntlPts[2],cntlPts[3]};
		
		trans[0].buildTransformation(e0,e1, flags);	

	}

	@Override
	public myPointf transformPoint(myPointf A, int transformIDX, float t) {return trans[0].transformPoint(A, t);}
	/**
	 * this ignores ty
	 */
	@Override
	public myPointf mapPoint(myPointf A, int[] transformIDX, float tx, float ty) {
		// TODO Auto-generated method stub
		return transformPoint(A, 0, tx);
	}

	@Override
	protected float drawRightSideBarMenuDescr_Indiv(my_procApplet pa, float yOff, float sideBarYDisp) {
		return yOff;
	}

}
