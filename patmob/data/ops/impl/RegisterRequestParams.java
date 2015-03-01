package patmob.data.ops.impl;

/**
 *
 * @author Piotr
 */
public class RegisterRequestParams {
    public static final int SEARCH_REQUEST = 0,
                            BIBLIO_REQUEST = 1;
    private final int registerSearchType;
    private String query = "";
    private String[] pns = new String[0];
    
    public RegisterRequestParams(String[] patentNumbers) {
        registerSearchType = BIBLIO_REQUEST;
        pns = patentNumbers;
    }
    
    public RegisterRequestParams(String searchQuery) {
        registerSearchType = SEARCH_REQUEST;
        query = searchQuery;
    }
    
    public int getSearchType() {
        return registerSearchType;
    }
    
    public String getSearchQuery() {
        return query;
    }
    
    public String[] getPatentNumbers() {
        return pns;
    }
}
