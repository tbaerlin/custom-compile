/*
 * SpsYieldRiskPicker.java
 *
 * Created on 18.05.2015 14:50
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.core.client.Callback;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import de.marketmaker.itools.gwtutil.client.widgets.highcharts.Highcharts;
import de.marketmaker.itools.gwtutil.client.widgets.notification.Notifications;
import de.marketmaker.iview.mmgwt.mmweb.client.Dimension;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.widget.NoValidationPopup;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.SpsAfterPropertiesSetEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.SpsAfterPropertiesSetHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.CssValues;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.tools.i18n.NonNLS;
import org.moxieapps.gwt.highcharts.client.Axis;
import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.ToolTipData;
import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;
import org.moxieapps.gwt.highcharts.client.events.PointSelectEvent;
import org.moxieapps.gwt.highcharts.client.events.PointSelectEventHandler;
import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;
import org.moxieapps.gwt.highcharts.client.labels.YAxisLabels;
import org.moxieapps.gwt.highcharts.client.plotOptions.SeriesPlotOptions;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * @author mdick
 */
@NonNLS
public class SpsYieldRiskPicker extends SpsObjectBoundWidget<FlowPanel, SpsLeafProperty, SpsGroupProperty> implements SpsAfterPropertiesSetHandler, NoValidationPopup {
    public static final String RISK_CLASS_KEY = "riskClass";
    private static final Dimension DEFAULT_SIZE = new Dimension(500, 300);

    private boolean highChartsInitialized = false;

    private FlowPanel panel;
    private Chart chart;
    private Dimension size = DEFAULT_SIZE;

    private YieldRiskData data;
    private String pendingValue;
    private HTML longDescription;

    public SpsYieldRiskPicker() {
        EventBusRegistry.get().addHandler(SpsAfterPropertiesSetEvent.getType(), this);
    }

    @Override
    public void setSize(Dimension size) {
        this.size = size == null ? DEFAULT_SIZE : size;
    }

    @Override
    public void onObjectPropertyChange() {
        final SpsGroupProperty groupProperty = getObjectBindFeature().getSpsProperty();

        if(groupProperty == null) {
            this.panel.setVisible(false);
            return;
        }

        this.data = new YieldRiskData();
        this.data.showXGrid = getBoolean("showXGrid", groupProperty);
        this.data.showYGrid = getBoolean("showYGrid", groupProperty);
        this.data.xAxisLabel = getString("xAxisLabel", groupProperty);
        this.data.yAxisLabel = getString("yAxisLabel", groupProperty);

        this.data.entries = getRiskClassEntries("riskClasses", groupProperty);

        this.panel.setVisible(true);

        if(this.chart != null) {
            updateChart();
        }
        else if(!this.highChartsInitialized){
            this.highChartsInitialized = true;

            Highcharts.initialize(Highcharts.Type.DEFAULT, new Callback<Void, Exception>() {
                @Override
                public void onFailure(Exception reason) {
                    Notifications.add(I18n.I.error(), I18n.I.spsErrorHighchartsInitFailed(reason.getMessage()));
                    panel.add(new Label(I18n.I.spsErrorHighchartsInitFailed("")));
                }

                @Override
                public void onSuccess(Void result) {
                    initChart();
                }
            });
        }
    }

    private boolean getBoolean(String propertyName, SpsGroupProperty groupProperty) {
        final SpsLeafProperty spsLeafProperty = (SpsLeafProperty) groupProperty.get(propertyName);
        return spsLeafProperty != null && MmTalkHelper.isTrue(spsLeafProperty.getDataItem());
    }

    private String getString(String propertyName, SpsGroupProperty groupProperty) {
        final SpsLeafProperty spsLeafProperty = (SpsLeafProperty) groupProperty.get(propertyName);
        if(spsLeafProperty == null) {
            return null;
        }
        return MmTalkHelper.asString(spsLeafProperty.getDataItem());
    }

    private Number getNumber(String propertyName, SpsGroupProperty groupProperty) {
        final SpsLeafProperty spsLeafProperty = (SpsLeafProperty) groupProperty.get(propertyName);
        if(spsLeafProperty == null) {
            return null;
        }
        final String s = MmTalkHelper.asString(spsLeafProperty.getDataItem());
        if(!StringUtil.hasText(s)) {
            return null;
        }
        return new BigDecimal(s);
    }

    private ArrayList<RiskClassEntry> getRiskClassEntries(String propertyName, SpsGroupProperty groupProperty) {
        final SpsListProperty spsListProperty = (SpsListProperty) groupProperty.get(propertyName);
        if(spsListProperty == null) {
            return null;
        }

        final ArrayList<RiskClassEntry> result = new ArrayList<>(spsListProperty.getChildCount());

        for (SpsProperty spsProperty : spsListProperty.getChildren()) {
            final SpsGroupProperty listEntry = (SpsGroupProperty)spsProperty;
            final RiskClassEntry riskClassEntry = new RiskClassEntry();

            riskClassEntry.riskClass = getString("riskClass", listEntry);
            riskClassEntry.label = getString("label", listEntry);
            riskClassEntry.shortDescription = getString("shortDescription", listEntry);
            riskClassEntry.longDescription = getString("longDescription", listEntry);
            riskClassEntry.x = getNumber("x", listEntry);
            riskClassEntry.y = getNumber("y", listEntry);

            result.add(riskClassEntry);
        }

        return result;
    }


    @Override
    public void onPropertyChange() {
        final SpsLeafProperty spsProperty = getBindFeature().getSpsProperty();
        if(spsProperty == null) {
            return;
        }
        final String stringValue = spsProperty.getStringValue();
        if(this.chart == null) {
            this.pendingValue = stringValue;
            return;
        }

        setSelectedValue(stringValue);
    }

    private void setSelectedValue(String stringValue) {
        for (Series series : this.chart.getSeries()) {
            for (Point point : series.getPoints()) {
                final JSONObject userData = point.getUserData();
                if (userData == null || userData.get(RISK_CLASS_KEY) == null || userData.get(RISK_CLASS_KEY).isNull() != null) {
                    continue;
                }
                final String riskClass = userData.get(RISK_CLASS_KEY).isString().stringValue();
                for (RiskClassEntry entry : this.data.entries) {
                    if(StringUtil.equals(riskClass, entry.riskClass) && StringUtil.hasText(entry.longDescription)) {
                        final SafeHtml descriptionHtml = new SafeHtmlBuilder()
                                .append(TextUtil.toSafeHtmlNoLineBreak(entry.label))
                                .appendEscaped(": ") // $NON-NLS$
                                .append(TextUtil.toSafeHtml(entry.longDescription))
                                .toSafeHtml();
                        longDescription.setHTML(descriptionHtml);
                        break;
                    }
                }
                if(StringUtil.equals(riskClass, stringValue)) {
                    point.select(true, false);
                    break;
                }
            }
        }
    }

    @Override
    protected FlowPanel createWidget() {
        if(this.panel != null) {
            return this.panel;
        }
        this.panel = new FlowPanel();
        return this.panel;
    }

    private void initChart() {
        final boolean readonly = isReadonly();

        final String[] chartColors = readonly ? CssValues.getReadonlyChartColors() : CssValues.getChartColors();

        final Chart chart = new Chart()
                .setType(Series.Type.SPLINE)
                .setSize(this.size.getWidth(), this.size.getHeight())
                .setColors(chartColors)
                .setChartTitleText(null)
                .setCredits(new Credits().setEnabled(false))
                .setOption("/legend/enabled", false)
                .setSeriesPlotOptions(new SeriesPlotOptions().setPointSelectEventHandler(new PointSelectEventHandler() {
                    @Override
                    public boolean onSelect(PointSelectEvent pointSelectEvent) {
                        return doOnSelect(pointSelectEvent.getPoint());
                    }
                })).setToolTip(new ToolTip().setFormatter(new ToolTipFormatter() {
                    @Override
                    public String format(ToolTipData toolTipData) {
                        return formatTooltip(toolTipData.getPoint());
                    }
                }));

        chart.getXAxis()
                .setType(Axis.Type.LINEAR)
                .setAxisTitle(new AxisTitle().setText(this.data.xAxisLabel).setOption("offset", 10))
                .setLabels(new XAxisLabels().setEnabled(false))
                .setOption("tickLength", 0)
                .setLineWidth(1)
                .setOffset(0)
                .setGridLineWidth(this.data.showXGrid ? 1 : 0).setMin(0)
        ;

        chart.getYAxis()
                .setType(Axis.Type.LINEAR)
                .setAxisTitle(new AxisTitle().setText(this.data.yAxisLabel).setOption("offset", 10))
                .setLabels(new YAxisLabels().setEnabled(false))
                .setLineWidth(1)
                .setOffset(0)
                .setGridLineWidth(this.data.showYGrid ? 1 : 0).setMin(0)
        ;

        final Series series = chart.createSeries()
                .setPoints(createPoints())
                .setOption("/states/hover/halo/size", 32)
                .setOption("/marker/radius", 15)
                .setOption("/marker/states/hover/radiusPlus", 10)
                .setOption("/marker/states/hover/enabled", !readonly)
                .setOption("/marker/states/select/radius", 25)
                .setOption("/marker/states/select/lineColor", chartColors[0])
                .setOption("/marker/states/select/lineWidth", 2)
                .setOption("allowPointSelect", !readonly)
                .setOption("/dataLabels/enabled", true)
                .setOption("/dataLabels/format", "{point.name}")
                .setOption("/dataLabels/y", 28)
                .setOption("/dataLabels/verticalAlign", "top")
                .setOption("/dataLabels/align", "left")
                ;
        if(!readonly) {
            series.setOption("cursor", "pointer");
        }

        chart.addSeries(series);

        this.chart = chart;
        this.panel.add(chart);

        this.longDescription = new HTML();
        this.panel.add(this.longDescription);

        if(StringUtil.hasText(this.pendingValue)) {
            setSelectedValue(this.pendingValue);
            this.pendingValue = null;
        }
    }

    private void updateChart() {
        this.chart.getXAxis()
                .setAxisTitle(new AxisTitle().setText(this.data.xAxisLabel).setOption("offset", 10), false)
                .setGridLineWidth(this.data.showXGrid ? 1 : 0)
        ;

        this.chart.getYAxis()
                .setAxisTitle(new AxisTitle().setText(this.data.yAxisLabel).setOption("offset", 10), false)
                .setGridLineWidth(this.data.showYGrid ? 1 : 0)
        ;

        this.chart.getSeries()[0].setPoints(createPoints(), false);

        this.chart.redraw();

        this.longDescription.setText(null);

        this.pendingValue = MmTalkHelper.asString(getBindFeature().getSpsProperty().getDataItem());

        if(StringUtil.hasText(this.pendingValue)) {
            setSelectedValue(this.pendingValue);
            this.pendingValue = null;
        }
    }

    private String formatTooltip(Point point) {
        final JSONObject userData = point.getUserData();

        if (userData == null || userData.get(RISK_CLASS_KEY) == null || userData.get(RISK_CLASS_KEY).isNull() != null) {
            return null;
        }

        final String riskClass = userData.get(RISK_CLASS_KEY).isString().stringValue();

        for (RiskClassEntry entry : data.entries) {
            if(StringUtil.equals(entry.riskClass, riskClass) && StringUtil.hasText(entry.shortDescription)) {
                return StringUtil.wrapLongLine(entry.shortDescription, 32, "<br/>");
            }
        }

        return null;
    }

    private boolean doOnSelect(Point selectedPoint) {
        if(isReadonly()) {
            return true;
        }
        if(selectedPoint == null) {
            return false;
        }

        final JSONObject userData = selectedPoint.getUserData();
        if((userData == null || userData.get(RISK_CLASS_KEY) == null || userData.get(RISK_CLASS_KEY).isNull() != null)
                && getBindFeature().getSpsProperty().getParsedTypeInfo().isDemanded()) {
            return false;
        }

        if(userData != null && userData.get(RISK_CLASS_KEY) != null && userData.get(RISK_CLASS_KEY).isString() != null) {
            getBindFeature().getSpsProperty().setValue(userData.get(RISK_CLASS_KEY).isString().stringValue());
        }
        else {
            getBindFeature().getSpsProperty().setValue((String)null);
        }

        return true;
    }

    private Point[] createPoints() {
        final Point[] points = new Point[this.data.entries.size()];
        int i = 0;
        for (RiskClassEntry entry : this.data.entries) {
            final Point point = new Point(entry.x, entry.y).setName(TextUtil.toSafeHtml(entry.label).asString());
            if(StringUtil.hasText(entry.riskClass)) {
                JSONObject o = new JSONObject();
                o.put(RISK_CLASS_KEY, new JSONString(entry.riskClass));
                point.setUserData(o);
            }
            else {
                point.setOption("/marker/enabled", false)
                        .setOption("/marker/states/hover/enabled", false)
                        .setOption("/marker/states/select/enabled", false);
            }
            points[i++] = point;
        }
        return points;
    }

    @Override
    public void afterPropertiesSet() {
        // necessary, because even if a child of the group has been changed, the group will never fire a change event.
        onObjectPropertyChange();
    }

    private static class RiskClassEntry {
        private String riskClass;
        private String label;
        private String shortDescription;
        private String longDescription;

        private Number x;
        private Number y;

        public RiskClassEntry() {
        }

        @SuppressWarnings("unused")
        public RiskClassEntry(Number x, Number y) {
            this(null, null, x, y, null, null);
        }

        @SuppressWarnings("unused")
        public RiskClassEntry(String label, String riskClass, Number x, Number y, String shortDescription, String longDescription) {
            this.label = label;
            this.riskClass = riskClass;
            this.x = x;
            this.y = y;
            this.shortDescription = shortDescription;
            this.longDescription = longDescription;
        }

        @Override
        public String toString() {
            return "RiskClassEntry{" +
                    "riskClass='" + riskClass + '\'' +
                    ", label='" + label + '\'' +
                    ", shortDescription='" + shortDescription + '\'' +
                    ", longDescription='" + longDescription + '\'' +
                    ", x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

    private static class YieldRiskData {
        private String xAxisLabel;
        private boolean showXGrid = false;

        private String yAxisLabel;
        private boolean showYGrid = false;

        private ArrayList<RiskClassEntry> entries;

        @Override
        public String toString() {
            return "YieldRiskData{" +
                    "xAxisLabel='" + xAxisLabel + '\'' +
                    ", showXGrid=" + showXGrid +
                    ", yAxisLabel='" + yAxisLabel + '\'' +
                    ", showYGrid=" + showYGrid +
                    ", entries=" + entries +
                    '}';
        }
    }
}
