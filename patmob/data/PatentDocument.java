package patmob.data;

//import patmob.util.ThreadPoolRunner;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import patmob.convert.OPSBiblio;
import patmob.core.TreeNodeInfoDisplayer;
import patmob.util.PatmobDesktop;

/**
 * This class represent an individual patent or application.
 * @author piotr
 */
public class PatentDocument implements PatentTreeNode {
    private OPSBiblio opsBiblio = null;
    private boolean deep = true;
    private int dbID = 0, parentID, hilite = PatentTreeNode.HILITE_NONE;

    protected String country = "", number = "", kindCode = "",
            title = "", pAbstract = "", claims = "",
            applicationNumber = "", priorityNumber = "",
            publicationDate = "", applicationDate = "", priorityDate = "",
            grantedDate = "", assignee = "",
            name = "", description="", familyNumber="";
    private ArrayList<String> applicantList, applicantNSList;       //claimList,
    private String[] claimList = null;
    private ArrayList<PatentTreeNode> children = new ArrayList<PatentTreeNode>();

    /**
     * The no arguments constructor should be always followed by call to 
     * setName(String s), where s in the form of "US20100324050 A1 20101223" 
     * or "US20100324050 A1" or "US20100324050".
     */
    public PatentDocument(){}
    
    //"US20100324050 A1 20101223" or "US20100324050 A1" or "US20100324050"
    public PatentDocument(String s) {
        setParamsFromString(s);
    }
    
    private void setParamsFromString(String s) {
        StringTokenizer st = new StringTokenizer(s);
        String pn = st.nextToken();
        country = pn.substring(0, 2);
        number = pn.substring(2).trim();
        if (st.hasMoreTokens()) {
            kindCode = st.nextToken();
            if (st.hasMoreTokens()) publicationDate = st.nextToken();
        }
    }

    //US20100324050, A1
    public PatentDocument(String pn, String kind) {
        country = pn.substring(0, 2);
        number = pn.substring(2).trim();
        kindCode = kind.trim();
    }

    //US20100324050, A1, 20101223
    public PatentDocument(String pn, String kind, String pubDate) {
        this(pn, kind);
        publicationDate = pubDate.trim();
    }

    public void setPublicationDate(String s) {publicationDate = s;}
    public String getPublicationDate() {return publicationDate;}
    public String getCountry() {return country;}
    
    //20130606 re-format number
    public void setNumber(String s) {number = s;}
            
    public String getNumber() {return number;}
    public String getKindCode() {return kindCode;}
//    public void setAppDate(String s) {appDate = s;}
//    public String getAppDate() {return appDate;}
//    public void setPriDate(String s) {priDate = s;}
//    public String getPriDate() {return priDate;}
    public void setTitle(String s) {title = s;}
    public String getTitle() {return title;}
    public void setAbstract(String a) {pAbstract = a;}
    public String getAbstract() {return pAbstract;}
    public void setClaims(String c) {claims = c;}
    public String getClaims() {return claims;}
    public void setAssignee(String a) {assignee = a;}
    public String getAssignee() {return assignee;}
//    public void setAppNumber(String s) {appNumber = s;}
//    public String getAppNumber() {return appNumber;}
//    public void setPriNumber(String s) {priNumber = s;}
//    public String getPriNumber() {return priNumber;}

    public void setFamilyNumber(String fn) {familyNumber = fn;}
    public String getFamilyNumber() {return familyNumber;}

//    public void addClaim(String claim) {
//        if (claimList==null) claimList = new ArrayList<String>();
//        claimList.add(claim);
//    }
//    public ArrayList<String> getClaimList() {return claimList;}
//    public void setClaimList(String[] clArray) {
//        Collections.addAll(claimList, clArray);
//    }

    public void setClaimList(String[] claims) {claimList = claims;}
    public String[] getClaimList() {return claimList;}

    public void addApplicant(String app) {
        if (applicantList==null) applicantList = new ArrayList<String>();
        applicantList.add(app);
    }
    public ArrayList<String> getApplicantList() {return applicantList;}
    public void addApplicantNS(String appNS) {
        if (applicantNSList==null) applicantNSList = new ArrayList<String>();
        applicantNSList.add(appNS);
    }
    public ArrayList<String> getApplicantNSList() {return applicantNSList;}

    @Override
    public String toString() {
        return (country + number + " " + kindCode + " " + publicationDate).trim();
    }

    public int size() {
//        if (children==null) return 0;
        return children.size();
    }

    public Collection<PatentTreeNode> getSortedChildren() {
        Collections.sort(children);
        return children;
    }

    public PopupMenu getPatentTreePopup(final TreeNodeInfoDisplayer nid) {
        PopupMenu pop = new PopupMenu();
        //menu for patent document
        MenuItem pOfficeItem = new MenuItem("Read at Patent Office");
//                 patBaseItem = new MenuItem("Read at PatBase Express"),
//                 opsClaimsItem = new MenuItem("Get OPS Claims");
        pOfficeItem.addActionListener(new AbstractAction(){
            public void actionPerformed(ActionEvent e) {
                PatmobDesktop.browsePatent(PatentDocument.this,
                        PatmobDesktop.PATOFFICE);
            }
        });
        pop.add(pOfficeItem);
//        patBaseItem.addActionListener(new AbstractAction(){
//            public void actionPerformed(ActionEvent e) {
//                PatmobDesktop.browsePatent(PatentDocument.this,
//                        PatmobDesktop.PATBASE);
//            }
//        });
//        pop.add(patBaseItem);
//        opsClaimsItem.addActionListener(new AbstractAction(){
//            public void actionPerformed(ActionEvent e) {
//                String patNumber = PatentDocument.this.getCountry() +
//                        PatentDocument.this.getNumber();
//                nid.displayText(OPS_SOAPClient.getClaims(patNumber));
//            }
//        });
//        pop.add(opsClaimsItem);
        pop.addSeparator();
        return pop;
    }

    @Override
    public String getInfo() {
        if (opsBiblio==null) {
            return toString() + " Biblio not available";
        }
        else {
            return opsBiblio.getInfo();
        }                                        //toString() + " Biblio:\n====================\n" +
    }

//    public void getBiblio(ThreadPoolRunner runner) {
////        String epoNumber = number;
////        if (country.equals("US") && number.length()==11)
////            epoNumber = number.substring(0,4) + number.substring(5);
//        //4-10-11
//        String pn = PNFormat.getPN(this, PNFormat.EPO);
//        opsBiblio = new OPSBiblio(country, pn, "*", "*", true);
//        runner.addJob(opsBiblio);
//    }
    
    public void setBiblio(OPSBiblio biblio) {
        opsBiblio = biblio;
    }

    public OPSBiblio getBiblio() {
        return opsBiblio;
    }

    public int getType() {
        return PatentTreeNode.PATENT_DOCUMENT;
    }

    public void setID(int id) {
        dbID = id;
    }

    public int getID() {
        return dbID;
    }

    public void setParentID(int pid) {
        parentID = pid;
    }

    public int getParentID() {
        return parentID;
    }

    public void addChild(PatentTreeNode child) {
//        if (children==null) children = new ArrayList<PatentTreeNode>();
        children.add(child);
    }

    public int compareTo(Object o) {
        return toString().compareTo(o.toString());
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<PatentTreeNode> getChildren() {
        return children;
    }

    public void setName(String n) {
        //TODO: cleanup name redundance
        name = n;
        setParamsFromString(n);
    }

    public String getName() {
        return name;
    }
    public void setDescription(String d) {description=d;}
    public String getDescription() {return description;}

    public void removeChildren() {
        children.removeAll(children);
    }

    public void setHilite(int hl) {
        hilite = hl;
    }

    public int getHilite() {
        return hilite;
    }

    public void setDeep(boolean b) {
        deep = b;
    }

    public boolean isDeep() {
        return deep;
    }

}
