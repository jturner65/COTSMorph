package COTS_Morph_PKG.ui;

import java.util.LinkedHashMap;

import COTS_Morph_PKG.ui.base.COTS_MorphWin;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;
import base_Render_Interface.IGraphicsAppInterface;
import base_UI_Objects.GUI_AppManager;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;

public class COTS_Morph2DWin extends COTS_MorphWin {
    
    private int _numPrivButtons = numBaseCOTSWinPrivFlags + 0;

    public COTS_Morph2DWin(IGraphicsAppInterface _p,  GUI_AppManager _AppMgr, int _winIdx) {
        super(_p, _AppMgr, _winIdx, false);        
        
    }
    @Override
    public final String getWinName() {return "COTS_Morph2DWin";}
    
    @Override
    protected final void initMe_Indiv() {}//initMe

    @Override
    protected void initDispFlags_Indiv() {}
    /**
     * return the initial bounds for the maps in the world space
     * @return 2-d array of 4 points - first idx is map idx, 2nd idx is 4 points
     */
    protected final myPointf[][] getKeyFrameMapBndPts(){
        myPointf[][] bndPts = new myPointf[2][4];
        //width of area per map
        float widthPerMap = .5f*winInitVals.rectDim[2],     halfWidth = .5f*widthPerMap;
        float size = winInitVals.rectDim[3] * .35f,            halfSize = .5f * size;
        
        float minX =winInitVals.rectDim[0]+ halfWidth - halfSize, minY = (winInitVals.rectDim[1]+.5f*winInitVals.rectDim[3]) - .5f*size - 150.0f;        
        float maxX = minX + size, maxY = minY + size;
        
        bndPts[0] = new myPointf[]{ new myPointf(minX+.1f, minY-.1f,0),
                                    new myPointf(maxX, minY,0),
                                    new myPointf(maxX, maxY,0),
                                    new myPointf(minX, maxY,0)};
        
        bndPts[1] = new myPointf[]{ new myPointf(minX + widthPerMap-.1f, minY-.2f,0),
                                    new myPointf(maxX + widthPerMap-.2f, minY+.1f,0),
                                    new myPointf(maxX + widthPerMap, maxY,0),
                                    new myPointf(minX + widthPerMap+.2f, maxY,0)};
            
        return bndPts;
    }

    /**
     * Retrieve the total number of defined privFlags booleans (application-specific state bools and interactive buttons)
     */
    @Override
    public int getTotalNumOfPrivBools(){        return _numPrivButtons;    }//initAllPrivBtns
    
    /**
     * Build all UI objects to be shown in left side bar menu for this window. This is the first child class function called by initThisWin
     * @param tmpUIObjMap : map of GUIObj_Params, keyed by unique string, with values describing the UI object
     *             - The object IDX                   
     *          - A double array of min/max/mod values                                                   
     *          - The starting value                                                                      
     *          - The label for object                                                                       
     *          - The object type (GUIObj_Type enum)
     *          - A boolean array of behavior configuration values : (unspecified values default to false)
     *               idx 0: value is sent to owning window,  
     *               idx 1: value is sent on any modifications (while being modified, not just on release), 
     *               idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
     *          - A boolean array of renderer format values :(unspecified values default to false) - Behavior Boolean array must also be provided!
     *                 - Should be multiline
     *                 - One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
     *                 - Force this object to be on a new row/line (For side-by-side layouts)
     *                 - Text should be centered (default is false)
     *                 - Object should be rendered with outline (default for btns is true, for non-buttons is false)
     *                 - Should have ornament
     *                 - Ornament color should match label color r 
     */
    @Override
    protected final void setupGUIObjsAras_Indiv(LinkedHashMap<String, GUIObj_Params> tmpUIObjMap) {}

    /**
     * Build all UI buttons to be shown in left side bar menu for this window. This is for instancing windows to add to button region
     * @param firstIdx : the first index to use in the map/as the objIdx
     * @param tmpUIBoolSwitchObjMap : map of GUIObj_Params to be built containing all flag-backed boolean switch definitions, keyed by sequential value == objId
     *                 the first element is the object index
     *                 the second element is true label
     *                 the third element is false label
     *                 the final element is integer flag idx 
     */
    @Override
    protected final void setupGUIBoolSwitchAras_Indiv(int firstIdx, LinkedHashMap<String, GUIObj_Params> tmpUIBoolSwitchObjMap) {}
        
    /**
     * which indexes are for rows, columns and slices
     */
    @Override
    protected final int[] getRowColSliceIDXs() {        return new int[] {1,2,3};    }
    
    /**
     * Check class-specific int/list ui objs upon population.  returns true if found
     */
    @Override
    protected boolean setUI_IntValsCustom_Indiv(int UIidx, int ival) {        return false;    }
    /**
     * Check class-specific float ui objs upon population.  returns true if found
     */
    @Override
    protected boolean setUI_FloatValsCustom_Indiv(int UIidx, float val) {        return false;    }
    @Override
    public void setPrivFlags_Indiv(int idx, boolean val) {
        switch (idx) {// special actions for each flag
            default : {}
        }
    }

    /////////////////////////////
    // draw routines
    @Override
    protected final void _drawMe_Indiv(float animTimeMod){}//_drawMe_Indiv    
    
    /**
     * called by maps
     * @param p
     * @param lbl
     * @param xOff
     * @param yOff
     */
    public final void _drawLabelAtPt(myPointf p, String lbl, float xOff, float yOff) {
        ri.pushMatState();    
        ri.translate(p);
        ri.scale(txtSclVal);
        ri.showText(lbl, xOff,yOff,0); 
        ri.popMatState();
    }
        
    ////////////////////////
    // keyboard and mouse

    @Override
    public final Float findDistToPtOrRay(myPointf _pt0, myPointf _pt, myPointf _notUsedPt, myVectorf _notUsedVec) {        return myPointf._SqrDist(_pt0, _pt);    };
    @Override
    public final myPointf getMouseClkPtInWorld(myPoint mseClckInWorld,int mouseX, int mouseY) {return new myPointf(mouseX,mouseY,0);}

    /**
     * handle map-specific mouse drag interaction
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param pmouseX previous mouse x
     * @param pmouseY previous mouse y
     * @param mouseClickIn3D 3d location of mouse 
     * @param mseDragInWorld displacement vector of mouse, in plane of screen normal
     * @param mseBtn which button was pressed
     */
    
    @Override
    protected final void handleMapMseDrag(int mouseX, int mouseY, int pmouseX, int pmouseY, myPoint mouseClickIn3D, myVector mseDragInWorld, int mseBtn) {
        //myVectorf mseDragInWorld_f = new myVectorf(mseDragInWorld);
        myVectorf defVec = new myVectorf(1.0f*(mouseX-pmouseX), 1.0f*(mouseY-pmouseY),0.0f);
        myPointf mseClickIn3D_f = new myPointf(mouseX, mouseY, 0);
        //mapManagers[currMapTypeIDX].currMseModMap.mseDragInMap(defVec, mseClickIn3D_f,keyPressed,keyCodePressed);
        boolean changed = mapManagers[currMapTypeIDX].mseDragInMap(defVec, mseClickIn3D_f,keyPressed,keyCodePressed);
        //currMseModMap.mseDrag_2D(mouseX, mouseY, 1.0f*(mouseX-pmouseX), 1.0f*(mouseY-pmouseY),mseDragInWorld,keyPressed,keyCodePressed);
        if(changed) {
            if(uiMgr.getPrivFlag(showOrientedLineupIDX)) {mapManagers[currMapTypeIDX].buildOrientedLineup();    }
        }
    }
    
    @Override
    protected final void mouseRelease_IndivMorphWin(){}

    @Override
    protected final myPoint getMsePtAs3DPt(myPoint mseLoc) {    return new myPoint(mseLoc.x, mseLoc.y, 0);}

}//class COTS_Morph2DWin
