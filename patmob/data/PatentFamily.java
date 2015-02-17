package patmob.data;

import patmob.data.PatentDocument;
import java.awt.PopupMenu;
import java.util.*;
import patmob.core.TreeNodeInfoDisplayer;

/**
 * Representation of a patent family (documents sharing priorities).
 * Extends TreeMap for sorting/removing children in PatentTree.
 * @author piotr
 */
public class PatentFamily extends TreeMap<String, PatentTreeNode>
        implements PatentTreeNode {
    public static final int CAPLUS  = 1,
                            WPIX    = 2,
                            INPADOC = 3,
                            PATBASE = 4,
                            UNKNOWN = 0;

    private int familySource = 0, dbID = 0, parentID, 
            hilite = PatentTreeNode.HILITE_NONE;
    private boolean deep = true;
    private String accessionNumber = "", label = "", summary = "",
            title = "", name = "", description="";
    private ArrayList<String> lineList;
    
    public PatentFamily (int type) {
        familySource = type;
    }

    public PatentFamily (String n) {
        name = n;
    }
    
    /**
     * Always follow by setName(String s)
     */
    public PatentFamily(){}

    @Override
    public String toString() {
        String source = "Unknown";
        if (!name.equals("")) return name;
        else if(!title.equals("")) return title;
        else {
            switch(familySource) {
                case CAPLUS:
                    source = "CAplus";
                    break;
                case WPIX:
                    source = "WPIX";
                    break;
                case INPADOC:
                    source = "INPADOCDB";
                    break;
                case PATBASE:
                    source = "PatBase";
            }
            return source + " " + getAccessionNumber();
        }
//        if (!label.equals("")) return title + "  [" + label + "]";
//        else return (title + " (" + accessionNumber + ")");
    }

    public void setFamilySource(int i) {familySource = i;}
    public int getFamilySource() {return familySource;}
    public void setAccessionNumber(String s) {accessionNumber = s;}
    public String getAccessionNumber() {return accessionNumber;}
    public void setLabel(String s) {label = s;}
    public String getLabel() {return label;}
    public void setSummary(String s) {summary = s;}
    public String getSummary() {return summary;}
    public void setTitle(String s) {title = s;}
    public String getTitle() {return title;}
    public void setLineList(ArrayList<String> ll) {lineList = ll;}
    public ArrayList<String> getLineList() {return lineList;}

    public String addPatent (PatentDocument pd) {
        put(pd.toString(), pd);
        return pd.toString();
    }

    public Iterator<String> patentNames() {
        return keySet().iterator();
    }

    public Collection<PatentTreeNode> getSortedChildren() {
//        ArrayList children = new ArrayList(values());
//        Collections.sort(children);
//        return children;
        return values();
    }

    public PopupMenu getPatentTreePopup(TreeNodeInfoDisplayer nid) {
        return new PopupMenu();
    }

    public String getInfo() {
        return toString() + "\n\n" + getDescription();
    }

    public int getType() {
        return PatentTreeNode.PATENT_FAMILY;
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

    @Override
    public void addChild(PatentTreeNode child) {
        //FOR NOW, all parent-child combinations allowed
//        if (child.getType()==PatentTreeNode.PATENT_DOCUMENT)
//            this.addPatent((PatentDocument)child);
        put(child.toString(), child);
    }

    public int compareTo(Object o) {
        return toString().compareTo(o.toString());
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<PatentTreeNode> getChildren() {
//        ArrayList<PatentTreeNode> al = new ArrayList<PatentTreeNode>(values());
//        ArrayList<PatentTreeNode> al = new ArrayList<PatentTreeNode>();
//        Iterator it = values().iterator();
//        while (it.hasNext()) al.add((PatentTreeNode)it.next());
//        return al;
        return values();
    }

    public void setName(String n) {
        name = n;
    }

    public String getName() {
        return name;
    }
    public void setDescription(String d) {description=d;}
    public String getDescription() {return description;}

    public void removeChildren() {
        throw new UnsupportedOperationException("Not supported yet.");
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
