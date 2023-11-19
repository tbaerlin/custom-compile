package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.dom.client.Element;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.MSCListDetailElement;
import de.marketmaker.iview.dmxml.MSCListDetails;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.util.CurrencyRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * NationalEconomyMatrixSnippet.java
 * <p/>
 * Created on Oct 1, 2008 3:50:35 PM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class NationalEconomyMatrixSnippet extends AbstractSnippet<NationalEconomyMatrixSnippet, SnippetTableView<NationalEconomyMatrixSnippet>> {

    public static class Class extends SnippetClass {
        public Class() {
            super("NationalEconomyMatrix"); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new NationalEconomyMatrixSnippet(context, config);
        }
    }

    private final DmxmlContext.Block<MSCListDetails> block;
    private final List<String> rows;
    private final List<String> columns;

    public NationalEconomyMatrixSnippet(DmxmlContext context, SnippetConfiguration configuration) {
        super(context, configuration);

        this.rows = configuration.getList("rows"); // $NON-NLS$
        this.columns = configuration.getList("columns"); // $NON-NLS$

        final List<String> vwdcodes = new ArrayList<>();

        for (final String row : this.rows) {
            final List<String> symbols = configuration.getList(row);
            if (symbols.size() != this.columns.size()) {
                throw new IllegalArgumentException("#symbols != #columns for " + configuration.getString("title")); // $NON-NLS$
            }
            for (final String symbol : symbols) {
                if (StringUtil.hasText(symbol)) {
                    vwdcodes.add(symbol);
                }
            }
        }

        this.block = this.context.addBlock("MSC_PriceDataMulti"); // $NON-NLS$
        this.block.setParameter("symbolStrategy", "vwdcode"); // $NON-NLS$
        this.block.setParameters("symbol", vwdcodes.toArray(new String[vwdcodes.size()])); // $NON-NLS$

        final TableColumn[] cols = new TableColumn[this.columns.size() + 1];
        cols[0] = new TableColumn("", 0.1f, new TableCellRenderers.StringRenderer("--", "mm-snippetTable-label")); // $NON-NLS$
        for (int i = 0; i < this.columns.size(); i++) {
            cols[i + 1] = new TableColumn(this.columns.get(i), 0.1f, TableCellRenderers.DEFAULT);
        }
        this.setView(new SnippetTableView<>(this, new DefaultTableColumnModel(cols)));
    }

    public void destroy() {
    }

    public void updateView() {
        if (!this.block.isResponseOk()) {
            return;
        }

        final List<MSCListDetailElement> elements = this.block.getResult().getElement();
        final Map<String, MSCListDetailElement> elementByVwdcode = new HashMap<>();
        for (final MSCListDetailElement element : elements) {
            if (element.getQuotedata() != null) {
                elementByVwdcode.put(element.getQuotedata().getVwdcode(), element);
            }
        }

        final DefaultTableDataModel dtm = new DefaultTableDataModel(this.rows.size(), this.columns.size() + 1);

        for (int i = 0; i < this.rows.size(); i++) {
            dtm.setValueAt(i, 0, this.rows.get(i));
        }

        final Set<String> setMissingVwdcodes = new HashSet<>();
        for (int i = 0; i < this.rows.size(); i++) {
            final String row = this.rows.get(i);
            final List<String> symbols = getConfiguration().getList(row);
            for (int j = 1; j < symbols.size() + 1; j++) {
                final String vwdcode = symbols.get(j - 1);
                final MSCListDetailElement element = elementByVwdcode.get(vwdcode);
                if (element == null) {
                    if (!(vwdcode == null || vwdcode.isEmpty())) {
                        setMissingVwdcodes.add(vwdcode);
                    }
                    dtm.setValueAt(i, j, "<center>--</center>"); // $NON-NLS$
                }
                else if (element.getPricedata().getPrice() == null) {
                    dtm.setValueAt(i, j, "<center>--</center>"); // $NON-NLS$
                }
                else {
                    final Link link = createLink(element);
                    dtm.setValueAt(i, j, link);
                }
            }
        }
        if (!setMissingVwdcodes.isEmpty()) {
            final String logMessage = "NationalEconomyMatrixSnippet: no data available for " + setMissingVwdcodes.toString(); // $NON-NLS$
            Firebug.log(logMessage);
            DebugUtil.logToServer(logMessage);
        }
        getView().update(dtm);
    }

    private Link createLink(final MSCListDetailElement element) {
        final String price = Renderer.PRICE.render(element.getPricedata().getPrice());
        final String currency = CurrencyRenderer.DEFAULT.render(element.getQuotedata().getCurrencyIso());
        final String text = currency + "<div class=\"economy-price-date\"> (" // $NON-NLS$
                + Formatter.LF.formatDateShort(element.getPricedata().getDate()) + ")</div>"; // $NON-NLS$
        return new Link(new LinkListener<Link>() {
            public void onClick(LinkContext linkContext, Element e) {
                PlaceUtil.goToChartcenter(element.getInstrumentdata(), element.getQuotedata());
            }
        }, "<center><span class=\"mm-link-center\">" + price + " " + text + "</span></center>", null); // $NON-NLS$
        //text-align: center doesn´t work
    }
}
