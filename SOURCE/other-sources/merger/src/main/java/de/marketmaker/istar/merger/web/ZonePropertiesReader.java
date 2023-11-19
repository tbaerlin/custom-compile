package de.marketmaker.istar.merger.web;

import com.netflix.servo.util.Strings;
import de.marketmaker.istar.common.util.PropertiesLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * Loads the properties for zone definitions.
 * Can read them either in a running web application or from the filesystem directly.
 * Depending on which of those use cases you want you need to use the appropriate 
 * constructor to set the web app context or the filesystem information.
 */
public class ZonePropertiesReader {

    private final Log logger = LogFactory.getLog(getClass());

    // These two fields have to be set in order to read the zone properties in a web app.
    private Resource zonePropertiesResource;
    private ApplicationContext appContext;

    // These two fields have to be set to read the zone properties from the filesystem.
    private String baseDir;
    private String zonePropertiesFile;

    public ZonePropertiesReader(ApplicationContext appContext, Resource zoneProperties){
        this.appContext = appContext;
        this.zonePropertiesResource = zoneProperties;
    }
    
    public ZonePropertiesReader(String baseDir, String zonesProperties) {
        this.baseDir = baseDir;
        this.zonePropertiesFile = zonesProperties;
    }

    public Map<String, Properties> loadZones() {
        Resource zoneSpec = appContext != null? zonePropertiesResource : getFilesystemResource(baseDir, zonePropertiesFile);
        final Properties zones = loadProperties(zoneSpec);
        if (zones == null) {
            return new HashMap<>();
        }

        final Map<String, Properties> input = loadZoneProperties(baseDir, zones);
        if (input == null) {
            this.logger.error("<loadZones> failed to load all properties from " + zoneSpec.getFilename());
            return new HashMap<>();
        }

        return input;
    }

    /**
     * Given the properties that lists the available zones load them all with their names into a map.
     * @param baseDir The base directory relative to which the zone properties files are defined.
     * @param zoneList The list of zone files as read from the zones properties.
     * @return A map of zone name to zone properties.
     */
    private Map<String, Properties> loadZoneProperties(String baseDir, Properties zoneList) {
        final HashMap<String, Properties> zones = new HashMap<>();
        for (Map.Entry<Object, Object> entry : zoneList.entrySet()) {
            final String key = (String) entry.getKey();
            final String res = (String) entry.getValue();

            final Resource r = getResource(res);
            if (!r.exists()) {
                this.logger.error("<loadZoneProperties> invalid resource for zone " + key + ": " + r);
                continue;
            }
            final Properties p = loadProperties(r);
            if (p == null) {
                continue;
            }
            zones.put(key, p);
        }
        
        return zones.size() == zoneList.size() ? resolveExtensions(zones) : null;
    }

    /**
     * Load the propeties defined in a resource.
     * @param resource The resource describing the the properites to load.
     * @return The loaded properties or null.
     */
    private Properties loadProperties(Resource resource) {
        try {
            return PropertiesLoader.load(resource);
        } catch (IOException e) {
            this.logger.error("<loadProperties> failed", e);
            return null;
        }
    }

    /**
     * Resolve a resource by name dependent on the way the ZonePropertiesReader was initialized.
     * @param resourceName The name of the resource.
     * @return A Resource either in the filesystem or the webapplication context.
     */
    private Resource getResource(String resourceName) {
        if (appContext != null) {
            return getWebappResource(appContext, resourceName);
        } else {
            return getFilesystemResource(baseDir, resourceName);
        }
    }

    /**
     * Get a Resource in the filesystem.
     * @param baseDir The base directory of the resource.
     * @param resourceName The name of the resource. Can contain a path too.
     * @return The Resource.
     */
    private Resource getFilesystemResource(String baseDir, String resourceName) {
        return new FileSystemResource(Paths.get(baseDir, resourceName).toString());
    }

    /**
     * Get a Resouce from the application context.
     * @param appContext The application context of a web app.
     * @param res The Name of the resource.
     * @return The Resource.
     */
    private Resource getWebappResource(ApplicationContext appContext, String res) {
        if (appContext instanceof WebApplicationContext) {
            final WebApplicationContext context = (WebApplicationContext) appContext;
            return context.getResource(res);
        }
        return new DefaultResourceLoader().getResource(res);
    }

    /**
     * Since zones can extend other zones we need to merge the extended properties with
     * the extending ones. 
     * 
     * This code does not change the original map of zones but creates a new one.
     * 
     * In case zones try to extend zones that do not exists they will not be included
     * in the result map.
     * 
     * @param zones The map of zones that needs extensions resolved.
     * @return A new map with Properties that have been merged with those they extend.
     */
    private Map<String, Properties> resolveExtensions(Map<String, Properties> zones) {
        Map<String, Properties> resolvedZones = new HashMap<>();

        Set<String> zoneNames = zones.keySet();
        
        while (!zoneNames.isEmpty()) {

            int numRemoved = 0;

            Set<String> names = new HashSet<>();
            names.addAll(zoneNames);
            for (String zoneName : names) {
                final Properties zoneProperties = zones.get(zoneName);

                String parentName = zoneProperties.getProperty("extends");

                if (parentName != null) {
                    Properties parentProperties = resolvedZones.get(parentName);
                    if (parentProperties != null) {
                        Properties resolvedProperties = new Properties();
                        for (String key : parentProperties.stringPropertyNames()) {
                            resolvedProperties.setProperty(key, parentProperties.getProperty(key));
                        }
                        for (String key : zoneProperties.stringPropertyNames()) {
                            if (!"extends".equals(key)) {
                                resolvedProperties.setProperty(key, zoneProperties.getProperty(key));
                            }
                        }
                        resolvedZones.put(zoneName, resolvedProperties);

                        zoneNames.remove(zoneName);
                        numRemoved++;
                    }

                } else {
                    resolvedZones.put(zoneName, zoneProperties);
                    zoneNames.remove(zoneName);
                    numRemoved++;
                }
            }
            
            if (numRemoved == 0) {
                logger.error("Unable to resolve all zone properties. Unresolved are: " + Strings.join(", ", zones.keySet().iterator()));
                return resolvedZones;
            }
        }
        
        return resolvedZones;
    }
}
