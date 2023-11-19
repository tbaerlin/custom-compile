/*
 * NewsHeadlinesSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.dom.client.Element;
import de.marketmaker.iview.dmxml.MSCListDetailElement;
import de.marketmaker.iview.dmxml.MSCListDetails;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QwiAndValue;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItemCollector;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.HtmlBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.util.HtmlBuilderImpl;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.MarketsWorkspace;

import java.util.ArrayList;
import java.util.Date;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MarketOverviewSnippet
        extends AbstractSnippet<MarketOverviewSnippet, SnippetTableView<MarketOverviewSnippet>>
        implements PushRegisterHandler, PushRenderItemCollector {

    public static class Class extends SnippetClass {
        public Class() {
            super("MarketOverview", I18n.I.marketOverview()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new MarketOverviewSnippet(context, config);
        }

        protected void addDefaultParameters(SnippetConfiguration config) {
            config.put("w", "150"); // $NON-NLS-0$ $NON-NLS-1$
            config.put("h", "400"); // $NON-NLS-0$ $NON-NLS-1$
        }
    }

    private DmxmlContext.Block<MSCListDetails> block;

    private final PriceSupport priceSupport = new PriceSupport(this);

    private TableCellRenderer quoteLinkRenderer
            = new TableCellRenderers.QuoteLinkRenderer(20, "").withoutClip(); // $NON-NLS-0$

    public final TableCellRenderer renderer = new TableCellRenderer() {
        private final Renderer<String> timeRenderer = DateRenderer.dateOrTime(true, "--"); // $NON-NLS-0$

        private final StringBuffer buffer = new StringBuffer();

        public void render(Object data, StringBuffer sb, Context context) {
            @SuppressWarnings("unchecked") final QwiAndValue<Price> qwi = (QwiAndValue<Price>) data;
            final Price p = qwi.getValue();
            final String pushed = p.isPushable() ? "t" : null; // $NON-NLS-0$

            HtmlBuilder hb = new HtmlBuilderImpl();
            hb.startTag("div").addClass("mm-marketElement"); // $NON-NLS$

            hb.startTag("div").addClass("mm-fClear"); // $NON-NLS$
            hb.startTag("div").addClass("mm-fRight").addAttribute("p", pushed) // $NON-NLS$
                    .setContent(this.timeRenderer.render(p.getDate()));
            hb.startTag("div").addClass("name"); // $NON-NLS$
            buffer.setLength(0);
            quoteLinkRenderer.render(qwi, buffer, context);
            hb.addClass(context.getStyle());
            hb.setContent(buffer.toString());
            hb.closeLast();

            hb.startTag("div").addClass("mm-fClear"); // $NON-NLS$
            hb.startTag("div").addClass("mm-fRight").addAttribute("p", pushed); // $NON-NLS$
            buffer.setLength(0);
            TableCellRenderers.CHANGE_PERCENT_PUSH.render(p, buffer, context);
            hb.addClass(context.getStyle());
            hb.setContent(buffer.toString());
            hb.startTag("div").addClass("b").addAttribute("p", pushed); // $NON-NLS$
            buffer.setLength(0);
            TableCellRenderers.LAST_PRICE_COMPARE_CHANGE_PUSH.render(p, buffer, context);
            hb.addClass(context.getStyle());
            hb.setContent(buffer.toString());

            hb.closeLast(); // closes <div class="mm-fClear">

            hb.closeLast(); // closes <div class="mm-marketElement">

            sb.append(hb.build());
        }

        public boolean isPushRenderer() {
            return false;
        }        

        public String getContentClass() {
            return null;
        }
    };


    private MarketOverviewSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        this.block = MarketsWorkspace.INSTANCE.copyBlock(context);

        final TableColumnModel tcm = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(null, 1.0f, renderer)
        }, false);

        setView(new SnippetTableView<>(this, tcm).withPriceSupport(this.priceSupport));
    }

    public void onPricesUpdated(PricesUpdatedEvent event) {
        if (!event.isPushedUpdate() && this.block.isResponseOk()) {
            doUpdateView(filterElements(this.block));
        }
    }

    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        if (this.block.isResponseOk()) {
            final int numAdded = event.addVwdcodes(this.block.getResult());
            if (numAdded == this.block.getResult().getElement().size()) {
                event.addComponentToReload(this.block, this);
            }
            if (numAdded > 0) {
                return getView().getRenderItems(this);
            }
        }
        return null;
    }

    public ArrayList<PushRenderItem> collect(ArrayList<Element> pushedTDs) {
        final ArrayList<MSCListDetailElement> elements = filterElements(this.block);
        final ArrayList<PushRenderItem> result = new ArrayList<>();
        int i = 0;
        for (MSCListDetailElement element : elements) {
            final Price price = Price.create(element);
            if (!price.isPushable()) {
                continue;
            }
            result.add(new PushRenderItem(pushedTDs.get(i++), price, TableCellRenderers.DATE_OR_TIME_COMPACT_PUSH));
            result.add(new PushRenderItem(pushedTDs.get(i++), price, TableCellRenderers.LAST_PRICE_COMPARE_CHANGE_PUSH));
            result.add(new PushRenderItem(pushedTDs.get(i++), price, TableCellRenderers.CHANGE_PERCENT_PUSH));
        }
        return result;
    }

    public void destroy() {
        this.priceSupport.deactivate();
        destroyBlock(this.block);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.priceSupport.deactivate();
    }

    public void updateView() {
        this.priceSupport.invalidateRenderItems();
        
        final ArrayList<MSCListDetailElement> elements = filterElements(this.block);
        if (elements.isEmpty()) {
            getView().update(null);
            return;
        }

        doUpdateView(elements);
        this.priceSupport.activate();
    }

    private void doUpdateView(ArrayList<MSCListDetailElement> elements) {
        getView().update(DefaultTableDataModel.create(elements,
                new AbstractRowMapper<MSCListDetailElement>() {
                    public Object[] mapRow(MSCListDetailElement e) {
                        return new Object[]{
                                new QwiAndValue<>(MarketsWorkspace.INSTANCE.getQuoteWithInstrument(e),
                                        priceSupport.getCurrentPrice(e.getQuotedata()))
                        };
                    }
                }));
    }

    /**
     * Removes L-DAX and X-DAX unless they are more recent than the DAX itself
     * @param block .
     * @return list with L-DAX and X-DAX removed if not current
     */
    public static ArrayList<MSCListDetailElement> filterElements(DmxmlContext.Block<MSCListDetails> block) {
        final ArrayList<MSCListDetailElement> result = new ArrayList<>();
        if (!block.isResponseOk()) {
            return result;
        }

        Date daxDate = null;
        for (final MSCListDetailElement element : block.getResult().getElement()) {
            if ("DAX".equals(element.getItemname())) { // $NON-NLS-0$
                daxDate = Formatter.parseISODate(element.getPricedata().getDate());
            }
            else if ("X-DAX".equals(element.getItemname()) || "L-DAX".equals(element.getItemname())) { // $NON-NLS-0$ $NON-NLS-1$
                if (isBefore(element, daxDate)) {
                    continue;
                }
            }

            result.add(element);
        }
        return result;
    }

    private static boolean isBefore(MSCListDetailElement element, Date daxDate) {
        final Date date = Formatter.parseISODate(element.getPricedata().getDate());
        return (date == null || daxDate == null || date.before(daxDate));
    }
}
