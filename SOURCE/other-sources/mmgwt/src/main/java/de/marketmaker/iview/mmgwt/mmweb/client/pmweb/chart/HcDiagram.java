package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.chart;

import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import de.marketmaker.itools.gwtutil.client.widgets.highcharts.Highcharts;
import de.marketmaker.itools.gwtutil.client.widgets.notification.Notifications;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.pmxml.DTDiagram;
import org.moxieapps.gwt.highcharts.client.Chart;

/**
 * Author: umaurer
 * Created: 15.04.15
 */
public class HcDiagram extends Composite implements RequiresResize {
    private final SimpleLayoutPanel resizePanel;
    private final SimplePanel panel;
    private Chart chart;

    public HcDiagram(final DTDiagram diagram, final boolean fullscreen) {
        if (fullscreen) {
            this.resizePanel = new SimpleLayoutPanel();
            this.panel = this.resizePanel;
        }
        else {
            this.resizePanel = null;
            this.panel = new SimplePanel();
        }
        final ChartFactory factory = ChartFactory.getFactory(diagram);
        Highcharts.initialize(factory.getType(), new Callback<Void, Exception>() {
            @Override
            public void onFailure(Exception e) {
                Notifications.add(I18n.I.error(), "Could not initialize Chart API"); // $NON-NLS$
            }

            @Override
            public void onSuccess(Void aVoid) {
                setChart(factory.createChart(diagram, fullscreen));
            }
        });
        initWidget(this.panel);
    }

    @Override
    public void onResize() {
        if (this.resizePanel == null) {
            return;
        }
        if (this.chart == null) {
            return;
        }
        final int width = this.resizePanel.getOffsetWidth();
        final int height = this.resizePanel.getOffsetHeight();
        if (width == 0 || height == 0) {
            return;
        }

        this.chart.setSize(width, height);
        if (!this.chart.isAttached()) {
            this.resizePanel.setWidget(this.chart);
        }
    }

    private void setChart(Chart chart) {
        this.chart = chart;
        if (this.resizePanel != null) {
            onResize();
        }
        else {
            this.panel.setWidget(chart);
        }
    }
}
