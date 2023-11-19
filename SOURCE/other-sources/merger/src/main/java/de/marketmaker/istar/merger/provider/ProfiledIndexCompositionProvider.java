package de.marketmaker.istar.merger.provider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.common.util.PropertiesLoader;
import de.marketmaker.istar.domain.data.NamedIdSet;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.data.IndexComposition;
import de.marketmaker.istar.instrument.IndexCompositionProvider;
import de.marketmaker.istar.instrument.IndexCompositionRequest;
import de.marketmaker.istar.instrument.IndexCompositionResponse;
import de.marketmaker.istar.instrument.IndexMembershipRequest;
import de.marketmaker.istar.instrument.IndexMembershipResponse;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.PermissionDeniedException;

/**
 * Implements {@link de.marketmaker.istar.instrument.IndexCompositionProvider}
 * and returns the results of the requested methods from cache if they are present,
 * else the results of corresponding methods of the delegate will be stored into the cache and returns.
 */
public class ProfiledIndexCompositionProvider implements IndexCompositionProvider, InitializingBean {

    private final Map<Long, Long> iidToBenchmarkIidCache;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private IndexCompositionProvider delegate;

    private InstrumentBenchmarkProvider benchmarkProvider;

    private File constituentsGroupPermissionsFile;

    private ActiveMonitor activeMonitor;

    private volatile Properties constituentsGroupPermissions;

    private Ehcache indexCache;

    public ProfiledIndexCompositionProvider() {
        this(4096);
    }

    public ProfiledIndexCompositionProvider(final int iidToBenchmarkIidCacheSize) {
        this.iidToBenchmarkIidCache = Collections.synchronizedMap(
                new LinkedHashMap<Long, Long>(iidToBenchmarkIidCacheSize * 4 / 3 + 1, 0.75f, true) {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry eldest) {
                        return size() > iidToBenchmarkIidCacheSize;
                    }
                }
        );
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setConstituentsGroupPermissionsFile(File constituentsGroupPermissionsFile) {
        this.constituentsGroupPermissionsFile = constituentsGroupPermissionsFile;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.benchmarkProvider = new InstrumentBenchmarkProvider(this);

        readConstituentsGroupPermissions();

        if (this.activeMonitor != null) {
            final FileResource resource = new FileResource(this.constituentsGroupPermissionsFile);
            resource.addPropertyChangeListener(evt -> readConstituentsGroupPermissions());
            this.activeMonitor.addResource(resource);
        }
    }

    private void readConstituentsGroupPermissions() {
        try {
            this.constituentsGroupPermissions = PropertiesLoader.load(this.constituentsGroupPermissionsFile);
        } catch (IOException e) {
            if (this.constituentsGroupPermissions == null) {
                throw new RuntimeException(e);
            }
            this.logger.error("<readConstituentsGroupPermissions> failed", e);
        }
    }

    public void setIndexCache(Ehcache indexCache) {
        this.indexCache = indexCache;
        this.logger.info("<setIndexCache> for #"
                + this.indexCache.getCacheConfiguration().getMaxElementsInMemory());
    }

    public void setDelegate(IndexCompositionProvider delegate) {
        this.delegate = delegate;
    }

    public Set<Long> getIndexPositionsByQuoteid(long quoteid) {
        return this.delegate.getIndexPositionsByQuoteid(quoteid);
    }

    public Set<Long> getIndexPositionIidsByQuoteid(long quoteid) {
        return this.delegate.getIndexPositionIidsByQuoteid(quoteid);
    }

    public Set<Long> getIndexPositionsByName(String name) {
        return this.delegate.getIndexPositionsByName(name);
    }

    public Set<Long> getIndexQuoteidsForQuoteids(List<Long> quoteid) {
        return this.delegate.getIndexQuoteidsForQuoteids(quoteid);
    }

    public NamedIdSet getIndexDefintionByName(String name) {
        return this.delegate.getIndexDefintionByName(name);
    }

    public IndexCompositionResponse getIndexCompositionByName(String listid) {
        return getIndexComposition(new IndexCompositionRequest(listid));
    }

    public IndexCompositionResponse getIndexCompositionByQid(long qid) {
        return getIndexComposition(new IndexCompositionRequest(qid));
    }

    private void checkPermission(String constituentsGroup) {
        if (!StringUtils.hasText(constituentsGroup)) {
            return;
        }
        final String selector = this.constituentsGroupPermissions.getProperty(constituentsGroup);
        if (!StringUtils.hasText(selector)) {
            throw new PermissionDeniedException("constituentsGroup w/o entitlement: " + constituentsGroup);
        }
        if (!isAllowedAnyAspect(selector)) {
            throw new PermissionDeniedException(EntitlementsVwd.toValue(selector));
        }
    }

    private boolean isAllowedAnyAspect(String selector) {
        for (Profile.Aspect aspect : Profile.Aspect.values()) {
            if (RequestContextHolder.getRequestContext().getProfile().isAllowed(aspect, selector)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IndexCompositionResponse getIndexComposition(IndexCompositionRequest request) {
        IndexCompositionResponse result = doGetIndexCompositionResponse(request);
        if (result.isValid()) {
            checkPermission(result.getIndexComposition().getConstituentsGroup());
        }
        return result;
    }

    private IndexCompositionResponse doGetIndexCompositionResponse(
            IndexCompositionRequest request) {
        final Element cached = this.indexCache.get(request.getKey());
        if (cached != null) {
            IndexComposition ic = (IndexComposition) cached.getValue();
            return new IndexCompositionResponse(ic);
        }
        final IndexCompositionResponse response = this.delegate.getIndexComposition(request);
        if (response.isValid()) {
            addToCache(request.getKey(), response.getIndexComposition());
        }
        return response;
    }

    @Override
    public IndexMembershipResponse getIndexMembership(IndexMembershipRequest request) {
        final IndexMembershipResponse result = this.delegate.getIndexMembership(request);
        if (result.isValid() && result.hasPermissionedItems()) {
            List<IndexMembershipResponse.Item> allowedItems = new ArrayList<>(result.getItems().size());
            for (IndexMembershipResponse.Item item : result.getItems()) {
                try {
                    checkPermission(item.consituentGroup);
                    allowedItems.add(item);
                } catch (PermissionDeniedException e) {
                    // ignore
                }
            }
            return new IndexMembershipResponse(allowedItems);
        }
        return result;
    }

    private IndexComposition addToCache(String key, IndexComposition ic) {
        final Element element = new Element(key, ic);
        this.indexCache.put(element);
        return ic;
    }


    public Long getBenchmarkId(Instrument instrument) {
        final Long cached = this.iidToBenchmarkIidCache.get(instrument.getId());
        if (cached != null) {
            return cached;
        }

        final Long result = this.benchmarkProvider.getBenchmarkId(instrument);
        if (result != null) {
            this.iidToBenchmarkIidCache.put(instrument.getId(), result);
        }
        return result;
    }
}

