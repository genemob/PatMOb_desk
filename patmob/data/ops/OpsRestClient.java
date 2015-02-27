package patmob.data.ops;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

/**
 * OPS Client started at application launch by a call to .initialize().
 * Handles all OPS access.  Multiple threads corresponding to different OPS 
 * throttles (as of v3.0, 12-20-12). Declared final and all methods are static
 * to assure consistent throttle control.
 * @author Piotr
 */
public final class OpsRestClient {
    public static final int OTHER_THROTTLE     = 0,
                            SEARCH_THROTTLE    = 1,
                            RETRIEVAL_THROTTLE = 2,
                            INPADOC_THROTTLE   = 3,
                            IMAGES_THROTTLE    = 4;
    public static final String 
//            OPS_URL = "https://ops.epo.org/3.1/rest-services/";
            OPS_URL = "https://ops.epo.org/rest-services/";     //20150226 version not needed?
    public static String currentAccessToken = "",
//20140705
myKey = "", mySecretKey = "";
    static private DefaultHttpClient httpclient;
    private static boolean connected2ops = false,
//20140705
registeredUser = false;
    
    public static boolean isInitialized() {return connected2ops;}
    
//    public static String initialize(String patmobProxy) {
    public static String initialize(String patmobProxy, 
            String opsKey, String opsSecretKey) {                               //20140705
        String opsConnStatus;
        
        //multithreading
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(
                new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        //OPS 3.1 uses https - either http or https good for anonymous users
        schemeRegistry.register(
                new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
        PoolingClientConnectionManager cm = 
                new PoolingClientConnectionManager(schemeRegistry);
        httpclient = new DefaultHttpClient(cm);
        
        //proxy address from properties
        //e.g. patmobProxy=globalproxy-amer.pharma.aventis.com\:3129
        if (patmobProxy!=null) {
            StringTokenizer st = new StringTokenizer(patmobProxy, ":");
            HttpHost proxy = new HttpHost(
                    st.nextToken(), Integer.parseInt(st.nextToken()));
            httpclient.getParams().setParameter(
                    ConnRoutePNames.DEFAULT_PROXY, proxy);
        }
        
        HttpPost httpPost;
        if (opsKey!=null && opsSecretKey!=null) {                               //20140705
            OpsAuth.setKeys(opsKey, opsSecretKey);
            httpPost = OpsAuth.getAccessTokenRequest();
            registeredUser = true;
            //TODO: handling of scenario when params from properties don't work
        } else {
            httpPost = new HttpPost(OPS_URL + 
                    "published-data/publication/epodoc/full-cycle");
            try {
                StringEntity queryEntity = new StringEntity("EP1000000");
                httpPost.setEntity(queryEntity);
            } catch (UnsupportedEncodingException ex) {ex.printStackTrace();}
        }
        try {
            HttpResponse httpResponse = httpclient.execute(httpPost);
            opsConnStatus = httpResponse.getStatusLine().toString();
            
            if (httpResponse.getStatusLine().getStatusCode()==200) {
                handleInitResponse(httpResponse);
            }
            else if (httpResponse.getStatusLine().getStatusCode()==407) {
                //proxy needs authentication
                NTCredentials ntc = ProxyCredentialsDialog.getNTCredentials();
                httpclient.getCredentialsProvider().setCredentials(
                        AuthScope.ANY, ntc);
                httpResponse = httpclient.execute(httpPost);
                opsConnStatus = httpResponse.getStatusLine().toString();
                
                if (httpResponse.getStatusLine().getStatusCode()==200) {
                    handleInitResponse(httpResponse);
                }
            }
        } catch (IOException ex) {
            opsConnStatus = ex.toString();
        }
        return opsConnStatus;
    }
    
    public static void handleInitResponse(HttpResponse httpResponse) {
        if (registeredUser) {                                                   //20140705
            currentAccessToken = OpsAuth.getAccessToken(httpResponse);
            System.out.println("YOUR KEY TO OPS: " + currentAccessToken);
        }
        OpsRestClient.startThreads();
        connected2ops = true;
    }
    
    public static void authorize(HttpRequestBase request) {
        if (registeredUser) {                                                   //20140705
            if (request.getFirstHeader("Authorization")==null) {
                request.addHeader("Authorization", "Bearer " + currentAccessToken);
            } else {
                request.setHeader("Authorization", "Bearer " + currentAccessToken);
            }
        }
    }
    
    public static void submitServiceRequest(OpsServiceRequest ticket, int throttle) {
        if (connected2ops) {
            switch (throttle) {
                case SEARCH_THROTTLE:
                    searchQueue.offer(ticket);
                    searchThread.wakeup();
                    break;
                case RETRIEVAL_THROTTLE:
                    retrievalQueue.offer(ticket);
                    retrievalThread.wakeup();
                    break;
                case INPADOC_THROTTLE:
                    inpadocQueue.offer(ticket);
                    inpadocThread.wakeup();
                    break;
                case IMAGES_THROTTLE:
                    imagesQueue.offer(ticket);
                    imagesThread.wakeup();
                    break;
                case OTHER_THROTTLE:
                    otherQueue.offer(ticket);
                    otherThread.wakeup();
            }
        }
        else {
            HttpResponse response = new BasicHttpResponse(
                    new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1),
                    404, "Could not connect to OPS"));
            ticket.handleResponse(response);
        }
    }
    
    private static class SearchThread extends Thread {
        public synchronized void wakeup() {
            searchThreadSuspended = false;
            notify();
        }
        
        @Override
        public void run() {
            while (true) {
                try {
                    if (searchThreadSuspended) {
                        synchronized(this) {
                            while (searchThreadSuspended) {
                                wait();
                            }
                        }
                    }
                } catch (Exception ex){ex.printStackTrace();}
                threadWork(SEARCH_THROTTLE);
            }
        }
    }
    
    private static class RetrievalThread extends Thread {
        public synchronized void wakeup() {
            retrievalThreadSuspended = false;
            notify();
        }
        
        @Override
        public void run() {
            while (true) {
                try {
                    if (retrievalThreadSuspended) {
                        synchronized(this) {
                            while (retrievalThreadSuspended)
                                wait();
                        }
                    }
                } catch (Exception ex){ex.printStackTrace();}
                threadWork(RETRIEVAL_THROTTLE);
            }
        }
    }
    
    private static class InpadocThread extends Thread {
        public synchronized void wakeup() {
            inpadocThreadSuspended = false;
            notify();
        }
        
        @Override
        public void run() {
            while (true) {
                try {
                    if (inpadocThreadSuspended) {
                        synchronized(this) {
                            while (inpadocThreadSuspended)
                                wait();
                        }
                    }
                } catch (Exception ex){ex.printStackTrace();}
                threadWork(INPADOC_THROTTLE);
            }
        }
    }
    
    private static class ImagesThread extends Thread {
        public synchronized void wakeup() {
            imagesThreadSuspended = false;
            notify();
        }
        
        @Override
        public void run() {
            while (true) {
                try {
                    if (imagesThreadSuspended) {
                        synchronized(this) {
                            while (imagesThreadSuspended)
                                wait();
                        }
                    }
                } catch (Exception ex){ex.printStackTrace();}
                threadWork(IMAGES_THROTTLE);
            }
        }
    }
    
    private static class OtherThread extends Thread {
        public synchronized void wakeup() {
            otherThreadSuspended = false;
            notify();
        }
        
        @Override
        public void run() {
            while (true) {
                try {
                    if (otherThreadSuspended) {
                        synchronized(this) {
                            while (otherThreadSuspended)
                                wait();
                        }
                    }
                } catch (Exception ex){ex.printStackTrace();}
                threadWork(OTHER_THROTTLE);
            }
        }
    }
    
    private static void threadWork(int throttle) {
        HttpRequestBase request;
        HttpResponse response;
        Queue<OpsServiceRequest> queue = null;
        
        switch (throttle) {
            case SEARCH_THROTTLE:
                queue = searchQueue;
                break;
            case RETRIEVAL_THROTTLE:
                queue = retrievalQueue;
                break;
            case INPADOC_THROTTLE:
                queue = inpadocQueue;
                break;
            case IMAGES_THROTTLE:
                queue = imagesQueue;
                break;
            case OTHER_THROTTLE:
                queue = otherQueue;
        }
        
        OpsServiceRequest ticket = queue.poll();
        if (ticket!=null) {
            HttpRequestBase[] requests = ticket.getRequests();
            for (int i=0; i<requests.length; i++) {
                request = ticket.getRequests()[i];
                //add access token header in v. 3.1
                authorize(request);
                try {
//System.out.println("REQ:   " + request);
                    response = httpclient.execute(request);
//System.out.println("RES:   " + response);
                    if (response!=null) {
       //20141114 
/*
 * 20141117
 * Equivalents request, followed by images request crashes the client:
 * request is created, but no response comes back.
REQ:   GET https://ops.epo.org/3.1/rest-services/published-data/publication/epodoc/WO0002850/equivalents/images,biblio,full-cycle HTTP/1.1
RES:   HTTP/1.1 200 OK [Cache-Control: no-cache, Content-Type: application/xml, Date: Tue, 18 Nov 2014 01:40:01 GMT, ETag: "0b518774cac69ba379d1c5e3ad41f1619", Pragma: no-cache, Server: Apache, X-API: ops-v3.1, X-EPO-Client-IP: 68.45.66.107, X-EPO-Forwarded: [68.45.66.107], X-IndividualQuotaPerHour-Used: 3505228, X-RegisteredQuotaPerWeek-Used: 24299988, X-Throttling-Control: overloaded (images=green:50, inpadoc=green:30, other=green:1000, retrieval=green:50, search=green:5), Content-Length: 875915, Connection: keep-alive]
*** per hour : 3505228;  per week :24299988
retrieval_THROTTLE = green:50
REQ:   GET https://ops.epo.org/3.1/rest-services/published-data/images/US/20030171352/PA/firstpage.png?Range=1 HTTP/1.1
RES:   HTTP/1.1 200 OK [Content-Disposition: attachment; filename=US    20030171352PAFP .png, Content-Type: image/png, Date: Tue, 18 Nov 2014 01:40:04 GMT, ETag: "09dac9de201b952997cf16635f1d20d64", Server: Apache, X-API: ops-v3.1, X-EPO-Client-IP: 68.45.66.107, X-EPO-Forwarded: [68.45.66.107], X-IndividualQuotaPerHour-Used: 3507113, X-RegisteredQuotaPerWeek-Used: 24301873, X-Throttling-Control: idle (images=green:200, inpadoc=green:60, other=green:1000, retrieval=green:200, search=green:30), Content-Length: 1885, Connection: keep-alive]
*** per hour : 3507113;  per week :24301873
images_THROTTLE = green:200
REQ:   POST https://ops.epo.org/3.1/rest-services/published-data/search HTTP/1.1
 *
 */
                        //Bad Request - access token expired?
                        if (response.getStatusLine().getStatusCode()==400) {
                            System.out.println("BAD REQUEST: " + response);
                            HttpClientUtils.closeQuietly(response);
                            //new access token
                            HttpPost httpPost = OpsAuth.getAccessTokenRequest();
                            HttpResponse httpResponse = httpclient.execute(httpPost);
                            currentAccessToken = OpsAuth.getAccessToken(httpResponse);
                            System.out.println("NEW KEY TO OPS: " + currentAccessToken);
                            //program hangs if don't kill httpResponse here
                            // - but not in initialize (line 103) ??
                            HttpClientUtils.closeQuietly(httpResponse);
                            authorize(request);
                            response = httpclient.execute(request);

//                            ticket.handleResponse(response);
                            processResponse(response, throttle, ticket);
                        } else {
                            processResponse(response, throttle, ticket);
//                            String perHour = "--", perWeek = "--";
//                            Header hrHeader = response.getFirstHeader(
//                                    "X-IndividualQuotaPerHour-Used"),
//                                    wkHeader = response.getFirstHeader(
//                                    "X-RegisteredQuotaPerWeek-Used");
//                            if (hrHeader!=null) perHour = hrHeader.getValue();
//                            if (wkHeader!=null) perWeek = wkHeader.getValue();
//                            System.out.println("*** per hour : " + perHour +
//                                    ";  per week :" + perWeek);
//
//                            adjustThrottle(response.getFirstHeader
//                                    ("X-Throttling-Control"), throttle);
//                            ticket.handleResponse(response);
                        }
                    }
                } catch (Exception ex) {ex.printStackTrace();}
            }
        }
        else {
            switch (throttle) {
                case SEARCH_THROTTLE:
                    searchThreadSuspended = true;
                    break;
                case RETRIEVAL_THROTTLE:
                    retrievalThreadSuspended = true;
                    break;
                case INPADOC_THROTTLE:
                    inpadocThreadSuspended = true;
                    break;
                case IMAGES_THROTTLE:
                    imagesThreadSuspended = true;
                    break;
                case OTHER_THROTTLE:
                    otherThreadSuspended = true;
            }
        }
    }
    
    private static void processResponse(HttpResponse response, int throttle,
            OpsServiceRequest ticket) {
        
        if (response.getStatusLine().getStatusCode()==403) {                    //20140705
            JOptionPane.showMessageDialog(null, 
                    response.getStatusLine().toString());
        }
        
        //Usage quotas
        String perHour = "--", perWeek = "--";
        Header hrHeader = response.getFirstHeader("X-IndividualQuotaPerHour-Used"),
                wkHeader = response.getFirstHeader("X-RegisteredQuotaPerWeek-Used");
        if (hrHeader!=null) {
                    perHour = hrHeader.getValue();
                }
        if (wkHeader!=null) {
                    perWeek = wkHeader.getValue();
                }
System.out.println("*** per hour : " + perHour + ";  per week :" + perWeek);

        adjustThrottle(response.getFirstHeader("X-Throttling-Control"), throttle);
        ticket.handleResponse(response);
    }
        
    /**
     * Example header value: "idle (retrieval=green:200, search=green:30, 
     * inpadoc=green:60, images=green:200, other=green:1000")
     * @param header
     * @param throttle 
     */
    private static void adjustThrottle(Header header, int throttle) {
        /**
         * Color indicates permitted request limit use: green less than 50%; 
         * yellow 50-75%; red more than 75%; black limit exceeded = suspended.
         */
        String trafficLight, throttleName = "search";
        /** 
         * Depending on load, permitted requests per minute. Expressed as 
         * long for ease conversion to thread sleep time.
         */
        long rpm;
        TrafficInfo trafficInfo;
        
        if (header!=null) {
            String headerValue = header.getValue();
            switch (throttle) {
                case SEARCH_THROTTLE:
                    throttleName = "search";
                    break;
                case RETRIEVAL_THROTTLE:
                    throttleName = "retrieval";
                    break;
                case INPADOC_THROTTLE:
                    throttleName = "inpadoc";
                    break;
                case IMAGES_THROTTLE:
                    throttleName = "images";
                    break;
                case OTHER_THROTTLE:
                    throttleName = "other";
            }
            trafficInfo = getTrafficInfo(headerValue, throttleName);
            trafficLight = trafficInfo.trafficLight();
            rpm = trafficInfo.rpm();
System.out.println(throttleName + "_THROTTLE = " + trafficLight + ":" + rpm);

            // ADJUST THROTTLE
            if ("yellow".equals(trafficLight)) {
                try {
                    long sleepTime = 60000/rpm;
                    Thread.sleep(2 * sleepTime);
                    System.out.println("YELLOW " +
                            Thread.currentThread().getClass() +
                            " sleep " + 2 * sleepTime);
                } catch (InterruptedException ex) {ex.printStackTrace();}
            }
            else if ("red".equals(trafficLight)) {
                try {
                    long sleepTime = 60000/rpm;
                    Thread.sleep(5 * sleepTime);
                    System.out.println("RED " +
                            Thread.currentThread().getClass() +
                            " sleep " + 5 * sleepTime);
                } catch (InterruptedException ex) {ex.printStackTrace();}
            }
         }
        else {
            System.out.println("NO X-Throttling-Control HEADER");
        }
    }
    
    private static class TrafficInfo {
        String trafficLight = "";
        long rpm = 0;
        TrafficInfo(){}     //no traffic info
        TrafficInfo(String tl, long l){
            trafficLight = tl;
            rpm = l;
        }
        String trafficLight() {return trafficLight;}
        long rpm() {return rpm;}
    }
    
    /**
     * Parses "X-Throttling-Control" Header value.
     * @param headerValue
     * @param throttleName
     * @return 
     */
    private static TrafficInfo getTrafficInfo(String headerValue, 
            String throttleName) {
        TrafficInfo ti;
        try {
            //idle (images=green:200, inpadoc=green:60, ... search=green:30)
            String throttleValues = headerValue.substring(
                    headerValue.indexOf("(")+1, headerValue.indexOf(")"));
            int throttleNameIndex = throttleValues.indexOf(throttleName);
            int commaIndex = throttleValues.indexOf(",", throttleNameIndex);
            String throttleValue;
            if (commaIndex>0) {
                throttleValue = throttleValues.substring(throttleNameIndex,
                        throttleValues.indexOf(",", throttleNameIndex));
            }
            else {
                throttleValue = throttleValues.substring(throttleNameIndex);
            }
            String trafficLight = throttleValue.substring(
                    throttleValue.indexOf("=")+1, throttleValue.indexOf(":"));
            long rpm = Long.parseLong(throttleValue.substring(
                    throttleValue.indexOf(":")+1));
            ti = new TrafficInfo(trafficLight, rpm);
        } catch (Exception ex) {
            ex.printStackTrace();
            ti = new TrafficInfo();
        }
        return ti;
    }
    
    private static void startThreads() {
        searchThread = new SearchThread();
        searchThread.start();
        retrievalThread = new RetrievalThread();
        retrievalThread.start();
        inpadocThread = new InpadocThread();
        inpadocThread.start();
        imagesThread = new ImagesThread();
        imagesThread.start();
        otherThread = new OtherThread();
        otherThread.start();
    }
    
    private static SearchThread searchThread;
    private static RetrievalThread retrievalThread;
    private static InpadocThread inpadocThread;
    private static ImagesThread imagesThread;
    private static OtherThread otherThread;
    private static Queue<OpsServiceRequest>
            searchQueue =  new LinkedList<OpsServiceRequest>(),
            retrievalQueue =  new LinkedList<OpsServiceRequest>(),
            inpadocQueue =  new LinkedList<OpsServiceRequest>(),
            imagesQueue =  new LinkedList<OpsServiceRequest>(),
            otherQueue =  new LinkedList<OpsServiceRequest>();
    private static volatile boolean 
            searchThreadSuspended = true,
            retrievalThreadSuspended = true,
            inpadocThreadSuspended = true,
            imagesThreadSuspended = true,
            otherThreadSuspended = true;
    
}