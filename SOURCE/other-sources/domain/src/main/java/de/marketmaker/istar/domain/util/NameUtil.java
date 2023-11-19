/*
 * NameUtil.java
 *
 * Created on 21.09.2006 09:35:02
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.util;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class NameUtil {

    public static String getDisplayName1(Quote quote) {
        if (quote.getInstrument().getInstrumentType() == InstrumentTypeEnum.CER) {
            final String gatrixxName = quote.getInstrument().getSymbol(KeysystemEnum.GATRIXX);
            if (StringUtils.hasText(gatrixxName)) {
                return gatrixxName;
            }
        }

        final String nameKurz = quote.getSymbolWmWpNameKurz();
        if (StringUtils.hasText(nameKurz)) {
            return nameKurz;
        }

        final String mdpName = quote.getInstrument().getName();
        if (StringUtils.hasText(mdpName)) {
            return mdpName;
        }

        final String isin = quote.getInstrument().getSymbolIsin();
        if (StringUtils.hasText(isin)) {
            return isin;
        }

        final String vwdcode = quote.getSymbolVwdcode();
        if (StringUtils.hasText(vwdcode)) {
            return vwdcode;
        }

        return "qid:" + quote.getId();
    }

    public static String getMarketmanagerName(Quote quote) {
        final String nameKurz = quote.getSymbolWmWpNameKurz();
        if (StringUtils.hasText(nameKurz)) {
            return nameKurz;
        }

        final String mdpName = quote.getInstrument().getName();
        if (StringUtils.hasText(mdpName)) {
            return mdpName;
        }

        final String isin = quote.getInstrument().getSymbolIsin();
        if (StringUtils.hasText(isin)) {
            return isin;
        }

        final String vwdcode = quote.getSymbolVwdcode();
        if (StringUtils.hasText(vwdcode)) {
            return vwdcode;
        }

        return "qid:" + quote.getId();
    }
}
