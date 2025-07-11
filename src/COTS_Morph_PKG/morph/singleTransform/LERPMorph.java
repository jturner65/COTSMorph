package COTS_Morph_PKG.morph.singleTransform;

import COTS_Morph_PKG.map.base.Base_PolyMap;
import COTS_Morph_PKG.mapManager.mapPairManager;
import COTS_Morph_PKG.morph.singleTransform.base.baseSingleTransformMorph;
import COTS_Morph_PKG.transformer.spiral.base.baseSpiralTransformer;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapUpdFromUIData;

public class LERPMorph extends baseSingleTransformMorph {
    public LERPMorph(COTS_MorphWin _win, mapPairManager _mapMgr, Base_PolyMap _mapA, Base_PolyMap _mapB,int _morphTypeIDX, String _morphTitle) {super(_win, _mapMgr,_mapA, _mapB, _morphTypeIDX, _morphTitle);transform =null;}//null transform forces lerp interpolation    
    
    public LERPMorph(LERPMorph _otr) {super(_otr);transform =null;}
    
    @Override
    protected void updateMorphValsFromUI_Indiv(mapUpdFromUIData upd) {}
    
    @Override
    protected final baseSpiralTransformer buildSimilarity() {    return null;        };
    
    @Override
    public final int calcMorph_Integer(float tA, int AVal, float tB, int BVal) { return (int) ((tA*AVal) + (tB*BVal));}
    @Override
    public float calcMorph_Float(float tA, float AVal, float tB, float BVal) {        return (tA*AVal) + (tB*BVal);}
    @Override
    public double calcMorph_Double(float tA, double AVal, float tB, double BVal) {        return (tA*AVal) + (tB*BVal);}
    
    /**
     * this function will conduct calculations between the two keyframe maps, if such calcs are used, whenever either is modified.  this is morph dependent
     * @param _calledFrom : string denoting who called this method.  For debugging
     */
    @Override
    public void mapCalcsAfterCntlPointsSet_Indiv(String _calledFrom) {    }

    /**
     * this will draw instancing morph-specific data on screen 
     */
    @Override
    public void drawMorphSpecificValues(boolean debug, boolean _isFill, boolean _drawCircles) {
        ri.pushMatState();    
        ri.setFill(0,0,0,255);
        ri.setStroke(0,0,0,255);
        ri.setStrokeWt(1.0f);
        
        ri.popMatState();    
    }

}//class LERPMorph
