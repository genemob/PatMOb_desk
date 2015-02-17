package patmob.data.ops;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.json.JSONObject;
import patmob.convert.OPSBiblio;
import patmob.data.PatentCollectionMap;
import patmob.data.PatentDocument;
import patmob.data.PatentTreeNode;
import patmob.data.ops.impl.BulkBiblioRequest;

/**
 *
 * @author Piotr
 */
public class BulkBiblioCall implements Callable<String> {
    PatentCollectionMap patMap;
    
    public String getBiblioJson(PatentCollectionMap map) {
        patMap = map;

        BulkBiblioRequest bbReq = new BulkBiblioRequest(patMap);
        bbReq.submit();
        
        String biblioJson;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(this);
        try {
            biblioJson = future.get();
            executor.shutdown();
        } catch (Exception e) {
            biblioJson = e.toString();
        }
        
        return biblioJson;
    }
    
    @Override
    public String call() throws Exception {
        while (notReady()) {
            Thread.sleep(100);
        }
        return parseOPSBiblios();
    }
    
    //check for OPSBiblio in patent docs
    private boolean notReady() {
        Iterator<PatentTreeNode> it = patMap.getChildren().iterator();
        while (it.hasNext()) {
            PatentDocument doc = (PatentDocument) it.next();
            if (doc.getBiblio()!=null) {
                return false;
            }
        }
        return true;
    }
    
    //create JSON data when BulkBiblioRequest returns
    private String parseOPSBiblios() {
        JSONObject jNodeBiblios = new JSONObject()
                .put("nodeId", patMap.getName());
        Iterator<PatentTreeNode> it = patMap.getChildren().iterator();
        while (it.hasNext()) {
            PatentDocument doc = (PatentDocument) it.next();
            JSONObject jBiblio = new JSONObject()
                    .put("members", "");
            OPSBiblio opsBiblio = doc.getBiblio();
            if (opsBiblio!=null) {
                Iterator<PatentTreeNode> it2 = 
                        opsBiblio.getBiblioCollection().getSortedChildren().iterator();
                String lastAb = "";
                while (it2.hasNext()) {
                    PatentDocument member = (PatentDocument) it2.next();

//System.out.println(doc.toString() + " : " + member.toString() + " ->" + member.getAbstract() + "<-");  
                    // TODO: too many members - WO docs printed 2 times
                    if (!member.getAbstract().equals("")) {
                        lastAb = member.getAbstract().replaceAll("[\"\']", ""); //.replace("\"", "*")
                    }
                    jBiblio.put("ti", member.getTitle().replaceAll("[\"\']", ""))   //ti and ab from latest pub
                            .put("members", jBiblio.getString("members") +      //? .replace("\"", "*")
                            member.getCountry() + member.getNumber() + " " + member.getKindCode()
                            + " " + member.getPublicationDate() + "; ")
                            .put("ab", lastAb);
                }
            }
            jNodeBiblios.put(doc.getCountry() + doc.getNumber(), jBiblio);
        }
        return jNodeBiblios.toString();
    }
}
