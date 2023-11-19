/*
 * InvestorView.java
 *
 * Created on 19.03.13 13:38
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.google.gwt.user.client.ui.Panel;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Account;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Depot;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.DepotObjectBouquet;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Investor;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Portfolio;

/**
 * @author Markus Dick
 */
public class InvestorView extends AbstractOwnerView<Investor, DepotObjectBouquet<Investor, Portfolio, Account, Depot>> {
    public InvestorView() {
        super();
    }

    @Override
    protected Investor getOwner(DepotObjectBouquet<Investor, Portfolio, Account, Depot> bouquet) {
        return bouquet.getRootObject();
    }

    @Override
    protected void addStaticData(Investor investor) {
        doAddStaticData(investor, I18n.I.investorNumberAbbr());
    }

    @Override
    protected void addContactData(Investor inv) {
        final Panel p = addSection(SC_CONTACT, I18n.I.contact());

        addAddress(I18n.I.mainAddress(), p, inv.getAddress());
        addAddress(I18n.I.referenceAddress(1), p, inv.getReferenceAddress1());
        addAddress(I18n.I.referenceAddress(2), p, inv.getReferenceAddress2());

        if (!p.iterator().hasNext()) {
            addField(p, I18n.I.address(), I18n.I.noContactDataAvailable());
        }
    }

    @Override
    protected void addReportingDetails(Investor i) {
        Panel p = addSection(SC_REPORTING, I18n.I.reportingDetails());
        addField(p, I18n.I.analysisCurrency(), i.getAnalysisCurrency());
        addField(p, I18n.I.reportingProfile(), i.getReportingProfile());
        addField(p, I18n.I.reportingFrequency(), PmRenderers.REPORTING_FREQUENCY.render(i.getReportingFrequency()));
        addField(p, I18n.I.scheduledReportingActive(), i.isScheduledReportingActive());
        addField(p, I18n.I.scheduledReporting(), PmRenderers.DATE_TIME_STRING.render(i.getScheduledReportingLastNotification()));
    }
}
