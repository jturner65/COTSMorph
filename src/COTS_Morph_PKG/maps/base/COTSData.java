package COTS_Morph_PKG.maps.base;

import base_Utils_Objects.MyMathUtils;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

/**
 * this class is a struct that describes a COTS configuration
 * @author john
 *
 */
public class COTSData {
	/**
	 * Spiral scaling factors
	 */
	protected float mu = 1.0f, mv = 1.0f;
	/**
	 * spiral angles
	 */
	protected float au = 0.0f, av = 0.0f, old_au = 0.0f, old_av = 0.0f;
	/**
	 * half-rotation values used to compensate for branching
	 */
	protected float au_BranchDisp = 0.0f, av_BranchDisp = 0.0f;
	/**
	 * spiral center
	 */
	public myPointf F;
	/**
	 * whether branching should be reset
	 */
	protected boolean resetBranching;
	
	
	public COTSData() {	}
	
	/**
	 * update the data for owning COTS based on passed control points
	 * @param cntlPts
	 * @param forceResetBranching whether branching state should be forced to be reset
	 * @param n norm
	 * @param I ortho to norm, 'x' dir in COTS plane
	 * @param J ortho to norm, 'y' dir in COTS plane
	 */
	public void updateCntlPoints(myPointf[] cntlPts, boolean forceResetBranching, myVectorf n, myVectorf I, myVectorf J) {
	    mu = spiralScale(cntlPts[0],cntlPts[1],cntlPts[3],cntlPts[2]); 
	    mv = spiralScale(cntlPts[0],cntlPts[3],cntlPts[1],cntlPts[2]);		
		old_au = au;
		old_av = av;
	    float aun = spiralAngle(cntlPts[0],cntlPts[1],cntlPts[3],cntlPts[2],n), 
	    		avn = spiralAngle(cntlPts[0],cntlPts[3],cntlPts[1],cntlPts[2],n); // new values	    
	    if((resetBranching) || forceResetBranching) {
	    	au = aun;
	    	av = avn;
	    	au_BranchDisp = 0.0f;
	    	av_BranchDisp = 0.0f;
	    	resetBranching = false;
	    } else {
			au_BranchDisp = calcAngleDelWthBranching(aun + au_BranchDisp, au, au_BranchDisp);
		    au=(aun + au_BranchDisp);
		    
			av_BranchDisp = calcAngleDelWthBranching(avn + av_BranchDisp, av, av_BranchDisp);
		    av=(avn + av_BranchDisp);
	    }    
	    F = spiralCenter(mu,au,cntlPts[0],cntlPts[3],n, I, J);  
	}
	
	/**
	 * calculate branching displacement/offset due to atan2 limited range
	 * @param newAngle new angle, with current branching offset added
	 * @param oldAngle previous angle
	 * @param angleBranchDisp current branching offset
	 * @return modified branching offset
	 */
	private float calcAngleDelWthBranching(float newAngle, float oldAngle, float angleBranchDisp) {
		float angleDel = newAngle - oldAngle;
	    if(Math.abs(angleDel+MyMathUtils.twoPi_f)<Math.abs(angleDel)) {angleDel+=MyMathUtils.twoPi_f; angleBranchDisp +=MyMathUtils.twoPi_f;}
	    if(Math.abs(angleDel-MyMathUtils.twoPi_f)<Math.abs(angleDel)) {angleDel-=MyMathUtils.twoPi_f; angleBranchDisp -=MyMathUtils.twoPi_f;}
		return angleBranchDisp;		
	}
	
			
	/**
	 * this will project the passed vector to the map plane
	 * @param vec
	 * @return
	 */
	public myVectorf projVecToMapPlane(myVectorf vec, myVectorf I, myVectorf J) {return myVectorf._add(myVectorf._mult(I, vec._dot(I)), myVectorf._mult(J, vec._dot(J)));}
	
	/**
	 * this will project the passed scalar quantities, as if they were the x,y coords of a vector, to the map plane spanned by I,J
	 * @param vec
	 * @return
	 */
	public myVectorf projVecToMapPlane(float u, float v, myVectorf I, myVectorf J) {return myVectorf._add(myVectorf._mult(I, u), myVectorf._mult(J, v));}
	
	
	public void setResetBranching(boolean _reset) {resetBranching = _reset;}

	/**
	 * rotate the point by angle a around spiral center F in plane spanned by I,J
	 * @param P point
	 * @param a angle
	 * @param I frame basis in 'x' dir
	 * @param J frame basis in 'y' dir
	 * @return rotated point
	 */	
	public final myPointf rotPtAroundF(myPointf P, float a, myVectorf I, myVectorf J) {
		myVectorf fpRaw = new myVectorf(F,P), 
				fp = projVecToMapPlane(fpRaw, I, J);
		double x = myVectorf._dot(fp,I),	y = myVectorf._dot(fp,J); 
		double c=Math.cos(a), s=Math.sin(a); 
		float iXVal = (float) ((x*c)-x-(y*s)), jYVal= (float) ((x*s)+(y*c)-y);			
		return myPointf._add(P,iXVal,I,jYVal,J); 
	}; 
	public float spiralAngle(myPointf A, myPointf B, myPointf C, myPointf D, myVectorf norm) {return myVectorf._angleBetween_Xprod(new myVectorf(A,B),new myVectorf(C,D), norm);}
	public float spiralScale(myPointf A, myPointf B, myPointf C, myPointf D) {return myPointf._dist(C,D)/ myPointf._dist(A,B);}
	
	/**
	 * find fixed points given scale and angle - point is pivot that takes A to C
	 * @param scale |CD|/|AB|
	 * @param angle angle between AB and CD around planar normal
	 * @param A
	 * @param C
	 * @return
	 */
	public final myPointf spiralCenter(float scale, float angle, myPointf A, myPointf C, myVectorf norm, myVectorf I, myVectorf J) {         // new spiral center
		//fixed point will move FA to FC and FB to FD
		float cos = (float) Math.cos(angle), sin = (float) Math.sin(angle);
		myVectorf CARaw=new myVectorf(C,A),
				CA = projVecToMapPlane(CARaw,I,J),
				U = projVecToMapPlane(scale*cos-1, scale*sin, I, J),
				rU = myVectorf._rotAroundAxis(U, norm, MyMathUtils.halfPi_f);		
		float uSqMag = U.sqMagn;
		myVectorf V = projVecToMapPlane(U._dot(CA)/uSqMag, rU._dot(CA)/uSqMag, I, J);
		return myPointf._add(A, V);
	}

	public final myPointf spiralPoint(myPointf A, float scl, float angle, float t, myVectorf I, myVectorf J) {	return new myPointf(F, (float) Math.pow(scl, t), rotPtAroundF(A, t*angle, I,J));}		
	public final myPointf mapPoint(myPointf A, float tx, float ty, myVectorf I, myVectorf J) {
		return spiralPoint(spiralPoint(A,mv,av, tx,I,J),mu,au, ty,I,J);
	}
	
	////////////////////////
	// getters/setters
	
	public final void setBranching(float[] brnchOffset) {
		//remove old displacement
		au -= au_BranchDisp;
		av -= av_BranchDisp;
		au_BranchDisp = brnchOffset[0];		
		av_BranchDisp = brnchOffset[1];
		//add new displacement
		au += au_BranchDisp;
		av += av_BranchDisp;
	}
	
	public final float[] getBranching() {return new float[] {this.au_BranchDisp, this.av_BranchDisp};}
	
	public final float get_mu() {return mu;}
	public final float get_mv() {return mv;}

	public final float get_au() {return au;}
	public final float get_av() {return av;}
	public final myPointf getF() {return F;}
	
	public String getDebugStr() {
		String dbgStr = " | a/s AB and DC : old a : " +String.format("%.4f",old_au);
		dbgStr += " | " +String.format("%.4f",au) +" | brnch :  "+String.format("%.4f",au_BranchDisp);
		dbgStr += " | uScl : " + String.format("%.4f",mu);
		dbgStr += " || a/s AD and BC: old a : " + String.format("%.4f",old_av);
		dbgStr += "|" +String.format("%.4f",av) +" | brnch :  "+String.format("%.4f",av_BranchDisp);
		dbgStr += " | vScl : " + String.format("%.4f",mv);
		dbgStr += " || F : " + F.toStrBrf();
		return dbgStr;}
	
}//class COTSData