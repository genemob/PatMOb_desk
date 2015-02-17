package patmob.core;

import java.util.Collection;
import patmob.data.PatentTreeNode;

/**
 * This interface defines methods used to access the patmob database. 
 * Implemented by DerbyDB.
 * @author piotr
 */
public interface PatmobDB {

    /**
     * Not used - too slow - returns the whole database
     * @return 
     */
    public PatentTreeNode getPatentCollections();

    /**
     * Returns 2 level shallow node for lazy loading in the main window.
     * @param nodeID
     * @return 
     */
    public PatentTreeNode getUserCollections(int nodeID);

    /**
     * Returns a node with all children for editing in tree branch editor.
     * @param nodeID
     * @return 
     */
    public PatentTreeNode getFullBranch(int nodeID);

    /**
     * Saves a node with all children. 
     * @param collection
     * @return 
     */
    public int savePatentCollection(PatentTreeNode collection);

    /**
     * Deletes a node with all children.
     * @param collection
     * @return 
     */
    public int deletePatentCollection(PatentTreeNode collection);

    /**
     * Performs simple keyword search.
     * @param query
     * @return 
     */
    public Collection<String> keywordSearch(String query);
}
