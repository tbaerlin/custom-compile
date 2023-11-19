package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.chart;

import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.ui.SimplePanel;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.widgets.highcharts.Highcharts;
import de.marketmaker.itools.gwtutil.client.widgets.notification.Notifications;
import de.marketmaker.iview.mmgwt.mmweb.client.Dimension;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsBoundWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsGroupProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsListProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.TextUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.list.SpsListBindFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.widget.NoValidationPopup;
import de.marketmaker.iview.mmgwt.mmweb.client.data.ContainerConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DataItemFormatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.DataContainerGroupNode;
import de.marketmaker.iview.pmxml.DataContainerLeafNodeDeclaration;
import de.marketmaker.iview.pmxml.DataContainerListNode;
import de.marketmaker.iview.pmxml.ListWidgetDescColumn;
import de.marketmaker.iview.pmxml.TiType;
import org.moxieapps.gwt.highcharts.client.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: umaurer
 * Created: 24.07.14
 */
public class SpsChart<P extends SpsProperty> extends SpsBoundWidget<SimplePanel, P> implements NoValidationPopup {
    private final SpsListBindFeature spsListBindFeature;
    private final SimplePanel panel = new SimplePanel();
    private final ChartFactory chartFactory;
    private String bindKeyLabel = null;
    private boolean labelIsDate = false;
    private final List<String> listBindKeyValue = new ArrayList<>();
    private final String[] columnNames;
    private List<DataItemFormatter> listDataItemFormatter = new ArrayList<>();
    private Dimension size = null;
    private int colSpan = -1;

    public SpsChart(Context context, BindToken parentToken, BindToken itemsBindToken, List<ListWidgetDescColumn> columns, ChartFactory chartFactory) {
        final DataContainerGroupNode entryDecl = (DataContainerGroupNode) ((DataContainerListNode) context.getDeclaration(itemsBindToken)).getChildren().get(0);
        final List<String> listColumnNames = new ArrayList<>(columns.size());
        for (final ListWidgetDescColumn column : columns) {
            final String fieldName = column.getFieldName();
            final DataContainerLeafNodeDeclaration decl = (DataContainerLeafNodeDeclaration) SpsUtil.getDeclChild(entryDecl, fieldName);
            if (decl == null) {
                throw new NullPointerException("SpsChart - no declaration found for field \"" + fieldName + "\": " + itemsBindToken); // $NON-NLS$
            }
            final TiType typeId = decl.getDescription().getTypeId();
            if (typeId == TiType.TI_STRING) {
                this.bindKeyLabel = fieldName;
            }
            else if (typeId == TiType.TI_DATE) {
                this.bindKeyLabel = fieldName;
                this.labelIsDate = true;
            }
            else if (typeId == TiType.TI_NUMBER) {
                this.listBindKeyValue.add(fieldName);
                listColumnNames.add(column.getColumnName());
                this.listDataItemFormatter.add(new DataItemFormatter(decl.getDescription(), null).withTrailingZeros(true));
            }
            else {
                throw new RuntimeException("SpsChart - unhandled column type: " + typeId); // $NON-NLS$
            }
        }
        this.columnNames = listColumnNames.toArray(new String[listColumnNames.size()]);
        if (this.bindKeyLabel == null) {
            throw new RuntimeException("SpsChart - no column corresponds to a TI_STRING field - " + itemsBindToken); // $NON-NLS$
        }
        if (this.listBindKeyValue.isEmpty()) {
            throw new RuntimeException("SpsChart - no column corresponds to a TI_NUMBER field - " + itemsBindToken); // $NON-NLS$
        }
        if (this.listBindKeyValue.size() > chartFactory.getMaxValueCount()) {
            throw new RuntimeException("SpsChart - TI_NUMBER column count exceeds maximum - " + itemsBindToken); // $NON-NLS$
        }
        this.chartFactory = chartFactory;

        this.spsListBindFeature = new SpsListBindFeature(context, parentToken, itemsBindToken) {
            @Override
            public void onChange() {
                Highcharts.initialize(Highcharts.Type.DEFAULT, new Callback<Void, Exception>() {
                    @Override
                    public void onFailure(Exception e) {
                        Notifications.add(I18n.I.error(), I18n.I.spsErrorHighchartsInitFailed(e.getMessage()));
                    }

                    @Override
                    public void onSuccess(Void aVoid) {
                        onItemsChange();
                    }
                });
            }
        };
    }

    @Override
    public void setContainerConfig(ContainerConfig containerConfig) {
        super.setContainerConfig(containerConfig);
        final int colSpan = containerConfig.getInt("colSpan", 1); // $NON-NLS$
        if (this.colSpan != -1 && this.colSpan != colSpan) {
            onItemsChange();
        }
        this.colSpan = colSpan;
    }

    @Override
    public void setSize(Dimension size) {
        this.size = size;
    }

    public boolean isLabelIsDate() {
        return labelIsDate;
    }

    private void onItemsChange() {
        final SpsListProperty itemListProperty = this.spsListBindFeature.getSpsProperty();
        final ChartWrapper chartWrapper = this.chartFactory.createChartWrapper();
        chartWrapper.configureDefaults(this, this.size);
        chartWrapper.configure(this, this.listDataItemFormatter.get(0));
        final List<SpsProperty> children = itemListProperty.getChildren();
        final int valueCount = this.listBindKeyValue.size();
        final int entryCount = children.size();
        final Point[][] points = new Point[valueCount][entryCount];
        final String[] categories;
        final Number[] dates;
        if (this.labelIsDate) {
            categories = null;
            dates = new Number[entryCount];
        }
        else {
            categories = new String[entryCount];
            dates = null;
        }
        for (int entryIndex = 0; entryIndex < entryCount; entryIndex++) {
            final SpsProperty spsProperty = children.get(entryIndex);
            if (!(spsProperty instanceof SpsGroupProperty)) {
                throw new IllegalStateException("not group property: " + spsProperty.getBindToken()); // $NON-NLS$
            }
            final SpsGroupProperty gp = (SpsGroupProperty) spsProperty;
            final SpsProperty spsPropLabel = gp.get(this.bindKeyLabel);
            if (spsPropLabel == null) {
                throw new RuntimeException("SpsChart - field \"" + this.bindKeyLabel + "\" not specified - " + gp.getBindToken()); // $NON-NLS$
            }
            final String category;
            final Number date;
            if (this.labelIsDate) {
                assert dates != null;
                category = null;
                date = JsDateFormatter.parseDdmmyyyy(((SpsLeafProperty) spsPropLabel).getDate().getValue()).getTime();
                dates[entryIndex] = date;
            }
            else {
                assert categories != null;
                category = ((SpsLeafProperty) spsPropLabel).getStringValue();
                categories[entryIndex] = TextUtil.toSafeHtml(category).asString();
                date = null;
            }

            for (int valueIndex = 0; valueIndex < valueCount; valueIndex++) {
                points[valueIndex][entryIndex] = createPoint(chartWrapper, gp, valueIndex, category, date);
            }
        }
        chartWrapper.drawChart(this.columnNames, categories, points, getBindFeature());
        getWidget().setWidget(chartWrapper);
    }

    private Point createPoint(ChartWrapper chartWrapper, SpsGroupProperty gp, int valueIndex, String category, Number date) {
        final String bindKeyValue = this.listBindKeyValue.get(valueIndex);
        final SpsProperty spsPropValue = gp.get(bindKeyValue);
        if (spsPropValue == null) {
            throw new RuntimeException("SpsChart - field \"" + bindKeyValue + "\" not specified - " + gp.getBindToken()); // $NON-NLS$
        }
        final SpsLeafProperty leafProperty = (SpsLeafProperty) spsPropValue;
        final String stringValue = leafProperty.getStringValue();
        if(!StringUtil.hasText(stringValue)) {
            return chartWrapper.createPoint(null, category, date);
        }
        final DataItemFormatter dataItemFormatter = this.listDataItemFormatter.get(valueIndex);
        final Number value = dataItemFormatter.value(leafProperty.getDataItem());
        final String formattedValue = dataItemFormatter.format(leafProperty.getDataItem());
        final Point point = chartWrapper.createPoint(value, category, date);
        if (formattedValue != null) {
            point.setText(formattedValue);
        }
        return point;
    }

    private float getValue(SpsGroupProperty gp, int valueIndex) {
        final String bindKeyValue = this.listBindKeyValue.get(valueIndex);
        final SpsProperty spsPropValue = gp.get(bindKeyValue);
        if (spsPropValue == null) {
            throw new RuntimeException("SpsChart - field \"" + bindKeyValue + "\" not specified - " + gp.getBindToken()); // $NON-NLS$
        }
        final String stringValue = ((SpsLeafProperty) spsPropValue).getStringValue();
        if(!StringUtil.hasText(stringValue)) {
            return Float.NaN;
        }
        return Float.parseFloat(stringValue);
    }

    @Override
    public void onPropertyChange() {
        // ignore
    }

    @Override
    protected SimplePanel createWidget() {
        return this.panel;
    }
}
