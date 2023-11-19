package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.ui.SimplePanel;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.highcharts.Highcharts;
import de.marketmaker.itools.gwtutil.client.widgets.notification.Notifications;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.util.CssValues;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.StockChart;
import org.moxieapps.gwt.highcharts.client.events.AxisSetExtremesEvent;
import org.moxieapps.gwt.highcharts.client.events.AxisSetExtremesEventHandler;

/**
 * Author: umaurer
 * Created: 14.01.15
 */
public class HighchartSnippetView extends SnippetView<HighchartSnippet> {
    private final SimplePanel panel = new SimplePanel();
    private StockChart chart;
    private Series series;
    private Series.Type lastType;

    protected HighchartSnippetView(final HighchartSnippet snippet) {
        super(snippet);
        Highcharts.initialize(Highcharts.Type.DEFAULT, new Callback<Void, Exception>() {
            @Override
            public void onFailure(Exception e) {
                Notifications.add(I18n.I.error(), "Could not initialize Chart API"); // $NON-NLS$
            }

            @Override
            public void onSuccess(Void aVoid) {
                createNewChart();
            }
        });
    }

    private void createNewChart() {
        this.chart = new StockChart()
                .setOption("scrollbar/liveRedraw", false) // $NON-NLS$
                .setColors(CssValues.getChartColors());
        this.chart.getXAxis().setAxisSetExtremesEventHandler(new AxisSetExtremesEventHandler() {
            @Override
            public boolean onSetExtremes(AxisSetExtremesEvent e) {
                chart.showLoading("loading data"); // TODO: $NON-NLS$
                final MmJsDate min = new MmJsDate(e.getMin().longValue());
                final MmJsDate max = new MmJsDate(e.getMax().longValue());
                snippet.loadNewExtremes(min, max);
                return false;
            }
        });
        this.series = this.chart.createSeries();
        this.series.setType(Series.Type.OHLC);
//        this.series.setOption("dataGrouping/enabled", false); // $NON-NLS$
        this.chart.addSeries(this.series);
        this.chart.getNavigator().setAdaptToUpdatedData(false);
    }

    private void createNewSeries(Series.Type type) {
        if (this.lastType == type) {
            return;
        }
        if (this.series != null) {
            this.chart.removeSeries(this.series);
        }
        this.series = this.chart.createSeries();
        this.series.setType(type);
        this.chart.addSeries(this.series);
        this.lastType = type;
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();
        this.container.setContentWidget(this.panel);
    }

    public void showMessage(String message) {
        this.chart.showLoading(message);
    }

    public void clear() {
    }

    public void update(final Point[] points, final boolean refreshChart) {
        Highcharts.initialize(Highcharts.Type.DEFAULT, new Callback<Void, Exception>() {
            @Override
            public void onFailure(Exception e) {
                Notifications.add(I18n.I.error(), "Could not initialize Chart API"); // $NON-NLS$
            }

            @Override
            public void onSuccess(Void aVoid) {
                if (refreshChart) {
                    createNewChart();
                    panel.setWidget(chart);
                }
                if (points.length > 0) {
                    if (points[0].getY() != null) {
                        createNewSeries(Series.Type.LINE);
                    }
                    else {
                        createNewSeries(Series.Type.OHLC);
                    }
                }
                chart.hideLoading();
                series.setPoints(points);
            }
        });
    }
}
