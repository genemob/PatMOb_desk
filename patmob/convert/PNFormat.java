package patmob.convert;

import patmob.data.PatentDocument;

/*
The format of PCT (WO) publication numbers 
    - from 1978 until 30/06/2002 was WOYYnnnnn (2 year digits and 5 numerics)
    - from 1/7/2002 until 31/12/2003 and was WOYYnnnnnn (2 years and 6 numerics)
    - since 1/1/2004 is WOYYYYnnnnnn (4 year digits and 6 numerics).

For certain countries, if you know the kind code, we recommend adding it
to the end of the number in order to retrieve the document you want,
eg ES1005422U. These countries are :
Austria (AT), Chile (CL), China (CN), Germany (DE), Denmark (DK), Spain (ES),
Finland (FI), Japan (JP), Korea (KR), The Netherlands (NL), Norway (NO),
Poland (PL), Romenia (RO), Sweden (SE), Turkey (TR), Taiwan (TW),
and Serbia and Montenegro (YU). 
    (From espacenet help)
 */

/**
 * Convert patent number formats used by different authorities.
 * @author piotr
 */
public class PNFormat {
    public static final int PATMOB = 1,  //for DB - original office format
                            USPTO  = 2,
                            EPO    = 3;

    public static String getPN(PatentDocument patent, int format) {
        String pn = patent.getNumber();

        switch(format) {
            case PATMOB:
                pn = getPatmobFormat(patent.getCountry(), pn);
                break;
            case USPTO:
                pn = getUSPTOFormat(patent.getCountry(), pn);
                break;
            case EPO:
                pn = getEPOFormat(patent.getCountry(), pn);
        }

        return pn;
    }

    private static String getPatmobFormat(String country, String pn) {
        if (country.equals("US") && pn.length()==10)
            pn = pn.substring(0, 4) + "0" + pn.substring(4);

        else if (country.equals("WO") && (pn.startsWith("1") ||
                (pn.startsWith("0") && Integer.parseInt(pn.substring(1, 2))>=4)))
            pn = "20" + pn;

        return pn;
    }

    private static String getUSPTOFormat(String country, String pn) {
        if (country.equals("US") && pn.length()==10)
            pn = pn.substring(0, 4) + "0" + pn.substring(4);

        return pn;
    }

    private static String getEPOFormat(String country, String pn) {
        if (country.equals("US") && pn.length()==11)
            pn = pn.substring(0, 4) + pn.substring(5);

        else if (country.equals("WO") && (pn.startsWith("1") ||
                (pn.startsWith("0") && Integer.parseInt(pn.substring(1, 2))>=4)))
            pn = "20" + pn;

        return pn;
    }

    public static void main(String[] args) {
        PatentDocument p1 = new PatentDocument("US2009312338 A1");
        PatentDocument p2 = new PatentDocument("US20080171014 A1");

        System.out.println("US: " + getPN(p1, PATMOB) + "; " + getPN(p2, PATMOB));
        System.out.println("EP: " + getPN(p1, EPO) + "; " + getPN(p2, EPO));
    }
}
