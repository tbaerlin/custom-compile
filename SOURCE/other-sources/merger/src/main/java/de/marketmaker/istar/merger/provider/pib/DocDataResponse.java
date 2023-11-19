/*
 * PibRequest.java
 *
 * Created on 28.03.11 16:33
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pib;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zzhao
 */
public class DocDataResponse extends PibAvailabilityResponse {

    public static final DocDataResponse NOT_AVAILABLE = new DocDataResponse();

    private static final long serialVersionUID = -2288100148179094874L;

    // todo for user editing blocks will contain more than one block with same key from different sources
    private final List<Block> blocks;

    private DocDataResponse() {
        super();
        this.blocks = null;
    }

    public DocDataResponse(String isin, String wkn, String name, String issuer, String gb198c,
            boolean available, List<Block> blocks) {
        super(isin, wkn, name, gb198c, available, issuer, false, false);
        this.blocks = new ArrayList<>(blocks);
    }

    public List<Block> getBlocks() {
        return new ArrayList<>(blocks);
    }

    protected void appendToString(StringBuilder sb) {
        sb.append(", blocks=").append(blocks);
    }
}
