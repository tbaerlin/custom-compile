/*
 * IqsFeedData.java
 *
 * Created on 02.10.13 15:46
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps.iqs;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataFactory;
import de.marketmaker.istar.feed.FeedMarket;
import de.marketmaker.istar.feed.Vendorkey;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.OrderedSnapData;
import de.marketmaker.istar.feed.ordered.OrderedSnapDataImpl;
import de.marketmaker.istar.feed.ordered.tick.OrderedTickData;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.mdps.util.EntitledFieldGroup;
import de.marketmaker.istar.mdps.util.OrderedEntitlementProvider;

/**
 * @author oflege
 */
public class IqsFeedData implements OrderedFeedData {

    public static final FeedDataFactory FACTORY = (vendorkey, market1) -> {
        final IqsFeedData result = new IqsFeedData(vendorkey, market1, new OrderedSnapDataImpl());
        result.setState(FeedData.STATE_NEW);
        return result;
    };

    private final ByteString vwdcode;

    private final FeedMarket market;

    private final OrderedSnapData snap;

    private byte[] subscriptions;

    private int flags;

    /**
     * cached entitled field groups for this vwd feed symbol; the value has to be reset
     * to null whenever the vendorkey type changes or new entitlements for this symbol's
     * market are available
     **/
    private EntitledFieldGroup[] fieldGroups;

    private IqsFeedData(Vendorkey vendorkey, FeedMarket market, OrderedSnapData snap) {
        this.vwdcode = vendorkey.toVwdcode();
        this.flags = vendorkey.getType() << TYPE_BIT_OFFSET;
        this.market = market;
        this.snap = snap;
    }

    @Override
    public int getCreatedTimestamp() {
        return 0;
    }

    @Override
    public void setCreatedTimestamp(int ts) {
        // ignored
    }

    public void removeCachedFieldGroups() {
        this.fieldGroups = null;
    }

    @Override
    public FeedMarket getMarket() {
        return this.market;
    }

    @Override
    public OrderedSnapData getSnapData(boolean realtime) {
        return realtime ? this.snap : null;
    }

    @Override
    public OrderedTickData getOrderedTickData() {
        return null;
    }

    @Override
    public Vendorkey getVendorkey() {
        return VendorkeyVwd.getInstance(this.vwdcode, getVendorkeyType());
    }

    @Override
    public ByteString getVwdcode() {
        return this.vwdcode;
    }

    @Override
    public boolean isReadyForPush() {
        return hasFlag(FLAG_READY_FOR_PUSH);
    }

    @Override
    public void setReadyForPush(boolean value) {
        if (value) {
            this.flags |= FLAG_READY_FOR_PUSH;
        }
        else if (isReadyForPush()) {
            this.flags ^= FLAG_READY_FOR_PUSH;
        }
    }

    @Override
    public void setState(int update) {
        this.flags = (this.flags & NOT_STATE_MASK) | update;
    }

    @Override
    public int getState() {
        return this.flags & FeedData.STATE_MASK;
    }

    @Override
    public void setVendorkeyType(int type) {
        if (type != getVendorkeyType()) {
            this.fieldGroups = null;
        }
        this.flags = (this.flags & 0x00FFFFFF) | (type << TYPE_BIT_OFFSET);
    }

    @Override
    public int getVendorkeyType() {
        return this.flags >>> TYPE_BIT_OFFSET;
    }

    private boolean hasFlag(final int flag) {
        return (this.flags & flag) != 0;
    }

    EntitledFieldGroup[] getFieldGroups(OrderedEntitlementProvider entitlementProvider) {
        assert Thread.holdsLock(this);

        if (this.fieldGroups == null) {
            final EntitledFieldGroup[] groups = entitlementProvider.getGroups(this);
            this.fieldGroups = (groups != null) ? groups : EntitledFieldGroup.NULL;
        }
        return (fieldGroups != EntitledFieldGroup.NULL) ? fieldGroups : null;
    }

    boolean hasSubscriptions() {
        return this.subscriptions != null &&
                (this.subscriptions[1] != 0 || this.subscriptions[0] != 0);
    }

    byte[] getSubscriptions() {
        return subscriptions;
    }

    void setSubscriptions(byte[] subscriptions) {
        this.subscriptions = subscriptions;
    }
}
