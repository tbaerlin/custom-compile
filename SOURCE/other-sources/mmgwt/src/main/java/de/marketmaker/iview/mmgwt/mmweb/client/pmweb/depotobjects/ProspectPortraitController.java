/*
 * ProspectMmTalkerController.java
 *
 * Created on 05.06.14 08:31
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.PageController;
import de.marketmaker.iview.mmgwt.mmweb.client.as.IsPrivacyModeProvider;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyModeProvider;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ContextItemComparator;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryContextProducer;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebModule;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.OwnerPersonLink;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Person;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Prospect;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.UserDefinedFields;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.history.PmItemListContext;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.Formula;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.view.HasIsProvidesPrintableView;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;
import de.marketmaker.iview.pmxml.HasEditProspectPrivResponse;
import de.marketmaker.iview.pmxml.HasPrivRequest;
import de.marketmaker.iview.pmxml.MMClassIndex;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.UMRightBody;
import de.marketmaker.iview.pmxml.WorkspaceSheetDesc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Markus Dick
 */
public class ProspectPortraitController extends AbstractDepotObjectPortraitController<Prospect>
        implements HasIsProvidesPrintableView, IsPrivacyModeProvider {
    private BlockAndTalker<Prospect.ProspectTalker, Prospect, Prospect> batProspect;
    private BlockAndTalker<UserDefinedFields.Talker, List<UserDefinedFields>, UserDefinedFields> batUserDefinedFields;

    private Prospect prospect;

    private final DmxmlContext.Block<HasEditProspectPrivResponse> blockEditAllowed;
    private final HasPrivRequest editAllowedRequest;
    private boolean editAllowed = false;

    private final ProspectPrivacyModeProvider privacyModeProvider = new ProspectPrivacyModeProvider(this);

    public ProspectPortraitController() {
        super(MMClassIndex.CI_T_INTERESSENT, MMClassIndex.CI_T_INTERESSENT, UMRightBody.UMRB_READ_DOCUMENTS, UMRightBody.UMRB_EDIT_ACTIVITY_INSTANCE);
        //With PM_UM_HasEditProspectPriv, PM simultaneously checks that one is allowed to edit prospects and persons.
        //Thus, it is not necessary to set a right body.
        this.editAllowedRequest = new HasPrivRequest();
        this.editAllowedRequest.setClassIdx(MMClassIndex.CI_T_SHELL_MM);
        this.blockEditAllowed = this.context.addBlock("PM_UM_HasEditProspectPriv"); //$NON-NLS$
        this.blockEditAllowed.setParameter(this.editAllowedRequest);
        this.blockEditAllowed.setToBeRequested();
    }

    public HistoryToken newProspectToken() {
        return HistoryToken.builder(PmWebModule.HISTORY_TOKEN_PROSPECT)
                .with(OBJECTID_KEY, this.prospect.getId())
                .build();
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        final HistoryToken historyToken = event.getHistoryToken();
        final String objectId = historyToken.get(OBJECTID_KEY);
        if (objectId != null) {
            if (!objectId.equals(this.editAllowedRequest.getId()) || !this.blockEditAllowed.isResponseOk()) {
                Firebug.debug("<ProspectMmTalkerController.onPlaceChange> Requesting if edit for objectId=" + objectId + " is allowed!");
                this.editAllowedRequest.setId(objectId);
                this.blockEditAllowed.setToBeRequested();
            }
        }

        super.onPlaceChange(event);
    }

    @Override
    public void onSuccess(ResponseType result) {
        if (this.blockEditAllowed.isResponseOk()) {
            final HasEditProspectPrivResponse hasPrivResponse = this.blockEditAllowed.getResult();
            this.editAllowed = (hasPrivResponse != null) && hasPrivResponse.isHasPriv();
            Firebug.debug("<ProspectMmTalkerController.onSuccess> result != null? " + (result != null) + "; edit allowed for '" + this.editAllowedRequest.getId() + "'? " + this.editAllowed);
        }
        else {
            Firebug.debug("<ProspectMmTalkerController.onSuccess> edit allowed block response is not ok");
            this.editAllowed = false;
        }

        super.onSuccess(result);
    }

    @Override
    public BlockAndTalker[] initBats() {
        batProspect = new BlockAndTalker<>(this.context, new Prospect.ProspectTalker());
        batUserDefinedFields = new BlockAndTalker<>(
                this.context,
                new UserDefinedFields.Talker(
                        Formula.create("Interessent").withUserFieldCategories( // $NON-NLS$
                                PmWebSupport.getInstance().getUserFieldDecls(ShellMMType.ST_INTERESSENT)
                        )
                )
        );

        return new BlockAndTalker[]{
                this.batProspect,
                this.batUserDefinedFields,
        };
    }

    protected NavItemSpec initNavItems(List<WorkspaceSheetDesc> sheets) {
        final NavItemSpec linked;

        final String prospectName = this.prospect.getName();
        final HistoryToken defaultToken = newProspectToken();
        final UserObjectController<Prospect, ProspectPortraitController, UserObjectDisplay.UserObjectPresenter<Prospect>> prospectUOC = new UserObjectController<>(this, new ProspectView(), this.editAllowed);

        final NavItemSpec root = new NavItemSpec("root", "root"); //$NON-NLS$
        root.addChild(setFallback(new NavItemSpec(AbstractUserObjectView.SC_STATIC, I18n.I.staticData(), defaultToken, prospectUOC)).withOpenWithSelection().withClosingSiblings().addChildren(
                new NavItemSpec(AbstractUserObjectView.SC_CONTACT, I18n.I.contact(), defaultToken, prospectUOC),
                new NavItemSpec(AbstractUserObjectView.SC_LINKED_PERSONS, I18n.I.linkedPersons(), defaultToken, prospectUOC),
                new NavItemSpec(AbstractUserObjectView.SC_REPORTING, I18n.I.reportingDetails(), defaultToken, prospectUOC),
                new NavItemSpec(AbstractUserObjectView.SC_ADVISOR, I18n.I.advisor(), defaultToken, prospectUOC),
                new NavItemSpec(AbstractUserObjectView.SC_TAX, I18n.I.taxDetails(), defaultToken, prospectUOC),
                new NavItemSpec(AbstractUserObjectView.SC_USER_DEFINED_FIELDS, I18n.I.userDefinedFields(), defaultToken, prospectUOC)
        ));

        if (this.editAllowed) {
            addEditNode(root);
        }

        root.addChild(linked = new NavItemSpec("V", I18n.I.linkedObjects()).withClosingSiblings()); // $NON-NLS$

        //sets also the default sub controller, which should be a dashboard if there is one.
        //So the order of addDashboard and createLayoutChildren matters!
        addDashboard(root);

        root.addChild(new NavItemSpec("R", I18n.I.pmAnalyses()).withHasDelegate().withClosingSiblings()  // $NON-NLS$
                .withSelectFirstChildOnOpen()
                .addChildren(createLayoutChildren(defaultToken, prospectName, this.prospect.getId(), sheets)));

        final List<OwnerPersonLink> ownerPersonLinks = this.prospect.linkedPersons();
        if (ownerPersonLinks != null && !ownerPersonLinks.isEmpty()) {
            final ArrayList<Person> persons = new ArrayList<>(ownerPersonLinks.size());
            for (final OwnerPersonLink personLink : ContextItemComparator.sort(ownerPersonLinks)) {
                if (personLink != null) {
                    final Person person = personLink.getPerson();
                    persons.add(person);

                    final HistoryToken token = HistoryToken.builder(PmWebModule.HISTORY_TOKEN_PERSON)
                            .with(OBJECTID_KEY, personLink.getPerson().getId())
                            .build();

                    final HistoryContextProducer contextProducer = PmItemListContext.createProducerForPerson(
                            prospectName, "pm-investor-prospect", person, persons);  // $NON-NLS$

                    linked.addChild(new NavItemSpec("VA-" + personLink.getPerson().getId(), person.getName(), token, //$NON-NLS$
                            contextProducer).withIsTransient().withIcon("pm-investor-person", SafeHtmlUtils.fromString(I18n.I.person())));  // $NON-NLS$
                }
            }
        }

        addActivityNode(root);

        return root;
    }

    @Override
    protected ShellMMType getShellMMType() {
        return ShellMMType.ST_INTERESSENT;
    }

    @Override
    protected Widget getNavNorthWidget() {
        if (this.prospect == null) {
            Firebug.debug("prospect is null!");
            return null;
        }
        return ObjectWidgetFactory.createProspectWidget(this.prospect);
    }

    @Override
    public Prospect createUserObject() {
        final Prospect prospect = this.batProspect.createResultObject();
        prospect.setUserDefinedFields(this.batUserDefinedFields.createResultObject().get(0));
        return prospect;
    }

    @Override
    protected Prospect getUserObject() {
        return this.prospect;
    }

    @Override
    protected void setUserObject(Prospect userObject) {
        this.prospect = userObject;
    }

    @Override
    protected String getName(Prospect userObject) {
        return I18n.I.staticData() + " " + userObject.getName();
    }

    @Override
    protected String getControllerId() {
        return PmWebModule.HISTORY_TOKEN_PROSPECT;
    }

    @Override
    public Widget getPrintView() {
        final PageController current = this.getCurrentPageController();
        if (current instanceof HasIsProvidesPrintableView) {
            return ((HasIsProvidesPrintableView) current).getPrintView();
        }
        return null;
    }

    @Override
    public PrivacyModeProvider asPrivacyModeProvider() {
        return this.privacyModeProvider;
    }

    private static class ProspectPrivacyModeProvider extends AbstractPrivacyModeProvider {
        private final ProspectPortraitController prospectPortraitController;

        public ProspectPrivacyModeProvider(ProspectPortraitController prospectPortraitController) {
            super(ShellMMType.ST_INTERESSENT);
            this.prospectPortraitController = prospectPortraitController;
        }

        @Override
        protected HistoryToken createDepotObjectToken() {
            return prospectPortraitController.newProspectToken();
        }

        @Override
        public Set<String> getObjectIdsAllowedInPrivacyMode() {
            final HashSet<String> ids = new HashSet<>();

            ids.add(this.prospectPortraitController.prospect.getId());
            for (OwnerPersonLink personLink : this.prospectPortraitController.prospect.linkedPersons()) {
                ids.add(personLink.getPerson().getId());
            }

            return ids;
        }

        @Override
        public void requestPrivacyModeActivatable(PrivacyModeActivatableCallback callback) {
            this.prospectPortraitController.privacyModeActivatableCallback = callback;
        }
    }
}