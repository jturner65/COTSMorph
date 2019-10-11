package COTS_Morph_PKG.analysis.base;

import java.util.ArrayList;

import COTS_Morph_PKG.analysis.stats.base.baseProbSummary;
import COTS_Morph_PKG.map.base.baseMap;
import base_UI_Objects.IRenderInterface;
import base_UI_Objects.my_procApplet;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

public abstract class baseAnalyzer {
	public final int ID;
	protected static int idGen = 0;
	/**
	 * current trajectory summary
	 */
	public baseProbSummary[] summaries;	
	public static final int
		posIDX		= 0,
		velIDX		= 1,
		accelIDX	= 2,
		jerkIDX		= 3;
	protected static final int numStatsToMeasure = 4;
	public static final String[] statNames = new String[]{"Position","Velocity","Acceleration","Jerk"};
	
	public static final String[] statDispLabels = new String[]{"P","V","X","J"};

	protected boolean debug = false;
	protected final String frmtStr = "%4.2f";

	public baseAnalyzer() {
		ID = idGen++;
	}
	
	public void setDebug(boolean val) {debug=val;}
	
	/**
	 * find the average value, ROC, rate of ROC, etc of the passed trajectory of areas of morph maps
	 * assumes each area sample is uniformly spaced in time
	 * @param pts
	 */
	public abstract void analyzeTrajectory(ArrayList pts, String name);
	

	/**
	 * this will properly format and display a string of text, and will translate the width, so multiple strings can be displayed on the same line with different colors
	 * @param tclr
	 * @param txt
	 */
	protected final void showOffsetText_RightSideMenuAbs(my_procApplet pa, int[] tclr, float dist,  String txt) {
		pa.setFill(tclr,tclr[3]);pa.setStroke(tclr,tclr[3]);
		pa.text(txt,0.0f,0.0f,0.0f);
		pa.translate(dist, 0.0f,0.0f);	
	}
	
	
	
	public void drawAnalyzerData(my_procApplet pa, String[] mmntDispLabels, float[] trajWinDims, String name) {
		float yDisp = trajWinDims[3];
		pa.pushMatrix();pa.pushStyle();		
		pa.translate(5.0f, yDisp, 0.0f);
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Black, 255), 6.0f, name);
		pa.popStyle();pa.popMatrix();
		pa.pushMatrix();pa.pushStyle();
			pa.translate(5.0f, 2*yDisp, 0.0f);			
			drawAllSummaryInfo(pa,mmntDispLabels, yDisp, trajWinDims[0]);
		pa.popStyle();pa.popMatrix();
		
		pa.translate(trajWinDims[0], 0.0f, 0.0f);
		pa.line(0.0f,trajWinDims[2], 0.0f, 0.0f, trajWinDims[0]+ trajWinDims[2], 0.0f );
	}//_drawAnalyzerData
	
	/**
	 * 
	 * @param pa
	 * @param mmntDispLabels
	 * @param trajWinDims array of float dims - width,height of window, y value where window starts, y value to displace every line
	 * @param yDisp
	 * @param name
	 */
	public void drawAnalyzerGraphs(my_procApplet pa, String[] mmntDispLabels, float[] trajWinDims, String name) {
		float yDisp = trajWinDims[3];
		float[] graphRect = new float[] {10.0f,10.0f,trajWinDims[0]-20.0f,(trajWinDims[1]/(1.0f*summaries.length + 1))-20.0f};
		pa.pushMatrix();pa.pushStyle();		
		pa.translate(5.0f, yDisp, 0.0f);
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Black, 255), 6.0f, name);
		pa.popStyle();pa.popMatrix();
		pa.pushMatrix();pa.pushStyle();
			pa.translate(5.0f, 2*yDisp, 0.0f);			
			drawAllGraphInfo(pa,mmntDispLabels,graphRect, yDisp, trajWinDims[0]);
		pa.popStyle();pa.popMatrix();
		
		pa.translate(trajWinDims[0], 0.0f, 0.0f);
		pa.line(0.0f,trajWinDims[2], 0.0f, 0.0f, trajWinDims[0]+ trajWinDims[2], 0.0f );
	}//_drawAnalyzerData
	
	
	
	protected void drawAllSummaryInfo(my_procApplet pa, String[] mmntDispLabels, float txtLineYDisp, float perDispBlockWidth) {//
		float mult = (perDispBlockWidth)/((mmntDispLabels.length + 1) * 3.1f);
		//System.out.println("perDispBlockWidth: " + perDispBlockWidth + " | mult : " + mult);
		pa.pushMatrix();pa.pushStyle();
		if(mult < 17) {pa.scale(1.0f,.93f,1.0f);}
		for(int i=0;i<summaries.length;++i) {		//per summary - single summary for pos, vel, accel, jerk, etc of each point
			//title of each summary 
			pa.pushMatrix();pa.pushStyle();
				pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Black, 255),1.4f* mult, statDispLabels[i]);
				for(int j=0;j<mmntDispLabels.length;++j) {		showOffsetText_RightSideMenuAbs(pa,pa.getClr(IRenderInterface.gui_DarkBlue, 255), mult*3.5f, mmntDispLabels[j]);}
			pa.popStyle();pa.popMatrix();			
			pa.translate(0.0f,txtLineYDisp,0.0f);

			
			drawSingleSummary(pa,mmntDispLabels, summaries[i],txtLineYDisp,mult);
			pa.translate(0.0f,.8f*txtLineYDisp,0.0f);
		}
		pa.popStyle();pa.popMatrix();
	}
	protected abstract void drawSingleSummary(my_procApplet pa, String[] mmntDispLabels, baseProbSummary smryRaw, float txtLineYDisp, float ltrMult);
	
	/**
	 * draw stats graph info
	 * @param mmntDispLabels
	 * @param txtLineYDisp
	 * @param perDispBlockWidth
	 */
	protected final void drawAllGraphInfo(my_procApplet pa, String[] mmntDispLabels, float[] graphRect, float txtLineYDisp, float perDispBlockWidth) {
		float mult = (perDispBlockWidth)/((mmntDispLabels.length + 1) * 3.1f);
		pa.pushMatrix();pa.pushStyle();
		if(mult < 17) {pa.scale(1.0f,.93f,1.0f);}
		for(int i=0;i<summaries.length;++i) {		//per summary - single summary for pos, vel, accel, jerk, etc of each point
			//title of each summary graph 
			pa.pushMatrix();pa.pushStyle();
				pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Black, 255),1.0f* mult, statDispLabels[i]);		
				drawSingleSmryGraphMinMaxLbls(pa, i, mult);
			pa.popStyle();pa.popMatrix();			
			pa.translate(0.0f,txtLineYDisp,0.0f);		
			pa.pushMatrix();pa.pushStyle();
			pa.fill(50,50,50,255);
			pa.rect(0.0f, -5.0f, graphRect[2], graphRect[3]+10.0f);
			pa.popStyle();pa.popMatrix();
			drawSingleSmryGraph(pa,mmntDispLabels, i,graphRect,mult);
			pa.translate(0.0f,1.5f*txtLineYDisp,0.0f);
		}
		pa.popStyle();pa.popMatrix();
	}//drawAllGraphInfo
	protected abstract void drawSingleSmryGraphMinMaxLbls(my_procApplet pa, int smryIdx, float ltrMult);
	protected abstract void drawSingleSmryGraph(my_procApplet pa, String[] mmntDispLabels, int smryIdx, float[] graphRect,  float ltrMult);
	
	/**
	 * draw a single trajectory within the bounds defined by trajRect
	 * @param pa
	 * @param trajRect rectangle of upper left corner x,y, width, height of drawable space for trajectory
	 * @param minMax min and max values of actual data - only for labels
	 * @param trajElems array of values 0->1 that lie between min (0) and max(1) 
	 * @param widthPerElem how far to displace between each element in trajElems
	 */
	protected void drawSingleTraj(my_procApplet pa, int[] clr, float[] trajRect, float[] minMax, float[] trajElems, float widthPerElem) {
		pa.pushMatrix();pa.pushStyle();
		pa.translate(0.0f,trajRect[3],0.0f);
		pa.stroke(clr[0],clr[1],clr[2],clr[3]);
		pa.setStrokeWt(1.0f);
		float[] oldLoc = new float[] {0.0f, 0.0f}, loc;
		for(int i=0;i<trajElems.length;++i) {
			loc = new float[] {oldLoc[0]+widthPerElem, -trajElems[i]*trajRect[3]};
			pa.line(oldLoc[0],oldLoc[1],0.0f, loc[0],loc[1],0.0f);
			oldLoc=loc;
		}
		pa.popStyle();pa.popMatrix();
	}//drawSingleTraj
	
	/**
	 * draw a single trajectory within the bounds defined by trajRect
	 * @param pa
	 * @param trajRect rectangle of upper left corner x,y, width, height of drawable space for trajectory
	 * @param minMax min and max values of actual data - only for labels
	 * @param trajElems array of values 0->1 that lie between min (0) and max(1) 
	 * @param widthPerElem how far to displace between each element in trajElems
	 */
	protected void drawSingleTraj(my_procApplet pa, int[] clr, double[] trajRect, double[] minMax, double[] trajElems, double widthPerElem) {
		pa.pushMatrix();pa.pushStyle();
		pa.translate(0.0f,trajRect[3],0.0f);
		pa.stroke(clr[0],clr[1],clr[2],clr[3]);
		pa.setStrokeWt(1.0f);
		double[] oldLoc = new double[] {0.0f, 0.0f}, loc;
		for(int i=0;i<trajElems.length;++i) {
			loc = new double[] {oldLoc[0]+widthPerElem, -trajElems[i]*trajRect[3]};
			pa.line(oldLoc[0],oldLoc[1],0.0f, loc[0],loc[1],0.0f);
			oldLoc=loc;
		}
		pa.popStyle();pa.popMatrix();
	}//drawSingleTraj
	

	protected void drawSingleMinMaxTxt(my_procApplet pa, int clrLabel, String txt, float ltrMult) {pa.showOffsetText_RightSideMenu(pa.getClr(clrLabel, 255),1.4f* ltrMult, txt);			}
	
	
	protected final myPointf[][] buildPtTrajVals(ArrayList<myPointf> pts){
		int numVals = pts.size();
		myPointf[][] res = new myPointf[numStatsToMeasure][];
		res[posIDX]=new myPointf[numVals];
		res[velIDX]=new myPointf[numVals-1];
		res[accelIDX]=new myPointf[numVals-2];
		res[jerkIDX]=new myPointf[numVals-3];
		for(int i=0;i<res[posIDX].length;++i) {		res[posIDX][i] = new myPointf(pts.get(i));}
		for(int i=0;i<res[velIDX].length;++i) {		res[velIDX][i]=myVectorf._sub(res[posIDX][i+1], res[posIDX][i]);}
		for(int i=0;i<res[accelIDX].length;++i) {	res[accelIDX][i]=myVectorf._sub(res[velIDX][i+1], res[velIDX][i]);}
		for(int i=0;i<res[jerkIDX].length;++i) {	res[jerkIDX][i]=myVectorf._sub(res[accelIDX][i+1], res[accelIDX][i]);}		
		return res;
	}
	
	protected final myVectorf[][] buildVecTrajVals(ArrayList<myVectorf> vecs){
		int numVals = vecs.size();
		myVectorf[][] res = new myVectorf[numStatsToMeasure][];
		res[posIDX]=new myVectorf[numVals];
		res[velIDX]=new myVectorf[numVals-1];
		res[accelIDX]=new myVectorf[numVals-2];
		res[jerkIDX]=new myVectorf[numVals-3];
		for(int i=0;i<res[posIDX].length;++i) {		res[posIDX][i] = new myVectorf(vecs.get(i));}
		for(int i=0;i<res[velIDX].length;++i) {		res[velIDX][i]=myVectorf._sub(res[posIDX][i+1], res[posIDX][i]);}
		for(int i=0;i<res[accelIDX].length;++i) {	res[accelIDX][i]=myVectorf._sub(res[velIDX][i+1], res[velIDX][i]);}
		for(int i=0;i<res[jerkIDX].length;++i) {	res[jerkIDX][i]=myVectorf._sub(res[accelIDX][i+1], res[accelIDX][i]);}		
		return res;
	}
	
	protected final float[][] buildFloatTrajVals(ArrayList<Float> vals){
		int numVals = vals.size();
		float[][] res = new float[numStatsToMeasure][];
		res[posIDX]=new float[numVals];
		res[velIDX]=new float[numVals-1];
		res[accelIDX]=new float[numVals-2];
		res[jerkIDX]=new float[numVals-3];
		for(int i=0;i<res[posIDX].length;++i) {		res[posIDX][i] = vals.get(i);}
		for(int i=0;i<res[velIDX].length;++i) {		res[velIDX][i] = res[posIDX][i+1]- res[posIDX][i];}
		for(int i=0;i<res[accelIDX].length;++i) {	res[accelIDX][i] = res[velIDX][i+1]-res[velIDX][i];}
		for(int i=0;i<res[jerkIDX].length;++i) {	res[jerkIDX][i] = res[accelIDX][i+1]- res[accelIDX][i];}		
		return res;
	}
	
	protected final double[][] buildDoubleTrajVals(ArrayList<Double> vals){
		int numVals = vals.size();
		double[][] res = new double[numStatsToMeasure][];
		res[posIDX]=new double[numVals];
		res[velIDX]=new double[numVals-1];
		res[accelIDX]=new double[numVals-2];
		res[jerkIDX]=new double[numVals-3];
		for(int i=0;i<res[posIDX].length;++i) {		res[posIDX][i] = vals.get(i);}
		for(int i=0;i<res[velIDX].length;++i) {		res[velIDX][i] = res[posIDX][i+1]- res[posIDX][i];}
		for(int i=0;i<res[accelIDX].length;++i) {	res[accelIDX][i] = res[velIDX][i+1]-res[velIDX][i];}
		for(int i=0;i<res[jerkIDX].length;++i) {	res[jerkIDX][i] = res[accelIDX][i+1]- res[accelIDX][i];}		
		return res;
	}

}//class baseAnalyzer
