package patmob.data.ops.impl;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.StringEntity;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import patmob.core.Controller;
import patmob.core.TreeBranchEditor_2;
import patmob.core.TreeNodeInfoDisplayer;
import patmob.data.PatentCollectionList;
import patmob.data.PatentCollectionMap;
import patmob.data.PatentDocument;
import patmob.data.PatentFamily;
import patmob.data.PatentTreeNode;
import patmob.data.ops.OpsRestClient;
import patmob.data.ops.OpsServiceRequest;
import patmob.data.ops.OpsXPathParser;

/**
 * Used to submit OPS bibliography search query to OpsRestClient and process 
 * the results. Implements OpsServiceRequest interface methods getRequests()
 * and handleResponse(HttpResponse response), which OpsRestClient calls to
 * get the request(s) and submit the response(s), respectively. Use submit()
 * method to start the client.
 * The handleResponse(HttpResponse response) method uses 
 * setupParser(InputStream is) method and variables inherited from
 * the abstract OpsXPathParser class to parse the XML response. When all
 * processing is done, an instance of this class displays the results in an
 * TreeBranchEditor_2 window.
 * OPS Published-Data Services.
 * @author Piotr
 */
public class BiblioSearchRequest extends OpsXPathParser 
implements OpsServiceRequest {
    public static final int SAMPLE_RESULT = 1,
                            FULL_RESULT   = 2,
            // insted of popping up TreeBranchEditor and updating query panel
            // return PatentTreeNode to display (controller should be null)
                            LIST_RESULT   = 3;
    String searchURL = "published-data/search";

    public BiblioSearchRequest(String query, TreeNodeInfoDisplayer display,
            Controller controller, int searchType) {
        this.query = query;
        try {
            queryEntity = new StringEntity(formatQuery(query));
        } catch (UnsupportedEncodingException ex) {ex.printStackTrace();}
        families = new PatentCollectionMap("All [" + query + "]");
        this.controller = controller;
        this.display = display;
        this.searchType = searchType;
    }

    public void submit() {
        //initial request
        int rangeTo = 25;   //default
        if (searchType==FULL_RESULT) {
            rangeTo = 100;
        }
        requests = new HttpRequestBase[]{prepareRequest(1, rangeTo)};
        OpsRestClient.submitServiceRequest(
                this, OpsRestClient.SEARCH_THROTTLE);
    }

    private HttpPost prepareRequest(int rangeFrom, int rangeTo) {
        HttpPost httpPost = new HttpPost(OpsRestClient.OPS_URL + searchURL);
        httpPost.setEntity(queryEntity);
        httpPost.setHeader("X-OPS-Range", Integer.toString(rangeFrom) +
                "-" + Integer.toString(rangeTo));
        return httpPost;
    }

    @Override
    public HttpRequestBase[] getRequests() {
        return requests;
    }

    @Override
    public void handleResponse(HttpResponse response) {
        if (response.getStatusLine().getStatusCode()==200) {
            switch (searchType) {
                case SAMPLE_RESULT:
                    showSampleResults(response);
                    break;
                case FULL_RESULT:
                    if (firstResponse) {
                        firstResponse = false;
                        appendFamilies(response);

                        //if total count > 100, submit more requests
                        int remainingHits = totalResultCount - 100;
                        if (remainingHits>0) {
                            //there seem to be a 2000 limit for results retrieval
                            if (remainingHits>1900) remainingHits=1900;
                            
                            roundsToGo = remainingHits/100;
                            if (remainingHits%100>0) {
                                roundsToGo+=1;
                            }
                            requests = new HttpRequestBase[roundsToGo];
                            for (int i=0; i<roundsToGo; i++) {
                                HttpPost request = 
                                        prepareRequest(101 + 100*i, 200 + 100*i);
                                requests[i] = request;
                            }
                            OpsRestClient.submitServiceRequest(
                                    this, OpsRestClient.SEARCH_THROTTLE);
                        }
                        else {
                            showAllResults();
                            //and wait for garbage collection...
                        }
                    }
                    else {
                        //there were more than 100 hits, so here they come...
                        appendFamilies(response);
                        if (--roundsToGo==0) {
                            showAllResults();
                            //ALL DONE!
                        }
                    }
                    break;
                case LIST_RESULT:
                    // should NOT be more than 100 publications
                    appendFamilies(response);
                    returnListResult();
            }
        }
        else {
            //response status not OK
            display.displayText(
                    "QUERY [" + query + "]: " + response.getStatusLine());
            HttpClientUtils.closeQuietly(response);
        }
    }

    private void appendFamilies(HttpResponse response) {
        HttpEntity resultEntity = response.getEntity();
        if (resultEntity!=null) {
            try {
                InputStream is = resultEntity.getContent();
//                setupParser(is);
                hits = parseSearchResults(is, query);
                Iterator<PatentTreeNode> it = hits.getChildren().iterator();
                while (it.hasNext()) {
                    PatentTreeNode doc = it.next();
                    String familyName = doc.getDescription();
                    if (families.containsKey(familyName)) {
                        families.get(familyName).addChild(doc);
                    } else {
                        //should get the title of patent for family
                        PatentFamily fam = new PatentFamily(familyName);
                        fam.addChild(doc);
                        families.put(familyName, fam);
                    }
                }
            } catch (Exception ex) {ex.printStackTrace();}
        }
    }
    
    public PatentTreeNode parseSearchResults(InputStream is, 
            String collectionName) {
        String biblioSearchExpression = "//ops:biblio-search",
                patentNodeExpression = "//ops:publication-reference",
                countryEpression = "ex:document-id/ex:country/text()",
                numberEpression = "ex:document-id/ex:doc-number/text()",
                kindEpression = "ex:document-id/ex:kind/text()";
        PatentTreeNode patents = 
                new PatentCollectionList("Sample [" + collectionName + "]");
        setupParser(is);
        XPath xPath = getXPath();
        InputSource inputSource = getInputSource();
        try {
            Element biblioSearchNode = (Element) xPath.evaluate(
                    biblioSearchExpression, inputSource, XPathConstants.NODE);
            totalResultCount = Integer.parseInt(
                    biblioSearchNode.getAttribute("total-result-count"));
            NodeList publicationNodes = (NodeList) xPath.evaluate(
                    patentNodeExpression, biblioSearchNode, XPathConstants.NODESET);
            for (int i=0; i<publicationNodes.getLength(); i++) {
                Element pubElement = (Element) publicationNodes.item(i);
                PatentDocument doc = new PatentDocument(
                        xPath.evaluate(countryEpression, pubElement) +
                        xPath.evaluate(numberEpression, pubElement),
                        xPath.evaluate(kindEpression, pubElement));
                doc.setDescription("Family " + 
                        pubElement.getAttribute("family-id"));
                patents.addChild(doc);
            }
        } catch (Exception ex) {ex.printStackTrace();}
        return patents;
    }

    private void showSampleResults(HttpResponse response) {
        HttpEntity resultEntity = response.getEntity();
        if (resultEntity!=null) {
            try {
                InputStream is = resultEntity.getContent();
//                setupParser(is);
                hits = parseSearchResults(is, query);
            } catch (Exception ex) {ex.printStackTrace();}
        }
        //TODO: Controller should have a method for showing trees!
        new TreeBranchEditor_2(hits, controller).setVisible(true);
        display.displayText(
                "SAMPLE [" + query + "]: " + hits.size() + " hits out of " +
                totalResultCount);
    }

    private void showAllResults() {
        //Controller??
        new TreeBranchEditor_2(families, controller).setVisible(true);
        if (totalResultCount>2000) {
            display.displayText(
                    "ALL [" + query + "]: 2000 (out of " + totalResultCount +
                    ") hits in " + families.size() + " families");
        }
        else {
            display.displayText(
                    "ALL [" + query + "]: " + totalResultCount + 
                    " hits in " + families.size() + " families");
        }
    }
    
    private void returnListResult() {
        display.displayNodeInfo(families);
    }

    private String formatQuery(String query) {
        query = "q=" + query;
        return query;
    }
    
    //non-String variable declarations
    Controller controller;
    TreeNodeInfoDisplayer display;
    HttpRequestBase[] requests;
    String query;
    StringEntity queryEntity;
    PatentCollectionMap families;
    PatentTreeNode hits;
    boolean firstResponse = true;
    int roundsToGo = 0, totalResultCount = 0, searchType;
}
