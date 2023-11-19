/*
 * RatingSystemProvider.java
 *
 * Created on 08.11.2005 16:45:39
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.rating;

import de.marketmaker.istar.common.lifecycle.Initializable;
import de.marketmaker.istar.common.util.PropertiesLoader;
import de.marketmaker.istar.domain.rating.RatingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RatingSystemProviderImpl implements RatingSystemProvider, Initializable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Resource resource;

    private final Map<String, RatingSystemImpl> systems = new HashMap<>();

    public RatingSystemProviderImpl() {
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public void initialize() throws Exception {
        final Properties prop = PropertiesLoader.load(this.resource);

        final String[] systems = prop.getProperty("systems").split(" ");
        for (final String system : systems) {
            final RatingSystemImpl ratingSystem = new RatingSystemImpl(system,
                    prop.getProperty(system + ".regex"));
            this.systems.put(system, ratingSystem);
        }

        for (String propName : prop.stringPropertyNames()) {
            if (!propName.startsWith("rating.")) {
                continue;
            }
            final String[] parts = propName.split("\\.");
            if (!this.systems.containsKey(parts[1])) { // rating system name
                this.logger.warn("<initialize> no rating system declared: {}", parts[1]);
            }
            else if (parts.length != 3) {
                this.logger.warn("<initialize> invalid rating spec: {}={}", propName,
                        prop.getProperty(propName));
            }
            else {
                this.systems.get(parts[1]).add(parts[2], Integer.parseInt(prop.getProperty(propName)));
            }
        }

        for (String propName : prop.stringPropertyNames()) {
            if (propName.startsWith("alias.")) {
                final String[] parts = propName.split("\\.");
                if (this.systems.containsKey(parts[1])) {
                    this.logger.warn("<initialize> alias already declared: {}", parts[1]);
                }
                else if (parts.length != 2) {
                    this.logger.warn("<initialize> invalid alias: {}={}", propName, prop.getProperty(propName));
                }
                else {
                    this.systems.put(parts[1], this.systems.get(prop.getProperty(propName)));
                }
            }
        }

        this.logger.info("<initialize> systems: " + this.systems);
    }

    public RatingSystem getRatingSystem(String name) {
        return this.systems.get(name);
    }

    public static void main(String[] args) throws Exception {
        final RatingSystemProviderImpl provider = new RatingSystemProviderImpl();
        provider.setResource(new FileSystemResource(args[0]));
        provider.initialize();
    }
}
