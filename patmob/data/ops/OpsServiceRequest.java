package patmob.data.ops;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * Implementations are placed in Queues for OpsRestClient Threads; deliver 
 * requests and get the responses.
 * @author Piotr
 */
public interface OpsServiceRequest {
    
    public HttpRequestBase[] getRequests();
    
    public void handleResponse(HttpResponse response);
    
}
