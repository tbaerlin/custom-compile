/*
 * SelectSymbolPresenter.java
 *
 * Created on 18.07.2014 11:40
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.research;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.MainController;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DTTableRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.AnalysisMetadataHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.EvaluationController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config.Config;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config.DatabaseObjectConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.ArchiveData;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.dms.DmsMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.dttablefilter.FilterData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.DatabaseObject;
import de.marketmaker.iview.pmxml.EvalLayoutTableResponse;
import de.marketmaker.iview.pmxml.HandleRequest;
import de.marketmaker.iview.pmxml.Language;
import de.marketmaker.iview.pmxml.LayoutDesc;
import de.marketmaker.iview.pmxml.MMClassIndex;
import de.marketmaker.iview.pmxml.Parameter;
import de.marketmaker.iview.pmxml.ShellMMInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author mdick
 */
public class SelectSymbolPresenter implements SelectSymbolDisplay.Presenter {
    private static final Map<String, Map<String, Parameter>> parametersDict = new HashMap<>();

    private final SelectSymbolDisplay<SelectSymbolDisplay.Presenter> display;

    private final AnalysisMetadataHandler metadataHandler = AnalysisMetadataHandler.getInstance();
    private final EvaluationController evaluationController;

    private ShellMMInfo selectedItem = null;
    private String layoutGuid;
    private LayoutDesc layoutDescEx;
    private String activityInstanceId;

    private Consumer<ShellMMInfo> selectedElementConsumer;

    public SelectSymbolPresenter(SelectSymbolDisplay<SelectSymbolDisplay.Presenter> display) {
        this.display = display;
        this.display.setPresenter(this);

        this.evaluationController = new EvaluationController.Builder(null)
                .build(null, this.display, null, this::requestResult);
    }

    public SelectSymbolPresenter(SelectSymbolDisplay<SelectSymbolDisplay.Presenter> display, String layoutGuid) {
        this(display);
        withLayoutGuid(layoutGuid);
    }

    public SelectSymbolPresenter withLayoutGuid(String layoutGuid) {
        this.layoutGuid = layoutGuid;
        return this;
    }

    public void setActivityInstanceId(String activityInstanceId) {
        this.activityInstanceId = activityInstanceId;
    }

    public void show(final Consumer<ShellMMInfo> selectedElementConsumer) {
        this.selectedElementConsumer = selectedElementConsumer;

        this.display.clear();

        if (!parametersDict.containsKey(this.layoutGuid)) {
            parametersDict.put(this.layoutGuid, new HashMap<>());
        }

        this.metadataHandler.getMetadata(this.layoutGuid, (layoutDescEx1, jsonConfig) -> {
            SelectSymbolPresenter.this.onMetadataAvailable(layoutDescEx1);
            display.setTitle(layoutDescEx1.getLayout().getLayoutName());
        });

        this.display.show();
    }

    private void onMetadataAvailable(LayoutDesc layoutDescEx) {
        this.layoutDescEx = layoutDescEx;
        this.display.setConfigButtonEnabled(!(layoutDescEx.getParameters() != null && layoutDescEx.getParameters().isEmpty()));
        evaluate(this.activityInstanceId, this.layoutGuid, this.layoutDescEx);
    }

    @Override
    public void onCancelClicked() {
        this.display.hide();
        cancelAsyncAndCloseSession();
    }

    @Override
    public void onOkClicked() {
        this.display.hide();
        this.selectedElementConsumer.accept(this.selectedItem);
        cancelAsyncAndCloseSession();
    }

    public void onItemSelected(ShellMMInfo selectedItem) {
        this.selectedItem = selectedItem;
    }

    private void updateDisplay(EvalLayoutTableResponse response, ArchiveData archiveData) {
        if (response == null) {
            this.display.clear();
            return;
        }
        this.layoutDescEx = archiveData.getLayoutDesc();
        this.display.showTable(response.getTable(), new DTTableRenderer.Options(), null, this.layoutDescEx, null, archiveData.getHandle());
    }

    private void evaluate(String activityInstanceId, String layoutGuid, LayoutDesc layoutDescEx) {
        if (!StringUtil.hasText(layoutGuid)) {
            throw new IllegalStateException("SelectSymbolPresenter: no layoutGuid given!"); // $NON-NLS$
        }
        final HistoryToken ht = MainController.INSTANCE.getHistoryThreadManager().getActiveThreadHistoryItem().getPlaceChangeEvent().getHistoryToken();
        final SafeHtml header = MainController.INSTANCE.getView().getContentHeader();

        final DatabaseObject databaseObject = new DatabaseObject();
        databaseObject.setClassIdx(MMClassIndex.CI_ACTIVITY);
        databaseObject.setId(activityInstanceId);

        final DatabaseObjectConfig config = Config.createWithDatabaseObject(ht, databaseObject, layoutGuid, header, PrivacyMode.isActive());
        this.evaluationController.eval(config, layoutDescEx, null);
    }

    private void requestResult(final ArchiveData archiveData) {
        final String handle = archiveData.getHandle();

        final DmxmlContext dmxmlContext = new DmxmlContext();
        final DmxmlContext.Block<EvalLayoutTableResponse> block = dmxmlContext.addBlock("PM_EvalLayoutResult"); // $NON-NLS$
        final HandleRequest handleRequest = new HandleRequest();
        handleRequest.setHandle(handle);

        block.setParameter(handleRequest);
        block.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable caught) {
                Dialog.error("Evaluating the layout failed!");  // $NON-NLS$
                Firebug.error("<SelectSymbolPresenter.requestResult> failed! handle=" + handle, caught);
            }

            @Override
            public void onSuccess(ResponseType result) {
                if (!block.isResponseOk()) {
                    updateDisplay(null, archiveData);
                }
                updateDisplay(block.getResult(), archiveData);
            }
        });
    }

    @Override
    public void setAnalysisParametersOfMetadataForm(Map<String, String> analysisParameters) {
        Firebug.debug("<SelectSymbolPresenter.setAnalysisParametersOfMetadataForm> analysisParamters<String, String>=" + analysisParameters.toString());
        parametersDict.put(this.layoutGuid, MmTalkHelper.toParameterMap(analysisParameters, this.layoutDescEx));
        this.display.clear();
        evaluate(this.activityInstanceId, this.layoutGuid, this.layoutDescEx);
    }

    @Override
    public void onShowReportSettings() {
        final Map<String, Parameter> layoutParameters = parametersDict.get(this.layoutGuid);
        if (layoutParameters != null) {
            this.display.showReportSettings(MmTalkHelper.toStringMap(layoutParameters), null);
            return;
        }
        this.display.showReportSettings(Collections.<String, String>emptyMap(), null);
    }

    @Override
    public void setFilterData(Map<Integer, FilterData> filterMetadata) {
        //do nothing.
    }

    @Override
    public void saveLayoutAndTableParams() {
        //do nothing
    }

    @Override
    public void cancelAsyncAndCloseSession() {
        this.evaluationController.cancelAsync(false, "SelectSymbolPresenter.cancelAsyncAndCloseSession");  // $NON-NLS$
    }

    @Override
    public void xlsExport(String handle) {
        Firebug.warn("<SelectSymbolPresenter.xlsExport> not implemented");
    }

    @Override
    public void sendAsyncToBackground() {
        Firebug.warn("<SelectSymbolPresenter.sendAsyncToBackground> not implemented");
    }

    @Override
    public void archive(String layoutName, String handle, String layoutType, String title, String comment, DmsMetadata metadata) {
        throw new IllegalStateException("not implemented!"); // $NON-NLS$
    }

    @Override
    public Config getConfig() {
        throw new IllegalStateException("not implemented!"); // $NON-NLS$
    }

    @Override
    public void setMaxDiagramIdx(Integer idx) {
        //nothing to do
    }

    @Override
    public void onArchiveButtonClicked() {
        //nothing to do
    }

    @Override
    public void onLanguageSelected(Language language) {
        //nothing to do
    }
}
