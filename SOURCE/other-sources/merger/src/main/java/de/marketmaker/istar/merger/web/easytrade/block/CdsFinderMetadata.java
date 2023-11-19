package de.marketmaker.istar.merger.web.easytrade.block;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.merger.web.easytrade.FinderMetaItem;

/**
 * <p>
 * Provides meta data and their available values that can be used in {@see CDS_Finder} for
 * searching credit default swaps.
 * <p/>
 *
 */
public class CdsFinderMetadata extends AbstractFinderMetadata {

    public CdsFinderMetadata() {
        super(Command.class, InstrumentTypeEnum.ZNS,
                "cdsfindermetadata",
                RatioDataRecord.Field.znsCategory,
                RatioDataRecord.Field.issuername,
                RatioDataRecord.Field.maturity,
                RatioDataRecord.Field.debtRanking,
                RatioDataRecord.Field.issuerType,
                RatioDataRecord.Field.restructuringRule,
                RatioDataRecord.Field.currency,
                RatioDataRecord.Field.source,
                RatioDataRecord.Field.country,
                RatioDataRecord.Field.sector,
                RatioDataRecord.Field.rating,
                RatioDataRecord.Field.standard);
    }

    @Override
    protected String getQuery(Object o) {
        return "znsCategory=='CDS'";
    }

    @Override
    protected void onDoHandle(Object o, Map<String, Object> model) {
        localizeModelElement(model, RatioDataRecord.Field.restructuringRule.name());
        localizeModelElement(model, RatioDataRecord.Field.debtRanking.name());
    }

    protected void localizeModelElement( Map<String, Object> model, String key) {
        model.put(key, localizeStringList((List<String>) (model.get(key)), key));
    }

    static List<FinderMetaItem> localizeItemList(List<FinderMetaItem> elements, String key) {
        final List<FinderMetaItem> result = new ArrayList<>(elements.size());
        for (FinderMetaItem value : elements) {
            result.add(new FinderMetaItem(value.getKey(),
                    localize(key + "." + value.getName(), value.getName()), value.getCount()));
        }
        return result;
    }

    static List<FinderMetaItem> localizeStringList(List<String> oldEntries, String key) {
        final List<FinderMetaItem> newEntries = new ArrayList<>(oldEntries.size());
        for (String value : oldEntries) {
            newEntries.add(new FinderMetaItem(value, localize(key + "." + value, value), 0));
        }
        return newEntries;
    }

}
