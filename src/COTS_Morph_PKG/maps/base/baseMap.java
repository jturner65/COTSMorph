package COTS_Morph_PKG.maps.base;

import base_UI_Objects.my_procApplet;
import base_UI_Objects.windowUI.myDispWindow;
import base_Utils_Objects.vectorObjs.myPointf;

/**
 * base class describing a mapping
 * @author john
 *
 */
public abstract class baseMap {
	/**
	 * array of corner cntl points
	 */
	protected myPointf[] cntlPts;
	protected static final String[] cntlPtLbls = new String[] {"A","B","C","D"};
	
	/**
	 * poly points - recalced when cntlpts move - idx 1 is x idx, idx2 is y idx
	 */
	protected myPointf[][] polyPoints;
	
	/**
	 * array of 2 poly colors
	 */
	protected int[][] polyColors;
	
	protected final int numPolyPerSide = 8;
	
	protected static final float sphereRad = 5.0f;
	/**
	 * map index for this map , either 0 or 1
	 */
	public final int mapIdx;
	
	public baseMap(myPointf[] _cntlPts, int _mapIdx) {
		cntlPts = new myPointf[_cntlPts.length];
		System.arraycopy(_cntlPts, 0, cntlPts, 0, cntlPts.length);
		polyColors = new int[2][];
		polyColors[0] = new int[] {255,200,0,255};
		polyColors[1] = new int[] {90,0,222,255};
		mapIdx = _mapIdx;
	}

	public abstract myPointf calcMapPt(float tx, float ty);
	
	public final myPointf[][] calcPolyPoints() {
		myPointf[][] polyPts = new myPointf[numPolyPerSide+1][numPolyPerSide+1];
		for(int i=0;i<polyPts.length;++i) {
			float tx = i/(1.0f*numPolyPerSide);
			for(int j=0;j<polyPts[i].length;++j) {	polyPts[i][j] = calcMapPt(tx, j/(1.0f*numPolyPerSide));}
		}		
		return polyPts;
	}

	private void _drawPoints(my_procApplet pa) {
		pa.sphereDetail(5);
		pa.stroke(0,0,0,255);
		for(myPointf p : cntlPts) {
			pa.pushMatrix();pa.pushStyle();	
			pa.translate(p);
			pa.sphere(sphereRad);
			pa.popStyle();pa.popMatrix();
		}
	}
	
	public void drawMap_Fill(my_procApplet pa) {
		pa.pushMatrix();pa.pushStyle();	
		pa.stroke(255,255,255,255);
		pa.setStrokeWt(2.0f);
		polyPoints = calcPolyPoints();
		int clrIdx = 0;
		for(int i=0;i<polyPoints.length-1;++i) {
			clrIdx = i % 2;
			for(int j=0;j<polyPoints[i].length-1;++j) {
				pa.setFill(polyColors[clrIdx], polyColors[clrIdx][3]);
				pa.beginShape();
				pa.gl_vertex(polyPoints[i][j]);
				pa.gl_vertex(polyPoints[i+1][j]);
				pa.gl_vertex(polyPoints[i+1][j+1]);
				pa.gl_vertex(polyPoints[i][j+1]);
				pa.endShape(pa.CLOSE);				
				clrIdx = (clrIdx + 1) % 2;
			}				
		}	
		_drawPoints(pa);
		pa.popStyle();pa.popMatrix();
	}
	public void drawMap_Wf(my_procApplet pa) {
		pa.pushMatrix();pa.pushStyle();	
		pa.noFill();
		pa.stroke(0,0,0,50);
		pa.setStrokeWt(2.0f);
		polyPoints = calcPolyPoints();
		int clrIdx = 0;
		for(int i=0;i<polyPoints.length-1;++i) {
			clrIdx = i % 2;
			for(int j=0;j<polyPoints[i].length-1;++j) {
				pa.beginShape();
				pa.gl_vertex(polyPoints[i][j]);
				pa.gl_vertex(polyPoints[i+1][j]);
				pa.gl_vertex(polyPoints[i+1][j+1]);
				pa.gl_vertex(polyPoints[i][j+1]);
				pa.endShape(pa.CLOSE);				
				clrIdx = (clrIdx + 1) % 2;
			}				
		}	
		_drawPoints(pa);
		

		pa.popStyle();pa.popMatrix();
	}
	
	public void drawLabels_3D(my_procApplet pa, myDispWindow animWin) {
		pa.pushMatrix();pa.pushStyle();
		pa.sphereDetail(5);
		pa.fill(0,0,0,255);
		pa.stroke(0,0,0,255);
		for(int i=0; i< cntlPts.length;++i) {
			pa.pushMatrix();pa.pushStyle();	
			pa.translate(cntlPts[i]);
			animWin.unSetCamOrient();
			pa.scale(1.5f);
			pa.text(cntlPtLbls[i] + "_"+mapIdx, 2.5f,-2.5f,0); 
			pa.popStyle();pa.popMatrix();
		}
		pa.popStyle();pa.popMatrix();
		
	}

	public void drawLabels_2D(my_procApplet pa) {
		pa.pushMatrix();pa.pushStyle();
		pa.sphereDetail(5);
		pa.fill(0,0,0,255);
		pa.stroke(0,0,0,255);
		for(int i=0; i< cntlPts.length;++i) {
			pa.pushMatrix();pa.pushStyle();	
			pa.translate(cntlPts[i]);
			pa.scale(1.5f);
			pa.text(cntlPtLbls[i] + "_"+mapIdx, 2.5f,-2.5f,0); 
			pa.popStyle();pa.popMatrix();
		}
		pa.popStyle();pa.popMatrix();
	}

}//class baseMap
