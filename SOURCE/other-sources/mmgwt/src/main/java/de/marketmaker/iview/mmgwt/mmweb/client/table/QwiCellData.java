package de.marketmaker.iview.mmgwt.mmweb.client.table;

import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkManager;

import java.util.List;

/**
 * Created on 22.09.2010 09:42:14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class QwiCellData extends CellData<QuoteWithInstrument> {

    public QwiCellData(QuoteWithInstrument qwi) {
        super(null, qwi, Sorting.NONE);
    }

    @Override
    public String getRenderedValue() {
        return this.value.getName();
    }

    public String getRenderedValue(RendererContext context) {
        TableCellRenderers.QuoteLinkRenderer qlr = new TableCellRenderers.QuoteLinkRenderer(100, "--");
        final StringBuffer result = new StringBuffer();
        qlr.render(this.value, result, context);
        return result.toString();
    }

    @Override
    public void computeRanking(List<? extends CellData> data) {
        // nothing to do
    }

    @Override
    public boolean isHtml() {
        return true;
    }
}
