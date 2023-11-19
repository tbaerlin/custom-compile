/*
 * PriceSearchController.java
 *
 * Created on 19.03.2008 14:58:25
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.extjs.gxt.ui.client.util.Format;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.HTML;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.MSCInstrumentPriceSearch;
import de.marketmaker.iview.dmxml.MSCInstrumentPriceSearchElement;
import de.marketmaker.iview.dmxml.SearchTypeCount;
import de.marketmaker.iview.mmgwt.mmweb.client.data.CurrentTrendBar;
import de.marketmaker.iview.mmgwt.mmweb.client.data.TrendBarData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.SearchResultEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.EmptyContext;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;

import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @author Markus Dick
 */
public class PriceSearchController extends AbstractSearchController<MSCInstrumentPriceSearch>
        implements QuickSearchController {

    private static final String[] VWDCODE = new String[]{"vwdcode"}; // $NON-NLS-0$

    private PriceSearchView view;

    public PriceSearchController(ContentContainer container) {
        super(container, "MSC_InstrumentPriceSearch", 20); // $NON-NLS$
    }

    protected void init() {
        JSONWrapper j = this.sessionData.getGuiDef("price_search_config"); // $NON-NLS$
        if (j.isValid()) {
            final String[] countTypes = getJsonTypeArrayConfig(j, "countTypes"); // $NON-NLS$
            if (countTypes != null) {
                setCountTypes(countTypes);
            }

            final String[] filterTypes = getJsonTypeArrayConfig(j, "filterTypes"); // $NON-NLS$
            if (filterTypes != null) {
                setFilterTypes(filterTypes);
            }

            final String strWithMsc = j.getString("withMsc"); // $NON-NLS$
            if (strWithMsc != null) {
                setWithMsc(Boolean.valueOf(strWithMsc));
            }

            final String strWithAll = j.getString("withAll"); // $NON-NLS$
            if (strWithAll != null) {
                setWithAll(Boolean.valueOf(strWithAll));
            }
        }
        super.init();
    }

    @Override
    protected String getViewGroup() {
        return "pricesearch"; // $NON-NLS$
    }

    private String[] getJsonTypeArrayConfig(JSONWrapper base, String configKey) {
        JSONWrapper ts = base.get(configKey);

        final int size = ts.size();
        if (size > 0) {
            String[] types = new String[size];
            for (int i = 0; i < size; i++) {
                types[i] = ts.get(i).stringValue();
            }
            return types;
        }
        return null;
    }

    @Override
    protected String[] getAdditionalSearchField() {
        return StringUtil.concat(super.getAdditionalSearchField(), VWDCODE);
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        final String query = getQuery(event);
        if (query == null) {
            getContentContainer().setContent(new HTML(I18n.I.noSelection()));
            return;
        }
        reset(query);
        Firebug.log("PSC.onPlaceChange(): " + query);
        selectNavItemSpec(event);

        if (this.sessionData.isIceDesign()) {
            MainController.INSTANCE.getView().setContentHeader(
                    I18n.I.searchResults() + " " + I18n.I.marketdata() +
                            " \"" + Format.htmlEncode(query) + "\"");

        }
    }

    private String getQuery(PlaceChangeEvent event) {
        final HistoryToken historyToken = event.getHistoryToken();
        if (historyToken.getAllParamCount() < 2) {
            return getCurrentQuery();
        }
        return historyToken.getByNameOrIndex("s", 1); // $NON-NLS$
    }

    private void selectNavItemSpec(PlaceChangeEvent event) {
        final String t = event.getHistoryToken().get("t"); // $NON-NLS$
        final NavItemSpec navItem = getNavItem(this.navItemSpecRoot, t);
        this.navItemSelectionModel.setSelected(navItem, true);
        onViewChanged();
    }


    private NavItemSpec getNavItem(NavItemSpec navItemSpec, String controllerId) {
        if (controllerId.equals(navItemSpec.getId())) {
            return navItemSpec;
        }
        if (navItemSpec.getChildren() == null || navItemSpec.getChildren().isEmpty()) {
            return null;
        }
        for (NavItemSpec child : navItemSpec.getChildren()) {
            final NavItemSpec found = getNavItem(child, controllerId);
            if (found != null) {
                return found;
            }
        }
        return navItemSpec.getChildren().get(0);
    }


    public void quickSearch(String ctrlKey, String query) {
        if (StringUtil.hasText(query)) {
            PlaceUtil.goTo(StringUtil.joinTokens(ctrlKey, "s=" + query)); // $NON-NLS$
        }
    }

    protected boolean doUpdateModel() {
        // SingleHit functionality works no longer for ICE/AS design, because if s.o.
        // clicks on tools, the controller receives a place change event again and if it has only
        // one single hit, it automatically switches to the portrait place.
        // This means, that it is not possible to change to tools from the portrait if the portrait
        // has been triggered from a single hit, because it will instantaneously change back to the
        // portrait. See MMWEB-761/R-82377 for details.
        if (!this.sessionData.isIceDesign() && isSingleHit()) {
            // jump to portrait on single result
            final MSCInstrumentPriceSearchElement element = getResult().getElement().get(0);
            PlaceUtil.goToPortrait(element.getInstrumentdata(), element.getQuotedata());
            return false;
        }

        AbstractMainController.INSTANCE.placeManager.addPendingHistoryToken();

        final TrendBarData tbd = TrendBarData.create(getResult());

        this.dtm = DefaultTableDataModel.create(getResult().getElement(),
                new AbstractRowMapper<MSCInstrumentPriceSearchElement>() {
                    public Object[] mapRow(MSCInstrumentPriceSearchElement e) {
                        final Price p = Price.create(e);
                        final QuoteWithInstrument qwi = createQwi(e);
                        return new Object[]{
                                qwi,
                                qwi,
                                e.getInstrumentdata().getWkn(),
                                e.getInstrumentdata().getIsin(),
                                e.getQuotedata().getMarketName(),
                                p.getLastPrice(),
                                e.getQuotedata().getCurrencyIso(),
                                p.getChangeNet(),
                                p.getChangePercent(),
                                new CurrentTrendBar(p.getChangePercent(), tbd),
                                p.getBid(),
                                p.getBidVolume(),
                                p.getAsk(),
                                p.getAskVolume(),
                                p.getDate(),

                        };
                    }
                });

        return true;
    }

    private QuoteWithInstrument createQwi(MSCInstrumentPriceSearchElement e) {
        final QuoteWithInstrument qwi = new QuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata());
        final MSCInstrumentPriceSearch result = getResult();
        if (result == null) {
            return qwi;
        }
        return qwi.withHistoryContext(EmptyContext.create(I18n.I.searchResults()));
    }

    private boolean isSingleHit() {
        int numFound = 0;
        for (SearchTypeCount count : getResult().getTypecount()) {
            if ((numFound += Integer.parseInt(count.getValue())) > 1) {
                return false;
            }
        }
        return numFound == 1;
    }

    protected void updateView() {
        if (this.view == null) {
            this.view = new PriceSearchView(this);
        }
        this.view.updateViewNames();
        if (this.dtm != null) {
            this.view.show(this.dtm);
        }
    }

    protected List<SearchTypeCount> getTypecount() {
        return getResult().getTypecount();
    }

    @Override
    protected SelectionHandler<NavItemSpec> getSelectionHandler() {
        return event -> {
            Firebug.log("#onSelection# " + event.getSelectedItem().getId() + "/" + event.getSelectedItem().getName());
            PlaceUtil.goTo("M_S/s=" + getCurrentQuery() + "/t=" + event.getSelectedItem().getId()); // $NON-NLS$
        };
    }

    @Override
    protected void onResult() {
        super.onResult();
        EventBusRegistry.get().fireEvent(SearchResultEvent.createDmxmlSearchResultEvent()
                .withSearchText(this.block.getParameter(SEARCHSTRING))
                .withNavItemSelectionModel(this.navItemSelectionModel));
    }
}