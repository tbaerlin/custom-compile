/*
 * DepotObjectSuggestOracle
 *
 * Created on 04.06.13 14:21
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractBlockBasedSuggestOracle;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractSuggestion;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmPlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.pmxml.SearchType;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.ShellObjectSearchRequest;
import de.marketmaker.iview.pmxml.ShellObjectSearchResponse;
import de.marketmaker.iview.pmxml.ZoneDesc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Markus Dick
 */
public class DepotObjectSuggestOracle extends AbstractBlockBasedSuggestOracle<DepotObjectSuggestOracle.DepotObjectSuggestion, ShellObjectSearchResponse> {
    public DepotObjectSuggestOracle() {
        super(SearchMethods.INSTANCE.createShellSearchBlock());
    }

    @Override
    protected boolean isQueryAcceptable(String query) {
        return !(query.indexOf('*') > -1 || query.indexOf('?') > -1 || query.length() < 3);
    }

    @Override
    public void setBlockParameters(String query, int limit) {
        final ShellObjectSearchRequest shellObjectSearchRequest = new ShellObjectSearchRequest();
        shellObjectSearchRequest.setSearchType(SearchType.SEARCH_DEPOT);

        shellObjectSearchRequest.getShellMMTypes().addAll(Arrays.asList(
                        ShellMMType.ST_INHABER,
                        ShellMMType.ST_PORTFOLIO,
                        ShellMMType.ST_DEPOT,
                        ShellMMType.ST_KONTO));

        if(Selector.AS_ACTIVITIES.isAllowed()) {
            shellObjectSearchRequest.getShellMMTypes().add(ShellMMType.ST_INTERESSENT);
        }

        shellObjectSearchRequest.setSearchString(query);
        shellObjectSearchRequest.setCount(Integer.toString(limit));
        getBlock().setParameter(shellObjectSearchRequest);
    }

    @Override
    protected List<DepotObjectSuggestion> getSuggestions(String query) {
        final List<DepotObjectSuggestion> suggestions = new ArrayList<DepotObjectSuggestion>();
        final ShellObjectSearchResponse response = getBlock().getResult();
        final String equery = SafeHtmlUtils.htmlEscape(query);

        final Map<String, ZoneDesc> zoneDescs = SearchMethods.INSTANCE.toMap(response.getZones());

        for(ShellMMInfo shellMMInfo : response.getObjects()) {
            switch(shellMMInfo.getTyp()) {
                case ST_INHABER:
                case ST_PORTFOLIO:
                case ST_DEPOT:
                case ST_KONTO:
                    break;
                case ST_INTERESSENT:
                    if(!Selector.AS_ACTIVITIES.isAllowed()) {
                        continue;
                    }
                    break;
                default:
                    continue;
            }

            final ZoneDesc zoneDesc = zoneDescs.get(shellMMInfo.getPrimaryZoneId());
            final String primaryZoneName = (zoneDesc != null) ? zoneDesc.getName() : "";
            suggestions.add(new DepotObjectSuggestion(equery, shellMMInfo, primaryZoneName));
        }

        return suggestions;
    }

    public static class DepotObjectSuggestion extends AbstractSuggestion {
        private final ShellMMInfo shellMMInfo;

        public DepotObjectSuggestion(String query, ShellMMInfo shellMMInfo, String primaryZoneName) {
            this.shellMMInfo = shellMMInfo;
            final String display = getStringWithMatch(query, shellMMInfo.getBezeichnung())
                    + " | "
                    + getStringWithMatch(query, I18n.I.zoneNameSuffixPlain(primaryZoneName))
                    + " | "
                    + getStringWithMatch(query, PmRenderers.SHELL_MM_TYPE.render(shellMMInfo.getTyp()));
            setDisplay(display);
        }

        public ShellMMInfo getShellMMInfo() {
            return this.shellMMInfo;
        }

        @Override
        public void goTo() {
            PmPlaceUtil.goTo(this.shellMMInfo);
        }
    }
}
