package de.marketmaker.iview.mmgwt.mmweb.client.view;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.as.NavigationWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryContext;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryItem;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryThreadManager;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.HistoryContextToolbar;

import java.util.Arrays;

/**
 * @author Ulrich Maurer
 *         Date: 11.12.12
 */
public class ObjectPanel implements NavigationWidget {

    private final DockLayoutPanel panel;
    private final SimplePanel toolbarPanel;
    private final SimplePanel northPanel;
    private final HistoryThreadManager threadManager;

    public ObjectPanel(Widget northWidget, Widget centerWidget) {
        this.panel = new DockLayoutPanel(Style.Unit.PX);
        this.panel.setStyleName("mm-navWidget");

        this.threadManager = AbstractMainController.INSTANCE.getHistoryThreadManager();

        this.toolbarPanel = new SimplePanel();
        this.toolbarPanel.setWidget(createToolbar(this.threadManager));
        this.panel.addNorth(this.toolbarPanel, 32d);
        this.northPanel = new SimplePanel();
        this.panel.addNorth(this.northPanel, 200d);
        updateNorthWidget(northWidget);
        this.panel.add(centerWidget);
    }

    public void updateNorthWidget(Widget northWidget) {
        if (northWidget == null) {
            northWidget = new HTML();
        }
        northWidget.addStyleName("mm-navNorth");
        this.northPanel.setWidget(northWidget);
    }

    public void updateHistoryContext() {
        this.toolbarPanel.setWidget(createToolbar(this.threadManager));
    }

    private Widget createToolbar(final HistoryThreadManager threadManager) {
        final HistoryItem activeHistoryItem = threadManager.getActiveThreadHistoryItem();
        if (activeHistoryItem != null) {
            final HistoryContext context = activeHistoryItem.getPlaceChangeEvent().getHistoryContext();
            if (context != null) {
                return HistoryContextToolbar.create(threadManager, context).asWidget();
            }
            else {
                final HistoryItem nearestContextItem = threadManager.getNearestUpstreamNonNullOrExplicitNullContext();
                if(nearestContextItem != null) {
                    final PlaceChangeEvent placeChangeEvent = nearestContextItem.getPlaceChangeEvent();
                    if(!placeChangeEvent.isExplicitHistoryNullContext()) {
                        //User navigates on sub-controller, so create a new toolbar for the nearest item
                        final HistoryContext lastNonNullContext = nearestContextItem.getPlaceChangeEvent().getHistoryContext();
                        if(lastNonNullContext != null) {
                            return HistoryContextToolbar.create(threadManager, lastNonNullContext).asWidget();
                        }
                    }
                    //User navigates on an explicit null context item, so create an empty context back button.
                }
            }
        }

        final Label emptyToolbar = new Label("");
        emptyToolbar.setStyleName("mm-toolbar as-navToolbar");
        return emptyToolbar;
    }

    @Override
    public Widget asWidget() {
        return this.panel;
    }

    @Override
    public void onNavWidgetSelected() {
        Firebug.log("not yet implemented: ObjectPanel.fireSelected() --> implementation needed????????");
    }

    @Override
    public void changeSelection(String[] ids) {
        Firebug.log("ObjectPanel.changeSelection(" + Arrays.asList(ids) + ")");
        throw new UnsupportedOperationException("not yet implemented: ObjectPanel.changeSelection()"); // $NON-NLS$
    }

    @Override
    public boolean isResponsibleFor(String[] ids) {
        return false;
    }

}
