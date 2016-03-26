package patmob.core;    // +JD 7-28-2012

import java.net.ProxySelector;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import patmob.util.PatmobProxySelector;
import patmob.util.TreeIconsMap;

/**
 * The main class launching the application, using Swing Application Framework.
 */
class App extends SingleFrameApplication {
    PatmobView patmobView;
    Controller patmobController;

    /**
     * At startup create and show the main frame of the application 
     * (patmob.core.PatmobView), instantiate Controller and set up
     * PatmobProxySelector.
     */
    @Override 
    protected void startup() {
        
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            System.out.println("STARTUP NIMBUS: " + ex);
        }        
        
        patmobView = new PatmobView(this);
        show(patmobView);
        patmobController = new Controller(patmobView);
        patmobView.setController(patmobController);

//        PatmobProxySelector sps = new PatmobProxySelector(
//                patmobController.getPatmobProperty("patmobProxy"));
//        ProxySelector.setDefault(sps);
        new TreeIconsMap();
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of App
     */
    public static App getApplication() {
        return Application.getInstance(App.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(App.class, args);
    }
}