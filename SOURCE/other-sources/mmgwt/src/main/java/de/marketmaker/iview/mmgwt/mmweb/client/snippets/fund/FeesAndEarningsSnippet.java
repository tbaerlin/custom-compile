/*
 * FeesAndEarningsSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.fund;

import de.marketmaker.iview.dmxml.FNDRatioData;
import de.marketmaker.iview.dmxml.FNDStaticData;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlDataUtil;

import java.util.ArrayList;
import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer.PERCENT;
import static de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer.PRICE;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FeesAndEarningsSnippet extends
        AbstractSnippet<FeesAndEarningsSnippet, SnippetTableView<FeesAndEarningsSnippet>> implements
        SymbolSnippet {
    public static class Class extends SnippetClass {
        public Class() {
            super("FeesAndEarnings", I18n.I.feesAndEarnings()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new FeesAndEarningsSnippet(context, config);
        }
    }

    private final DmxmlContext.Block<FNDStaticData> block;

    private final DmxmlContext.Block<FNDRatioData> blockRatiodata;

    private FeesAndEarningsSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        this.setView(new SnippetTableView<>(this,
                new DefaultTableColumnModel(new TableColumn[]{
                        new TableColumn(I18n.I.type(), 0.45f, new TableCellRenderers.StringRenderer("--", "mm-snippetTable-label")),  // $NON-NLS-0$ $NON-NLS-1$
                        new TableColumn(I18n.I.value(), 0.55f, TableCellRenderers.DEFAULT_RIGHT) 
                })));

        this.block = createBlock("FND_StaticData"); // $NON-NLS-0$
        this.blockRatiodata = createBlock("FND_RatioData"); // $NON-NLS-0$
        setSymbol(InstrumentTypeEnum.FND, config.getString("symbol", null), null); // $NON-NLS-0$
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.block.setEnabled(symbol != null);
        this.block.setParameter("symbol", symbol); // $NON-NLS-0$
        this.blockRatiodata.setEnabled(symbol != null);
        this.blockRatiodata.setParameter("symbol", symbol); // $NON-NLS-0$
    }

    public void destroy() {
        destroyBlock(this.block);
        destroyBlock(this.blockRatiodata);
    }

    public void updateView() {
        final TableDataModel tdm;
        if (this.block.isResponseOk()) {
            final FNDStaticData data = this.block.getResult();
            final List<Object[]> list = new ArrayList<>();
            // fee
            list.add(new Object[]{I18n.I.issueSurcharge(), PERCENT.render(data.getIssueSurcharge())}); 
            if (data.getRedemptionFee() != null) {
                list.add(new Object[]{I18n.I.redemptionFee(), PERCENT.render(data.getRedemptionFee())}); 
            }
            list.add(new Object[]{I18n.I.managementFee(), PERCENT.render(data.getManagementFee())}); 
            list.add(new Object[]{I18n.I.accountFee(), PERCENT.render(data.getAccountFee())});
            list.add(new Object[]{I18n.I.ongoingCharges(), PERCENT.render(data.getOngoingCharge())});
            if (data.getTer() != null) {
                list.add(new Object[]{I18n.I.totalExpenseRatio(), PERCENT.render(data.getTer())});
            }

            final String currency = data.getCurrency() != null ? " " + data.getCurrency() : ""; // $NON-NLS-0$ $NON-NLS-1$
            // earning
            final DmxmlDataUtil.FundDistributionStrategy distributionStrategy = DmxmlDataUtil.getFundDistributionStrategy(data.getDistributionStrategy());
            if (distributionStrategy == DmxmlDataUtil.FundDistributionStrategy.DISTRIBUTING) {
                list.add(new Object[]{I18n.I.distributionStrategy(),
                        data.getDistributionStrategy() + " / " + // $NON-NLS-0$
                                (data.getDistributionFrequency() != null ? data.getDistributionFrequency() : "--")}); // $NON-NLS-0$

                if (data.getLastDistribution() == null) {
                    list.add(new Object[]{I18n.I.lastDistributionDate(), "--"});  // $NON-NLS-0$
                }
                else {
                    list.add(new Object[]{I18n.I.lastDistributionDate() + " (" + data.getLastDistributionDate() + ")", PRICE.render(data.getLastDistribution()) + currency});  // $NON-NLS-0$ $NON-NLS-1$
                }
            }
            else if (distributionStrategy == DmxmlDataUtil.FundDistributionStrategy.RETAINING) {
                list.add(new Object[]{I18n.I.lastRetainingDistributionDate(), data.getLastDistributionDate()}); 
            }

            if (this.blockRatiodata.isResponseOk()) {
                final String interimProfit = this.blockRatiodata.getResult().getInterimprofit();
                final String sInterimProfit = PRICE.render(interimProfit) + currency;
                list.add(new Object[]{I18n.I.interimProfit(), interimProfit != null ? sInterimProfit : "--"});  // $NON-NLS-0$
            }

            tdm = DefaultTableDataModel.create(list);
        }
        else {
            tdm = DefaultTableDataModel.NULL;
        }
        getView().update(tdm);
    }
}
