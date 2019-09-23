package COTS_Morph_PKG.morphs.analysis.base;

import java.util.ArrayList;

import COTS_Morph_PKG.morphs.analysis.myProbSummary_ptOrVec;
import COTS_Morph_PKG.morphs.base.baseMorph;
import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import base_UI_Objects.IRenderInterface;
import base_UI_Objects.my_procApplet;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

public abstract class baseMorphAnalyzer {
	public final int ID;
	protected static int idGen = 0;
	protected my_procApplet pa;
	protected COTS_MorphWin win;
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
		ownrMorph=_ownrMorph; pa=ownrMorph.pa; win=ownrMorph.win;
	}
		
	@SuppressWarnings("rawtypes")
	public abstract void analyzeTrajectory(ArrayList pts, String name);
	
	/**
	 * this will properly format and display a string of text, and will translate the width, so multiple strings can be displayed on the same line with different colors
	 * @param tclr
	 * @param txt
	 */
	protected final void showOffsetText_RightSideMenuAbs(int[] tclr, float dist,  String txt) {
		pa.setFill(tclr,tclr[3]);pa.setStroke(tclr,tclr[3]);
		pa.text(txt,0.0f,0.0f,0.0f);
		pa.translate(dist, 0.0f,0.0f);	
	}
	
	public final void drawAllSummaryInfo(String[] mmntDispLabels, float txtLineYDisp, float perDispBlockWidth) {//
		float mult = (perDispBlockWidth)/((mmntDispLabels.length + 1) * 3.1f);//72.0f/this.ownrMorph.getNumTrajCntlPts();// 12.0f;
		//System.out.println("perDispBlockWidth: " + perDispBlockWidth + " | mult : " + mult);
		pa.pushMatrix();pa.pushStyle();
		if(mult < 17) {pa.scale(1.0f,.93f,1.0f);}
		for(int i=0;i<summaries.length;++i) {		//per summary - single summary for pos, vel, accel, jerk, etc of each point
			//title of each summary 
			pa.pushMatrix();pa.pushStyle();
				pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Black, 255),1.4f* mult, statDispLabels[i]);
				for(int j=0;j<mmntDispLabels.length;++j) {		showOffsetText_RightSideMenuAbs(pa.getClr(IRenderInterface.gui_DarkBlue, 255), mult*3.5f, mmntDispLabels[j]);}
			pa.popStyle();pa.popMatrix();			
			pa.translate(0.0f,txtLineYDisp,0.0f);

			
			drawSingleSummary( mmntDispLabels, summaries[i],txtLineYDisp,mult);
			pa.translate(0.0f,.8f*txtLineYDisp,0.0f);
		}
		pa.popStyle();pa.popMatrix();
	}
	protected abstract void drawSingleSummary(String[] mmntDispLabels, baseProbSummary smryRaw, float txtLineYDisp, float ltrMult);
	
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
	
	protected float[][] buildAreaTrajVals(ArrayList<Float> areas){
		int numVals = areas.size();
		float[][] res = new float[numStatsToMeasure][];
		res[posIDX]=new float[numVals];
		res[velIDX]=new float[numVals-1];
		res[accelIDX]=new float[numVals-2];
		res[jerkIDX]=new float[numVals-3];
		for(int i=0;i<res[posIDX].length;++i) {		res[posIDX][i] = areas.get(i);}
		for(int i=0;i<res[velIDX].length;++i) {		res[velIDX][i] = res[posIDX][i+1]- res[posIDX][i];}
		for(int i=0;i<res[accelIDX].length;++i) {	res[accelIDX][i] = res[velIDX][i+1]-res[velIDX][i];}
		for(int i=0;i<res[jerkIDX].length;++i) {	res[jerkIDX][i] = res[accelIDX][i+1]- res[accelIDX][i];}		
		return res;
	}
	

}//class baseMorphAnalyzer
