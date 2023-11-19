/*
 * ParameterConfigPopup.java
 *
 * Created on 08.05.15
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.PopupPanelFix;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.AnalysisMetadataForm;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ToolbarPopupPanel;
import de.marketmaker.iview.pmxml.LayoutDesc;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mloesch
 */
public class ParameterConfigPopup {

    private final PopupPanel popupPanel;

    public interface Apply {
        void onApply(HashMap<String, String> params);
    }

    public ParameterConfigPopup(LayoutDesc desc, Map<String, String> params, final Apply apply) {
        final FlowPanel panel = new FlowPanel();
        this.popupPanel = new ToolbarPopupPanel(true);
        this.popupPanel.addAttachHandler(event -> PopupPanelFix.addFrameDummy(this.popupPanel));
        this.popupPanel.setWidget(panel);

        final AnalysisMetadataForm metadataForm = new AnalysisMetadataForm(desc, params, false, this.popupPanel);
        panel.add(metadataForm);
        final FlexTable table = new FlexTable();
        table.setCellPadding(0);
        table.setCellSpacing(10);
        table.setWidget(0, 0, Button.text(I18n.I.accept()).clickHandler(event -> {
            this.popupPanel.hide();
            apply.onApply(metadataForm.getLayoutParameters());
        }).build());
        table.setWidget(0, 1, Button.text(I18n.I.reset()).clickHandler(event -> metadataForm.resetToDefaults()).build());
        table.setWidget(0, 2, Button.text(I18n.I.cancel()).clickHandler(event -> this.popupPanel.hide()).build());
        panel.add(table);
    }

    public PopupPanel getPopupPanel() {
        return this.popupPanel;
    }
}
