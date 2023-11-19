package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.notification.Notifications;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.MainController;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebModule;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.AbstractDepotObjectPortraitController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.history.PmItemListContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.TimeTracer;
import de.marketmaker.iview.pmxml.ActivityDefinitionInfo;
import de.marketmaker.iview.pmxml.ActivityDefinitionsRequest;
import de.marketmaker.iview.pmxml.ActivityDefinitionsResponse;
import de.marketmaker.iview.pmxml.ActivityInstanceInfo;
import de.marketmaker.iview.pmxml.ActivityInstancesRequest;
import de.marketmaker.iview.pmxml.ActivityInstancesResponse;
import de.marketmaker.iview.pmxml.ActivityRemoveInstanceRequest;
import de.marketmaker.iview.pmxml.ActivityRemoveInstanceResponse;
import de.marketmaker.iview.pmxml.MMClassIndex;

import java.util.List;

/**
 * Author: umaurer
 * Created: 21.02.14
 */
public class ActivityOverviewController extends AbstractPageController implements ActivityOverviewView.Presenter, PrivacyMode.InterestedParty{
    private final DmxmlContext.Block<ActivityDefinitionsResponse> blockActivityDefs;
    private final ActivityDefinitionsRequest activityDefinitionsRequest;
    private final DmxmlContext.Block<ActivityInstancesResponse> blockActivityInsts;
    private final ActivityInstancesRequest activityInstancesRequest;
    private final ActivityOverviewView view = new ActivityOverviewViewImpl(this);

    private final DmxmlContext.Block<ActivityRemoveInstanceResponse> blockActRemove;
    private final ActivityRemoveInstanceRequest actRemoveRequest;

    private final MMClassIndex mmClassIndex;
    private String currentObjectId;

    public ActivityOverviewController(MMClassIndex mmClassIndex) {
        super(new DmxmlContext());

        this.mmClassIndex = mmClassIndex;

        this.activityDefinitionsRequest = new ActivityDefinitionsRequest();
        this.blockActivityDefs = this.context.addBlock("ACT_GetDefinitions"); // $NON-NLS$
        this.blockActivityDefs.setParameter(this.activityDefinitionsRequest);
        this.activityDefinitionsRequest.setInputType(this.mmClassIndex);
        this.activityDefinitionsRequest.setCustomerDesktopActive(PrivacyMode.isActive());

        this.activityInstancesRequest = new ActivityInstancesRequest();
        this.blockActivityInsts = this.context.addBlock("ACT_GetInstances"); // $NON-NLS$
        this.blockActivityInsts.setParameter(this.activityInstancesRequest);
        this.activityInstancesRequest.setCustomerDesktopActive(PrivacyMode.isActive());

        this.actRemoveRequest = new ActivityRemoveInstanceRequest();
        this.blockActRemove = new DmxmlContext().addBlock("ACT_RemoveInstance"); // $NON-NLS$
        this.blockActRemove.setParameter(this.actRemoveRequest);

        PrivacyMode.subscribe(this);
    }

    @Override
    public void privacyModeStateChanged(boolean privacyModeActive, PrivacyMode.StateChangeProcessedCallback processed) {
        this.activityInstancesRequest.setCustomerDesktopActive(privacyModeActive);
        this.blockActivityInsts.setToBeRequested();

        this.activityDefinitionsRequest.setCustomerDesktopActive(privacyModeActive);
        this.blockActivityDefs.setToBeRequested();

        processed.privacyModeStateChangeProcessed(this);
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        this.currentObjectId = event.getHistoryToken().get(AbstractDepotObjectPortraitController.OBJECTID_KEY);

        this.activityDefinitionsRequest.setCustomerDesktopActive(PrivacyMode.isActive());
        this.blockActivityDefs.setToBeRequested();

        this.activityInstancesRequest.setObjectId(this.currentObjectId);
        this.blockActivityInsts.setToBeRequested();

        this.context.issueRequest(this);
        this.view.clear();
        getContentContainer().setContent(this.view.asWidget());
        MainController.INSTANCE.getView().setContentHeader(I18n.I.activityOverview());
    }

    @Override
    protected void onResult() {
        if (!this.blockActivityDefs.isResponseOk()) {
            DebugUtil.displayServerError(this.blockActivityDefs);
            return;
        }
        if (!this.blockActivityInsts.isResponseOk()) {
            DebugUtil.displayServerError(this.blockActivityInsts);
            return;
        }

        final List<ActivityDefinitionInfo> defs = this.blockActivityDefs.getResult().getDefinitions();
        final List<ActivityInstanceInfo> insts = this.blockActivityInsts.getResult().getInstances();
        this.view.setActivities(defs, insts);
    }

    @Override
    public void createNewActivity(ActivityDefinitionInfo def) {
        final TimeTracer tt = new TimeTracer();
        tt.trace("ActivityOverviewController.createNewActivity"); // $NON-NLS$
        ActivityNavPageController.createInstance(this.currentObjectId, this.mmClassIndex, null, def.getId(), tt);
    }

    public void goToActivityInstance(ActivityInstanceInfo inst) {
        final List<ActivityInstanceInfo> insts = this.blockActivityInsts.getResult().getInstances();
        goToActivityInstance(inst, insts);
    }

    public static void goToActivityInstance(ActivityInstanceInfo inst, List<ActivityInstanceInfo> insts) {
        final MainInput mainInput = MainInput.Factory.get(inst.getMainInput());
        HistoryToken.Builder.create(PmWebModule.HISTORY_TOKEN_ACTIVITY)
                .with(ActivityNavPageController.PARAM_ACTIVITY_INSTANCE, inst.getId())
                .with(ActivityNavPageController.PARAM_GOTO_OBJECT_ON_ERROR_ID, mainInput.getObjectIdToGoOnError())
                .with(ActivityNavPageController.PARAM_GOTO_OBJECT_ON_ERROR_TYPE, mainInput.getObjectTypeToGoOnError().name())
                .fire(PmItemListContext.createForActivity(mainInput.getName(), mainInput.getIconKey(), inst, insts));
    }

    @Override
    public void deleteActivity(ActivityInstanceInfo inst) {
        this.actRemoveRequest.setInstanceId(inst.getId());
        this.blockActRemove.setToBeRequested();
        this.blockActRemove.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable caught) {
                Firebug.warn("cannot remove activity", caught);
                Notifications.add(I18n.I.error(), caught.getMessage());
            }

            @Override
            public void onSuccess(ResponseType result) {
                reloadActivityInstances();
            }
        });
    }

    @Override
    public void deleteAllActivities() {
        final DmxmlContext context = new DmxmlContext();
        for (ActivityInstanceInfo inst : this.blockActivityInsts.getResult().getInstances()) {
            final DmxmlContext.Block<ActivityRemoveInstanceResponse> blockActRemove = context.addBlock("ACT_RemoveInstance"); // $NON-NLS$
            final ActivityRemoveInstanceRequest actRemoveRequest = new ActivityRemoveInstanceRequest();
            actRemoveRequest.setInstanceId(inst.getId());
            blockActRemove.setParameter(actRemoveRequest);
        }
        context.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable caught) {
                Firebug.warn("cannot remove all activities", caught);
                Notifications.add(I18n.I.error(), caught.getMessage());
            }

            @Override
            public void onSuccess(ResponseType result) {
                reloadActivityInstances();
            }
        });
    }

    private void reloadActivityInstances() {
        this.blockActivityInsts.setToBeRequested();
        refresh();
    }
}
