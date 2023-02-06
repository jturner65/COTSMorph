package COTS_Morph_PKG.utils.controlFlags;

import COTS_Morph_PKG.morph.base.baseMorph;
import COTS_Morph_PKG.utils.controlFlags.base.Base_ControlFlags;

public class morphCntlFlags extends Base_ControlFlags {
	protected final baseMorph owner;
	
	public morphCntlFlags(baseMorph _owner) {
		owner = _owner;
	}

	public morphCntlFlags(morphCntlFlags _otr) {
		super(_otr);
		owner = _otr.owner;
	}

	@Override
	protected void handleCntlFlagSet_Indiv(int idx, boolean val, boolean oldval) {
	}

	@Override
	protected void handleSettingDebug(boolean val) {
		owner.handleMapCntlDebug(val);
	}

}
