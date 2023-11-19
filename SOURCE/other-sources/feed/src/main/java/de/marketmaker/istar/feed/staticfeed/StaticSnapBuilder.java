/*
 * StaticSnapBuilder.java
 *
 * Created on 19.04.13 14:35
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.staticfeed;

import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.OrderedSnapBuilder;
import de.marketmaker.istar.feed.ordered.OrderedUpdate;

import static de.marketmaker.istar.feed.mdps.MdpsTypeMappings.UNKNOWN;

/**
 * @author oflege
 */
public class StaticSnapBuilder extends OrderedSnapBuilder {
    public interface DefinedTypeListener {
        void onDefinedType(FeedData data);
    }

    private DefinedTypeListener definedTypeListener;

    public void setDefinedTypeListener(DefinedTypeListener definedTypeListener) {
        this.definedTypeListener = definedTypeListener;
    }

    @Override
    public void process(OrderedFeedData fd, OrderedUpdate update) {
        super.process(fd, update);
        doUpdateType(fd, update);
    }

    protected void doUpdateType(FeedData fd, OrderedUpdate update) {
        final int type = update.getVwdKeyType();
        // type in staticfeed is either the "real" type, or 0 (either OT (other) or unknown).
        // The rule is to override the type iff it is not 0, as some components that send
        // static data do not know the real type and send OT instead
        if (type != UNKNOWN) {
            final int previousType = fd.getVendorkeyType();
            fd.setVendorkeyType(type);
            if (previousType == UNKNOWN && !update.isDelete()) {
                onDefinedType(fd);
            }
        }
    }

    private void onDefinedType(FeedData fd) {
        if (this.definedTypeListener != null) {
            this.definedTypeListener.onDefinedType(fd);
        }
    }
}
