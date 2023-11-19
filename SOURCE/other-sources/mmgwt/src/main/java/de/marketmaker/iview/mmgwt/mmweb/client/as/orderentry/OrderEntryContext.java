/*
 * OrderEntryContext.java
 *
 * Created on 05.08.2014 10:27
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec.ParameterMap;

/**
* @author mdick
*/
public interface OrderEntryContext {
    ParameterMap getParameterMap();
    PresenterDisposedHandler getPresenterDisposedHandler();

    public final class Factory {
        private Factory() {
        }

        public static OrderEntryContext create() {
            return new OrderEntryContext() {
                final ParameterMap parameterMap = new ParameterMap();

                @Override
                public ParameterMap getParameterMap() {
                    return this.parameterMap;
                }

                @Override
                public PresenterDisposedHandler getPresenterDisposedHandler() {
                    return null;
                }
            };
        }

        public static OrderEntryContext create(String depotId) {
            final OrderEntryContext orderEntryContext = create();
            orderEntryContext.getParameterMap().setDepotId(depotId);
            return orderEntryContext;
        }

        public static OrderEntryContext create(final ParameterMap parameterMap, final PresenterDisposedHandler presenterDisposedHandler) {
            return new OrderEntryContext() {
                @Override
                public ParameterMap getParameterMap() {
                    return parameterMap;
                }

                @Override
                public PresenterDisposedHandler getPresenterDisposedHandler() {
                    return presenterDisposedHandler;
                }
            };
        }
    }
}
