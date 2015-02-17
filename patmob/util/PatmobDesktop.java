package patmob.util;

import patmob.convert.PNFormat;
import patmob.data.PatentDocument;
import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JFileChooser;
import org.json.JSONArray;
import org.json.JSONObject;
import patmob.convert.PatmobXML;
import patmob.data.PatentTreeNode;

/**
 * This class provides static methods to write and read system files and
 * to open URLs in the default browser.
 * @author piotr
 */
public class PatmobDesktop {
    public static final int PATOFFICE   = 1,
                            PATBASE     = 2;
    private static String
            USPatentURL = "http://patft1.uspto.gov/netacgi/nph-Parser?patentnumber=",
            USPatAppURL = "http://appft1.uspto.gov/netacgi/nph-Parser?Sect1" +
                          "=PTO1&Sect2=HITOFF&d=PG01&p=1&u=/ne&r=1&f=G&l=50&s1=",
            PatBaseURL  = "http://www.patbase.com/express/default.asp?saction=P-",
            ESPACENETURL1 = "http://v3.espacenet.com/publicationDetails/biblio?CC=",
            ESPACENETURL2 = "&NR=";

    public static void saveNodeToJSONFile(PatentTreeNode collection) {
        JFileChooser fc = new JFileChooser();
        int i = fc.showSaveDialog(null);
        if (i==JFileChooser.APPROVE_OPTION) {
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(
                        fc.getSelectedFile()));
                bw.write(getJSON(collection).toString());
                bw.flush();
                bw.close();
            } catch (Exception x) {System.out.println("PatmobDesktop.saveNodeToJSONFile: " + x);}
        }
    }
    
    private static JSONObject getJSON(PatentTreeNode ptn) {
        JSONObject jTree = new JSONObject()
                .put("name", ptn.toString())
                .put("hilite", Integer.toString(ptn.getHilite()))
                .put("type", Integer.toString(ptn.getType()))
                .put("children", new JSONArray());
        Collection<PatentTreeNode> c = ptn.getSortedChildren();
        if (c!=null) {
            Iterator<PatentTreeNode> it = c.iterator();
            while (it.hasNext()) {
                PatentTreeNode child = it.next();
                jTree.getJSONArray("children").put(getJSON(child));
            }
        }
        
        return jTree;
    }
    
    /**
     * Writes the names of collection, and all levels of its children to
     * an indented text file.
     */
    public static void saveNodeToTextFile(PatentTreeNode collection) {
        JFileChooser fc = new JFileChooser();
        int i = fc.showSaveDialog(null);
        if (i==JFileChooser.APPROVE_OPTION) {
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(
                        fc.getSelectedFile()));
                bw.write(collection.toString() + "\n");
                bw.flush();
                writeChildren(bw, collection, "\t");
                bw.close();
            } catch (Exception x) {System.out.println("PatmobDesktop.saveNodeToTextFile" + x);}
        }
    }
    private static void writeChildren(BufferedWriter bw,
            PatentTreeNode collection, String tabs) {
        Iterator<PatentTreeNode> it = null;
        try {
            Collection<PatentTreeNode> c = collection.getSortedChildren();
            if (c!=null) {
                it = c.iterator();
                while (it.hasNext()) {
                    PatentTreeNode ptn = it.next();
                    bw.write(tabs + ptn.toString() + "\n");
                    writeChildren(bw, ptn, tabs+"\t");
                }
            }
            bw.flush();
        } catch (Exception x) {System.out.println("PatmobDesktop.writeChildren" + x); x.printStackTrace();}
    }

    /**
     * Writes the provided Document to an XML file, using DOMRunner in PatmobXML.
     * The Document in the default patmob format can be obtained from
     * documentForCollection(PatentTreeNode collection) in PatmobXML.
     */
    public static void saveDOMToXMLFile(org.w3c.dom.Document dom) {
        FileOutputStream fos = getFileOutputStream();
        if (fos!=null) PatmobXML.writeDocumentToStream(dom, fos);

//        JFileChooser fc = new JFileChooser();
//        int i = fc.showSaveDialog(null);
//        if (i==JFileChooser.APPROVE_OPTION) {
//            try {
//                FileOutputStream fos = new FileOutputStream(fc.getSelectedFile());
//                PatmobXML.writeDocumentToStream(dom, fos);
//            } catch (Exception x) {System.out.println("DesktopService couldn't save file: " + x);}
//        }
    }

    public static FileOutputStream getFileOutputStream() {
        FileOutputStream fos = null;
        JFileChooser fc = new JFileChooser();
        int i = fc.showSaveDialog(null);
        if (i==JFileChooser.APPROVE_OPTION) {
            try {
                fos = new FileOutputStream(fc.getSelectedFile());
            } catch (Exception x) {System.out.println("DesktopService getFileOutputStream: " + x);}
        }
        return fos;
    }

    /**
     * Returns a Document representing the selected XML file. Documents from the
     * default XML format are converted to PatentTreeNode with
     * collectionForDocument(Document document) in PatmobXML.
     */
    public static org.w3c.dom.Document loadDOMFromXMLFile() {
        org.w3c.dom.Document dom = null;
        JFileChooser fc = new JFileChooser();
        int i = fc.showOpenDialog(null);
        if (i==JFileChooser.APPROVE_OPTION) {
            try {
                FileInputStream fis = new FileInputStream(fc.getSelectedFile());
                dom = PatmobXML.getDocumentFromStream(fis);
            } catch (Exception x) {System.out.println("DesktopService couldn't load file: " + x);}
        }
        return dom;
    }

    /**
     * Opens URL in the default web browser.
     * @param pd
     * @param db - either PATOFFICE or PATBASE
     */
    public static void browsePatent(PatentDocument pd, int db) {
        String urlString = "", pn;
        //4-10-11 pn
        try {
            switch (db) {
                case PATOFFICE:
                    if (pd.getCountry().equals("US")) {
                        pn = PNFormat.getPN(pd, PNFormat.USPTO);
                        if (pd.getNumber().length()==7)
                            urlString = USPatentURL + pn;
                        else urlString = USPatAppURL + pn;
                    } else {
                        pn = PNFormat.getPN(pd, PNFormat.EPO);
                        urlString = ESPACENETURL1 + pd.getCountry() +
                            ESPACENETURL2 + pn;
                    }
                    break;
                case PATBASE:
                    urlString = PatBaseURL + pd.getCountry() + pd.getNumber();
                }
            Desktop.getDesktop().browse(new URL(urlString).toURI());
        } catch (Exception x) {
            System.out.println("PatmobDesktop.browsePatent: " + x);
        }
    }
}
