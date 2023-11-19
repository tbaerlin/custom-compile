package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.chart;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.ImageSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

import static com.google.gwt.dom.client.Style.Unit.PCT;
import static com.google.gwt.dom.client.Style.Unit.PX;
import static com.google.gwt.dom.client.Style.VerticalAlign.TOP;

/**
 * Author: umaurer
 * Created: 21.04.15
 */
public class MaximizePanel extends Composite implements RequiresResize {
    private final Widget widget;

    public MaximizePanel(Widget widget, boolean fullscreen, ClickHandler handler) {
        this.widget = widget;
        final String iconClass = fullscreen ? "as-tool-resize-min" : "as-tool-resize-max"; // $NON-NLS$
        final Button button = Button.icon(iconClass)
                .tooltip(fullscreen ? I18n.I.pmChartMinimize() : I18n.I.pmChartMaximize())
                .clickHandler(handler)
                .build();
        button.setVisible(false);

        final Grid grid = new Grid(1, 2);
        final HTMLTable.CellFormatter cellFormatter = grid.getCellFormatter();
        grid.setCellSpacing(0);
        grid.setCellPadding(0);
        grid.setWidget(0, 0, button);
        grid.setWidget(0, 1, widget);
        cellFormatter.getElement(0, 0).getStyle().setVerticalAlign(TOP);
        grid.getColumnFormatter().setWidth(0, IconImage.getImageResource(iconClass).getWidth() + 4 + "px"); // 1px margin and 1px padding = 4px  // $NON-NLS$
        grid.addDomHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent mouseOverEvent) {
                button.setVisible(true);
            }
        }, MouseOverEvent.getType());
        grid.addDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent mouseOutEvent) {
                button.setVisible(false);
            }
        }, MouseOutEvent.getType());
        if (fullscreen) {
            cellFormatter.getElement(0, 1).getStyle().setPosition(Style.Position.RELATIVE);
            setFullscreen(widget.getElement().getStyle());
            grid.setSize("100%", "100%"); // $NON-NLS$
        }
        initWidget(grid);
    }

    private void setFullscreen(Style style) {
        style.setPosition(Style.Position.ABSOLUTE);
        style.setWidth(100, PCT);
        style.setHeight(100, PCT);
        style.setTop(0, PX);
        style.setRight(0, PX);
        style.setBottom(0, PX);
        style.setLeft(0, PX);
    }

    @Override
    public void onResize() {
        if (this.widget instanceof RequiresResize) {
            ((RequiresResize) this.widget).onResize();
        }
    }
}
