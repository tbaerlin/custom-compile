/*
 * NewsHeadlinesSnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ChartUrlFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateTimeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BasicChartSnippetView<S extends BasicChartSnippet<S, V>, V extends BasicChartSnippetView<S, V>> extends
        SnippetView<S> implements LoadHandler, ErrorHandler, ClickHandler {

    public static final String INTRADAY_NOT_ALLOWED = I18n.I.messageNoAccessToIntradayTickdata();

    protected static final DateRenderer DATE_OR_TIME = DateRenderer.dateOrTime(true, "--");

    private FlowPanel panel = new FlowPanel();

    private Image image = new Image();

    private HTML footer = new HTML();

    private SimplePanel printCaption = new SimplePanel();

    private HTML message = new HTML();

    protected PlaceChangeEvent linkToChartcenter;

    private final boolean nameInTitle;

    public BasicChartSnippetView(S snippet) {
        super(snippet);
        this.nameInTitle = snippet.getConfiguration().getBoolean("nameInTitle", false); // $NON-NLS-0$
        setTitle(snippet.getTitle());

        this.image.setStyleName("mm-chartlink"); // $NON-NLS-0$
        this.image.addClickHandler(this);
        this.image.addLoadHandler(this);
        this.image.addErrorHandler(this);

        this.message.setStylePrimaryName("mm-snippet-chart-message"); // $NON-NLS-0$
        this.message.setVisible(false);

        this.footer.setStylePrimaryName("mm-snippet-chart-footer"); // $NON-NLS-0$
        this.printCaption.setStyleName("mm-onlyPrint-block"); // $NON-NLS-0$

        this.panel.add(this.message);
        this.panel.add(this.image);
        this.panel.add(this.footer);
        this.panel.add(this.printCaption);
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();
        this.container.setContentWidget(this.panel);
    }

    public void onLoad(LoadEvent loadEvent) {
        this.image.setVisible(true);
        Tooltip.addQtipLabel(this.image, I18n.I.gotoChartcenter());
        this.message.setVisible(false);
    }

    public void onError(ErrorEvent errorEvent) {
        update(I18n.I.messageChartNotAvailable(), false);
    }

    protected void showMessage(final String s) {
        this.image.setVisible(false);
        setText(this.message, s, true);
    }

    public void onClick(ClickEvent clickEvent) {
        if (this.linkToChartcenter != null) {
            goToChartcenter(this.linkToChartcenter);
        }
    }

    protected void goToChartcenter(PlaceChangeEvent event) {
        PlaceUtil.fire(event);
    }

    protected void update(IMGResult ipr) {
        setImageUrl(ipr, ipr.getInstrumentdata().getType());
        if (this.nameInTitle) {
            StringUtil.reduceCurrencyNameLength(ipr.getInstrumentdata());
            getConfiguration().put("title", ipr.getInstrumentdata().getName()); // $NON-NLS-0$
            reloadTitle();
        }
        updateFooter(ipr);
    }

    protected void push(IMGResult ipr) {
        updateFooter(ipr);
    }

    protected void setImageUrl(IMGResult ipr, final String instrumentType) {
        if (isChartAllowed(ipr)) {
            final String imageUrl = getImageUrl(ipr);
            this.image.setUrl(imageUrl);
            this.linkToChartcenter = new PlaceChangeEvent(PlaceUtil.getPortraitPlace(instrumentType,
                    ipr.getQuotedata().getQid(), "C")); // $NON-NLS$
            addConfigValue(this.linkToChartcenter, "percent", "percent"); // $NON-NLS$
            addConfigValuePeriod(this.linkToChartcenter);
        }
        else {
            showMessage(I18n.I.chartIntradayNotAllowed());
            this.linkToChartcenter = null;
        }
    }

    private void addConfigValue(PlaceChangeEvent event, String configKey, String propertyName) {
        final String value = this.snippet.getConfiguration().getString(configKey);
        if (value != null) {
            event.withProperty(propertyName, value);
        }
    }

    private void addConfigValuePeriod(PlaceChangeEvent event) {
        final SnippetConfiguration config = this.snippet.getConfiguration();
        final String value = config.getString("period"); // $NON-NLS$
        if (value != null) {
            event.withProperty("period", value); // $NON-NLS$
        }
        final String from = config.getString("from"); // $NON-NLS$
        final String to = config.getString("to"); // $NON-NLS$
        if ("start".equals(from) && "today".equals(to)) { // $NON-NLS$
            event.withProperty("period", DateTimeUtil.PERIOD_KEY_ALL); // $NON-NLS$
        }
    }

    private boolean isChartAllowed(IMGResult ipr) {
        if (ipr.getPricedata() == null) {
            return true;
        }
        if (!"END_OF_DAY".equals(ipr.getPricedata().getPriceQuality())) { // $NON-NLS-0$
            return true;
        }
        final String period = this.snippet.getConfiguration().getString("period"); // $NON-NLS-0$
        return period == null || !period.endsWith("D"); // $NON-NLS-0$
    }

    void update(String s, boolean asHtml) {
        setFooterText(s, asHtml);
        this.image.setVisible(false);
    }

    protected void setFooterText(String s, boolean asHtml) {
        setText(this.footer, s, asHtml);
    }

    protected void setPrintCaption(Widget widget) {
        this.printCaption.setWidget(widget);
    }

    private void setText(HTML widget, String s, boolean asHtml) {
        if (asHtml) {
            widget.setHTML(s);
        }
        else {
            widget.setText(s);
        }
        widget.setVisible(true);
    }

    protected void updateFooter(IMGResult ipr) {
        final Price price = Price.create(ipr);
        setFooterText(
                Formatter.formatNumber(price.getLastPrice().getPrice(), "-", false)
                        + "  " + DATE_OR_TIME.render(price.getDate())
                        + "  " + Formatter.formatNumber(price.getChangeNet(), "-", true)
                        + "  (" + Formatter.formatPercent(price.getChangePercent(), "-", true)
                        + ")  " + ipr.getQuotedata().getMarketName()
                , false);
    }

    protected Image getImage() {
        return this.image;
    }

    protected String getImageUrl(IMGResult ipr) {
        return ChartUrlFactory.getUrl(ipr.getRequest());
    }

    protected boolean isConfiguredPeriod(String period) {
        if (period == null || DateTimeUtil.PERIOD_KEY_ALL.equals(period)) {
            return "start".equals(this.snippet.getConfiguration().getString("from")); // $NON-NLS$
        }
        return period.equals(this.snippet.getConfiguration().getString("period")); // $NON-NLS$
    }
}
