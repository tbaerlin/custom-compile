/*
 * PagesWorkspaceItem.java
 *
 * Created on 05.08.2008 15:33:54
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import com.extjs.gxt.ui.client.data.BaseTreeModel;

import de.marketmaker.iview.mmgwt.mmweb.client.terminalpages.PageType;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author Oliver Flege
* @author Thomas Kiesgen
*/
@SuppressWarnings("GwtInconsistentSerializableClass")
class PagesWorkspaceItem extends BaseTreeModel implements TreeContentItem {
    private final PagesWorkspaceConfig.Item item;

    PagesWorkspaceItem(PagesWorkspaceConfig.Item item) {
        this.item = item;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PagesWorkspaceItem that = (PagesWorkspaceItem) o;

        return getFolderName().equals(that.getFolderName()) && getKey().equals(that.getKey());
    }

    @Override
    public int hashCode() {
        return getFolderName().hashCode() * getKey().hashCode();
    }

    public int compareTo(PagesWorkspaceItem o) {
        return this.item.compareTo(o.item);
    }

    public String getFolderName() {
        return getType().toString();
    }

    public String getHistoryToken() {
        return getType().getControllerName() + "/" + getKey(); // $NON-NLS-0$
    }

    public void rename(String s) {
        item.setAlias(s);
    }

    public String getDisplayName() {
        return item.getKey();
    }

    public String getAlias() {
        return item.getAlias();
    }

    public String getNameInWorkspace() {
        return getAlias() != null ? getAlias() : getKey();
    }

    public void setNameInWorkspace(String name) {
        if (!StringUtil.hasText(name) || getKey().equals(name)) {
            this.item.setAlias(name);
        }
        else {
            this.item.setAlias(name);
        }
    }

    public int getOrder() {
        return item.getOrder();
    }

    public void setOrder(int order) {
        this.item.setOrder(order);
    }

    public String getKey() {
        return item.getKey();
    }

    public PageType getType() {
        return item.getType();
    }

    public PagesWorkspaceConfig.Item getItem() {
        return this.item;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <X> X get(String s) {
        if ("name".equals(s)) { // $NON-NLS$
            return (X) getNameInWorkspace();
        }
        return super.get(s);
    }
}
