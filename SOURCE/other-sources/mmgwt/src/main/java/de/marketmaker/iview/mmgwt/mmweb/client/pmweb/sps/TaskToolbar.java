package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * Author: umaurer
 * Created: 04.11.14
 */
@NonNLS
public class TaskToolbar extends Composite implements TaskViewPanel.PinDisplay {
    private final FlexTable table = new FlexTable();
    private final FlexTable.FlexCellFormatter tableFormatter = this.table.getFlexCellFormatter();
    private final TaskViewPanel tvp;
    private int col = 0;
    private final Image imagePin;

    public TaskToolbar(final TaskViewPanel tvp) {
        this.tvp = tvp;
        this.imagePin = IconImage.get("sps-south-unpinned").createImage();
        updatePinImage(imagePin);
        this.imagePin.setStyleName("sps-southPanel-pin");
        this.imagePin.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                tvp.toggleSouthWidgetPinned();
                updatePinImage(imagePin);
            }
        });
        this.imagePin.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                IconImage.get("sps-south-pin hover").applyTo(imagePin);
            }
        });
        this.imagePin.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                updatePinImage(imagePin);
            }
        });

        final FlowPanel panel = new FlowPanel();
        panel.add(this.imagePin);
        panel.add(this.table);
        initWidget(panel);
    }

    private void updatePinImage(Image image) {
        setPinned(image, this.tvp.isSouthWidgetPinned());
    }

    private static void setPinned(Image image, boolean pinned) {
        IconImage.get(pinned ? "sps-south-pinned" : "sps-south-unpinned").applyTo(image);
        Tooltip.addQtip(image, pinned ? I18n.I.spsTaskDetachToolbar() : I18n.I.spsTaskFixToolbar());
    }

    public void addSpace(String width) {
        this.table.setHTML(0, this.col, "&nbsp;");
        this.tableFormatter.setWidth(0, this.col, width);
        this.col++;
    }

    public void addWidget(Widget widget, String width) {
        addWidget(widget, width, null);
    }

    public void addWidget(Widget widget, String width, HasHorizontalAlignment.HorizontalAlignmentConstant alignment) {
        this.table.setWidget(0, this.col, widget);
        this.tableFormatter.setWidth(0, this.col, width);
        if (alignment != null) {
            this.tableFormatter.setHorizontalAlignment(0, this.col, alignment);
        }
        this.col++;
    }

    @Override
    public void setPinned(boolean pinned) {
        setPinned(this.imagePin, pinned);
    }
}
