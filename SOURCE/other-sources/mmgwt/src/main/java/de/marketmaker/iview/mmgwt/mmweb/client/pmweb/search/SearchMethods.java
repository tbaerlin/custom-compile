
/*
 * SearchMethods.java
 *
 * Created on 04.01.13 12:41
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.SymbolUtil;
import de.marketmaker.iview.pmxml.SearchType;
import de.marketmaker.iview.pmxml.SearchTypeWP;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.ShellObjectSearchRequest;
import de.marketmaker.iview.pmxml.ShellObjectSearchResponse;
import de.marketmaker.iview.pmxml.ZoneDesc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Markus Dick
 */
public class SearchMethods {
    interface UpdateSearchElementStateCallback {
        void onSuccess(List<CombinedSearchElement> searchElements);
        void onFailure(List<CombinedSearchElement> searchElements);
    }

    public static final SearchMethods INSTANCE = new SearchMethods();

    private DmxmlContext.Block<ShellObjectSearchResponse> createInstrumentSearchBlock(final String searchString, Set<ShellMMType> filterTypes) {
        final ShellObjectSearchRequest request = createInstrumentSearchRequest(searchString, filterTypes);
        return createBlock(request);
    }

    private ShellObjectSearchRequest createInstrumentSearchRequest(String searchString, Collection<ShellMMType> filterTypes) {
        final ShellObjectSearchRequest request = new ShellObjectSearchRequest();
        request.setSearchType(SearchType.SEARCH_WP);
        request.setSearchTypeWP(SearchTypeWP.STWP_SECURITY);
        request.setSearchString(searchString);
        addShellMMTypes(filterTypes, request);
        return request;
    }

    private DmxmlContext.Block<ShellObjectSearchResponse> createDepotObjectSearchBlock(final String searchString) {
        final ShellObjectSearchRequest request = new ShellObjectSearchRequest();
        request.setSearchType(SearchType.SEARCH_DEPOT);
        request.setSearchString(searchString);

        return createBlock(request);
    }

    public void instrumentSearch(String searchString, final AsyncCallback<ShellObjectSearchResponse> callback) {
        instrumentSearch(searchString, null, callback);
    }

    public void instrumentSearch(String searchString, Set<ShellMMType> filterTypes, final AsyncCallback<ShellObjectSearchResponse> callback) {
        final DmxmlContext.Block<ShellObjectSearchResponse> block = createInstrumentSearchBlock(searchString, filterTypes);

        search(searchString, block, callback);
    }

    public void instrumentSearchWkn(final String wkn, final Set<ShellMMType> filterTypes, final AsyncCallback<ShellMMInfo> callback) {
        if(!SymbolUtil.isWkn(wkn)) {
            callback.onFailure(new IllegalArgumentException("Given symbol is not a WKN")); //$NON-NLS$
            return;
        }

        final ShellObjectSearchRequest searchRequest = createInstrumentSearchRequest(wkn, filterTypes);
        searchRequest.setCount(Integer.toString(2));

        instrumentSearch(wkn, filterTypes, new AsyncCallback<ShellObjectSearchResponse>() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(ShellObjectSearchResponse result) {
                onWknSearchSuccess(wkn, result, filterTypes, callback);
            }
        });
    }

    public void instrumentSearchIsin(final String isin, final Set<ShellMMType> filterTypes, final AsyncCallback<ShellMMInfo> callback) {
        if(!SymbolUtil.isIsin(isin)) {
            callback.onFailure(new IllegalArgumentException("Given symbol is not an ISIN")); //$NON-NLS$
            return;
        }

        doUnambiguousIsinSearch(isin, filterTypes, callback);
    }

    private void onWknSearchSuccess(String wkn, ShellObjectSearchResponse result, Set<ShellMMType> filterTypes, final AsyncCallback<ShellMMInfo> callback) {
        List<ShellMMInfo> shellMMInfos = result.getObjects();
        if(shellMMInfos.isEmpty()) {
            callback.onFailure(new RuntimeException("Given WKN not found")); //$NON-NLS$
            return;
        }
        final ShellMMInfo firstItem = shellMMInfos.get(0);
        if(wkn.equals(firstItem.getNumber())) {
            if(StringUtil.hasText(firstItem.getISIN())) {
                doUnambiguousIsinSearch(firstItem.getISIN(), filterTypes, callback);
                return;
            }
            callback.onFailure(new RuntimeException("Security found but has no ISIN")); //$NON-NLS$
            return;
        }
        callback.onFailure(new RuntimeException("Given WKN is ambiguous")); //$NON-NLS$
    }

    private void doUnambiguousIsinSearch(String isin, Set<ShellMMType> filterTypes, final AsyncCallback<ShellMMInfo> callback) {
        instrumentSearch(isin, filterTypes, new AsyncCallback<ShellObjectSearchResponse>() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(ShellObjectSearchResponse result) {
                onUnambiguousIsinSearchSuccess(result, callback);
            }
        });
    }

    private void onUnambiguousIsinSearchSuccess(ShellObjectSearchResponse result, AsyncCallback<ShellMMInfo> callback) {
        final List<ShellMMInfo> shellMMInfos = result.getObjects();
        if(shellMMInfos.isEmpty() || shellMMInfos.size() > 1) {
            callback.onFailure(new RuntimeException("ISIN search result empty or ISINs are ambiguous")); //$NON-NLS$
            return;
        }
        callback.onSuccess(shellMMInfos.get(0));
    }

    public void depotObjectSearch(String searchString, final AsyncCallback<ShellObjectSearchResponse> callback) {
        final DmxmlContext.Block<ShellObjectSearchResponse> block = createDepotObjectSearchBlock(searchString);
        search(searchString, block, callback);
    }

    private void addShellMMTypes(Collection<ShellMMType> filterTypes, ShellObjectSearchRequest request) {
        if(filterTypes != null) {
            request.getShellMMTypes().addAll(filterTypes);
        }
    }

    private void search(final String searchString, final DmxmlContext.Block<ShellObjectSearchResponse> block, final AsyncCallback<ShellObjectSearchResponse> callback) {
        block.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(ResponseType result) {
                if (block.isResponseOk()) {
                    callback.onSuccess(block.getResult());
                } else {
                    //TODO: i18n!
                    callback.onFailure(new RuntimeException("Searching for '" + searchString + "' failed")); //$NON-NLS$
                }
            }
        });
    }

    public DmxmlContext.Block<ShellObjectSearchResponse> createShellSearchBlock() {
        return createShellSearchBlock(new DmxmlContext());
    }

    public DmxmlContext.Block<ShellObjectSearchResponse> createShellSearchBlock(DmxmlContext context) {
        return context.addBlock("PM_ShellSearch"); //$NON-NLS$
    }

    private DmxmlContext.Block<ShellObjectSearchResponse> createBlock(ShellObjectSearchRequest request) {
        final DmxmlContext.Block<ShellObjectSearchResponse> block = createShellSearchBlock();
        block.setParameter(request);
        return block;
    }

    public void updateSearchElementState(final List<CombinedSearchElement> searchElements,
                                         final Set<ShellMMType> typesAllowedForOrderEntry,
                                         final UpdateSearchElementStateCallback callback) {
        final DmxmlContext context = new DmxmlContext();
        final ArrayList<DmxmlContext.Block<ShellObjectSearchResponse>> searchBlocks =
                new ArrayList<>();

        for(CombinedSearchElement element: searchElements) {
            if(StringUtil.hasText(element.getIsin())) {
                //Do not use shell mm types to filter the search result here, because otherwise users won't see
                //for which types ordering is not allowed due to business rules of the bank!
                //The types are only necessary later on render the corresponding icon in the result list.
                final ShellObjectSearchRequest request = createInstrumentSearchRequest(element.getIsin(), null);
                final DmxmlContext.Block<ShellObjectSearchResponse> block = createShellSearchBlock(context);
                block.setParameter(request);
                searchBlocks.add(block);
            }
        }

        context.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onSuccess(ResponseType result) {
                onUpdateSearchElementStateSuccess(searchBlocks, searchElements, typesAllowedForOrderEntry, callback);
            }

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(searchElements);
            }
        });
    }

    private void onUpdateSearchElementStateSuccess(List<DmxmlContext.Block<ShellObjectSearchResponse>> searchBlocks, List<CombinedSearchElement> searchElements, Set<ShellMMType> typesAllowedForOrderEntry, UpdateSearchElementStateCallback callback) {
        Firebug.debug("<SearchMethods.onUpdateSearchElementStateSuccess>");

        for(int i = 0, j = 0; i < searchElements.size(); i++) {
            final CombinedSearchElement searchElement = searchElements.get(i);

            if(StringUtil.hasText(searchElement.getIsin())) {
                DmxmlContext.Block<ShellObjectSearchResponse> searchBlock = searchBlocks.get(j++);

                if(searchBlock.isResponseOk()) {
                    final ShellObjectSearchResponse response = searchBlock.getResult();

                    switch(response.getObjects().size()) {
                        case 1:
                            Firebug.debug("<SearchMethods.onUpdateSearchElementStateSuccess> pm found exactly 1 ISIN=" + response.getObjects().get(0).getISIN());
                            final ShellMMInfo shellMMInfo = response.getObjects().get(0);
                            searchElement.setShellMMInfo(shellMMInfo);
                            if(typesAllowedForOrderEntry == null || typesAllowedForOrderEntry.contains(shellMMInfo.getTyp())) {
                                searchElement.setShellMmInfoState(CombinedSearchElement.State.AVAILABLE);
                            }
                            else {
                                searchElement.setShellMmInfoState(CombinedSearchElement.State.NO_ORDER_ENTRY_DUE_TO_BUSINESS_RULES);
                            }
                            break;
                        case 0:
                            Firebug.debug("<SearchMethods.onUpdateSearchElementStateSuccess> pm found nothing");
                            searchElement.setShellMmInfoState(CombinedSearchElement.State.NOT_AVAILABLE);
                            break;
                        default:
                            Firebug.debug("<SearchMethods.onUpdateSearchElementStateSuccess> pm found more than one ISIN size=" + searchBlock.getResult().getObjects().size());
                            searchElement.setShellMmInfoState(CombinedSearchElement.State.AMBIGUOUS_ISIN);
                    }
                }
            }
            else {
                searchElement.setShellMmInfoState(CombinedSearchElement.State.EMPTY_ISIN);
            }
        }

        callback.onSuccess(searchElements);
    }

    public Map<String, ZoneDesc> toMap(List<ZoneDesc> zoneDescs){
        if(zoneDescs == null) return Collections.emptyMap();

        HashMap<String, ZoneDesc> zoneDescMap = new HashMap<>();

        for(ZoneDesc zoneDesc : zoneDescs) {
            zoneDescMap.put(zoneDesc.getId(), zoneDesc);
        }

        return zoneDescMap;
    }

    public static class ShellMMInfoComparator implements Comparator<ShellMMInfo> {
        private final boolean ignoreCase;

        @SuppressWarnings("unused")
        public ShellMMInfoComparator() {
            this(false);
        }

        public ShellMMInfoComparator(boolean ignoreCase) {
            this.ignoreCase = ignoreCase;
        }

        @Override
        public int compare(ShellMMInfo o1, ShellMMInfo o2) {
            if(o1 == o2) return 0;
            if(o1 == null) return -1;
            if(o2 == null) return 1;

            if(this.ignoreCase) {
                return o1.getBezeichnung().compareToIgnoreCase(o2.getBezeichnung());
            }

            return o1.getBezeichnung().compareTo(o2.getBezeichnung());
        }
    }
}
