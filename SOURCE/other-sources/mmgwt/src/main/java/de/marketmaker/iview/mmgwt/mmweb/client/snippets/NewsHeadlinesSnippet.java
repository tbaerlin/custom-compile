/*
 * NewsHeadlinesSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.NWSSearch;
import de.marketmaker.iview.dmxml.NWSSearchElement;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderController;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormKeys;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderNews;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderNews;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.NewsSearchParser;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.NewsHeadlinesConfigurationView;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.PagingPanel;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NewsHeadlinesSnippet extends
        AbstractSnippet<NewsHeadlinesSnippet, NewsHeadlinesSnippetView>
        implements PagingPanel.Handler, SymbolSnippet, PdfUriSnippet {
    static final String NEWS_COUNT_SELECTION = "newsCountSelection";// $NON-NLS$
    static final String NEWS_COUNT = "newsCount";// $NON-NLS$
    private static final String BLOCK = FeatureFlags.Feature.VWD_RELEASE_2014.isEnabled() ? "NWS_Finder" : "NWS_Search"; // $NON-NLS$

    private NewsEntrySnippet entrySnippet;

    private final NewsSearchParser newsSearchParser = new NewsSearchParser();

    public static class Class extends SnippetClass {
        public Class() {
            super("NewsHeadlines", I18n.I.news()); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new NewsHeadlinesSnippet(context, config);
        }

        public void addDefaultParameters(SnippetConfiguration config) {
            config.put("withText", "false"); // $NON-NLS$
            config.put("count", 10); // $NON-NLS$
            config.put("colSpan", 2); // $NON-NLS$
        }
    }

    /**
     * Supports auto reload of news headlines every 2 minutes. To just reload the news, this objects
     * creates its own DmxmlContext and Block.
     */
    private class AutoReloader {
        private static final int AUTO_RELOAD_INTERVAL_MILLIS = 120000; // 120s=2min

        private final Timer timer = new Timer() {
            public void run() {
                autoReload();
            }
        };

        final DmxmlContext context = new DmxmlContext();

        final DmxmlContext.Block<NWSSearch> reloadBlock = context.addBlock(BLOCK);

        public void cancel() {
            this.timer.cancel();
        }

        public void prepare() {
            this.timer.schedule(AUTO_RELOAD_INTERVAL_MILLIS);
        }

        private void autoReload() {
            getView().reloading(true);
            this.reloadBlock.removeAllParameters();
            this.reloadBlock.addParametersFrom(NewsHeadlinesSnippet.this.block);

            this.context.issueRequest(new AsyncCallback<ResponseType>() {
                public void onFailure(Throwable throwable) {
                    scheduleAutoReload();
                    getView().reloading(false);
                }

                public void onSuccess(ResponseType responseType) {
                    updateView(reloadBlock.getResult(), false);
                    scheduleAutoReload();
                    getView().reloading(false);
                }
            });
        }
    }

    /**
     * auto reload support; will be defined iff snippet's configuration contains autoReload=true
     */
    private AutoReloader autoReloader;

    static final boolean DEFAULT_WITH_AGENCY = true;

    private int pageSize;

    private final boolean withAgency;

    private final boolean withHitCount;

    private final boolean withPdf;

    private final boolean showUnderlying;

    private DmxmlContext.Block<NWSSearch> block;

    private boolean blockAutoReload = false;

    private String[] newsIds = null;

    private ModHeightSnippet modHeightSnippet;


    private NewsHeadlinesSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        final boolean withCountSelection = config.getBoolean("withCountSelection", false); // $NON-NLS$
        if (withCountSelection) {
            config.put("count", getCount()); // $NON-NLS$
        }
        this.pageSize = config.getInt("count", 10); // $NON-NLS-0$
        this.withAgency = config.getBoolean("withAgency", DEFAULT_WITH_AGENCY); // $NON-NLS-0$
        this.withHitCount = isWithHitCount();
        this.withPdf = config.getBoolean("withPdf", false); // $NON-NLS-0$
        this.showUnderlying = SymbolSnippet.SYMBOL_UNDERLYING.equals(config.getString("symbol", null)); // $NON-NLS-0$
        config.putDefault("title", this.showUnderlying ? I18n.I.newsUnderlying() : I18n.I.news());  // $NON-NLS-0$

        if (config.getBoolean("autoReload", false)) { // $NON-NLS-0$
            this.autoReloader = new AutoReloader();
        }

        setView(new NewsHeadlinesSnippetView(this));

        this.block = createBlock(BLOCK);
        onParametersChanged();
    }

    private boolean isWithHitCount() {
        final SnippetConfiguration config = getConfiguration();
        final String paging = config.getString("paging", null); // $NON-NLS-0$
        return config.getBoolean("withHitCount", false) // $NON-NLS-0$
                || "true".equals(paging) || "restricted".equals(paging); // $NON-NLS-0$ $NON-NLS-1$
    }

    public void onControllerInitialized() {
        final String entryId = getConfiguration().getString("entryId", null); // $NON-NLS-0$
        if (entryId != null) {
            this.entrySnippet = (NewsEntrySnippet) this.contextController.getSnippet(entryId);
            if (this.entrySnippet == null) {
                throw new IllegalArgumentException("snippet not found: " + entryId); // $NON-NLS-0$
            }
            getView().setLinkListener(this.entrySnippet);
        }
        final String id = getConfiguration().getString("modHighSnippetId", null); // $NON-NLS$
        if (id != null) {
            this.modHeightSnippet = (ModHeightSnippet) this.contextController.getSnippet(id);
            if (this.modHeightSnippet == null) {
                throw new IllegalArgumentException("snippet not found: " + id); // $NON-NLS$
            }
        }
    }

    public void configure(Widget triggerWidget) {
        NewsHeadlinesConfigurationView.show(this);
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public boolean isConfigurable() {
        return true;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    private void resetOnValueChange(String key, String value) {
        final String oldValue = getConfiguration().getString(key);
        if (oldValue == null || !oldValue.equals(value)) {
            getConfiguration().put("offset", "0"); // $NON-NLS-0$ $NON-NLS-1$
            getConfiguration().put(key, value);
            onParametersChanged();
        }
    }

    public void ackNewOffset(int offset) {
        getConfiguration().put("offset", Integer.toString(offset)); // $NON-NLS-0$
        ackParametersChanged();
    }

    protected void ackParametersChanged() {
        cancelAutoReload();
        super.ackParametersChanged();
    }

    private void cancelAutoReload() {
        if (this.autoReloader != null) {
            this.autoReloader.cancel();
        }
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        resetOnValueChange("symbol", symbol); // $NON-NLS-0$
    }

    public void setTopic(String topic) {
        resetOnValueChange("topic", topic); // $NON-NLS-0$
    }

    @Override
    public void deactivate() {
        cancelAutoReload();
    }

    private void scheduleAutoReload() {
        if (this.autoReloader == null
                || this.blockAutoReload
                || !"0".equals(this.block.getParameter("offset")) // user selected another page, do not disturb // $NON-NLS-0$ $NON-NLS-1$
                || !"true".equals(SessionData.INSTANCE.getUserProperty(AppConfig.NEWS_HEADLINES_AUTO_RELOAD)) // $NON-NLS-0$
                ) {
            return;
        }
        this.autoReloader.prepare();
    }

    public void updateView() {
        if (!this.block.blockChanged()) {
            scheduleAutoReload();
            return;
        }
        if (!block.isResponseOk()) {
            this.newsIds = null;
            getView().update(DefaultTableDataModel.NULL, 0, 0, 0);
            return;
        }
        updateView(this.block.getResult(), true);
        scheduleAutoReload();
        Scheduler.get().scheduleDeferred(this::setModSnippetHeight);
    }

    private void setModSnippetHeight() {
        final Integer height = this.getView().getHeight();
        if (this.modHeightSnippet != null && height != null) {
            this.modHeightSnippet.setHeight(height);
        }
    }

    private void updateView(final NWSSearch nws, boolean updateEntry) {
        final int rows = Integer.parseInt(nws.getCount());
        final int columns = this.withAgency ? 3 : 2;
        final DefaultTableDataModel dtm = new DefaultTableDataModel(rows, columns);
        final List<NWSSearchElement> elementList = nws.getElement();
        final String[] newsIds = new String[elementList.size()];
        int row = 0;
        for (int i = 0, elementListSize = elementList.size(); i < elementListSize; i++) {
            final NWSSearchElement element = elementList.get(i);
            newsIds[i] = element.getNewsid();
            dtm.setValueAt(row, 0, element.getDate());
            dtm.setValueAt(row, 1, element);
            if (this.withAgency) {
                dtm.setValueAt(row, 2, element.getSource());
            }
            row++;
        }
        if (this.newsIds == null) {
            this.newsIds = newsIds;
            AbstractMainController.INSTANCE.getView().getTopToolbar().updatePdfButtonState();
        }
        else {
            this.newsIds = newsIds;
        }
        final int offset = Integer.parseInt(nws.getOffset());
        final int count = Integer.parseInt(nws.getCount());
        final int total = Integer.parseInt(nws.getTotal());
        getView().update(dtm, offset, count, total);

        if (updateEntry && this.entrySnippet != null) {
            this.entrySnippet.ackNewsid(getFirstNewsId(nws));
        }
    }

    private String getFirstNewsId(NWSSearch nws) {
        if (nws.getElement().isEmpty()) {
            return null;
        }
        return nws.getElement().get(0).getNewsid();
    }

    public boolean isTopToolbarUri() {
        return !this.withPdf;
    }

    public PdfOptionSpec getPdfOptionSpec() {
        if (this.newsIds == null || this.newsIds.length == 0) {
            Firebug.log("NewsHeadlinesSnippet.getPdfOptionSpec() - newIds == null"); // $NON-NLS-0$
            return null;
        }
        final StringBuilder sbQuery = new StringBuilder();
        String divider = "id IN ("; // $NON-NLS-0$
        for (String newsId : this.newsIds) {
            sbQuery.append(divider).append('\'').append(newsId).append('\'');
            divider = ","; // $NON-NLS-0$
        }
        sbQuery.append(")"); // $NON-NLS-0$

        final Map<String, String> map = new HashMap<>();
        map.put("count", String.valueOf(this.newsIds.length)); // $NON-NLS-0$
        map.put("offset", "0"); // $NON-NLS-0$ $NON-NLS-1$
        map.put("query", sbQuery.toString()); // $NON-NLS-0$
        return new PdfOptionSpec("newssearch.pdf", map, null); // $NON-NLS-0$
    }

    protected void onParametersChanged() {
        final SnippetConfiguration config = getConfiguration();

        this.block.setParameter("withText", config.getBoolean("withText", false)); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("withHitCount", this.withHitCount); // $NON-NLS-0$
        this.block.setParameter("offset", config.getInt("offset", 0)); // $NON-NLS$
        this.block.setParameters("symbol", getSymbolParameters()); // $NON-NLS-0$
        this.block.setParameters("topic", getTopicParameter()); // $NON-NLS-0$
        this.block.setParameter("query", newsSearchParser.parse(config.getString("searchstring"))); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("count", config.getInt("count", this.pageSize)); // $NON-NLS$
    }

    private int getCount() {
        final String[] newsCount = getConfiguration().getArray(NEWS_COUNT);
        if (newsCount != null && newsCount.length > 0) {
            return Integer.valueOf(getNewsCountSelection(newsCount[0]));
        }
        final JSONWrapper array = SessionData.INSTANCE.getGuiDef(NEWS_COUNT_SELECTION).get("items"); // $NON-NLS-0$
        return Integer.valueOf(getNewsCountSelection(array.get(0).get("count").stringValue())); // $NON-NLS$
    }

    String getNewsCountSelection(final String defaultValue) {
        return SessionData.INSTANCE.getUser().getAppConfig().getProperty(NEWS_COUNT_SELECTION, defaultValue);
    }

    void setNewsCountSelection(String count) {
        SessionData.INSTANCE.getUser().getAppConfig().addProperty(NEWS_COUNT_SELECTION, count);
        getConfiguration().put("count", count); // $NON-NLS$
        getConfiguration().put("offset", 0); // $NON-NLS$
        ackParametersChanged();
    }

    private String[] getSymbolParameters() {
        final String symbol = getConfiguration().getString("symbol"); // $NON-NLS-0$
        if (!isValidSymbol(symbol)) {
            return null;
        }
        if (this.showUnderlying) {
            return new String[]{symbol, "underlying(" + symbol + ")"}; // $NON-NLS-0$ $NON-NLS-1$
        }
        return new String[]{symbol};
    }

    private boolean isValidSymbol(String symbol) {
        return symbol != null && !SymbolSnippet.NO_SYMBOL.equals(symbol);
    }

    private String[] getTopicParameter() {
        final ArrayList<String> list = getConfiguration().getList("topic"); // $NON-NLS-0$
        return (list != null && !list.isEmpty())
                ? list.toArray(new String[list.size()])
                : null;
    }

    void onPinChanged(boolean pinned) {
        this.blockAutoReload = pinned;
        if (this.blockAutoReload) {
            cancelAutoReload();
        }
        else {
            scheduleAutoReload();
        }
//        DebugUtil.logToFirebugConsole("blockAutoReload: " + this.blockAutoReload);
    }

    protected void openFinder() {
        final FinderController controller =
                (FeatureFlags.Feature.VWD_RELEASE_2014.isEnabled() || FeatureFlags.Feature.DZ_RELEASE_2016.isEnabled())
                        ? LiveFinderNews.INSTANCE
                        : FinderNews.INSTANCE;

        FinderFormConfig ffc = null;
        final String searchstring = getConfiguration().getString("searchstring"); // $NON-NLS$
        if (searchstring != null) {
            ffc = new FinderFormConfig("temp", controller.getId()); // $NON-NLS$
            ffc.put("base", "true"); // $NON-NLS$
            ffc.put("text", "true"); // $NON-NLS$
            ffc.put("text-text", searchstring); // $NON-NLS$
        }

        final ArrayList<String> list = getConfiguration().getList("topic"); // $NON-NLS$
        if (list != null && !list.isEmpty()) {
            if (ffc == null) {
                ffc = new FinderFormConfig("temp", controller.getId()); // $NON-NLS$
            }
            ffc.put("topics", "true"); // $NON-NLS$
            ffc.put(FinderFormKeys.TOPICS, "true"); // $NON-NLS-0$
            for (String topic : list) {
                ffc.put(FinderFormKeys.TOPICS + '-' + topic, "true"); // $NON-NLS-0$
            }
        }
        final String symbol = getConfiguration().getString("symbol"); // $NON-NLS-0$
        if (symbol != null) {
            ffc = new FinderFormConfig("temp", controller.getId()); // $NON-NLS-0$
            ffc.put(FinderNews.BASE_ID, "true"); // $NON-NLS-0$
            ffc.put(FinderFormKeys.SYMBOL, "true"); // $NON-NLS-0$
            ffc.put(FinderFormKeys.SYMBOL + "-symbol", symbol); // $NON-NLS$
            ffc.put(FinderFormKeys.SYMBOL + "-name", symbol); // $NON-NLS$
        }
        // This snippet supports searching for news for the underlying of the symbol too. Since
        // (Live)FinderNews does not have a form element for that this information is not passed.
        if (ffc != null) {
            controller.prepareFind(ffc);
        }
        PlaceUtil.goTo("N_S"); // $NON-NLS$
    }
}
