package COTS_Morph_PKG.analysis.morph;

import java.util.ArrayList;
import java.util.TreeMap;

import COTS_Morph_PKG.analysis.morph.base.baseMorphAnalyzer;
import COTS_Morph_PKG.analysis.prob.myProbSummary_ptOrVec;
import COTS_Morph_PKG.analysis.prob.base.baseProbSummary;
import COTS_Morph_PKG.morph.base.baseMorph;
import base_UI_Objects.IRenderInterface;
import base_UI_Objects.my_procApplet;
import base_Utils_Objects.vectorObjs.myPointf;

/**
 * a class that analyzes the performance of a morph trajectory
 * @author john
 *
 */
public class morphCntlPtTrajAnalyzer extends baseMorphAnalyzer{

	/**
	 * all trajectory values : per stat, per t value list of point and vector trajectories
	 */
	public myPointf[][] trajVals;
	
	public morphCntlPtTrajAnalyzer(baseMorph _ownrMorph) {
		super(_ownrMorph);	
		summaries = new myProbSummary_ptOrVec[numStatsToMeasure];	
	}
	
	/**
	 * find the average position, velocity, accel, etc of the passed trajectory of points
	 * assumes each position sample is uniformly spaced in time
	 * @param pts
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public final void analyzeTrajectory(ArrayList pts, String name) {analyzeMyPtTrajectory((ArrayList<myPointf>)pts, name, false);}
	private void analyzeMyPtTrajectory(ArrayList<myPointf> pts, String name, boolean _dbg) {
		if((null==pts) || (pts.size() < 4)) {return;}
		if(_dbg) {System.out.println("Analyzer " + ID+ " for : " + name  + " # Pts : " +pts.size() +" morphCntlPtTrajAnalyzer::analyzeTrajectory : ");}

		trajVals = buildPtTrajVals(pts);
		for(int i=0;i<summaries.length;++i) {
			summaries[i]=new myProbSummary_ptOrVec(name +" " +statNames[i], trajVals[i], ((i==0) || (i>3) ? 3 : 4));
			if(_dbg) {System.out.println("\nSummary : " +summaries[i].toString());}
		}		
		if(_dbg) {System.out.println("Analyzer " + ID+ " for : " + name +" Done ");}
	}
	

	@Override
	protected boolean drawSingleSummary(my_procApplet pa, String[] mmntDispLabels, baseProbSummary smryRaw, float txtLineYDisp, float ltrMult) {
		myProbSummary_ptOrVec smry = ((myProbSummary_ptOrVec)smryRaw);
			
		TreeMap<String,String>[] smryStrings = smry.summaryStringAra();
		for(int row=0;row<smryStrings.length;++row) {
			if(smryStrings[row].get(mmntDispLabels[mmntDispLabels.length-1]).toLowerCase().contains("nan")) {continue;}
			pa.pushMatrix();pa.pushStyle();
			pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Black, 255), ltrMult*.3f, smryStrings[row].get("summaryName"));
			for(int i=0;i<mmntDispLabels.length;++i) {
				showOffsetText_RightSideMenuAbs(pa, pa.getClr(IRenderInterface.gui_DarkBlue, 255), ltrMult*3.5f, smryStrings[row].get(mmntDispLabels[i]));
			}			
			pa.popStyle();pa.popMatrix();
			pa.translate(0.0f,txtLineYDisp,0.0f);	
		}
		return true;
	}//	drawSingleSummary

	@Override
	protected boolean drawSingleSmryGraph(my_procApplet pa, String[] mmntDispLabels, baseProbSummary smryRaw, float txtLineYDisp,float ltrMult) {
		
		return true;
	}
	
	
}//class baseMorphAnalyzer
