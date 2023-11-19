/*
 * SpsAnalysisWidget.java
 *
 * Created on 25.03.2014
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.MainController;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.widget.NoValidationPopup;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.AnalysisController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config.Config;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.SpsAfterPropertiesSetEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.SpsAfterPropertiesSetHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.IsPrintable;
import de.marketmaker.iview.pmxml.DatabaseObject;
import de.marketmaker.iview.pmxml.MMClassIndex;

/**
 * @author mloesch
 */
public class SpsAnalysisWidget extends SpsBoundWidget<Widget, SpsGroupProperty> implements SpsAfterPropertiesSetHandler, IsPrintable, NoValidationPopup {
    public static final String OBJECT_ID_LEAF = "Id";  // $NON-NLS$
    public static final String ANALYSIS_ID_LEAF = "AnalysisId";  // $NON-NLS$

    private final AnalysisController lc = new AnalysisController("SPS-AW"); // $NON-NLS$
    private boolean prefixedHeader = false;

    public SpsAnalysisWidget() {
        EventBusRegistry.get().addHandler(SpsAfterPropertiesSetEvent.getType(), this);
    }

    @SuppressWarnings("unused")
    public SpsAnalysisWidget withPrefixedHeader() {
        this.prefixedHeader = true;
        return this;
    }

    @Override
    public void afterPropertiesSet() {
        final SpsGroupProperty group = getBindFeature().getSpsProperty();
        evaluate(SpsUtil.getLeafStringValue(group, OBJECT_ID_LEAF),
                SpsUtil.getLeafStringValue(group, ANALYSIS_ID_LEAF),
                SpsUtil.getLeafStringValue(group, "ClassIndex")  // $NON-NLS$
        );
    }

    private void evaluate(String id, String analysisId, String classIndex) {
        if (!StringUtil.hasText(analysisId)) {
            throw new IllegalStateException("no analysisId given!"); // $NON-NLS$
        }
        if (!StringUtil.hasText(classIndex)) {
            throw new IllegalStateException("no classIndex given!"); // $NON-NLS$
        }
        if (!StringUtil.hasText(id)) {
            throw new IllegalStateException("no id given!"); // $NON-NLS$
        }
        final HistoryToken ht = MainController.INSTANCE.getHistoryThreadManager().getActiveThreadHistoryItem().getPlaceChangeEvent().getHistoryToken();
        final DatabaseObject dbo = new DatabaseObject();
        dbo.setClassIdx(MMClassIndex.fromValue(classIndex));
        dbo.setId(id);
        final SafeHtml header = this.prefixedHeader ? MainController.INSTANCE.getView().getContentHeader() : null;
        this.lc.evaluate(Config.createWithDatabaseObject(ht, dbo, analysisId, header, PrivacyMode.isActive()));
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
        return this.lc.getView().asWidget();
    }

    @Override
    public String getPrintHtml() {
        return this.lc.getView().getPrintHtml();
    }
}
