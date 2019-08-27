package COTS_Morph_PKG.maps;

import COTS_Morph_PKG.maps.base.COTSData;
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
	 * data holding COTS map control values
	 */
	protected COTSData cots;
	

	public COTSMap(COTS_MorphWin _win, myPointf[] _cntlPts, int _mapIdx, int[][] _pClrs, int _numCellPerSide) {	
		super(_win,_cntlPts, _mapIdx, _pClrs, _numCellPerSide, "COTS Map");	
		cots = new COTSData();
		updateMapFromCntlPtVals_Indiv( true);
	}
	
	/**
	 * Instance-class specific initialization
	 */	
	@Override
	protected final void updateMapFromCntlPtVals_Indiv(boolean reset) {
		if(null==cots) {return;}

		cots.updateCntlPoints(cntlPts, reset, basisVecs[0], basisVecs[2],basisVecs[1]);		
		
	    if((this.mapIdx == 0) || (this.mapIdx == 1)){
		    String debug = this.mapTitle + " reset : " + reset + " | share : " + this.shouldShareBranching + " | "+ cots.getDebugStr()+ " | A : " + cntlPts[0].toStrBrf();
		    //for(int i=0;i<cntlPts.length;++i) { 	debug +="\n\t"+cntlPts[i].toStrBrf();   }
		    System.out.println(debug);
	    }
//	    a/s between AB and DC : 0.0021673138 | 1.0043322 || a/s between AD and BC : -0.004325232 | 1.0021533 || F : (-41836.438,-85003.484)
//		(651.9,431.3)
//		(1113.2999,430.3)
//		(1113.2999,893.7)
//		(649.9,893.7)
	}
	

	@Override
	public void updateMeWithMapVals(baseMap otrMap) {
		cots.setBranching(((COTSMap)otrMap).cots.getBranching());
		updateMapFromOtrMapVals(false);
	}

	
	@Override
	public myPointf calcMapPt(float tx, float ty) {
		//tx interpolates between "vertical" edges, scale and angle, ty interpolates between "horizontal" edges, scale and angle
		//mapPoint(myPointf A, float tx, float ty, myVectorf I, myVectorf J)
		return cots.mapPoint(cntlPts[0], tx, ty, basisVecs[2], basisVecs[1]);
		//return spiralPoint(spiralPoint(cntlPts[0],mv,av, tx,basisVecs[2], basisVecs[1]),mu,au, ty,basisVecs[2], basisVecs[1]);
	}
	
	@Override
	protected boolean updateMapVals_Indiv() {		boolean hasBeenUpdated = false;		return hasBeenUpdated;}
	
	/**
	 * instance-specific point drawing
	 * @param pa
	 */
	@Override
	protected void _drawPoints_Indiv() {
		pa.sphereDetail(5);
		pa.setStroke(polyColors[1], 255);
		_drawPt(cots.F, sphereRad*1.5f);		
	}
	
	@Override
	protected final void _drawPointLabels_2D_Indiv() {
		_drawLabelAtPt(cots.F,"Spiral Center : "+ cots.F.toStrBrf(), 2.5f,-2.5f);		
	}
	
	@Override
	protected final void _drawPointLabels_3D_Indiv(myDispWindow animWin) {
		_drawLabelAtPt_UnSetCam(animWin,cots.F,"Spiral Center : "+ cots.F.toStrBrf(), 2.5f,-2.5f);
	}

	@Override
	protected void mseRelease_Indiv() {}

}

