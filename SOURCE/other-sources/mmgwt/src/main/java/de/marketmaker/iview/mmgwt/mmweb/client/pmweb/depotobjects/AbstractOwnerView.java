/*
 * AbstractOwnerView.java
 *
 * Created on 05.06.14 10:01
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Panel;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.AbstractOwner;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Address;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.OwnerPersonLink;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.List;

/**
 * @author Markus Dick
 */
public abstract class AbstractOwnerView<T extends AbstractOwner, H> extends AbstractUserObjectView<H, UserObjectDisplay.UserObjectPresenter<H>> {
    public AbstractOwnerView() {
        super();
    }

    @Override
    public void updateView(H h) {
        super.updateView(h);
        doUpdateView(getOwner(h));
    }

    protected abstract T getOwner(H h);

    protected void doUpdateView(T t) {
        addStaticData(t);
        addContactData(t);
        if(Selector.AS_ACTIVITIES.isAllowed()) {
            addLinkedPersons(t);
        }
        addReportingDetails(t);
        addTaxDetails(t);
        addUserDefinedFields(t);
    }

    protected void addStaticData(T t) {
        doAddStaticData(t, I18n.I.number());
    }

    protected void doAddStaticData(T t, final String numberLabel) {
        Panel p = addSection(SC_STATIC, I18n.I.staticData());
        setSelectedSection(SC_STATIC);

        addField(p, I18n.I.pmInvestor(), t.getName());
        final String investorFullName = PmRenderers.ADDRESS_FULL_NAME_WITH_SALUTATION.render(t.getAddress());
        addField(p, I18n.I.name(), investorFullName);
        addField(p, I18n.I.organization(), t.getAddress().getOrganization());
        addField(p, numberLabel, t.getNumber());
        addField(p, I18n.I.customerCategory(), t.getCustomerCategory());
        addCreationDeactivationDates(p, t.getCreationDate(), t.getDeactivationDate());
        addMultilineField(p, I18n.I.comment(), t.getComment());
        addField(p,
                PmRenderers.DATA_STATUS_LABEL.render(I18n.I.dataStatus(), t.getDataStatusDate()),
                PmRenderers.DATA_STATUS.render(t.getDataStatus()));

        addField(p, I18n.I.zone(), t.getZone());
    }

    protected void addLinkedPersons(T t) {
        final Panel p = addSection(SC_LINKED_PERSONS, I18n.I.linkedPersons());

        final List<OwnerPersonLink> linkedPersons = t.linkedPersons();

        if(linkedPersons == null || linkedPersons.isEmpty()) {
            addSubHeading(p, I18n.I.none());
            return;
        }

        for(final OwnerPersonLink l : t.linkedPersons()) {
            addSubHeading(p, l.getPerson().getName());
            addField(p, I18n.I.name(), PmRenderers.PERSON_FULL_NAME_WITH_SALUTATION.render(l.getPerson()));
            addField(p, I18n.I.type(), PmRenderers.OWNER_PERSON_LINK_TYPE_RENDERER.render(l.getType()));
            if(StringUtil.hasText(l.getComment())) {
                addMultilineField(p, I18n.I.comment(), l.getComment());
            }
        }
    }

    protected void addContactData(T t) {
        final Panel p = addSection(SC_CONTACT, I18n.I.contact());

        addAddress(I18n.I.mainAddress(), p, t.getAddress());

        if (!p.iterator().hasNext()) {
            addField(p, I18n.I.address(), I18n.I.noContactDataAvailable());
        }
    }

    protected void addAddress(String label, HasWidgets hw, Address address) {
        if (address == null || address.isEmpty()) {
            return;
        }

        addSubHeading(hw, label);

        final AddressWidget addressWidget = new AddressWidget();
        hw.add(addressWidget.asWidget());
        addressWidget.setValue(address);
    }

    protected void addReportingDetails(T t) {
        Panel p = addSection(SC_REPORTING, I18n.I.reportingDetails());
        addField(p, I18n.I.analysisCurrency(), t.getAnalysisCurrency());
    }

    protected void addTaxDetails(T t) {
        final Panel p = addSection(SC_TAX, I18n.I.taxDetails());
        addField(p, I18n.I.taxDomicile(), t.getTaxDomicile());
        addField(p, I18n.I.currencyOfTax(), t.getCurrencyOfTax());
        addField(p, I18n.I.married(), t.isMarried());
        addField(p, I18n.I.flatRateSavingsAllowance(), Renderer.PRICE23.render(t.getFlatRateSavingsAllowance()));
        addField(p, I18n.I.lossCarryforwardSpeculativeGains(), Renderer.PRICE23.render(t.getLossCarryforwardSpeculativeGains()));
        addField(p, I18n.I.lossCarryforwardEquities(), Renderer.PRICE23.render(t.getLossCarryforwardEquities()));
        addField(p, I18n.I.lossCarryforwardCapitalAssets(), Renderer.PRICE23.render(t.getLossCarryforwardCapitalAssets()));
        addField(p, I18n.I.taxRate(), Renderer.PERCENT23.render(t.getTaxRate()));
        addField(p, I18n.I.churchTaxRate(), Renderer.PERCENT23.render(t.getChurchTaxRate()));
        addField(p, I18n.I.churchTaxDeductionByBank(), t.isChurchTaxDeductionByBank());
        addField(p, I18n.I.churchTaxRateOfSpouse(), Renderer.PERCENT23.render(t.getChurchTaxRateOfSpouse()));
        addField(p, I18n.I.churchTaxDeductionByBankOfSpouse(), t.isChurchTaxDeductionByBankOfSpouse());
        addField(p, I18n.I.ownershipShareOfSpouse(), Renderer.PERCENT23.render(t.getOwnershipShareOfSpouse()));
    }
}
