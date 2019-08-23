package COTS_Morph_PKG.maps;

import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import base_UI_Objects.windowUI.myDispWindow;
import base_Utils_Objects.MyMathUtils;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

/**
 * COTS map based on Jarek's paper
 * @author john
 *
 */
public class COTSMap extends baseMap {
	/**
	 * Spiral scaling factors
	 */
	protected float mu = 1.0f, mv = 1.0f;
	/**
	 * spiral angles
	 */
	protected float au = 0.0f, av = 0.0f;
	protected float old_au = au, old_av = av;
	/**
	 * spiral center
	 */
	protected myPointf F;
	
	/**
	 * whether branching of angles should be reset or not
	 */
	protected boolean resetBranching = false;
	
	public COTSMap(COTS_MorphWin _win, myPointf[] _cntlPts, int _mapIdx, int[][] _pClrs, int _numCellPerSide) {	super(_win,_cntlPts, _mapIdx, _pClrs, _numCellPerSide, "COTS Map");	}
	
	/**
	 * Instance-class specific initialization
	 */	
	protected final void updateCntlPtVals_Indiv(boolean reset) {
	    mu = spiralScale(cntlPts[0],cntlPts[1],cntlPts[3],cntlPts[2]); 
	    mv = spiralScale(cntlPts[0],cntlPts[3],cntlPts[1],cntlPts[2]);
	    float pau=au, pav=av; // previous angles
	    setAnglesWithBranching(reset);
	    
	    F = spiralCenter(mu,au,cntlPts[0],cntlPts[3]);  
	    //F = spiralCenter(cntlPts[0],cntlPts[1],cntlPts[3],cntlPts[2]);  
	    if((this.mapIdx == 2) && ((Math.abs(pau - au) > .5) || (Math.abs(pav - av) > .5))) {
		    String debug = this.mapTitle + ": A : " + cntlPts[0].toStrBrf()+ " | a/s between AB and DC : old a : " +pau + "|" +au +" | " + mu+ " || a/s between AD and BC: old a : " + pav+ "|" +av +" | " + mv+ " || F : " + F.toStrBrf();
		    //for(int i=0;i<cntlPts.length;++i) { 	debug +="\n\t"+cntlPts[i].toStrBrf();   }
		    System.out.println(debug);
	    }
//	    a/s between AB and DC : 0.0021673138 | 1.0043322 || a/s between AD and BC : -0.004325232 | 1.0021533 || F : (-41836.438,-85003.484)
//		(651.9,431.3)
//		(1113.2999,430.3)
//		(1113.2999,893.7)
//		(649.9,893.7)
	}
	
	public void setResetBranching(boolean _reset) {resetBranching = _reset;}
	
	private void setAnglesWithBranching(boolean reset) {
	    float aun = spiralAngle2(cntlPts[0],cntlPts[1],cntlPts[3],cntlPts[2]), 
	    		avn = spiralAngle2(cntlPts[0],cntlPts[3],cntlPts[1],cntlPts[2]); // new values	    
	    if((resetBranching) || reset) {
	    	au = aun;
	    	av = avn;
	    	resetBranching = false;
	    } else {
	    	calcBranchAngles_Old(aun, avn);
	    }
	    
	}
	
	private void calcBranchAngles_Old(float aun, float avn) {
	    float pau=au, pav=av; // previous angles
	    float TWO_PI = MyMathUtils.twoPi_f;
	    
	    float aue = aun-au; // u difference		    		   
	    if(Math.abs(aue+TWO_PI)<Math.abs(aue)) {aue+=TWO_PI;}
	    if(Math.abs(aue-TWO_PI)<Math.abs(aue)) {aue-=TWO_PI;}
	    if(Math.abs(aue+TWO_PI)<Math.abs(aue)) {aue+=TWO_PI;}
	    if(Math.abs(aue-TWO_PI)<Math.abs(aue)) {aue-=TWO_PI;}
	     
	    float ave = avn-av; // v difference
	    if(Math.abs(ave+TWO_PI)<Math.abs(ave)) {ave+=TWO_PI;}
	    if(Math.abs(ave-TWO_PI)<Math.abs(ave)) {ave-=TWO_PI;}
	    if(Math.abs(ave+TWO_PI)<Math.abs(ave)) {ave+=TWO_PI;}
	    if(Math.abs(ave-TWO_PI)<Math.abs(ave)) {ave-=TWO_PI;}
	    
	    au=pau+aue;
	    av=pav+ave;
	}

	public final myPointf rotPtAroundF(myPointf P, float a) {
		myVectorf fpRaw = new myVectorf(F,P), fp = projVecToMapPlane(fpRaw);//myVectorf._add(myVectorf._mult(basisVecs[2], fpRaw._dot(basisVecs[2])), myVectorf._mult(basisVecs[1], fpRaw._dot(basisVecs[1])));
		
		double x = myVectorf._dot(fp,basisVecs[2]),	y = myVectorf._dot(fp,basisVecs[1]); 
		double c=Math.cos(a), s=Math.sin(a); 
		float iXVal = (float) (x*c-x-y*s), jYVal= (float) (x*s+y*c-y);			
		return myPointf._add(P,iXVal,basisVecs[2],jYVal,basisVecs[1]); 
	}; 
	public float spiralAngle2(myPointf A, myPointf B, myPointf C, myPointf D) {return myVectorf._angleBetween_Xprod(new myVectorf(A,B),new myVectorf(C,D), basisVecs[0]);}
	public float spiralScale(myPointf A, myPointf B, myPointf C, myPointf D) {return myPointf._dist(C,D)/ myPointf._dist(A,B);}
	
	// spiral given 4 points, AB and CD are edges corresponding through rotation
	public final myPointf spiralCenter(float m, float angle, myPointf A, myPointf C) {         // new spiral center
		//fixed point will move FA to FC and FB to FD
		float c=(float) Math.cos(angle), s=(float) Math.sin(angle);
		myVectorf CARaw=new myVectorf(C,A),
				CA = projVecToMapPlane(CARaw),
				U = projVecToMapPlane(m*c-1, m*s),
				rU = myVectorf._rotAroundAxis(U, basisVecs[0], MyMathUtils.halfPi_f);		
		float uSqMag = U.sqMagn;
		myVectorf V = projVecToMapPlane(U._dot(CA)/uSqMag, rU._dot(CA)/uSqMag);//myVectorf._add(myVectorf._mult(basisVecs[2], U._dot(CA)/uSqMag), myVectorf._mult(basisVecs[1],rU._dot(CA)/uSqMag));
		return new myPointf(A, V);
	}

	@Override
	protected boolean updateMapVals_Indiv(boolean hasBeenUpdated) {
		return hasBeenUpdated;
	}

	public final myPointf spiralPoint(myPointf A, float scl, float angle, float t) {
		return new myPointf(F, (float) Math.pow(scl, t), rotPtAroundF(A, t*angle));
	}	
	
	@Override
	public myPointf calcMapPt(float tx, float ty) {
		//return spiralPoint(spiralPoint(cntlPts[0],mu,au, tx),mv,av, ty);	
		return spiralPoint(spiralPoint(cntlPts[0],mv,av, tx),mu,au, ty);//needed to flip for image
	}
	
	/**
	 * instance-specific point drawing
	 * @param pa
	 */
	@Override
	protected void _drawPoints_Indiv() {
		pa.sphereDetail(5);
		pa.setStroke(polyColors[1], 255);
		_drawPt(F, sphereRad*1.5f);		
	}
	
	@Override
	protected final void _drawPointLabels_2D_Indiv() {
		_drawLabelAtPt(F,"Spiral Center : "+ F.toStrBrf(), 2.5f,-2.5f);		
	}
	
	@Override
	protected final void _drawPointLabels_3D_Indiv(myDispWindow animWin) {
		_drawLabelAtPt_UnSetCam(animWin,F,"Spiral Center : "+ F.toStrBrf(), 2.5f,-2.5f);
	}

	@Override
	protected void mseRelease_Indiv() {}
	
	public final float get_mu() {return mu;}
	public final float get_mv() {return mv;}

	public final float get_au() {return au;}
	public final float get_av() {return av;}


}

