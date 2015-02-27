package patmob.data.ops;

import javax.swing.JOptionPane;
import patmob.core.PatmobPlugin;
import patmob.data.ops.impl.RegisterRequest;

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
        String userPN = JOptionPane.showInputDialog("EP PN for opposition data:");
        RegisterRequest rr = new RegisterRequest(userPN);
        rr.submit();
    }
    
}
