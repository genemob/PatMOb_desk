package patmob.data.ops.impl.register;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.HttpClientUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import patmob.data.ops.impl.RegisterRequest;

/**
 * Overrides handleResponse() in RegisterRequest, to return all publications
 * details (so that missing abstract and claims for EP pub can be retrieved
 * from a corresponding WO pub). 
 * @author Piotr
 */
public class AllPubsRegisterRequest extends RegisterRequest {
    RegisterRequestParams params;
            
    public AllPubsRegisterRequest(RegisterRequestParams searchParams) {
        super(searchParams);
        params = searchParams;
    }
    
    @Override
    public void handleResponse(HttpResponse response) {
        if (response.getStatusLine().getStatusCode()==200) {
            HttpEntity resultEntity = response.getEntity();
            if (resultEntity!=null) {
                try {
                    InputStream is = resultEntity.getContent();
                    setupParser(is);
                    InputSource inputSource = getInputSource();
                    String pubs, docIdExpression = "//reg:publication-reference/reg:document-id";
                    StringBuilder sb = new StringBuilder();
                    NodeList docIdNodes = (NodeList) getXPath().evaluate(
                            docIdExpression, inputSource, XPathConstants.NODESET);
                    for (int i=0; i<docIdNodes.getLength(); i++) {
                        Node docIdNode = docIdNodes.item(i);
                        sb.append(getXPath().evaluate("reg:country/text()", docIdNode))
                                .append(getXPath().evaluate("reg:doc-number/text()", docIdNode))
                                .append(" ")
                                .append(getXPath().evaluate("reg:kind/text()", docIdNode))
                                .append(" ")
                                .append(getXPath().evaluate("reg:date/text()", docIdNode))
                                .append("; ");
                    }
                    pubs=sb.substring(0, sb.length()-2);
                    params.addResultRow(pubs);
                } catch (IOException | UnsupportedOperationException | XPathExpressionException ex) {
                    System.out.println("AllPubsRegisterRequest (Exception): " + ex);
                }
            }
        } else {
            //response status not OK
            System.out.println("AllPubsRegisterRequest (status): " + response.getStatusLine());
            HttpClientUtils.closeQuietly(response);
        }
        // tell Callable to return
        responseCount++;
    }
}
