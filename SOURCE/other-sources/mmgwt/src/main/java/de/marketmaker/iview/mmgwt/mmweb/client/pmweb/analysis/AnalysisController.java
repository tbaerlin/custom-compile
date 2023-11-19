package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.marketmaker.itools.gwtutil.client.util.CompareUtil;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.notification.NotificationMessage;
import de.marketmaker.itools.gwtutil.client.widgets.notification.Notifications;
import de.marketmaker.iview.dmxml.ErrorType;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.Dimension;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.MainController;
import de.marketmaker.iview.mmgwt.mmweb.client.MainView;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.as.AsMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.GetStateKeyException;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryItem;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryThreadManager;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ThreadStateHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ThreadStateSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PageLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingWidgets;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DTTableRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config.Config;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config.DatabaseIdConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config.HandleConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.ArchiveData;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.AbstractDepotObjectPortraitController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.dms.DmsDisplay;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.dms.DmsMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.dms.DmsPresenter;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.dttablefilter.FilterData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.UrlBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.view.HasNavWidget;
import de.marketmaker.iview.pmxml.DMSMoveFromAsyncResponse;
import de.marketmaker.iview.pmxml.DMSMoveFromAsyncSingleRequest;
import de.marketmaker.iview.pmxml.DTTable;
import de.marketmaker.iview.pmxml.DocumentOrigin;
import de.marketmaker.iview.pmxml.EvalLayoutChartRequest;
import de.marketmaker.iview.pmxml.EvalLayoutChartResponse;
import de.marketmaker.iview.pmxml.EvalLayoutGadgetResponse;
import de.marketmaker.iview.pmxml.EvalLayoutResponse;
import de.marketmaker.iview.pmxml.EvalLayoutTableResponse;
import de.marketmaker.iview.pmxml.GetStateResponse;
import de.marketmaker.iview.pmxml.HandleRequest;
import de.marketmaker.iview.pmxml.Language;
import de.marketmaker.iview.pmxml.LayoutDesc;
import de.marketmaker.iview.pmxml.Parameter;
import de.marketmaker.iview.pmxml.QueryResponseState;
import de.marketmaker.iview.pmxml.UMRightBody;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.AnalysisUtil.getLayoutGuid;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.AnalysisUtil.getLayoutName;

/**
 * Created on 29.08.13 08:59
 * Copyright (c) market maker Software AG. All Rights Reserved.
 * @author Michael Lösch
 */

public class AnalysisController implements AnalysisDisplay.Presenter, HasNavWidget, ThreadStateHandler {
    private static final String ANALYSIS_CONTROLLER_STATE_KEY = "ACS"; // $NON-NLS$

    private static final int TOOLBAR_HEIGHT = 33;

    public static final String HKEY_HANDLE = "handle"; // $NON-NLS$

    private static final String ANALYSIS_HEADLINE_KEY = "AC:HEAD"; // $NON-NLS$

    private final EvalLayoutChartRequest chartRequest = new EvalLayoutChartRequest();

    private final EvaluationController evaluationController;

    private LayoutDesc layoutDescEx;

    private DTTableRenderer.Options dtTableOptions = null;

    private final HashMap<String, String> analysisParametersOfMetadataForm = new HashMap<>();

    private final PagingFeature chartPagingFeature;

    private final AnalysisDisplay<AnalysisDisplay.Presenter> view;

    private Config config;

    private final HeaderHelper headerHelper = new HeaderHelper();

    private final String stateKeyPrefix;

    private String lastFinishedHandle;

    private final DmsPresenter dmsPresenter;

    private Language selectedLanguage;

    private Map<String, Parameter> analysisParameters = new HashMap<>();

    private Integer maxDiagramIdx = null;

    private Map<Integer, FilterData> filterData = null;

    private ThreadStateSupport threadStateSupport;

    private class ChartBlockListTypePager implements PagingFeature.Pager {
        private final DmxmlContext.Block<EvalLayoutChartResponse> block;

        private ChartBlockListTypePager(DmxmlContext.Block<EvalLayoutChartResponse> block) {
            this.block = block;
        }

        @Override
        public void setOffset(int offset) {
            chartRequest.setOffset(String.valueOf(offset));
        }

        @Override
        public void setCount(int count) {
            chartRequest.setCount(String.valueOf(count));
        }

        @Override
        public int getTotal() {
            return Integer.parseInt(this.block.getResult().getCompleteCount());
        }

        @Override
        public int getOffset() {
            return Integer.parseInt(this.block.getResult().getOffset());
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public boolean isResponseOk() {
            return this.block != null && this.block.isResponseOk() && this.block.getResult() != null;
        }
    }

    private class ChartPageLoader implements PageLoader {
        @Override
        public void reload() {
            HistoryToken.Builder.fromHistoryToken(config.getHistoryToken())
                    .with("offset", chartRequest.getOffset()) // $NON-NLS$
                    .fire();
        }
    }

    public AnalysisController(String stateKeyPrefix) {
        this(new AnalysisView<>(), stateKeyPrefix);
    }

    public AnalysisController(AnalysisDisplay<AnalysisDisplay.Presenter> display) {
        this(display, null);
    }

    public AnalysisController(AnalysisDisplay<AnalysisDisplay.Presenter> display, String stateKeyPrefix) {
        this(display, new EvaluationController.Builder(new ContentSizeProvider() {
            @Override
            public Dimension getContentSize(Config config) {
                return AnalysisController.getContentSize(config);
            }

            @Override
            public int getToolbarHeight() {
                return TOOLBAR_HEIGHT;
            }
        }), stateKeyPrefix);
    }

    public AnalysisController(EvaluationController.Builder ecBuilder, String stateKeyPrefix) {
        this(new AnalysisView<>(), ecBuilder, stateKeyPrefix);
    }

    public AnalysisController(AnalysisDisplay<AnalysisDisplay.Presenter> display, EvaluationController.Builder ecBuilder,
            String stateKeyPrefix) {
        this.stateKeyPrefix = stateKeyPrefix;
        if (this.stateKeyPrefix != null) {
            this.threadStateSupport = new ThreadStateSupport(this, false);
        }
        this.view = display;
        this.view.setPresenter(this);
        this.dmsPresenter = new DmsPresenter();

        this.view.setDmsPresenter(this.dmsPresenter);
        this.evaluationController = ecBuilder.build(this.chartRequest, this.view, this.analysisParameters, new JobDoneCommand() {
            @Override
            public void onResult(ArchiveData result) {
                Firebug.info("<AnalysisController..JobDoneCommand.onResult> layoutDescEx=" + getLayoutGuid(result) + " " + getLayoutName(result) + " handle=" + result.getHandle() + " objectName=" + result.getObjectName());
                lastFinishedHandle = result.getHandle();
                requestEvalResult(result);
            }
        });

        final PagingWidgets cpw = this.view.getChartPagingWidgets();
        if (cpw != null) {
            this.chartPagingFeature = new PagingFeature(new ChartPageLoader(), new ChartBlockListTypePager(null), 1);
            this.chartPagingFeature.setPagingWidgets(cpw);
        }
        else {
            this.chartPagingFeature = null;
        }
        this.chartRequest.setCount("1"); // $NON-NLS$
        this.chartRequest.setOffset("0"); // $NON-NLS$

        this.view.setChartPagingWidgetsVisible(false);
    }

    public void destroy() {
        if (this.dmsPresenter != null) {
            this.dmsPresenter.destroy();
        }
    }

    private void updateConfig(Config config, AnalysisMetadataCallback callback) {
        this.config = config;
        clearParams();
        requestLayoutDesc(config, callback);
    }

    private void clearParams() {
        Firebug.warn("<AnalysisController.clearParams>");
        this.analysisParameters.clear();
        this.dtTableOptions = null;
        this.filterData = null;
        this.maxDiagramIdx = null;
        this.selectedLanguage = null;
        syncAnalysisParameterMaps();
        syncSelectedLanguage();
    }

    private LayoutDesc getLayoutDesc() {
        if (this.config == null) {
            throw new IllegalStateException("config must not be null!"); // $NON-NLS$
        }
        if (this.layoutDescEx == null || this.layoutDescEx.getLayout() == null) {
            throw new IllegalStateException("layoutDesc == null || this.layoutDesc.getLayout() == null"); // $NON-NLS$
        }
        final String configlayoutGuid = this.config.getLayoutGuid();
        final String currentLayoutGuid = this.layoutDescEx.getLayout().getGuid();
        if (!currentLayoutGuid.equals(configlayoutGuid)) {
            throw new IllegalStateException("Guids unequal! layoutDesc.guid == " + currentLayoutGuid + ";" + // $NON-NLS$
                    "configuration.layoutGuid == " + configlayoutGuid); // $NON-NLS$
        }
        return this.layoutDescEx;
    }

    @Override
    public void setAnalysisParametersOfMetadataForm(Map<String, String> analysisParameters) {
        setAnalysisParametersOfMetadataForm(analysisParameters, getLayoutDesc());
        evaluate();
    }

    private void setAnalysisParametersOfMetadataForm(Map<String, String> analysisParameters, LayoutDesc layoutDesc) {
        Firebug.debug("<AnalysisController.setAnalysisParametersOfMetadataForm> analysisParameters<String, String>=" + analysisParameters.toString());
        this.analysisParametersOfMetadataForm.clear();
        this.analysisParametersOfMetadataForm.putAll(analysisParameters);
        applyAnalysisParameters(MmTalkHelper.toParameterMap(this.analysisParametersOfMetadataForm, layoutDesc), layoutDesc);
        AnalysisMetadataHandler.getInstance().putConfig(layoutDesc, this.analysisParameters, this.filterData, this.dtTableOptions, this.maxDiagramIdx, this.selectedLanguage);
    }

    public void applyAnalysisParameters(Map<String, Parameter> mapParameters, LayoutDesc layoutDesc) {
        applyAnalysisParameters(mapParameters, layoutDesc, false);
    }

    public void applyAnalysisParameters(Map<String, Parameter> mapParameters, LayoutDesc layoutDesc, boolean saveParams) {
        if (this.layoutDescEx == null) {
            this.layoutDescEx = layoutDesc;
        }
        this.analysisParameters.clear();
        this.analysisParameters.putAll(mapParameters);
        logAsGroup("AnalysisController <applyAnalysisParameters> layout='" + getLayoutName(layoutDesc) + "'", this.analysisParameters); // $NON-NLS$
        if (saveParams) {
            AnalysisMetadataHandler.getInstance().putConfig(layoutDesc, this.analysisParameters, this.filterData, this.dtTableOptions, this.maxDiagramIdx, this.selectedLanguage);
        }
    }

    public static void logAsGroup(String description, Map<String, Parameter> map) {
        if (map == null) {
            Firebug.group(description);
            return;
        }
        final String[] s = new String[map.size()];
        int i = 0;
        for (Map.Entry<String, Parameter> entry : map.entrySet()) {
            s[i++] = entry.getKey() + ": " + (entry.getValue() == null ? "null" : MmTalkHelper.toLogString(entry.getValue()));  // $NON-NLS$
        }
        Firebug.group(description, s);
    }

    public void reevaluate() {
        if (this.config != null) {
            evaluate();
        }
    }

    private void evaluate() {
        this.lastFinishedHandle = null;

        this.view.clear();
        this.evaluationController.eval(this.config, getLayoutDesc(), this.selectedLanguage);
    }

    public void showError(String message) {
        this.view.showError(this.layoutDescEx, message);
        this.view.setAggregationButtonVisible(false);
        this.view.setSortOrderButtonVisible(false);
        this.view.setChartPagingWidgetsVisible(false);
        this.view.setXlsExportButtonVisible(false);
    }

    public void fetch(final HandleConfig config) {
        this.lastFinishedHandle = null;

        Firebug.info("<AnalysisController.fetch> config=" + config);

        this.evaluationController.cancelOrCleanupAsync(true, "AnalysisController.fetch");  // $NON-NLS$
        updateConfig(config, new AnalysisMetadataCallback() {
            @Override
            public void onMetadataAvailable(LayoutDesc layoutDescEx, String jsonConfig) {
                Firebug.warn("<AnalysisController.fetch..onMetdadataAvailable> json = " + jsonConfig);
                view.clear();
                applyParamsFromJson(layoutDescEx, jsonConfig);
                syncAnalysisParameterMaps();
                syncSelectedLanguage();
                evaluationController.register(config.getId(), layoutDescEx);
            }
        });
    }

    public void evaluate(final Config config, Map<String, String> params) {
        evaluate(config, false, params);
    }

    public void evaluate(final Config config) {
        evaluate(config, false, null);
    }

    public void evaluate(final Config config, boolean force) {
        evaluate(config, force, null);
    }

    private void evaluate(final Config config, boolean force, final Map<String, String> params) {
        Firebug.info("<AnalysisController.evaluate> config=" + config + " force=" + force);
        if (config == null) {
            throw new IllegalArgumentException("config must not be null"); // $NON-NLS$
        }
        if (this.config != null && this.config.equals(config) && !force) {
//            Firebug.debug("<AnalysisController.evaluate> current and new config are equal. Nothing to do. Config=" + config + " force=" + false);
            setHeaderFromHistory();
            return;
        }
//        Firebug.debug("<AnalysisController.evaluate> going further on... config=" + config + " force=" + force);
        this.lastFinishedHandle = null;
        this.evaluationController.cancelOrCleanupAsync(true, "AnalysisController.evaluate");  // $NON-NLS$
        updateConfig(config, new AnalysisMetadataCallback() {
            @Override
            public void onMetadataAvailable(LayoutDesc layoutDescEx, String jsonConfig) {
                Firebug.debug("<AnalysisController.evaluate..onMetadataAvailable> json = " + jsonConfig);
                if (params != null) {
                    applyAnalysisParameters(MmTalkHelper.toParameterMap(params, layoutDescEx), layoutDescEx);
                }
                else {
                    applyParamsFromJson(layoutDescEx, jsonConfig);
                }
                syncAnalysisParameterMaps();
                syncSelectedLanguage();
                evaluate();
//                Firebug.debug("<AnalysisController.evaluate..evaluate> config=" + config + " force=" + force);
            }
        });
    }

    private void syncAnalysisParameterMaps() {
        final Map<String, String> analysisParametersOfMetadataForm = this.analysisParametersOfMetadataForm;
        logAsGroup("<AnalysisController.syncAnalysisParametersMaps>", this.analysisParameters);  // $NON-NLS$
        if (this.analysisParameters == null || this.analysisParameters.isEmpty()) {
            this.analysisParametersOfMetadataForm.clear();
            return;
        }
        for (String s : this.analysisParameters.keySet()) {
            final Parameter parameter = this.analysisParameters.get(s);
            final Map<String, String> param = MmTalkHelper.paramAsStringMap(parameter);
            final String paramForm = analysisParametersOfMetadataForm.get(s);
            if (param.isEmpty() && paramForm == null) {
                continue;
            }
            if (!param.isEmpty() && !StringUtil.equals(param.get(s), paramForm)) {
                setAnalysisParametersOfMetadataForm(MmTalkHelper.toStringMap(this.analysisParameters), getLayoutDesc());
                return;
            }
        }
    }

    private void syncSelectedLanguage() {
        Firebug.debug("<AnalysisController.syncSelectedLanguage> language=" + getLangLong()); // $NON-NLS$
        this.view.setSelectedLanguage(this.selectedLanguage);
    }

    private void requestLayoutDesc(final Config config, final AnalysisMetadataCallback callback) {
        this.view.setConfigButtonEnabled(false);
        AnalysisMetadataHandler.getInstance().getMetadata(config.getLayoutGuid(), new AnalysisMetadataCallback() {
            @Override
            public void onMetadataAvailable(LayoutDesc layoutDescEx, String jsonConfig) {
                if (!CompareUtil.equals(config, AnalysisController.this.config)) {
                    Firebug.info("<AnalysisController.requestLayoutDesc..onMetadataAvailable> requested config does not match current config; do nothing. \n Requested: " + config + " \n Current: " + AnalysisController.this.config);
                    return;
                }

                AnalysisController.this.layoutDescEx = layoutDescEx;
                final boolean configButtonEnabled = !(
                        layoutDescEx.getParameters() != null && layoutDescEx.getParameters().isEmpty()
                                || (config instanceof HandleConfig && !((HandleConfig) config).hasSourceConfig())
                );
                AnalysisController.this.view.setConfigButtonEnabled(configButtonEnabled);

                AnalysisController.this.view.setLanguages(PmWebSupport.getInstance().getLanguages());

                AnalysisController.this.view.setSelectedLanguage(AnalysisController.this.selectedLanguage);

                Firebug.debug("<AnalysisController.requestLayoutDesc..onMetadataAvailable> selectedLanguage = " + getLangLong());

/*
                if (configButtonEnabled) {
                    applyParamsFromJson(layoutDescEx, jsonConfig);
                }
*/
                if (callback != null) {
                    callback.onMetadataAvailable(layoutDescEx, jsonConfig);
                }
            }
        });
    }

    private static Dimension getContentSize(Config config) {
        final int chartHeight = config.getChartHeight();
        final int chartWidth = config.getChartWidth();

        if (chartHeight > -1 && chartWidth > -1) {
            return new Dimension(chartWidth, chartHeight);
        }
        return ((MainView) AsMainController.INSTANCE.getView()).getContentSize();
    }

    @Override
    public void onShowReportSettings() {
        this.view.showReportSettings(Collections.unmodifiableMap(this.analysisParametersOfMetadataForm), null);
    }

    @Override
    public void setFilterData(Map<Integer, FilterData> filterData) {
        this.filterData = filterData;
    }

    @Override
    public void xlsExport(String handle) {
        if (handle != null) {
            Window.open(UrlBuilder.forPmReport("pmweb/report?handle=" + handle).toURL(), "_blank", ""); // $NON-NLS$
        }
    }

    @Override
    public void archive(final String layoutName, final String handle, final String layoutType, final String title, final String comment,
            final DmsMetadata metadata) {
        if (!(this.config instanceof DatabaseIdConfig) && !(this.config instanceof HandleConfig)) {
            Firebug.error("could not archive document because of wrong config type. Expected DatabaseIdConfig or HandleConfig, found " + this.config.getClass().getSimpleName());
            return;
        }

        final DMSMoveFromAsyncSingleRequest req = new DMSMoveFromAsyncSingleRequest();
        req.setCopyNotMove(true);
        req.setFilterOrigin(DocumentOrigin.DO_ASYNC);
        req.setAllowCcDms(true);
        req.setIDType(metadata.getType());
        req.setID(metadata.getIdentifier());
        req.setWriteOnlyZonename(metadata.getZone());
        req.setCreatedBy(SessionData.INSTANCE.getUser().getLogin());
        if (handle != null) {
            req.setFilterHandle(handle);
        }
        req.setComment(comment);
        req.setDocumentName(title);
        req.setLayoutName(layoutName);
        if (StringUtil.hasText(layoutType)) {
            req.setDocumentType(layoutType);
        }

        final DmxmlContext context = new DmxmlContext();
        context.setCancellable(false);
        final DmxmlContext.Block<DMSMoveFromAsyncResponse> dmsBlock = context.addBlock("DMS_MoveFromAsync"); // $NON-NLS$
        dmsBlock.setParameter(req);
        dmsBlock.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable throwable) {
                DebugUtil.logToServer("could not archive document", throwable); // $NON-NLS$
                Dialog.error(I18n.I.dmsArchiveDocumentErrorAny());
            }

            @Override
            public void onSuccess(ResponseType responseType) {
                if (dmsBlock.getResult().isCCError()) {
                    // Means: DMS file copy is configured in PM but failed. Therefore, the whole archiving process of
                    // the document failed. The message is only a hint for us if customers are sending screenshots to
                    // get an idea of the error as long as we do not have PM log files.
                    Dialog.error(I18n.I.dmsArchiveDocumentErrorCC());
                }
                else if (dmsBlock.getResult().isInsufficientRights()) {
                    Dialog.error(I18n.I.dmsArchiveDocumentErrorInsufficientRights());
                }
                else {
                    Dialog.info(I18n.I.dmsArchiveDocumentSuccess());
                    dmsPresenter.update();
                    dmsPresenter.layoutWhenUpdateDone();
                }
            }
        });
    }

    @Override
    public void requestNavWidget(NavWidgetCallback callback) {

    }

    private void requestGadgetResult(final ArchiveData archiveData) {
        final DmxmlContext dmxmlContext = new DmxmlContext();
        final DmxmlContext.Block<EvalLayoutGadgetResponse> resultBlock = dmxmlContext.addBlock("PM_EvalLayoutResult"); // $NON-NLS$
        final DmxmlContext.Block<GetStateResponse> stateBlock = dmxmlContext.addBlock("PM_AsyncState"); // $NON-NLS$
        final HandleRequest handleRequest = new HandleRequest();
        handleRequest.setHandle(archiveData.getHandle());
        resultBlock.setParameter(handleRequest);
        stateBlock.setParameter(handleRequest);
        dmxmlContext.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable caught) {
                onGadgetAsyncResult(resultBlock, archiveData);
            }

            @Override
            public void onSuccess(ResponseType result) {
                if (!stateBlock.isResponseOk() || !resultBlock.isResponseOk()) {
                    final ErrorType error = stateBlock.getError();
                    if (error != null) {
                        Notifications.add(stateBlock.getKey() + ": " + error.getCode(), error.getDescription()).requestStateDelayed(NotificationMessage.State.DELETED, 8);
                        Firebug.warn(stateBlock.getKey() + ": " + error.getCode() + "\n" + error.getDescription());
                    }
                    else {
                        Firebug.warn("AnalysisController.requestGadgetResult() -> PM_AsyncState failed");
                    }
                    return;
                }
                onGadgetAsyncResult(resultBlock, archiveData);
            }
        });
    }

    private void onGadgetAsyncResult(DmxmlContext.Block<EvalLayoutGadgetResponse> block, ArchiveData archiveData) {
        if (handleError(block, archiveData)) {
            return;
        }
        this.view.showGadget(block.getResult().getGadgetResult(), getLayoutDesc(), archiveData.getHandle());
    }

    private void requestPdfResult(final ArchiveData archiveData) {
        final DmxmlContext dmxmlContext = new DmxmlContext();
        final DmxmlContext.Block<GetStateResponse> stateBlock = dmxmlContext.addBlock("PM_AsyncState"); // $NON-NLS$
        final HandleRequest handleRequest = new HandleRequest();
        handleRequest.setHandle(archiveData.getHandle());
        stateBlock.setParameter(handleRequest);
        stateBlock.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable caught) {
                view.showError(layoutDescEx, caught.getMessage());
            }

            @Override
            public void onSuccess(ResponseType result) {
                final String reportUrl = prepareReportResult(archiveData);
                view.showPdf(reportUrl, layoutDescEx, archiveData.getHandle());
                setHeader(archiveData, stateBlock.getResult().getFinished());
                Firebug.debug("<requestPdfResult..onSuccess> language " + getLangLong());
                view.setSelectedLanguage(selectedLanguage);
                view.setLanguageButtonVisible(true);
            }
        });
        this.view.setChartPagingWidgetsVisible(false);
        handleDms(archiveData);
    }

    private String getLangLong() {
        return selectedLanguage != null ? selectedLanguage.getLanguageLong() : "";
    }

    private void handleDms(final ArchiveData archiveData) {
        if (!(this.config instanceof DatabaseIdConfig) && !(this.config instanceof HandleConfig)) {
            Firebug.warn("<AnalysisController.handleDms> Could not request dms metadata because of wrong config type. Expected DatabaseIdConfig or HandleConfig, found " + this.config.getClass().getSimpleName());
            return;
        }
        final String objectId = getObjectIdFromConfig();
        if (!StringUtil.hasText(objectId)) {
            Firebug.warn("<AnalysisController.handleDms> Could not request dms metadata because of missing object ID.");
            return;
        }

        this.dmsPresenter.requestDmsMetadata(objectId, new DmsDisplay.DmsMetadataCallback() {
            @Override
            public void metadataAvailable(DmsMetadata metadata) {
                Firebug.info("metadata.getPriv().allowed(UMRightBody.UMRB_WRITE_DOCUMENTS): " + metadata.getPriv().allowed(UMRightBody.UMRB_WRITE_DOCUMENTS));
                Firebug.info("metadata.getPriv().allowed(UMRightBody.UMRB_READ_DOCUMENTS): " + metadata.getPriv().allowed(UMRightBody.UMRB_READ_DOCUMENTS));
                view.setDmsArchivButtonVisible(Selector.AS_DMS.isAllowed() && metadata.getPriv().allowed(UMRightBody.UMRB_WRITE_DOCUMENTS));
                view.setDmsPopupButtonVisible(Selector.AS_DMS.isAllowed() && metadata.getPriv().allowed(UMRightBody.UMRB_READ_DOCUMENTS));
                dmsPresenter.update(metadata, archiveData.getLayoutDesc());
                dmsPresenter.layoutWhenUpdateDone();
            }
        });
    }

    public String getObjectIdFromConfig() {
        if (this.config instanceof HandleConfig) {
            final HistoryToken historyToken = this.config.getHistoryToken();
            if (historyToken == null) {
                Firebug.warn("<AnalysisController.getObjectIdFromConfig> Failed to get object ID from history token. HandleConfig " + this.config.getId() + " has no history token.");
                return null;
            }
            final String oid = historyToken.get(AbstractDepotObjectPortraitController.OBJECTID_KEY);
            if (!StringUtil.hasText(oid)) {
                Firebug.warn("<AnalysisController.getObjectIdFromConfig> Failed to get object ID. History token of HandleConfig " + this.config.getId() + " has no object ID parameter.");
                return null;
            }
            return oid;
        }
        else if (this.config instanceof DatabaseIdConfig) {
            return this.config.getId();
        }
        else {
            Firebug.warn("<AnalysisController.getObjectIdFromConfig> Failed to get object ID. Configs of type " + this.config.getClass().getSimpleName() + " are not supported.");
            return null;
        }
    }

    private void requestChartResult(final ArchiveData archiveData) {
        final DmxmlContext dmxmlContext = new DmxmlContext();
        final DmxmlContext.Block<EvalLayoutChartResponse> resultBlock = dmxmlContext.addBlock("PM_EvalLayoutResult"); // $NON-NLS$
        final DmxmlContext.Block<GetStateResponse> stateBlock = dmxmlContext.addBlock("PM_AsyncState"); // $NON-NLS$
        final HandleRequest handleRequest = new HandleRequest();
        handleRequest.setHandle(archiveData.getHandle());
//        handleRequest.setWithoutPayload(true); // TODO: Steffen muss das noch implementieren
        resultBlock.setParameter(handleRequest);
        stateBlock.setParameter(handleRequest);
        dmxmlContext.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable caught) {
                Firebug.warn("onFailure", caught);
                setChartPager(new ChartBlockListTypePager(resultBlock));
            }

            @Override
            public void onSuccess(ResponseType result) {
                // Order matters: prepareReportResult sets the visibility of the chart pager to false,
                // but setChartPager sets the visibility depending on the total value of the result.
                view.showChart(prepareReportResult(archiveData), layoutDescEx, archiveData.getHandle());
                setChartPager(new ChartBlockListTypePager(resultBlock));

                setHeader(archiveData, stateBlock.getResult().getFinished());
            }
        });
        this.view.setLanguageButtonVisible(false);
        this.view.hideDmsButtons();
    }

    private void setChartPager(ChartBlockListTypePager pager) {
        if (pager.isResponseOk() && pager.getTotal() > 1) {
            this.chartPagingFeature.setPager(pager);
            this.chartPagingFeature.onResult();
            this.view.setChartPagingWidgetsVisible(true);
        }
        else {
            this.view.setChartPagingWidgetsVisible(false);
        }
    }

    private String prepareReportResult(ArchiveData archiveData) {
        this.view.setAggregationButtonVisible(false);
        this.view.setSortOrderButtonVisible(false);
        this.view.setXlsExportButtonVisible(false);
        this.view.setChartPagingWidgetsVisible(false);
        return createUrl("report", archiveData.getHandle()); // $NON-NLS$
    }

    public static String createUrl(String byteService, String handle) {
        final String serverPrefix = UrlBuilder.getServerPrefix();
        final String url;

        if (serverPrefix != null) {
            String reportUrl = serverPrefix + "/pmxml-1/pmweb/" + byteService; // $NON-NLS$
            url = reportUrl + "?handle=" + handle; // $NON-NLS$
        }
        else {
            url = UrlBuilder.forPmReport("pmweb/" + byteService + "?handle=" + handle).toURL(); // $NON-NLS$
        }
        return url;
    }

    private void requestTableResult(final ArchiveData archiveData) {
        // check if still valid: config != null;
        // config is set to null, if save state was called.
        if (this.config == null) {
            Firebug.info("<AnalysisController.requestTableResult> config is null; do nothing.");
            return;
        }

//        Firebug.debug("<AnalysisController.requestTableResult> layoutDescEx=" + getLayoutGuid(archiveData) + " " + getLayoutName(archiveData) + " handle=" + archiveData.getHandle() + " objectName=" + archiveData.getObjectName());
        final DmxmlContext dmxmlContext = new DmxmlContext();
        final DmxmlContext.Block<EvalLayoutTableResponse> resultBlock = dmxmlContext.addBlock("PM_EvalLayoutResult"); // $NON-NLS$
        final DmxmlContext.Block<GetStateResponse> stateBlock = dmxmlContext.addBlock("PM_AsyncState"); // $NON-NLS$
        final HandleRequest handleRequest = new HandleRequest();
        handleRequest.setHandle(archiveData.getHandle());
        resultBlock.setParameter(handleRequest);
        stateBlock.setParameter(handleRequest);
        dmxmlContext.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable caught) {
                // check if still valid: config != null;
                // config is set to null, if save state was called.
                if (AnalysisController.this.config == null) {
                    Firebug.info("<AnalysisController.requestTableResult..onFailure> config is null; do nothing.");
                    return;
                }
                onTableAsyncResult(resultBlock, archiveData);
            }

            @Override
            public void onSuccess(ResponseType result) {
                // check if still valid: config != null;
                // config is set to null, if save state was called.
                if (AnalysisController.this.config == null) {
                    Firebug.info("<AnalysisController.requestTableResult..onSuccess> config is null; do nothing.");
                    return;
                }
//                Firebug.debug("<AnalysisController.requestTableResult..onSuccess> layoutDescEx=" + getLayoutGuid(archiveData) + " " + getLayoutName(archiveData) + " handle=" + archiveData.getHandle() + " objectName=" + archiveData.getObjectName());
                onTableAsyncResult(resultBlock, archiveData);
                if (stateBlock.isResponseOk() && resultBlock.isResponseOk()) {
                    final String caption = getTableResultCaption(resultBlock);
                    if (StringUtil.hasText(caption)) {
                        setHeader(caption, stateBlock.getResult().getFinished());
                        return;
                    }
                    setHeader(archiveData, stateBlock.getResult().getFinished());
                }
                else {
                    final ErrorType error = stateBlock.getError();
                    if (error != null) {
                        Notifications.add(stateBlock.getKey() + ": " + error.getCode(), error.getDescription()).requestStateDelayed(NotificationMessage.State.DELETED, 8);
                        Firebug.warn(stateBlock.getKey() + ": " + error.getCode() + "\n" + error.getDescription());
                    }
                    else {
                        Firebug.warn("AnalysisController.requestTableResult() -> PM_AsyncState failed");
                    }
                }
            }
        });
        this.view.setChartPagingWidgetsVisible(false);
        this.view.setLanguageButtonVisible(false);
        this.view.hideDmsButtons();
    }

    private String getTableResultCaption(DmxmlContext.Block<EvalLayoutTableResponse> resultBlock) {
        final EvalLayoutTableResponse tableResponse = resultBlock.getResult();
        final DTTable table = tableResponse != null ? tableResponse.getTable() : null;
        return table != null ? table.getCaption() : null;
    }

    private void onTableAsyncResult(DmxmlContext.Block<EvalLayoutTableResponse> block, ArchiveData archiveData) {
//        Firebug.debug("<AnalysisController.onTableAsyncResult> layoutDescEx=" + getLayoutGuid(archiveData) + " " + getLayoutName(archiveData) + " handle=" + archiveData.getHandle() + " objectName=" + archiveData.getObjectName());
        if (handleError(block, archiveData)) {
            return;
        }
        final DTTable dtTable = block.getResult().getTable();

        //The group with index 0 is the top level group that is always available.
        //Its column index is given by pm as -1.
        //
        //If no groups are given, the grouping/aggregation button should be not visible.
        //
        //However, the top level group may contain aggregated rows without any further groupings.
        //In this case the aggregation button should be visible.
        //In all cases with aggregation, enabled aggregation should be the default value.
        final boolean hasGroups = (dtTable.getGroupSpecs().size() > 1)
                || ((dtTable.getGroupSpecs().size() == 1) && (dtTable.getToplevelGroup().getAggregatedRow() != null));

        this.view.setSortOrderButtonVisible(true);
        this.view.setXlsExportButtonVisible(true);
        this.view.setAggregationButtonVisible(hasGroups);
        if (this.dtTableOptions == null) {
            this.dtTableOptions = new DTTableRenderer.Options();
        }
        // the state of option.withAggregations is determined by loadState and saveState.
        // Hence, we reset this state only if one changes layouts but does not toggle between threads,
        // because then no state is saved or loaded and we have to ensure that a table that has no aggregations shows
        // the aggregated state. But if one changes a thread we want to present the state of the aggregation button as
        // it was saved before.
        if (!hasGroups) {
            this.dtTableOptions.withAggregations(false);
        }
        this.view.showTable(dtTable, this.dtTableOptions, this.filterData, getLayoutDesc(), this.maxDiagramIdx, archiveData.getHandle());
    }

    private boolean handleError(DmxmlContext.Block<? extends EvalLayoutResponse> block, ArchiveData archiveData) {
        if (!block.isResponseOk()) {
            Firebug.warn("AnalysisController <handleError> response not ok!");
            return true;
        }
        if (block.getResult() == null) {
            Firebug.warn("AnalysisController <handleError> block.getResult() is null!");
            return true;
        }
        if (block.getResult().getQueryResponseState() == QueryResponseState.QRS_OK) {
            return false;
        }
        if (block.getError() != null) {
            Firebug.warn("AnalysisController <handleError> " + block.getError().getDescription());
        }
        else {
            Firebug.warn("AnalysisController <handleError> reponse not ok. no error message found.");
        }
        if (block.getResult().getQueryResponseState() != QueryResponseState.QRS_OK) {
            final String err = "AnalysisController <handleError> " + block.getResult().getQueryResponseState() + " for " + archiveData; // $NON-NLS$
            Firebug.error(err);
            DebugUtil.logToServer(err);
        }
        this.view.showError(this.layoutDescEx, I18n.I.noDataAvailable());
        this.view.setAggregationButtonVisible(false);
        this.view.setSortOrderButtonVisible(false);
        this.view.setXlsExportButtonVisible(false);
        this.view.setLanguageButtonVisible(false);
        return true;
    }

    private void setHeader(String text, String completedDateStr) {
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendEscaped(text);
        if (completedDateStr != null) {
            final MmJsDate completedDate = toMmJsDate(completedDateStr);
            sb.appendHtmlConstant("<span class=\"as-jobCompletedTime\">");
            if (completedDate != null && completedDate.isToday()) {
                sb.appendEscaped(JsDateFormatter.formatHhmmss(completedDate));
            }
        }
        setHeader(sb.toSafeHtml());
    }

    static MmJsDate toMmJsDate(String finishedDate) {
        try {
            return JsDateFormatter.parseDdmmyyyy(finishedDate, true);
        } catch (Exception e) {
            Firebug.warn("<AnalysisController.toMmJsDate> Cannot parse '" + finishedDate + "'", e);
            return null;
        }
    }

    public void setHeader(ArchiveData archiveData, String completedDate) {
        if (archiveData == null) {
            Firebug.debug("AnalysisController <setHeader> archiveData is null!");
            return;
        }
        // render a double dash if the layout name is null or empty
        setHeader(Renderer.STRING_DOUBLE_DASH.render(getLayoutName(archiveData)), completedDate);
    }

    public void setHeader(SafeHtml safeHtml) {
        final HistoryThreadManager htm = MainController.INSTANCE.getHistoryThreadManager();
        htm.getActiveThreadHistoryItem().getPlaceChangeEvent()
                .withProperty(ANALYSIS_HEADLINE_KEY, safeHtml.asString());
        this.headerHelper.update(this.config, safeHtml);

        if (config != null && config.getHeaderPrefix() != null) {
            safeHtml = StringUtil.asHeader(config.getHeaderPrefix(), safeHtml);
        }
        MainController.INSTANCE.getView().setContentHeader(safeHtml);
    }

    public AnalysisDisplay getView() {
        return this.view;
    }

    @Override
    public boolean providesContentHeader() {
        return true;
    }

    @SuppressWarnings("unchecked")
    private void applyParamsFromJson(LayoutDesc layoutDesc, String json) {
        if (layoutDesc == null || json == null) {
            return;
        }
        final Map<String, Object> values = JsonUtil.fromJson(json);
        this.dtTableOptions = (DTTableRenderer.Options) values.get(JsonUtil.DTTABLE_RENDERER_OPTIONS);
        this.filterData = (Map<Integer, FilterData>) values.get(JsonUtil.COLUMN_FILTER_DATA);
        Map<String, String> loadedFormParameters = (HashMap<String, String>) values.get(JsonUtil.ANALYSIS_PARAMETERS);
        final Object mdi = values.get(JsonUtil.MAX_DIAGRAM_IDX);
        this.maxDiagramIdx = mdi == null
                ? null
                : (Integer) mdi;
        if (loadedFormParameters == null) {
            loadedFormParameters = Collections.emptyMap();
        }
        this.selectedLanguage = (Language) values.get(JsonUtil.SELECTED_LANGUAGE);

        Firebug.debug("<AnalysisMetadataHandler.applyParamsFromJson> selectedLanguage = " + getLangLong());

        applyAnalysisParameters(MmTalkHelper.toParameterMap(loadedFormParameters, layoutDesc), layoutDesc);
    }

    @Override
    public Map<String, String> saveState(HistoryItem item) {
//        Firebug.debug("<AnalysisController.saveState>" + item.getPlaceChangeEvent().getHistoryToken().toStringWithHid());

        saveArchiveDataInPlaceChangeEvent(item.getPlaceChangeEvent());

        // saving state means that the thread has been changed recently.
        // So setting the config to null triggers forces an re-evaluation of the pm analysis
        this.config = null;

        return Collections.emptyMap();
    }

    @Override
    public void loadState(HistoryItem item, Map<String, String> data) {
        //nothing to do
    }

    @Override
    public String getStateKey(HistoryItem item) throws GetStateKeyException {
        return this.stateKeyPrefix + ANALYSIS_CONTROLLER_STATE_KEY + toString();
    }

    private void saveArchiveDataInPlaceChangeEvent(PlaceChangeEvent pce) {
        if (pce == null) {
            return;
        }
        //1. when running, send to background
        boolean wasFromSendToAsync = true;
        String handle = this.evaluationController.sendAsyncToBackground();
        //2. when finished, remember handle
        if (handle == null) {
            wasFromSendToAsync = false;
            handle = this.lastFinishedHandle;
        }
        Firebug.debug("<AnalysisController.saveArchiveDataInPlaceChangeEvent> wasFromSendToAsync=" + wasFromSendToAsync + " handle=" + handle + " pce's token=" + pce.getHistoryToken().toStringWithHid());
        pce.withProperty(HKEY_HANDLE, handle);
    }

    public void deactivate() {
        this.evaluationController.cancelOrCleanupAsync(false, "AnalysisController.deactivate");  // $NON-NLS$
        if (this.threadStateSupport != null) {
            threadStateSupport.unregister();
        }
        this.threadStateSupport = null;

    }

    public void activate() {
        if (this.threadStateSupport != null && this.stateKeyPrefix != null) {
            this.threadStateSupport = new ThreadStateSupport(this, false);
        }
    }

    public void saveLayoutAndTableParams() {
        if (this.layoutDescEx == null) {
            return;
        }
        Firebug.debug("<ArchiveController.saveLayoutAndTableParams>");
        AnalysisMetadataHandler.getInstance().putConfig(this.layoutDescEx, this.analysisParameters, filterData, this.dtTableOptions, this.maxDiagramIdx, this.selectedLanguage);
    }

    public Config getConfig() {
        return this.config;
    }

    @Override
    public void setMaxDiagramIdx(Integer idx) {
        this.maxDiagramIdx = idx;
        saveLayoutAndTableParams();
    }

    @Override
    public void onArchiveButtonClicked() {
        final String objectId = getObjectIdFromConfig();
        if (!StringUtil.hasText(objectId)) {
            Firebug.warn("<AnalysisController.onArchiveButtonClicked> Could not request dms metadata because of missing object ID.");
            return;
        }

        this.dmsPresenter.requestDmsMetadata(objectId, new DmsDisplay.DmsMetadataCallback() {
            @Override
            public void metadataAvailable(final DmsMetadata metadata) {
                view.showArchiveDialog(metadata);
            }
        });
    }

    @Override
    public void onLanguageSelected(Language language) {
        if (this.layoutDescEx == null) {
            return;
        }

        Firebug.warn("<ArchiveController.onLanguageSelected> with language " + language.getLanguageLong());
        this.selectedLanguage = language;

        Firebug.warn("<ArchiveController.onLanguageSelected> call putConfig");
        AnalysisMetadataHandler.getInstance().putConfig(this.layoutDescEx, this.analysisParameters, filterData, this.dtTableOptions, this.maxDiagramIdx, this.selectedLanguage);

        reevaluate();
    }

    public boolean isPrintable() {
        return this.view.isPrintable();
    }

    public String getPrintHtml() {
        return this.view.getPrintHtml();
    }

    public void setHeaderFromHistory() {
        final HistoryThreadManager htm = MainController.INSTANCE.getHistoryThreadManager();
        final String head = htm.getActiveThreadHistoryItem().getPlaceChangeEvent().getProperty(ANALYSIS_HEADLINE_KEY);
        if (StringUtil.hasText(head)) {
            // this case is entered if the analysis has been loaded already, e.g. in an different thread context,
            // and you are switching back to it, e.g. from another thread context.
            final SafeHtmlBuilder sb = new SafeHtmlBuilder();
            sb.appendHtmlConstant(head);
            MainController.INSTANCE.getView().setContentHeader(sb.toSafeHtml());
        }
        else if (this.headerHelper.isFor(this.config)) {
            // this case is entered if the last executed evaluation is called again,
            // but the force flag indicates not to re-evaluate the analysis.
            MainController.INSTANCE.getView().setContentHeader(this.headerHelper.getSafeHtml());
        }
    }

    public void requestEvalResult(final ArchiveData archiveData) {
        final ArchiveData.ContentType contentType = archiveData.getType();
        switch (contentType) {
            case TABLE:
                requestTableResult(archiveData);
                break;
            case PDF:
                requestPdfResult(archiveData);
                break;
            case CHART:
                requestChartResult(archiveData);
                break;
            case GADGET:
                requestGadgetResult(archiveData);
                break;
        }
    }

    @Override
    public void cancelAsyncAndCloseSession() {
        this.evaluationController.cancelAsync(false, "AnalysisController.cancelAsyncAndCloseSession");  // $NON-NLS$
    }

    @Override
    public void sendAsyncToBackground() {
        this.evaluationController.sendAsyncToBackground();
    }
}