package de.marketmaker.iview.mmgwt.mmweb.client.watchlist;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.user.client.ui.Grid;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.ResponseTypeCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.StructPieSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.WatchlistPortfolioUtil;

/**
 * PortfolioVisualizationView.java
 * Created on Sep 24, 2009 10:24:50 AM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
class PortfolioVisualizationContainer implements ActionPerformedHandler {

    private final StructPieSnippet[] pieSnippets = new StructPieSnippet[4];

    private final ContentPanel view;

    private final DmxmlContext context = new DmxmlContext();

    private final String uid;

    private String lastPortfolioid;

    static PortfolioVisualizationContainer create(String uid) {
        final PortfolioVisualizationContainer result = new PortfolioVisualizationContainer(uid);
        EventBusRegistry.get().addHandler(ActionPerformedEvent.getType(), result);
        return result;
    }

    private PortfolioVisualizationContainer(String uid) {
        this.uid = uid;
        this.view = newContentPanel();
        this.view.setHeaderVisible(false);
        this.view.setBodyBorder(false);
        this.view.setBorders(false);
        this.view.addStyleName("mm-content"); // $NON-NLS-0$
        this.view.add(createGrid());
    }

    private Grid createGrid() {
        final SnippetConfiguration conf = new SnippetConfiguration()
                .with("width", "600") // $NON-NLS$
                .with("blockType", "PF_Visualization") // $NON-NLS$
                .with("userid", this.uid); // $NON-NLS$

        final String[] types = new String[]{"TYP", "LAND", "WAEHRUNG", "ASSET"}; // $NON-NLS$
        final String[] titles = new String[]{I18n.I.type(), I18n.I.country(), I18n.I.currency(), I18n.I.asset()}; 

        final Grid result = new Grid(4, 1);

        for (int i = 0; i < types.length; i++) {
            this.pieSnippets[i] = new StructPieSnippet(this.context,
                    conf.copy().with("type", types[i]).with("title", titles[i])); // $NON-NLS$

            final ContentPanel container = newContentPanel();
            this.pieSnippets[i].getView().setContainer(container);

            result.setWidget(i, 0, container);
            result.getCellFormatter().setStyleName(i, 0, "mm-gridSnippets"); // $NON-NLS-0$
        }
        return result;
    }

    private ContentPanel newContentPanel() {
        final ContentPanel panel = new ContentPanel();
        panel.setAutoHeight(true);
        panel.setAutoWidth(true);
        return panel;
    }

    void update(String portfolioid) {
        if (StringUtil.equals(this.lastPortfolioid, portfolioid)) {
            return;
        }
        updateSnippets(portfolioid);

        this.context.issueRequest(new ResponseTypeCallback() {
            protected void onResult() {
                for (StructPieSnippet snippet : pieSnippets) {
                    snippet.updateView();
                }
            }
        });
    }

    private void updateSnippets(String portfolioid) {
        this.lastPortfolioid = portfolioid;
        for (StructPieSnippet snippet : this.pieSnippets) {
            snippet.setPortfolioId(portfolioid);
        }
    }

    ContentPanel getView() {
        return this.view;
    }

    public String getPrintHtml(String portfolioName, String initialInvestment, String cash,
            String realizedGain, String portfolioCurrency) {
        final AppConfig config = SessionData.INSTANCE.getUser().getAppConfig();
        String result = "";   // $NON-NLS$
        result += WatchlistPortfolioUtil.getPrintHeadWithDate(portfolioName);
        result += "<br>";  // $NON-NLS$
        result += getView().getElement().getInnerHTML();
        result += "<div class=\"mm-printFooter\" >"; // $NON-NLS$
        // investment
        result += I18n.I.investment() + ": " + Renderer.LARGE_PRICE_MAX2.render(initialInvestment)  // $NON-NLS$
                + " " + portfolioCurrency + " ";   // $NON-NLS$
        // cash
        if (config.getBooleanProperty(AppConfig.SHOW_CASH_IN_PORTFOLIO, true)) {
            result += I18n.I.moneyAsCash() + ": " + Renderer.LARGE_PRICE_MAX2.render(cash)  // $NON-NLS$
                + " " + portfolioCurrency + " ";  // $NON-NLS$
        }
        // profit
        result += I18n.I.realizedProfit() + ": " + Renderer.LARGE_PRICE_MAX2.render(realizedGain) // $NON-NLS$
                + " " + portfolioCurrency;  // $NON-NLS$
        result += "</div>";  // $NON-NLS$
        return result;
    }

    public void onAction(ActionPerformedEvent event) {
        if (isPortfolioAction(event)) {
            // erase current portfolioid so that reload will happen even if portfolioid didn't change
            updateSnippets("");
        }
    }

    private boolean isPortfolioAction(ActionPerformedEvent event) {
        return event.getKey().startsWith("X_PF"); // $NON-NLS-0$
    }
}
