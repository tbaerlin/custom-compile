/*
 * LabelFactory.java
 *
 * Created on 02.06.2014 13:06
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.factory;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsGauge;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLabelWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsMiniBar;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsPibKiidAvailabilityIconWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsPibKiidAvailabilityWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.TextUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DataItemFormatter;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.DataContainerLeafNodeDeclaration;
import de.marketmaker.iview.pmxml.LabelWidgetDesc;
import de.marketmaker.iview.pmxml.MMNumber;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import de.marketmaker.iview.pmxml.TiType;

/**
 * @author Markus Dick
 */
public class LabelFactory extends Factory<LabelWidgetDesc> {
    public LabelFactory(String baseClass) {
        super(baseClass);
    }

    @Override
    SpsWidget doCreateSpsWidget(LabelWidgetDesc labelWidgetDesc, Context context, BindToken parentToken) {
        //docman dmxmlcontexts are shared between several widget classes
        if(TextUtil.hasStyle(labelWidgetDesc, "pibKiidAvailability")) {  // $NON-NLS$
            final SpsPibKiidAvailabilityWidget spsPibKiidAvailabilityWidget =
                    new SpsPibKiidAvailabilityWidget(context.getSharedDmxmlContextSupplier("pibKiidAvail_5da7eae1", false));  // $NON-NLS$
            spsPibKiidAvailabilityWidget.setCaption(labelWidgetDesc.getCaption());
            return spsPibKiidAvailabilityWidget;
        }
        if(TextUtil.hasStyle(labelWidgetDesc, "pibKiidAvailabilityIcon")) {  // $NON-NLS$
            final SpsPibKiidAvailabilityWidget spsPibKiidAvailabilityWidget =
                    new SpsPibKiidAvailabilityIconWidget(context.getSharedDmxmlContextSupplier("pibKiidAvail_5da7eae1", false));  // $NON-NLS$
            spsPibKiidAvailabilityWidget.setCaption(labelWidgetDesc.getCaption());
            return spsPibKiidAvailabilityWidget;
        }
        if (TextUtil.hasStyle(labelWidgetDesc, "miniBar")) { // $NON-NLS$
            final SpsMiniBar spsMiniBar = createMiniBar(labelWidgetDesc, context, parentToken);
            if (spsMiniBar != null) {
                return spsMiniBar;
            }
        }
        if (TextUtil.hasStyle(labelWidgetDesc, "gauge", "chartGauge")) { // $NON-NLS$
            if (TextUtil.hasStyle(labelWidgetDesc, "gauge")) {  // $NON-NLS$
                Firebug.warn("LabelFactory <doCreateSpsWidget> style 'gauge' is deprecated, use 'chartGauge' instead");
            }
            final SpsGauge spsGauge = createGauge(labelWidgetDesc, context, parentToken);
            if (spsGauge != null) {
                return spsGauge;
            }
        }
        final SpsLabelWidget spsLabelWidget = new SpsLabelWidget(labelWidgetDesc.getText());
        if (TextUtil.hasStyle(labelWidgetDesc, "useLabelTextAsIconTooltip")) { // $NON-NLS$
            spsLabelWidget.withUseLabelTextAsIconTooltip();
        }
        spsLabelWidget.setCaption(labelWidgetDesc.getCaption());
        if (StringUtil.hasText(labelWidgetDesc.getIconNameBind())) {
            final BindToken bindToken = BindToken.create(parentToken, labelWidgetDesc.getIconNameBind());
            spsLabelWidget.createIconNameBindFeature().setContextAndTokens(context, parentToken, bindToken);
        }
        return spsLabelWidget;
    }

    private SpsMiniBar createMiniBar(LabelWidgetDesc labelWidgetDesc, Context context, BindToken parentToken) {
        final String bindKey = labelWidgetDesc.getBind();
        final BindToken bindToken = BindToken.create(parentToken, bindKey);
        final ParsedTypeInfo pti = context.getLeafDeclaration(bindToken).getDescription();
        if (pti.getTypeId() != TiType.TI_NUMBER) {
            Firebug.warn("LabelFactory cannot create miniBar: tiType is " + pti.getTypeId() + " (should be TI_NUMBER)");
            return null;
        }

        if ("0".equals(pti.getMin()) && "0".equals(pti.getMax())) { // $NON-NLS$
            Firebug.warn("ParsedTypeInfo of MiniBar must contain min and max values"); // $NON-NLS$
            return null;
        }

        final DataItemFormatter dataItemFormatter = new DataItemFormatter(pti, labelWidgetDesc.getStyle());
        final double minValue = parseMinMax("Min", dataItemFormatter, pti.getMin()); // $NON-NLS$
        final double maxValue = parseMinMax("Max", dataItemFormatter, pti.getMax()); // $NON-NLS$
        final SpsMiniBar spsMiniBar = new SpsMiniBar(minValue, maxValue);
        spsMiniBar.setCaption(labelWidgetDesc.getCaption());
        return spsMiniBar;
    }

    private SpsGauge createGauge(LabelWidgetDesc labelWidgetDesc, Context context, BindToken parentToken) {
        final String bindKey = labelWidgetDesc.getBind();
        final BindToken bindToken = BindToken.create(parentToken, bindKey);
        final DataContainerLeafNodeDeclaration decl = context.getLeafDeclaration(bindToken);
        final ParsedTypeInfo pti = decl.getDescription();
        if (pti.getTypeId() != TiType.TI_NUMBER) {
            Firebug.warn("LabelFactory cannot create gauge: tiType is " + pti.getTypeId() + " (should be TI_NUMBER)");
            return null;
        }

        if ("0".equals(pti.getMin()) && "0".equals(pti.getMax())) { // $NON-NLS$
            Firebug.warn("ParsedTypeInfo of gauge must contain min and max values"); // $NON-NLS$
            return null;
        }

        final String titleText = labelWidgetDesc.getText();

        final MMNumber defaultNumber = MmTalkHelper.asMMNumber(decl.getDefaultValue());
        final double defaultValue = defaultNumber == null ? 0d : Double.parseDouble(defaultNumber.getValue());

        final DataItemFormatter dataItemFormatter = new DataItemFormatter(pti, labelWidgetDesc.getStyle());
        final double minValue = parseMinMax("Min", dataItemFormatter, pti.getMin()); // $NON-NLS$
        final double maxValue = parseMinMax("Max", dataItemFormatter, pti.getMax()); // $NON-NLS$
        final SpsGauge spsGauge = new SpsGauge(titleText, minValue, maxValue, defaultValue, dataItemFormatter);
        spsGauge.setCaption(labelWidgetDesc.getCaption());
        return spsGauge;
    }

    private double parseMinMax(String type, DataItemFormatter dataItemFormatter, String m) {
        if (m == null) {
            throw new NullPointerException("ParsedTypeInfo." + type + " of gauge value is null"); // $NON-NLS$
        }
        return dataItemFormatter.value(m).doubleValue();
    }
}
