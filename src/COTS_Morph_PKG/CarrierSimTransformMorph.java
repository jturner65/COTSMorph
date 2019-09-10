package COTS_Morph_PKG;

import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.morphs.CarrierSimDiagMorph;
import COTS_Morph_PKG.morphs.base.baseMorph;
import COTS_Morph_PKG.similarities.CarrierSimilarity;
import COTS_Morph_PKG.similarities.SpiralSimilarityWithTranslation;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

public class CarrierSimTransformMorph extends baseMorph {
	/**
	 * similarity that will act as transformation
	 */
	protected SpiralSimilarityWithTranslation transform;
	
	/**
	 * vector to add to displacement to manage morphing between frames in 3D
	 */
	protected myVectorf normDispTimeVec;

	public CarrierSimTransformMorph(COTS_MorphWin _win, baseMap _a, baseMap _b, baseMap _morphMap, int _morphScope) {
		super(_win, _a, _b, _morphMap, _morphScope, "Carrier Sim w/Transformation");
		normDispTimeVec = new myVectorf(mapA.getCOV(), mapB.getCOV());
		normDispTimeVec = myVectorf._mult(mapA.basisVecs[0], normDispTimeVec._dot(mapA.basisVecs[0]));		
		transform = new SpiralSimilarityWithTranslation(mapA.basisVecs[0],mapA.basisVecs[2],mapA.basisVecs[1]);		
		calcMorph();	
	}
	

	@Override
	protected void initCalcMorph_Indiv(float tA, float tB) {
		if(null==transform) {return;}
		boolean resetBranching= true;
		myVectorf dispBetweenMaps = new myVectorf();
		float[] angleAndScale = new float[2];
		mapB.findDifferenceToMe(mapA, dispBetweenMaps, angleAndScale);
		
		myPointf mapPtA = new myPointf(mapA.getCOV()),mapPtB = new myPointf(mapB.getCOV());

		transform.deriveSimilarityFromCntlPts(new myPointf[] {dispBetweenMaps, new myPointf(angleAndScale[0],angleAndScale[1],0.0f),mapPtA,mapPtB},resetBranching);
	}

	@Override
	protected final int calcMorph_Integer(float tA, int AVal, float tB, int BVal) { return (int) ((tA*AVal) + (tB*BVal));}
	@Override
	protected float calcMorph_Float(float tA, float AVal, float tB, float BVal) {		return (tA*AVal) + (tB*BVal);}
	@Override
	protected final double calcMorph_Double(float tA, double AVal, float tB, double BVal) {		return (tA*AVal) + (tB*BVal);}

	@Override
	protected myPointf calcMorph_Point(float tA, myPointf AVal, float tB, myPointf BVal) {
		if(null==transform) {return new myPointf(AVal);}
		myPointf res = transform.transformPoint(AVal,tB);//carrier.transformPoint(AVal,tB);
		return myPointf._add(res, myVectorf._mult(normDispTimeVec, tB));
		
	}

	@Override
	protected float drawMorphRtSdMenuDescr_Indiv(float yOff, float sideBarYDisp) {	
		transform.drawRightSideBarMenuDescr(pa, yOff, sideBarYDisp);
		yOff += sideBarYDisp;
		pa.translate(0.0f,sideBarYDisp, 0.0f);	
		
		return yOff;
	}

}
