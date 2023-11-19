package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author umaurer
 */
public class InvestorInfoPanel implements InvestorItemView {
    private final ContentPanel panel;
    private InvestorItem item;
    private final InvestorConfigController controller;
    private final Grid grid;

    public InvestorInfoPanel(InvestorConfigController controller) {
        this.controller = controller;
        this.panel = new ContentPanel();
        this.panel.setHeaderVisible(false);
        this.panel.setStyleName("mm-investor-config"); // $NON-NLS-0$

        final AbstractImagePrototype iconGroup = InvestorItem.Type.Inhaber.getIconLarge();
        this.grid = new Grid(1, 2);
        this.grid.getCellFormatter().setWidth(0, 0, "32px"); // $NON-NLS-0$
        this.grid.getCellFormatter().setHeight(0, 0, "32px"); // $NON-NLS-0$
        this.grid.setHTML(0, 0, iconGroup.getHTML());
        this.grid.setText(0, 1, InvestorItem.Type.Inhaber.getDesciption());

        this.panel.add(this.grid);
    }

    public void setItem(InvestorItem item) {
        assert (item.getType() == InvestorItem.Type.Inhaber);
        this.item = item;
        this.grid.setHTML(0, 0, item.getType().getIconLarge().getHTML());
        this.grid.setText(0, 1, item.getName());
    }

    public Widget getWidget() {
        return this.panel;
    }
}
