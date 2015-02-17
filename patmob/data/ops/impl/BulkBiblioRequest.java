package patmob.data.ops.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.StringEntity;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import patmob.convert.OPSBiblio;
import patmob.data.PatentCollectionList;
import patmob.data.PatentCollectionMap;
import patmob.data.PatentDocument;
import patmob.data.PatentTreeNode;
import patmob.data.ops.OpsRestClient;
import patmob.data.ops.OpsServiceRequest;
import patmob.data.ops.OpsXPathParser;

/**
 * Fills the PatentDocuments in the submitted PatentCollectionMap with 
 * full-cycle info, by POSTing list of publication numbers to OPS.
 * OPS Published-Data Services.
 * @author Piotr
 */
public class BulkBiblioRequest extends OpsXPathParser 
implements OpsServiceRequest {
    String bulkBiblioURL = "published-data/publication/epodoc/full-cycle";
    //parsing XML
    String exchangeDocExpression = "//ex:exchange-document",
            appRefExpression = "ex:bibliographic-data/ex:application-reference",
            dateExpression = "ex:bibliographic-data/ex:publication-reference/ex:document-id/ex:date",
            assigneeExpression = "ex:bibliographic-data/ex:parties/ex:applicants/ex:applicant/ex:applicant-name/ex:name",
            titleExpression = "ex:bibliographic-data/ex:invention-title",
            abstractExpression = "ex:abstract";
    PatentCollectionMap patentDocs;
    StringEntity queryEntity;
    HttpRequestBase[] requests;
    
    public BulkBiblioRequest(PatentCollectionMap patentDocs) {
        this.patentDocs = patentDocs;
    }
    
    /**
     * Splits the list in chunks of 100, if needed.
     */
    public void submit() {
        Collection<String> c = patentDocs.keySet();
        List<String> patDocList = new ArrayList<>(c);
        int docNumber = patDocList.size();
        int batchCount = docNumber/100;
        if (docNumber%100>0) {
            batchCount+=1;
        }
        if (batchCount>0) {
            requests = new HttpRequestBase[batchCount];
            for (int i=0; i<batchCount; i++) {
                HttpPost httpPost = 
                        new HttpPost(OpsRestClient.OPS_URL + bulkBiblioURL);
                int subFrom = 0 + 100*i, subTo = 100 + 100*i;
                if (subTo>patDocList.size()) {
                    subTo = patDocList.size();
                }
                String docList = patDocList.subList(subFrom, subTo).toString();
                //Adjacent elements are separated by the characters ", " (comma and space).
                docList = docList.replace(" ", "");
                docList = docList.substring(1, docList.length()-1);
                try {
                    queryEntity = new StringEntity(docList);
                } catch (UnsupportedEncodingException ex) {ex.printStackTrace();}
                httpPost.setEntity(queryEntity);
                requests[i] = httpPost;
            }
            OpsRestClient.submitServiceRequest(
                    this, OpsRestClient.RETRIEVAL_THROTTLE);
            
        }
    }
    
    @Override
    public HttpRequestBase[] getRequests() {
        return requests;
    }

    @Override
    public void handleResponse(HttpResponse response) {
        if (response.getStatusLine().getStatusCode()==200) {
            HttpEntity resultEntity = response.getEntity();
            if (resultEntity!=null) {
                try {
                    InputStream is = resultEntity.getContent();
                    parseBulkResponse(is);
//                    BufferedReader br = 
//                            new BufferedReader(new InputStreamReader(is));
//                    String line;
//                    while ((line=br.readLine())!=null) {
//                        System.out.println(line);
//                    }
                } catch (Exception ex) {ex.printStackTrace();}
            }
        }
        else {
            //response status not OK
            System.out.println(
                    "BulkBiblioRequest: " + response.getStatusLine());
            HttpClientUtils.closeQuietly(response);
        }
    }
    
    private void parseBulkResponse(InputStream is) {
        PatentCollectionMap map = new PatentCollectionMap();
        
        setupParser(is);
        XPath xPath = getXPath();
        InputSource inputSource = getInputSource();
        try {
            NodeList exchangeDocNodes = (NodeList) xPath.evaluate(
                    exchangeDocExpression, inputSource, XPathConstants.NODESET);
            for (int i=0; i<exchangeDocNodes.getLength(); i++) {
                Element exchangeDocElement = (Element) exchangeDocNodes.item(i);
                /*
                 * Use only "found" docs
                 * <exchange-document system="ops.epo.org" country="ES" doc-number="2227430" status="not found"></exchange-document>
                 * <exchange-document system="ops.epo.org" family-id="8160389" country="CA" doc-number="2441582" kind="A1"></exchange-document>
                 */
                if (!exchangeDocElement.hasAttribute("status")) {
                    PatentDocument doc = new PatentDocument(
                            exchangeDocElement.getAttribute("country") +
                            exchangeDocElement.getAttribute("doc-number"), 
                            exchangeDocElement.getAttribute("kind"));
                    doc.setPublicationDate(xPath.evaluate(
                        dateExpression, exchangeDocElement).trim());
                    doc.setAssignee(xPath.evaluate(
                        assigneeExpression, exchangeDocElement).trim());
                    doc.setTitle(getTitleEn(exchangeDocElement, xPath).trim());
                    doc.setAbstract(getAbstractEn(exchangeDocElement, xPath));

                    //documents from the same publication full-cycle are in
                    //different exchange-document nodes, and can be grouped by
                    //application number in application-reference node
                    Element appReference = (Element) xPath.evaluate(
                        appRefExpression, exchangeDocElement, XPathConstants.NODE);
                    String appReferenceID = appReference.getAttribute("doc-id");
                    if (map.containsKey(appReferenceID)) {
                        map.get(appReferenceID).addChild(doc);
                    }
                    else {
                        PatentCollectionList biblioCollection = 
                                new PatentCollectionList();
                        biblioCollection.addChild(doc);
                        map.put(appReferenceID, biblioCollection);
                    }
                }
            }
        } catch (Exception ex) {ex.printStackTrace();}
        Iterator<PatentTreeNode> it = map.values().iterator();
        while (it.hasNext()) {
            PatentCollectionList biblioCollection = 
                    (PatentCollectionList) it.next();
            Iterator<PatentTreeNode> it2 = 
                    biblioCollection.getChildren().iterator();
            while(it2.hasNext()) {
                PatentDocument doc = (PatentDocument) it2.next();
                if (patentDocs.containsKey(doc.getCountry() + doc.getNumber())){
                    ((PatentDocument) 
                            patentDocs.get(doc.getCountry() + doc.getNumber())).
                            setBiblio(new OPSBiblio(biblioCollection));
                }
            }
        }
    }
    
    //Get title in English
    private String getTitleEn(Element exchangeDocElement, XPath xPath) {
        try {
            NodeList tiNodes = (NodeList) xPath.evaluate(
                    titleExpression, exchangeDocElement, 
                    XPathConstants.NODESET);
            if (tiNodes!=null) {
                Element tiNode = null;
                for (int k=0; k<tiNodes.getLength(); k++) {
                    tiNode = (Element) tiNodes.item(k);
                    String tongue = tiNode.getAttribute("lang");
                    if (tongue.equals("en")) {
                        break;
                    }
                }
                if (tiNode!=null) {
                    return tiNode.getTextContent();
                }
            }
        } catch (Exception x) {x.printStackTrace();}
        return "";
    }
    
    //Get abstract in English
    private String getAbstractEn(Element exchangeDocElement, XPath xPath) {
        try {
            NodeList abNodes = (NodeList) xPath.evaluate(
                    abstractExpression, exchangeDocElement, 
                    XPathConstants.NODESET);
            if (abNodes!=null) {
                Element abNode = null;
                for (int k=0; k<abNodes.getLength(); k++) {
                    abNode = (Element) abNodes.item(k);
                    String tongue = abNode.getAttribute("lang");
                    if (tongue.equals("en")) {
                        break;
                    }
                }
                if (abNode!=null) {
                    return parseAbstract(abNode, xPath);
                }
            }
        } catch (Exception x) {x.printStackTrace();}
        return "";
    }
    
    //Combine <p> nodes in abstract
    private String parseAbstract(Node abNode, XPath xPath) {
        StringBuilder sb = new StringBuilder();
        try {
            NodeList paragraphs = (NodeList) xPath.evaluate("ex:p",
                    abNode, XPathConstants.NODESET);
            for (int i=0; i<paragraphs.getLength(); i++) {
                sb.append(paragraphs.item(i).getTextContent())
                        .append(";");
            }
        } catch (Exception ex) {ex.printStackTrace();}
        if (sb.length()>0) {
            return sb.substring(0, sb.length()-1);
        }
        return "";
    }
}