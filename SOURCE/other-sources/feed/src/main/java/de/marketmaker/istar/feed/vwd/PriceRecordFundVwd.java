/*
* PriceRecordFundVwd.java
*
* Created on 26.07.2006 14:18:38
*
* Copyright (c) market maker Software AG. All Rights Reserved.
*/

package de.marketmaker.istar.feed.vwd;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecordFund;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domainimpl.data.NullPrice;

import static de.marketmaker.istar.feed.vwd.VwdFieldDescription.*;

/**
 * @author Martin Wilke
 */

public class PriceRecordFundVwd extends PriceRecordVwd implements PriceRecordFund {
    public PriceRecordFundVwd(long quoteid, SnapRecord sr, PriceQuality priceQuality,
            boolean pushAllowed) {
        super(quoteid, sr, priceQuality, pushAllowed, null, false);
    }

    @Override
    protected boolean getCurrentDayDefined() {
        return isDefinedAndNotZero(getField(ADF_NAV)) || isDefinedAndNotZero(getField(ADF_Ruecknahme));
    }

    public Price getIssuePrice() {
        final Price price = getFundPrice(ADF_Ausgabe, ADF_Ausgabe_Vortag);
        if (price.isDefined() && price.getValue().signum() == 0) {
            // HACK to mask feed values of 0 for issue price
            return NullPrice.INSTANCE;
        }
        return price;
    }

    public Price getPreviousIssuePrice() {
        return getPreviousPrice(ADF_Ausgabe_Vortag, null);
    }

    public Price getRedemptionPrice() {
        return getFundPrice(ADF_Ruecknahme, ADF_Ruecknahme_Vortag);
    }

    public Price getPreviousRedemptionPrice() {
        final SnapField ruecknahme = getField(ADF_Ruecknahme);

        final SnapField price;
        final SnapField datefield;
        if (isDefinedAndNotZero(ruecknahme)) {
            price = getField(ADF_Ruecknahme_Vortag);
            datefield = getField(ADF_Schluss_Vortagesdatum);
        }
        else {
            price = getField(MMF_Schluss_Vorvortag);
            datefield = getField(MMF_Schluss_Vorvortagesdatum);
        }

        return createPrice(price, null, null, getDateTime(datefield));
    }

    public Price getNetAssetValue() {
        return getFundPrice(VwdFieldDescription.ADF_NAV, VwdFieldDescription.ADF_NAV_Vortag);
    }

    public Price getPreviousNetAssetValue() {
        final SnapField nav = getField(ADF_NAV);

        final SnapField price;
        final SnapField datefield;
        if (isDefinedAndNotZero(nav)) {
            price = getField(ADF_NAV_Vortag);
            datefield = getField(ADF_Schluss_Vortagesdatum);
        }
        else {
            price = getField(MMF_NAV_Vorvortag);
            datefield = getField(MMF_Schluss_Vorvortagesdatum);
        }

        return createPrice(price, null, null, getDateTime(datefield));
    }

    private Price getFundPrice(Field currentField, Field previousDayField) {
        // TODO: handle EndOfDay PriceQuality

        final SnapField price;
        final SnapField datefield;
        if (this.currentDayDefined) {
            price = getField(currentField);
            datefield = getField(ADF_Handelsdatum);
        }
        else {
            price = getField(previousDayField);
            datefield = isDefinedAndNotZero(ADF_Schluss_Vortagesdatum)
                    ? getField(ADF_Schluss_Vortagesdatum)
                    : getField(ADF_Vorheriges_Datum);
        }
        if (!price.isDefined()) {
            return NullPrice.INSTANCE;
        }

        return createPrice(price, null, null, getDateTime(datefield));
    }

    protected Price doGetPrice() {
        final SnapField nav = getField(ADF_NAV);
        final SnapField pnav = getField(ADF_NAV_Vortag);

        if (isDefinedAndNotZero(nav) || isDefinedAndNotZero(pnav)) {
            return getNetAssetValue();
        }
        return getRedemptionPrice();
    }

    @Override
    public Price getLastClose() {
        return getPrice();
    }

    @Override
    public Price getClose() {
        return getPrice();
    }

    protected Price doGetPreviousClose() {
        final Price nav = getPreviousNetAssetValue();
        if (nav.getValue() != null && nav.getValue().signum() != 0) {
            return nav;
        }
        return getPreviousRedemptionPrice();
    }

    public BigDecimal getChangeNet() {
        final BigDecimal cp = super.getChangeNet();
        if (cp != null) {
            return cp;
        }

        final SnapField previousClose = getField(ADF_Ruecknahme_Vortag);

        if (!isDefinedAndNotZero(previousClose)) {
            return null;
        }

        final SnapField bezahlt = getField(ADF_Ruecknahme);
        if (isDefinedAndNotZero(bezahlt)) {
            return bezahlt.getPrice().subtract(previousClose.getPrice());
        }

        final SnapField prepreviousClose = getField(MMF_Schluss_Vorvortag);
        if (isDefinedAndNotZero(prepreviousClose)) {
            return previousClose.getPrice().subtract(prepreviousClose.getPrice());
        }

        return null;
    }

    public BigDecimal getCloseBefore(LocalDate date) {
        final int[] dates = new int[]{
                ADF_Schluss_Vortagesdatum.id(),
                MMF_Schluss_Vorvortagesdatum.id()
        };
        final int[] prices = new int[]{
                ADF_Ruecknahme_Vortag.id(),
                MMF_Schluss_Vorvortag.id()
        };

        return getCloseBefore(date, dates, prices);
    }

    public BigDecimal getChangePercent() {
        final BigDecimal cp = super.getChangePercent();
        if (cp != null) {
            return cp;
        }

        final SnapField previousClose = getField(ADF_Ruecknahme_Vortag);

        if (!isDefinedAndNotZero(previousClose)) {
            return null;
        }

        final SnapField bezahlt = getField(ADF_Ruecknahme);
        if (isDefinedAndNotZero(bezahlt)) {
            return bezahlt.getPrice().subtract(previousClose.getPrice()).divide(previousClose.getPrice(), MC);
        }

        final SnapField prepreviousClose = getField(MMF_Schluss_Vorvortag);
        if (isDefinedAndNotZero(prepreviousClose)) {
            return previousClose.getPrice().subtract(prepreviousClose.getPrice()).divide(prepreviousClose.getPrice(), MC);
        }

        return null;
    }

    public Price getHighDay() {
        return NullPrice.INSTANCE;
    }

    public Price getLowDay() {
        return NullPrice.INSTANCE;
    }
}
