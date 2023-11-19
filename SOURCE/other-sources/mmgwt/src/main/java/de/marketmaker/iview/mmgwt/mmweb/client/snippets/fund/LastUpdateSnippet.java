package de.marketmaker.iview.mmgwt.mmweb.client.snippets.fund;

import de.marketmaker.iview.dmxml.FNDAllocations;
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
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author umaurer
 */
@NonNLS
public class LastUpdateSnippet extends
        AbstractSnippet<LastUpdateSnippet, SnippetTextView<LastUpdateSnippet>> implements
        SymbolSnippet {
    public static class Class extends SnippetClass {
        public Class() {
            super("LastUpdate");
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new LastUpdateSnippet(context, config);
        }
    }

    private DmxmlContext.Block<FNDStaticData> staticDataBlock;

    private DmxmlContext.Block<FNDAllocations> allocationsBlock;

    LastUpdateSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.setView(new SnippetTextView<>(this));
        getView().setStyleName("mm-right");

        this.staticDataBlock = context.addBlock("FND_StaticData");
        this.allocationsBlock = context.addBlock("FND_Allocations");
    }

    public void destroy() {
        destroyBlock(this.staticDataBlock);
        destroyBlock(this.allocationsBlock);
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name,
            String... compareSymbols) {
        this.staticDataBlock.setParameter("symbol", symbol);
        this.allocationsBlock.setParameter("symbol", symbol);
        this.allocationsBlock.setParameter("withConsolidatedAllocations", "true");
    }

    public void updateView() {
        String text;

        if (this.staticDataBlock.isResponseOk()) {
            final FNDStaticData sd = this.staticDataBlock.getResult();
            final String portfolioDate = sd.getPortfolioDate();
            text = I18n.I.lastUpdate() + ": " + (portfolioDate == null ? "--" : portfolioDate);

            if (this.allocationsBlock.isResponseOk() && "VWD".equals(allocationsBlock.getResult().getSource())) {
                text += I18n.I.copyrightVwdGroup();
            }
            else if (this.allocationsBlock.isResponseOk() && "MORNINGSTAR".equals(allocationsBlock.getResult().getSource())) {
                text += I18n.I.copyrightMorningstar();
            }
        } else {
            text = I18n.I.lastUpdate() + ": --";
        }

        getView().setText(text);
    }
}
