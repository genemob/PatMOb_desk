package patmob.data;

import patmob.core.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import patmob.convert.PNFormat;
import patmob.data.ops.impl.BulkBiblioRequest;
//import patmob.util.ThreadPoolRunner;
import patmob.util.TreeIconsMap;

/**
 * All the data in Patmob database is organized as a tree. This class is used
 * to display that data (lazy loading!), as well as any new set of data before
 * storing it in the db.
 * @author piotr
 */
public class PatmobTree extends JTree {
    public static final int MAIN_WINDOW = 1,
                            TREE_EDITOR = 2,
                            LIGHTWEIGHT = 3;

    private int treeType;
    private TreeNodeInfoDisplayer patentInfoDisplayer;
    private Controller patmobController;
    private PatentTreeNode rootCollection;

    public PatmobTree (PatentTreeNode collection, TreeNodeInfoDisplayer nid,
            Controller sc, int patmobTreeType) {
        super();
        rootCollection = collection;
        patentInfoDisplayer = nid;
        patmobController = sc;
        treeType = patmobTreeType;
        
        //fetch full-cycle info for patents in the root node
        updatePatentInfo(rootCollection);

        DefaultMutableTreeNode treeNode =
                new DefaultMutableTreeNode(rootCollection);
        addNodes(rootCollection, treeNode);
        setModel(new DefaultTreeModel(treeNode));
        addTreeSelectionListener(new PatmobTreeSelectionListener());
        addMouseListener(new PatmobTreeMouseListener());
        setCellRenderer(new PatmobRenderer());

        //handles OPSBiblio on demand - add loading from DB
        if (patmobTreeType==MAIN_WINDOW) {
            addTreeWillExpandListener(new PatmobTreeWillExpandListener());
        }
        else if (patmobTreeType==TREE_EDITOR) {
            addTreeWillExpandListener(new OPSTreeWillExpandListener());
        }
        //OPSTreeWillExpandListener
        //TransferHandler test 8/20/12
        if (patmobTreeType==TREE_EDITOR) {  //need different handlers for different trees
            setDragEnabled(true);
            setDropMode(DropMode.ON);
            setTransferHandler(new PatmobTreeTransferHandler(this));
        }
    }

    private void addNodes(PatentTreeNode patCollection,
            DefaultMutableTreeNode parentNode) {
        DefaultMutableTreeNode childNode;
        if (patCollection.getSortedChildren()!=null) {
            Iterator it = patCollection.getSortedChildren().iterator();
            while (it.hasNext()) {
                PatentTreeNode pte = (PatentTreeNode) it.next();
                childNode = new DefaultMutableTreeNode(pte);
                parentNode.add(childNode);
                addNodes(pte, childNode);
            }
        }
    }
    
    private class PatmobRenderer extends DefaultTreeCellRenderer {
        
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded,
                    leaf, row, hasFocus);
            DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) value;
            PatentTreeNode ptn = (PatentTreeNode) dmtn.getUserObject();
            
            //flags for docs
            if (ptn.getType()==PatentTreeNode.PATENT_DOCUMENT) {
                PatentDocument doc = (PatentDocument) ptn;
                setIcon(TreeIconsMap.getIcon(doc.getCountry()));
            } else if (ptn.getType()==PatentTreeNode.NET_FEATURE) 
                setIcon(TreeIconsMap.getIcon("Up"));
            
            if (ptn.getHilite()==PatentTreeNode.HILITE_NONE) {
                setFont(new Font("Tahoma", Font.PLAIN, 11));
                setForeground(getForeground());
            } else {
                setFont(new Font(getFont().getFontName(), Font.BOLD,
                        getFont().getSize()));
                switch (ptn.getHilite()) {
                    case PatentTreeNode.HILITE_RED:
                        setForeground(Color.RED);
                        break;
                    case PatentTreeNode.HILITE_GREEN:
                        setForeground(Color.GREEN);
                        break;
                    case PatentTreeNode.HILITE_GOLD:
                        setForeground(Color.ORANGE);
                        break;
                    case PatentTreeNode.HILITE_BLUE:
                        setForeground(Color.BLUE);
                        break;
                }
            }
            
            return this;
        }
    }

    private class PatmobTreeSelectionListener implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent e) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                    getLastSelectedPathComponent();
            if (node!=null) {
                PatentTreeNode ptNode = (PatentTreeNode) node.getUserObject();
                patentInfoDisplayer.displayNodeInfo(ptNode);
            }
        }
    }

    private class PatmobTreeMouseListener implements MouseListener {
        public void mouseClicked(MouseEvent e) {
            int x = e.getX(), y = e.getY();
            TreePath path = getClosestPathForLocation(x,y);
            DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode) path.getLastPathComponent();
            PatentTreeNode ptNode = (PatentTreeNode) node.getUserObject();
            setSelectionPath(path);
            if (e.getButton()==MouseEvent.BUTTON2 ||
                    e.getButton()==MouseEvent.BUTTON3 ||
                    e.getModifiersEx()==InputEvent.CTRL_DOWN_MASK){
//                int x = e.getX(), y = e.getY();
//                TreePath path = getClosestPathForLocation(x,y);
//                DefaultMutableTreeNode node =
//                        (DefaultMutableTreeNode) path.getLastPathComponent();
//                PatentTreeNode ptNode = (PatentTreeNode) node.getUserObject();
//                setSelectionPath(path);
                PopupMenu pop = ptNode.getPatentTreePopup(patentInfoDisplayer);
                switch (treeType()) {
                    case MAIN_WINDOW:
                        if (node!=PatmobTree.this.getModel().getRoot())
                            setMainItems(pop);
                        break;
                    case TREE_EDITOR:
                        setEditorItems(pop);
                        break;
                    case LIGHTWEIGHT:
                        break;
                }
                add(pop);
                pop.show(PatmobTree.this,x,y);
            } else if (e.getModifiersEx()==InputEvent.SHIFT_DOWN_MASK) {
                try {
                    Desktop.getDesktop().browse(
                            new URL(ptNode.getInfo()).toURI());
                } catch (Exception ex) {ex.printStackTrace();}
            }
            //with PatmobTreeSelectionListener disabled, it's EITHER left or right click
//            else {
//                DefaultMutableTreeNode node = (DefaultMutableTreeNode)
//                        getLastSelectedPathComponent();
//                if (node!=null) {
//                    PatentTreeNode ptNode = (PatentTreeNode) node.getUserObject();
//                    patentInfoDisplayer.displayNodeInfo(ptNode);
//                }
//            }
        }
        public void mousePressed(MouseEvent e) {}
        public void mouseReleased(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
    }

    public PatentTreeNode getPatentCollection() {return rootCollection;}
    public int treeType() {return treeType;}

    // =========================================================================
    // POPUP MENU for MAIN window
    // =========================================================================
    private void setMainItems(PopupMenu pop) {
        MenuItem editNodeItem = new MenuItem("Edit Node...");
        MenuItem deleteNodeItem = new MenuItem("Delete Node...");
        editNodeItem.addActionListener(new AbstractAction(){
            public void actionPerformed(ActionEvent e) {
                editSelectedNode();
            }
        });
        pop.add(editNodeItem);
        deleteNodeItem.addActionListener(new AbstractAction(){
            public void actionPerformed(ActionEvent e) {
                deleteNode();
            }
        });
        pop.add(deleteNodeItem);
    }

    private void deleteNode() {
        PatentTreeNode ptNode = getSelectedNode();
        if (ptNode!=null) {
            int i = JOptionPane.showConfirmDialog(this,
                    "Do you want to delete \"" + ptNode + "\"?");
            if (i==JOptionPane.YES_OPTION)
                patmobController.deleteNode(ptNode);
        }
    }

    private void editSelectedNode() {
        PatentTreeNode ptNode = getSelectedNode();
        if (ptNode!=null) {
            patmobController.editNode(ptNode);
        }
    }

    private PatentTreeNode getSelectedNode() {
        PatentTreeNode ptNode = null;
        TreePath currentSelection = getSelectionPath();
        if (currentSelection != null) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
                         (currentSelection.getLastPathComponent());
            ptNode = (PatentTreeNode) currentNode.getUserObject();
        }
        return ptNode;
    }
    
    // =========================================================================
    // POPUP MENU for TREE BRANCH EDITOR window
    // =========================================================================
    private void setEditorItems(PopupMenu pop) {
        MenuItem removeNodeItem = new MenuItem("Remove Node"),
                 addChildItem = new MenuItem("Add Child Node..."),
                 redItem = new MenuItem("RED"),
                 greenItem = new MenuItem("GREEN"),
                 goldItem = new MenuItem("GOLD"),
                 blueItem = new MenuItem("BLUE"),
                 noneItem = new MenuItem("NONE"),
                 propItem = new MenuItem("Properties...");
        Menu hiliteMenu = new Menu("Set Hilite");

        removeNodeItem.addActionListener(new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedNode();
            }
        });
        pop.add(removeNodeItem);
        addChildItem.addActionListener(new AbstractAction(){
            public void actionPerformed(ActionEvent e) {
                addChildNode();
            }
        });
        pop.add(addChildItem);

        AbstractAction abac = new AbstractAction(){
            public void actionPerformed(ActionEvent e) {
                setHilite(e);
            }
        };
        noneItem.addActionListener(abac);
        redItem.addActionListener(abac);
        greenItem.addActionListener(abac);
        goldItem.addActionListener(abac);
        blueItem.addActionListener(abac);
        hiliteMenu.add(noneItem);
        hiliteMenu.add(redItem);
        hiliteMenu.add(greenItem);
        hiliteMenu.add(goldItem);
        hiliteMenu.add(blueItem);
        pop.add(hiliteMenu);
        
        //9/2/2012 PropertiesDialog
        pop.addSeparator();
        propItem.addActionListener(new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                PatentTreeNode editedNode = getCollectionFromUser(
                        getSelectedNode(), null, true);
                System.out.println(editedNode.getName());

            }
        });
        pop.add(propItem);
    }
    
    public static PatentTreeNode getCollectionFromUser(PatentTreeNode ptn,
            Frame parent, boolean modal) {
        return PropertiesDialog.getEditedNode(ptn, parent, modal);
    }

    private void setHilite(ActionEvent e) {
        PatentTreeNode node = getSelectedNode();
        if (node!=null) {
            if (e.getActionCommand().equals("RED"))
                node.setHilite(PatentTreeNode.HILITE_RED);
            else if (e.getActionCommand().equals("GREEN"))
                node.setHilite(PatentTreeNode.HILITE_GREEN);
            else if (e.getActionCommand().equals("GOLD"))
                node.setHilite(PatentTreeNode.HILITE_GOLD);
            else if (e.getActionCommand().equals("BLUE"))
                node.setHilite(PatentTreeNode.HILITE_BLUE);
            else if (e.getActionCommand().equals("NONE"))
                node.setHilite(PatentTreeNode.HILITE_NONE);
        }
    }

    private void addChildNode() {
        TreePath currentSelection = getSelectionPath();
        if (currentSelection != null) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
                         (currentSelection.getLastPathComponent());
            PatentTreeNode parent = (PatentTreeNode) currentNode.getUserObject();
            PatentTreeNode child = getCollectionFromUser(null, null, true);
            if (child!=null) {
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);

                DefaultTreeModel model = (DefaultTreeModel) getModel();
                model.insertNodeInto(childNode, currentNode, 0);
                parent.addChild(child);

                TreePath selectedPath = new TreePath(childNode.getPath());
                setSelectionPath(selectedPath);
                patentInfoDisplayer.displayNodeInfo(child);
            }
        }
    }

    private void removeSelectedNode() {
        TreePath currentSelection = getSelectionPath();
        if (currentSelection != null) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
                         (currentSelection.getLastPathComponent());
            MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
            if (parent != null) {
                PatentTreeNode node = (PatentTreeNode) currentNode.getUserObject();
                removePatentTreeNode(node, rootCollection);

                DefaultTreeModel model = (DefaultTreeModel) getModel();
                model.removeNodeFromParent(currentNode);
            }
        }
    }
    private boolean removePatentTreeNode(PatentTreeNode node, PatentTreeNode parent) {
        boolean removed = false;
        if (parent.size()>0 && parent.getChildren().contains(node)) {
            parent.getChildren().remove(node);
            removed = true;
        }
        else {
            Collection<PatentTreeNode> collection = parent.getChildren();
            if (collection!=null) {
                Iterator<PatentTreeNode> it = collection.iterator();
                while (it.hasNext()) 
                    if (removePatentTreeNode(node, it.next())==true)
                        return true;
            }
        }
        return removed;
    }
    
    private void updatePatentInfo(PatentTreeNode patentCollection) {
        PatentCollectionMap patentChildren = new PatentCollectionMap();
        if (patentCollection.getChildren()!=null) {
            Iterator<PatentTreeNode> it = 
                    patentCollection.getChildren().iterator();
            while (it.hasNext()) {
                PatentTreeNode patentChild = it.next();
                if (patentChild.getType()==PatentTreeNode.PATENT_DOCUMENT) {
                    PatentDocument patentDoc = (PatentDocument) patentChild;
                    patentChildren.put(patentDoc.getCountry() +
                            PNFormat.getPN(patentDoc, PNFormat.EPO), 
                            patentDoc);
                }
            }
            if (patentChildren.size()>0) {
                BulkBiblioRequest bulkRequest = 
                        new BulkBiblioRequest(patentChildren);
                bulkRequest.submit();
            }
        }
    }
    
    //OPS bulk full-cycle retrieval
    private class OPSTreeWillExpandListener implements TreeWillExpandListener {

        @Override
        public void treeWillExpand(TreeExpansionEvent event) 
                throws ExpandVetoException {
//            PatentCollectionMap patentChildren = new PatentCollectionMap();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) 
                    event.getPath().getLastPathComponent();
            PatentTreeNode patentCollection = (PatentTreeNode) 
                    node.getUserObject();
            updatePatentInfo(patentCollection);
        }

        @Override
        public void treeWillCollapse(TreeExpansionEvent event) 
                throws ExpandVetoException {}
        
    }
    
    
    // =========================================================================
    // TreeWillExpandListener
    // =========================================================================
    private class PatmobTreeWillExpandListener implements TreeWillExpandListener {

        /**
         * If the expanding tree nod is not deep, gets the grandchildren.
         * If children are patents, gets their bibliography from EPO OPS.
         */
        public void treeWillExpand(TreeExpansionEvent event)
                throws ExpandVetoException {
//            ThreadPoolRunner runner = patmobController.getThreadPoolRunner();
            //JTree expanding node, its child, and grand-child
            DefaultMutableTreeNode xpDMTNode, xpDMTChild1, xpDMTChild2;
            PatentTreeNode xpSabNode, xpSabChild1,                              //before expansion
                           xpSabNodeDeep, xpSabChild1Deep, xpSabChild2Deep;     //fetched from db
            Iterator<PatentTreeNode> it, deepIt = null, deepIt2 = null;
            int kidNumber = 0;

            xpDMTNode = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
            xpSabNode = (PatentTreeNode) xpDMTNode.getUserObject();
            if (xpSabNode.getSortedChildren()!=null) {
                it = xpSabNode.getSortedChildren().iterator();
                if (!xpSabNode.isDeep()) {
                    xpSabNodeDeep = patmobController.getUserContent(xpSabNode.getID());
                    deepIt = xpSabNodeDeep.getSortedChildren().iterator();
                }
                while (it.hasNext()) {
                    xpSabChild1 = it.next();
//                    if (xpSabChild1.getType()==PatentTreeNode.PATENT_DOCUMENT
//                            && runner!=null) {
//                        PatentDocument pd = (PatentDocument) xpSabChild1;
//                        pd.getBiblio(runner);
//                    }
                    if (!xpSabNode.isDeep()) {
                        xpSabChild1Deep = deepIt.next();
                        if (xpSabChild1Deep.getSortedChildren()!=null) {
                            deepIt2 = xpSabChild1Deep.getSortedChildren().iterator();
                            xpDMTChild1 = (DefaultMutableTreeNode) xpDMTNode.getChildAt(kidNumber++);
                            while (deepIt2.hasNext()) {
                                xpSabChild2Deep = deepIt2.next();
                                xpSabChild1.addChild(xpSabChild2Deep);
                                xpDMTChild2 = new DefaultMutableTreeNode(xpSabChild2Deep);
                                xpDMTChild1.add(xpDMTChild2);
                            }
                        }
                    }
                }
                xpSabNode.setDeep(true);
                if (treeType==MAIN_WINDOW) {
                    updatePatentInfo(xpSabNode);
                }
            }
        }

        public void treeWillCollapse(TreeExpansionEvent event)
                throws ExpandVetoException {}
    }
    
    // =========================================================================
    // Lightweight tree - no listeners, only renderer to remove icons and
    // highlight a desired node.
    // 1-28-13 changed variable names - 
    // from now this JTree used only to diplay the whole db tree
    // =========================================================================

    public PatmobTree (PatentTreeNode dbRootCollection, TreeNodeInfoDisplayer nid,
            Controller pc, final PatentTreeNode nodeToSave, int patmobTreeType) {
        super();
        rootCollection = dbRootCollection;
        treeType = patmobTreeType;

        DefaultMutableTreeNode treeNode =
                new DefaultMutableTreeNode(rootCollection);
        DefaultMutableTreeNode selectedNode =
                addLightNodes(rootCollection, nodeToSave, treeNode);
        setModel(new DefaultTreeModel(treeNode));
        setShowsRootHandles(true);
        getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        setCellRenderer(new LightweightRenderer(selectedNode));
        if (selectedNode!=null) {
            TreePath selectedPath = new TreePath(selectedNode.getPath());
            setSelectionPath(selectedPath);
            nid.displayNodeInfo((PatentTreeNode)selectedNode.getUserObject());
        }
        
        // 1-28-13
        patmobController = pc;
        patentInfoDisplayer = nid;
        addTreeSelectionListener(new TreeSelectionListener(){
            @Override
            public void valueChanged(TreeSelectionEvent tse) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                        getLastSelectedPathComponent();
                if (node!=null) {
                    PatentTreeNode ptNode = (PatentTreeNode) node.getUserObject();
                    patentInfoDisplayer.displayNodeInfo(ptNode);
                    patentInfoDisplayer.displayText("enable OK");
                    
                    //***DISABLE OK BUTTON WHEN CLICK ON/UNDER NODE2SAVE***
                    if (nodeToSave.getID()!=0) {        //new node is not in db
                        Object[] objectsInPath = getSelectionPath().getPath();
                        for (int i=0; i<objectsInPath.length; i++) {
                            DefaultMutableTreeNode dmtn = 
                                    (DefaultMutableTreeNode) objectsInPath[i];
                            PatentTreeNode ptn = (PatentTreeNode) dmtn.getUserObject();
                            if (ptn.getID()==nodeToSave.getID()) {
                                patentInfoDisplayer.displayText("disable OK");
                                break;
                            }
                        }
                    }
                }
            }
        });
        addTreeWillExpandListener(new PatmobTreeWillExpandListener());
    }

    // Builds the JTree nodes and identifies the parent node to be selected.
    // May return null because of lazy loading.
    // Does not remove the node to save, but TreeSelectionListener disables OK button.
    private DefaultMutableTreeNode addLightNodes(PatentTreeNode dbRootCollection,
            PatentTreeNode nodeToSave, DefaultMutableTreeNode dbRootNode) {
        DefaultMutableTreeNode childNode, selectedNode=null;
        if (dbRootCollection.getSortedChildren()!=null) {
            Iterator it = dbRootCollection.getSortedChildren().iterator();
            while (it.hasNext()) {
                PatentTreeNode pte = (PatentTreeNode) it.next();
                if (pte.getID()==nodeToSave.getID()) {
                    selectedNode=dbRootNode;
                    //not adding all nodes causes problems with lazy loading
                    //***DISABLE OK BUTTON WHEN CLICK ON/UNDER NODE2SAVE***
//                    continue;
                }
                childNode = new DefaultMutableTreeNode(pte);
                dbRootNode.add(childNode);

                DefaultMutableTreeNode returnedNode =
                        addLightNodes(pte, nodeToSave, childNode);
                if (returnedNode!=null) selectedNode=returnedNode;
            }
        }
        return selectedNode;
    }

    private class LightweightRenderer extends DefaultTreeCellRenderer {
        private DefaultMutableTreeNode selectedNode;
        boolean notDone = true;

        public LightweightRenderer(DefaultMutableTreeNode node) {
            selectedNode = node;
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {
            //hilite selectedNode only when first initilized
            if (notDone) {
                DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) value;
                if (dmtn==selectedNode) {
                    selected = true;
                    notDone = false;
                }
            }
            setClosedIcon(null);
            setOpenIcon(null);
            setLeafIcon(null);
            super.getTreeCellRendererComponent(tree, value, selected, expanded,
                    leaf, row, hasFocus);
            return this;
        }
    }
}