package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.chart;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.highcharts.Highcharts;
import de.marketmaker.iview.mmgwt.mmweb.client.util.CssValues;
import de.marketmaker.iview.pmxml.DTAxisRef;
import de.marketmaker.iview.pmxml.DTAxisSpec;
import de.marketmaker.iview.pmxml.DTDiagram;
import de.marketmaker.iview.pmxml.DTPoint;
import de.marketmaker.iview.pmxml.DTSeries;
import de.marketmaker.iview.pmxml.TeeSeriesTypes;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartSubtitle;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.Series;

import java.util.ArrayList;
import java.util.List;

import static com.google.gwt.dom.client.Style.Unit.PCT;

/**
 * Author: umaurer
 * Created: 15.04.15
 */
public abstract class ChartFactory {
    private static final int SMALL_CHART_WIDTH = 500;
    private static final int SMALL_CHART_HEIGHT = 200;

    public static ChartFactory getFactory(DTDiagram diagram) {
        if (diagram == null) {
            return null;
        }
        final List<DTSeries> series = diagram.getSeries();
        if (series == null || series.isEmpty()) {
            return null;
        }
        return getFactory(series.get(0).getSeriesType());
    }

    public static ChartFactory getFactory(TeeSeriesTypes dtType) {
        switch (dtType) {
            case TST_LINE_SERIES:
            case TST_AREA_SERIES:
            case TST_POINT_SERIES:
                return new PointChartFactory();

            case TST_PIE_SERIES:
            case TST_DONUT_SERIES:
                return new PieChartFactory();

            case TST_BAR_SERIES:
            case TST_HORIZ_BAR_SERIES:
                return new BarChartFactory();

            default:
                return null;
        }
    }

    protected Highcharts.Type getType() {
        // overwrite this in subclasses, if other type is needed
        return Highcharts.Type.DEFAULT;
    }

    public abstract Chart createChart(DTDiagram diagram, boolean fullscreen);

    protected Chart createChart(Series.Type type, String title, boolean fullscreen) {
        final Chart chart = new Chart()
                .setType(type)
                .setTitle(new ChartTitle().setText(title), new ChartSubtitle())
                .setColors(CssValues.getChartColors())
                .setCredits(new Credits().setEnabled(false));
        if (fullscreen) {
            chart.addAttachHandler(new AttachEvent.Handler() {
                @Override
                public void onAttachOrDetach(AttachEvent attachEvent) {
                    setChartContainerFullscreen(chart.getElement());

                }
            });
        }
        else {
            Firebug.log("createHcChart(" + type + ", \"" + title + "\", noFs, " + SMALL_CHART_WIDTH + ", " + SMALL_CHART_HEIGHT + ")");
            chart.setSize(SMALL_CHART_WIDTH, SMALL_CHART_HEIGHT);
        }
        return chart;
    }

    private void setChartContainerFullscreen(Element chartElement) {
        Element child = chartElement.getFirstChildElement();
        while (child != null) {
            if ("highcharts-container".equals(child.getAttribute("class"))) { // $NON-NLS$
                child.getStyle().setWidth(100, PCT);
                child.getStyle().setHeight(100, PCT);
                return;
            }
            child = child.getNextSiblingElement();
        }
        Firebug.warn("setChartContainerFullscreen() -> highcharts-container not found");
    }

    protected List<DTAxisSpec> getAxes(List<DTAxisSpec> axes, List<DTAxisRef> axisRefs) {
        final ArrayList<DTAxisSpec> list = new ArrayList<>(axisRefs.size());
        for (DTAxisRef axisRef : axisRefs) {
            list.add(axes.get(Integer.parseInt(axisRef.getAxisIndex())));
        }
        return list;
    }

    protected List<DTPoint> getDataPoints(DTSeries series, boolean showGroups) {
        return showGroups
                ? series.getGroups()
                : series.getPoints();
    }

    protected JSONObject createUserData(int seriesId, int pointId) {
        final JSONObject userData = new JSONObject();
        userData.put("seriesId", new JSONNumber(seriesId)); // $NON-NLS$
        userData.put("pointId", new JSONNumber(pointId)); // $NON-NLS$
        return userData;
    }

}
