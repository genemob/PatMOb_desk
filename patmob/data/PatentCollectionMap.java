package patmob.data;

import java.awt.PopupMenu;
import java.util.*;
import patmob.core.TreeNodeInfoDisplayer;

/**
 * Tree node collection as a TreeMap - to support getSortedChildren() 
 * which will allow modifications of the underlying 
 */
public class PatentCollectionMap extends TreeMap<String,PatentTreeNode>
        implements PatentTreeNode {
    private String name = "Patent Collection", 
            description = "";
    private int dbID = 0, parentID, hilite = PatentTreeNode.HILITE_NONE;
    private boolean deep = true;

    public PatentCollectionMap(String s) {name = s;}
    public PatentCollectionMap() {}

    @Override
    public void addChild(PatentTreeNode pte) {
        put(pte.toString(), pte);
    }

    //sorted alphabetically
    @Override
    public Collection<PatentTreeNode> getSortedChildren() {
//        ArrayList children = new ArrayList(values());
//        Collections.sort(children);
//        return children;
        return values();
    }

    @Override
    public String toString() {return name;}

    public void setName(String n) {name=n;}
    public String getName() {return name;}
    public void setDescription(String d) {description=d;}
    public String getDescription() {return description;}

    public PopupMenu getPatentTreePopup(TreeNodeInfoDisplayer nid) {
        return new PopupMenu();
    }

    public String getInfo() {
        return toString() + "\n\n" + getDescription();
    }

    public int getType() {
        return PatentTreeNode.PATENT_MAP;
    }

    public void setID(int id) {
        dbID = id;
    }

    public int getID() {
        return dbID;
    }

    public void setParentID(int pid) {
        parentID = pid;
    }

    public int getParentID() {
        return parentID;
    }

    public int compareTo(Object o) {
        return toString().compareTo(o.toString());
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<PatentTreeNode> getChildren() {
        return values();
    }

    public void removeChildren() {
        keySet().removeAll(keySet());
    }

    public void setHilite(int hl) {
        hilite = hl;
    }

    public int getHilite() {
        return hilite;
    }

    public void setDeep(boolean b) {
        deep = b;
    }

    public boolean isDeep() {
        return deep;
    }
}
