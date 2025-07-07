package COTS_Morph_PKG.transform.base;

import COTS_Morph_PKG.utils.controlFlags.base.Base_ControlFlags;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;
import base_Render_Interface.IRenderInterface;

/**
 * base class holding a definition of a transformation
 * @author john
 *
 */
public abstract class baseTransform {
    public final String name;
    /**
     * basis of plane for transform
     */
    protected myVectorf norm, I, J;
    
    public baseTransform(String _name, myVectorf _n, myVectorf _I, myVectorf _J) {
        name=_name;
        setNewFrame(_n,_I,_J);
    }
    
    public baseTransform(String _name, baseTransform _otr) {
        name= _name;
        setNewFrame(_otr.norm,_otr.I,_otr.J);
    }
    
    /**
     * reset values
     */
    public void reset() {
        reset_Indiv();
    }
    protected abstract void reset_Indiv();
    
    protected final void showOffsetText_RightSideMenu(IRenderInterface ri, int[] tclr, float mult,  String txt) {
        ri.setFill(tclr,tclr[3]);ri.setStroke(tclr,tclr[3]);
        ri.showText(txt,0.0f,0.0f,0.0f);
        ri.translate(txt.length()*mult, 0.0f,0.0f);        
    }

    
    /**
     * modify the frame for this transformation
     * @param _n
     * @param _I
     * @param _J
     */
    public final void setNewFrame(myVectorf _n, myVectorf _I, myVectorf _J) {
        norm=new myVectorf(_n);
        I=new myVectorf(_I);
        J=new myVectorf(_J);
    }
    /**
     * build this transformation from two edge pt arrays
     *          cntl pts expected to be in circle so that 0 maps to 3 and 1 maps to 2
     * @param cntlPts
     * @param flags any instance-specific flags to use to build transformation 
     */    
    public abstract void buildTransformation(myPointf[] e0, myPointf[] e1,  Base_ControlFlags flags);
    
    /**
     * calc 1D transformation point for given point
     * @param A base point to transform
     * @param t time 
     * @return
     */    
    public abstract myPointf transformPoint(myPointf A, float t);            

    /**
     * calc 1D transformation point for given point
     * @param A base point to transform
     * @param t time 
     * @return
     */    
    public abstract myPointf transformVector(myVectorf V, float t);            

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
     * configure and draw this similiarity's quantities on right side display
     * @param ri
     * @param yOff
     * @param sideBarYDisp
     * @param coordName
     * @return
     */
    public abstract float drawRightSideBarMenuDescr(IRenderInterface pa, float yOff, float sideBarYDisp, String coordName);
    
    
    public final String getDebugStr() {
        String res = "Ortho Frame of transform : norm : " + norm.toStrBrf() +" | I : " + I.toStrBrf() + " | J : " + J.toStrBrf()+"\n";
        res += getDebugStr_Indiv()+"\n";
        return res;        
    };
    public abstract String getDebugStr_Indiv();
}//class baseTransform
