package patmob.data.ops;

import javax.swing.JOptionPane;
import patmob.core.PatmobPlugin;
import patmob.data.ops.impl.RegisterRequest;
import patmob.data.ops.impl.RegisterRequestParams;

/**
 *
 * @author Piotr
 */
public class OpsRequestTestPlugin implements PatmobPlugin {

    @Override
    public String getName() {
        return "OPS Request Test Plugin";
    }

    @Override
    public void doJob() {
//        String userPN = JOptionPane.showInputDialog("EP PN for opposition data:");
        String[] pubNums = new String[]{"EP0921117","EP1355673","EP1920794","EP1324776"};
        RegisterRequestParams searchParams = new RegisterRequestParams(pubNums);
        RegisterRequest rr = new RegisterRequest(searchParams);
        rr.submit();
    }
    
}
