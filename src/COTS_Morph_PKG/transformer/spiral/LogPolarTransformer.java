package COTS_Morph_PKG.transformer.spiral;

import COTS_Morph_PKG.transform.SpiralTransform;
import COTS_Morph_PKG.transformer.spiral.base.baseSpiralTransformer;
import COTS_Morph_PKG.utils.mapCntlFlags;
import base_UI_Objects.my_procApplet;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

public class LogPolarTransformer extends baseSpiralTransformer {
	protected final int numTransforms;
	/**
	 * transform that uses the results of the individual transforms
	 */
	protected SpiralTransform AggregateTransform;
	public LogPolarTransformer(String _name, myVectorf _n, myVectorf _I, myVectorf _J, int _numTransforms) {
		super(_name+"_LogPolarTransfrmr", _n, _I, _J);numTransforms=_numTransforms;trans = initTransform();
		AggregateTransform = new SpiralTransform(name+"_aggr", norm,I,J);
	}

	public LogPolarTransformer(String _name, LogPolarTransformer _otr) {
		super(_name+"_LogPolarTransfrmr_Cpy", _otr);numTransforms=_otr.numTransforms;trans = initTransformFromCopy(_otr);
		AggregateTransform = new SpiralTransform(name+"_aggr", _otr.AggregateTransform);
	}
	
	/**
	 * build 3 spiral transforms, 1 for each edge comparison
	 */
	@Override
	protected int getNumSpiralTransforms() {		return numTransforms;}

	@Override
	protected void _reset_Indiv() {	}

	/**
	 * build similarity for 1 control point
	 */
	@Override
	public void deriveSimilarityFromCntlPts(myPointf[] cntlPts, mapCntlFlags flags) {
		int baseAPt = 0, baseBPt = numTransforms+1;
		float angle = 0.0f;
		float scale = 1.0f;
		for(int i=0;i<trans.length ;++i) {				
			int nextAPt = i+1, nextBPt = baseBPt + nextAPt;
			//System.out.println("Cntl points "+i+" : A0:" + cntlPts[baseAPt].toStrBrf() +" A"+nextAPt+":"+cntlPts[nextAPt].toStrBrf() + " | B0:"+ cntlPts[baseBPt].toStrBrf() +" B"+nextBPt+":"+cntlPts[nextBPt].toStrBrf());
			trans[i].buildTransformation(new myPointf[] {cntlPts[baseAPt],cntlPts[nextAPt]},new myPointf[] {cntlPts[baseBPt],cntlPts[nextBPt]}, flags);	
			angle += ((SpiralTransform) trans[i]).get_Angle();
			scale *= ((SpiralTransform) trans[i]).get_Scale();
		}
		angle/=trans.length;
		if(scale < 0) {System.out.println("Negative scale!");}
		scale = (float) Math.pow(scale, 1.0f/trans.length);		
		
		AggregateTransform.buildTransformation(angle, scale, cntlPts[baseAPt], cntlPts[baseBPt]);
	}

	@Override
	public myPointf transformPoint(myPointf A, int transformIDX, float t) {
		return AggregateTransform.transformPoint(A, t);
	}

	@Override
	public myPointf mapPoint(myPointf A, int[] transformIDX, float tx, float ty) {
		return AggregateTransform.transformPoint(A, tx);
	}

	@Override
	protected float drawRightSideBarMenuDescr_Indiv(my_procApplet pa, float yOff, float sideBarYDisp) {
		yOff += AggregateTransform.drawRightSideBarMenuDescr(pa, yOff, sideBarYDisp,"Aggregate");
		return yOff;
	}

}
