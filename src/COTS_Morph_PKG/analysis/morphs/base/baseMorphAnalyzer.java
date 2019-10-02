package COTS_Morph_PKG.analysis.morphs.base;

import java.util.ArrayList;

import COTS_Morph_PKG.analysis.prob.base.baseProbSummary;
import COTS_Morph_PKG.morphs.base.baseMorph;
import base_UI_Objects.IRenderInterface;
import base_UI_Objects.my_procApplet;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

public abstract class baseMorphAnalyzer {
	public final int ID;
	protected static int idGen = 0;
	//protected my_procApplet pa;
	protected baseMorph ownrMorph;
	/**
	 * current morph trajectory summary
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

	public baseMorphAnalyzer(baseMorph _ownrMorph) {
		ID = idGen++;
		ownrMorph=_ownrMorph; 
	}
		
	@SuppressWarnings("rawtypes")
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
	
	public void drawAnalyzerGraphs(my_procApplet pa, String[] mmntDispLabels, float[] trajWinDims, String name) {
		float yDisp = trajWinDims[3];
		pa.pushMatrix();pa.pushStyle();		
		pa.translate(5.0f, yDisp, 0.0f);
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Black, 255), 6.0f, name);
		pa.popStyle();pa.popMatrix();
		pa.pushMatrix();pa.pushStyle();
			pa.translate(5.0f, 2*yDisp, 0.0f);			
			drawAllGraphInfo(pa,mmntDispLabels, yDisp, trajWinDims[0]);
		pa.popStyle();pa.popMatrix();
		
		pa.translate(trajWinDims[0], 0.0f, 0.0f);
		pa.line(0.0f,trajWinDims[2], 0.0f, 0.0f, trajWinDims[0]+ trajWinDims[2], 0.0f );
	}//_drawAnalyzerData
	
	
	
	public final void drawAllSummaryInfo(my_procApplet pa, String[] mmntDispLabels, float txtLineYDisp, float perDispBlockWidth) {//
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
	protected abstract boolean drawSingleSummary(my_procApplet pa, String[] mmntDispLabels, baseProbSummary smryRaw, float txtLineYDisp, float ltrMult);
	
	/**
	 * draw stats graph info
	 * @param mmntDispLabels
	 * @param txtLineYDisp
	 * @param perDispBlockWidth
	 */
	public final void drawAllGraphInfo(my_procApplet pa, String[] mmntDispLabels, float txtLineYDisp, float perDispBlockWidth) {
		float mult = (perDispBlockWidth)/((mmntDispLabels.length + 1) * 3.1f);
		pa.pushMatrix();pa.pushStyle();
		if(mult < 17) {pa.scale(1.0f,.93f,1.0f);}
		for(int i=0;i<summaries.length;++i) {		//per summary - single summary for pos, vel, accel, jerk, etc of each point
			//title of each summary graph 
			pa.pushMatrix();pa.pushStyle();
				pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Black, 255),1.4f* mult, statDispLabels[i]);				
			pa.popStyle();pa.popMatrix();			
			pa.translate(0.0f,txtLineYDisp,0.0f);

			
			drawSingleSmryGraph(pa,mmntDispLabels, summaries[i],txtLineYDisp,mult);
			pa.translate(0.0f,.8f*txtLineYDisp,0.0f);
		}
		pa.popStyle();pa.popMatrix();
	}//drawAllGraphInfo
	protected abstract boolean drawSingleSmryGraph(my_procApplet pa, String[] mmntDispLabels, baseProbSummary smryRaw, float txtLineYDisp, float ltrMult);
	
	
	protected myPointf[][] buildPtTrajVals(ArrayList<myPointf> pts){
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
	
	protected float[][] buildFloatTrajVals(ArrayList<Float> vals){
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
	
	protected double[][] buildDoubleTrajVals(ArrayList<Double> vals){
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
	

}//class baseMorphAnalyzer
