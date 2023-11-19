/*
 * EvaluationController.java
 *
 * Created on 14.04.14
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.notification.NotificationEvent;
import de.marketmaker.itools.gwtutil.client.widgets.notification.NotificationHandler;
import de.marketmaker.itools.gwtutil.client.widgets.notification.NotificationMessage;
import de.marketmaker.itools.gwtutil.client.widgets.notification.Notifications;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.Dimension;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmInvalidSessionException;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config.Config;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config.DatabaseIdConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config.DatabaseObjectConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config.GlobalConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config.HandleConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config.SecurityIdConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.ArchiveData;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.AsyncArchive;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.AsyncData;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.AsyncHandleResult;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.AsyncStateResult;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.InvalidJobStateException;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.PmAsyncManager;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.PmAsyncEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.PmAsyncEventFilter;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.PmAsyncHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.DatabaseId;
import de.marketmaker.iview.pmxml.DatabaseIdQuery;
import de.marketmaker.iview.pmxml.DatabaseObject;
import de.marketmaker.iview.pmxml.DatabaseObjectQuery;
import de.marketmaker.iview.pmxml.EvalLayoutChartRequest;
import de.marketmaker.iview.pmxml.EvalLayoutRequest;
import de.marketmaker.iview.pmxml.Key;
import de.marketmaker.iview.pmxml.Language;
import de.marketmaker.iview.pmxml.LayoutDesc;
import de.marketmaker.iview.pmxml.MMLayoutType;
import de.marketmaker.iview.pmxml.Parameter;
import de.marketmaker.iview.pmxml.QueryStandardSelection;
import de.marketmaker.iview.pmxml.ShellMMType;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.AnalysisUtil.getLayoutGuid;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.AnalysisUtil.getLayoutName;

/**
 * @author mloesch
 */
public class EvaluationController {

    public interface View {
        String ASYNC_HANDLE_MARKER = "vwd:async-handle"; // $NON-NLS$

        void updateAsyncHandleMarker(String asyncHandle);

        void startProgress(String header, String progressText);

        void setProgress(String progressText, int progress);

        void showError(LayoutDesc layoutDescEx1, String... messages);
    }

    public static class Builder {
        private final ContentSizeProvider sizeProvider;

        public Builder(ContentSizeProvider contentSizeProvider) {
            this.sizeProvider = contentSizeProvider;
        }

        public EvaluationController build(EvalLayoutChartRequest chartRequest, EvaluationController.View view,
                Map<String, Parameter> analysisParameters, JobDoneCommand cmd) {
            return new EvaluationController(this.sizeProvider, analysisParameters, chartRequest, view, cmd);
        }
    }

    private final View view;

    private final JobDoneCommand cmd;

    private final ContentSizeProvider sizeProvider;

    private final Map<String, Parameter> analysisParameters;

    private final EvalLayoutChartRequest chartRequest;

    private Config config;

    private LayoutDesc layoutDesc;

    private HandlerRegistration asyncRegistration;

    private ArchiveData archiveData = null;

    private ArchiveData.ContentType contentType;

    private Language language;

    private HashSet<IgnorableAsyncCallback> runningRequests = new HashSet<>();

    public EvaluationController(ContentSizeProvider contentSizeProvider, Map<String, Parameter> analysisParameters,
            EvalLayoutChartRequest chartRequest, View view, JobDoneCommand cmd) {
        this.chartRequest = chartRequest;
        this.view = view;
        this.cmd = cmd;
        this.sizeProvider = contentSizeProvider;
        this.analysisParameters = analysisParameters;
    }


    public void register(final String handle, final LayoutDesc layoutDesc) {
        final IgnorableAsyncCallback<AsyncStateResult> stateResultCallback = new IgnorableAsyncCallback<AsyncStateResult>("Source: EvaluationController.register; handle=" + handle + " layoutDesc=" + AnalysisUtil.getLayoutGuid(layoutDesc)) {  // $NON-NLS$
            @Override
            public void doOnFailure(Throwable caught) {
                if (caught instanceof PmInvalidSessionException) {
                    Firebug.warn("<EvaluationController.register> invalid pm session exception. debugging out.");
                    DebugUtil.logToServer("EvaluationController <register> PmInvalidSessionException -> forcelogout");
                    AbstractMainController.INSTANCE.logoutExpiredSession();
                    return;
                }
                if (caught instanceof InvalidJobStateException) {
                    final String state = ((InvalidJobStateException) caught).getState();
                    view.showError(layoutDesc, I18n.I.invalidAsyncJobState(state));
                    Firebug.warn("<EvaluationController.register> requested handle (" + handle + ") has an invalid state (" + state + ")");
                    return;
                }
                Firebug.warn("<EvaluationController.register> cannot request evaluation", caught);
            }

            @Override
            public void doOnSuccess(AsyncStateResult result) {
                final ArchiveData.ContentType ct = getContentType(layoutDesc.getLayout().getLayoutType());
                if (!result.isFinished()) {
                    startProgress(result, ct, layoutDesc, language);
                }
                else {
                    cmd.onResult(new ArchiveData(ct, result.getHandle(), layoutDesc, result.getObjectName()));
                }
            }
        };

        ignoreRunningRequestsButAddNew(stateResultCallback);

        PmAsyncManager.getInstance().registerHandle(handle, stateResultCallback);
    }

    public void eval(Config config, LayoutDesc layoutDesc, Language language) {
        Firebug.debug("<EvaluationController.eval> config=" + config + " layoutDesc=" + getLayoutGuid(layoutDesc) + " " + getLayoutName(layoutDesc) + (language != null ? " " + language.getLanguageShort() : ""));
        this.config = config;
        this.layoutDesc = layoutDesc;
        this.language = language;
        doEvalRequest();
    }

    private void doEvalRequest() {
        view.updateAsyncHandleMarker(null);

        final EvalLayoutRequest req = createEvalRequest();
        final IgnorableAsyncCallback<AsyncHandleResult> handleResultCallback = new IgnorableAsyncCallback<AsyncHandleResult>("Source: doEvalRequest; " + this.config.toString()) { // $NON-NLS$
            @Override
            public void doOnFailure(Throwable caught) {
                view.updateAsyncHandleMarker("failure " + caught);  // $NON-NLS$
                if (caught instanceof PmInvalidSessionException) {
                    Firebug.warn("<EvaluationController.doEvalRequest> invalid pm session exception. debugging out.");
                    DebugUtil.logToServer("<EvaluationController.doEvalRequest> PmInvalidSessionException -> forcelogout");
                    AbstractMainController.INSTANCE.logoutExpiredSession();
                    return;
                }
                Firebug.warn("<EvaluationController.doEvalRequest> cannot request evaluation", caught);
            }

            @Override
            public void doOnSuccess(AsyncHandleResult asyncHandleResult) {
                view.updateAsyncHandleMarker(asyncHandleResult.getHandle());
                startProgress(asyncHandleResult, contentType, layoutDesc, language);
            }
        };

        ignoreRunningRequestsButAddNew(handleResultCallback);

        PmAsyncManager.getInstance().evaluate(req, handleResultCallback);
    }

    private void ignoreRunningRequestsButAddNew(IgnorableAsyncCallback stateResultCallback) {
        for (IgnorableAsyncCallback runningEval : this.runningRequests) {
            runningEval.setIgnore(true);
        }
        this.runningRequests.add(stateResultCallback);
    }

    private EvalLayoutRequest createEvalRequest() {
        if (this.config == null || this.layoutDesc == null) {
            throw new IllegalStateException("Config and layoutDesc must not be null");  // $NON-NLS$
        }

        final MMLayoutType layoutType = this.layoutDesc.getLayout().getLayoutType();
        final EvalLayoutRequest req;
        if (layoutType == MMLayoutType.LTP_CHART) {
            this.contentType = ArchiveData.ContentType.CHART;

            final Dimension d = this.sizeProvider.getContentSize(this.config);
            req = this.chartRequest;
            this.chartRequest.setCustomerDesktopActive(this.config.isPrivacyMode());
            this.chartRequest.setCount("1"); // $NON-NLS$
            this.chartRequest.setOffset(this.config.getHistoryToken().get("offset", "0")); // $NON-NLS$
            this.chartRequest.setBackColor("FFFFFF"); // $NON-NLS$
            this.chartRequest.setColor("FFFFFF"); // $NON-NLS$
            this.chartRequest.setHeight(String.valueOf(d.getHeight() - this.sizeProvider.getToolbarHeight() - 4));
            this.chartRequest.setWidth(String.valueOf(d.getWidth()));
            this.chartRequest.setGenerateChart(true);
        }
        else {
            this.contentType = getContentType(layoutType);
            req = new EvalLayoutRequest();
            if (this.contentType == ArchiveData.ContentType.PDF && this.language != null) {
                req.setLanguage(this.language.getLanguageShort());
            }
        }
        req.setCustomerDesktopActive(this.config.isPrivacyMode());
        req.setLayoutGuid(getLayoutGuid(layoutDesc));
        addAnalysisParameters(req);
        addQuery(this.config, req);
        return req;
    }

    private void addQuery(Config config, EvalLayoutRequest req) {
        if (config instanceof DatabaseObjectConfig) {
            addDatabaseObjectQuery(req, ((DatabaseObjectConfig) config).getDbo());
        }
        else if (config instanceof DatabaseIdConfig) {
            addDatabaseIdQuery(req, config.getId());
        }
        else if (config instanceof SecurityIdConfig) {
            addQueryStandardSelection(req, config.getId());
        }
        else if (config instanceof GlobalConfig) {
            addQueryNull(req);
        }
        else if (config instanceof HandleConfig) {
            final HandleConfig handleConfig = (HandleConfig) config;
            if (handleConfig.hasSourceConfig()) {
                addQuery(handleConfig.getSourceConfig(), req);
            }
            else {
                throw new IllegalStateException("config is a HandleConfig, but has no sourceConfig"); // $NON-NLS$
            }
        }
        else {
            throw new IllegalStateException("config must a DatabaseObjectConfig, DatabaseIdConfig, SecurityIdConfig, " +  // $NON-NLS$
                    "or GlobalConfig, but was " + (config != null ? config.getClass().getSimpleName() : "null")); // $NON-NLS$
        }
    }

    private void addDatabaseObjectQuery(EvalLayoutRequest req, DatabaseObject dbo) {
        final DatabaseObjectQuery doq = new DatabaseObjectQuery();
        doq.getObjs().add(dbo);
        req.setQuery(doq);
    }

    private ArchiveData.ContentType getContentType(MMLayoutType layoutType) {
        if (layoutType == null) {
            throw new IllegalStateException("layoutType is null!"); // $NON-NLS$
        }

        switch (layoutType) {
            case LTP_WEBGUI_FORMULA_GADGET:
                return ArchiveData.ContentType.GADGET;
            case LTP_CRYSTAL:
            case LTP_REPORT_MAPPE:
                return ArchiveData.ContentType.PDF;
            case LTP_CHART:
                return ArchiveData.ContentType.CHART;
            case LTP_TABLE:
            default:
                return ArchiveData.ContentType.TABLE;
        }
    }

    private void addAnalysisParameters(EvalLayoutRequest req) {
        if (this.analysisParameters == null || this.analysisParameters.isEmpty()) {
            req.getParameters().clear();
            return;
        }

        final List<Parameter> parameters = req.getParameters();
        parameters.clear();
        for (String s : this.analysisParameters.keySet()) {
            final Parameter value = this.analysisParameters.get(s);
            if (value == null) {
                continue;
            }
            parameters.add(value);
        }
    }

    private void addQueryNull(EvalLayoutRequest req) {
        req.setQuery(null);
    }

    private void addQueryStandardSelection(EvalLayoutRequest req, String securityId) {
        final QueryStandardSelection query = new QueryStandardSelection();
        query.setDataItemType(ShellMMType.ST_WP);
        final Key key = new Key();
        key.setKey(securityId);
        query.getKeys().add(key);
        req.setQuery(query);
    }

    private void addDatabaseIdQuery(EvalLayoutRequest req, String databaseId) {
        final DatabaseIdQuery query = new DatabaseIdQuery();
        final DatabaseId dbId = new DatabaseId();
        dbId.setId(databaseId);
        query.getIds().add(dbId);
        req.setQuery(query);
    }

    private void startProgress(AsyncHandleResult asyncHandleResult, ArchiveData.ContentType type, LayoutDesc layoutDesc, Language language) {
        Firebug.debug("<EvaluationController.startProgress> config=" + this.config + " asyncHandleResult=" + asyncHandleResult + " layoutDesc=" + getLayoutGuid(layoutDesc) + " " + getLayoutName(layoutDesc));

        final String handle = asyncHandleResult.getHandle();
        final String objectName = asyncHandleResult.getObjectName();
        this.archiveData = AsyncArchive.I.getByHandle(handle);
        if (this.archiveData == null) {
            this.archiveData = new ArchiveData(type, handle, layoutDesc, objectName);
        }
        if (this.asyncRegistration != null) {
            this.asyncRegistration.removeHandler();
        }
        this.asyncRegistration = PmAsyncEventFilter.addFilteredHandler(handle, new PmAsyncHandler() {
            @Override
            public void onAsync(PmAsyncEvent event) {
                onAsyncData(event.getAsyncData());
            }
        });
        this.view.startProgress(layoutDesc.getLayout().getLayoutName() + " - " + objectName, I18n.I.pmEvaluationStarted());
        //Maybe the evaluation is done yet and no event was received because of an awkward timegap
        if (!PmAsyncManager.getInstance().isRunning(this.archiveData.getHandle())) {
            Firebug.debug("<EvaluationController.startProgress> handle " + this.archiveData.getHandle() + " was not running when visualization of progress started! pulling state...");
            PmAsyncManager.getInstance().pullHandleStatus(this.archiveData.getHandle());
        }
    }

    private void onAsyncData(AsyncData asyncData) {
        if (this.archiveData == null || !this.archiveData.getHandle().equals(asyncData.getHandle())) {
            Firebug.debug("<EvaluationController.onAsyncData> asyncData " + asyncData + " -> not current handle " + (this.archiveData == null ? "<null>" : this.archiveData.getHandle()));
            return;
        }
//        Firebug.debug("<EvaluationController.onTableAsync> " + asyncData);
        switch (asyncData.getState()) {
            case STARTED:
                break;
            case PROGRESS:
                this.view.setProgress(I18n.I.pmEvaluationRunning(), asyncData.getProgress());
                break;
            case PAUSED:
                this.view.setProgress(I18n.I.pmEvaluationPaused(), asyncData.getProgress());
                break;
            case FINISHED:
                this.view.setProgress(I18n.I.pmEvaluationReadyLoading(), 100);
                final ArchiveData ad = this.archiveData;
                cleanupAsync();
                cmd.onResult(ad);
                break;
            case ERROR:
                this.view.showError(this.layoutDesc, asyncData.getMessage());
                cleanupAsync();
                break;
        }
    }

    void cleanupAsync() {
        this.archiveData = null;
        removeEventRegistration();
    }

    private void removeEventRegistration() {
        if (this.asyncRegistration != null) {
            this.asyncRegistration.removeHandler();
            this.asyncRegistration = null;
        }
    }

    public void cancelOrCleanupAsync(boolean keepSessionAlive, String callee) {
        if (this.archiveData == null) {
            Firebug.debug("<EvaluationController.cancelOrCleanupAsync> layout archiveData null keepSessionAlive " + keepSessionAlive + " callee " + callee);
            return;
        }

        Firebug.debug("<EvaluationController.cancelOrCleanupAsync> layout " + this.archiveData.getLayoutDesc().getLayout().getGuid() + " handle " + this.archiveData.getHandle() + " keepSessionAlive " + keepSessionAlive + " callee " + callee);

        if (this.archiveData.isInBackground()) {
            cleanupAsync();
        }
        else {
            cancelAsync(keepSessionAlive, callee + " --> EvaluationController.cancelOrCleanupAsync");  // $NON-NLS$
        }
    }

    public void cancelAsync(boolean keepSessionAlive, final String callee) {
        if (this.archiveData == null) {
            Firebug.debug("<EvaluationController.cancelAsync> layout archiveData null keepSessionAlive " + keepSessionAlive);
            return;
        }
        Firebug.debug("<EvaluationController.cancelAsync> layout " + this.archiveData.getLayoutDesc().getLayout().getGuid() + " handle " + this.archiveData.getHandle() + " keepSessionAlive " + keepSessionAlive);

        PmAsyncManager.getInstance().unregisterHandle(this.archiveData.getHandle(), keepSessionAlive, callee + " -> EvaluationController.cancelAsync");  // $NON-NLS$
        cleanupAsync();
        showEvaluationCancelledError();
    }

    private void showEvaluationCancelledError() {
        this.view.showError(this.layoutDesc, I18n.I.pmEvaluationCancelled());
    }

    public String sendAsyncToBackground() {
        if (this.archiveData == null) {
            return null;
        }
        if (this.archiveData.isInBackground()) {
            return this.archiveData.getHandle();
        }

        BackgroundProcessesRepository.INSTANCE.add(new BackgroundProcessImpl().start(this.archiveData));

        this.archiveData.setInBackground(true);
        return this.archiveData.getHandle();
    }

    private class BackgroundProcessImpl implements BackgroundProcess {
        private String handle;

        private NotificationMessage message;

        private HandlerRegistration filteredHandlerRegistration;

        private NotificationHandler userCancelledBackgroundTaskHandler;

        private HandlerRegistration userCancelledBackgroundTaskHandlerRegistration;

        private Label widget;

        private HistoryToken historyToken;

        private String header;

        private double progress = 0d;

        private boolean canceled = false;

        private BackgroundProcessImpl start(final ArchiveData archiveData) {
            this.handle = archiveData.getHandle();
            this.widget = new Label(I18n.I.pmEvaluationRunning());
            this.header = getHeader(archiveData);
            this.userCancelledBackgroundTaskHandler = new NotificationHandler() {
                @Override
                public void onNotification(NotificationEvent event) {
                    //see: addHiddenNotification, removeNotification, BackgroundProcessesRepository#privacyModeStateChanged
                    if (message == event.getMessage() && event.getRequestedState() == NotificationMessage.State.DELETED) {
                        cancel();
                        widget.setText(I18n.I.pmEvaluationCancelled());
                    }
                }
            };
            addHiddenNotification();

            this.filteredHandlerRegistration = PmAsyncEventFilter.addFilteredHandler(archiveData.getHandle(), new PmAsyncHandler() {
                @Override
                public void onAsync(PmAsyncEvent event) {
                    onBackgroundAsync(event.getAsyncData());
                }
            });
            this.historyToken = config.getHistoryToken();
            AsyncArchive.I.add(archiveData);

            return this;
        }

        @Override
        public void addHiddenNotification() {
            this.message = Notifications.addHidden(this.header, this.widget, this.progress, true);
            // add previously removed handler, so that it is possible to cancel a still running background task again
            // cf. BackgroundProcessesRepository#privacyModeStateChanged
            addUserCancelledBackgroundTaskHandler();
        }

        private void addUserCancelledBackgroundTaskHandler() {
            if (this.userCancelledBackgroundTaskHandler != null && this.userCancelledBackgroundTaskHandlerRegistration == null) {
                this.userCancelledBackgroundTaskHandlerRegistration = Notifications.I.addNotificationHandler(this.userCancelledBackgroundTaskHandler);
            }
        }

        @Override
        public void removeNotification() {
            // remove handler, so that we can clearly distinguish DELETED by user click to cancel the background task
            // and by removeNotification when entering privacy mode (background task is not cancelled)
            // cf. BackgroundProcessesRepository#privacyModeStateChanged
            removeUserCancelledBackgroundTaskHandler();
            this.message.requestState(NotificationMessage.State.DELETED);
        }

        @Override
        public boolean isNotificationRemoved() {
            return this.message.getState() == NotificationMessage.State.DELETED;
        }

        @Override
        public boolean isCanceled() {
            return canceled;
        }

        @Override
        public void cancel() {
            this.message.requestState(NotificationMessage.State.DELETED);
            PmAsyncManager.getInstance().unregisterHandle(this.handle, true, "EvaluationController.cancel");  // $NON-NLS$
            this.filteredHandlerRegistration.removeHandler();
            this.userCancelledBackgroundTaskHandlerRegistration.removeHandler();
            this.canceled = true;
        }

        private String getHeader(ArchiveData archiveData) {
            final String objectName = archiveData.getObjectName();
            if (StringUtil.hasText(objectName)) {
                return archiveData.getLayoutDesc().getLayout().getLayoutName() + " - " + objectName;
            }
            return archiveData.getLayoutDesc().getLayout().getLayoutName();
        }

        private void onBackgroundAsync(final AsyncData asyncData) {
            switch (asyncData.getState()) {
                case STARTED:
                    break;
                case PROGRESS:
                    this.progress = (double) asyncData.getProgress() / 100d;
                    this.message.setProgress(this.progress);
                    this.widget.setText(I18n.I.pmEvaluationProgress(asyncData.getProgress()));
                    AsyncArchive.I.getByHandle(asyncData.getHandle()).setProgress(asyncData.getProgress());
                    break;
                case PAUSED:
                    this.widget.setText(I18n.I.pmEvaluationPaused());
                    break;
                case FINISHED:
                    this.progress = 1d;
                    this.message.setProgress(this.progress);
                    final MmJsDate finishedDate = AnalysisController.toMmJsDate(asyncData.getFinishedDate());
                    final String jobCompletedString = finishedDate != null ? JsDateFormatter.formatHhmmss(finishedDate) : "";  // $NON-NLS$
                    this.widget.setText(I18n.I.pmEvaluationReadyKlick(jobCompletedString));
                    this.widget.setStyleName("mm-link");
                    this.widget.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            onBackgroundClick(historyToken, asyncData);
                            message.requestState(NotificationMessage.State.FORCE_HIDDEN);
                        }
                    });
                    this.filteredHandlerRegistration.removeHandler();
                    removeUserCancelledBackgroundTaskHandler();
                    this.message.requestState(NotificationMessage.State.VISIBLE);
                    break;
                case ERROR:
                    this.progress = 1d;
                    this.message.setProgress(this.progress);
                    this.widget.setText(asyncData.getMessage());
                    this.filteredHandlerRegistration.removeHandler();
                    removeUserCancelledBackgroundTaskHandler();
                    break;
            }
        }

        private void removeUserCancelledBackgroundTaskHandler() {
            if (this.userCancelledBackgroundTaskHandlerRegistration != null) {
                this.userCancelledBackgroundTaskHandlerRegistration.removeHandler();
                this.userCancelledBackgroundTaskHandlerRegistration = null;
            }
        }

        HistoryToken getHistoryToken() {
            return historyToken;
        }

        @Override
        public String toString() {
            return "BackgroundProcessImpl{" +     // $NON-NLS$
                    "handle='" + handle + '\'' +  // $NON-NLS$
                    ", canceled=" + canceled +  // $NON-NLS$
                    ", header='" + header + '\'' +  // $NON-NLS$
                    ", historyToken=" + historyToken +  // $NON-NLS$
                    '}';
        }
    }

    public void onBackgroundClick(HistoryToken historyToken, AsyncData asyncData) {
        HistoryToken.Builder.fromHistoryToken(historyToken).with(AnalysisController.HKEY_HANDLE, asyncData.getHandle()).fire();
    }

    private abstract class IgnorableAsyncCallback<T extends AsyncHandleResult> implements AsyncCallback<T> {
        private boolean ignore = false;

        private String debugName;

        public abstract void doOnFailure(Throwable caught);

        public abstract void doOnSuccess(T result);

        public IgnorableAsyncCallback(String debugName) {
            this.debugName = debugName;
        }

        public boolean isIgnore() {
            return this.ignore;
        }

        public void setIgnore(boolean ignore) {
            this.ignore = ignore;
        }

        @Override
        public final void onFailure(Throwable caught) {
            EvaluationController.this.runningRequests.remove(this);
            doOnFailure(caught);
        }

        @Override
        public final void onSuccess(T result) {
            EvaluationController.this.runningRequests.remove(this);
            if (isIgnore()) {
                Firebug.info("<IgnorableAsyncCallback.onSuccess> ignored:  " + this.debugName);
                return;
            }
            doOnSuccess(result);
        }
    }
}