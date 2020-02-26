package COTS_Morph_PKG.transformer.base;

import COTS_Morph_PKG.transform.base.baseTransform;
import base_JavaProjTools_IRender.base_Render_Interface.IRenderInterface;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;

/**
 * class to manage the execution of one or more transforms
 * @author john
 *
 */
public abstract class baseTransformer {
	public final String name;
	/**
	 * basis of plane similarity is working in
	 */
	protected myVectorf norm, I, J;
	/**
	 * array of  transformations (idxed U,V,...)
	 */
	protected baseTransform[] trans;

	public baseTransformer(String _name, myVectorf _n, myVectorf _I, myVectorf _J) {	
		name=_name;
		norm=new myVectorf(_n);
		I=new myVectorf(_I);
		J=new myVectorf(_J);	
		trans = initTransform();
	}
	
	public baseTransformer(String _name, baseTransformer _otr) {
		name=_name;
		norm=new myVectorf(_otr.norm);
		I=new myVectorf(_otr.I);
		J=new myVectorf(_otr.J);
		trans = initTransformFromCopy(_otr);
	}
	/**
	 * build array of appropriate transformations
	 * @return
	 */
	protected abstract baseTransform[] initTransform();
	
	/**
	 * build array of appropriate transformations
	 * @return
	 */
	protected abstract baseTransform[] initTransformFromCopy(baseTransformer _otr);
	
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
	public final myPointf transformPoint(myPointf A, float t) {return transformPoint(A, 0, t);};			
	public abstract myPointf transformPoint(myPointf A, int transformIDX, float t);			
	/**
	 * 2D mapping of transformation of point A
	 * @param A corner of map
	 * @param tx interpolant along undeformed map x
	 * @param ty interpolant along underformed map y
	 * @return
	 */
	public final myPointf mapPoint(myPointf A, float tx, float ty) {return mapPoint(A, new int[] {0,1}, tx, ty);}
	public abstract myPointf mapPoint(myPointf A, int[] transformIDX, float tx, float ty);
	//////////////////////////
	// draw routines
	protected static final String[] transLbls = {"U","V","W","X","Y","Z"};
	
	protected final void showOffsetText_RightSideMenu(IRenderInterface pa, int[] tclr, float mult,  String txt) {
		pa.setFill(tclr,tclr[3]);pa.setStroke(tclr,tclr[3]);
		pa.showText(txt,0.0f,0.0f,0.0f);
		pa.translate(txt.length()*mult, 0.0f,0.0f);		
	}


	/**
	 * configure and draw this similiarity's quantities on right side display
	 * @param pa
	 * @param yOff
	 * @param sideBarYDisp
	 * @return
	 */
	public final float drawRightSideBarMenuDescr(IRenderInterface pa, float yOff, float sideBarYDisp) {
		return drawRightSideBarMenuDescr(pa, yOff, sideBarYDisp, transLbls);
//		for (int i=0;i<trans.length;++i) { yOff += trans[i].drawRightSideBarMenuDescr(pa, yOff, sideBarYDisp, transLbls[i]);}
//		yOff = drawRightSideBarMenuDescr_Indiv(pa, yOff, sideBarYDisp);
//		return yOff;
	}
	public final float drawRightSideBarMenuDescr(IRenderInterface pa, float yOff, float sideBarYDisp, String[] _transLbls) {
		for (int i=0;i<trans.length;++i) { yOff += trans[i].drawRightSideBarMenuDescr(pa, yOff, sideBarYDisp, _transLbls[i]);}
		yOff = drawRightSideBarMenuDescr_Indiv(pa, yOff, sideBarYDisp);
		return yOff;
	}
	protected abstract float drawRightSideBarMenuDescr_Indiv(IRenderInterface pa, float yOff, float sideBarYDisp);
	
	
	/////////////////////////
	// getters/setters
	public final myVectorf getNorm() {return norm;}
	public final baseTransform[] getTrans() {return trans;}
	
}//class baseTransformer
