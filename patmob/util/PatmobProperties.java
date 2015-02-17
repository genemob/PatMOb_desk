package patmob.util;

import java.io.*;

/**
 * Access to the properties file. Plug-in names, URLs, proxies and user 
 * preferences will be stored in that file
 */
public class PatmobProperties extends java.util.Properties {
    File defDir, propFile;
    BufferedWriter bw;
    BufferedReader br;

    public PatmobProperties (File f) {
        super();
        try {
            defDir = f;
            propFile = new File(defDir + System.getProperty("file.separator") +
                    "patmob.properties");
            try {
                br = new BufferedReader(new FileReader(propFile));
                load(br);
                br.close();
            } catch (FileNotFoundException x) {
                //first run - create prop file
                saveFile();
            }
        } catch (Exception ex) {System.out.println("PatmobProperties: " + ex);}
    }

    public final void saveFile(){
        try {
            bw = new BufferedWriter(new FileWriter(propFile));
            store(bw, "");
            bw.close();
        } catch (Exception x) {System.out.println("PatmobProperties.saveFile: " + x);}
    }
}