/*
 * VwdEntitlementsProviderImpl.java
 *
 * Created on 24.03.2007 15:40:35
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.profile;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.monitor.Resource;
import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.DefaultProfile;
import de.marketmaker.istar.domainimpl.profile.ProfileProvider;
import de.marketmaker.istar.domainimpl.profile.ProfileRequest;
import de.marketmaker.istar.domainimpl.profile.ProfileResponse;

/**
 * Provides profiles based on vwd entitlements read from a file. Profiles in the file are
 * separated by an empty line, each profile specification consists of two lines: the first line
 * contains the user-id, the second one contains a set of vwd entitlements as strings
 * separated by whitespace (e.g., 7A 17G).
 * The component can also parse an update file with entitlements for selected user-ids. The contents
 * of the update file will be merged with the contents of the complete file.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ThreadSafe
public class VwdProfileSource implements InitializingBean, Lifecycle, ProfileProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ActiveMonitor monitor = new ActiveMonitor();

    private File entitlementsFile;

    private File entitlementsUpdateFile;

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);

    @GuardedBy("this.rwl")
    private Map<String, Profile> profiles = new HashMap<>();

    public void setEntitlementsFile(File entitlementsFile) {
        this.entitlementsFile = entitlementsFile;
    }

    public void setEntitlementsUpdateFile(File entitlementsUpdateFile) {
        this.entitlementsUpdateFile = entitlementsUpdateFile;
    }

    public void afterPropertiesSet() throws Exception {
        reloadEntitlements();
        if (this.entitlementsUpdateFile.lastModified() > this.entitlementsFile.lastModified()) {
            updateEntitlements();
        }
    }

    private void reloadEntitlements() {
        readEntitlements(this.entitlementsFile, false);
    }

    private void updateEntitlements() {
        readEntitlements(this.entitlementsUpdateFile, true);
    }

    private void readEntitlements(File f, boolean update) {
        final TimeTaker tt = new TimeTaker();
        Scanner s = null;
        int lineNo = 0;
        try {
            s = new Scanner(f);
            String uid = null;

            final Map<String, Profile> tmp = new HashMap<>();
            while (s.hasNextLine()) {
                final String line = s.nextLine();
                lineNo++;
                if (!StringUtils.hasText(line)) {
                    uid = null;
                } else if (uid == null) {
                    uid = line.trim();
                } else {
                    final Profile p = toProfile(uid, line);
                    tmp.put(uid, p);
                }
            }

            this.rwl.writeLock().lock();
            try {
                if (update) {
                    this.profiles.putAll(tmp);
                } else {
                    this.profiles = tmp;
                }
                this.logger.info("<readEntitlements> update=" + update + ", took " + tt
                        + ", numRead=" + tmp.size() + ", total size=" + this.profiles.size());
            } finally {
                this.rwl.writeLock().unlock();
            }

        }
        catch (Exception e) {
            this.logger.error("<readEntitlements> failed for " + f.getAbsolutePath()
                    + ", line:" + lineNo, e);
        }
        finally {
            if (s != null) {
                s.close();
            }
        }
    }

    private Profile toProfile(String uid, String line) {
        final String[] tokens = line.split("\\s");
        HashSet<String> rt = new HashSet<>();
        HashSet<String> nt = new HashSet<>();
        HashSet<String> invalid = new HashSet<>();
        for (String token : tokens) {
            final int ent = EntitlementsVwd.toValue(token);
            if (ent <= 520) {
                rt.add(token);
            }
            else if (ent <= 1040) {
                nt.add(EntitlementsVwd.toEntitlement(ent - 520));
            }
            else {
                invalid.add(token);
            }
        }
        if (!invalid.isEmpty()) {
            this.logger.warn("<toProfile> invalid selectors for " + uid + ": " + invalid);
        }
        final DefaultProfile.AspectData ad = DefaultProfile.AspectData.create(rt, nt, Collections.<String>emptyList());
        // use the same for prices and news as those selectors don't intersect
        // (and we don't know which ones are which)
        return new DefaultProfile(uid, ad, ad);
    }

    @Override
    public boolean isRunning() {
        return this.monitor.isRunning();
    }

    public void start() {
        this.monitor.setFrequency(30 * 1000);

        final FileResource resource = new FileResource(this.entitlementsFile);
        resource.addPropertyChangeListener(evt -> reloadEntitlements());

        final FileResource updateResource = new FileResource(this.entitlementsUpdateFile);
        updateResource.addPropertyChangeListener(evt -> updateEntitlements());

        this.monitor.setResources(new Resource[]{resource, updateResource});
        this.monitor.start();
    }

    public void stop() {
        this.monitor.stop();
    }

    public ProfileResponse getProfile(ProfileRequest request) {
        this.logger.info("<getProfile> getting profile for " + request);

        final Profile profile = getProfile(request.getAuthentication());
        return profile != null ? new ProfileResponse(profile) : ProfileResponse.invalid();
    }

    private Profile getProfile(String userid) {
        final Profile profile;
        this.rwl.readLock().lock();
        try {
            profile = this.profiles.get(userid);
        } finally {
            this.rwl.readLock().unlock();
        }
        if (profile == null) {
            this.logger.warn("<getProfile> no profile found for '" + userid + "'");
        }
        return profile;
    }

    public static void main(String[] args) throws Exception {
        final VwdProfileSource source = new VwdProfileSource();
        source.setEntitlementsFile(new File("/Users/oflege/tmp/client_selectors.txt"));
        source.setEntitlementsUpdateFile(new File("/Users/oflege/tmp/client_selectors_selected.txt"));
        source.afterPropertiesSet();
    }

}
