package patmob.data;

import java.awt.PopupMenu;
import java.util.*;
import patmob.core.TreeNodeInfoDisplayer;

/**
 * Tree node collection as an ArrayList.
 * @author piotr
 */
public class PatentCollectionList extends ArrayList<PatentTreeNode>
        implements PatentTreeNode {
    private String name = "Patent Collection", 
            description = "";
    private int dbID = 0, parentID = 0, hilite = PatentTreeNode.HILITE_NONE;
    private boolean deep = true;

    public PatentCollectionList(String s) {name = s;}
    public PatentCollectionList() {}

    public void addChild(PatentTreeNode pte) {add(pte);}

    //sorted alphabetically
    public Collection<PatentTreeNode> getSortedChildren() {
        Collections.sort(this);
        return this;
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
        return PatentTreeNode.PATENT_LIST;
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
    }

    public Collection<PatentTreeNode> getChildren() {
        return this;
    }

    public void removeChildren() {
        this.removeAll(this);
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
