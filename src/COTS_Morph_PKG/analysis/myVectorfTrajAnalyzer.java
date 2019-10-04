package COTS_Morph_PKG.analysis;

import java.util.ArrayList;

import COTS_Morph_PKG.analysis.base.baseVecTrajAnalyzer;
import COTS_Morph_PKG.analysis.prob.myProbSummary_ptOrVec;
import base_Utils_Objects.vectorObjs.myVectorf;

/**
 * measure and record the morph trajecto
 * @author john
 *
 */
public class myVectorfTrajAnalyzer extends baseVecTrajAnalyzer {
	/**
	 * all trajectory values : per stat, per t value list of point and vector trajectories
	 */
	public myVectorf[][] trajVals;

	public myVectorfTrajAnalyzer() {
		super();
	}

	/**
	 * find the average position, velocity, accel, etc of the passed trajectory of points
	 * assumes each position sample is uniformly spaced in time
	 * @param pts
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public final void analyzeTrajectory(ArrayList vecs, String name) {analyzeMyPtTrajectory((ArrayList<myVectorf>)vecs, name, debug);}
	private void analyzeMyPtTrajectory(ArrayList<myVectorf> vecs, String name, boolean _dbg) {
		if((null==vecs) || (vecs.size() < 4)) {return;}
		if(_dbg) {System.out.println("Analyzer " + ID+ " for : " + name  + " # Vecs : " +vecs.size() +" myVectorfTrajAnalyzer::analyzeTrajectory : ");}

		trajVals = buildVecTrajVals(vecs);
		for(int i=0;i<summaries.length;++i) {
			summaries[i]=new myProbSummary_ptOrVec(name +" " +statNames[i], trajVals[i], 4);
			if(_dbg) {System.out.println("\nSummary : " +summaries[i].toString());}
		}		
		if(_dbg) {System.out.println("Analyzer " + ID+ " for : " + name +" Done ");}
	}
	

}
