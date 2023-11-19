/*
 * VwdEntProfileProviderFile.java
 *
 * Created on 09.02.2010 11:59:11
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.profile;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.DirectoryResource;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.domainimpl.profile.ProfileRequest;
import de.marketmaker.istar.domainimpl.profile.ProfileResponse;
import de.marketmaker.istar.domainimpl.profile.VwdProfile;
import de.marketmaker.istar.domainimpl.profile.VwdProfileFactory;

/**
 * Reads vwd-ent profiles from files. Intended to be used as a delegate of
 * {@link de.marketmaker.istar.merger.provider.profile.VwdEntProfileProvider} so that test-profiles
 * can be provided easily.
 * @author oflege
 */
public class VwdEntProfileProviderFile implements InitializingBean, PropertyChangeListener {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final VwdProfileFactory profileFactory = new VwdProfileFactory();

    private ActiveMonitor monitor;

    private File inputDirectory;

    private final Set<String> filenames = new ConcurrentSkipListSet<>();

    private int timeToLive = 60;

    private static final String FILE_SUFFIX = ".xml";

    public void setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
    }

    public void setMonitor(ActiveMonitor monitor) {
        this.monitor = monitor;
    }

    public void setInputDirectory(File inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public void afterPropertiesSet() throws Exception {
        final DirectoryResource resource
                = new DirectoryResource(this.inputDirectory.getAbsolutePath(), new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(FILE_SUFFIX);
            }
        }, false);
        resource.addPropertyChangeListener(this);
        addFilenames(resource.getFiles());
        if (this.monitor != null) {
            this.monitor.addResource(resource);
        }
    }

    ProfileResponse getProfile(ProfileRequest request, VwdEntAccessMethod method, String appId) {
        final String name = getFilename(appId, request, method.isAddClientId());
        if (this.filenames.contains(name)) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getProfile> reading from file: " + name);
            }
            return readProfileFromFile(name, appId);
        }
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<getProfile> not in a file: " + name);
        }
        return null;
    }

    private String getFilename(String appId, ProfileRequest request, boolean addClientId) {
        final StringBuilder sb = new StringBuilder(40);
        sb.append(appId).append("_");
        if (addClientId) {
            sb.append(request.getClientId()).append("_");
        }
        return sb.append(request.getAuthentication()).append(FILE_SUFFIX).toString();
    }

    private ProfileResponse readProfileFromFile(String name, String appId) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File(this.inputDirectory, name));
            final VwdProfile vwdProfile = this.profileFactory.read(fis);
            vwdProfile.setAppId(appId);
            final ProfileResponse result = new ProfileResponse(vwdProfile);
            result.setTimeToLive(this.timeToLive);
            return result;
        } catch (Exception e) {
            this.logger.warn("<readProfileFromFile> failed for '" + name + "'", e);
            return null;
        } finally {
            IoUtils.close(fis);
        }
    }

    private void addFilenames(Collection<File> files) {
        for (File file : files) {
            this.filenames.add(file.getName());
            this.logger.info("<addFilenames> added " + file.getName());
        }
    }

    private void removeFilenames(Collection<File> files) {
        for (File file : files) {
            this.filenames.remove(file.getName());
            this.logger.info("<removeFilenames> removed " + file.getName());
        }
    }

    @SuppressWarnings({"unchecked"})
    public void propertyChange(PropertyChangeEvent evt) {
        if (DirectoryResource.ADDED.equals(evt.getPropertyName())) {
            addFilenames((Collection<File>) evt.getNewValue());
        } else if (DirectoryResource.REMOVED.equals(evt.getPropertyName())) {
            removeFilenames((Collection<File>) evt.getNewValue());
        }
    }

}
