/*
 * EdgDataMethod.java
 *
 * Created on 19.04.2010 13:46:48
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.data.EdgData;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.data.NullEdgData;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.certificatedata.EdgDataProvider;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class EdgDataMethod {
    private final EdgDataProvider controller;

    private final Instrument instrument;

    public EdgDataMethod(EdgDataProvider controller, Instrument instrument) {
        this.controller = controller;
        this.instrument = instrument;
    }

    public EdgData invoke() {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        return profile.isAllowed(Selector.EDG_DATA) || profile.isAllowed(Selector.EDG_DATA_2)
                ? this.controller.getEdgData(instrument.getId())
                : NullEdgData.INSTANCE;
    }
}