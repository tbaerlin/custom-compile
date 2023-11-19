/*
 * SecTypeChangeAnalyzer.java
 *
 * Created on 01.10.13 10:49
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.staticfeed;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.VendorkeyFilterFactory;
import de.marketmaker.istar.feed.mcrip.AbstractRipReader;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.OrderedUpdate;
import de.marketmaker.istar.feed.ordered.OrderedUpdateBuilder;

/**
 * Analyzes static feed dumps and counts for each key the number of mdps sec type changes;
 * keys with type changes will be written to stdout with the respective type change count for
 * that day.
 *
 * @author oflege
 */
public class SecTypeChangeAnalyzer extends AbstractRipReader implements OrderedUpdateBuilder, Runnable {

    private static final FileFilter FILTER = File::isFile;

    private static BlockingQueue<File> files = new ArrayBlockingQueue<>(1000);

    private final Map<ByteString, int[]> map = new HashMap<>(1 << 16);

    public static void main(String[] args) throws Exception {
        for (String arg : args) {
            addFiles(new File(arg));
        }

        final int nThreads = Runtime.getRuntime().availableProcessors();
        final ExecutorService es = Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nThreads; i++) {
            es.submit(new SecTypeChangeAnalyzer());
        }
        es.shutdown();
        es.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        File f;
        while ((f = files.poll()) != null) {
            map.clear();
            try {
                read(f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void addFiles(File file) throws Exception {
        if (file.isDirectory()) {
            addDir(file);
        }
        else {
            files.add(file);
        }
    }

    private static void addDir(File dir) throws Exception {
        final File[] files = dir.listFiles(FILTER);
        for (File file : files) {
            addFiles(file);
        }
    }

    private void read(File file) throws Exception {
        read(file, VendorkeyFilterFactory.ACCEPT_ALL, this);
        for (Map.Entry<ByteString, int[]> e : map.entrySet()) {
            if (e.getValue()[1] > 1) {
                System.out.println(e.getKey() + " " + e.getValue()[1]);
            }
        }
    }

    @Override
    public void process(OrderedFeedData data, OrderedUpdate update) {
        final int type = update.getMdpsKeyType();
        int[] ints = map.get(data.getVwdcode());
        if (ints == null) {
            map.put(data.getVwdcode(), new int[]{type, (type == 0) ? 0 : 1});
        }
        else {
            if (ints[0] != type && type != 0) {
                ints[0] = type;
                ints[1]++;
            }
        }
    }
}
