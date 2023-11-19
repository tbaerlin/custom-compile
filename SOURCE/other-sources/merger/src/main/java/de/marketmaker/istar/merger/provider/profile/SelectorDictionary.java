/*
 * PermissionGuard.java
 *
 * Created on 30.12.13 13:28
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.profile;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.ArraysUtil;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.instrument.QuoteDp2;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.InternalFailure;
import de.marketmaker.istar.merger.web.PermissionDeniedException;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategy;

/**
 * @author oflege
 */
public class SelectorDictionary implements InitializingBean {

    interface Filter {
        boolean appliesTo(Object o);
    }

    static class Entry {
        private final String entitlement;

        private final Profile.Aspect aspect;

        private final int[] vwdFieldInstanceIds;

        private final Filter filter;

        Entry(int entitlement, Profile.Aspect aspect, int[] vwdFieldInstanceIds,
                Filter filter) {
            this.entitlement = Integer.toString(entitlement);
            this.aspect = aspect;
            this.vwdFieldInstanceIds = vwdFieldInstanceIds;
            this.filter = filter;
        }

        public boolean isDefinedForField(int vwdFieldInstanceId) {
            return ArraysUtil.contains(this.vwdFieldInstanceIds, vwdFieldInstanceId);
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "entitlement=" + entitlement +
                    ", vwdFieldInstanceIds=" + Arrays.toString(vwdFieldInstanceIds) +
                    ", filter=" + filter +
                    '}';
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Entry[][] entriesByContext = null;

    private File inputFile;

    private ActiveMonitor activeMonitor;

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        loadDictionary();
        if (this.entriesByContext == null) {
            throw new IllegalStateException("failed to load selector dictionary");
        }

        if (activeMonitor != null) {
            final FileResource fileResource = new FileResource(this.inputFile);
            this.activeMonitor.addResource(fileResource);
            fileResource.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    loadDictionary();
                }
            });
            this.logger.info("<afterPropertiesSet> registered with ActiveMonitor: " + this.inputFile.getAbsolutePath());
        }
    }

    private void loadDictionary() {
        try {
            final Entry[][] tmp = new SelectorDictionaryReader().read(this.inputFile);
            synchronized (this) {
                this.entriesByContext = tmp;
            }
        } catch (Exception e) {
            this.logger.error("<loadDictionary> failed", e);
        }
    }

    public void checkPermission(Permission p, Quote q) {
        Entry[] entries = getEntries(p);
        if (entries == null) {
            if (!p.allowIfNoFilterMatches) {
                this.logger.error("<checkPermission> no rules for permission " + p);
                throw new InternalFailure("undefined permissions");
            }
            return;
        }

        Set<String> missingEntitlements = null;
        Profile profile = RequestContextHolder.getRequestContext().getProfile();
        for (Entry entry : entries) {
            if (entry.isDefinedForField(p.vwdFieldInstanceId) && entry.filter.appliesTo(q)) {
                if (profile.isAllowed(entry.aspect, entry.entitlement)) {
                    return;
                }
                if (missingEntitlements == null) {
                    missingEntitlements = new HashSet<>();
                }
                missingEntitlements.add(entry.entitlement);
            }
        }

        if (missingEntitlements != null) {
            throw new PermissionDeniedException("missing selector: " + missingEntitlements);
        }
        if (!p.allowIfNoFilterMatches) {
            final String msg = "no rule allows " + p + " for " + q;
            this.logger.error("<checkPermission> " + msg);
            throw new PermissionDeniedException(msg);
        }
    }

    private Entry[] getEntries(Permission p) {
        synchronized (this) {
            return entriesByContext[p.vwdContextId];
        }
    }

    public static void main(String[] args) throws Exception {
        File f = new File(args[0]);
        SelectorDictionary sd = new SelectorDictionary();
        sd.setInputFile(f);
        sd.afterPropertiesSet();
        System.out.println(Arrays.deepToString(sd.entriesByContext));

        RequestContextHolder.setRequestContext(new RequestContext(ProfileFactory.valueOf(false), MarketStrategy.STANDARD));

        QuoteDp2 q = new QuoteDp2(1);
        q.setSymbol(KeysystemEnum.VWDFEED, "6.FOO.FTSE");
        sd.checkPermission(Permission.INDEX_COMPOSITION, q);
    }
}
