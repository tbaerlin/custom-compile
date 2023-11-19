package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.history.GetStateKeyException;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryItem;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ThreadStateHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ThreadStateSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DTTableRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.dttablefilter.FilterData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.GetLayoutDataRequest;
import de.marketmaker.iview.pmxml.GetLayoutDataResponse;
import de.marketmaker.iview.pmxml.Language;
import de.marketmaker.iview.pmxml.LayoutDesc;
import de.marketmaker.iview.pmxml.Parameter;

/**
 * User this class to request and cache a LayoutDesc.
 * Parameters, TableFilters and TableRendererOptions are saved in mapConfig
 * and will be saved (and loaded) in the ThreadState when switching to another thread.
 * @author umaurer
 */
public class AnalysisMetadataHandler implements ThreadStateHandler {
    public static final String LAYOUTS = "LayoutBlobs"; // $NON-NLS$

    private static AnalysisMetadataHandler instance;

    private final DmxmlContext context;

    private final DmxmlContext.Block<GetLayoutDataResponse> block;

    private final HashMap<String, LayoutDesc> mapMetadata = new HashMap<>();

    private final HashMap<String, String> mapConfig = new HashMap<>();

    private AnalysisMetadataHandler() {
        new ThreadStateSupport(this, false);
        this.context = new DmxmlContext();
        this.block = this.context.addBlock("PM_GetLayoutData"); // $NON-NLS$
        this.context.setCancellable(false);
    }

    public static AnalysisMetadataHandler getInstance() {
        if (instance == null) {
            instance = new AnalysisMetadataHandler();
        }
        return instance;
    }

    public void getMetadata(final String layoutGuid, final AnalysisMetadataCallback callback) {
        getMetadata(layoutGuid, callback, false, true);
    }

    public void getMetadata(final String layoutGuid, final AnalysisMetadataCallback callback, boolean loadBlob, boolean initParams) {
        final LayoutDesc report = this.mapMetadata.get(layoutGuid);
        if (report != null) {
            Scheduler.get().scheduleDeferred(new Command() {
                public void execute() {
                    Firebug.info("AnalysisMetadataHandler <getMetadata> guid: " + layoutGuid + " / jsonConfig: " + mapConfig.get(layoutGuid));
                    callback.onMetadataAvailable(report, mapConfig.get(layoutGuid));
                }
            });
            return;
        }

        final GetLayoutDataRequest layoutRequest = new GetLayoutDataRequest();
        layoutRequest.setLayoutGuid(layoutGuid);
        layoutRequest.setInitParams(initParams);
        layoutRequest.setLoadBlob(loadBlob);
        this.block.setParameter(layoutRequest);
        this.context.issueRequest(new AsyncCallback<ResponseType>() {
            public void onFailure(Throwable caught) {
                AbstractMainController.INSTANCE.showError(I18n.I.cannotCreateEvaluation());
                Firebug.warn("no metadata available", caught); // $NON-NLS-0$
                callback.onMetadataAvailable(null, null);
            }

            public void onSuccess(ResponseType result) {
                if (!block.isResponseOk() || block.getResult() == null || block.getResult().getLayout() == null) {
                    AbstractMainController.INSTANCE.showError(I18n.I.cannotCreateEvaluation());
                    if (!block.isResponseOk()) {
                        Firebug.warn("no metadata available - " + block.getError().getDescription()); // $NON-NLS-0$
                    }
                    else {
                        Firebug.warn("no metadata available - result==null?" + (block.getResult() == null) + " result.layout==null?" + (block.getResult() != null && block.getResult().getLayout() == null));
                    }
                    return;
                }
                final LayoutDesc report = block.getResult().getLayout();
                mapMetadata.put(layoutGuid, report);
                Firebug.log("AnalysisMetadataHandler <getMetadata> guid: " + layoutGuid + " / jsonConfig: " + mapConfig.get(layoutGuid));
                callback.onMetadataAvailable(report, mapConfig.get(layoutGuid));
            }
        });
    }

    /////////////////////////////////////////////////////////////////////////
    // ThreadStateHandler implementation ////////////////////////////////////

    @Override
    public Map<String, String> saveState(HistoryItem item) {
        Firebug.debug("<AnalysisMetadataHandler.saveState>");
        final JSONArray descs = new JSONArray();
        int i = 0;
        for (String json : mapConfig.values()) {
            Firebug.debug("<AnalysisMetadataHandler.saveState> json = " + json);
            descs.set(i++, new JSONString(json));
        }
        return Collections.singletonMap(LAYOUTS, descs.toString());
    }

    @Override
    public void loadState(HistoryItem item, Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        final String json = data.get(LAYOUTS);
        if (!StringUtil.hasText(json) || !JsonUtils.safeToEval(json)) {
            return;
        }
        final JSONArray array = new JSONArray(JsonUtils.safeEval(json));

        try {
            for (int i = 0; i < array.size(); i++) {
                final JSONString layout = array.get(i).isString();
                if (!StringUtil.hasText(layout.stringValue())) {
                    continue;
                }
                final Map<String, Object> values = JsonUtil.fromJson(layout.stringValue());
                final String guid = (String) values.get(JsonUtil.ANALYSIS_GUID);
                final HashMap<String, String> params = (HashMap<String, String>) values.get(JsonUtil.ANALYSIS_PARAMETERS);
                final Map<Integer, FilterData> filterData = (Map<Integer, FilterData>) values.get(JsonUtil.COLUMN_FILTER_DATA);
                final DTTableRenderer.Options tableOptions = (DTTableRenderer.Options) values.get(JsonUtil.DTTABLE_RENDERER_OPTIONS);
                final Object mdi = values.get(JsonUtil.MAX_DIAGRAM_IDX);
                final Integer maxDiagramIdx = mdi == null
                        ? null
                        : (Integer) mdi;
                final Language lang = (Language) values.get(JsonUtil.SELECTED_LANGUAGE);
                Firebug.debug("<AnalysisMetadataHandler.loadState> lang is " + (lang == null ? "null" : lang.getLanguageLong()));
                getMetadata(guid, new AnalysisMetadataCallback() {
                    @Override
                    public void onMetadataAvailable(LayoutDesc layoutDesc, String jsonConfig) {
                        final String json = JsonUtil.toJson(tableOptions, params, layoutDesc.getLayout().getGuid(), filterData, maxDiagramIdx, lang);
                        Firebug.debug("<AnalysisMetadataHandler.loadState> json = " + json);
                        mapConfig.put(layoutDesc.getLayout().getGuid(), json);
                    }
                });
            }
        } catch (Exception e) {
            Firebug.error("JSON: " + json, e);
        }
    }

    @Override
    public String getStateKey(HistoryItem item) throws GetStateKeyException {
        return "LAYOUTS-METADATA"; // $NON-NLS$
    }

    public void putConfig(LayoutDesc layoutDesc, Map<String, Parameter> analysisParameters, Map<Integer, FilterData> filterData,
            DTTableRenderer.Options dtTableOptions, Integer maxDiagramIdx, Language lang) {
        if (layoutDesc == null) {
            return;
        }
        final String json = JsonUtil.toJson(dtTableOptions, MmTalkHelper.toStringMap(analysisParameters), layoutDesc.getLayout().getGuid(), filterData, maxDiagramIdx, lang);
        Firebug.debug("AnalysisMetadataHandler <putConfig> json: " + json);
        this.mapConfig.put(layoutDesc.getLayout().getGuid(), json);
    }
    ///////////////////////////////////////////////////////////////////////////
}