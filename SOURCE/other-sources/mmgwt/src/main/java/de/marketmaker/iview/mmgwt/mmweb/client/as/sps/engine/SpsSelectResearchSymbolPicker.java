/*
 * SpsSelectResearchSymbolPicker.java
 *
 * Created on 18.07.2014 12:59
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.research.SelectSymbolPresenter;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.research.SelectSymbolView;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author mdick
 */
@NonNLS
public class SpsSelectResearchSymbolPicker extends SpsBoundWidget<Button, SpsLeafProperty> implements ClickHandler {
    private final Button button = Button.icon("pm-research-list") // $NON-NLS$
            .clickHandler(this)
            .build();
    private final String layoutGuid;

    private String activityInstanceId = null;

    private SelectSymbolPresenter picker;

    public SpsSelectResearchSymbolPicker(String layoutGuid) {
        this.layoutGuid = layoutGuid;
    }

    public SpsSelectResearchSymbolPicker withVisible(boolean visible) {
        this.button.setEnabled(visible);
        this.button.setVisible(visible);
        return this;
    }

    @Override
    public void onPropertyChange() {
        //do nothing
    }

    @Override
    public void onClick(ClickEvent event) {
        if(this.picker == null) {
            this.picker = new SelectSymbolPresenter(new SelectSymbolView(), layoutGuid);
            if(this.activityInstanceId != null) {
                this.picker.setActivityInstanceId(activityInstanceId);
            }
        }
        this.picker.show(this::updateProperty);
    }

    private void updateProperty(ShellMMInfo shellMMInfo) {
        final SpsLeafProperty spsProperty = getBindFeature().getSpsProperty();
        if(spsProperty != null) {
            spsProperty.setValue(shellMMInfo);
        }
    }

    private void updateButtonCaption() {
        if(hasCaption()) {
            this.button.setHTML(getCaption());
        }
    }

    @Override
    protected HTML createCaptionWidget() {
        return isForceCaptionWidget() ? new HTML() : null;
    }

    @Override
    protected Button createWidget() {
        updateButtonCaption();
        return this.button;
    }

    public SpsSelectResearchSymbolPicker withActivityInstanceId(String activityInstanceId) {
        this.activityInstanceId = activityInstanceId;
        return this;
    }
}
