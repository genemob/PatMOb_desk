package patmob.data.inpadoc;

import java.util.ArrayList;

/**
 *
 * @author Piotr
 */
public class PatentApplication extends PatentEntity {
    ArrayList<PatentEntity> publications = null, 
            priorities = null;

    public PatentApplication(String s) {
        super(s);
    }

    public PatentApplication(String s1, String s2, String s3, String s4) {
        super(s1,s2,s3,s4);
    }

    public ArrayList<PatentEntity> getPublications() {return publications;}
    public ArrayList<PatentEntity> getPriorities() {return priorities;}

    public void addPublication(String pubString) {
        if (publications==null) {
            publications = new ArrayList<PatentEntity>();
        }
        publications.add(new PatentEntity(pubString));
    }

    public void addPriority(String priString) {
        if (priorities==null) {
            priorities = new ArrayList<PatentEntity>();
        }
        priorities.add(new PatentEntity(priString));
    }
}
