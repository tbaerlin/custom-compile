/*
 * RendererContext.java
 *
 * Created on 01.02.2010 17:39:06
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.table;

import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkManager;

/**
* @author oflege
*/
public class RendererContext implements TableCellRenderer.Context {

    private final LinkManager linkManager;

    private final PriceSupport priceSupport;

    private String style;

    public RendererContext(LinkManager linkManager, PriceSupport priceSupport) {
        this.linkManager = linkManager;
        this.priceSupport = (priceSupport != null) ? priceSupport : new PriceSupport(null);
    }

    public int getPriceGeneration() {
        return this.priceSupport.getPriceGeneration();
    }

    public void setStyle(String s) {
        this.style = s;
    }

    public String getStyle() {
        String result = this.style;
        this.style = null;
        return result;
    }

    public boolean isPush() {
        return false;
    }

    public void updatePriceGeneration() {
        this.priceSupport.updatePriceGeneration();
    }

    public void appendLink(LinkContext lc, String content, String tooltip, StringBuffer sb) {
        linkManager.appendLink(lc, content, tooltip,sb);
    }

    public void appendLink(String token, String content, String tooltip, StringBuffer sb) {
        linkManager.appendLink(token, content, tooltip,sb);
    }
}
