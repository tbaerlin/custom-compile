/*
 * FinderFND.java
 *
 * Created on 10.06.2008 13:44:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import de.marketmaker.iview.dmxml.OPTFinderElement;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.OptUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class OPTFinderElementMapper {

    static final RowMapper<OPTFinderElement> BASE_ROW_MAPPER = new AbstractRowMapper<OPTFinderElement>() {
        public Object[] mapRow(OPTFinderElement e) {
            return new Object[]{
                    createQuoteWithInsturment(e),
                    e.getExpirationDate(),
                    Renderer.WARRANT_TYPE.render(e.getOptionType()),
                    e.getStrike(),
                    e.getPrice(),
                    e.getVolumeTrade(),
                    e.getChangeNet(),
                    e.getChangePercent(),
                    e.getVolume(),
                    e.getBid(),
                    e.getAsk(),
                    e.getDate(),
                    e.getPreviousSettlement()
            };
        }

        @Override
        public String getRowClass(int row, OPTFinderElement e) {
            return OptUtil.getNearMoneyStyle(e);
        }
    };

    static final RowMapper<OPTFinderElement> GREEK_ROW_MAPPER = new AbstractRowMapper<OPTFinderElement>() {
        public Object[] mapRow(OPTFinderElement e) {
            return new Object[]{
                    createQuoteWithInsturment(e),
                    e.getDelta(),
                    e.getOmega(),
                    e.getGamma(),
                    e.getTheta(),
                    e.getRho(),
                    e.getVega()
            };
        }
    };

    private static QuoteWithInstrument createQuoteWithInsturment(OPTFinderElement e) {
        return AbstractFinder.createQuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata());
    }

}
