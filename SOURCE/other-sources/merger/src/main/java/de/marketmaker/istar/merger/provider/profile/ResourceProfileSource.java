/*
 * ResourceProfileSource.java
 *
 * Created on 04.02.2008 14:03:07
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.profile;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.PropertiesLoader;
import de.marketmaker.istar.domain.profile.PermissionProvider;
import de.marketmaker.istar.domain.profile.PermissionType;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.domainimpl.profile.ProfileProvider;
import de.marketmaker.istar.domainimpl.profile.ProfileRequest;
import de.marketmaker.istar.domainimpl.profile.ProfileResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * ProfileProvider that creates PermissionProvider objects from a properties resource.
 * An active monitor can be used to monitor the resource (if a FileResource or a ClasspathResource
 * located outside a jar file is used). The default settings use the same classpath resource
 * that the {@link de.marketmaker.istar.domainimpl.profile.ResourcePermissionProvider} uses, note
 * that this cannot be monitored!
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class ResourceProfileSource implements ProfileProvider, InitializingBean {
    private static class MyPermissionProvider implements PermissionProvider {
        private final String name;

        private final Properties properties;

        private MyPermissionProvider(String name, Properties properties) {
            this.name = name;
            this.properties = properties;
        }

        /**
         * MUST NOT be changed, external components (reporting) depend on matching profile names
         * with configuration parameters
         * @return name
         */
        public String getName() {
            return "resource:" + this.name;
        }

        public Collection<String> getPermissions(PermissionType type) {
            final String property = (String) this.properties.get(this.name + "." + type.name());
            //noinspection unchecked
            return StringUtils.commaDelimitedListToSet(property);
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ActiveMonitor activeMonitor;

    private Map<String, PermissionProvider> providers = Collections.emptyMap();

    private Resource resource = new ClassPathResource("de/marketmaker/istar/domain/resources/resource-profiles.properties");

    public void afterPropertiesSet() throws Exception {
        loadProviders();
        if (activeMonitor != null) {
            final File file = this.resource.getFile();
            final FileResource fileResource = new FileResource(file);
            this.activeMonitor.addResource(fileResource);
            fileResource.addPropertyChangeListener(evt -> loadProviders());
            this.logger.info("<afterPropertiesSet> registered with ActiveMonitor: " + file.getAbsolutePath());
        }
    }

    public ProfileResponse getProfile(ProfileRequest request) {
        final Profile profile = getProfile(request.getAuthentication());
        return profile != null ? new ProfileResponse(profile) : ProfileResponse.invalid();
    }

    private Profile getProfile(String key) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<getProfile> looking for " + key);
        }
        final PermissionProvider pp = getProvider(key);
        if (pp == null) {
            return null;
        }
        Profile profile = ProfileFactory.createInstance(pp);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<getProfile> returning profile: " + profile);
        }
        return profile;
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "authentication", description = "like bluewin")
    })
    public String getResourceProfileStr(String authentication) {
        return getProfile(authentication).toString();
    }

    public Profile getProfile(String authenticationType, String authentication) {
        if ("resource".equals(authenticationType)) {
            return getProfile(authentication);
        }
        return null;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    private synchronized PermissionProvider getProvider(String key) {
        return this.providers.get(key);
    }

    @ManagedOperation
    public void loadProviders() {
        loadProviders_Impl();
    }

    @ManagedOperation
    public Set<String> loadProviders_Impl() {

        final Set<String> profileNames = new TreeSet<>();
        try {
            final Properties p = PropertiesLoader.load(this.resource);

            for (final Object o : p.keySet()) {
                final String key = o.toString();
                profileNames.add(key.substring(0, key.lastIndexOf('.')));
            }

            Map<String, PermissionProvider> tmp = new HashMap<>();
            for (final String name : profileNames) {
                final MyPermissionProvider pp = new MyPermissionProvider(name, p);
                tmp.put(name, pp);
            }

            synchronized (this) {
                this.providers = tmp;
            }
            this.logger.info("<loadProviders> succeeded for " + profileNames);

        } catch (IOException e) {
            this.logger.error("<loadProviders> failed", e);
        }
        return profileNames;
    }
}
