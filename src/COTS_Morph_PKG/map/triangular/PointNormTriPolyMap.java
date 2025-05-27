package COTS_Morph_PKG.map.triangular;

import java.util.TreeMap;

import COTS_Morph_PKG.map.base.Base_PolyMap;
import COTS_Morph_PKG.map.triangular.base.Base_TriangleMap;
import COTS_Morph_PKG.mapManager.mapPairManager;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import COTS_Morph_PKG.utils.controlFlags.base.Base_ControlFlags;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;

public class PointNormTriPolyMap extends Base_TriangleMap {
	/**
	 * normals at each control point - used to set other map's cntl point location
	 */
	protected myVectorf[] cntlPtNorms, origCntlPtNorms;
	/**
	 * points to display on UI for enpoints of control point normal vectors
	 */
	protected myPointf[] cntlPtNormEndPts;
	
	public myPointf[] otrCntlPts;
	
		//array of labels to use for control points
	protected String[] cntlPtNormLbls;	
	
	/**
	 * editable Q point
	 */
	public myPointf Qpt;
	public float[] qPointNBC;
	
	protected final float normDispVecOffset = 50.0f;
	
	public PointNormTriPolyMap(COTS_MorphWin _win, mapPairManager _mapMgr,myPointf[] _cntlPts, int _mapIdx, int _mapTypeIDX, int[][] _pClrs,mapUpdFromUIData _currUIVals,  boolean _isKeyFrame, String _mapTitle) {
		super(_win, _mapMgr, _cntlPts, _mapIdx, _mapTypeIDX, _pClrs, _currUIVals, _isKeyFrame,_mapTitle);
		 _initCntlPtNormVecs();
	}

	public PointNormTriPolyMap(String _mapTitle, PointNormTriPolyMap _otr) {
		super(_mapTitle, _otr);	
		_initCntlPtNormVecs();
	
	}
	
	private void _initCntlPtNormVecs() {
		if(cntlPtNorms != null) {return;}
		Qpt = new myPointf(cntlPts[0], .5f, cntlPtCOV);
		qPointNBC = calcNormBaryCoords(Qpt);	
		cntlPtNorms = new myVectorf[cntlPts.length];
		origCntlPtNorms = new myVectorf[cntlPts.length];
		cntlPtNormLbls = new String[cntlPtNorms.length];
		cntlPtNormEndPts = new myPointf[cntlPtNorms.length];
		otrCntlPts = new myPointf[cntlPtNorms.length];
		for(int i=0;i<cntlPtNorms.length;++i) {	
			cntlPtNorms[i] = new myVectorf();
			origCntlPtNorms[i] = new myVectorf();
			cntlPtNormLbls[i]="" + ((char)(i+'U'));	
			cntlPtNormEndPts[i] = new myPointf();
			otrCntlPts[i] = new myPointf();
		}	
	}
	
	/**
	 * only call if otrMap has been set
	 */
	private boolean origNormsSaved = false;;
	protected void _setCntlPtNorms() {
		if(otrMap == null) {return;}
		myPointf[] otrCntlPts = otrMap.getCntlPts();
		if(cntlPtNorms == null) {			_initCntlPtNormVecs();	}
		for(int i=0;i<cntlPtNorms.length;++i) {	
			cntlPtNorms[i].set(cntlPts[i], otrCntlPts[i]);
			cntlPtNorms[i]._normalize();
			cntlPtNormEndPts[i].set(myPointf._add(cntlPts[i], normDispVecOffset, cntlPtNorms[i]));
		}
		if(!origNormsSaved) {
			for(int i=0;i<cntlPtNorms.length;++i) {			origCntlPtNorms[i].set(new myVectorf(cntlPtNorms[i]));	}
			origNormsSaved=true;
		}
	}
	
	/**
	 * Instance-class specific initialization
	 */	
	@Override
	protected final void updateMapFromCntlPtVals_Indiv(Base_ControlFlags flags) {
		boolean reset = flags.getResetBranching();
		//first move other map's control points
		if(otrMap == null) {			return;		}
		//myPointf[] otrMapCntlPts = otrMap.getCntlPts();
		if(isKeyFrameMap) {
			if(reset) {
				for(int i=0;i<cntlPtNorms.length;++i) {	
					cntlPtNorms[i].set(origCntlPtNorms[i]);
					cntlPtNormEndPts[i].set(myPointf._add(cntlPts[i], normDispVecOffset, cntlPtNorms[i]));
				}		
				Qpt = new myPointf(cntlPts[0], .5f, cntlPtCOV);
				qPointNBC = calcNormBaryCoords(Qpt);	
				
			}
			setOtrMapVals(flags);
		}		
	}
	
	private void setOtrMapVals(Base_ControlFlags flags) {
		for(int i=0;i<cntlPts.length;++i) {
			myPointf pt = otrMap.findPointInMyPlane(cntlPts[i], cntlPtNorms[i]);
			otrCntlPts[i].set(pt);
		}		
		otrMap.updateMeWithMapVals(this,flags);		
	}

	
	@Override
	public void updateMeWithMapVals(Base_PolyMap otrMap, Base_ControlFlags flags) {
		if(this.mapIdx==1) {
			for(int i=0;i<cntlPts.length;++i) {		cntlPts[i].set(((PointNormTriPolyMap)otrMap).otrCntlPts[i]);		}
		}
		Qpt.set(calcPointFromNormBaryCoords(((PointNormTriPolyMap)otrMap).qPointNBC));
		qPointNBC = calcNormBaryCoords(Qpt);	
		_setCntlPtNorms();
		//find center of control points
		finalizeValsAfterCntlPtsMod();		
	}

	@Override
	protected boolean updateMapVals_FromUI_Indiv(mapUpdFromUIData upd) {
		boolean hasBeenUpdated = false;		
		return hasBeenUpdated;
	}
	
	@Override
	protected void registerMeToVals_PreIndiv(myVectorf dispBetweenMaps, float[] angleAndScale) {}
	@Override
	public final float calcTtlSurfaceArea() {	
		return mgr.calcAreaOfPolyInPlane(cntlPts, distPlanarPt, basisVecs[0]);
	}//calcTtlSurfaceArea	
	
	/**
	 * Return array of all morph-relevant cntl/info points for this map.
	 * Call if morph map -after-  morph is calced.  
	 * Include COV and possibly F point, if COTS or other spiral-based map
	 * @return
	 */
	@Override
	public final void getAllMorphCntlPts_Indiv(myPointf[] res) {
		res[cntlPts.length]= new myPointf(this.cntlPtCOV);
	};
	@Override
	public final int getNumAllMorphCntlPts() {	return cntlPts.length + 1;};
	/**
	 * instance specific values should be added here
	 * @param map
	 */
	@Override
	public final void getTrajAnalysisKeys_Indiv(TreeMap<String, Integer> map) {
		int numTtlPts = getNumAllMorphCntlPts();
		map.put(COV_Label, numTtlPts-1);
	}

	
	/////////////////////
	// draw routines	
	@Override
	protected final void _drawCntlPoints_Indiv(boolean isCurMap, int detail) {
		if(!isKeyFrameMap) {return;}		//don't want to draw normals for non-keyframe
		if(detail<COTS_MorphWin.drawMapDet_CntlPts_COV_EdgePts_IDX) {return;}
		for(int i=0;i<cntlPtNorms.length;++i) {
			myPointf p = cntlPts[i];
			mgr._drawVec(p, cntlPtNormEndPts[i], new int[] {0,0,0,255}, (p.equals(currMseModCntlPt) && isCurMap ? 2.0f*sphereRad : sphereRad));
		}
		ri.pushMatState();
		if(mapIdx==0) {		ri.setStroke(255, 0 ,255,255);	} else {ri.setStroke(0, 255,255,255);}
		
		ri.setStrokeWt(1.0f);
		for(int i=0;i<otrCntlPts.length;++i) {
			myPointf p = otrCntlPts[i];
			mgr._drawPt(p, sphereRad);
		}
		ri.popMatState();
		
		ri.pushMatState();	
		ri.setStroke(255,255,0,255);
		ri.setStrokeWt(1.0f);		
		mgr._drawPt(Qpt, sphereRad);
		if((otrMap != null) && (mapIdx == 0)) {
			ri.drawLine(((PointNormTriPolyMap)otrMap).Qpt, Qpt);
		}
		ri.popMatState();
	}
	
	@Override
	protected final void _drawPointLabels_Indiv(int detail) {
		if(detail<COTS_MorphWin.drawMapDet_CntlPts_COV_EdgePts_IDX) {return;}
			for(int i=0;i<cntlPtNorms.length;++i) {
				myPointf p = cntlPtNormEndPts[i];
				win._drawLabelAtPt(p,   cntlPtNormLbls[i]+ "_"+mapIdx + " : " + cntlPtNorms[i].toStrBrf(), 2.5f,-2.5f);
			}
			win._drawLabelAtPt(Qpt, (mapIdx == 0 ? "P":"Q") + " : [" +String.format("%.3f",qPointNBC[0]) +","+String.format("%.3f",qPointNBC[1])+ "," + String.format("%.3f",qPointNBC[2])+"] : " + Qpt.toStrBrf(),  2.5f,-2.5f);
	}
	@Override
	protected float drawRtSdMenuDescr_Indiv(float yOff, float sideBarYDisp) {
		return yOff;
	}

	
	/////////////////////
	// set/get	
	@Override
	protected final void setOtrMap_Indiv() {_setCntlPtNorms();	}
	
	/**
	 * set instance-specific flags
	 * @param flags
	 */
	@Override
	public final void setFlags(boolean[] flags) {};
	/**
	 * manage mouse/map drag movement for child-class specific fields
	 */
	@Override	
	protected final void findClosestCntlPt_Indiv(myPointf _mseLoc, myPointf _rayOrigin, myVectorf _rayDir, TreeMap<Float, myPointf> ptsByDist) {
		if(this.mapIdx == 0) {return;}
		for(int i=0;i<cntlPtNormEndPts.length;++i) {		ptsByDist.put(win.findDistToPtOrRay(_mseLoc, cntlPtNormEndPts[i],_rayOrigin,_rayDir), cntlPtNormEndPts[i]);}	
		ptsByDist.put(win.findDistToPtOrRay(_mseLoc, Qpt, _rayOrigin, _rayDir), Qpt);
	}	
	/**
	 * after control points are moved, make sure they are normalized
	 */
	@Override
	protected final void mseDragInMap_Indiv(myVectorf defVec, myPointf mseClickIn3D_f,boolean isScale,boolean isRotation,  boolean isTranslation,char key, int keyCode) {
		for(int i=0;i<cntlPtNorms.length;++i) {	
			cntlPtNorms[i].set(cntlPts[i], cntlPtNormEndPts[i]);
			cntlPtNorms[i]._normalize();
			cntlPtNormEndPts[i].set(myPointf._add(cntlPts[i], normDispVecOffset, cntlPtNorms[i]));
		}	
		qPointNBC = calcNormBaryCoords(Qpt);		
	}
	/**
	 * update control point normals upon editing, as well as editable q point and bound points
	 */
	private final void updateCntlPtNormEndPts() {
		for(int i=0;i<cntlPtNormEndPts.length;++i) {	cntlPtNormEndPts[i].set(myPointf._add(cntlPts[i], normDispVecOffset, cntlPtNorms[i]));}
		//recalculate Qpt
		Qpt.set(calcPointFromNormBaryCoords(qPointNBC));
	}
	
	/**
	 * move normals appropriately for dilation
	 */
	@Override
	protected final void dilateMap_Indiv(float amt) {	updateCntlPtNormEndPts();}	
	/**
	 * move normals appropriately for rotation
	 */
	@Override
	protected final void rotateMapInPlane_Indiv(float thet) {updateCntlPtNormEndPts();}
	/**
	 * move normals appropriately 
	 */
	@Override
	protected final void moveMapInPlane_Indiv(myVectorf defVec) {updateCntlPtNormEndPts();}
	
	@Override
	protected final void moveCntlPtInPlane_Indiv(myVectorf defVec) {
		for(int i=0;i<cntlPts.length;++i) {	if(currMseModCntlPt.equals(cntlPts[i])) {updateCntlPtNormEndPts(); return;}	}		//if moving control point, move vector and vector end point along with it
	}
	
	
	@Override
	protected final void mseRelease_Indiv() {
	}


}//class TriangleBiLinMap
