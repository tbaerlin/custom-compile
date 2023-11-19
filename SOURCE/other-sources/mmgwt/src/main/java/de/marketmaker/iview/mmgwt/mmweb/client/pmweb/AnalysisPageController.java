package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.AnalysisController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config.Config;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.AbstractDepotObjectPortraitController;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;

/**
 * Created on 30.08.13 11:41
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class AnalysisPageController extends AbstractPageController {
    private final AnalysisController analysisController;
    private boolean forceEval = false;

    public AnalysisPageController(String stateKeyPrefix) {
        this.analysisController = new AnalysisController(stateKeyPrefix);

        PrivacyMode.subscribe(new PrivacyMode.InterestedParty() {
            @Override
            public void privacyModeStateChanged(boolean privacyModeActive, PrivacyMode.StateChangeProcessedCallback processed) {
                forceEval = true;
                //do not allow to put running async analysis into background in privacy mode
                analysisController.getView().setProgressPanelBackgroundButtonVisible(!privacyModeActive);
                processed.privacyModeStateChangeProcessed(this);
            }
        });
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        final HistoryToken historyToken = event.getHistoryToken();
        final String dbid = historyToken.get(AbstractDepotObjectPortraitController.OBJECTID_KEY);
        final String sc = historyToken.get(NavItemSpec.SUBCONTROLLER_KEY);
        if (!accept(dbid, sc)) {
            this.analysisController.setHeaderFromHistory();
            return;
        }
        String handle = event.getProperty(AnalysisController.HKEY_HANDLE);
        if (handle == null) {
            handle = event.getHistoryToken().get(AnalysisController.HKEY_HANDLE);
//            Firebug.debug("<AnalysisPageController.onPlaceChange> handle-source: event.getHistoryToken().get(AnalysisController.HKEY_HANDLE) " + handle);
        }
//        else {
//            Firebug.debug("<AnalysisPageController.onPlaceChange> handle-source: event.getProperty(AnalysisController.HKEY_HANDLE) " + handle + " historyToken=" + historyToken.toStringWithHid());
//        }

        if (StringUtil.hasText(handle) && !this.forceEval) {
            // Database ID and SC have already been accepted, so createConfig should work as expected. However, there
            // is no guarantee that the database ID and SC are non-null, because null or empty values may be legally
            // accepted by accept(String dbid, String sc) as well (i.e. in case of config type GlobalConfig)
            final LayoutNode layoutNode = LayoutNode.create(sc);
            final Config sourceConfig = createConfig(historyToken, dbid, layoutNode);
            final String guid = layoutNode != null ? layoutNode.getGuid() : null;
            this.analysisController.fetch(Config.createWithHandle(historyToken, handle, guid, null, sourceConfig, PrivacyMode.isActive()));
        }
        else {
            this.analysisController.evaluate(createConfig(historyToken, dbid, LayoutNode.create(sc)), this.forceEval);
        }
        this.forceEval = false;
        getContentContainer().setContent(this.analysisController.getView().asWidget());
    }

    protected Config createConfig(HistoryToken historyToken, String dbid, LayoutNode layoutNode) {
        return Config.createWithDatabaseId(historyToken, dbid, layoutNode.getGuid(), null, PrivacyMode.isActive());
    }

    protected boolean accept(String dbid, String sc) {
        return StringUtil.hasText(dbid) && StringUtil.hasText(sc) && sc.contains(";");
    }

    public void forceEval() {
        this.forceEval = true;
    }

    @Override
    public void deactivate() {
        this.analysisController.deactivate();
    }

    @Override
    public void activate() {
        this.analysisController.activate();
    }

    @Override
    public boolean isPrintable() {
        return this.analysisController.isPrintable();
    }

    @Override
    public String getPrintHtml() {
        return this.analysisController.getPrintHtml();
    }

    @Override
    public void refresh() {
        this.analysisController.reevaluate();
    }
}