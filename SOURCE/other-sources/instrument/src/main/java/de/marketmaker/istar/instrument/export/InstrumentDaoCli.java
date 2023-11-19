/*
 * InstrumentDaoCli.java
 *
 * Created on 17.01.13 12:04
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapperImpl;

import de.marketmaker.istar.domain.ItemWithSymbols;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.DomainContextImpl;
import de.marketmaker.istar.domainimpl.ItemWithSymbolsDp2;
import de.marketmaker.istar.domainimpl.instrument.InstrumentDp2;
import de.marketmaker.istar.domainimpl.instrument.QuoteDp2;
import de.marketmaker.istar.instrument.protobuf.InstrumentSerializer;

import static de.marketmaker.istar.domain.KeysystemEnum.VWDFEED;
import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

/**
 * @author oflege
 */
public class InstrumentDaoCli {

    private static final DateTimeFormatter YYYY_MM_DD = DateTimeFormat.forPattern("yyyy_MM_dd");

    private static final DateTimeFormatter YYYY_MM_DD__HH_mm_SS = DateTimeFormat.forPattern("yyyy_MM_dd__HH_mm_SS");

    private static Pattern EXPORT_DIR = Pattern.compile("(?:update|instrument)_(20\\d\\d_\\d\\d_\\d\\d__\\d\\d_\\d\\d_\\d\\d)");

    private static Pattern DAY_DIR = Pattern.compile("20\\d\\d_\\d\\d_\\d\\d");

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            if ("compare".startsWith(args[0])) {
                compare(args);
            }
            else if ("history".startsWith(args[0])) {
                history(args);
            }
            else if ("examine".startsWith(args[0])) {
                examineUpdates(args);
            }
            else if ("inspect".startsWith(args[0])) {
                inspectInstruments(args);
            }
            else {
                System.out.println("Usage: InstrumentDaoCli command options");
                System.out.println("available commands are");
                System.out.println("- inspect <work_dir> <iid>+");
                System.out.println("   print data for iids as read from work_dir");
                System.out.println("- compare <work_dir1> <work_dir2> <iid>");
                System.out.println("   print data for iid as read from work_dir1 and work_dir2");
                System.out.println("- history [-v] <path_to_daydirs> <iid>");
                System.out.println("   print data for iid contained in all instrument files under path_to_daydirs");
                System.out.println("   use -v for verbose output");
            }
        }
    }

    private static void examineUpdates(String[] args) throws Exception {
        final List<File> files = new ArrayList<>(FileUtils.listFiles(new File(args[1]), new String[]{"iol"}, true));
        files.sort(null);
        InstrumentDirDao[] daos = new InstrumentDirDao[files.size()];
        for (int i = 0; i < daos.length; i++) {
            daos[i] = (i == 0)
                    ? new InstrumentDirDao(files.get(i).getParentFile())
                    : new InstrumentDirDao(files.get(i).getParentFile(), daos[0].getDomainContext());
        }
        InstrumentSerializer is = new InstrumentSerializer();
        Instrument last = null;
        for (int i = 0; i < daos.length; i++) {
            final Instrument instrument = daos[i].getInstrument(177905385);
            if (last != null) {
                final byte[] b1 = is.serialize((InstrumentDp2) instrument);
                final byte[] b2 = is.serialize((InstrumentDp2) last);
                boolean beq = Arrays.equals(b1, b2);
                final boolean eq = instrument.equals(last);
                System.out.println("----- " + eq + " " + beq + " -------");
                printDiff((InstrumentDp2) instrument, (InstrumentDp2) last);
                System.out.println(last.getQuotes().get(0).getEntitlement());
                System.out.println("-----------------------------");
            }
            last = instrument;
        }
    }

    private static void inspectInstruments(String[] args) throws Exception {
        InstrumentDirDao dao = new InstrumentDirDao(new File(args[1]));
        InstrumentSerializer is = new InstrumentSerializer();
        for (int i = 2; i < args.length; i++) {
            System.out.printf("-- %s -----------------%n", args[i]);
            final Instrument instrument = dao.getInstrument(Long.parseLong(args[i]));
            if (instrument != null) {
                dumpInstrument(instrument);
            }
        }
    }

    private static void dumpInstrument(Instrument instrument) {
        System.out.println(ToStringBuilder.reflectionToString(instrument, MULTI_LINE_STYLE));
        for (Quote quote : quotesSortedById(instrument)) {
            System.out.print("* " + quote + " ");
            System.out.println(ToStringBuilder.reflectionToString(quote, MULTI_LINE_STYLE));
        }
        System.out.println("-----------------------------");
    }

    private static void printDiff(InstrumentDp2 i1, InstrumentDp2 i2) {
        final PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(i1.getClass());
        final BeanWrapperImpl bw1 = new BeanWrapperImpl(i1);
        final BeanWrapperImpl bw2 = new BeanWrapperImpl(i2);
        for (PropertyDescriptor pd : pds) {
            printDiff(pd.getName(), bw1.getPropertyValue(pd.getName()), bw2.getPropertyValue(pd.getName()));
        }
    }

    private static void printDiff(String name, Object o1, Object o2) {
        final String s1 = (o1 != null) && o1.getClass().isArray() ? Arrays.toString((Object[]) o1) : String.valueOf(o1);
        final String s2 = (o2 != null) && o2.getClass().isArray() ? Arrays.toString((Object[]) o2) : String.valueOf(o2);
        if (!s1.equals(s2)) {
            System.out.println(name + ": " + s1 + " <=> " + s2);
        }
    }

    private static void compare(String[] args) {
        File dir1 = new File(args[1], "data/instruments");
        File dir2 = new File(args[2], "data/instruments");
        long iid = Long.parseLong(args[3]);
        try (InstrumentDirDao dao1 = new InstrumentDirDao(dir1); InstrumentDirDao dao2 = new InstrumentDirDao(dir2);) {

            InstrumentDp2 i1 = (InstrumentDp2) dao1.getInstrument(iid);
            System.out.println("--FROM " + dir1.getAbsolutePath() + "--");
            printInstrument(i1);

            System.out.println();
            System.out.println("--FROM " + dir2.getAbsolutePath() + "--");
            InstrumentDp2 i2 = (InstrumentDp2) dao2.getInstrument(iid);
            printInstrument(i2);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void printInstrument(InstrumentDp2 instrument) {
        System.out.println(instrument);
        if (instrument == null) {
            return;
        }
        printSymbols(instrument, " ");
        for (Quote quote : quotesSortedById(instrument)) {
            System.out.println("  " + quote + " " + Arrays.toString(quote.getEntitlement().getEntitlements(VWDFEED)));
            printSymbols((QuoteDp2) quote, "   ");
        }
    }

    private static void printSymbols(ItemWithSymbolsDp2 i1, final String prefix) {
        for (Map.Entry<KeysystemEnum, String> e : i1.getSymbols()) {
            System.out.println(prefix + e.getKey() + "(" + e.getKey().ordinal() + ") => " + e.getValue());
        }
    }

    private static void history(String[] args) throws Exception {
        int n = 1;

        boolean verbose = false;
        if ("-v".equals(args[n])) {
            verbose = true;
            n++;
        }

        File dir = new File(args[n++]);
        long iid = Long.parseLong(args[n++]);
        System.out.println("looking at " + dir + " for " + iid);
        NavigableMap<DateTime, File> dirMap = findDirs(dir, DAY_DIR, YYYY_MM_DD);

        for (Map.Entry<DateTime, File> e : dirMap.entrySet()) {
            NavigableMap<DateTime, File> dirs = findDirs(e.getValue(), EXPORT_DIR, YYYY_MM_DD__HH_mm_SS);
            history(dirs, iid, verbose);
        }
    }

    private static void history(NavigableMap<DateTime, File> dirs, long iid, boolean verbose) {
        DomainContextImpl dc = null;
        for (Map.Entry<DateTime, File> e : dirs.entrySet()) {
            File dir = e.getValue();
            try (InstrumentDirDao dao = new InstrumentDirDao(new File(dir, "data/instruments"), dc)) {
                if (dir.getName().startsWith("instrument")) {
                    dc = dao.getDomainContext();
                }
                Instrument instrument = dao.getInstrument(iid);
                prettyPrint(dir.getName(), instrument, verbose);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void prettyPrint(String dirName, Instrument instrument, boolean verbose) {
        System.out.println(dirName);
        if (instrument == null) {
            return;
        }
        if (verbose) {
            dumpInstrument(instrument);
        }
        else {
            System.out.println(instrument);
            for (Quote quote : quotesSortedById(instrument)) {
                System.out.println("  " + quote + "  "
                        + Arrays.toString(quote.getEntitlement().getEntitlements(VWDFEED)));
            }
            System.out.println();
        }
    }

    private static List<Quote> quotesSortedById(Instrument instrument) {
        List<Quote> quotes = new ArrayList<>(instrument.getQuotes());
        quotes.sort(ItemWithSymbols.BY_ID);
        return quotes;
    }

    private static NavigableMap<DateTime, File> findDirs(File dir, final Pattern p,
            final DateTimeFormatter formatter) {
        File[] dirs = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        NavigableMap<DateTime, File> dirMap = new TreeMap<>();
        for (File dayDir : dirs) {
            Matcher m = p.matcher(dayDir.getName());
//            System.out.println("dayDir = " + dayDir);
//            System.out.println("m.matches() = " + m.matches());
            if (m.matches()) {
                dirMap.put(DateTime.parse(m.groupCount() >= 1 ? m.group(1) : m.group(), formatter), dayDir);
            }
        }
        return dirMap;
    }
}
