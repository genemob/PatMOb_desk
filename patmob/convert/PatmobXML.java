package patmob.convert;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import patmob.data.*;

/**
 * Reads and writes Patmob native XML files.
 * @author piotr
 */
public class PatmobXML {

    /**
     * By default, FileOutputStream from PatmobDesktop.
     */
    public static void writeDocumentToStream(Document dom, OutputStream os) {
//        DOMRunner runner = new DOMRunner(dom, os);
//        runner.start();
        // not sure why it was ran in thread
        try {
            DOMSource source = new DOMSource(dom.getDocumentElement());
            StreamResult result = new StreamResult(os);
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.transform(source, result);
            os.close();
        } catch (Exception x) {
            System.out.println("PatmobXML.writeDocumentToStream : " + x);
        }
        
        
    }

    /**
     * By default, FileInputStream from PatmobDesktop.
     */
    public static Document getDocumentFromStream(InputStream is) {
        Document dom = null;
        if (is!=null) {
            try {
                StreamSource source = new StreamSource(is);
                DOMResult result = new DOMResult();
                TransformerFactory tFactory = TransformerFactory.newInstance();
                Transformer transformer = tFactory.newTransformer();
                transformer.transform(source, result);
                is.close();
                dom = (Document) result.getNode();
            } catch (Exception x) {System.out.println("getDOM : " + x);}
        }
        return dom;
    }

    /**
     * Converts PatentTreeNode to a DOM Document in a default patmob format.
     */
    public static Document documentForCollection(PatentTreeNode collection) {
        Document document = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();
            Element root = createCollectionElement(collection, document);
            document.appendChild(root);
        } catch (Exception x) {System.out.println("PatmobXML.documentForCollection:" + x);}
        return document;
    }
    private static Element createCollectionElement(PatentTreeNode collection,
            Document document) {
        Element collectionElement = null, notesElement, childCollectionElement;
        try {
            collectionElement = document.createElement("patent_tree_node");
            collectionElement.setAttribute("name",
                    collection.toString());
            collectionElement.setAttribute("type",
                    Integer.toString(collection.getType()));
            collectionElement.setAttribute("hilite",
                    Integer.toString(collection.getHilite()));
//            notesElement = document.createElement("notes");
            collectionElement.appendChild(                                      //notesElement
                    document.createTextNode(collection.getDescription()));
//            collectionElement.appendChild(notesElement);

            // Child collections
            if (collection.size() > 0) {
                Iterator<PatentTreeNode> it =
                        collection.getSortedChildren().iterator();
                while (it.hasNext()) {
                    childCollectionElement = createCollectionElement(it.next(),
                            document);
                    collectionElement.appendChild(childCollectionElement);
                }
            }
        } catch (Exception x) {System.out.println("PatmobXML.createCollectionElement: " + x);}
        return collectionElement;
    }

    /**
     * Converts a DOM Document in a default patmob format to PatentTreeNode.
     */
    public static PatentTreeNode collectionForDocument(Document document) {
        PatentTreeNode collection = null;
        try {
            Element root = document.getDocumentElement();
            collection = extractCollection(root);
        } catch (Exception x) {System.out.println("PatmobXML.collectionForDocument: " + x);}
        return collection;
    }
    private static PatentTreeNode extractCollection(Element element) {
        PatentTreeNode collection = null;
        try {
            int nodeType = Integer.parseInt(element.getAttribute("type"));
            String nodeName = element.getAttribute("name");
            switch (nodeType) {
                case PatentTreeNode.PATENT_LIST:
                    collection = new PatentCollectionList(nodeName);
                    break;
                case PatentTreeNode.PATENT_MAP:
                    collection = new PatentCollectionMap(nodeName);
                    break;
                case PatentTreeNode.PATENT_DOCUMENT:
                    collection = new PatentDocument(nodeName);
                    break;
                case PatentTreeNode.PATENT_FAMILY:
                    collection = new PatentFamily(nodeName);
                    break;
                case PatentTreeNode.NET_FEATURE:
                    collection = new NetFeature(nodeName);
                    break;
            }
            collection.setHilite(Integer.parseInt(element.getAttribute("hilite")));
            NodeList kids = element.getChildNodes();
            for (int i=0; i<kids.getLength(); i++) {
                if (kids.item(i).getNodeType()==Node.TEXT_NODE)
                    collection.setDescription(kids.item(i).getNodeValue());
                else if (kids.item(i).getNodeType()==Node.ELEMENT_NODE) {
                    Element kid = (Element) kids.item(i);
                    collection.addChild(extractCollection(kid));
                }
            }
        } catch (Exception x) {System.out.println("PatmobXML.extractCollection: " + x);}
        return collection;

    }

    //not needed?
    private static class DOMRunner extends Thread {
        Document dom;
        OutputStream out;
        DOMRunner(Document d, OutputStream o) {
            dom = d;
            out = o;
        }
        @Override
        public void run() {
            if (out!=null) {
                try {
                    DOMSource source = new DOMSource(dom.getDocumentElement());
                    StreamResult result = new StreamResult(out);
                    TransformerFactory tFactory = TransformerFactory.newInstance();
                    Transformer transformer = tFactory.newTransformer();
                    transformer.transform(source, result);
                    out.close();
                } catch (Exception x) {System.out.println("DOMRunner : " + x);}
            }
        }
    }

}
