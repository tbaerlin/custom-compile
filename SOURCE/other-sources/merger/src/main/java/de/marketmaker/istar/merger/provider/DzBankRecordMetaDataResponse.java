package de.marketmaker.istar.merger.provider;


import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.merger.query.DistinctValueCounter;
import groovy.lang.Tuple;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

public class DzBankRecordMetaDataResponse extends AbstractIstarResponse {
    protected static final long serialVersionUID = 4L;

    private int size;
    private final Set<String> fields;
    private final Set<? extends DistinctValueCounter> metadataSet;

    public DzBankRecordMetaDataResponse(int size, Set<String> fields,
                                        Set<? extends DistinctValueCounter> metadataSet) {
        this.size = size;
        this.fields = fields;
        this.metadataSet = metadataSet;
    }

    public int getSize() {
        return size;
    }

    public Set<String> getFields() {
        return fields;
    }

    public Set<? extends DistinctValueCounter> getMetadataSet() {
        return metadataSet;
    }

}
