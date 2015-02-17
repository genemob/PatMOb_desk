package patmob.convert;
//temp from PatMOb_core patmob.util

import java.util.*;
import patmob.convert.OPSBiblio;
import patmob.convert.PNFormat;
import patmob.convert.PNFormat;
import patmob.data.*;

/**
 *
 * @author piotr
 */
public class TreeAnalytics {
    
    /**
     * Converts a tree in which patents are annotated by addition of various
     * NetFeature children, to a list of all found NetFeatures with their 
     * corresponding patents added as children. Uses getTreeList to flatten the 
     * original tree.
     * @param familyTree - for example, a collection of PatBase families, such 
     * as one returned by Linguamatics.parseCompactResults.
     * @return 
     */
    public static PatentTreeNode featureTree(PatentTreeNode familyTree) {
        PatentCollectionMap fTree = new PatentCollectionMap("Feature_Tree");        
        PatentTreeNode treeList = getTreeList(familyTree, 
                new PatentCollectionList("Tree_List"));
        Collection<PatentTreeNode> coll = treeList.getChildren();
        Iterator<PatentTreeNode> it = coll.iterator();
        while (it.hasNext()) {
            PatentDocument doc = (PatentDocument) it.next();
            Collection<PatentTreeNode> features = doc.getChildren();
            if (features!=null) {
                Iterator<PatentTreeNode> itF = features.iterator();
                while (itF.hasNext()) {
                    PatentTreeNode ptn = itF.next();
                    if (ptn.getType()==PatentTreeNode.NET_FEATURE) {
                        NetFeature feat = (NetFeature) ptn;
                        if (fTree.containsKey(feat.toString()))
                            fTree.get(feat.toString()).addChild(doc);
                        else {
                            PatentCollectionList featColl = new PatentCollectionList(feat.toString());
                            featColl.addChild(doc);
                            fTree.addChild(featColl);
                        }
                    }
                }
            }
        }
        return fTree;
    }
    
    /**
     * Extracts PatentDocuments from a branched tree and adds them as children 
     * to a supplied PatentTreeNode.
     * @param complexTree
     * @param treeList
     * @return treeList with children comprising all the PatentDocuments from complexTree
     */
    public static PatentTreeNode getTreeList(PatentTreeNode complexTree,
            PatentTreeNode treeList) {
        Collection<PatentTreeNode> coll = complexTree.getChildren();
        Iterator<PatentTreeNode> it = coll.iterator();
        while (it.hasNext()) {
            PatentTreeNode ptn = it.next();
            if (ptn.getType()==PatentTreeNode.PATENT_DOCUMENT)
                treeList.addChild(ptn);
            else getTreeList(ptn, treeList);
        }
        return treeList;
    }
    
    /**
     * Fetches bibliographic info (title, assignees, abstract) from EPO OPS
     * for the patent in complexTree, and returns a flat list of patents with
     * biblio, preserving their children (e.g. NetFeatures).
     * @param complexTree
     * @param treeList
     * @return 
     */
    public static PatentTreeNode getTreeListWithBiblio(PatentTreeNode complexTree,
            PatentTreeNode treeList) {
        Collection<PatentTreeNode> coll = complexTree.getChildren();
        Iterator<PatentTreeNode> it = coll.iterator();
        while (it.hasNext()) {
            PatentTreeNode ptn = it.next();
            if (ptn.getType()==PatentTreeNode.PATENT_DOCUMENT) {
                PatentDocument doc = (PatentDocument) ptn;
                String cc = doc.getCountry(),
                       pn = PNFormat.getPN(doc, PNFormat.EPO),
                       kc = doc.getKindCode();
                if (cc.equals("US")) kc = "*";    //inconsistent kind codes -- not needed
                //no separate threads
                OPSBiblio opsBiblio = new OPSBiblio(cc, pn, kc, "*", false);
                opsBiblio.run();
                
                if (opsBiblio.getBiblioCollection().getChildren().size()>0) {
                    PatentTreeNode fullDoc = opsBiblio.getBiblioCollection().get(0);
                    Collection<PatentTreeNode> c = doc.getChildren();
                    Iterator<PatentTreeNode> it2 = c.iterator();
                    while (it2.hasNext()) fullDoc.addChild(it2.next());
                    treeList.addChild(fullDoc);
                } else treeList.addChild(ptn);
            }
            else getTreeListWithBiblio(ptn, treeList);
        }
        return treeList;
    }
        
    //TODO: !!! Patent List with Paths! SEE addDescriptors in Intellixir!!!
    public static PatentTreeNode getTreeListWithPath(PatentTreeNode complexTree,
            PatentTreeNode treeList, ArrayList<String> path) {

        Collection<PatentTreeNode> coll = complexTree.getChildren();
        Iterator<PatentTreeNode> it = coll.iterator();
        while (it.hasNext()) {
            PatentTreeNode ptn = it.next();
System.out.println(ptn.toString());
            if (ptn.getType()==PatentTreeNode.PATENT_DOCUMENT) {
                ptn.removeChildren();   //to be safe
                ptn.addChild(new PatentCollectionList(complexTree.toString()));
                ptn.setDeep(true);
                treeList.addChild(ptn);
            } else {
                getTreeListWithPath(ptn, treeList, path);
            }
        }
        return treeList;
    }
    
//    public static PatentTreeNode getTreeListWithPath(PatentTreeNode complexTree,
//            PatentTreeNode treeList, ArrayList<String> path) { //PatentTreeNode path
//        path.add(complexTree.toString());
//System.out.println(complexTree.toString());
//        Collection<PatentTreeNode> coll = complexTree.getChildren();
//        Iterator<PatentTreeNode> it = coll.iterator();
//        while (it.hasNext()) {
//            PatentTreeNode ptn = it.next();
//            if (ptn.getType()==PatentTreeNode.PATENT_DOCUMENT) {
//                ptn.removeChildren();   //to be safe
////                ptn.addChild(path);
//                PatentTreeNode node = null;
//                listToNode(path, node);
//                ptn.addChild(node);
//                ptn.setDeep(true);
//                treeList.addChild(ptn);
////                path.remove(path.size()-1);
//            } else {
////                path.add(ptn.toString());
//                getTreeListWithPath(ptn, treeList, path);
//            }
////            path.remove(path.size()-1);
//        }
//        return treeList;
//    }
//    
//    //call with null node
//    private static void listToNode(ArrayList<String> list, PatentTreeNode node) {
//        if (list.size()>0) {
//System.out.println("\t" + node);            PatentCollectionList pcl = new PatentCollectionList(list.remove(0));
//            if (node!=null) pcl.addChild(node);
//            listToNode(list, pcl);
//        }
//    }
}
