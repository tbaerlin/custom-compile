/*
 * DiskMeter.java
 *
 * Created on 17.01.14 10:36
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.util.Random;

import de.marketmaker.istar.common.util.NumberUtil;
import de.marketmaker.istar.common.util.TimeTaker;


/**
 * @author mwilke
 */
public class DiskMeter {

    /* little tool to stress test disks of productive machines and to compare them
    *
    * write a large (10 GB) test file (and show performance of small (1k) block writes):
    *
    * java -cp merger-1.5.jar;common-1.5.jar
    * de.marketmaker.istar.merger.util.DiskMeter write -n 10000000 -b 1024 DiskMeter
    *
    *
    * */

    private boolean write;

    private boolean read;

    private boolean delete = false;

    private long numBlocks;

    private long modulo;

    private int blockSize = 8096;


    private String filename = "./_DiskMeter_";

    private static void usage() {
        System.err.println("Usage: java de.marketmaker.istar.merger.util.DiskMeter write/read <options> file");
        System.err.println(" Writes and/or reads through a file and does some stats");
        System.err.println(" Options are");
        System.err.println("   -d                    -- delete file after work");
        System.err.println("   -n numBlocks          -- number of blocks");
        System.err.println("   -b blockSize          -- size of blocks");
        System.err.println(" file                    -- testfile");
        System.exit(-2);
    }

    public void setWrite(boolean write) {
        this.write = write;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public void setNumBlocks(int numBlocks) {
        this.numBlocks = numBlocks;
        this.modulo = numBlocks / 100;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public static void main(String[] args) throws Exception {
        DiskMeter dm = new DiskMeter();

        int n = 0;
        if (args.length == 0) {
            usage();
        }
        else if ("write".equals(args[0])) {
            dm.setWrite(true);
        }
        else if ("read".equals(args[0])) {
            dm.setRead(true);
        }
        else {
            usage();
        }
        n++;

        while (args.length > n && args[n].startsWith("-")) {
            if ("-b".equals(args[n])) {
                dm.setBlockSize(Integer.parseInt(args[++n]));
            }
            else if ("-n".equals(args[n])) {
                dm.setNumBlocks(Integer.parseInt(args[++n]));
            }
            else if ("-d".equals(args[n])) {
                dm.setDelete(true);
            }
            else {
                usage();
            }
            n++;

        }

        while (args.length > n) {
            dm.setFilename(args[n++]);
        }

        dm.runTest();

    }

    private void runTest() throws IOException {
        System.out.println("#############  OPTIONS #############");
        System.out.println("numBlocks " + this.numBlocks);
        System.out.println("blockSize " + this.blockSize);
        if (this.write) {
            System.out.println("that is   " + NumberUtil.prettyPrint(this.blockSize * this.numBlocks));
        }
        else {
            System.out.println("that is   " + NumberUtil.prettyPrint(new File(this.filename).length()));
        }

        System.out.println("filename  " + this.filename);
        System.out.println("delete    " + this.delete);

        if (this.write) {
            writeFile();
        }
        if (this.read) {
            readFile();
        }
        final File file = new File(this.filename);
        System.out.println("#############  DONE. #############");
        if (this.delete) {
            System.out.println("Deleting " + file.getAbsolutePath());
            file.delete();
        }
        else {
            System.out.println("NOT deleting " + file.getAbsolutePath());
        }

    }

    private void readFile() throws IOException {
        final File file = new File(this.filename);

        System.out.println("#############  READ #############");
        final long length = file.length();
        System.out.println("Reading " + this.numBlocks + " times " + this.blockSize + " bytes in " + NumberUtil.prettyPrint(length) + " " + file.getAbsolutePath());

        RandomAccessFile ramfile = new RandomAccessFile(this.filename, "r");
        TimeTaker tt = new TimeTaker();
        long pointer = 0;
        byte[] bytes = new byte[this.blockSize];

        for (int i = 0; i < this.numBlocks; i++) {

            if (((i) % this.modulo) == 0) {
                int percent = (int) ((long) (i) * 100 / this.numBlocks);
                System.out.print("\rread " + percent + " % ");
                System.out.print("[");
                for (int j = 1; j <= percent; j++) {
                    System.out.print("=");
                }
                for (int j = percent+1; j <= 100; j++) {
                    System.out.print(".");
                }
                System.out.print("]");
            }
            final long pos = (long) (Math.random() * length);

            ramfile.seek(pos);
//            System.out.println("pos = " + pos);
            pointer = ramfile.read(bytes);
//            System.out.println("ramfile.read() = " + pointer);
        }
        tt.stop();

        System.out.print(pointer + "\r");
        System.out.println("Done in " + tt);
        float seeksPerSec = ((float) this.numBlocks / tt.getTotalElapsedMs()) * 1000;
        System.out.println((long) seeksPerSec + " per second");

        ramfile.close();
    }


    private void writeFile() throws IOException {
        final File file = new File(this.filename);

        System.out.println("#############  WRITE #############");
        System.out.println("Writing " + NumberUtil.prettyPrint(numBlocks * blockSize) + " to " + file.getAbsolutePath());


        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), "windows-1252"));

        TimeTaker tt = new TimeTaker();

        for (int i = 0; i < numBlocks; i++) {
            if (((i) % this.modulo) == 0) {
                int percent = (int) ((long) (i) * 100 / numBlocks);
                System.out.print("\rwrote " + percent + " %");
            }
            char[] bytes = getRnd(blockSize);
            bw.write(bytes);
            bw.flush();
        }
        bw.close();
        tt.stop();

        System.out.print("\r");
        System.out.println("Done in " + tt);
        float bytesPerSec = ((float) (this.numBlocks * this.blockSize) / tt.getTotalElapsedMs()) * 1000;
        System.out.println(NumberUtil.prettyPrint((long) bytesPerSec) + " per second");


    }


    private static char[] getRnd(int blockSize) {
        Random randomGenerator = new Random();
        byte[] b = new byte[blockSize];
        randomGenerator.nextBytes(b);
        char[] c = new char[blockSize];
        for (int i = 0; i < b.length; i++) {
            c[i] = (char) b[i];
        }
        return c;
    }
}
