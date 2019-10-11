package COTS_Morph_PKG.morph;

import java.util.ArrayList;

import COTS_Morph_PKG.analysis.registration.mapRegDist;
import COTS_Morph_PKG.map.base.baseMap;
import COTS_Morph_PKG.mapManager.mapPairManager;
import COTS_Morph_PKG.morph.base.baseMorph;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_UI_Objects.IRenderInterface;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

/**
 * this morph will manage 4 independent morphing calculations for each of the 4 desirable morphing characteristics (shape, orientation, translation, scale)
 * @author john
 *
 */
public class CompoundMorph extends baseMorph {
	
	public static final int
		shapeMorphIDX 		= 0,
		orientMorphIDX 		= 1,
		transMorphIDX		= 2,
		scaleMorphIDX		= 3,
		scalarValMorphIDX	= 4;		//for scalar calculations
	public static final int numMorphFeatures = 5;
	/**
	 * individual morph types to compose into final compound morph
	 */
	public static final String[] morphFtrNames = new String[] {
		"Shape Morph",
		"Orientation Morph",
		"Translation Morph",
		"Scale Morph",
		"Scalar Value Morph"	
	};
	
	/**
	 * array of current morphs to use - 1st index is type of morph, 2nd idx is 
	 */

	protected baseMorph[][] morphsAvailable;
	
	/**
	 * class to perform registrations and reg-related calculations, if necessary for a particular morph, for each type of constituent morph
	 */
	protected mapRegDist[] mapRegDistCalcs;

	
	/**
	 * index of which morph to use for each type of morph 
	 */
	protected int[] currMorphToUseIDX;
	/**
	 * array of morph maps to use for each of numMorphs type of morph results
	 */
	protected baseMap[] _currPerMorphMaps;
	
	/**
	 * 
	 * @param _win
	 * @param _morphMgr
	 * @param _mapMgr
	 * @param _morphTitle
	 */	
	public CompoundMorph(COTS_MorphWin _win, mapPairManager _mapMgr, baseMap _mapA, baseMap _mapB,int _morphTypeIDX,  String _morphTitle) {super(_win, _mapMgr,_mapA, _mapB, _morphTypeIDX,_morphTitle);}
	public CompoundMorph(CompoundMorph _otr) {super(_otr);}

	@Override
	protected final void _endCtorInit() {
		mapRegDistCalcs = new mapRegDist[numMorphFeatures];
		currMorphToUseIDX = new int[numMorphFeatures];
		_currPerMorphMaps = new baseMap[numMorphFeatures];
		morphsAvailable = new baseMorph[numMorphFeatures][mapPairManager.morphTypes.length-1];//don't allow for compound of compound morphs
		for(int i=0;i<numMorphFeatures;++i) {
			currMorphToUseIDX[i]=0;
			_currPerMorphMaps[i]=getCopyOfMap(mapA, "Map for "+morphFtrNames[i]);
			mapRegDistCalcs[i]=new mapRegDist(mapMgr, mapA, mapB);
			ArrayList<baseMorph> tmpNewMorphs = new ArrayList<baseMorph>();
			for(int j=0;j<mapPairManager.morphTypes.length;++j) {
				if(j==mapPairManager.CompoundMorphIDX) {continue;}
				//tmpNewMorphs.add((baseSimpleMorph) mapMgr.buildMorph(j));
				tmpNewMorphs.add(mapMgr.buildMorph(j, mapA, mapB));
			}
			morphsAvailable[i] = tmpNewMorphs.toArray(new baseMorph[0]);
		}
	}


	@Override
	protected void updateMorphValsFromUI_Indiv(mapUpdFromUIData upd) {
		currMorphToUseIDX[shapeMorphIDX] = upd.getCurrMorphType_ShapeIDX();       
		currMorphToUseIDX[orientMorphIDX] = upd.getCurrMorphType_OrientIDX();       
		currMorphToUseIDX[transMorphIDX] = upd.getCurrMorphType_COVPathIDX();       
		currMorphToUseIDX[scaleMorphIDX] = upd.getCurrMorphType_SizeIDX();      
		currMorphToUseIDX[scalarValMorphIDX] = upd.getCurrMorphType_COVPathIDX();  	
	}

	@Override
	public int calcMorph_Integer(float tA, int AVal, float tB, int BVal) {return morphsAvailable[scalarValMorphIDX][currMorphToUseIDX[scalarValMorphIDX]].calcMorph_Integer(tA,AVal,tB,BVal);}
	@Override
	public float calcMorph_Float(float tA, float AVal, float tB, float BVal) {return morphsAvailable[scalarValMorphIDX][currMorphToUseIDX[scalarValMorphIDX]].calcMorph_Float(tA,AVal,tB,BVal);}
	@Override
	public double calcMorph_Double(float tA, double AVal, float tB, double BVal) {return morphsAvailable[scalarValMorphIDX][currMorphToUseIDX[scalarValMorphIDX]].calcMorph_Double(tA,AVal,tB,BVal);}

	@Override
	public void calcMorphAndApplyToMap(baseMap _curMorphMap, float tA, float tB) {
		myPointf[] aCntlPts = mapA.getCntlPts(), bCntlPts = mapB.getCntlPts(); 
		//this is shape with points deregistered (aligned) against mapA on translation, rotation and scale
		myPointf[] resPts = getDeRegShapeMapCntlPts(aCntlPts, bCntlPts,tA, tB);
		
		_curMorphMap.setCntlPts(resPts, mapFlags[mapUpdateNoResetIDX]);		
		//_curMorphMap.registerMeToVals(myVectorf._mult(distBetweenAllMaps[transMorphIDX],-1.0f), new float[] {-angleScalesAllMaps[orientMorphIDX][0],1.0f/angleScalesAllMaps[scaleMorphIDX][1]});
		_curMorphMap.registerMeToVals(myVectorf._mult(mapRegDistCalcs[transMorphIDX].getDispBetweenMaps(),-1.0f), new float[] {-mapRegDistCalcs[orientMorphIDX].getAngle(),1.0f/mapRegDistCalcs[scaleMorphIDX].getScale()});
		
		_curMorphMap.setCurMorphTVal(tB);
	}

	@Override
	public void calcMorphBetweenTwoSetsOfCntlPoints(myPointf[] aCntlPts, myPointf[] bCntlPts, myPointf[] destPts, float tA,	float tB) {
		//this is shape with points deregistered (aligned) against mapA on translation, rotation and scale
		myPointf[] resPts = getDeRegShapeMapCntlPts(aCntlPts, bCntlPts, tA, tB);
		baseMap tmpMap = getCopyOfMap(mapA, "Tmp Copy of Map A");
		
		tmpMap.setCntlPts(resPts, mapFlags[mapUpdateNoResetIDX]);
		
		tmpMap.registerMeToVals(myVectorf._mult(mapRegDistCalcs[transMorphIDX].getDispBetweenMaps(),-1.0f), new float[] {-mapRegDistCalcs[orientMorphIDX].getAngle(),1.0f/mapRegDistCalcs[scaleMorphIDX].getScale()});
		destPts = tmpMap.getCntlPts();	
	}
	/**
	 * Calculate the control points of the shape map, and also return the arrays holding COV distance vector, angles and scales
	 * @param aCntlPts
	 * @param bCntlPts
	 * @param distBetweenAllMaps
	 * @param angleScalesAllMaps
	 * @param tA
	 * @param tB
	 * @return
	 */
	private myPointf[] getDeRegShapeMapCntlPts(myPointf[] aCntlPts, myPointf[] bCntlPts, float tA, float tB) {
		myPointf[][] newPts = new myPointf[numMorphFeatures][aCntlPts.length];		
		for(int i=0;i<_currPerMorphMaps.length;++i) {			
			int morphIDXToUse = currMorphToUseIDX[i];
			//System.out.println("Morph Feature : " + i + " = " + morphFtrNames[i] + " using idx : " + morphIDXToUse + " : " +baseMorphManager.morphTypes[morphIDXToUse] );
			morphsAvailable[i][morphIDXToUse].calcMorphBetweenTwoSetsOfCntlPoints(aCntlPts, bCntlPts, newPts[i], tA, tB);			
			_currPerMorphMaps[i].setCntlPts( newPts[i], mapFlags[mapUpdateNoResetIDX]);
			mapRegDistCalcs[i].setMapsAndCalc(_currPerMorphMaps[i], mapA);
			//mapA.findDifferenceToMe(_currPerMorphMaps[i], distBetweenAllMaps[i], angleScalesAllMaps[i]);
		}
		//remove its registration values to be left only with shape
		_currPerMorphMaps[shapeMorphIDX].registerMeToVals(mapRegDistCalcs[shapeMorphIDX].getDispBetweenMaps(), new float[] {mapRegDistCalcs[shapeMorphIDX].getAngle(),mapRegDistCalcs[shapeMorphIDX].getScale()});
		//these are control points from map which is desired shape
		myPointf[] resPts = _currPerMorphMaps[shapeMorphIDX].getCntlPts_Copy();		
		return resPts;
	}

	@Override
	public void mapCalcsAfterCntlPointsSet_Indiv(String _calledFrom) {
		for(int i=0;i<numMorphFeatures;++i) {
			morphsAvailable[i][currMorphToUseIDX[i]].mapCalcsAfterCntlPointsSet_Indiv(_calledFrom);
		}
	}

	@Override
	public float drawMorphRtSdMenuDescr_Indiv(float yOff, float sideBarYDisp) {
		for(int i=0;i<_currPerMorphMaps.length;++i) {
			pa.pushMatrix();pa.pushStyle();
				pa.translate(-10.0f,0.0f,0.0f);
				pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Green, 255), 6.5f, "Morph For :");
				pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 6.5f, morphFtrNames[i]);
			pa.popStyle();pa.popMatrix();		
			yOff += sideBarYDisp;
			pa.translate(0.0f,sideBarYDisp, 0.0f);			
			yOff =  morphsAvailable[i][currMorphToUseIDX[i]].drawMorphTitle(yOff, sideBarYDisp);
			yOff = morphsAvailable[i][currMorphToUseIDX[i]].drawMorphRtSdMenuDescr_Indiv(yOff, sideBarYDisp);
			yOff += sideBarYDisp;
			pa.translate(0.0f,sideBarYDisp, 0.0f);			

		}
		return yOff;
	}

	@Override
	public void drawMorphSpecificValues(boolean debug, boolean drawCntlPts, boolean showLbls) {
		pa.pushMatrix();pa.pushStyle();	
		pa.fill(0,0,0,255);
		pa.stroke(0,0,0,255);
		pa.strokeWeight(1.0f);
		for(int i=0;i<numMorphFeatures;++i) {
			morphsAvailable[i][currMorphToUseIDX[i]].drawMorphSpecificValues(debug, drawCntlPts, showLbls);
		}
		pa.popStyle();pa.popMatrix();		
	}

	
	@Override
	public void resetAllBranching_Indiv() {
		for(int i=0;i<numMorphFeatures;++i) {for(int j=0;j<morphsAvailable[i].length;++j) {morphsAvailable[i][j].resetAllBranching();}}
		mapCalcsAfterCntlPointsSet("this::resetAllBranching_Indiv", true, false);	//possibly redundant?  TODO
	}

}//class CompoundMorph
