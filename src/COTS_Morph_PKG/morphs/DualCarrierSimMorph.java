package COTS_Morph_PKG.morphs;

import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.morphs.base.baseMorph;
import COTS_Morph_PKG.similarities.CarrierSimilarity;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

public class DualCarrierSimMorph extends baseMorph {
	/**
	 * similarity that will act as carrier
	 */
	private CarrierSimilarity carrier;
	

	public DualCarrierSimMorph(COTS_MorphWin _win, baseMap _a, baseMap _b, baseMap _morphMap, int _morphScope) {
		super(_win, _a, _b, _morphMap, _morphScope, "Dual Carrier Similiarty");
		carrier = new CarrierSimilarity(mapA.basisVecs[0],mapA.basisVecs[2],mapA.basisVecs[1]);
		calcMorph();	
	}

	@Override
	protected void initCalcMorph_Indiv(float tA, float tB) {
		if(null==carrier) {return;}
		myPointf[] mapADiag = mapA.getCntlPtDiagonal(), mapBDiag = mapB.getCntlPtDiagonal();
		
		carrier.deriveSimilarityFromCntlPts(new myPointf[] {mapADiag[0],mapADiag[1],mapBDiag[0],mapBDiag[1]}, true);

	}
	@Override
	protected final int calcMorph_Integer(float tA, int AVal, float tB, int BVal) { return (int) ((tA*AVal) + (tB*BVal));}
	@Override
	protected float calcMorph_Float(float tA, float AVal, float tB, float BVal) {		return (tA*AVal) + (tB*BVal);}
	@Override
	protected double calcMorph_Double(float tA, double AVal, float tB, double BVal) {		return (tA*AVal) + (tB*BVal);}

	@Override
	protected myPointf calcMorph_Point(float tA, myPointf AVal, float tB, myPointf BVal) {
		if(null==carrier) {return new myPointf(AVal);}
		//return carrier.mapPoint(AVal,tA, tB);                                                                                                    
		//return myPointf._add(myPointf._mult(carrier.transformPoint(AVal,tB), tA),myPointf._mult(carrier.transformPoint(BVal,tA), tB));		 
		return myPointf._add(myPointf._mult(carrier.transformPoint(AVal,tB), tA),myPointf._mult(carrier.transformPoint(BVal,tA), tB));	
	}


	@Override
	protected float drawMorphRtSdMenuDescr_Indiv(float yOff, float sideBarYDisp) {
		carrier.drawRightSideBarMenuDescr(pa, yOff, sideBarYDisp);
		yOff += sideBarYDisp;
		pa.translate(0.0f,sideBarYDisp, 0.0f);	
		
		return yOff;
	}

}
