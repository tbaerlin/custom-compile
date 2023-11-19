/*
 * FNDYieldsSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.fund;

import com.extjs.gxt.ui.client.util.DateWrapper;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.MSCQuarterlyYields;
import de.marketmaker.iview.dmxml.QuarterlyYield;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FNDYieldsSnippet extends AbstractSnippet<FNDYieldsSnippet, SnippetTableView<FNDYieldsSnippet>> implements SymbolSnippet {
    public static class Class extends SnippetClass {

        public Class() {
            super("FNDYields", I18n.I.quarterYields()); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new FNDYieldsSnippet(context, config);
        }
    }

    private static class YieldItem {
        private static final YieldItem EMPTY = new YieldItem("", "", "");

        private String from;

        private String to;

        private String yield;

        static YieldItem create(QuarterlyYield entry) {
            return (entry != null)
                    ? new YieldItem(entry.getFrom(), entry.getTo(), entry.getYield())
                    : EMPTY;
        }

        private YieldItem(String from, String to, String yield) {
            this.from = from;
            this.to = to;
            this.yield = yield;
        }

        public String getFrom() {
            return Formatter.LF.formatDate(this.from);
        }

        public String getTo() {
            return Formatter.LF.formatDate(this.to);
        }

        public String getYield() {
            return this.yield;
        }
    }


    private final DmxmlContext.Block<MSCQuarterlyYields> block;

    private FNDYieldsSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        this.setView(new SnippetTableView<>(this,
                new DefaultTableColumnModel(new TableColumn[]{
                        new TableColumn(I18n.I.year(), 0.4f, new TableCellRenderers.StringRenderer("--", "mm-snippetTable-label")),  // $NON-NLS$
                        new TableColumn(I18n.I.quarterShortcut(1), 0.15f, TableCellRenderers.STRING_RIGHT), 
                        new TableColumn(I18n.I.quarterShortcut(2), 0.15f, TableCellRenderers.STRING_RIGHT), 
                        new TableColumn(I18n.I.quarterShortcut(3), 0.15f, TableCellRenderers.STRING_RIGHT), 
                        new TableColumn(I18n.I.quarterShortcut(4), 0.15f, TableCellRenderers.STRING_RIGHT) 
                })));

        this.block = this.context.addBlock("MSC_QuarterlyYields"); // $NON-NLS$
        setSymbol(InstrumentTypeEnum.FND, config.getString("symbol", null), null); // $NON-NLS$
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.block.setEnabled(symbol != null);
        this.block.setParameter("symbol", symbol); // $NON-NLS-0$
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public void updateView() {
        if (this.block.isResponseOk()) {
            getView().update(createTableDataModel());
        } else {
            getView().update(DefaultTableDataModel.NULL);
        }
    }

    private DefaultTableDataModel createTableDataModel() {
        int listSize = this.block.getResult().getElement().size();
        if (listSize != 21) {
            Firebug.log("this.block.getResult().getElement().size() != 21"); // $NON-NLS$
        }

        final DateWrapper dw = new DateWrapper();
        final int currentYear = dw.getFullYear();

        List<YieldItem> yields = createYieldItems(listSize, Integer.toString(currentYear));
        // write the table
        final DefaultTableDataModel result = new DefaultTableDataModel(5, 5);
        for (int i = 0; i < 5; i++) {
            final int year = currentYear - i;
            String[] line = new String[5];
            line[0] = Integer.toString(year);
            for (int n = 0; n < 4; n++) {
                int index = i * 4 + n;
                YieldItem item = yields.get(index);
                if ("".equals(item.yield)) { // $NON-NLS-0$
                    line[4 - n] = item.yield;
                } else {
                    line[4 - n] = Renderer.TOOLTIP.render(new String[] {Renderer.CHANGE_PERCENT.render(item.yield),
                    item.getFrom() + " - " + item.getTo()});
                }
            }
            result.setValuesAt(i, line);
        }
        return result;
    }

    private List<YieldItem> createYieldItems(int listSize, String currentYear) {
        final List<YieldItem> result = new ArrayList<>();
        final List<QuarterlyYield> quarterlyYields = this.block.getResult().getElement();
        // fill up empty quarters of the current year
        for (int i = 0; i < 4; i++) {
            final QuarterlyYield entry = quarterlyYields.get(i);
            final String entryYear = Formatter.LF.formatDateYyyy(entry.getFrom(), "");
            if (!currentYear.equals(entryYear)) {
                result.add(YieldItem.EMPTY);
            }
        }
        // copy the rest into yields
        final int size = (listSize - result.size() - 1);
        for (int i = 0; i < size; i++) {
            result.add(YieldItem.create(quarterlyYields.get(i)));
        }
        return result;
    }

}
