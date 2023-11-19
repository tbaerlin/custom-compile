/*
 * PriceListSnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Orderbook;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class OrderbookSnippetView extends SnippetTableView<OrderbookSnippet> {

    private static abstract class ItemRenderer implements TableCellRenderer {
        protected final TableCellRenderer delegate;

        private ItemRenderer(TableCellRenderer delegate) {
            this.delegate = delegate;
        }

        public boolean isPushRenderer() {
            return true;
        }

        public String getContentClass() {
            return this.delegate.getContentClass();
        }

        public void render(Object data, StringBuffer sb, Context context) {
            if (data == null) {
                sb.append("&nbsp;"); // $NON-NLS-0$
                return;
            }
            doRender((Orderbook.Item)data, sb, context);
        }

        protected abstract void doRender(Orderbook.Item item, StringBuffer sb, Context context);
    }

    private static class ItemPriceRenderer extends ItemRenderer {
        private ItemPriceRenderer(TableCellRenderer renderer) {
            super(renderer);
        }

        protected void doRender(Orderbook.Item data, StringBuffer sb, Context context) {
            this.delegate.render(data.getPrice(), sb, context);
        }
    }

    private static class ItemVolumeRenderer extends ItemRenderer {
        private ItemVolumeRenderer(TableCellRenderer renderer) {
            super(renderer);
        }

        protected void doRender(Orderbook.Item data, StringBuffer sb, Context context) {
            this.delegate.render(data.getVolume(), sb, context);
        }
    }

    public OrderbookSnippetView(OrderbookSnippet snippet) {
        super(snippet, new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn("", 0.25f, TableCellRenderers.EXTEND_BAR_RIGHT), // $NON-NLS-0$
                new TableColumn(I18n.I.volumeBid(), 0.1f, new ItemVolumeRenderer(TableCellRenderers.DEFAULT_RIGHT)),
                new TableColumn(I18n.I.bid(), 0.1f, new ItemPriceRenderer(TableCellRenderers.PRICE)),
                new TableColumn("", 0.1f, TableCellRenderers.LABEL_CENTER), // $NON-NLS-0$
                new TableColumn(I18n.I.ask(), 0.1f, new ItemPriceRenderer(TableCellRenderers.PRICE_LEFT)),
                new TableColumn(I18n.I.volumeAsk(), 0.1f, new ItemVolumeRenderer(TableCellRenderers.DEFAULT)),
                new TableColumn("", 0.25f, TableCellRenderers.EXTEND_BAR_LEFT), // $NON-NLS-0$
        }));
        setTitle(I18n.I.orderBook()); 
        this.panel.setWidth("100%"); // $NON-NLS-0$
    }
}
