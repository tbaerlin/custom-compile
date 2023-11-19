/*
 * MarketSelectionButton.java
 *
 * Created on 23.05.2008 16:00:11
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.style.Styles;

import java.util.List;

/**
 * A ToolbarMenuButton that allows to select a quote from all instrument quotes by
 * selecting a specific market by name.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MarketSelectionButton implements IsWidget {
    public interface Callback {
        void updateQuote(String qid);
    }

    public interface NameStrategy {
        String getName(QuoteData data);
    }

    public static NameStrategy NAME_STRATEGY = QuoteData::getMarketName;

    private String getButtonText(String text) {
        if (text.length() <= this.maxTextLen) {
            return text;
        }
        for (int i = this.maxTextLen - 2; i-- > 0; ) {
            if (text.charAt(i) == ' ') {
                return text.substring(0, i) + "..."; // $NON-NLS$
            }
        }
        return text.substring(0, this.maxTextLen - 3) + "..."; // $NON-NLS$
    }

    private SelectButton button;

    private Menu quotesMenu;

    private int maxTextLen;

    public MarketSelectionButton(final Callback callback, int maxTextLen) {
        this(callback, maxTextLen, Button.getRendererType());
    }

    public MarketSelectionButton(final Callback callback, int maxTextLen,
            Button.RendererType rendererType) {
        this.maxTextLen = maxTextLen;
        this.quotesMenu = new Menu();
        this.button = new SelectButton(rendererType)
                .withMenu(this.quotesMenu)
                .withClickOpensMenu()
                .withNoSelectionText(SafeHtmlUtils.fromString(I18n.I.market1()))
                .withSelectionHandler(event -> {
                    final MenuItem selectedItem = event.getSelectedItem();
                    if (selectedItem != null) {
                        callback.updateQuote((String) selectedItem.getData("qid")); // $NON-NLS$
                    }
                })
                .withSelectionTextStrategy(menuItem -> {
                    return SafeHtmlUtils.fromString((String) menuItem.getData("buttonText")); // $NON-NLS$
                });
        this.button.setEnabled(false);
    }

    public MarketSelectionButton withFormStyling() {
        Styles.tryAddStyles(this.button, Styles.get().comboBox(), Styles.get().comboBoxWidth180());
        return this;
    }

    public void updateQuotesMenu(List<QuoteData> listQuotes, QuoteData selected) {
        this.quotesMenu.removeAll();
        for (QuoteData data : listQuotes) {
            final String name = NAME_STRATEGY.getName(data);
            this.quotesMenu.add(new MenuItem(name)
                    .withData("data", data) // $NON-NLS$
                    .withData("qid", data.getQid()) // $NON-NLS$
                    .withData("buttonText", getButtonText(name)) // $NON-NLS$
            );
        }
        this.button.setSelectedData("qid", selected.getQid(), false); // $NON-NLS$
        this.button.setEnabled(true);
    }

    @Override
    public Widget asWidget() {
        return this.button;
    }

    public void setEnabled(boolean enabled) {
        this.button.setEnabled(enabled);
    }
}
