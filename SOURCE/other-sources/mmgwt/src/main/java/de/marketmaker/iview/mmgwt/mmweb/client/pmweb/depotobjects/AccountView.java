/*
 * AccountView.java
 *
 * Created on 12.03.13 10:29
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.google.gwt.user.client.ui.Panel;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Account;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

/**
 * @author Markus Dick
 */
public class AccountView extends AbstractUserObjectView<Account, UserObjectDisplay.UserObjectPresenter<Account>> {
    public AccountView() {
        super();
    }

    @Override
    public void updateView(Account a) {
        super.updateView(a);
        addStaticData(a);
        addUserDefinedFields(a);
    }

    private void addStaticData(final Account a) {
        final Panel p = addSection(SC_STATIC, I18n.I.staticData());
        setSelectedSection(SC_STATIC);

        addField(p, I18n.I.pmInvestor(), a.getInvestorName());
        addField(p, I18n.I.account(), a.getName());
        addField(p, I18n.I.accountNo(), a.getAccountNumber());
        addField(p, I18n.I.currency(), a.getCurrency());
        addField(p, I18n.I.bankCodeAbbr(), a.getBankCode());

        if(a.getBankAddress() != null && !a.getBankAddress().isEmpty()) {
            addField(p, I18n.I.bank(), new LabelWithAddress(a.getBank(), a.getBankAddress()));
        }
        else {
            addField(p, I18n.I.bank(), a.getBank());
        }

        addField(p, "IBAN", a.getIban()); //$NON-NLS$
        addCreationDeactivationDates(p, a.getCreationDate(), a.getDeactivationDate());
        addField(p, I18n.I.overnightDepositAccount(), a.isOvernightDepositAccount());

        addField(p, I18n.I.interestRate(), Renderer.PERCENT23.render(a.getInterestRate()), PmRenderers.DI_NUMBER_PERCENT23, a.getInterestRateHistory());

        addField(p, I18n.I.interestDay(), a.getInterestDay());
        addField(p, I18n.I.interestRateMethod(), a.getInterestRateMethod());
        addField(p, I18n.I.excludeFromTaxAnalysis(), a.isExcludeFromTaxAnalysis());
        addMultilineField(p, I18n.I.comment(), a.getComment());
        addField(p,
                PmRenderers.DATA_STATUS_LABEL.render(I18n.I.dataStatusAccount(), a.getDataStatusAccountDate()),
                PmRenderers.DATA_STATUS.render(a.getDataStatusAccount()));
        addField(p,
                PmRenderers.DATA_STATUS_LABEL.render(I18n.I.dataStatusSpecialInvestments(),
                        a.getDataStatusSpecialInvestmentsDate()),
                PmRenderers.DATA_STATUS.render(a.getDataStatusSpecialInvestments()));

        addField(p, I18n.I.zone(), a.getZone());
    }
}
