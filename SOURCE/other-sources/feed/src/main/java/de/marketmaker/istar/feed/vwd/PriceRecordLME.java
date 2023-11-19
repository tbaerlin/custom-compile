/*
* PriceRecordFundVwd.java
*
* Created on 26.07.2006 14:18:38
*
* Copyright (c) market maker Software AG. All Rights Reserved.
*/

package de.marketmaker.istar.feed.vwd;

import java.math.BigDecimal;

import de.marketmaker.istar.domain.data.FieldTypeEnum;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;

import org.joda.time.DateTime;

import static de.marketmaker.istar.feed.snap.SnapRecordUtils.getString;
import static de.marketmaker.istar.feed.snap.SnapRecordUtils.getTime;

/**
 * @author Michael Lösch
 */

public class PriceRecordLME extends PriceRecordVwd {

    public PriceRecordLME(long quoteid, SnapRecord sr, PriceQuality priceQuality,
            boolean pushAllowed, BigDecimal subscriptionRatio, boolean isForward) {
        super(quoteid, sr, priceQuality, pushAllowed, subscriptionRatio, isForward);
    }

    @Override
    public String getLmeSubsystemPrice() {
        return getString(this.sr, VwdFieldDescription.ADF_Last_Subsys_Code.id());
    }

    @Override
    public String getLmeSubsystemBid() {
        return getString(this.sr, VwdFieldDescription.ADF_Bid_Subsys_Code.id());
    }

    @Override
    public String getLmeSubsystemAsk() {
        return getString(this.sr, VwdFieldDescription.ADF_Ask_Subsys_Code.id());
    }

    @Override
    protected DateTime getBidAskDateTime() {
        return toDateTime(getInt(VwdFieldDescription.ADF_Handelsdatum), getTime(this.sr));
    }
}