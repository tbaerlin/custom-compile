/*
 * PriceListSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.dmxml.CURCrossRateTable;
import de.marketmaker.iview.dmxml.CrossRateItem;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CrossrateSnippet extends AbstractSnippet<CrossrateSnippet, CrossrateSnippetView> {
    public static class Class extends SnippetClass {
        public Class() {
            super("Crossrates", I18n.I.crossRates()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new CrossrateSnippet(context, config);
        }
    }

    private DmxmlContext.Block<CURCrossRateTable> block;

    private final String[] isoCodes;

    private CrossrateSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.block = createBlock("CUR_CrossRateTable"); // $NON-NLS-0$
        this.isoCodes = config.getArray("isocode"); // $NON-NLS-0$
        this.block.setParameters("isocode", this.isoCodes); // $NON-NLS-0$

        this.setView(new CrossrateSnippetView(this));
    }

    int size() {
        return this.isoCodes.length;
    }

    String getIsoCode(int i) {
        return this.isoCodes[i];
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public void updateView() {
        if (!block.isResponseOk()) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }

        final CURCrossRateTable result = this.block.getResult();

        final DefaultTableDataModel dtm = new DefaultTableDataModel(size(), size() + 1);
        for (int i = 0; i < this.isoCodes.length; i++) {
            dtm.setValueAt(i, 0, this.isoCodes[i]);
        }

        for (CrossRateItem item : result.getCell()) {
            final String from = item.getFrom();
            final String to = item.getTo();
            final String price = item.getPrice();
            dtm.setValueAt(getPosition(from), getPosition(to) + 1, price);

        }

        getView().update(dtm);
    }

    private int getPosition(String s) {
        for (int i = 0; i < this.isoCodes.length; i++) {
            if (this.isoCodes[i].equals(s)) {
                return i;
            }
        }
        assert false;
        return 0;
    }
}
