/*
 * IndexCompositionResponse.java
 *
 * Created on 21.03.14 11:06
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domainimpl.data.IndexComposition;

/**
 * @author oflege
 */
public class IndexCompositionResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 122L;

    private final IndexComposition indexComposition;

    public IndexCompositionResponse(IndexComposition indexComposition) {
        this.indexComposition = indexComposition;
        if (indexComposition == null) {
            setInvalid();
        }
    }

    public IndexComposition getIndexComposition() {
        return this.indexComposition;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(", ").append(this.indexComposition);
    }
}
