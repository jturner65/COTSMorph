package COTS_Morph_PKG.transform;

import COTS_Morph_PKG.transform.base.baseTransform;
import base_UI_Objects.IRenderInterface;
import base_UI_Objects.my_procApplet;
import base_Utils_Objects.MyMathUtils;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

public class SpiralTransform extends baseTransform {
	/**
	 * Spiral scaling factor
	 */
	protected float m = 1.0f;
	/**
	 * spiral angle
	 */
	protected float a = 0.0f;//, old_a = 0.0f;
	/**
	 * half-rotation values used to compensate for branching of atan2
	 */
	protected float a_BranchDisp = 0.0f;
	/**
	 * spiral center
	 */
	public myPointf F;
	/**
	 * whether branching should be reset
	 */
	protected boolean resetBranching = false;
	
	protected static final String[] rtMenuDispType = new String[] {"Scale","Angle","Branching"};


	public SpiralTransform(myVectorf _n, myVectorf _I, myVectorf _J) {	super(_n, _I, _J);}
	
	public SpiralTransform(SpiralTransform _otr) {
		super(_otr);
		m= _otr.m;
		a= _otr.a;
		//old_a = _otr.old_a;
		a_BranchDisp = _otr.a_BranchDisp;
		F = new myPointf(_otr.F);
		resetBranching = _otr.resetBranching;
	}
	
	@Override
	protected final void reset_Indiv() {
		 m = 1.0f;
		 a = 0.0f; 
		// old_a = 0.0f; 
		 a_BranchDisp = 0.0f; 
		 F = new myPointf();
		 resetBranching = false;
	}
	
	/**
	 * build this transformation from control point array
	 * 		 cntl pts expected to be in circle so that 0 maps to 3 and 1 maps to 2
	 * @param cntlPts
	 * @param flags any instance-specific flags to use to build transformation 
	 */	
	@Override
	public final void buildTransformation(myPointf[] cntlPts, boolean[] flags) {
	    m = spiralScale(cntlPts[0],cntlPts[1],cntlPts[3],cntlPts[2]); 	
		//old_a = a;
	    float an = spiralAngle(cntlPts[0],cntlPts[1],cntlPts[3],cntlPts[2]); // new values	    
	    if((resetBranching) || flags[0]) {
	    	a = an;
	    	a_BranchDisp = 0.0f;
	    	resetBranching = false;
	    } else {
	    	a_BranchDisp = calcAngleDelWthBranching(an + a_BranchDisp, a, a_BranchDisp);
		    a=(an + a_BranchDisp);
	    }    
	    F = spiralCenter(m,a,cntlPts[0],cntlPts[3]);  
	}
	
	/**
	 * build this transformation from two edge pt arrays
	 * 		 edges map e0[0] -> e1[0], e0[1]->e1[1]
	 * @param cntlPts
	 * @param flags any instance-specific flags to use to build transformation 
	 */	
	@Override
	public final void buildTransformation(myPointf[] e0,myPointf[] e1, boolean[] flags) {
	    m = spiralScale(e0[0],e0[1],e1[0],e1[1]); 	
		//old_a = a;
	    float an = spiralAngle(e0[0],e0[1],e1[0],e1[1]); // new values	    
	    if((resetBranching) || flags[0]) {
	    	a = an;
	    	a_BranchDisp = 0.0f;
	    	resetBranching = false;
	    } else {
	    	a_BranchDisp = calcAngleDelWthBranching(an + a_BranchDisp, a, a_BranchDisp);
		    a=(an + a_BranchDisp);
	    }    
	    F = spiralCenter(m,a,e0[0],e1[0]);  
	}

	
	/**
	 * calculate branching displacement/offset due to atan2 limited range
	 * @param newAngle new angle, with current branching offset added
	 * @param oldAngle previous angle
	 * @param angleBranchOffset current branching offset
	 * @return modified branching offset
	 */
	protected float calcAngleDelWthBranching(float newAngle, float oldAngle, float angleBranchOffset) {
		float angleDel = newAngle - oldAngle;
	    if(Math.abs(angleDel+MyMathUtils.twoPi_f)<Math.abs(angleDel)) {angleDel+=MyMathUtils.twoPi_f; angleBranchOffset +=MyMathUtils.twoPi_f;}
	    if(Math.abs(angleDel-MyMathUtils.twoPi_f)<Math.abs(angleDel)) {angleDel-=MyMathUtils.twoPi_f; angleBranchOffset -=MyMathUtils.twoPi_f;}
		return angleBranchOffset;		
	}//calcAngleDelWthBranching
		

	/**
	 * rotate the point by angle a around spiral center F in plane spanned by I,J
	 * @param P point to rotate
	 * @param F center point of rotation 
	 * @param a angle
	 * @param I ortho to norm, 'x' dir in COTS plane
	 * @param J ortho to norm, 'y' dir in COTS plane
	 * @return rotated point
	 */	
	public final myPointf rotPtAroundF(myPointf P, myPointf F, float a) {
		myVectorf fpRaw = new myVectorf(F,P), 
				fp = projVecToMapPlane(fpRaw);
		double x = myVectorf._dot(fp,I),	y = myVectorf._dot(fp,J); 
		double c=Math.cos(a), s=Math.sin(a); 
		float iXVal = (float) ((x*c)-x-(y*s)), jYVal= (float) ((x*s)+(y*c)-y);			
		return myPointf._add(P,iXVal,I,jYVal,J); 
	}; 
	public float spiralAngle(myPointf A, myPointf B, myPointf C, myPointf D) {return myVectorf._angleBetween_Xprod(new myVectorf(A,B),new myVectorf(C,D), norm);}
	public float spiralScale(myPointf A, myPointf B, myPointf C, myPointf D) {return myPointf._dist(C,D)/ myPointf._dist(A,B);}
	
	/**
	 * spiral given 4 points, AB and CD are edges corresponding through rotation
	 * @param A
	 * @param B
	 * @param C
	 * @param D
	 * @return
	 */
	public final myPointf spiralCenter(myPointf A, myPointf B, myPointf C, myPointf D) {         // new spiral center
		myVectorf AB=new myVectorf(A,B), CD=new myVectorf(C,D), AC=new myVectorf(A,C);
		float mu = CD.magn/AB.magn, magSq=CD.magn*AB.magn;		
		myVectorf rAB = myVectorf._rotAroundAxis(AB, norm, MyMathUtils.halfPi_f);
		float c=AB._dot(CD)/magSq,	s=rAB._dot(CD)/magSq;
		float AB2 = AB._dot(AB), 
				a=AB._dot(AC)/AB2, 
				b=rAB._dot(AC)/AB2, 
				x=(a-mu*( a*c+b*s)), y=(b-mu*(-a*s+b*c)), d=1+mu*(mu-2*c);  if((c!=1)&&(mu!=1)) { x/=d; y/=d; };
		return new myPointf(new myPointf(A,x,AB),y,rAB);
	 }

	
	/**
	 * find fixed points given scale and angle - point is pivot that takes A to C
	 * @param scale |CD|/|AB|
	 * @param angle angle between AB and CD around planar normal
	 * @param A
	 * @param C
	 * @return
	 */
	public final myPointf spiralCenter(float scale, float angle, myPointf A, myPointf C) {         // new spiral center
		//fixed point will move FA to FC and FB to FD
		float cos = (float) Math.cos(angle), sin = (float) Math.sin(angle);
		myVectorf CARaw=new myVectorf(C,A),
				CA = projVecToMapPlane(CARaw),
				U = projVecToMapPlane(scale*cos-1, scale*sin),
				rU = myVectorf._rotAroundAxis(U, norm, MyMathUtils.halfPi_f);		
		float uSqMag = U.sqMagn;
		myVectorf V = projVecToMapPlane(U._dot(CA)/uSqMag, rU._dot(CA)/uSqMag);
		return myPointf._add(A, V);
	}
	
	/**
	 * call after recalculating the center point - this will translate the center point by passed displacement
	 * @param _disp
	 */
	public final void translateCenterPointByVec(myVectorf _disp) {	F._add(_disp);}
	
	/**
	 * calc 1D transformation point for given point
	 * @param A base point to transform
	 * @param t time 
	 * @return
	 */	
	@Override
	public final myPointf transformPoint(myPointf A, float t) {return new myPointf(F, (float) Math.pow(m, t), rotPtAroundF(A, F, t*a));}	
	
	/**
	 * calc 1D transformation vector for given vector
	 * @param A base point to transform
	 * @param t time 
	 * @return
	 */	
	public final myPointf transformVector(myVectorf V, float t) {return myVectorf._mult(myVectorf._rotAroundAxis(V, norm, t*a), (float) Math.pow(m, t));}
	
	
	/**
	 * configure and draw this similiarity's quantities on right side display
	 * @param pa
	 * @param yOff
	 * @param sideBarYDisp
	 * @return
	 */
	@Override
	public final float drawRightSideBarMenuDescr(my_procApplet pa, float yOff, float sideBarYDisp, String coordName) {
		pa.translate(10.0f, 0.0f, 0.0f);
		String[] dispVals = new String[]{String.format("%.4f",m),String.format("%.4f",a),String.format("%.4f",a_BranchDisp)};	
		pa.pushMatrix();pa.pushStyle();
		for(int j=0;j<rtMenuDispType.length;++j) {			
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 5.0f, rtMenuDispType[j] + " " + coordName + " : ");
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_LightGreen, 255), 7.0f, dispVals[j]);
		}
		pa.popStyle();pa.popMatrix();
		yOff += sideBarYDisp;
		pa.translate(0.0f,sideBarYDisp, 0.0f);
		
		pa.pushMatrix();pa.pushStyle();		
		pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 5.5f, "Fixed Point : ");
		pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_LightCyan, 255), 7.0f, F.toStrBrf());
		pa.popStyle();pa.popMatrix();
			
		yOff += sideBarYDisp;
		pa.translate(-10.0f,sideBarYDisp, 0.0f);
		return yOff;
	};
	
	public final float drawFixedPoint(my_procApplet pa, float yOff, float sideBarYDisp){
		pa.translate(10.0f, 0.0f, 0.0f);
		pa.pushMatrix();pa.pushStyle();		
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 5.5f, "Fixed Point : ");
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_LightCyan, 255), 7.0f, F.toStrBrf());
		pa.popStyle();pa.popMatrix();
			
		yOff += sideBarYDisp;
		pa.translate(-10.0f,sideBarYDisp, 0.0f);
		return yOff;
		
	}	
	
	/////////////////////////
	// getters/setters
	
	public final void setResetBranching(boolean _reset) {resetBranching = _reset;}
	public final void setBranching(float brnchOffset) {
		//remove old displacement
		a -= a_BranchDisp;
		a_BranchDisp = brnchOffset;	
		//add new displacement
		a += a_BranchDisp;
	}//setBranching
	
	public final float getBranching() {return a_BranchDisp;}
	
	public final float[] getAnglesAndBranching() {return new float[] {a, a_BranchDisp};}
	
	public final float get_Scale() {return m;}

	public final float get_Angle() {return a;}
	public final myPointf getCenterPoint() {return F;}

	@Override
	protected String getDebugStr_Indiv() {
		String dbgStr = " | a/s AB and DC : ";//old a : " +String.format("%.4f",old_a) +" |";
		dbgStr += " Angle : " +String.format("%.4f",a) +" | Brnch :  "+String.format("%.4f",a_BranchDisp);
		dbgStr += " | Scl : " + String.format("%.4f",m) + " || F : " + F.toStrBrf();
		return dbgStr;
	}


	
}//class SpiralTransform
