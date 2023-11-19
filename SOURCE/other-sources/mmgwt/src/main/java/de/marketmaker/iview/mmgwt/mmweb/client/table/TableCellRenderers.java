/*
 * TableCellRenderers.java
 *
 * Created on 24.03.2008 10:26:50
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.table;

import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.SimplePanel;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.iview.dmxml.GISFinderElement;
import de.marketmaker.iview.dmxml.MSCPriceDataExtendedElement;
import de.marketmaker.iview.dmxml.NWSSearchElement;
import de.marketmaker.iview.dmxml.PageSummary;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.Token;
import de.marketmaker.iview.mmgwt.mmweb.client.VRFinderLinkMenu;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.dzwgz.DzPibReportLinkController;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.ContentFlagsEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Image;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.data.PriceWithSupplement;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderResearch;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.terminalpages.VwdPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.util.AlertStatusRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ContentFlagUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.MultiRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.MultiRendererImpl;
import de.marketmaker.iview.mmgwt.mmweb.client.util.NewsUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PriceStringRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PriceWithSuffixRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.RecommendationRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.DzPibMarginDialog;

import static com.google.gwt.dom.client.Style.VerticalAlign.MIDDLE;
import static de.marketmaker.iview.mmgwt.mmweb.client.Selector.WGZ_BANK_USER;
import static de.marketmaker.iview.mmgwt.mmweb.client.util.StringBasedNumberFormat.ROUND_2;

/**
 * Provides public inner classes implementing TableCellRenderer.
 * <p/>
 * If working with toolTips, ensure that the Component which hosts the table
 * is known by a QuickTip instance (e.g. new QuickTip(myPanelWhichHostsATable)).
 * This is complied by MainViews centerPanel.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TableCellRenderers {
    public static final TableCellRenderer CHANGE_NET
            = new DelegateRenderer<>(Renderer.CHANGE_PRICE, "mm-right"); // $NON-NLS-0$

    public static final TableCellRenderer CHANGE_PERCENT
            = new DelegateRenderer<>(Renderer.CHANGE_PERCENT, "mm-right"); // $NON-NLS-0$

    public static final TableCellRenderer COMPACT_DATETIME
            = new DelegateRenderer<>(DateRenderer.compactDateTime("--")); // $NON-NLS-0$

    public static final TableCellRenderer DATE_OR_TIME
            = new DelegateRenderer<>(DateRenderer.dateOrTime(false, "--")); // $NON-NLS-0$

    public static final TableCellRenderer COMPACT_DATE_OR_TIME
            = new DelegateRenderer<>(DateRenderer.dateOrTime(true, "--")); // $NON-NLS-0$

    public static final TableCellRenderer COMPACT_TIME
            = new DelegateRenderer<>(DateRenderer.compactTime("--")); // $NON-NLS-0$

    public static final TableCellRenderer DATE = new DelegateRenderer<>(DateRenderer.date("")); // $NON-NLS-0$

    public static final TableCellRenderer DATE_AND_TIME
            = new DelegateRenderer<>(DateRenderer.dateAndTime("--"), "mm-right"); // $NON-NLS-0$ $NON-NLS-1$

    public static final TableCellRenderer LOCAL_TZ_DATE_TIME
            = new DelegateRenderer<>(DateRenderer.localTimezoneDateTime("--"), "mm-right"); // $NON-NLS-0$ $NON-NLS-1$

    public static final TableCellRenderer DATE_RIGHT
            = new DelegateRenderer<>(DateRenderer.date(""), "mm-right"); // $NON-NLS-0$ $NON-NLS-1$

    public static final TableCellRenderer FULL_TIME_LEFT
            = new DelegateRenderer<>(DateRenderer.fullTime(""), "mm-left"); // $NON-NLS-0$ $NON-NLS-1$

    public static final TableCellRenderer MULTI_PRICE = new MultiDelegateRenderer(
            new MultiRendererImpl<>(Renderer.PRICE, "&nbsp;/&nbsp;", "--"), "mm-center"); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$

    public static final TableCellRenderer MULTILINE_PRICE = new MultiDelegateRenderer(
            new MultiRendererImpl<>(Renderer.PRICE, "<br>", "--"), "mm-center"); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$

    public static final TableCellRenderer MULTILINE_ARRAY = new MultiDelegateRenderer(
            new MultiRendererImpl<>(Renderer.STRING_DOUBLE_DASH, "<br>", "--"), "mm-left"); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$

    public static final TableCellRenderer PERCENT
            = new DelegateRenderer<>(Renderer.PERCENT, "mm-right"); // $NON-NLS-0$

    public static final TableCellRenderer PRICE_PERCENT =
            new DelegateRenderer<>(new PriceWithSuffixRenderer(Renderer.PRICE, "%"), "mm-right"); // $NON-NLS-0$ $NON-NLS-1$

    public static final TableCellRenderer PRICE
            = new DelegateRenderer<>(Renderer.PRICE, "mm-right"); // $NON-NLS-0$

    public static final TableCellRenderer PRICE_WITH_SUPPLEMENT
            = new DelegateRenderer<>(Renderer.PRICE_WITH_SUPPLEMENT, "mm-right"); // $NON-NLS-0$

    public static final TableCellRenderer PRICE_2
            = new DelegateRenderer<>(new PriceStringRenderer(ROUND_2, "--"), "mm-right"); // $NON-NLS-0$ $NON-NLS-1$

    public static final TableCellRenderer PRICE23
            = new DelegateRenderer<>(PriceStringRenderer.PRICE23, "mm-right"); // $NON-NLS-0$

    public static final TableCellRenderer PRICE25
            = new DelegateRenderer<>(PriceStringRenderer.PRICE25, "mm-right"); // $NON-NLS-0$

    public static final TableCellRenderer PRICE_MAX2
            = new DelegateRenderer<>(Renderer.PRICE_MAX2, "mm-right"); // $NON-NLS-0$

    public static final TableCellRenderer PRICE_LEFT
            = new DelegateRenderer<>(Renderer.PRICE, "mm-left"); // $NON-NLS-0$

    public static final TableCellRenderer QUOTELINK_18 = new QuoteLinkRenderer(18, "n/a"); // $NON-NLS-0$

    public static final TableCellRenderer QUOTELINK_22 = new QuoteLinkRenderer(22, "n/a"); // $NON-NLS-0$

    public static final TableCellRenderer QUOTELINK_32 = new QuoteLinkRenderer(32, "n/a"); // $NON-NLS-0$

    public static final TableCellRenderer QUOTELINK_42 = new QuoteLinkRenderer(42, "n/a"); // $NON-NLS-0$

    public static final TableCellRenderer QUOTELINK_32_WITH_CURRENCY = new TableCellRenderers.QuoteLinkRendererWithCurrency(32, "n/a"); // $NON-NLS-0$

    public static final TableCellRenderer OPTIONAL_QUOTELINK_32 = new OptionalQuoteLinkRenderer(32, "n/a", "nicht börsennotiertes Instrument"); // $NON-NLS-0$ $NON-NLS-1$

    public static final TableCellRenderer OPTIONAL_QUOTELINK_27 = new OptionalQuoteLinkRenderer(27, "n/a", "nicht börsennotiertes Instrument"); // $NON-NLS-0$ $NON-NLS-1$

    public static final TableCellRenderer OPTIONAL_QUOTELINK_18 = new OptionalQuoteLinkRenderer(18, "n/a", "nicht börsennotiertes Instrument"); // $NON-NLS-0$ $NON-NLS-1$

    public static final TableCellRenderer LINK_32 = new LinkRenderer(32, ""); // $NON-NLS-0$

    public static final TableCellRenderer LINK_90 = new LinkRenderer(90, ""); // $NON-NLS-0$

    public static final TableCellRenderer MARKETLINK_22 = new MarketLinkRenderer(22, ""); // $NON-NLS-0$

    public static final TableCellRenderer MARKETNAME_22 = new MarketNameRenderer(22, ""); // $NON-NLS-0$

    public static final TableCellRenderer VWDCODELINK_22 = new VwdcodeLinkRenderer(22, ""); // $NON-NLS-0$

    public static final TableCellRenderer STRING = new StringRenderer("--"); // $NON-NLS-0$

    public static final TableCellRenderer STRING_CENTER = new StringRenderer("", "mm-center"); // $NON-NLS-0$ $NON-NLS-1$

    public static final TableCellRenderer STRING_EMPTY = new StringRenderer(""); // $NON-NLS-0$

    public static final TableCellRenderer STRING_LEFT = new StringRenderer("", "mm-left"); // $NON-NLS-0$ $NON-NLS-1$

    public static final TableCellRenderer STRING_RIGHT = new StringRenderer("", "mm-right"); // $NON-NLS-0$ $NON-NLS-1$

    public static final TableCellRenderer HTML = new HtmlRenderer("--"); // $NON-NLS-0$

    public static final TableCellRenderer LABEL = new StringRenderer("", "mm-snippetTable-label"); // $NON-NLS-0$ $NON-NLS-1$

    public static final TableCellRenderer DEFAULT_LABEL
            = new DefaultRenderer("", "mm-snippetTable-label"); // $NON-NLS-0$ $NON-NLS-1$

    public static final TableCellRenderer LABEL_CENTER
            = new StringRenderer("", "mm-snippetTable-label mm-center"); // $NON-NLS-0$ $NON-NLS-1$

    public static final TableCellRenderer DEFAULT = new DefaultRenderer(null);

    public static final TableCellRenderer DEFAULT_28 = new DefaultRenderer(null, null, 28);

    public static final TableCellRenderer DEFAULT_CENTER = new DefaultRenderer(null, "mm-center"); // $NON-NLS-0$

    public static final TableCellRenderer DEFAULT_RIGHT = new DefaultRenderer(null, "mm-right"); // $NON-NLS-0$

    public static final TableCellRenderer STRING_10 = new MaxLengthStringRenderer(10, "--"); // $NON-NLS-0$

    public static final TableCellRenderer VOLUME = new DelegateRenderer<>(Renderer.VOLUME, "mm-right"); // $NON-NLS-0$

    public static final TableCellRenderer VOLUME_LONG
            = new DelegateRenderer<>(Renderer.VOLUME_LONG, "mm-right"); // $NON-NLS-0$

    public static final TableCellRenderer LARGE_NUMBER
            = new DelegateRenderer<>(Renderer.LARGE_NUMBER, "mm-right"); // $NON-NLS-0$

    public static final TableCellRenderer TURNOVER
            = new DelegateRenderer<>(Renderer.TURNOVER, "mm-right"); // $NON-NLS-0$

    public static final TableCellRenderer TRENDBAR
            = new DelegateRenderer<>(Renderer.TRENDBAR);

    public static final TableCellRenderer RSC_RECOMMENDATION
            = new DelegateRenderer<>(Renderer.RSC_RECOMMENDATION);

    public static final TableCellRenderer RESEARCH_RECOMMENDATION
            = new DelegateRenderer<>(RecommendationRenderer.createResearchRecommendationRenderer());

    public static final TableCellRenderer EXTEND_BAR_LEFT
            = new DelegateRenderer<>(Renderer.EXTEND_BAR_LEFT, "mm-extendBar"); // $NON-NLS-0$

    public static final TableCellRenderer EXTEND_BAR_RIGHT
            = new DelegateRenderer<>(Renderer.EXTEND_BAR_RIGHT, "mm-extendBar"); // $NON-NLS-0$

    public static final TableCellRenderer ALERT_STATUS
            = new DelegateRenderer<>(AlertStatusRenderer.INSTANCE, "mm-right"); // $NON-NLS-0$

    public static final TableCellRenderer VWD_PAGENUMER_LINK =
            new PageSummaryRenderer(PageSummaryRenderer.Field.PAGENUMBER, 15);

    public static final TableCellRenderer VWD_PAGE_HEADING_LINK =
            new PageSummaryRenderer(PageSummaryRenderer.Field.HEADING, 70);

    public static final TableCellRenderer DATE_OR_TIME_PUSH
            = new PushRenderer<String>(DateRenderer.dateOrTime(false, "--"), "mm-center") { // $NON-NLS-0$ $NON-NLS-1$

        public String getValue(Price price) {
            return price.getDate() != null ? price.getDate() : price.getBidAskDate();
        }
    };

    public static final TableCellRenderer DATE_PUSH
            = new PushRenderer<String>(DateRenderer.date("--"), "mm-center") { // $NON-NLS-0$ $NON-NLS-1$

        public String getValue(Price price) {
            return price.getDate() != null ? price.getDate() : price.getBidAskDate();
        }
    };

    public static final TableCellRenderer VR_ICON_LINK = new VRIconLinkRenderer();

    public static final TableCellRenderer DZ_RESEARCH_POPUP_ICON_LINK = new DzResearchPopupIconLinkCellRenderer();

    public static final TableCellRenderer DZPIB_DOWNLOAD_ICON_LINK_QWI = new DzPibDownloadIconLinkCellRendererQwI();

    public static final TableCellRenderer DZPIB_DOWNLOAD_ICON_LINK_URL = new DzPibDownloadIconLinkCellRendererUrl();

    public static final TableCellRenderer DZPIB_DOWNLOAD_ICON_LINK_ELEM = new DzPibDownloadIconLinkCellRendererElem();

    public static final TableCellRenderer DATE_OR_TIME_COMPACT_PUSH
            = new PushRenderer<String>(DateRenderer.dateOrTime(true, "--"), "mm-center") { // $NON-NLS-0$ $NON-NLS-1$

        public String getValue(Price price) {
            return price.getDate();
        }
    };

    public static final TableCellRenderer BIDASK_DATE_COMPACT_PUSH
            = new PushRenderer<String>(DateRenderer.dateOrTime(true, "--"), "mm-center") { // $NON-NLS-0$ $NON-NLS-1$

        public String getValue(Price price) {
            return price.getBidAskDate();
        }
    };

    public static final TableCellRenderer COMPACT_DATETIME_PUSH
            = new PushRenderer<String>(DateRenderer.compactDateTime("--"), "mm-center") { // $NON-NLS-0$ $NON-NLS-1$

        public String getValue(Price price) {
            return price.getDate();
        }
    };

    public static final TableCellRenderer TIME_PUSH
            = new PushRenderer<String>(DateRenderer.fullTime("--:--:--"), "mm-center") { // $NON-NLS-0$ $NON-NLS-1$

        public String getValue(Price price) {
            return price.getDate();
        }
    };

    public static final TableCellRenderer VOLUME_LONG_PUSH
            = new PushRenderer<Long>(Renderer.VOLUME_LONG, "mm-right") { // $NON-NLS-0$

        public Long getValue(Price price) {
            return price.getVolume();
        }
    };

    public static final TableCellRenderer TURNOVER_PUSH
            = new PushRenderer<String>(Renderer.TURNOVER, "mm-right") { // $NON-NLS-0$

        public String getValue(Price price) {
            return (price == null) ? null : price.getTurnoverDay();
        }
    };

    public static final TableCellRenderer TRADE_LONG_PUSH
            = new PushRenderer<Long>(Renderer.VOLUME_LONG, "mm-right") { // $NON-NLS-0$

        public Long getValue(Price price) {
            return price.getNumTrades();
        }
    };

    public static final TableCellRenderer LOW_PUSH
            = new PushCompareRenderer<String>(Renderer.PRICE, "mm-right") { // $NON-NLS-0$

        public String getValue(Price price) {
            return price.getLow();
        }

        public int compareWithPrevious(Price p) {
            return p.getPrevious().compareLow(p);
        }
    };

    public static final TableCellRenderer HIGH_PUSH
            = new PushCompareRenderer<String>(Renderer.PRICE, "mm-right") { // $NON-NLS-0$

        public String getValue(Price price) {
            return price.getHigh();
        }

        public int compareWithPrevious(Price p) {
            return p.getPrevious().compareHigh(p);
        }
    };

    public static final TableCellRenderer BID_PUSH
            = new PushCompareRenderer<String>(Renderer.PRICE, "mm-right") { // $NON-NLS-0$

        public String getValue(Price price) {
            return price.getBid();
        }

        public int compareWithPrevious(Price p) {
            return p.getPrevious().compareBid(p);
        }
    };

    public static final TableCellRenderer BID_VOLUME_PUSH
            = new PushRenderer<Long>(Renderer.VOLUME_LONG, "mm-right") { // $NON-NLS-0$

        public Long getValue(Price price) {
            return price.getBidVolume();
        }
    };

    public static final TableCellRenderer ASK_PUSH
            = new PushCompareRenderer<String>(Renderer.PRICE, "mm-right") { // $NON-NLS-0$

        public String getValue(Price price) {
            return price.getAsk();
        }

        @Override
        public int compareWithPrevious(Price p) {
            return p.getPrevious().compareAsk(p);
        }
    };

    public static final TableCellRenderer ASK_VOLUME_PUSH
            = new PushRenderer<Long>(Renderer.VOLUME_LONG, "mm-right") { // $NON-NLS-0$

        public Long getValue(Price price) {
            return price.getAskVolume();
        }
    };

    public static final TableCellRenderer VOLUME_PRICE_PUSH
            = new PushRenderer<Long>(Renderer.VOLUME_LONG, "mm-right") { // $NON-NLS-0$

        public Long getValue(Price price) {
            return price.getVolumePrice();
        }
    };

    public static final PushCompareRenderer LAST_PRICE_WITH_SUPPLEMENT_PUSH
            = new PushCompareRenderer<PriceWithSupplement>(Renderer.PRICE_WITH_SUPPLEMENT, "mm-right") { // $NON-NLS-0$

        @Override
        protected PriceWithSupplement getValue(Price p) {
            return p.getLastPrice();
        }

        @Override
        public int compareWithPrevious(Price p) {
            return p.getPrevious().compareLastPrice(p);
        }

        @Override
        protected boolean isWithChange(Price p) {
            return super.isWithChange(p) && p.getPrevious().getLastPrice().getPrice() != null;
        }
    };

    public static final TableCellRenderer LAST_PRICE_PUSH
            = new PushCompareRenderer<String>(Renderer.PRICE, "mm-right") { // $NON-NLS-0$

        @Override
        protected String getValue(Price p) {
            return p.getLastPrice() != null ? p.getLastPrice().getPrice() : null;
        }

        @Override
        public int compareWithPrevious(Price p) {
            return p.getPrevious().compareLastPrice(p);
        }

        @Override
        protected boolean isWithChange(Price p) {
            return super.isWithChange(p) && p.getPrevious().getLastPrice().getPrice() != null;
        }
    };

    public static final TableCellRenderer PRICE_PERCENT_PUSH
            = new PushCompareRenderer<String>(new PriceWithSuffixRenderer(Renderer.PRICE, "%"), "mm-right") { // $NON-NLS-0$ $NON-NLS-1$

        @Override
        protected String getValue(Price p) {
            return p.getLastPrice() != null ? p.getLastPrice().getPrice() : null;
        }

        @Override
        public int compareWithPrevious(Price p) {
            return p.getPrevious().compareLastPrice(p);
        }

        @Override
        protected boolean isWithChange(Price p) {
            return super.isWithChange(p) && p.getPrevious().getLastPrice().getPrice() != null;
        }
    };

    public static final PushChangeRenderer LAST_PRICE_COMPARE_CHANGE_PUSH
            = new PushChangeRenderer(Renderer.PRICE, null) {
        @Override
        protected String getValue(Price p) {
            return p.getLastPrice() != null ? p.getLastPrice().getPrice() : null;
        }

        @Override
        protected String getSignumStyle(StringBuffer sb, Price p) {
            return getSignumStyle(sb, p.getChangePercent());
        }

        @Override
        public int compareWithPrevious(Price p) {
            return p.getPrevious().compareLastPrice(p);
        }

        @Override
        protected boolean isWithChange(Price p) {
            return super.isWithChange(p) && p.getPrevious().getLastPrice().getPrice() != null;
        }
    };

    public static final TableCellRenderer CHANGE_PERCENT_PUSH
            = new PushChangeRenderer(Renderer.PERCENT, "mm-right") { // $NON-NLS-0$

        protected String getValue(Price p) {
            return p.getChangePercent();
        }

        public int compareWithPrevious(Price p) {
            return p.getPrevious().compareChangePercent(p);
        }
    };

    public static final TableCellRenderer CHANGE_NET_PUSH
            = new PushChangeRenderer(Renderer.PRICE, "mm-right") { // $NON-NLS-0$

        protected String getValue(Price p) {
            return p.getChangeNet();
        }

        public int compareWithPrevious(Price p) {
            return p.getPrevious().compareChangeNet(p);
        }
    };

    public static final TableCellRenderer PREVIOUS_CLOSE_DATE_PUSH
            = new PushRenderer<String>(DateRenderer.date("--"), "mm-right") { // $NON-NLS-0$ $NON-NLS-1$

        public String getValue(Price price) {
            return price.getPreviousCloseDate();
        }
    };

    public static final TableCellRenderer PREVIOUS_PRICE_PUSH
            = new PushRenderer<PriceWithSupplement>(Renderer.PRICE_WITH_SUPPLEMENT, "mm-right") { // $NON-NLS-0$

        public PriceWithSupplement getValue(Price price) {
            return price.getPreviousPrice();
        }
    };

    public static final TableCellRenderer INTERPOLATED_CLOSING_PUSH
            = new PushCompareRenderer<String>(Renderer.PRICE, "mm-right") { // $NON-NLS-0$

        protected String getValue(Price p) {
            return p.getInterpolatedClosing();
        }

        public int compareWithPrevious(Price p) {
            return p.getPrevious().compareInterpolatedClosing(p);
        }
    };

    public static final TableCellRenderer PROVISIONAL_EVALUATION_PUSH
            = new PushCompareRenderer<String>(Renderer.PRICE, "mm-right") { // $NON-NLS-0$

        protected String getValue(Price p) {
            return p.getProvisionalEvaluation();
        }

        public int compareWithPrevious(Price p) {
            return p.getPrevious().compareProvisionalEvaluation(p);
        }
    };

    public static final TableCellRenderer OFFICIAL_ASK_PUSH
            = new PushCompareRenderer<String>(Renderer.PRICE, "mm-right") { // $NON-NLS-0$

        protected String getValue(Price p) {
            return p.getOfficialAsk();
        }

        public int compareWithPrevious(Price p) {
            return p.getPrevious().compareOfficialAsk(p);
        }
    };

    public static final TableCellRenderer OFFICIAL_BID_PUSH
            = new PushCompareRenderer<String>(Renderer.PRICE, "mm-right") { // $NON-NLS-0$

        protected String getValue(Price p) {
            return p.getOfficialBid();
        }

        public int compareWithPrevious(Price p) {
            return p.getPrevious().compareOfficialBid(p);
        }
    };

    public static final TableCellRenderer UNOFFICIAL_ASK_PUSH
            = new PushCompareRenderer<String>(Renderer.PRICE, "mm-right") { // $NON-NLS-0$

        protected String getValue(Price p) {
            return p.getUnofficialAsk();
        }

        public int compareWithPrevious(Price p) {
            return p.getPrevious().compareUnofficialAsk(p);
        }
    };

    public static final TableCellRenderer UNOFFICIAL_BID_PUSH
            = new PushCompareRenderer<String>(Renderer.PRICE, "mm-right") { // $NON-NLS-0$

        protected String getValue(Price p) {
            return p.getUnofficialBid();
        }

        public int compareWithPrevious(Price p) {
            return p.getPrevious().compareUnofficialBid(p);
        }
    };

    public abstract static class GenericPushRenderer<T, K> extends DelegateRenderer<T> {
        GenericPushRenderer(Renderer<T> delegate) {
            this(delegate, null);
        }

        public GenericPushRenderer(Renderer<T> delegate, String contentClass) {
            super(delegate, contentClass);
        }

        @Override
        public void render(Object data, StringBuffer sb, Context context) {
            if (data == null) {
                super.render(data, sb, context);
                return;
            }

            final K p = (K) data;
            if (getValue(p) == null) {
                super.render(null, sb, context);
                return;
            }

            context.setStyle(getStyle(sb, p, context.isPush()));
            final String content = render(getValue(p));
            sb.append(content);
        }

        public boolean isPushRenderer() {
            return SessionData.INSTANCE.isWithPush();
        }

        protected String getStyle(StringBuffer sb, K p, boolean push) {
            return null;
        }

        protected abstract T getValue(K p);
    }

    public abstract static class PushRenderer<T> extends GenericPushRenderer<T, Price> {

        PushRenderer(Renderer<T> delegate) {
            super(delegate);
        }

        public PushRenderer(Renderer<T> delegate, String contentClass) {
            super(delegate, contentClass);
        }
    }

    /*
    public abstract static class PushRenderer<T> extends DelegateRenderer<T> {
        PushRenderer(Renderer<T> delegate) {
            this(delegate, null);
        }

        public PushRenderer(Renderer<T> delegate, String contentClass) {
            super(delegate, contentClass);
        }

        @Override
        public void render(Object data, StringBuffer sb, Context context) {
            if (data == null) {
                super.render(data, sb, context);
                return;
            }

            final Price p = (Price) data;
            if (getValue(p) == null) {
                super.render(null, sb, context);
                return;
            }

            context.setStyle(getStyle(sb, p, context.isPush()));
            final String content = render(getValue(p));
            sb.append(content);
        }

        public boolean isPushRenderer() {
            return SessionData.INSTANCE.isWithPush();
        }

        protected String getStyle(StringBuffer sb, Price p, boolean push) {
            return null;
        }

        protected abstract T getValue(Price p);
    }
    */

    public enum PushColorType {
        changes, updates, none
    }

    public abstract static class GenericPushCompareRenderer<T, K> extends
            GenericPushRenderer<T, K> {
        private static boolean colorUpdates = isColorUpdates();

        private static boolean colorChanges = isColorChanges();

        public static void updatePushStyles() {
            colorUpdates = isColorUpdates();
            colorChanges = isColorChanges();
        }

        public static boolean isColorChanges() {
            return getPushColorType() == PushColorType.changes;
        }

        public static boolean isColorUpdates() {
            return getPushColorType() == PushColorType.updates;
        }

        public static PushColorType getPushColorType() {
            final String pct = SessionData.INSTANCE.getUserProperty(AppConfig.COLOR_PUSH);
            return pct == null ? PushColorType.changes : PushColorType.valueOf(pct);
        }

        protected GenericPushCompareRenderer(Renderer<T> delegate, String contentClass) {
            super(delegate, contentClass);
        }

        protected String getStyle(StringBuffer sb, K p, boolean push) {
            return push ? getChange(p) : null;
        }

        private String getChange(K p) {
            if ((colorChanges || colorUpdates) && isWithChange(p)) {
                final int cmp = compareWithPrevious(p);
                if (cmp < 0) {
                    return "up"; // $NON-NLS-0$
                }
                if (cmp > 0) {
                    return "dn"; // $NON-NLS-0$
                }
                return colorChanges ? null : "eq"; // $NON-NLS-0$
            }
            return null;
        }

        abstract protected boolean isWithChange(K p);

        public abstract int compareWithPrevious(K p);
    }

    public abstract static class PushCompareRenderer<T> extends
            GenericPushCompareRenderer<T, Price> {

        protected PushCompareRenderer(Renderer<T> delegate, String contentClass) {
            super(delegate, contentClass);
        }

        protected boolean isWithChange(Price p) {
            return p.getPrevious() != null && getValue(p.getPrevious()) != null;
        }

        public abstract int compareWithPrevious(Price p);
    }

    public abstract static class ExtendedPushCompareRenderer<T> extends
            GenericPushCompareRenderer<T, MSCPriceDataExtendedElement> {

        protected ExtendedPushCompareRenderer(Renderer<T> delegate, String contentClass) {
            super(delegate, contentClass);
        }

        protected boolean isWithChange(MSCPriceDataExtendedElement e) {
            Price p = Price.create(e);
            return p.getPrevious() != null && getValue(p.getPrevious()) != null;
        }

        protected T getValue(MSCPriceDataExtendedElement e) {
            Price p = Price.create(e);
            return getValue(p);
        }

        abstract T getValue(Price price);

        public abstract int compareWithPrevious(MSCPriceDataExtendedElement e);
    }

    public abstract static class PushChangeRenderer extends PushCompareRenderer<String> {
        protected PushChangeRenderer(Renderer<String> delegate, String contentClass) {
            super(delegate, contentClass);
        }

        @Override
        protected String getStyle(StringBuffer sb, Price p, boolean push) {
            final String change = super.getStyle(sb, p, push);
            final String style = getSignumStyle(sb, p);
            return (change == null) ? style : (style + change);
        }

        protected String getSignumStyle(StringBuffer sb, final Price p) {
            final String result = getSignumStyle(sb, getValue(p));
            if ("p".equals(result)) { // $NON-NLS-0$
                sb.append("+"); // $NON-NLS-0$
            }
            return result;
        }

        protected String getSignumStyle(StringBuffer sb, final String value) {
            if (value == null || "0".equals(value)) { // $NON-NLS$
                return "e"; // equal // $NON-NLS$
            }
            return value.startsWith("-") ? "n" : "p"; // $NON-NLS$
        }
    }

    public static class DefaultRenderer implements TableCellRenderer {
        private final String nullText;

        private final Integer maxTextLength;

        private String contentClass;

        public DefaultRenderer(String nullText) {
            this(nullText, null);
        }

        public DefaultRenderer(String nullText, String contentClass) {
            this(nullText, contentClass, null);
        }

        public DefaultRenderer(String nullText, String contentClass, Integer maxTextLength) {
            this.nullText = nullText;
            this.contentClass = contentClass;
            this.maxTextLength = maxTextLength;
        }

        public String getContentClass() {
            return this.contentClass;
        }

        public void render(Object data, StringBuffer sb, Context context) {
            if (data == null) {
                if (this.nullText != null) {
                    sb.append(this.nullText);
                }
                return;
            }
            if (data instanceof String) {
                sb.append((String) data);
            }
            else if (data instanceof Long) {
                sb.append(Renderer.LARGE_NUMBER.renderLong((Long) data));
            }
            else if (data instanceof QuoteWithInstrument) {
                final QuoteWithInstrument qwi = (QuoteWithInstrument) data;
                final String display = (this.maxTextLength != null && qwi.getName().length() > this.maxTextLength)
                        ? qwi.getName().substring(0, this.maxTextLength) + "..."
                        : qwi.getName();
                renderQuoteLink(context, sb, qwi, qwi.getName(), display,
                        !SessionData.INSTANCE.isAnonymous());

            }
            else if (data instanceof Price) {
                final PriceWithSupplement pws = ((Price) data).getLastPrice();
                sb.append(Renderer.PRICE.render(pws.getPrice()))
                        .append(Renderer.SUPPLEMENT.render(pws.getSupplement()));
            }
            else if (data instanceof PriceWithSupplement) {
                final PriceWithSupplement pws = (PriceWithSupplement) data;
                sb.append(Renderer.PRICE.render(pws.getPrice()))
                        .append(Renderer.SUPPLEMENT.render(pws.getSupplement()));
            }
            else if (data instanceof Link) {
                renderLink(sb, context, (Link) data);
            }
            else if (data instanceof Link[]) {
                final Link[] links = (Link[]) data;
                for (final Link link : links) {
                    sb.append("<div class=\"mm-linkListEntry\">"); // $NON-NLS-0$
                    renderLink(sb, context, link);
                    sb.append("</div>"); // $NON-NLS-0$
                }
            }
            else if (data instanceof Image) {
                sb.append(Renderer.IMAGE.render((Image) data));
            }
            else if (data instanceof CellData) {
                final CellData celldata = (CellData) data;
                final String display = (this.maxTextLength != null && celldata.getRenderedValue().length() > this.maxTextLength)
                        ? celldata.getRenderedValue().substring(0, this.maxTextLength) + "..."
                        : celldata.getRenderedValue();

                sb.append(display);
            }
        }

        public boolean isPushRenderer() {
            return false;
        }
    }

    private static void renderLink(StringBuffer sb, TableCellRenderer.Context context, Link link) {
        renderLink(sb, context, link, link.getText());
    }

    private static void renderLink(StringBuffer sb, TableCellRenderer.Context context, Link link,
            String display) {
        if (link.getListener() != null) {
            context.appendLink(new LinkContext<>(link.getListener(), link), display, link.getText(), sb);
        }
        else {
            sb.append("<a href=\"").append(link.getHref()).append("\""); // $NON-NLS-0$ $NON-NLS-1$
            if (link.getTarget() != null) {
                sb.append(" target=\"").append(link.getTarget()).append("\""); // $NON-NLS-0$ $NON-NLS-1$
            }
            if (link.getTooltip() != null) {
                sb.append(" qtip=\"").append(link.getTooltip()).append("\""); // $NON-NLS-0$ $NON-NLS-1$
            }
            if (link.getStyle() != null) {
                sb.append(" class=\"").append(link.getStyle()).append("\""); // $NON-NLS-0$ $NON-NLS-1$
            }
            sb.append(">").append(display).append("</a>"); // $NON-NLS-0$ $NON-NLS-1$
        }
    }


    public static class DelegateRenderer<T> implements TableCellRenderer {
        private String contentClass;

        private Renderer<T> delegate;

        public DelegateRenderer(Renderer<T> delegate) {
            this(delegate, null);
        }

        public DelegateRenderer(Renderer<T> delegate, String contentClass) {
            this.delegate = delegate;
            this.contentClass = contentClass;
        }

        public String getContentClass() {
            return this.contentClass;
        }

        public Renderer<T> getDelegate() {
            return this.delegate;
        }

        @SuppressWarnings("unchecked")
        public void render(Object data, StringBuffer sb, Context context) {
            sb.append(render((T) data));
        }

        protected String render(T data) {
            return this.delegate.render(data == null ? null : data);
        }

        public boolean isPushRenderer() {
            return false;
        }
    }

    /**
     * Renders its data object as a Hyperlink widget iff the cell's data object is an
     * QuoteWithInstrument instance and the instrument has a defined name. The name of the
     * hyperlink can be restricted to user defined number of characters.
     */
    public static class MarketLinkRenderer extends QuoteLinkRenderer {
        public MarketLinkRenderer(int maxLength, String nullText) {
            super(maxLength, nullText);
        }

        protected String getName(QuoteWithInstrument qwi) {
            return getMarketName(qwi);
        }

        protected static String getMarketName(QuoteWithInstrument qwi) {
            return getMarketName(qwi.getQuoteData());
        }

        public static String getMarketName(QuoteData quoteData) {
            final String market = quoteData.getMarketName();
            if (market == null) {
                return null;
            }

            final String suffixAndCurrency = getSuffixAndCurrency(quoteData);

            if (suffixAndCurrency == null) {
                return market;
            }
            return market + " (" + suffixAndCurrency + ")"; // $NON-NLS$
        }

        private static String getSuffixAndCurrency(QuoteData qd) {
            final String currency = qd.getCurrencyIso();
            if (!FeatureFlags.Feature.NAME_SUFFIX.isEnabled()) {
                return currency;
            }

            final String suffix = qd.getNameSuffix();
            if (suffix == null) {
                return currency;
            }
            if (currency == null || suffix.contains(currency)) {
                return suffix;
            }
            return suffix + " - " + currency; // $NON-NLS$
        }
    }

    /**
     * Renders its data object as plain text iff the cell's data object is an
     * QuoteWithInstrument instance and the instrument has a defined name. The length of the
     * text can be restricted to user defined number of characters.
     */
    public static class MarketNameRenderer extends MaxLengthStringRenderer {
        public MarketNameRenderer(int maxLength, String nullText) {
            super(maxLength, nullText);
        }

        @Override
        protected String getText(final Object data) {
            if (data instanceof QuoteWithInstrument) {
                return MarketLinkRenderer.getMarketName((QuoteWithInstrument) data);
            }
            return null;
        }
    }

    /**
     * Renders its data object as a Hyperlink widget iff the cell's data object is an
     * QuoteWithInstrument instance and the quote has a defined vwdcode. The name of the
     * hyperlink can be restricted to user defined number of characters.
     */
    public static class VwdcodeLinkRenderer extends QuoteLinkRenderer {
        public VwdcodeLinkRenderer(int maxLength, String nullText) {
            super(maxLength, nullText);
        }

        protected String getName(QuoteWithInstrument qwi) {
            return qwi.getQuoteData().getVwdcode();
        }
    }

    /**
     * Extends {@link StringRenderer}, if the resulting text exceeds the user defined maximum
     * length by more than three characters, the string will be clipped and 3 dots will be appended.
     */
    public static class MaxLengthStringRenderer extends StringRenderer {
        protected final int maxLength;

        private final int cutAfterNChars;

        public MaxLengthStringRenderer(int maxLength, String nullText) {
            this(maxLength, nullText, null);
        }

        public MaxLengthStringRenderer(int maxLength, int cutAfterNChars, String nullText) {
            this(maxLength, cutAfterNChars, nullText, null);
        }

        public MaxLengthStringRenderer(int maxLength, String nullText, String textStyle) {
            this(maxLength, -1, nullText, textStyle);
        }


        public MaxLengthStringRenderer(int maxLength, int cutAfterNChars, String nullText,
                String textStyle) {
            super(nullText, textStyle);
            this.maxLength = maxLength;
            this.cutAfterNChars = cutAfterNChars;
        }

        public void render(Object data, StringBuffer sb, Context context) {
            final String text = getText(data);
            if (text == null) {
                super.render(data, sb, context);
                return;
            }

            final String display = getMaxLengthText(text);
            //noinspection StringEquality
            if (text == display) {
                sb.append(display);
            }
            else {
                appendWithQTip(sb, text, display);
            }
        }

        protected StringBuffer appendWithQTip(StringBuffer sb, String text, String display) {
            return sb.append("<span qtip=\"").append(text).append("\">").append(display).append("</span>"); // $NON-NLS$
        }

        protected String getMaxLengthText(String tmp) {
            if (tmp.length() <= this.maxLength + 3) {
                return tmp;
            }
            return getShortVersion(tmp);
        }

        protected String getShortVersion(String tmp) {
            if (this.cutAfterNChars > -1) {
                final StringBuilder result = new StringBuilder();
                result.append(tmp.substring(0, this.cutAfterNChars)).append("...");
                final int tmpMaxLength = this.maxLength - this.cutAfterNChars;
                final String substring = tmp.substring(tmp.length() - tmpMaxLength, tmp.length());
                result.append(substring);
                return result.toString();
            }
            return tmp.substring(0, this.maxLength) + "..."; // $NON-NLS-0$
        }
    }

    public static class MultiLineMaxLengthStringRenderer extends MaxLengthStringRenderer {
        private final int nrOfLines;

        public MultiLineMaxLengthStringRenderer(int nrOfLines, int maxLength, String nullText) {
            super(maxLength, nullText);
            if (maxLength <= 4) {
                throw new IllegalArgumentException("maxLength must be greater than 4"); // $NON-NLS$
            }
            this.nrOfLines = nrOfLines;
        }

        @Override
        protected StringBuffer appendWithQTip(StringBuffer sb, String text, String display) {
                return sb.append("<div style='overflow: auto; text-overflow: ellipsis;' tooltipStyle='mm-tooltipMultiline' qtip=\"")  //$NON-NLS$
                        .append(text).append("\">").append(display).append("</div>");  //$NON-NLS$
//            return sb.append("<span class=\"mm-nobreak\" qtip=\"").append(text).append("\">").append(display).append("</span>"); // $NON-NLS$
        }

        @Override
        protected String getMaxLengthText(String s) {
            final StringBuilder sb = new StringBuilder();
            addShortLines(sb, s, this.nrOfLines);
            return sb.toString();
        }

        private void addShortLines(final StringBuilder sb, final String s, final int nrOfLines) {
            final boolean lastLine = nrOfLines == 1;
            final int startPos = lastLine ? this.maxLength - 3 : this.maxLength;
            if (s.length() <= startPos) {
                sb.append(s);
                return;
            }
            int pos = startPos;
            final int minPos = this.maxLength / 2;
            while (pos > minPos && !allowBreak(s.charAt(pos))) {
                pos--;
            }
            if (!allowBreak(s.charAt(pos))) {
                pos = startPos;
                int maxPos = startPos + this.maxLength / 3;
                if (maxPos >= s.length()) {
                    sb.append(s);
                    return;
                }
                while (pos < maxPos && !allowBreak(s.charAt(pos))) {
                    pos++;
                }
                if (!allowBreak(s.charAt(pos))) {
                    pos = startPos;
                }
            }

            sb.append(s.substring(0, omitCharacterWhenNewline(s.charAt(pos)) ? pos : pos + 1));
            if (lastLine) {
                sb.append("...");
            }
            else {
                sb.append("<br/>"); // $NON-NLS$
                addShortLines(sb, s.substring(pos + 1), nrOfLines - 1);
            }
        }

        private boolean allowBreak(char ch) {
            return ch == ' ' ||
                    ch == '-';
        }

        private boolean omitCharacterWhenNewline(char ch) {
            return ch == ' ';
        }
    }

    public static class MultiDelegateRenderer implements TableCellRenderer {
        private final String contentClass;

        private final MultiRenderer delegate;

        public MultiDelegateRenderer(MultiRenderer delegate, String contentClass) {
            this.contentClass = contentClass;
            this.delegate = delegate;
        }

        public String getContentClass() {
            return this.contentClass;
        }

        @SuppressWarnings("unchecked")
        public void render(Object data, StringBuffer sb, Context context) {
            if (data instanceof String[]) {
                sb.append(this.delegate.render((String[]) data));
            }
            else if (data instanceof Long[]) {
                sb.append(this.delegate.render((Long[]) data));
            }
            else {
                sb.append(this.delegate.render(null));
            }
        }

        public boolean isPushRenderer() {
            return false;
        }
    }


    /**
     * Renders its data object as a Hyperlink widget iff the cell's data object is an
     * instance and the instrument has a defined name. The name of the
     * hyperlink can be restricted to user defined number of characters.
     */
    public static class NewsLinkRenderer extends MaxLengthStringRenderer {
        private LinkListener<NWSSearchElement> listener = null;

        final private boolean showDate;

        public NewsLinkRenderer(int maxLength, String nullText) {
            super(maxLength, nullText);
            this.showDate = false;
        }

        public NewsLinkRenderer(LinkListener<NWSSearchElement> listener, int maxLength,
                String nullText, boolean showDate) {
            super(maxLength, nullText);
            this.listener = listener;
            this.showDate = showDate;
        }

        public void render(Object data, StringBuffer sb, Context context) {
            if (data instanceof NWSSearchElement) {
                final NWSSearchElement nws = (NWSSearchElement) data;
                final String hl;
                if (this.showDate) {
                    hl = NewsUtil.headlineWithDate(nws);
                }
                else {
                    hl = NewsUtil.headlineWithoutAgency(nws);
                }
                if (hl == null) {
                    sb.append(this.nullText);
                    return;
                }
                final String display = getMaxLengthText(hl);
                context.appendLink(LinkContext.newsLink(nws, this.listener), display, hl, sb);
            }
            else {
                sb.append(this.nullText);
            }
        }
    }

    public static class HeadlinesLinkRenderer extends StringRenderer {
        private LinkListener<NWSSearchElement> listener = null;

        final private boolean showDate;

        public HeadlinesLinkRenderer(LinkListener<NWSSearchElement> listener, int maxLength,
                String nullText, boolean showDate) {
            super(nullText);
            this.listener = listener;
            this.showDate = showDate;
        }

        public void render(Object data, StringBuffer sb, Context context) {
            if (data instanceof NWSSearchElement) {
                final NWSSearchElement nws = (NWSSearchElement) data;
                final String hl;
                if (this.showDate) {
                    hl = NewsUtil.headlineWithDate(nws);
                }
                else {
                    hl = NewsUtil.headlineWithoutAgency(nws);
                }
                if (hl == null) {
                    sb.append(this.nullText);
                    return;
                }
                context.appendLink(LinkContext.newsLink(nws, this.listener), hl, hl, sb);
            }
            else {
                sb.append(this.nullText);
            }
        }
    }

    private static void renderQuoteLink(TableCellRenderer.Context context, StringBuffer sb,
            QuoteWithInstrument qwi, String name, String display, boolean withClip) {
        final LinkContext pinLinkContext;
        if (withClip) {
            pinLinkContext = LinkContext.pin(qwi);
        }
        else {
            pinLinkContext = null;
        }
        renderPinLink(context, sb, pinLinkContext, LinkContext.quoteLink(qwi), name, display, withClip, true);
    }

    public static void renderPinLink(TableCellRenderer.Context context, StringBuffer sb,
            LinkContext pinLinkContext,
            LinkContext linkContext, String name, String display, boolean withClip,
            boolean withLink) {
        if (withClip) {
            sb.append("<table class=\"mm-quoteLinkTable\" cellspacing=\"0\" cellpadding=\"0\">") // $NON-NLS$
                    .append("<tr><td style=\"width: 8px\" class=\"mm-contextTrigger-link-9\">"); // $NON-NLS$
            final String linkContent = "<img src=\"clear.cache.gif\" border=\"none\">"; // $NON-NLS$
            context.appendLink(pinLinkContext, linkContent, null, sb);
            sb.append("</td><td>"); // $NON-NLS$
            if (withLink) {
                sb.append("<td>"); // $NON-NLS$
            }
        }

        if (withLink) {
            context.appendLink(linkContext, display, name, sb);
        }

        if (withClip) {
            if (withLink) {
                sb.append("</td>"); // $NON-NLS-0$
            }
            sb.append("</td></tr></table>"); // $NON-NLS-0$
        }
    }

    /**
     * Renderer for {@link de.marketmaker.iview.dmxml.PageSummary}, i.e. the result of a search
     * for vwd pages. The constructor parameter determines the shown text in the column.
     */
    public static class PageSummaryRenderer extends MaxLengthStringRenderer {

        public enum Field {
            /**
             * show the pagenumber of the page
             */
            PAGENUMBER("mm-right"), // $NON-NLS$
            /**
             * show the heading of the page
             */
            HEADING("mm-left"); // $NON-NLS$

            private Field(String style) {
                this.style = style;
            }

            final String style;
        }

        final Field fieldToRender;

        public PageSummaryRenderer(Field fieldToRender, int maxHeadingLength) {
            super(maxHeadingLength, "--", fieldToRender.style); // $NON-NLS-0$
            this.fieldToRender = fieldToRender;
        }

        public void render(Object data, StringBuffer sb, Context context) {
            PageSummary page = (PageSummary) data;
            final String pagenumber = page.getPagenumber();
            switch (this.fieldToRender) {
                case PAGENUMBER:
                    context.appendLink(StringUtil.joinTokens(VwdPageController.KEY, pagenumber),
                            pagenumber, null, sb);
                    break;
                case HEADING:
                    final String head = page.getHeading();
                    context.appendLink(StringUtil.joinTokens(VwdPageController.KEY, pagenumber),
                            getMaxLengthText(head),
                            getMaxLengthText(head).equals(head) ? null : head, sb);
                    break;
            }
        }
    }


    /**
     * Renders its data object as a Hyperlink widget iff the cell's data object is an
     * Link instance. The name of the hyperlink can be restricted to user defined number of characters.
     */
    public static class LinkRenderer extends MaxLengthStringRenderer {
        public LinkRenderer(int maxLength, String nullText) {
            super(maxLength, nullText);
        }

        public LinkRenderer(int maxLength, String nullText, String textStyle) {
            super(maxLength, nullText, textStyle);
        }

        public void render(Object data, StringBuffer sb, Context context) {
            if (data instanceof Link) {
                final Link link = (Link) data;
                renderLink(sb, context, link, getMaxLengthText(link.getText()));
            }
            else {
                sb.append(this.nullText);
            }
        }

    }

    /**
     * Renders its data object as a Hyperlink widget if the cell's data object is an
     * IdAndName instance and display the value of the field name.
     * The resulting Hyperlink is concatenated by the historyTokens and the id of the IdAndName-Object.
     * The displayed string of the hyperlink can be restricted to user defined number of characters.
     */

    public static class IdAndNameLinkRenderer extends MaxLengthStringRenderer {
        private final String historyTokens;

        public IdAndNameLinkRenderer(String historyTokens, int maxLength, String nullText) {
            super(maxLength, nullText);
            this.historyTokens = historyTokens;
        }

        @Override
        public void render(Object data, StringBuffer sb, Context context) {
            if (data instanceof IdAndName) {
                final IdAndName idAndName = (IdAndName) data;

                if (!StringUtil.hasText(idAndName.getName())) {
                    sb.append(this.nullText);
                    return;
                }

                final String display = getMaxLengthText(idAndName.getName());
                if (StringUtil.hasText(this.historyTokens)) {
                    final Token token = new Token(this.historyTokens, idAndName.getHistoryContext());
                    context.appendLink(LinkContext.historyTokenLink(token.appendToken(idAndName.getId())),
                            display, idAndName.getName(), sb);
                }
                else {
                    sb.append(display);
                }
            }
            else {
                sb.append(this.nullText);
            }

        }
    }

    /**
     * Renders its data object as a Hyperlink widget iff the cell's data object is an
     * QuoteWithInstrument instance and the instrument has a defined name. The name of the
     * hyperlink can be restricted to user defined number of characters.
     */
    public static class QuoteLinkRenderer extends QuoteNameRenderer {
        private boolean withClip = !SessionData.INSTANCE.isAnonymous();

        private String prefixToDelete;

        public QuoteLinkRenderer(int maxLength, String nullText) {
            super(maxLength, nullText);
        }

        public QuoteLinkRenderer(int maxLength, String nullText, String textStyle) {
            super(maxLength, nullText, textStyle);
        }

        public void render(Object data, StringBuffer sb, Context context) {
            if (data instanceof QuoteWithInstrument) {
                final QuoteWithInstrument qwi = (QuoteWithInstrument) data;
                final String rawName = getName(qwi);

                if (rawName == null) {
                    sb.append(this.nullText);
                    return;
                }

                final String name = this.prefixToDelete != null && rawName.startsWith(this.prefixToDelete)
                        ? rawName.substring(this.prefixToDelete.length())
                        : rawName;

                final String display = getMaxLengthText(name);

                //This case is used when rendering the companyName which is stored as the Name of the QwI
                if (qwi.isNullQuoteOrNullInstrument()) {
                    sb.append(display);
                    return;
                }

                //The standard case
                renderQuoteLink(context, sb, qwi, name, display, this.withClip);
            }
            else {
                sb.append(this.nullText);
            }
        }

        public QuoteLinkRenderer withoutClip() {
            this.withClip = false;
            return this;
        }

        public QuoteLinkRenderer withPrefixToDelete(String prefixToDelete) {
            this.prefixToDelete = prefixToDelete;
            return this;
        }
    }

    /**
     * render a QuoteWithInstrument as a link,
     * fallback to QuoteWithInstrument.getName() with tooltip if no quotedata are available
     */
    public static class OptionalQuoteLinkRenderer extends QuoteLinkRenderer {

        private String noQuoteTooltip;

        public OptionalQuoteLinkRenderer(int maxLength, String nullText, String noQuoteTooltip) {
            super(maxLength - 1, nullText); // 8px padding left
            this.noQuoteTooltip = noQuoteTooltip;
        }

        public void render(Object data, StringBuffer sb, TableCellRenderer.Context context) {
            if (data instanceof QuoteWithInstrument) {
                final QuoteWithInstrument qwi = (QuoteWithInstrument) data;
                if (qwi.isNullQuoteOrNullInstrument()) {
                    String name = getName(qwi);
                    if (name == null) {
                        name = this.nullText;
                    }
                    sb.append("<span qtip=\"").append(noQuoteTooltip).append("\" style=\"padding-left: 8px;\">");  // $NON-NLS$
                    sb.append(getMaxLengthText(name));
                    sb.append("</span>");  // $NON-NLS$
                    return;
                }
            }
            super.render(data, sb, context);
        }

    }


    /**
     * Like a QuoteLinkRenderer but appends a quote's currency ISO in parenthesis to its name
     */
    public static class QuoteLinkRendererWithCurrency extends QuoteLinkRenderer {
        public QuoteLinkRendererWithCurrency(int maxLength, String nullText) {
            super(maxLength, nullText);
        }

        public QuoteLinkRendererWithCurrency(int maxLength, String nullText, String textStyle) {
            super(maxLength, nullText, textStyle);
        }

        @Override
        protected String getName(QuoteWithInstrument qwi) {
            final String name = super.getName(qwi);
            if (name == null) {
                return null;
            }
            final String currency = qwi.getQuoteData().getCurrencyIso();
            if (currency == null) {
                return name;
            }
            return name + " (" + currency + ")"; // $NON-NLS-0$ $NON-NLS-1$
        }
    }

    /**
     * Renders its data object as a Hyperlink widget iff the cell's data object is an
     * QuoteWithInstrument instance and the instrument has a defined name. The name of the
     * hyperlink can be restricted to user defined number of characters.
     */
    public static class QuoteNameRenderer extends MaxLengthStringRenderer {
        // TODO: remove, if not, inline since this is not to be translated
        private static final String CUR_EUR_HACK = I18n.I.curEuroHack();

        // TODO: inline since this is not to be translated
        private static final String STAATSANL_HACK = I18n.I.governmentBonds10YearAbbr();

        public QuoteNameRenderer(int maxLength, String nullText) {
            super(maxLength, nullText);
        }

        public QuoteNameRenderer(int maxLength, String nullText, String textStyle) {
            super(maxLength, nullText, textStyle);
        }

        protected String getName(final QuoteWithInstrument qwi) {
            String name = qwi.getName();
            if (name == null) {
                return null;
            }

            if (name.startsWith(STAATSANL_HACK)) {
                name = name.substring(STAATSANL_HACK.length());
            }

            return name;
        }

        protected String getText(final Object data) {
            if (data instanceof QuoteWithInstrument) {
                return getName((QuoteWithInstrument) data);
            }
            return null;
        }
    }


    /**
     * Renders all data as text and escapes html;
     */

    public static class HtmlRenderer extends StringRenderer {
        public HtmlRenderer(String nullText) {
            super(nullText);
        }

        public HtmlRenderer(String nullText, String contentClass) {
            super(nullText, contentClass);
        }

        @Override
        public void render(Object data, StringBuffer sb, Context context) {
            final String text = getText(data);
            final SafeHtmlBuilder shb = new SafeHtmlBuilder();
            shb.appendEscaped(text != null ? text : this.nullText);
            sb.append(shb.toSafeHtml().asString());
        }
    }


    /**
     * Renders all data as text; if the cell has a non-null data object, its toString method will
     * be called to obtain the text value, otherwise a user defined nullText will be used.
     */
    public static class StringRenderer implements TableCellRenderer {
        protected final String contentClass;

        protected final String nullText;

        public StringRenderer(String nullText) {
            this(nullText, null);
        }

        public StringRenderer(String nullText, String contentClass) {
            this.nullText = nullText;
            this.contentClass = contentClass;
        }

        public String getContentClass() {
            return this.contentClass;
        }

        public void render(Object data, StringBuffer sb, Context context) {
            final String text = getText(data);
            sb.append(text != null ? text : this.nullText);
        }

        protected String getText(final Object data) {
            return data != null ? data.toString() : null;
        }

        public boolean isPushRenderer() {
            return false;
        }

    }

    public static class LocalLinkRenderer implements TableCellRenderer {
        private final LinkListener<QuoteWithInstrument> listener;

        private final String contentClass;

        private final String selectedClass;

        private String selectedSymbol = null;

        private final String nullText;

        private String toolTip = null;

        public LocalLinkRenderer(LinkListener<QuoteWithInstrument> listener, String contentClass) {
            this(listener, contentClass, contentClass, "", ""); // $NON-NLS-0$ $NON-NLS-1$
        }

        public LocalLinkRenderer(LinkListener<QuoteWithInstrument> listener, String contentClass,
                String toolTip) {
            this(listener, contentClass, contentClass, "", toolTip); // $NON-NLS-0$
        }

        public LocalLinkRenderer(LinkListener<QuoteWithInstrument> listener, String contentClass,
                String selectedClass, String nullText, String toolTip) {
            this.listener = listener;
            this.contentClass = contentClass;
            this.selectedClass = selectedClass;
            this.nullText = nullText;
            this.toolTip = toolTip;
        }

        public void setSelectedSymbol(String selectedSymbol) {
            this.selectedSymbol = selectedSymbol;
        }

        public void render(Object data, StringBuffer sb, Context context) {
            if (data instanceof QuoteWithInstrument) {
                final QuoteWithInstrument qwi = (QuoteWithInstrument) data;
                final String styleName = isSelected(qwi) ? this.selectedClass : this.contentClass;
                String tt = ""; // $NON-NLS-0$
                if (this.toolTip != null) {
                    tt = " qtip=\"" + this.toolTip + "\""; // $NON-NLS-0$ $NON-NLS-1$
                }
                final String linkContent = "<div class=\"" + styleName + "-content\"" + tt + "></div>"; // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
                context.appendLink(new LinkContext<>(this.listener, qwi), linkContent, null, sb);
            }
            else {
                sb.append(this.nullText);
            }
        }

        private boolean isSelected(QuoteWithInstrument qwi) {
            return qwi.getQuoteData().getQid().equals(this.selectedSymbol)
                    || qwi.getInstrumentData().getIid().equals(this.selectedSymbol);
        }

        public boolean isPushRenderer() {
            return false;
        }

        public String getContentClass() {
            return this.contentClass;
        }
    }

    public static class IconLinkRenderer<D> implements TableCellRenderer {
        private final String tooltip;

        protected String iconUrl;

        private final AbstractImagePrototype icon;

        private final LinkListener<D> listener;

        public IconLinkRenderer(LinkListener<D> listener, String iconUrl, String tooltip) {
            this.tooltip = tooltip;
            this.iconUrl = iconUrl;
            this.icon = null;
            this.listener = listener;
        }

        public IconLinkRenderer(LinkListener<D> listener, AbstractImagePrototype icon,
                String tooltip) {
            this.tooltip = tooltip;
            this.iconUrl = null;
            this.icon = icon;
            this.listener = listener;
        }

        public void render(Object data, StringBuffer sb, Context context) {
            final String linkContent;
            if (this.iconUrl == null) {
                linkContent = getHtml(this.icon, this.tooltip);
            }
            else {
                String tt = ""; // $NON-NLS-0$
                if (this.tooltip != null) {
                    tt = " qtip=\"" + this.tooltip + "\""; // $NON-NLS$
                }
                linkContent = "<img src=\"" + this.iconUrl + "\"" + tt + "></img>"; // $NON-NLS$
            }
            context.appendLink(new LinkContext<>(this.listener, (D) data), linkContent, null, sb);
        }

        public boolean isPushRenderer() {
            return false;
        }

        public String getContentClass() {
            return null;
        }
    }

    private static String getHtml(AbstractImagePrototype aip, String tooltip) {
        final com.google.gwt.user.client.ui.Image image = aip.createImage();
        image.getElement().getStyle().setVerticalAlign(MIDDLE);
        if (tooltip != null) {
            Tooltip.addQtip(image, tooltip);
        }
        return new SimplePanel(image).getElement().getInnerHTML();
    }

    public static class IconLinkWithTextRenderer extends MaxLengthStringRenderer {

        private boolean multiline;

        public IconLinkWithTextRenderer(int maxLength, String nullText, boolean multiline) {
            super(maxLength, nullText, "");
            this.multiline = multiline;
        }

        @Override
        public void render(Object data, StringBuffer sb, Context context) {
            if (!(data instanceof Link)) {
                return;
            }

            Link link = (Link) data;

            if (link.getIcon() != null) {
                String iconHtml = "<div qtip=\'" + link.getIconTooltip() + "\'>" + link.getIcon().getHTML() + "</div>";  //$NON-NLS$

                context.appendLink(new LinkContext(link.getListener(), link), iconHtml, null, sb);
            }

            String textStyle = (multiline ? "white-space: pre-wrap" : "white-space: nowrap");  //$NON-NLS$
            sb.append("<div style='" + textStyle + "; padding-right:20px'>");   //$NON-NLS$
            super.render(link.getText(), sb, context);
            sb.append("</div>");    //$NON-NLS$
        }

        @Override
        protected StringBuffer appendWithQTip(StringBuffer sb, String text, String display) {
            return sb.append("<div style='overflow: hidden; text-overflow: ellipsis;' tooltipStyle='mm-tooltipMultiline' qtip=\"")  //$NON-NLS$
                    .append(text).append("\">").append(display).append("</div>");  //$NON-NLS$
        }
    }

    public static class VRIconLinkRenderer extends IconLinkRenderer<QuoteWithInstrument> {
        public VRIconLinkRenderer() {
            super(new LinkListener<QuoteWithInstrument>() {
                public void onClick(LinkContext<QuoteWithInstrument> context, Element e) {
                    final QuoteWithInstrument qwi = context.getData();
                    final QuoteData qd = qwi.getQuoteData();
                    if (isDzAndHighlightingAllowed() && isSingleFlagAllowed(qd) == null) {
                        VRFinderLinkMenu.INSTANCE.show(qwi, e);
                    }
                    else if (Selector.WGZ_BANK_USER.isAllowed() && isSingleFlagAllowed(qd) == null) {
                        VRFinderLinkMenu.gotoCerFinder(qwi);
                    }
                    else {
                        VRFinderLinkMenu.gotoFinder(qwi, isSingleFlagAllowed(qd));
                    }
                }
            }, "images/logo-dzbank-16.png", ""); // $NON-NLS$
        }

        private static String getLogo() {
            //HACK!!! TODO: REMOVE HACK
            return Customer.INSTANCE.isDbk()
                    ? "images/favicon/deutsche-bank.png" // $NON-NLS$
                    : (Customer.INSTANCE.isHvb()
                    ? "images/favicon/hvb.png" // $NON-NLS$
                    : "images/logo-dzbank-16.png"); // $NON-NLS$
        }


        private static boolean isDzAndHighlightingAllowed() {
            return Selector.DZ_BANK_USER.isAllowed() && Selector.PRODUCT_HIGHLIGHTING.isAllowed();
        }


        @Override
        public void render(Object data, StringBuffer sb, Context context) {
            if (!(data instanceof QuoteWithInstrument)) {
                return;
            }
            //TODO: Remove Hack!
            // This is a temporary hack for DBK. Customer.CER_INSTANCE is not available when Renderer is created!
            this.iconUrl = getLogo();
            final QuoteWithInstrument qwi = (QuoteWithInstrument) data;
            final boolean display;
            final QuoteData qd = qwi.getQuoteData();

            if (isDzAndHighlightingAllowed()) {
                display = ContentFlagsEnum.CerUnderlyingDzbank.isAvailableFor(qd) ||
                        ContentFlagsEnum.WntUnderlyingDzbank.isAvailableFor(qd) ||
                        ContentFlagsEnum.LeverageProductUnderlyingDzbank.isAvailableFor(qd);
            }
            else {
                display = Selector.WGZ_BANK_USER.isAllowed()
                        && Selector.PRODUCT_HIGHLIGHTING.isAllowed()
                        && ContentFlagsEnum.CerUnderlyingWgzbank.isAvailableFor(qd);
            }
            if (!display) {
                return;
            }
            super.render(data, sb, context);
        }


        private static ContentFlagsEnum isSingleFlagAllowed(QuoteData qd) {
            if (ContentFlagsEnum.CerUnderlyingDzbank.isAvailableFor(qd) &&
                    !ContentFlagsEnum.WntUnderlyingDzbank.isAvailableFor(qd) &&
                    !ContentFlagsEnum.LeverageProductUnderlyingDzbank.isAvailableFor(qd) &&
                    !ContentFlagsEnum.CerUnderlyingWgzbank.isAvailableFor(qd)) {
                return ContentFlagsEnum.CerUnderlyingDzbank;
            }
            if (ContentFlagsEnum.WntUnderlyingDzbank.isAvailableFor(qd) &&
                    !ContentFlagsEnum.CerUnderlyingDzbank.isAvailableFor(qd) &&
                    !ContentFlagsEnum.LeverageProductUnderlyingDzbank.isAvailableFor(qd) &&
                    !ContentFlagsEnum.CerUnderlyingWgzbank.isAvailableFor(qd)) {
                return ContentFlagsEnum.WntUnderlyingDzbank;
            }
            if (ContentFlagsEnum.CerUnderlyingWgzbank.isAvailableFor(qd) &&
                    !ContentFlagsEnum.WntUnderlyingDzbank.isAvailableFor(qd) &&
                    !ContentFlagsEnum.LeverageProductUnderlyingDzbank.isAvailableFor(qd) &&
                    !ContentFlagsEnum.CerUnderlyingDzbank.isAvailableFor(qd)) {
                return ContentFlagsEnum.CerUnderlyingWgzbank;
            }
            if (ContentFlagsEnum.LeverageProductUnderlyingDzbank.isAvailableFor(qd) &&
                    !ContentFlagsEnum.CerUnderlyingWgzbank.isAvailableFor(qd) &&
                    !ContentFlagsEnum.WntUnderlyingDzbank.isAvailableFor(qd) &&
                    !ContentFlagsEnum.CerUnderlyingDzbank.isAvailableFor(qd)) {
                return ContentFlagsEnum.CerUnderlyingWgzbank;
            }
            return null;
        }
    }

    public static class DzPibDownloadIconLinkCellRendererQwI extends
            DzPibDownloadIconLinkCellRenderer {
        @Override
        public void render(Object data, StringBuffer sb, Context context) {
            if (data instanceof QuoteWithInstrument) {
                final QuoteWithInstrument qwi = (QuoteWithInstrument) data;
                final QuoteData qd = qwi.getQuoteData();
                if (Selector.PRODUCT_WITH_PIB.isAllowed() && ContentFlagsEnum.PibDz.isAvailableFor(qd)) {
                    super.render(qd.getQid(), sb, context);
                }
            }
        }
    }

    public static class DzResearchPopupIconLinkCellRenderer extends
            IconLinkRenderer<QuoteWithInstrument> {
        public DzResearchPopupIconLinkCellRenderer() {
            super(new LinkListener<QuoteWithInstrument>() {
                public void onClick(LinkContext<QuoteWithInstrument> qwiContext, Element e) {
                    final QuoteWithInstrument qwi = qwiContext.getData();
                    if (qwi.getQuoteData() != null) {
                        LiveFinderResearch.INSTANCE.prepareFindInstrument(new QuoteWithInstrument(qwi.getInstrumentData(), qwi.getQuoteData()));
                        if (WGZ_BANK_USER.isAllowed()) {
                            PlaceUtil.goTo("WZ_LF_RES1"); // $NON-NLS-0$
                        }
                        else {
                            PlaceUtil.goTo("DZ_LF_RES1"); // $NON-NLS-0$
                        }
                    }
                    else {
                        Firebug.error("no symbol found");
                    }
                }
            }, IconImage.get("mm-icon-dzbank-rs"), I18n.I.dzBankRsTooltip()); //$NON-NLS$
        }

        @Override
        public void render(Object data, StringBuffer sb, Context context) {
            if (data instanceof QuoteWithInstrument) {
                if (ContentFlagUtil.hasAccessibleResearchDocuments(((QuoteWithInstrument) data).getQuoteData())) {
                    super.render(data, sb, context);
                }
            }
        }
    }

    public static class DzPibDownloadIconLinkCellRendererElem extends
            IconLinkRenderer<GISFinderElement> {
        public DzPibDownloadIconLinkCellRendererElem() {
            super((context, e) -> {
                GISFinderElement element = context.getData();
                QuoteData qd = element.getQuotedata();
                if (qd == null || qd.getQid() == null) {
                    Window.open(element.getPibUrl(), "_blank", null);  //$NON-NLS$
                }
                else {
                    DzPibReportLinkController.INSTANCE.openDialogOrReport(qd);
                }
            }, IconImage.get("mm-icon-dzbank-pib"), I18n.I.dzBankPibTooltip()); //$NON-NLS$
        }

        @Override
        public void render(Object data, StringBuffer sb, Context context) {
            final GISFinderElement element = (GISFinderElement) data;
            final QuoteData qd = element.getQuotedata();
            if ((qd == null || qd.getQid() == null) && !StringUtil.hasText(element.getPibUrl())) {
                // see R-79052: if quote data or QID are null and the PIB URL is empty, does not
                // render the PIB icon, and hence does not provide a link to the PIB PDF.
                return;
            }
            if (!ContentFlagsEnum.PibDz.isAvailableFor(qd)) {
                // see R-79052: check content flag of quote as it is done with other calls to
                // DzPibReportLinkController.INSTANCE.openDialogOrReport (see constructor)
                // see also DzPibDownloadIconLinkCellRendererQwI
                return;
            }
            super.render(data, sb, context);
        }
    }


    public static class DzPibDownloadIconLinkCellRenderer extends IconLinkRenderer<String> {
        public DzPibDownloadIconLinkCellRenderer() {
            this(new LinkListener<String>() {
                public void onClick(LinkContext<String> context, Element e) {
                    DzPibReportLinkController.INSTANCE.openDialogOrReport(context.getData());
                }
            });
        }

        public DzPibDownloadIconLinkCellRenderer(LinkListener<String> listener) {
            super(listener, IconImage.get("mm-icon-dzbank-pib"), I18n.I.dzBankPibTooltip()); // $NON-NLS$
        }

        @Override
        public void render(Object data, StringBuffer sb, Context context) {
            if (data instanceof String) {
                if (Selector.PRODUCT_WITH_PIB.isAllowed()) {
                    super.render(data, sb, context);
                }
            }
        }
    }

    public static class DzPibDownloadIconLinkCellRendererUrl extends
            DzPibDownloadIconLinkCellRenderer {
        public DzPibDownloadIconLinkCellRendererUrl() {
            super(new LinkListener<String>() {
                @Override
                public void onClick(LinkContext<String> stringLinkContext, Element e) {
                    if (!stringLinkContext.getData().startsWith("http")) { //$NON-NLS$
                        Window.open(DzPibMarginDialog.DYN_PIB_BASE_URL + stringLinkContext.getData(), "_blank", null); //$NON-NLS$
                    }
                    else {
                        Window.open(stringLinkContext.getData(), "_blank", null); //$NON-NLS$
                    }
                }
            });
        }
    }

    private TableCellRenderers() {
    }
}
