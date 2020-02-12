package COTS_Morph_PKG.analysis;

import java.util.ArrayList;

import COTS_Morph_PKG.analysis.base.baseVecTrajAnalyzer;
import COTS_Morph_PKG.analysis.stats.myProbSummary_ptOrVec;
import base_Math_Objects.vectorObjs.floats.myPointf;

/**
 * a class that analyzes the performance of a morph trajectory
 * @author john
 *
 */
public class myPointfTrajAnalyzer extends baseVecTrajAnalyzer{

	/**
	 * all trajectory values : per stat, per t value list of point and vector trajectories
	 */
	public myPointf[][] trajVals;
	
	public myPointfTrajAnalyzer() {
		super();			
	}
	
	/**
	 * find the average position, velocity, accel, etc of the passed trajectory of points
	 * assumes each position sample is uniformly spaced in time
	 * @param pts
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected final void analyzeMyPtTrajectory_Indiv(ArrayList pts, String name) {analyzeMyPtTrajectory((ArrayList<myPointf>)pts, name, debug);}
	protected void analyzeMyPtTrajectory(ArrayList<myPointf> pts, String name, boolean _dbg) {
		if((null==pts) || (pts.size() < 4)) {return;}
		if(_dbg) {System.out.println("Analyzer " + ID+ " for : " + name  + " # Pts : " +pts.size() +" myPointfTrajAnalyzer::analyzeTrajectory : ");}

		trajVals = buildPtTrajVals(pts);
		for(int i=0;i<summaries.length;++i) {
			summaries[i]=new myProbSummary_ptOrVec(name +" " +statNames[i], trajVals[i], ((i==0) || (i>3) ? 3 : 4));
			if(_dbg) {System.out.println("\nSummary : " +summaries[i].toString());}
		}		
		if(_dbg) {System.out.println("Analyzer " + ID+ " for : " + name +" Done ");}
	}
	

	
}//class baseMorphAnalyzer
