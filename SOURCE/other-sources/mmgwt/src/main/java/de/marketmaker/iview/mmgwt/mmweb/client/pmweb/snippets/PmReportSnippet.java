/*
 * PmReportSnippet.java
 *
 * Created on 05.04.2011
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.Command;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.MSCQuoteMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.LayoutNode;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.AnalysisController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config.Config;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config.HandleConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.MetadataAware;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.HashMap;

/**
 * @author Ulrich Maurer
 * @author Markus Dick
 */
public class PmReportSnippet extends AbstractSnippet<PmReportSnippet, PmReportSnippetView> implements MetadataAware {
    public static final String LAYOUT_GUID = "layoutGuid"; //$NON-NLS$
    public static final String SECURITY_ID = "sec"; //$NON-NLS$
    private Config config;
    private HistoryToken historyToken;
    private Command pendingEvaluation = null;

    private HashMap<String, String> pendingQidParams = null;

    @Override
    public boolean isMetadataNeeded() {
        return true; //only necessary to resolve QIDs to IIDs to PM-SIDs
    }

    @Override
    public void onMetadataAvailable(MSCQuoteMetadata metadata) {
        Firebug.debug("<PmReportSnippet.onMetadataAvailable>");

        if(this.pendingQidParams != null && metadata != null && metadata.getInstrumentdata() != null) {
            final String iid = metadata.getInstrumentdata().getIid();
            final String sid = iid.substring(0, iid.indexOf(".iid")); // $NON-NLS$
            this.pendingQidParams.put(SECURITY_ID, sid);
            Firebug.debug("<PmReportSnippet.onMetadataAvailable> setParameters: " + this.pendingQidParams);
            setParameters(this.pendingQidParams);
            this.pendingQidParams = null;
        }
    }

    public static class Class extends SnippetClass {
        public Class() {
            super("PmReport", I18n.I.pmReportSnippetTitle()); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new PmReportSnippet(context, config);
        }
    }

    private final AnalysisController analysisController;

    public PmReportSnippet(DmxmlContext context, SnippetConfiguration configuration) {
        super(context, configuration);
        this.analysisController = new AnalysisController("RS"); // $NON-NLS$
        setView(new PmReportSnippetView(this));

        // The chart's width an height can only be get if the view is attached to the DOM and if the browser
        // has finished its layout work (deferred). Otherwise it is likely that the height and the width are zero,
        // which forces PM to throw an exception.
        analysisController.getView().asWidget().addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if(event.isAttached() && pendingEvaluation != null) {
                    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                        @Override
                        public void execute() {
                            final Command tmpPending = pendingEvaluation;
                            pendingEvaluation = null;
                            tmpPending.execute();
                        }
                    });
                }
            }
        });
    }

    public AnalysisController getAnalysisController() {
        return this.analysisController;
    }

    public void destroy() {
        //nothing
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        updateConfig(event);
    }

    private void updateConfig(PlaceChangeEvent event) {
        this.historyToken = event.getHistoryToken();
        final String id = this.historyToken.get(1);
        final HashMap<String, String> params = getCopyOfParameters();
        Firebug.debug("<PmReportSnippet.updateConfig> event.getHistoryToken(): " + this.historyToken);
        final String layoutNodeAndGuid = this.historyToken.get(2);
        if (!PlaceUtil.UNDEFINDED_PORTRAIT_VIEW.equals(layoutNodeAndGuid)) {
            final LayoutNode layoutNode = LayoutNode.create(layoutNodeAndGuid);
            params.put(PmReportSnippet.LAYOUT_GUID, layoutNode != null ? layoutNode.getGuid(): null);
        }

        String handle = event.getHistoryToken().get(AnalysisController.HKEY_HANDLE);
        if(handle == null) {
            handle = this.historyToken.get(AnalysisController.HKEY_HANDLE);
        }
        if(handle != null) {
            params.put(AnalysisController.HKEY_HANDLE, handle);
        }
        else {
            params.remove(AnalysisController.HKEY_HANDLE);
        }

        final String sid;
        if (id.endsWith(".sid")) { // $NON-NLS$
            sid = id.substring(0, id.indexOf(".sid")); // $NON-NLS$
        }
        else if (id.endsWith(".iid")) { // $NON-NLS$
            sid = id.substring(0, id.indexOf(".iid")); // $NON-NLS$
        }
        else if (id.endsWith(".qid")) { // $NON-NLS$
            // PM knows only IIDs. Hence, we delay this request until quote metadata is available.
            this.pendingQidParams = params;
            return;
        }
        else if (this.historyToken.get(SECURITY_ID) != null) {
            sid = this.historyToken.get(SECURITY_ID);
        }
        else {
            throw new IllegalArgumentException("no valid id found: " + id); // $NON-NLS$
        }
        params.put(SECURITY_ID, sid);
        Firebug.debug("<PmReportSnippet.updateConfig> setParameters: " + params);
        setParameters(params);
    }

    @Override
    protected void ackParametersChanged() {
        //the chart's width an height can only be get if the view is attached to the DOM
        if (this.analysisController.getView().asWidget().isAttached()) {
            doAckParametersChanged();
        }
        else {
            this.pendingEvaluation = new Command() {
                @Override
                public void execute() {
                    doAckParametersChanged();
                }
            };
        }
    }

    private void doAckParametersChanged() {
        onParametersChanged();
        getView().reloadTitle();
        if(this.config instanceof HandleConfig) {
            this.analysisController.fetch((HandleConfig)this.config);
        }
        else {
            this.analysisController.evaluate(this.config);
        }
    }

    @Override
    protected void onParametersChanged() {
        final boolean privacyModeActive = PrivacyMode.isActive();

        final SnippetConfiguration config = getConfiguration();

        final String securityId = config.getString(SECURITY_ID);
        final String layoutGuid = config.getString(LAYOUT_GUID);

        final String handle = config.getString(AnalysisController.HKEY_HANDLE);

        final Config sourceConfig = Config.createWithSecurityId(this.historyToken, securityId, layoutGuid,
                getView().getChartHeight(), getView().getChartWidth(), privacyModeActive);

        if(StringUtil.hasText(handle)) {
            this.config = Config.createWithHandle(this.historyToken, handle, layoutGuid, null, sourceConfig, privacyModeActive);
        }
        else {
            this.config = sourceConfig;
        }
    }

    public void updateView() {
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.analysisController.deactivate();
    }
}