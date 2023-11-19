package de.marketmaker.iview.mmgwt.mmweb.client.util;

import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.ContentFlagsEnum;


public final class ContentFlagUtil {

    private ContentFlagUtil(){}

    public static boolean hasAccessibleResearchDocuments(QuoteData quoteData) {
        // HM1 is a subset of HM2 is a subset of HM3
        // there is at most one of HM1,HM2,HM3 enabled
        return (
                (ContentFlagsEnum.ResearchDzHM1.isAvailableFor(quoteData)
                        && (Selector.DZHM3.isAllowed() || Selector.DZHM2.isAllowed() || Selector.DZHM1.isAllowed()))

                        || (ContentFlagsEnum.ResearchDzHM2.isAvailableFor(quoteData)
                        && (Selector.DZHM3.isAllowed() || Selector.DZHM2.isAllowed()))

                        || (ContentFlagsEnum.ResearchDzHM3.isAvailableFor(quoteData)
                        && (Selector.DZHM3.isAllowed()))

                        || (ContentFlagsEnum.ResearchDzFP4.isAvailableFor(quoteData)
                        && Selector.DZFP4.isAllowed())
        );
    }

}
