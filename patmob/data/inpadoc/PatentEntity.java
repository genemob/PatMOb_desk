package patmob.data.inpadoc;

import java.util.StringTokenizer;

/**
 * General functionality common to PatentApplication, PatentPublication and
 * PatentPriority in InpadocFamily.
 * @author Piotr
 */
public class PatentEntity implements Comparable<PatentEntity> {
    String country = "", number = "na", kind = "na", date = "0000";

    /**
     * @param s Publication number, including kind code and date, as one String
     * e.g. "US6060276 A 20000509"
     */
    public PatentEntity(String s) {
        StringTokenizer st = new StringTokenizer(s);
        try {
            String ccnum = st.nextToken();
            country = ccnum.substring(0,2);
            number = ccnum.substring(2);
            kind = st.nextToken();
            date = st.nextToken();
        } catch (Exception ex) {}
    }

    /**
     * Components of the publication number entered as separate Strings.
     * @param cc e.g. "US"
     * @param nu e.g. "6060276"
     * @param ki e.g. "A"
     * @param da e.g. "20000509"
     */
    public PatentEntity(String cc, String nu, String ki, String da) {
        country = cc;
        number = nu;
        kind = ki;
        date = da;
    }

    public String getCountry() {return country;}
    public String getNumber() {return number;}
    public String getKind() {return kind;}
    public String getDate() {return date;}
    @Override
    public String toString() {
        return country + number + " " + kind + " " + date;
    }

    /**
     * Same entity if number, kind and date match;
     * otherwise sort by date.
     */
    @Override
    public int compareTo(PatentEntity o) {
        if (toString().equals(o.toString())) {
            return 0;
        } else if (date.compareTo(o.getDate())>0) {
            return 1;
        } else if (date.compareTo(o.getDate())<0) {
            return -1;
        } else {
            return 0;
        }
    }
}
