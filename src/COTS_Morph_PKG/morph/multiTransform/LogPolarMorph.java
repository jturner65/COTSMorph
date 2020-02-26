package COTS_Morph_PKG.morph.multiTransform;

import java.util.ArrayList;

import COTS_Morph_PKG.map.base.baseMap;
import COTS_Morph_PKG.mapManager.mapPairManager;
import COTS_Morph_PKG.morph.multiTransform.base.baseMultiTransformMorphs;
import COTS_Morph_PKG.transformer.spiral.LogPolarTransformer;
import COTS_Morph_PKG.transformer.spiral.base.baseSpiralTransformer;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;


/**
 * 3-Implement a LMP (Log Polar Morph), where, for each corner pair (say A0 and A1) we compute the arithmetic average rotation angle (A0B0^A1B1+ A0C0^A1C1+  A0D0^A1D1)/3   
 * and geometric average magnification ratio (cubic root of the product of ratios |A1B1| / | A0B0| …
 * Then compute At as a point on the spiral from A0 to A1 with that average angle and ratio.
 * @author john
 *
 */
public class LogPolarMorph extends baseMultiTransformMorphs {
	/**
	 * offset index for where map b points start in morph - should == # of cntl points in map
	 */
	private int bPointOffsetIDX;
	public LogPolarMorph(COTS_MorphWin _win, mapPairManager _mapMgr, baseMap _mapA, baseMap _mapB,int _morphTypeIDX, String _morphTitle) {super(_win, _mapMgr,_mapA, _mapB, _morphTypeIDX, _morphTitle);}
	public LogPolarMorph(LogPolarMorph _otr) {super(_otr);}
	//////////////////
	// map construction	

	/**
	 * called in baseMultiTransformMorphs in _endCtorInit 
	 */
	@Override
	protected myPointf[][] getCornerPtAras() {
		myPointf[] aPts = mapA.getCntlPts(),bPts = mapB.getCntlPts();
		bPointOffsetIDX = aPts.length;
		//need combination of every edge from every point
		//for each cntl point, array of all edges from that point, for both a and b
		ArrayList<myPointf> pList= new ArrayList<myPointf>();
		myPointf[][] tmpPtAra = new myPointf[aPts.length][];		
		for(int i=0;i<aPts.length;++i) {
			pList.clear();
			for(int j=0;j<aPts.length;++j) {	pList.add(aPts[(j+i)%aPts.length]);		}
			for(int j=0;j<aPts.length;++j) {	pList.add(bPts[(j+i)%bPts.length]);		}			
			tmpPtAra[i]=pList.toArray(new myPointf[0]);
		}		
		return tmpPtAra;
	}

	@Override
	protected baseSpiralTransformer buildSimilarity(int i) {
		int numTransforms = (bPointOffsetIDX-1);
		return new LogPolarTransformer(morphTitle+"_"+i, mapA.basisVecs[0], mapA.basisVecs[2],mapA.basisVecs[1],numTransforms);		
	}
	
	/**
	 * this function will conduct calculations between the two keyframe maps, if such calcs are used, whenever either is modified.  this is morph dependent
	 * @param _calledFrom : string denoting who called this method.  For debugging
	 */
	@Override
	public void mapCalcsAfterCntlPointsSet_Indiv(String _calledFrom) {	
		for(int i=0;i<transforms.length ;++i) {
			transforms[i].deriveSimilarityFromCntlPts(crnrPtAras[i], mapFlags[mapUpdateNoResetIDX]);
		}
		
	}//mapCalcsAfterCntlPointsSet_Indiv

	@Override
	protected final boolean transformsAreBad() {
		return transforms==null;// || true;
	};

	@Override
	protected void _calcMorphCntlPoints_MultiTransIndiv(myPointf[] Apts, myPointf[] Bpts, myPointf[] destPts, float tA, float tB) {
		for(int i=0;i<transforms.length ;++i) {
			myPointf res = transforms[i].transformPoint( Apts[i],tB);			
			destPts[i]= myPointf._add(res, myVectorf._mult(normDispTimeVec, tB));//calcMorph_Point(tA, Apts[i], tB, Bpts[i]);					
		}	
	}


	
	@Override
	public final int calcMorph_Integer(float tA, int AVal, float tB, int BVal) { return (int) ((tA*AVal) + (tB*BVal));};
	@Override
	public float calcMorph_Float(float tA, float AVal, float tB, float BVal) {		return (tA*AVal) + (tB*BVal);}
	@Override
	public double calcMorph_Double(float tA, double AVal, float tB, double BVal) {		return (tA*AVal) + (tB*BVal);}

	
	/**
	 * this will draw instancing morph-specific data on screen 
	 */
	@Override
	public void drawMorphSpecificValues(boolean debug, boolean _isFill, boolean _drawCircles) {
		pa.pushMatState();	
		pa.setFill(0,0,0,255);
		pa.setStroke(0,0,0,255);
		pa.setStrokeWt(1.0f);
		// ...
		pa.popMatState();	
	}
}//class drawnLineMorph
