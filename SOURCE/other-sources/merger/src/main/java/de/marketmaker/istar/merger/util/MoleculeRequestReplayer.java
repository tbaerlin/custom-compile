/*
 * MoleculeRequestReplayer.java
 *
 * Created on 25.10.11 13:26
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.output.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import de.marketmaker.istar.common.http.RestTemplateFactory;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.fusion.dmxml.ResponseType;
import de.marketmaker.istar.merger.web.GsonUtil;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;

/**
 * Utility class for testing a dmxml webapp by submitting requests from molecule logs.
 *
 * @author oflege
 */
public class MoleculeRequestReplayer implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final AtomicLong numBytesReceived = new AtomicLong();

    private static final OutputStream NULL_OUTPUT_STREAM = new OutputStream() {
        @Override
        public void write(int b) throws IOException {
            numBytesReceived.incrementAndGet();
        }

        @Override
        public void write(byte[] b) throws IOException {
            numBytesReceived.addAndGet(b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            numBytesReceived.addAndGet(len);
        }
    };

    private PrintWriter failureWriter = new PrintWriter(new NullOutputStream());

    private RestTemplateFactory templateFactory;

    private static class Item {
        final String zone;

        final MoleculeRequest request;

        private Item(String zone, MoleculeRequest request) {
            this.zone = zone;
            this.request = request;
        }
    }

    private static class MoleculeFile {

        private static final Pattern DATE_PATTERN = Pattern.compile("20[0-9]{2}-[0-9]{2}-[0-9]{2}");

        private String fileDate;

        private Scanner sc;

        private String nextLine;

        private MoleculeFile(File f) throws IOException {
            sc = new Scanner(getInputStream(f), "utf8");
            nextLine = sc.hasNextLine() ? sc.nextLine() : null;
            Matcher m = DATE_PATTERN.matcher(f.getName());
            this.fileDate = m.find() ? m.group() : new LocalDate().toString("yyyy-MM-dd");
        }

        private String peek() {
            return this.nextLine;
        }

        boolean hasNextLine() {
            return this.nextLine != null;
        }

        private String nextLine() {
            if (this.nextLine == null) {
                return null;
            }
            String result = nextLine;
            nextLine = sc.hasNextLine() ? sc.nextLine() : null;
            return result;
        }

        private void close() {
            this.sc.close();
        }
    }

    // avoid anything that modifies external state
    private final static Pattern SEQUENTIAL_ATOMS = Pattern.compile("(PF_|WL_|LT_|ALT_|MSC_User|GIS_Ebrokerage).*");

    private final static DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("HH:mm:ss.SSS");

    private AtomicInteger numFailed = new AtomicInteger();

    private AtomicInteger numSucceeded = new AtomicInteger();

    private AtomicInteger numRejected = new AtomicInteger();

    private int maxNumRequests = Integer.MAX_VALUE;

    private String host = "te-mmfweb.vwd.com";

    private int targetRequestsPerSec = 0;

    private int numRequests = 0;

    private int numThreads = 2;

    private boolean realtime = false;

    private List<File> moleculeFiles = new ArrayList<>();

    private final BlockingQueue<Item> queue = new LinkedBlockingQueue<>(1000);

    private final Item DEATH_PILL = new Item(null, null);

    private CountDownLatch cdl;

    private RestTemplate restTemplate;

    private String baseUri;

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: MoleculeRequestReplayer [options] [files]");
            System.err.println(" [options]");
            System.err.println(" -h <name>: host to submit requests to (default = te-mmfweb.vwd.com)");
            System.err.println(" -t <num> : number of threads that submit requests (default = 2)");
            System.err.println(" -n <num> : max number of requests that will be submitted");
            System.err.println(" -x <num> : try to submit this many requests per second");
            System.err.println("            request queue might overflow");
            System.err.println("            cannot be used with -r");
            System.err.println(" -r       : realtime replay: all files are read in parallel");
            System.err.println("            requests are played in the second the occurred,");
            System.err.println("            tick data requests request today's ticks");
            System.err.println(" -f <file>: file to write failures to");
            System.err.println(" [files]");
            System.err.println(" a number of molecule request files (gzipped or not) from which");
            System.err.println(" requests will be retrieved");
            System.exit(1);
        }
        MoleculeRequestReplayer mrp = new MoleculeRequestReplayer();
        int n = 0;
        while (n < args.length && args[n].startsWith("-")) {
            if ("-t".equals(args[n])) {
                mrp.numThreads = Integer.parseInt(args[++n]);
            }
            else if ("-n".equals(args[n])) {
                mrp.maxNumRequests = Integer.parseInt(args[++n]);
            }
            else if ("-x".equals(args[n])) {
                mrp.targetRequestsPerSec = Integer.parseInt(args[++n]);
            }
            else if ("-f".equals(args[n])) {
                mrp.failureWriter = new PrintWriter(new File(args[++n]));
            }
            else if ("-r".equals(args[n])) {
                mrp.realtime = true;
            }
            else if ("-h".equals(args[n])) {
                mrp.baseUri = args[++n];
            }
            n++;
        }

        while (n < args.length) {
            File f = new File(args[n++]);
            if (f.canRead() && f.isFile()) {
                mrp.moleculeFiles.add(f);
            }
            else {
                System.err.println("not a readable file " + f.getAbsolutePath());
                System.exit(-1);
            }
        }
        if (mrp.moleculeFiles.isEmpty()) {
            mrp.moleculeFiles.add(new File("d:/temp/molecule.log"));
        }

        mrp.logger.info("<main> " + mrp.getClass().getSimpleName() + " " + Arrays.toString(args));
        mrp.init();
        try {
            mrp.play();
        } finally {
            mrp.failureWriter.close();
        }
    }

    private void init() throws Exception {
        templateFactory = new RestTemplateFactory();
        templateFactory.setMaxTotalConnections(this.numThreads + 50);
        templateFactory.setMaxConnectionsPerRoute(this.numThreads + 50);

        this.restTemplate = templateFactory.getObject();
    }


    private void play() throws Exception {
        cdl = new CountDownLatch(this.numThreads);
        for (int i = 0; i < this.numThreads; i++) {
            new Thread(this, "player-" + i).start();
        }


        TimeTaker tt = new TimeTaker();
        if (this.realtime) {
            playRealtime();
        }
        else {
            playFiles();
        }
        for (int i = 0; i < this.numThreads; i++) queue.add(DEATH_PILL);
        cdl.await();
        this.logger.info("<play> took " + tt + " for " + numRequests);
        this.logger.info("<play> #succeeded=" + numSucceeded.get() + ", #failed=" + numFailed.get());
        this.templateFactory.destroy();
    }

    private void startStatsTime(final boolean withRejected) {
        new Timer(true).scheduleAtFixedRate(new TimerTask() {
            int n = 0;
            long lastNumBytes = 0;

            @Override
            public void run() {
                long numBytes = numBytesReceived.get();
                n = printStats(n, numBytes - this.lastNumBytes, withRejected);
                this.lastNumBytes = numBytes;
            }
        }, 1000, 1000);
    }

    private void playRealtime() throws IOException, InterruptedException {
        MoleculeFile[] files = new MoleculeFile[this.moleculeFiles.size()];
        int scanTime = 0;
        for (int i = 0; i < files.length; i++) {
            File mf = this.moleculeFiles.get(i);
            files[i] = new MoleculeFile(mf);
            scanTime = Math.max(scanTime, Math.max(5,
                    (int)(mf.length() * (mf.getName().endsWith("gz") ? 20 : 1) / (1024 * 1024 * 10))));
        }

        LocalTime lt = new LocalTime().plusSeconds(scanTime);
        String now = format(lt);

        for (MoleculeFile file : files) {
            while (file.hasNextLine() && file.peek().compareTo(now) < 0) {
                file.nextLine();
            }
        }

        Thread.sleep(Math.max(0, lt.toDateTimeToday().getMillis() - new LocalTime().toDateTimeToday().getMillis()));
        startStatsTime(true);
        MAIN_LOOP:
        while (lt.getMillisOfDay() > 0) {
            now = format(lt);
            LocalTime ltn = lt.plusSeconds(1);
            String end = format(ltn);

            for (MoleculeFile file : files) {
                while (file.hasNextLine()) {
                    if (file.peek().compareTo(now) < 0) {
                        file.nextLine();
                    }
                    else if (file.peek().compareTo(end) < 0) {
                        if (!offer(file)) break MAIN_LOOP;
                    }
                    else {
                        break;
                    }
                }
            }
            lt = ltn;
            Thread.sleep(Math.max(0, lt.toDateTimeToday().getMillis() - new DateTime().getMillis()));
            if (!hasNextLine(files)) {
                break;
            }
        }

        for (MoleculeFile file : files) {
            file.close();
        }
    }

    private boolean offer(MoleculeFile mf) {
        String line = mf.nextLine();
        Item item = toItem(line, mf.fileDate);
        if (item == null) {
            return true;
        }
        if (!this.queue.offer(item)) {
            this.numRejected.incrementAndGet();
            return true;
        }
        return (++this.numRequests < this.maxNumRequests);
    }

    private boolean hasNextLine(MoleculeFile[] files) {
        for (MoleculeFile mf : files) {
            if (mf.hasNextLine()) {
                return true;
            }
        }
        return false;
    }

    private String format(LocalTime ltn) {
        return ltn.toString("HH:mm:ss") + ".000";
    }

    private void playFiles() throws IOException, InterruptedException {
        startStatsTime(this.targetRequestsPerSec <= 0);
        for (File moleculeFile : moleculeFiles) {
            if (!playFile(moleculeFile)) {
                break;
            }
        }
    }

    private int printStats(int n, long numBytes, boolean withRejected) {
        int s = this.numSucceeded.get();
        int f = this.numFailed.get();
        StringBuffer sb = new StringBuffer(100);
        Formatter fmt = new Formatter(sb);
        if (withRejected) {
            fmt.format(" - #success=%6d  #failed=%6d  #rejected=%6d  #req/s=%3d  #bytes/s=%8d%n",
                    s, f, this.numRejected.get(), (s + f - n), numBytes);
        }
        else {
            fmt.format(" - #success=%6d  #failed=%6d  #req/s=%3d  #bytes/s=%8d%n",
                    s, f, (s + f - n), numBytes);
        }
        this.logger.info(sb.toString());
        return s + f;
    }

    private boolean playFile(File moleculeFile) throws IOException, InterruptedException {
        InputStream is = getInputStream(moleculeFile);

        long ms = System.currentTimeMillis() + 1000;
        int numOffered = 0;

        Scanner sc = null;
        try {
            sc = new Scanner(is, "utf8");
            while (sc.hasNextLine()) {
                final Item item = toItem(sc.nextLine(), null);
                if (item != null) {
                    if (this.targetRequestsPerSec <= 0) {
                        this.queue.put(item);
                    }
                    else {
                        if (this.queue.offer(item)) {
                            if (++numOffered == targetRequestsPerSec) {
                                Thread.sleep(Math.max(0, ms - System.currentTimeMillis()));
                                ms = System.currentTimeMillis() + 1000;
                                numOffered = 0;
                            }
                        } else {
                            this.numRejected.incrementAndGet();
                            continue;
                        }
                    }
                    if (++this.numRequests == this.maxNumRequests) {
                        return false;
                    }
                }
            }
            return true;
        } finally {
            if (sc != null) {
                sc.close();
            }
        }
    }

    public static InputStream getInputStream(File moleculeFile) throws IOException {
        InputStream is = new FileInputStream(moleculeFile);
        if (moleculeFile.getName().endsWith(".gz")) {
            is = new GZIPInputStream(is);
        }
        return is;
    }

    public Item toItem(String line, String date) {
        int jsonStart = line.indexOf('{');
        MoleculeRequest mr = GsonUtil.fromJson(line.substring(jsonStart), MoleculeRequest.class);
        MoleculeRequest mrFiltered = new MoleculeRequest(mr);
        for (MoleculeRequest.AtomRequest a : mr.getAtomRequests()) {
            if (SEQUENTIAL_ATOMS.matcher(a.getName()).matches()) {
                continue;
            }
            Map<String,String[]> params = a.getParameterMap();
            if (date != null && a.getName().equals("MSC_TickData")) {
                String start = params.containsKey("start") ? params.get("start")[0] : null;
                String end = params.containsKey("end") ? params.get("end")[0] : null;
                String today = new LocalDate().toString("yyyy-MM-dd");
                if (start != null && start.startsWith(date)) {
                    params.put("start", new String[]{today + start.substring(10) });
                    if (end != null) {
                        params.put("end", new String[]{today + start.substring(10)});
                    }
                }
            }
            mrFiltered.addAtom(a.getId(), a.getName(), params, a.getDependsOnId());
        }
        if (mrFiltered.getAtomRequests().isEmpty()) {
            return null;
        }
        int i = jsonStart - 2;
        while (line.charAt(i) != ' ') {
            i--;
        }
        return new Item(line.substring(i + 1, jsonStart - 1), mrFiltered);
    }

    @Override
    public void run() {
        Item item;
        while ((item = getNext()) != DEATH_PILL) {
            handle(item);
        }
        cdl.countDown();
    }

    private Item getNext() {
        try {
            return this.queue.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    private void handle(Item item) {
        final String json = GsonUtil.toJson(item.request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);

        URI uri = UriComponentsBuilder.fromHttpUrl(this.baseUri
                + "/dmxml-1/" + item.zone + "/retrieve.xml").build().toUri();

        try {
            this.restTemplate.postForObject(uri, entity, ResponseType.class);
        } catch (Exception e) {
            this.logger.error("<doEvaluate> failed for " + json, e);
            addFailure(json, e.getClass().getSimpleName(), e.getMessage());
        }
    }

    private void addFailure(String json, Object... args) {
        numFailed.incrementAndGet();
        synchronized (this.failureWriter) {
            this.failureWriter.print(json);
            for (Object arg : args) {
                if (arg != null) {
                    this.failureWriter.print(" ");
                    this.failureWriter.print(arg);
                }
            }
            this.failureWriter.println();
        }
    }
}
