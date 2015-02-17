package patmob.core;

import java.awt.Desktop;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import patmob.convert.FreeMind;
import patmob.convert.Ontology;
import patmob.util.PatmobDesktop;
import patmob.data.PatmobTree;
import patmob.convert.PatmobXML;
import patmob.data.PatentCollectionList;
import patmob.data.PatentDocument;
import patmob.data.PatentTreeNode;
import patmob.convert.TreeAnalytics;

/**
 * The window used to display and edit branches of the data tree.
 * @author piotr
 */
public class TreeBranchEditor extends javax.swing.JFrame
        implements TreeNodeInfoDisplayer {

    private Controller patmobController;
    private PatmobTree editedTree;
    private PatentTreeNode rootNode, currentPTNode;
    private DefaultMutableTreeNode parentNode;

    public TreeBranchEditor() {
        initComponents();
    }

    public TreeBranchEditor(PatentTreeNode patentCollection, Controller sc) {
        this();
        this.setTitle("Tree Branch Editor:  " + patentCollection.toString());
        patmobController = sc;
        rootNode = patentCollection;
        resetTree();

        displayNodeInfo(rootNode);
    }

    public static void createNewCollection(Controller sc) {
        PatentTreeNode newNode = 
                PatmobTree.getCollectionFromUser(null, null, true);
        if (newNode!=null)
            new TreeBranchEditor(newNode, sc).setVisible(true);
    }
    
//     private void resetTree(int selRow) {
//          resetTree();
//          jTree1.expandRow(selRow);
//     }
    public void resetTree() {
        editedTree = new PatmobTree(rootNode, this,
                patmobController, PatmobTree.TREE_EDITOR);
        jTree1 = editedTree;
        jScrollPane1.setViewportView(jTree1);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTree2 = new javax.swing.JTree();
        jLabel4 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jRadioButton5 = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        jButton5 = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTextArea3 = new javax.swing.JTextArea();
        jButton7 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItem7 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(TreeBranchEditor.class);
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveToDb_ActionPerformed(evt);
            }
        });

        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jSplitPane1.setName("jSplitPane1"); // NOI18N

        jScrollPane1.setMinimumSize(new java.awt.Dimension(150, 200));
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTree1.setMaximumSize(new java.awt.Dimension(400, 64));
        jTree1.setMinimumSize(new java.awt.Dimension(100, 100));
        jTree1.setName("jTree1"); // NOI18N
        jTree1.setPreferredSize(new java.awt.Dimension(100, 100));
        jScrollPane1.setViewportView(jTree1);

        jSplitPane1.setLeftComponent(jScrollPane1);

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setName("jTextArea1"); // NOI18N
        jScrollPane2.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jTextField1.setText(resourceMap.getString("jTextField1.text")); // NOI18N
        jTextField1.setName("jTextField1"); // NOI18N
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setSelected(true);
        jRadioButton1.setText(resourceMap.getString("jRadioButton1.text")); // NOI18N
        jRadioButton1.setEnabled(false);
        jRadioButton1.setName("jRadioButton1"); // NOI18N

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText(resourceMap.getString("jRadioButton2.text")); // NOI18N
        jRadioButton2.setEnabled(false);
        jRadioButton2.setName("jRadioButton2"); // NOI18N

        buttonGroup1.add(jRadioButton3);
        jRadioButton3.setText(resourceMap.getString("jRadioButton3.text")); // NOI18N
        jRadioButton3.setEnabled(false);
        jRadioButton3.setName("jRadioButton3"); // NOI18N

        buttonGroup1.add(jRadioButton4);
        jRadioButton4.setText(resourceMap.getString("jRadioButton4.text")); // NOI18N
        jRadioButton4.setEnabled(false);
        jRadioButton4.setName("jRadioButton4"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jTree2.setName("jTree2"); // NOI18N
        jTree2.setShowsRootHandles(true);
        jScrollPane3.setViewportView(jTree2);

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jButton3.setText(resourceMap.getString("jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateTree_Prop_ActionPerformed(evt);
            }
        });

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        jTextArea2.setColumns(20);
        jTextArea2.setLineWrap(true);
        jTextArea2.setRows(5);
        jTextArea2.setWrapStyleWord(true);
        jTextArea2.setName("jTextArea2"); // NOI18N
        jScrollPane4.setViewportView(jTextArea2);

        buttonGroup1.add(jRadioButton5);
        jRadioButton5.setText(resourceMap.getString("jRadioButton5.text")); // NOI18N
        jRadioButton5.setEnabled(false);
        jRadioButton5.setName("jRadioButton5"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
                            .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jRadioButton1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jRadioButton2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jRadioButton3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jRadioButton4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jRadioButton5))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(205, 205, 205)
                        .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
                        .addGap(207, 207, 207)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton2)
                    .addComponent(jRadioButton3)
                    .addComponent(jRadioButton4)
                    .addComponent(jRadioButton5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        jPanel3.setName("jPanel3"); // NOI18N

        jButton5.setText(resourceMap.getString("jButton5.text")); // NOI18N
        jButton5.setName("jButton5"); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateTree_Doc_ActionPerformed(evt);
            }
        });

        jScrollPane5.setName("jScrollPane5"); // NOI18N

        jTextArea3.setColumns(20);
        jTextArea3.setRows(5);
        jTextArea3.setName("jTextArea3"); // NOI18N
        jScrollPane5.setViewportView(jTextArea3);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(205, 205, 205)
                        .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
                        .addGap(207, 207, 207)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton5)
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        jSplitPane1.setRightComponent(jTabbedPane1);

        jButton7.setText(resourceMap.getString("jButton7.text")); // NOI18N
        jButton7.setName("jButton7"); // NOI18N
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jMenuBar1.setName("jMenuBar1"); // NOI18N

        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        jMenuItem2.setText(resourceMap.getString("jMenuItem2.text")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuItem3.setText(resourceMap.getString("jMenuItem3.text")); // NOI18N
        jMenuItem3.setName("jMenuItem3"); // NOI18N
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuItem4.setText(resourceMap.getString("jMenuItem4.text")); // NOI18N
        jMenuItem4.setName("jMenuItem4"); // NOI18N
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem4);

        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setEnabled(false);
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        jMenu2.setText(resourceMap.getString("jMenu2.text")); // NOI18N
        jMenu2.setName("jMenu2"); // NOI18N

        jMenuItem5.setText(resourceMap.getString("jMenuItem5.text")); // NOI18N
        jMenuItem5.setName("jMenuItem5"); // NOI18N
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem5);

        jMenuItem6.setText(resourceMap.getString("jMenuItem6.text")); // NOI18N
        jMenuItem6.setName("jMenuItem6"); // NOI18N
        jMenu2.add(jMenuItem6);

        jSeparator1.setName("jSeparator1"); // NOI18N
        jMenu2.add(jSeparator1);

        jMenuItem7.setText(resourceMap.getString("jMenuItem7.text")); // NOI18N
        jMenuItem7.setName("jMenuItem7"); // NOI18N
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem7);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 718, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(187, 187, 187)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 92, Short.MAX_VALUE)
                .addGap(21, 21, 21)
                .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jButton7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(176, 176, 176))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton1)
                    .addComponent(jButton7))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //Properties Tab "Update Tree"  BUTTON
    private void updateTree_Prop_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateTree_Prop_ActionPerformed
        String s = jTextField1.getText();
//        if (s.length()>50) s = s.substring(0, 49);
        currentPTNode.setName(s);

        TreePath newPath = jTree2.getSelectionPath();
        if (newPath!=null) {
            DefaultMutableTreeNode newParentNode = (DefaultMutableTreeNode)
                    newPath.getLastPathComponent();
            PatentTreeNode newParent = (PatentTreeNode)
                    newParentNode.getUserObject();
            newParent.getChildren().add(currentPTNode);
            editedTree.getPatentCollection().setParentID(newParent.getID());
        }
        if (parentNode!=null) {
            PatentTreeNode oldParent = (PatentTreeNode)
                    parentNode.getUserObject();
            oldParent.getChildren().remove(currentPTNode);
        }

        s = jTextArea2.getText();
//        if (s.length()>500) s = s.substring(0, 499);
        currentPTNode.setDescription(s);
//        int selRow = jTree1.getMinSelectionRow();
        resetTree();
    }//GEN-LAST:event_updateTree_Prop_ActionPerformed

    //"Save to DB"  BUTTON
    private void saveToDb_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveToDb_ActionPerformed
        PatentTreeNode patentCollection = editedTree.getPatentCollection();
        if (JOptionPane.showConfirmDialog(this, "Save " + patentCollection.getName()
                + "\nas child of " + patentCollection.getParentID())==JOptionPane.OK_OPTION)
            if (patmobController.saveTreeNode(patentCollection) > -1) dispose();
    }//GEN-LAST:event_saveToDb_ActionPerformed

    //"Write to XML"  BUTTON
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        PatmobDesktop.saveDOMToXMLFile(
                PatmobXML.documentForCollection(rootNode));
    }//GEN-LAST:event_jButton2ActionPerformed

    //Documents tab "Update Tree" BUTTON
    private void updateTree_Doc_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateTree_Doc_ActionPerformed
        currentPTNode.removeChildren();
        StringTokenizer st1 = new StringTokenizer(jTextArea3.getText(), "\n");
        while (st1.hasMoreTokens()) {
            StringTokenizer st2 = new StringTokenizer(st1.nextToken());
            PatentDocument doc = new PatentDocument(st2.nextToken());
            currentPTNode.addChild(doc);
        }
        resetTree();
    }//GEN-LAST:event_updateTree_Doc_ActionPerformed

        //"Write to TXT"  BUTTON
    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        PatmobDesktop.saveNodeToTextFile(rootNode);
    }//GEN-LAST:event_jButton7ActionPerformed

    //Feature Tree Menu Item
    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        new TreeBranchEditor(TreeAnalytics.featureTree(rootNode),patmobController).setVisible(true);
//        TreeAnalytics.featureTree(rootNode);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    /**
     * Change name and notes without resetting tree by hitting return in the field
     * Still need to reset tree to change parents.
     */
private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
    currentPTNode.setName(jTextField1.getText());
    currentPTNode.setDescription(jTextArea2.getText());
    DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)(editedTree.getSelectionPath().getLastPathComponent());
    DefaultTreeModel dtm = (DefaultTreeModel) editedTree.getModel();
    dtm.nodeChanged(dmtn);
}//GEN-LAST:event_jTextField1ActionPerformed

    //Ontology tree menu item
    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        HashMap map = new HashMap();
        Iterator<PatentTreeNode> it = rootNode.getChildren().iterator();
        while (it.hasNext()) {
            PatentTreeNode ptn = it.next();
            map.put(ptn.getName(), ptn);
        }
        
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(null);
        if(returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {
            PatentTreeNode ontology = Ontology.treeFromTabbedFile(chooser.getSelectedFile());
             Ontology.checkTermForMembers(ontology, map);
            
            new TreeBranchEditor(ontology,
                    patmobController).setVisible(true);
        }
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    //FreeMind menu item
    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        FreeMind.patmobToFreemind();
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    //Node 1 Item
    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        jMenuItem5.setText("Node 1: " + currentPTNode.toString());
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    //TODO: !!! Tree Editor Node Menu!
    //Node 1 AND 2 Item
    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        new TreeBranchEditor(TreeAnalytics.getTreeListWithPath(currentPTNode, 
                new PatentCollectionList("Tree_List"), new ArrayList<String>()), 
                patmobController).setVisible(true);
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TreeBranchEditor().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JRadioButton jRadioButton5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextArea jTextArea3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTree jTree1;
    private javax.swing.JTree jTree2;
    // End of variables declaration//GEN-END:variables

    @Override
    public void displayNodeInfo(PatentTreeNode node) {
        currentPTNode = node;

        //INFO tab
        jTextArea1.setText(currentPTNode.getInfo());

        //Properties tab
        jTextField1.setText(node.toString());
        switch (currentPTNode.getType()) {
            case PatentTreeNode.PATENT_LIST:
            case PatentTreeNode.PATENT_MAP:
                jRadioButton1.setSelected(true);
                jTextField1.setEnabled(true);
                jMenuItem1.setEnabled(false);
                break;
            case PatentTreeNode.PATENT_DOCUMENT:
                jRadioButton2.setSelected(true);
                jTextField1.setEnabled(false);
                jMenuItem1.setEnabled(false);
                break;
            case PatentTreeNode.PATENT_FAMILY:
                jRadioButton3.setSelected(true);
                jTextField1.setEnabled(true);
                jMenuItem1.setEnabled(false);
                break;
            case PatentTreeNode.NET_FEATURE:
                jRadioButton5.setSelected(true);
                jTextField1.setEnabled(true);
                jMenuItem1.setEnabled(true);
//                try {
//                    Desktop.getDesktop().browse(
//                            new URL(currentPTNode.getInfo()).toURI());
//                } catch (Exception x) {x.printStackTrace();}
                break;
        }
        PatmobTree parentsTree;
        if (rootNode==node) {
            parentsTree = new PatmobTree(patmobController.getUserContent(0),
                this, patmobController, node, PatmobTree.LIGHTWEIGHT);
//        else parentsTree = new PatmobTree(editedTree.getPatentCollection(),
//                node, PatmobTree.LIGHTWEIGHT);
        
            TreePath path = parentsTree.getSelectionPath();
            if (path!=null)
                parentNode = (DefaultMutableTreeNode) path.getLastPathComponent();

            jTree2 = parentsTree;
            jScrollPane3.setViewportView(jTree2);
            jTree2.scrollPathToVisible(jTree2.getSelectionPath());
        } else jScrollPane3.setViewportView(new JTree());
        jTextArea2.setText(currentPTNode.getDescription());

        //Documents tab
        jTextArea3.setText("");
        Collection<PatentTreeNode> docs = currentPTNode.getSortedChildren();
        if (docs!=null) {
            Iterator<PatentTreeNode> it = docs.iterator();
            while (it.hasNext()) {
                PatentTreeNode ptn = it.next();
                if (ptn.getType()==PatentTreeNode.PATENT_DOCUMENT)
                    jTextArea3.append(ptn.toString() + "\n");
            }
        }
    }

    @Override
    public void displayText(String text) {
        jTextArea1.setText(text);
    }

}
