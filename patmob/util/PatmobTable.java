package patmob.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Iterator;
import javax.swing.JFileChooser;
import patmob.data.PatentCollectionList;
import patmob.data.PatentDocument;
import patmob.data.PatentTreeNode;
import patmob.plugin.patbase.PatBaseAPI;

/**
 * 20141217 This is for creating HTML (XLS) table from an alert tree:
 * Job Title                    <caption>
 *   Project Name               <td>, first in each row under it
 *     PatBase Family           skip
 *       Patent 1               skip
 *       Patent 2, red hilite   <tr> 1
 * @author Piotr
 */
public class PatmobTable {
    static BufferedWriter bw;
    static String projectColor = "#FF9999";
    
//    public static void printAlertTable(PatentTreeNode rootNode) {
//        System.out.println(rootNode);                                               //title
//        if (rootNode.size()>0) {
//            Iterator<PatentTreeNode> iterator = 
//                    rootNode.getChildren().iterator();                              //projects
//            while (iterator.hasNext()) {
//                printProject(iterator.next());
//            }
//        }        
//    }
    
    /**
     * Called from menu in TreeBranchEditor_2. Writes project name to the table
     * caption, creates the header row, then cycles through the project nodes, 
     * sending them to printProject(). Finally finishes off the table and the 
     * HTML document.
     * @param rootNode - The patent tree in the current editor window.
     */
    public static void printAlertTable(PatentTreeNode rootNode) {
        String captionColor = "#FFD700";            //GOLD
        JFileChooser fc = new JFileChooser();
        int i = fc.showSaveDialog(null);
        if (i==JFileChooser.APPROVE_OPTION) {
            try {
                bw = new BufferedWriter(new FileWriter(fc.getSelectedFile()));
                bw.write("<html><body><table>" +
                        "<caption style=\"background-color:" + captionColor + "; "
                        + "padding:10px\">"
                        + rootNode.toString() + "</caption>" +
                        "<tr style=\"background-color:" + captionColor + "\">" +
                        "<th>Project</th>" +
                        "<th>Publication</th>" +
                        "<th>Date</th>" +
                        "<th>Title</th>" +
                        "<th>Assignee</th>" +
                        "<th>Structure</th>" +
                        "<th>Abstract</th>" +
                        "</tr>");
                bw.flush();
            } catch (Exception x) {
                System.out.println("PatmobTable.printAlertTable-1: " + x);
            }
        
            if (rootNode.size()>0) {
                Iterator<PatentTreeNode> iterator = 
                        rootNode.getChildren().iterator();                              //projects
                while (iterator.hasNext()) {
                    printProject(iterator.next());
                }
            }

            try {
                bw.write("</table></body></html>");
                bw.close();
            } catch (Exception x) {
                System.out.println("PatmobTable.printAlertTable-2: " + x);
            }
        }
    }
    
    /**
     * Iterates through families in the project node, sending their publications
     * to printPatent() for adding rows to the table.
     * @param projectNode 
     */
    private static void printProject(PatentTreeNode projectNode) {
        boolean firstRedInProject = true;
        String projectName = projectNode.toString(),
                familyID = "";
        if (projectNode.size()>0) {
            Iterator<PatentTreeNode> iterator = 
                    projectNode.getChildren().iterator();
            while (iterator.hasNext()) {
                PatentTreeNode familyNode = iterator.next();
                String famMemberURL = familyNode.getDescription();
                familyID = famMemberURL.substring(
                        famMemberURL.lastIndexOf("id=") + 3);
                if (familyNode.size()>0) {
                    Iterator<PatentTreeNode> iterator2 = 
                            familyNode.getChildren().iterator();
                    while (iterator2.hasNext()) {
                        PatentTreeNode pat = iterator2.next();
                        if (pat.getHilite()==PatentTreeNode.HILITE_RED) {
                            PatentDocument doc = (PatentDocument) pat;
                            doc.setFamilyNumber(familyID);
                            //alternating colors - ONCE for each project
                            if (firstRedInProject) {
                                if (projectColor.equals("#FF9999")) {
                                    projectColor = "#9999FF";
                                } else {
                                    projectColor = "#FF9999";
                                }
                                firstRedInProject = false;
                            }
                            printPatent(projectName, doc);
                        }
                    }
                }
            }
        }
    }
    //patmob.plugin.patbase.PatBaseAPI.
    /**
     * Writes selected (RED highlight) patent to a table row.
     * @param projectName - Project name for the first column.
     * @param patentNode - Should have the patmob.convert.OPSBiblio object with
     * EPO data.
     */
    private static void printPatent(String projectName, 
            PatentDocument patent) {
//        PatentDocument patent = (PatentDocument) patentNode;
//        if (patent.getHilite()==PatentTreeNode.HILITE_RED) {
        String pn = patent.getCountry() + patent.getNumber(),
                kd = patent.getKindCode(),
                pd = patent.getPublicationDate(),
                tit = "", ass = "", abs = "", imgURL = "";

        if (PatBaseAPI.isInitialized) {
            //PatBaseAPI is currently in plugin project so RISKY IMPORT!!!
//            System.out.println("PATBASE!!!");
            String[] memberData = PatBaseAPI.getMember(
                    patent.getFamilyNumber(), pn+kd);
            if (memberData!=null) {
                tit = memberData[0];
                ass = memberData[1];
                if (memberData[2]!=null) {
                    imgURL = "<img src=\"https://www.patbase.com/getimg/ftimg.asp?id="
                            + memberData[2] + "\" width=200px>";
                }
                abs = memberData[3];
            }
        } else {
            //Get the EPO data from the last publication in full-cycle
            System.out.println(projectName + "\t" +
                    patent.getCountry() + patent.getNumber());
            PatentCollectionList fullCycle = null;
            try {
                fullCycle = patent.getBiblio().getBiblioCollection();
            } catch (NullPointerException np) {
                System.out.println("\t* OPSBiblio not found *");
            }
            if (fullCycle!=null) {
                Iterator<PatentTreeNode> iterator = fullCycle.iterator();
                while (iterator.hasNext()) {
                    PatentDocument pub = (PatentDocument) iterator.next();
                    tit = pub.getTitle();
                    // ass String comes out as "PHARMACYCLICS INCâ€‚[US]",
                    // but ass = pub.getAssignee().replace("â€‚", " ");
                    // does not fix
                    // FIND AND REPLACE IN EXCEL!!!
                    //TODO: fix UTF-8 problem
                    ass = pub.getAssignee();
                    abs = pub.getAbstract();
                    // look for manually saved image in the img directory
                    imgURL = "<img src=\"img/" + pn + ".jpg\" width=200px>";
                }
            }
        }
        
        try {
            bw.write("<tr>" +
                    "<td style=\"background-color:" + projectColor + "\">" +    //project
                    projectName + "</td>" +
                    "<td><a href=\"http://www.patbase.com/express/"             //patent
                    + "default.asp?saction=P-" + pn + "\" target=\"pbView\">" + 
                    pn + " " + kd + "</a></td>" +
                    "<td>" + pd + "</td>" +                                     //date
                    "<td>" + tit + "</td>" +                                    //title
                    "<td>" + ass + "</td>" +                                    //assignee
                    "<td>" + imgURL + "</td>" +   //structure
                    "<td>" + abs + "</td>" +                                    //abstract
                    "</tr>");
            bw.flush();
        } catch(Exception ex) {
            System.out.println("PatmobTable.printPatent: " + ex);
        }
            
//            System.out.println(
//                    projectName + "\t" +
//                    patent.getCountry() + patent.getNumber() + " " + patent.getKindCode() + "\t" +
//                    patent.getPublicationDate());
            
//            PatentCollectionList fullCycle = null;
//            try {
//                fullCycle = patent.getBiblio().getBiblioCollection();
//            } catch (NullPointerException np) {
//                System.out.println("\t" + fullCycle);
//            }
//            if (fullCycle!=null) {
//                Iterator<PatentTreeNode> iterator = fullCycle.iterator();
//                while (iterator.hasNext()) {
//                    System.out.println("\t" + iterator.next());
//                }
//            }
//        }
    }
    
//    private static void printPatent(String projectName, PatentTreeNode patentNode) {
//        PatentDocument patent = (PatentDocument) patentNode;
//        if (patent.getHilite()==PatentTreeNode.HILITE_RED) {
//            System.out.println(
//                    projectName + "\t" +
//                    patent.getCountry() + patent.getNumber() + " " + patent.getKindCode() + "\t" +
//                    patent.getPublicationDate());
//            PatentCollectionList fullCycle = null;
//            try {
//                fullCycle = patent.getBiblio().getBiblioCollection();
//            } catch (NullPointerException np) {
//                System.out.println("\t" + fullCycle);
//            }
//            if (fullCycle!=null) {
//                Iterator<PatentTreeNode> iterator = fullCycle.iterator();
//                while (iterator.hasNext()) {
//                    System.out.println("\t" + iterator.next());
//                }
//            }
//        }
//    }
    
//    public static void printTree(PatentTreeNode node) {
//        System.out.println(node);
//        if (node.size()>0) {
//            Iterator<PatentTreeNode> iterator = 
//                    node.getChildren().iterator();
//            while (iterator.hasNext()) {
//                printTree(iterator.next());
//            }
//        }
//    }
}
