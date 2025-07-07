package COTS_Morph_PKG.transformer.spiral;

import COTS_Morph_PKG.transformer.spiral.base.baseSpiralTransformer;
import COTS_Morph_PKG.utils.controlFlags.base.Base_ControlFlags;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;
import base_Render_Interface.IRenderInterface;

/**
 * transformer for calculating single spiral transformation
 * @author john
 *
 */
public class SpiralTransformer extends baseSpiralTransformer {

    public SpiralTransformer(String _name, myVectorf _n, myVectorf _I, myVectorf _J) {
        super(_name+"_SpiralTransfrmr", _n, _I, _J);
    }

    public SpiralTransformer(String _name, baseSpiralTransformer _otr) {
        super(_name+"_SpiralTransfrmr_Cpy", _otr);
    }

    @Override
    protected int getNumSpiralTransforms() {    return 1;}

    @Override
    protected void _reset_Indiv() {}

    @Override
    public void deriveSimilarityFromCntlPts(myPointf[] cntlPts, Base_ControlFlags flags) {
    
        trans[0].buildTransformation(new myPointf[] {cntlPts[0],cntlPts[1]},new myPointf[] {cntlPts[2],cntlPts[3]}, flags);    

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
    protected float drawRightSideBarMenuDescr_Indiv(IRenderInterface pa, float yOff, float sideBarYDisp) {
        return yOff;
    }

}
