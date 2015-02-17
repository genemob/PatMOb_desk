package patmob.data.ops;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xml.sax.InputSource;

/**
 * Various utilities.
 * @author Piotr
 */
public class OpsAuth {
    static String authURL = "https://ops.epo.org/3.1/auth/accesstoken",
            myKey = "", mySecretKey = "";
            //PatMOb
//            myKey = "xbtoneDpHut6Fj4MQVKRJ3W6t5kdByj1",
//            mySecretKey = "oGqOxBf6xSCRRwAr";
//            //PatMObNet
//            myKey = "W4zFGcMgeNfciO4E67xTtZgGvdNCIMEx",
//            mySecretKey = "M8zl74dzAW8juCRq";
    
    static public void setKeys(String opsKey, String opsSecretKey) {
        myKey = opsKey;
        mySecretKey = opsSecretKey;
    }
    
    static public HttpPost getAccessTokenRequest() {
        HttpPost httpPost = new HttpPost(authURL);
        String base64Encoding = getBase64Encoding(myKey, mySecretKey);
        httpPost.setHeader("Authorization", "Basic " + base64Encoding);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        StringEntity entity = null;
        try {
            entity = new StringEntity("grant_type=client_credentials");
        } catch (UnsupportedEncodingException x) {x.printStackTrace();}
        httpPost.setEntity(entity);
        return httpPost;
    }
    
    static public String getBase64Encoding(String key, String secretKey) {
        Base64 base64 = new Base64();
        String keys = key + ":" + secretKey;
        String base64Encoding = base64.encodeToString(keys.getBytes());
        return base64Encoding;
    }
    
    static public String getAccessToken(HttpResponse response) {
        String accessToken = null;
        HttpEntity resultEntity = response.getEntity();
        if (resultEntity!=null) {
            try {
                InputStream is = resultEntity.getContent();
                JSONTokener jToke = new JSONTokener(is);
                JSONObject jOb = new JSONObject(jToke);
                accessToken = jOb.getString("access_token");
            } catch (Exception x) {x.printStackTrace();}
        }
        return accessToken;
    }
    
}
