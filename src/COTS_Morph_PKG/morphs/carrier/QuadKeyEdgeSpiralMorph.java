package COTS_Morph_PKG.morphs.carrier;

import COTS_Morph_PKG.managers.mapManagers.mapPairManager;
import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.morphs.base.baseSimpleMorph;
import COTS_Morph_PKG.similarities.SpiralSimilarity;
import COTS_Morph_PKG.similarities.base.baseSimilarity;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

public class QuadKeyEdgeSpiralMorph extends baseSimpleMorph {

	public QuadKeyEdgeSpiralMorph(COTS_MorphWin _win, mapPairManager _mapMgr, String _morphTitle) {	super(_win, _mapMgr, _morphTitle);}
	
	//////////////////
	// map construction	

	/**
	 * called in baseSimpleMorph in _endCtorInit - since we override that method, we can ignore these functions
	 */
	@Override
	protected myPointf[][] getDiagPtsAras() {
		myPointf[] aPts = mapA.getCntlPts(),bPts = mapB.getCntlPts();
		myPointf[][] tmpPtAra = new myPointf[aPts.length][];		
		for(int i=0;i<aPts.length;++i) {	
			int nextI = (i+1)%aPts.length;
			tmpPtAra[i]=new myPointf[] {aPts[i],aPts[nextI],bPts[i],bPts[nextI]};
		}		
		return tmpPtAra;
	}

	@Override
	protected baseSimilarity buildSimilarity(int i) {
		return new SpiralSimilarity("4EdgSprlMrph_["+i+","+((i+1)%transforms.length)+"]", mapA.basisVecs[0], mapA.basisVecs[2],mapA.basisVecs[1]);		
	}
	
	
	@Override
	protected void updateMorphValsFromUI_Indiv(mapUpdFromUIData upd) {}

	/**
	 * this function will conduct calculations between the two keyframe maps, if such calcs are used, whenever either is modified.  this is morph dependent
	 * @param _calledFrom : string denoting who called this method.  For debugging
	 */
	@Override
	public void mapCalcsAfterCntlPointsSet_Indiv(String _calledFrom) {
		if(transforms == null) {return;}		
		for(int i=0;i<transforms.length;++i) {
			//transforms[i].deriveSimilarityFromCntlPts(new myPointf[] {edgesA[i][0],edgesA[i][1],edgesB[i][0],edgesB[i][1]}, mapFlags[mapUpdateNoResetIDX]);
			transforms[i].deriveSimilarityFromCntlPts(crnrPtAras[i], mapFlags[mapUpdateNoResetIDX]);
			
			//edgeTransforms[i].buildTransformation(edgesA[i], edgesB[i], mapFlags[mapUpdateNoResetIDX]);
			//win.getMsgObj().dispInfoMessage("SpiralEdgeMapMgr", "mapCalcsAfterCntlPointsSet::"+_calledFrom, "i: " + i + " | F : " +edgeTransforms[i].getCenterPoint().toStrBrf() + "| edge A : " + toStringEdge(edgesA[i])+ "| edge B : " + toStringEdge(edgesB[i]) );
		}
	}

	@Override
	public final int calcMorph_Integer(float tA, int AVal, float tB, int BVal) { return (int) ((tA*AVal) + (tB*BVal));};
	@Override
	public float calcMorph_Float(float tA, float AVal, float tB, float BVal) {		return (tA*AVal) + (tB*BVal);}
	@Override
	public double calcMorph_Double(float tA, double AVal, float tB, double BVal) {		return (tA*AVal) + (tB*BVal);}
	
	/**
	 * calcluate this morph algorithm between Apts and Bpts, putting result in destPts
	 * @param Apts
	 * @param Bpts
	 * @param destPts
	 * @param tA
	 * @param tB
	 */
	@Override
	public final void calcMorphBetweenTwoSetsOfCntlPoints(myPointf[] Apts, myPointf[] Bpts, myPointf[] destPts, float tA, float tB) {
		if(transforms == null) {return;}		
		myPointf[] edgeStPts = new myPointf[transforms.length];
		myPointf[] edgeEndPts = new myPointf[transforms.length];
		//build points based on averages of endpoints from transformations				
		for(int i=0;i<transforms.length; ++i) {
			myVectorf I = myVectorf._mult(normDispTimeVec,tB);  	     
		    edgeStPts[i] = (myPointf._add(transforms[i].transformPoint(crnrPtAras[i][0], tB), I));
		    edgeEndPts[i] = (myPointf._add(transforms[i].transformPoint(crnrPtAras[i][1], tB),I));		     
		}  		
		//average edge points to equate to transform
		for(int i=0;i<edgeStPts.length;++i) {
			destPts[i]= new myPointf(edgeStPts[i],.5f, edgeEndPts[((i-1)+edgeEndPts.length)%edgeEndPts.length]);
		}
		//now set morph
		
	}//calcMorphBetweenTwoSetsOfPoints
	
	private static final String[] transLbls = {"AB","BC","CD","DA"};

	@Override
	public float drawMorphRtSdMenuDescr_Indiv(float yOff, float sideBarYDisp) {
		for(int i=0;i<transforms.length; ++i) {
			yOff += transforms[i].drawRightSideBarMenuDescr(pa, yOff, sideBarYDisp, new String[] {transLbls[i]+":"});			//wackiness due to how transformations vs similarities are being constructed
		}
		return yOff;
	}
	
	/**
	 * map-type specific drawing
	 * @param drawTexture
	 * @param drawOrtho
	 * @param drawEdgeLines
	 * @param drawCntlPts
	 * @param showLbls  
	 */
	@Override
	public final void drawMorphSpecificValues(boolean debug, boolean drawCntlPts, boolean showLbls) {
		if(showLbls) {
			for(int i=0;i<transforms.length;++i) {			
				myPointf F = new myPointf(transforms[i].getF()); 				
				F.set(myPointf._add(F, myVectorf._mult(normDispTimeVec, morphT)));
				win._drawLabelAtPt(F,"Spiral Center for edges ["+i+","+((i+1)%transforms.length)+"] : (" + F.toStrCSV(baseMap.strPointDispFrmt8)+")", 2.5f,-2.5f);
			}		
		}
		if(debug) {
			pa.pushMatrix();pa.pushStyle();	
			pa.fill(0,0,0,255);
			pa.stroke(0,0,0,255);
			pa.strokeWeight(1.0f);
			for(int i=0;i<transforms.length;++i) {
				for(float t=0.0f; t<=1.0f;t+=.1f) {
//					myPointf ea0 = myPointf._add(transforms[i].transformPoint(edgesA[i][0], 1.0f-t), myVectorf._mult(normDispTimeVec,1.0f-t)),
//							ea1 = myPointf._add(transforms[i].transformPoint(edgesA[i][1], 1.0f-t),myVectorf._mult(normDispTimeVec, 1.0f-t));
					myPointf ea0 = myPointf._add(transforms[i].transformPoint(crnrPtAras[i][0], 1.0f-t), myVectorf._mult(normDispTimeVec,1.0f-t)),
							ea1 = myPointf._add(transforms[i].transformPoint(crnrPtAras[i][1], 1.0f-t),myVectorf._mult(normDispTimeVec, 1.0f-t));
					pa.line(ea0, ea1);
				}
			}	
			pa.popStyle();pa.popMatrix();	
		}
		if(drawCntlPts) {
			pa.pushMatrix();pa.pushStyle();	
			pa.fill(0,0,0,255);
			pa.stroke(0,0,0,255);
			pa.strokeWeight(1.0f);
			pa.sphereDetail(5);
			pa.setStroke(mapA.getPolyColors()[1], 255);
			for(int i=0;i<transforms.length;++i) {			
				myPointf F = new myPointf(transforms[i].getF()); 				
				F.set(myPointf._add(F, myVectorf._mult(normDispTimeVec, morphT)));
				mapMgr._drawPt(F, baseMap.sphereRad*1.5f);	
			}
			pa.popStyle();pa.popMatrix();	
		}
	}//drawMorphSpecificValues


}//class QuadKeyEdgeSpiralMorph
