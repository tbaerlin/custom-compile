/*
 * DashboardWidgetNotAvailableSnippet.java
 *
 * Created on 16.09.2015 10:05
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.dashboard.DashboardSnippetErrorUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTextView;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * @author mdick
 */
public class DashboardWidgetNotAvailableSnippet extends
        AbstractSnippet<DashboardWidgetNotAvailableSnippet, SnippetTextView<DashboardWidgetNotAvailableSnippet>> {
    public static class Class extends SnippetClass {
        private final boolean marketDataSnippet;

        public Class(String name, boolean marketDataSnippet) {
            super(name);
            this.marketDataSnippet = marketDataSnippet;
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new DashboardWidgetNotAvailableSnippet(context, config, this.marketDataSnippet);
        }
    }

    DashboardWidgetNotAvailableSnippet(DmxmlContext context, SnippetConfiguration config,
            boolean marketDataSnippet) {
        super(context, config);
        this.setView(new SnippetTextView<>(this));
        this.getView().setHtml(DashboardSnippetErrorUtil.getErrorHtml(
                I18n.I.dashboardWidgetNotAvailableText(),
                (marketDataSnippet ?
                        I18n.I.dashboardWidgetNotAvailableTooltipNoMarketData() :
                        I18n.I.dashboardWidgetNotAvailableTooltipMissingLayout())));
    }

    public void destroy() {
    }

    public void updateView() {
    }
}
