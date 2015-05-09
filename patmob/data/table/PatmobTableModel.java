package patmob.data.table;

import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.table.AbstractTableModel;
import patmob.data.PatentDocument;
import patmob.data.PatentTreeNode;

public class PatmobTableModel extends AbstractTableModel {
    private String[] colNames;
    private Object[][] objectData;
    
    /**
     * Currently, assume list of projects, which contain patents.
     * @param rootNode 
     */
    public PatmobTableModel(PatentTreeNode rootNode) {
        ArrayList<Object[]> objectList = new ArrayList<>();
        
        Iterator<PatentTreeNode> it = rootNode.getChildren().iterator();
        while (it.hasNext()) {
            PatentTreeNode childNode = it.next();
            extractPatents(childNode, childNode.toString(), objectList);
        }
        Object[][] data = new Object[objectList.size()][];
        int i = 0;
        for (Object[] objectArray: objectList) {
            data[i++] = objectArray;
        }
        // *** COLUMN TITLES ***
        String[] titles = new String[]{
            "Select", 
            "Project", 
            "Publication", 
            "Date", 
            "Title", 
            "Assignee", 
//            "Structure", 
            "Abstract"};
        objectData = data;
        colNames = titles;
    }
    
    private void extractPatents(PatentTreeNode childNode, String projectName, 
            ArrayList<Object[]> prelimData) {
        Iterator<PatentTreeNode> it = childNode.getChildren().iterator();
        while (it.hasNext()) {
            PatentTreeNode node = it.next();
            if (node.getType()==PatentTreeNode.PATENT_DOCUMENT) {
                PatentDocument patent = (PatentDocument) node;
                boolean selected = false;
                if (patent.getHilite()==PatentTreeNode.HILITE_RED) selected=true;
                // *** COLUMN DATA ***
                prelimData.add(new Object[]{
                    selected, 
                    projectName, 
                    patent.getCountry() + patent.getNumber() + " " + patent.getKindCode(),
                    patent.getPublicationDate(),
                    patent.getTitle(),
                    patent.getAssignee(),
//                    "",
                    patent.getAbstract()
                });
            } else {
                extractPatents(node, projectName, prelimData);
            }
        }
    }
    
    @Override
    public int getRowCount() {
//        return cellData.length;
        return objectData.length;
    }

    @Override
    public int getColumnCount() {
        return colNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
//        return cellData[rowIndex][columnIndex];
        return objectData[rowIndex][columnIndex];
    }
    
    @Override
    public String getColumnName(int column) {
        return colNames[column];
    }
    
    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }
    
    @Override
    public boolean isCellEditable(int row, int col) {
        return col==0;
    }
    
    @Override
    public void setValueAt(Object value, int row, int col) {
        objectData[row][col] = value;
        fireTableCellUpdated(row, col);
    }
}
