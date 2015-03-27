package patmob.data.ops.impl;

import patmob.data.ops.impl.register.RegisterRequestParams;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
import patmob.data.PatentCollectionList;
import patmob.data.PatentDocument;
import patmob.data.PatentTreeNode;
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
    int totalSearchResultsCount = 0;
    boolean notReady = true;

    /**
     * Task is determined by the RegisterRequestParams.
     * If BIBLIO_REQUEST, multiple publication numbers, from a String[] are
     * submitted one by one; data from each response written to a file.
     * If SEARCH_REQUEST, a String query is submitted, and resulting documents
     * (up to 2000) are retrieved 100 at a time.
     * @param searchParams 
     */
    public RegisterRequest(RegisterRequestParams searchParams) {
        params = searchParams;
        resultRows = new ArrayList<>();
        switch (params.getRequestType()) {
            case RegisterRequestParams.BIBLIO_REQUEST:
                createRetrievalRequests();
                setupWriter();
                break;
            case RegisterRequestParams.SEARCH_REQUEST:
                createSearchRequests(1, 25);
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
    
    private void createSearchRequests(int rangeFrom, int rangeTo) {
        HttpPost httpPost = new HttpPost(OpsRestClient.OPS_URL + searchURL);
        httpPost.setHeader("X-OPS-Range", Integer.toString(rangeFrom) +
                "-" + Integer.toString(rangeTo));
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
    
    public RegisterRequestParams submitCall() {
        this.submit();
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<RegisterRequestParams> future;
        future = executor.submit(new Callable<RegisterRequestParams>() {
            @Override
            public RegisterRequestParams call() throws Exception {
                while (notReady) {
                    Thread.sleep(100);
                }
                return params;
            }
        });
        
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException x) {
            return null;
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
                    switch (params.getRequestType()) {
                        case RegisterRequestParams.BIBLIO_REQUEST:
//                            handleBiblioResponse(is);
                            //TODO: currently no notification when finished
                            bw.write(getRegisterData(is));
                            bw.newLine();
                            bw.flush();
                            break;
                        case RegisterRequestParams.SEARCH_REQUEST:
                            parseSearchResults(is);
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
    
    //this did not print "ALL DONE!!!" for a large query
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
    
    /**
     * Parse Register search results and 
     * store patent numbers in RegisterRequestParams.
     * @param is - InputStream from HttpResponse
     * @return 
     */
    private void parseSearchResults(InputStream is) {
        String regSearchExpression = "//ops:register-search",
                rangeExpression = "ops:range";
        int rangeTo;
        setupParser(is);
        InputSource inputSource = getInputSource();
        try {
            Element regSearchElement = (Element) getXPath().evaluate(
                    regSearchExpression, inputSource, XPathConstants.NODE);
            Element rangeElement = (Element) getXPath().evaluate(
                    rangeExpression, regSearchElement, XPathConstants.NODE);
            rangeTo = Integer.parseInt(rangeElement.getAttribute("end"));
            
            //parse the data and put patent numbers in RegisterRequestParams
            PatentTreeNode resultsBatch = getDocuments(regSearchElement);
            if (params.getPatents()==null) {
                // first batch
                totalSearchResultsCount = Integer.parseInt(
                        regSearchElement.getAttribute("total-result-count"));
                resultsBatch.setDescription("Total results: " + 
                        totalSearchResultsCount);
                params.setPatents(resultsBatch);
            } else {
                // append results
                Iterator<PatentTreeNode> it = resultsBatch.getChildren().iterator();
                while (it.hasNext()) {
                    params.getPatents().addChild(it.next());
                }
            }
            
            // re-submit for another batch, if neccessary - or ready to return
            if (params.getResultType()==RegisterRequestParams.ALL_RESULT && 
                    rangeTo<totalSearchResultsCount) {
                int newFrom = rangeTo + 1;
                int newTo = newFrom + 99;
                createSearchRequests(newFrom, newTo);
                submit();
            } else {
                notReady = false;
            }
        } catch (XPathExpressionException | NumberFormatException x) {
            System.out.println("getSearchResults: " + x);
        }
    }
    
    /**
     * Called from parseSearchResults to process the data.
     * @param regSearchElement
     * @return 
     */
    private PatentTreeNode getDocuments(Element regSearchElement) {
        String docIdExpression = "//reg:register-document/reg:bibliographic-data"
                + "/reg:publication-reference/reg:document-id";
        PatentCollectionList documents = new PatentCollectionList("reg search");
        try {
            NodeList docIdNodes = (NodeList) getXPath().evaluate(
                    docIdExpression, regSearchElement, XPathConstants.NODESET);
            for (int i=0; i<docIdNodes.getLength(); i++) {
                Node docIdNode = docIdNodes.item(i);
                PatentDocument doc = new PatentDocument(
                        getXPath().evaluate("reg:country/text()", docIdNode) +
                        getXPath().evaluate("reg:doc-number/text()", docIdNode));
                documents.addChild(doc);
            }
        } catch (Exception x) {
            System.out.println("RegisterRequest.getDocuments: " + x);
        }
        return documents;
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
            System.out.println("getPatentTitle: " + x);
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
            System.out.println("getOpponents: " + x);
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
            System.out.println("getApplicants: " + x);
        }
        if (!uniqueAppliNames.isEmpty()) {
            applicants = uniqueAppliNames.toString();
        }
        return applicants;
    }
}
