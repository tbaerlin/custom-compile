package de.marketmaker.istar.merger.web.easytrade.moleculestatistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * This class reads a csv for lookup up appids to app names and holds them for future lookup.
 */
public class AppidLookup {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * This class is used to group app names and app short names.
     */

    public static class AppName {
        public String name;
        public String shortName;
    }
    
    private final Map<String, AppName> lookupMap;
    private final AppName defaultAppName;

    /**
     * Read the lookup file and build a map from it.
     * @param lookupFile The csv file containing appid, shortName, name
     */
    public AppidLookup(String lookupFile) {
        lookupMap = new HashMap<>();

        defaultAppName = new AppName();
        defaultAppName.name = "n/a";
        defaultAppName.shortName = "n/a";

        try {
            InputStream is = new FileInputStream(new File(lookupFile));
            int lineNumber = 0;
            try (Scanner sc = new Scanner(is, "utf8")) {
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    lineNumber++;

                    if (line.isEmpty() || lineNumber == 1) {
                        continue;
                    }
                    
                    String[] parts = line.split(",");
                    String appid = parts[0];
                    AppName appName = new AppName();
                    appName.shortName = parts[1];
                    appName.name = parts[2];
                    
                    lookupMap.put(appid, appName);
                }
            }

        } catch (Exception e) {
            logger.error("Can't read appid lookup file <" + lookupFile + "> due to error:", e);
        }
        
    }

    /**
     * Get the names for one id.
     * @param id The id to look up. It can be either appid or vwdid.
     * @return
     */
    public AppName getAppName(String id) {
        String lookupId = id; 
        if (id != null && id.contains(":")) {
            lookupId = id.substring(0, id.indexOf(":"));
        }
        return lookupMap.getOrDefault(lookupId, defaultAppName);
    }

    /**
     * Try to find the short name for the given vwdid.
     * @param vwdId The vwdId to look up
     * @return The short name of the customer or the vwdId if no name was found.
     */
    public String shortNameOrId(String vwdId) {
        String name = getAppName(vwdId).shortName;
        if ("n/a".equals(name)) {
            name = vwdId;
        }
        return name;
    }

    /**
     * Try to find the name for the given vwdid.
     * @param vwdId The vwdId to look up
     * @return The name of the customer or the vwdId if no name was found.
     */
    public String nameOrId(String vwdId) {
        String name = getAppName(vwdId).name;
        if ("n/a".equals(name)) {
            name = vwdId;
        }
        return name;
    }
}
