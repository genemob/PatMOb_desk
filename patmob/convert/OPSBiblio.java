package patmob.convert;
//temp from PatMOb_core

import patmob.data.PatentDocument;
import patmob.data.PatentCollectionList;
import java.io.*;
import java.net.*;
import java.util.Iterator;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

/**
 * Get and parse info from EPO OPS.
 * @author piotr
 */
public class OPSBiblio implements Runnable{
    private URL url;
    private InputStream in;
    private Document document;
    private DOMResult result;
    private TransformerFactory tFactory = TransformerFactory.newInstance();
    private String biblioURL1 = "http://ops.epo.org/2.6.2/rest-services/published-data/publication/epodoc/",
            // Feb 29, 2012 switch to OPS version 2.6.2
//            "http://ops.epo.org/rest-services/" +
//            "biblio-retrieval/publication-reference/epodoc/",
            biblioURL2 = "/full-cycle",
//            "/?fullPublicationCycle=",
            bu3="http://ops.epo.org/2.6.2/rest-services/published-data/publication/epodoc/US8119649/full-cycle",
            fullBiblioURL = "";

    private PatentCollectionList biblioCollection = null;
    
    //shortcircuit OPS call from here
    public OPSBiblio(PatentCollectionList bc) {
        biblioCollection = bc;
    }

    public OPSBiblio (String country, String number, String kind,
            String date, boolean fullCycle) {
        biblioCollection = new PatentCollectionList(country + number + 
                "  Biblio:\n====================\n");
//        fullBiblioURL = biblioURL1 + country + "/" + number + "/" + kind +
//                "/" + date + biblioURL2 + fullCycle;
        fullBiblioURL = biblioURL1 + country + number + biblioURL2;
    }

    private void getPatents (NodeList nodeList) {
        for (int i=0; i<nodeList.getLength(); i++) {
            if (!(nodeList.item(i) instanceof  com.sun.org.apache.xerces.internal.dom.TextImpl)) {
                Element element = (Element) nodeList.item(i);
                if (element.getNodeName().equals("exchange-document")) {
                    String country = element.getAttribute("country");
                    String number = element.getAttribute("doc-number");
                    String kind = element.getAttribute("kind");
                    PatentDocument doc = new PatentDocument(country + number, kind);
                    biblioCollection.addChild(getPatentInfo(doc,element.getChildNodes()));
                } else {
                    NodeList nodes = element.getChildNodes();
                    getPatents(nodes);
                }
        }
        }
    }

    private PatentDocument getPatentInfo (PatentDocument doc, NodeList nodes) {
        for (int i=0; i<nodes.getLength(); i++) {
            try {
                Node node = nodes.item(i);
                if (!(node instanceof  com.sun.org.apache.xerces.internal.dom.TextImpl)) {
                    Element element = (Element) node;

    //                System.out.println(element.getNodeName());

                    if (element.getNodeName().equals("abstract") &&
                            element.getAttribute("lang").equals("en")) {
                        doc.setAbstract(element.getTextContent());
                    } else if (element.getNodeName().equals("invention-title") &&
                            element.getAttribute("lang").equals("en")) {
                        doc.setTitle(element.getTextContent());
                    } else if (element.getNodeName().equals("publication-reference") /*&&
                            element.getAttribute("data-format").equals("docdb")*/) {

                        NodeList prefKids = element.getChildNodes();
                        for (int j=0; j<prefKids.getLength(); j++) {
                            if (prefKids.item(j).getNodeName().equals("document-id")) {
                                NodeList grandKids = prefKids.item(j).getChildNodes();
                                for (int k=0; k<grandKids.getLength(); k++) {
                                    if (grandKids.item(k).getNodeName().equals("date")) {
                                        doc.setPublicationDate(grandKids.item(k).getTextContent());
//                                    System.out.println(grandKids.item(k).getNodeName());
                                    }
                                }
                                
//                            System.out.println(prefKids.item(j).getLastChild().getNodeName());
                           
                            }
                        }
//
//                        Node el1 = element.getFirstChild();
//
//                        System.out.println(el1.getNodeName() + " <-------------------------> ");
//
//                        Element el2 = (Element) el1.getLastChild();
//
//                         System.out.println(el2.getNodeName() + " <-------------------------> ");
//
//                        if (el2.getNodeName().equals("date")) {
//                            doc.setPublicationDate(el2.getTextContent());
//                        }
                    } else if (element.getNodeName().equals("applicant") &&
                            element.getAttribute("data-format").equals("original")) {
                        doc.setAssignee(doc.getAssignee() + element.getTextContent().trim() + "; ");    //TRIM 3/1/12
                    }
                    getPatentInfo(doc, element.getChildNodes());
                }
            } catch (ClassCastException x) {
//                x.printStackTrace();      //Node may not be Element
            } catch (Exception y) {
                y.printStackTrace();
            }
        }
        return doc;
    }
    
    public PatentCollectionList getBiblioCollection() {
        return biblioCollection;
    }

    public String getInfo() {
        String biblio = "";                 //biblioCollection.getName() + "\n";
        Iterator it = biblioCollection.getSortedChildren().iterator();
        while (it.hasNext()) {
            PatentDocument doc = (PatentDocument) it.next();
            biblio += "PN: " + doc.getCountry() + doc.getNumber()
                    + " " + doc.getKindCode() + "\n";
            biblio += "PD: " + doc.getPublicationDate() + "\n";
            biblio += "TI: " + doc.getTitle() + "\n";
            biblio += "PA: " + doc.getAssignee() + "\n";
            biblio += "AB: " + doc.getAbstract() + "\n";
            biblio += "--------------------" + "\n";
        }
        return biblio;
    }

    public void run() {
        try {
            url = new URL(fullBiblioURL);
            in = url.openStream();
//            SocketAddress addr = new InetSocketAddress
//                    ("prx-res-vip.net.sanofi-aventis.com", 3129);
//            Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
//            URLConnection uConn = url.openConnection(proxy);
//            in = uConn.getInputStream();
            result = new DOMResult();
            StreamSource source = new StreamSource(in);
            Transformer transformer = tFactory.newTransformer();
            transformer.transform(source, result);
            in.close();
            document = (Document) result.getNode();
            Element root = document.getDocumentElement();
            NodeList nodes1 = root.getChildNodes();
            getPatents(nodes1);
        } catch (Exception x) {
            x.printStackTrace();
            biblioCollection.setName(biblioCollection.getName() + x.toString()
                    + "\n\n" + fullBiblioURL);
        }
    }
}
