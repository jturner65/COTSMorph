package COTS_Morph_PKG.maps;

import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.similarities.COTS_Similarity;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import base_UI_Objects.windowUI.myDispWindow;
import base_Utils_Objects.vectorObjs.myPointf;

/**
 * COTS map based on Jarek's paper
 * @author john
 *
 */
public class COTSMap extends baseMap {
	
	/**
	 * data holding COTS map control values
	 */
	protected COTS_Similarity cots;
	

	public COTSMap(COTS_MorphWin _win, myPointf[] _cntlPts, int _mapIdx, int _mapTypeIdx, int[][] _pClrs, int _numCellPerSide, String _mapTitle) {	
		super(_win,_cntlPts,_mapIdx, _mapTypeIdx, _pClrs,_numCellPerSide, _mapTitle);
		cots = new COTS_Similarity(basisVecs[0], basisVecs[2], basisVecs[1]);
		updateMapFromCntlPtVals_Indiv( true);
	}
	public COTSMap(COTSMap _otrMap) {
		super(_otrMap);
		cots = new COTS_Similarity(_otrMap.cots);
		updateMapFromCntlPtVals_Indiv( true);
	}
	
	/**
	 * Instance-class specific initialization
	 */	
	@Override
	protected final void updateMapFromCntlPtVals_Indiv(boolean reset) {
		if(null==cots) {return;}		//need this check since cots similarity not built before this is first called

		cots.deriveSimilarityFromCntlPts(cntlPts, reset);		
		
//	    if((this.mapIdx == 0) || (this.mapIdx == 1)){
//		    String debug = this.mapTitle + " reset : " + reset + " | share : " + this.shouldShareBranching + " | "+ cots.getDebugStr()+ " | A : " + cntlPts[0].toStrBrf();
//		    //for(int i=0;i<cntlPts.length;++i) { 	debug +="\n\t"+cntlPts[i].toStrBrf();   }
//		    System.out.println(debug);
//	    }
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
		return cots.mapPoint(cntlPts[0], tx, ty);
	}
	
	@Override
	public myPointf getCenterPoint() {
		return cots.getF();
	}

	
	@Override
	protected boolean updateMapVals_Indiv() {		boolean hasBeenUpdated = false;		return hasBeenUpdated;}
	
	@Override
	protected float drawRightSideBarMenuDescr_Indiv(float yOff, float sideBarYDisp) {
		yOff = cots.drawRightSideBarMenuDescr(pa, yOff, sideBarYDisp);
		return yOff;
	}
	
	/**
	 * instance-specific point drawing
	 * @param pa
	 */
	@Override
	protected void _drawPoints_Indiv() {
		pa.sphereDetail(5);
		pa.setStroke(polyColors[1], 255);
		_drawPt(cots.getF(), sphereRad*1.5f);		
	}
	
	@Override
	protected final void _drawPointLabels_2D_Indiv() {
		_drawLabelAtPt(cots.getF(),"Spiral Center : "+ cots.getF().toStrBrf(), 2.5f,-2.5f);		
	}
	
	@Override
	protected final void _drawPointLabels_3D_Indiv(myDispWindow animWin) {
		_drawLabelAtPt_UnSetCam(animWin,cots.getF(),"Spiral Center : "+ cots.getF().toStrBrf(), 2.5f,-2.5f);
	}

	@Override
	protected void mseRelease_Indiv() {}

}//COTSMap

