package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.HasResizeHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.CompareUtil;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.GuiDefsLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.MainController;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.HasChildrenFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.RequiresRelease;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsCompositeProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsGroupProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsListProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.ViewStateFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.context.DmxmlContextFacade;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.validator.ValidationEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.validator.ValidationMocker;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.validator.ValidationResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.validator.ValidatorController;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.validator.WidgetAction;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.SpsAfterPropertiesSetEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Finalizer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.TimeTracer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.HasIsProvidesPrintableView;
import de.marketmaker.iview.mmgwt.mmweb.client.view.IsPrintable;
import de.marketmaker.iview.pmxml.ActivityNavigationDirection;
import de.marketmaker.iview.pmxml.ActivityTask;
import de.marketmaker.iview.pmxml.ActivityTransactionRequest;
import de.marketmaker.iview.pmxml.ActivityTransactionResponse;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerDataItems;
import de.marketmaker.iview.pmxml.DataContainerGroupNode;
import de.marketmaker.iview.pmxml.DataContainerLeafNodeDataItem;
import de.marketmaker.iview.pmxml.DataContainerListNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.ErrorMM;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.SubmitAction;
import de.marketmaker.iview.pmxml.TaskViewData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Author: umaurer
 * Created: 04.02.14
 */
@SuppressWarnings({"Convert2Lambda", "Convert2streamapi", "Convert2Diamond", "Anonymous2MethodRef"})
// due to source compatibility with 5_30/as 1.30 do not introduce diamonds, lambdas, method references, or streams here.
// TODO: remove annotation after cherry pick into 1.30
public class TaskController implements TaskDisplay.Presenter, HasIsProvidesPrintableView,
        PrivacyMode.InterestedParty {
    private final PreProcessHook preProcessHook = GWT.create(PreProcessHook.class);

    private final ValidatorController validatorController;

    private ActivityTransactionResponse atr;

    private final TaskDisplay view;

    private final ActivityPageController activityPageController;

    private final Command refreshInternalCommand = new Command() {
        @Override
        public void execute() {
            onImplicitSave(false);
        }
    };

    private Context spsContext;

    private ActivityTask task;

    private SpsWidget spsRootWidget;

    private MainInput mainInput;

    private boolean allowSave = false;

    private boolean allowImplicitSave = false;

    private Command cancelCommand;

    private HandlerRegistration refreshChangeHandler;

    private String lastTaskId;

    private final HashMap<String, Map<String, String>> viewState = new HashMap<>();

    private final HashSet<ViewStateFeature> viewStateFeatures = new HashSet<>();

    private final TimeTracer tt;

    public TaskController(ActivityPageController activityPageController, boolean simpleView,
            TimeTracer tt) {
        this.tt = tt;
        this.view = simpleView ? new SimpleTaskView().withPresenter(this) : new TaskView().withPresenter(this);
        this.activityPageController = activityPageController;
        this.validatorController = new ValidatorController(this);

        PrivacyMode.subscribe(this);

        releaseAndClearView();
    }

    @Override
    public void privacyModeStateChanged(boolean privacyModeActive,
            PrivacyMode.StateChangeProcessedCallback processed) {
        this.lastTaskId = null;
        this.viewState.clear();

        processed.privacyModeStateChangeProcessed(this);
    }

    public void setTask(ActivityTransactionResponse atr, ActivityTask task, MainInput mainInput) {
        Firebug.debug("TaskController <setTask> activityInstanceId: " + getActivityInstanceId(atr) + " / taskId: " + task.getId());
        this.tt.trace("TaskController.setTask (enter)"); // $NON-NLS$
        this.tt.startTrace("TaskController.setTask"); // $NON-NLS$

        this.atr = atr;
        this.task = task;
        this.mainInput = mainInput;

        releaseView("setTask"); // $NON-NLS$

        initTask();

        this.tt.stopTrace("TaskController.setTask"); // $NON-NLS$
    }

    private static String getActivityInstanceGUID(ActivityTransactionResponse atr) {
        return atr.getInfo().getGUID();
    }

    private static String getActivityInstanceId(ActivityTransactionResponse atr) {
        return atr.getInfo().getId();
    }

    private static String getActivityName(ActivityTransactionResponse atr) {
        return atr.getInfo().getDefinition().getName();
    }

    public void releaseView(String callee) {
        Firebug.debug("<TaskController.releaseView> callee: " + callee);
        if (this.spsRootWidget == null) {
            return;
        }

        if (!this.tt.isRunning()) {
            this.tt.start();
        }
        this.tt.trace("TaskController.releaseView (enter) callee: " + callee); // $NON-NLS$
        this.tt.startTrace("TaskController.releaseView"); // $NON-NLS$
        saveSpsWidgetViewState();
        releaseSpsWidgets();

        this.spsRootWidget = null;

        this.tt.stopTrace("TaskController.releaseView"); // $NON-NLS$
    }

    public void releaseAndClearView() {
        if (!this.tt.isRunning()) {
            this.tt.start();
        }
        this.tt.startTrace("TaskController.releaseAndClearView"); // $NON-NLS$
        this.view.setSubmitAction(null);
        this.view.clear();
        releaseView("releaseAndClearView"); // $NON-NLS$
        this.tt.stopTrace("TaskController.releaseAndClearView", "TaskController.releaseAndClearView (incomplete due to scheduleFinally)"); // $NON-NLS$
    }

    public Widget getViewWidget() {
        return this.view.asWidget();
    }

    private IsPrintable findFirstIsPrintable(SpsWidget parent) {
        if (parent instanceof IsPrintable) {
            return ((IsPrintable) parent);
        }
        else {
            if (parent instanceof HasChildrenFeature) {
                for (SpsWidget w : (((HasChildrenFeature) parent).getChildrenFeature().getChildren())) {
                    final IsPrintable isPrintable = findFirstIsPrintable(w);
                    if (isPrintable != null) {
                        return isPrintable;
                    }
                }
            }
        }
        return null;
    }

    public void initTask() {
        this.tt.trace("TaskController.initTask (enter)"); // $NON-NLS$
        if (!this.tt.isTraceStarted("TaskController.initTask")) { // $NON-NLS$
            this.tt.startTrace("TaskController.initTask"); // $NON-NLS$
        }

        this.tt.startTrace("TaskController.initTask: static"); // $NON-NLS$
        this.allowSave = false;
        this.allowImplicitSave = false;

        if (!StringUtil.equals(this.lastTaskId, this.task.getId())) {
            this.viewState.clear();
            this.lastTaskId = this.task.getId();
        }
        this.viewStateFeatures.clear();

        final List<SubmitAction> submitCapabilities = this.atr.getSubmitCapabilities();

        this.allowSave = hasSubmitAction(submitCapabilities, SubmitAction.SA_SAVE);
        this.allowImplicitSave = hasSubmitAction(submitCapabilities, SubmitAction.SA_IMPLICIT_SAVE);

        this.view.setSubmitAction(
                hasSubmitAction(submitCapabilities, SubmitAction.SA_COMMIT)
                        ? SubmitAction.SA_COMMIT
                        : (this.allowSave ? SubmitAction.SA_SAVE : null));

        final SectionDesc formDescRoot = this.atr.getFormDesc().getRoot();
        DataContainerCompositeNode dataDeclRoot = this.atr.getTypeDecls().getRoot();
        DataContainerCompositeNode dataRoot = this.atr.getData().getContainer().getRoot();

        assertRootAvailable(formDescRoot);
        if (dataDeclRoot == null) {
            dataDeclRoot = new DataContainerGroupNode();
        }
        if (dataRoot == null) {
            dataRoot = new DataContainerGroupNode();
        }

        this.tt.stopTrace("TaskController.initTask: static"); // $NON-NLS$
        this.tt.startTrace("TaskController.initTask: mock and log"); // $NON-NLS$
        //these hooks may be used for mocking/testing
        final List<ErrorMM> taskErrors = this.task.getErrors();
        this.preProcessHook.preProcess(formDescRoot, dataDeclRoot, dataRoot, taskErrors);

        if (SessionData.INSTANCE.isUserPropertyTrue("fbSpsTasks")) { // $NON-NLS$
            ActivityLogUtil.logForm("Task", this.atr.getCurrentTask(), this.atr.getFormDesc(), submitCapabilities, dataDeclRoot, dataRoot, taskErrors); // $NON-NLS$
        }

        this.tt.stopTrace("TaskController.initTask: mock and log"); // $NON-NLS$

        this.tt.startTrace("TaskController.initTask: remove caption, create context"); // $NON-NLS$

        removeTopSectionCaption(formDescRoot);

        this.spsContext = new Context(dataDeclRoot, this.mainInput, getActivityInstanceId(this.atr), getActivityInstanceGUID(this.atr), getActivityName(this.atr) + " (" + this.task.getName() + ")", // $NON-NLS$
                this.atr.getCurrentTask(), this.refreshInternalCommand, this.viewState, this.viewStateFeatures);

        if (SessionData.INSTANCE.isUserPropertyTrue("fbValidationMocker")) { // $NON-NLS$
            ValidationMocker.addMocks(formDescRoot, this.spsContext);
        }

        handleRefreshButton(submitCapabilities);

        this.tt.stopTrace("TaskController.initTask: remove caption, create context"); // $NON-NLS$

        this.tt.startTrace("TaskController.initTask: transferDataToProperties"); // $NON-NLS$
        this.spsContext.transferDataToProperties(dataRoot, dataDeclRoot, false);
        this.tt.stopTrace("TaskController.initTask: transferDataToProperties"); // $NON-NLS$

        this.tt.startTrace("TaskController.initTask: create widgets"); // $NON-NLS$

        assert this.spsRootWidget == null : "root widget not null. Has the view been released?";
        this.spsRootWidget = this.spsContext.getEngine().createSpsWidget(formDescRoot);

        final Widget northWidget = this.spsRootWidget.createNorthWidget();
        this.view.setNorthWidget(northWidget);
        // necessary for fixed headers that contain a pie chart. As long as the chart engine is loaded initially,
        // loading the size of the chart is zero. So we have to resize the north widget if the chart engine was
        // loaded and the chart was rendered. This is very tedious so we are just listening for a resize event.
        // See also AS-1281
        if (northWidget instanceof HasResizeHandlers) {
            ((HasResizeHandlers) northWidget).addResizeHandler(new ResizeHandler() {
                @Override
                public void onResize(ResizeEvent event) {
                    view.layoutNorthWidget();
                }
            });
        }

        this.view.setWidgets(this.spsRootWidget.asWidgets());
        this.view.updatePinnedMode();

        this.tt.stopTrace("TaskController.initTask: create widgets"); // $NON-NLS$

        this.tt.startTrace("TaskController.initTask: replayChangeEvents, etc."); // $NON-NLS$
        this.spsContext.replayChangeEvents();

        SpsAfterPropertiesSetEvent.fireAndRemoveHandlers();
        this.spsRootWidget.focusFirst();
        handleErrors(this.task);

        executeSharedDmxmlContextFacades();
        this.tt.stopTrace("TaskController.initTask: replayChangeEvents, etc."); // $NON-NLS$

        this.tt.stopTrace("TaskController.initTask"); // $NON-NLS$
    }

    private void handleRefreshButton(List<SubmitAction> submitCapabilities) {
        final boolean hasSubmitActionRefresh = hasSubmitAction(submitCapabilities, SubmitAction.SA_REFRESH);
        if (hasSubmitActionRefresh) {
            this.refreshChangeHandler = this.spsContext.getRootProp().addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent changeEvent) {
                    view.setSubmitAction(SubmitAction.SA_REFRESH);
                }
            });

            boolean initiallyViewRefreshButton = false;
            for (ErrorMM errorMM : this.task.getErrors()) {
                switch (errorMM.getErrorSeverity()) {
                    case ESV_ERROR:
                        initiallyViewRefreshButton = true;
                        break;
                    default:
                }
            }
            if (initiallyViewRefreshButton) {
                this.view.setSubmitAction(SubmitAction.SA_REFRESH);
            }
        }
    }

    private void executeSharedDmxmlContextFacades() {
        this.spsContext.setActivateSharedDmxmlContextSuppliersByDefault(true);

        final Collection<Supplier<DmxmlContextFacade>> dmxmlSuppliers = this.spsContext.getSharedDmxmlContextFacadeSuppliers();
        Firebug.debug("<TaskController.executeSharedDmxmlContextFacades> number of shared DmxmlContextFacade suppliers: " + dmxmlSuppliers.size());
        for (Supplier<DmxmlContextFacade> dmxmlSupplier : dmxmlSuppliers) {
            final DmxmlContextFacade facade = dmxmlSupplier.get();
            facade.activate();

            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    facade.reload();
                }
            });
        }
    }

    private void saveSpsWidgetViewState() {
        this.tt.trace("TaskController.saveSpsWidgetViewState (enter)"); // $NON-NLS$
        this.tt.startTrace("TaskController.saveSpsWidgetViewState"); // $NON-NLS$
        for (ViewStateFeature viewStateFeature : this.viewStateFeatures) {
            final Map<String, String> widgetViewState = viewStateFeature.saveState();
            final String stateKey = viewStateFeature.getStateKey();
            if (StringUtil.hasText(stateKey) && widgetViewState != null && !widgetViewState.isEmpty()) {
                this.viewState.put(stateKey, widgetViewState);
            }
        }
        this.viewStateFeatures.clear();
        this.tt.stopTrace("TaskController.saveSpsWidgetViewState"); // $NON-NLS$
    }

    private void releaseSpsWidgets() {
        final boolean wasRunning = this.tt.isRunning();
        if (!wasRunning) {
            this.tt.start();
        }
        this.tt.startTrace("TaskController.releaseSpsWidgets"); // $NON-NLS$

        //release and then null if it is not already null
        if (this.spsRootWidget instanceof RequiresRelease) {
            ((RequiresRelease) this.spsRootWidget).release();
        }
        this.spsRootWidget = null;

        this.tt.stopTrace("TaskController.releaseSpsWidgets"); // $NON-NLS$
        if (!wasRunning) {
            this.tt.stop();
        }
    }

    private void handleErrors(ActivityTask task) {
        final List<ErrorMM> errors = task.getErrors();
        if (errors != null && !errors.isEmpty()) {
            final ArrayList<ValidationResponse> responses = new ArrayList<>(errors.size());
            for (final ErrorMM error : errors) {
                // The correlation ID may be explicitly empty, to indicate, that an error does not depend on a
                // data item. It is completely ok, that such errors are not visualized. See
                final String correlationSource = error.getCorrelationSource();
                if (!StringUtil.hasText(correlationSource)) {
                    continue;
                }

                final ValidationResponse res = new ValidationResponse(BindToken.create(correlationSource), new WidgetAction() {
                    @Override
                    public void doIt(SpsWidget widget, ValidationResponse response) {
                        widget.visualizeError(error, true);
                    }
                }, error.getErrorString());
                responses.add(res);
            }
            if (!responses.isEmpty()) {
                ValidationEvent.fire(task.getId(), createTaskLink(task), true, responses);
            }
        }
    }

    private boolean hasSubmitAction(List<SubmitAction> submitCapabilities,
            SubmitAction... actions) {
        for (SubmitAction capability : submitCapabilities) {
            for (SubmitAction action : actions) {
                if (capability == action) {
                    return true;
                }
            }
        }
        return false;
    }

    private void removeTopSectionCaption(SectionDesc rootSection) {
        final String caption = rootSection.getCaption();
        if (CompareUtil.equals(this.task.getName(), caption)) {
            rootSection.setCaption(null);
        }
    }

    private void assertRootAvailable(SectionDesc formDescRoot) {
        if (formDescRoot == null) {
            throw new IllegalStateException("<TaskController.initTask>\n" // $NON-NLS$
                    + "ActivityTaskDeclResponse.ActivityTaskDecl.FormDesc.Root must not be null!\n" // $NON-NLS$
                    + "To prevent this exception from being raised, you must provide non-null FormDesc root element!"); // $NON-NLS$
        }
    }

    public void onImplicitSave(boolean raiseConfirm) {
        this.tt.stop();
        Firebug.info("<TaskController.onImplicitSave> time trace of former request: " + this.tt.toString());
        this.tt.reset();

        Firebug.debug("<TaskController.onImplicitSave> raiseConfirm=" + raiseConfirm);
        if (this.allowImplicitSave && this.spsContext != null) {
            if (this.spsContext.getRootProp().hasChanged() && raiseConfirm) {
                final String title = I18n.I.activity() + " \"" + getActivityName(this.atr) + "\"";
                final SafeHtml message = SafeHtmlUtils.fromSafeConstant(I18n.I.saveTaskViewData(this.task.getName()));
                final Finalizer<Boolean> flagYesPressed = new Finalizer<>(false);
                Dialog.confirm(title, message, new Command() {
                    @Override
                    public void execute() {
                        flagYesPressed.set(true);
                        onSubmit(ActivityNavigationDirection.AND_THIS, SubmitAction.SA_IMPLICIT_SAVE);
                    }
                }).withCloseCommand(new Command() {
                    @Override
                    public void execute() {
                        if (!flagYesPressed.get()) {
                            onImplicitSaveCancelled();
                        }
                    }
                });
            }
            else {
                onSubmit(ActivityNavigationDirection.AND_THIS, SubmitAction.SA_IMPLICIT_SAVE);
            }
        }
    }

    private void onImplicitSaveCancelled() {
        if (this.atr == null) {
            throw new IllegalStateException("ActivityInstanceRequest is null cannot process onImplicitSaveCancelled"); // $NON-NLS$
        }
        this.activityPageController.onImplicitSaveCancelled(getActivityInstanceId(this.atr), this.atr.getCurrentTask());
    }

    @Override
    public void onCancel() {
        logTimeTraceOfCurrentTask("onCancel"); // $NON-NLS$
        releaseView("onCancel"); // $NON-NLS$
        if (this.cancelCommand != null) {
            this.cancelCommand.execute();
        }
    }

    @Override
    public void onPrevious() {
        logTimeTraceOfCurrentTask("onPrevious"); // $NON-NLS$
        onSubmit(ActivityNavigationDirection.AND_PREV, this.allowSave ? SubmitAction.SA_SAVE : null);
    }

    @Override
    public void onRefresh() {
        logTimeTraceOfCurrentTask("onRefresh"); // $NON-NLS$
        onSubmit(ActivityNavigationDirection.AND_THIS, SubmitAction.SA_REFRESH);
    }

    @Override
    public void onSubmit() {
        logTimeTraceOfCurrentTask("onSubmit"); // $NON-NLS$
        if (!this.validatorController.checkForShowStopper(true)) {
            onSubmit(null, SubmitAction.SA_SAVE);
        }
    }

    @Override
    public void onCommit() {
        logTimeTraceOfCurrentTask("onCommit"); // $NON-NLS$
        if (!this.validatorController.checkForShowStopper(true)) {
            onSubmit(null, SubmitAction.SA_COMMIT);
        }
    }

    public void tryToSaveViewData() {
        tryToSaveViewData(null);
    }

    public void tryToSaveViewData(final Command command) {
        if (this.atr == null || this.task == null) {
            Firebug.debug("<TaskController.tryToSaveViewData> either ActivityTransactionResponse or Task is null -> no data saved");
            tryExecuteCommand("<TaskController.tryToSaveViewData> 0 command != null --> execute", command); // $NON-NLS$
            return;
        }
        if (!this.allowImplicitSave) {
            Firebug.debug("<TaskController.tryToSaveViewData> SubmitCapabilities do not contain saImplicitSave -> no data saved");
            tryExecuteCommand("<TaskController.tryToSaveViewData> 1 command != null --> execute", command); // $NON-NLS$
            return;
        }
        if (this.spsContext == null) {
            Firebug.debug("<TaskController.tryToSaveViewData> no spsContext available -> no data saved");
            tryExecuteCommand("<TaskController.tryToSaveViewData> 2 command != null --> execute", command); // $NON-NLS$
            return;
        }
        if (this.spsRootWidget == null) {
            // the SPS widgets have already been released
            Firebug.debug("<TaskController.tryToSaveViewData> no spsRootWidget available -> no data saved");
            tryExecuteCommand("<TaskController.tryToSaveViewData> 3 command != null --> execute", command); // $NON-NLS$
            return;
        }

        Firebug.debug("<TaskController.tryToSaveViewData>");
        logPropertiesTryToSave();
        this.spsRootWidget.updateProperties();
        logPropertiesTryToSave();

        final SpsGroupProperty rootProp = this.spsContext.getRootProp();

        if (rootProp.hasChanged()) {
            final String taskId = this.task.getId();
            final String activityInstanceId = getActivityInstanceId(this.atr);
            Firebug.debug("<TaskController.tryToSaveViewData> 1 root prop has changed --> trySaveData activity ID: " + activityInstanceId + " task ID: " + taskId);
            trySaveData(rootProp, activityInstanceId, taskId, command);
        }
        else {
            tryExecuteCommand("<TaskController.tryToSaveViewData> 4 command != null --> execute", command);  // $NON-NLS$
        }
    }

    private void tryExecuteCommand(String debugMessage, Command command) {
        if (command != null) {
            Firebug.debug(debugMessage);
            command.execute();
        }
    }

    private void logTimeTraceOfCurrentTask(final String methodName) {
        this.tt.stop();
        Firebug.info("<TaskController." + methodName + "> time trace of former request: " + this.tt.toString());
        this.tt.reset();
    }

    public void logPropertiesTryToSave() {
        if (SessionData.INSTANCE.isUserPropertyTrue("fbSpsPropsBeforeTryToSave") &&  // $NON-NLS$
                this.spsContext != null && this.spsContext.getRootProp() != null) {
            onLogProperties();
        }
    }

    private void trySaveData(SpsGroupProperty rootProp, String instanceId, String taskId,
            Command command) {
        if (!this.allowImplicitSave) {
            throw new IllegalStateException("trySaveData(...) although SubmitCapabilities does not contain saSave"); // $NON-NLS$
        }
        submitData(ActivityNavigationDirection.AND_THIS, rootProp, instanceId, taskId, SubmitAction.SA_IMPLICIT_SAVE, true, command);
    }

    private void onSubmit(final ActivityNavigationDirection direction,
            final SubmitAction submitAction) {
        logPropertiesTryToSave();
        getRootWidget().updateProperties();
        logPropertiesTryToSave();
        submitData(direction, this.spsContext.getRootProp(), getActivityInstanceId(this.atr), this.task.getId(), submitAction, false, null);
    }

    private void submitData(final ActivityNavigationDirection direction,
            final SpsGroupProperty rootProp, String activityInstanceId, final String taskId,
            final SubmitAction submitAction, final boolean justSaveData, final Command command) {
        final String traceKey = "TaskController.submitData direction=" + direction + " submitAction=" + submitAction + " justSaveData=" + justSaveData; // $NON-NLS$

        if (!this.tt.isRunning()) {
            this.tt.start();
            this.tt.trace(traceKey);
        }

        this.tt.startTrace(traceKey);

        if (this.refreshChangeHandler != null) {
            this.refreshChangeHandler.removeHandler();
            this.refreshChangeHandler = null;
        }

        Firebug.info("<TaskController.submitData> direction=" + direction + " activityInstanceId=" + activityInstanceId + " taskId=" + taskId + " justSaveData?" + justSaveData + " command?" + (command != null));
        final DmxmlContext context = new DmxmlContext();
        final DmxmlContext.Block<ActivityTransactionResponse> block = context.addBlock("ACT_Transaction"); // $NON-NLS$
        final ActivityTransactionRequest request = new ActivityTransactionRequest();
        block.setParameter(request);
        request.setCustomerDesktopActive(PrivacyMode.isActive());
        request.setInstanceId(activityInstanceId);
        request.setSourceTask(taskId);
        request.setDirection(direction);
        final TaskViewData taskViewData = new TaskViewData();
        request.setData(taskViewData);

        //cf. AS-1539
        if (direction == ActivityNavigationDirection.AND_THIS) {
            request.setTargetTask(taskId);
        }

        final DataContainerDataItems dataItems = new DataContainerDataItems();
        taskViewData.setContainer(dataItems);
        taskViewData.setAction(submitAction);

        rootProp.setChanged(); //enforces root node creation
        final DataContainerGroupNode rootNode = (DataContainerGroupNode) createNode(rootProp, -1, false);
        dataItems.setRoot(rootNode);

        releaseView("submitData direction=" + direction + " submitAction=" + submitAction); // $NON-NLS$

        final AsyncCallback<ResponseType> callback = new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable caught) {
                if (tt.isTraceStarted(traceKey)) {
                    tt.stopTrace(traceKey);
                }
                else {
                    //when and why does this happen?!? (if tt is reset e.g. in deactivate before stop trace was called)
                    tt.trace(traceKey + "..onFailure (expected trace \"" + traceKey + "\" but has not been started"); // $NON-NLS$
                }
                tt.stop();

                Firebug.warn("<TaskController.submitData> failed to submit sps data", caught);

                rootProp.resetChanged(); //we do not want to ask the user to save the data again when we swap to another task.
                if (justSaveData && command != null) {
                    Firebug.debug("<TaskController.submitData> onFailure --> executing command");
                    handleBlockErrors(block);
                    command.execute();
                }
            }

            @Override
            public void onSuccess(ResponseType result) {
                if (tt.isTraceStarted(traceKey)) {
                    tt.stopTrace(traceKey);
                }
                else {
                    //when and why does this happen?!?
                    tt.trace(traceKey + "..onSuccess (expected trace \"" + traceKey + "\" has not been not started"); // $NON-NLS$
                }
                tt.stop();

                rootProp.resetChanged(); //we do not want to ask the user to save the data again when we swap to another task.

                if (justSaveData) {
                    if (command != null) {
                        Firebug.debug("<TaskController.submitData> onSuccess --> executing command");
                        handleBlockErrors(block);
                        command.execute();
                    }
                }
                else {
                    if (submitAction == SubmitAction.SA_REFRESH || submitAction == SubmitAction.SA_IMPLICIT_SAVE) {
                        Firebug.debug("<TaskController.submitData> onSuccess --> calling onSubmitSuccess without firePlaceChangeEvent : " + submitAction);
                        onSubmitSuccess(block, false);
                    }
                    else {
                        Firebug.debug("<TaskController.submitData> onSuccess --> calling onSubmitSuccess with firePlaceChangeEvent : " + submitAction);
                        onSubmitSuccess(block, true);
                    }
                }
            }
        };
        context.issueRequest(callback);
        ActivityLogUtil.logAsGroup("SPS Data submitted", rootNode); // $NON-NLS$
    }

    private DataContainerCompositeNode createCompositeNode(SpsCompositeProperty prop,
            DataContainerCompositeNode node, int index, boolean ignoreChange) {
        final Collection<SpsProperty> children = prop.getChildren();
        for (SpsProperty child : children) {
            final DataContainerNode childNode = createNode(child, index, ignoreChange);
            if (index != -1) {
                index++;
            }
            if (childNode != null) {
                node.getChildren().add(childNode);
            }
        }
        return node;
    }

    private DataContainerNode createLeafNode(SpsLeafProperty prop) {
        final DataContainerLeafNodeDataItem dataItemNode = new DataContainerLeafNodeDataItem();
        dataItemNode.setDataItem(prop.getDataItem());
        return dataItemNode;
    }

    private DataContainerNode createNode(SpsProperty prop, int index, boolean ignoreChange) {
        if (!prop.hasChanged() && !ignoreChange) {
            return null;
        }

        final DataContainerNode node;
        if (prop instanceof SpsGroupProperty) {
            final SpsGroupProperty groupProperty = (SpsGroupProperty) prop;
            final DataContainerGroupNode groupNode = new DataContainerGroupNode();
            groupNode.setNodeGUID(groupProperty.getNodeGUID());
            node = createCompositeNode(groupProperty, groupNode, -1, ignoreChange);
        }
        else if (prop instanceof SpsListProperty) {
            node = createCompositeNode((SpsListProperty) prop, new DataContainerListNode(), 0, true);
        }
        else if (prop instanceof SpsLeafProperty) {
            node = createLeafNode((SpsLeafProperty) prop);
        }
        else {
            throw new IllegalStateException("unhandled property type ('" + prop.getBindToken() + "'): " + prop.getClass().getSimpleName()); // $NON-NLS$
        }

        node.setIsModified(true);
        if (index == -1) {
            node.setNodeLevelName(prop.getBindKey());
        }
        else {
            node.setNodeLevelName(String.valueOf(index));
        }

        return node;
    }

    private void onSubmitSuccess(DmxmlContext.Block<ActivityTransactionResponse> block,
            boolean firePlaceChangeEvent) {
        if (handleBlockErrors(block)) return;
        this.activityPageController.onSubmitSuccess(block.getResult(), firePlaceChangeEvent);
    }

    private boolean handleBlockErrors(DmxmlContext.Block<ActivityTransactionResponse> block) {
        if (!block.isResponseOk()) {
            DebugUtil.displayServerError(block);
            return true;
        }
        if (block.getResult().isAborted()) {
            final List<ErrorMM> errors = block.getResult().getErrors();
            final StringBuilder sb = new StringBuilder();
            for (ErrorMM error : errors) {
                sb.append('\n').append(error.getErrorString());
                MainController.INSTANCE.showError(error.getErrorString());
            }
            final String errMsg = "<TaskController.handleBlockErrors>\n" + sb.toString(); // $NON-NLS$
            if (sb.length() > 0) {
                DebugUtil.logToServer(errMsg);
                Firebug.error(errMsg);
            }
            return true;
        }
        return false;
    }

    void setActionPreviousVisible(boolean visible) {
        this.view.setActionPreviousVisible(visible);
    }

    @Override
    public void onLogProperties() {
        ActivityLogUtil.logAsGroup("Properties", this.spsContext.getRootProp()); // $NON-NLS$
    }

    public SpsWidget getRootWidget() {
        return this.spsRootWidget;
    }

    public Context getSpsContext() {
        return this.spsContext;
    }

    void activate() {
        this.validatorController.activate();
    }

    void deactivate() {
        this.validatorController.deactivate();
    }

    public String getTaskId() {
        if (this.task == null) {
            return null;
        }
        return this.task.getId();
    }

    public static String createTaskLink(ActivityTask task) {
        //create link to target-task. link must be "laundered" before it'll be visualized

        Firebug.info("task.getName(): " + task.getName());
        Firebug.info("task.getId(): " + task.getId());

        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendHtmlConstant("<a href=\"") // $NON-NLS$
                .appendHtmlConstant(MainController.INSTANCE.contextPath).appendHtmlConstant("/").appendHtmlConstant(GuiDefsLoader.getModuleName())
                .appendHtmlConstant("/#") // $NON-NLS$
                .appendHtmlConstant(HistoryToken.Builder.fromCurrent().with(ActivityPageController.PARAM_ACTIVITY_TASK, task.getId()).build().toString())
                .appendHtmlConstant("\">") // $NON-NLS$
                .appendEscaped("Seite: ").appendEscaped(task.getName()) // $NON-NLS$
                .appendHtmlConstant("</a>"); // $NON-NLS$
        return sb.toSafeHtml().asString();
    }

    @Override
    public Widget getPrintView() {
        final IsPrintable isPrintable = findFirstIsPrintable(this.getRootWidget());
        if (isPrintable != null) {
            final HTML printView = new HTML(SafeHtmlUtils.fromTrustedString(isPrintable.getPrintHtml()));
            printView.setStyleName(TaskDisplay.SPS_TASK_VIEW_STYLE);
            return printView;
        }

        final SectionDesc formDescRoot = this.atr.getFormDesc().getRoot();
        final DataContainerCompositeNode dataDeclRoot = this.atr.getTypeDecls().getRoot();

        final Context printContext = new Context(dataDeclRoot, this.mainInput, getActivityInstanceId(this.atr), getActivityInstanceGUID(this.atr), getActivityName(this.atr) + " (" + this.task.getName() + ")", // $NON-NLS$
                this.task.getId(), this.refreshInternalCommand, (SpsGroupProperty) SpsUtil.clone(this.spsContext.getRootProp(), null), new HashMap<String, Map<String, String>>(), new HashSet<ViewStateFeature>());
        printContext.setForceReadonly(true);
        printContext.setForceDisabledLinksIfReadonly(true);

        if (SessionData.INSTANCE.isUserPropertyTrue("fbSpsTasks")) { // $NON-NLS$
            ActivityLogUtil.logAsGroup("Cloned SPS-Properties", printContext.getRootProp()); // $NON-NLS$
        }

        final SpsWidget rootPrintWidget = printContext.getEngine().createSpsWidget(formDescRoot);

        final FlowPanel printView = new FlowPanel();
        printView.setStyleName(TaskDisplay.SPS_TASK_VIEW_STYLE);
        for (Widget widget : rootPrintWidget.asWidgets()) {
            printView.add(widget);
        }

        printContext.replayChangeEvents();
        SpsAfterPropertiesSetEvent.fireAndRemoveHandlers();

        return printView;
    }

    @Override
    public boolean isPrintable() {
        return true;
    }

    public TaskController withCancelCommand(Command cancelCommand) {
        if (cancelCommand != null) {
            if (this.view instanceof SimpleTaskView) {
                ((SimpleTaskView) this.view).withCancelButton();
            }
        }
        this.cancelCommand = cancelCommand;
        return this;
    }

    /**
     * Ensures that the position of a SPS widget is visible in the TaskView.
     *
     * FF ESR 31 and FF ESR 38 do not automatically scroll the widget that gains the focus via JavaScript
     * calls into view, as IE 9, 10, 11, and Chrome do. Moreover it preserves any scroll positions from
     * the previous page views. Hence, it is necessary to scroll the widget that should gain the focus into
     * view by ourselves. See AS-1263 for further details.
     */
    public void ensureVisible(SpsWidget spsWidget) {
        this.view.ensureVisible(spsWidget);
    }
}