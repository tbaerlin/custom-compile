package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ResizeLayoutPanel;
import de.marketmaker.itools.gwtutil.client.widgets.Button;

/**
 * @author umaurer
 */
public class BrowserSpecificIE7 extends BrowserSpecificIE {
    @Override
    public void setPriceTeaserMarket(final Button button, final String text) {
        Scheduler.get().scheduleDeferred(new Command() {
            public void execute() {
                button.setText(text);
            }
        });
    }

    @Override
    public void forceLayout(DockLayoutPanel layoutPanel) {
        layoutPanel.forceLayout();
    }

    @Override
    public String getBodyStyles() {
        return "bs-ie"; // $NON-NLS$
    }

    @Override
    public void clearWidthBeforeRecalculation(com.google.gwt.dom.client.Style style) {
        style.setWidth(50000d, com.google.gwt.dom.client.Style.Unit.PX);
    }

    @Override
    public boolean isToolbarFillSupported() {
        return false;
    }

    @Override
    public void fixIe7FloatingToolbar(ResizeLayoutPanel toolbarPanel, FlowPanel panelOuter) {
        toolbarPanel.getElement().getStyle().setPadding(0, Style.Unit.PX);
        toolbarPanel.getElement().getStyle().setHeight(26, Style.Unit.PX);
        panelOuter.getElement().getStyle().setWidth(100, Style.Unit.PC);
    }

    @Override
    public boolean isVmlSupported() {
        return true;
    }

}
