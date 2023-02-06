package COTS_Morph_PKG.utils.controlFlags;

import COTS_Morph_PKG.map.base.baseMap;
import COTS_Morph_PKG.utils.controlFlags.base.Base_ControlFlags;

/**
 * this class holds a flags/state machine object to control map calculations - used so that additional control options can be added easily without recoding
 * @author john
 *
 */
public class mapCntlFlags extends Base_ControlFlags{
	
	protected final baseMap owner;
	
	public mapCntlFlags(baseMap _owner) {
		super();
		owner = _owner;
	}
	
	public mapCntlFlags(mapCntlFlags _otr) {
		super(_otr);		
		owner = _otr.owner;
	}
	
	////////////////////
	// indiv values

			

	@Override
	protected void handleSettingDebug(boolean val) {
		owner.handleMapCntlDebug(val);		
	}

	@Override
	protected void handleCntlFlagSet_Indiv(int idx, boolean val, boolean oldval) {
		// TODO Auto-generated method stub
		
	}

	
}//class mapCntlFlags
