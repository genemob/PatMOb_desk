package patmob.data;

import java.awt.Frame;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JOptionPane;
//import patmob.data.PatentTreeNode;

/**
 * Package access - only from PatmobTree:
 * 1. To create a new PatmobTreeNode;
 * 2. To edit any existing node.
 */
class PropertiesDialog extends javax.swing.JDialog {
    static PatentTreeNode editedNode = null;

    public PropertiesDialog(Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    public PropertiesDialog(PatentTreeNode ptn, Frame parent, boolean modal) {
        this(parent, modal);
//        initComponents();
        
        editedNode = ptn;
        setGUI();
//        jTextField1.setText(editedNode.toString());
    }
    
    private void setGUI() {
        if (editedNode==null) {
            //creating new node
            jRadioButton2.setSelected(true);
            jTextField1.setText("New Collection");
        } else {
            adjustRadios();
            jTextField1.setText(editedNode.toString());
//            {       //if NOT root
//                jTextField2.setText("<Use Drag and Drop>");
//                jTextField2.setEnabled(false);
//                jButton1.setEnabled(false);
//            }
            
            jTextArea1.setText(editedNode.getDescription());
            
            Collection<PatentTreeNode> docs = editedNode.getSortedChildren();
            if (docs!=null) {
                Iterator<PatentTreeNode> it = docs.iterator();
                while (it.hasNext()) {
                    PatentTreeNode ptn = it.next();
                    if (ptn.getType()==PatentTreeNode.PATENT_DOCUMENT)
                        jTextArea2.append(ptn.toString() + "\n");
                }
            }
        }
    }
    
    private void adjustRadios() {
        jRadioButton1.setEnabled(false);
        jRadioButton2.setEnabled(false);
        jRadioButton3.setEnabled(false);
        jRadioButton4.setEnabled(false);
        jRadioButton5.setEnabled(false);
        switch (editedNode.getType()) {
            case PatentTreeNode.PATENT_DOCUMENT:
                jRadioButton1.setSelected(true);
                break;
            case PatentTreeNode.PATENT_LIST:
                jRadioButton2.setSelected(true);
                break;
            case PatentTreeNode.PATENT_MAP:
                jRadioButton3.setSelected(true);
                break;
            case PatentTreeNode.PATENT_FAMILY:
                jRadioButton4.setSelected(true);
                break;
            case PatentTreeNode.NET_FEATURE:
                jRadioButton5.setSelected(true);
                break;
        }
    }
    
    private boolean updateNode() {
        boolean updated = false;
        if (editedNode==null) {
            //creating new node
            if (jRadioButton1.isSelected()) {
                //PatentDocument
                PatentDocument pd = new PatentDocument();
                editedNode = pd;
            } else if (jRadioButton2.isSelected()) {
                //List
                PatentCollectionList pcl = new PatentCollectionList();
                editedNode = pcl;
            } else if (jRadioButton3.isSelected()) {
                //Map
                PatentCollectionMap pcm = new PatentCollectionMap();
                editedNode = pcm;
            } else if (jRadioButton4.isSelected()) {
                //Family
                PatentFamily pf = new PatentFamily();
                editedNode = pf;
            } else if (jRadioButton5.isSelected()) {
                //Feature
                NetFeature nf = new NetFeature();
                editedNode = nf;
            }
        }
        editedNode.setName(jTextField1.getText());
        editedNode.setDescription(jTextArea1.getText());
        //add documents
        updated = true;
        
        return updated;
    }
    
    public static PatentTreeNode getEditedNode(PatentTreeNode ptn, 
            Frame parent, boolean modal) {
        PropertiesDialog pd = new PropertiesDialog(ptn, parent, modal);
        pd.setVisible(true);
        
        return editedNode;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jRadioButton5 = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Patent Tree Node Properties"); // NOI18N

        jLabel1.setText("Name");
        jLabel1.setName("jLabel1"); // NOI18N

        jTextField1.setText("jTextField1");
        jTextField1.setName("jTextField1"); // NOI18N

        jLabel2.setText("Type");
        jLabel2.setName("jLabel2"); // NOI18N

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setText("Patent Publication");
        jRadioButton1.setName("jRadioButton1"); // NOI18N

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("List");
        jRadioButton2.setName("jRadioButton2"); // NOI18N

        buttonGroup1.add(jRadioButton3);
        jRadioButton3.setText("Map");
        jRadioButton3.setName("jRadioButton3"); // NOI18N

        buttonGroup1.add(jRadioButton4);
        jRadioButton4.setText("Family");
        jRadioButton4.setName("jRadioButton4"); // NOI18N

        buttonGroup1.add(jRadioButton5);
        jRadioButton5.setText("Net Feature");
        jRadioButton5.setName("jRadioButton5"); // NOI18N

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("NOTES"));
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setName("jTextArea1"); // NOI18N
        jScrollPane1.setViewportView(jTextArea1);

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder("DOCUMENTS"));
        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jTextArea2.setColumns(20);
        jTextArea2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jTextArea2.setLineWrap(true);
        jTextArea2.setRows(5);
        jTextArea2.setWrapStyleWord(true);
        jTextArea2.setName("jTextArea2"); // NOI18N
        jScrollPane2.setViewportView(jTextArea2);

        jButton2.setText("OK");
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Cancel");
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jSeparator1.setName("jSeparator1"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jRadioButton1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jRadioButton2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jRadioButton3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jRadioButton4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jRadioButton5))
                                    .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(173, 173, 173)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3)))
                .addContainerGap())
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton2)
                    .addComponent(jRadioButton3)
                    .addComponent(jRadioButton4)
                    .addComponent(jRadioButton5)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //"OK" button
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (updateNode()) dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    //"Cancel" button
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton3ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JRadioButton jRadioButton5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
