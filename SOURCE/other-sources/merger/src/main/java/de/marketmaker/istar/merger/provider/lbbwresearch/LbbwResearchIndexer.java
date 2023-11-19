package de.marketmaker.istar.merger.provider.lbbwresearch;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.soap.SOAPException;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.common.http.RestTemplateFactory;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.domain.instrument.ContentFlags;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.fusion.dmxml.DmxmlFacade;
import de.marketmaker.istar.fusion.dmxml.DmxmlRequest;
import de.marketmaker.istar.fusion.dmxml.ErrorType;
import de.marketmaker.istar.fusion.dmxml.HttpDmxmlFacade;
import de.marketmaker.istar.fusion.dmxml.InstrumentData;
import de.marketmaker.istar.fusion.dmxml.QuoteData;
import de.marketmaker.istar.fusion.dmxml.STKStaticData;
import de.marketmaker.istar.merger.provider.ContentFlagsWriter;
import de.marketmaker.istar.merger.provider.gisresearch.PlainTextSupport;
import de.marketmaker.istar.news.analysis.NewsAnalyzer;

import static de.marketmaker.istar.merger.provider.lbbwresearch.LbbwResearchIndexConstants.*;
import static java.time.temporal.ChronoUnit.DAYS;

/**
 * LbbwResearchIndexer to fetch documents and metadata from SOAP API and index the results
 * @author mcoenen
 */
public class LbbwResearchIndexer implements InitializingBean {

    static final String LBBW_RESEARCH_DOCS = "lbbw-research-docs.obj";

    private static final int MAX_TRIES = 2;

    private static final int SLEEP_BETWEEN_RETRIES = 5000;

    private static final Analyzer ANALYZER = new NewsAnalyzer();

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, BasicDocument> basicDocuments;

    private final Map<String, InstrumentInfo> isinToInstrument = new HashMap<>();

    private DmxmlFacade dmxmlFacade;

    private LbbwSOAPClient lbbwSOAPClient;

    private PatternProvider patternProvider;

    private File indexDir;

    private long maxDocumentAgeInDays = 3650L;

    private String authentication = "mm-xml";

    private String authenticationType = "resource";

    private int maxDaysToCheck = 7;

    private static LocalDate OLDEST_DOCUMENT_DATE = LocalDate.of(2016, 1, 1);

    private IndexWriter indexWriter;

    private File pdfBaseDir = LocalConfigProvider.getProductionDir("var/data/lbbwresearch/");

    private ContentFlagsWriter contentFlagsWriter = ContentFlagsWriter.NULL;

    public void setPdfBaseDir(String pdfBaseDir) {
        this.pdfBaseDir = new File(pdfBaseDir);
        this.indexDir = new File(this.pdfBaseDir, "index");
    }

    public void setLbbwSOAPClient(LbbwSOAPClient lbbwSOAPClient) {
        this.lbbwSOAPClient = lbbwSOAPClient;
    }

    public void setPatternProvider(PatternProvider patternProvider) {
        this.patternProvider = patternProvider;
    }

    public void setMaxDocumentAgeInDays(long maxDocumentAgeInDays) {
        this.maxDocumentAgeInDays = maxDocumentAgeInDays;
    }

    public void setDmxmlFacade(DmxmlFacade dmxmlFacade) {
        this.dmxmlFacade = dmxmlFacade;
    }

    public void setMaxDaysToCheck(int maxDaysToCheck) {
        this.maxDaysToCheck = maxDaysToCheck;
    }

    public void setContentFlagsWriter(ContentFlagsWriter contentFlagsWriter) {
        this.contentFlagsWriter = contentFlagsWriter;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        fixEncodings();

        if (!this.pdfBaseDir.isDirectory() && !this.pdfBaseDir.mkdirs()) {
            throw new IllegalArgumentException("<afterPropertiesSet> pdfBaseDir does not exist or is not a directory " + this.pdfBaseDir);
        }
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<afterPropertiesSet> Using " + this.pdfBaseDir.getAbsolutePath() + " as working dir");
        }

        this.removeEmptyPdfs();

        try {
            this.indexWriter = new IndexWriter(
                    FSDirectory.open(this.indexDir),
                    ANALYZER,
                    IndexWriter.MaxFieldLength.UNLIMITED);

            this.readBasicDocuments();

            this.removeExpiredDocuments();

            try {
                String loadMissingFromProperty = System.getProperty("loadMissingFrom");
                String loadMissingToProperty = System.getProperty("loadMissingTo");
                String replaceExisting = System.getProperty("replaceExisting");

                if (!StringUtils.isBlank(loadMissingFromProperty) && !StringUtils.isBlank(loadMissingToProperty)) {
                    LocalDate loadMissingFrom = LocalDate.parse(loadMissingFromProperty);
                    LocalDate loadMissingTo = LocalDate.parse(loadMissingToProperty);

                    if (DAYS.between(loadMissingFrom, loadMissingTo) >= this.maxDaysToCheck) {
                        throw new IllegalArgumentException("<afterPropertiesSet> To avoid excessive load on LBBW server it's not allowed to load more than " + maxDaysToCheck
                                + " days. Please split the date range into smaller pieces!");
                    }

                    this.loadDocuments(loadMissingFrom, loadMissingTo, "true".equals(replaceExisting));
                }
            } catch (DateTimeParseException e) {
                this.logger.warn("<afterPropertiesSet> Invalid date was passed as an argument", e);
            }

            LocalDate loadFrom = getLatestPublicationDate().orElse(OLDEST_DOCUMENT_DATE);
            this.loadDocuments(loadFrom, LocalDate.now(), false);

            this.writeBasicDocuments();

            this.updateFlags();
        } finally {
            this.indexWriter.close();
        }
    }

    private void updateFlags() {
        this.logger.info("<updateFlags>...");
        this.contentFlagsWriter.writeContentFlags(this.initFlags());
        this.logger.info("<updateFlags> ...done");
    }

    private Map<String, Set<ContentFlags.Flag>> initFlags() {
        this.logger.info("<initFlags> isinToInstrument.size: " + isinToInstrument.size());
        final Set<ContentFlags.Flag> set = Collections.singleton(ContentFlags.Flag.ResearchLBBW);

        return this.isinToInstrument.values()
                .stream()
                .distinct()
                .map(InstrumentInfo::getIid)
                .filter(e -> e.endsWith(".iid"))
                .map(e -> e.substring(0, e.length() - 4))
                .collect(Collectors.toMap(e -> e, e -> set));
    }

    private void removeEmptyPdfs() {
        try (Stream<Path> emptyFiles = Files.find(
                this.pdfBaseDir.toPath(),
                1,
                (path, basicFileAttributes) ->
                        basicFileAttributes.size() == 0L
                                && basicFileAttributes.isRegularFile()
                                && path.endsWith(".pdf"))) {
            emptyFiles.map(Path::toFile).forEach(File::delete);
        } catch (IOException e) {
            this.logger.warn("<removeEmptyPdfs> Problem removing empty PDF files.", e);
        }
    }

    private void fixEncodings() {
        fixEncoding("file.encoding");
        fixEncoding("sun.jnu.encoding");
    }

    private void fixEncoding(String key) {
        String encoding = System.getProperty(key);
        if (!("UTF-8".equalsIgnoreCase(encoding) || "UTF8".equalsIgnoreCase(encoding))) {
            this.logger.warn("<fixEncoding> " + key + " is set to " + encoding + ". Trying to fix to UTF-8.");
            System.setProperty(key, "UTF-8");
        }
    }

    private synchronized void loadDocuments(LocalDate from, LocalDate to, boolean replaceExisting) {
        this.logger.info("<loadDocuments> Loading documents for the following days: " + from + " to " + to + ", replaceExisting flag is " + replaceExisting);
        Map<String, BasicDocument> fetchedBasicDocuments = this.fetchBasicDocumentsWithCompanyInfo(from, to, !replaceExisting);
        this.logger.info("<loadDocuments> Fetched " + fetchedBasicDocuments.size() + " documents from server");

        Map<String, BasicDocument> modifiedIndexedElements = fetchAndUpdateCompanyInfo(this.basicDocuments, fetchedBasicDocuments);
        this.logger.info("<loadDocuments> Company info was changed in " + modifiedIndexedElements.size() + " documents");

        if (fetchedBasicDocuments.isEmpty() && modifiedIndexedElements.isEmpty()) {
            this.logger.info("<loadDocuments> No documents fetched, nothing has changed.");
            return;
        }

        this.basicDocuments =
                ImmutableMap.<String, BasicDocument>builder()
                        .putAll(this.basicDocuments)
                        .putAll(fetchedBasicDocuments)
                        .build();

        try {
            this.logger.info("<loadDocuments> removing modified documents...");
            this.removeFromIndex(modifiedIndexedElements.keySet(), false);

            Ordering<BasicDocument> dateOrdering = Ordering.natural().onResultOf(BasicDocument::getPublicationDate);
            if (!modifiedIndexedElements.isEmpty()) {
                this.logger.info("<loadDocuments> add updated documents...");
                for (BasicDocument basicDocument : dateOrdering.sortedCopy(modifiedIndexedElements.values())) {
                    persist(basicDocument);
                }
            }

            if (!fetchedBasicDocuments.isEmpty()) {
                this.logger.info("<loadDocuments> add new documents...");
                for (BasicDocument basicDocument : dateOrdering.sortedCopy(fetchedBasicDocuments.values())) {
                    persist(basicDocument);
                }
            }

            this.logger.info("<loadDocuments> optimize...");
            this.indexWriter.optimize();
            this.logger.info("<loadDocuments> done.");
        } catch (IOException e) {
            this.logger.error("<loadDocuments> Error indexing data", e);
        }
    }

    private Optional<LocalDate> getLatestPublicationDate() {
        return this.basicDocuments.values()
                .stream()
                .map(BasicDocument::getPublicationDate)
                .max(Comparator.naturalOrder())
                .map(OffsetDateTime::toLocalDate);
    }

    private void initInstrumentInfos(List<String> isins) {
        if (isins.isEmpty()) {
            return;
        }

        this.logger.info("<initInstrumentInfos> for " + isins.size() + " isins...");

        for (int i = 0; i < isins.size(); i += 50) {
            doInitInstrumentInfos(isins.subList(i, Math.min(isins.size(), i + 50)));
        }

        if (this.logger.isDebugEnabled()) {
            for (String isin : new TreeSet<>(this.isinToInstrument.keySet())) {
                this.logger.debug(isin + " => " + this.isinToInstrument.get(isin));
            }
        }

        this.logger.info("<initInstrumentInfos> done.");
    }

    private DmxmlRequest.Builder getBuilder() {
        return DmxmlRequest.createBuilder().withAuth(authentication, authenticationType);
    }

    private void doInitInstrumentInfos(List<String> isins) {
        DmxmlRequest.Builder builder = getBuilder();

        List<DmxmlRequest.Builder.Block<STKStaticData>> molecules =
                isins.stream()
                        .map(isin ->
                                builder.addBlock("STK_StaticData", STKStaticData.class)
                                        .with("symbol", isin)
                                        .with("symbolStrategy", "ISIN"))
                        .collect(Collectors.toList());

        DmxmlRequest request = builder.build();

        if (!this.dmxmlFacade.evaluate(request)) {
            throw new IllegalStateException();
        }

        for (DmxmlRequest.Builder.Block<STKStaticData> molecule : molecules) {

            if (molecule.getError() != null) {
                ErrorType error = molecule.getError();
                this.logger.warn("Block failed: "
                        + error.getCode() + " - "
                        + error.getDescription() + " - "
                        + error.getKey());
                continue;
            }
            STKStaticData e = molecule.getResult();
            LbbwResearchIndexer.InstrumentInfo info = (e != null) ? getInfo(e.getInstrumentdata(), e.getQuotedata()) : null;
            if (info != null) {
                info.name = e.getInstrumentdata().getName();
            }
        }
    }

    private LbbwResearchIndexer.InstrumentInfo getInfo(InstrumentData id, QuoteData qd) {
        if (id == null || id.getIsin() == null) {
            return null;
        }
        return this.isinToInstrument.computeIfAbsent(id.getIsin(), key -> new InstrumentInfo(id.getIid(), qd.getQid()));
    }

    private void readBasicDocuments() throws IOException, ClassNotFoundException {
        Path path = Paths.get(this.pdfBaseDir.getAbsolutePath(), LBBW_RESEARCH_DOCS);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<readBasicDocuments> Trying to read existing basic documents from " + path);
        }
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
            //noinspection unchecked
            this.basicDocuments = (Map<String, BasicDocument>) ois.readObject();
            int sizeSerialized = this.basicDocuments.size();
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<readBasicDocuments> Loaded " + sizeSerialized + " basic documents");
            }
            this.basicDocuments =
                    ImmutableMap.copyOf(
                            this.basicDocuments.entrySet()
                                    .stream()
                                    .filter(e -> new File(this.pdfBaseDir, e.getValue().getFilename()).isFile())
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                    );
            int sizeMissingRemoved = this.basicDocuments.size();
            if (this.logger.isDebugEnabled() && sizeSerialized > sizeMissingRemoved) {
                this.logger.debug("<readBasicDocuments> Removed " + (sizeSerialized - sizeMissingRemoved) + " elements with missing PDF.");
            }
        } catch (NoSuchFileException e) {
            this.logger.warn("<readBasicDocuments> No existing basic documents found. Starting new collection.");
            this.basicDocuments = ImmutableMap.of();
        }
    }

    private void writeBasicDocuments() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(Paths.get(this.pdfBaseDir.getAbsolutePath(), LBBW_RESEARCH_DOCS)))) {
            oos.writeObject(this.basicDocuments);
        }
    }

    private void removeFromIndex(Set<String> objectIds, boolean deleteFile) {
        for (String objectId : objectIds) {
            removeFromIndex(objectId, deleteFile);
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<removeFromIndex> Removed " + objectIds.size() + " documents from index.");
        }
    }

    private void removeExpiredDocuments() throws IOException {
        Set<String> expiredDocuments;
        try (LbbwIndexSearcher searcher = new LbbwIndexSearcher(this.indexWriter.getReader())) {
            LocalDateTime oldestDate = LocalDate.now().minusDays(this.maxDocumentAgeInDays).atTime(23, 59);
            Query expiredDocumentsQuery = NumericRangeQuery.newLongRange(FIELD_PUBLICATION_DATE, 0L, oldestDate.toEpochSecond(ZoneOffset.UTC), true, true);
            expiredDocuments = searcher.search(expiredDocumentsQuery);
        }

        for (String objectId : expiredDocuments) {
            removeFromIndex(objectId, true);
        }

        if (!expiredDocuments.isEmpty()) {
            this.basicDocuments = ImmutableMap.copyOf(Maps.filterKeys(this.basicDocuments, key -> !expiredDocuments.contains(key)));
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<removeExpiredDocuments> Removed " + expiredDocuments.size() + " expired documents from index.");
        }
    }

    private void removeFromIndex(String objectId, boolean deleteFile) {
        try {
            this.indexWriter.deleteDocuments(new Term(FIELD_ID, objectId));
        } catch (IOException e) {
            return;
        }

        if (!deleteFile || !this.basicDocuments.containsKey(objectId)) {
            return;
        }
        BasicDocument basicDocument = this.basicDocuments.get(objectId);
        File pdf = new File(this.pdfBaseDir, basicDocument.getFilename());
        if (!pdf.delete()) {
            this.logger.warn("<removeFromIndex> Removed expired document with id " + objectId + " from index but could not delete file " + pdf.getAbsolutePath());
        }
    }

    private boolean persist(BasicDocument basicDocument) {
        try {
            byte[] pdf = fetchPdf(basicDocument);
            String text = augmentDocumentFromPDF(basicDocument, pdf);
            writePdfToDisk(pdf, basicDocument.getFilename());
            Document luceneDocument = createDocument(basicDocument, text);
            this.indexWriter.updateDocument(new Term(LbbwResearchIndexConstants.FIELD_ID, basicDocument.getObjectId()), luceneDocument);
            return true;
        } catch (Exception e) {
            this.logger.warn("<persist> Error persisting BasicDocument " + basicDocument.getObjectId(), e);
            return false;
        }
    }

    private String augmentDocumentFromPDF(BasicDocument basicDocument, byte[] pdfDocument) {
        String text = convertPdfToText(pdfDocument);
        setDocumentValuesFromText(basicDocument, text);
        return text;
    }

    private byte[] fetchPdf(BasicDocument basicDocument) {
        return this.readPDFFromDisk(basicDocument)
                .map(Optional::of)
                .orElseGet(() -> this.lbbwSOAPClient.callGetDocumentContents(basicDocument.getObjectId()))
                .orElseThrow(() -> new IllegalStateException("<fetchPdf> PDF for " + basicDocument.getFilename() + " (" +
                        basicDocument.getObjectId() + ") neither on disk nor via webservice available or empty. Skipping."));
    }

    private Map<String, BasicDocument> fetchBasicDocumentsWithCompanyInfo(LocalDate from, LocalDate to, boolean skipExisting) {
        Map<String, BasicDocument> basicDocuments;

        Set<BasicDocument> fetchedBasicDocuments = new HashSet<>();
        try {
            for (LocalDate fetchDate = from; !fetchDate.isAfter(to) && DAYS.between(from, fetchDate) < maxDaysToCheck; fetchDate = fetchDate.plusDays(1)) {
                for (int remainingTries = MAX_TRIES - 1; remainingTries >= 0; remainingTries--) {
                    try {
                        Set<BasicDocument> basicDocumentSet = this.lbbwSOAPClient.callGetBasicDocuments(fetchDate, fetchDate);
                        this.logger.info("<fetchBasicDocumentsWithCompanyInfo> Fetched " + basicDocumentSet.size() + " documents for " + fetchDate);
                        fetchedBasicDocuments.addAll(basicDocumentSet);
                        break;
                    } catch (SOAPException e) {
                        // We need to stop fetching data if one day fails repeatedly
                        // because it will otherwise not be able to fill the gaps automatically
                        if (remainingTries == 0) {
                            throw e;
                        }
                        this.logger.warn(
                                "<fetchBasicDocumentsWithCompanyInfo> Fetching documents for " + fetchDate + " failed: " + e.getMessage()
                                        + ". Retrying after " + (SLEEP_BETWEEN_RETRIES / 1000) + "s.");
                        try {
                            Thread.sleep(SLEEP_BETWEEN_RETRIES);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            }
        } catch (SOAPException e) {
            this.logger.error("<fetchBasicDocumentsWithCompanyInfo> Fetching documents failed repeatedly.", e);
        }

        basicDocuments =
                ImmutableMap.copyOf(
                        fetchedBasicDocuments.stream()
                                .filter(bd -> !skipExisting || !this.basicDocuments.containsKey(bd.getObjectId()))
                                .collect(Collectors.toMap(BasicDocument::getObjectId, Function.identity()))
                );

        Map<String, CompanyInfo> companies =
                basicDocuments.values().stream()
                        .flatMap(bd -> bd.getCompanyObjectIds().stream())
                        .filter(Objects::nonNull)
                        .distinct()
                        .map(companyObjectId -> this.lbbwSOAPClient.callGetCompanyInfo(companyObjectId, CompanyInfo.ENABLED_DETAILS))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toMap(CompanyInfo::getObjectId, Function.identity()));

        for (BasicDocument basicDocument : basicDocuments.values()) {
            basicDocument.setCompanyInfos(
                    basicDocument.getCompanyObjectIds()
                            .stream()
                            .map(companies::get)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet())
            );
        }

        return basicDocuments;
    }

    /* updates company info of given basic documents with information from dmxml.
       returns basic documents that are already stored in the lucene index and
       whose company info has changed. */
    private Map<String, BasicDocument> fetchAndUpdateCompanyInfo(
            Map<String, BasicDocument> existingBasicDocuments,
            Map<String, BasicDocument> fetchedBasicDocuments) {

        this.initInstrumentInfos(
                Stream.concat(existingBasicDocuments.values().stream(), fetchedBasicDocuments.values().stream())
                        .flatMap(bd -> bd.getCompanyInfos().stream())
                        .map(CompanyInfo::getIsin)
                        .distinct()
                        .collect(Collectors.toList())
        );

        Map<String, BasicDocument> modifiedIndexedElements = new HashMap<>();
        Stream.concat(
                existingBasicDocuments.values().stream(),
                fetchedBasicDocuments.values().stream()
        )
                .forEach(basicDocument ->
                        basicDocument.getCompanyInfos()
                                .forEach(companyInfo -> {
                                    InstrumentInfo ii = this.isinToInstrument.get(companyInfo.getIsin());
                                    if (ii != null) {
                                        boolean alreadyInIndex = existingBasicDocuments.containsKey(basicDocument.getObjectId());
                                        boolean companyInfoChanged = !Objects.equals(companyInfo.name, ii.name);

                                        if (alreadyInIndex && companyInfoChanged) {
                                            modifiedIndexedElements.put(basicDocument.getObjectId(), basicDocument);
                                        }

                                        companyInfo.setName(ii.name);
                                    }
                                }));

        return ImmutableMap.copyOf(modifiedIndexedElements);
    }

    private Document createDocument(BasicDocument basicDocument, String text) {
        Document d = new Document();

        d.add(new Field(FIELD_ID, basicDocument.getObjectId(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
        d.add(new Field(FIELD_FILENAME, basicDocument.getFilename(), Field.Store.YES, Field.Index.NO));
        d.add(new NumericField(FIELD_PUBLICATION_DATE).setLongValue(basicDocument.getPublicationDate().toEpochSecond()));

        for (CompanyInfo companyInfo : basicDocument.getCompanyInfos()) {
            this.addKeyword(d, FIELD_ISIN, companyInfo.getIsin());
            companyInfo.getCountry().ifPresent(
                    country -> this.addKeyword(d, FIELD_COUNTRY, country)
            );
            companyInfo.getName().ifPresent(
                    name -> this.addKeyword(d, FIELD_NAME, name)
            );
        }

        String documentType = basicDocument.getDocumentType();
        this.addKeyword(d, FIELD_DOCUMENT_TYPE, documentType);
        this.addKeyword(d, FIELD_SELECTOR, this.getSelector(documentType));
        this.addKeyword(d, FIELD_CATEGORY, basicDocument.getCategory());
        this.addKeyword(d, FIELD_LANGUAGE, basicDocument.getLanguage());
        this.addKeyword(d, FIELD_SECTOR, basicDocument.getSector());

        d.add(new NumericField(FIELD_RATING).setIntValue(basicDocument.getRating()));
        d.add(new NumericField(FIELD_PREVIOUS_RATING).setIntValue(basicDocument.getPreviousRating()));

        basicDocument.getDecimalTargetPrice()
                .ifPresent(targetPrice -> d.add(new NumericField(FIELD_TARGET_PRICE)
                        .setLongValue(targetPrice.multiply(TARGET_PRICE_FACTOR).longValue())));
        basicDocument.getPreviousDecimalTargetPrice()
                .ifPresent(previousTargetPrice -> d.add(new NumericField(FIELD_PREVIOUS_TARGET_PRICE)
                        .setLongValue(previousTargetPrice.multiply(TARGET_PRICE_FACTOR).longValue())));

        basicDocument.getTitle()
                .ifPresent(title -> {
                    d.add(new Field(FIELD_TITLE, title, Field.Store.NO, Field.Index.ANALYZED));
                    d.add(new Field(FIELD_TEXT, title, Field.Store.NO, Field.Index.ANALYZED));
                });

        d.add(new Field(FIELD_TEXT, text, Field.Store.NO, Field.Index.ANALYZED));

        basicDocument.getCompanyInfos()
                .stream()
                .map(CompanyInfo::getCompanyGuidance)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(companyGuidance -> new Field(FIELD_COMPANY_GUIDANCE, companyGuidance, Field.Store.NO, Field.Index.ANALYZED))
                .forEach(d::add);

        return d;
    }

    private String getSelector(String documentType) {
        if ("BURKERTS_BLICK".equals(documentType) || "MAERKTE_IM_BLICK".equals(documentType)) {
            return String.valueOf(Selector.LBBW_RESEARCH_MAERKTE_IM_BLICK.getId());
        }
        else {
            return String.valueOf(Selector.LBBW_RESEARCH_RESTRICTED_REPORTS.getId());
        }
    }

    private void addKeyword(Document d, String fieldName, String value) {
        d.add(new Field(fieldName, value != null ? value : "", Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
    }

    private void setDocumentValuesFromText(BasicDocument basicDocument, String text) {
        Map<String, Pattern> patternMap = this.patternProvider.patternsPerLanguage.get(basicDocument.getLanguage());
        if (patternMap == null) {
            throw new IllegalStateException("<setDocumentValuesFromText> no pattern found for document " + basicDocument.getObjectId());
        }
        setRatings(patternMap, basicDocument, text);
        setTargetPrices(patternMap, basicDocument, text);
    }

    /* sets document's target price from the given text. if multiple target prices are present,
       they are ignored. */
    void setTargetPrices(Map<String, Pattern> patternMap, BasicDocument basicDocument,
            String text) {
        Matcher targetPriceMatcher = patternMap.get("targetPrice").matcher(text);
        if (targetPriceMatcher.find()) {
            BigDecimal targetPrice = new BigDecimal(targetPriceMatcher.group(1).replace(',', '.'));
            basicDocument.setDecimalTargetPrice(targetPrice);
            BigDecimal previousTargetPrice = Optional.ofNullable(targetPriceMatcher.group(2))
                    .map(p -> p.replace(',', '.'))
                    .map(BigDecimal::new)
                    .orElse(targetPrice);
            basicDocument.setPreviousDecimalTargetPrice(previousTargetPrice);
        }

        if (targetPriceMatcher.find()) {
            basicDocument.setDecimalTargetPrice(null);
            basicDocument.setPreviousDecimalTargetPrice(null);
        }
    }

    /* sets document's rating from the given text. if multiple ratings are present,
       they are ignored. */
    void setRatings(Map<String, Pattern> patternMap, BasicDocument basicDocument,
            String text) {
        Matcher ratingMatcher = patternMap.get("rating").matcher(text);
        if (ratingMatcher.find()) {
            String rating = ratingMatcher.group(1);
            basicDocument.setRating(this.patternProvider.getRatingValue(rating));
            String previousRating = ratingMatcher.group(2);
            basicDocument.setPreviousRating(this.patternProvider.getRatingValue(previousRating != null ? previousRating : rating));
        }

        if (ratingMatcher.find()) {
            basicDocument.setRating(BasicDocument.NO_VALUE);
            basicDocument.setPreviousRating(BasicDocument.NO_VALUE);
        }
    }

    private boolean hasValidPdfFileName(BasicDocument basicDocument) {
        return basicDocument.getFilename().startsWith(basicDocument.getObjectId() + '_');
    }

    private void fixPdfFileName(BasicDocument basicDocument) {
        if (this.hasValidPdfFileName(basicDocument)) {
            return;
        }
        String pdfCurrentName = basicDocument.getFilename();
        String pdfNewName = basicDocument.getObjectId() + '_' + basicDocument.getFilename();
        basicDocument.setFilename(pdfNewName);
        File pdfCurrentFile = new File(this.pdfBaseDir, pdfCurrentName);
        if (!pdfCurrentFile.isFile()) {
            return;
        }
        if (!pdfCurrentFile.renameTo(new File(this.pdfBaseDir, pdfNewName))) {
            this.logger.warn("<fixPdfFileName> Could not rename " + pdfCurrentName + " to " + pdfNewName);
        }
    }

    private Optional<byte[]> readPDFFromDisk(BasicDocument basicDocument) {
        this.fixPdfFileName(basicDocument);
        File pdfFile = new File(this.pdfBaseDir, basicDocument.getFilename());
        if (!pdfFile.isFile()) {
            return Optional.empty();
        }
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<readPDFFromDisk> Fetching document content for " + basicDocument.getObjectId() + " from " + pdfFile.getAbsolutePath());
        }
        try {
            return Optional.of(Files.readAllBytes(Paths.get(pdfFile.getAbsolutePath())));
        } catch (IOException e) {
            this.logger.warn("<readPDFFromDisk> Problem loading " + pdfFile.getAbsolutePath() + " from disk. Trying to fetch from server again.", e);
        }
        return Optional.empty();
    }

    private void writePdfToDisk(byte[] pdf, String filename) {
        File outputPath = new File(this.pdfBaseDir, filename);
        if (outputPath.exists() && outputPath.length() == pdf.length) {
            return;
        }

        try (OutputStream os = Files.newOutputStream(outputPath.toPath())) {
            os.write(pdf);
        } catch (Exception e) {
            throw new IllegalStateException("<writePDFToDisk> Error writing PDF to " + outputPath, e);
        }
    }

    private String convertPdfToText(byte[] doc) {
        try (PDDocument pdf = PDDocument.load(doc)) {
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            String text = pdfTextStripper.getText(pdf);
            return PlainTextSupport.joinSplitWords(new Scanner(text), text.length() + 128);
        } catch (IOException e) {
            throw new IllegalStateException("<convertToText> Problem converting PDF to plaintext", e);
        }
    }

    public static void main(String... args) throws Exception {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            configurator.doConfigure("config/src/main/resources/lbbw-research-indexer/classes/logback.xml"); // loads logback file
        } catch (JoranException je) {
            // StatusPrinter will handle this
        } catch (Exception ex) {
            ex.printStackTrace(); // Just in case, so we see a stacktrace
        }

        //System.setProperty("convertPriceToDecimalType", "true");
        //System.setProperty("loadMissingFrom", "2016-08-03");
        //System.setProperty("loadMissingTo", "2016-08-30");

        LbbwResearchIndexer indexer = new LbbwResearchIndexer();
        LbbwSOAPClientImpl soapClient = new LbbwSOAPClientImpl();
        PatternProvider patternProvider = new PatternProvider();
        patternProvider.afterPropertiesSet();

        HttpDmxmlFacade df = new HttpDmxmlFacade();
        String host = "mmfweb.vwd.com";
        String path = "/dmxml-1/iview/retrieve.xml";
        df.setRetrieveUrl("http://" + host + path);
        df.setRestTemplate(new RestTemplateFactory().getObject());

        indexer.setDmxmlFacade(df);
        indexer.setLbbwSOAPClient(soapClient);
        indexer.setPdfBaseDir(System.getenv("HOME") + "/produktion/var/data/research/lbbw");
        indexer.setPatternProvider(patternProvider);

        indexer.afterPropertiesSet();
    }

    private static class InstrumentInfo {

        private final String iid;

        private final String qid;

        String name;

        InstrumentInfo(String iid, String qid) {
            this.iid = iid;
            this.qid = qid;
        }

        private String getIid() {
            return iid;
        }

        @Override
        public String toString() {
            return "InstrumentInfo{" + this.iid + ", " + this.qid +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}
