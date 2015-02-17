package patmob.util;

import patmob.core.Controller;
import java.util.Collection;
import java.util.Iterator;

/**
 * The window used to search patmob database.
 * @author piotr
 */
public class PatmobFind extends javax.swing.JFrame {
    Controller control;

    public PatmobFind(Controller sc) {
        this();
        control = sc;
    }

    public PatmobFind() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        nameBox = new javax.swing.JComboBox();
        nameField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        noteBox = new javax.swing.JComboBox();
        noteField = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        searchButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(PatmobFind.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        nameBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "starts with", "is" }));
        nameBox.setName("nameBox"); // NOI18N

        nameField.setText(resourceMap.getString("nameField.text")); // NOI18N
        nameField.setName("nameField"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        noteBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "start with", "contain" }));
        noteBox.setName("noteBox"); // NOI18N

        noteField.setText(resourceMap.getString("noteField.text")); // NOI18N
        noteField.setName("noteField"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setName("jTextArea1"); // NOI18N
        jScrollPane1.setViewportView(jTextArea1);

        searchButton.setText(resourceMap.getString("searchButton.text")); // NOI18N
        searchButton.setName("searchButton"); // NOI18N
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(6, 6, 6)
                        .addComponent(nameBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nameField, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(noteBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(noteField, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(163, 163, 163)
                .addComponent(searchButton, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                .addGap(170, 170, 170))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(nameBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(noteField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(noteBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(searchButton)
                .addGap(11, 11, 11)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        String nameQ, noteQ, fullQ, operator, result = "Couldn't get results";
        nameQ = nameField.getText();
        noteQ = noteField.getText();
        if (nameQ.equals("") && noteQ.equals("")) result = "No valid query";
        else {
            if (!nameQ.equals("")) {
                operator = (String) nameBox.getSelectedItem();
                if (operator.equals("is")) nameQ = "NAME = '" + nameQ + "'";
                else nameQ = "NAME like '" + nameQ + "%'";
            }
            if (!noteQ.equals("")) {
                operator = (String) noteBox.getSelectedItem();
                if (operator.equals("contain")) noteQ = "NOTES like '%" + noteQ + "%'";
//                else noteQ = "NAME like '" + nameQ + "%'";
                else noteQ = "NOTES like '" + noteQ + "%'";     // 1/17/2011
            }
            if (!nameQ.equals("") && !noteQ.equals(""))
                fullQ = nameQ + " and " + noteQ;
            else fullQ = nameQ + noteQ;
            Collection<String> results = control.runSQLQuery(fullQ);
            if (results!=null) {
                if (results.isEmpty()) result = fullQ + ":: NO HITS";
                else {
                    result = "";
                    Iterator<String> it = results.iterator();
                    while (it.hasNext()) result += it.next() + "\n";
                }
            }
        }
        jTextArea1.setText(result);
    }//GEN-LAST:event_searchButtonActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PatmobFind().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JComboBox nameBox;
    private javax.swing.JTextField nameField;
    private javax.swing.JComboBox noteBox;
    private javax.swing.JTextField noteField;
    private javax.swing.JButton searchButton;
    // End of variables declaration//GEN-END:variables

}
