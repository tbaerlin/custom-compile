/*
 * StkUnternehmensportrait.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import static de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.REQUEST_ATTRIBUTE_NAME;

import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.data.DownloadableItem;
import de.marketmaker.istar.domain.data.DownloadableItem.Type;
import de.marketmaker.istar.domain.data.RegulatoryReportingRecord;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.ProfileUtil;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.data.DownloadableItemImpl;
import de.marketmaker.istar.instrument.search.SearchRequestResultType;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.RegulatoryReportingProvider;
import de.marketmaker.istar.merger.provider.RegulatoryReportingProvider.EuwaxDates;
import de.marketmaker.istar.merger.provider.RegulatoryReportingRequest;
import de.marketmaker.istar.merger.provider.gis.GisDocumentType;
import de.marketmaker.istar.merger.provider.gis.GisProductType;
import de.marketmaker.istar.merger.provider.report.ReportConstraint;
import de.marketmaker.istar.merger.provider.report.ReportSelectionStrategy;
import de.marketmaker.istar.merger.provider.report.ReportServiceDelegate;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;
import de.marketmaker.istar.merger.web.easytrade.misc.DynamicPibController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Delivers reports for a specific instrument.
 * <p>
 * There are currently four major providers providing instrument report:
 * <ul>
 * <li>Software Systems AT</li>
 * <li>FWW</li>
 * <li>Stock Selection</li>
 * <li>DZ Bank</li>
 * <li>Fund Info</li>
 * </ul>
 * The reports returned by this service depend on the providers that are configured for the current client.
 * If multiple providers are configured for a client, the delivered reports will depend on the configured
 * report selection strategy.
 * If a preferred report source is set, only reports from that source will be delivered.
 * </p>
 * <p>
 * Reports are categorized in types. If multiple reports exist for a given type, and only a single
 * report is requested, the newer or the preferred report is returned.
 * The preference is decided by using the configured report selection strategy.
 * </p>
 * <p>
 * Reports can further be filtered by language and country preference. If no language preference is set
 * explicitly, the language setting of the requesting client's context will be used.
 * </p>
 * <p>
 * The margin parameter is used for certain report types that are customized and created on demand in the backend.
 * The value of the Parameter is in percent.
 * </p>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscReports extends EasytradeCommandController {

    private static final Logger log = LoggerFactory.getLogger(MscReports.class);

    private static final List<Type> GENO_TYPES = Arrays.asList(Type.PIB, Type.PIF, Type.BIB);

    public static class Command extends DefaultSymbolCommand {
        private String language;

        private String country;

        private String filterStrategy;

        private String margin;

        private DateTime date;

        private String type;

        private boolean isDzHausmeinung;

        private boolean isDzOfferte;

        private boolean isDzActiveProduct;

        private String dzProductType;

        /**
         * @return 2 character language symbol according to ISO 639-1 (e.g. de, en).
         */
        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        /**
         * @return 2 character language symbol according to ISO 3166-1 (e.g. DE, EN).
         */
        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        /**
         * @return apply special filter strategy.
         */
        @RestrictedSet("DZBANK-PIB,UNION-PIF")
        public String getFilterStrategy() {
            return filterStrategy;
        }

        public void setFilterStrategy(String filterStrategy) {
            this.filterStrategy = filterStrategy;
        }

        /**
         * @return the user provided margin in percent, or "default" if the backend provided default margin should be used
         */
        public String getMargin() {
            return margin;
        }

        public void setMargin(String margin) {
            this.margin = margin;
        }

        /**
         * @return earliest date of requested reports according to ISO 8601 (e.g. 2004-07-11).
         */
        public DateTime getDate() {
            return date;
        }

        public void setDate(DateTime date) {
            this.date = date;
        }

        /**
         * @return type of requested reports
         * <p>
         * one of:
         * <ul>
         * <li>{@link Type#Monthly Monthly}</li>
         * <li>{@link Type#SemiAnnual SemiAnnual}</li>
         * <li>{@link Type#Annual Annual}</li>
         * <li>{@link Type#Accounts Accounts}</li>
         * <li>{@link Type#Prospectus Prospectus}</li>
         * <li>{@link Type#ProspectusSimplified ProspectusSimplified}</li>
         * <li>{@link Type#ProspectusUnfinished ProspectusUnfinished}</li>
         * <li>{@link Type#FactSheet FactSheet}</li>
         * <li>{@link Type#TermSheet TermSheet}</li>
         * <li>{@link Type#SpectSheet SpectSheet}</li>
         * <li>{@link Type#Addendum Addendum}</li>
         * <li>{@link Type#KIID KIID}</li>
         * <li>{@link Type#PIB PIB}</li>
         * <li>{@link Type#PIF PIF}</li>
         * <li>{@link Type#BIB BIB}</li>
         * <li>{@link Type#Unknown Unknown}</li>
         * </ul>
         * </p>
         */
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        /**
         * <p>
         * # <b>special parameter</b> (mainly internal use) for geno-x reports (PIB, PIF, BIB) </br>
         * # only relevant with Permission-Selector ZertificatePIB 2268
         * </p>
         * <p>
         * The product type (geno-x) of the request instrument </br>
         * This is not a filter parameter, but an input parameter for the requested instrument.
         * </p>
         */
        @RestrictedSet("Zinsprodukt,Zeichnungsprodukt,Flowprodukt,Sonstige")
        public String getDzProductType() {
            return dzProductType;
        }

        public void setDzProductType(String dzProductType) {
            this.dzProductType = dzProductType;
        }

        /**
         * <p>
         * # <b>special parameter</b> (mainly internal use) for geno-x reports (PIB, PIF, BIB) </br>
         * # only relevant with Permission-Selector ZertificatePIB 2268
         * </p>
         * <p>
         * Whether the request instrument belongs to the DZ-Hausmeinung </br>
         * This is not a filter parameter, but an input parameter for the requested instrument.
         * </p>
         */
        public boolean isDzHausmeinung() {
            return isDzHausmeinung;
        }

        public void setDzHausmeinung(boolean isDzHausmeinung) {
            this.isDzHausmeinung = isDzHausmeinung;
        }

        /**
         * <p>
         * # <b>special parameter</b> (mainly internal use) for geno-x reports (PIB, PIF, BIB) </br>
         * # only relevant with Permission-Selector ZertificatePIB 2268
         * </p>
         * <p>
         * Whether the request instrument belongs to the DZ-Offerte</br>
         * This is not a filter parameter, but an input parameter for the requested instrument.
         * </p>
         */
        public boolean isDzOfferte() {
            return isDzOfferte;
        }

        public void setDzOfferte(boolean isDzBankHandelsofferte) {
            this.isDzOfferte = isDzBankHandelsofferte;
        }
        /**
         * <p>
         * # <b>special parameter</b> (mainly internal use) for geno-x reports (PIB, PIF, BIB) </br>
         * # only relevant with Permission-Selector ZertificatePIB 2268
         * </p>
         * <p>
         * Whether the request instrument is an active DZ-Product </br>
         * This is not a filter parameter, but an input parameter for the requested instrument.
         * </p>
         */
        public boolean isDzActiveProduct() {
            return isDzActiveProduct;
        }

        public void setDzBankActiveProduct(boolean dzActiveProduct) {
            isDzActiveProduct = dzActiveProduct;
        }

    }

    private RegulatoryReportingProvider regulatoryReportingProvider;

    public void setRegulatoryReportingProvider(RegulatoryReportingProvider regulatoryReportingProvider) {
        this.regulatoryReportingProvider = regulatoryReportingProvider;
    }

    private ReportServiceDelegate reportServiceDelegate;

    private EasytradeInstrumentProvider instrumentProvider;

    private String template = "fndreports";

    private boolean uniqueTypes = false;

    private Selector mandatorySelector;

    private ReportSelectionStrategy reportSelectionStrategy;

    private boolean newlyFiltered = false;

    public MscReports() {
        super(Command.class);
    }

    public void setPreferredSource(String src) {
        this.reportSelectionStrategy =
                new ReportSelectionStrategy.IncludeThis(DownloadableItem.Source.valueOf(src));
    }

    public void setReportServiceDelegate(ReportServiceDelegate reportServiceDelegate) {
        this.reportServiceDelegate = reportServiceDelegate;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setUniqueTypes(boolean uniqueTypes) {
        this.uniqueTypes = uniqueTypes;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setMandatorySelector(int mandatorySelector) {
        this.mandatorySelector = Selector.valueOf(mandatorySelector);
    }

    public void setNewlyFiltered(boolean newlyFiltered) {
        this.newlyFiltered = newlyFiltered;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;
        return getModelAndView(cmd, (MoleculeRequest) request.getAttribute(REQUEST_ATTRIBUTE_NAME));
    }

    private ModelAndView getModelAndView(Command cmd, MoleculeRequest mr) {
        if (this.mandatorySelector != null) {
            checkPermission(this.mandatorySelector);
        }

        final RequestContext oldRC = RequestContextHolder.getRequestContext();

        final Instrument instrument;
        try {
            RequestContextHolder.setRequestContext(oldRC.withSearchRequestResultType(SearchRequestResultType.QUOTE_ANY));
            instrument = this.instrumentProvider.identifyInstrument(cmd.getSymbol(), cmd.getSymbolStrategy());
        } finally {
            RequestContextHolder.setRequestContext(oldRC);
        }

        final Map<String, Object> model = new HashMap<>();
        model.put("instrument", instrument);

        if (instrument != null) {
            final Quote quote = getQuote(cmd, instrument);

            List<DownloadableItem> items = this.reportServiceDelegate.getReports(
                    instrument.getId(), instrument.getInstrumentType(), createConstraint(cmd));

            if (hasPermission(Selector.PRODUCT_WITH_PIB)) {
                items = addPIBAdaptorDownloadableItems(cmd, mr, instrument, items);
            }

            model.put("quote", quote);
            model.put("items", items);
        }

        return new ModelAndView(this.template, model);
    }


    private static final EnumSet<GisDocumentType> BIB_DOCTYPE_SET = EnumSet.of(GisDocumentType.BIB);
    private static final EnumSet<GisDocumentType> PIB_DOCTYPE_SET = EnumSet.of(GisDocumentType.PIB);
    private static final EnumSet<GisDocumentType> PIF_BIB_DOCTYPE_SET =
        EnumSet.of(GisDocumentType.PIF, GisDocumentType.BIB);
    private static final EnumSet<GisDocumentType> EMPTY_DOCTYPE_SET = EnumSet.noneOf(GisDocumentType.class);

    private static boolean isPriip(String adf2923) {
        if (adf2923 == null) {
            return false;
        }
        //GW2 PRIIPS Values
        //E=in Prüfung, Einzelfall;in Prüfung, Einzelfall;Prüf.Einz.;N;;being reviewed
        //– individual case;be. Reviewed- ind. Case;re. Ind.c.

        //J=Ja;Ja;Ja;N;;Yes;Yes;Yes
        //N=Nein;Nein;Nein;N;;No;No;No
        //P=in Prüfung;in Prüfung;in Prüfung;N;;Being examined;Being examined;Being ex.

        //U=UCITs Fonds, bzw. Fonds künftig unter die PRIIPS-VO fallen;UCITS Fond;UCITS Fond;N;
        //;UCITs funds or funds that will be subject to the PRIIPs regulation in the future
        //;UCITS Funds;UCITS Fund

        //X=nicht relevant;nicht relevant;n. relev.;N;;not relevant;not relevant;n. relev.
        switch (adf2923) {
            case "J":
            case "U":
                return true;
            default:
                return false;
        }
    }

    private List<DownloadableItem> addPIBAdaptorDownloadableItems(Command cmd, MoleculeRequest mr,
        Instrument instrument, List<DownloadableItem> items) {
        //PRIIP
        final RegulatoryReportingRecord priceRegulatoryReportingRecord =
            this.regulatoryReportingProvider.getPriceRegulatoryReportingRecord(
                new RegulatoryReportingRequest(instrument.getId()));

        final boolean isPriip = isPriip(priceRegulatoryReportingRecord.getPriipsID());
        final boolean isDzOfferte = cmd.isDzOfferte;
        final boolean isDzHausmeinung = cmd.isDzHausmeinung;
        final boolean isDzActiveProduct = cmd.isDzActiveProduct;
        final String credentials = credentials(mr);
        final String margin = StringUtils.isEmpty(cmd.getMargin()) ? "default" : cmd.getMargin();
        final GisProductType productType = GisProductType.resolve(cmd.dzProductType);

        final List<DownloadableItem> reports = new ArrayList<>();

        final EnumSet<GisDocumentType> expectedDocumentTypes;
        if (isPriip && (isDzOfferte || isDzHausmeinung)) {
            expectedDocumentTypes = PIF_BIB_DOCTYPE_SET;
        } else if (isDzOfferte || isDzHausmeinung) {
            expectedDocumentTypes = PIB_DOCTYPE_SET;
        } else if (isPriip && isDzActiveProduct) {
            expectedDocumentTypes = BIB_DOCTYPE_SET;
        } else {
            expectedDocumentTypes = EMPTY_DOCTYPE_SET;
        }

        final Optional<Boolean> isInQuietPhase = isInQuietPhase(expectedDocumentTypes,
            () -> this.regulatoryReportingProvider.getEuwaxDates(instrument.getSymbolIsin()));

        for (GisDocumentType expectedDocumentType : expectedDocumentTypes) {
            final DynamicPibController.PibAdaptorDownloadLink link =
                new DynamicPibController.PibAdaptorDownloadLink();
            link.setMargin(margin);
            link.setIsin(instrument.getSymbolIsin());
            link.setWkn(instrument.getSymbolWkn());
            link.setEncodedCredentials(credentials);
            link.setProductType(productType);
            link.setDocumentType(expectedDocumentType);

            DateTime now = DateTime.now();
            final DownloadableItemImpl downloadableItem = new DownloadableItemImpl(
                now.getYear(),
                expectedDocumentType.getDownloadableItemType(),
                expectedDocumentType.getDescription(),
                link.asString(),
                now,
                null, null, null, null, instrument.getInstrumentType(), null);
            if (!PIF_BIB_DOCTYPE_SET.contains(expectedDocumentType)) {
                // not reg doc
                reports.add(downloadableItem);
            } else {
                if (isInQuietPhase.orElse(Boolean.FALSE)) { // if cannot determine, deliver
                    if (log.isDebugEnabled()) {
                        log.debug("<addPIBAdaptorDownloadableItems> no {} during quiet phase {}",
                            expectedDocumentType, instrument.getSymbolIsin());
                    }
                } else {
                    reports.add(downloadableItem);
                }
            }
        }

        final EnumSet<Type> itemTypes = expectedDocumentTypes.stream()
            .map(GisDocumentType::getDownloadableItemType)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(Type.class)));

        for (DownloadableItem item : items) {
            if (!itemTypes.contains(item.getType())) {
                reports.add(item);
            }
        }

        return reports;
    }

    private static Optional<Boolean> isInQuietPhase(EnumSet<GisDocumentType> gisDocTypes,
        Supplier<Optional<EuwaxDates>> supplier) {
        if (gisDocTypes.stream().anyMatch(PIF_BIB_DOCTYPE_SET::contains)) {
            final LocalDateTime now = LocalDateTime.now();
            return supplier.get()
                .filter(m -> m.getFirstTradingDay() != null && m.getSubscriptionEnd() != null)
                .map(m -> now.isAfter(m.getSubscriptionEnd())
                    && now.toLocalDate().isBefore(m.getFirstTradingDay()));
        }
        return Optional.empty();
    }

    private boolean hasPermission(final Selector selector) {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        return profile.isAllowed(selector);
    }

    private String credentials(MoleculeRequest mr) {
        return mr != null
            ? ProfileUtil.encodeCredential(mr.getAuthentication(), mr.getAuthenticationType())
            : null;
    }

    public Reports getReports(String symbol) {
        return getReports(symbol, null, null, null);
    }

    public Reports getReports(String symbol, String docType, String language, String country) {
        final Command cmd = new Command();
        cmd.setSymbol(symbol);
        cmd.setLanguage(language);
        cmd.setCountry(country);
        cmd.setType(docType);
        final Map<String, Object> model = getModelAndView(cmd, null).getModel();
        //noinspection unchecked
        return new Reports((Instrument) model.get("instrument"),
                (List<DownloadableItem>) model.get("items"));
    }

    public static final class Reports {
        private final Instrument instrument;

        private final List<DownloadableItem> reports;

        private Reports(Instrument instrument,
                List<DownloadableItem> reports) {
            this.instrument = instrument;
            this.reports = reports;
        }

        public Instrument getInstrument() {
            return instrument;
        }

        public List<DownloadableItem> getReports() {
            return reports;
        }
    }

    private ReportConstraint createConstraint(Command cmd) {
        final ReportConstraint constraint = new ReportConstraint(getLanguage(cmd), cmd.getCountry(),
                cmd.getFilterStrategy(), this.uniqueTypes, cmd.getDate(), cmd.getType());
        constraint.setSelectionStrategy(this.reportSelectionStrategy);
        constraint.setNewlyFiltered(this.newlyFiltered);
        return constraint;
    }

    private String getLanguage(Command cmd) {
        return null == cmd.getLanguage() ?
                RequestContextHolder.getRequestContext().getLocale().getLanguage() :
                cmd.getLanguage();
    }

    private Quote getQuote(Command cmd, Instrument instrument) {
        try {
            final Quote quote = this.instrumentProvider.getQuote(instrument, cmd.getMarket(), cmd.getMarketStrategy());
            if (null != quote) {
                return quote;
            }
        } catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getQuote> failed getting quote for " + cmd.getSymbol());
            }
        }

        if (instrument.getQuotes().isEmpty()) {
            return null;
        }

        return instrument.getQuotes().get(0);
    }
}
