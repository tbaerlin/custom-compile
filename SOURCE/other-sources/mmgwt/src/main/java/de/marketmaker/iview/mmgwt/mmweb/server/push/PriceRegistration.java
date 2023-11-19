/*
 * Registration.java
 *
 * Created on 10.02.2010 07:00:15
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.push;

import java.util.BitSet;
import java.util.Collection;

import net.jcip.annotations.ThreadSafe;

import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.feed.ParsedRecord;
import de.marketmaker.istar.feed.ordered.OrderedUpdate;
import de.marketmaker.istar.instrument.InstrumentUtil;
import de.marketmaker.istar.merger.PriceRecordFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceDataType;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushPrice;

import static de.marketmaker.istar.domain.data.PriceQuality.REALTIME;

/**
 * @author oflege
 */
@ThreadSafe
class PriceRegistration extends Registration<PushPrice> {

    private final PriceDataType priceDataType;

    private final PriceUpdateFlags flags = new PriceUpdateFlags();

    private final String symbolVwdfeed;

    private final String market;

    PriceRegistration(Quote quote) {
        super(quote);
        this.priceDataType = getPriceDataType(quote);
        this.symbolVwdfeed = quote.getSymbolVwdfeed();
        this.market = quote.getSymbolVwdfeedMarket().intern();
    }

    // DO NOT MOVE THIS METHOD to PriceDataType: it is GWT-compliant code, cannot use Quote there
    private static PriceDataType getPriceDataType(Quote quote) {
        if (isFundOtc(quote)) {
            return PriceDataType.FUND_OTC;
        }
        if (isContract(quote)) {
            return PriceDataType.CONTRACT_EXCHANGE;
        }
        if (isLme(quote)) {
            return PriceDataType.LME;
        }

        return PriceDataType.STANDARD;
    }

    private static boolean isContract(Quote quote) {
        final InstrumentTypeEnum type = quote.getInstrument().getInstrumentType();
        return type == InstrumentTypeEnum.OPT || type == InstrumentTypeEnum.FUT;
    }

    private static boolean isFundOtc(Quote quote) {
        return quote.getInstrument().getInstrumentType() == InstrumentTypeEnum.FND
                && "FONDS".equals(quote.getSymbolVwdfeedMarket());
    }

    private static boolean isLme(Quote quote) {
        return InstrumentUtil.isLMEMarket(quote.getSymbolVwdfeedMarket());
    }

    @Override
    protected PushPrice createComplete(SnapRecord sr) {
        final PriceRecord pr = createPriceRecord(sr);
        final PushPrice result = PushPriceFactory.create(pr, this.priceDataType);
        result.setVwdCode(this.symbolVwdcode);
        return result;
    }

    @Override
    protected PushPrice createDiff(PushPrice complete, PushPrice previous, BitSet allowedFields) {
        final PushPrice result = PushPriceFactory.createDiff(complete, previous, "846900.ETR".equals(this.symbolVwdcode));
        this.flags.postProcessDiff(result, allowedFields);
        return result;
    }

    @Override
    void clearUpdated() {
        this.flags.reset();
        super.clearUpdated();
    }

    @Override
    protected boolean isFreshUpdate(ParsedRecord pr) {
        this.flags.update(pr);
        return super.isFreshUpdate();
    }

    @Override
    protected boolean isFreshUpdate(OrderedUpdate update) {
        this.flags.update(update);
        return super.isFreshUpdate();
    }

    private PriceRecord createPriceRecord(SnapRecord record) {
        // qid and price quality don't matter, use dummy values
        return new PriceRecordFactory.Builder(0L, record, REALTIME)
                .withSymbolVwdfeed(this.symbolVwdfeed)
                .withMarket(this.market)
                .build();
    }

    protected void push(Collection<AbstractClient> clients, PushPrice price) {
        for (AbstractClient client : clients) {
            client.push(price);
        }
    }
}
