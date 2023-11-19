package de.marketmaker.istar.merger.provider;


import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domain.special.DzBankRecord;
import de.marketmaker.istar.merger.query.DistinctValueCounter;
import de.marketmaker.istar.merger.web.easytrade.ListInfo;

import java.util.List;
import java.util.Set;

public class DzBankRecordSearchResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 5L;

    private final List<DzBankRecord> dzBankRecords;
    private final ListInfo listInfo;
    private final Set<? extends DistinctValueCounter> metadataSet;


    public DzBankRecordSearchResponse(List<DzBankRecord> dzBankRecords, ListInfo listInfo,
                                      Set<? extends DistinctValueCounter> metadataSet) {
        this.dzBankRecords = dzBankRecords;
        this.listInfo = listInfo;
        this.metadataSet = metadataSet;
    }

    public List<DzBankRecord> getDzBankRecords() {
        return dzBankRecords;
    }

    public ListInfo getListInfo() {
        return listInfo;
    }

    public Set<? extends DistinctValueCounter> getMetadataSet() {
        return metadataSet;
    }

}
