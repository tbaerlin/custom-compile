package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

import static com.google.gwt.dom.client.Style.Position.ABSOLUTE;
import static com.google.gwt.dom.client.Style.Position.RELATIVE;
import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * Author: umaurer
 * Created: 20.03.15
 *
 * This panel allows to add widgets at the top without specifying their size
 * and a widget below, which uses the rest of the height
 */
public class TopBorderPanel implements IsWidget {
    private final FlowPanel panel = new FlowPanel();
    private final FlowPanel northPanel = new FlowPanel();
    private final ScrollPanel scrollPanel = new ScrollPanel();

    public TopBorderPanel() {
        this.panel.getElement().getStyle().setPosition(RELATIVE);
        final Style spStyle = this.scrollPanel.getElement().getStyle();
        spStyle.setPosition(ABSOLUTE);
        spStyle.setTop(0, PX);
        spStyle.setRight(0, PX);
        spStyle.setBottom(0, PX);
        spStyle.setLeft(0, PX);
        final ContentResizePanel crp = new ContentResizePanel();
        crp.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                spStyle.setTop(event.getHeight(), PX);
            }
        });
        crp.setContentWidget(this.northPanel);
        this.panel.add(crp);
        this.panel.add(this.scrollPanel);
    }

    public void setStyleName(String style) {
        this.panel.setStyleName(style);
    }

    @Override
    public Widget asWidget() {
        return this.panel;
    }

    public void addNorth(Widget widget) {
        this.northPanel.add(widget);
    }

    public void setWidget(Widget widget) {
        this.scrollPanel.setWidget(widget);
    }
}
