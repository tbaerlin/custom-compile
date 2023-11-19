package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ListView;

/**
 * @author Ulrich Maurer
 *         Date: 24.03.11
 */
public class InvestorListView extends ListView<InvestorItem> {
    private final ListStore<InvestorItem> listStore;

    public InvestorListView() {
        this.listStore = new ListStore<InvestorItem>();
        addStyleName("pm-borderPanel"); // $NON-NLS$
        setStore(this.listStore);
        setDisplayProperty("label"); // $NON-NLS$
    }

    public void removeAll() {
        this.listStore.removeAll();
    }

    public void setInvestorItems(List<InvestorItem> list) {
        this.listStore.removeAll();
        this.listStore.add(list);
    }
}
