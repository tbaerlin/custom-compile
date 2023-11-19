/*
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.user.client.ui.FlowPanel;
import de.marketmaker.iview.dmxml.MSCRegulatoryReportingRecord;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModelBuilder;

public class RegulatoryReportingSnippetView extends SnippetView<RegulatoryReportingSnippet> {

    private SnippetTableWidget mifidRecord;

    private TableColumnModel mifidModelStatic;

    private SnippetTableWidget tmRecord;

    private TableColumnModel tmModelStatic;

    private SnippetTableWidget ntmRecord;

    private TableColumnModel ntmModelStatic;

    private SnippetTableWidget cfRecord;

    private TableColumnModel cfModelStatic;

    private SnippetTableWidget cspRecord;

    private TableColumnModel cspModelStatic;

    public RegulatoryReportingSnippetView(RegulatoryReportingSnippet snippet) {
        super(snippet);
        setTitle(I18n.I.regulatoryReporting());

        this.mifidModelStatic = createTableColumnModel();
        this.tmModelStatic = createTableColumnModel();
        this.ntmModelStatic = createTableColumnModel();
        this.cfModelStatic = createTableColumnModel();
        this.cspModelStatic = createTableColumnModel();
    }

    private TableColumnModel createTableColumnModel() {
        return new DefaultTableColumnModel(new TableColumn[]{
            new TableColumn(I18n.I.type(), 0.3f, TableCellRenderers.DEFAULT_LABEL),
            new TableColumn(I18n.I.value(), 0.7f)
        });
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();

        this.mifidRecord = SnippetTableWidget.create(this.mifidModelStatic);
        this.tmRecord = SnippetTableWidget.create(this.tmModelStatic);
        this.ntmRecord = SnippetTableWidget.create(this.ntmModelStatic);
        this.cfRecord = SnippetTableWidget.create(this.cfModelStatic);
        this.cspRecord = SnippetTableWidget.create(this.cspModelStatic);

        final FlowPanel panel = new FlowPanel();

        panel.add(this.mifidRecord);
        panel.add(this.tmRecord);
        panel.add(this.ntmRecord);
        panel.add(this.cfRecord);
        panel.add(this.cspRecord);

        this.container.setContentWidget(panel);

        update(null);
    }

    public void update(MSCRegulatoryReportingRecord record) {
        if (record == null) {
            this.mifidRecord.setMessage(I18n.I.noDataAvailable(), false);
            this.tmRecord.setMessage(I18n.I.noDataAvailable(), false);
            this.ntmRecord.setMessage(I18n.I.noDataAvailable(), false);
            this.cfRecord.setMessage(I18n.I.noDataAvailable(), false);
            this.cspRecord.setMessage(I18n.I.noDataAvailable(), false);
            return;
        }

        // builder constructor call: update row counts

        final TableDataModelBuilder mifidBuilder = new TableDataModelBuilder(8, 2);
        mifidBuilder.addRow(I18n.I.regulatoryMifidCapitalClass(), record.getMifidCapitalClass());
        mifidBuilder.addRow(I18n.I.regulatoryMifidAssetClass(), record.getMifidAssetClass());
        mifidBuilder.addRow(I18n.I.regulatoryMifidAssetClassAdition(), record.getMifidAssetClassAddition());
        mifidBuilder.addRow(I18n.I.regulatoryMifidClFiRts28(), record.getMifidClFiRts28());
        mifidBuilder.addRow(I18n.I.regulatoryMifidLeverageProductIdentifier(), record.getMifidLeverageProductIdentifier());
        mifidBuilder.addRow(I18n.I.regulatoryMifidTickSize(), record.getMifidTickSize());
        mifidBuilder.addRow(I18n.I.regulatoryMifidProductCategory(), record.getMifidProductCategory());
        mifidBuilder.addRow(I18n.I.regulatoryMifidProductApprovalProcessIdentifier(), record.getMifidProductApprovalProcessIdentifier());
        this.mifidRecord.updateData(mifidBuilder.getResult());

        final TableDataModelBuilder tmBuilder = new TableDataModelBuilder(12, 2);
        tmBuilder.addRow(I18n.I.regulatoryTmSourceOfTargetMarketData(), record.getTmSourceOfTargetMarketData());
        tmBuilder.addRow(I18n.I.regulatoryTmCustomerCatagory(), record.getTmCustomerCategory());
        tmBuilder.addRow(I18n.I.regulatoryTmInvestmentObjectives(), record.getTmInvestmentObjectives());
        tmBuilder.addRow(I18n.I.regulatoryTmInvestmentHorizon(), record.getTmInvestmentHorizon());
        tmBuilder.addRow(I18n.I.regulatoryTmRiskIndicator(), record.getTmRiskIndicator());
        tmBuilder.addRow(I18n.I.regulatoryTmCalculationMethodOfRiskIndicator(), record.getTmCalculationMethodOfRiskIndicator());
        tmBuilder.addRow(I18n.I.regulatoryTmRiskProfile(), record.getTmRiskProfile());
        tmBuilder.addRow(I18n.I.regulatoryTmFinancialLossBearingCapacity(), record.getTmFinancialLossBearingCapacity());
        tmBuilder.addRow(I18n.I.regulatoryTmKnowledgeAndExperience(), record.getTmKnowledgeAndExperience());
        tmBuilder.addRow(I18n.I.regulatoryTmDistributionStrategy(), record.getTmDistributionStrategy());
        tmBuilder.addRow(I18n.I.regulatoryTmSpecialRequirements(), record.getTmSpecialRequirements());
        tmBuilder.addRow(I18n.I.regulatoryTmAddendumToTheSpecialRequirements(), record.getTmAddendumToTheSpecialRequirements());
        this.tmRecord.updateData(tmBuilder.getResult());

        final TableDataModelBuilder ntmBuilder = new TableDataModelBuilder(9, 2);
        ntmBuilder.addRow(I18n.I.regulatoryNtmCustomerCategory(), record.getNtmCustomerCategory());
        ntmBuilder.addRow(I18n.I.regulatoryNtmInvestmentObjectives(), record.getNtmInvestmentObjectives());
        ntmBuilder.addRow(I18n.I.regulatoryNtmInvestmentHorizon(), record.getNtmInvestmentHorizon());
        ntmBuilder.addRow(I18n.I.regulatoryNtmRiskIndicator(), record.getNtmRiskIndicator());
        ntmBuilder.addRow(I18n.I.regulatoryNtmRiskAndYieldProfile(), record.getNtmRiskAndYieldProfile());
        ntmBuilder.addRow(I18n.I.regulatoryNtmFinancialLossBearingCapacity(), record.getNtmFinancialLossBearingCapacity());
        ntmBuilder.addRow(I18n.I.regulatoryNtmKnowledgeAndExperience(), record.getNtmKnowledgeAndExperience());
        ntmBuilder.addRow(I18n.I.regulatoryNtmDistributionStrategy(), record.getNtmDistributionStrategy());
        ntmBuilder.addRow(I18n.I.regulatoryNtmSpecialRequirements(), record.getNtmSpecialRequirements());
        this.ntmRecord.updateData(ntmBuilder.getResult());

        final TableDataModelBuilder cfBuilder = new TableDataModelBuilder(24, 2);
        cfBuilder.addRow(I18n.I.regulatoryCfPerformanceFeeIdentifier(), record.getCfPerformanceFeeIdentifier());
        cfBuilder.addRow(I18n.I.regulatoryCfSwingPricingIdentifier(), record.getCfSwingPricingIdentifier());
        cfBuilder.addRow(I18n.I.regulatoryCfRunningFundCostsEst(), record.getCfRunningFundCostsEst());
        cfBuilder.addRow(I18n.I.regulatoryCfRunningFundCostsEstDate(), record.getCfRunningFundCostsEstDate());
        cfBuilder.addRow(I18n.I.regulatoryCfTransactionCostsFundsEst(), record.getCfTransactionCostsFundsEst());
        cfBuilder.addRow(I18n.I.regulatoryCfTransactionCostsFundsEstDate(), record.getCfTransactionCostsFundsEstDate());
        cfBuilder.addRow(I18n.I.regulatoryCfEventRelatedCostsFundsEst(), record.getCfEventRelatedCostsFundsEst());
        cfBuilder.addRow(I18n.I.regulatoryCfEventRelatedCostsFundsEstDate(), record.getCfEventRelatedCostsFundsEstDate());
        cfBuilder.addRow(I18n.I.regulatoryCfActualRedemptionCostsFund(), record.getCfActualRedemptionCostsFund());
        cfBuilder.addRow(I18n.I.regulatoryCfRActualRedemptionCostsFundDate(), record.getCfActualRedemptionCostsFundDate());
        cfBuilder.addRow(I18n.I.regulatoryCfMinimumBackEndLoad(), record.getCfMinimumBackEndLoad());
        cfBuilder.addRow(I18n.I.regulatoryCfMinimumBackEndLoadCurrency(), record.getCfMinimumBackEndLoadCurrency());
        cfBuilder.addRow(I18n.I.regulatoryCfMinimumBackEndLoadPercentSign(), record.getCfMinimumBackEndLoadPercentSign());
        cfBuilder.addRow(I18n.I.regulatoryCfMinimumBackEndLoadReferenceValue(), record.getCfMinimumBackEndLoadReferenceValue());
        cfBuilder.addRow(I18n.I.regulatoryCfMaximumBackEndLoad(), record.getCfMaximumBackEndLoad());
        cfBuilder.addRow(I18n.I.regulatoryCfMaximumBackEndLoadCurrency(), record.getCfMaximumBackEndLoadCurrency());
        cfBuilder.addRow(I18n.I.regulatoryCfMaximumBackEndLoadPercentSign(), record.getCfMaximumBackEndLoadPercentSign());
        cfBuilder.addRow(I18n.I.regulatoryCfMaximumBackEndLoadReferenceValue(), record.getCfMaximumBackEndLoadReferenceValue());
        cfBuilder.addRow(I18n.I.regulatoryCfTotalFundCostsDateFrom(), record.getCfTotalFundCostsDateFrom());
        cfBuilder.addRow(I18n.I.regulatoryCfTotalFundCostsDateTo(), record.getCfTotalFundCostsDateTo());
        cfBuilder.addRow(I18n.I.regulatoryCfTotalFundCostsTransaction(), record.getCfTotalFundCostsTransaction());
        cfBuilder.addRow(I18n.I.regulatoryCfTotalFundCostsRunning(), record.getCfTotalFundCostsRunning());
        cfBuilder.addRow(I18n.I.regulatoryCfTotalFundCostsEventRelated(), record.getCfTotalFundCostsEventRelated());
        cfBuilder.addRow(I18n.I.regulatoryCfTotalFundCostsCorrectionID(), record.getCfTotalFundCostsCorrectionID());
        this.cfRecord.updateData(cfBuilder.getResult());

        final TableDataModelBuilder cspBuilder = new TableDataModelBuilder(21, 2);
        cspBuilder.addRow(I18n.I.regulatoryCspFairValueInstrument(), record.getCspFairValueInstrument());
        cspBuilder.addRow(I18n.I.regulatoryCspInstrumentWithRunningCosts(), record.getCspInstrumentWithRunningCosts());
        cspBuilder.addRow(I18n.I.regulatoryCspEstRunningCostsPrFv(), record.getCspEstRunningCostsPrFv());
        cspBuilder.addRow(I18n.I.regulatoryCspEstRunningCostsPrFvCurrency(), record.getCspEstRunningCostsPrFvCurrency());
        cspBuilder.addRow(I18n.I.regulatoryCspEstRunningCostsPrFvPercentSign(), record.getCspEstRunningCostsPrFvPercentSign());
        cspBuilder.addRow(I18n.I.regulatoryCspEstRunningCostsPrFvDate(), record.getCspEstRunningCostsPrFvDate());
        cspBuilder.addRow(I18n.I.regulatoryCspEntryCostsPrFv(), record.getCspEntryCostsPrFv());
        cspBuilder.addRow(I18n.I.regulatoryCspEntryCostsPrFvCurrency(), record.getCspEntryCostsPrFvCurrency());
        cspBuilder.addRow(I18n.I.regulatoryCspEntryCostsPrFvPercentSign(), record.getCspEntryCostsPrFvPercentSign());
        cspBuilder.addRow(I18n.I.regulatoryCspEntryCostsPrFvTime(), record.getCspEntryCostsPrFvTime());
        cspBuilder.addRow(I18n.I.regulatoryCspEntryCostsPrFvCorrectionId(), record.getCspEntryCostsPrFvCorrectionId());
        cspBuilder.addRow(I18n.I.regulatoryCspExitCostsPrFv(), record.getCspExitCostsPrFv());
        cspBuilder.addRow(I18n.I.regulatoryCspExitCostsPrFvCurrency(), record.getCspExitCostsPrFvCurrency());
        cspBuilder.addRow(I18n.I.regulatoryCspExitCostsPrFvPercentSign(), record.getCspExitCostsPrFvPercentSign());
        cspBuilder.addRow(I18n.I.regulatoryCspExitCostsPrFvTime(), record.getCspExitCostsPrFvTime());
        cspBuilder.addRow(I18n.I.regulatoryCspExitCostsPrFvCorrectionId(), record.getCspExitCostsPrFvCorrectionId());
        cspBuilder.addRow(I18n.I.regulatoryCspRunningIncrementalCostsPrFv(), record.getCspRunningIncrementalCostsPrFv());
        cspBuilder.addRow(I18n.I.regulatoryCspRunningIncrementalCostsPrFvCurrency(), record.getCspRunningIncrementalCostsPrFvCurrency());
        cspBuilder.addRow(I18n.I.regulatoryCspRunningIncrementalCostsPrFvPercentSign(), record.getCspRunningIncrementalCostsPrFvPercentSign());
        cspBuilder.addRow(I18n.I.regulatoryCspRunningIncrementalCostsPrFvDate(), record.getCspRunningIncrementalCostsPrFvDate());
        cspBuilder.addRow(I18n.I.regulatoryCspRunningIncrementalCostsPrFvCorrectionId(), record.getCspRunningIncrementalCostsPrFvCorrectionId());
        this.cspRecord.updateData(cspBuilder.getResult());
    }
}
