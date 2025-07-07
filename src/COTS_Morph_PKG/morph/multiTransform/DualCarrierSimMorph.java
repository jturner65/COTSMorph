package COTS_Morph_PKG.morph.multiTransform;

import COTS_Morph_PKG.map.base.Base_PolyMap;
import COTS_Morph_PKG.mapManager.mapPairManager;
import COTS_Morph_PKG.morph.multiTransform.base.baseMultiTransformMorphs;
import COTS_Morph_PKG.transformer.spiral.CarrierTransformer;
import COTS_Morph_PKG.transformer.spiral.base.baseSpiralTransformer;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;

public class DualCarrierSimMorph extends baseMultiTransformMorphs {

    public DualCarrierSimMorph(COTS_MorphWin _win, mapPairManager _mapMgr, Base_PolyMap _mapA, Base_PolyMap _mapB,int _morphTypeIDX, String _morphTitle) {super(_win, _mapMgr,_mapA, _mapB, _morphTypeIDX, _morphTitle);}
    public DualCarrierSimMorph(DualCarrierSimMorph _otr) {super(_otr);}
    @Override
    protected final baseSpiralTransformer buildSimilarity(int i) {
        return new CarrierTransformer(morphTitle+"_"+i,mapA.basisVecs[0],mapA.basisVecs[2],mapA.basisVecs[1]);        
    };
        
    @Override
    protected final myPointf[][] getCornerPtAras(){
        myPointf[][] res = new myPointf[2][];
        myPointf[] mapADiag = mapA.getCntlPtDiagonal(),mapBDiag = mapB.getCntlPtDiagonal(); 
        res[0] = new myPointf[] {mapADiag[0],mapADiag[1],mapBDiag[0],mapBDiag[1]};
        myPointf[] mapAOffDiag =  mapA.getCntlPtOffDiagonal(), mapBOffDiag =  mapB.getCntlPtOffDiagonal();
        res[1] = new myPointf[] {mapAOffDiag[0],mapAOffDiag[1],mapBOffDiag[0],mapBOffDiag[1]};
        return res;
    }

    
    /**
     * this function will conduct calculations between the two keyframe maps, if such calcs are used, whenever either is modified.  this is morph dependent
     * @param _calledFrom : string denoting who called this method.  For debugging
     */
    @Override
    public void mapCalcsAfterCntlPointsSet_Indiv(String _calledFrom) {
        if(null==transforms) {return;}
        for(int i=0;i<transforms.length;++i) {
            transforms[i].deriveSimilarityFromCntlPts(crnrPtAras[i], mapFlags[mapUpdateNoResetIDX]);
        }                    
    }


    @Override
    public final int calcMorph_Integer(float tA, int AVal, float tB, int BVal) { return (int) ((tA*AVal) + (tB*BVal));}
    @Override
    public float calcMorph_Float(float tA, float AVal, float tB, float BVal) {        return (tA*AVal) + (tB*BVal);}
    @Override
    public double calcMorph_Double(float tA, double AVal, float tB, double BVal) {        return (tA*AVal) + (tB*BVal);}
    @Override
    protected final boolean transformsAreBad() {    return transforms==null;};

    /**
     * calcluate this morph algorithm between Apts and Bpts, putting result in destPts
     * @param Apts
     * @param Bpts
     * @param destPts
     * @param tA
     * @param tB
     */
    @Override
    protected final void _calcMorphCntlPoints_MultiTransIndiv(myPointf[] Apts, myPointf[] Bpts, myPointf[] destPts, float tA, float tB) {
        int carrierIdx = 0;
        for(int i=0;i<Apts.length;++i) {    
            carrierIdx = i%2;
            //destPts[i]=  myPointf._add(myPointf._mult(transforms[carrierIdx].transformPoint(Apts[i], tB), tA),myPointf._mult(transforms[carrierIdx].transformPoint(Bpts[i], tA), tB));
            //destPts[i]=  transforms[carrierIdx].transformPoint(Apts[i], tB);
            myPointf res = transforms[carrierIdx].transformPoint( Apts[i],tB);            
            destPts[i]= myPointf._add(res, myVectorf._mult(normDispTimeVec, tB));//calcMorph_Point(tA, Apts[i], tB, Bpts[i]);        

            
            //destPts[i]= myPointf._add(res, myVectorf._mult(normDispTimeVec, tB));//calcMorph_Point(tA, Apts[i], tB, Bpts[i]);    
//            destPts[i]= (null==carrier) ? 
//                    myPointf._add(new myPointf(Apts[i]),myVectorf._mult(normDispTimeVec, tB)) : 
//                    myPointf._add(myPointf._mult(carrier.transformPoint(Apts[i],tB), tA),myPointf._mult(carrier.transformPoint(Bpts[i],tA), tB));
        }        
    }

    /**
     * this will draw instancing morph-specific data on screen 
     */
    @Override
    public void drawMorphSpecificValues(boolean debug, boolean _isFill, boolean _drawCircles) {
        ri.pushMatState();    
        ri.setFill(0,0,0,255);
        ri.setStroke(0,0,0,255);
        ri.setStrokeWt(1.0f);
        // ...
        ri.popMatState();    
    }


}//class DualCarrierSimMorph
