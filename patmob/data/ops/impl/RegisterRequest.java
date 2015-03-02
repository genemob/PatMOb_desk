package patmob.data.ops.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
    ArrayList<String> resultRows;
    BufferedWriter bw;

    public RegisterRequest(RegisterRequestParams searchParams) {
        params = searchParams;
        resultRows = new ArrayList<>();
        switch (params.getSearchType()) {
            case RegisterRequestParams.BIBLIO_REQUEST:
                createRetrievalRequests();
                setupWriter();
                break;
            case RegisterRequestParams.SEARCH_REQUEST:
                createSearchRequests();
        }
    }
    
    private void setupWriter() {
        String randomFileName = Integer.toString(params.hashCode());
        File file = new File(randomFileName + ".txt");
        try {
            bw = new BufferedWriter(new FileWriter(file));
        } catch (Exception ex) {
            System.out.println("RegisterRequest.setupWriter: " + ex);
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
        
        //TEMP :)
        //<ops:register-search total-result-count="3745">
        //    <ops:query syntax="CQL">pa=sanofi and pn=ep</ops:query>
        //So let's hard-code 38 requests with the max range of 100 each
        
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
//                            handleBiblioResponse(is);
                            bw.write(getRegisterData(is));
                            bw.newLine();
                            bw.flush();
                            break;
                        case RegisterRequestParams.SEARCH_REQUEST:
                            getSearchResults(is);
//                            printStream(is);
                    }
                } catch (IOException | IllegalStateException ex) {
                    System.out.println("RegisterRequest (Exception): " + ex);
                }
            }
        } else {
            //response status not OK
            System.out.println("RegisterRequest (status): " + response.getStatusLine());
            HttpClientUtils.closeQuietly(response);
        }
    }
    
//    private void handleBiblioResponse(InputStream is) {
//        String resultRow = getRegisterData(is);
//        resultRows.add(resultRow);
//        System.out.println(resultRow.substring(0, resultRow.indexOf("\t")));
        //this is not perfect so for now print as we go
//        if (params.getPatentNumbers().length == resultRows.size()) {
//            System.out.println("ALL DONE!!!");
            //write to a file
//String randomFileName = Integer.toString(resultRows.hashCode());
//File file = new File(randomFileName + ".txt");
//try {
//    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
//                    for (String row : resultRows) {
//                        bw.write(row);
//                        bw.newLine();
////                    System.out.println(row);
//                    }
//                }
//            } catch (Exception ex) {
//                System.out.println("RegisterRequest.handleBiblioResponse: " + ex);
//            }
//        }
//    }
    
    private String getSearchResults(InputStream is) {
        String searchRes = "oops",
                docIdExpression = "//reg:register-document/reg:bibliographic-data"
                + "/reg:publication-reference/reg:document-id";
        setupParser(is);
        InputSource inputSource = getInputSource();
        try {
            NodeList docIdNodes = (NodeList) getXPath().evaluate(
                    docIdExpression, inputSource, XPathConstants.NODESET);
            for (int i=0; i<docIdNodes.getLength(); i++) {
                Node docIdNode = docIdNodes.item(i);
                System.out.println(
                        getXPath().evaluate("reg:country/text()", docIdNode) +
                        getXPath().evaluate("reg:doc-number/text()", docIdNode) +
                                " " +
                        getXPath().evaluate("reg:date/text()", docIdNode));
            }
        } catch (Exception x) {
            System.out.println("getSearchResults: " + x);
        }
        
        return searchRes;
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
            System.out.println("getRegisterData: " + x);
        }
        if (biblioElement != null) {
            StringBuilder sb =                                                  //tab-separated values:
                    new StringBuilder(getPatentNumber(biblioElement));          //patentNumber
            sb.append("\t").append(getApplicants(biblioElement))                //applicantNames
                    .append("\t").append(getPatentTitle(biblioElement))         //patentTitle
                    .append("\t").append(biblioElement.getAttribute("status"))  //patentStatus
                    .append("\t").append(getOpponents(biblioElement));          //opponentNames
            oppoData = sb.toString();
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
