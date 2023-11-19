/*
 * IpoUpdater.java
 *
 * Created on 21.08.13 12:27
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.ipomonitor;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.object.BatchSqlUpdate;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.DomainContextImpl;
import de.marketmaker.istar.instrument.export.InstrumentDirDao;

/**
 * @author oflege
 */
class IpoAnalyzer {

    private static final Pattern KEY_PATTERN = Pattern.compile("[^\\.]+\\.([A-Z_]+)(\\..*)?");

    private static final String[] INSTRUMENT_DAO_FILENAMES = new String[]{"instruments.iol", "instruments.dat"};

    private static final DateTimeFormatter INDEX_ZIP_DTF = DateTimeFormat.forPattern("yyyy_MM_dd__HH_mm_ss");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private File baseDir = new File(System.getProperty("user.home"), "produktion/var/data/ipos/");

    private File newKeysDir = new File(baseDir, "newkeys");

    private File tempDir = new File(baseDir, "temp");

    private DataSource dataSource;

    private JdbcTemplate jt;

    private Map<String, Item> items = new HashMap<>(262144);

    public IpoAnalyzer(DataSource ds) {
        this.dataSource = ds;
        this.jt = new JdbcTemplate(ds);
    }

    private void deleteOldItems(LocalDate ld) {
        final int numDeleted = jt.update("DELETE FROM ipos WHERE created < '" + ld + "'");
        this.logger.info("deleted " + numDeleted + " items older than " + ld);
    }

    private void readItems() {
        final PreparedStatementCreator psc = new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(
                    Connection con) throws SQLException {
                final PreparedStatement result = con.prepareStatement(
                        "SELECT vwdcode, created, indexed, biskeyed FROM ipos",
                        ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                result.setFetchSize(Integer.MIN_VALUE);
                return result;
            }
        };
        final ResultSetExtractor<Object> rse = new ResultSetExtractor<Object>() {
            @Override
            public Object extractData(
                    ResultSet rs) throws SQLException, DataAccessException {
                while (rs.next()) {
                    final String vwdcode = rs.getString(1);
                    final int created = getSeconds(rs.getTimestamp(2));
                    final int indexed = getSeconds(rs.getTimestamp(3));
                    final int biskeyed = getSeconds(rs.getTimestamp(4));
                    items.put(vwdcode, new Item(vwdcode, getMarket(vwdcode), created, indexed, biskeyed));
                }
                return null;
            }
        };
        this.jt.query(psc, rse);
        this.logger.info("Read " + items.size() + " items from db");
    }

    private void insertNewKeys(LocalDate from) throws IOException {
        LocalDate ld = from;
        File f;
        while ((f = getNewKeysFile(ld)).canRead()) {
            insertNewKeys(f);
            ld = ld.plusDays(1);
        }
    }

    private File getNewKeysFile(LocalDate ld) {
        return new File(this.newKeysDir,
                "newkeys-staticfeed.log." + ISODateTimeFormat.date().print(ld) + ".gz");
    }

    private void insertNewKeys(File f) throws IOException {
        BatchSqlUpdate insert = new BatchSqlUpdate(this.dataSource,
                "INSERT INTO ipos (vwdcode, market, created) VALUES (?,?,?)",
                new int[]{Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP}, 200);

        try (Scanner sc = new Scanner(new GZIPInputStream(new FileInputStream(f)))) {
            while (sc.hasNextLine()) {
                final String s = sc.nextLine();
                if (s.length() <= 24 || s.charAt(24) == '#') {
                    continue;
                }
                // looks like 2013-08-20T00:04:21.178 VZ0EQP.EUWAX
                // we sometimes have s.th. like test.WGZ and TEST.WGZ, so uppercase
                final String vwdcode = s.substring(24).toUpperCase();
                if (!StringUtils.hasText(vwdcode) || items.containsKey(vwdcode)) {
                    continue;
                }
                String market = getMarket(vwdcode);
                if (market == null || "WM".equals(market) || market.endsWith("MT")) {
                    continue;
                }
                final DateTime dt = ISODateTimeFormat.dateHourMinuteSecondMillis().parseDateTime(s.substring(0, 23));
                insert.update(vwdcode, market, new Timestamp(dt.getMillis()));
                items.put(vwdcode, new Item(vwdcode, market, getSeconds(dt), 0, 0));
            }
        } finally {
            insert.flush();
        }
    }

    private String getMarket(String vwdcode) {
        final Matcher matcher = KEY_PATTERN.matcher(vwdcode);
        return matcher.matches() ? matcher.group(1).intern() : null;
    }

    private void processIndexes(final LocalDate from) throws IOException {
        final NavigableMap<DateTime, File> files = findFiles();

        BatchSqlUpdate update = new BatchSqlUpdate(this.dataSource,
                "UPDATE ipos set indexed=?, biskeyed=? WHERE vwdcode=?",
                new int[]{Types.TIMESTAMP, Types.TIMESTAMP, Types.VARCHAR}, 200);

        DomainContextImpl dc = null;
        for (Map.Entry<DateTime, File> e : files.tailMap(from.toDateTimeAtStartOfDay()).entrySet()) {
            File file = e.getValue();

            InstrumentDirDao dao = createDao(file, dc);
            if (file.getName().startsWith("instrument")) {
                dc = dao.getDomainContext();
            }
            this.logger.info("Processing instruments from " + file.getName());
            for (Instrument instrument : dao) {
                for (Quote quote : instrument.getQuotes()) {
                    final String vwdcode = quote.getSymbolVwdcode();
                    final Item item = this.items.get(vwdcode);
                    if (item == null || item.isComplete()) {
                        continue;
                    }
                    boolean updated = false;
                    if (item.indexed == 0) {
                        item.indexed = getSeconds(e.getKey());
                        updated = true;
                    }
                    if (item.biskeyed == 0 && quote.getSymbolBisKey() != null) {
                        item.biskeyed = getSeconds(e.getKey());
                        updated = true;
                    }
                    if (updated) {
                        update.update(createTimestamp(item.indexed), createTimestamp(item.biskeyed), vwdcode);
                    }
                }
            }
        }
        update.flush();
    }

    private int getSeconds(final DateTime dt) {
        return (int) (dt.getMillis() / 1000L);
    }

    private int getSeconds(Timestamp ts) {
        return ts != null ? (int) (ts.getTime() / 1000L) : 0;
    }

    private Timestamp createTimestamp(int secs) {
        return secs > 0 ? new Timestamp(secs * 1000L) : null;
    }

    private InstrumentDirDao createDao(File file, DomainContextImpl dc) throws IOException {
        this.logger.info("Unzipping " + file.getName());
        try (ZipFile zf = new ZipFile(file, ZipFile.OPEN_READ)) {
            for (String name : INSTRUMENT_DAO_FILENAMES) {
                final ZipEntry entry = zf.getEntry("data/instruments/" + name);
                File f = new File(this.tempDir, name);
                if (f.exists() && !f.delete()) {
                    throw new IOException("failed to delete " + f.getAbsoluteFile());
                }
                FileCopyUtils.copy(zf.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(f), 65536));
            }
        }
        try {
            return new InstrumentDirDao(this.tempDir, dc);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private NavigableMap<DateTime, File> findFiles() {
        final Pattern p = Pattern.compile("(instrument|update)\\.zip\\.(20[\\d_]{18})");
        final NavigableMap<DateTime, File> result = new TreeMap<>();

        File indexesDir = new File(System.getProperty("user.home"), "produktion/var/data/instrument/out/");
        indexesDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                final Matcher m = p.matcher(f.getName());
                if (m.matches()) {
                    final DateTime dt = INDEX_ZIP_DTF.parseDateTime(m.group(2));
                    result.put(dt, f);
                    return true;
                }
                return false;
            }
        });
        return result;
    }

    private void createReports() throws IOException {
        new IpoReportWriter(this.items, new File(this.baseDir, "reports")).createReports();
    }

    public static void main(String[] args) throws IOException {
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://localhost/buildindex";
        String user = "indexadm";
        String password = "indexadm";

        LocalDate from = new LocalDate().minusDays(1);
        boolean insertKeys = true;
        boolean processIndexes = true;

        int n = 0;
        while (n < args.length) {
            if ("-f".equals(args[n])) {
                from = ISODateTimeFormat.date().parseLocalDate(args[++n]);
            }
            else if ("-u".equals(args[n])) {
                url = args[++n];
            }
            else if ("-r".equals(args[n])) {
                insertKeys = false;
                processIndexes = false;
            }
            else {
                throw new IllegalArgumentException("Unknown arg '" + args[n] + "'");
            }
            n++;
        }

        SingleConnectionDataSource ds = new SingleConnectionDataSource();
        ds.setDriverClassName(driver);
        ds.setPassword(password);
        ds.setUsername(user);
        ds.setUrl(url);

        final IpoAnalyzer ipoAnalyzer = new IpoAnalyzer(ds);

        ipoAnalyzer.deleteOldItems(new LocalDate().minusDays(10));
        ipoAnalyzer.readItems();

        if (insertKeys) {
            ipoAnalyzer.insertNewKeys(from);
        }
        if (processIndexes) {
            ipoAnalyzer.processIndexes(from);
        }
        ipoAnalyzer.createReports();
    }
}
