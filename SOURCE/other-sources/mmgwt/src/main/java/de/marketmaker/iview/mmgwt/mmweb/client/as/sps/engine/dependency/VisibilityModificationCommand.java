/*
 * VisibilityModificationCommand.java
 *
 * Created on 08.10.2014 14:46
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.dependency;

import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.HasVisibility;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.pmxml.HasCode;
import de.marketmaker.iview.pmxml.MM;

/**
* @author mdick
*/
public class VisibilityModificationCommand extends AbstractSpsWidgetModificationCommand {
    private boolean inverted = false;
    private RegExp regExp;

    @Override
    public void setWidget(SpsWidget targetWidget) {
        super.setWidget(targetWidget);
    }

    @Override
    public void execute() {
        final HasVisibility targetWidget = getTargetWidget();
        final SpsProperty spsProperty = getBindFeature().getSpsProperty();
        if(spsProperty == null || !(spsProperty instanceof SpsLeafProperty)) {
            return;
        }
        final Boolean value = accept((SpsLeafProperty) spsProperty);
        if(this.inverted) {
            targetWidget.setVisible(Boolean.TRUE != value);
        }
        else {
            targetWidget.setVisible(Boolean.TRUE == value);
        }
    }

    private Boolean accept(SpsLeafProperty spsProperty) {
        final MM mm = spsProperty.getDataItem();
        if(this.regExp != null) {
            final String value = mm instanceof HasCode ? MmTalkHelper.asCode(mm) : MmTalkHelper.asString(mm);
            return this.regExp.test(value);
        }

        return MmTalkHelper.asBoolean(mm);
    }

    public VisibilityModificationCommand withInverted() {
        this.inverted = true;
        return this;
    }

    public VisibilityModificationCommand withRegExp(String regExp) {
        this.regExp = RegExp.compile(regExp);
        return this;
    }
}
