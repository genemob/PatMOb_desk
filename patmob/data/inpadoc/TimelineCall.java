/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package patmob.data.inpadoc;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Piotr
 */
public class TimelineCall implements Callable<String> {
    String[] pubNumbers;
    InpadocFamily[] families;
    
    public String getTimelines(String[] pns) {
        pubNumbers = pns;
        families = new InpadocFamily[pns.length];
        for (int i=0; i<families.length; i++) {
            InpadocFamilyCall famCall = new InpadocFamilyCall();
            families[i] = famCall.getInpadocFamily(pns[i]);
        }
        
        String timelineJson;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(this);
        try {
            timelineJson = future.get();
            executor.shutdown();
        } catch (Exception e) {
            timelineJson = e.toString();
        }
        
        return timelineJson;
    }
    
    //returned by call() when notReady() returns false
    private String getTimelineJson() {
        JSONObject jTimelines = new JSONObject(),
                info = new JSONObject()
                .put("project", "OPS data");
        JSONArray patents = new JSONArray();
        
        for (int i=0; i<families.length; i++) {
            patents.put(TimelineParser.getTimeline(
                    families[i].getApplicationForPub(pubNumbers[i]), 
                    pubNumbers[i]));
        }
        jTimelines.put("info", info)
                .put("patents", patents);
        return jTimelines.toString();
    }
    
    private boolean notReady() {
        for (int i=0; i<families.length; i++) {
            if (families[i]==null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String call() throws Exception {
        while (notReady()) {
            Thread.sleep(100);
        }
        return getTimelineJson();
    }
    
    /*public static void main(String[] args) {
        TimelineCall tc = new TimelineCall();
        System.out.println(tc.getTimelines(new String[]{"p","ns"}));
        System.out.println(tc.families[0]);
        System.out.println(tc.families[1]);
        System.out.println(tc.families[2]);
    }*/
    
}
