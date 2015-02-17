package patmob.data;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import javax.activation.DataHandler;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

class PatmobTreeTransferHandler extends TransferHandler {
    DataFlavor patmobDocFlavor, patmobListFlavor, patmobMapFlavor,
            patmobFamilyFlavor, patmobFeatureFlavor;
    JTree tree;
    DefaultTreeModel model;
    DefaultMutableTreeNode srcNode;
    
    PatmobTreeTransferHandler(JTree jt) {
        tree = jt;
        model = (DefaultTreeModel) tree.getModel();
        makeFlavors();
    }

    private void makeFlavors() {
        try {
            patmobDocFlavor = new DataFlavor(
                    "patmob/pat-doc; class=patmob.data.PatentDocument");
            patmobListFlavor = new DataFlavor(
                    "patmob/pat-list; class=patmob.data.PatentCollectionList");
            patmobMapFlavor = new DataFlavor(
                    "patmob/pat-map; class=patmob.data.PatentCollectionMap");
            patmobFamilyFlavor = new DataFlavor(
                    "patmob/pat-family; class=patmob.data.PatentFamily");
            patmobFeatureFlavor = new DataFlavor(
                    "patmob/pat-feature; class=patmob.data.NetFeature");
        } catch (ClassNotFoundException ex) {}
    }
    
    @Override
    /**
     * EXPORT
     * This method is used to query what actions are supported by the source
     * component, such as COPY, MOVE, or LINK, in any combination.
     */
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    @Override
    /**
     * EXPORT
     * This method bundles up the data to be exported into a Transferable
     * object in preparation for the transfer.
     */
    public Transferable createTransferable(JComponent c) {
        srcNode = (DefaultMutableTreeNode)
                tree.getSelectionPath().getLastPathComponent();
        PatentTreeNode ptn = (PatentTreeNode) srcNode.getUserObject();
        DataHandler dh = null;

        if (PatentTreeNode.PATENT_DOCUMENT==ptn.getType()) {
            PatentDocument transferDoc = (PatentDocument) ptn;
            dh = new DataHandler(transferDoc, "patmob/pat-doc");
        } else if (PatentTreeNode.PATENT_LIST==ptn.getType()) {
            PatentCollectionList transferList = (PatentCollectionList) ptn;
            dh = new DataHandler(transferList, "patmob/pat-list");
        } else if (PatentTreeNode.PATENT_MAP==ptn.getType()) {
            PatentCollectionMap transferMap = (PatentCollectionMap) ptn;
            dh = new DataHandler(transferMap, "patmob/pat-map");
        } else if (PatentTreeNode.PATENT_FAMILY==ptn.getType()) {
            PatentFamily transferFamily = (PatentFamily) ptn;
            dh = new DataHandler(transferFamily, "patmob/pat-family");
        } else if (PatentTreeNode.NET_FEATURE==ptn.getType()) {
            NetFeature transferFeature = (NetFeature) ptn;
            dh = new DataHandler(transferFeature, "patmob/pat-feature");
        }

        // *** NEED TO DISTINGUISH LIST AND MAP! ***

        return dh;
    }

    @Override
    /**
     * EXPORT
     * This method is invoked after the export is complete. When the action
     * is a MOVE, the data needs to be removed from the source after the
     * transfer is complete.
     */
    public void exportDone(JComponent c, Transferable t, int action) {
        if (action == MOVE) {
            DefaultMutableTreeNode srcParent = (DefaultMutableTreeNode) srcNode.getParent();
            PatentTreeNode oldParent = (PatentTreeNode) srcParent.getUserObject(),
                    oldChild = (PatentTreeNode) srcNode.getUserObject();

            Collection<PatentTreeNode> coll = oldParent.getChildren();
            coll.remove(oldChild);

            model.removeNodeFromParent(srcNode);
        }
    }

    @Override
    /**
     * IMPORT
     * This method is called repeatedly during a drag gesture and returns
     * true if the area below the cursor can accept the transfer, or false
     * if the transfer will be rejected.
     * 
     * LOGIC FOR AVAILABLE TRANSFERS???
     * 1. root node cannot be transferred
     * 2. node cannot be transferred onto its child path
     */
    public boolean canImport(TransferSupport supp) {
//        PatentTreeNode target = null;
//        DropLocation dl = supp.getDropLocation();
//        if (dl instanceof JTree.DropLocation) {
        JTree.DropLocation loc = (JTree.DropLocation) supp.getDropLocation();
        TreePath dropPath = loc.getPath();
        if (dropPath!=null) {
            DefaultMutableTreeNode dstNode =
                    (DefaultMutableTreeNode) dropPath.getLastPathComponent();
            TreeNode[] dstPath = dstNode.getPath();
            for (int i=0; i<dstPath.length; i++) {
                if (dstPath[i]==srcNode) return false;   //don't copy into descendant path!
            }
            
//            target = (PatentTreeNode) dstNode.getUserObject();
        } else return false;
//        }
//        //TODO: other flavors
//        if (supp.isDataFlavorSupported(patmobDocFlavor)) {
//            if (target.getType()==PatentTreeNode.PATENT_LIST)
//                return true;
//            else return false;
//        } else if (supp.isDataFlavorSupported(patmobListFlavor)) {
//            if (target.getType()==PatentTreeNode.PATENT_LIST)
//                return true;
//            else return false;
//        }
        
        return true;
    }

    @Override
    /**
     * IMPORT
     * This method is called on a successful drop (or paste) and initiates
     * the transfer of data to the target component. This method returns
     * true if the import was successful and false otherwise.
     * 
     * MAKE CHANGES IN THE UNDERLYING COLLECTIONS???
     * RECONSTRUCT THE TREE BRANCHES OF THE MOVED NODE???
     */
    public boolean importData(TransferSupport supp) {
        DefaultMutableTreeNode parentNode, newNode;
        PatentTreeNode parentPTN, importPTN = null;
        
        if (!canImport(supp)) {
            return false;
        }

        // Fetch the drop location
        JTree.DropLocation loc = (JTree.DropLocation) supp.getDropLocation();
        TreePath path = loc.getPath();
        parentNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        parentPTN = (PatentTreeNode) parentNode.getUserObject();

        // Fetch the Transferable and its data
        Transferable t = supp.getTransferable();
        try {
            if (t.isDataFlavorSupported(patmobDocFlavor))
                importPTN = (PatentTreeNode) t.getTransferData(patmobDocFlavor);
            else if (t.isDataFlavorSupported(patmobListFlavor))
                importPTN = (PatentTreeNode) t.getTransferData(patmobListFlavor);
            else if (t.isDataFlavorSupported(patmobMapFlavor))
                importPTN = (PatentTreeNode) t.getTransferData(patmobMapFlavor);
            else if (t.isDataFlavorSupported(patmobFamilyFlavor))
                importPTN = (PatentTreeNode) t.getTransferData(patmobFamilyFlavor);
            else if (t.isDataFlavorSupported(patmobFeatureFlavor))
                importPTN = (PatentTreeNode) t.getTransferData(patmobFeatureFlavor);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Insert and select the data
        if (importPTN!=null) {
            parentPTN.addChild(importPTN);
            // ***DATABASE IDs***
//            importPTN.setParentID(parentPTN.getID());
            
            newNode = getNodeWithChildren(importPTN);
            model.insertNodeInto(newNode, parentNode, 0);
            tree.makeVisible(path.pathByAddingChild(newNode));
            tree.scrollRectToVisible(tree.getPathBounds(path.pathByAddingChild(newNode)));
            tree.setSelectionPath(path.pathByAddingChild(newNode));
        return true;
        } else return false;
    }
    
    private DefaultMutableTreeNode getNodeWithChildren (PatentTreeNode importPTN) {
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode (importPTN);
        Collection<PatentTreeNode> c = importPTN.getChildren();
        //if c is null (as was from PatentDocument), this method just dies here
        //no NullPointerException
            Iterator<PatentTreeNode> it = c.iterator();
            while (it.hasNext()) {
                PatentTreeNode childPTN = it.next();
                newNode.add(getNodeWithChildren(childPTN));
                System.out.println("children: " + childPTN);
            }
        return newNode;
    }
}