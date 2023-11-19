/*
 * GenericListSnippetView.java
 *
 * Created on 4/20/15
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.HTML;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingWidgets;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.view.PagingPanel;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author Stefan Willenbrock
 */
@NonNLS
public class GenericListSnippetView<T extends GenericListSnippet<T>> extends SnippetView<T> {

    private final SnippetTableView<T> table;
    
    private SelectButton issuerBox, typeBox, investFocusBox, alphabetBox, maturityBox;

    private PagingPanel pp;

    private boolean withIssuer, withType, withInvestFocus, withAlphabet, withMaturity;

    private boolean hasAlreadyTitle;

    public GenericListSnippetView(T snippet, SnippetTableView<T> table) {
        super(snippet);

        final SnippetConfiguration configuration = snippet.getConfiguration();
        setTitle(configuration.getString("title"));
        this.withIssuer = configuration.getBoolean("withIssuer", false);
        this.withType = configuration.getBoolean("withType", false);
        this.withInvestFocus = configuration.getBoolean("withInvestFocus", false);
        this.withAlphabet = configuration.getBoolean("withAlphabet", false);
        this.withMaturity = configuration.getBoolean("withMaturity", false);

        this.table = table;
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();

        if (this.snippet.hasPagingPanel()) {
            final FloatingToolbar toolbar = new FloatingToolbar();

            final SelectionHandler queryHandler = new SelectionHandler() {
                @Override
                public void onSelection(SelectionEvent event) {
                    snippet.setQuery();
                    snippet.ackNewOffset(0);
                }
            };

            this.typeBox = addSelectButton(this.withType, this.snippet.getTypeLabel(), toolbar, queryHandler);
            this.issuerBox = addSelectButton(this.withIssuer, this.snippet.getIssuerLabel(), toolbar, queryHandler);
            this.investFocusBox = addSelectButton(this.withInvestFocus, this.snippet.getInvestFocusLabel(), toolbar, queryHandler);
            this.alphabetBox = addSelectButton(this.withAlphabet, "Alph. overview", toolbar, queryHandler);
            this.maturityBox = addSelectButton(this.withMaturity, I18n.I.maturity(), toolbar, queryHandler);

            toolbar.addFill();

            final PagingWidgets.Config pwc = new PagingWidgets.Config()
                    .withToolbar(toolbar)
                    .withMode(PagingWidgets.Mode.FULL);
            final PagingPanel.Config ppc = new PagingPanel.Config(this.container)
                    .withWidgetsConfig(pwc);
            this.pp = new PagingPanel(ppc);
            this.pp.setHandler(snippet);

        }

        this.table.setContainer(this.container);
    }

    private SelectButton addSelectButton(boolean enabled, String title, FloatingToolbar toolbar, SelectionHandler handler) {
        if (enabled) {
            final SelectButton sb = new SelectButton().withMenu(new Menu());
            sb.setClickOpensMenu(true);
            if (title != null) {
                toolbar.add(new HTML("<span style='margin: 0 0.5em 0 " + (this.hasAlreadyTitle ? "1em" : "0") + "'>" + title + ":</span>"));
            }
            toolbar.add(sb);
            this.hasAlreadyTitle = true;
            sb.addSelectionHandler(handler);
            return sb;
        }
        return null;
    }

    public void update(TableDataModel tdm, int offset, int count, int total) {
        this.table.update(tdm);
        if (this.pp != null) {
            this.pp.update(offset, count, total);
        }
    }

    public String getIssuerValue() {
        return getValue(this.issuerBox);
    }

    public String getTypeValue() {
        return getValue(this.typeBox);
    }

    public String getInvestFocusValue() {
        return getValue(this.investFocusBox);
    }

    public String getMaturityValue() {
        return getValue(this.maturityBox);
    }

    private String getValue(SelectButton sb) {
        final MenuItem item = sb.getSelectedItem();
        return item == null ? "" : (String) item.getData("value");
    }

    public SelectButton getIssuerBox() {
        return issuerBox;
    }

    public SelectButton getTypeBox() {
        return typeBox;
    }

    public SelectButton getInvestFocusBox() {
        return investFocusBox;
    }

    public SelectButton getAlphabetBox() {
        return alphabetBox;
    }

    public SelectButton getMaturityBox() {
        return maturityBox;
    }

    protected void setSortLinkListener(LinkListener<String> sortLinkListener) {
        this.table.setSortLinkListener(sortLinkListener);
    }
}
