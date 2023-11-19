package de.marketmaker.iview.pmxml.block;

import de.marketmaker.istar.merger.web.easytrade.ListCommand;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.block.SortSupport;
import de.marketmaker.itools.pmxml.frontend.PmxmlException;
import de.marketmaker.iview.dmxml.SearchTypeCount;
import de.marketmaker.iview.pmxml.Comparators;
import de.marketmaker.iview.pmxml.SearchType;
import de.marketmaker.iview.pmxml.SearchTypeWP;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.ShellObjectSearchRequest;
import de.marketmaker.iview.pmxml.ShellObjectSearchResponse;
import de.marketmaker.iview.pmxml.internaltypes.ShellSearchResult;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 23.07.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class ShellSearch extends AbstractInternalBlock<ShellSearchResult, ShellSearch.ShellSearchCmd> {

    @SuppressWarnings("UnusedDeclaration")
    public static class ShellSearchCmd extends ListCommand implements AbstractInternalBlock.InternalCommand, Serializable {
        private String correlationId;
        private String atomname;
        private String searchString;
        private SearchType searchType;
        private SearchTypeWP searchTypeWP;
        private ShellMMType[] shellMMTypes;
        private String pagingHandle;

        public void setCorrelationId(String correlationId) {
            this.correlationId = correlationId;
        }

        public void setAtomname(String atomname) {
            this.atomname = atomname;
        }

        public void setSearchString(String searchString) {
            this.searchString = searchString;
        }

        public void setSearchType(SearchType searchType) {
            this.searchType = searchType;
        }

        public void setSearchTypeWP(SearchTypeWP searchTypeWP) {
            this.searchTypeWP = searchTypeWP;
        }

        public void setShellMMTypes(ShellMMType[] shellMMTypes) {
            this.shellMMTypes = shellMMTypes;
        }

        public String getPagingHandle() {
            return pagingHandle;
        }

        public void setPagingHandle(String pagingHandle) {
            this.pagingHandle = pagingHandle;
        }

        @Override
        public AbstractInternalBlock.InternalCommandFeature getInternalCmdFeature() {
            return new AbstractInternalBlock.InternalCommandFeature().withAtomname(this.atomname).withCorrelationId(this.correlationId);
        }
    }

    private static final SortSupport<ShellMMInfo> SORT_SUPPORT;

    private static final String DEFAULT_SORT_BY = "default";

    private static final List<String> SORT_FIELDS;

    static {
        SORT_SUPPORT = SortSupport.createBuilder("default", Comparators.SHELL_BY_NAME)
                .add("name", Comparators.SHELL_BY_NAME)
                .build();

        SORT_FIELDS = SORT_SUPPORT.getSortNames();
    }

    private Ehcache resultCache;

    public void setResultCache(Ehcache resultCache) {
        this.resultCache = resultCache;
    }

    public ShellSearch() {
        super(ShellSearchCmd.class);
    }

    private ShellObjectSearchResponse getResFromCache(String searchString) {
        final Element element = this.resultCache.get(searchString);
        return (ShellObjectSearchResponse) ((element != null) ? element.getValue() : null);
    }


    @Override
    protected ShellSearchResult internalDoHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, ShellSearchCmd cmd, BindException e) {
        final String cacheKey = StringUtils.hasText(cmd.pagingHandle) ? cmd.getPagingHandle() : generateCacheKey(cmd, httpServletRequest);
        final ShellObjectSearchResponse resFromCache = getResFromCache(cacheKey);
        final ShellObjectSearchResponse res;
        final String pagingHandle;

        if (resFromCache == null) {
            res = doSearch(cmd);
            pagingHandle = UUID.randomUUID().toString();
            this.resultCache.put(new Element(pagingHandle, res));
            this.resultCache.put(new Element(cacheKey, res));
        }
        else {
            pagingHandle = cmd.pagingHandle;
            res = resFromCache;
        }

        final List<ShellMMInfo> objects = new ArrayList<>(res.getObjects());
        final List<SearchTypeCount> counts = createTypeCounts(objects);

        final ListResult listResult
                = ListResult.create(cmd, SORT_FIELDS, DEFAULT_SORT_BY, objects.size());

        listResult.setTotalCount(objects.size());
        SORT_SUPPORT.apply(listResult.getSortedBy(), cmd, objects);
        listResult.setCount(objects.size());

        return new ShellSearchResult(objects, res.getZones(), counts, String.valueOf(listResult.getCount()),
                String.valueOf(listResult.getOffset()), String.valueOf(listResult.getTotalCount()), pagingHandle);
    }

    private List<SearchTypeCount> createTypeCounts(List<ShellMMInfo> objects) {
        final HashMap<ShellMMType, AtomicInteger> map = new HashMap<>();
        for (ShellMMInfo object : objects) {
            if (!map.containsKey(object.getTyp())) {
                map.put(object.getTyp(), new AtomicInteger(0));
            }
            map.get(object.getTyp()).incrementAndGet();
        }
        final ArrayList<SearchTypeCount> result = new ArrayList<>();
        for (Map.Entry<ShellMMType, AtomicInteger> e : map.entrySet()) {
            final SearchTypeCount stc = new SearchTypeCount();
            stc.setType(e.getKey().toString());
            stc.setValue(e.getValue().toString());
            result.add(stc);
        }
        return result;
    }

    private ShellObjectSearchResponse doSearch(ShellSearchCmd cmd) {
        final ShellObjectSearchRequest req = new ShellObjectSearchRequest();
        req.setSearchString(cmd.searchString);
        req.setSearchType(cmd.searchType);
        req.setSearchTypeWP(cmd.searchTypeWP);
        if (cmd.shellMMTypes != null) {
            req.getShellMMTypes().addAll(Arrays.asList(cmd.shellMMTypes));
        }

        final ShellObjectSearchResponse res;
        try {
            res = this.pmxmlImpl.exchangeData(req, "Search_ShellObject", ShellObjectSearchResponse.class);
        }
        catch (PmxmlException ex) {
            throw new IllegalStateException(ex);
        }
        return res;
    }

    private String generateCacheKey(ShellSearchCmd cmd, HttpServletRequest request) {
        final String authToken = PmExchangeData.getAuthToken(request);
        final String searchStr = cmd.searchString;
        final String searchType = cmd.searchType != null ? cmd.searchType.value() : "";
        final String searchTypeWP = cmd.searchTypeWP != null ? cmd.searchTypeWP.value() : "";
        final StringBuilder sb = new StringBuilder();
        sb.append(authToken).append(searchStr).append(searchType).append(searchTypeWP);
        final ShellMMType[] shellMMTypes = cmd.shellMMTypes;
        if (shellMMTypes == null || shellMMTypes.length == 0) {
            return sb.toString();
        }
        for (ShellMMType shellMMType : shellMMTypes) {
            sb.append(shellMMType.value());
        }
        return sb.toString();
    }
}