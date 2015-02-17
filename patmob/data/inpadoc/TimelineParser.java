package patmob.data.inpadoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * "dates" and "events" arrays map the following events:
 * 0 - earliest priority
 * 1 - last event in the publication cycle (grant?)
 * 2 - in-force starts
 * 3 - calculated expiry
 * 4 - extension
 * 5 - marker
'{"info":{"project":"Diabetes"},"patents":[' +
    '{"product":"Apidra","patent":"US6221633","dates":[19970620,20010424,19980618,20180618,0,0],"events":["","","","","",""]},' +
    '{"product":"Apidra","patent":"US7193035","dates":[20021029,20070320,20031029,20231029,20240828,0],"events":["","","","","",""]},' +
    '{"product":"Lantus","patent":"US5656722","dates":[19881108,19970812,19970812,20140812,20150212,0],"events":["","","","","",""]}' +
']}'
 * 
 */
public class TimelineParser {
    
    public static JSONObject getTimeline(PatentApplication pa, String pn) {
        JSONObject jTimeline = new JSONObject()
                .put("product", pn)
                .put("patent", pn);
        JSONArray dates = new JSONArray(),
                events = new JSONArray();
        List<PatentEntity> sortedPriors = new ArrayList(pa.getPriorities());
        Collections.sort(sortedPriors);
        List<PatentEntity> sortedPubs = new ArrayList(pa.getPublications());
        Collections.sort(sortedPubs);
        
        //earliest priority
        PatentEntity firstPri = sortedPriors.get(0);
        events.put(firstPri.getCountry() + firstPri.getNumber());
        dates.put(firstPri.getDate());

        //grant?
        PatentEntity lastPub = sortedPubs.get(sortedPubs.size()-1);
        events.put(lastPub.getCountry() + lastPub.getNumber());
        dates.put(lastPub.getDate());
        
        //TODO: CALCULATIONS...
        //in-force starts
        events.put(pa.getCountry() + pa.getNumber());
        dates.put(pa.getDate());
        
        //in-force ends
        events.put("20 years");
        dates.put(Integer.toString(Integer.parseInt(pa.getDate()) + 200000));
        
        //extension
        events.put("");
        dates.put("");
        
        //marker
        events.put("");
        dates.put("");
        
        return jTimeline
                .put("dates", dates)
                .put("events", events);
    }
}
