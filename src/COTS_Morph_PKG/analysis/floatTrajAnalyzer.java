package COTS_Morph_PKG.analysis;

import java.util.ArrayList;
import java.util.TreeMap;

import COTS_Morph_PKG.analysis.base.baseAnalyzer;
import COTS_Morph_PKG.analysis.stats.myProbSummary_Flts;
import COTS_Morph_PKG.analysis.stats.base.baseProbSummary;
import base_UI_Objects.IRenderInterface;
import base_UI_Objects.my_procApplet;

public class floatTrajAnalyzer extends baseAnalyzer {
	/**
	 * all trajectory values : per stat, per t value list of point and vector trajectories
	 */
	public float[][] trajVals;

	public floatTrajAnalyzer() {
		super();
		summaries = new myProbSummary_Flts[numStatsToMeasure];	
	}

	/**
	 * find the average value, ROC, rate of ROC, etc of the passed trajectory of areas of morph maps
	 * assumes each area sample is uniformly spaced in time
	 * @param pts
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override	
	public void analyzeTrajectory(ArrayList areas, String name) {analyzeAreaTrajectory((ArrayList<Float>)areas, name, debug);}
	private void analyzeAreaTrajectory(ArrayList<Float> areas, String name, boolean _dbg) {
		if((null==areas) || (areas.size() < 4)) {return;}
		if(_dbg) {System.out.println("Float Analyzer " + ID+ " for : " + name  + " # float vals : " +areas.size() +" floatTrajAnalyzer::analyzeTrajectory : ");}

		trajVals = buildFloatTrajVals(areas);
		for(int i=0;i<summaries.length;++i) {
			summaries[i]=new myProbSummary_Flts(name +" " +statNames[i], trajVals[i]);
			if(_dbg) {System.out.println("\nSummary : " +summaries[i].toString());}
		}		
		if(_dbg) {System.out.println("Analyzer " + ID+ " for : " + name +" Done ");}
	}

	@Override
	protected void drawSingleSummary(my_procApplet pa, String[] mmntDispLabels, baseProbSummary smryRaw, float txtLineYDisp, float ltrMult) {
		myProbSummary_Flts smry = ((myProbSummary_Flts)smryRaw);
		TreeMap<String,String> smryStrings = smry.summaryStringAra("A");
		pa.pushMatrix();pa.pushStyle();
		pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_Black, 255), ltrMult*.3f, smryStrings.get("summaryName"));
		for(int i=0;i<mmntDispLabels.length;++i) {
			showOffsetText_RightSideMenuAbs(pa, pa.getClr(IRenderInterface.gui_DarkBlue, 255), ltrMult*3.5f, smryStrings.get(mmntDispLabels[i]));
		}			
		pa.popStyle();pa.popMatrix();
		pa.translate(0.0f,txtLineYDisp,0.0f);	
		
	}//drawSingleSummary

	@Override
	protected void drawSingleSmryGraph(my_procApplet pa, String[] mmntDispLabels, baseProbSummary smryRaw, float txtLineYDisp,float ltrMult) {
	}
	
	

}//class morphAreaTrajAnalyzer
