/*
 * BestToolSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.dom.client.Element;
import de.marketmaker.iview.dmxml.BestToolElement;
import de.marketmaker.iview.dmxml.FinderGroupCell;
import de.marketmaker.iview.dmxml.FinderGroupItem;
import de.marketmaker.iview.dmxml.FinderGroupRow;
import de.marketmaker.iview.dmxml.FinderGroupTable;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.Selector.DZ_BANK_USER;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BestToolSnippet extends AbstractSnippet<BestToolSnippet, BestToolSnippetView>
        implements LinkListener<QuoteWithInstrument> {

    public static class Class extends SnippetClass {
        public Class() {
            super("BestTool"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new BestToolSnippet(context, config);
        }
    }

    private final boolean hasLeverageProducts;

    private DmxmlContext.Block<FinderGroupTable> block, blockAlt;

    private List<SymbolSnippet> symbolSnippets = null;

    private boolean firstData = true;

    private BestToolSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        this.hasLeverageProducts = DZ_BANK_USER.isAllowed()
                && InstrumentTypeEnum.CER.toString().equals(config.getString("type", null)); // $NON-NLS-0$

        this.block = configureBlock("MSC_BestTool", config); // $NON-NLS-0$

        if (config.getString("type").equals("FND")) { // $NON-NLS-0$ $NON-NLS-1$
            this.block.setParameter("query", Customer.INSTANCE.getFndBestToolQuery()); // $NON-NLS-0$
        }
        else if (config.getString("type").equals("CER")) { // $NON-NLS-0$ $NON-NLS-1$
            this.block.setParameter("query", Customer.INSTANCE.getCerBestToolQuery() + createLeverageExpr(false)); // $NON-NLS-0$
        }
        else {
            this.block.setParameter("query", config.getString("query", "")); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        }

        if (hasLeverageProducts) {
            this.blockAlt = configureBlock("MSC_BestTool", config); // $NON-NLS-0$
            this.blockAlt.setParameter("query", Customer.INSTANCE.getCerBestToolQuery() + createLeverageExpr(true)); // $NON-NLS-0$
        }

        this.setView(new BestToolSnippetView(this));
    }

    private DmxmlContext.Block<FinderGroupTable> configureBlock(String key, SnippetConfiguration config) {
        DmxmlContext.Block<FinderGroupTable> b = createBlock(key);
        b.setParameter("type", config.getString("type", null)); // $NON-NLS-0$ $NON-NLS-1$
        b.setParameter("primaryField", config.getString("primaryField", null)); // $NON-NLS-0$ $NON-NLS-1$
        b.setParameter("numResults", "1"); // $NON-NLS-0$ $NON-NLS-1$
        b.setParameter("sortField", config.getString("sortField", null)); // $NON-NLS-0$ $NON-NLS-1$
        b.setParameter("disablePaging", "true"); // $NON-NLS-0$ $NON-NLS-1$
        b.setParameter("ascending", "false"); // $NON-NLS-0$ $NON-NLS-1$
        return b;
    }

    private String createLeverageExpr(final boolean isAlternative) {
        if (hasLeverageProducts && isAlternative) {
            return "&&dzIsLeverageProduct=='true'"; // $NON-NLS-0$
        } else if (hasLeverageProducts) {
            return "&&dzIsLeverageProduct=='false'"; // $NON-NLS-0$
        } else {
            return "";
        }
    }

    private List<SymbolSnippet> getSymbolSnippets() {
        if (this.symbolSnippets == null) {
            this.symbolSnippets = new ArrayList<>();
            final List<String> detailIds = getConfiguration().getList("detailIds"); // $NON-NLS-0$
            if (detailIds != null) {
                for (String detailId : detailIds) {
                    this.symbolSnippets.add((SymbolSnippet) this.contextController.getSnippet(detailId));
                }
            }
        }
        return this.symbolSnippets;
    }

    public void destroy() {
        destroyBlock(this.block);
        if (hasLeverageProducts) {
            destroyBlock(this.blockAlt);
        }
    }

    public boolean isConfigurable() {
        return true;
    }

    public void updateView() {
        if (!block.isResponseOk()) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        } else if (hasLeverageProducts && !blockAlt.isResponseOk()) {
            final DefaultTableDataModel m = DefaultTableDataModel.NULL;
            getView().update(m, m);
            return;
        }

        if (hasLeverageProducts) {
            getView().update(createTableData(this.block), createTableData(this.blockAlt));
        } else {
            getView().update(createTableData(this.block));
        }
    }

    private TableDataModel createTableData(DmxmlContext.Block<FinderGroupTable> block) {
        final FinderGroupTable result = block.getResult();
        final List<Object[]> list = new ArrayList<>();

        // TODO: move sorting to backend
        final List<FinderGroupRow> rowList = result.getRow();
        Collections.sort(rowList, new Comparator<FinderGroupRow>() {
            public int compare(FinderGroupRow o1, FinderGroupRow o2) {
                final String k1 = renderType(o1.getKey());
                final String k2 = renderType(o2.getKey());
                return k1.compareTo(k2);
            }
        });

        for (final FinderGroupRow row : rowList) {
            for (FinderGroupCell column : row.getColumn()) {
                for (FinderGroupItem item : column.getItem()) {
                    final BestToolElement e = (BestToolElement) item;
                    if (this.firstData) {
                        final String type = e.getInstrumentdata().getType();
                        setDependentSymbol(InstrumentTypeEnum.valueOf(type), e.getQuotedata().getQid());
                        this.firstData = false;
                    }
                    final QuoteWithInstrument qwi = new QuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata());
                    list.add(new Object[]{
                            row.getKey(),
                            qwi,
                            e.getValue(),
                            qwi
                    });
                }
            }
        }

        return DefaultTableDataModel.create(list);
    }

    String renderType(String key) {
        final String type = getConfiguration().getString("type"); // $NON-NLS-0$
        final boolean isCertificate = InstrumentTypeEnum.CER.name().equals(type);
        return isCertificate ? Renderer.CERTIFICATE_CATEGORY.render(key) : key;
    }

    public void onClick(LinkContext<QuoteWithInstrument> context, Element e) {
        final QuoteWithInstrument qwi = context.data;
        final String symbol = qwi.getQuoteData().getQid();
        final String type = qwi.getInstrumentData().getType();
        setDependentSymbol(InstrumentTypeEnum.valueOf(type), symbol);
    }

    private void setDependentSymbol(InstrumentTypeEnum type, String symbol) {
        getView().getLocalLinkRenderer().setSelectedSymbol(symbol);
        for (final SymbolSnippet s : getSymbolSnippets()) {
            s.setSymbol(type, symbol, null);
        }
        ackParametersChanged();
    }

    protected boolean hasLeverageProducts() {
        return hasLeverageProducts;
    }
}
