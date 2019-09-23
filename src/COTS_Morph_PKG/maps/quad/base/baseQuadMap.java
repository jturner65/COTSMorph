package COTS_Morph_PKG.maps.quad.base;

import java.util.ArrayList;

import COTS_Morph_PKG.managers.mapManagers.mapPairManager;
import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapCntlFlags;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_Utils_Objects.MyMathUtils;
import base_Utils_Objects.vectorObjs.myPointf;
import processing.core.PConstants;
import processing.core.PImage;

public abstract class baseQuadMap extends baseMap {
	/**
	 * poly corner t values - recalced when cntlpts move - idx 1 is x idx, idx2 is y idx
	 */
	private float[][][] polyPointTVals;	
		//# of subdivisions per poly for checkerboard
	private float subDivLenPerPoly;
		//# of total points for edge of map
	protected static final float numTtlPtsPerEdge = 300.0f;

//	private PShape[] checkerBoard;
//	private PShape[] circleGrid;
//	
//	public static final int 
//		shapeFile_IDX = 0,
//		shapeWF_IDX = 1;
//	private static final int numShapes = 2;

	public baseQuadMap(COTS_MorphWin _win,  mapPairManager _mapMgr, myPointf[] _cntlPts, int _mapIdx, int _mapTypeIDX, int[][] _pClrs,mapUpdFromUIData _currUIVals,  boolean _isKeyFrame, String _mapTitle) {
		super(_win, _mapMgr,  _cntlPts, _mapIdx, _mapTypeIDX, _pClrs, _currUIVals, _isKeyFrame, _mapTitle);	
	}

	public baseQuadMap(String _mapTitle, baseMap _otr) {super(_mapTitle,  _otr);	}
	
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
		_updateQuadMapFromCntlPtVals_Indiv(flags);
		//rebuildCBandCircleGrid();
	}
	
	protected abstract void _updateQuadMapFromCntlPtVals_Indiv(mapCntlFlags flags);
	
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
	 * precalculate the tx,ty values for the grid poly bounds - only necessary when numCellsPerSide changes
	 */
	@Override
	protected final void buildPolyPointTVals() {
		
		subDivLenPerPoly = numCellsPerSide/numTtlPtsPerEdge;

		polyPointTVals = new float[numCellsPerSide+1][numCellsPerSide+1][2];			
		float tx;		
		for(int i=0;i<polyPointTVals.length;++i) {
			tx = i/(1.0f*numCellsPerSide);
			for(int j=0;j<polyPointTVals[i].length;++j) {
				polyPointTVals[i][j][0]=tx;
				polyPointTVals[i][j][1]=j/(1.0f*numCellsPerSide);	//ty			
			}
		}			
	}//buildPolyPointTVals
	
	/**
	 * build single tile points
	 * @param i
	 * @param j
	 * @param numPtsPerEdge # of points to interpolate between adjacent t values
	 * @return
	 */
	protected ArrayList<myPointf> buildPolyPointAra(int i, int j, int numPtsPerEdge) {
		float subDivPerPoly = 1.0f/numPtsPerEdge;
		ArrayList<myPointf> resList = new ArrayList<myPointf>();
		float tx, ty = polyPointTVals[i][j][1];
		for (tx = polyPointTVals[i][j][0]; tx<polyPointTVals[i+1][j][0];tx+=subDivPerPoly) {
			resList.add(calcMapPt(tx, ty));		
		}
		tx = polyPointTVals[i+1][j][0];					
		for (ty = polyPointTVals[i+1][j][1]; ty<polyPointTVals[i+1][j+1][1];ty+=subDivPerPoly) {
			resList.add(calcMapPt(tx, ty));			
		}
		
		ty = polyPointTVals[i+1][j+1][1];
		for (tx = polyPointTVals[i+1][j+1][0]; tx>polyPointTVals[i][j+1][0];tx-=subDivPerPoly) {
			resList.add(calcMapPt(tx, ty));		
		}
		tx = polyPointTVals[i][j+1][0];	
		for (ty = polyPointTVals[i][j+1][1]; ty>polyPointTVals[i][j][1];ty-=subDivPerPoly) {
			resList.add(calcMapPt(tx, ty));			
		}		
		return resList;
	}
	

	
	protected final void _drawPoly(int i, int j) {
		myPointf pt;
		float tx, ty = polyPointTVals[i][j][1];
		pa.beginShape();
		
		pa.normal(basisVecs[0].x, basisVecs[0].y, basisVecs[0].z);
		for (tx = polyPointTVals[i][j][0]; tx<polyPointTVals[i+1][j][0];tx+=subDivLenPerPoly) {
			pt = calcMapPt(tx, ty);
			pa.vertex(pt.x,pt.y,pt.z);
		}
		tx = polyPointTVals[i+1][j][0];					
		for (ty = polyPointTVals[i+1][j][1]; ty<polyPointTVals[i+1][j+1][1];ty+=subDivLenPerPoly) {
			pt = calcMapPt(tx, ty);
			pa.vertex(pt.x,pt.y,pt.z);
		}
		
		ty = polyPointTVals[i+1][j+1][1];
		for (tx = polyPointTVals[i+1][j+1][0]; tx>polyPointTVals[i][j+1][0];tx-=subDivLenPerPoly) {
			pt = calcMapPt(tx, ty);
			pa.vertex(pt.x,pt.y,pt.z);
		}
		tx = polyPointTVals[i][j+1][0];	
		for (ty = polyPointTVals[i][j+1][1]; ty>polyPointTVals[i][j][1];ty-=subDivLenPerPoly) {
			pt = calcMapPt(tx, ty);
			pa.vertex(pt.x,pt.y,pt.z);
		}
		pa.endShape(PConstants.CLOSE);	
	}
	
	protected final void _drawPolyTexture(PImage _img, int i, int j) {
		myPointf pt;
		float tx, ty = polyPointTVals[i][j][1];
		
		pa.beginShape();
		pa.texture(_img);
		pa.normal(basisVecs[0].x, basisVecs[0].y, basisVecs[0].z);
		for (tx = polyPointTVals[i][j][0]; tx<polyPointTVals[i+1][j][0];tx+=subDivLenPerPoly) {
			pt = calcMapPt(tx, ty);
			pa.vertex(pt.x,pt.y,pt.z, tx, ty);
		}
		tx = polyPointTVals[i+1][j][0];					
		for (ty = polyPointTVals[i+1][j][1]; ty<polyPointTVals[i+1][j+1][1];ty+=subDivLenPerPoly) {
			pt = calcMapPt(tx, ty);
			pa.vertex(pt.x,pt.y,pt.z, tx, ty);
		}
		
		ty = polyPointTVals[i+1][j+1][1];
		for (tx = polyPointTVals[i+1][j+1][0]; tx>polyPointTVals[i][j+1][0];tx-=subDivLenPerPoly) {
			pt = calcMapPt(tx, ty);
			pa.vertex(pt.x,pt.y,pt.z, tx, ty);
		}
		tx = polyPointTVals[i][j+1][0];	
		for (ty = polyPointTVals[i][j+1][1]; ty>polyPointTVals[i][j][1];ty-=subDivLenPerPoly) {
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
		for(int i=0;i<polyPointTVals.length-1;++i) {for(int j=0;j<polyPointTVals[i].length-1;++j) {		_drawPolyTexture(imageToMap, i,j);		}}			
		pa.popStyle();pa.popMatrix();
	}

	@Override
	public final void drawMap_Fill() {
		pa.pushMatrix();pa.pushStyle();	
		pa.stroke(255,255,255,255);
		pa.setStrokeWt(1.0f);
		int clrIdx = 0;
		
		for(int i=0;i<polyPointTVals.length-1;++i) {
			clrIdx = i % 2;
			for(int j=0;j<polyPointTVals[i].length-1;++j) {
				pa.setFill(polyColors[clrIdx], polyColors[clrIdx][3]);
				_drawPoly(i,j);		
				clrIdx = (clrIdx + 1) % 2;
			}				
		}	
		pa.popStyle();pa.popMatrix();
	}//drawMap_Fill
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
			for(int j=0;j<polyPointTVals[i].length-1;++j) {
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
	public final void drawMap_Wf() {
		pa.pushMatrix();pa.pushStyle();	
		pa.noFill();
		pa.setStroke(gridColor, gridColor[3]);
		pa.setStrokeWt(2.0f);	
		for(int i=0;i<polyPointTVals.length-1;++i) {for(int j=0;j<polyPointTVals[i].length-1;++j) {_drawPoly(i,j);}}	
		pa.popStyle();pa.popMatrix();
	}//drawMap_Wf
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
			for(int j=0;j<polyPointTVals[i].length-1;++j) {
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
}
