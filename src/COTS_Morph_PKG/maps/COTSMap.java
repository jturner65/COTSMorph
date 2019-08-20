package COTS_Morph_PKG.maps;

import COTS_Morph_PKG.maps.base.baseMap;
import base_UI_Objects.my_procApplet;
import base_Utils_Objects.MyMathUtils;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

/**
 * COTS map based on Jarek's code
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
	/**
	 * spiral center
	 */
	protected myPointf F;
	
	/**
	 * whether branching of angles should be reset or not
	 */
	protected boolean resetBranching = false;
	
	public COTSMap(myPointf[] _cntlPts, int _mapIdx, int[][] _pClrs, int _numCellPerSide) {	super(_cntlPts, _mapIdx, _pClrs, _numCellPerSide, "COTS Map");	}
	
	/**
	 * Instance-class specific initialization
	 */	
	protected final void updateCntlPtVals(boolean reset) {
	    mu = spiralScale(cntlPts[0],cntlPts[1],cntlPts[3],cntlPts[2]); 
	    mv = spiralScale(cntlPts[0],cntlPts[3],cntlPts[1],cntlPts[2]);

	    setAnglesWithBranching(reset);
	    
	    F = spiralCenter(mu,au,cntlPts[0],cntlPts[3]);  
	    //F = spiralCenter(cntlPts[0],cntlPts[1],cntlPts[3],cntlPts[2]);  
//	    String debug = this.mapTitle + ": a/s between AB and DC : " +au +" | " + mu+ " || a/s between AD and BC : " +av +" | " + mv+ " || F : " + F.toStrBrf();
//	    for(int i=0;i<cntlPts.length;++i) {
//	    	debug +="\n\t"+cntlPts[i].toStrBrf();
//	    }
//	    System.out.println(debug);
	    
	    //System.exit(0);
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
	    
	}

	public final myPointf rotPtAroundF(myPointf P, float a) {
		myVectorf fpRaw = new myVectorf(F,P),
				fp = myVectorf._add(myVectorf._mult(basisVecs[2], fpRaw._dot(basisVecs[2])), myVectorf._mult(basisVecs[1], fpRaw._dot(basisVecs[1])));
		
		double x = myVectorf._dot(fp,basisVecs[2]), 
				y = myVectorf._dot(fp,basisVecs[1]); 
		double c=Math.cos(a), s=Math.sin(a); 
		float iXVal = (float) (x*c-x-y*s), jYVal= (float) (x*s+y*c-y);			
		return myPointf._add(P,iXVal,basisVecs[2],jYVal,basisVecs[1]); 
	}; 
	public float spiralAngle2(myPointf A, myPointf B, myPointf C, myPointf D) {return myVectorf._angleBetween_Xprod(new myVectorf(A,B),new myVectorf(C,D));}
	public float spiralScale(myPointf A, myPointf B, myPointf C, myPointf D) {return myPointf._dist(C,D)/ myPointf._dist(A,B);}
	
	// spiral given 4 points, AB and CD are edges corresponding through rotation
	public final myPointf spiralCenter(float m, float angle, myPointf A, myPointf C) {         // new spiral center
		//fixed point will move FA to FC and FB to FD
		float c=(float) Math.cos(angle), s=(float) Math.sin(angle);
		myVectorf 
				CARaw=new myVectorf(C,A),
				//rCARaw = myVectorf._rotAroundAxis(CARaw, basisVecs[0], MyMathUtils.halfPi_f),	
				CA = myVectorf._add(myVectorf._mult(basisVecs[2], CARaw._dot(basisVecs[2])), myVectorf._mult(basisVecs[1], CARaw._dot(basisVecs[1]))),
				//CA =new myVectorf(C,A),
				U = myVectorf._add(myVectorf._mult(basisVecs[2], m*c-1), myVectorf._mult(basisVecs[1],m*s));
		myVectorf rU = myVectorf._rotAroundAxis(U, basisVecs[0], MyMathUtils.halfPi_f);		
		float uSqMag = U.sqMagn;
		myVectorf V = myVectorf._add(myVectorf._mult(basisVecs[2], U._dot(CA)/uSqMag), myVectorf._mult(basisVecs[1],rU._dot(CA)/uSqMag));
		return new myPointf(A, V);
	}

	@Override
	protected boolean updateMapVals_Indiv(boolean hasBeenUpdated) {
		// TODO Auto-generated method stub
		return hasBeenUpdated;
	}

	public final myPointf spiralPoint(myPointf A, float scl, float angle, float t) {
		myPointf rotA = rotPtAroundF(A, t*angle);
		return new myPointf(F, (float) Math.pow(scl, t), rotA);
	}
	
	
	@Override
	public myPointf calcMapPt(float tx, float ty) {
		// TODO Auto-generated method stub
		return spiralPoint(spiralPoint(cntlPts[0],mu,au, tx),mv,av, ty);
	}
	
	/**
	 * instance-specific point drawing
	 * @param pa
	 */
	@Override
	protected void _drawPoints_Indiv(my_procApplet pa) {
		pa.sphereDetail(5);
		pa.setStroke(polyColors[1], 255);
		_drawPt(pa, F, sphereRad*1.5f);
		
	}


	@Override
	protected void mseRelease_Indiv() {
	}


}
