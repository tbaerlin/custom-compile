package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.google.gwt.http.client.URL;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.UrlBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author umaurer
 */
public class InvestorTreeModel {
    private static final String APP_CONFIG_KEY_INVESTORS = "pm-investortree"; // $NON-NLS-0$

    private static InvestorTreeModel instance;

    private final TreeStore<InvestorItem> store;

    private final InvestorItem rootItemAllInvestors;

    private int maxGroupId = 0;

    private InvestorTreeModel() {
        this.rootItemAllInvestors = null;
/*
        this.rootItemAllInvestors = PmWebSupport.getInstance().getInvestorItemRoot();
        final List<InvestorItem> listRootItems = readFromAppConfig();
*/

        final TreeLoader<InvestorItem> loader = new BaseTreeLoader<InvestorItem>(InvestorRpcProxy.getInstance()){
            @Override
            public boolean hasChildren(InvestorItem parent) {
                return parent.getHasChildren() == InvestorItem.HasChildren.YES;
            }
        };
        this.store = new TreeStore<>(loader);
        this.store.setKeyProvider(new ModelKeyProvider<InvestorItem>() {
            public String getKey(InvestorItem item) {
//                return "node_" + item.get("id"); // $NON-NLS$
                return "node_" + item.getKey(); // $NON-NLS$
            }
        });
        this.store.setStoreSorter(new StoreSorter<InvestorItem>(new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                final InvestorItem t1 = (InvestorItem) o1;
                final InvestorItem t2 = (InvestorItem) o2;
                int result = t1.isEditable() ? (t2.isEditable() ? 0 : 1) : -1;
                if (result != 0) {
                    return result;
                }
                result = t1.getName().compareTo(t2.getName());
                if (result != 0) {
                    return result;
                }
                return t1.getId().compareTo(t2.getId());
            }
        }));

/*
        for (InvestorItem item : listRootItems) {
            this.store.add(item, true);
        }
*/
    }

    public static InvestorTreeModel getInstance() {
        if (instance == null) {
            instance = new InvestorTreeModel();
        }
        return instance;
    }

    public TreeStore<InvestorItem> getStore() {
        return this.store;
    }

    public void saveToAppConfig(List<InvestorItem> rootItems) {
        final UrlBuilder ub = new UrlBuilder("", false); // $NON-NLS-0$
        for (InvestorItem item : rootItems) {
            if (!item.isEditable()) {
                continue;
            }
            saveToAppConfig(item, ub);
        }
        SessionData.INSTANCE.getUser().getAppConfig().addProperty(APP_CONFIG_KEY_INVESTORS, ub.toURL());
    }

    private void saveToAppConfig(InvestorItem item, UrlBuilder ub) {
        if (item.isType(InvestorItem.Type.Gruppe)) {
            ub.add(item.getId(), item.getName());
            final InvestorItem parent = (InvestorItem) item.getParent();
            if (parent != null) {
                ub.add("p-" + item.getId(), parent.getId()); // $NON-NLS-0$
            }
            final List<ModelData> children = item.getChildren();
            for (ModelData child : children) {
                final InvestorItem childItem = (InvestorItem) child;
                saveToAppConfig(childItem, ub);
            }
        }
        else {
            ub.add("p-" + item.getId(), ((InvestorItem) item.getParent()).getId()); // $NON-NLS-0$
        }
    }

    public List<InvestorItem> readFromAppConfig() {
        final String url = SessionData.INSTANCE.getUser().getAppConfig().getProperty(APP_CONFIG_KEY_INVESTORS);

        final String[] params = url != null
                ? url.substring(url.indexOf('?') + 1).split("&") // $NON-NLS-0$
                : new String[0];

        final Map<String, InvestorItem> map = new HashMap<>();
        copyToMap(this.rootItemAllInvestors, map);

        // read group items and relationship from app config
        final List<InvestorItem> listItems = new ArrayList<>();
        for (String param : params) {
            final int posEqual = param.indexOf('=');
            final String key = URL.decodeQueryString(param.substring(0, posEqual));
            final String value = URL.decodeQueryString(param.substring(posEqual + 1));
            if (key.startsWith("p-")) { // $NON-NLS-0$
                final InvestorItem item = map.get(key.substring(2));
                if (item != null) {
                    final InvestorItem parent = map.get(value);
                    parent.add(item.isEditable() ? item : item.deepCopy());
                }
            }
            else {
                final int groupId = Integer.parseInt(key.substring(2));
                if (groupId > this.maxGroupId) {
                    this.maxGroupId = groupId;
                }
                final InvestorItem item = new InvestorItem(key, InvestorItem.Type.Gruppe, value, InvestorItem.ZONE_PMWEB_LOCAL, true);
                map.put(key, item);
                listItems.add(item);
            }
        }

        final List<InvestorItem> listRootItems = new ArrayList<>();
        listRootItems.add(this.rootItemAllInvestors);
        for (InvestorItem item : listItems) {
            if (item.getParent() == null) {
                // this is a root item,
                listRootItems.add(item);
            }
        }
        return listRootItems;
    }

    private void copyToMap(InvestorItem item, Map<String, InvestorItem> map) {
        map.put(item.getId(), item);
        for (ModelData child : item.getChildren()) {
            copyToMap((InvestorItem) child, map);
        }
    }

    public void add(InvestorItem item) {
        assert (item.isType(InvestorItem.Type.Gruppe));
        this.store.add(item, true);
    }

    public void delete(InvestorItem item) {
        this.store.remove(item);
        final TreeModel parent = item.getParent();
        if (parent != null) {
            parent.remove(item);
        }
    }

    public void reload(InvestorItem item) {
        final InvestorItem parent = this.store.getParent(item);
        final int index = this.store.indexOf(item);
        if (index >= 0) {
            this.store.remove(item);
            if (parent == null) {
                this.store.insert(item, index, true);
            }
            else {
                this.store.insert(parent, item, index, true);
            }
        }
        else {
            this.store.add(item, true);
        }

        saveToAppConfig(this.store.getRootItems());
    }
}
