/*
 * SearchBox.java
 *
 * Created on 14.02.13
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.event.KeyModifiers;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractSuggestion;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.InstrumentSuggestOracle;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.TopToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.SearchHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.SearchResultEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search.DepotObjectSuggestOracle;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.SuggestOracleMultiplexer;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.SearchWorkspace;

/**
 * @author Ulrich Maurer
 * @author mdick
 */
public class SearchBox implements IsWidget, SearchHandler {
    private final FlowPanel panel = new FlowPanel();

    private final TextBox textBox;

    private SearchMode searchMode;

    private boolean keyDownProcessed = false;

    private LinkedHashMap<SearchMode, Image> switches = new LinkedHashMap<>(); // insertion order of keys and values matters!

    private final SuggestOracleMultiplexer<SearchMode> suggestOracleMultiplexer = new SuggestOracleMultiplexer<>();

    private boolean privacyMode = false;

    public boolean isVisible() {
        return this.panel.isVisible();
    }

    public enum SearchMode {
        PM_DEPOT_OBJECT, MM_INSTRUMENT, MM_NEWS, MM_VWD_PAGE, MM_DZ_PAGE
    }

    public SearchBox() {
        final SuggestOracleMultiplexer<SearchMode> mux = this.suggestOracleMultiplexer;
        mux.put(SearchMode.PM_DEPOT_OBJECT, new DepotObjectSuggestOracle());
        mux.put(SearchMode.MM_INSTRUMENT, new InstrumentSuggestOracle());
        mux.setOracleEnabled(SearchMode.MM_INSTRUMENT, Selector.INSTRUMENT_SEARCH_SUGGESTION.isAllowed());

        this.textBox = new TextBox();
        final SuggestionsView suggestionDisplay = new SuggestionsView();
        final SuggestBox suggestBox = new SuggestBox(this.suggestOracleMultiplexer, this.textBox, suggestionDisplay);
        suggestBox.setAutoSelectEnabled(false);
        suggestBox.addSelectionHandler(event -> {
            if (this.keyDownProcessed) {
                return;
            }
            AbstractSuggestion abstractSuggestion = (AbstractSuggestion) event.getSelectedItem();
            abstractSuggestion.goTo();
            this.keyDownProcessed = true;
        });
        suggestBox.addKeyUpHandler(event -> this.keyDownProcessed = false);
        suggestBox.addKeyDownHandler(event -> {
            if (this.keyDownProcessed) {
                this.keyDownProcessed = false;
                return;
            }
            if (!suggestionDisplay.isSuggestionListShowing() || !suggestionDisplay.isItemSelected()) {
                switch (event.getNativeKeyCode()) {
                    case KeyCodes.KEY_ENTER:
                        search(suggestBox.getText().trim());
                        break;
                }
                this.keyDownProcessed = true;
            }
        });

        final Image imageSearch = IconImage.get("x-tbar-search").createImage(); // $NON-NLS$
        imageSearch.setStyleName("as-doSearch");
        imageSearch.addClickHandler(event -> search(suggestBox.getText().trim()));

        if (SessionData.isWithPmBackend()) {
            createAndAddSwitch(SearchMode.PM_DEPOT_OBJECT, "pm-icon-16", I18n.I.pmDepotobjects(), suggestBox); // $NON-NLS$
        }

        if (SessionData.isWithMarketData()) {
            createAndAddSwitch(SearchMode.MM_INSTRUMENT, "mm-icon-16", // $NON-NLS$
                    !SessionData.isWithPmBackend() ? I18n.I.instruments() : I18n.I.marketdata(), suggestBox);

            if (!SessionData.isWithPmBackend()) {
                createAndAddSwitch(SearchMode.MM_NEWS, "mm-news-icon-16", I18n.I.news(), suggestBox); // $NON-NLS$

                if (Selector.PAGES_VWD.isAllowed()) {
                    createAndAddSwitch(SearchMode.MM_VWD_PAGE, "mm-icon-vwdpage-quicksearch", I18n.I.vwdPages(), suggestBox); // $NON-NLS$
                }

                if (Selector.PAGES_DZBANK.isAllowed()) {
                    createAndAddSwitch(SearchMode.MM_DZ_PAGE, "mm-icon-dzbank-page-quicksearch", I18n.I.quickcodes(), suggestBox); // $NON-NLS$
                }
            }
        }

        this.panel.setStyleName("as-searchBox");
        this.panel.add(suggestBox);
        this.panel.add(imageSearch);

        this.textBox.setSelectionRange(0, this.textBox.getValue().length());
        this.textBox.setFocus(true);
        this.panel.addStyleName("as-focus");
        setSearchMode(SessionData.isWithPmBackend() ? SearchMode.PM_DEPOT_OBJECT : SearchMode.MM_INSTRUMENT);
        EventBusRegistry.get().addHandler(SearchResultEvent.getType(), this);
        setupKeyboardShortcuts();
    }

    private void createAndAddSwitch(SearchMode searchMode, String iconClass,
            String tooltip, SuggestBox suggestBox) {
        addSwitch(createSwitch(iconClass, suggestBox, searchMode, tooltip), searchMode);
    }

    private Image createSwitch(String iconClass, SuggestBox suggestBox,
            SearchMode currentSearchMode, String tooltip) {
        final Image image = IconImage.get(iconClass).createImage();
        image.setStyleName("as-switchMode");
        image.addClickHandler(event -> searchModeSwitchClicked(suggestBox, currentSearchMode));
        Tooltip.addQtip(image, tooltip);
        return image;
    }

    private void addSwitch(Image switchPm, SearchMode searchMode) {
        this.switches.put(searchMode, switchPm);
        this.panel.add(switchPm);
    }

    private void searchModeSwitchClicked(SuggestBox suggestBox, SearchMode searchMode) {
        // This check is sufficient only because advisory solution does not have mm[web]'s pages
        // and news search mode. If this changes, this check has to be generalized!
        if (this.privacyMode) {
            return;
        }
        setSearchMode(findNextSearchMode(searchMode));
        if (StringUtil.hasText(suggestBox.getText())) {
            search(suggestBox.getText().trim());
        }
        this.textBox.setFocus(true);
    }

    private SearchMode findNextSearchMode(SearchMode currentSearchMode) {
        if (this.switches.size() <= 1) {
            return currentSearchMode;
        }

        SearchMode last = null;
        for (SearchMode mode : this.switches.keySet()) {
            if (last == currentSearchMode) {
                return mode;
            }
            last = mode;
        }
        final Iterator<SearchMode> iterator = this.switches.keySet().iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        // should never happen.
        throw new IllegalStateException("no search modes available"); // $NON-NLS$s
    }

    private void setSearchMode(SearchMode searchMode) {
        if (searchMode == null) {
            return;
        }

        boolean found = false;
        for (Map.Entry<SearchMode, Image> entry : this.switches.entrySet()) {
            final boolean visible = entry.getKey() == searchMode;
            entry.getValue().setVisible(visible);
            if(visible) {
                found = true;
            }
        }

        if(!found) {
            final Iterator<Map.Entry<SearchMode, Image>> iterator = this.switches.entrySet().iterator();
            if (iterator.hasNext()) {
                final Map.Entry<SearchMode, Image> first = iterator.next();
                first.getValue().setVisible(true);
                this.searchMode = first.getKey();
                this.suggestOracleMultiplexer.setActiveOracle(this.searchMode);
            }
            else {
                // should never happen.
                throw new IllegalStateException("no search mode switch available"); // $NON-NLS$
            }
        }
        else {
            this.searchMode = searchMode;
            this.suggestOracleMultiplexer.setActiveOracle(searchMode);
        }
    }

    @Override
    public Widget asWidget() {
        return this.panel;
    }

    private void search(String value) {
        if (value.isEmpty()) {
            return;
        }
        switch (this.searchMode) {
            case PM_DEPOT_OBJECT:
                SearchWorkspace.getInstance().searchPm(value);
                break;
            case MM_INSTRUMENT:
                SearchWorkspace.getInstance().searchDmxml(value);
                break;
            case MM_NEWS:
                TopToolbar.Util.search("N_S", value); // $NON-NLS$
                break;
            case MM_VWD_PAGE:
                TopToolbar.Util.search("P_V", value); // $NON-NLS$
                break;
            case MM_DZ_PAGE:
                TopToolbar.Util.search("P_D", value); // $NON-NLS$
                break;
        }
    }

    @Override
    public void onSearchResult(SearchResultEvent event) {
        if (event.getSearchDestination() == SearchResultEvent.SearchDestination.DMXML && SessionData.isWithMarketData()) {
            setSearchMode(SearchMode.MM_INSTRUMENT);
        }
        else {
            setSearchMode(SearchMode.PM_DEPOT_OBJECT);
        }
        this.textBox.setText(event.getSearchText());
        this.textBox.setFocus(true);
        this.textBox.selectAll();
    }

    public boolean isInterestingKeycode(NativeEvent event) {
        return KeyModifiers.isAltShift(event) && event.getKeyCode() == 70; // 70 = f
    }

    private void setupKeyboardShortcuts() {
        Event.addNativePreviewHandler(preview -> {
            NativeEvent event = preview.getNativeEvent();
            if (event.getType().equalsIgnoreCase("keydown") && isInterestingKeycode(event)) { // $NON-NLS$
                handleSearchBoxShortcut();
                preview.cancel();
            }
        });
    }

    private void handleSearchBoxShortcut() {
        if (hasFocus()) {
            setSearchMode(findNextSearchMode(this.searchMode));
        }
        else {
            this.textBox.setFocus(true);
        }
    }

    private boolean hasFocus() {
        return hasFocus(this.textBox.getElement());
    }

    private native boolean hasFocus(Element element) /*-{ //doesn't work with old browsers. see https://developer.mozilla.org/en-US/docs/Web/API/document.activeElement?redirectlocale=en-US&redirectslug=DOM%2Fdocument.activeElement#Browser_compatibility
        return $doc.activeElement == element;
    }-*/;

    public void setPrivacyMode(boolean privacyMode) {
        this.privacyMode = privacyMode;
        setSearchMode(privacyMode ? SearchMode.MM_INSTRUMENT : SearchMode.PM_DEPOT_OBJECT);
        this.textBox.setValue(null);
        this.panel.setVisible(SessionData.isWithMarketData() || !privacyMode && SessionData.isWithPmBackend());
        Optional.ofNullable(this.switches.get(SearchMode.PM_DEPOT_OBJECT)).ifPresent(
                aSwitch -> aSwitch.setVisible(!privacyMode && SessionData.isWithPmBackend()));
    }
}