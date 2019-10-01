package COTS_Morph_PKG.maps.triangular.base;


import java.util.ArrayList;

import COTS_Morph_PKG.managers.mapManagers.mapPairManager;
import COTS_Morph_PKG.maps.base.baseMap;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import COTS_Morph_PKG.utils.mapUpdFromUIData;
import base_Utils_Objects.vectorObjs.myPointf;
import processing.core.PConstants;

/**
 * base class for triangle maps, that provides overrides for functions in baseMap that don't make sense for triangles
 * @author john
 *
 */
public abstract class baseTriangleMap extends baseMap {

	public baseTriangleMap(COTS_MorphWin _win, mapPairManager _mapMgr, myPointf[] _cntlPts, int _mapIdx, int _mapTypeIDX, int[][] _pClrs, mapUpdFromUIData _currUIVals,  boolean _isKeyFrame, String _mapTitle) {
		super(_win, _mapMgr,_cntlPts, _mapIdx, _mapTypeIDX, _pClrs, _currUIVals, _isKeyFrame, _mapTitle);		
	}

	public baseTriangleMap(String _mapTitle, baseTriangleMap _otr) {		super(_mapTitle, _otr);			}
	
	/**
	 * instead of tx and ty being used as planar interpolants, for triangles these can be set as barycentric coords v and w, with u = 1-v-w
	 * 
	 */
	@Override
	public final myPointf calcMapPt(float tx, float ty) {	return calcPointFromNormBaryCoords(new float[] {1-tx-ty, tx, ty});}

	/**
	 * build a set of edge points around the edge of this map
	 */
	protected final myPointf[][] buildEdgePoints() {
		 myPointf[][] ePts = new myPointf[cntlPts.length][numCellsPerSide];
		 for(int j=0;j<ePts[0].length;++j) { 
			 float t = (1.0f*j)/ePts[0].length;
			 ePts[0][j] = calcMapPt(t, 0.0f);
			 ePts[1][j] = calcMapPt(1.0f-t, t);
			 ePts[2][j] = calcMapPt(0.0f, 1.0f-t);
		 }
		 return ePts;
	}//buildEdgePointsdc
	
	/**
	 * build corners of all polys - return a column/row indexed list of cntrl points for map cells
	 * will return 2*cntlPts.length for certain polys - need to query size when processing
	 */
	@Override
	public final myPointf[][][] buildPolyCorners(){
		myPointf[][][] res = new myPointf[numCellsPerSide][numCellsPerSide][];		
		
//		pt = calcMapPt(polyPointTVals[i], polyPointTVals[j]);
//		pa.vertex(pt.x,pt.y,pt.z);
//		pt = calcMapPt(polyPointTVals[i+1], polyPointTVals[j]);			
//		pa.vertex(pt.x,pt.y,pt.z);
//		pt = calcMapPt(polyPointTVals[i],polyPointTVals[j+1]);
//		pa.vertex(pt.x,pt.y,pt.z);
			
//			pt = calcMapPt(polyPointTVals[i], polyPointTVals[j]);
//			pa.vertex(pt.x,pt.y,pt.z);
//			pt = calcMapPt(polyPointTVals[i],polyPointTVals[j+1]);		
//			pa.vertex(pt.x,pt.y,pt.z);
//			pt = calcMapPt(polyPointTVals[i-1],polyPointTVals[j+1]);
//			pa.vertex(pt.x,pt.y,pt.z);		
		
		for(int i=0;i<numCellsPerSide;++i) {
			for(int j=0;j<numCellsPerSide;++j) {
				ArrayList<myPointf> tmpAra = new ArrayList<myPointf>();
				if(i+j < polyPointTVals.length-1) {					
					tmpAra.add(calcMapPt(polyPointTVals[i], polyPointTVals[j]));
					tmpAra.add(calcMapPt(polyPointTVals[i+1], polyPointTVals[j]));
					tmpAra.add(calcMapPt(polyPointTVals[i], polyPointTVals[j+1]));				
				}
				if(i>j) {//lower triangle of triangle pair
					int newI = (i > j ? i - j : i);
					int newJ = (i > j ? j : j-i);					
					tmpAra.add(calcMapPt(polyPointTVals[newI], polyPointTVals[newJ]));
					tmpAra.add(calcMapPt(polyPointTVals[newI], polyPointTVals[newJ+1]));	
					tmpAra.add(calcMapPt(polyPointTVals[newI-1], polyPointTVals[newJ+1]));
				} 
				res[i][j] = tmpAra.toArray(new myPointf[0]);
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
		ArrayList<myPointf> resList = new ArrayList<myPointf>();
		float subDivPerPoly = 1.0f/numPtsPerPolyEdge;
		float tv, tw;		
		if(i+j < polyPointTVals.length-1) {
			for(tv = polyPointTVals[i]; tv <= polyPointTVals[i+1]; tv +=subDivPerPoly) {
				resList.add(calcMapPt(tv, polyPointTVals[j]));
			}
			for(tv = polyPointTVals[i+1], tw = polyPointTVals[j]; 
					tv >= polyPointTVals[i] && tw <=polyPointTVals[j+1]; 
					tv -=subDivPerPoly, tw+=subDivPerPoly) {
				resList.add(calcMapPt(tv, tw));			
			}
			for(tw = polyPointTVals[j+1]; tw >= polyPointTVals[j]; tw-=subDivPerPoly) {
				resList.add(calcMapPt(polyPointTVals[i], tw));	
				
			}	
		} 
		if(i > j){
			int newI = (i > j ? i - j : i);
			int newJ = (i > j ? j : j-i);
			for(tw = polyPointTVals[newJ]; tw <= polyPointTVals[newJ+1]; tw +=subDivPerPoly) {
				resList.add(calcMapPt(polyPointTVals[newI], tw));
			}
			for(tv = polyPointTVals[newI]; tv >= polyPointTVals[newI-1]; tv -=subDivPerPoly) {
				resList.add(calcMapPt(tv, polyPointTVals[newJ+1]));
			}
			for(tv = polyPointTVals[newI-1], tw = polyPointTVals[newJ+1]; 
					tv <= polyPointTVals[newI] && tw >=polyPointTVals[newJ]; 
					tv +=subDivPerPoly, tw-=subDivPerPoly) {
				resList.add(calcMapPt(tv, tw));	
			}
		}
		return resList;
	}
	@Override
	protected final void _drawPoly(int i, int j) {
		//if(i+j>= polyPointTVals.length-1) {return;}
		myPointf pt;
		float tv, tw;
		if(i+j < polyPointTVals.length-1) {
			pa.beginShape();		
			pa.normal(basisVecs[0].x, basisVecs[0].y, basisVecs[0].z);
			for(tv = polyPointTVals[i]; tv <= polyPointTVals[i+1]; tv +=subDivLenPerPoly) {
				pt = calcMapPt(tv, polyPointTVals[j]);
				pa.vertex(pt.x,pt.y,pt.z);
			}
			for(tv = polyPointTVals[i+1], tw = polyPointTVals[j]; 
					tv >= polyPointTVals[i] && tw <=polyPointTVals[j+1]; 
					tv -=subDivLenPerPoly, tw+=subDivLenPerPoly) {
				pt = calcMapPt(tv, tw);			
				pa.vertex(pt.x,pt.y,pt.z);
			}
			for(tw = polyPointTVals[j+1]; tw >= polyPointTVals[j]; tw-=subDivLenPerPoly) {
				pt = calcMapPt(polyPointTVals[i], tw);			
				pa.vertex(pt.x,pt.y,pt.z);
				
			}	
			pa.endShape(PConstants.CLOSE);	
			
		} 
		if(i > j){
			int newI = (i > j ? i - j : i);
			int newJ = (i > j ? j : j-i);
			pa.beginShape();		
			pa.normal(basisVecs[0].x, basisVecs[0].y, basisVecs[0].z);
			for(tw = polyPointTVals[newJ]; tw <= polyPointTVals[newJ+1]; tw +=subDivLenPerPoly) {
				pt = calcMapPt(polyPointTVals[newI], tw);
				pa.vertex(pt.x,pt.y,pt.z);
			}
			for(tv = polyPointTVals[newI]; tv >= polyPointTVals[newI-1]; tv -=subDivLenPerPoly) {
				pt = calcMapPt(tv, polyPointTVals[newJ+1]);			
				pa.vertex(pt.x,pt.y,pt.z);
			}
			for(tv = polyPointTVals[newI-1], tw = polyPointTVals[newJ+1]; 
					tv <= polyPointTVals[newI] && tw >=polyPointTVals[newJ]; 
					tv +=subDivLenPerPoly, tw-=subDivLenPerPoly) {
				pt = calcMapPt(tv, tw);			
				pa.vertex(pt.x,pt.y,pt.z);
			}
			pa.endShape(PConstants.CLOSE);
		}
	}//_drawPoly
		

	/**
	 * draw a minimized lineup picture with appropriate settings
	 */
	@Override	
	protected final void drawMap_LineUp_Indiv(boolean fillOrWf, boolean notUsed1, boolean notUsed2) {
		drawMap_Fill();	
	}//drawMap_LineUp_Indiv

	@Override
	//ignore circles for this map type
	public final void drawMap_PolyCircles_Fill() {	drawMap_Fill();}
	
	@Override
	//ignore circles for this map type
	public final void drawMap_PolyCircles_Wf() {		drawMap_Wf();	}	
	@Override
	//ignore texture - use wire frame
	public final void drawMap_Texture() {				drawMap_Wf();}
	
	@Override
	protected final void drawRtSdMenuTitle_Indiv() {}
	@Override
	public final int getNumCntlPts() {return 3;}
	public final myPointf[] getCntlPtOffDiagonal() {	return new myPointf[] {cntlPts[1],cntlPts[2]};}
	

}
