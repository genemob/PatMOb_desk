package patmob.data.ops.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.HttpClientUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import patmob.core.TreeNodeInfoDisplayer;
import patmob.data.ops.OpsRestClient;
import patmob.data.ops.OpsServiceRequest;
import patmob.data.ops.OpsXPathParser;

/**
 * OPS Family Service.
 * @author Piotr
 */
public class InpadocFamilyRequest extends OpsXPathParser 
implements OpsServiceRequest {
    //without constituents
    String familyURL = "family/publication/docdb/";
    //docdb format
    String patentNumber;
    TreeNodeInfoDisplayer display;
    HttpRequestBase[] requests;
    ArrayList<String> familyMembers;

    /**
     * 
     * @param patentNumber in docdb format
     * @param display 
     */
    public InpadocFamilyRequest (String patentNumber,
            TreeNodeInfoDisplayer display) {
        this.patentNumber = patentNumber;
        this.display = display;
    }
    
    public void submit() {
        requests = new HttpRequestBase[]{new HttpGet(OpsRestClient.OPS_URL + 
                familyURL + patentNumber)};
        OpsRestClient.submitServiceRequest(
                this, OpsRestClient.INPADOC_THROTTLE);
    }

    @Override
    public HttpRequestBase[] getRequests() {
        return requests;
    }

    @Override
    public void handleResponse(HttpResponse response) {
        
        /*
        //*** DEBUG 12/20/2013
        Header header1 = response.getFirstHeader("X-IndividualQuotaPerHour-Used"),
                header2 = response.getFirstHeader("X-RegisteredQuotaPerWeek-Used"),
                header3 = response.getFirstHeader("X-RegisteredPayingQuotaPerWeek-Used"),
                header4 = response.getFirstHeader("X-Rejection-Reason"),
                header5 = response.getFirstHeader("X-Throttling-Control");
        StringBuilder sb = new StringBuilder();
        sb.append("\nX-IndividualQuotaPerHour-Used: ");
        if (header1!=null) sb.append(header1.getValue());
        else sb.append("null");
        sb.append("\nX-RegisteredQuotaPerWeek-Used: ");
        if (header2!=null) sb.append(header2.getValue());
        else sb.append("null");
        sb.append("\nX-RegisteredPayingQuotaPerWeek-Used: ");
        if (header3!=null) sb.append(header3.getValue());
        else sb.append("null");
        sb.append("\nX-Rejection-Reason: ");
        if (header4!=null) sb.append(header4.getValue());
        else sb.append("null");
        sb.append("\nX-Throttling-Control: ");
        if (header5!=null) sb.append(header5.getValue());
        else sb.append("null");
        display.displayText(
                    "INPADOC Family: " + sb.toString());
        */
        
        if (response.getStatusLine().getStatusCode()==200) {
            HttpEntity resultEntity = response.getEntity();
            if (resultEntity!=null) {
                try {
                    InputStream is = resultEntity.getContent();
                    String formatedList = parseFamily(is, patentNumber);
                    display.displayText(formatedList);
                } catch (Exception ex) {ex.printStackTrace();}
            }
        }
        else {
            //response status not OK
            
            /*
            //*** DEBUG 12/19-20/2013
            String entityText = "";
            try {
                entityText = EntityUtils.toString(response.getEntity());
            } catch (Exception x) {
                entityText = x.toString();
            }
            display.displayText(
                    "INPADOC Family [" + patentNumber + "]: "
                    + response.getStatusLine()
                    + "\nPatMObNet: " + entityText);
            */
            
            display.displayText(
                    "INPADOC Family [" + patentNumber + "]: "
                    + response.getStatusLine());
            
            HttpClientUtils.closeQuietly(response);
        }
    }
    
    /**
     * If family too large, it gets truncated (to first 700 members?).
     * <ops:patent-family legal="false" truncatedFamily="true">
     * @param is
     * @param patentNumber
     * @return 
     */
    public String parseFamily(InputStream is, String patentNumber) {
        String familyMemberExpression = "//ops:family-member",
                publicationExpression = "ex:publication-reference",
                applicationExpression = "ex:application-reference",
                priorityExpression = "ex:priority-claim";
        String memberList = "";
        StringBuilder sb;
        familyMembers = new ArrayList();
        setupParser(is);
        XPath xPath = getXPath();
        InputSource inputSource = getInputSource();
        try {
            // Get all family members
            NodeList familyMemberNodes = (NodeList) xPath.evaluate(
                    familyMemberExpression, inputSource, XPathConstants.NODESET);
            for (int i=0; i<familyMemberNodes.getLength(); i++) {
                sb = new StringBuilder();
                Node memberNode = familyMemberNodes.item(i);
                Node pubNode = (Node) xPath.evaluate(publicationExpression, 
                        memberNode, XPathConstants.NODE),
                        appNode = (Node) xPath.evaluate(applicationExpression, 
                        memberNode, XPathConstants.NODE);
                NodeList priorityNodes = (NodeList) xPath.evaluate(
                        priorityExpression, memberNode, XPathConstants.NODESET);
                
                sb.append(parsePublicationNode(pubNode, xPath))
                        .append(parseApplicationNode(appNode, xPath))
                        .append(parsePriorityNodes(priorityNodes, xPath));
                familyMembers.add(sb.toString());
            }
            
        } catch (Exception ex) {ex.printStackTrace();}
        Collections.sort(familyMembers);
        Iterator<String> it = familyMembers.iterator();
        sb = new StringBuilder();
        while (it.hasNext()) {
            sb.append(it.next())
                    .append("\n");
        }
        memberList = sb.toString();
        return memberList;
    }
    
    private String parsePriorityNodes(NodeList priorityNodes, XPath xPath) {
        StringBuilder sb = new StringBuilder("");
        try {
            for (int i=0; i<priorityNodes.getLength(); i++) {
                Node priorityNode = priorityNodes.item(i);
                sb.append(xPath.evaluate("ex:document-id/ex:country", 
                        priorityNode))
                        .append(xPath.evaluate("ex:document-id/ex:doc-number", 
                        priorityNode))
                        .append(" ")
                        .append(xPath.evaluate("ex:document-id/ex:kind", 
                        priorityNode))
                        .append(" ")
                        .append(xPath.evaluate("ex:document-id/ex:date", 
                        priorityNode));
                String activeIndicator = xPath.evaluate(
                        "ex:priority-active-indicator", priorityNode);
                if (activeIndicator.equals("YES")) {
                    sb.append(" [+], ");
                } else {
                    sb.append(" [-], ");
                }
            }
        } catch (Exception ex) {ex.printStackTrace();}
        return sb.toString();
    }
    
    private String parsePublicationNode(Node pubNode, XPath xPath) {
        StringBuilder sb = new StringBuilder("docID not found");
        try {
            NodeList docIDs = (NodeList) xPath.evaluate("ex:document-id", 
                    pubNode, XPathConstants.NODESET);
            for (int i=0; i<docIDs.getLength(); i++) {
                Element docID = (Element) docIDs.item(i);
                String idType = docID.getAttribute("document-id-type");
                if (idType.equals("docdb")) {
                    sb = new StringBuilder();
                    sb.append(xPath.evaluate("ex:country", docID))
                            .append(xPath.evaluate("ex:doc-number", docID))
                            .append(" ")
                            .append(xPath.evaluate("ex:kind", docID))
                            .append(" ")
                            .append(xPath.evaluate("ex:date", docID))
                            .append(", ");
                    return sb.toString();
                }
                else if (idType.equals("epodoc")) {
                    sb = new StringBuilder();
                    sb.append(xPath.evaluate("ex:doc-number", docID))
                            .append(" ")
                            .append(xPath.evaluate("ex:date", docID))
                            .append(", ");
                }
            }
        } catch (Exception ex) {ex.printStackTrace();}
        return sb.toString();
    }
    
    private String parseApplicationNode(Node pubNode, XPath xPath) {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(xPath.evaluate("ex:document-id/ex:country", pubNode))
                    .append(xPath.evaluate("ex:document-id/ex:doc-number", pubNode))
                    .append(" ")
                    .append(xPath.evaluate("ex:document-id/ex:kind", pubNode))
                    .append(" ")
                    .append(xPath.evaluate("ex:document-id/ex:date", pubNode))
                    .append(", ");
        } catch (Exception ex) {ex.printStackTrace();}
        return sb.toString();
    }
}
