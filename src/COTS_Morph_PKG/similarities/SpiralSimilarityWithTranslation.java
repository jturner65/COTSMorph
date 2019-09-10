package COTS_Morph_PKG.similarities;

import COTS_Morph_PKG.similarities.base.baseSimilarity;
import base_UI_Objects.IRenderInterface;
import base_UI_Objects.my_procApplet;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVectorf;

public class SpiralSimilarityWithTranslation extends baseSimilarity {
	
	protected myVectorf translation;

	public SpiralSimilarityWithTranslation(myVectorf _n, myVectorf _I, myVectorf _J) {
		super(_n, _I, _J);
		translation = new myVectorf();
	}

	public SpiralSimilarityWithTranslation(SpiralSimilarityWithTranslation _otr) {
		super(_otr);
		translation = new myVectorf(_otr.translation);
	}

	/**
	 * how many transforms this similiarity consists of
	 */
	@Override
	protected final int getNumSpiralTransforms() { return 1;}

	@Override
	protected void _reset_Indiv() {	}
	
	/**
	 * this similarity is not built from control points - instead it uses the control points array to encode 
	 * the translation/rotation/scale values
	 * ara idx 0 : dispBetweenMaps
	 * ara idx 1 : new myPointf(angle,Scale,0.0f)},resetBranching);
	 */
	@Override
	public void deriveSimilarityFromCntlPts(myPointf[] cntlPts, boolean forceResetBranching) {
		translation = new myVectorf(cntlPts[0]);
		//float scale, float angle, myPointf A, myPointf B
		trans[0].buildTransformation(cntlPts[1].x, cntlPts[1].y, cntlPts[2],cntlPts[3]);		
	}

	/**
	 * calc transformation point for given point and spiral quantities
	 * @param A base point to transform
	 * @param t time 
	 * @return
	 */	
	@Override			
	public final myPointf transformPoint(myPointf A, float t) {	
		myPointf pt = trans[0].transformPoint(A, t);
		//pt._add(myVectorf._mult(translation, t));
		return pt;		
	}

	/**
	 * this similarity cannot manage tx/ty interpolant - uses tx for rot/scale, ty for translation
	 * @param A corner of map
	 * @param tx interpolant along undeformed map x
	 * @param ty interpolant along underformed map y
	 * @param I ortho to norm, 'x' dir in COTS plane
	 * @param J ortho to norm, 'y' dir in COTS plane
	 * @return
	 */
	@Override
	public final myPointf mapPoint(myPointf A, float tx, float ty) {
		myPointf pt = trans[0].transformPoint(A, tx);
		//pt._add(myVectorf._mult(translation, ty));
		return pt;		

	}
	
	@Override
	protected float drawRightSideBarMenuDescr_Indiv(my_procApplet pa, float yOff, float sideBarYDisp) {	
		pa.translate(10.0f, 0.0f, 0.0f);
		pa.pushMatrix();pa.pushStyle();		
		pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_White, 255), 5.5f, "Translation : ");
		pa.showOffsetText_RightSideMenu(pa.getClr(IRenderInterface.gui_LightCyan, 255), 7.0f, translation.toStrBrf());
		pa.popStyle();pa.popMatrix();
		yOff += sideBarYDisp;
		pa.translate(-10.0f,sideBarYDisp, 0.0f);

		return yOff;	}

}
