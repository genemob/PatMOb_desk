package patmob.data.ops.impl;

import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.HttpClientUtils;
import patmob.data.inpadoc.InpadocFamily;
import patmob.data.inpadoc.InpadocFamilyCall;
import patmob.data.ops.OpsRestClient;
import patmob.data.ops.OpsServiceRequest;

/**
 * InpadocFamily object is requested while InpadocFamilyCall waits.
 * @author Piotr
 */
public class InpadocFamilyObjectRequest implements OpsServiceRequest {
    //without constituents
    String familyURL = "family/publication/epodoc/";
    //epodoc format
    String patentNumber;
    InpadocFamilyCall famCall;
    HttpRequestBase[] requests;
    
    public InpadocFamilyObjectRequest (String patentNumber,
            InpadocFamilyCall famCall) {
        this.patentNumber = patentNumber;
        this.famCall = famCall;
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
        InpadocFamily iFam = new InpadocFamily();
        iFam.setRequestPubNumber(patentNumber);
        
        if (response.getStatusLine().getStatusCode()==200) {
            HttpEntity resultEntity = response.getEntity();
            if (resultEntity!=null) {
                try {
                    InputStream is = resultEntity.getContent();
                    iFam.processStream(is);
                } catch (Exception ex) {
                    iFam.setErrorMessage("OK, but " + ex.toString());
                }
            }
            else {
                iFam.setErrorMessage("Null entity.");
            }
        }
        else {
            iFam.setErrorMessage("OPS request failed: "
                    + response.getStatusLine());
            HttpClientUtils.closeQuietly(response);
        }
        famCall.setInpadocFamily(iFam);
    }
    
}
