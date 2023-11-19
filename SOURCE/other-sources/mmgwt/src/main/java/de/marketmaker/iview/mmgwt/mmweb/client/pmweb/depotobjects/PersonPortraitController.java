/*
 * PersonPortraitController.java
 *
 * Created on 17.03.14 11:17
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryContextProducer;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebModule;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.AbstractOwner;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.OwnerPersonLink;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Person;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.UserDefinedFields;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.history.PmItemListContext;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.Formula;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;
import de.marketmaker.iview.pmxml.MMClassIndex;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.UMRightBody;
import de.marketmaker.iview.pmxml.WorkspaceSheetDesc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Markus Dick
 */
public class PersonPortraitController extends AbstractDepotObjectPortraitController<Person> {
    public static final String ICON_KEY = "pm-investor-person";  // $NON-NLS$

    private BlockAndTalker<Person.PersonTalker, Person, Person> batPerson;
    private BlockAndTalker<UserDefinedFields.Talker, List<UserDefinedFields>, UserDefinedFields> batUserDefinedFields;
    private Person person;

    public PersonPortraitController() {
        super(MMClassIndex.CI_T_PERSON, MMClassIndex.CI_T_PERSON, UMRightBody.UMRB_EDIT_ACTIVITY_INSTANCE);
    }

    @Override
    public BlockAndTalker[] initBats() {
        batPerson = new BlockAndTalker<>(this.context, new Person.PersonTalker());

        batUserDefinedFields = new BlockAndTalker<>(
                this.context,
                new UserDefinedFields.Talker(
                        Formula.create("Person").withUserFieldCategories( // $NON-NLS$
                                PmWebSupport.getInstance().getUserFieldDecls(ShellMMType.ST_PERSON)
                        )
                )
        );

        return new BlockAndTalker[]{
                this.batPerson,
                this.batUserDefinedFields
        };
    }

    private HistoryToken newPersonToken() {
        final HistoryToken.Builder builder = HistoryToken.builder(PmWebModule.HISTORY_TOKEN_PERSON)
                .with(OBJECTID_KEY, this.person.getId());
        return builder.build();
    }

    protected NavItemSpec initNavItems(List<WorkspaceSheetDesc> sheets) {
        final NavItemSpec linked;
        final UserObjectController<Person, PersonPortraitController, UserObjectDisplay.UserObjectPresenter<Person>>
                personUOC = new UserObjectController<>(this, new PersonView());
        final HistoryToken defaultToken = newPersonToken();

        final NavItemSpec root = new NavItemSpec("root", "root"); // $NON-NLS$
        root.addChild(
                setFallback(new NavItemSpec(AbstractUserObjectView.SC_STATIC, I18n.I.staticData(), defaultToken, personUOC)).withOpenWithSelection().withClosingSiblings().addChildren(
                        new NavItemSpec(AbstractUserObjectView.SC_CONTACT, I18n.I.contact(), defaultToken, personUOC),
                        new NavItemSpec(AbstractUserObjectView.SC_LINKED_INVESTORS, I18n.I.linkedInvestors(), defaultToken, personUOC),
                        new NavItemSpec(AbstractUserObjectView.SC_LINKED_PROSPECTS, I18n.I.linkedProspects(), defaultToken, personUOC),
                        new NavItemSpec(AbstractUserObjectView.SC_USER_DEFINED_FIELDS, I18n.I.userDefinedFields(), defaultToken, personUOC)
                )
        );

        root.addChild(linked = new NavItemSpec("V", I18n.I.linkedObjects()).withClosingSiblings()); // $NON-NLS$

        if(PrivacyMode.isActive()) {
            applyPrivacyMode(this.person.getLinkedInvestors());
            applyPrivacyMode(this.person.getLinkedProspects());
        }
        addOwnerPersonLinks(linked, this.person.getLinkedInvestors(), PmWebModule.HISTORY_TOKEN_INVESTOR_PROFILE,
                "pm-investor", I18n.I.pmInvestor());  // $NON-NLS$
        addOwnerPersonLinks(linked, this.person.getLinkedProspects(), PmWebModule.HISTORY_TOKEN_PROSPECT,
                "pm-investor-prospect", I18n.I.prospect());  // $NON-NLS$

        addActivityNode(root);

        return root;
    }

    private void applyPrivacyMode(List<OwnerPersonLink> linkedOwners) {
        for (OwnerPersonLink personLink : new ArrayList<>(linkedOwners)) {
            if(!PrivacyMode.isObjectIdAllowed(personLink.getOwner().getId())) {
                linkedOwners.remove(personLink);
            }
        }
    }

    private void addOwnerPersonLinks(NavItemSpec linked, List<OwnerPersonLink> ownerPersonLinks, final String historyToken, String ownerIconKey, String iconTooltip) {
        final ArrayList<AbstractOwner> owners = new ArrayList<>(ownerPersonLinks.size());
        for (final OwnerPersonLink ownerPersonLink : ownerPersonLinks) {
            if(ownerPersonLink != null) {
                final AbstractOwner owner = ownerPersonLink.getOwner();
                owners.add(owner);

                final HistoryToken token = HistoryToken.builder(historyToken)
                        .with(OBJECTID_KEY, owner.getId())
                        .build();

                final HistoryContextProducer contextProducer = PmItemListContext.createProducerForOwner(person.getName(),
                        ICON_KEY, owner, owners, historyToken);

                linked.addChild(new NavItemSpec("VP-" + owner.getId(), owner.getName(), token, contextProducer)// $NON-NLS$
                        .withIsTransient().withIcon(ownerIconKey, SafeHtmlUtils.fromString(iconTooltip)));
            }
        }
    }

    @Override
    protected ShellMMType getShellMMType() {
        return ShellMMType.ST_PERSON;
    }

    @Override
    protected Widget getNavNorthWidget() {
        if (this.person == null) {
            Firebug.log("person is null!");
            return null;
        }
        return ObjectWidgetFactory.createPersonWidget(this.person);
    }

    @Override
    public Person createUserObject() {
        final Person resultObject = this.batPerson.createResultObject();

        final List<UserDefinedFields> userDefinedFieldsList = this.batUserDefinedFields.createResultObject();

        if(userDefinedFieldsList != null) {
            assert userDefinedFieldsList.size() == 1 : "unexpected user defined fields list size. Must be 1!";
            resultObject.setUserDefinedFields(userDefinedFieldsList.get(0));
        }

        return resultObject;
    }

    @Override
    protected Person getUserObject() {
        return this.person;
    }

    @Override
    protected void setUserObject(Person userObject) {
        this.person = userObject;
    }

    @Override
    protected String getName(Person userObject) {
        return I18n.I.staticData() + " " + userObject.getName();
    }

    @Override
    protected String getControllerId() {
        return PmWebModule.HISTORY_TOKEN_PERSON;
    }
}