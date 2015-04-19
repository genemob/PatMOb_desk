package patmob.core;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import patmob.convert.FreeMind;
import patmob.convert.PatmobXML;
import patmob.data.PatentDocument;
import patmob.data.PatentTreeNode;
import patmob.data.PatmobTree;
import patmob.data.ops.impl.EquivalentsRequest;
import patmob.data.ops.impl.InpadocFamilyRequest;
import patmob.data.ops.impl.LegalRequest;
import patmob.data.table.PatmobTableModel;
import patmob.data.table.TestTable;
import patmob.util.PatmobDesktop;
import patmob.util.PatmobTable;

/**
 *
 * @author Piotr
 */
public class TreeBranchEditor_2 extends javax.swing.JFrame 
implements TreeNodeInfoDisplayer {
    private Controller patmobController;
    private PatmobTree editedTree;
    private PatentTreeNode rootNode, currentPTNode;
    private DefaultMutableTreeNode parentNode;

    public TreeBranchEditor_2() {
        initComponents();
    }

    public static void createNewCollection(Controller sc) {
        PatentTreeNode newNode = 
                PatmobTree.getCollectionFromUser(null, null, true);
        if (newNode!=null) {
            new TreeBranchEditor_2(newNode, sc).setVisible(true);
        }
    }
    
    public TreeBranchEditor_2(PatentTreeNode patentCollection, Controller sc) {
        this();
        this.setTitle("Tree Branch Editor:  " + patentCollection.toString());
        patmobController = sc;
        rootNode = patentCollection;
        resetTree();
        //move to resetTree?
        displayNodeInfo(rootNode);
    }

    public final void resetTree() {
        editedTree = new PatmobTree(rootNode, this,
                patmobController, PatmobTree.TREE_EDITOR);
        jTree1 = editedTree;
        jScrollPane1.setViewportView(jTree1);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        fullCycleButton = new javax.swing.JRadioButton();
        equivalentsButton = new javax.swing.JRadioButton();
        familyButton = new javax.swing.JRadioButton();
        legalButton = new javax.swing.JRadioButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        saveToDbMenuItem = new javax.swing.JMenuItem();
        writeToXmlMenuItem = new javax.swing.JMenuItem();
        writeToTextMenuItem = new javax.swing.JMenuItem();
        writeToJsonMenuItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        freeMindMenuItem = new javax.swing.JMenuItem();
        convertToTableMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jSplitPane1.setDividerLocation(150);

        jScrollPane1.setViewportView(jTree1);

        jSplitPane1.setLeftComponent(jScrollPane1);

        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setWrapStyleWord(true);
        jScrollPane2.setViewportView(jTextArea1);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Patent Info Display"));

        buttonGroup1.add(fullCycleButton);
        fullCycleButton.setSelected(true);
        fullCycleButton.setText("Full-cycle");
        fullCycleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fullCycleButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(equivalentsButton);
        equivalentsButton.setText("Equivalents");
        equivalentsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                equivalentsButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(familyButton);
        familyButton.setText("INPADOC Family");
        familyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                familyButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(legalButton);
        legalButton.setText("Legal Status");
        legalButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                legalButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fullCycleButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(equivalentsButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(familyButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(legalButton)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(fullCycleButton)
                .addComponent(equivalentsButton)
                .addComponent(familyButton)
                .addComponent(legalButton))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(24, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(24, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jSplitPane1.setRightComponent(jPanel1);

        jMenu1.setText("File");

        saveToDbMenuItem.setText("Save to Database...");
        saveToDbMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveToDbMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(saveToDbMenuItem);

        writeToXmlMenuItem.setText("Write to XML File...");
        writeToXmlMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                writeToXmlMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(writeToXmlMenuItem);

        writeToTextMenuItem.setText("Write to Text File...");
        writeToTextMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                writeToTextMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(writeToTextMenuItem);

        writeToJsonMenuItem.setText("Write to JSON File...");
        writeToJsonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                writeToJsonMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(writeToJsonMenuItem);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Tree");

        freeMindMenuItem.setText("Convert to FreeMind Map...");
        freeMindMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                freeMindMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(freeMindMenuItem);

        convertToTableMenuItem.setText("Convert to a Table...");
        convertToTableMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                convertToTableMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(convertToTableMenuItem);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 568, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Get patent equivalents from OPS.
     */
    // <editor-fold defaultstate="collapsed" desc="Patent Info Display: Equivalents">
    private void equivalentsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_equivalentsButtonActionPerformed
        if (currentPTNode.getType()==PatentTreeNode.PATENT_DOCUMENT) {
            jTextArea1.setLineWrap(false);
            PatentDocument doc = (PatentDocument) currentPTNode;
            EquivalentsRequest eqRequest = 
                    new EquivalentsRequest(
                    doc.getCountry() + doc.getNumber(), this);
            eqRequest.submit();
        }
    }//GEN-LAST:event_equivalentsButtonActionPerformed
    //</editor-fold>
    
    /**
     * Redisplay the bibliographic information.
     */
    // <editor-fold defaultstate="collapsed" desc="Patent Info Display: Full-cycle">
    private void fullCycleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fullCycleButtonActionPerformed
        jTextArea1.setLineWrap(true);
        jTextArea1.setText(currentPTNode.getInfo());
    }//GEN-LAST:event_fullCycleButtonActionPerformed
    //</editor-fold>

    /**
     * Get INPADOC family from OPS.
     */
    // <editor-fold defaultstate="collapsed" desc="Patent Info Display: INPADOC Family">
    private void familyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_familyButtonActionPerformed
        if (currentPTNode.getType()==PatentTreeNode.PATENT_DOCUMENT) {
            jTextArea1.setLineWrap(false);
            PatentDocument doc = (PatentDocument) currentPTNode;
            //TODO: Should use PNFormat
            InpadocFamilyRequest famRequest = 
                    new InpadocFamilyRequest(
                    doc.getCountry() + "." +
                    doc.getNumber() + "." +
                    doc.getKindCode(), this);
            famRequest.submit();
        }
    }//GEN-LAST:event_familyButtonActionPerformed
    //</editor-fold>
    
    /**
     * Get legal information from OPS.
     */
    // <editor-fold defaultstate="collapsed" desc="Patent Info Display: Legal Status">
    private void legalButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_legalButtonActionPerformed
        if (currentPTNode.getType()==PatentTreeNode.PATENT_DOCUMENT) {
            jTextArea1.setLineWrap(false);
            PatentDocument doc = (PatentDocument) currentPTNode;
            //TODO: Should use PNFormat
            LegalRequest legalRequest = 
                    new LegalRequest(
                    doc.getCountry() + "." +
                    doc.getNumber() + "." +
                    doc.getKindCode(),
//                    doc.getCountry() + doc.getNumber(),
                    this);
            legalRequest.submit();
        }
    }//GEN-LAST:event_legalButtonActionPerformed
    //</editor-fold>
    
    /**
     * Save this tree to the database.
     */
    // <editor-fold defaultstate="collapsed" desc="File Menu: Save to Database...">
    private void saveToDbMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveToDbMenuItemActionPerformed
        PatentTreeNode patentCollection = editedTree.getPatentCollection();
        SaveToDbDialog sDialog = 
                new SaveToDbDialog(patentCollection, patmobController, this, true);
        sDialog.setVisible(true);
        
        
//        if (JOptionPane.showConfirmDialog(this, "Save " + patentCollection.getName()
//                + "\nas child of " + patentCollection.getParentID())==JOptionPane.OK_OPTION) {
//            if (patmobController.saveTreeNode(patentCollection) > -1) {
//                        dispose();
//                    }
//        }
    }//GEN-LAST:event_saveToDbMenuItemActionPerformed
    //</editor-fold>
    
    /**
     * Write this tree to an XML file.
     */
    // <editor-fold defaultstate="collapsed" desc="File Menu: Write to XML File...">
    private void writeToXmlMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_writeToXmlMenuItemActionPerformed
        PatmobDesktop.saveDOMToXMLFile(
                PatmobXML.documentForCollection(rootNode));
    }//GEN-LAST:event_writeToXmlMenuItemActionPerformed
    //</editor-fold>
    
    /**
     * Write this tree to a text file.
     */
    // <editor-fold defaultstate="collapsed" desc="File Menu: Write to Text File...">
    private void writeToTextMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_writeToTextMenuItemActionPerformed
        PatmobDesktop.saveNodeToTextFile(rootNode);
    }//GEN-LAST:event_writeToTextMenuItemActionPerformed
    //</editor-fold>
    
    /**
     * Write this tree to an XML file in FreeMind format.
     */
    // <editor-fold defaultstate="collapsed" desc="Tree Menu: Convert to FreeMind Map...">
    private void freeMindMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_freeMindMenuItemActionPerformed
        FreeMind.patmobToFreemind(rootNode);
    }//GEN-LAST:event_freeMindMenuItemActionPerformed

    private void writeToJsonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_writeToJsonMenuItemActionPerformed
        PatmobDesktop.saveNodeToJSONFile(rootNode);
    }//GEN-LAST:event_writeToJsonMenuItemActionPerformed

    private void convertToTableMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_convertToTableMenuItemActionPerformed
//        PatmobTable.printAlertTable(rootNode);
//        new PatmobTableModel(rootNode);
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TestTable(rootNode).setVisible(true);
            }
        });
    }//GEN-LAST:event_convertToTableMenuItemActionPerformed
    //</editor-fold>
    

    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JMenuItem convertToTableMenuItem;
    private javax.swing.JRadioButton equivalentsButton;
    private javax.swing.JRadioButton familyButton;
    private javax.swing.JMenuItem freeMindMenuItem;
    private javax.swing.JRadioButton fullCycleButton;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTree jTree1;
    private javax.swing.JRadioButton legalButton;
    private javax.swing.JMenuItem saveToDbMenuItem;
    private javax.swing.JMenuItem writeToJsonMenuItem;
    private javax.swing.JMenuItem writeToTextMenuItem;
    private javax.swing.JMenuItem writeToXmlMenuItem;
    // End of variables declaration//GEN-END:variables

    @Override
    public void displayNodeInfo(PatentTreeNode node) {
        jTextArea1.setLineWrap(true);
        fullCycleButton.setSelected(true);
        currentPTNode = node;
        jTextArea1.setText(currentPTNode.getInfo());
    }

    @Override
    public void displayText(String text) {
        jTextArea1.setText(text);
    }
}
