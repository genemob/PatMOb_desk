package patmob.data.table;

import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.table.AbstractTableModel;
import patmob.data.PatentDocument;
import patmob.data.PatentTreeNode;

/**
 *
 * @author Piotr
 */
public class PatmobTableModel extends AbstractTableModel {
    private String[] colNames;
    private String[][] cellData;
    
    public PatmobTableModel(String[][] data, String[] columnNames) {
        cellData = data;
        colNames = columnNames;
    }
    
    /**
     * Currently, assume list of projects, which contain patents.
     * @param rootNode 
     */
    public PatmobTableModel(PatentTreeNode rootNode) {
        ArrayList<String[]> prelimData = new ArrayList<>();
        Iterator<PatentTreeNode> it = rootNode.getChildren().iterator();
        while (it.hasNext()) {
            PatentTreeNode childNode = it.next();
            extractPatents(childNode, childNode.toString(), prelimData);
        }
        String[][] data = new String[prelimData.size()][];
        int i = 0;
        for (String[] stringArray: prelimData) {
            data[i++] = stringArray;
        }
        String[] titles = new String[]{"Project", "PN", "PD"};
        cellData = data;
        colNames = titles;
        
//        this(data, titles);
//        for (int i=0; i<prelimData.size(); i++) 
//        Iterator<String[]> it2 = prelimData.iterator();
//        while (it2.hasNext()) {
//            String[] proPat = it2.next();
//            System.out.println(proPat[0] + ": " + proPat[1]);
//        }
    }
    
    private void extractPatents(PatentTreeNode childNode, String projectName, 
            ArrayList<String[]> prelimData) {
        Iterator<PatentTreeNode> it = childNode.getChildren().iterator();
        while (it.hasNext()) {
            PatentTreeNode node = it.next();
            if (node.getType()==PatentTreeNode.PATENT_DOCUMENT) {
                PatentDocument patent = (PatentDocument) node;
                prelimData.add(new String[]{projectName, 
                    patent.getCountry() + patent.getNumber() + " " + patent.getKindCode(),
                    patent.getPublicationDate()});
            } else {
                extractPatents(node, projectName, prelimData);
            }
        }
    }
    
    @Override
    public int getRowCount() {
        return cellData.length;
    }

    @Override
    public int getColumnCount() {
        return colNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return cellData[rowIndex][columnIndex];
    }
    
    @Override
    public String getColumnName(int column) {
        return colNames[column];
    }
}
