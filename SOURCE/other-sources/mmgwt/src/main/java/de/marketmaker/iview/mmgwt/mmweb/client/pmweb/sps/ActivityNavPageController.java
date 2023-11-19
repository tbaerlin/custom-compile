package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.notification.Notifications;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.NavigationWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.EmptyContext;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryContext;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.history.NullContext;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmPlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebModule;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.ShellMMTypeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.AbstractDepotObjectPortraitController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.ObjectWidgetFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.TimeTracer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.HasNavWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ObjectPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ObjectTree;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ObjectTreeModel;
import de.marketmaker.iview.pmxml.ActivityChild;
import de.marketmaker.iview.pmxml.ActivityInstanceInfo;
import de.marketmaker.iview.pmxml.ActivityTask;
import de.marketmaker.iview.pmxml.ActivityTaskStatus;
import de.marketmaker.iview.pmxml.ActivityTransactionResponse;
import de.marketmaker.iview.pmxml.ErrorMM;
import de.marketmaker.iview.pmxml.FlowKind;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMClassIndex;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.List;
import java.util.Map;

/**
 * Author: umaurer
 * Created: 15.01.14
 */

@SuppressWarnings({"Convert2Lambda", "Convert2streamapi"}) // due to source compatibility with 5_30/as 1.30 do not introduce diamonds, lambdas, or streams here. TODO: remove annotation after cherry pick into 1.30
public class ActivityNavPageController extends ActivityPageController implements HasNavWidget {

    private HasNavWidget.NavWidgetCallback navWidgetCallback;

    private NavigationWidget navWidget;

    private String gotoObjectOnErrorId;

    private String gotoObjectOnErrorTypeName;

    private HistoryContext gotoObjectOnErrorContext;

    public static void createInstance(final String objectid, MMClassIndex mmClassIndex,
            String ownerId, final String activityDefinitionId, TimeTracer tt) {
        createInstance(objectid, mmClassIndex, ownerId, activityDefinitionId, tt, new CreateInstanceCallback() {
            @Override
            public void created(ActivityInstanceInfo info) {
                final MainInput mainInput = MainInput.Factory.get(info.getMainInput());

                final EmptyContext context = EmptyContext.create(mainInput.getName())
                        .withIconKey(mainInput.getIconKey());
                HistoryToken.Builder.create(PmWebModule.HISTORY_TOKEN_ACTIVITY)
                        .with(ActivityPageController.PARAM_ACTIVITY_INSTANCE, info.getId())
                        .with(ActivityPageController.PARAM_GOTO_OBJECT_ON_ERROR_ID, mainInput.getObjectIdToGoOnError())
                        .with(ActivityPageController.PARAM_GOTO_OBJECT_ON_ERROR_TYPE, mainInput.getObjectTypeToGoOnError().name())
                        .fire(context);
            }
        });
    }

    protected void initTaskController() {
        this.taskController = new TaskController(this, false, this.tt);
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        this.gotoObjectOnErrorContext = event.getHistoryContext();
        this.gotoObjectOnErrorId = event.getHistoryToken().get(PARAM_GOTO_OBJECT_ON_ERROR_ID);
        this.gotoObjectOnErrorTypeName = event.getHistoryToken().get(PARAM_GOTO_OBJECT_ON_ERROR_TYPE);
        this.navWidget = null;
        super.onPlaceChange(event);
    }

    public void updateNavWidget(ActivityInstanceInfo aii, ObjectTree objectTree) {
        Firebug.debug("ActivityNavPageController.updateNavWidget()"); // TODO: remove  // $NON-NLS$
        this.navWidget = new ObjectPanel(ObjectWidgetFactory.createActivityInfoWidget(aii), objectTree.asWidget());
        if (this.navWidgetCallback != null) {
            this.navWidgetCallback.setNavWidget(this.navWidget);
        }
    }

    @Override
    public void requestNavWidget(NavWidgetCallback callback) {
        this.navWidgetCallback = callback;
        if (this.navWidget != null) {
            this.navWidgetCallback.setNavWidget(this.navWidget);
        }
        else {
            this.navWidgetCallback.showGlass();
        }
    }

    @Override
    public boolean providesContentHeader() {
        return true;
    }

    @Override
    protected void onResult() {
        if (!this.tt.isRunning()) {
            this.tt.start();
        }
        // Stop the trace started by ActivityPageController here, so that the traces are filed in
        // the correct order. Order would be not human compatible if we stop the trace eventually
        // in super.onResult.
        if (this.tt.isTraceStarted("ActivityPageController.onResult (from doRefresh)")) { // $NON-NLS$
            this.tt.stopTrace("ActivityPageController.onResult (from doRefresh)", "ActivityNavPageController.onResult (from doRefresh)"); // $NON-NLS$
        }

        this.tt.trace("ActivityNavPageController.onResult"); // $NON-NLS$
        this.tt.startTrace("ActivityNavPageController.onResult (before super call)"); // $NON-NLS$

        if (!this.block.isResponseOk()) {
            if (StringUtil.hasText(this.gotoObjectOnErrorId) && StringUtil.hasText(this.gotoObjectOnErrorTypeName)) {
                try {
                    Notifications.add(I18n.I.serverError(), I18n.I.cannotLoadActivityDetails());

                    if (this.gotoObjectOnErrorContext != null) {
                        Firebug.debug("<ActivityNavPageController.onResult> Response not ok. Going back to bread crumb.");
                        AbstractMainController.INSTANCE.getHistoryThreadManager().backToBreadCrumb();
                        return;
                    }

                    Firebug.debug("<ActivityNavPageController.onResult> Response not ok. No bread crumb. Going to activity overview.");
                    final ShellMMInfo shellMMInfo = new ShellMMInfo();
                    shellMMInfo.setId(gotoObjectOnErrorId);
                    shellMMInfo.setTyp(ShellMMType.valueOf(this.gotoObjectOnErrorTypeName));
                    PmPlaceUtil.goToActivityOverview(shellMMInfo, NullContext.getInstance());
                } catch (Exception e) {
                    DebugUtil.showDeveloperNotification("Cannot go back to bread crumb or activity overview", e);
                }
            }
            return;
        }
        this.tt.stopTrace("ActivityNavPageController.onResult (before super call)"); // $NON-NLS$

        super.onResult();
        // do not file a stop trace again if one has already been filed by the super call.
        if (this.tt.isRunning()) {
            this.tt.stop();
        }
    }

    @Override
    protected void onResult(ActivityTransactionResponse atr) {
        this.tt.trace("ActivityNavPageController.onResult(:ActivityTransactionResponse) (enter)"); // $NON-NLS$
        super.onResult(atr);

        this.tt.startTrace("ActivityNavPageController.onResult(:ActivityTransactionResponse) (after super call)"); // $NON-NLS$

        final Map<String, ActivityTask> tasks = createTasksMap(atr);
        final ObjectTreeModel model = new ObjectTreeModel(initNavItems(atr, tasks));
        final NavItemSpec item = model.getItem("S-" + atr.getCurrentTask()); // $NON-NLS$
        if (item != null) {
            model.setSelected(item, false);
        }

        final ObjectTree objectTree = new ObjectTree(model);
        objectTree.addSelectionHandler(new SelectionHandler<NavItemSpec>() {
            @Override
            public void onSelection(SelectionEvent<NavItemSpec> event) {
                event.getSelectedItem().getHistoryToken().fire();
            }
        });

        updateNavWidget(atr.getInfo(), objectTree);
        this.tt.stopTrace("ActivityNavPageController.onResult(:ActivityTransactionResponse) (after super call)"); // $NON-NLS$
    }

    private NavItemSpec initNavItems(ActivityTransactionResponse atr,
            Map<String, ActivityTask> tasks) {
        ActivityTask rootTask = tasks.get(atr.getRootTask());
        final NavItemSpec rootItem = new NavItemSpec("root", "root"); // $NON-NLS$
        rootItem.addChild(createLinked(atr));

        // If a user does not have permissions to change the inner state of an activity, PM probably sends no task
        // children. It is not possible to check those inner permission a priori.
        final List<NavItemSpec> children = createTasks(atr.getInfo().getId(), tasks, rootTask).getChildren();
        if (children != null) {
            for (NavItemSpec nis : children) {
                rootItem.addChild(nis);
            }
        }
        return rootItem;
    }

    private NavItemSpec createLinked(ActivityTransactionResponse atr) {
        final NavItemSpec linked = new NavItemSpec("V", I18n.I.linkedObjects()).withOpenByDefault(); // $NON-NLS$
        final MM mm = atr.getInfo().getMainInput();

        assert mm instanceof ShellMMInfo;

        final ShellMMInfo mmInfo = (ShellMMInfo) mm;
        final ShellMMType mmType = mmInfo.getTyp();
        final SafeHtml pmObjectType = SafeHtmlUtils.fromString(PmRenderers.SHELL_MM_TYPE.render(mmType));
        final String controllerId = ShellMMTypeUtil.getControllerId(mmType);
        final HistoryToken token = HistoryToken.builder(controllerId)
                .with(AbstractDepotObjectPortraitController.OBJECTID_KEY, mmInfo.getId())
                .build();
        linked.addChild(new NavItemSpec(controllerId, mmInfo.getBezeichnung(), token).withIcon(ShellMMTypeUtil.getIconKey(mmType), pmObjectType));
        if (SessionData.INSTANCE.isUserPropertyTrue("developer")) { // $NON-NLS$
            linked.addChild(new NavItemSpec(AbstractDepotObjectPortraitController.HISTORY_TOKEN_ACTIVITY_OVERVIEW, I18n.I.activities(), token.with(NavItemSpec.SUBCONTROLLER_KEY, AbstractDepotObjectPortraitController.HISTORY_TOKEN_ACTIVITY_OVERVIEW).build()).withIcon("pm-activity")); // $NON-NLS$
        }
        return linked;
    }

    private NavItemSpec createTasks(String activityInstanceId, Map<String, ActivityTask> tasks,
            ActivityTask task) {
        final String taskName = StringUtil.hasText(task.getParent()) ? task.getName() : I18n.I.activityTasks();
        final ActivityTaskStatus status = task.getStatus();
        final FlowKind flowKind = task.getKind();
        final NavItemSpec nis;
        if (status == ActivityTaskStatus.ATS_INACTIVE || status == ActivityTaskStatus.ATS_ERROR || flowKind != FlowKind.FK_USER) {
            nis = new NavItemSpec("S-" + task.getId(), taskName).withOpenByDefault(); // $NON-NLS$
        }
        else {
            final HistoryToken token = createTaskToken(activityInstanceId, task.getId());
            nis = new NavItemSpec("S-" + task.getId(), taskName, token, this).withOpenByDefault(); // $NON-NLS$
        }
        setIcon(nis, status, flowKind, task);

        final List<ActivityChild> children = task.getChildren();
        if (children == null) {
            return nis;
        }
        for (ActivityChild child : children) {
            final ActivityTask childTask = tasks.get(child.getId());
            nis.addChild(createTasks(activityInstanceId, tasks, childTask));
        }
        return nis;
    }

    private void setIcon(NavItemSpec nis, ActivityTaskStatus status, FlowKind flowKind,
            ActivityTask task) {
        if (flowKind != FlowKind.FK_USER) {
            return;
        }
        switch (status) {
            case ATS_ERROR:
                nis.withLeftIcon("sps-task-error", getStatusTooltip(I18n.I.spsStatusError(), status, task)); // $NON-NLS$
                break;
            case ATS_INACTIVE:
                if (hasErrors(task)) {
                    nis.withLeftIcon("sps-task-inactive", getStatusTooltip(null, status, task)); // $NON-NLS$
                }
                break;
            case ATS_ACTIVE:
                if (hasErrors(task)) {
                    nis.withLeftIcon("sps-task-active", getStatusTooltip(null, status, task)); // $NON-NLS$
                }
                break;
            case ATS_INCOMPLETE:
                nis.withLeftIcon("sps-task-incomplete", getStatusTooltip(I18n.I.spsStatusIncomplete(), status, task)); // $NON-NLS$
                break;
            case ATS_FINISHED:
                if (hasErrors(task)) {
                    nis.withLeftIcon("sps-task-finished", getStatusTooltip(null, status, task)); // $NON-NLS$
                }
                break;
        }
    }

    private boolean hasErrors(ActivityTask task) {
        final List<ErrorMM> errors = task.getErrors();
        return errors != null && !errors.isEmpty();
    }

    private SafeHtml getStatusTooltip(String header, ActivityTaskStatus status, ActivityTask task) {
        final List<ErrorMM> errors = task.getErrors();
        if (header == null && (errors == null || errors.isEmpty())) {
            return null;
        }
        Firebug.groupStart("SPS Task Status (" + task.getName() + "): " + status.toString());
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendHtmlConstant("<div class=\"sps-task-status\">"); // $NON-NLS$
        if (header != null) {
            sb.appendHtmlConstant("<div class=\"sps-task-status-header\">").appendEscaped(header).appendHtmlConstant("</div>");
        }
        if (errors != null && !errors.isEmpty()) {
            for (ErrorMM error : errors) {
                sb.append(IconImage.get("pmSeverity-" + error.getErrorSeverity().value()).getSafeHtml()); // $NON-NLS$
                sb.appendHtmlConstant("<div class=\"sps-task-status-error\">"); // $NON-NLS$
                sb.appendEscaped(" ");
                sb.appendEscaped(error.getErrorString());
                sb.appendHtmlConstant("</div>"); // $NON-NLS$

                Firebug.debug(error.getErrorSeverity().value() + "\n" // $NON-NLS$
                        + error.getErrorString() + "\n" // $NON-NLS$
                        + "Source: " + error.getCorrelationSource() + "\n" // $NON-NLS$
                        + "Target: " + error.getCorrelationTarget() // $NON-NLS$
                );
            }
        }
        sb.appendHtmlConstant("</div>"); // $NON-NLS$
        Firebug.groupEnd();
        return sb.toSafeHtml();
    }

    protected HistoryToken createTaskToken(String activityInstanceId, String taskId) {
        final HistoryToken.Builder builder = HistoryToken.Builder.fromCurrent() //reuse goneid and gonetype if present
                .with(PARAM_ACTIVITY_INSTANCE, activityInstanceId) //this is not really necessary, but for safety reasons
                .with(PARAM_ACTIVITY_TASK, taskId);

        return builder.build();
    }

    public void onSubmitSuccess(ActivityTransactionResponse atr, boolean firePlaceChangeEvent) {
        Firebug.debug("<ActivityNavPageController.onSubmitSuccess> currentTasks=" + atr.getCurrentTask() + " firePlaceChangeEvent?" + firePlaceChangeEvent);
        this.consumableActivityTransactionRequest.push(atr);

        final HistoryToken taskToken = createTaskToken(atr.getInfo().getId(), atr.getCurrentTask());
        if (firePlaceChangeEvent) {
            taskToken.fire();
        }
        else {
            doPlaceChange(taskToken);
        }
    }
}