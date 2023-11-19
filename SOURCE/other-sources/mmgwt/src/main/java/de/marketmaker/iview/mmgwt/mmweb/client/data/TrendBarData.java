/*
* TrendBarData.java
*
* Created on 26.08.2008 10:47:56
*
* Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client.data;

import java.util.List;

import de.marketmaker.iview.dmxml.HasPricedata;
import de.marketmaker.iview.dmxml.MSCInstrumentPriceSearch;
import de.marketmaker.iview.dmxml.MSCListDetails;
import de.marketmaker.iview.dmxml.MSCPriceDatas;
import de.marketmaker.iview.dmxml.PFEvaluation;
import de.marketmaker.iview.dmxml.PortfolioPositionElement;
import de.marketmaker.iview.dmxml.WLWatchlist;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceDataType;

/**
 * @author Michael Lösch
 */
public class TrendBarData {
    private String positiveSideWidth;

    private String negativeSideWidth;

    private float min;

    private float max;

    private boolean valid;

    // simple factory that maintains min and max value
    private static class Factory {
        private float min = 0;

        private float max = 0;

        public void update(String s) {
            if (s == null) {
                return;
            }
            final float change = Float.parseFloat(s);
            this.min = Math.min(this.min, change);
            this.max = Math.max(this.max, change);
        }


        private TrendBarData finish() {
            return new TrendBarData(this.max, this.min);
        }

        private void update(HasPricedata data) {
            switch (PriceDataType.fromDmxml(data.getPricedatatype())) {
                case FUND_OTC:
                    if (data.getFundpricedata() != null) {
                        update(data.getFundpricedata().getChangePercent());
                    }
                    break;
                case CONTRACT_EXCHANGE:
                    if (data.getContractpricedata() != null) {
                        update(data.getContractpricedata().getChangePercent());
                    }
                    break;
                default:
                    if (data.getPricedata() != null) {
                        update(data.getPricedata().getChangePercent());
                    }
            }
        }
    }

    public static TrendBarData create(WLWatchlist wl) {
        return create(wl.getPositions().getPosition());
    }

    public static TrendBarData createGuV(PFEvaluation wl) {
        final TrendBarData.Factory factory = new TrendBarData.Factory();
        for (PortfolioPositionElement e : wl.getElements().getPosition()) {
            factory.update(e.getPositionChangePercentInPortfolioCurrency());
        }
        return factory.finish();
    }

    public static TrendBarData createPrice(PFEvaluation wl) {
        return create(wl.getElements().getPosition());
    }

    public static TrendBarData create(MSCPriceDatas priceDatas) {
        return create(priceDatas.getElement());
    }

    public static TrendBarData create(MSCInstrumentPriceSearch priceSearch) {
        return create(priceSearch.getElement());
    }

    public static TrendBarData create(MSCListDetails listDetails) {
        return create(listDetails.getElement());
    }

    public static TrendBarData create(List<? extends HasPricedata> elements) {
        final TrendBarData.Factory factory = new TrendBarData.Factory();
        for (HasPricedata e : elements) {
            factory.update(e);
        }
        return factory.finish();
    }

    private TrendBarData(float maxChangeInList, float minChangeInList) {
        this.min = minChangeInList;
        this.max = maxChangeInList;
        this.positiveSideWidth = calcPositiveSideWidth();
        this.negativeSideWidth = calcNegativeSideWidth();
        this.valid = (this.min < 0) || (this.max > 0);
    }

    private String calcPositiveSideWidth() {
        if (this.max == 0) {
            return "0%"; // $NON-NLS-0$
        }
        if (this.min == 0) {
            return "100%"; // $NON-NLS-0$
        }
        return Math.round((this.max / ((this.min * -1) + this.max)) * 100) + "%"; // $NON-NLS-0$
    }

    private String calcNegativeSideWidth() {
        if (this.max == 0) {
            return "100%"; // $NON-NLS-0$
        }
        if (this.min == 0) {
            return "0%"; // $NON-NLS-0$
        }
        return Math.round(((this.min * -1) / ((this.min * -1) + this.max)) * 100) + "%"; // $NON-NLS-0$
    }

    public String getPositiveSideWidth() {
        return this.positiveSideWidth;
    }

    public String getNegativeSideWidth() {
        return this.negativeSideWidth;
    }

    public String getPositiveWidth(String changeOfCurrentPosition) {
        if (changeOfCurrentPosition == null) {
            return "0%"; // $NON-NLS-0$
        }
        final float current = Float.parseFloat(changeOfCurrentPosition);
        if (current <= 0) {
            return "0%"; // $NON-NLS-0$
        }
        return (current / max * 100) + "%"; // $NON-NLS-0$
    }

    public String getNegativeWidth(String changeOfCurrentPosition) {
        if (changeOfCurrentPosition == null) {
            return "0%"; // $NON-NLS-0$
        }
        final float current = Float.parseFloat(changeOfCurrentPosition);
        if (current >= 0) {
            return "0%"; // $NON-NLS-0$
        }
        return (current / this.min * 100) + "%"; // $NON-NLS-0$
    }

    public boolean isValid(String changeOfCurrentPosition) {
        return ((changeOfCurrentPosition != null) && this.valid);
    }
}
