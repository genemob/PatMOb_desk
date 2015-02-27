package patmob.data.ops.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import patmob.data.ops.OpsRestClient;
import patmob.data.ops.OpsServiceRequest;
import patmob.data.ops.OpsXPathParser;

/**
 * Check if there are opponents for the given EP publications. If so, write 
 * to a file.
 * @author Piotr
 */
public class RegisterRequest extends OpsXPathParser implements OpsServiceRequest {
    String searchURL1 = "register/publication/epodoc/"; //default biblio format
//            + "EP0921117"
//            searchURL2 = "/biblio,events,procedural-steps";
    HttpRequestBase[] requests;

    public RegisterRequest(String pubNum) {
        HttpGet httpGet = new HttpGet(OpsRestClient.OPS_URL + searchURL1 + pubNum);
        requests = new HttpRequestBase[]{httpGet};
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
                    getOppostionData(is);
//                    BufferedReader br = 
//                            new BufferedReader(new InputStreamReader(is));
//                    String line;
//                    while ((line=br.readLine())!=null) {
//                        System.out.println(line);
//                    }
                } catch (Exception ex) {
                    System.out.println("RegisterRequest: " + ex);
                }
            }
        }
        else {
            //response status not OK
            System.out.println("RegisterRequest: " + response.getStatusLine());
            HttpClientUtils.closeQuietly(response);
        }
    }
    
    private String getOppostionData(InputStream is) {
        String oppoData = "oops",
                oppoDataExpression = "//reg:opposition-data";
        setupParser(is);
        XPath xPath = getXPath();
        InputSource inputSource = getInputSource();
        try {
            NodeList oppositionNodes = (NodeList) xPath.evaluate(
                    oppoDataExpression, inputSource, XPathConstants.NODESET);
            for (int i=0; i<oppositionNodes.getLength(); i++) {
                Node oppoNode = oppositionNodes.item(i);
                System.out.println(oppoNode.getTextContent());
            }
        } catch (Exception x) {
            System.out.println("getOppostionData: " + x);
        }
        return oppoData;
    }
}
