/*
 * NewsHeadlinesSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.BNDDetailedStaticData;
import de.marketmaker.iview.dmxml.BlockType;
import de.marketmaker.iview.dmxml.CERStaticData;
import de.marketmaker.iview.dmxml.EDGData;
import de.marketmaker.iview.dmxml.FNDStaticData;
import de.marketmaker.iview.dmxml.MSCStaticData;
import de.marketmaker.iview.dmxml.OPTStaticData;
import de.marketmaker.iview.dmxml.STKStaticData;
import de.marketmaker.iview.dmxml.WMStaticData;
import de.marketmaker.iview.dmxml.WNTStaticData;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.extensions.ExtensionTool;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.extensions.StaticDataTableExtension;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.EdgUtil;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@NonNLS
public class StaticDataSnippet<T extends BlockType> extends AbstractSnippet<StaticDataSnippet<T>, StaticDataSnippetView<T>>
        implements SymbolSnippet {

    public static class Class extends SnippetClass {
        public Class() {
            super("StaticData");
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return StaticDataSnippet.newInstance(context, config);
        }
    }

    private static final String CONFIG_TYPE = "type";

    private static final String CONFIG_BLOCK_TYPE = "blockType";

    private static final String CONFIG_SYMBOL = "symbol";

    private static final String CONFIG_TITLE = "title";

    private static final String DMXML_SYMBOL = "symbol";

    private final DmxmlContext.Block<T> blockStatic;

    private DmxmlContext.Block<EDGData> blockEdg;

    private DmxmlContext.Block<WMStaticData> blockWmStaticData;

    private final List<StaticDataTableExtension> extensions;

    public static Snippet newInstance(DmxmlContext context, SnippetConfiguration config) {
        final String typeStr = config.getString(CONFIG_TYPE, null);
        final InstrumentTypeEnum type = typeStr == null ? null : InstrumentTypeEnum.valueOf(typeStr);
        final String blockTypeStr = getBlockType(config);

        if (type != null) {
            switch (type) {
                case CUR: {
                    final StaticDataSnippet<STKStaticData> snippet = new StaticDataSnippet<>(context, config);
                    snippet.setView(new StaticDataSnippetView.CurrencyView(snippet));
                    return snippet;
                }
                case IND: {
                    final StaticDataSnippet<MSCStaticData> snippet = new StaticDataSnippet<>(context, config);
                    snippet.setView(new StaticDataSnippetView.IndexView(snippet));
                    return snippet;
                }
            }
        } else {
            switch (blockTypeStr) {
                case "FUT_StaticData": {
                    final StaticDataSnippet<STKStaticData> snippet = new StaticDataSnippet<>(context, config);
                    snippet.setView(new StaticDataSnippetView.FutureView(snippet));
                    return snippet;
                }
                case "OPT_StaticData": {
                    final StaticDataSnippet<OPTStaticData> snippet = new StaticDataSnippet<>(context, config);
                    snippet.setView(new StaticDataSnippetView.OptionView(snippet));
                    return snippet;
                }
                case "STK_StaticData": {
                    final StaticDataSnippet<STKStaticData> snippet = new StaticDataSnippet<>(context, config);
                    snippet.setView(new StaticDataSnippetView.StockView(snippet));
                    return snippet;
                }
                case "MSC_StaticData": {
                    final StaticDataSnippet<MSCStaticData> snippet = new StaticDataSnippet<>(context, config);
                    snippet.setView(new StaticDataSnippetView.MscView(snippet));
                    return snippet;
                }
                case "WNT_StaticData": {
                    final StaticDataSnippet<WNTStaticData> snippet = new StaticDataSnippet<>(context, config);
                    snippet.setView(new StaticDataSnippetView.WarrantView(snippet));
                    return snippet;
                }
                case "BND_DetailedStaticData": {
                    final StaticDataSnippet<BNDDetailedStaticData> snippet = new StaticDataSnippet<>(context, config);
                    snippet.setView(new StaticDataSnippetView.BondView(snippet));
                    return snippet;
                }
                case "CER_StaticData": {
                    final StaticDataSnippet<CERStaticData> snippet = new StaticDataSnippet<>(context, config);
                    snippet.setView(new StaticDataSnippetView.CertificateView(snippet));
                    return snippet;
                }
                case "FND_StaticData": {
                    final StaticDataSnippet<FNDStaticData> snippet = new StaticDataSnippet<>(context, config);
                    snippet.setView(new StaticDataSnippetView.FundView(snippet));
                    return snippet;
                }
            }
        }

        DebugUtil.logToServer(StaticDataSnippet.class.getName() + " does not support " + CONFIG_TYPE + '=' + typeStr + " and " + CONFIG_BLOCK_TYPE + '=' + blockTypeStr);

        // To avoid errors upon initialization, deliver default e.g. MSC_StaticData
        final StaticDataSnippet<MSCStaticData> snippet = new StaticDataSnippet<>(context, config);
        snippet.setView(new StaticDataSnippetView.MscView(snippet));
        return snippet;
    }

    private static String getBlockType(SnippetConfiguration config) {
        String type = config.getString(CONFIG_BLOCK_TYPE, "STK_StaticData");

        // TODO: Remove when FUT_StaticData is available
        if ("FUT_StaticData".equals(type)) {
            return "STK_StaticData";
        }

        return type;
    }

    private StaticDataSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        config.putDefault(CONFIG_TITLE, isShowUnderlying() ? I18n.I.underlyingStaticData() : I18n.I.staticData());

        //Extensions
        this.extensions = ExtensionTool.createExtensions(StaticDataTableExtension.class, config, "extensions", context);

        for (StaticDataTableExtension extension : extensions) {
            extension.setSymbol(null, config.getString(CONFIG_SYMBOL, null), null);
        }

        final String blockType = getBlockType(config);
        this.blockStatic = createBlock(blockType);
        this.blockEdg = EdgUtil.createBlock(config, context);
        this.blockWmStaticData = isWmDataEnabled(blockType) ? createBlock("WM_StaticData") : null;

        setSymbol(null, config.getString(CONFIG_SYMBOL, null), null);
    }
    
    private boolean isWmDataEnabled(String blockType) {
        return Customer.INSTANCE.isOlb() && blockType == "FND_StaticData" || Selector.DZ_BANK_USER.isAllowed();
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name,
            String... compareSymbols) {
        this.blockStatic.setEnabled(symbol != null);

        for (StaticDataTableExtension extension : extensions) {
            extension.setSymbol(type, symbol, name);
        }

        if (isShowUnderlying()) {
            symbol = "underlying(" + symbol + ")";
        }
        if (this.blockEdg != null) {
            this.blockEdg.setParameter(DMXML_SYMBOL, symbol);
        }

        if (this.blockWmStaticData != null) {
            this.blockWmStaticData.setParameter(DMXML_SYMBOL, symbol);
        }

        this.blockStatic.setParameter(DMXML_SYMBOL, symbol);
    }

    public void destroy() {
        destroyBlock(this.blockStatic);

        ExtensionTool.destroy(extensions);

        destroyBlock(this.blockEdg);
        destroyBlock(this.blockWmStaticData);
    }

    protected boolean isAllData() {
        return getConfiguration().getBoolean("allData", true);
    }

    protected boolean isShowUnderlying() {
        return SymbolSnippet.SYMBOL_UNDERLYING.equals(getConfiguration().getString(CONFIG_SYMBOL, null));
    }

    protected boolean isWithQuoteLink() {
        return getConfiguration().getBoolean("withQuoteLink", false);
    }

    boolean isEdgAllowedAndAvailable() {
        return this.blockEdg != null && Selector.EDG_RATING.isAllowed() && this.blockEdg.isResponseOk() &&
                this.blockEdg.getResult().getRating().getEdgTopScore() != null;
    }

    @Override
    public void updateView() {
        if (this.blockStatic.isResponseOk()) {
            getView().update(this.blockStatic.getResult());
        }
        else {
            Firebug.log("<updateView> response not ok");
        }
    }

    public List<StaticDataTableExtension> getExtensions() {
        return extensions;
    }

    public DmxmlContext.Block<WMStaticData> getBlockWmStaticData() {
        return blockWmStaticData;
    }

    public DmxmlContext.Block<EDGData> getBlockEdg() {
        return blockEdg;
    }
}
