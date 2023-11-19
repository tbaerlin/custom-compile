/*
 * ItemListContext.java
 *
 * Created on 29.11.12 11:24
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.history;

import de.marketmaker.iview.dmxml.*;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Lösch
 */

public abstract class ItemListContext<T extends ContextItem> implements HistoryContext<ContextList<T>, ItemListContext<T>> {
    private final ContextList<T> list;

    private final String contextName;

    private boolean breadCrumb;

    private Map<String, String> properties = null;

    private final String iconKey;

    protected ItemListContext(ContextList<T> list, String contextName, String iconKey, boolean breadCrumb) {
        this.list = list;
        this.contextName = contextName;
        this.iconKey = iconKey;
        this.breadCrumb = breadCrumb;
    }

    @Override
    public ContextList<T> getValue() {
        return this.list;
    }

    public String getName() {
        return contextName;
    }

    @Override
    public String getIconKey() {
        return this.iconKey;
    }

    @Override
    public ItemListContext<T> withoutBreadCrumb() {
        final ItemListContext delegate = this;
        final ItemListContext<T> result = new ItemListContext<T>(this.list, this.contextName, this.iconKey, false) {
            @Override
            public void action() {
                delegate.action();
            }
        };
        result.properties = this.properties;
        return result;
    }

    public static HistoryContext createForWntPortrait(WNTFinderElement e, List<WNTFinderElement> elements, String contextName) {
        final List<QuoteWithInstrument> list = new ArrayList<>();
        for (WNTFinderElement element : elements) {
            list.add(new QuoteWithInstrument(element.getInstrumentdata(), element.getQuotedata()));
        }
        return createQwiList(e.getQuotedata().getQid(), contextName, list);

    }

    public static HistoryContext createForCerPortrait(CERFinderElement e, List<CERFinderElement> elements, String contextName) {
        final List<QuoteWithInstrument> list = new ArrayList<>();
        for (CERFinderElement element : elements) {
            list.add(new QuoteWithInstrument(element.getInstrumentdata(), element.getQuotedata()));
        }
        return createQwiList(e.getQuotedata().getQid(), contextName, list);
    }

    public static HistoryContext createForBndPortrait(BNDFinderElement e, List<BNDFinderElement> elements, String contextName) {
        final List<QuoteWithInstrument> list = new ArrayList<>();
        for (BNDFinderElement element : elements) {
            list.add(new QuoteWithInstrument(element.getInstrumentdata(), element.getQuotedata()));
        }
        return createQwiList(e.getQuotedata().getQid(), contextName, list);
    }

    public static ItemListContext<QuoteWithInstrument> createForBndYieldPortrait(BndYieldItem item, List<BndYieldItem> yields, String contextName) {
        final List<QuoteWithInstrument> list = new ArrayList<>();
        for (BndYieldItem yield : yields) {
            final QuoteWithInstrument qwi = QuoteWithInstrument.createFromQid(yield.getQid(), Renderer.PERCENT.render(yield.getValue()), yield.getName(), "IND"); // $NON-NLS$
            list.add(qwi);
        }
        return createQwiList(item.getQid(), contextName, list);
    }

    public static ItemListContext<QuoteWithInstrument> createForFndPortrait(FNDFinderElement e, List<FNDFinderElement> elements, String contextName) {
        final List<QuoteWithInstrument> list = new ArrayList<>();
        for (FNDFinderElement element : elements) {
            list.add(new QuoteWithInstrument(element.getInstrumentdata(), element.getQuotedata()));
        }
        return createQwiList(e.getQuotedata().getQid(), contextName, list);
    }

    public static HistoryContext createForPortrait(InstrumentDateItem e, List<InstrumentDateItem> elements) {
        final List<QuoteWithInstrument> list = new ArrayList<>();
        for (InstrumentDateItem element : elements) {
            if (e.getTitle().equals(element.getTitle()) && e.getDate().equals(element.getDate())) {
                list.add(new QuoteWithInstrument(element.getInstrumentdata(), element.getQuotedata()));
            }
        }
        return createQwiList(e.getQuotedata().getQid(), e.getTitle(), list);
    }

    public static ItemListContext<QuoteWithInstrument> createForPortrait(IdentifierData e, List<IdentifierDataWithPrio> elements, String contextName) {
        final List<QuoteWithInstrument> list = new ArrayList<>();
        for (IdentifierDataWithPrio element : elements) {
            list.add(new QuoteWithInstrument(element.getInstrumentdata(), element.getQuotedata()));
        }
        return createQwiList(e.getQuotedata().getQid(), contextName, list);
    }

    public static ItemListContext<QuoteWithInstrument> createForPortrait(PortfolioPositionElement e, List<PortfolioPositionElement> elements, String contextName) {
        final List<QuoteWithInstrument> list = new ArrayList<>();
        for (PortfolioPositionElement element : elements) {
            list.add(new QuoteWithInstrument(element.getInstrumentdata(), element.getQuotedata()));
        }
        return createQwiList(e.getQuotedata().getQid(), contextName, list);
    }

    public static ItemListContext<QuoteWithInstrument> createForPortrait(WatchlistPositionElement e, List<WatchlistPositionElement> elements, String contextName) {
        final List<QuoteWithInstrument> list = new ArrayList<>();
        for (WatchlistPositionElement element : elements) {
            list.add(new QuoteWithInstrument(element.getInstrumentdata(), element.getQuotedata()));
        }
        return createQwiList(e.getQuotedata().getQid(), contextName, list);
    }

    public static ItemListContext<QuoteWithInstrument> createForPortrait(MSCPriceDataExtendedElement e, List<MSCPriceDataExtendedElement> elements, String contextName) {
        final List<QuoteWithInstrument> list = new ArrayList<>();
        for (MSCPriceDataExtendedElement element : elements) {
            list.add(new QuoteWithInstrument(element.getInstrumentdata(), element.getQuotedata()));
        }
        return createQwiList(e.getQuotedata().getQid(), contextName, list);
    }

    public static ItemListContext<QuoteWithInstrument> createForPortrait(MSCInstrumentPriceSearchElement e, List<MSCInstrumentPriceSearchElement> elements, String contextName) {
        final List<QuoteWithInstrument> list = new ArrayList<>();
        for (MSCInstrumentPriceSearchElement element : elements) {
            list.add(new QuoteWithInstrument(element.getInstrumentdata(), element.getQuotedata()));
        }
        return createQwiList(e.getQuotedata().getQid(), contextName, list);
    }

    public static ItemListContext<QuoteWithInstrument> createForPortrait(MSCListDetailElement e, List<MSCListDetailElement> elements, String contextName) {
        return createForPortrait(e.getQuotedata().getQid(), elements, contextName);
    }

    public static ItemListContext<QuoteWithInstrument> createForPortrait(String qid, List<MSCListDetailElement> elements, String contextName) {
        final List<QuoteWithInstrument> list = new ArrayList<>();
        for (MSCListDetailElement element : elements) {
            final QuoteWithInstrument qwi = QuoteWithInstrument.createQuoteWithInstrument(element.getInstrumentdata(), element.getQuotedata(), element.getItemname());
            if(qwi == null) {
                continue;
            }
            list.add(qwi);
        }
        return createQwiList(qid, contextName, list);
    }

    public static ItemListContext<QuoteWithInstrument> createQwiList(String selectedQid, final String contextName, List<QuoteWithInstrument> list) {
        final QwiList qwis = new QwiList(list);
        qwis.setSelected(selectedQid);
        return new ItemListContext<QuoteWithInstrument>(qwis, contextName, null, true) {
            @Override
            public void action() {
                PlaceUtil.goToPortraitUndefView(qwis.getSelected().getInstrumentData(), qwis.getSelected().getQuoteData(),
                        withoutBreadCrumb());
            }
        };
    }

    @Override
    public boolean isBreadCrumb() {
        return this.breadCrumb;
    }

    public String putProperty(String key, String value) {
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        return this.properties.put(key, value);
    }

    public String getProperty(String key) {
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        return this.properties.get(key);
    }
}