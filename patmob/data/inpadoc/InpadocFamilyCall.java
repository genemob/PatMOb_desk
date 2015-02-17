package patmob.data.inpadoc;

import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import patmob.convert.PNFormat;
import patmob.data.PatentCollectionMap;
import patmob.data.PatentDocument;
import patmob.data.ops.impl.BulkBiblioRequest;
import patmob.data.ops.impl.InpadocFamilyObjectRequest;

/**
 * The getInpadocFamily() method submits a InpadocFamilyObjectRequest to OPS,
 * and waits for response. If the requested publication number format cannot be
 * verified, request is not submitted, and empty InpadocFamily with an error
 * message is returned.
 * @author Piotr
 */
public class InpadocFamilyCall implements Callable<InpadocFamily> {
    InpadocFamily iFam = null;
    PatentDocument reqPub;
    PatentCollectionMap patMap;

    /**
     * Blocks till InpadocFamilyObjectRequest is done.
     * @param pubNum epodoc format
     */
    public InpadocFamily getInpadocFamily(String pubNum) {
        InpadocFamily family = new InpadocFamily();
        
        //verify pubNum format
        if ((pubNum = verify(pubNum))==null) {
            family.setErrorMessage("Couldn't verify number format (" +
                    pubNum + ").");
            return family;
        }
        
        //get the pub title
        reqPub = new PatentDocument(pubNum);
        patMap = new PatentCollectionMap();
        patMap.addChild(reqPub);
        BulkBiblioRequest bbReq = new BulkBiblioRequest(patMap);
        bbReq.submit();
        
        InpadocFamilyObjectRequest famReq = 
                new InpadocFamilyObjectRequest (pubNum, this);
        famReq.submit();
                
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<InpadocFamily> future = executor.submit(this);
		
        try {
            family = future.get();
            executor.shutdown();
        } catch (Exception e) {
            family.setErrorMessage(e.toString());
        }
        
        return family;
    }
    
    /**
     * Used by InpadocFamilyObjectRequest.
     * @param family 
     */
    public void setInpadocFamily(InpadocFamily family) {iFam = family;}
    
    @Override
    public InpadocFamily call() throws Exception {
        while (iFam==null) {
            Thread.sleep(100);
        }
        
        iFam.setRequestPubTitle(getReqTitle());
        return iFam;
    }
    
    //todo: Better way to get biblio
    //todo: Wait until title request either finishes or fails
    String getReqTitle() {
        reqPub = (PatentDocument) patMap.getChildren().iterator().next();
        String info = reqPub.getInfo();
        if (info.startsWith("PN: ")) {
            return info.substring(info.indexOf("TI: ")+4, info.indexOf("PA: ")-1);
        }
        else {
            return "Could not get title";
        }
    }
    
    /**
     * Elementary format verification attempt.
     * @param pubnum
     * @return Verified publication number String
     */
    String verify(String pubnum) {
        StringTokenizer st = new StringTokenizer(pubnum);
        String vPubnum = "";
        while (st.hasMoreTokens()) {
            vPubnum += st.nextToken();
            if (vPubnum.length()>2) {
                break;
            }
        }
        if (vPubnum.length()>2) {
            vPubnum = vPubnum.replaceAll("\\p{Punct}", "");
//            vPubnum = vPubnum.replaceAll("\\/", "")
//                    .replaceAll("\\-", "")
//                    .replaceAll("\\,", "")
//                    .replaceAll("\\.", "");
            PatentDocument pat = new PatentDocument(vPubnum);
            vPubnum = PNFormat.getPN(pat, PNFormat.EPO);
            return pat.getCountry() + vPubnum;
        }
        else {
            return null;
        }
    }
}
