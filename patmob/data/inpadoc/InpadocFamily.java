package patmob.data.inpadoc;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Objects of this class hold data from INPADOC family from OPS. However, this
 * data is organized by applications, rather than publications.
 * (See InpadocFamilyParser.)
 * @author Piotr
 */
public class InpadocFamily extends HashMap<String, PatentApplication> {
    String errorMessage = "", reqPubNumber = "", reqPubTitle = "";
    
    public void setRequestPubNumber(String pubNum) {reqPubNumber = pubNum;}
    public String getRequestPubNumber() {return reqPubNumber;}
    public void setRequestPubTitle(String pubTit) {reqPubTitle = pubTit;}
    public String getRequestPubTitle() {return reqPubTitle;}
    
    public void setErrorMessage(String eMess) {errorMessage = eMess;}
    public String getErrorMessage() {return errorMessage;}
    
//    public InpadocFamily(){}
    
    /**
     * Used by handleResponse() method of InpadocFamilyObjectRequest to 
     * send the contents of OPS response. Calls InpadocFamilyParser to 
     * populate this object with data from OPS XML.
     * @param is 
     */
    public void processStream(InputStream is) {
        InpadocFamilyParser parser = new InpadocFamilyParser();
        parser.parseFamily(is, this);
    }
    
    /**
     * Returns this family as a String specifying a JSON object with "nodes"
     * and "links" arrays. All applications are listed, with all their 
     * publications linked in a chronological order.
     * @return JSON String
     */
    public String getD3ChronoJson() {
        JSONObject jFamily = new JSONObject();
        JSONArray jNodes = new JSONArray(),
                jLinks = new JSONArray();
        //indexes of entities in jNodes
        ArrayList<String> nodeIndex = new ArrayList<String>();
        
        List<PatentApplication> appList = 
                new ArrayList<PatentApplication>(this.values());
        Iterator<PatentApplication> appIterator = appList.iterator();
        
        //first round: link publications to the application
        while (appIterator.hasNext()) {
            PatentApplication app = appIterator.next();
            String appName = app.toString();
            //application and publication numbers may be the same
            nodeIndex.add("app_" + appName);
            
            jNodes.put(new JSONObject()
                    .put("name", appName)
                    .put("group", 1));
            
            List<PatentEntity> publications = app.getPublications();
            if (publications!=null) {
                //we rely on temporal order
                Collections.sort(publications);
                Iterator<PatentEntity> pubIterator = publications.iterator();
                while (pubIterator.hasNext()) {
                    PatentEntity pub = pubIterator.next();
                    String pubName = pub.toString();
                    nodeIndex.add(pubName);
                    int pubIndex = nodeIndex.indexOf(pubName);
                    
                    jNodes.put(new JSONObject()
                            .put("name", pubName)
                            .put("group", 0));
                    jLinks.put(new JSONObject()
                            .put("source", pubIndex)
                            .put("target", pubIndex-1)
                            .put("value", 1));
                }
            }
        }
        
        //second round: link application to priorities (= other apps)
        appIterator = appList.iterator();
        while (appIterator.hasNext()) {
            PatentApplication app = appIterator.next();
            String appName = app.toString();
            List<PatentEntity> priorities = app.getPriorities();
            if (priorities!=null) {
                Iterator<PatentEntity> priIterator = priorities.iterator();
                while (priIterator.hasNext()) {
                    PatentEntity pri = priIterator.next();
                    String priName = pri.toString();
                    
                    //if appName is listed in priorities, we're done
                    if (!priName.equals(appName)) {
                        int appIndex = nodeIndex.indexOf("app_" + appName),
                            priIndex = nodeIndex.indexOf("app_" + priName);
                        //group 2 are applications claimed as priority
                        //by other applications
                        ((JSONObject) jNodes.get(priIndex))
                                .put("group", 2);
                        jLinks.put(new JSONObject()
                                .put("source", appIndex)
                                .put("target", priIndex)
                                .put("value", 1));
                    }
                }
            }
        }
        
        jFamily.put("nodes", jNodes);
        jFamily.put("links", jLinks);

//        return jFamily.toString(2);
        return jFamily.toString();
    }
    
    /**
     * Condensed summary of the family.
     * @return 
     */
    public String printFamily() {
        //Collection appCollection = this.values();
        StringBuilder sb = new StringBuilder(
                "INPADOC Family for " + getRequestPubNumber() + " - "
                + getRequestPubTitle() + "\n\n");
        List<PatentApplication> appList = new ArrayList(this.values());
        Collections.sort(appList);
        Iterator<PatentApplication> it = appList.iterator();
        Iterator<PatentEntity> pit;
        while (it.hasNext()) {
            PatentApplication app = it.next();
            sb.append("AN: ").append(app).append("\n");
            if (app.getPublications()!=null) {
                pit = app.getPublications().iterator();
                sb.append("    PN: ").append(pit.next()).append("\n");
                while (pit.hasNext()) {
                    sb.append("        ").append(pit.next()).append("\n");
                }
            }
            if (app.getPriorities()!=null) {
                pit = app.getPriorities().iterator();
                sb.append("    PR: ").append(pit.next()).append("\n");
                while (pit.hasNext()) {
                    sb.append("        ").append(pit.next()).append("\n");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
    /**
     * Get application with the desired publication number (e.g. US6221633 ).
     */
    public PatentApplication getApplicationForPub(String pn) {
        List<PatentApplication> appList = new ArrayList(this.values());
        Iterator<PatentApplication> it = appList.iterator();
        Iterator<PatentEntity> pit;
        while (it.hasNext()) {
            PatentApplication app = it.next();
            if (app.getPublications()!=null) {
                pit = app.getPublications().iterator();
                while (pit.hasNext()) {
                    PatentEntity pub = pit.next();
                    if (pn.equals(pub.getCountry() + pub.getNumber())) {
                        return app;
                    }
                }
            }
        }
        return null;
    }
}