package de.marketmaker.istar.domain.data;


import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * Encapsulates the rules for computing synthetic trades
 */
public class SyntheticTickBuilder {
    private static final long VOL = 0;

    private static final String SUPPLEMENT = null;

    private static final String TRADE_ID = null;

    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    private BigDecimal latestAsk = null;

    private BigDecimal latestBid = null;

    public void updateAsk(BigDecimal ask) {
        assert ask != null;
        this.latestAsk = ask;
    }

    public void updateBid(BigDecimal bid) {
        assert bid != null;
        this.latestBid = bid;
    }

    public TickImpl build(DateTime dt) {
        return new TickImpl(dt, getSyntheticPrice(), VOL, SUPPLEMENT, TRADE_ID, TickImpl.Type.SYNTHETIC_TRADE);
    }

    public BigDecimal getSyntheticPrice() {
        if (this.latestAsk == null) {
            if (this.latestBid == null) {
                throw new IllegalStateException();
            }
            return this.latestBid;
        }
        if (this.latestBid == null) {
            return this.latestAsk;
        }
        return this.latestAsk.add(this.latestBid).divide(TWO);
    }
}
