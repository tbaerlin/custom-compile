/*
 * YieldsSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import java.util.ArrayList;
import java.util.List;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.*;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ItemListContext;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class YieldsSnippet extends AbstractSnippet<YieldsSnippet, YieldsSnippetView> {
    private List<String> types;
    private List<String> names;
    private List<DmxmlContext.Block<BlockType>> blocks;

    public static class Class extends SnippetClass {

        public Class() {
            super("Yields"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new YieldsSnippet(context, config);
        }
    }

    private YieldsSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        final List<String> listids = config.getList("listids"); // $NON-NLS-0$
        this.names = config.getList("names"); // $NON-NLS-0$
        this.types = config.getList("types"); // $NON-NLS-0$
        assert listids.size() == this.names.size();
        assert listids.size() == this.types.size();

        this.blocks = new ArrayList<DmxmlContext.Block<BlockType>>(listids.size());
        for (int i = 0; i < listids.size(); i++) {
            final String listid = listids.get(i);
            final String type = this.types.get(i);

            final DmxmlContext.Block<BlockType> block;
            if ("bid/ask".equals(type)) { // $NON-NLS-0$
                block = createBlock("MSC_List_Details"); // $NON-NLS-0$
                block.setParameter("disablePaging", "true"); // $NON-NLS-0$ $NON-NLS-1$
            }
            else {
                block = createBlock("BND_Yields"); // $NON-NLS-0$
            }
            block.setParameter("listid", listid); // $NON-NLS-0$
            this.blocks.add(block);
        }

        this.setView(new YieldsSnippetView(this));
    }

    public void destroy() {
        for (final DmxmlContext.Block block : blocks) {
            destroyBlock(block);
        }
    }

    List<String> getNames() {
        return names;
    }

    public void updateView() {
        final int rows = 10;
        int numOk = 0;

        final DefaultTableDataModel dtm = new DefaultTableDataModel(rows, this.blocks.size() + 1);
        for (int row = 1; row <= rows; row++) {
            dtm.setValueAt(row - 1, 0, I18n.I.nYears(row)); 
        }

        for (int col = 0; col < this.blocks.size(); col++) {
            final DmxmlContext.Block<BlockType> block = this.blocks.get(col);
            if (!block.isResponseOk()) {
                continue;
            }
            numOk++;

            if ("bid/ask".equals(this.types.get(col))) { // $NON-NLS-0$
                final MSCListDetails list = (MSCListDetails) this.blocks.get(col).getResult();
                for (final MSCListDetailElement item : list.getElement()) {
                    final int row = getRow(item.getQuotedata().getVwdcode());
                    if (row < 0 || row >= rows) {
                        continue;
                    }
                    final String value = Renderer.PRICE3.render(item.getPricedata().getBid())
                            + "/" + Renderer.PRICE3.render(item.getPricedata().getAsk()); // $NON-NLS-0$
                    final QuoteWithInstrument qwi = new QuoteWithInstrument(item.getInstrumentdata(), item.getQuotedata(), value, item.getInstrumentdata().getName())
                            .withHistoryContext(ItemListContext.createForPortrait(item, list.getElement(), getView().getTitle()));
                    dtm.setValueAt(row, col + 1, qwi);
                }
            }
            else {
                final BNDYields yields = (BNDYields) this.blocks.get(col).getResult();
                List<BndYieldItem> yield = yields.getYield();
                for (int i = 0, yieldSize = yield.size(); i < yieldSize; i++) {
                    BndYieldItem item = yield.get(i);
                    final String p = item.getPeriod();
                    if (!p.startsWith("P") || !p.endsWith("Y")) { // $NON-NLS-0$ $NON-NLS-1$
                        continue;
                    }
                    final int row = Integer.parseInt(p.substring(1, p.length() - 1)) - 1;
                    if (row >= rows) {
                        continue;
                    }

                    final QuoteWithInstrument qwi = QuoteWithInstrument.createFromQid(item.getQid(), Renderer.PERCENT.render(item.getValue()), item.getName(), "IND") // $NON-NLS$
                            .withHistoryContext(ItemListContext.createForBndYieldPortrait(item, yield, getView().getTitle()));
                    dtm.setValueAt(row, col + 1, qwi);
                }
            }
        }
        if (numOk == 0) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }
        getView().update(dtm);
    }


    private int getRow(String vwdcode) {
        final String euIrsPattern = "EUIRS([0-9]{1,2})J\\.FX(VWD)?"; // $NON-NLS-0$

        try {
            if(vwdcode.matches(euIrsPattern)) {
                return Integer.parseInt(vwdcode.replaceFirst(euIrsPattern, "$1")) - 1; // $NON-NLS-0$
            }
            else {
                final int dot = vwdcode.lastIndexOf("."); // $NON-NLS-0$
                final int year = vwdcode.lastIndexOf("Y"); // $NON-NLS-0$

                if (dot < 0 || year < 0) {
                    Firebug.log("YieldsSnippet: unexpected vwdcode (see '.' or 'Y'): " + vwdcode); // $NON-NLS-0$
                    DebugUtil.logToServer("YieldsSnippet: unexpected vwdcode (see '.' or 'Y'): " + vwdcode); // $NON-NLS-0$
                    return -1;
                }
                return Integer.parseInt(vwdcode.substring(dot + 1, year)) - 1;
            }
        }
        catch (NumberFormatException e) {
            Firebug.error("YieldsSnippet: unexpected vwdcode: " + vwdcode, e); // $NON-NLS-0$
            DebugUtil.logToServer("YieldsSnippet: unexpected vwdcode: " + vwdcode, e); // $NON-NLS-0$
            return -1;
        }
    }
}
