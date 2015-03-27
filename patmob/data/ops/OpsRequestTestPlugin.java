package patmob.data.ops;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import patmob.core.PatmobPlugin;
import patmob.data.ops.impl.RegisterRequest;
import patmob.data.ops.impl.register.RegisterRequestParams;

/**
 *  ***DELETE***
 * @author Piotr
 */
public class OpsRequestTestPlugin implements PatmobPlugin {

    @Override
    public String getName() {
        return "OPS Request Test Plugin";
    }

    @Override
    public void doJob() {
        JOptionPane.showMessageDialog(null, "Plug-in used for testing.");
        //TODO: need GUI to determine search/biblio request
//        submitBiblioRequest();
//        submitSearchRequest();
        
    }
    
//    private void submitSearchRequest() {
//        RegisterRequestParams searchParams = 
//                new RegisterRequestParams("(pa = sanofi and pn = ep) and pd >= 2009");
//        RegisterRequest rr = new RegisterRequest(searchParams);
//        rr.submit();
//    }
    
//    private void submitBiblioRequest() {
//        ArrayList<String> patentList = new ArrayList<>();
//        //read list of PNs from user-selected text file
//        JFileChooser fc = new JFileChooser();
//        int i = fc.showOpenDialog(null);
//        if (i==JFileChooser.APPROVE_OPTION) {
//            File patentListFile = fc.getSelectedFile();
//            try {
//                try (BufferedReader br = new BufferedReader(
//                        new FileReader(patentListFile))) {
//                    String line;
//                    while ((line=br.readLine())!=null) {
//                        if (line.contains(" ")) {
//                            //skip kind code etc,
//                            patentList.add(line.substring(0, line.indexOf(" ")));
//                        } else {
//                            patentList.add(line);
//                        }
//                    }
//                }
//            } catch (Exception x) {
//                System.out.println("OpsRequestTestPlugin: " + x);
//            }
//        }
//        String[] pubNums = patentList.toArray(new String[0]);
//        RegisterRequestParams searchParams = new RegisterRequestParams(pubNums);
//        RegisterRequest rr = new RegisterRequest(searchParams);
//        rr.submit();
//    }
    
}
