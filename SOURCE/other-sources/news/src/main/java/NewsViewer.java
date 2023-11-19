/*
 * Newser.java
 *
 * Created on 16.08.2007 13:14:10
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.joda.time.DateTime;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import de.marketmaker.istar.news.backend.NewsDaoDb;
import de.marketmaker.istar.news.frontend.NewsRecord;

/**
 * Helper to show all fields for a set of news stored in a database.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NewsViewer extends NewsViewerBase {
    private File outfile = null;
    private File infile = null;
    private boolean encodeInfileIds = false;

    public NewsViewer(String[] args) throws Exception {
        int n = 0;

        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://localhost/news";
        String user = "newsadm";
        String password = "newsadm";
        DateTime from = null;
        DateTime to = null;
        boolean raw = false;

        while (n < args.length && args[n].startsWith("-")) {
            if (args[n].equals("-from")) {
                from = DTF.parseDateTime(args[++n]);
            }
            else if (args[n].equals("-to")) {
                to = DTF.parseDateTime(args[++n]);
            }
            else if (args[n].equals("-d")) {
                driver = args[++n];
            }
            else if (args[n].equals("-j")) {
                url = args[++n];
            }
            else if (args[n].equals("-u")) {
                user = args[++n];
            }
            else if (args[n].equals("-p")) {
                password = args[++n];
            }
            else if (args[n].equals("-t")) {
                textLen = Integer.parseInt(args[++n]);
            }
            else if (args[n].equals("-rt")) {
                rawTextLen = Integer.parseInt(args[++n]);
            }
            else if (args[n].equals("-o")) {
                outfile = new File(args[++n]);
            }
            else if (args[n].equals("-i")) {
                infile = new File(args[++n]);
            }
            else if (args[n].equals("-e")) {
                encodeInfileIds = true;
            }
            else if (args[n].equals("-r")) {
                raw = true;
            }
            n++;
        }

        SingleConnectionDataSource ds = null;
        try {
            ds = new SingleConnectionDataSource();
            ds.setDriverClassName(driver);
            ds.setPassword(password);
            ds.setUsername(user);
            ds.setUrl(url);

            final NewsDaoDb dao = new NewsDaoDb();
            dao.setDataSource(ds);
            dao.afterPropertiesSet();

            final List<String> ids;
            if (from != null && to != null) {
                ids = dao.getIdsFromTo(from, to);
                System.err.println("found " + ids.size() + " news");
            }
            else if (infile != null) {
                ids = readIdsFromFile();
            }
            else {
                ids = collectNewsIds(args, n);
            }

            if (ids.isEmpty()) {
                System.err.println("No news ids, returning");
                return;
            }


            if (this.outfile == null) {
                final List<NewsRecord> items = dao.getItems(ids, true, true);
                if (items.isEmpty()) {
                    System.err.println("No items found in db");
                    return;
                }
                if (raw) {
                    dumpItems(items);
                }
                else {
                    printItems(items);
                }
            }
            else {
                storeItems(dao, ids);
            }
        } finally {
            if (ds != null) {
                closeConnection(ds);
            }
        }
    }

    private List<String> readIdsFromFile() throws IOException {
        ArrayList<String> result = new ArrayList<>();
        Scanner sc = new Scanner(this.infile);
        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.length() > 0) {
                if (encodeInfileIds) {
                    line = Long.toString(Long.parseLong(line), Character.MAX_RADIX);
                }
                result.add(line);
            }
        }
        return result;
    }

    private void storeItems(NewsDaoDb dao, List<String> ids) {
        try {
            final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(this.outfile));
            for (int i = 0; i < ids.size(); i += 50) {
                final List<String> tmp = ids.subList(i, Math.min(ids.size(), i + 50));
                final List<NewsRecord> items = dao.getItems(tmp, true, true);
                for (NewsRecord item : items) {
                    oos.writeObject(item);
                    oos.reset();
                }
            }
            oos.close();
            System.out.println("Stored " + ids.size() + " news in " + this.outfile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to stored news in " + this.outfile.getAbsolutePath());
            e.printStackTrace();
        }
    }


    private void closeConnection(SingleConnectionDataSource ds) {
        try {
            ds.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<String> collectNewsIds(String[] args, int n) {
        final List<String> result = new ArrayList<>();
        for (int i = n; i < args.length; i++) {
            final String id = args[i];
            try {
                result.add(Long.toString(Long.parseLong(id), Character.MAX_RADIX));
            } catch (NumberFormatException e) {
                result.add(id);
            }
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: NewsViewer [-d <driver>] [-j <jdbcUrl>] [-u <user>] [-p <password>] [-o <outfile>] [-i <infile> [-e]] [-t <textlen>] newsid*");
            System.err.println("prints news records to stdout (unless -o is specified)");
            System.err.println(" -t textlen : prints the first textlen chars of the story, 0 = all");
            System.err.println(" -rt textlen: prints the first textlen chars of the raw story, 0 = all");
            System.err.println(" -o outfile : serializes news records into file, no print to stdout");
            System.err.println(" -i infile  : read newsids (e.g., as printed by NewsCli) from infile");
            System.err.println(" -e         : encode newsids from infile (if ids are from mysql-DB)");
            System.exit(-1);
        }
        new NewsViewer(args);
    }
}
