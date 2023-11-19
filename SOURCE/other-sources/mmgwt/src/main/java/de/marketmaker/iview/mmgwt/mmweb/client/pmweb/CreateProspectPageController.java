package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import com.google.gwt.user.client.Command;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.notification.Notifications;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.MainController;
import de.marketmaker.iview.mmgwt.mmweb.client.ResponseTypeCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.AbstractDepotObjectPortraitController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.ActivityPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;
import de.marketmaker.iview.pmxml.ActivityEditDefinitionRequest;
import de.marketmaker.iview.pmxml.ActivityEditDefinitionResponse;
import de.marketmaker.iview.pmxml.CreateDepManRequest;
import de.marketmaker.iview.pmxml.CreateDepManResponse;
import de.marketmaker.iview.pmxml.CreateShellMMResponseType;
import de.marketmaker.iview.pmxml.GetZonesRequest;
import de.marketmaker.iview.pmxml.GetZonesResponse;
import de.marketmaker.iview.pmxml.LinkOwnerPersonRequest;
import de.marketmaker.iview.pmxml.LinkOwnerPersonResponse;
import de.marketmaker.iview.pmxml.MMClassIndex;
import de.marketmaker.iview.pmxml.MMOwnerPersonLink;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.UMZoneType;
import de.marketmaker.iview.pmxml.ZoneDesc;

import java.util.List;

/**
 * Created on 28.08.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class CreateProspectPageController extends AbstractPageController {
    private DmxmlContext.Block<CreateDepManResponse> prospectBlock;
    private DmxmlContext.Block<CreateDepManResponse> personBlock;
    private DmxmlContext.Block<LinkOwnerPersonResponse> connectBlock;
    private DmxmlContext.Block<GetZonesResponse> zoneBlock;
    private final DmxmlContext.Block<ActivityEditDefinitionResponse> blockEditAct;
    private CreateProspectView view;
    private final ContentContainer container;
    private String prospectId;

    public CreateProspectPageController(final ContentContainer contentContainer) {
        this.container = contentContainer;
        this.prospectBlock = new DmxmlContext().addBlock("PM_CreateDepMan"); // $NON-NLS$
        this.personBlock = new DmxmlContext().addBlock("PM_CreateDepMan"); // $NON-NLS$
        this.connectBlock = new DmxmlContext().addBlock("PM_LinkOwnerPerson"); // $NON-NLS$

        this.zoneBlock = this.context.addBlock("PM_GetZones"); // $NON-NLS$
        this.zoneBlock.setParameter(createZonesRequest());

        this.blockEditAct = this.context.addBlock("ACT_EditDefinition"); // $NON-NLS$
        final ActivityEditDefinitionRequest editReq = new ActivityEditDefinitionRequest();
        editReq.setInputType(MMClassIndex.CI_T_INTERESSENT);
        this.blockEditAct.setParameter(editReq);
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        reset();
        this.context.issueRequest(new ResponseTypeCallback() {
            @Override
            protected void onResult() {
                if (!blockEditAct.isResponseOk() || blockEditAct.getResult().getInfo() == null) {
                    MainController.INSTANCE.showError(I18n.I.prospectCreateErrorLoadingActivityFailed());
                    return;
                }
                if (!zoneBlock.isResponseOk()) {
                    MainController.INSTANCE.showError(I18n.I.prospectCreateErrorLoadingZonesFailed());
                    return;
                }
                view = new CreateProspectView(CreateProspectPageController.this);
                container.setContent(view.asWidget());
                if (AbstractMainController.INSTANCE != null) {
                    AbstractMainController.INSTANCE.getView().setContentHeader(I18n.I.prospectCreate());
                }
            }
        });
    }

    private void reset() {
        this.prospectId = null;
        this.personBlock.setEnabled(true);
        this.prospectBlock.setEnabled(true);
    }

    private GetZonesRequest createZonesRequest() {
        final GetZonesRequest gzr = new GetZonesRequest();
        gzr.getZoneTypes().add(UMZoneType.UMZT_SHELLOBJECT);
        gzr.setAddOnlyMutable(true);
        return gzr;
    }

    void create() {
        if (this.prospectBlock.isEnabled()) {
            createProspect();
        }
        else if (this.personBlock.isEnabled() && StringUtil.hasText(this.prospectId)) {
            createPerson(this.prospectId);
        }
    }

    private void createProspect() {
        this.prospectBlock.issueRequest(new ResponseTypeCallback() {
            @Override
            protected void onResult() {
                handleProspectCreateResult();
            }
        });
    }

    private void handleProspectCreateResult() {
        if (!this.prospectBlock.isResponseOk()) {
            if (this.prospectBlock.getResult() != null) {
                Firebug.info("getResponseType: " + this.prospectBlock.getResult().getResponseType());
            }
            showError(I18n.I.prospectCreateErrorProspectCreationFailed());
            this.view.hide();
            return;
        }
        final CreateDepManResponse prospectRes = this.prospectBlock.getResult();
        this.prospectId = prospectRes.getId();
        if (responseTypeOk(prospectRes)) {
            this.prospectBlock.setEnabled(false);
            createPerson(prospectRes.getId());
        }
        else {
            if (handleErrorResponse(prospectRes, I18n.I.prospectCreateErrorInsufficientRightsProspect(), I18n.I.prospectCreateErrorProspectCreationFailed())) {
                return;
            }
            if (prospectRes.getResponseType() == CreateShellMMResponseType.SCRT_DUPLICATE_NAME) {
                final String recommendation = prospectRes.getNameRecommendation();
                this.view.setProspectRecommendation(recommendation);
            }
        }
    }

    private void createPerson(final String prospectId) {
        this.personBlock.issueRequest(new ResponseTypeCallback() {
            @Override
            protected void onResult() {
                if (handlePersonCreateResult()) {
                    final String persId = personBlock.getResult().getId();
                    personBlock.setEnabled(false);
                    connectBoth(prospectId, persId, new Command() {
                        @Override
                        public void execute() {
                            HistoryToken.Builder.create(PmWebModule.HISTORY_TOKEN_PROSPECT)
                                    .with(AbstractDepotObjectPortraitController.OBJECTID_KEY, prospectId)
                                    .with(NavItemSpec.SUBCONTROLLER_KEY, AbstractDepotObjectPortraitController.HISTORY_TOKEN_EDIT_ACTIVITY)
                                    .with(ActivityPageController.PARAM_ACTIVITY_DEFINITION, blockEditAct.getResult().getInfo().getId())
                                    .build().fire();
                        }
                    });
                }
            }
        });
    }

    private boolean handlePersonCreateResult() {
        if (!this.personBlock.isResponseOk()) {
            if (this.personBlock.getResult() != null) {
                Firebug.info("getResponseType: " + this.prospectBlock.getResult().getResponseType());
            }
            showError(I18n.I.prospectCreateErrorPersonCreationFailed());
            this.view.hide();
            return false;
        }
        final CreateDepManResponse res = this.personBlock.getResult();
        if (!responseTypeOk(res)) {
            if (handleErrorResponse(res, I18n.I.prospectCreateErrorInsufficientRightsPerson(), I18n.I.prospectCreateErrorPersonCreationFailed())) {
                return false;
            }
        }
        return true;
    }

    private boolean handleErrorResponse(CreateDepManResponse res, String insufficientRightsMessage, String unknownMessage) {
        switch (res.getResponseType()) {
            case SCRT_UNKNOWN:
                this.view.hide();
                showError(unknownMessage);
                return true;
            case SCRT_INSUFFICIENT_RIGHTS:
                this.view.hide();
                showError(insufficientRightsMessage);
                return true;
        }
        return false;
    }

    private boolean responseTypeOk(CreateDepManResponse res) {
        return res.getResponseType() == CreateShellMMResponseType.SCRT_OK;
    }

    private void showError(String s) {
        Notifications.add(I18n.I.prospectCreateErrorCreationFailed(), s);
    }

    void configureProspect(String zoneId, String name) {
        configureDepotObject(zoneId, ShellMMType.ST_INTERESSENT, name, this.prospectBlock);
    }

    void configurePerson(String zoneId) {
        configureDepotObject(zoneId, ShellMMType.ST_PERSON, null, this.personBlock);
    }

    private void configureDepotObject(String zoneId, ShellMMType type, String name, DmxmlContext.Block<CreateDepManResponse> block) {
        final CreateDepManRequest req = new CreateDepManRequest();
        req.setName(name);
        req.setShellType(type);
        req.setPrimaryZoneId(zoneId);
        block.setParameter(req);
    }

    private void connectBoth(String prospectId, String personId, final Command doneCmd) {
        final LinkOwnerPersonRequest req = new LinkOwnerPersonRequest();
        final MMOwnerPersonLink link = new MMOwnerPersonLink();
        link.setOwnerId(prospectId);
        link.setPersonId(personId);
        req.setLink(link);
        this.connectBlock.setParameter(req);
        this.connectBlock.issueRequest(new ResponseTypeCallback() {
            @Override
            protected void onResult() {
                if (!connectBlock.isResponseOk()) {
                    showError(I18n.I.prospecLinkToPersonErrorAny());
                    return;
                }
                if (connectBlock.getResult().isInsufficientRights()) {
                    showError(I18n.I.prospecLinkToPersonErrorInsufficientRights());
                }
                else {
                    doneCmd.execute();
                }
            }
        });
    }

    public List<ZoneDesc> getZones() {
        return this.zoneBlock.getResult().getZones();
    }

    @Override
    public boolean isPrintable() {
        return false; //may not be printable, cf. AS-892
    }
}