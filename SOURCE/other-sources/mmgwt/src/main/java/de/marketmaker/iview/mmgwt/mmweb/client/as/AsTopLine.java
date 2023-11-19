package de.marketmaker.iview.mmgwt.mmweb.client.as;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.floating.FloatingPanel;
import de.marketmaker.iview.dmxml.MSCListDetailElement;
import de.marketmaker.iview.dmxml.MSCListDetails;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.ResponseTypeCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.RefreshDmxmlContextEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.RefreshDmxmlContextHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ItemListContext;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.MarketOverviewSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RendererContext;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkManager;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.WidgetUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ulrich Maurer
 *         Date: 18.02.13
 */
public class AsTopLine implements IsWidget, AsyncCallback<ResponseType>, PushRegisterHandler,
        RefreshDmxmlContextHandler {
    private static final int REQUEST_INTERVAL_MILLIS = 120000;
    private static final int POPUP_OPEN_MILLIS = 300;
    private static final int POPUP_CLOSE_MILLIS = 800;

    private final FloatingPanel floatingPanel = new FloatingPanel(FloatingPanel.Orientation.HORIZONTAL);
    private final FlowPanel panel = new FlowPanel();
    private final DmxmlContext.Block<MSCListDetails> block;
    private final DmxmlContext context = new DmxmlContext();
    private final Map<String, String> mapNames = new HashMap<>();
    private ArrayList<QuoteWithInstrument> currentQwis = new ArrayList<>();
    private final LinkManager linkManager = new LinkManager();
    private final PriceSupport priceSupport = new PriceSupport(this);
    private final RendererContext rendererContext = new RendererContext(this.linkManager, this.priceSupport);
    private boolean autoRefresh = false;
    private boolean attached = false;
    private PopupPanel popupPanel = new PopupPanel(true, false);
    private QuoteWithInstrument popupQwi = null;
    private HTML popupHtml = new HTML();
    private Widget popupTarget = null;
    private Timer popupOpenTimer = new Timer() {
        @Override
        public void run() {
            if (popupTarget == null) {
                return;
            }
            popupPanel.showRelativeTo(popupTarget);
        }
    };
    private Timer popupCloseTimer = new Timer() {
        @Override
        public void run() {
            popupPanel.hide();
            popupQwi = null;
        }
    };
    protected final ResponseTypeCallback pushReloadCallback = new ResponseTypeCallback() {
        @Override
        protected void onResult() {
            if (priceSupport.isActive()) {
                updateView();
            }
        }
    };

    private final Scheduler.RepeatingCommand rc = () -> {
        if (autoRefresh) {
            update();
            return true;
        }
        return false;
    };

    public AsTopLine(SessionData sessionData) {
        this.floatingPanel.addStyleName("as-topLineFloat");
        this.context.setCancellable(false);
        this.panel.setStyleName("as-topLine");
        final List<QuoteWithInstrument> list = sessionData.getList("list_markets_workspace");  // $NON-NLS$
        if (list.isEmpty()) {
            this.block = this.context.addBlock("MSC_List_Details"); // $NON-NLS$
            this.block.setParameter("listid", sessionData.getGuiDef("markets_workspace_listid").stringValue()); // $NON-NLS$
            this.block.setParameter("onlyEntitledQuotes", "true"); // $NON-NLS$
            this.block.setParameter("disablePaging", "true"); // $NON-NLS$
            this.block.setParameter("sortBy", "none"); // $NON-NLS$
        }
        else {
            for (QuoteWithInstrument qwi : list) {
                mapNames.put(qwi.getQuoteData().getQid(), qwi.getName());
            }
            this.block = this.context.addBlock("MSC_PriceDataMulti"); // $NON-NLS$
            this.block.setParameters("symbol", getSymbols(list)); // $NON-NLS$
        }

        this.panel.addAttachHandler(event -> {
            this.attached = event.isAttached();
            if (this.attached && !this.autoRefresh) {
                this.autoRefresh = true;
                update();
                Scheduler.get().scheduleFixedPeriod(this.rc, REQUEST_INTERVAL_MILLIS);
            }
            else {
                this.autoRefresh = false;
            }
        });
        this.floatingPanel.setWidget(this.panel);
        this.popupPanel.setStyleName("as-topLine as-topLine-popup");
        this.popupPanel.setWidget(this.popupHtml);
        this.popupHtml.setStyleName("as-topLine-element");
        this.popupHtml.addMouseOverHandler(event -> popupCloseTimer.cancel());
        this.popupHtml.addMouseOutHandler(event -> popupCloseTimer.schedule(POPUP_CLOSE_MILLIS));
        this.popupHtml.addClickHandler(event -> popupQwi.goToPortrait());
    }

    @Override
    public Widget asWidget() {
        return this.floatingPanel.asWidget();
    }

    private String[] getSymbols(List<QuoteWithInstrument> list) {
        final String[] symbols = new String[list.size()];
        for (int i = 0; i < symbols.length; i++) {
            symbols[i] = list.get(i).getQuoteData().getQid();
        }
        return symbols;
    }

    private void update() {
        if (this.attached && this.block.isToBeRequested()) {
            this.context.issueRequest(this);
        }
    }

    @Override
    public void onFailure(Throwable caught) {
        onAsyncResponse();
    }

    @Override
    public void onSuccess(ResponseType result) {
        onAsyncResponse();
    }

    private void onAsyncResponse() {
        if (this.block.isResponseOk()) {
            updateView(MarketOverviewSnippet.filterElements(this.block));
        }
    }

    @Override
    public void onRefreshDmxmlContext(RefreshDmxmlContextEvent event) {
        if(this.attached) {
            Firebug.info("<AsTopLine.onRefreshDmxmlContext>");
            this.block.setToBeRequested();
            update();
        }
    }

    @SuppressWarnings("Duplicates")
    @Override
    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        if (this.block.isResponseOk()) {
            final int numAdded = event.addVwdcodes(this.block.getResult());
            if (numAdded == this.block.getResult().getElement().size()) {
                event.addComponentToReload(this.block, this.pushReloadCallback);
            }
            if (numAdded > 0) {
                return getRenderItems();
            }
        }
        return null;
    }

    protected ArrayList<PushRenderItem> getRenderItems() {
        final ArrayList<Element> pushedElements = WidgetUtil.getPushedElements(this.panel, "span"); // $NON-NLS$
//        pushedElements.addAll(WidgetUtil.getPushedElements(this.popupHtml, "span")); // $NON-NLS$ // TODO: wie geht das am Besten --> Inhalt ändert sich ????
//        DebugUtil.logToFirebugConsole("found " + pushedTDs.size() + " push tds");
        final ArrayList<PushRenderItem> result = new ArrayList<>();
        int i = 0;
        for (QuoteWithInstrument qwi : this.currentQwis) {
            final Price price = this.priceSupport.getCurrentPrice(qwi.getQuoteData());
            if (!price.isPushable()) {
                continue;
            }
//            result.add(new PushRenderItem(pushedElements.get(i++), price, TableCellRenderers.DATE_OR_TIME_PUSH));
//            result.add(new PushRenderItem(pushedElements.get(i++), price, TableCellRenderers.CHANGE_PERCENT_PUSH));
            result.add(new PushRenderItem(pushedElements.get(i++), price, TableCellRenderers.LAST_PRICE_COMPARE_CHANGE_PUSH));
        }
//        DebugUtil.logToFirebugConsole("created " + result.size() + " render items");
        return result;
    }


    private boolean isNewPriceAvailable() {
        for (QuoteWithInstrument qwi : this.currentQwis) {
            if (this.priceSupport.isNewerPriceAvailable(qwi.getQuoteData())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPricesUpdated(PricesUpdatedEvent event) {
        Firebug.log("onPricesUpdated");
        if (this.priceSupport.isLatestPriceGeneration()) {
            return;
        }
        if (!event.isPushedUpdate() && this.block.isResponseOk() && isNewPriceAvailable()) {
            updateView();
        }
    }

    private void updateView(ArrayList<MSCListDetailElement> list) {
        this.currentQwis.clear();
        for (MSCListDetailElement element : list) {
            this.currentQwis.add(getQuoteWithInstrument(element));
        }
        updateView();
    }

    private void updateView() {
        this.priceSupport.invalidateRenderItems();
        this.panel.clear();
        this.priceSupport.activate();
        for (QuoteWithInstrument qwi : this.currentQwis) {
            this.panel.add(getWidget(qwi));
        }
        this.priceSupport.updatePriceGeneration();
        this.floatingPanel.onResize();
    }

    private Widget getWidget(final QuoteWithInstrument qwi) {
        final Price p = this.priceSupport.getCurrentPrice(qwi.getQuoteData());
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();

        sb.appendHtmlConstant("<span class=\"name\">");
        sb.appendEscaped(qwi.getName());
        sb.appendHtmlConstant("</span>");

        final StringBuffer buffer = new StringBuffer();
        TableCellRenderers.LAST_PRICE_COMPARE_CHANGE_PUSH.render(p, buffer, this.rendererContext);
        sb.appendHtmlConstant("<span class=\"price " + this.rendererContext.getStyle() + "\" p=\"t\">");
        sb.appendEscaped(buffer.toString());
        sb.appendHtmlConstant("</span>");

        final HTML html = new HTML(sb.toSafeHtml());
        html.setStyleName("as-topLine-element");
        html.addClickHandler(event -> qwi.goToPortrait(ItemListContext.createForPortrait(
                qwi.getQuoteData().getQid(), MarketOverviewSnippet.filterElements(block), I18n.I.ticker()
        )));
        html.addMouseOverHandler(event -> {
            popupCloseTimer.cancel();
            popupHtml.setHTML(getPopupContent(qwi, p));
            if (popupPanel.isShowing()) {
                popupPanel.showRelativeTo(html);
            }
            else {
                popupTarget = html;
                popupOpenTimer.schedule(POPUP_OPEN_MILLIS);
            }
//                popupPanel.showRelativeTo(html);
        });
        html.addMouseOutHandler(event -> {
            popupTarget = null;
            popupOpenTimer.cancel();
            popupCloseTimer.schedule(POPUP_CLOSE_MILLIS);
        });
        if (this.popupPanel.isShowing() && qwi.equals(this.popupQwi)) {
            this.popupHtml.setHTML(getPopupContent(qwi, p));
        }
        return html;
    }

    private SafeHtml getPopupContent(QuoteWithInstrument qwi, final Price p) {
        this.popupQwi = qwi;
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        final StringBuffer buffer = new StringBuffer();

        buffer.setLength(0);
        TableCellRenderers.DATE_OR_TIME_PUSH.render(p, buffer, this.rendererContext);
        sb.appendHtmlConstant("<span class=\"time\">");
        sb.appendEscaped(buffer.toString());
        sb.appendHtmlConstant("</span>");

        buffer.setLength(0);
        TableCellRenderers.CHANGE_PERCENT_PUSH.render(p, buffer, this.rendererContext);
        sb.appendHtmlConstant("<span class=\"diff " + this.rendererContext.getStyle() + "\">");
        sb.appendEscaped(buffer.toString());
        sb.appendHtmlConstant("</span>");
        return sb.toSafeHtml();
    }

    private QuoteWithInstrument getQuoteWithInstrument(MSCListDetailElement data) {
        String name = mapNames.get(data.getQuotedata().getQid());
        if (name == null) {
            name = data.getItemname();
        }
        return new QuoteWithInstrument(data.getInstrumentdata(), data.getQuotedata(), name);
    }
}
