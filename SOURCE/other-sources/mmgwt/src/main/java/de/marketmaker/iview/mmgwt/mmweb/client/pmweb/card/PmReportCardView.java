package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.card;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.LayoutMenuItem;
import de.marketmaker.iview.pmxml.LayoutDesc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author umaurer
 */
public class PmReportCardView extends ContentPanel {
    private Frame frame = null;
    private PmReportCard card;
    private final Map<String, TabItem> mapTabItems = new HashMap<String, TabItem>();
    private boolean listenersDisabled = false;

    public PmReportCardView(PmReportCard card) {
        super(new FitLayout());
        setHeaderVisible(false);
        this.card = card;
        final LayoutMenuItem reportMenuRootItem = PmWebSupport.getInstance().getReportMenuRootItem(card.getInvestorItem().getType());
        add(createTabPanel(reportMenuRootItem.getChildren()));
    }

    public PmReportCard getCard() {
        return card;
    }

    private TabPanel createTabPanel(List<LayoutMenuItem> reportItems) {
        if (reportItems == null || reportItems.isEmpty()) {
            return null;
        }
        final TabPanel tabPanel = new TabPanel(){
            @Override
            protected void onAttach() {
                listenersDisabled = true;
                super.onAttach();
                listenersDisabled = false;
            }
        };
        tabPanel.setAutoSelect(false);
        tabPanel.setTabScroll(true);

        for (final LayoutMenuItem reportItem : reportItems) {
            final TabItem tabItem = new TabItem(reportItem.getName());
            tabItem.setLayout(new FitLayout());
            tabPanel.add(tabItem);
            final TabPanel childPanel = createTabPanel(reportItem.getChildren());
            if (childPanel != null) {
                tabItem.add(childPanel);
                addParentSelectListener(tabItem, childPanel);
            }
            else {
                if (reportItem.getLayoutDesc() == null) {
                    continue;
                }
                final String layoutGuid = reportItem.getLayoutDesc().getLayout().getGuid();
                this.mapTabItems.put(layoutGuid, tabItem);

                addReportSelectListener(tabItem, reportItem);
/*
                if (layoutGuid.equals(card.getReport().getGuid())) {
                    select(tabItem);
                }
*/
            }
        }

        return tabPanel;
    }

    private void addParentSelectListener(final TabItem tabItem, final TabPanel childPanel) {
        tabItem.addListener(Events.Select, new Listener<TabPanelEvent>() {
            public void handleEvent(TabPanelEvent tpe) {
                if (listenersDisabled) {
                    return;
                }
                final TabItem selectedItem = childPanel.getSelectedItem();
                if (selectedItem == null) {
                    childPanel.setSelection(childPanel.getItem(0));
                }
                else {
                    selectedItem.fireEvent(Events.Select, new TabPanelEvent(childPanel, selectedItem));
                }
            }
        });
    }

    private void addReportSelectListener(final TabItem tabItem, final LayoutMenuItem reportItem) {
        tabItem.addListener(Events.Select, new Listener<TabPanelEvent>() {
            public void handleEvent(TabPanelEvent tpe) {
                if (listenersDisabled) {
                    return;
                }
                if (frame == null) {
                    frame = new Frame();
                }
                else {
                    frame.removeFromParent();
                }

                PmReportCardController.getInstance().gotoCardWithReportId(card, reportItem.getLayoutDesc().getLayout().getGuid());
            }
        });
    }

    public void activateReport(LayoutDesc report) {
        select(this.mapTabItems.get(report.getLayout().getGuid()));
    }

    private void select(TabItem tabItem) {
        while (tabItem != null) {
            final TabPanel tabPanel = tabItem.getTabPanel();
            tabPanel.setSelection(tabItem);
            tabItem = tabPanel.getParent() instanceof TabItem ? (TabItem) tabPanel.getParent() : null;
        }
    }

    public void setContentWidget(Widget widget) {
        final TabItem tabItem = this.mapTabItems.get(this.card.getReport().getLayout().getGuid());
        tabItem.removeAll();
        tabItem.add(widget);
        tabItem.layout();
    }

}
