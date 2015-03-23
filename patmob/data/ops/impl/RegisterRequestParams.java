package patmob.data.ops.impl;

import patmob.data.PatentTreeNode;

/**
 *
 * @author Piotr
 */
public class RegisterRequestParams {
    public static final int SEARCH_REQUEST = 0,
                            BIBLIO_REQUEST = 1,
            
                            SAMPLE_RESULT = 10,
                            ALL_RESULT    = 11;
    
    private final int registerRequestType;
    //sample result by default
    private int resultType = SAMPLE_RESULT;
    private String resultFilePath;
    private String query = "";
    private String[] pns = new String[0];
    private PatentTreeNode patents = null;
    
    /**
     * Params for biblio request. 
     * Get information for the submitted publication numbers.
     * @param patentNumbers - String[] with EP publication numbers.
     */
    public RegisterRequestParams(String[] patentNumbers) {
        registerRequestType = BIBLIO_REQUEST;
        pns = patentNumbers;
     }
    
    /**
     * Params for search request. 
     * Get publication numbers for the search results. Note: on 2015-03-22,
     * "pa = sanofi and pn = ep" finds 3,755 items in Register but only
     * 3,557 items in Espacenet.
     * @param searchQuery - boolean query used to search Register.
     */
    public RegisterRequestParams(String searchQuery) {
        registerRequestType = SEARCH_REQUEST;
        query = searchQuery;
    }
    
    public void setFilePath(String resultFile) {
        resultFilePath = resultFile;
    }
    
    public String getFilePath() {
        return resultFilePath;
    }
    
    public int getRequestType() {
        return registerRequestType;
    }
    
    public void setResultType(int result) {
        resultType = result;
    }
    
    public int getResultType() {
        return resultType;
    }
    
    public String getSearchQuery() {
        return query;
    }
    
    public String[] getPatentNumbers() {
        return pns;
    }
    
    public void setPatents(PatentTreeNode ptn) {
        patents = ptn;
    }
    
    public PatentTreeNode getPatents() {
        return patents;
    }
}
