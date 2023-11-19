/*
 * ServletPermissionProvider.java
 *
 * Created on 06.03.2003 13:22:34
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.PropertiesLoader;
import de.marketmaker.istar.domain.profile.PermissionProvider;
import de.marketmaker.istar.domain.profile.PermissionType;

/**
 * Provides permissions based on a resource file.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ResourcePermissionProvider implements PermissionProvider {
    private static final String RESOURCE_FILE = System.getProperty("istar.profiles",
            "/de/marketmaker/istar/domain/resources/resource-profiles.properties");

    private static final Map<String, PermissionProvider> INSTANCES = new HashMap<>();

    private static Properties s_resources;

    static {
        loadResources(ResourcePermissionProvider.class.getResourceAsStream(RESOURCE_FILE));
    }

    private static void loadResources(InputStream in) {
        try {
            s_resources = PropertiesLoader.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final Set<String> profileNames = new HashSet<>();
        for (final Object o : s_resources.keySet()) {
            final String key = o.toString();
            profileNames.add(key.substring(0, o.toString().lastIndexOf('.')));
        }

        for (final String name : profileNames) {
            final ResourcePermissionProvider pp = new ResourcePermissionProvider(name);
            INSTANCES.put(name, pp);
        }
    }

    private final String prefix;

    public static PermissionProvider getInstance(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }

        return INSTANCES.get(key);
    }

    static Set<String> getInstanceKeys() {
        return INSTANCES.keySet();
    }

    private ResourcePermissionProvider(String key) {
        this.prefix = key.endsWith(".") ? key : key + ".";
    }

    @Override
    public String getName() {
        return this.prefix + "resource";
    }

    public String getResourceKey() {
        return this.prefix.substring(0, this.prefix.length() - 1);
    }

    private String getProperty(String key) {
        return s_resources.getProperty(this.prefix + key);
    }

    @Override
    public Collection<String> getPermissions(PermissionType type) {
        final String property = getProperty(type.name());
        if (property == null) {
            return Collections.emptySet();
        }
        return StringUtils.commaDelimitedListToSet(property.trim());
    }
}
