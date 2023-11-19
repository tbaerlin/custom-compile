package de.marketmaker.iview.mmgwt.mmweb.client.dashboard;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.floating.FloatingPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.MainView;
import de.marketmaker.iview.mmgwt.mmweb.client.data.DashboardConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.myspace.SnippetMenuConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets.DashboardSnippet;

import java.util.ArrayList;

import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * Author: umaurer
 * Created: 25.06.15
 */
public class DashboardPalette {
    public final FlowPanel panel = new FlowPanel();
    public final TextBox textBoxName = new TextBox();

    public DashboardPalette(final DashboardController controller, DashboardConfig config) {
        this.textBoxName.setStyleName("as-dashboardNameBox");
        this.textBoxName.setMaxLength(50);
        final Button buttonSave = Button.icon("mm-save-icon") // $NON-NLS$
                .tooltip(I18n.I.save())
                .clickHandler(e -> controller.saveEditMode())
                .build();
        final Button buttonCancel = Button.icon("x-tool-cancel") // $NON-NLS$
                .tooltip(I18n.I.cancel())
                .clickHandler(e -> controller.cancelEditMode())
                .build();

        final FlexTable headerTable = new FlexTable();
        final FlexTable.FlexCellFormatter formatter = headerTable.getFlexCellFormatter();
        headerTable.setCellPadding(0);
        headerTable.setCellSpacing(0);
        headerTable.setStyleName("mm-toolbar as-navToolbar");
        headerTable.setWidth("100%"); // $NON-NLS$
        headerTable.setWidget(0, 0, this.textBoxName);
        headerTable.setWidget(0, 1, buttonSave);
        headerTable.setWidget(0, 2, buttonCancel);
        formatter.setWidth(0, 0, "99%"); // $NON-NLS$
        formatter.setWidth(0, 1, "16px"); // $NON-NLS$
        formatter.setWidth(0, 2, "16px"); // $NON-NLS$

        final FlowPanel panel = new FlowPanel();
        final ArrayList<SnippetMenuConfig.Item> snippetMenuItems = SnippetMenuConfig.INSTANCE.getItems();

        final boolean globalDb = config.getRoles().contains(ConfigDao.DASHBOARD_ROLE_GLOBAL);
        boolean spaceSet = !SessionData.isWithMarketData();
        for (SnippetMenuConfig.Item item : snippetMenuItems) {
            if (isMarketSnippet(item) && !SessionData.isWithMarketData()) {
                continue;
            }
            final boolean globalSnippet = !item.getClazz().getDefaultConfig().getBoolean("needsInputObject", false); // $NON-NLS$
            if (!globalDb || globalSnippet) { //Dashboards with InputObject(Inhaber,...) contain all snippets, global Dashboards contain only global snippets
                spaceSet = addSpace(panel, spaceSet, item);
                panel.add(new SnippetTemplate(item.getClazz()));
            }
        }
        final FloatingPanel floatingPanel = new FloatingPanel(FloatingPanel.Orientation.VERTICAL, true).withWidget(panel);

        this.panel.setStyleName("as-navigationArea as-dashboardPalette");

        addNorth(headerTable);
        final SimplePanel centerPanel = new SimplePanel(floatingPanel.asWidget());
        addCenter(centerPanel);

        this.panel.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent attachEvent) {
                floatingPanel.onResize();
                final Style style = DashboardPalette.this.panel.getElement().getStyle();
                style.setTop(MainView.getInstance().getContentHeaderTop(), PX);
                style.setWidth(MainView.getInstance().getContentHeaderLeft(), PX);
                textBoxName.setFocus(true);
                textBoxName.selectAll();
            }
        });
        Window.addResizeHandler(e -> floatingPanel.onResize());
    }

    private boolean addSpace(FlowPanel panel, boolean spaceSet, SnippetMenuConfig.Item item) {
        if (spaceSet || isMarketSnippet(item)) { //!= DashboardSnippet.class because marketdata-snippets come first, then we need a spacer
            return spaceSet;
        }
        final Widget spacer = new HTML("</div>"); // $NON-NLS$
        spacer.addStyleName("as-dashpalette-spacer");
        panel.add(spacer);
        return true;
    }

    private boolean isMarketSnippet(SnippetMenuConfig.Item item) {
        return item.getClazz().getClass() != DashboardSnippet.Class.class;
    }

    private void addNorth(Widget widget) {
        final Style style = widget.getElement().getStyle();
        style.setPosition(Style.Position.ABSOLUTE);
        style.setTop(0, PX);
        style.setRight(0, PX);
        style.setLeft(0, PX);
        style.setHeight(MainView.CONTENT_HEADER_HEIGHT, PX);
        this.panel.add(widget);
    }

    private void addCenter(Widget widget) {
        final Style style = widget.getElement().getStyle();
        style.setPosition(Style.Position.ABSOLUTE);
        style.setTop(MainView.CONTENT_HEADER_HEIGHT, PX);
        style.setRight(0, PX);
        style.setBottom(0, PX);
        style.setLeft(0, PX);
        this.panel.add(widget);
    }

    public void show() {
        RootPanel.get().add(this.panel);
    }

    public void hide() {
        this.panel.removeFromParent();
    }

    public String getName() {
        return this.textBoxName.getText();
    }

    public void setName(String name) {
        this.textBoxName.setText(name);
    }
}
