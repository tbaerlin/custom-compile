/*
 * CompanyFundamentalsProviderConvensys.java
 *
 * Created on 09.08.2006 07:57:42
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.convensys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.DirectoryResource;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.monitor.Resource;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.common.xml.XmlWriter;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.BasicBalanceFigures;
import de.marketmaker.istar.domain.data.CompanyProfile;
import de.marketmaker.istar.domain.data.ConvensysRawdata;
import de.marketmaker.istar.domain.data.DownloadableItem;
import de.marketmaker.istar.domain.data.IpoData;
import de.marketmaker.istar.domain.data.LocalizedString;
import de.marketmaker.istar.domain.data.ProfitAndLoss;
import de.marketmaker.istar.domain.data.ReferenceInterval;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.data.BasicBalanceFiguresImpl;
import de.marketmaker.istar.domainimpl.data.CompanyProfileImpl;
import de.marketmaker.istar.domainimpl.data.ConvensysRawdataImpl;
import de.marketmaker.istar.domainimpl.data.IpoDataImpl;
import de.marketmaker.istar.domainimpl.data.ProfitAndLossImpl;
import de.marketmaker.istar.domainimpl.instrument.InstrumentDp2;
import de.marketmaker.istar.domainimpl.instrument.StockDp2;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.domainimpl.profile.ResourcePermissionProvider;
import de.marketmaker.istar.merger.Constants;
import de.marketmaker.istar.merger.provider.CompanyFundamentalsProvider;
import de.marketmaker.istar.merger.provider.ConvensysRawdataRequest;
import de.marketmaker.istar.merger.provider.ConvensysRawdataResponse;
import de.marketmaker.istar.merger.provider.companydate.CompanyDateDaysRequest;
import de.marketmaker.istar.merger.provider.companydate.CompanyDateDaysResponse;
import de.marketmaker.istar.merger.provider.companydate.CompanyDateProvider;
import de.marketmaker.istar.merger.provider.companydate.CompanyDateProviderImpl;
import de.marketmaker.istar.merger.provider.companydate.CompanyDateRequest;
import de.marketmaker.istar.merger.provider.companydate.CompanyDateResponse;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CompanyFundamentalsProviderConvensys implements CompanyFundamentalsProvider, InitializingBean {

    final static DateTimeFormatter DTF = DateTimeFormat.forPattern("dd.MM.yyyy");

    private final static BigDecimal HUNDRED = new BigDecimal(100);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private CompanyDateProvider companyDateProvider;

    private ActiveMonitor activeMonitor;

    private File portraitFile;

    private File shareholderFile;

    private File balanceFile;

    private File ratiotoolFile;

    private File ipoFile;

    private File downloadFile;

    private File rawdataBasedir;

    private File rawDataProfiles;

    private File rawDataTransformed;

    private File rawDataKeyData;

    private File rawDataFacunda;

    private File rawDataFacundaBoerseGo;

    private final List<File> directoryDirs = new ArrayList<>();

    private final Map<File, ReentrantReadWriteLock> locks = new HashMap<>();

    private final Map<File, Map<String, Long>> symbols = new HashMap<>();

    private File contentBasedir;

    private final Map<Long, CompanyProfileImpl> profiles = new HashMap<>();

    private final Map<Long, List<BasicBalanceFigures>> figures = new HashMap<>();

    private final Map<Long, List<ProfitAndLoss>> profitAndLoss = new HashMap<>();

    private final List<IpoData> ipoData = new ArrayList<>();

    private final Map<Long, List<DownloadableItem>> downloads = new HashMap<>();

    private boolean limitedRead = true;

    static final Comparator<ReferenceInterval> RI_COMPARATOR = (o1, o2) -> {
        final int i = o1.getInterval().getStart().compareTo(o2.getInterval().getStart());
        if (i != 0) {
            return i;
        }
        final int s1 = o1.isShortenedFiscalYear() ? 0 : 1;
        final int s2 = o2.isShortenedFiscalYear() ? 0 : 1;
        return s1 - s2;
    };

    public void setLimitedRead(boolean limitedRead) {
        this.limitedRead = limitedRead;
    }

    public void setCompanyDateProvider(CompanyDateProvider companyDateProvider) {
        this.companyDateProvider = companyDateProvider;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setDownloadFile(File downloadFile) {
        this.downloadFile = downloadFile;
    }

    public void setIpoFile(File ipoFile) {
        this.ipoFile = ipoFile;
    }

    public void setRatiotoolFile(File ratiotoolFile) {
        this.ratiotoolFile = ratiotoolFile;
    }

    public void setPortraitFile(File portraitFile) {
        this.portraitFile = portraitFile;
    }

    public void setShareholderFile(File shareholderFile) {
        this.shareholderFile = shareholderFile;
    }

    public void setBalanceFile(File balanceFile) {
        this.balanceFile = balanceFile;
    }

    public void setRawdataBasedir(File rawdataBasedir) {
        this.rawdataBasedir = rawdataBasedir;
    }

    public void setContentBasedir(File contentBasedir) {
        this.contentBasedir = contentBasedir;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        readPortraitAndShareholderData();
        readBalanceData();
        readIpoData();
        readDownloadData();

        final FileResource shareholder = new FileResource(this.shareholderFile);
        shareholder.addPropertyChangeListener(evt -> readPortraitAndShareholderData());

        final FileResource balance = new FileResource(this.balanceFile);
        balance.addPropertyChangeListener(evt -> readBalanceData());

        final FileResource ipo = new FileResource(this.ipoFile);
        ipo.addPropertyChangeListener(evt -> readIpoData());

        final FileResource download = new FileResource(this.downloadFile);
        download.addPropertyChangeListener(evt -> readDownloadData());

        this.activeMonitor.addResources(new Resource[]{shareholder, balance, ipo, download});

        if (this.rawdataBasedir == null) {
            return;
        }

        final List<DirectoryResource> directoryResources = initDirectoryLists();
        this.activeMonitor.addResources(directoryResources.toArray(new Resource[directoryResources.size()]));
    }

    private List<DirectoryResource> initDirectoryLists() throws Exception {
        this.rawDataProfiles = new File(this.rawdataBasedir, "profiles");
        this.rawDataKeyData = new File(this.rawdataBasedir, "keydata");
        this.rawDataTransformed = new File(this.rawdataBasedir, "transformed");
        this.rawDataFacunda = new File(this.rawdataBasedir, "facunda");
        this.rawDataFacundaBoerseGo = new File(this.rawdataBasedir, "facunda-BoerseGo");

        this.directoryDirs.addAll(Arrays.asList(
                this.rawDataProfiles,
                this.rawDataKeyData,
                this.rawDataFacunda,
                this.rawDataFacundaBoerseGo
        ));

        final List<DirectoryResource> result = new ArrayList<>();

        for (final File directoryDir : this.directoryDirs) {
            this.locks.put(directoryDir, new ReentrantReadWriteLock());
            this.symbols.put(directoryDir, new HashMap<>());

            processRawDataDirSymbols(directoryDir, null, null);

            final DirectoryResource resource = new DirectoryResource(directoryDir.getAbsolutePath());
            resource.addPropertyChangeListener(evt -> {
                final String pn = evt.getPropertyName();
                final Set<File> nv = (Set<File>) evt.getNewValue();
                processRawDataDirSymbols(directoryDir, pn, nv);
            });

            result.add(resource);
        }

        return result;
    }

    private void processRawDataDirSymbols(File directoryDir, String pn, Set<File> files) {
        final TimeTaker tt = new TimeTaker();
        final ReentrantReadWriteLock.WriteLock lock = getWriteLock(directoryDir);
        lock.lock();
        try {
            final Map<String, Long> symbols = getSymbols(directoryDir);
            if (pn == null) {
                // read from the directory directly
                if (directoryDir != null) {
                    directoryDir.listFiles(file -> {
                        putIntoSymbolsMap(symbols, file);
                        return false;
                    });
                }
            }
            else if (DirectoryResource.ADDED.equals(pn) || DirectoryResource.MODIFIED.equals(pn)) {
                for (File file : files) {
                    putIntoSymbolsMap(symbols, file);
                }
            }
            else if (DirectoryResource.REMOVED.equals(pn)) {
                for (File file : files) {
                    removeFromSymbols(symbols, file);
                }
            }
            else {
                throw new IllegalArgumentException("illegal property name: '" + pn + "'");
            }
        } finally {
            lock.unlock();
        }
        this.logger.info("<processRawDataDirSymbols> read " + directoryDir + ", pn=" + pn + ", #files=" + (files == null ? "null" : files.size()) + ", took " + tt);
    }

    private void putIntoSymbolsMap(Map<String, Long> symbols, File file) {
        final String name = file.getName();
        if (name.toLowerCase().endsWith(".xml") && name.length() >= 16) {
            symbols.put(name.substring(0, 12), file.lastModified());
        }
    }

    private void removeFromSymbols(Map<String, Long> symbols, File file) {
        final String name = file.getName();
        if (name.toLowerCase().endsWith(".xml") && name.length() >= 16) {
            symbols.remove(name.substring(0, 12));
        }
    }

    private Map<String, Long> getSymbols(File directoryDir) {
        return this.symbols.get(directoryDir);
    }

    private ReentrantReadWriteLock.ReadLock getReadLock(File directoryDir) {
        return this.locks.get(directoryDir).readLock();
    }

    private ReentrantReadWriteLock.WriteLock getWriteLock(File directoryDir) {
        return this.locks.get(directoryDir).writeLock();
    }

    private static class MyInputStream extends FilterInputStream {
        protected MyInputStream(InputStream in) {
            super(in);
        }

        public int read() throws IOException {
            final int i = super.read();
            if (i < 0) {
                return i;
            }
            if (i < 32) {
                return 32;
            }
            return i;
        }

        public int read(byte b[], int off, int len) throws IOException {
            final int numChars = super.read(b, off, len);
            if (numChars < 0) {
                return numChars;
            }
            for (int i = off; i < off + numChars; i++) {
                if ((b[i] & 0xff) < 32) {
                    b[i] = 32;
                }
            }
            return numChars;
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "REC", justification = "catch all exceptions")
    private void readDownloadData() {
        try {
            final TimeTaker tt = new TimeTaker();
            final Map<Long, List<DownloadableItem>> data =
                    new DownloadsReader().read(this.downloadFile);
            synchronized (this) {
                this.downloads.clear();
                this.downloads.putAll(data);
            }
            this.logger.info("<readDownloadData> read " + this.downloadFile + ", took " + tt);
        } catch (Exception e) {
            this.logger.error("<readCompanyDate> failed", e);
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "REC", justification = "catch all exceptions")
    private void readIpoData() {
        try {
            final TimeTaker tt = new TimeTaker();
            final List<IpoData> data = new ArrayList<>();

            final SAXBuilder saxBuilder = new SAXBuilder();
            final InputStream is = new MyInputStream(new GZIPInputStream(new FileInputStream(this.ipoFile)));
            final Document document = saxBuilder.build(is);
            is.close();

            //noinspection unchecked
            final List<Element> elements = document.getRootElement().getChildren("ROW");
            for (final Element element : elements) {
                final String idText = element.getChildTextTrim("INSTRUMENTID");
                final Long instrumentid = StringUtils.hasText(idText) ? Long.parseLong(idText) : null;
                final String name = element.getChildTextTrim("NAME");
                final DateTime issueDate = parseDateTime(element, "FIRSTLISTING");
                final String issuePrice = element.getChildTextTrim("PRICING");
                final String issueCurrency = element.getChildTextTrim("CURRENCY");
                final DateTime startOfSubscriptionPeriod = parseDateTime(element, "SIGNINGFROM");
                final DateTime endOfSubscriptionPeriod = parseDateTime(element, "SIGNINGTO");
                final String marketsegment = element.getChildTextTrim("MARKETSEGMENT");
                final BigDecimal bookbuildingLow = parsePrice(element, "BOOKBUILDINGLOW");
                final BigDecimal bookbuildingHigh = parsePrice(element, "BOOKBUILDINGHIGH");
                final String lms = element.getChildTextTrim("NAMELEAD");
                final List<String> leadManagers;
                if (StringUtils.hasText(lms)) {
                    final String[] els = lms.split("\\|");
                    leadManagers = new ArrayList<>(els.length);
                    for (final String s : els) {
                        leadManagers.add(s.trim());
                    }
                }
                else {
                    leadManagers = Collections.emptyList();
                }

                data.add(new IpoDataImpl(instrumentid, name, issueDate, issuePrice, issueCurrency,
                        startOfSubscriptionPeriod, endOfSubscriptionPeriod, bookbuildingLow,
                        bookbuildingHigh, marketsegment, leadManagers));
            }

            synchronized (this) {
                this.ipoData.clear();
                this.ipoData.addAll(data);
            }
            this.logger.info("<readIpoData> read " + this.ipoFile + ", took " + tt);
        } catch (Exception e) {
            this.logger.error("<readIpoData> failed", e);
        }
    }

    private DateTime parseDateTime(Element element, String tagname) {
        final String text = element.getChildTextTrim(tagname);
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return DTF.parseDateTime(text);
    }

    private BigDecimal parsePrice(Element element, String tagname) {
        final String text = element.getChildTextTrim(tagname);
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return new BigDecimal(text);
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "REC", justification = "catch all exceptions")
    private void readPortraitAndShareholderData() {
        try {
            final TimeTaker tt = new TimeTaker();
            final Map<Long, CompanyProfileImpl> tmpProfiles = new HashMap<>();

            final SAXBuilder saxBuilder = new SAXBuilder();
            final InputStream isPortrait = new MyInputStream(new GZIPInputStream(new FileInputStream(this.portraitFile)));
            final Document docPortrait = saxBuilder.build(isPortrait);
            isPortrait.close();

            //noinspection unchecked
            final List<Element> portraits = docPortrait.getRootElement().getChildren("ROW");
            for (final Element portrait : portraits) {
                final String idText = portrait.getChildTextTrim("INSTRUMENTID");
                if (!StringUtils.hasText(idText)) {
                    continue;
                }

                final CompanyProfileImpl.Builder b = new CompanyProfileImpl.Builder();
                b.setInstrumentid(Long.parseLong(idText));
                b.setStreet(portrait.getChildTextTrim("STREET"));
                b.setCity(portrait.getChildTextTrim("CITY"));
                b.setCountry(portrait.getChildTextTrim("COUNTRY"));
                b.setTelephone(portrait.getChildTextTrim("PHONE"));
                b.setFax(portrait.getChildTextTrim("FAX"));
                b.setUrl(portrait.getChildTextTrim("INTERNET"));
                b.setEmail(portrait.getChildTextTrim("EMAIL"));
                b.setPortrait(portrait.getChildTextTrim("PORTRAIT"), LocalizedString.DEFAULT_LANGUAGE);

                final CompanyProfileImpl cpi = new CompanyProfileImpl(b);
                tmpProfiles.put(cpi.getInstrumentid(), cpi);
            }


            final InputStream isShareholders = new MyInputStream(new GZIPInputStream(new FileInputStream(this.shareholderFile)));
            final Document docShareholders = saxBuilder.build(isShareholders);
            isPortrait.close();

            //noinspection unchecked
            final List<Element> shareholders = docShareholders.getRootElement().getChildren("ROW");
            for (final Element shareholder : shareholders) {
                final String idText = shareholder.getChildTextTrim("INSTRUMENTID");
                if (!StringUtils.hasText(idText)) {
                    continue;
                }
                final long instrumentid = Long.parseLong(idText);
                final String name = shareholder.getChildTextTrim("NAME");
                final String shareText = shareholder.getChildTextTrim("SHARE_");
                final BigDecimal share = shareText == null ? null : new BigDecimal(shareText);

                final CompanyProfileImpl cp = tmpProfiles.get(instrumentid);
                if (cp == null) {
                    continue;
                }
                cp.getShareholders().add(new CompanyProfileImpl.ShareholderImpl(name,
                        share != null ? share.divide(BigDecimal.valueOf(100), Constants.MC) : null));
            }

            synchronized (this) {
                this.profiles.clear();
                this.profiles.putAll(tmpProfiles);
            }
            this.logger.info("<readPortraitAndShareholderData> read " + this.portraitFile + " and " + this.shareholderFile + ", took " + tt);
        } catch (Exception e) {
            this.logger.error("<readPortraitAndShareholderData> failed", e);
        }
    }

    private void readBalanceData() {
        try {
            final TimeTaker tt = new TimeTaker();
            final Map<Long, List<BasicBalanceFigures>> tmpFigures = new HashMap<>();
            final Map<Long, List<ProfitAndLoss>> tmpProfitAndLoss = new HashMap<>();

            final BalancesReader br = new BalancesReader(this.limitedRead);
            br.read(this.balanceFile);

            final Map<Long, Map<String, Map<ReferenceInterval, BigDecimal>>> values = br.getValues();
            final Map<Long, String> currencies = br.getCurrencies();

            for (final Map.Entry<Long, Map<String, Map<ReferenceInterval, BigDecimal>>> entry : values.entrySet()) {
                final long instrumentid = entry.getKey();
                final Map<String, Map<ReferenceInterval, BigDecimal>> fs = entry.getValue();

                final Set<ReferenceInterval> intervalSet = new TreeSet<>(RI_COMPARATOR);
                for (final Map<ReferenceInterval, BigDecimal> is : fs.values()) {
                    intervalSet.addAll(is.keySet());
                }

                for (final ReferenceInterval interval : intervalSet) {
                    // ########### basic balance figures
                    final BigDecimal shareholdersEquity = getValue(fs, interval, "/financialstatement/liabilitiesandequity/equity/subscribedcapital/year");
                    final BigDecimal balanceSheetTotal = getValue(fs, interval, "/financialstatement/assets/totalassets/year");
                    final BigDecimal equityCapital = getValue(fs, interval, "/financialstatement/liabilitiesandequity/equity/totalequity/year");
                    final BigDecimal employeesBd = getValue(fs, interval, "/employeeinformations/employees/year");
                    final Integer employees = employeesBd == null ? null : employeesBd.intValue();
                    final BigDecimal assetsBank = getValue(fs, interval, "/financialstatement/assetsbank/investments/year");
                    final BigDecimal assetsInsurance = getValue(fs, interval, "/financialstatement/assetsinsurance/investments/year");
                    final BigDecimal assets;
                    if (assetsBank != null && assetsInsurance != null) {
                        assets = assetsBank.add(assetsInsurance);
                    }
                    else if (assetsBank == null && assetsInsurance != null) {
                        assets = assetsInsurance;
                    }
                    else if (assetsBank != null && assetsInsurance == null) {
                        assets = assetsBank;
                    }
                    else {
                        assets = null;
                    }

                    final BasicBalanceFiguresImpl bbf = new BasicBalanceFiguresImpl(interval, currencies.get(instrumentid), equityCapital,
                            shareholdersEquity, balanceSheetTotal, employees, assets);
                    List<BasicBalanceFigures> bbfs = tmpFigures.get(instrumentid);
                    if (bbfs == null) {
                        bbfs = new ArrayList<>();
                        tmpFigures.put(instrumentid, bbfs);
                    }
                    bbfs.add(bbf);

                    // ########### profit and loss
                    final BigDecimal sales = getValue(fs, interval, "/incomestatement/sales/year");
                    final BigDecimal profitYear = getValue(fs, interval, "/incomestatement/netincome/year");
                    final BigDecimal dividend = getValue(fs, interval, "/pershare/dividendpershare/year");
                    BigDecimal dividendYield = getValue(fs, interval, "/kennzahlen/rating/dividendratio/year");
                    if (dividendYield != null) {
                        dividendYield = dividendYield.divide(HUNDRED, Constants.MC);
                    }
                    final BigDecimal earningPerShare = getValue(fs, interval, "/pershare/earningpershare/basiceps/year");
                    final BigDecimal dilutedEarningPerShare = getValue(fs, interval, "/pershare/earningpershare/dilutedeps/year");

                    final ProfitAndLossImpl paf = new ProfitAndLossImpl(interval, currencies.get(instrumentid),
                            sales, profitYear, dividend, dividendYield, earningPerShare, dilutedEarningPerShare);
                    List<ProfitAndLoss> pafs = tmpProfitAndLoss.get(instrumentid);
                    if (pafs == null) {
                        pafs = new ArrayList<>();
                        tmpProfitAndLoss.put(instrumentid, pafs);
                    }
                    pafs.add(paf);
                }
            }

//            System.out.println("#values: " + values.size());
//            final Map<String, Map<Interval, BigDecimal>> daimler = values.get(20665L);
//            System.out.println("#paths: " + daimler.size());
//            for (final Map.Entry<String, Map<Interval, BigDecimal>> entry : daimler.entrySet()) {
//                System.out.println(entry.getKey() + ", #intervals: " + entry.getValue().size());
//                final BigDecimal value = entry.getValue().get(new Interval(DTF.parseDateTime("01.01.2005"), DTF.parseDateTime("31.12.2005")));
//                System.out.println("  ==> " + value);
//            }
//            final Map<Interval, BigDecimal> intervals = daimler.get("/financialstatement/liabilitiesandequity/liabilities/totalliabilities/year");
//            System.out.println(intervals);

            synchronized (this) {
                this.figures.clear();
                this.figures.putAll(tmpFigures);
                this.profitAndLoss.clear();
                this.profitAndLoss.putAll(tmpProfitAndLoss);
            }
            this.logger.info("<readBalanceData> read " + this.balanceFile + ", took " + tt);
            writeFileForRatiotool(tmpProfitAndLoss);
        } catch (Exception e) {
            this.logger.error("<readBalanceData> failed", e);
        }
    }

    private void writeFileForRatiotool(
            Map<Long, List<ProfitAndLoss>> profitAndLoss) throws Exception {
        if (this.ratiotoolFile == null) {
            this.logger.warn("<writeFileForRatiotool> no ratiotoolFile defined, returning");
            return;
        }

        final TimeTaker tt = new TimeTaker();
        final XmlWriter writer = new XmlWriter();
        writer.setCdataElements(new String[0]);
        writer.setEncoding("UTF-8");
        writer.setGzipped(true);
        writer.setPrettyPrint(true);
        writer.setRootElement("ROWS");
        writer.setFile(this.ratiotoolFile);
        writer.start();

        final org.w3c.dom.Document outDocument = writer.getDocument();

        final int vorjahr = new LocalDate().getYear() - 1;
        final int vorvorjahr = vorjahr - 1;

        for (final Map.Entry<Long, List<ProfitAndLoss>> entry : profitAndLoss.entrySet()) {
            final ProfitAndLoss vorPal = getProfitAndLoss(vorjahr, entry.getValue());
            final ProfitAndLoss vorvorPal = getProfitAndLoss(vorvorjahr, entry.getValue());

            if (vorPal == null && vorvorPal == null) {
                continue;
            }

            final org.w3c.dom.Element element = outDocument.createElement("ROW");
            element.appendChild(createElement(outDocument, "INSTRUMENTID", entry.getKey().toString()));

            if (vorPal != null) {
                writeValue(element, outDocument, "convensysProfit_1Y", vorPal.getProfitYear());
                writeValue(element, outDocument, "convensysEps_1Y", vorPal.getEarningPerShare());
                writeValue(element, outDocument, "convensysDividend_1Y", vorPal.getDividend());
                writeValue(element, outDocument, "convensysDividendyield_1Y", vorPal.getDividendYield());
            }
            if (vorvorPal != null) {
                writeValue(element, outDocument, "convensysProfit_2Y", vorvorPal.getProfitYear());
                writeValue(element, outDocument, "convensysEps_2Y", vorvorPal.getEarningPerShare());
                writeValue(element, outDocument, "convensysDividend_2Y", vorvorPal.getDividend());
                writeValue(element, outDocument, "convensysDividendyield_2Y", vorvorPal.getDividendYield());
            }

            writer.writeNode(element);
        }

        writer.stop();
        this.logger.info("<writeFileForRatiotool> took " + tt);
    }

    private void writeValue(org.w3c.dom.Element element, org.w3c.dom.Document outDocument,
            String tagname, BigDecimal value) {
        if (value == null) {
            return;
        }
        element.appendChild(createElement(outDocument, tagname, Double.toString(value.doubleValue())));
    }

    private ProfitAndLoss getProfitAndLoss(int year, List<ProfitAndLoss> pals) {
        for (final ProfitAndLoss pal : pals) {
            if (pal.getReference().getInterval().getStart().getYear() == year) {
                return pal;
            }
        }
        return null;
    }

    private org.w3c.dom.Element createElement(org.w3c.dom.Document document, String tagname,
            String item) {
        final org.w3c.dom.Element el = document.createElement(tagname);
        el.appendChild(document.createTextNode(item));
        return el;
    }

    private BigDecimal getValue(Map<String, Map<ReferenceInterval, BigDecimal>> fs,
            ReferenceInterval interval, String key) {
        final Map<ReferenceInterval, BigDecimal> data = fs.get(key);
        return data == null ? null : data.get(interval);
    }

    synchronized public CompanyProfile getProfile(long instrumentid) {
        return this.profiles.get(instrumentid);
    }

    @Override
    synchronized public List<BasicBalanceFigures> getBalanceFigures(long instrumentid) {
        final List<BasicBalanceFigures> c = this.figures.get(instrumentid);
        if (c != null) {
            return new ArrayList<>(c);
        }

        return Collections.emptyList();
    }

    @Override
    synchronized public List<DownloadableItem> getDownloads(long instrumentid) {
        final List<DownloadableItem> c = this.downloads.get(instrumentid);
        if (c != null) {
            return new ArrayList<>(c);
        }

        return Collections.emptyList();
    }

    @Override
    synchronized public List<IpoData> getUpcomingIpos() {
        final DateTime now = new DateTime();

        return this.ipoData
                .stream()
                .filter(data -> data.getIssueDate() == null || data.getIssueDate().isAfter(now))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    synchronized public List<IpoData> getPastIpos() {
        final DateTime now = new DateTime();

        return this.ipoData
                .stream()
                .filter(data -> data.getIssueDate() != null && !data.getIssueDate().isAfter(now))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    synchronized public List<ProfitAndLoss> getProfitAndLosses(long instrumentid) {
        final List<ProfitAndLoss> c = this.profitAndLoss.get(instrumentid);
        if (c != null) {
            return new ArrayList<>(c);
        }

        return Collections.emptyList();
    }

    public static void main(String[] args) throws Exception {
        final CompanyFundamentalsProviderConvensys cp = new CompanyFundamentalsProviderConvensys();
        cp.setDownloadFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider/istar-convensys-download.xml.gz"));
        cp.setPortraitFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider/istar-convensys-portrait.xml.gz"));
        cp.setShareholderFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider/istar-convensys-shareholder.xml.gz"));
        cp.setBalanceFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider/istar-convensys-balance.xml.gz"));
        cp.setRatiotoolFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider/istar-ratios-stk-convensys.xml.gz"));
        cp.setIpoFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider/istar-convensys-ipo.xml.gz"));
        cp.setRawdataBasedir(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/convensys"));

        final CompanyDateProviderImpl cdp = CompanyDateProviderImpl.createProvider(null);

        cp.setActiveMonitor(new ActiveMonitor());
        cp.setCompanyDateProvider(cdp);
        cp.setLimitedRead(false);
        cp.afterPropertiesSet();

        final InstrumentDp2 instrument = new StockDp2(20665);
        instrument.setSymbol(KeysystemEnum.ISIN, "DE0007100000");

        eval(cp, instrument, "mm-xml", false, false);
        eval(cp, instrument, "mm-xml", true, false);
        eval(cp, instrument, "mm-xml", false, true);
        eval(cp, instrument, "mm-xml", true, true);

        eval(cp, instrument, "facunda-test", false, false);
        eval(cp, instrument, "facunda-test", true, false);
        eval(cp, instrument, "facunda-test", false, true);
        eval(cp, instrument, "facunda-test", true, true);
        eval(cp, instrument, "boersego-test", false, false);
    }

    private static void eval(CompanyFundamentalsProviderConvensys cp, InstrumentDp2 instrument,
            final String profileKey, final boolean keydata, final boolean metadata) {
        final Profile profile = ProfileFactory.createInstance(ResourcePermissionProvider.getInstance(profileKey));
        final ConvensysRawdataResponse response = cp.getRawdata(new ConvensysRawdataRequest(profile, instrument, keydata, false, metadata));
        final ConvensysRawdata rawdata = response.getRawdata();
        System.out.println(profileKey + "/keydata=" + keydata + "/metadata=" + metadata + ": " + (rawdata == null ? "null" : rawdata.getContent()));
    }

    private File getRawMetadataFile(Profile profile, boolean keydata) {
        final String filename = keydata ? "translationk.xml" : "translationlist.xml";
        final File dir = getDir(profile, false, keydata);

        if (dir == null) {
            return null;
        }

        return new File(dir, filename);
    }

    private File getDir(Profile profile, boolean transformed, boolean keydata) {
        final File convensysDir = keydata ? this.rawDataKeyData : this.rawDataProfiles;

        if (profile == null) {
            // TODO: old default => change to Exception or no delivery after general update
            return convensysDir;
        }

        if (transformed) {
            if (profile.isAllowed(Selector.CONVENSYS_I) || profile.isAllowed(Selector.CONVENSYS_II)) {
                return this.rawDataTransformed;
            }

            return null;
        }

        if (profile.isAllowed(Selector.CONVENSYS_I) || profile.isAllowed(Selector.CONVENSYS_II)) {
            return convensysDir;
        }

        if (profile.isAllowed(Selector.FACUNDA_COMPANY_DATA)) {
            return this.rawDataFacunda;
        }

        if (profile.isAllowed(Selector.FACUNDA_COMPANY_DATA_BOERSE_GO)) {
            return this.rawDataFacundaBoerseGo;
        }

        return null;
    }

    private File getRawdataFile(Profile profile, String isin, boolean transformed,
            boolean keydata) {
        final String filename = isin + ".xml";
        final File dir = getDir(profile, transformed, keydata);
        if (dir == null) {
            return null;
        }
        return new File(dir, filename);
    }

    private ConvensysRawdata readRawdata(File file) {
        if (!file.exists()) {
            return null;
        }

        try {
            final String s = FileUtils.readFileToString(file, "UTF-8");
            return new ConvensysRawdataImpl(s);
        } catch (IOException e) {
            this.logger.error("<readRawdata> failed", e);
            return null;
        }
    }

    @Override
    public String getConvensysContent(String isin, String contentKey) {
        final File dir = new File(this.contentBasedir, contentKey);
        if (!dir.isDirectory()) {
            this.logger.warn("<getConvensysContent> no such directory: " + dir.getAbsolutePath());
            return null;
        }
        final File f = new File(dir, isin + ".txt");
        if (!f.canRead()) {
            this.logger.info("<getConvensysContent> no such file: " + f.getAbsolutePath());
            return null;
        }
        try {
            // see: ISTAR-854
            // not the most efficient way, but no worrying about closing streams etc.
            return new String(FileCopyUtils.copyToByteArray(f), Charset.forName("UTF-8"));
        } catch (IOException e) {
            this.logger.warn("<getConvensysContent> failed for" + f.getAbsolutePath(), e);
            return null;
        }
    }

    @Override
    public Map<String, Object> getAdditionalInformation(String isin) {
        throw new UnsupportedOperationException();
    }

    public List<String> getConvensysRawdataDir(Profile profile, boolean keydata,
            DateTime referenceDate) {
        final File dir = getDir(profile, false, keydata);

        if (dir == null) {
            return Collections.emptyList();
        }

        final ReentrantReadWriteLock.ReadLock lock = getReadLock(dir);
        lock.lock();
        try {
            final long ms = referenceDate.getMillis();
            final Map<String, Long> symbols = getSymbols(dir);
            final List<String> result =
                    symbols.entrySet()
                            .stream()
                            .filter(entry -> entry.getValue() >= ms)
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toCollection(ArrayList::new));
            return result;
        } finally {
            lock.unlock();
        }
    }

    public List<String> getConvensysRawdataDir(boolean keydata, DateTime referenceDate) {
        return getConvensysRawdataDir(null, keydata, referenceDate);
    }

    @Override
    public byte[] getOrganisationStructurePdf(String isin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getPortraitPdf(String isin) {
        throw new UnsupportedOperationException();
    }

    public ConvensysRawdata getPortraitData(String isin) {
        return readRawdata(getRawdataFile(null, isin, false, false));
    }

    public ConvensysRawdata getRatioData(String isin) {
        return readRawdata(getRawdataFile(null, isin, false, true));
    }

    public ConvensysRawdata getPortraitMetadata() {
        return readRawdata(getRawMetadataFile(null, false));
    }

    public ConvensysRawdata getRatioMetadata() {
        return readRawdata(getRawMetadataFile(null, true));
    }

    @Override
    public ConvensysRawdataResponse getRawdata(ConvensysRawdataRequest request) {
        final File file;
        if (request.isMetadata()) {
            file = getRawMetadataFile(request.getProfile(), request.isKeydata());
        }
        else {
            final String isin = request.getInstrument().getSymbolIsin();
            file = getRawdataFile(request.getProfile(), isin, request.isTransformed(), request.isKeydata());
        }

        if (file == null) {
            final ConvensysRawdataResponse invalid = new ConvensysRawdataResponse();
            invalid.setInvalid();
            return invalid;
        }

        final ConvensysRawdata rawdata = readRawdata(file);

        final ConvensysRawdataResponse response = new ConvensysRawdataResponse();
        response.setRawdata(rawdata);
        return response;
    }

    public CompanyDateResponse getCompanyDates(CompanyDateRequest request) {
        return companyDateProvider.getCompanyDates(request);
    }

    public CompanyDateDaysResponse getDaysWithCompanyDates(CompanyDateDaysRequest request) {
        return this.companyDateProvider.getDaysWithCompanyDates(request);
    }

}
