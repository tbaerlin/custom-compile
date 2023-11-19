/*
 * EditWithObjectFactory.java
 *
 * Created on 28.07.2014 09:36
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.factory;

import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsPibKiidArchiveDownloadWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsObjectBoundWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsPibKiidAvailabilityIconWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsPibKiidAvailabilityWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsSelectDepotSymbolPicker;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsYieldRiskPicker;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.TextUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.EditWidgetWithObjectDesc;
import de.marketmaker.iview.pmxml.TiType;

/**
 * @author mdick
 */
public class EditWithObjectFactory extends Factory<EditWidgetWithObjectDesc> {
    public EditWithObjectFactory(String baseClass) {
        super(baseClass);
    }

    @Override
    SpsWidget doCreateSpsWidget(EditWidgetWithObjectDesc widgetDesc, Context context, BindToken parentToken) {
        final BindToken bindToken = BindToken.create(parentToken, widgetDesc.getBind());
        final BindToken objectBindToken = StringUtil.hasText(widgetDesc.getObjectBind()) ? BindToken.create(parentToken, widgetDesc.getObjectBind()) : null;

        final SpsObjectBoundWidget widget = createWidgetForType(widgetDesc, context, bindToken, objectBindToken);
        widget.setReadonly(widgetDesc.isIsReadonly() || context.isForceReadonly());
        widget.setCaption(widgetDesc.getCaption());

        if(objectBindToken != null) {
            widget.getObjectBindFeature().setContextAndTokens(context, parentToken, objectBindToken);
        }

        return widget;
    }

    private SpsObjectBoundWidget createWidgetForType(EditWidgetWithObjectDesc widgetDesc, Context context, BindToken bindToken, BindToken objectBindToken) {
        final TiType tiType = context.getTiType(bindToken);

        switch(tiType) {
            case TI_SHELL_MM:
                if (TextUtil.hasStyle(widgetDesc, "pickDepotSymbol")) {    // $NON-NLS$
                    return new SpsSelectDepotSymbolPicker(context.getSharedDmxmlContextSupplier(widgetDesc.getId(), false))
                            .withMainInput(context.getMainInput().asShellMMInfo())
                            .withVisible(!(widgetDesc.isIsReadonly() || context.isForceReadonly()))
                            .withoutObjectBind(objectBindToken == null);
                }
                if (TextUtil.hasStyle(widgetDesc, "pibKiidAvailability")) {  // $NON-NLS$
                    return new SpsPibKiidAvailabilityWidget(context.getSharedDmxmlContextSupplier("pibKiidAvail_5da7eae1", false));  // $NON-NLS$
                }
                if (TextUtil.hasStyle(widgetDesc, "pibKiidAvailabilityIcon")) {  // $NON-NLS$
                    return new SpsPibKiidAvailabilityIconWidget(context.getSharedDmxmlContextSupplier("pibKiidAvail_5da7eae1", false));  // $NON-NLS$
                }
                break;
            case TI_STRING:
                if (TextUtil.hasStyle(widgetDesc, "pibKiidArchiveDownload")) {  // $NON-NLS$
                    return new SpsPibKiidArchiveDownloadWidget().withVisible(!(widgetDesc.isIsReadonly() || context.isForceReadonly()));
                }
                else if(TextUtil.hasStyle(widgetDesc, "yieldRiskPicker")) {  // $NON-NLS$
                    final SpsYieldRiskPicker spsYieldRiskPicker = new SpsYieldRiskPicker();
                    spsYieldRiskPicker.setReadonly(widgetDesc.isIsReadonly());
                    return spsYieldRiskPicker;
                }
        }

        throw new IllegalStateException("EditFactoryWithObject: nothing to create!");  // $NON-NLS$
    }
}
