package patmob.data;

import java.awt.PopupMenu;
import java.util.Collection;
import patmob.core.TreeNodeInfoDisplayer;

/**
 * All types of data in PatMOb implement this interface, so they can be
 * easily displayed as PatmobTree nodes.
 */
public interface PatentTreeNode extends Comparable {
    public static final int PATENT_DOCUMENT   = 1,
                            PATENT_FAMILY     = 2,
                            PATENT_LIST       = 3,
                            PATENT_MAP        = 4,
//                            PATENT_COLLECTION = 3,                              //PATENT_LIST
//                            JOURNAL_ARTICLE   = 4,                              //PATENT_MAP
                            NET_FEATURE       = 5,
                            
                            HILITE_NONE     = 0,
                            HILITE_RED      = 10,
                            HILITE_GREEN    = 11,
                            HILITE_GOLD     = 12,
                            HILITE_BLUE     = 13;

    /**
     * Sets the color used to display this node. The available values are
     * HILITE_NONE, HILITE_RED, HILITE_GREEN, HILITE_GOLD, HILITE_BLUE.
     * @param hilite 
     */
    public void setHilite(int hilite);
    
    /**
     * Returns the color used to display this node in the tree.
     * @return 
     */
    public int getHilite();

    /**
     * Used by PatmobTreeWillExpandListener in lazy loading of the data tree.
     * @param b 
     */
    public void setDeep(boolean b);
    
    /**
     * Used by PatmobTreeWillExpandListener in lazy loading of the data tree.
     * @return true if has "grandchildren"
     */
    public boolean isDeep();

    @Override
    /**
     * The name showing up in the tree.
     */
    public String toString();

    /**
     * The number of children of this node.
     * @return 
     */
    public int size();

    /**
     * Adds a child node. A collection, family, patent or feature to collection,
     * a patent to family, or a feature to patent.
     * @param child 
     */
    public void addChild(PatentTreeNode child);
    
    /**
     * Removes a specific child.
     */
    //public void removeChild(PatentTreeNode child);                            //new method?
    
    /**
     * Removes all children.
     */
    public void removeChildren();
    
    /**
     * Returns reference to the underlying collection. For a map, the
     * collection is always sorted by keys?
     */
    public Collection<PatentTreeNode> getChildren();
    
    /**
     * Returns the sorted collection. For a map, the collection is allways 
     * sorted by keys?
     *
     */
    public Collection<PatentTreeNode> getSortedChildren();

    
    public PopupMenu getPatentTreePopup(TreeNodeInfoDisplayer nid);

    //TODO: return a swing panel?
    public String getInfo();

    public int getType();

    public void setID(int id);
    public int getID();
    public void setParentID(int pid);
    public int getParentID();

    public void setName(String name);
    public String getName();
    public void setDescription(String d);
    public String getDescription();
}
