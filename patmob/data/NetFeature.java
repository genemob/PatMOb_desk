package patmob.data;

import java.awt.PopupMenu;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import patmob.core.TreeNodeInfoDisplayer;

/**
 *
 * @author piotr
 */
public class NetFeature implements PatentTreeNode {
    private int dbID = 0, parentID, hilite = PatentTreeNode.HILITE_NONE;
    private boolean deep = true;
    private String name = "", description = "\nDO NOT CHANGE FIRST LINE!";
    private ArrayList<PatentTreeNode> children = new ArrayList<PatentTreeNode>();
    
    public NetFeature(String n, String url) {
        name = n;
        description = url + description;
    }

    /**
     * When using this constructor remember to add URL with setDescription...
     * @param n 
     */
    public NetFeature(String n) {
        name = n;
    }

    /**
     * Follow by setName(String name) & setDescription(String url)
     */
    public NetFeature(){}
    
    @Override
    public void setHilite(int hi) {
        hilite = hi;
    }

    @Override
    public int getHilite() {
        return hilite;
    }

    @Override
    public void setDeep(boolean b) {
        deep = b;
    }

    @Override
    public boolean isDeep() {
        return deep;
    }

    @Override
    public int size() {
        return children.size();
    }

    @Override
    public void addChild(PatentTreeNode child) {
        children.add(child);
    }

    @Override
    public void removeChildren() {
         children.removeAll(children);
    }

    @Override
    public Collection<PatentTreeNode> getChildren() {
        return children;
    }

    @Override
    public Collection<PatentTreeNode> getSortedChildren() {
        Collections.sort(children);
        return children;
    }

    @Override
    public PopupMenu getPatentTreePopup(TreeNodeInfoDisplayer nid) {
        return new PopupMenu();
    }

    @Override
    public String getInfo() {
        return description.split("\n")[0];
    }

    @Override
    public int getType() {
        return PatentTreeNode.NET_FEATURE;
    }

    @Override
    public void setID(int id) {
        dbID = id;
    }

    @Override
    public int getID() {
        return dbID;
    }

    @Override
    public void setParentID(int pid) {
        parentID = pid;
    }

    @Override
    public int getParentID() {
        return parentID;
    }

    @Override
    public void setName(String n) {
        name = n;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setDescription(String d) {
        description = d;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int compareTo(Object o) {
        return toString().compareTo(o.toString());
    }
    
    @Override
    public String toString() {
        return getName();
    }
}
