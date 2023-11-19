/*
 * FndFindersuchergebnis.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.common.util.ArraysUtil;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.PermissionType;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.domainimpl.profile.ResourcePermissionProvider;
import de.marketmaker.istar.instrument.InstrumentUtil;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.web.QueryCommand;
import de.marketmaker.istar.merger.web.easytrade.ListCommand;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.DataRecordStrategy;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.RatioSearchResponse;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Queries funds that match given criteria.
 * <p>
 * Criteria can be specified by:
 * <ul>
 * <li><code>query</code> parameter, which is composed of field predicates. Allowed search fields
 * can be found in the sort field lists in the response. Note that some fields have limited
 * values. Those values can be queried using {@see FND_FinderMetadata}.</li>
 * <li>various search parameters related to fund ratio fields. The expressiveness of this style
 * of query is limited by its nature. It is recommended to use the <code>query</code> parameter
 * directly.</li>
 * </ul>
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FndFindersuchergebnis extends AbstractFindersuchergebnis {
    private static final int MAX_NUM_KAGS = 5;

    private IntradayProvider intradayProvider;

    public FndFindersuchergebnis() {
        super(Command.class);
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public static class Command extends ListCommand implements QueryCommand {
        private String[] rawparam;

        private String[] searchstring;

        private String fondstyp;

        private String sektor;

        private String[] kag;

        private String ausschuettungsart;

        private String waehrung;

        private String performance1y;

        private String performance3y;

        private String performance5y;

        private String volatilitaet;

        private String sharperatio;

        private String verwaltungsgebuehr;

        private String depotbankverguetung;

        private String ausgabeaufschlag;

        private String etf;

        private String fundVolume;

        private String issueDate;

        private String vertriebszulassung;

        private String ter;

        private String marketAdmission;

        private String query;

        private DataRecordStrategy.Type dataRecordStrategy = DataRecordStrategy.Type.PREFER_FUND_ISSUER_QUOTE;

        private String providerPreference;

        public void setSearchstring(String[] searchstring) {
            this.searchstring = ArraysUtil.copyOf(searchstring);
        }

        /**
         * @return string values to be searched in fields ISIN, WKN or name.
         */
        @MmInternal
        public String[] getSearchstring() {
            return ArraysUtil.copyOf(searchstring);
        }

        /**
         * @return a fund type.
         */
        @MmInternal
        public String getFondstyp() {
            return fondstyp;
        }

        public void setFondstyp(String fondstyp) {
            this.fondstyp = fondstyp;
        }

        /**
         * @return an investment sector.
         */
        @MmInternal
        public String getSektor() {
            return sektor;
        }

        public void setSektor(String sektor) {
            this.sektor = sektor;
        }

        /**
         * @return names of Capital Investment Companies.
         */
        @MmInternal
        public String[] getKag() {
            return ArraysUtil.copyOf(kag);
        }

        public void setKag(String[] kag) {
            this.kag = ArraysUtil.copyOf(kag);
        }

        /**
         * @return method of dividend payout.
         */
        @MmInternal
        public String getAusschuettungsart() {
            return ausschuettungsart;
        }

        public void setAusschuettungsart(String ausschuettungsart) {
            this.ausschuettungsart = ausschuettungsart;
        }

        /**
         * @return fund currency in 3-character currency symbol.
         */
        @MmInternal
        public String getWaehrung() {
            return waehrung;
        }

        public void setWaehrung(String waehrung) {
            this.waehrung = waehrung;
        }

        /**
         * @return fund price volatility.
         */
        @MmInternal
        public String getVolatilitaet() {
            return volatilitaet;
        }

        public void setVolatilitaet(String volatilitaet) {
            this.volatilitaet = volatilitaet;
        }

        /**
         * @return fund performance in the past one year.
         */
        @MmInternal
        public String getPerformance1y() {
            return performance1y;
        }

        public void setPerformance1y(String performance1y) {
            this.performance1y = performance1y;
        }

        /**
         * @return fund performance in the past three years.
         */
        @MmInternal
        public String getPerformance3y() {
            return performance3y;
        }

        public void setPerformance3y(String performance3y) {
            this.performance3y = performance3y;
        }

        /**
         * @return fund performance in the past five years.
         */
        @MmInternal
        public String getPerformance5y() {
            return performance5y;
        }

        public void setPerformance5y(String performance5y) {
            this.performance5y = performance5y;
        }

        /**
         * @return sharpe ratio to measure risk-adjusted performance.
         */
        @MmInternal
        public String getSharperatio() {
            return sharperatio;
        }

        public void setSharperatio(String sharperatio) {
            this.sharperatio = sharperatio;
        }

        /**
         * @return management fee
         */
        @MmInternal
        public String getVerwaltungsgebuehr() {
            return verwaltungsgebuehr;
        }

        public void setVerwaltungsgebuehr(String verwaltungsgebuehr) {
            this.verwaltungsgebuehr = verwaltungsgebuehr;
        }

        /**
         * @return custodial bank fees for depot maintenance.
         */
        @MmInternal
        public String getDepotbankverguetung() {
            return depotbankverguetung;
        }

        public void setDepotbankverguetung(String depotbankverguetung) {
            this.depotbankverguetung = depotbankverguetung;
        }

        /**
         * @return issue surcharge.
         */
        @MmInternal
        public String getAusgabeaufschlag() {
            return ausgabeaufschlag;
        }

        public void setAusgabeaufschlag(String ausgabeaufschlag) {
            this.ausgabeaufschlag = ausgabeaufschlag;
        }

        /**
         * @return total expense ratio.
         */
        @MmInternal
        public String getTer() {
            return ter;
        }

        public void setTer(String ter) {
            this.ter = ter;
        }

        /**
         * @return market admission given by 2-character country symbol.
         */
        @MmInternal
        public String getMarketAdmission() {
            return marketAdmission;
        }

        public void setMarketAdmission(String marketAdmission) {
            this.marketAdmission = marketAdmission;
        }

        /**
         * @return if the funds are exchange-traded funds.
         */
        @MmInternal
        public String getEtf() {
            return etf;
        }

        public void setEtf(String etf) {
            this.etf = etf;
        }

        /**
         * @return emission volume of funds.
         */
        @MmInternal
        public String getFundVolume() {
            return fundVolume;
        }

        public void setFundVolume(String fundVolume) {
            this.fundVolume = fundVolume;
        }

        /**
         * @return emission date of funds.
         */
        @MmInternal
        public String getIssueDate() {
            return issueDate;
        }

        public void setIssueDate(String issueDate) {
            this.issueDate = issueDate;
        }

        /**
         * @return if the funds are allowed to be traded in Germany exchange market.
         */
        @MmInternal
        public String getVertriebszulassung() {
            return vertriebszulassung;
        }

        public void setVertriebszulassung(String vertriebszulassung) {
            this.vertriebszulassung = vertriebszulassung;
        }

        @MmInternal
        public String[] getRawparam() {
            return rawparam;
        }

        public void setRawparam(String[] rawparam) {
            this.rawparam = rawparam;
        }

        /**
         * {@inheritDoc}
         *
         * @sample issuername==Deka
         */
        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        /**
         * @return A strategy for selecting one out of a number of data record elements.
         */
        @MmInternal
        public DataRecordStrategy.Type getDataRecordStrategy() {
            return dataRecordStrategy;
        }

        public void setDataRecordStrategy(DataRecordStrategy.Type dataRecordStrategy) {
            this.dataRecordStrategy = dataRecordStrategy;
        }

        @RestrictedSet("FWW,VWDIT,VWDBENL,MORNINGSTAR,SSAT,FIDA,VWD")
        public String getProviderPreference() {
            return providerPreference;
        }

        public void setProviderPreference(String providerPreference) {
            this.providerPreference = providerPreference;
        }
    }

    private static final Map<String, String> SORTFIELDS = new HashMap<>();

    static {
        SORTFIELDS.put("name", "name");
        SORTFIELDS.put("wkn", "wkn");
        SORTFIELDS.put("isin", "isin");
        SORTFIELDS.put("vwdCode", "vwdCode");
        SORTFIELDS.put("spStars", "spStars");
        SORTFIELDS.put("bviperformance1y", "bviperformance1y");
        SORTFIELDS.put("bviperformance3y", "bviperformance3y");
        SORTFIELDS.put("issueSurcharge", "issueSurcharge");
        SORTFIELDS.put("postbankSurcharge", "postbankSurcharge");
        SORTFIELDS.put("postbankRiskclass", "postbankRiskclass");
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final String providerPreference = cmd.getProviderPreference();
        final Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields
                = getFields(providerPreference, InstrumentTypeEnum.FND, PermissionType.FUNDDATA);

        final RatioSearchRequest rsr = createRequest(InstrumentTypeEnum.FND, null,
                cmd.getDataRecordStrategy(),
                fields, true, cmd.getSearchstring(), providerPreference);

        final ListResult listResult;

        if (cmd.getQuery() != null) {
            rsr.addParameters(parseQuery(cmd.getQuery(), fields));

            final List<String> sortfields = asSortfields(fields);
            listResult = ListResult.create(cmd, sortfields, "name", 0);
        }
        else {
            addParametersOldStyle(cmd, rsr);
            listResult = ListResult.create(cmd, new ArrayList<>(SORTFIELDS.keySet()), "name", 0);
        }

        addSorts(rsr, listResult, fields);

        rsr.addParameter("i", Integer.toString(cmd.getOffset()));
        rsr.addParameter("n", Integer.toString(cmd.getAnzahl()));

        if (cmd.getRawparam() != null) {
            for (String s : cmd.getRawparam()) {
                final String[] strings = s.split("\\|");
                rsr.addParameter(strings[0], strings[1]);
            }
        }

        final RatioSearchResponse sr = this.ratiosProvider.search(rsr);

        if (!sr.isValid()) {
            errors.reject("ratios.searchfailed", "invalid search response");
            return null;
        }

        final Map<String, Object> model = createResultModel(sr, listResult, fields, false, false);

        //noinspection unchecked
        final List<Quote> quotes = (List<Quote>) model.get("quotes");
        final List<PriceRecord> priceRecords = this.intradayProvider.getPriceRecords(quotes);
        model.put("priceRecords", priceRecords);

        final List<Boolean> isFundotc = new ArrayList<>(quotes.size());
        for (final Quote quote : quotes) {
            isFundotc.add(quote != null && InstrumentUtil.isVwdFund(quote));
        }

        model.put("isFundotc", isFundotc);

        return new ModelAndView("fndfindersuchergebnis", model);
    }

    private void addParametersOldStyle(Command cmd, RatioSearchRequest rsr) {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();

        final Collection<String> permissions = profile.getPermission(PermissionType.FUNDDATA);

        addParameter(rsr, "fwwfundtype", cmd.getFondstyp());
        addParameter(rsr,  "fwwsector", cmd.getSektor());

        if (cmd.getKag() != null && cmd.getKag().length > 0) {
            final StringBuilder stb = new StringBuilder();
            boolean first = true;
            for (int i = 0; i < Math.min(MAX_NUM_KAGS, cmd.getKag().length); i++) {
                final String s = cmd.getKag()[i];

                if (!StringUtils.hasText(s)) {
                    continue;
                }
                if (first) {
                    first = false;
                }
                else {
                    stb.append("@");
                }
                stb.append(s);
            }
            rsr.addParameter("fwwkag", stb.toString());
        }

        addParameter(rsr, "retaining", cmd.getAusschuettungsart());
        addParameter(rsr, "currency", cmd.getWaehrung());
        addParameter(rsr, "volatility1y:R", cmd.getVolatilitaet());
        addParameter(rsr, "bviperformance1y:R", cmd.getPerformance1y());
        addParameter(rsr, "bviperformance3y:R", cmd.getPerformance3y());
        addParameter(rsr, "bviperformance5y:R", cmd.getPerformance5y());
        addParameter(rsr, "sharperatio1y:R", cmd.getSharperatio());
        addParameter(rsr, "managementfee:R", cmd.getVerwaltungsgebuehr());
        addParameter(rsr, "accountFee:R", cmd.getDepotbankverguetung());
        addParameter(rsr, "surcharge:R", cmd.getAusgabeaufschlag());
        addParameter(rsr, "fwwter:R", cmd.getTer());
        addParameter(rsr, "isetf", cmd.getEtf());
        addParameter(rsr, "issuedate", cmd.getIssueDate());
        addParameter(rsr, "msMarketAdmission", cmd.getMarketAdmission());
    }

    public static void main(String[] args) {
        final Profile p = ProfileFactory.createInstance(ResourcePermissionProvider.getInstance("mm-xml"));
        RequestContextHolder.setRequestContext(new RequestContext(p, LbbwMarketStrategy.INSTANCE));
        System.out.println(parseQuery("issuername == Deka && issuesurcharge <= 5.25",
                getFields(InstrumentTypeEnum.FND, PermissionType.FUNDDATA)));
    }
}
