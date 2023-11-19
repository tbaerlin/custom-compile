/*
 * NewsReport.java
 *
 * Created on 06.05.13 07:07
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import de.marketmaker.istar.news.backend.NewsReportItem.Option;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.store.FSDirectory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.news.data.NewsRecordImpl;
import de.marketmaker.istar.news.frontend.NewsAttributeEnum;
import de.marketmaker.istar.news.frontend.NewsIndexConstants;
import de.marketmaker.istar.news.frontend.NewsRecord;

/**
 * Creates four reports for news that have been received in a certain period.
 *
 * <dl>
 * <dt>supplier.txt</dt>
 * <dd><em>select NDB_Supplier, count(NDB_Supplier) from new group by NDB_Supplier</em></dd>
 * <dt>bizwire.csv</dt>
 * <dd>for all news with NDB_Supplier =~ bizwire*</dd>
 * <dt>dgap.csv</dt>
 * <dd>for all news with NDB_Supplier = DGAP (see T-38805)</dd>
 * <dt>globenewswire.csv</dt>
 * <dd>for all news with NDB_ID_News_Agency = GlobeNewswire (see T-41167)</dd>
 * </dl>
 *
 * @author oflege
 */
public class NewsReport {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String REPORT_XLSX = "newsreport.xlsx";

    private static final DateTimeFormatter DTF = ISODateTimeFormat.localDateOptionalTimeParser().withZone(DateUtil.DTZ_BERLIN);

    private static final DateTimeFormatter DATE = DateTimeFormat.forPattern("yyyy-MM-dd");

    private static final DateTimeFormatter TIME = DateTimeFormat.forPattern("HH:mm:ss");

    private final NewsDao dao;

    private final Sort sort = new Sort(new SortField(NewsIndexConstants.FIELD_TIMESTAMP, SortField.INT, true));

    private final Map<String, MutableInt> suppliers = new HashMap<>();

    private final List<NewsReportItem> configs;

    private boolean verbose = false;

    private CellStyle green;

    private CellStyle blue;

    private CellStyle bold;

    private File newsDir = new File(System.getProperty("user.home") + "/produktion/var/data/news/historicIndex");

    private File outDir = new File(System.getProperty("user.home") + "/produktion/var/data/news/reports");

    private DateTime startDate = new LocalDate().withDayOfMonth(1).minusMonths(1).toDateTimeAtStartOfDay();

    private DateTime endDate = new LocalDate().withDayOfMonth(1).toDateTimeAtStartOfDay();

    private FSDirectory dir;

    public NewsReport(NewsDao dao, List<NewsReportItem> configs) {
        this.dao = dao;
        this.configs = configs;
    }

    private XSSFWorkbook prepareWorkbook() {
        XSSFWorkbook workbook = new XSSFWorkbook();

        Font headerFont = workbook.createFont();
        headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        headerFont.setFontHeightInPoints((short) 16);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        Font subheaderFont = workbook.createFont();
        subheaderFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        subheaderFont.setFontHeightInPoints((short) 11);
        subheaderFont.setColor(IndexedColors.WHITE.getIndex());

        Font boldFont = workbook.createFont();
        boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        boldFont.setFontHeightInPoints((short) 11);
        boldFont.setColor(IndexedColors.BLACK.getIndex());

        green = workbook.createCellStyle();
        green.setBorderBottom(CellStyle.BORDER_THIN);
        green.setBorderLeft(CellStyle.BORDER_THIN);
        green.setBorderRight(CellStyle.BORDER_THIN);
        green.setBorderTop(CellStyle.BORDER_THIN);
        green.setFont(headerFont);
        green.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        green.setFillPattern(CellStyle.SOLID_FOREGROUND);
        green.setAlignment(CellStyle.ALIGN_CENTER);

        blue = workbook.createCellStyle();
        blue.setBorderBottom(CellStyle.BORDER_THIN);
        blue.setBorderLeft(CellStyle.BORDER_THIN);
        blue.setBorderRight(CellStyle.BORDER_THIN);
        blue.setBorderTop(CellStyle.BORDER_THIN);
        blue.setFont(subheaderFont);
        blue.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        blue.setFillPattern(CellStyle.SOLID_FOREGROUND);

        bold = workbook.createCellStyle();
        bold.setBorderBottom(CellStyle.BORDER_THIN);
        bold.setBorderLeft(CellStyle.BORDER_THIN);
        bold.setBorderRight(CellStyle.BORDER_THIN);
        bold.setBorderTop(CellStyle.BORDER_THIN);
        bold.setFont(boldFont);

        return workbook;
    }

    private void generateReports() throws IOException {
        prepareOutDir();

        collectSupplierCounts();

        removeHtmlNews("ddp", "mynewsdesk");

        XSSFWorkbook workbook = prepareWorkbook();

        XSSFSheet overviewSheet = workbook.createSheet("Overview");
        createOverview(overviewSheet);

        for (NewsReportItem config : this.configs) {
            parametricReport(config, workbook.createSheet(config.getDisplayName()), overviewSheet);
        }

        XSSFSheet supplierSheet = workbook.createSheet("Supplier");
        supplierReport(supplierSheet, overviewSheet);

        overviewSheet.autoSizeColumn(0);
        overviewSheet.autoSizeColumn(1);
        overviewSheet.autoSizeColumn(2);

        FileOutputStream fileOut = new FileOutputStream(new File(this.outDir, REPORT_XLSX));
        workbook.write(fileOut);
        fileOut.close();
    }

    private void createOverview(XSSFSheet overviewSheet) {
        XSSFRow monthRow = overviewSheet.createRow(0);
        monthRow.createCell(0).setCellValue(startDate.monthOfYear().getAsText() + " " + startDate.year().get());
        monthRow.getCell(0).setCellStyle(green);
    }

    private void prepareOutDir() throws IOException {
        if (!outDir.isDirectory() && !outDir.mkdirs()) {
            throw new IOException("cannot use/create outDir " + outDir.getAbsolutePath());
        }
        for (String s : new String[]{REPORT_XLSX}) {
            final File f = new File(this.outDir, s);
            if (f.exists()) {
                File bak = new File(this.outDir, s + ".bak");
                if (bak.exists() && !bak.delete()) {
                    throw new IOException("cannot delete " + bak.getAbsolutePath());
                }
                if (!f.renameTo(bak)) {
                    throw new IOException("cannot rename " + f.getAbsolutePath());
                }
            }
        }
    }

    private void collectSupplierCounts() throws IOException {
        this.dir = FSDirectory.open(newsDir);
        try (final IndexReader reader = IndexReader.open(dir, true)) {
            final Set<String> names = collectSuppliers(reader);

            final IndexSearcher searcher = new IndexSearcher(reader);

            for (String supplier : names) {
                Query q = createQuery(NewsIndexConstants.FIELD_SUPPLIER, supplier);
                final TopDocs topDocs = searcher.search(q, 1000000);
                this.suppliers.put(supplier, new MutableInt(topDocs.totalHits));
            }
        }
    }

    private void supplierReport(XSSFSheet supplierSheet, XSSFSheet overviewSheet) throws FileNotFoundException, UnsupportedEncodingException {

        XSSFRow titleRow = overviewSheet.createRow(overviewSheet.getLastRowNum() + 2);
        titleRow.createCell(0).setCellValue("General Overview");
        titleRow.getCell(0).setCellStyle(blue);

        XSSFRow xssfHeaderRow1 = overviewSheet.createRow(overviewSheet.getLastRowNum() + 1);
        xssfHeaderRow1.createCell(0).setCellValue("Name");
        xssfHeaderRow1.getCell(0).setCellStyle(bold);
        xssfHeaderRow1.createCell(1).setCellValue("Count");
        xssfHeaderRow1.getCell(1).setCellStyle(bold);

        XSSFRow xssfHeaderRow2 = supplierSheet.createRow(0);
        xssfHeaderRow2.createCell(0).setCellValue("Name");
        xssfHeaderRow2.createCell(1).setCellValue("Count");

        int total = 0;

        for (Map.Entry<String, MutableInt> e : suppliers.entrySet()) {
            int value = e.getValue().intValue();

            XSSFRow xssfRow1 = supplierSheet.createRow(supplierSheet.getLastRowNum() + 1);
            xssfRow1.createCell(0).setCellValue(e.getKey());
            xssfRow1.createCell(1).setCellValue(value);

            XSSFRow xssfRow2 = overviewSheet.createRow(overviewSheet.getLastRowNum() + 1);
            xssfRow2.createCell(0).setCellValue(e.getKey());
            xssfRow2.createCell(1).setCellValue(value);

            total += value;
        }

        XSSFRow totalRow = overviewSheet.createRow(overviewSheet.getLastRowNum() + 1);
        totalRow.createCell(0).setCellValue("Total Result");
        totalRow.getCell(0).setCellStyle(bold);
        totalRow.createCell(1).setCellValue(total);
        totalRow.getCell(1).setCellStyle(bold);

        supplierSheet.autoSizeColumn(0);
        supplierSheet.autoSizeColumn(1);
    }

    /**
     * some suppliers send news in text and html format; to avoid duplicate counts, we remove
     * the html news...
     * @param supplierNames
     * @throws IOException
     */
    private void removeHtmlNews(String... supplierNames) throws IOException {
        try (final IndexReader reader = IndexReader.open(dir, true)) {
            final IndexSearcher searcher = new IndexSearcher(reader);

            for (String supplier : supplierNames) {
                MutableInt mi = this.suppliers.get(supplier.toLowerCase());
                if (mi == null) {
                    logger.warn("Unknown supplier " + supplier.toLowerCase());
                    continue;
                }

                Query q = createQuery(NewsIndexConstants.FIELD_SUPPLIER, supplier);

                List<String> ids = getNewsIds(searcher, q);

                for (int i = 0; i < ids.size(); i += 50) {
                    final List<NewsRecord> items = dao.getItems(ids.subList(i, Math.min(i + 50, ids.size())), false, false);
                    for (NewsRecord item : items) {
                        NewsRecordImpl impl = (NewsRecordImpl) item;
                        if (impl.isHtml() || impl.isNitf()) {
                            mi.decrement();
                        }
                    }
                }
            }
        }
    }

    private void parametricReport(NewsReportItem reportConfig, XSSFSheet reportSheet, XSSFSheet overviewSheet) throws IOException {
        List<NewsRecordImpl> news = new ArrayList<>();
        final Option configOption = reportConfig.getOption();
        if (configOption == Option.NAME_IS_PREFIX) {
            for (String supplier : suppliers.keySet()) {
                if (supplier.startsWith(reportConfig.getName())) {
                    news.addAll(getNewsRecords(createQuery(reportConfig.getField(), supplier)));
                }
            }
        } else {
            news = getNewsRecords(createQuery(reportConfig.getField(), reportConfig.getName()));
        }

        XSSFRow xssfHeaderRow = reportSheet.createRow(0);
        int cellId = 0;
        xssfHeaderRow.createCell(cellId++).setCellValue("Date");
        xssfHeaderRow.createCell(cellId++).setCellValue("Time");
        if (verbose) {
            xssfHeaderRow.createCell(cellId++).setCellValue("Id");
        }
        if (configOption == Option.USE_COMBINED_KEY) {
            xssfHeaderRow.createCell(cellId++).setCellValue("ProductId");
            xssfHeaderRow.createCell(cellId++).setCellValue("UniqueId");
            xssfHeaderRow.createCell(cellId++).setCellValue("Type");
            xssfHeaderRow.createCell(cellId++).setCellValue("CombinedKey");
        }
        xssfHeaderRow.createCell(cellId++).setCellValue("Language");
        if (configOption == Option.NAME_IS_PREFIX) {
            xssfHeaderRow.createCell(cellId++).setCellValue("Supplier");
        }
        xssfHeaderRow.createCell(cellId++).setCellValue("Headline");

        HashMap<String, HashMap<String, Integer>> totals = new HashMap<>();
        Set<String> ids = new HashSet<>();

        for (NewsRecordImpl impl : news) {
            if (impl.isHtml() || impl.isNitf()) {
                if (configOption == Option.NAME_IS_PREFIX) {
                    this.suppliers.get(impl.getSupplier().toLowerCase()).decrement();
                }
                continue;
            }

            final DateTime dt = impl.getTimestamp();
            String type = "";
            String combinedKey = getCombinedKey(impl);

            if (reportConfig.getKeys().size() == 0 || reportConfig.getKeys().contains(combinedKey.toLowerCase())) {
                cellId = 0;
                XSSFRow xssfRow = reportSheet.createRow(reportSheet.getLastRowNum() + 1);
                xssfRow.createCell(cellId++).setCellValue(DATE.print(dt));
                xssfRow.createCell(cellId++).setCellValue(TIME.print(dt));
                if (verbose) {
                    xssfRow.createCell(cellId++).setCellValue(impl.getId());
                }
                if (configOption == Option.USE_COMBINED_KEY) {
                    xssfRow.createCell(cellId++).setCellValue(impl.getProductId());
                    String uniqueId = "";
                    try {
                        uniqueId = impl.getProductId().split("_")[1];
                    } catch (Exception e) {
                        logger.error("Unable to extract unique id from " + impl.getProductId() + " due to " + e.getMessage());
                    }
                    ids.add(uniqueId);
                    xssfRow.createCell(cellId++).setCellValue(uniqueId);
                    type = impl.getHeadline().toLowerCase().contains(reportConfig.getName()) ? "Corporate" : "Press";
                    xssfRow.createCell(cellId++).setCellValue(type);
                    xssfRow.createCell(cellId++).setCellValue(combinedKey);
                }
                xssfRow.createCell(cellId++).setCellValue(impl.getLanguage());
                if (configOption == Option.NAME_IS_PREFIX) {
                    xssfRow.createCell(cellId++).setCellValue(impl.getSupplier());
                }
                xssfRow.createCell(cellId++).setCellValue(impl.getHeadline());

                if (configOption == Option.USE_COMBINED_KEY) {
                    HashMap<String, Integer> typeTotal = totals.getOrDefault(type, new HashMap<>());
                    Integer total = typeTotal.getOrDefault(combinedKey, 0) + 1;
                    typeTotal.put(combinedKey, total);
                    totals.put(type, typeTotal);
                } else if (configOption == Option.NAME_IS_PREFIX) {
                    HashMap<String, Integer> typeTotal = totals.getOrDefault(impl.getSupplier(), new HashMap<>());
                    Integer total = typeTotal.getOrDefault(impl.getLanguage(), 0) + 1;
                    typeTotal.put(impl.getLanguage(), total);
                    totals.put(impl.getSupplier(), typeTotal);
                }
            }
        }

        XSSFRow overviewRow = overviewSheet.createRow(overviewSheet.getLastRowNum() + 2);
        overviewRow.createCell(0).setCellValue(reportConfig.getDisplayName());
        overviewRow.getCell(0).setCellStyle(blue);
        XSSFRow overviewRow2 = overviewSheet.createRow(overviewSheet.getLastRowNum() + 1);
        overviewRow2.createCell(0).setCellValue("Total News Items");
        overviewRow2.getCell(0).setCellStyle(bold);
        overviewRow2.createCell(1).setCellValue(reportSheet.getLastRowNum());
        overviewRow2.getCell(1).setCellStyle(bold);
        if (configOption == Option.USE_COMBINED_KEY) {
            XSSFRow uniquesRow = overviewSheet.createRow(overviewSheet.getLastRowNum() + 1);
            uniquesRow.createCell(0).setCellValue("Distinct News Items");
            uniquesRow.getCell(0).setCellStyle(bold);
            XSSFCell distinctCell = uniquesRow.createCell(1);
            distinctCell.setCellValue(ids.size());
            distinctCell.setCellStyle(bold);
        }

        if (configOption == Option.USE_COMBINED_KEY || configOption == Option.NAME_IS_PREFIX) {
            XSSFRow totalsHeaderRow = overviewSheet.createRow(overviewSheet.getLastRowNum() + 2);

            if (configOption == Option.USE_COMBINED_KEY) {
                totalsHeaderRow.createCell(0).setCellValue("Type");
                totalsHeaderRow.getCell(0).setCellStyle(bold);
                totalsHeaderRow.createCell(1).setCellValue("CombinedKey");
                totalsHeaderRow.getCell(1).setCellStyle(bold);
                totalsHeaderRow.createCell(2).setCellValue("Count");
                totalsHeaderRow.getCell(2).setCellStyle(bold);
            } else if (configOption == Option.NAME_IS_PREFIX) {
                totalsHeaderRow.createCell(0).setCellValue("Supplier");
                totalsHeaderRow.getCell(0).setCellStyle(bold);
                totalsHeaderRow.createCell(1).setCellValue("Language");
                totalsHeaderRow.getCell(1).setCellStyle(bold);
                totalsHeaderRow.createCell(2).setCellValue("Count");
                totalsHeaderRow.getCell(2).setCellStyle(bold);
            }
            int total = 0;
            for (String type : totals.keySet()) {
                XSSFRow typeRow = overviewSheet.createRow(overviewSheet.getLastRowNum() + 1);
                typeRow.createCell(0).setCellValue(type);
                HashMap<String, Integer> typeTotals = totals.get(type);
                for (String key : typeTotals.keySet()) {
                    XSSFRow keyRow = overviewSheet.createRow(overviewSheet.getLastRowNum() + 1);
                    keyRow.createCell(1).setCellValue(key);
                    keyRow.createCell(2).setCellValue(typeTotals.get(key));
                    total += typeTotals.get(key);
                }
            }
            XSSFRow totalsResultRow = overviewSheet.createRow(overviewSheet.getLastRowNum() + 1);
            totalsResultRow.createCell(0).setCellValue("Total Result");
            totalsResultRow.getCell(0).setCellStyle(bold);
            totalsResultRow.createCell(2).setCellValue(total);
            totalsResultRow.getCell(2).setCellStyle(bold);
        }

        for (int i = 0; i < cellId; i++) {
            reportSheet.autoSizeColumn(i);
        }
    }

    private List<NewsRecordImpl> getNewsRecords(Query query) throws IOException {
        this.dir = FSDirectory.open(newsDir);
        List<NewsRecordImpl> news = new ArrayList<>();
        try (final IndexReader reader = IndexReader.open(dir, true)) {
            final IndexSearcher searcher = new IndexSearcher(reader);

            List<String> ids = getNewsIds(searcher, query);

            for (int i = 0; i < ids.size(); i += 50) {
                final List<NewsRecord> items = dao.getItems(ids.subList(i, Math.min(i + 50, ids.size())), false, false);
                for (NewsRecord item : items) {
                    news.add((NewsRecordImpl) item);
                }
            }
        }
        return news;
    }

    /**
     * see 20140731_Process_Specification_DGAP.xlsx attached to T-38804
     */
    private String getCombinedKey(NewsRecordImpl impl) {
        Set<String> rubriken = impl.getAttributes(NewsAttributeEnum.CATEGORY);
        if (rubriken.size() != 1) {
            return rubriken.isEmpty() ? "-" : StringUtils.collectionToDelimitedString(rubriken, ",");
        }
        String rubrik = rubriken.iterator().next();
        String suffix = "de".equals(impl.getLanguage()) ? "D"
                : "en".equals(impl.getLanguage()) ? "E" : String.valueOf(impl.getLanguage());
        if ("WPU".equals(rubrik) || ("adhoc".equals(rubrik) && impl.getHeadline().toLowerCase().contains("wpüg"))) {
            return "WPU";
        }
        if ("adhoc".equals(rubrik)) {
            return "AD" + suffix;
        }
        if ("corporate".equals(rubrik)) {
            return "CN" + suffix;
        }
        return (rubrik + suffix).toUpperCase();
    }

    private List<String> getNewsIds(IndexSearcher searcher, Query q) throws IOException {
        final TopFieldCollector collector =
                TopFieldCollector.create(sort, 50000, true, false, false, false);
        searcher.search(q, collector);
        final TopDocs docs = collector.topDocs();

        List<String> result = new ArrayList<>(docs.totalHits);
        for (ScoreDoc scoreDoc : docs.scoreDocs) {
            final Document document = searcher.doc(scoreDoc.doc);
            result.add(document.getField(NewsIndexConstants.FIELD_ID).stringValue());
        }
        return result;
    }

    private Query createQuery(String field, String supplier) {
        Query q = new TermQuery(new Term(field, supplier));
        BooleanQuery result = new BooleanQuery();
        result.add(q, BooleanClause.Occur.MUST);
        result.add(NumericRangeQuery.newIntRange(NewsIndexConstants.FIELD_TIMESTAMP, News2Document.encodeTimestamp(startDate),
            News2Document.encodeTimestamp(endDate), true, false), BooleanClause.Occur.MUST);
        return result;

    }

    private Set<String> collectSuppliers(IndexReader reader) throws IOException {
        Set<String> result = new TreeSet<>();

        Term term = new Term(NewsIndexConstants.FIELD_SUPPLIER, "0");
        final TermEnum termEnum = reader.terms(term);
        //noinspection StringEquality
        do {
            result.add(termEnum.term().text());
        } while (termEnum.next() && termEnum.term().field() == term.field());
        return result;
    }

    public static void main(String[] args) throws IOException {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(NewsReportConfig.class);
        final NewsReport nr = ctx.getBean(NewsReport.class);

        int n = 0;
        while (n < args.length && args[n].startsWith("-")) {
            if ("-start".equals(args[n])) {
                nr.startDate = DTF.parseDateTime(args[++n]);
            }
            else if ("-end".equals(args[n])) {
                nr.endDate = DTF.parseDateTime(args[++n]);
            }
            else if ("-out".equals(args[n])) {
                nr.outDir = new File(args[++n]);
            }
            else if ("-v".equals(args[n])) {
                nr.verbose = true;
            }
            n++;
        }

        nr.generateReports();
    }
}
