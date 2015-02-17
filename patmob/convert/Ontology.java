// package patmob.util;
package patmob.convert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import patmob.data.PatentCollectionList;
import patmob.data.PatentTreeNode;

/**
 * Utilities for ontologies.
 */
public class Ontology {

    /**
     * Parse ontology from text file where a child has one more tab than its parent.
     * <pre>
     * root
     *     child 1
     *         grandchild 1
     *         grandchild 2
     *     child 2
     * </pre>
     * @param tabText
     * @return
     */
    public static PatentTreeNode treeFromTabbedFile(File tabText) {
        PatentTreeNode ontoRoot = new PatentCollectionList();
        try {
            BufferedReader br = new BufferedReader(new FileReader(tabText));
            String line = br.readLine();        //first line = root
            ontoRoot.setName(line.trim());
            ArrayList<PatentTreeNode> ontoPath = new ArrayList<PatentTreeNode>();
            ontoPath.add(ontoRoot);
            ArrayList<String> lines = new ArrayList<String>();
            while ((line=br.readLine())!=null) lines.add(line);
            addCategory(lines, ontoPath);
        } catch (Exception x) {x.printStackTrace();}

        return ontoRoot;
    }

    private static void addCategory(ArrayList<String> lines,
            ArrayList<PatentTreeNode> ontoPath) {

        String line = lines.remove(0);
        int level = line.lastIndexOf("\t") + 1; //top level no tab
        if (level>0) {  //real terms must have at least one tab!
            PatentTreeNode term = new PatentCollectionList(line.trim());
            if (level>=ontoPath.size()) ontoPath.add(term);
            else ontoPath.set(level, term);
            ontoPath.get(level-1).addChild(term);
        }
        if (lines.size()>0) addCategory(lines, ontoPath);

    }

    /**
     * Takes an ontology tree and fills it with collections from a HashMap,
     * deleting unused terms.
     * LIMITATION: assumes only leaf nodes found in features.
     * @param term
     * @param features
     * @return 
     */
    //with the 2 modifications picks up all nodes - bugs saving...
//    public static boolean checkTermForMembers(PatentTreeNode term, HashMap features) {
//        boolean hasMembers = false;
//        if (features.containsKey(term.getName())) {
//            PatentTreeNode found = (PatentTreeNode) features.get(term.getName());
//            Iterator<PatentTreeNode> i1 = found.getChildren().iterator();
//            while (i1.hasNext()) term.addChild(i1.next());
//             hasMembers = true;      //return
//        }  if (term.size()>0) {        //else
//            Iterator<PatentTreeNode> it = term.getChildren().iterator();
//            while (it.hasNext()) {
//                PatentTreeNode ptn = it.next();
//                if (!checkTermForMembers(ptn, features)) it.remove();
//                else hasMembers = true;
//            }
//        }
//        return hasMembers;
//    }
    //picks only leaf nodes
    public static boolean checkTermForMembers(PatentTreeNode term, HashMap features) {
        boolean hasMembers = false;
        if (term.size()>0) {        //else
            Iterator<PatentTreeNode> it = term.getChildren().iterator();
            while (it.hasNext()) {
                PatentTreeNode ptn = it.next();
                if (!checkTermForMembers(ptn, features)) it.remove();
                else hasMembers = true;
            }
        } else if (features.containsKey(term.getName())) {
            PatentTreeNode found = (PatentTreeNode) features.get(term.getName());
            Iterator<PatentTreeNode> i1 = found.getChildren().iterator();
            while (i1.hasNext()) term.addChild(i1.next());
             hasMembers = true;      //return
        }
        return hasMembers;
    }

    public static void main(String args[]) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(null);
        if(returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {
            treeFromTabbedFile(chooser.getSelectedFile());
        }
    }
}
