package patmob.convert;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Iterator;
import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import patmob.data.PatentDocument;
import patmob.data.PatentTreeNode;
import patmob.util.PatmobDesktop;

/**
 * PatMOb collections as FreeMind mind maps.
 * http://freemind.sourceforge.net/wiki/index.php/Main_Page
 */
public class FreeMind {
    
    public static void patmobToFreemind(PatentTreeNode patNode) {
        JFileChooser fc = new JFileChooser();
        int i = fc.showSaveDialog(null);
        if (i==JFileChooser.APPROVE_OPTION) {
            try {
                File mmFile = fc.getSelectedFile();
                FileOutputStream fos = new FileOutputStream(mmFile);
                Document doc = getDocument(patNode);
                PatmobXML.writeDocumentToStream(doc, fos);

                //<?xml version="1.0" encoding="UTF-8"?><map version="0.9.0">
                //FreeMind does not accept XML declaration
                BufferedReader br = 
                        new BufferedReader(new FileReader(mmFile));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line=br.readLine())!=null) {
                    if (line.contains("<map")) {
                        line = line.substring(line.indexOf("<map"));
                    }
                    sb.append(line);
                }
                br.close();
                BufferedWriter bw = new BufferedWriter(new FileWriter(mmFile));
                bw.write(sb.toString());
                bw.close();
            } catch (Exception x) {
                System.out.println("FreeMind.patmobToFreemind: " + x);
            }
        }

        
//        Document doc = getDocument(patNode);
//        PatmobXML.writeDocumentToStream(doc, fos);
        
        //there must be a better way
        
    }

    public static void patmobToFreemind() {
        FileOutputStream fos = PatmobDesktop.getFileOutputStream();
        PatentTreeNode pcn = PatmobXML.collectionForDocument(
                PatmobDesktop.loadDOMFromXMLFile());
        Document doc = getDocument(pcn);
        PatmobXML.writeDocumentToStream(doc, fos);
    }

    public static Document getDocument(PatentTreeNode rootCollection) {
        Document document = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();
            Element map = document.createElement("map");
            map.setAttribute("version", "0.9.0");
            document.appendChild(map);

            Element root = document.createElement("node");
            root.setAttribute("TEXT", rootCollection.toString());
            map.appendChild(root);
            buildDocument (document, rootCollection, root);
        } catch (Exception x) {x.printStackTrace();}
        return document;
    }

    private static void buildDocument(Document doc, PatentTreeNode ptn, Element element) {
        if (ptn.getType()==PatentTreeNode.PATENT_DOCUMENT) {
            PatentDocument patent = (PatentDocument) ptn;
            String patURL = "";
            if (patent.getCountry().equals("US")) {
                if (patent.getNumber().length()<=7)
                    patURL = "http://patft1.uspto.gov/netacgi/"
                            + "nph-Parser?patentnumber=" + PNFormat.getPN(patent, PNFormat.USPTO);//patent.getNumber();
                else patURL = "http://appft1.uspto.gov/netacgi/"
                        + "nph-Parser?Sect1=PTO1&Sect2=HITOFF&d=PG01&p=1&u=/"
                        + "ne&r=1&f=G&l=50&s1=" + patent.getNumber();
            } else if (patent.getCountry().equals("WO"))
                patURL = "http://www.wipo.int/patentscope/search/en/"
                        + "detail.jsf?docId=WO" + patent.getNumber();
            else patURL = "http://worldwide.espacenet.com/publicationDetails/"
                    + "biblio?CC=" + patent.getCountry()
                    + "&NR=" + patent.getNumber();

            element.setAttribute("LINK", patURL);
        }
//        } else {
        if (ptn.size()>0) {
            Iterator<PatentTreeNode> it = ptn.getChildren().iterator();
            while (it.hasNext()) {
                PatentTreeNode patNode = it.next();
                Element el = doc.createElement("node");
                el.setAttribute("TEXT", patNode.toString());
                element.appendChild(el);
                buildDocument(doc, patNode, el);
            }
        }
//        }
    }

    public static void main(String args[]) {
        patmobToFreemind();
    }
}
