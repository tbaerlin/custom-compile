/*
 * FundDataProviderImpl.java
 *
 * Created on 11.08.2006 18:34:41
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.stockdata;

import java.io.File;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.marketmaker.istar.domain.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.InitializingBean;
import org.xml.sax.SAXException;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.monitor.Resource;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.xml.AbstractSaxReader;
import de.marketmaker.istar.domain.data.AnnualReportData;
import de.marketmaker.istar.domain.data.CompanyProfile;
import de.marketmaker.istar.domainimpl.data.AnnualReportAssetsImpl;
import de.marketmaker.istar.domainimpl.data.AnnualReportBalanceSheetImpl;
import de.marketmaker.istar.domainimpl.data.AnnualReportDataImpl;
import de.marketmaker.istar.domainimpl.data.AnnualReportKeyFiguresImpl;
import de.marketmaker.istar.domainimpl.data.AnnualReportLiabilitiesImpl;
import de.marketmaker.istar.domainimpl.data.CompanyProfileImpl;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CompanyDataProviderVwdBeNl implements CompanyDataProvider, InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final static Locale[] LOCALES = new Locale[]{new Locale("nl"), Locale.FRENCH, Locale.ENGLISH};

    private final Map<String, Map<Long, CompanyProfile>> companyData = new HashMap<>();

    private final Map<Long, List<AnnualReportData>> annualReport = new HashMap<>();

    {
        Arrays.stream(LOCALES).forEach(locale -> this.companyData.put(locale.getLanguage(), new HashMap<>()));
    }

    private ActiveMonitor activeMonitor;

    private File companyDataFile;

    private File companyPersonsFile;

    private File annualReportFile;

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setCompanyDataFile(File companyDataFile) {
        this.companyDataFile = companyDataFile;
    }

    public void setCompanyPersonsFile(File companyPersonsFile) {
        this.companyPersonsFile = companyPersonsFile;
    }

    public void setAnnualReportFile(File annualReportFile) {
        this.annualReportFile = annualReportFile;
    }

    public void afterPropertiesSet() throws Exception {
        final FileResource companyDataResource = new FileResource(this.companyDataFile);
        companyDataResource.addPropertyChangeListener(evt -> readCompanyData());

        final FileResource companyPersonsResource = new FileResource(this.companyPersonsFile);
        companyPersonsResource.addPropertyChangeListener(evt -> readCompanyData());

        final FileResource annualReportResource = new FileResource(this.annualReportFile);
        annualReportResource.addPropertyChangeListener(evt -> readAnnualReport());

        this.activeMonitor.addResources(new Resource[]{companyDataResource, companyPersonsResource, annualReportResource});

        readCompanyData();
        readAnnualReport();
    }

    private void readAnnualReport() {
        try {
            final AnnualReportReader ur = new AnnualReportReader();
            ur.read(this.annualReportFile);

            synchronized (this) {
                this.annualReport.clear();
                this.annualReport.putAll(ur.getValues());
            }

            this.logger.info("<readAnnualReport> read annual report data");
        }
        catch (Exception e) {
            this.logger.error("<readAnnualReport> failed", e);
        }
    }

    private void readCompanyData() {
        try {
            final CompanyPersonsReader pr = new CompanyPersonsReader();
            pr.read(this.companyPersonsFile);

            final CompanyDataReader ur = new CompanyDataReader(pr.getValues());
            ur.read(this.companyDataFile);

            synchronized (this) {
                this.companyData.clear();
                this.companyData.put(LOCALES[0].getLanguage(), ur.getValues_NL());
                this.companyData.put(LOCALES[1].getLanguage(), ur.getValues_FR());
                this.companyData.put(LOCALES[2].getLanguage(), ur.getValues_EN());
            }

            this.logger.info("<readCompanyData> read company data");
        }
        catch (Exception e) {
            this.logger.error("<readCompanyData> failed", e);
        }
    }

    public synchronized List<AnnualReportData> getAnnualReportData(CompanyDataRequest request) {
        return this.annualReport.get(request.getInstrumentId());
    }

    public synchronized CompanyProfile getCompanyProfile(CompanyDataRequest request) {
        final Map<Long, CompanyProfile> map = getCompanyProfileMap(request.getLocales());
        final CompanyProfile data = map.get(request.getInstrumentId());
        return data != null ? data : CompanyProfile.NULL_INSTANCE;
    }

    private Map<Long, CompanyProfile> getCompanyProfileMap(List<Locale> locales) {
        if (locales != null) {
            for (final Locale locale : locales) {
                final Map<Long, CompanyProfile> map = this.companyData.get(locale.getLanguage());
                if (map != null && !map.isEmpty()) {
                    return map;
                }
            }
        }
        return this.companyData.get(LOCALES[0].getLanguage());
    }

    public static class AnnualReportReader extends AbstractSaxReader {
        private static final Map<String, String> TAG_MAPPINGS = new HashMap<>();

        private static final Map<String, Class> CLASSES = new HashMap<>();

        private final Matcher matcher = Pattern.compile("([^_]*)_(.*)").matcher("");

        static {
            CLASSES.put("A", AnnualReportAssetsImpl.Builder.class);
            CLASSES.put("L", AnnualReportLiabilitiesImpl.Builder.class);
            CLASSES.put("B", AnnualReportBalanceSheetImpl.Builder.class);
            CLASSES.put("F", AnnualReportKeyFiguresImpl.Builder.class);

            TAG_MAPPINGS.put("A_DEBANDOTHERFIXEDINCOMES", "debenturesAndOtherFixedIncomes");
            TAG_MAPPINGS.put("L_TECPROVWINVESTMNOTFOROWNRISK", "technicalProvisionsWithInvestmentsNotForOwnRisk");
            TAG_MAPPINGS.put("L_DEPRECEIVEDFROMREINSURERS", "depositsReceivedFromReinsurers");
            TAG_MAPPINGS.put("B_INCOMEFROMSTOCKSANDPART", "incomeFromStocksAndParticipations");
            TAG_MAPPINGS.put("B_GENERALADMINEXPENSES", "generalAdministrativeExpenses");
            TAG_MAPPINGS.put("B_DEPRECIATIONSANDAMORT", "depreciationsAndAmortizations");
            TAG_MAPPINGS.put("B_OTHERREVENUESANDEXPBANKING", "otherRevenuesAndExpensesBanking");
            TAG_MAPPINGS.put("B_INVESTACTNOTINCLINTECRESULT", "investmentActivitiesNotIncludedInTechnicalResult");
            TAG_MAPPINGS.put("B_TOTALCURRESULTBEFORETAXES", "totalCurrentResultBeforeTaxes");
            TAG_MAPPINGS.put("B_CONSINCOMEFROMPRIVEQUPARTS", "consolidatedIncomeFromPrivateEquityParticipations");
            TAG_MAPPINGS.put("F_CURRESULTBANKBEFORETAXES", "currentResultBankingBeforeTaxes");
            TAG_MAPPINGS.put("F_CURRESULTINSBEFORETAXES", "currentResultInsuranceBeforeTaxes");
            TAG_MAPPINGS.put("F_INTERESTTOGROSSBANKRR", "interestToGrossBankResultRatio");
            TAG_MAPPINGS.put("F_PROVISIONSTOGROSSBANKRR", "provisionsToGrossBankResultRatio");
            TAG_MAPPINGS.put("F_NETTRADINGINCTOGROSSBANKRR", "netTradingIncomeToGrossBankResultRatio");
            TAG_MAPPINGS.put("F_RESULTBANKINGTOGROSSBANKRR", "resultBankingToGrossBankResultRatio");
            TAG_MAPPINGS.put("F_RESINSTOTOTRESBEFTAXESR", "resultInsuranceToTotalResultBeforeTaxesRatio");
            TAG_MAPPINGS.put("F_TECRESTOPREMIUMINCOMERATIO", "technicalResultToPremiumIncomeRatio");
            TAG_MAPPINGS.put("F_NETINCOMETOPREMIUMINCOMER", "netIncomeToPremiumIncomeRatio");
            TAG_MAPPINGS.put("F_LIABLEASSETSTOTOTBALSHEETR", "liableAssetsToTotalBalanceSheetRatio");
            TAG_MAPPINGS.put("F_SHAREHEQUITYTOPREMIUMINCR", "shareholdersEquityToPremiumIncomeRatio");
            TAG_MAPPINGS.put("F_RESBEFTAXESTOEMPCOSTSINTHS", "resultBeforeTaxesToEmployeeCostsInThousands");
        }

        private final Map<Long, List<AnnualReportData>> values = new HashMap<>();

        private final Map<String, Object> builders = new HashMap<>();

        private final Map<String, Method> methods = new HashMap<>();

        private long iid = -1;

        private int year = -1;

        public void endElement(String uri, String localName, String tagName) throws SAXException {
            try {
                if (tagName.equals("ROW")) {
                    // i have finished a new row => process
                    storeFields();
                }
                else if (tagName.equals("IID")) {
                    this.iid = getCurrentLong();
                }
                else if (tagName.equals("INSTRUMENTID")) {
                    this.iid = getCurrentLong();
                }
                else if (tagName.equals("YEAR")) {
                    this.year = getCurrentInt();
                }
                else if (tagName.equals("ROWS")) {
                    // ignored
                }
                else if (matcher.reset(tagName).matches()) {
                    final String type = this.matcher.group(1);
                    final String fieldname = getFieldname(tagName);

                    final Object builder = getBuilder(type);

                    final Method setter = getSetter(builder, tagName, fieldname);

                    if (setter == null) {
                        this.logger.error("<endElement> unknown field: " + tagName);
                        return;
                    }

                    if (setter.getParameterTypes()[0].equals(BigDecimal.class)) {
                        setter.invoke(builder, getCurrentBigDecimal());
                    }
                    else if (setter.getParameterTypes()[0].equals(boolean.class)) {
                        setter.invoke(builder, getCurrentBoolean());
                    }
                    else {
                        this.logger.error("<endElement> unknown type: " + setter.getParameterTypes()[0]);
                    }
                }
            }
            catch (Exception e) {
                this.logger.error("<endElement> error in " + tagName, e);
                this.errorOccured = true;
            }
        }

        private Method getSetter(Object builder, String tagname, String fieldname) {
            final Method method = this.methods.get(tagname);
            if (method != null) {
                return method;
            }

            for (final Method m : builder.getClass().getMethods()) {
                if (m.getName().equalsIgnoreCase("set" + fieldname)) {
                    this.methods.put(tagname, m);
                    return m;
                }
            }

            return null;
        }

        private Object getBuilder(String type) {
            final Object o = this.builders.get(type);

            if (o != null) {
                return o;
            }

            final Class clazz = CLASSES.get(type);
            try {
                final Object b = clazz.newInstance();
                this.builders.put(type, b);
                return b;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private String getFieldname(String tagName) {
            final String mappedName = TAG_MAPPINGS.get(tagName);
            return mappedName != null ? mappedName : this.matcher.group(2);
        }

        private void storeFields() {
            this.limiter.ackAction();

            if (this.errorOccured || this.iid < 0 || this.year < 0 || this.builders.isEmpty()) {
                reset();
                return;
            }

            List<AnnualReportData> items = this.values.get(this.iid);
            if (items == null) {
                items = new ArrayList<>();
                this.values.put(this.iid, items);
            }

            final Interval reference = new Interval(new LocalDate(year, 1, 1).toDateTimeAtStartOfDay(),
                    new LocalDate(year + 1, 1, 1).toDateTimeAtStartOfDay().minusSeconds(1));

            final AnnualReportAssetsImpl.Builder a = (AnnualReportAssetsImpl.Builder) this.builders.get("A");
            final AnnualReportLiabilitiesImpl.Builder l = (AnnualReportLiabilitiesImpl.Builder) this.builders.get("L");
            final AnnualReportBalanceSheetImpl.Builder b = (AnnualReportBalanceSheetImpl.Builder) this.builders.get("B");
            final AnnualReportKeyFiguresImpl.Builder f = (AnnualReportKeyFiguresImpl.Builder) this.builders.get("F");

            final AnnualReportDataImpl ard = new AnnualReportDataImpl(this.iid, reference,
                    a != null ? a.build() : null,
                    l != null ? l.build() : null,
                    b != null ? b.build() : null,
                    f != null ? f.build() : null);
            items.add(ard);

            reset();
        }

        protected void reset() {
            this.builders.clear();
            this.iid = -1;
            this.year = -1;
            this.errorOccured = false;
        }

        public Map<Long, List<AnnualReportData>> getValues() {
            return values;
        }
    }

    public static class CompanyDataReader extends AbstractSaxReader {
        private final Map<Long, CompanyProfile> values_nl = new HashMap<>();

        private final Map<Long, CompanyProfile> values_fr = new HashMap<>();

        private final Map<Long, CompanyProfile> values_en = new HashMap<>();

        private CompanyProfileImpl.Builder builder_nl = new CompanyProfileImpl.Builder();

        private CompanyProfileImpl.Builder builder_fr = new CompanyProfileImpl.Builder();

        private CompanyProfileImpl.Builder builder_en = new CompanyProfileImpl.Builder();

        private final Map<Long, List<CompanyProfile.BoardMember>> boardMembers;

        public CompanyDataReader(Map<Long, List<CompanyProfile.BoardMember>> boardMembers) {
            this.boardMembers = boardMembers;
        }

        public void endElement(String uri, String localName, String tagName) throws SAXException {
            try {
                if (tagName.equals("ROW")) {
                    // i have finished a new row => process
                    storeFields();
                }
                else if (tagName.equals("IID")) {
                    this.builder_nl.setInstrumentid(getCurrentLong());
                    this.builder_fr.setInstrumentid(getCurrentLong());
                    this.builder_en.setInstrumentid(getCurrentLong());
                }
                else if (tagName.equals("NAME_NL")) {
                    this.builder_nl.setName(getCurrentString());
                }
                else if (tagName.equals("NAME_FR")) {
                    this.builder_fr.setName(getCurrentString());
                }
                else if (tagName.equals("NAME_EN")) {
                    this.builder_en.setName(getCurrentString());
                }
                else if (tagName.equals("ADDRESS_NL")) {
                    this.builder_nl.setStreet(getCurrentString());
                }
                else if (tagName.equals("ADDRESS_FR")) {
                    this.builder_fr.setStreet(getCurrentString());
                }
                else if (tagName.equals("ADDRESS_EN")) {
                    this.builder_en.setStreet(getCurrentString());
                }
                else if (tagName.equals("CITY_NL")) {
                    this.builder_nl.setCity(getCurrentString());
                }
                else if (tagName.equals("CITY_FR")) {
                    this.builder_fr.setCity(getCurrentString());
                }
                else if (tagName.equals("CITY_EN")) {
                    this.builder_en.setCity(getCurrentString());
                }
                else if (tagName.equals("POSTALCODE")) {
                    this.builder_nl.setPostalcode(getCurrentString());
                    this.builder_fr.setPostalcode(getCurrentString());
                    this.builder_en.setPostalcode(getCurrentString());
                }
                else if (tagName.equals("COUNTRY")) {
                    this.builder_nl.setCountry(getCurrentString());
                    this.builder_fr.setCountry(getCurrentString());
                    this.builder_en.setCountry(getCurrentString());
                }
                else if (tagName.equals("PHONE")) {
                    this.builder_nl.setTelephone(getCurrentString());
                    this.builder_fr.setTelephone(getCurrentString());
                    this.builder_en.setTelephone(getCurrentString());
                }
                else if (tagName.equals("FAX")) {
                    this.builder_nl.setFax(getCurrentString());
                    this.builder_fr.setFax(getCurrentString());
                    this.builder_en.setFax(getCurrentString());
                }
                else if (tagName.equals("URL")) {
                    this.builder_nl.setUrl(getCurrentString());
                    this.builder_fr.setUrl(getCurrentString());
                    this.builder_en.setUrl(getCurrentString());
                }
                else if (tagName.equals("EMAIL")) {
                    this.builder_nl.setEmail(getCurrentString());
                    this.builder_fr.setEmail(getCurrentString());
                    this.builder_en.setEmail(getCurrentString());
                }
                else if (tagName.equals("ANNUALREPORTFACTOR")) {
                    this.builder_nl.setAnnualReportFactor(getCurrentBigDecimal());
                    this.builder_fr.setAnnualReportFactor(getCurrentBigDecimal());
                    this.builder_en.setAnnualReportFactor(getCurrentBigDecimal());
                }
                else if (tagName.equals("ANNUALREPORTCURRENCY")) {
                    this.builder_nl.setAnnualReportCurrency(getCurrentString());
                    this.builder_fr.setAnnualReportCurrency(getCurrentString());
                    this.builder_en.setAnnualReportCurrency(getCurrentString());
                }
                else if (tagName.equals("ANNUALREPORTTYPE")) {
                    this.builder_nl.setAnnualReportType(getCurrentString());
                    this.builder_fr.setAnnualReportType(getCurrentString());
                    this.builder_en.setAnnualReportType(getCurrentString());
                }
                else if (tagName.equals("PORTRAIT_NL")) {
                    this.builder_nl.setPortrait(getCurrentString(), Language.nl);
                }
                else if (tagName.equals("PORTRAIT_FR")) {
                    this.builder_fr.setPortrait(getCurrentString(), Language.fr);
                }
                else if (tagName.equals("PORTRAIT_EN")) {
                    this.builder_en.setPortrait(getCurrentString(), Language.en);
                }
            }
            catch (Exception e) {
                this.logger.error("<endElement> error in " + tagName, e);
                this.errorOccured = true;
            }
        }

        private void storeFields() {
            this.limiter.ackAction();

            if (this.errorOccured) {
                reset();
                return;
            }

            if (this.builder_nl.isValid()) {
                addBoardMembers(this.builder_nl);
                final CompanyProfile data_nl = this.builder_nl.build();
                this.values_nl.put(data_nl.getInstrumentid(), data_nl);
            }

            if (this.builder_fr.isValid()) {
                addBoardMembers(this.builder_fr);
                final CompanyProfile data_fr = this.builder_fr.build();
                this.values_fr.put(data_fr.getInstrumentid(), data_fr);
            }

            if (this.builder_en.isValid()) {
                addBoardMembers(this.builder_en);
                final CompanyProfile data_en = this.builder_en.build();
                this.values_en.put(data_en.getInstrumentid(), data_en);
            }

            reset();
        }

        private void addBoardMembers(CompanyProfileImpl.Builder builder) {
            final List<CompanyProfile.BoardMember> members = this.boardMembers.get(builder.getInstrumentid());
            if (members != null) {
                for (final CompanyProfile.BoardMember member : members) {
                    builder.add(member);
                }
            }
        }

        protected void reset() {
            this.builder_nl = new CompanyProfileImpl.Builder();
            this.builder_fr = new CompanyProfileImpl.Builder();
            this.builder_en = new CompanyProfileImpl.Builder();
            this.errorOccured = false;
        }

        public Map<Long, CompanyProfile> getValues_NL() {
            return values_nl;
        }

        public Map<Long, CompanyProfile> getValues_FR() {
            return values_fr;
        }

        public Map<Long, CompanyProfile> getValues_EN() {
            return values_en;
        }
    }

    public static class CompanyPersonsReader extends AbstractSaxReader {
        private final Map<Long, List<CompanyProfile.BoardMember>> values = new HashMap<>();

        private long iid = -1;

        private String name;

        private String title;

        public void endElement(String uri, String localName, String tagName) throws SAXException {
            try {
                if (tagName.equals("ROW")) {
                    // i have finished a new row => process
                    storeFields();
                }
                else if (tagName.equals("IID")) {
                    this.iid = getCurrentLong();
                }
                else if (tagName.equals("NAME")) {
                    this.name = getCurrentString();
                }
                else if (tagName.equals("TITLE")) {
                    this.title = getCurrentString();
                }
                else {
                    notParsed(tagName);
                }
            }
            catch (Exception e) {
                this.logger.error("<endElement> error in " + tagName, e);
                this.errorOccured = true;
            }
        }

        private void storeFields() {
            this.limiter.ackAction();

            if (this.errorOccured) {
                reset();
                return;
            }

            if (this.iid < 0) {
                reset();
                return;
            }

            List<CompanyProfile.BoardMember> list = this.values.get(this.iid);

            if (list == null) {
                list = new ArrayList<>();
                this.values.put(this.iid, list);
            }

            list.add(new CompanyProfileImpl.BoardMemeberImpl(this.name, this.title));

            reset();
        }

        protected void reset() {
            this.iid = -1;
            this.name = null;
            this.title = null;
            this.errorOccured = false;
        }

        public Map<Long, List<CompanyProfile.BoardMember>> getValues() {
            return values;
        }
    }

    public static void main(String[] args) throws Exception {
        final CompanyDataProviderVwdBeNl p = new CompanyDataProviderVwdBeNl();
        p.setActiveMonitor(new ActiveMonitor());
        p.setCompanyDataFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider/istar-vwdbenl-company-portrait.xml.gz"));
        p.setCompanyPersonsFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider/istar-vwdbenl-company-persons.xml.gz"));
        p.setAnnualReportFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider/istar-vwdbenl-company-annual-report.xml.gz"));
        p.afterPropertiesSet();
        System.out.println(p.getCompanyProfile(new CompanyDataRequest(619283, null, Arrays.asList(new Locale("nl")))));
        System.out.println(p.getAnnualReportData(new CompanyDataRequest(619283, null, null)));
    }
}
