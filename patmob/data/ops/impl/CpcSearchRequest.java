package patmob.data.ops.impl;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import patmob.core.TreeNodeInfoDisplayer;
import patmob.data.ops.OpsRestClient;
import patmob.data.ops.OpsServiceRequest;
import patmob.data.ops.OpsXPathParser;

/**
 * OPS Classification Services.
 * @author Piotr
 */
public class CpcSearchRequest  extends OpsXPathParser 
implements OpsServiceRequest {
    String cpcSearchURL = "classification/cpc/search";
    
    TreeNodeInfoDisplayer display;
    HttpRequestBase[] requests;
    String query;
    StringEntity queryEntity;

    public CpcSearchRequest (String query, TreeNodeInfoDisplayer display) {
        this.query = query;
        try {
            queryEntity = new StringEntity(formatQuery(query));
        } catch (UnsupportedEncodingException ex) {ex.printStackTrace();}
        this.display = display;
    }
    
    public void submit() {
        HttpPost httpPost = new HttpPost(OpsRestClient.OPS_URL + cpcSearchURL);
        httpPost.setEntity(queryEntity);
        requests = new HttpRequestBase[]{httpPost};
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
                    String formatedList = parseClassification(is);
                    display.displayText(formatedList);
                } catch (Exception ex) {ex.printStackTrace();}
            }
        }
        else {
            //response status not OK
            display.displayText(
                    "CPC Search [" + query + "]: " + response.getStatusLine());
            HttpClientUtils.closeQuietly(response);
        }
    }
    
    private String parseClassification(InputStream is) {
        StringBuilder sb = new StringBuilder();
        String classExpression = "//ops:classification-statistics",
                titleExpression = "cpc:class-title/cpc:title-part/cpc:text";  
        
        ArrayList mainClasses = new ArrayList();
        setupParser(is);
        XPath xPath = getXPath();
        InputSource inputSource = getInputSource();
        try {
            NodeList classNodes = (NodeList) xPath.evaluate(
                    classExpression, inputSource, XPathConstants.NODESET);
            for (int i=0; i<classNodes.getLength(); i++) {
                sb = new StringBuilder();
                Element classElement = (Element) classNodes.item(i);
                sb.append(classElement.getAttribute("percentage"))
                        .append("\t")
                        .append(classElement.getAttribute(
                        "classification-symbol"))
                        .append("\t")
                        .append(xPath.evaluate(titleExpression, classElement).trim());
                mainClasses.add(sb.toString());
            }
        } catch (Exception ex) {ex.printStackTrace();}
        
        Collections.sort(mainClasses, new Comparator(){
            @Override
            public int compare(Object o1, Object o2) {
                int i = 0;
                String s1 = (String) o1;
                String s2 = (String) o2;
                if (Float.parseFloat(s1.split("\t")[0]) < 
                        Float.parseFloat(s2.split("\t")[0])) {
                    i = 1;
                }
                else if (Float.parseFloat(s1.split("\t")[0]) > 
                        Float.parseFloat(s2.split("\t")[0])) {
                    i = -1;
                }
                return i;
        }
    });
        Iterator<String> it = mainClasses.iterator();
        sb = new StringBuilder();
        while (it.hasNext()) {
            sb.append(it.next())
                    .append("\n");
        }
        return "CPC query [" + query + "]\n" + sb.toString();
    }
    
    private String formatQuery(String query) {
        return query;
    }
}
