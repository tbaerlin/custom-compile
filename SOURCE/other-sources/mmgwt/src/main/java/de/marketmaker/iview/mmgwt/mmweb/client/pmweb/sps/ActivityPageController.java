/*
 * ActivityPageController.java
 *
 * Created on 01.09.14
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.notification.NotificationMessage;
import de.marketmaker.itools.gwtutil.client.widgets.notification.Notifications;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.MainController;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ValidationMessagePopup;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.AbstractDepotObjectPortraitController;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.TimeTracer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.function.SingleConsumable;
import de.marketmaker.iview.mmgwt.mmweb.client.view.HasIsProvidesPrintableView;
import de.marketmaker.iview.pmxml.ActivityCreateRequest;
import de.marketmaker.iview.pmxml.ActivityCreateResponse;
import de.marketmaker.iview.pmxml.ActivityInstanceInfo;
import de.marketmaker.iview.pmxml.ActivityNavigationDirection;
import de.marketmaker.iview.pmxml.ActivityTask;
import de.marketmaker.iview.pmxml.ActivityTransactionRequest;
import de.marketmaker.iview.pmxml.ActivityTransactionResponse;
import de.marketmaker.iview.pmxml.DatabaseObject;
import de.marketmaker.iview.pmxml.MMClassIndex;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author mloesch
 */
@SuppressWarnings("Convert2Lambda") // due to source compatibility with 5_30/as 1.30 do not introduce diamonds, lambdas, or streams here. TODO: remove annotation after cherry pick into 1.30
public class ActivityPageController extends AbstractPageController implements
        HasIsProvidesPrintableView, PrivacyMode.InterestedParty {


    interface CreateInstanceCallback {
        void created(ActivityInstanceInfo info);
    }

    public static final String PARAM_ACTIVITY_INSTANCE = "inst"; // $NON-NLS$

    public static final String PARAM_ACTIVITY_DEFINITION = "def"; // $NON-NLS$

    public static final String PARAM_ACTIVITY_TASK = "task"; // $NON-NLS$

    public static final String PARAM_APPLY_AS_MAIN_INPUT = "aami";  // $NON-NLS$

    public static final String PARAM_GOTO_OBJECT_ON_ERROR_ID = "goneid";  // $NON-NLS$

    public static final String PARAM_GOTO_OBJECT_ON_ERROR_TYPE = "gonetype";  // $NON-NLS$

    protected final DmxmlContext.Block<ActivityTransactionResponse> block;

    private final ActivityTransactionRequest activityTransactionRequest;

    protected TaskController taskController;

    private Command afterSubmit;

    private MMClassIndex mmClassIndex;

    protected final TimeTracer tt = new TimeTracer(false);

    protected final SingleConsumable<ActivityTransactionResponse> consumableActivityTransactionRequest = new SingleConsumable<>();

    public ActivityPageController() {
        this(null, null);
    }

    public ActivityPageController(MMClassIndex mmClassIndex, Command afterSubmit) {
        initTaskController();
        this.mmClassIndex = mmClassIndex;
        this.afterSubmit = afterSubmit;
        this.block = this.context.addBlock("ACT_Transaction"); // $NON-NLS$
        this.activityTransactionRequest = new ActivityTransactionRequest();
        this.activityTransactionRequest.setCustomerDesktopActive(PrivacyMode.isActive());
        this.block.setParameter(this.activityTransactionRequest);

        PrivacyMode.subscribe(this);
    }

    @Override
    public void privacyModeStateChanged(boolean privacyModeActive,
            PrivacyMode.StateChangeProcessedCallback processed) {
        this.activityTransactionRequest.setCustomerDesktopActive(privacyModeActive);
        this.block.setToBeRequested();

        processed.privacyModeStateChangeProcessed(this);
    }

    public ActivityPageController withCancelCommand(Command onCancel) {
        this.taskController.withCancelCommand(onCancel);
        return this;
    }

    protected void initTaskController() {
        this.taskController = new TaskController(this, true, this.tt);
    }

    @Override
    public void onPlaceChange(final PlaceChangeEvent event) {
        if (this.tt.isRunning()) {
            this.tt.stop();
        }
        Firebug.info("<" + getClass().getSimpleName() + ".onPlaceChange> time trace of former request: " + this.tt.toString());

        this.tt.reset();
        this.tt.start();
        this.tt.trace("onPlaceChange");  // $NON-NLS$

        // The command executes the place change event just after the data has been definitely saved.
        // This is necessary, because the saved data may have an effect on the structure and the order of the
        // following/next task.
        this.taskController.tryToSaveViewData(new Command() {
            @Override
            public void execute() {
                doPlaceChange(event.getHistoryToken());
            }
        });
    }

    protected void doPlaceChange(final HistoryToken historyToken) {
        if (!this.tt.isRunning()) {
            this.tt.start();
        }
        this.tt.trace("ActivityPageController.doPlaceChange");  // $NON-NLS$

        Firebug.debug("<ActivityPageController.doPlaceChange> " + historyToken);
        final String objectId = historyToken.get(AbstractDepotObjectPortraitController.OBJECTID_KEY);
        final String activityDef = historyToken.get(PARAM_ACTIVITY_DEFINITION);

        if (StringUtil.hasText(objectId) && StringUtil.hasText(activityDef)) {
            //this branch is only active for edit activities
            final String applyAsMainInputId = historyToken.get(PARAM_APPLY_AS_MAIN_INPUT);

            final String mainInputId;
            final String ownerId;

            //If an alternative mainInput is present, use this as the main input and the object ID as the owner Id;
            //Otherwise use the object ID as the main input and do not apply an owner ID.
            //This is necessary for Edit activities that do not use the object identified by object ID as their main
            //input, e.g., the portfolio version.
            if (StringUtil.hasText(applyAsMainInputId)) {
                mainInputId = applyAsMainInputId;
                ownerId = objectId;
            }
            else {
                mainInputId = objectId;
                ownerId = objectId;
            }

            createInstance(mainInputId, this.mmClassIndex, ownerId, activityDef, this.tt, new CreateInstanceCallback() {
                @Override
                public void created(ActivityInstanceInfo info) {
                    requestInstance(info.getId(), null, null);
                }
            });
        }
        else {
            final String instanceId = historyToken.get(PARAM_ACTIVITY_INSTANCE);
            final String taskId = historyToken.get(PARAM_ACTIVITY_TASK);

            final Optional<ActivityTransactionResponse> optionalAtr = this.consumableActivityTransactionRequest.pull();
            if (optionalAtr.isPresent()) {
                final ActivityTransactionResponse atr = optionalAtr.get();
                if (StringUtil.equals(instanceId, atr.getInfo().getId()) &&
                        StringUtil.equals(taskId, atr.getCurrentTask())) {
                    useInstance(atr);
                    return;
                }
            }
            // fall through case: request the task instance:

            // if an task id is given, load exactly this task if no task id is given let PM
            // determine the appropriate task.
            requestInstance(instanceId, taskId, StringUtil.hasText(taskId) ? ActivityNavigationDirection.AND_THIS : null);
        }
    }

    private void requestInstance(String instanceId, String taskId,
            ActivityNavigationDirection direction) {
        this.tt.trace("ActivityPageController.requestInstance (enter)");  // $NON-NLS$
        this.tt.startTrace("ActivityPageController.requestInstance");  // $NON-NLS$

        this.activityTransactionRequest.setInstanceId(instanceId);
        this.activityTransactionRequest.setTargetTask(taskId);
        this.activityTransactionRequest.setDirection(direction);

        this.block.setToBeRequested();

        doRefresh();

        getContentContainer().setContent(this.taskController.getViewWidget());
        this.tt.stopTrace("ActivityPageController.requestInstance");  // $NON-NLS$
    }

    protected void useInstance(final ActivityTransactionResponse activityTransactionResponse) {
        this.tt.trace("ActivityPageController.useInstance (enter)");  // $NON-NLS$
        this.tt.startTrace("ActivityPageController.useInstance");  // $NON-NLS$

        getContentContainer().setContent(this.taskController.getViewWidget());

        // Simulates an async request, because as long as PlaceChangeEvents are being dispatched,
        // no SpsAfterPropertiesSetEvent handlers will be added to/removed from the event bus.
        // Thus firing SpsAfterPropertiesSetEvent will take no effect. Hence, it is necessary to
        // delay the handler registration and firing the event until dispatching ends (as it
        // naturally occurs, if we request the task asynchronously).
        Scheduler.get().scheduleFinally(new Command() {
            @Override
            public void execute() {
                if (!tt.isRunning()) {
                    tt.start();
                }
                onResult(activityTransactionResponse);
                tt.stop();
            }
        });

        this.tt.stopTrace("ActivityPageController.useInstance");  // $NON-NLS$
    }

    protected static void createInstance(final String mainInputId, MMClassIndex mmClassIndex,
            String ownerId, final String activityDef, final TimeTracer tt,
            final CreateInstanceCallback callback) {

        tt.startTrace("createInstance");  // $NON-NLS$

        final DmxmlContext context = new DmxmlContext();
        final DmxmlContext.Block<ActivityCreateResponse> block = context.addBlock("ACT_CreateInstance"); // $NON-NLS$
        final ActivityCreateRequest request = new ActivityCreateRequest();
        request.setCustomerDesktopActive(PrivacyMode.isActive());
        request.setMainObject(newDatabaseObject(mainInputId, ownerId, mmClassIndex));
        request.setDefinitionId(activityDef);
        block.setParameter(request);
        context.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable caught) {
                if (tt.isTraceStarted("createInstance")) {   // $NON-NLS$
                    tt.stopTrace("createInstance", "createInstance..onFailure");  // $NON-NLS$
                }
                Firebug.error("cannot create activity instance (mainInputId=" + mainInputId + ", activity=" + activityDef + ")", caught);
                Notifications.add("Verbindungsfehler", "Kann Aktivität nicht erstellen"); // TODO: $NON-NLS$
            }

            @Override
            public void onSuccess(ResponseType result) {
                if (tt.isTraceStarted("createInstance")) {   // $NON-NLS$
                    tt.stopTrace("createInstance", "createInstance..onSuccess");  // $NON-NLS$
                }
                if (!block.isResponseOk()) {
                    Firebug.error("cannot create activity instance (mainInputId=" + mainInputId + ", activity=" + activityDef + ") --> check server log");
                    Notifications.add("Serverfehler", "Kann Aktivität nicht erstellen").requestStateDelayed(NotificationMessage.State.DELETED, 8); // TODO: $NON-NLS$
                    return;
                }
                if (callback != null) {
                    callback.created(block.getResult().getInfo());
                }
            }
        });
    }

    private static DatabaseObject newDatabaseObject(String objectId, String ownerId,
            MMClassIndex mmClassIndex) {
        final DatabaseObject databaseObject = new DatabaseObject();
        databaseObject.setClassIdx(mmClassIndex);
        databaseObject.setId(objectId);
        databaseObject.setOwnerId(ownerId);
        return databaseObject;
    }

    @Override
    public void refresh() {
        Firebug.debug("<ActivityPageController.refresh>");
        this.taskController.onImplicitSave(true);
    }

    public void doRefresh() {
        Firebug.debug("<ActivityPageController.doRefresh>");
        if (!this.tt.isRunning()) {
            this.tt.start();
        }
        this.tt.trace("ActivityPageController.doRefresh (enter)"); // $NON-NLS$
        this.tt.startTrace("ActivityPageController.doRefresh"); // $NON-NLS$

        this.tt.startTrace("ActivityPageController.onResult (from doRefresh)"); // $NON-NLS$
        ActivityPageController.super.refresh();

        this.tt.stopTrace("ActivityPageController.doRefresh"); // $NON-NLS$
    }

    @Override
    protected void onResult() {
        if (!this.tt.isRunning()) {
            this.tt.start();
        }
        if (this.tt.isTraceStarted("ActivityPageController.onResult (from doRefresh)")) { // $NON-NLS$
            this.tt.stopTrace("ActivityPageController.onResult (from doRefresh)"); // $NON-NLS$
        }
        this.tt.trace("ActivityPageController.onResult (enter)"); // $NON-NLS$
        this.tt.startTrace("ActivityPageController.onResult"); // $NON-NLS$

        if (!this.block.isResponseOk()) {
            DebugUtil.displayServerError(this.block);
            getContentContainer().setContent(new Label(I18n.I.cannotLoadActivityDetails()));
            return;
        }

        Firebug.debug("<ActivityPageController.onResult>");
        onResult(this.block.getResult());

        this.tt.stopTrace("ActivityPageController.onResult"); // $NON-NLS$
        this.tt.stop();
    }

    protected void onResult(ActivityTransactionResponse atr) {
        this.tt.trace("ActivityPageController.onResult(:ActivityTransactionResponse) (enter)"); // $NON-NLS$
        this.tt.startTrace("ActivityPageController.onResult(:ActivityTransactionResponse)"); // $NON-NLS$

        final Map<String, ActivityTask> tasks = createTasksMap(atr);
        final String currentTaskId = atr.getCurrentTask();
        final ActivityTask task;
        if (!StringUtil.hasText(currentTaskId)) {
            MainController.INSTANCE.getView().setContentHeader("");
            getContentContainer().setContent(new Label("Die Aktivität hat momentan keine bearbeitbaren Seiten. Eventuell fehlen Ihnen die nötigen Berechtigungen.")); // TODO: $NON-NLS$
            return;
        }
        else if (!tasks.containsKey(currentTaskId)) {
            Notifications.add("Fehler", "ActivityInstanceResponse.CurrentTask ist nicht in der Liste der Tasks enthalten"); // $NON-NLS$
            return;
        }
        else {
            task = tasks.get(currentTaskId);
        }

        final MainInput mainInput = MainInput.Factory.get(atr.getInfo().getMainInput());
        this.taskController.setTask(atr, task, mainInput);
        this.taskController.setActionPreviousVisible(StringUtil.hasText(atr.getPreviousTask()));
        MainController.INSTANCE.getView().setContentHeader(task.getName());

        this.tt.stopTrace("ActivityPageController.onResult(:ActivityTransactionResponse)"); // $NON-NLS$
    }

    @Override
    public void deactivate() {
        if (!this.tt.isRunning()) {
            this.tt.start();
        }
        super.deactivate();
        ValidationMessagePopup.I.hide(false);
        this.taskController.tryToSaveViewData();
        this.taskController.deactivate();
        this.taskController.releaseAndClearView();

        this.consumableActivityTransactionRequest.pull().ifPresent(new Consumer<ActivityTransactionResponse>() {
            @Override
            public void accept(ActivityTransactionResponse activityTransactionResponse) {
                Firebug.info("<ActivityPageController.deactivate> removed consumable ActivityTransactionResponse");
                consumableActivityTransactionRequest.pull().ifPresent(new Consumer<ActivityTransactionResponse>() {
                    @Override
                    public void accept(ActivityTransactionResponse activityTransactionResponse) {
                        Firebug.error("<ActivityPageController.deactivate> removed ActivityTransactionResponse but it is still present");
                    }
                });
            }
        });

        this.tt.stop();
        Firebug.info("<ActivityPageController.deactivate> time trace: " + this.tt.toString());
        //do not reset tt because then we loose the log data of the former request when entering onPlaceChange again
    }

    @Override
    public void activate() {
        super.activate();
        this.taskController.activate();
    }

    protected Map<String, ActivityTask> createTasksMap(ActivityTransactionResponse atr) {
        final Map<String, ActivityTask> map = new HashMap<>();
        for (ActivityTask task : atr.getTasks()) {
            map.put(task.getId(), task);
        }
        return map;
    }

    protected HistoryToken createTaskToken(String activityInstanceId, String taskId) {
        final HistoryToken.Builder builder = HistoryToken.Builder.fromCurrent() //reuse goneid and gonetype if present
                .with(PARAM_ACTIVITY_INSTANCE, activityInstanceId) //this is not really necessary, but for safety reasons
                .with(PARAM_ACTIVITY_TASK, taskId);

        return builder.build();
    }

    public void onImplicitSaveCancelled(String activityInstanceId, String taskId) {
        final HistoryToken taskToken = createTaskToken(activityInstanceId, taskId);
        doPlaceChange(taskToken);
    }

    public void onSubmitSuccess(ActivityTransactionResponse atr, boolean firePlaceChangeEvent) {
        Firebug.warn("<ActivityPageController.onSubmitSuccess> " + atr.getCurrentTask());
        this.consumableActivityTransactionRequest.push(atr);

        if (this.afterSubmit == null) {
            return;
        }
        this.afterSubmit.execute();
    }

    @Override
    public String getPrintHtml() {
        // getPrintHtml is specified by PageController, but we do not use it here, because
        // ActivityPageController implements HasProvidesPrintableView and hence getPrintView
        // is used instead.
        return I18n.I.notPrintable();
    }

    @Override
    public Widget getPrintView() {
        return this.taskController.getPrintView();
    }
}