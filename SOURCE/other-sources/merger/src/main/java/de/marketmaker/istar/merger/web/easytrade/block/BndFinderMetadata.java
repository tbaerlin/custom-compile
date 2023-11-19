/*
 * BndFinderMetadata.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.data.RatioDataRecord.Field;
import java.util.Map;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * Provides meta data and their available values that can be used in {@see BND_Finder} for searching
 * bonds.
 * <p>
 * Each entry of meta data is given by a list of key/name pairs. Additionally an attribute
 * <code>enum</code> indicates whether the values listed by the keys are considered to be enums. A
 * query on meta data of enum values conforms with enum match, e.g. part of string match won't be
 * considered as a valid query result, i.e. a query with <code>issuerCategory=='Finanzinstitute'</code>
 * delivers only bonds whose issuers are categorized in "Finanzinstitute", not those bonds whose
 * issuers are categorized in "Finanzinstitute / Geschäftsbanken".
 * </p>
 * <p>
 * The results can be limited by following parameters:
 * <table border="1">
 * <tr><th>Name</th><th>Effect</th></tr>
 * <tr><td><code>country</code></td><td>Limit bond issuers to the given country</td></tr>
 * <tr><td><code>couponType</code></td><td>Limit bond types by the given coupon type</td></tr>
 * </table>
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BndFinderMetadata extends AbstractFinderMetadata {
    public static class Command extends AbstractFinderMetadata.Command {
        private String country;

        private String couponType;

        private boolean withDetailedSymbol;

        /**
         * A full country name in German. Bond issuers are limited to those that reside in the given
         * country.
         *
         * @return A full country name in German.
         */
        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public void setLand(String country) {
            setCountry(country);
        }

        public boolean isWithDetailedSymbol() {
            return withDetailedSymbol;
        }

        public void setWithDetailedSymbol(boolean withDetailedSymbol) {
            this.withDetailedSymbol = withDetailedSymbol;
        }

        /**
         * A coupon type. Bond types are limited to those that have the given coupon type.
         *
         * @return A coupon type.
         */
        public String getCouponType() {
            return couponType;
        }

        public void setCouponType(String couponType) {
            this.couponType = couponType;
        }

        public void setKupontyp(String kupontyp) {
            setCouponType(kupontyp);
        }
    }

    public BndFinderMetadata() {
        super(Command.class, InstrumentTypeEnum.BND,
                Field.country,
                Field.couponType,
                Field.bondType,
                Field.bondRank,
                Field.issuername,
                Field.ratingMoodys,
                Field.currency,
                Field.issuerCategory,
                Field.sector,
                Field.interestPeriod,
                Field.ratingFitchLongTerm,
                Field.ratingFitchLongTermAction,
                Field.ratingFitchShortTerm,
                Field.ratingFitchShortTermAction,
                Field.ratingMoodysShortTerm,
                Field.ratingMoodysShortTermAction,
                Field.ratingMoodysLongTerm,
                Field.ratingMoodysLongTermAction,
                Field.ratingSnPLocalShortTerm,
                Field.ratingSnPLocalLongTerm,
                Field.ratingSnPLongTerm,
                Field.ratingSnPLongTermAction,
                Field.ratingSnPShortTerm,
                Field.ratingSnPShortTermAction
        );
    }

    protected void onDoHandle(Object o, Map<String, Object> model) {
        final Command cmd = (Command) o;

        if (StringUtils.hasText(cmd.getCountry())) {
            final Map<String, Map<String, Integer>> issuerByCountry =
                    this.ratiosProvider.getMetaData(InstrumentTypeEnum.BND,
                            RatioFieldDescription.country.id(),
                            RatioFieldDescription.issuerName.id(), cmd.isWithDetailedSymbol());

            model.put("issuername", getKeyList(issuerByCountry, cmd.getCountry()));
        }

        if (StringUtils.hasText(cmd.getCouponType())) {
            final Map<String, Map<String, Integer>> bondtypeByCoupontype =
                    this.ratiosProvider.getMetaData(InstrumentTypeEnum.BND,
                            RatioFieldDescription.wmCoupontype.id(),
                            RatioFieldDescription.wmBondtype.id(), cmd.isWithDetailedSymbol());

            model.put("bondType", getKeyList(bondtypeByCoupontype, cmd.getCouponType()));
        }
    }

    @Override
    protected void afterMetaDataMethodConstructed(FinderMetadataMethod metadataMethod, Object cmd) {
        metadataMethod.setWithDetailedSymbol(((Command) cmd).isWithDetailedSymbol());
    }
}