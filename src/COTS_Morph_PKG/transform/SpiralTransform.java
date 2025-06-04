package COTS_Morph_PKG.transform;

import COTS_Morph_PKG.map.base.Base_PolyMap;
import COTS_Morph_PKG.transform.base.baseTransform;
import COTS_Morph_PKG.utils.controlFlags.base.Base_ControlFlags;
import base_Math_Objects.MyMathUtils;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;
import base_Render_Interface.IRenderInterface;

/**
 * this is a transformation between two edges using a spiral
 * @author john
 *
 */
public class SpiralTransform extends baseTransform {
	/**
	 * Spiral scaling factor
	 */
	protected float m = 1.0f;
	/**
	 * spiral angle
	 */
	protected float a = 0.0f, old_alpha = 0.0f;
	/**
	 * half-rotation values used to compensate for branching of atan2
	 */
	protected float a_BranchDisp = 0.0f;
	/**
	 * spiral center
	 */
	public myPointf F;
	/**
	 * whether this spiral is degenerate = if angle is 0 then can't calculate F point, so use LERP instead
	 */
	protected boolean isDegenerate;
	
	//protected static final String[] rtMenuDispType = new String[] {"Scale","Angle","Branch"};
	protected static final String[] rtMenuDispType = new String[] {"S","A","B"};
	/**
	 * these are only  used if degenerate, for lerp
	 */
	private myPointf startP, endP;


	public SpiralTransform(String _name, myVectorf _n, myVectorf _I, myVectorf _J) {	
		super( _name+"_Sprl", _n, _I, _J);
		startP = new myPointf();
		endP = new myPointf();
	}
	
	public SpiralTransform(String _name, SpiralTransform _otr) {
		super( _name+"_Spiral_Cpy",_otr);
		m= _otr.m;		a= _otr.a;		
		startP = new myPointf(_otr.startP);
		endP = new myPointf(_otr.endP);		
		isDegenerate = a==0;
		old_alpha = _otr.old_alpha;
		a_BranchDisp = _otr.a_BranchDisp;
		F = new myPointf(_otr.F);
	}
	
	@Override
	protected final void reset_Indiv() {
		m = 1.0f;
		a = 0.0f; 
		startP = new myPointf();
		endP = new myPointf();
		isDegenerate = a==0;
		old_alpha = 0.0f;
		a_BranchDisp = 0.0f; 
		F = new myPointf();
	}
	
	/**
	 * manually build this transformation
	 * @param scale scale of transformation
	 * @param angle angle of rotational difference - must be +/- PI
	 * @param A, B transformation should map from A to B, for building center point
	 */
	public final void buildTransformation(float angle, float scale, myPointf A, myPointf B) {
		m=scale;
		old_alpha = a;
		a=angle;
		isDegenerate = a==0;
		//no branching, since this angle will be set to be +/- PI
		a_BranchDisp = 0.0f;
		startP.set(A);
		endP.set(B);
		
		F = spiralCenter(m,a,A,B);  
	}
	
	/**
	 * calcluate the optimal value for alpha given the passed alpha new value - the new alpha value that is closest to the old alpha value, based on branching 2piK offsets
	 * also need to update branching
	 * @return
	 */
	private void calcOptimalAlpha(float alphaNew, Base_ControlFlags flags) {	
		//if((old_old_alpha != old_alpha) || (a != old_alpha)) {
		if(flags.getIsDebug() && (name.contains("currMorphMap"))){//&& (!name.toLowerCase().contains("cpy"))) {// && ((old_old_alpha != old_alpha) || (a != old_alpha))) {
			//System.out.println(name+" : Old alpha : " + old_alpha + " alphaNew : " + alphaNew + " | a : " + a + " | a_BranchDisp : " + a_BranchDisp);
			//if(Math.abs(old_alpha - alphaNew)
			
		}		
	}
	
	/**
	 * build this transformation from two edge pt arrays
	 * 		 edges map e0[0] -> e1[0], e0[1]->e1[1]
	 * @param cntlPts
	 * @param flags any instance-specific flags to use to build transformation 
	 */	
	@Override
	public final void buildTransformation(myPointf[] e0, myPointf[] e1,  Base_ControlFlags flags) {
	    m = spiralScale(e0[0],e0[1],e1[0],e1[1]); 	
	    float alphaNew = spiralAngle(e0[0],e0[1],e1[0],e1[1]); // new values	
	    
		boolean reset = flags.getResetBranching();
		boolean optimizeAlpha = flags.getOptimizeAlpha();			//this is performed for morphing, to keep alpha close as possible to previous alpha
		
		old_alpha = a;	    	
		if(reset) {	    	
	    	a_BranchDisp = 0.0f;
	    } else {
	    	a_BranchDisp = calcAngleDelWthBranching(alphaNew + a_BranchDisp, a, a_BranchDisp);
	    }  
	    a=(alphaNew + a_BranchDisp);
	    
	    if(optimizeAlpha) {
	    	calcOptimalAlpha(alphaNew, flags);
	    }
	    isDegenerate = a==0;
		startP.set(e0[0]);
		endP.set(e1[0]);
	    
	    
	    F = spiralCenter(m,a,e0[0],e1[0]);  
	}//buildTransformation

	
	/**
	 * calculate branching displacement/offset due to atan2 limited range
	 * @param newAngle new angle, with current branching offset added
	 * @param oldAngle previous angle
	 * @param angleBranchOffset current branching offset
	 * @return modified branching offset
	 */
	protected float calcAngleDelWthBranching(float newAngle, float oldAngle, float angleBranchOffset) {
		float angleDel = newAngle - oldAngle;
	    if(Math.abs(angleDel+MyMathUtils.TWO_PI_F)<Math.abs(angleDel)) {angleDel+=MyMathUtils.TWO_PI_F; angleBranchOffset +=MyMathUtils.TWO_PI_F;}
	    if(Math.abs(angleDel-MyMathUtils.TWO_PI_F)<Math.abs(angleDel)) {angleDel-=MyMathUtils.TWO_PI_F; angleBranchOffset -=MyMathUtils.TWO_PI_F;}
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
		if(isDegenerate) {return new myPointf();}//if is degenerate then bypassing spiral
		myVectorf AB=new myVectorf(A,B), CD=new myVectorf(C,D), AC=new myVectorf(A,C);
		float mu = CD.magn/AB.magn, magSq=CD.magn*AB.magn;		
		myVectorf rAB = myVectorf._rotAroundAxis(AB, norm, MyMathUtils.HALF_PI_F);
		float c=AB._dot(CD)/magSq,	s=rAB._dot(CD)/magSq;
		float AB2 = AB._dot(AB), 
				a=AB._dot(AC)/AB2, 
				b=rAB._dot(AC)/AB2, 
				x=(a-mu*( a*c+b*s)), y=(b-mu*(-a*s+b*c)), d=1+mu*(mu-2*c);  if((c!=1)&&(mu!=1)) { x/=d; y/=d; };
		myPointf res = new myPointf(new myPointf(A,x,AB),y,rAB);
		return res;
	 }

	
	/**
	 * find fixed points given scale and angle - point is pivot that takes A to C
	 * @param scale |CD|/|AB|
	 * @param angle angle between AB and CD around planar normal - can't be 0
	 * @param A
	 * @param C
	 * @return
	 */
	public final myPointf spiralCenter(float scale, float angle, myPointf A, myPointf C) {         // new spiral center
		if(isDegenerate) {return new myPointf();}//if is degenerate then bypassing spiral
		//fixed point will move FA to FC and FB to FD
		float cos = (float) Math.cos(angle), sin = (float) Math.sin(angle);
		myVectorf CARaw=new myVectorf(C,A),
				CA = projVecToMapPlane(CARaw),
				U = projVecToMapPlane(scale*cos-1, scale*sin),
				rU = myVectorf._rotAroundAxis(U, norm, MyMathUtils.HALF_PI_F);		
		float uSqMag = U.sqMagn;
		myVectorf V = projVecToMapPlane(U._dot(CA)/uSqMag, rU._dot(CA)/uSqMag);
		myPointf res = myPointf._add(A, V);
		return res;
	}
	
	
	/**
	 * calc 1D transformation point for given point
	 * @param A base point to transform
	 * @param t time 
	 * @return
	 */	
	@Override
	public final myPointf transformPoint(myPointf A, float t) {
		if(isDegenerate) {//lerp
			//System.out.println("Transforming point with degenerate spiral.");
			myPointf disp = new myPointf(startP, t, endP);//(A-startP)  + (startP *(1-t) + endP * (t))
			return myPointf._add(myPointf._sub(A, startP), disp);			
		}//if is degenerate then bypassing spiral
		return new myPointf(F, (float) Math.pow(m, t), rotPtAroundF(A, F, t*a));}	
	
	/**
	 * calc 1D transformation vector for given vector
	 * @param A base point to transform
	 * @param t time 
	 * @return
	 */	
	public final myPointf transformVector(myVectorf V, float t) {
		if(isDegenerate) {//lerp
			myPointf disp = new myPointf(startP, t, endP);//(V-startP)  + (startP *(1-t) + endP * (t))
			return myPointf._add(myPointf._sub(V, startP), disp);
		}//if is degenerate then bypassing spiral
		return myVectorf._mult(myVectorf._rotAroundAxis(V, norm, t*a), (float) Math.pow(m, t));}
	
	
	/**
	 * configure and draw this similiarity's quantities on right side display
	 * @param ri
	 * @param yOff
	 * @param sideBarYDisp
	 * @return
	 */
	@Override
	public final float drawRightSideBarMenuDescr(IRenderInterface ri, float yOff, float sideBarYDisp, String coordName) {
		ri.translate(-10.0f, 0.0f, 0.0f);
		String[] dispVals = new String[]{String.format(Base_PolyMap.strPointDispFrmt8,m),String.format(Base_PolyMap.strPointDispFrmt8,a),String.format(Base_PolyMap.strPointDispFrmt8,a_BranchDisp)};	
		ri.pushMatState();
		showOffsetText_RightSideMenu(ri, ri.getClr(IRenderInterface.gui_White, 255), 3.5f, coordName + " :");
		for(int j=0;j<rtMenuDispType.length;++j) {			
			showOffsetText_RightSideMenu(ri, ri.getClr(IRenderInterface.gui_White, 255), 3.0f, rtMenuDispType[j]+" :");
			showOffsetText_RightSideMenu(ri, ri.getClr(IRenderInterface.gui_LightGreen, 255), 5.7f, dispVals[j]);
		}
		
//		ri.popMatState();
//		yOff += sideBarYDisp;
//		ri.translate(0.0f,sideBarYDisp, 0.0f);
//		ri.pushMatState();		
		if(isDegenerate) {
			showOffsetText_RightSideMenu(ri, ri.getClr(IRenderInterface.gui_White, 255), 3.5f, "No F");
		} else {
			
			showOffsetText_RightSideMenu(ri, ri.getClr(IRenderInterface.gui_White, 255), 3.5f, "F : ");
			showOffsetText_RightSideMenu(ri, ri.getClr(IRenderInterface.gui_LightCyan, 255), 3.0f, "("+F.toStrCSV(Base_PolyMap.strPointDispFrmt8)+")");
		}
		ri.popMatState();
			
		yOff += sideBarYDisp;
		ri.translate(10.0f,sideBarYDisp, 0.0f);
		//ri.translate(0.0f,sideBarYDisp, 0.0f);
		return yOff;
	};
	
	public final float drawFixedPoint(IRenderInterface ri, float yOff, float sideBarYDisp){
		ri.translate(10.0f, 0.0f, 0.0f);
		if(isDegenerate) {
			showOffsetText_RightSideMenu(ri, ri.getClr(IRenderInterface.gui_White, 255), 3.5f, "Theta == 0 caused degenerate spiral.");
		} else {
			ri.pushMatState();		
				showOffsetText_RightSideMenu(ri, ri.getClr(IRenderInterface.gui_White, 255), 5.5f, "Fixed Point : ");
				showOffsetText_RightSideMenu(ri, ri.getClr(IRenderInterface.gui_LightCyan, 255), 3.0f, "("+F.toStrCSV(Base_PolyMap.strPointDispFrmt8)+")");
			ri.popMatState();
		}
		yOff += sideBarYDisp;
		ri.translate(-10.0f,sideBarYDisp, 0.0f);
		return yOff;
	}	
	
	/////////////////////////
	// getters/setters
	
	//public final void setResetBranching(boolean _reset) {resetBranching = _reset;}
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
	public String getDebugStr_Indiv() {
		String dbgStr = " | a/s AB and DC : ";//old a : " +String.format(baseMap.strPointDispFrmt,old_a) +" |";
		dbgStr += " Angle : " +String.format(Base_PolyMap.strPointDispFrmt8,a) +" | Brnch :  "+String.format(Base_PolyMap.strPointDispFrmt8,a_BranchDisp);
		dbgStr += " | Scl : " + String.format(Base_PolyMap.strPointDispFrmt8,m) + " || F : " + F.toStrBrf();
		return dbgStr;
	}


	
}//class SpiralTransform
