/*
 * AstractListWorkspace.java
 *
 * Created on 10.11.2009 08:39:03
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import com.extjs.gxt.ui.client.dnd.DragSource;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.BoxComponentEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Html;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import de.marketmaker.iview.dmxml.BlockType;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.ResponseTypeCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RendererContext;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.HtmlBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.util.HtmlBuilderImpl;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkManager;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.WidgetUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A workspace that displays quote based data (name, price, change, time) in a grid.
 * Each cell of the grid can be dragged
 * onto areas that can be configured with QuoteWithInstrument-Objects (DnD group "ins"). // $NON-NLS-0$
 * @author oflege
 */
abstract class AbstractListWorkspace<D extends BlockType, T> extends AbstractWorkspaceItem implements
        AsyncCallback<ResponseType>, PushRegisterHandler {
    // for auto-reload
    private static final int REQUEST_INTERVAL_MILLIS = 120000;

    protected DmxmlContext.Block<D> block;

    protected final DmxmlContext context = new DmxmlContext();

    private final LinkManager linkManager = new LinkManager();

    private final PriceSupport priceSupport = new PriceSupport(this);

    private final RendererContext rendererContext
            = new RendererContext(this.linkManager, this.priceSupport);

    private int labelWidth = 0;

    private final Renderer<String> timeRenderer = DateRenderer.dateOrTime(true, "--"); // $NON-NLS-0$

    private ArrayList<QuoteWithInstrument> currentQwis = new ArrayList<QuoteWithInstrument>();

    protected Label lblColumnHeader;

    private final Timer t = new Timer() {
        public void run() {
            update();
        }
    };

    private final boolean renderTime;

    protected final ResponseTypeCallback pushReloadCallback = new ResponseTypeCallback() {
        @Override
        protected void onResult() {
            if (priceSupport.isActive()) {
                updateView();
            }
        }
    };

    // adapted version of de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.PRICE_PERCENT_PUSH
    // MMWEB-573: If a pushed update contains an unchanged change percent but a changed absolute value
    // (percent change is rounded), we previously rendered the percent without change indicator.
    // BUT: the div that contains the percent value is rendered on top of the div that contains the
    // price, so that the price's background is also the background of the percent value.
    // Solution: use compare value provided by getPriceRenderer(), so that the compare results
    // are always identical.
    private final TableCellRenderer MY_CHANGE_PERCENT_PUSH
            = new TableCellRenderers.PushChangeRenderer(Renderer.PERCENT, "mm-right") { // $NON-NLS-0$

        protected String getValue(Price p) {
            return p.getChangePercent();
        }

        public int compareWithPrevious(Price p) {
            return getPriceRenderer().compareWithPrevious(p);
        }
    };


    protected AbstractListWorkspace(final String heading, boolean hideColumnHeader, boolean renderTime) {
        super(heading);
        this.renderTime = renderTime;

        addListener(Events.Render, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent baseEvent) {
                updateLabelWidth(getWidth());
            }
        });

        addListener(Events.Resize, new Listener<BoxComponentEvent>() {
            public void handleEvent(BoxComponentEvent baseEvent) {
                onChangedWidth(baseEvent.getWidth());
            }
        });

        addListener(Events.Expand, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent baseEvent) {
                refresh();
            }
        });

        addListener(Events.Collapse, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent baseEvent) {
                priceSupport.deactivate();
            }
        });

        addSchedulingListener();

        if (!hideColumnHeader) {
            this.lblColumnHeader = new Label("--");
            this.lblColumnHeader.setStyleName("mm-marketElement-header");
            this.add(this.lblColumnHeader);
        }
    }

    protected abstract Price getPrice(T data);

    protected abstract QuoteWithInstrument getQuoteWithInstrument(T data);

    protected abstract List<T> getCurrentList();

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        this.labelWidth = 0;
        if (!isCollapsed()) {
            refresh();
        }
    }

    private boolean isNewPriceAvailable() {
        for (QuoteWithInstrument qwi : this.currentQwis) {
            if (this.priceSupport.isNewerPriceAvailable(qwi.getQuoteData())) {
                return true;
            }
        }
        return false;
    }

    public void onPricesUpdated(PricesUpdatedEvent event) {
        if (isCollapsed() || this.priceSupport.isLatestPriceGeneration()) {
            return;
        }
        if (!event.isPushedUpdate() && this.block.isResponseOk() && isNewPriceAvailable()) {
            updateView();
        }
    }

    private void onDataChanged() {
        this.priceSupport.updatePriceGeneration();
    }


    protected String getStatusText(QuoteWithInstrument qwi) {
        final InstrumentData i = qwi.getInstrumentData();
        final QuoteData q = qwi.getQuoteData();
        if (i.getName() != null) {
            if (q.getMarketVwd() != null) {
                return i.getName() + ", " + q.getMarketVwd(); // $NON-NLS-0$
            }
            return i.getName();
        }
        if (i.getIsin() != null) {
            return i.getIsin();
        }
        return q.getQid();
    }

    protected void updateView() {
        this.priceSupport.invalidateRenderItems();

        this.linkManager.clear();

        removeAll();
        if (this.lblColumnHeader != null) {
            add(this.lblColumnHeader);
        }
        this.currentQwis.clear();
        if (!this.block.isResponseOk()) {
            return;
        }

        this.priceSupport.activate();
        final List<T> list = getCurrentList();

        for (T element : list) {
            final QuoteWithInstrument qwi = getQuoteWithInstrument(element);
            final Html html = createHtml(qwi);
            add(html);
            this.currentQwis.add(qwi);
        }
        layout();
        onDataChanged();
    }

    private Html createHtml(final QuoteWithInstrument qwi) {
        final String qwiTitle = AbstractListWorkspace.this.getStatusText(qwi);
        final Html result = new Html(renderCell(qwi));
        result.addStyleName("mm-marketElement");
        result.setTitle(qwiTitle);
        result.addListener(Events.OnClick, new Listener<BaseEvent>(){
            public void handleEvent(BaseEvent be) {
                qwi.goToPortrait();
            }
        });
        final DragSource dragSource = new DragSource(result) {
            @Override
            protected void onDragStart(DNDEvent event) {
                event.setData(qwi);
                event.getStatus().update(new Label(qwiTitle).getElement());
            }
        };
        dragSource.setGroup("ins"); // $NON-NLS$
        return result;
    }

    protected void setColumnHeader(String header) {
        if (this.lblColumnHeader != null) {
            this.lblColumnHeader.setText(header);
        }
    }

    public void forceRefresh() {
        this.block.setToBeRequested();
        refresh();
    }

    public void onFailure(Throwable throwable) {
        onAsyncResponse();
    }

    public void onSuccess(ResponseType responseType) {
        onAsyncResponse();
    }

    public void refresh() {
        if (!isCollapsed()) {
            this.context.issueRequest(this);
        }
    }

    private void addSchedulingListener() {
        addListener(Events.Render, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent baseEvent) {
                if (!isCollapsed()) {
                    scheduleRefresh(100);
                }
            }
        });
        addListener(Events.Collapse, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent baseEvent) {
                cancelRefresh();
                priceSupport.deactivate();
            }
        });
        addListener(Events.Expand, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent baseEvent) {
                scheduleRefresh(100);
            }
        });
    }

    private void cancelRefresh() {
        this.t.cancel();
    }

    private void scheduleRefresh(final int millis) {
        this.t.schedule(millis);
    }

    protected boolean isAutoRefreshEnabled() {
        return false;
    }

    private void onAsyncResponse() {
        if (this.labelWidth == 0) {
            updateLabelWidth(getWidth());
        }
        updateView();
        if (isAutoRefreshEnabled()) {
            scheduleRefresh(REQUEST_INTERVAL_MILLIS);
        }
    }

    private void onChangedWidth(int rawWidth) {
        if (updateLabelWidth(rawWidth) && this.block != null && this.block.isResponseOk()) {
            updateView();
        }
    }

    private void update() {
        cancelRefresh();
        if (block.isToBeRequested()) {
            refresh();
        }
        else {
            scheduleRefresh(REQUEST_INTERVAL_MILLIS);
        }
    }

    private boolean updateLabelWidth(int width) {
        int tmp = 11;
        if (width > 120) {
            tmp += (width - 120) / 4;
            tmp = Math.min(tmp, 50);
        }

        boolean result = (tmp != labelWidth);
        this.labelWidth = tmp;
        return result;
    }

    protected String renderCell(QuoteWithInstrument qwi) {
        final Price p = this.priceSupport.getCurrentPrice(qwi.getQuoteData());
        final String qwiName = qwi.getName();

        final String pushed = p.isPushable() ? "t" : null; // $NON-NLS$

        final StringBuffer buffer = new StringBuffer();
        HtmlBuilder hb = new HtmlBuilderImpl();
        hb.startTag("div").addClass("mm-fClear"); // $NON-NLS$
        if (this.renderTime) {
            hb.startTag("div").addClass("mm-fRight").addAttribute("p", pushed) // $NON-NLS$
                    .setContent(this.timeRenderer.render(p.getDate()));
        }
        hb.startTag("div").addClass("name"); // $NON-NLS$
        buffer.setLength(0);
        hb.addClass(this.rendererContext.getStyle());
        hb.addClass("mm-link"); // $NON-NLS$
        buffer.append(qwiName.length() <= this.labelWidth + 3 ? qwiName : (qwiName.substring(0, this.labelWidth) + "..."));
        hb.setContent(buffer.toString());
        hb.closeLast(); // closes <div class="mm-fClear">

        hb.startTag("div").addClass("mm-fClear"); // $NON-NLS$
        hb.startTag("div").addClass("mm-fRight").addAttribute("p", pushed); // $NON-NLS$
        buffer.setLength(0);
        TableCellRenderers.CHANGE_PERCENT_PUSH.render(p, buffer, this.rendererContext);
        hb.addClass(this.rendererContext.getStyle());
        hb.setContent(buffer.toString());

        hb.startTag("div").addAttribute("p", pushed).addClass(getPriceClass()); // $NON-NLS$
        buffer.setLength(0);
        getPriceRenderer().render(p, buffer, this.rendererContext);
        hb.addClass(this.rendererContext.getStyle());
        hb.setContent(buffer.toString());
        hb.closeLast(); // closes <div class="mm-fClear">

        return hb.build();
    }

    protected TableCellRenderers.PushCompareRenderer getPriceRenderer() {
        return TableCellRenderers.LAST_PRICE_WITH_SUPPLEMENT_PUSH;
    }

    protected String getPriceClass() {
        return null;
    }

    protected ArrayList<PushRenderItem> getRenderItems() {
        final ArrayList<Element> pushedElements = WidgetUtil.getPushedElements(this, "div"); // $NON-NLS-0$
//        DebugUtil.logToFirebugConsole("found " + pushedTDs.size() + " push tds");
        final ArrayList<PushRenderItem> result = new ArrayList<PushRenderItem>();
        int i = 0;
        for (QuoteWithInstrument qwi : this.currentQwis) {
            final Price price = this.priceSupport.getCurrentPrice(qwi.getQuoteData());
            if (!price.isPushable()) {
                continue;
            }
            if (this.renderTime) {
                result.add(new PushRenderItem(pushedElements.get(i++), price, TableCellRenderers.DATE_OR_TIME_COMPACT_PUSH));
            }
            result.add(new PushRenderItem(pushedElements.get(i++), price, MY_CHANGE_PERCENT_PUSH));
            result.add(new PushRenderItem(pushedElements.get(i++), price, getPriceRenderer()));
        }
//        DebugUtil.logToFirebugConsole("created " + result.size() + " render items");
        return result;
    }
}
