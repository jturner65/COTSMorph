package COTS_Morph_PKG.utils;

/**
 * structure holding UI-derived data used to update maps
 * @author john
 *
 */
public class mapUpdFromUIData {
	/**
	 * # of cells to set in patch per side
	 */
	public int numCellsPerSide;
	/**
	 * branchSharingStrategy strategy for sharing angle branching, for cots maps.  
	 * 			0 : no branch sharing
	 * 			1 : force all branching to map A's branching
	 * 			2 : force all branching to map B's branching
	 * 			3 : force all branching to most recent edited map's branching
	 */
	public int branchSharingStrategy;
	/**
	 * state flags
	 */
	public int[] flags;
	public static final int
		forceUpdateIDX 			= 0;
	public static final int 
		numFlags				= 1;
	/**
	 * 
	 * @param ints : idx 0 : numCellsPerSide; idx 1 : branchSharingStrategy
	 * @param bools : idx 0 : forceUpdate;
	 */
	public mapUpdFromUIData(int[] ints, boolean[] bools) {
		initFlags();
		setFlags(forceUpdateIDX, bools[0]);
		numCellsPerSide = ints[0];
		branchSharingStrategy = ints[1];	
	}
	
	
	//boolean flags init
	public void initFlags(){flags = new int[1 + numFlags/32];for(int i =0; i<numFlags;++i){setFlags(i,false);}}		

	public boolean getFlags(int idx){int bitLoc = 1<<(idx%32);return (flags[idx/32] & bitLoc) == bitLoc;}	
	public void setFlags(int idx, boolean val){
		int flIDX = idx/32, mask = 1<<(idx%32);
		flags[flIDX] = (val ?  flags[flIDX] | mask : flags[flIDX] & ~mask);
		switch(idx){
			case forceUpdateIDX : {break;}
		}
	}//setFlags
	
	public boolean forceUpdate() {return getFlags(forceUpdateIDX);}

}//class mapUpdateFromUIData
