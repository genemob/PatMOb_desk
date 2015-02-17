package patmob.core;

/**
 * All patmob plugins have to implement this interface. The inner class 
 * PatmobAccess provides access to the application methods.
 * @author piotr
 */
public interface PatmobPlugin {
    /**
     * Access to the core - the registerPlugins() method in the Controller
     * sets the variable values.
     */
    class CoreAccess {
        private Controller controller = null;
        private PatmobView view = null;
        public Controller getController() { return controller; }
        public PatmobView getView() { return view; }
        public void setController(Controller c) { controller = c; }
        public void setView(PatmobView v) { view = v; }
    }
    CoreAccess coreAccess = new CoreAccess();

    /**
     * The name appearing in the Plugin menu, and used as the key in the plugin
     * HashMap of GenemobClient.
     */
    public String getName();

    /**
     * Called when user selects the corresponding menu item.
     */
    public void doJob();
    
}
