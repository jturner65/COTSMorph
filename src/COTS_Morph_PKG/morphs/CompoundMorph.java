package COTS_Morph_PKG.morphs;

import java.util.ArrayList;

import COTS_Morph_PKG.managers.mapManagers.mapPairManager;
import COTS_Morph_PKG.managers.morphManagers.base.baseMorphManager;
import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.morphs.base.baseMorph;
import COTS_Morph_PKG.morphs.base.baseSimpleMorph;
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

	protected baseSimpleMorph[][] morphsAvailable;
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
	
	public CompoundMorph(COTS_MorphWin _win, baseMorphManager _morphMgr, mapPairManager _mapMgr, String _morphTitle) {
		super(_win, _morphMgr, _mapMgr, _morphTitle);
	}

	@Override
	public void _endCtorInit() {
		currMorphToUseIDX = new int[numMorphFeatures];
		_currPerMorphMaps = new baseMap[numMorphFeatures];
		morphsAvailable = new baseSimpleMorph[numMorphFeatures][baseMorphManager.morphTypes.length-1];//don't allow for compound of compound morphs
		for(int i=0;i<numMorphFeatures;++i) {
			currMorphToUseIDX[i]=0;
			_currPerMorphMaps[i]=getCopyOfMap(mapA, "Map for "+morphFtrNames[i]);
			ArrayList<baseSimpleMorph> tmpNewMorphs = new ArrayList<baseSimpleMorph>();
			for(int j=0;j<baseMorphManager.morphTypes.length;++j) {
				if(j==baseMorphManager.CompoundMorphIDX) {continue;}
				tmpNewMorphs.add((baseSimpleMorph) mapMgr.buildMorph(j));
			}
			morphsAvailable[i] = tmpNewMorphs.toArray(new baseSimpleMorph[0]);
		}
	}

	@Override
	public void initCalcMorph_Indiv(float tA, float tB) {
		

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
		myPointf[][] newPts = new myPointf[numMorphFeatures][aCntlPts.length];
		myVectorf[] distBetweenAllMaps = new myVectorf[_currPerMorphMaps.length];
		float[][] angleScalesAllMaps = new float[_currPerMorphMaps.length][];
		
		
		for(int i=0;i<_currPerMorphMaps.length;++i) {
			distBetweenAllMaps[i] = new myVectorf();
			angleScalesAllMaps[i] = new float[] {0.0f,0.0f};
			
			int morphIDXToUse = currMorphToUseIDX[i];
			//System.out.println("Morph Feature : " + i + " = " + morphFtrNames[i] + " using idx : " + morphIDXToUse + " : " +baseMorphManager.morphTypes[morphIDXToUse] );
			morphsAvailable[i][morphIDXToUse].calcMorphBetweenTwoSetsOfCntlPoints(aCntlPts, bCntlPts, newPts[i], tA, tB);			
			_currPerMorphMaps[i].setCntlPts( newPts[i], mapFlags[mapUpdateNoResetIDX]);
			mapA.findDifferenceToMe(_currPerMorphMaps[i], distBetweenAllMaps[i], angleScalesAllMaps[i]);
		}
		//remove its registration values to be left only with shape
		_currPerMorphMaps[shapeMorphIDX].registerMeToVals(distBetweenAllMaps[shapeMorphIDX], new float[] {angleScalesAllMaps[shapeMorphIDX][0],angleScalesAllMaps[shapeMorphIDX][1]});
		//this is shape with points on mapA
		myPointf[] resPts = _currPerMorphMaps[shapeMorphIDX].getCntlPts_Copy();		
		_curMorphMap.setCntlPts(resPts, mapFlags[mapUpdateNoResetIDX]);		
		_curMorphMap.registerMeToVals(myVectorf._mult(distBetweenAllMaps[transMorphIDX],-1.0f), new float[] {-angleScalesAllMaps[orientMorphIDX][0],1.0f/angleScalesAllMaps[scaleMorphIDX][1]});
	}

	@Override
	public void calcMorphBetweenTwoSetsOfCntlPoints(myPointf[] Apts, myPointf[] Bpts, myPointf[] destPts, float tA,	float tB) {
		myPointf[][] newPts = new myPointf[numMorphFeatures][Apts.length];
		myVectorf[] distBetweenAllMaps = new myVectorf[_currPerMorphMaps.length];
		float[][] angleScalesAllMaps = new float[_currPerMorphMaps.length][];	
		
		for(int i=0;i<_currPerMorphMaps.length;++i) {
			distBetweenAllMaps[i] = new myVectorf();
			angleScalesAllMaps[i] = new float[] {0.0f,0.0f};
			
			int morphIDXToUse = currMorphToUseIDX[i];
			//System.out.println("Morph Feature : " + i + " = " + morphFtrNames[i] + " using idx : " + morphIDXToUse + " : " +baseMorphManager.morphTypes[morphIDXToUse] );
			morphsAvailable[i][morphIDXToUse].calcMorphBetweenTwoSetsOfCntlPoints(Apts, Bpts, newPts[i], tA, tB);			
			_currPerMorphMaps[i].setCntlPts( newPts[i], mapFlags[mapUpdateNoResetIDX]);
			mapA.findDifferenceToMe(_currPerMorphMaps[i], distBetweenAllMaps[i], angleScalesAllMaps[i]);
		}
		//remove its registration values to be left only with shape
		_currPerMorphMaps[shapeMorphIDX].registerMeToVals(distBetweenAllMaps[shapeMorphIDX], new float[] {angleScalesAllMaps[shapeMorphIDX][0],angleScalesAllMaps[shapeMorphIDX][1]});
		//this is shape with points on mapA
		myPointf[] resPts = _currPerMorphMaps[shapeMorphIDX].getCntlPts_Copy();		
		baseMap tmpMap = getCopyOfMap(mapA, "Tmp Copy of Map A");
		
		tmpMap.setCntlPts(resPts, mapFlags[mapUpdateNoResetIDX]);
		
		tmpMap.registerMeToVals(myVectorf._mult(distBetweenAllMaps[transMorphIDX],-1.0f), new float[] {-angleScalesAllMaps[orientMorphIDX][0],1.0f/angleScalesAllMaps[scaleMorphIDX][1]});
		destPts = tmpMap.getCntlPts();	
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
	public void resetAllBranching() {
		for(int i=0;i<numMorphFeatures;++i) {
			for(int j=0;j<morphsAvailable[i].length;++j) {
				morphsAvailable[i][j].resetAllBranching();
			}
		}
	}

}//class CompoundMorph
