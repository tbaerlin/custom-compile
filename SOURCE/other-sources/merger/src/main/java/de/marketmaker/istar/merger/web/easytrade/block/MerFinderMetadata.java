/*
 * MerFinderMetadata.java
 *
 * Created on 15.10.2015 10:35:42
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.merger.web.QueryCommand;

/**
 * @author mdick
 */
public class MerFinderMetadata extends AbstractFinderMetadata {
    public static class Command extends AbstractFinderMetadata.Command implements QueryCommand {
        private String query;

        public void setQuery(String query) {
            this.query = query;
        }

        @Override
        public String getQuery() {
            return this.query;
        }
    }

    public MerFinderMetadata() {
        super(Command.class, InstrumentTypeEnum.MER,
                RatioDataRecord.Field.market,
                RatioDataRecord.Field.currency,
                RatioDataRecord.Field.lmeMetalCode);
    }

    @Override
    protected String getQuery(Object o) {
        if (o instanceof Command) {
            return ((Command) o).getQuery();
        }
        return super.getQuery(o);
    }
}