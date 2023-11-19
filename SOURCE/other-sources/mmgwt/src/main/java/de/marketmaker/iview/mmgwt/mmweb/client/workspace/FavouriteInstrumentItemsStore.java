/*
 * FavouriteInstrumentItemsStore.java
 *
 * Created on 12.11.2015 18:19
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.InstrumentTypeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author mdick
 */
public class FavouriteInstrumentItemsStore extends AbstractFavouriteItemsStore<QuoteWithInstrument,
        InstrumentWorkspaceConfig.Item, FavouriteInstrumentItemsStore.InstrumentWorkspaceItemAdapter> {
    public static final String INSTRUMENTS_WORKSPACE_CONFIG_KEY = "ins"; // $NON-NLS$

    public static final Comparator<InstrumentWorkspaceConfig.Item> ITEM_COMPARATOR = (o1, o2) -> o1.getOrder() - o2.getOrder();

    public InstrumentWorkspaceConfig getWorkspaceConfig() {
        return (InstrumentWorkspaceConfig) SessionData.INSTANCE.getUser().getAppConfig().getWorkspaceConfig(INSTRUMENTS_WORKSPACE_CONFIG_KEY);
    }

    public void tryCreateWorkspaceConfig() {
        if (getWorkspaceConfig() == null) {
            final InstrumentWorkspaceConfig instrumentWorkspaceConfig = new InstrumentWorkspaceConfig();
            SessionData.INSTANCE.getUser().getAppConfig().addWorkspace(INSTRUMENTS_WORKSPACE_CONFIG_KEY, instrumentWorkspaceConfig);
        }
    }

    @Override
    protected List<InstrumentWorkspaceConfig.Item> getRawItems() {
        final InstrumentWorkspaceConfig c = getWorkspaceConfig();
        if (c == null) {
            return Collections.emptyList();
        }
        final ArrayList<InstrumentWorkspaceConfig.Item> items = c.getItems();
        if (items == null) {
            return Collections.emptyList();
        }
        return items;
    }

    @Override
    protected InstrumentWorkspaceItemAdapter adaptRawItem(InstrumentWorkspaceConfig.Item item) {
        return new InstrumentWorkspaceItemAdapter(item);
    }

    @Override
    public void addItem(QuoteWithInstrument qwi) {
        tryCreateWorkspaceConfig();
        final InstrumentWorkspaceConfig c = getWorkspaceConfig();
        final ArrayList<InstrumentWorkspaceConfig.Item> items = c.getItems();
        final InstrumentWorkspaceConfig.Item newItem = createItem(qwi);
        if (!items.isEmpty()) {
            final InstrumentWorkspaceConfig.Item maxItem = Collections.max(items, ITEM_COMPARATOR);

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

    public int normalizeOrder(ArrayList<InstrumentWorkspaceConfig.Item> items) {
        Collections.sort(items, ITEM_COMPARATOR);
        int order = 0;
        for (InstrumentWorkspaceConfig.Item item : items) {
            item.setOrder(order++);
        }
        return order;
    }

    @Override
    public boolean canAddItem(QuoteWithInstrument qwi) {
        final InstrumentWorkspaceConfig c = getWorkspaceConfig();
        if (c == null) {
            return true;
        }
        for (InstrumentWorkspaceConfig.Item item : c.getItems()) {
            if (StringUtil.equals(qwi.getId(), item.getId())) {
                return false;
            }
        }
        return true;
    }

    public InstrumentWorkspaceConfig.Item createItem(QuoteWithInstrument qwi) {
        final QuoteData qd = qwi.getQuoteData();
        return new InstrumentWorkspaceConfig.Item(
                qwi.getIid(true), qd != null ? qd.getQid() : null, qwi.getRealName(), qwi.getInstrumentData().
                getType(),
                qd != null ? qd.getMarketName() : null,
                qd != null ? qd.getMarketVwd() : null
        );
    }

    @Override
    public String getLabel() {
        return I18n.I.instruments();
    }

    @Override
    public String getConfigKey() {
        return INSTRUMENTS_WORKSPACE_CONFIG_KEY;
    }

    public class InstrumentWorkspaceItemAdapter extends
            AbstractFavouriteItemsStore<QuoteWithInstrument, InstrumentWorkspaceConfig.Item, FavouriteInstrumentItemsStore.InstrumentWorkspaceItemAdapter>.AbstractFavouriteItem {
        public InstrumentWorkspaceItemAdapter(InstrumentWorkspaceConfig.Item item) {
            super(item);
        }

        @Override
        public String getIdentifier() {
            return this.item.getId();
        }

        public String getLabel() {
            return this.item.getAlias() != null ? this.item.getAlias() : this.item.getName();
        }

        public String getType() {
            return this.item.getType();
        }

        public String getTypeLabel() {
            return InstrumentTypeUtil.getName(this.item.getType());
        }

        public int getOrder() {
            return this.item.getOrder();
        }

        @Override
        public String getHistoryToken() {
            return PlaceUtil.getPortraitPlace(this.item.getType(), this.item.getId(), null);
        }

        @Override
        public String getIconName() {
            return "pm-instrument";  // $NON-NLS$
        }

        @Override
        protected void setAlias(String alias) {
            this.item.setAlias(alias);
        }

        @Override
        protected void setOrder(int order) {
            this.item.setOrder(order);
        }
    }
}
