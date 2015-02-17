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
 * OPS Legal Service.
 * @author Piotr
 */
public class LegalRequest extends OpsXPathParser 
implements OpsServiceRequest {
    String legalURL = "legal/publication/docdb/";
//    String legalURL = "legal/publication/epodoc/";

    String patentNumber;
    TreeNodeInfoDisplayer display;
    HttpRequestBase[] requests;
    ArrayList<String> legalEvents;

    public LegalRequest (String patentNumber,
            TreeNodeInfoDisplayer display) {
        this.patentNumber = patentNumber;
        this.display = display;
    }
    
    public void submit() {
        requests = new HttpRequestBase[]{new HttpGet(OpsRestClient.OPS_URL + 
                legalURL + patentNumber)};
        OpsRestClient.submitServiceRequest(
                this, OpsRestClient.INPADOC_THROTTLE);
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
                    String formatedList = parseLegal(is, patentNumber);
                    display.displayText(formatedList);
                } catch (Exception ex) {ex.printStackTrace();}
            }
        }
        else {
            //response status not OK
            display.displayText(
                    "Legal Status [" + patentNumber + "]: " + response.getStatusLine());
            HttpClientUtils.closeQuietly(response);
        }
    }
    
    private String parseLegal(InputStream is, String patentNumber) {
        String legalExpression = "//ops:legal",
                gazetteDateExpression = "ops:L007EP";
//        String legalList = "";
        StringBuilder sb = new StringBuilder("No legal data.");
        legalEvents = new ArrayList();
        setupParser(is);
        XPath xPath = getXPath();
        InputSource inputSource = getInputSource();
        try {
            // Get all family members
            NodeList legalNodes = (NodeList) xPath.evaluate(
                    legalExpression, inputSource, XPathConstants.NODESET);
            for (int i=0; i<legalNodes.getLength(); i++) {
                sb = new StringBuilder();
                Node legalNode = legalNodes.item(i);
                Element legalElement = (Element) legalNode;
                sb.append(xPath.evaluate(gazetteDateExpression, legalNode))
                        .append("\t")
                        .append(legalElement.getAttribute("code"))
                        .append("\t")
                        .append(legalElement.getAttribute("desc"));
                legalEvents.add(sb.toString());
            }
            
        } catch (Exception ex) {ex.printStackTrace();}
//        Collections.sort(legalEvents);
        Iterator<String> it = legalEvents.iterator();
        sb = new StringBuilder();
        while (it.hasNext()) {
            sb.append(it.next())
                    .append("\n");
        }
//        legalList = sb.toString();
        return sb.toString();
    }
}
