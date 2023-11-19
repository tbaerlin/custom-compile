/*
 * DepotView.java
 *
 * Created on 18.03.13 10:13
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;


import com.google.gwt.user.client.ui.Panel;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Bank;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.CommissionScale;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Depot;

/**
 * @author Markus Dick
 */
public class DepotView extends AbstractUserObjectView<Depot, UserObjectDisplay.UserObjectPresenter<Depot>> {
    public DepotView() {
        super();
    }

    @Override
    public void updateView(Depot d) {
        super.updateView(d);

        addStaticData(d);
        addUserDefinedFields(d);
    }

    private void addStaticData(Depot d) {
        final Panel p = addSection(SC_STATIC, I18n.I.staticData());
        setSelectedSection(SC_STATIC);

        addField(p, I18n.I.pmInvestor(), d.getInvestorName());
        addField(p, I18n.I.pmDepot(), d.getName());
        addField(p, I18n.I.orderEntryDepotNo(), d.getDepotNumber());

        final Bank bank = d.getBank();
        if(bank != null && bank.getAddress() != null && !bank.getAddress().isEmpty()) {
            addField(p, I18n.I.bank(), new LabelWithAddress(d.getBankName(), bank.getAddress()));
        }
        else {
            addField(p, I18n.I.bank(), d.getBankName());
        }

        addField(p, I18n.I.currency(), d.getCurrency());
        addCreationDeactivationDates(p, d.getCreationDate(), d.getDeactivationDate());
        addField(p, I18n.I.performanceCalculationFrom(), PmRenderers.DATE_TIME_STRING.render(d.getPerformanceCalculationFromDate()));

        final CommissionScale cs = d.getCommissionScale();
        final String csName = cs != null ? cs.getName() : "";
        addField(p, I18n.I.commissionScale(), csName);

        final String ssaName = d.getStandardSettlementAccount() != null ? d.getStandardSettlementAccount().getName() : "";
        addField(p, I18n.I.standardSettlementAccount(), ssaName);
        addField(p, I18n.I.excludeFromTaxAnalysis(), d.isExcludeFromTaxAnalysis());

        addMultilineField(p, I18n.I.comment(), d.getComment());

        addField(p,
                PmRenderers.DATA_STATUS_LABEL.render(I18n.I.dataStatus(), d.getDataStatusDate()),
                PmRenderers.DATA_STATUS.render(d.getDataStatus()));

        addField(p, I18n.I.zone(), d.getZone());
    }
}
