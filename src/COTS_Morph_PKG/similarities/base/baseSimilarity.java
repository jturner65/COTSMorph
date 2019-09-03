package COTS_Morph_PKG.similarities.base;

import base_UI_Objects.my_procApplet;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

/**
 * this class is a struct that describes a proper similarity transformation as contained in a spiral
 * @author john
 *
 */
public abstract class baseSimilarity {
	/**
	 * basis of plane similarity is working in
	 */
	protected myVectorf norm, I, J;
	
	public baseSimilarity(myVectorf _n, myVectorf _I, myVectorf _J) {	
		norm=new myVectorf(_n);
		I=new myVectorf(_I);
		J=new myVectorf(_J);
	}
	
	public baseSimilarity(baseSimilarity _otr) {	
		norm=new myVectorf(_otr.norm);
		I=new myVectorf(_otr.I);
		J=new myVectorf(_otr.J);
	}//copy ctor
	
	/**
	 * reset similarity values
	 */
	public abstract void reset();
	
	/**
	 * use this function if this similarity is describing/used by a COTS map
	 * update the data for owning COTS based on passed control points
	 * @param cntlPts
	 * @param forceResetBranching whether branching state should be forced to be reset
	 */	
	public abstract void deriveSimilarityFromCntlPts(myPointf[] cntlPts, boolean forceResetBranching);

				
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
	public abstract myPointf transformPoint(myPointf A, float t);			
	/**
	 * 2D mapping of transformation of point A
	 * @param A corner of map
	 * @param tx interpolant along undeformed map x
	 * @param ty interpolant along underformed map y
	 * @return
	 */
	public abstract myPointf mapPoint(myPointf A, float tx, float ty);
		
	/**
	 * configure and draw this similiarity's quantities on right side display
	 * @param pa
	 * @param yOff
	 * @param sideBarYDisp
	 * @return
	 */
	public abstract float drawRightSideBarMenuDescr(my_procApplet pa, float yOff, float sideBarYDisp);
	
	////////////////////////
	// getters/setters
	public myVectorf getNorm() {return norm;}
	
	public abstract String getDebugStr();
	
}//class COTSData