/*
 * FundStrategySnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.fund;

import de.marketmaker.iview.dmxml.FNDStaticData;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTextView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FundStrategySnippet extends AbstractSnippet<FundStrategySnippet, SnippetTextView<FundStrategySnippet>> implements SymbolSnippet {
    public static class Class extends SnippetClass {
        public Class() {
            super("FundStrategy", I18n.I.strategy()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new FundStrategySnippet(context, config);
        }
    }

    private final DmxmlContext.Block<FNDStaticData> block;

    private FundStrategySnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        this.setView(new SnippetTextView<>(this));

        this.block = createBlock("FND_StaticData"); // $NON-NLS-0$
        setSymbol(InstrumentTypeEnum.FND, config.getString("symbol", null), null); // $NON-NLS-0$
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
            getView().setText(this.block.getResult().getStrategy());
        }
        else {
            getView().setText(""); // $NON-NLS-0$
        }
    }
}
