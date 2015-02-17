package patmob.data.inpadoc;

import javax.swing.JOptionPane;
import patmob.core.PatmobPlugin;

/**
 *
 * @author Piotr
 */
public class InpadocTestPlugin implements PatmobPlugin {

    @Override
    public String getName() {
//        return "INPADOC Test";
        return "INPADOC Family Test";
    }

    @Override
    public void doJob() {
        String pn = JOptionPane.showInputDialog(
                "Enter publication number in epodoc format");
        if (pn!=null && !pn.equals("")) {
            /*
            String[] pns = pn.split(" ");
            TimelineCall tc = new TimelineCall();
            System.out.println(tc.getTimelines(pns));
            */
            InpadocFamilyCall famCall = new InpadocFamilyCall();
            InpadocFamily iFam = famCall.getInpadocFamily(pn);

            System.out.println(iFam.printFamily());

            System.out.println(iFam.getRequestPubNumber() + ", error: "
                    + iFam.getErrorMessage());
        }
    }
    
}
