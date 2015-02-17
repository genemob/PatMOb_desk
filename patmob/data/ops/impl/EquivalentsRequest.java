package patmob.data.ops.impl;

import java.awt.Point;
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
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import patmob.core.TreeNodeInfoDisplayer;
import patmob.data.PatentCollectionList;
import patmob.data.PatentDocument;
import patmob.data.PatentTreeNode;
import patmob.data.ops.OpsRestClient;
import patmob.data.ops.OpsServiceRequest;
import patmob.data.ops.OpsXPathParser;

/**
 * OPS Published-Data Services.
 * @author Piotr
 */
public class EquivalentsRequest extends OpsXPathParser 
implements OpsServiceRequest {
    String equivalentsURL1 = "published-data/publication/epodoc/",
            equivalentsURL2 = "/equivalents/images,biblio,full-cycle";
    
    String patentNumber;
    TreeNodeInfoDisplayer display;
    HttpRequestBase[] requests;
    //parsing products: full-cycle Strings and png image links
    ArrayList<String> inquiryResults, imageLinks;
//    boolean showImg = true;
    boolean showImg = false;    //20141117 NO IMAGES by default - ImageRequest crashes client

    public EquivalentsRequest (String patentNumber,
            TreeNodeInfoDisplayer display) {
        this.patentNumber = patentNumber;
        this.display = display;
    }

    /**
     * Use to suppress display of images. 
     * @param patentNumber
     * @param display
     * @param showImages 
     */
    public EquivalentsRequest (String patentNumber,
            TreeNodeInfoDisplayer display, boolean showImages) {
        this(patentNumber, display);
        showImg = showImages;
    }

    public void submit() {
        requests = new HttpRequestBase[]{new HttpGet(OpsRestClient.OPS_URL + 
                equivalentsURL1 + patentNumber + equivalentsURL2)};
        OpsRestClient.submitServiceRequest(
                this, OpsRestClient.RETRIEVAL_THROTTLE);
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
                    String formatedList = parseEquivalents(is, patentNumber);
                    display.displayText(formatedList);
                    
                    // Show images, if desired
                    if (showImg) {
                        ImageRequest imageRequest = new ImageRequest(
                                imageLinks, ImageRequest.PNG_IMAGE);
                        imageRequest.submit();
                    }
                } catch (Exception ex) {ex.printStackTrace();}
            }
        }
        else {
            //response status not OK
            display.displayText(
                    "Equivalents [" + patentNumber + "]: " + response.getStatusLine());
            HttpClientUtils.closeQuietly(response);
        }
    }
    
    public String parseEquivalents(InputStream is, String patentNumber) {
        String inquiryResultExpression = "//ops:inquiry-result",                //an equivalent
                docInstanceExpression = "ops:document-instance",                //image types
                exchangeDocExpression = 
                "ex:exchange-documents/ex:exchange-document",                   //pub cycle doc
                docFormatExpression = 
                "ops:document-format-options/ops:document-format",              //image formats
                docDateExpression = 
                "ex:bibliographic-data/ex:publication-reference/"
                + "ex:document-id/ex:date";                                     //pub date
        String equivalentList = "";
        StringBuilder sb;
        inquiryResults = new ArrayList();
        imageLinks = new ArrayList();
        setupParser(is);
        XPath xPath = getXPath();
        InputSource inputSource = getInputSource();
        try {
            // Get all equivalents
            NodeList inquiryResultNodes = (NodeList) xPath.evaluate(
                    inquiryResultExpression, inputSource, XPathConstants.NODESET);
            for (int i=0; i<inquiryResultNodes.getLength(); i++) {
                
                // Get all pub-cycle docs of an equivalent
                NodeList exchangeDocumentNodes = (NodeList) xPath.evaluate(
                        exchangeDocExpression, inquiryResultNodes.item(i), 
                        XPathConstants.NODESET);
                sb = new StringBuilder();
                for (int j=0; j<exchangeDocumentNodes.getLength(); j++) {
                    Element xDocEl = (Element) exchangeDocumentNodes.item(j);
                    sb.append(xDocEl.getAttribute("country"))
                            .append(xDocEl.getAttribute("doc-number"))
                            .append("\t")
                            .append(xDocEl.getAttribute("kind"))
                            .append("\t")
                            .append(xPath.evaluate(docDateExpression, xDocEl))
                            .append(";");
                }
                sb.append("-----------------------------------\n");
                inquiryResults.add(sb.toString());
                
                // Get image links of an equivalent
                NodeList documentInstanceNodes = (NodeList) xPath.evaluate(
                        docInstanceExpression, inquiryResultNodes.item(i), 
                        XPathConstants.NODESET);
                for (int k=0; k<documentInstanceNodes.getLength(); k++) {
                    Element docInsEl = (Element) documentInstanceNodes.item(k);
                    String documentInstanceLink = docInsEl.getAttribute("link");
                    NodeList documentFormatNodes = (NodeList) xPath.evaluate(
                            docFormatExpression, docInsEl, XPathConstants.NODESET);
                    for (int m=0; m<documentFormatNodes.getLength(); m++) {
                        String imgType = documentFormatNodes.item(m).getTextContent();
                        if ("image/png".equals(imgType)) {
                            imageLinks.add(documentInstanceLink);
//                            RestClient.showImage(documentInstanceLink);
                        }
                    }
                }
            }

            
        } catch (Exception ex) {ex.printStackTrace();}
        Collections.sort(inquiryResults);
        Iterator<String> it = inquiryResults.iterator();
        sb = new StringBuilder();
        while (it.hasNext()) {
            sb.append(it.next().replace(";", "\n"));
        }
        equivalentList = sb.toString();
        return equivalentList;
    }
}
