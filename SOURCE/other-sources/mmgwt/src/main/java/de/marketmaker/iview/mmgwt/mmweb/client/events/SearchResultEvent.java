package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.GwtEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebModule;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSelectionModel;

/**
 * Created on 13.02.13 07:41
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class SearchResultEvent extends GwtEvent<SearchHandler> {

    public enum SearchDestination {
        DMXML("M_S"), // $NON-NLS$
        PM_DEPOT(PmWebModule.HISTORY_TOKEN_SEARCH_DEPOT),
        PM_INSTRUMENT(PmWebModule.HISTORY_TOKEN_SEARCH_INSTRUMENT);

        private final String controllerId;

        private SearchDestination(String controllerId) {
            this.controllerId = controllerId;
        }

        public String getControllerId() {
            return this.controllerId;
        }
    }

    private static Type<SearchHandler> TYPE;
    private final SearchDestination searchDestination;
    private String searchText;
    private NavItemSelectionModel navItemSelectionModel;

    public static SearchResultEvent createDmxmlSearchResultEvent() {
        return new SearchResultEvent(SearchDestination.DMXML);
    }

    public static SearchResultEvent createPmDepotSearchResultEvent() {
        return new SearchResultEvent(SearchDestination.PM_DEPOT);
    }

    public static SearchResultEvent createPmInstrumentSearchResultEvent() {
        return new SearchResultEvent(SearchDestination.PM_INSTRUMENT);
    }

    private SearchResultEvent(SearchDestination searchDestination) {
        this.searchDestination = searchDestination;
    }

    public static Type<SearchHandler> getType(){
        if (TYPE == null){
            TYPE = new Type<SearchHandler>();
        }
        return TYPE;
    }

    @Override
    public Type<SearchHandler> getAssociatedType() {
        return TYPE;
    }

    public SearchDestination getSearchDestination() {
        return this.searchDestination;
    }

    @Override
    protected void dispatch(SearchHandler handler) {
        handler.onSearchResult(this);
    }

    public String getSearchText() {
        return this.searchText;
    }

    public SearchResultEvent withSearchText(String searchText) {
        this.searchText = searchText;
        return this;
    }

    public SearchResultEvent withNavItemSelectionModel(NavItemSelectionModel navItemSelectionModel) {
        this.navItemSelectionModel = navItemSelectionModel;
        return this;
    }

    public NavItemSelectionModel getNavItemSelectionModel() {
        return this.navItemSelectionModel;
    }
}