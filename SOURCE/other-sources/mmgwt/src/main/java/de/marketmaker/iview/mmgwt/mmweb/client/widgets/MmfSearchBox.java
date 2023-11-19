/*
 * MmfSearchBox.java
 *
 * Created on 1/23/15 4:54 PM
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.SplitButton;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.InstrumentSuggestOracle;
import de.marketmaker.iview.mmgwt.mmweb.client.InstrumentSuggestion;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.TopToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;

/**
 * @author kmilyut
 */
public class MmfSearchBox implements IsWidget {

    private static final String SEARCH_BOX_ID_PREFIX = "SB_"; // $NON-NLS-0$

    private SplitButton sbMenuButton;

    private List<CheckMenuItem> sbMenuItems = new ArrayList<>();

    private final InstrumentSuggestOracle instrumentSuggestOracle = new InstrumentSuggestOracle();

    private Panel mainPanel = new FlowPanel();

    public MmfSearchBox() {
        this.instrumentSuggestOracle.setEnabled(isSuggestAllowed());

        this.sbMenuItems.add(createItem(TopToolbar.WP_SEARCH_KEY, I18n.I.instrument(), true));
        this.sbMenuItems.add(createItem("N_S", I18n.I.news(), false));  // $NON-NLS-0$
        if (Selector.PAGES_VWD.isAllowed()) {
            this.sbMenuItems.add(createItem("P_V", I18n.I.vwdPage(), false));  // $NON-NLS-0$
        }
        if (Selector.PAGES_DZBANK.isAllowed()) {
            this.sbMenuItems.add(createItem("P_D", I18n.I.quickcodes(), false));  // $NON-NLS-0$
        }

        addSearchWidgets();
    }

    private boolean isSuggestAllowed() {
        return Selector.INSTRUMENT_SEARCH_SUGGESTION.isAllowed();
    }

    private void search(String value) {
        final String ctrlKey = getSelectedSbItem().getId().substring(SEARCH_BOX_ID_PREFIX.length());
        TopToolbar.Util.search(ctrlKey, value);
    }

    private void cycleItems() {
        final int current = getChecked();
        if (current == -1) {
            this.sbMenuItems.get(0).setChecked(true);
            return;
        }
        this.sbMenuItems.get(current).setChecked(false);
        final int next = (current + 1) % sbMenuItems.size();
        this.sbMenuItems.get(next).setChecked(true);
    }

    private int getChecked() {
        for (int i = 0; i < this.sbMenuItems.size(); i++) {
            if (this.sbMenuItems.get(i).isChecked()) {
                return i;
            }
        }
        return -1;
    }

    private final SelectionListener<MenuEvent> checkItemListener = new SelectionListener<MenuEvent>() {
        @Override
        public void componentSelected(MenuEvent menuEvent) {
            onChangedSearchMenuItem();
        }
    };

    private CheckMenuItem getSelectedSbItem() {
        return this.sbMenuItems.get(getChecked());
    }

    private SuggestBox createSuggestBox() {
        final SuggestBox.DefaultSuggestionDisplay suggestionDisplay
                = new SuggestBox.DefaultSuggestionDisplay();
        final SuggestBox suggestBox
                = new SuggestBox(this.instrumentSuggestOracle, new TextBox(), suggestionDisplay);
        suggestBox.setText(TopToolbar.SEARCH_DEFAULT);
        suggestBox.setLimit(10);
        suggestBox.setAutoSelectEnabled(false);
        suggestBox.setAccessKey('s');

        suggestBox.getValueBox().addKeyUpHandler(new KeyUpHandler() {
            private boolean suggestionSelected = false;

            public void onKeyUp(KeyUpEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    if (!this.suggestionSelected) {
                        search(suggestBox.getText());
                    }
                    // selectionHandler's onSelection will be called, reset the selected flag
                    this.suggestionSelected = false;
                }
                if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
                    suggestionDisplay.hideSuggestions();
                    this.suggestionSelected = false;
                }
                if (event.getNativeKeyCode() == KeyCodes.KEY_DOWN
                        && suggestionDisplay.isSuggestionListShowing()) {
                    this.suggestionSelected = true;
                }
            }
        });

        suggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
            public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
                final InstrumentData data
                        = ((InstrumentSuggestion) event.getSelectedItem()).getData();
                PlaceUtil.goToPortrait(data);
            }
        });

        suggestBox.getValueBox().addFocusHandler(new FocusHandler() {
            public void onFocus(FocusEvent focusEvent) {
                if (TopToolbar.SEARCH_DEFAULT.equals(suggestBox.getText())) {
                    suggestBox.setText("");
                }
            }
        });
        return suggestBox;
    }

    private void onChangedSearchMenuItem(CheckMenuItem checkItem) {
        this.sbMenuButton.setText(checkItem.getText());
        this.instrumentSuggestOracle.setEnabled(checkItem.getId().endsWith(TopToolbar.WP_SEARCH_KEY)
                && isSuggestAllowed());
    }

    private CheckMenuItem createItem(String id, String name, final boolean checked) {
        final CheckMenuItem result = new CheckMenuItem(name);
        result.setChecked(checked);
        result.setId(SEARCH_BOX_ID_PREFIX + id);
        result.setGroup("sbitem"); // $NON-NLS-0$
        result.addSelectionListener(this.checkItemListener);
        return result;
    }

    private void addSearchWidgets() {
        this.sbMenuButton = new SplitButton(getSelectedSbItem().getText());
        this.sbMenuButton.addListener(Events.Select, new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent buttonEvent) {
                cycleItems();
                onChangedSearchMenuItem();
            }
        });

        final Menu sbMenu = new Menu();
        for (final CheckMenuItem sbMenuItem : this.sbMenuItems) {
            sbMenu.add(sbMenuItem);
        }
        this.sbMenuButton.setMenu(sbMenu);
        addWidget(this.sbMenuButton);

        final SuggestBox suggestBox = createSuggestBox();
        addWidget(suggestBox);

        final Button buttonSearch = new Button();
        buttonSearch.addStyleName("mm-topToolbar-searchButton"); // $NON-NLS-0$
        buttonSearch.addListener(Events.Select, new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent buttonEvent) {
                if (!TopToolbar.SEARCH_DEFAULT.equals(suggestBox.getText())) {
                    search(suggestBox.getText());
                }
            }
        });
        addWidget(buttonSearch);
    }

    private void onChangedSearchMenuItem() {
        onChangedSearchMenuItem(getSelectedSbItem());
    }

    @Override
    public Widget asWidget() {
        return mainPanel;
    }

    private void addWidget(Widget widget) {
        this.mainPanel.add(new FloatPanel(widget));
    }

    class FloatPanel extends SimplePanel {
        public FloatPanel(Widget widget) {
            super();
            this.setStyleName("mm-floatWidget"); // $NON-NLS-0$
            this.setWidget(widget);
        }

    }
}
