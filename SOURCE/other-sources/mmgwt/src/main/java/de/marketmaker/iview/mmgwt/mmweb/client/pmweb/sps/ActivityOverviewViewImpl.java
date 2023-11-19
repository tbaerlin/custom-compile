package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.event.KeyModifiers;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.IconImageIcon;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuButton;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ScrollPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.SortableTableFactory;
import de.marketmaker.iview.pmxml.ActivityDefinitionInfo;
import de.marketmaker.iview.pmxml.ActivityInstanceInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: umaurer
 * Created: 24.02.14
 */
public class ActivityOverviewViewImpl implements ActivityOverviewView {
    private final DockLayoutPanel dlp = new DockLayoutPanel(Style.Unit.PX);
    private final Presenter presenter;
    private final ScrollPanel scrollPanel = new ScrollPanel(new Label());
    private final Menu menuNewActivities = new Menu();

    public ActivityOverviewViewImpl(Presenter presenter) {
        this.presenter = presenter;

        final FloatingToolbar toolbar = new FloatingToolbar(FloatingToolbar.ToolbarHeight.FOR_ICON_SIZE_S);
        toolbar.add(new MenuButton(I18n.I.createNewOne()).withMenu(this.menuNewActivities).withClickOpensMenu());

        this.dlp.setStyleName("as-actOverview");
        this.dlp.addNorth(toolbar, toolbar.getToolbarHeightPixel());
        this.dlp.add(this.scrollPanel);
    }

    @Override
    public Widget asWidget() {
        return this.dlp;
    }

    @Override
    public void clear() {
        this.scrollPanel.setWidget(new Label());
    }

    @Override
    public void setActivities(final List<ActivityDefinitionInfo> defs, final List<ActivityInstanceInfo> insts) {
        this.menuNewActivities.removeAll();
        for (final ActivityDefinitionInfo def : defs) {
            this.menuNewActivities.add(new MenuItem(def.getName(), new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    presenter.createNewActivity(def);
                }
            }));
        }

        final FlexTable table = createTable(presenter, insts, true);
        this.scrollPanel.setWidget(table);
    }

    public static FlexTable createTable(final Presenter presenter, List<ActivityInstanceInfo> insts, boolean withDeleteBtn) {
        final ArrayList<String> headers = new ArrayList<>();
        headers.add(I18n.I.actOverviewReferenceDate());
        headers.add(I18n.I.actOverviewLastChange());
        headers.add(I18n.I.actOverviewDefinitionName());
        headers.add(I18n.I.advisor());
        headers.add(I18n.I.actOverviewStatus());
        headers.add(I18n.I.actOverviewDescription());
        if (withDeleteBtn) {
            headers.add(null);
        }
        final SortableTableFactory sortableTableFactory = new SortableTableFactory(headers.toArray(new String[headers.size()]));
        for (final ActivityInstanceInfo inst : insts) {
            final ClickHandler clickHandler = new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    presenter.goToActivityInstance(inst);
                }
            };

            final String description = inst.getDescription();
            final String status = inst.getStatus();
            final String advisorName = inst.getAdvisorName();
            final String definitionName = inst.getDefinition().getName();
            final ArrayList<SortableTableFactory.Cell> cells = new ArrayList<>();
            cells.add(createDateTimeLinkCell(inst.getReferenceDate(), clickHandler));
            cells.add(createDateTimeLinkCell(inst.getLastSaved(), clickHandler));
            cells.add(new SortableTableFactory.Cell(Renderer.STRING_DOUBLE_DASH.render(definitionName), Renderer.STRING_DOUBLE_DASH.render(definitionName), null, clickHandler));
            cells.add(new SortableTableFactory.Cell(Renderer.STRING_DOUBLE_DASH.render(advisorName), Renderer.STRING_DOUBLE_DASH.render(advisorName), null, clickHandler));
            cells.add(new SortableTableFactory.Cell(Renderer.STRING_DOUBLE_DASH.render(status), Renderer.STRING_DOUBLE_DASH.render(status), null, clickHandler));
            cells.add(new SortableTableFactory.Cell(Renderer.STRING_DOUBLE_DASH.render(description), Renderer.STRING_DOUBLE_DASH.render(description), null, clickHandler).withStyle("sps-actOv-description")); // $NON-NLS$
            if (withDeleteBtn) {
                cells.add(inst.isRemovable() ? createDeleteButtonCell(presenter, inst) : new SortableTableFactory.Cell(null, null, null));
            }
            sortableTableFactory.addRow(cells.toArray(new SortableTableFactory.Cell[cells.size()]));
        }
        final FlexTable table = sortableTableFactory.createTable(0, false);
        table.addStyleName("as-actOverviewTable");
        return table;
    }

    private static SortableTableFactory.Cell createDateTimeLinkCell(String isoDate, ClickHandler clickHandler) {
        return new SortableTableFactory.Cell(isoDate, new Label(PmRenderers.DATE_TIME_HHMM_STRING.render(isoDate)), null, clickHandler);
    }

    private static SortableTableFactory.Cell createDeleteButtonCell(final Presenter presenter, final ActivityInstanceInfo inst) {
        final IconImageIcon icon = new IconImageIcon("sps-activity-instance-delete"); // $NON-NLS$
        final ClickHandler clickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onDeleteButtonClicked(presenter, event, inst);
            }
        };
//        final Button button = new Button().withIcon("sps-activity-instance-delete").withClickHandler(clickHandler); // $NON-NLS$
//        return new SortableTableFactory.Cell(null, button, null, null);
        return new SortableTableFactory.Cell(null, icon, null, clickHandler).withStyle("sps-activity-instance-delete"); // $NON-NLS$
    }

    private static void onDeleteButtonClicked(final Presenter presenter, ClickEvent event, final ActivityInstanceInfo inst) {
        if (SessionData.INSTANCE.isUserPropertyTrue("developer")) {  // $NON-NLS$
            if (KeyModifiers.isCtrlAltShift(event.getNativeEvent())
                    && Window.confirm("Alle Aktivitäten löschen?")) { // $NON-NLS$
                presenter.deleteAllActivities();
                return;
            }
            else if (KeyModifiers.isShift(event.getNativeEvent())) {
                presenter.deleteActivity(inst);
                return;
            }
        }

        final MmJsDate parsedDate = JsDateFormatter.parseDdmmyyyy(inst.getReferenceDate());
        final String dateTime = JsDateFormatter.formatDdmmyyyyHhmm(parsedDate);

        Dialog.confirm(I18n.I.deleteActivityTitle(), I18n.I.deleteActivity(inst.getDefinition().getName(), dateTime), new Command() {
            @Override
            public void execute() {
                presenter.deleteActivity(inst);
            }
        });
    }
}