/*
 * BNDIssuerFinder.java
 *
 * Created on 07.05.12 14:48
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.rating;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.rating.IssuerRatingDescriptor;
import de.marketmaker.istar.merger.provider.rating.IssuerRatingMetaDataKey;
import de.marketmaker.istar.merger.provider.rating.IssuerRatingProvider;
import de.marketmaker.istar.merger.provider.rating.IssuerRatingSearchRequest;
import de.marketmaker.istar.merger.provider.rating.IssuerRatingSearchResponse;
import de.marketmaker.istar.merger.provider.rating.IssuerRatingTermVisitor;
import de.marketmaker.istar.merger.provider.rating.RatingSource;
import de.marketmaker.istar.merger.web.easytrade.FinderMetaItem;
import de.marketmaker.istar.merger.web.easytrade.ListCommand;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeCommandController;

/**
 * Queries bond issuers matching the given criteria.
 * <p>
 * Bond issuers are maintained using some meta data and a set of rating related fields. Searchable
 * are:
 * <ul>
 * <li><code>source</code></li>
 * <li><code>issuername</code></li>
 * <li><code>lei</code></li>
 * <li><code>countryiso</code></li>
 * <li><code>currencyiso</code></li>
 * </ul>
 * Sortable are:
 * <ul>
 * <li><code>issuername</code></li>
 * <li><code>ratingfitchissuerlongterm</code></li>
 * <li><code>ratingfitchissuerlongtermdate</code></li>
 * <li><code>ratingfitchissuershortterm</code></li>
 * <li><code>ratingfitchissuershorttermdate</code></li>
 * <li><code>ratingfitchissuerifs</code></li>
 * <li><code>ratingfitchissuerifsdate</code></li>
 * <li><code>ratingmoodysissuerlongterm</code></li>
 * <li><code>ratingmoodysissuerlongtermdate</code></li>
 * <li><code>ratingmoodysissuershortterm</code></li>
 * <li><code>ratingmoodysissuershorttermdate</code></li>
 * <li><code>ratingmoodysissuerbfs</code></li>
 * <li><code>ratingmoodysissuerbfsdate</code></li>
 * <li><code>ratingstandardandpoorsissuerlongterm</code></li>
 * <li><code>ratingstandardandpoorsissuerlongtermdate</code></li>
 * <li><code>ratingstandardandpoorsissuershortterm</code></li>
 * <li><code>ratingstandardandpoorsissuershorttermdate</code></li>
 * </ul>
 * </p>
 *
 * @author zzhao
 */
public class BndIssuerFinder extends EasytradeCommandController {

    static final List<IssuerRatingDescriptor> SORT_FIELDS = Arrays.asList(
            IssuerRatingDescriptor.ISSUERNAME,
            IssuerRatingDescriptor.RATING_FITCH_ISSUER_LT,
            IssuerRatingDescriptor.RATING_FITCH_ISSUER_LT_DATE,
            IssuerRatingDescriptor.RATING_FITCH_ISSUER_ST,
            IssuerRatingDescriptor.RATING_FITCH_ISSUER_ST_DATE,
            IssuerRatingDescriptor.RATING_FITCH_ISSUER_IFS,
            IssuerRatingDescriptor.RATING_FITCH_ISSUER_IFS_DATE,
            // Counterparty Rating (CTP)
            IssuerRatingDescriptor.RATING_MDYS_ISSR_LT,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_ST,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_D,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_D,
            // Counterparty Rating (CTP) backed
            IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_B,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_B,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_D_B,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_D_B,
            // Senior Unsecured Rating (SU)
            IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_SU,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_SU,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_D_SU,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_D_SU,
            // Senior Unsecured Rating (SU) backed
            IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_SU_B,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_SU_B,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_D_SU_B,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_D_SU_B,
            // Bank Deposit Rating (BDR)
            IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_BDR,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_BDR,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_D_BDR,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_D_BDR,
            // Bank Deposit Rating (BDR) backed
            IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_BDR_B,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_BDR_B,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_D_BDR_B,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_D_BDR_B,
            // Insurance Financial Strength Rating (IFS)
            IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_IFSR,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_IFSR,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_D_IFSR,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_D_IFSR,
            // Insurance Financial Strength Rating (IFS) backed
            IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_IFSR_B,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_IFSR_B,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_D_IFSR_B,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_D_IFSR_B,
            // ---
            IssuerRatingDescriptor.RATING_SNP_ISSUER_LT,
            IssuerRatingDescriptor.RATING_SNP_ISSUER_LT_DATE,
            IssuerRatingDescriptor.RATING_SNP_ISSUER_LT_RID,
            IssuerRatingDescriptor.RATING_SNP_ISSUER_ST,
            IssuerRatingDescriptor.RATING_SNP_ISSUER_ST_DATE,
            IssuerRatingDescriptor.RATING_SNP_ISSUER_LT_FSR,
            IssuerRatingDescriptor.RATING_SNP_ISSUER_LT_FSR_DATE,
            IssuerRatingDescriptor.RATING_SNP_ISSUER_ST_FSR,
            IssuerRatingDescriptor.RATING_SNP_ISSUER_ST_FSR_DATE,
            IssuerRatingDescriptor.RATING_SNP_ISSUER_LT_FER,
            IssuerRatingDescriptor.RATING_SNP_ISSUER_LT_FER_DATE,
            IssuerRatingDescriptor.RATING_SNP_ISSUER_ST_FER,
            IssuerRatingDescriptor.RATING_SNP_ISSUER_ST_FER_DATE
    );

    static final List<IssuerRatingMetaDataKey> META_DATA_KEYS = Arrays.asList(
            IssuerRatingMetaDataKey.SOURCE,
            IssuerRatingMetaDataKey.COUNTRY_ISO,
            IssuerRatingMetaDataKey.CURRENCY_ISO,
            IssuerRatingMetaDataKey.RATING_FITCH_ISSUER_LT,
            IssuerRatingMetaDataKey.RATING_FITCH_ISSUER_ST,
            IssuerRatingMetaDataKey.RATING_FITCH_ISSUER_IFS,
            // Counterparty Rating (CTP)
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_LT,
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_ST,
            // Counterparty Rating (CTP) backed
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_LT_B,
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_ST_B,
            // Senior Unsecured Rating (SU)
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_LT_SU,
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_ST_SU,
            // Senior Unsecured Rating (SU) backed
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_LT_SU_B,
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_ST_SU_B,
            // Bank Deposit Rating (BDR)
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_LT_BDR,
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_ST_BDR,
            // Bank Deposit Rating (BDR) backed
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_LT_BDR_B,
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_ST_BDR_B,
            // Insurance Financial Strength Rating (IFS)
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_LT_IFSR,
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_ST_IFSR,
            // Insurance Financial Strength Rating (IFS) backed
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_LT_IFSR_B,
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_ST_IFSR_B,
            // ---
            IssuerRatingMetaDataKey.RATING_SNP_ISSUER_LT,
            IssuerRatingMetaDataKey.RATING_SNP_ISSUER_LT_RID,
            IssuerRatingMetaDataKey.RATING_SNP_ISSUER_ST,
            IssuerRatingMetaDataKey.RATING_SNP_ISSUER_LT_FSR,
            IssuerRatingMetaDataKey.RATING_SNP_ISSUER_ST_FSR,
            IssuerRatingMetaDataKey.RATING_SNP_ISSUER_LT_FER,
            IssuerRatingMetaDataKey.RATING_SNP_ISSUER_ST_FER
    );

    public static class Command extends ListCommand {

        public static final String DEFAULT_SORT_BY = "issuername";

        public static final boolean DEFAULT_ASCENDING = false;

        private String query;

        private String source;

        private String issuername;

        private String countryIso;

        private String currencyIso;

        private boolean withDetailedSymbol;

        public Command() {
            setSortBy(DEFAULT_SORT_BY);
            setAscending(DEFAULT_ASCENDING);
        }

        /**
         * @return a query string using istar provider query syntax. Only for advanced usage. When
         *         set, other parameters are ignored.
         */
        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        /**
         * @return the source that provides issuer rating data.
         */
        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        /**
         * @return issuer names, separated with <code>@</code>. Issuers whose name contains the given
         *         parts will be included in search result.
         */
        public String getIssuername() {
            return issuername;
        }

        public void setIssuername(String issuername) {
            this.issuername = issuername;
        }

        /**
         * @return country ISOs, separated with <code>@</code>. Issuers whose country ISO is mentioned
         *         in this parameter will be included in search result.
         */
        public String getCountryIso() {
            return countryIso;
        }

        public void setCountryIso(String countryIso) {
            this.countryIso = countryIso;
        }

        /**
         * @return currency ISOs, separated with <code>@</code>. Issuers whose currency ISO is mentioned
         *         in this parameter will be included in search result.
         */
        public String getCurrencyIso() {
            return currencyIso;
        }

        public void setCurrencyIso(String currencyIso) {
            this.currencyIso = currencyIso;
        }

        /**
         * @return whether to sort the result in ascending order. Default is {@value #DEFAULT_ASCENDING}.
         */
        @Override
        public boolean isAscending() {
            return super.isAscending();
        }

        /**
         * Field on which the results are sorted. Default is {@value #DEFAULT_SORT_BY}.
         *
         * @return sort field.
         */
        @Override
        public String getSortBy() {
            return super.getSortBy();
        }

        public boolean isWithDetailedSymbol() {
            return withDetailedSymbol;
        }

        public void setWithDetailedSymbol(boolean withDetailedSymbol) {
            this.withDetailedSymbol = withDetailedSymbol;
        }
    }

    private IssuerRatingProvider provider;

    public BndIssuerFinder() {
        super(Command.class);
    }

    public void setProvider(IssuerRatingProvider provider) {
        this.provider = provider;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        final Command cmd = (Command) o;
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final Locale locale = RequestContextHolder.getRequestContext().getLocale();
        final List<IssuerRatingMetaDataKey> metaDataKeys = getMetaDataKeys(profile);
        final IssuerRatingSearchRequest req = new IssuerRatingSearchRequest(cmd.getOffset(),
                cmd.getCount(), cmd.getSortBy(), cmd.isAscending(), filterQuery(profile, getQuery(cmd)),
                cmd.isWithDetailedSymbol(), metaDataKeys);
        final IssuerRatingSearchResponse resp = this.provider.search(req);
        if (!resp.isValid()) {
            errors.reject("search.failure", resp.getMessage());
            return null;
        }
        else {
            final ListResult listInfo = ListResult.create(cmd, filterSortFields(profile),
                    cmd.getSortBy(), resp.getTotalCount());
            listInfo.setCount(resp.getIssuerRatings().size());

            final HashMap<String, Object> model = new HashMap<>(5);
            model.put("listinfo", listInfo);
            model.put("issuerratings", resp.getIssuerRatings());
            model.put("metadata", localize(locale, resp.getMetaData()));

            return new ModelAndView("bndissuerfinder", model);
        }
    }

    private Map<IssuerRatingMetaDataKey, List<FinderMetaItem>> localize(Locale locale, Map<IssuerRatingMetaDataKey, List<FinderMetaItem>> metaData) {
        final Map<IssuerRatingMetaDataKey, List<FinderMetaItem>> result = new HashMap<>();
        for (Map.Entry<IssuerRatingMetaDataKey, List<FinderMetaItem>> entry : metaData.entrySet()) {
            if (entry.getKey() == IssuerRatingMetaDataKey.COUNTRY_ISO) {
                result.put(entry.getKey(), localize(locale, entry.getValue()));
            } else {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private List<FinderMetaItem> localize(Locale locale, List<FinderMetaItem> value) {
        final List<FinderMetaItem> result = new ArrayList<>();
        for (FinderMetaItem item : value) {
            result.add(new FinderMetaItem(item.getKey(),
                    BndIssuerFinderMetaData.localize(locale, item.getName()),
                    item.getCount()));
        }
        return result;
    }

    static List<IssuerRatingMetaDataKey> getMetaDataKeys(Profile profile) {
        final List<IssuerRatingMetaDataKey> ret = new ArrayList<>(META_DATA_KEYS.size());
        for (IssuerRatingMetaDataKey metaDataKey : META_DATA_KEYS) {
            if (metaDataKey.getDesc().accept(profile)) {
                ret.add(metaDataKey);
            }
        }
        return ret;
    }

    static String filterQuery(Profile profile, String query) {
        final String srcConstraint = getSourceConstraint(profile);
        if (StringUtils.hasText(srcConstraint)) {
            return StringUtils.hasText(query) ? "(" + srcConstraint + ") AND (" + query + ")"
                    : srcConstraint;
        }
        else {
            return query;
        }
    }

    private static String getSourceConstraint(Profile profile) {
        final EnumSet<RatingSource> negative = EnumSet.noneOf(RatingSource.class);
        final RatingSource[] values = RatingSource.values();
        for (RatingSource src : values) {
            if (!src.accept(profile)) {
                negative.add(src);
            }
        }

        if (negative.size() == 0) {
            return null;
        }
        else {
            final StringBuilder sb = new StringBuilder();
            for (RatingSource src : negative) {
                if (sb.length() > 0) {
                    sb.append(" AND ");
                }
                sb.append(IssuerRatingDescriptor.SOURCE.getValue()).append(" != '")
                        .append(src.name()).append("'");
            }
            return sb.toString();
        }
    }

    static List<String> filterSortFields(Profile profile) {
        final ArrayList<String> ret = new ArrayList<>(SORT_FIELDS.size());
        for (IssuerRatingDescriptor desc : SORT_FIELDS) {
            if (desc.accept(profile)) {
                ret.add(desc.getValue());
            }
        }

        return ret;
    }

    static String getQuery(Command cmd) {
        if (null != cmd.getQuery()) {
            return cmd.getQuery();
        }
        else {
            final StringBuilder sb = new StringBuilder();
            fromField(IssuerRatingDescriptor.SOURCE, cmd, sb);
            fromField(IssuerRatingDescriptor.ISSUERNAME, cmd, sb);
            fromField(IssuerRatingDescriptor.COUNTRYISO, cmd, sb);
            fromField(IssuerRatingDescriptor.CURRENCYISO, cmd, sb);
            return sb.toString();
        }
    }

    private static void fromField(IssuerRatingDescriptor desc, Command cmd, StringBuilder sb) {
        final String val;
        switch (desc) {
            case SOURCE:
                val = cmd.getSource();
                break;
            case ISSUERNAME:
                val = cmd.getIssuername();
                break;
            case COUNTRYISO:
                val = cmd.getCountryIso();
                break;
            case CURRENCYISO:
                val = cmd.getCurrencyIso();
                break;
            default:
                throw new UnsupportedOperationException("no support for: " + desc);
        }

        if (!StringUtils.hasText(val)) {
            return;
        }

        if (sb.length() > 0) {
            sb.append(" AND ");
        }
        if (desc == IssuerRatingDescriptor.ISSUERNAME) {
            final String[] parts = val.split(IssuerRatingTermVisitor.SEP_OR);
            if (parts.length == 1) {
                sb.append(desc.getValue()).append(" =~ '.*?").append(parts[0]).append(".*'");
            }
            else {
                sb.append(desc.getValue()).append(" =~ '");
                for (int i = 0; i < parts.length; i++) {
                    sb.append(".*?").append(parts[i]).append(".*");
                    if (i < parts.length - 1) {
                        sb.append(IssuerRatingTermVisitor.SEP_OR);
                    }
                }
                sb.append("'");
            }
        }
        else {
            sb.append(desc.getValue()).append(" == '").append(val).append("'");
        }
    }
}
