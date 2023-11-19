/*
 * FndFindersuchkriterien.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.merger.web.easytrade.ProviderSelectionCommand;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * Provides meta data and their available values that can be used in {@see FND_Finder} for
 * searching funds.
 * <p>
 * Each entry of meta data is given by a list of key/name pairs. Additionally an attribute
 * <code>enum</code> indicates whether the values listed by the keys are considered to be enums. A
 * query on meta data of enum values conforms with enum match, e.g. part of string match won't be
 * considered as a valid query result, i.e. a query with <code>issuerCategory=='Finanzinstitute'</code>
 * delivers only funds whose issuers are categorized in "Finanzinstitute", not those funds whose
 * issuers are categorized in "Finanzinstitute / Geschäftsbanken".
 * </p>
 * <p>
 * The returned meta data can be further limited by a given fund type.
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FndFindersuchkriterien extends AbstractFinderMetadata {
    public static class Command implements ProviderSelectionCommand {
        private String fondstyp;
        private String providerPreference;

        /**
         * @return a fund type to limit the returned meta data.
         */
        public String getFondstyp() {
            return fondstyp;
        }

        public void setFondstyp(String fondstyp) {
            this.fondstyp = fondstyp;
        }

        @RestrictedSet("FWW,VWDIT,VWDBENL,MORNINGSTAR,SSAT,FIDA,VWD")
        public String getProviderPreference() {
            return providerPreference;
        }

        public void setProviderPreference(String providerPreference) {
            this.providerPreference = providerPreference;
        }
    }

    public FndFindersuchkriterien() {
        super(Command.class, InstrumentTypeEnum.FND,
                RatioDataRecord.Field.fundtype,
                RatioDataRecord.Field.subtype,
                RatioDataRecord.Field.investmentFocus,
                RatioDataRecord.Field.issuername,
                RatioDataRecord.Field.currency,
                RatioDataRecord.Field.country,
                RatioDataRecord.Field.distributionStrategy,
                RatioDataRecord.Field.ratingMorningstar,
                RatioDataRecord.Field.ratingFeri,
                RatioDataRecord.Field.fidaRating,
                RatioDataRecord.Field.marketAdmission,
                RatioDataRecord.Field.diamondRating,
                RatioDataRecord.Field.srriValue
        );
    }

    public void setRatiosProvider(RatiosProvider ratiosProvider) {
        this.ratiosProvider = ratiosProvider;
    }

    protected String getQuery(Object o) {
        final Command cmd = (Command) o;
        final String fundtype = cmd.getFondstyp();
        if (StringUtils.hasText(fundtype)) {
            return "fundtype=='" + fundtype + "'";
        }
        return null;
    }

    protected void onDoHandle(Object o, Map<String, Object> model) {
        final Command cmd = (Command) o;
        if (StringUtils.hasText(cmd.getFondstyp())) {
            final Map<String, Object> m = new FinderMetadataMethod(InstrumentTypeEnum.FND,
                    this.ratiosProvider, this.instrumentProvider,
                    new RatioDataRecord.Field[]{RatioDataRecord.Field.fundtype},
                    cmd.getProviderPreference(), null).invoke();
            model.put("fundtype", m.get("fundtype"));
        }
    }
}
