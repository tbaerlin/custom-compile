/*
 * GisStaticDataExtension.java
 *
 * Created on 23.10.2012 09:27:09
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.extensions;

import de.marketmaker.iview.dmxml.GISStaticData;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.table.CellMetaData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Markus Dick
 */
class GisStaticDataExtension extends AbstractExtension implements StaticDataTableExtension {
    private static final String EXTENSION_NAME = "GisStaticDataExtension"; //$NON-NLS$

    public static class Class extends ExtensionClass {
        public Class() {
            super(EXTENSION_NAME);
        }

        @Override
        public boolean isImplementationOf(java.lang.Class clazz) {
            return StaticDataTableExtension.class.equals(clazz);
        }

        @Override
        public Extension newExtension(DmxmlContext context, SnippetConfiguration config) {
            return new GisStaticDataExtension(context, config);
        }
    }

    private static final String LINE_BREAK = "<br/>"; //$NON-NLS$
    private static final RowData EMPTY_TWO_COLS_ROW = new RowData("", ""); //$NON-NLS$

    private final DmxmlContext.Block<GISStaticData> block;
    private InstrumentTypeEnum instrumentType;

    public GisStaticDataExtension(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.block = createBlock("GIS_StaticData"); //$NON-NLS$
        this.block.disableRefreshOnRequest();
    }

    public boolean isAllowed() {
        return Customer.INSTANCE.isDzWgz() && Selector.DZ_KAPITALMARKT.isAllowed();
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name) {
        this.instrumentType = type;
        this.block.setEnabled(symbol != null);
        this.block.setParameter("symbol", symbol); // $NON-NLS$
    }

    @Override
    public void addData(List<RowData> list, List<CellMetaDataEntry> metaDataEntries, int columnCount) {
        if ((InstrumentTypeEnum.BND == this.instrumentType || InstrumentTypeEnum.CER == this.instrumentType)) {
            switch (columnCount) {
                case 2:
                    addTwoColsData(list, metaDataEntries);
                    break;
                case 5:
                    addFiveColsData(list, metaDataEntries);
                    break;
                default:
            }
        }
    }

    public void addTwoColsData(List<RowData> list, List<CellMetaDataEntry> metaDataEntries) {
        if(!this.block.isResponseOk()) return;

        final GISStaticData data = this.block.getResult();
        if (data == null) {
            return;
        }
        //The reference date is not optional.
        //If there is any data, then there must also be a reference date!
        if (StringUtil.hasText(data.getReferencedate())) {
            list.add(new RowData(I18n.I.gisStaticDataHeadline(data.getReferencedate()), "")); //$NON-NLS$
            metaDataEntries.add(new CellMetaDataEntry(list.size() - 1, 0,
                    new CellMetaData().withCellClass("mm-emphasized").withColSpan(2))); //$NON-NLS$
        }

        if (StringUtil.hasText(data.getRendite())) {
            list.add(createRendite(data));
        }
        if (StringUtil.hasText(data.getCoupon())) {
            list.add(createCoupon(data));
        }
        if (StringUtil.hasText(data.getExpiration())) {
            list.add(createExpiration(data));
        }
        if (StringUtil.hasText(data.getBonibrief())) {
            list.add(createBonibrief(data));
        }
        if (StringUtil.hasText(data.getBonifikation())) {
            list.add(createBonifikation(data));
        }
        if (StringUtil.hasText(data.getRisikoklasse())) {
            list.add(createRisikoklasse(data));
        }
        if (hasHinweis(data)) {
            list.add(createHinweis(data));
        }
        if (StringUtil.hasText(data.getTopargument())) {
            list.add(createTopArgument(data));
        }
    }

    public void addFiveColsData(List<RowData> list, List<CellMetaDataEntry> metaDataEntries) {
        if(!this.block.isResponseOk()) return;

        final GISStaticData data = this.block.getResult();

        if (data == null) {
            return;
        }
        ArrayList<RowData> numericRows = new ArrayList<RowData>();
        ArrayList<RowData> stringRows = new ArrayList<RowData>();

        if (StringUtil.hasText(data.getRendite())) {
            numericRows.add(createRendite(data));
        }
        if (StringUtil.hasText(data.getCoupon())) {
            numericRows.add(createCoupon(data));
        }
        if (StringUtil.hasText(data.getExpiration())) {
            numericRows.add(createExpiration(data));
        }
        if (StringUtil.hasText(data.getBonibrief())) {
            numericRows.add(createBonibrief(data));
        }
        if (StringUtil.hasText(data.getBonifikation())) {
            numericRows.add(createBonifikation(data));
        }
        if (StringUtil.hasText(data.getRisikoklasse())) {
            numericRows.add(createRisikoklasse(data));
        }
        if (hasHinweis(data)) {
            stringRows.add(createHinweis(data));
        }
        if (StringUtil.hasText(data.getTopargument())) {
            stringRows.add(createTopArgument(data));
        }

        //The reference date is not optional.
        if (numericRows.size() > 0 || stringRows.size() > 0) {
            if (StringUtil.hasText(data.getReferencedate())) {
                list.add(new RowData(I18n.I.gisStaticDataHeadline(data.getReferencedate()), "", "", "", "")); //$NON-NLS$
                metaDataEntries.add(new CellMetaDataEntry(list.size() - 1, 0,
                        new CellMetaData().withCellClass("mm-emphasized").withColSpan(5))); //$NON-NLS$
            }

            //display two label value pair columns for numeric values
            if ((numericRows.size() > 0) && (numericRows.size() % 2 != 0)) {
                numericRows.add(EMPTY_TWO_COLS_ROW);
            }
            for (int i = 0; i < numericRows.size(); i += 2) {
                list.add(RowData.combineRowData(numericRows.get(i), numericRows.get(i + 1)));
            }

            //display one label value pair columns for string values
            for (RowData stringRow : stringRows) {
                list.add(RowData.combineRowData(stringRow, EMPTY_TWO_COLS_ROW));
                metaDataEntries.add(new CellMetaDataEntry(list.size() - 1, 1,
                        new CellMetaData().withColSpan(4)));
            }
        }
    }

    private boolean hasHinweis(GISStaticData data) {
        return StringUtil.hasText(data.getHinweise()) || StringUtil.hasText(data.getSonderheit());
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    private RowData createExpiration(GISStaticData data) {
        return new RowData(I18n.I.gisExpiration(), data.getExpiration());
    }

    private RowData createCoupon(GISStaticData data) {
        return new RowData(I18n.I.coupon(), Renderer.PERCENT.render(data.getCoupon()));
    }

    private RowData createRendite(GISStaticData data) {
        return new RowData(I18n.I.gisRendite(), Renderer.PERCENT.render(data.getRendite()));
    }

    private RowData createTopArgument(GISStaticData data) {
        return new RowData(I18n.I.gisTopargument(), data.getTopargument());
    }

    private RowData createRisikoklasse(GISStaticData data) {
        return new RowData(I18n.I.gisRisikoklasse(), data.getRisikoklasse());
    }

    private RowData createBonifikation(GISStaticData data) {
        final String bonifikation = data.getBonifikation()
                .replaceFirst("1", I18n.I.gisBonifikation1()).replaceFirst("2", I18n.I.gisBonifikation2()); // $NON-NLS$
        return new RowData(I18n.I.gisBonifikation(), bonifikation);
    }

    private RowData createBonibrief(GISStaticData data) {
        return new RowData(I18n.I.gisBonibrief(), Renderer.PRICE23.render(data.getBonibrief()));
    }

    private RowData createHinweis(GISStaticData data) {
        String value = "";
        final String hinweise = data.getHinweise();
        final String sonderheit = data.getSonderheit();

        if(StringUtil.hasText(hinweise)) {
            value = hinweise;
        }

        if(StringUtil.hasText(sonderheit)) {
            if(StringUtil.hasText(value)) {
                value += LINE_BREAK + sonderheit;
            }
            else {
                value = sonderheit;
            }
        }

        return new RowData(I18n.I.gisHinweis(), value);
    }
}