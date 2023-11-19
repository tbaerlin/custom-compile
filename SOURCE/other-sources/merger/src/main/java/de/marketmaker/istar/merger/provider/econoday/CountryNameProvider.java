/*
 * CountryNameProvider.java
 *
 * Created on 10.04.12 08:35
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import de.marketmaker.istar.common.spring.MessageSourceFactory;
import de.marketmaker.istar.domain.Language;

/**
 * @author zzhao
 */
public class CountryNameProvider {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final MessageSource NAMES = MessageSourceFactory.create(CountryNameProvider.class);

    public String getCountryName(String symbol, Language lang) {
        try {
            return NAMES.getMessage(symbol, null, lang.getLocale());
        } catch (NoSuchMessageException e) {
            this.logger.error("<getCountryName> missing ISO 3166 entry for: {}, language {}",
                    symbol, lang);
            return symbol;
        }
    }
}
