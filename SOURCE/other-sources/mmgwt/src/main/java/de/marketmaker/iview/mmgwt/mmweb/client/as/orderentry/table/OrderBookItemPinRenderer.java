/*
 * OrderBookItemPinRenderer.java
 *
 * Created on 07.11.13 14:00
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.table;

import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.pmxml.OrderbookDataType;

/**
 * @author Markus Dick
 */
public class OrderBookItemPinRenderer<T extends OrderbookDataType> implements TableCellRenderer {
    protected final String contentClass;
    protected final LinkListener<T> pinLinkListener;
    protected final LinkListener<T> linkListener;

    public OrderBookItemPinRenderer(LinkListener<T> pinLinkListener, LinkListener<T> linkListener) {
        this("", pinLinkListener, linkListener);
    }

    public OrderBookItemPinRenderer(String contentClass, LinkListener<T> pinLinkListener, LinkListener<T> linkListener) {
        this.contentClass = contentClass;
        this.pinLinkListener = pinLinkListener;
        this.linkListener = linkListener;
    }

    @SuppressWarnings("unchecked")
    public void render(Object data, StringBuffer sb, TableCellRenderer.Context context) {
        final T orderBookDataType = (T)data;
        final LinkContext<T> pinLc = new LinkContext<T>(this.pinLinkListener, orderBookDataType);
        final LinkContext<T> lc = new LinkContext<T>(this.linkListener, orderBookDataType);
        final String orderNumber = orderBookDataType.getOrderNumber();

        TableCellRenderers.renderPinLink(context, sb, pinLc, lc, orderNumber, orderNumber, false, true);
    }

    @Override
    public boolean isPushRenderer() {
        return false;
    }

    @Override
    public String getContentClass() {
        return this.contentClass;
    }
}
