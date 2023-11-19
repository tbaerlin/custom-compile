package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.dmxml.CERDetailedStaticData;
import de.marketmaker.iview.dmxml.CERRatioData;
import de.marketmaker.iview.dmxml.EDGData;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.CertificateTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.extensions.ExtensionTool;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.extensions.StaticDataTableExtension;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.extensions.StaticDataTableExtension.CellMetaDataEntry;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnAndData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnAndDataConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.EdgUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * StaticDataCERSnippet.java
 * Created on Nov 5, 2008 3:28:05 PM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class StaticDataCERSnippet extends AbstractSnippet<StaticDataCERSnippet, ConfigurableTableSnippetView<StaticDataCERSnippet>>
        implements SymbolSnippet {

    public static class Class extends SnippetClass {
        public Class() {
            super("StaticCER"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new StaticDataCERSnippet(context, config);
        }
    }

    private static final String TITLE_TYPE_TRIGGER = "@type@"; // $NON-NLS$

    private final DmxmlContext.Block<CERDetailedStaticData> block;
    private final DmxmlContext.Block<CERRatioData> blockRatios;
    private final DmxmlContext.Block<EDGData> blockEdg;
    private TableColumnAndDataConfig<StaticDataCER> config = null;
    private final String title;
    private final List<StaticDataTableExtension> extensions;

    protected StaticDataCERSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.block = createBlock("CER_DetailedStaticData"); // $NON-NLS-0$
        this.block.setParameter("providerPreference", SessionData.INSTANCE.getGuiDefValue("providerPreference")); // $NON-NLS-0$ $NON-NLS-1$
        this.blockRatios = createBlock("CER_RatioData"); // $NON-NLS-0$
        this.blockEdg = EdgUtil.createBlock(config, context);

        final String title = config.getString("title", I18n.I.staticData());  // $NON-NLS-0$
        this.title = title.contains(TITLE_TYPE_TRIGGER) ? title : null;
        setView(new ConfigurableTableSnippetView<StaticDataCERSnippet>(this, null));

        this.extensions = ExtensionTool.createExtensions(StaticDataTableExtension.class, config, "extensions", context); //$NON-NLS$
    }

    public void destroy() {
        destroyBlock(this.block);
        destroyBlock(this.blockRatios);
        if (this.blockEdg != null) {
            destroyBlock(this.blockEdg);
        }

        ExtensionTool.destroy(this.extensions);
    }

    public void updateView() {
        final ConfigurableTableSnippetView<StaticDataCERSnippet> view = getView();
        if (!this.block.isResponseOk() || !this.blockRatios.isResponseOk()) {
            view.update(DefaultTableDataModel.NULL);
            return;
        }
        final String typeKey = this.block.getResult().getTypeKey();
        if (this.title != null) {
            final StringBuilder sbType = new StringBuilder();
            sbType.append("'").append(CertificateTypeEnum.getCertificateTypeDescription(typeKey)).append("'");

            final StringBuilder sbTimeStamp = new StringBuilder();
            if (getConfiguration().getBoolean("withTimestamp", false)) { // $NON-NLS$
                final String referenceTS = this.blockRatios.getResult().getReferenceTimestamp();
                if (referenceTS != null) {
                    final Date reference = Formatter.parseISODate(referenceTS);
                    final String date = Formatter.LF.formatDate(reference);
                    final String time = Formatter.formatTimeHhmm(reference);
                    sbTimeStamp.append(" (").append(I18n.I.updated()).append(": ")
                            .append(date).append(" ").append(time).append(")");
                }
            }
            final String title = this.title.replace(TITLE_TYPE_TRIGGER, sbType.toString()) + sbTimeStamp.toString();
            getConfiguration().put("title", title); // $NON-NLS-0$
            view.reloadTitle();
        }
        final TableColumnAndData<StaticDataCER> conf
                = this.config.getTableColumnAndData(typeKey);

        view.setConfig(conf);

        final List<RowData> rowDatas = getRowDataList(conf);
        final List<CellMetaDataEntry> metaDataEntries = new ArrayList<StaticDataTableExtension.CellMetaDataEntry>();

        for(StaticDataTableExtension extension : extensions) {
            final int columnCount = conf.getTableColumnModel().getColumnCount();
            extension.addData(rowDatas, metaDataEntries, columnCount);
        }

        DefaultTableDataModel defaultTableDataModel = DefaultTableDataModel.createWithRowData(rowDatas);
        //add cell meta data from extensions
        for(CellMetaDataEntry cmde : metaDataEntries) {
            defaultTableDataModel.setMetaData(cmde.getRow(), cmde.getColumn(), cmde.getCellMetaData());
        }

        view.update(defaultTableDataModel);
    }

    private List<RowData> getRowDataList(TableColumnAndData<StaticDataCER> conf) {
        final StaticDataCER data =
                new StaticDataCER(this.block.getResult(), this.blockRatios.getResult(), getEdgResult());
        return conf.getRowData(data);
    }

    private EDGData getEdgResult() {
        return this.blockEdg != null && this.blockEdg.isResponseOk() ? this.blockEdg.getResult() : null;
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.block.setParameter("symbol", symbol); // $NON-NLS-0$
        this.blockRatios.setParameter("symbol", symbol); // $NON-NLS-0$
        if (this.blockEdg != null) {
            this.blockEdg.setParameter("symbol", symbol); // $NON-NLS-0$
        }

        for(StaticDataTableExtension extension : extensions) {
            extension.setSymbol(type, symbol, name);
        }
    }

    public void setConfig(TableColumnAndDataConfig<StaticDataCER> config) {
        this.config = config;
    }
}