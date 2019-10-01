package COTS_Morph_PKG.maps.quad.base;

import java.util.ArrayList;

import COTS_Morph_PKG.managers.mapManagers.mapPairManager;
import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapCntlFlags;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_UI_Objects.IRenderInterface;
import base_Utils_Objects.MyMathUtils;
import base_Utils_Objects.vectorObjs.myPointf;
import processing.core.PConstants;
import processing.core.PImage;

public abstract class baseQuadMap extends baseMap {

//	private PShape[] checkerBoard;
//	private PShape[] circleGrid;
//	
//	public static final int 
//		shapeFile_IDX = 0,
//		shapeWF_IDX = 1;
//	private static final int numShapes = 2;
	/**
	 * whether or not this quad derives barycentric coordinates to determine 3rd control point on b-map
	 */
	protected boolean isBaryQuad = false;
	/**
	 * bary centric coordinates of control point d (idx 3), taken from map A, applied to map B
	 */
	protected float[] cntlPtD_baryCoords;

	/**
	 * 
	 * @param _win
	 * @param _mapMgr
	 * @param _cntlPts
	 * @param _mapIdx
	 * @param _mapTypeIDX
	 * @param _pClrs
	 * @param _currUIVals
	 * @param _isKeyFrame
	 * @param _mapTitle
	 */
	public baseQuadMap(COTS_MorphWin _win,  mapPairManager _mapMgr, myPointf[] _cntlPts, int _mapIdx, int _mapTypeIDX, int[][] _pClrs,mapUpdFromUIData _currUIVals,  boolean _isKeyFrame, boolean _isBaryQuad, String _mapTitle) {
		super(_win, _mapMgr,  _cntlPts, _mapIdx, _mapTypeIDX, _pClrs, _currUIVals, _isKeyFrame, _mapTitle);	
		isBaryQuad = _isBaryQuad;
		if(isBaryQuad) {		
			dispTitleOn2Lines = true;
			cntlPtD_baryCoords = cntlPts[3].calcNormBaryCoords(cntlPts);
			
		}	
	}

	public baseQuadMap(String _mapTitle, baseQuadMap _otr) {
		super(_mapTitle,  _otr);	
		isBaryQuad = _otr.isBaryQuad;
		if(isBaryQuad) {		
			cntlPtD_baryCoords = new float[_otr.cntlPtD_baryCoords.length];		
			for(int i=0;i<_otr.cntlPtD_baryCoords.length;++i) {
				cntlPtD_baryCoords[i]=_otr.cntlPtD_baryCoords[i];
			}
		}
		dispTitleOn2Lines = false;	
	}
	
	/**
	 * build a set of edge points around the edge of this map
	 */
	@Override
	protected final myPointf[][] buildEdgePoints() {
		 myPointf[][] ePts = new myPointf[cntlPts.length][numCellsPerSide];
		 if(!isAbleToExec()) {return ePts;}
		 for(int j=0;j<ePts[0].length;++j) { 
			 float t = (1.0f*j)/ePts[0].length;
			 ePts[0][j] = calcMapPt(t, 0.0f);
			 ePts[1][j] = calcMapPt(1.0f, t);
			 ePts[2][j] = calcMapPt(1.0f-t, 1.0f);
			 ePts[3][j] = calcMapPt(0.0f, 1.0f-t);
		 }
		return ePts;
	}
	
	/**
	 * Instance-class specific initialization
	 */	
	@Override
	protected final void updateMapFromCntlPtVals_Indiv(mapCntlFlags flags) {
		if(isBaryQuad) {_updateCntrlPtDFromOtherMap(flags);}
		_updateQuadMapFromCntlPtVals_Indiv(flags);
		//rebuildCBandCircleGrid();
	}
	
	protected abstract void _updateQuadMapFromCntlPtVals_Indiv(mapCntlFlags flags);
	
	
	@Override
	public final void updateMeWithMapVals(baseMap otrMap, mapCntlFlags flags) {
		if(isBaryQuad) {_updateCntrlPtDFromOtherMap(flags);}
		_updateMeWithQuadMapVals(otrMap, flags);		
	}
	protected abstract void _updateMeWithQuadMapVals(baseMap otrMap, mapCntlFlags flags);
	
	@Override
	protected final void setOtrMap_Indiv() {
		if(isBaryQuad) {_updateCntrlPtDFromOtherMap(resetMapUpdateFlags);}
		_setOtrQuadMap_Indiv();
		
	}
	protected abstract void _setOtrQuadMap_Indiv();

	/**
	 * update this map's control point D from barycentric coords, or update bary coords from contrl point d location
	 * @param flags
	 */
	protected void _updateCntrlPtDFromOtherMap(mapCntlFlags flags) {
		if((isKeyFrameMap) && (mapIdx == 1)){		///b map, 
			if(this.otrMap !=null) {
				baseQuadMap _otr = ((baseQuadMap)otrMap);
				cntlPtD_baryCoords = new float[_otr.cntlPtD_baryCoords.length];
				for(int i=0;i<_otr.cntlPtD_baryCoords.length;++i) {
					cntlPtD_baryCoords[i]=_otr.cntlPtD_baryCoords[i];
				}
				cntlPts[3].set(myPointf.calcPointFromNormBaryCoords(cntlPts, cntlPtD_baryCoords));
			} else {
				win.getMsgObj().dispInfoMessage("baseQuadMap", "_updateCntrlPtDFromOtherMap", "Warning : " + this.mapTitle +" not properly calculating D control point due to otrMap == null");
			}	
		} else {		//a map or morph maps - recalc barycentric coords based on state of d
			cntlPtD_baryCoords = cntlPts[3].calcNormBaryCoords(cntlPts);		
		}
		
	}//_updateCntrlPtDFromOtherMap

	
	/**
	 * whether this map is ready to execute
	 * @return
	 */
	public abstract boolean isAbleToExec();
	
	@Override
	public final int getNumCntlPts() {return 4;}
	@Override
	public final myPointf[] getCntlPtOffDiagonal() {	return new myPointf[] {cntlPts[1],cntlPts[3]};}
	
	
	/**
	 * build corners of all polys - return a column/row indexed list of cntrl points for map cells
	 */
	@Override
	public final myPointf[][][] buildPolyCorners(){
		myPointf[][][] res = new myPointf[numCellsPerSide][numCellsPerSide][cntlPts.length];		
		for(int i=0;i<res.length;++i) {
			for(int j=0;j<res[i].length;++j) {
				res[i][j] = new myPointf[cntlPts.length];
				res[i][j][0]=calcMapPt(polyPointTVals[i], polyPointTVals[j]);
				res[i][j][1]=calcMapPt(polyPointTVals[i+1], polyPointTVals[j]);
				res[i][j][2]=calcMapPt(polyPointTVals[i+1], polyPointTVals[j+1]);
				res[i][j][3]=calcMapPt(polyPointTVals[i], polyPointTVals[j+1]);
			}
		}
		return res;	
	}	

	/**
	 * build single tile points
	 * @param i
	 * @param j
	 * @param numPtsPerEdge # of points to interpolate between adjacent t values
	 * @return
	 */
	@Override
	protected final ArrayList<myPointf> buildPolyPointAra(int i, int j, int numPtsPerPolyEdge) {
		float subDivPerPoly = 1.0f/numPtsPerPolyEdge;
		ArrayList<myPointf> resList = new ArrayList<myPointf>();
		float tx, ty = polyPointTVals[j];
		for (tx = polyPointTVals[i]; tx<polyPointTVals[i+1];tx+=subDivPerPoly) {
			resList.add(calcMapPt(tx, ty));		
		}
		tx = polyPointTVals[i+1];					
		for (ty = polyPointTVals[j]; ty<polyPointTVals[j+1];ty+=subDivPerPoly) {
			resList.add(calcMapPt(tx, ty));			
		}
		
		ty = polyPointTVals[j+1];
		for (tx = polyPointTVals[i+1]; tx>polyPointTVals[i];tx-=subDivPerPoly) {
			resList.add(calcMapPt(tx, ty));		
		}
		tx = polyPointTVals[i];	
		for (ty = polyPointTVals[j+1]; ty>polyPointTVals[j];ty-=subDivPerPoly) {
			resList.add(calcMapPt(tx, ty));			
		}		
		return resList;
	}
	
	@Override
	protected final void _drawPoly(int i, int j) {
		myPointf pt;
		float tx, ty = polyPointTVals[j];
		pa.beginShape();
		
		pa.normal(basisVecs[0].x, basisVecs[0].y, basisVecs[0].z);
		for (tx = polyPointTVals[i]; tx<polyPointTVals[i+1];tx+=subDivLenPerPoly) {
			pt = calcMapPt(tx, ty);
			pa.vertex(pt.x,pt.y,pt.z);
		}
		tx = polyPointTVals[i+1];					
		for (ty = polyPointTVals[j]; ty<polyPointTVals[j+1];ty+=subDivLenPerPoly) {
			pt = calcMapPt(tx, ty);
			pa.vertex(pt.x,pt.y,pt.z);
		}
		
		ty = polyPointTVals[j+1];
		for (tx = polyPointTVals[i+1]; tx>polyPointTVals[i];tx-=subDivLenPerPoly) {
			pt = calcMapPt(tx, ty);
			pa.vertex(pt.x,pt.y,pt.z);
		}
		tx = polyPointTVals[i];	
		for (ty = polyPointTVals[j+1]; ty>polyPointTVals[j];ty-=subDivLenPerPoly) {
			pt = calcMapPt(tx, ty);
			pa.vertex(pt.x,pt.y,pt.z);
		}
		pa.endShape(PConstants.CLOSE);	
	}
	
	protected final void _drawPolyTexture(PImage _img, int i, int j) {
		myPointf pt;
		float tx, ty = polyPointTVals[j];
		
		pa.beginShape();
		pa.texture(_img);
		pa.normal(basisVecs[0].x, basisVecs[0].y, basisVecs[0].z);
		for (tx = polyPointTVals[i]; tx<polyPointTVals[i+1];tx+=subDivLenPerPoly) {
			pt = calcMapPt(tx, ty);
			pa.vertex(pt.x,pt.y,pt.z, tx, ty);
		}
		tx = polyPointTVals[i+1];					
		for (ty = polyPointTVals[j]; ty<polyPointTVals[j+1];ty+=subDivLenPerPoly) {
			pt = calcMapPt(tx, ty);
			pa.vertex(pt.x,pt.y,pt.z, tx, ty);
		}
		
		ty = polyPointTVals[j+1];
		for (tx = polyPointTVals[i+1]; tx>polyPointTVals[i];tx-=subDivLenPerPoly) {
			pt = calcMapPt(tx, ty);
			pa.vertex(pt.x,pt.y,pt.z, tx, ty);
		}
		tx = polyPointTVals[i];	
		for (ty = polyPointTVals[j+1]; ty>polyPointTVals[j];ty-=subDivLenPerPoly) {
			pt = calcMapPt(tx, ty);
			pa.vertex(pt.x,pt.y,pt.z, tx, ty);
		}
		pa.endShape(PConstants.CLOSE);	
	}
	/**
	 * draw a minimized lineup picture with appropriate settings
	 */
	@Override	
	protected final void drawMap_LineUp_Indiv(boolean fillOrWf, boolean drawCircles, boolean drawTexture) {
		if(fillOrWf) {	drawMap_Fill();	} 
		else {			drawMap_Wf();}	
		if(drawCircles) {
			if(fillOrWf) {	drawMap_PolyCircles_Fill();	} 
			else {			drawMap_PolyCircles_Wf();}	
		}
		if(drawTexture) {		drawMap_Texture();	}		
	}//drawMap_LineUp_Indiv
	
	@Override
	public void drawMap_Texture() {
		pa.pushMatrix();pa.pushStyle();	
		pa.noFill();
		pa.noStroke();
		for(int i=0;i<polyPointTVals.length-1;++i) {for(int j=0;j<polyPointTVals.length-1;++j) {		_drawPolyTexture(getImageToMap(), i,j);		}}			
		pa.popStyle();pa.popMatrix();
	}
	
	@Override
	public final void drawMap_PolyCircles_Fill() {
		pa.pushMatrix();pa.pushStyle();	
		pa.stroke(255,255,255,255);
		pa.setStrokeWt(2.0f);		
		int clrIdx = 1;		
		float r = 1.f/(1.0f*numCellsPerSide), tx, ty, halfR = r/2.0f;
		float circInterp = .12f;
		myPointf pt;
		for(int i=0;i<polyPointTVals.length-1;++i) {
			float ri = r*i;
			clrIdx = 1-(i % 2);
			for(int j=0;j<polyPointTVals.length-1;++j) {
				float rj = r * j;
				pa.beginShape();
			    for(float u=0; u<MyMathUtils.twoPi_f; u+=circInterp) {
			    	pa.setFill(polyColors[clrIdx], polyColors[clrIdx][3]);
			    	tx=(float) (halfR + ri + halfR*Math.cos(u));
			    	ty=(float) (halfR + rj + halfR*Math.sin(u));
					pt = calcMapPt(tx, ty);
					pa.vertex(pt.x,pt.y,pt.z);
					
			    }
				pa.endShape(PConstants.CLOSE);	
				clrIdx = (clrIdx + 1) % 2;
			}	
		}
		
		pa.popStyle();pa.popMatrix();
	}
	@Override
	public final void drawMap_PolyCircles_Wf() {
		pa.pushMatrix();pa.pushStyle();	
		pa.noFill();
		pa.setStroke(gridColor, gridColor[3]);
		pa.setStrokeWt(2.0f);	
		
		float r = 1.0f/(1.0f*numCellsPerSide), tx, ty, halfR = r/2.0f;
		float circInterp = .12f;
		myPointf pt;
		for(int i=0;i<polyPointTVals.length-1;++i) {
			float ri = r*i;
			for(int j=0;j<polyPointTVals.length-1;++j) {
				float rj = r * j;
				pa.beginShape();
			    for(float u=0; u<MyMathUtils.twoPi_f; u+=circInterp) {
			    	tx=(float) (halfR + ri + halfR*Math.cos(u));
			    	ty=(float) (halfR + rj + halfR*Math.sin(u));
					pt = calcMapPt(tx, ty);
					pa.vertex(pt.x,pt.y,pt.z);			    
			    }
				pa.endShape(PConstants.CLOSE);	
			}	
		}
	
		pa.popStyle();pa.popMatrix();		
	}
	
	@Override
	protected final float drawRtSdMenuDescr_Indiv(float yOff, float sideBarYDisp) {
		if(this.isBaryQuad) {
			pa.pushMatrix();pa.pushStyle();	
			pa.translate(10.0f,0.0f, 0.0f);
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 5.5f, "Cntl Pt D Bary : ");
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_LightCyan, 255), 3.0f, "["+String.format(baseMap.strPointDispFrmt8,cntlPtD_baryCoords[0])+","+String.format(baseMap.strPointDispFrmt8,cntlPtD_baryCoords[1])+","+String.format(baseMap.strPointDispFrmt8,cntlPtD_baryCoords[2])+"]");
			pa.popStyle();pa.popMatrix();
				
			yOff += sideBarYDisp;pa.translate(0.0f,sideBarYDisp, 0.0f);
		
		}
		yOff = _drawQuadMapRtSdMenuDescr_Indiv(yOff, sideBarYDisp);
	
		return yOff;
	}

	protected abstract float _drawQuadMapRtSdMenuDescr_Indiv(float yOff, float sideBarYDisp);
	
	@Override
	public String toString() {
		String res = super.toString();
		if(this.isBaryQuad) {
			res +=  " BaryQuadMap :  Bary Coords of cntlPt[3] : ["+cntlPtD_baryCoords[0]+","+cntlPtD_baryCoords[1]+","+cntlPtD_baryCoords[2]+"]\n";
		}		
		return res;
	}
	
	
}//class baseQuadMap
