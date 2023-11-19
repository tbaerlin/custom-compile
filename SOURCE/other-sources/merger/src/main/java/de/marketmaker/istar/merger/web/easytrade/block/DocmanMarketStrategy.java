/*
 * DZMarketStrategy.java
 *
 * Created on 25.11.2009 13:46:00
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.Arrays;

import org.springframework.beans.factory.FactoryBean;

import de.marketmaker.istar.domain.instrument.QuoteOrder;

import static de.marketmaker.istar.merger.web.easytrade.block.LbbwMarketStrategy.*;

/**
 * @author oflege
 */
public class DocmanMarketStrategy implements FactoryBean {

    public Object getObject() throws Exception {
        return new MarketStrategy.Builder()
                .withFilter(QuoteFilters.WITH_VWDSYMBOL)
                .withSelectors(Arrays.asList(
                        LBBW_FONDS,
                        new QuoteSelectors.ByOrder(QuoteOrder.VOLUME_DE),
                        EUWAX_OR_FFMST,
                        GERMAN_HOME_EXHANGE_OR_INDEX,
                        LBBW_PREFERRED_EXCHANGES,
                        QuoteSelectors.SELECT_FIRST
                ))
                .build();
    }

    public Class getObjectType() {
        return MarketStrategy.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
