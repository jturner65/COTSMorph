package COTS_Morph_PKG.analysis.base;

import java.util.TreeMap;

import COTS_Morph_PKG.analysis.stats.myProbSummary_ptOrVec;
import COTS_Morph_PKG.analysis.stats.base.baseProbSummary;
import base_UI_Objects.IRenderInterface;
import base_UI_Objects.my_procApplet;

/**
 * base class for analyzers that work on points and vectors
 * @author john
 *
 */
public abstract class baseVecTrajAnalyzer extends baseAnalyzer {

	public baseVecTrajAnalyzer() {
		super();
		summaries = new myProbSummary_ptOrVec[numStatsToMeasure];	
	}

	@Override
	protected final void drawSingleSummary(my_procApplet pa, String[] mmntDispLabels, baseProbSummary smryRaw, float txtLineYDisp, float ltrMult) {
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
	}//	drawSingleSummary

	@Override
	protected final void drawSingleSmryGraph(my_procApplet pa, String[] mmntDispLabels, baseProbSummary smryRaw, float txtLineYDisp,float ltrMult) {
		
	}
	

}
