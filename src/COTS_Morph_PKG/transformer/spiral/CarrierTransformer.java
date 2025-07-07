package COTS_Morph_PKG.transformer.spiral;

import COTS_Morph_PKG.transformer.spiral.base.baseSpiralTransformer;
import COTS_Morph_PKG.utils.controlFlags.base.Base_ControlFlags;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;
import base_Render_Interface.IRenderInterface;

/**
 * calculate carrier-based transform 
 * @author john
 *
 */
public class CarrierTransformer extends baseSpiralTransformer {

    public CarrierTransformer(String _name, myVectorf _n, myVectorf _I, myVectorf _J) {
        super(_name + "_Carrier_Sim", _n, _I, _J);
    }

    public CarrierTransformer(String _name, CarrierTransformer _otr) {
        super(_name + "_Carrier_Sim_Cpy",_otr);
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
    public final void deriveSimilarityFromCntlPts(myPointf[] cntlPts,  Base_ControlFlags flags) {
        myPointf[] e0 = new myPointf[] {cntlPts[0],cntlPts[1]},
                e1 = new myPointf[] {cntlPts[2],cntlPts[3]},
                e0Ortho = new myPointf[] {e0[0],e1[0]},
                e1Ortho = new myPointf[] {e0[1],e1[1]};
        
        //e1 end points, in order, of edge on map a  (diagonal a->c) 
        //e2 end points, in order, of edge on map b  (diagonal a->c) 
        trans[0].buildTransformation(e0, e1, flags);
        trans[1].buildTransformation(e0Ortho, e1Ortho, flags);

    }
    
    /**
     * calc transformation point for given point and spiral quantities
     * @param A base point to transform
     * @param t time 
     * @return
     */    
    @Override            
    public final myPointf transformPoint(myPointf A, int transformIDX, float t) {    return trans[transformIDX].transformPoint(A, t);}

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
    public final myPointf mapPoint(myPointf A, int[] transformIDX, float tx, float ty) {return trans[transformIDX[0]].transformPoint(trans[transformIDX[0]].transformPoint(A, tx), ty);}

    @Override
    protected float drawRightSideBarMenuDescr_Indiv(IRenderInterface pa, float yOff, float sideBarYDisp) {        return yOff;    }

}
