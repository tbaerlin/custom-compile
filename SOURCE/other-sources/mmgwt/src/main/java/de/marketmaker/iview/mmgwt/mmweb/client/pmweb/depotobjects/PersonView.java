/*
 * PersonView.java
 *
 * Created on 17.03.14 10:26
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.google.gwt.user.client.ui.Panel;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.AbstractOwner;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.OwnerPersonLink;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Person;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.List;

/**
 * @author Markus Dick
 */
public class PersonView extends AbstractUserObjectView<Person, UserObjectDisplay.UserObjectPresenter<Person>> {
    public PersonView() {
        super();
    }

    @Override
    public void updateView(Person person) {
        super.updateView(person);

        addStaticData(person);
        addAddress(person);
        addLinkedInvestors(person);
        addLinkedProspects(person);
        addUserDefinedFields(person);
    }

    private void addStaticData(Person person) {
        final Panel p = addSection(SC_STATIC, I18n.I.staticData());
        setSelectedSection(SC_STATIC);

        addField(p, I18n.I.name(), PmRenderers.PERSON_FULL_NAME_WITH_SALUTATION.render(person));
        addField(p, I18n.I.serialNumber(), person.getSerialNumber());
        addField(p, I18n.I.incapacityToContract(), person.getIncapacityToContract());
        addField(p, I18n.I.identityCardType(), person.getIdentityCardType());
        addField(p, I18n.I.identityNumber(), person.getIdentityNumber());
        addField(p, I18n.I.identityCardIssuedBy(), person.getIdentityCardIssuedBy());
        addField(p, I18n.I.nationalStatus(), person.getNationalStatus());
        addField(p, I18n.I.placeOfBirth(), person.getPlaceOfBirth());
        addField(p, I18n.I.dateOfBirth(), PmRenderers.DATE_STRING.render(person.getDateOfBirth()));
        addField(p, I18n.I.maritalStatus(), PmRenderers.FAMILIENSTAND_RENDERER.render(person.getMaritalStatus()));
        addField(p, I18n.I.occupation(), person.getOccupation());
        addField(p, I18n.I.taxNumberAbbr(), person.getTaxIdentificationNumber());
        addField(p, I18n.I.comment(), person.getComment());

        addField(p, I18n.I.zone(), person.getZone());
    }

    private void addAddress(Person person) {
        final Panel p = addSection(SC_CONTACT, I18n.I.contact());
        setSelectedSection(SC_CONTACT);

        addField(p, I18n.I.address(), person.getAddress());
        addField(p, I18n.I.addressSupplement(), person.getAddressSupplement());
        addField(p, I18n.I.postCode(), person.getPostCode());
        addField(p, I18n.I.city(), person.getCity());
        addField(p, I18n.I.country(), person.getCountry());
    }

    private void addLinkedInvestors(Person person) {
        final Panel p = addSection(SC_LINKED_INVESTORS, I18n.I.linkedInvestors());
        addLinkedOwners(p, person.getLinkedInvestors());
    }

    private void addLinkedProspects(Person person) {
        final Panel p = addSection(SC_LINKED_PROSPECTS, I18n.I.linkedProspects());
        addLinkedOwners(p, person.getLinkedProspects());
    }

    private void addLinkedOwners(Panel p, List<OwnerPersonLink> linkedOwners) {
        if(linkedOwners == null || linkedOwners.isEmpty()) {
            addSubHeading(p, I18n.I.none());
            return;
        }

        for(final OwnerPersonLink l : linkedOwners) {
            if(l != null) {
                final AbstractOwner owner = l.getOwner();

                final String ownerName = Renderer.STRING_DOUBLE_DASH.render(
                        PmRenderers.ADDRESS_FULL_NAME_WITH_SALUTATION.render(owner.getAddress()));

                addOwnerLinkEntry(p, l, ownerName);
            }
        }
    }

    private void addOwnerLinkEntry(Panel p, OwnerPersonLink l, String ownerName) {
        addSubHeading(p, l.getOwner().getName());
        addField(p, I18n.I.name(), ownerName);
        addField(p, I18n.I.type(), PmRenderers.OWNER_PERSON_LINK_TYPE_RENDERER.render(l.getType()));
        addField(p, I18n.I.contractNumber(), l.getContractNumber());
        addField(p, I18n.I.validFrom(), PmRenderers.DATE_STRING.render(l.getValidFrom()));
        addField(p, I18n.I.validTo(), PmRenderers.DATE_STRING.render(l.getValidTo()));
        if(StringUtil.hasText(l.getComment())) {
            addMultilineField(p, I18n.I.comment(), l.getComment());
        }
    }
}
