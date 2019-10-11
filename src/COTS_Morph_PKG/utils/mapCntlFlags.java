package COTS_Morph_PKG.utils;

/**
 * this class holds a flags/state machine object to control map calculations - used so that additional control options can be added easily without recoding
 * @author john
 *
 */
public class mapCntlFlags {
	/**
	 * bitwise representation of boolean control flags
	 */
	private int[] cntlFlags;
	
	public static final int
		debugIDX			= 0,
		resetBranchingIDX 	= 1, 	//for spiral-based maps, reset the branching offset value
		copyBranchingIDX	= 2,	//for spiral-based maps, should copy branching explicitly from otrmap
		optimizeAlphaIDX 	= 3;	//for spiral-based maps, choose alpha to be closest to previous alpha
	
	/**
	 * # of control flags being managed
	 */
	private static final int numPrivFlags = 4;
	
	public mapCntlFlags() {
		initFlags();
	}
	
	public mapCntlFlags(mapCntlFlags _otr) {
		initFlags();
		for(int i=0;i<cntlFlags.length;++i) {cntlFlags[i]=_otr.cntlFlags[i];}
		
	}
	
	//child-class flag init
	protected void initFlags(){cntlFlags = new int[1 + numPrivFlags/32]; resetAllFlags();}
	/**
	 * reset all map control flags
	 */
	public void resetAllFlags() {for(int i = 0; i<numPrivFlags; ++i){setFlag(i,false);}}
	/**
	 * set flag value
	 * @param idx
	 * @param val
	 */
	public void setFlag(int idx, boolean val) {
		int flIDX = idx/32, mask = 1<<(idx%32);
		cntlFlags[flIDX] = (val ?  cntlFlags[flIDX] | mask : cntlFlags[flIDX] & ~mask);
		switch(idx){
			case debugIDX			: {break;}
			case resetBranchingIDX 	: {break;} 
			case copyBranchingIDX 	: {break;}
			case optimizeAlphaIDX 	: {break;} 
		}
	}
	/**
	 * return passed flag value
	 * @param idx
	 * @return
	 */
	public boolean getFlag(int idx){int bitLoc = 1<<(idx%32);return (cntlFlags[idx/32] & bitLoc) == bitLoc;}	
	
	////////////////////
	// indiv values
	
	
	public boolean getDebug() {return getFlag(debugIDX);}
	public void setDebug(boolean val) {setFlag(debugIDX, val);}
	
	public boolean getResetBranching() {return getFlag(resetBranchingIDX);}
	public void setResetBranching(boolean val) {setFlag(resetBranchingIDX, val);}
	
	public boolean getOptimizeAlpha() {return getFlag(optimizeAlphaIDX);}
	public void setOptimizeAlpha(boolean val) {setFlag(optimizeAlphaIDX, val);}
	
	public boolean getCopyBranching() {return getFlag(copyBranchingIDX);}
	public void setCopyBranching(boolean val) {setFlag(copyBranchingIDX, val);}
		
	
}//class mapCntlFlags
