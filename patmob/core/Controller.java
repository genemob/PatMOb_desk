package patmob.core;    // +JD 7-29-2012

import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import patmob.convert.*;
import patmob.data.*;
import patmob.data.ops.OpsRestClient;
import patmob.util.*;

 /**
  * Business logic of the application. The registerPlugins() method installs
  * the available plug-ins, passing them references to the instances of this
  * class and the main application window through the CoreAccess inner class
  * in the PatmobPlugin interface.
  * @author piotr
  */
public class Controller {
    private PatmobDB patmobDB;
    PatmobView view;
    PatmobProperties patmobProperties;
    Map<String, PatmobPlugin> plugins = null;
    boolean connected2ops = false;

    /**
     * In the constructor, initialize application resources: (i) create (if not
     * existing), or get references to, patmob_data directory (in the user's 
     * default starting directory), PatMOb database and properties file;
     * (ii) start the thread used to fetch data from EPO OPS; (iii) register 
     * available plug-ins; (ii) get database content and display it as tree in
     * PatmovView.
     * @param v 
     */
    protected Controller (PatmobView v) {
        //patmob_data directory for properties and database
        File patmobData = null;
        try {
            File defDir = FileSystemView.getFileSystemView().getDefaultDirectory();
            patmobData = new File (defDir, "patmob_data");
            if (!patmobData.exists()) patmobData.mkdir();
            System.getProperties().setProperty("derby.system.home", patmobData.getPath());
        } catch (Exception x) {x.printStackTrace();}

        view = v;
        patmobDB = new DerbyDB();
        patmobProperties = new PatmobProperties(patmobData);
        
        init();
        resetTree(view);
        connectToOps();
    }
    
    private void connectToOps() {
        new Thread (new Runnable() {
            @Override
            public void run() {
                String patmobProxy = getPatmobProperty("patmobProxy"),
                        opsKey = getPatmobProperty("opsKey"),                   //20140705
                        opsSecretKey = getPatmobProperty("opsSecretKey");
//                System.out.println("opsKey>" + opsKey + "<");
//                String opsSecretKey = getPatmobProperty("opsSecretKey");
//                System.out.println("opsSecretKey>" + opsSecretKey + "<");
                
                String opsConnStatus = OpsRestClient.initialize(patmobProxy,
                        opsKey, opsSecretKey),                                  //20140705
                        messageTitle = "OPS Connection Status",
                        userMessage = "Could not connect to OPS.";
                int messageType = JOptionPane.ERROR_MESSAGE;
                if (opsConnStatus.endsWith("200 OK")) {
                    connected2ops = true;
                    userMessage = "Connected to OPS.";
                    messageType = JOptionPane.INFORMATION_MESSAGE;
                }
                JOptionPane.showMessageDialog(null,
                        userMessage + "\n(" + opsConnStatus + ")",
                        messageTitle, messageType);
            }
        }).start();        
    }

    private void init() {
//        runner = new ThreadPoolRunner();
//        new Thread(runner).start();
        
        registerPlugins();
        if (plugins!=null) {
            setMenus(plugins.keySet());
        }
    }

    private void resetTree(PatmobView patmobView) {
        PatmobTree tree = new PatmobTree(getUserContent(0),
                patmobView, this, PatmobTree.MAIN_WINDOW);
        patmobView.setTree(tree);
    }

    /**
     * Returns the node selected by user in the main window tree.
     * BUG? Probably not all levels deep.
     * @return 
     */
    public PatentTreeNode getSelectedNode() {
        return view.getSelectedNode();
    }

    /**
     * Fetches the requested node from the database with two levels of child
     * nodes. This methods is repeatedly called as user unfolds the tree,
     * allowing lazy loading of nodes.
     * @param nodeID
     * @return 
     */
    public PatentTreeNode getUserContent(int nodeID) {
        PatentTreeNode patmobNode = patmobDB.getUserCollections(nodeID);
        if (nodeID==0) patmobNode.setName("DATABASE");
        return patmobNode;
    }

    /**
     * Threads, such as OPSBiblio, can be added to the pool in ThreadPoolRunner.
     * The runner wakes up every 0.5 s to check for threads to execute.
     * @return 
     */
//    public ThreadPoolRunner getThreadPoolRunner() {return runner;}

    /**
     * Saves patent collection to the database.
     * @param collection
     * @return 
     */
    public int saveTreeNode(PatentTreeNode collection) {
        int result = patmobDB.savePatentCollection(collection);
        if (result > -1) {
            resetTree(view);
            view.printMessage(collection.toString() + " has been saved");
        } else view.printMessage("Couldn't save " + collection.toString());
        return result;
    }

    /**
     * Deletes patent collection from the database
     * @param ptn 
     */
    public void deleteNode(PatentTreeNode ptn) {
        if (patmobDB.deletePatentCollection(ptn) > -1) {
            resetTree(view);
            view.printMessage(ptn.toString() + " has been deleted");
        } else view.printMessage("Couldn't delete " + ptn.toString());
    }

    /**
     * Opens the selected patent collection in TreeBranchEditor for editing.
     * @param ptn 
     */
    public void editNode(PatentTreeNode ptn) {
        //since nodes are loaded lazily, check for grandchildren
//        if (ptn.getSortedChildren()!=null && !ptn.isDeep())
//            ptn = getUserContent(ptn.getID());
        PatentTreeNode edPtn = patmobDB.getFullBranch(ptn.getID());
        if (edPtn!=null) {
//            new TreeBranchEditor(edPtn, this).setVisible(true);
            new TreeBranchEditor_2(edPtn, this).setVisible(true);
        }
    }

    /**
     * Opens TreeBrachEditor with a new patent collection.
     */
    public void newCollection() {
//        PatentCollectionList pcl = new PatentCollectionList();
//        new TreeBranchEditor(pcl, this).setVisible(true);
        TreeBranchEditor_2.createNewCollection(this);
    }
    
    /**
     * Asks user to select an XML file with a PatMOb collection, and opens 
     * it in TreeBranchEditor.
     */
    public void loadXML() {
        PatentTreeNode pcn = PatmobXML.collectionForDocument(
                PatmobDesktop.loadDOMFromXMLFile());
        new TreeBranchEditor_2(pcn, this).setVisible(true);
    }

    /**
     * Sets the named property.
     * @param key
     * @param value 
     */
    public void setPatmobProperty(String key, String value) {
        patmobProperties.setProperty(key, value);
    }
    
    /**
     * Gets the named property.
     * @param key
     * @return 
     */
    public String getPatmobProperty(String key) {
        return patmobProperties.getProperty(key);
    }
    
    /**
     * Saves the properties to the file.
     */
    public void savePatmobProperties() {
        patmobProperties.saveFile();
    }

    private void setMenus(Set<String> pluginNames) {
        JMenu patmobMenu = new JMenu ("Plugins");
        Iterator<String> it = pluginNames.iterator();
        while (it.hasNext()) {
            final String pluginName = it.next();
            JMenuItem item = new JMenuItem();
            item.setAction(new AbstractAction(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    plugins.get(pluginName).doJob();
                }
            });
            item.getAction().putValue(Action.NAME, pluginName);
            patmobMenu.add(item);
        }
        view.addMenu(patmobMenu);
    }

    private void registerPlugins() {
        PatmobPlugin.coreAccess.setView(view);
        PatmobPlugin.coreAccess.setController(this);

        ClassLoader cl = this.getClass().getClassLoader();
        java.io.BufferedReader br;
        try {
            
            //register plugins included in PatMOb jar (names in txt file - jar in libraries)
            java.net.URL url = cl.getResource("plugins.txt");
            if (url!=null) {
                plugins = new HashMap<String,PatmobPlugin>();
                br = new java.io.BufferedReader (
                        new java.io.InputStreamReader(url.openStream()));
                String line;
                while ((line=br.readLine()) != null) {
                    registerPlugin(line, cl);
                }
            }
                
            //register "standalone" plugins (name in properties - classpath must be defined!)
            //java -cp ./*:/home/piotr/patmob_data/plugins patmob.core.App
            String pluginNames = getPatmobProperty("plugin.names");
            if (pluginNames!=null) {
                if (plugins==null) plugins = new HashMap<String,PatmobPlugin>();
                StringTokenizer st = new StringTokenizer(pluginNames, ";");
                while (st.hasMoreTokens()) {
                    String pluginName = st.nextToken().trim();
                    registerPlugin(pluginName, cl);
                }
            }

        } catch (Exception x){
            System.out.println("registerPlugins: " + x);
        }
    }
    
    private void registerPlugin(String name, ClassLoader cl) {
        try {
            PatmobPlugin plugin = 
                    (PatmobPlugin) cl.loadClass(name).newInstance();
            plugins.put(plugin.getName(),plugin);
            System.out.println("Found plugin " + plugin.getName());
        } catch (Exception x) {x.printStackTrace();}
    }

    /**
     * Runs a SQL query and returns results as chains of node names.
     * @param fullQ
     * @return 
     */
    public Collection<String> runSQLQuery(String fullQ) {
        return patmobDB.keywordSearch(fullQ);
    }
}
