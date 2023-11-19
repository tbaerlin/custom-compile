package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.MainController;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.widget.NoValidationPopup;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.AnalysisController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config.Config;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.SpsAfterPropertiesSetEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.SpsAfterPropertiesSetHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.IsPrintable;

/**
 * Created on 25.03.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class SpsAnalysisResultWidget extends SpsBoundWidget<Widget, SpsGroupProperty> implements SpsAfterPropertiesSetHandler, IsPrintable, NoValidationPopup {
    private final AnalysisController lc = new AnalysisController("SPS-ARW"); // $NON-NLS$
    private boolean prefixedHeader = false;

    public SpsAnalysisResultWidget() {
        this.lc.getView().hideProgressPanelCancelButton();
        this.lc.getView().setProgressPanelBackgroundButtonVisible(false);
        EventBusRegistry.get().addHandler(SpsAfterPropertiesSetEvent.getType(), this);
    }

    @SuppressWarnings("unused")
    public SpsAnalysisResultWidget withPrefixedHeader() {
        this.prefixedHeader = true;
        return this;
    }

    @Override
    public void afterPropertiesSet() {
        final SpsGroupProperty group = getBindFeature().getSpsProperty();
        evaluate(
                ((SpsLeafProperty) group.get("AsyncHandle")).getStringValue(), // $NON-NLS$
                ((SpsLeafProperty) group.get("AnalysisId")).getStringValue() // $NON-NLS$
        );
    }

    private void evaluate(String asyncHandle, String analysisId) {
        if (!StringUtil.hasText(asyncHandle)) {
            throw new IllegalStateException("no handle given!"); // $NON-NLS$
        }
        if (!StringUtil.hasText(analysisId)) {
            throw new IllegalStateException("no analysisId given!"); // $NON-NLS$
        }
        final HistoryToken ht = MainController.INSTANCE.getHistoryThreadManager().getActiveThreadHistoryItem().getPlaceChangeEvent().getHistoryToken();
        final SafeHtml header = this.prefixedHeader ? MainController.INSTANCE.getView().getContentHeader() : null;
        this.lc.fetch(Config.createWithHandle(ht, asyncHandle, analysisId, header));
    }

    @Override
    public void onPropertyChange() {
        //nothing to do. one would implement it, if there's a list or group which can be changed(add/delete)
    }

    @Override
    protected HTML createCaptionWidget() {
        return null;
    }

    @Override
    protected SimplePanel createInfoIconPanel() {
        return null;
    }

    @Override
    protected Widget createWidget() {
        return this.lc.getView().getContentPanel();
    }

    @Override
    public String getPrintHtml() {
        return this.lc.getView().getPrintHtml();
    }
}
