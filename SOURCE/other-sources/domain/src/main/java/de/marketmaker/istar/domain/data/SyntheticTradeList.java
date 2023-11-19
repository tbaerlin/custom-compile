package de.marketmaker.istar.domain.data;


import de.marketmaker.istar.common.util.PriceCoder;

import org.joda.time.DateTime;

import java.util.BitSet;
import java.util.Iterator;


public class SyntheticTradeList extends TickList {

    SyntheticTradeList(final Iterable<DataWithInterval<TickEvent>> timeseries, DateTime startDateTime,
            DateTime endDateTime) {
        super(timeseries, TickImpl.Type.SYNTHETIC_TRADE, startDateTime, endDateTime);
    }

    private boolean isUsableAsk(TickEvent tickEvent) {
        return tickEvent.isAsk() && tickEvent.getAskPrice() > 0;
    }

    private boolean isUsableBid(TickEvent tickEvent) {
        return tickEvent.isBid() && tickEvent.getBidPrice() > 0;
    }

    @Override
    public TickList withAdditionalFields(BitSet additionalFieldIds) {
        return this;
    }

    @Override
    public TickList withPermissions(FieldPermissions permissions) {
        return this;
    }

    @Override
    public Iterator<TickImpl> iterator() {
        return new Iterator<TickImpl>() {
            private final SyntheticTickBuilder builder = new SyntheticTickBuilder();

            private final Iterator<DataWithInterval<TickEvent>> it = timeseries.iterator();

            private TickImpl next = advance();

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean hasNext() {
                return this.next != null;
            }

            @Override
            public TickImpl next() {
                assert hasNext();
                TickImpl result = this.next;
                this.next = advance();
                return result;
            }

            protected TickImpl advance() {
                while (this.it.hasNext()) {
                    DataWithInterval<TickEvent> next = this.it.next();
                    final TickEvent tickEvent = next.getData();
                    boolean usable = false;
                    if (isUsableAsk(tickEvent)) {
                        this.builder.updateAsk(PriceCoder.decode(tickEvent.getAskPrice()));
                        usable = true;
                    }
                    if (isUsableBid(tickEvent)) {
                        this.builder.updateBid(PriceCoder.decode(tickEvent.getBidPrice()));
                        usable = true;
                    }
                    if (usable) {
                        return this.builder.build(next.getInterval().getStart());
                    }
                }
                return null;
            }
        };
    }
}
