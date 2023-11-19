/*
 * InstrumentWorkspaceItem.java
 *
 * Created on 08.05.2008 14:01:26
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import com.extjs.gxt.ui.client.data.BaseTreeModel;

import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.util.InstrumentTypeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;

/**
 * @author Oliver Flege
* @author Thomas Kiesgen
*/
@SuppressWarnings("GwtInconsistentSerializableClass")
class InstrumentWorkspaceItem extends BaseTreeModel implements TreeContentItem {

    private final InstrumentWorkspaceConfig.Item item;

    public static InstrumentWorkspaceItem create(QuoteWithInstrument qwi) {
        final QuoteData qd = qwi.getQuoteData();
        return new InstrumentWorkspaceItem(new InstrumentWorkspaceConfig.Item(
                qwi.getIid(true), qd != null ? qd.getQid() : null, qwi.getRealName(), qwi.getInstrumentData().getType(),
                qd != null ? qd.getMarketName() : null,
                qd != null ? qd.getMarketVwd() : null
        ));
    }

    InstrumentWorkspaceItem(InstrumentWorkspaceConfig.Item item) {
        this.item = item;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstrumentWorkspaceItem that = (InstrumentWorkspaceItem) o;
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public String toString() {
        return getId() + "/" + getName() + "/" + getMarketCode() + "#" + getOrder(); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
    }

    public String getFolderName() {
        return InstrumentTypeUtil.getName(getType());
    }

    public String getHistoryToken() {
        return PlaceUtil.getPortraitPlace(getType(), getId(), null);
    }

    public void rename(String s) {
        this.item.setName(s);
    }

    public String getNameInWorkspace() {
        return item.getAlias() != null ? item.getAlias() : getDisplayName();
    }

    public void setNameInWorkspace(String name) {
        if (!StringUtil.hasText(name) || getDisplayName().equals(name)) {
            this.item.setAlias(null);
        }
        else {
            this.item.setAlias(name);
        }
    }

    public int getOrder() {
        return this.item.getOrder();
    }

    public void setOrder(int order) {
        this.item.setOrder(order);
    }

    InstrumentWorkspaceConfig.Item getItem() {
        return this.item;
    }

    public String getId() {
        return this.item.getId();
    }

    public String getIid() {
        return this.item.getIid();
    }

    public String getQid() {
        return this.item.getQid();
    }

    public String getMarketCode() {
        return this.item.getMarketCode();
    }

    public String getMarketName() {
        return this.item.getMarketName();
    }

    public String getName() {
        return this.item.getName();
    }

    public String getDisplayName() {
        if (item.getMarketCode() != null) {
            return this.item.getName() + ", " + item.getMarketCode(); // $NON-NLS-0$
        }
        return this.item.getName();
    }

    public QuoteWithInstrument toQuoteWithInstrument() {
        final InstrumentData id = new InstrumentData();
        id.setIid(getIid());
        id.setType(getType());
        id.setName(getName());
        final QuoteData qd = new QuoteData();
        qd.setQid(getQid());
        qd.setMarketName(getMarketName());
        qd.setMarketVwd(getMarketCode());
        return new QuoteWithInstrument(id, qd);
    }


    public String getType() {
        return this.item.getType();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <X> X get(String s) {
        if ("name".equals(s)) { // $NON-NLS-0$
            return (X) getNameInWorkspace();
        }
        return super.get(s);
    }
}
