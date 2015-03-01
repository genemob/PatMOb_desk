package patmob.data.ops.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.StringEntity;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import patmob.data.ops.OpsRestClient;
import patmob.data.ops.OpsServiceRequest;
import patmob.data.ops.OpsXPathParser;

/**
 * Sends requests to EPO Register Service and handles responses.
 * @author Piotr
 */
public class RegisterRequest extends OpsXPathParser implements OpsServiceRequest {
    //HTTP POST request, even though bulk retrieval does not seem enabled
    String biblioURL = "register/publication/epodoc/biblio",
            eventsURL = "register/publication/epodoc/events",                   //not used
            proceduralStepsURL = "register/publication/epodoc/procedural-steps",//not used
            fullRetrievalURL = 
            "register/publication/epodoc/biblio,events,procedural-steps",       //not used
            searchURL = "register/search";
    HttpRequestBase[] requests;
    RegisterRequestParams params;

    public RegisterRequest(RegisterRequestParams searchParams) {
        params = searchParams;
        switch (params.getSearchType()) {
            case RegisterRequestParams.BIBLIO_REQUEST:
                createRetrievalRequests();
                break;
            case RegisterRequestParams.SEARCH_REQUEST:
                createSearchRequests();
        }
    }
    
    private void createRetrievalRequests() {
        String[] patentNumbers = params.getPatentNumbers();
        requests = new HttpRequestBase[patentNumbers.length];
        int i = 0;
        for (String patentNumber : patentNumbers) {
            HttpPost httpPost = new HttpPost(OpsRestClient.OPS_URL + biblioURL);
            setRequestQuery(httpPost, patentNumber);
            requests[i++] = httpPost;
        }
    }
    
    private void createSearchRequests() {
        HttpPost httpPost = new HttpPost(OpsRestClient.OPS_URL + searchURL);
        setRequestQuery(httpPost, params.getSearchQuery());
        requests = new HttpRequestBase[]{httpPost};
    }
    
    private void setRequestQuery(HttpPost httpPost, String query) {
        try {
            StringEntity queryEntity = new StringEntity(query);
            httpPost.setEntity(queryEntity);
        } catch (UnsupportedEncodingException ex) {
            System.out.println("RegisterRequest.setRequestQuery: " + ex);
        }
    }
    
    public void submit() {
        OpsRestClient.submitServiceRequest(this, OpsRestClient.OTHER_THROTTLE);
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
                    switch (params.getSearchType()) {
                        case RegisterRequestParams.BIBLIO_REQUEST:
                            getRegisterData(is);
                            break;
                        case RegisterRequestParams.SEARCH_REQUEST:
                            printStream(is);
                    }
                } catch (IOException | IllegalStateException ex) {
                    System.out.println("RegisterRequest: " + ex);
                }
            }
        } else {
            //response status not OK
            System.out.println("RegisterRequest: NULL HttpEntity!");
            HttpClientUtils.closeQuietly(response);
        }
    }
    
    private void printStream(InputStream is) {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        try {
            while ((line=br.readLine())!=null) {
                System.out.println(line);
            }
        } catch (IOException ex) {
            System.out.println("RegisterRequest.printStream: " + ex);
        }
    }
    
    private String getRegisterData(InputStream is) {
        String oppoData = "oops",
                biblioExpression = "//reg:bibliographic-data";
        setupParser(is);
        InputSource inputSource = getInputSource();
        Element biblioElement = null;
        try {
            biblioElement = (Element) getXPath().evaluate(
                    biblioExpression, inputSource, XPathConstants.NODE);
        } catch (Exception x) {
            System.out.println("getOppostionData: " + x);
        }
        if (biblioElement != null) {
            String patentStatus = biblioElement.getAttribute("status");
            String patentNumber = getPatentNumber(biblioElement);
            String patentTitle = getPatentTitle(biblioElement);
            String opponentNames = getOpponents(biblioElement);
            String applicantNames = getApplicants(biblioElement);
            
            System.out.println(patentNumber + "\t" + applicantNames + "\t" + patentTitle + "\t" + 
                    patentStatus + "\t" + opponentNames);
        }
        return oppoData;
    }
    
    private String getPatentNumber(Element biblioElement) {
        String pn = "n/a",
                docIdExpression = "reg:publication-reference/reg:document-id";
        try {
            NodeList docIdNodes = (NodeList) getXPath().evaluate(
                    docIdExpression, biblioElement, XPathConstants.NODESET);
            for (int i=0; i<docIdNodes.getLength(); i++) {
                Node docIdNode = docIdNodes.item(i);
                pn = getXPath().evaluate("reg:country/text()", docIdNode) +
                        getXPath().evaluate("reg:doc-number/text()", docIdNode);
                if (pn.startsWith("EP")) break;
            }
        } catch (Exception x) {
            System.out.println("getPatentNumber: " + x);
        }
        return pn;
    }
    
    private String getPatentTitle(Element biblioElement) {
        String title = "n/a",
                titleExpression = "reg:invention-title";
        try {
            NodeList titleNodes = (NodeList) getXPath().evaluate(
                    titleExpression, biblioElement, XPathConstants.NODESET);
            for (int i=0; i<titleNodes.getLength(); i++) {
                Element docIdNode = (Element) titleNodes.item(i);
                title = docIdNode.getTextContent();
                if ("en".equals(docIdNode.getAttribute("lang"))) break;
            }
        } catch (XPathExpressionException | DOMException x) {
            System.out.println("getPatentNumber: " + x);
        }
        return title;
    }
    
    private String getOpponents(Element biblioElement) {
        String opponents = "n/a",
                opponentExpression = "reg:opposition-data/reg:opponent",
                oppoNameExpression = "reg:addressbook/reg:name/text()";
        HashSet<String> uniqueOppoNames = new HashSet<>();
        
        try {
            NodeList oppoNodes = (NodeList) getXPath().evaluate(
                    opponentExpression, biblioElement, XPathConstants.NODESET);
            for (int i=0; i<oppoNodes.getLength(); i++) {
                Node oppoNode = oppoNodes.item(i);
                String oppoName = getXPath().evaluate(oppoNameExpression, oppoNode);
                uniqueOppoNames.add(oppoName);
            }
        } catch (Exception x) {
            System.out.println("getPatentNumber: " + x);
        }
        if (!uniqueOppoNames.isEmpty()) {
            opponents = uniqueOppoNames.toString();
        }
        return opponents;
    }
    
    private String getApplicants(Element biblioElement) {
        String applicants = "n/a",
                applicantExpression = "reg:parties/reg:applicants/reg:applicant",
                appliNameExpression = "reg:addressbook/reg:name/text()";
        HashSet<String> uniqueAppliNames = new HashSet<>();
        try {
            NodeList appliNodes = (NodeList) getXPath().evaluate(
                    applicantExpression, biblioElement, XPathConstants.NODESET);
            for (int i=0; i<appliNodes.getLength(); i++) {
                Node appliNode = appliNodes.item(i);
                String appliName = getXPath().evaluate(appliNameExpression, appliNode);
                uniqueAppliNames.add(appliName);
            }
        } catch (Exception x) {
            System.out.println("getPatentNumber: " + x);
        }
        if (!uniqueAppliNames.isEmpty()) {
            applicants = uniqueAppliNames.toString();
        }
        return applicants;
    }
}
