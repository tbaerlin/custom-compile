/*
 * GisStaticDataSnippet.java
 *
 * Created on 12.04.2013 11:47:42
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.extensions.ExtensionTool;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.extensions.StaticDataTableExtension;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.extensions.StaticDataTableExtension.CellMetaDataEntry;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Markus Dick
 */
public class GisStaticDataSnippet extends AbstractSnippet<GisStaticDataSnippet,
        SnippetTableView<GisStaticDataSnippet>> implements SymbolSnippet {

    private boolean visible = true;

    public static class Class extends SnippetClass {
        public Class() {
            super("GisStaticData"); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new GisStaticDataSnippet(context, config);
        }
    }

    private final StaticDataTableExtension gisStaticDataExtension;
    private final List<StaticDataTableExtension> extensions;

    private GisStaticDataSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        config.put("title", I18n.I.gisHintsForCustomerConsulants()); //$NON-NLS$

        this.setView(SnippetTableView.create(this, new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.type(), 0.3f, TableCellRenderers.DEFAULT_LABEL),
                new TableColumn(I18n.I.value(), 0.7f, TableCellRenderers.DEFAULT_RIGHT)
        })));

        //Extensions
        this.gisStaticDataExtension = ExtensionTool.createExtension(StaticDataTableExtension.class, config, "GisStaticDataExtension", context); //$NON-NLS$
        this.extensions = ExtensionTool.createExtensions(StaticDataTableExtension.class, config, "extensions", context); //$NON-NLS$

        setSymbol(null, config.getString("symbol", null), null); // $NON-NLS$
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        if(StringUtil.hasText(name)) {
            this.getConfiguration().put("title", name); //$NON-NLS$
        }
        else {
            this.getConfiguration().put("title", I18n.I.gisHintsForCustomerConsulants()); //$NON-NLS$
        }

        setVisible(StringUtil.hasText(symbol));

        if (this.gisStaticDataExtension != null) {
            this.gisStaticDataExtension.setSymbol(type, symbol, name);
        }
        for(StaticDataTableExtension extension : extensions) {
            extension.setSymbol(type, symbol, name);
        }
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if ((getView() != null) && (getView().container != null)) {
            getView().container.setVisible(visible);
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void destroy() {
        if (this.gisStaticDataExtension != null) {
            this.gisStaticDataExtension.destroy();
        }
        ExtensionTool.destroy(extensions);
    }

    public void updateView() {
        final DefaultTableDataModel defaultTableDataModel;
        final List<RowData> list = new ArrayList<RowData>();
        final List<CellMetaDataEntry> metaDataEntries = new ArrayList<CellMetaDataEntry>();

        //add data from extensions
        if (this.gisStaticDataExtension != null) {
            this.gisStaticDataExtension.addData(list, metaDataEntries, 2);
        }
        for(StaticDataTableExtension extension : extensions) {
            extension.addData(list, metaDataEntries, 2);
        }

        defaultTableDataModel = DefaultTableDataModel.createWithRowData(list);
        //add cell metadata from extensions
        for(CellMetaDataEntry cmde : metaDataEntries) {
            defaultTableDataModel.setMetaData(cmde.getRow(), cmde.getColumn(), cmde.getCellMetaData());
        }

        getView().update(defaultTableDataModel);
    }
}
