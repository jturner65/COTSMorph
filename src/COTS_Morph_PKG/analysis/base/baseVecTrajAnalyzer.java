package COTS_Morph_PKG.analysis.base;

import java.util.ArrayList;
import java.util.TreeMap;

import COTS_Morph_PKG.analysis.stats.myProbSummary_ptOrVec;
import COTS_Morph_PKG.analysis.stats.base.baseProbSummary;
import base_JavaProjTools_IRender.base_Render_Interface.IRenderInterface;

/**
 * base class for analyzers that work on points and vectors
 * @author john
 *
 */
public abstract class baseVecTrajAnalyzer extends baseAnalyzer {
	/**
	 * calculate these after trajectory values are calculated, only once, to display for graph
	 * 1st idx is summary, 2nd idx is x,y,z,mag(if present), 3rd idx is trajectory idx
	 */
	protected float[][][] perSummaryScaledValTrajs;
	/**
	 * build this after trajectories are summarized.  
	 * 1st idx is summary, 2nd idx is x,y,z,mag(if present) 3rd idx is min or max
	 */
	protected float[][][] perSummaryMinMax;
	
	
	public baseVecTrajAnalyzer() {
		super();
		summaries = new myProbSummary_ptOrVec[numStatsToMeasure];
		perSummaryScaledValTrajs = new float[summaries.length][][];
		perSummaryMinMax = new float[summaries.length][][];
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public final void analyzeTrajectory(ArrayList vecs, String name) {
		analyzeMyPtTrajectory_Indiv(vecs, name);
		for(int i=0;i<summaries.length;++i) {
			perSummaryScaledValTrajs[i] = ((myProbSummary_ptOrVec) summaries[i]).getScaledVals();
			perSummaryMinMax[i] = ((myProbSummary_ptOrVec) summaries[i]).getMinMax();
		}
	}
	@SuppressWarnings("rawtypes")
	protected abstract void analyzeMyPtTrajectory_Indiv(ArrayList vecs, String name);

	@Override
	protected final void drawSingleSummary(IRenderInterface pa, String[] mmntDispLabels, baseProbSummary smryRaw, float txtLineYDisp, float ltrMult) {
		myProbSummary_ptOrVec smry = ((myProbSummary_ptOrVec)smryRaw);
			
		TreeMap<String,String>[] smryStrings = smry.summaryStringAra();
		for(int row=0;row<smryStrings.length;++row) {
			if(smryStrings[row].get(mmntDispLabels[mmntDispLabels.length-1]).toLowerCase().contains("nan")) {continue;}
			pa.pushMatState();
			showOffsetText_RightSideMenu(pa,pa.getClr(IRenderInterface.gui_Black, 255), ltrMult*.3f, smryStrings[row].get("summaryName"));
			for(int i=0;i<mmntDispLabels.length;++i) {
				showOffsetText_RightSideMenuAbs(pa, pa.getClr(IRenderInterface.gui_DarkBlue, 255), ltrMult*3.5f, smryStrings[row].get(mmntDispLabels[i]));
			}			
			pa.popMatState();
			pa.translate(0.0f,txtLineYDisp,0.0f);	
		}
	}//	drawSingleSummary
	
	public int[][] trajClrs = new int[][] {
		{255,100,100,255},
		{0,255,0,255},
		{150,190,255,255},
		{255,0,255,255},
	};
	
	@Override
	protected final void drawSingleSmryGraph(IRenderInterface pa, String[] mmntDispLabels, int smryIdx, float[] graphRect, float ltrMult) {
		float[][] perSmry_Traj = perSummaryScaledValTrajs[smryIdx], perSmry_MinMax = perSummaryMinMax[smryIdx];
		float widthPerElem = graphRect[2]/(1.0f*perSmry_Traj[0].length);
		for(int sDim = 0; sDim < perSmry_Traj.length;++sDim) {
			pa.pushMatState();			
			//drawSingleTraj(IRenderInterface pa, int[] clr, float[] trajRect, float[] minMax, float[] trajElems, float widthPerElem)
			drawSingleTraj(pa, trajClrs[sDim], graphRect, perSmry_MinMax[sDim], perSmry_Traj[sDim],widthPerElem);
			pa.popMatState();
			
		}
		pa.translate(0.0f,graphRect[3],0.0f);				//draw all these lines on each other
	}
	
	@Override
	protected final void drawSingleSmryGraphMinMaxLbls(IRenderInterface pa, int smryIdx, float ltrMult) {
		float[][] perSmry_MinMax = perSummaryMinMax[smryIdx];
		pa.scale(.8f);
		float newLtrMult = 4.5f;
		//(IRenderInterface pa, int clrLabel, String txt, float ltrMult)
		drawSingleMinMaxTxt(pa, IRenderInterface.gui_Red, "["+ String.format(frmtStr,perSmry_MinMax[0][0])+", " + String.format(frmtStr,perSmry_MinMax[0][1])+"]",newLtrMult);
		drawSingleMinMaxTxt(pa, IRenderInterface.gui_DarkGreen, "["+ String.format(frmtStr,perSmry_MinMax[1][0])+", " + String.format(frmtStr,perSmry_MinMax[1][1])+"]",newLtrMult);
		drawSingleMinMaxTxt(pa, IRenderInterface.gui_DarkBlue, "["+ String.format(frmtStr,perSmry_MinMax[2][0])+", " + String.format(frmtStr,perSmry_MinMax[2][1])+"]",newLtrMult);
		if(perSmry_MinMax.length > 3) {
			drawSingleMinMaxTxt(pa, IRenderInterface.gui_Magenta, "["+ String.format(frmtStr,perSmry_MinMax[3][0])+", " + String.format(frmtStr,perSmry_MinMax[3][1])+"]",5.0f);
		}
	}
}//class baseVecTrajAnalyzer
