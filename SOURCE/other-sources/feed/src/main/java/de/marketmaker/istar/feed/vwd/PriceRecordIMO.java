/*
 * PriceRecordIMO.java
 *
 * Created on 22.07.14 13:47
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.vwd;

import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domainimpl.data.NullPrice;

import static de.marketmaker.istar.feed.vwd.VwdFieldDescription.ADF_Avg_Price_per_SquareMetre;
import static de.marketmaker.istar.feed.vwd.VwdFieldDescription.ADF_Handelsdatum;

/**
 * PriceRecord for quotes with instrument sectype IMO.
 * @author oflege
 */
public class PriceRecordIMO extends PriceRecordVwd {
    public PriceRecordIMO(long quoteid, SnapRecord sr,
            PriceQuality priceQuality, boolean pushAllowed) {
        super(quoteid, sr, priceQuality, pushAllowed, null, false);
    }

    @Override
    protected Price doGetPrice() {
        // mdp provides just these two fields
        return getPrice(ADF_Avg_Price_per_SquareMetre, ADF_Handelsdatum);
    }

    @Override
    public Price getHighDay() {
        return NullPrice.INSTANCE;
    }

    @Override
    public Price getLowDay() {
        return NullPrice.INSTANCE;
    }
}
