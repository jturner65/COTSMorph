package COTS_Morph_PKG.morphs;

import COTS_Morph_PKG.managers.mapManagers.mapPairManager;
import COTS_Morph_PKG.managers.morphManagers.base.baseMorphManager;
import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.morphs.base.baseSimpleMorph;
import COTS_Morph_PKG.transform.SpiralTransform;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

public class QuadKeyEdgeSpiralMorph extends baseSimpleMorph {
	
	/**
	 * manage a transformation for each edge, between each key frame, for morphs which require this information
	 */
	protected SpiralTransform[] edgeTransforms;
	/**
	 * per side cntl point edges, in order, of each poly
	 */	
	protected myPointf[][] edgesA, edgesB;
	/**
	 * arrays of transformed edge start and end points
	 */
	protected myPointf[] edgeStPts, edgeEndPts;

	public QuadKeyEdgeSpiralMorph(COTS_MorphWin _win, baseMorphManager _morphMgr, mapPairManager _mapMgr, String _morphTitle) {	super(_win, _morphMgr, _mapMgr, _morphTitle);}
	
	//////////////////
	// map construction	
	/**
	 * this will perform initialization of morph-specific data before initial morph calc is performed, from base class ctor
	 */	
	@Override
	public void _endCtorInit() {//build edge transforms here
		//init edge transformations, for morphs that use them
		int size = mapA.getCntlPts().length;
		edgeTransforms = new SpiralTransform[size];
		edgesA = new myPointf[size][];		
		edgesB = new myPointf[size][];
		edgeStPts = new myPointf[edgeTransforms.length];
		edgeEndPts = new myPointf[edgeTransforms.length];
		
		myPointf[] aPts = mapA.getCntlPts(),bPts = mapB.getCntlPts();

		myVectorf n = mapA.basisVecs[0], I = mapA.basisVecs[2], J = mapA.basisVecs[1];
		for(int i=0;i<edgeTransforms.length;++i) {	
			int nextI = (i+1)%aPts.length;
			edgeTransforms[i] = new SpiralTransform("4EdgSprlMrph_["+i+","+nextI+"]",n,I,J);		
			edgesA[i]=new myPointf[] {aPts[i],aPts[nextI]};
			edgesB[i]=new myPointf[] {bPts[i],bPts[nextI]};	
			edgeStPts[i]=new myPointf();
			edgeEndPts[i]=new myPointf();	
		}	
	}

	@Override
	public void initCalcMorph_Indiv(float tA, float tB) {
		
		
	}//initCalcMorph_Indiv
	

	@Override
	protected void updateMorphValsFromUI_Indiv(mapUpdFromUIData upd) {
	}

	/**
	 * this function will conduct calculations between the two keyframe maps, if such calcs are used, whenever either is modified.  this is morph dependent
	 * @param _calledFrom : string denoting who called this method.  For debugging
	 */
	@Override
	public void mapCalcsAfterCntlPointsSet_Indiv(String _calledFrom) {
		if(edgeTransforms == null) {return;}		
		for(int i=0;i<edgeTransforms.length;++i) {
			edgeTransforms[i].buildTransformation(edgesA[i], edgesB[i], mapFlags[mapUpdateNoResetIDX]);
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
		//build points based on averages of endpoints from transformations				
		for(int i=0;i<edgeTransforms.length; ++i) {
		     edgeStPts[i].set(myPointf._add(edgeTransforms[i].transformPoint(edgesA[i][0], tB), myVectorf._mult(normDispTimeVec,tB)));
		     edgeEndPts[i].set(myPointf._add(edgeTransforms[i].transformPoint(edgesA[i][1], tB),myVectorf._mult(normDispTimeVec, tB)));		     
		}  		
		for(int i=0;i<edgeStPts.length;++i) {
			destPts[i]= new myPointf(edgeStPts[i],.5f, edgeEndPts[((i-1)+edgeEndPts.length)%edgeEndPts.length]);
		}
		//now set morph
		
	}//calcMorphBetweenTwoSetsOfPoints
	
//	protected final void calcMorphBetweenTwoSetsOfPoints(baseMap _map, myPointf[] Apts, myPointf[] Bpts, myPointf[] destPts, float tA, float tB) {
//		//build points based on averages of endpoints from transformations				
//		for(int i=0;i<edgeTransforms.length; ++i) {
//		     edgeStPts[i].set(myPointf._add(edgeTransforms[i].transformPoint(edgesA[i][0], tB), myVectorf._mult(normDispTimeVec,tB)));
//		     edgeEndPts[i].set(myPointf._add(edgeTransforms[i].transformPoint(edgesA[i][1], tB),myVectorf._mult(normDispTimeVec, tB)));		     
//		}  
//		myPointf[] cntlPts = new myPointf[edgeStPts.length];
//		
//		for(int i=0;i<edgeStPts.length;++i) {
//			cntlPts[i]= new myPointf(edgeStPts[i],tA, edgeEndPts[((i-1)+edgeEndPts.length)%edgeEndPts.length]);
//		}
//		_map.setCntlPts(cntlPts, mapFlags[mapUpdateNoResetIDX]);
//		
//		//now set morph
//		
//		
//		
//	}//calcMorphBetweenTwoSetsOfPoints
	
	private static final String[] transLbls = {"AB","BC","CD","DA"};

	@Override
	public float drawMorphRtSdMenuDescr_Indiv(float yOff, float sideBarYDisp) {
		for(int i=0;i<edgeTransforms.length; ++i) {
			yOff += edgeTransforms[i].drawRightSideBarMenuDescr(pa, yOff, sideBarYDisp, transLbls[i]);
			pa.translate(0.0f,sideBarYDisp, 0.0f);	
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
			for(int i=0;i<edgeTransforms.length;++i) {			
				myPointf F = new myPointf(edgeTransforms[i].getCenterPoint()); 				
				F.set(myPointf._add(F, myVectorf._mult(normDispTimeVec, morphT)));
				win._drawLabelAtPt(F,"Spiral Center for edges ["+i+","+((i+1)%edgeTransforms.length)+"] : (" + F.toStrCSV(baseMap.strPointDispFrmt8)+")", 2.5f,-2.5f);
			}		
		}
		if(debug) {
			pa.pushMatrix();pa.pushStyle();	
			pa.fill(0,0,0,255);
			pa.stroke(0,0,0,255);
			pa.strokeWeight(1.0f);
			for(int i=0;i<edgeTransforms.length;++i) {
				for(float t=0.0f; t<=1.0f;t+=.1f) {
					myPointf ea0 = myPointf._add(edgeTransforms[i].transformPoint(edgesA[i][0], 1.0f-t), myVectorf._mult(normDispTimeVec,1.0f-t)),
							ea1 = myPointf._add(edgeTransforms[i].transformPoint(edgesA[i][1], 1.0f-t),myVectorf._mult(normDispTimeVec, 1.0f-t));
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
			for(int i=0;i<edgeTransforms.length;++i) {			
				myPointf F = new myPointf(edgeTransforms[i].getCenterPoint()); 				
				F.set(myPointf._add(F, myVectorf._mult(normDispTimeVec, morphT)));
				mapMgr._drawPt(F, baseMap.sphereRad*1.5f);	
			}
			pa.popStyle();pa.popMatrix();	
		}
	}//drawMorphSpecificValues

	@Override
	public void resetAllBranching() {
		for(int i=0;i<edgeTransforms.length;++i) {			edgeTransforms[i].setBranching(0.0f);	}		
		mapCalcsAfterCntlPointsSet("self::resetAllBranching", true);
	}

}//class QuadKeyEdgeSpiralMorph
