package de.marketmaker.istar.merger.query;


import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteNameStrategy;
import de.marketmaker.istar.domain.special.DzBankRecord;
import de.marketmaker.istar.instrument.InstrumentRequest;
import de.marketmaker.istar.instrument.InstrumentServer;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class UnderlyingCounter implements Serializable, DistinctValueCounter {
    protected static final long serialVersionUID = 2L;

    public static final String PROPERTY_NAME = "underlying";

    private final TreeMap<PropertyValueKey, Integer> values;

    public UnderlyingCounter() {
        this(new TreeMap<PropertyValueKey, Integer>());
    }

    private UnderlyingCounter(TreeMap<PropertyValueKey, Integer> values) {
        this.values = values;
    }

    @Override
    public String getPropertyName() {
        return PROPERTY_NAME;
    }

    @Override
    public TreeMap<PropertyValueKey, Integer> getValues() {
        return values;
    }

    public UnderlyingCounter countUnderlyings(InstrumentServer instrumentServer,
            List<DzBankRecord> records) {
        final HashMap<Long, Integer> iid2count = createUnderlyingCounts(records);
        final List<Instrument> instruments = createInstrumentListForIids(instrumentServer, iid2count.keySet());

        for (Instrument instrument : instruments) {
            if (instrument != null) {
                final long iid = instrument.getId();
                DistinctValueKeyImpl key = new DistinctValueKeyImpl<>(
                        EasytradeInstrumentProvider.iidSymbol(instrument.getId()), instrument.getName());
                values.put(key, iid2count.get(iid));
            }
        }
        return this;
    }

    public UnderlyingCounter rewriteUnderlyings(QuoteNameStrategy nameStrategy,
            EasytradeInstrumentProvider instrumentProvider, boolean useVwdcodeAsKey) {
        final TreeMap<PropertyValueKey, Integer> tmp = new TreeMap<>();

        for (final Map.Entry<DistinctValueCounter.PropertyValueKey, Integer> entry : values.entrySet()) {
            final String iid = (String) entry.getKey().getValue();

            final Quote quote;
            try {
                quote = instrumentProvider.identifyQuote(iid, SymbolStrategyEnum.IID, null, null);
            } catch (UnknownSymbolException use) {
                continue;
            }
            if (quote == null) {
                continue;
            }

            final String metadataKey = useVwdcodeAsKey ? quote.getSymbolVwdcode() : iid;
            final String metadataValue = nameStrategy.getName(quote);
            tmp.put(new DistinctValueKeyImpl<>(metadataKey, metadataValue), entry.getValue());
        }

        return new UnderlyingCounter(tmp);
    }

    private HashMap<Long, Integer> createUnderlyingCounts(List<DzBankRecord> records) {
        final HashMap<Long, Integer> result = new HashMap<>();
        for (DzBankRecord record : records) {
            final Long uiid = record.getUnderlyingIid();
            Integer count = result.get(uiid);
            count = count == null ? 1 : count + 1;
            result.put(uiid, count);
        }
        return result;
    }

    private List<Instrument> createInstrumentListForIids(InstrumentServer instrumentServer,
            Set<Long> uiids) {
        final InstrumentRequest request = new InstrumentRequest();
        for (Long iid : uiids) {
            if (iid != null && !iid.equals(Long.valueOf(0))) {
                request.addItem(Long.toString(iid), InstrumentRequest.KeyType.IID);
            }
        }
        return instrumentServer.identify(request).getInstruments();
    }

}
