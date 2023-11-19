/*
 * SellHoldBuyRenderer.java
 *
 * Created on 20.08.2008 17:08:26
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.util;

import de.marketmaker.iview.mmgwt.mmweb.client.data.SellHoldBuy;

/**
 * @author Ulrich Maurer
 */
public class SellHoldBuyRenderer implements Renderer<SellHoldBuy> {
    public String render(SellHoldBuy buyHoldSell) {
        final StringBuffer sb = new StringBuffer();
        sb.append("<div class=\"mm-sellHoldBuy\" style=\"width: ").append(buyHoldSell.getMaxAllPercent()).append("%\">"); // $NON-NLS-0$ $NON-NLS-1$
        sb.append("<div class=\"mm-shb sell\" style=\"width: ").append(buyHoldSell.getAllSellPercent()).append("%\"></div>"); // $NON-NLS-0$ $NON-NLS-1$
        sb.append("<div class=\"mm-shb hold\" style=\"width: ").append(buyHoldSell.getHoldPercent()).append("%\"></div>"); // $NON-NLS-0$ $NON-NLS-1$
        sb.append("<div class=\"mm-shb buy\" style=\"width: ").append(buyHoldSell.getAllBuyPercent()).append("%\"></div>"); // $NON-NLS-0$ $NON-NLS-1$
        sb.append("</div>"); // $NON-NLS-0$
        return sb.toString();
    }
}
