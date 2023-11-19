package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentView;

import java.util.HashMap;
import java.util.Map;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author umaurer
 */
public class InvestorConfigView implements ContentView {
    private final InvestorConfigController controller;
    private final ContentPanel panel;
    private final ContentPanel contentPanel;
    private final Map<InvestorItem.Type, InvestorItemView> mapItemViews= new HashMap<InvestorItem.Type, InvestorItemView>();
    private InvestorTreeView treeView;


    public InvestorConfigView(InvestorConfigController controller) {
        this.controller = controller;
        this.panel = new ContentPanel();
        this.panel.setLayout(new BorderLayout());
        this.panel.addStyleName("mm-contentData"); // $NON-NLS-0$
        this.panel.addStyleName("pm-reportView"); // $NON-NLS-0$
        this.panel.setHeaderVisible(false);

        final BorderLayoutData data = new BorderLayoutData(Style.LayoutRegion.WEST, 180);
        data.setCollapsible(true);
        data.setFloatable(true);
        data.setSplit(true);
        data.setMinSize(100);
        data.setMaxSize(400);

        this.panel.add(createInvestorPanel(), data);

        this.contentPanel = new ContentPanel(new FitLayout());
        this.contentPanel.setHeaderVisible(false);
        final HTML htmlInfo = new HTML(I18n.I.selectOneCustomerEntry()); 
        htmlInfo.setStyleName("mm-investor-groupConfigPanel"); // $NON-NLS-0$
        this.contentPanel.add(htmlInfo);
        this.panel.add(this.contentPanel, new BorderLayoutData(Style.LayoutRegion.CENTER));

        initMapItemViews();
    }

    private void initMapItemViews() {
        final InvestorInfoPanel investorInfoPanel = new InvestorInfoPanel(this.controller);
        this.mapItemViews.put(InvestorItem.Type.Inhaber, investorInfoPanel);
        this.mapItemViews.put(InvestorItem.Type.Gruppe, new GroupConfigPanel(this.controller));
        this.mapItemViews.put(InvestorItem.Type.Depot, investorInfoPanel);
        this.mapItemViews.put(InvestorItem.Type.Portfolio, investorInfoPanel);
        this.mapItemViews.put(InvestorItem.Type.Konto, investorInfoPanel);
    }

    public Widget getWidget() {
        return this.panel;
    }

    public void onBeforeHide() {

    }

    private ContentPanel createInvestorPanel() {
        final ContentPanel investorPanel = new ContentPanel(new FitLayout());
        investorPanel.addStyleName("mm-contentData-leftPanel"); // $NON-NLS-0$
        investorPanel.setHeading(I18n.I.customers()); 
        this.treeView = new InvestorTreeView(Style.SelectionMode.MULTI, true);
        this.treeView.addEditToolbar();
/*
TODO
        this.treeView.addTreeListener(Events.OnClick, new Listener<TreePanelEvent<InvestorItem>>(){
            public void handleEvent(TreePanelEvent<InvestorItem> e) {
                onInvestorSelected(e.getItem());
            }
        });

        this.treeView.addSelectionChangedListener(new SelectionChangedListener<InvestorItem>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<InvestorItem> e) {
                onInvestorSelected(e.getSelectedItem());
            }
        });
*/

        investorPanel.add(this.treeView);
        return investorPanel;
    }

    private void onInvestorSelected(InvestorItem item) {
        final InvestorItemView itemView = this.mapItemViews.get(item.getType());
        if (itemView != null) {
            this.contentPanel.removeAll();
            itemView.setItem(item);
            this.contentPanel.add(itemView.getWidget());
            this.contentPanel.layout();
        }
    }

    public void reload(InvestorItem item) {
        this.treeView.reload(item);
    }
}
