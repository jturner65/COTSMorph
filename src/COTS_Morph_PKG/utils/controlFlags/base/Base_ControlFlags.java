package COTS_Morph_PKG.utils.controlFlags.base;

import base_Utils_Objects.tools.flags.Base_BoolFlags;

/**
 * this class holds a flags/state machine object to control map calculations - used so that additional control options can be added easily without recoding
 * @author john
 *
 */
public abstract class Base_ControlFlags extends Base_BoolFlags{

	public static final int
		//debug is specified in base class as idx 0
		resetBranchingIDX 	= _numBaseFlags, 	//for spiral-based maps, reset the branching offset value
		copyBranchingIDX	= _numBaseFlags+1,	//for spiral-based maps, should copy branching explicitly from otrmap
		optimizeAlphaIDX 	= _numBaseFlags+2;	//for spiral-based maps, choose alpha to be closest to previous alpha
	
	/**
	 * # of control flags being managed
	 */
	private static final int numPrivFlags = _numBaseFlags+3;
	
	public Base_ControlFlags() {
		super(numPrivFlags);
	}
	
	public Base_ControlFlags(Base_ControlFlags _otr) {
		super(_otr);	
	}
	
	////////////////////
	// indiv values


	@Override
	protected final void handleFlagSet_Indiv(int idx, boolean val, boolean oldval) {
		switch(idx){
			case resetBranchingIDX 	: {break;} 
			case copyBranchingIDX 	: {break;}
			case optimizeAlphaIDX 	: {break;}
			default : {
				handleCntlFlagSet_Indiv(idx, val, oldval);
			}
		}		
	}
	protected abstract void handleCntlFlagSet_Indiv(int idx, boolean val, boolean oldval);
			
	public final boolean getResetBranching() {return getFlag(resetBranchingIDX);}
	public final void setResetBranching(boolean val) {setFlag(resetBranchingIDX, val);}
	
	public final boolean getOptimizeAlpha() {return getFlag(optimizeAlphaIDX);}
	public final void setOptimizeAlpha(boolean val) {setFlag(optimizeAlphaIDX, val);}
	
	public final boolean getCopyBranching() {return getFlag(copyBranchingIDX);}
	public final void setCopyBranching(boolean val) {setFlag(copyBranchingIDX, val);}

	
}//class mapCntlFlags
