/*
 * DividendListSnippet.java
 *
 * Created on 09.10.2014 17:23
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.dmxml.CorporateAction;
import de.marketmaker.iview.dmxml.MSCCorporateActions;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateTimeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.DATE;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.DEFAULT;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.PERCENT;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.PRICE;

/**
 * List view of a stock's dividends
 *
 * @author jkirchg
 */
public class DividendListSnippet extends AbstractSnippet<DividendListSnippet, SnippetTableView<DividendListSnippet>> implements SymbolSnippet {

    private final DmxmlContext.Block<MSCCorporateActions> blockCorporateActions;

    public static class Class extends SnippetClass {
        public Class() {
            super("DividendList", I18n.I.dividendList()); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new DividendListSnippet(context, config);
        }
    }

    private DividendListSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        this.blockCorporateActions = createBlock("MSC_CorporateActions"); // $NON-NLS$
        this.blockCorporateActions.setParameter("period", config.getString("period", "P10Y")); // $NON-NLS$

        this.setView(new SnippetTableView<DividendListSnippet>(this,
                new DefaultTableColumnModel(new TableColumn[]{
                        new TableColumn(I18n.I.paymentDate(), -1f, DATE, "date").alignRight(),  // $NON-NLS$
                        new TableColumn(I18n.I.dividendYield(), -1f, PERCENT),
                        new TableColumn(I18n.I.dividendAndStock(), -1f, PRICE),
                        new TableColumn(I18n.I.currency(), -1f, DEFAULT)
                }, true)));
    }

    @Override
    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.blockCorporateActions.setParameter("symbol", symbol); // $NON-NLS$
    }

    @Override
    public void destroy() {
        destroyBlock(this.blockCorporateActions);
    }

    @Override
    public void updateView() {
        if (!this.blockCorporateActions.isResponseOk() || this.blockCorporateActions.getResult() == null || this.blockCorporateActions.getResult().getCorporateAction() == null) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }

        final List<Object[]> list = new ArrayList<Object[]>();
        final List<CorporateAction> corporateActions = this.blockCorporateActions.getResult().getCorporateAction();
        for (CorporateAction corporateAction : corporateActions) {
            if ("DIVIDEND".equals(corporateAction.getType())) { // $NON-NLS$
                list.add(new Object[]{
                        DateTimeUtil.DATE_FORMAT_DMY.format(Formatter.FORMAT_ISO_DATE.parse(corporateAction.getDate())),
                        corporateAction.getYield(),
                        corporateAction.getValue(),
                        corporateAction.getCurrency()
                });
            }
        }
        Collections.reverse(list);

        final TableDataModel tdm = DefaultTableDataModel.create(list);
        getView().update(tdm);
    }

}
