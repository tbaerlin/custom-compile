/*
 * GlobalAnalysisPageController
 *
 * Created on 26.06.2014 15:43
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.workspace;

import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.AnalysisPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.LayoutNode;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.AnalysisController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config.Config;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author Markus Dick
 */
public class GlobalAnalysisPageController extends AnalysisPageController {
    public GlobalAnalysisPageController() {
        super("GLPC"); // $NON-NLS$
    }

    @Override
    protected boolean accept(String dbid, String sc) {
        return StringUtil.hasText(sc) && sc.contains(";");
    }

    protected Config createConfig(HistoryToken historyToken, String dbid, LayoutNode layoutNode) {
        ExplorerWorkspace.getInstance().selectGlobalLayout(layoutNode);
        return Config.createGlobal(historyToken, layoutNode.getGuid(), null, PrivacyMode.isActive()); //this controller is not used in privacy mode
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        // Force re-evaluation even if the current analysis is the same as the requested analysis.
        // But do not force re-evaluation if there is already an async result (handle).
        // Forcing re-evaluation is necessary, because otherwise the analysis will not be re-evaluated of one
        // clicks again on the corresponding menu item in the Workspace Explorer.

        String handle = event.getProperty(AnalysisController.HKEY_HANDLE);
        if (handle == null) {
            handle = event.getHistoryToken().get(AnalysisController.HKEY_HANDLE);
        }

        if(!StringUtil.hasText(handle)) {
            forceEval();
        }

        super.onPlaceChange(event);
    }
}
