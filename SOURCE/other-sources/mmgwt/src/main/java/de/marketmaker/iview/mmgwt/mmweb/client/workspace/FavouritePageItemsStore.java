/*
 * FavouritePageItemsStore.java
 *
 * Created on 12.11.2015 18:24
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONValue;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.terminalpages.PageType;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author mdick
 */
public class FavouritePageItemsStore extends
        AbstractFavouriteItemsStore<FavouritePageItemsStore.Page,
                PagesWorkspaceConfig.Item, FavouritePageItemsStore.PagesWorkspaceItemAdapter> {
    public static final String PAGES_WORKSPACE_CONFIG_KEY = "pages";  // $NON-NLS$

    public static final Comparator<PagesWorkspaceConfig.Item> ITEM_COMPARATOR = (o1, o2) -> o1.getOrder() - o2.getOrder();

    private boolean importAsBookmarks = true;

    public PagesWorkspaceConfig getWorkspaceConfig() {
        return (PagesWorkspaceConfig) SessionData.INSTANCE.getUser().getAppConfig().getWorkspaceConfig(PAGES_WORKSPACE_CONFIG_KEY);
    }

    public void tryCreateWorkspaceConfig() {
        if (getWorkspaceConfig() == null) {
            final PagesWorkspaceConfig config = new PagesWorkspaceConfig();
            SessionData.INSTANCE.getUser().getAppConfig().addWorkspace(PAGES_WORKSPACE_CONFIG_KEY, config);
        }
    }

    @Override
    protected List<PagesWorkspaceConfig.Item> getRawItems() {
        tryImportAsBookmarks();

        final PagesWorkspaceConfig c = getWorkspaceConfig();
        if (c == null) {
            return Collections.emptyList();
        }
        final ArrayList<PagesWorkspaceConfig.Item> items = c.getItems();
        if (items == null) {
            return Collections.emptyList();
        }
        return items;
    }

    /**
     * Necessary to import pages bookmarks created with advisory solution versions
     * 1.12, 1.20, and 1.30.
     */
    private void tryImportAsBookmarks() {
        if (this.importAsBookmarks && SessionData.isWithPmBackend()) {
            HashSet<Page> pages = new HashSet<>();
            for (PageType pageType : PageType.values()) {
                final String userPropertyKey = "asPages(" + pageType + ")"; // $NON-NLS$
                final String sPageKeys = SessionData.INSTANCE.getUserProperty(userPropertyKey);
                if (StringUtil.hasText(sPageKeys)) {
                    for (String pageKey : Arrays.asList(sPageKeys.split(","))) { // $NON-NLS$
                        pages.add(new Page(pageKey, pageType));
                    }
                }
                SessionData.INSTANCE.getUser().getAppConfig().addProperty(userPropertyKey, null);
            }
            if (!pages.isEmpty()) {
                for (Page page : pages) {
                    if (canAddItem(page)) {
                        addItem(page);
                    }
                }
            }
        }
        this.importAsBookmarks = false;
    }

    @Override
    public List<PagesWorkspaceItemAdapter> getItems() {
        final ArrayList<PagesWorkspaceItemAdapter> customerPages = getCustomerPages();
        if (customerPages == null || customerPages.isEmpty()) {
            return super.getItems();
        }
        customerPages.addAll(super.getItems());
        return customerPages;
    }

    private ArrayList<PagesWorkspaceItemAdapter> getCustomerPages() {
        final JSONWrapper cp = SessionData.INSTANCE.getGuiDef("customerPages"); // $NON-NLS$
        if (cp != null && cp.isValid()) {
            final JSONArray array = cp.getValue().isArray();
            if (array != null) {
                final ArrayList<PagesWorkspaceItemAdapter> items = new ArrayList<>(array.size());
                for (int i = 0; i < array.size(); i++) {
                    final JSONValue entry = array.get(i);
                    final JSONValue page = entry.isObject().get("page"); // $NON-NLS$
                    final JSONValue name = entry.isObject().get("name"); // $NON-NLS$
                    final PagesWorkspaceConfig.Item item = new PagesWorkspaceConfig.Item(
                            page.isString().stringValue(), PageType.CUSTOM);
                    item.setAlias(name.isString().stringValue());
                    item.setOrder(Integer.MIN_VALUE + i);
                    items.add(adaptRawItem(item));
                }
                return items;
            }
        }
        return null;
    }

    @Override
    protected PagesWorkspaceItemAdapter adaptRawItem(PagesWorkspaceConfig.Item item) {
        return new PagesWorkspaceItemAdapter(item, item.getType() != PageType.CUSTOM);
    }

    @Override
    public FavouriteItem getItem(String identifier) {
        for (FavouriteItem favouriteItem : getItems()) {
            if (StringUtil.equals(identifier, favouriteItem.getIdentifier())) {
                return favouriteItem;
            }
        }
        return null;
    }

    public void addItem(Page page) {
        tryCreateWorkspaceConfig();
        final PagesWorkspaceConfig c = getWorkspaceConfig();
        final ArrayList<PagesWorkspaceConfig.Item> items = c.getItems();
        final PagesWorkspaceConfig.Item newItem = new PagesWorkspaceConfig.Item(page.getKey(), page.getType());
        if (!items.isEmpty()) {
            final PagesWorkspaceConfig.Item maxItem = Collections.max(items, ITEM_COMPARATOR);

            // normalize order
            if (maxItem.getOrder() >= items.size()) {
                final int i = normalizeOrder(items);
                newItem.setOrder(i);
            }
            else {
                newItem.setOrder(maxItem.getOrder() + 1);
            }
        }

        if (items.add(newItem)) {
            fireItemAddedEvent(adaptRawItem(newItem));
        }
    }

    public int normalizeOrder(ArrayList<PagesWorkspaceConfig.Item> items) {
        Collections.sort(items, ITEM_COMPARATOR);
        int order = 0;
        for (PagesWorkspaceConfig.Item item : items) {
            item.setOrder(order++);
        }
        return order;
    }

    public boolean canAddItem(Page page) {
        for (PagesWorkspaceConfig.Item item : getRawItems()) {
            if (StringUtil.equals(page.getKey(), item.getKey()) && page.getType() == item.getType()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getLabel() {
        return I18n.I.pages();
    }

    @Override
    public String getConfigKey() {
        return PAGES_WORKSPACE_CONFIG_KEY;
    }

    @Override
    public String getName() {
        return getConfigKey();
    }

    public class PagesWorkspaceItemAdapter extends
            AbstractFavouriteItemsStore<FavouritePageItemsStore.Page,
                    PagesWorkspaceConfig.Item, FavouritePageItemsStore.PagesWorkspaceItemAdapter>.AbstractFavouriteItem {

        private final boolean editable;

        public PagesWorkspaceItemAdapter(PagesWorkspaceConfig.Item item, boolean editable) {
            super(item);
            this.editable = editable;
        }

        @Override
        public String getIdentifier() {
            return this.item.getType().name() + ":" + this.item.getKey();
        }

        @Override
        public String getLabel() {
            return this.item.getAlias() != null ? item.getAlias() : item.getKey();
        }

        @Override
        public String getType() {
            return this.item.getType().name();
        }

        @Override
        public String getTypeLabel() {
            return this.item.getType().toString();
        }

        @Override
        public int getOrder() {
            return this.item.getOrder();
        }

        @Override
        public String getHistoryToken() {
            return this.item.getType().getControllerName() + "/" + this.item.getKey(); // $NON-NLS$
        }

        @Override
        public String getIconName() {
            switch (this.item.getType()) {
                case DZBANK:
                    return "mm-icon-dzbank-page"; // $NON-NLS$
                case VWD:
                default:
                    return "mm-icon-vwdpage"; // $NON-NLS$
            }
        }

        @Override
        protected void setAlias(String alias) {
            this.item.setAlias(alias);
        }

        @Override
        protected void setOrder(int order) {
            this.item.setOrder(order);
        }

        @Override
        public boolean canMove() {
            return this.editable;
        }

        @Override
        public boolean canRename() {
            return this.editable;
        }

        @Override
        public boolean canRemove() {
            return this.editable;
        }
    }

    @SuppressWarnings("SimplifiableIfStatement")
    public static class Page {
        private final String key;

        private final PageType type;

        public Page(String key, PageType type) {
            this.key = key;
            this.type = type;
        }

        public String getKey() {
            return key;
        }

        public PageType getType() {
            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Page)) return false;

            Page page = (Page) o;

            if (key != null ? !key.equals(page.key) : page.key != null) return false;
            return type == page.type;

        }

        @Override
        public int hashCode() {
            int result = key != null ? key.hashCode() : 0;
            result = 31 * result + (type != null ? type.hashCode() : 0);
            return result;
        }
    }
}
