/*
 * TestMulticasting.java
 *
 * Created on 02.11.2008 11:04:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.connect;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import de.marketmaker.istar.common.util.NumberUtil;
import de.marketmaker.istar.common.util.ThroughputLimiter;

/**
 * Used to evaluate multicast networking. Can be started to run a multicast sender or multicast
 * receiver; will print a number of statistics every n seconds.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MulticastTester {    

    private enum Mode {
        SEND, RECEIVE
    }

    private static class Gap {
        private final long from;

        private final int num;

        private Gap(long from, int num) {
            this.from = from;
            this.num = num;
        }

        @Override
        public String toString() {
            return from + "/" + num;
        }
    }

    private abstract class Component implements Runnable {
        protected AtomicBoolean logStats = new AtomicBoolean(false);
        protected DateTimeFormatter dtf = ISODateTimeFormat.hourMinuteSecondMillis();

        protected void triggerLogStats() {
            this.logStats.set(true);
        }

        protected long perSec(long num, float secs) {
            return (long) (num / secs);
        }
    }

    private class Receiver extends Component {
        private final MulticastSocket socket;

        private long numReceived = 0;

        private long lastId = -1;

        private long start = 0;

        private long numOutOfOrder = 0;

        private long numMissed = 0;

        private long lastNumReceived = 0;

        private long lastNow;

        private int lineCount = 0;

        private List<Gap> gaps = new LinkedList<>();

        private Receiver() throws Exception {
            socket = new MulticastSocket(port);
            final NetworkInterface ni = getNetworkInterface();
            if (ni != null) {
                socket.setNetworkInterface(ni);
            }
            final InetAddress group = InetAddress.getByName(groupName);
            socket.joinGroup(group);

            if (ni == null) {
                System.out.println("NetworkInterface = " + socket.getInterface());
            }

            socket.setReceiveBufferSize(bufferSize);
            if (bufferSize != this.socket.getReceiveBufferSize()) {
                throw new Exception("receiveBufferSize " + bufferSize
                        + " requested, got " + this.socket.getReceiveBufferSize());
            }
        }

        public void run() {
            final byte[] data = new byte[packetSize];
            final ByteBuffer bb = ByteBuffer.wrap(data);
            final DatagramPacket packet = new DatagramPacket(data, packetSize);

            do {
                try {
                    this.socket.receive(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                if (this.numReceived++ == 0) {
                    resetStats();
                }
                final long id = bb.getLong(0);
                if (verbose) {
                    info("received %5d, length=%5d", id, packet.getLength());
                }
                synchronized (this) {
                    if (lastId == -1) {
                        this.lastId = id;
                    }
                    else if (id <= lastId) {
                        this.numOutOfOrder++;
                    }
                    else if (id != (lastId + 1)) {
                        final long gapSize = id - lastId - 1;
                        this.numMissed += gapSize;
                        this.gaps.add(new Gap(lastId + 1, (int) gapSize));
                    }
                    if (bb.get(8) != 0) {
                        doLogStats();
                    }
                }
                lastId = id;
            } while (limit == 0 || this.numReceived < limit);
            endLatch.countDown();
        }

        private void resetStats() {
            this.start = System.currentTimeMillis();
            this.lastNow = start;
        }

        private void doLogStats() {
            if ((lineCount % headerCount) == 0) {
                System.out.println("              #received  #last/s #total/s outOfOrder missed gaps");
            }
            lineCount++;

            final long now = System.currentTimeMillis();
            final float lastDiff = (now - lastNow) / 1000f;
            final float totalDiff = (now - start) / 1000f;

            info("%-14s%9d%9d%9d%11d%7d %s", dtf.print(new DateTime()), this.numReceived,
                    perSec(this.numReceived - lastNumReceived, lastDiff),
                    perSec(this.numReceived, totalDiff),
                    this.numOutOfOrder, this.numMissed, this.gaps
            );
            if (!this.gaps.isEmpty()) {
                this.gaps.clear();
            }
            this.lastNumReceived = this.numReceived;
            this.lastNow = now;
        }
    }

    private class Sender extends Component {

        private long numSent = 0;

        private long lastNumSent = 0;

        private ThroughputLimiter limiter;

        private final long start = System.currentTimeMillis();

        private long lastNow = start;

        private int lineCount = 0;

        private MulticastSocket socket;

        private final ByteBuffer buffer;

        private Sender() throws Exception {
            this.socket = new MulticastSocket();
            final NetworkInterface ni = getNetworkInterface();
            if (ni != null) {
                socket.setNetworkInterface(ni);
            }
            this.socket.setSendBufferSize(bufferSize);
            this.socket.setTimeToLive(ttl);
            this.socket.connect(InetAddress.getByName(groupName), port);

            if (ni == null) {
                System.out.println("NetworkInterface = " + socket.getInterface());
            }

            if (bufferSize != this.socket.getSendBufferSize()) {
                throw new Exception("sendBufferSize " + bufferSize
                        + " requested, got " + this.socket.getSendBufferSize());
            }
            this.buffer = ByteBuffer.allocate(packetSize);
        }

        public void run() {
            startLatch.countDown();
            this.limiter = new ThroughputLimiter(frequency, Math.max(1, frequency / 10));
            do {
                final boolean log = this.logStats.compareAndSet(true, false);
                if (send(numSent, log)) {
                    break;
                }
                this.numSent++;
                if (log) {
                    doLogStats();
                }
                this.limiter.ackAction();
            } while (limit == 0 || numSent < limit);

            send(-1L, false);
            endLatch.countDown();
        }

        private boolean send(long num, boolean log) {
            this.buffer.clear();
            this.buffer.putLong(num);
            this.buffer.put((byte) (log ? 1 : 0));
            final DatagramPacket dp = new DatagramPacket(this.buffer.array(), 0, packetSize);
            try {
                this.socket.send(dp);
            } catch (IOException e) {
                e.printStackTrace();
                return true;
            }
            return false;
        }

        public void triggerLogStats() {
            this.logStats.set(true);
        }

        private void doLogStats() {
            if ((lineCount % headerCount) == 0) {
                System.out.println("                  #sent  #last/s #total/s");
            }
            lineCount++;

            final long now = System.currentTimeMillis();
            final float lastDiff = (now - lastNow) / 1000f;
            final float totalDiff = (now - start) / 1000f;

            info("%-14s%9d%9d%9d", dtf.print(new DateTime()), numSent,
                    perSec(numSent - lastNumSent, lastDiff),
                    perSec(numSent, totalDiff));
            this.lastNumSent = numSent;
            this.lastNow = now;
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || "-h".equals(args[0])) {
            System.err.println("Usage: MulticastTester <options>");
            System.err.println(" where options is any combination of");
            System.err.println("-p <port>, default is 13000");
            System.err.println("-i <interface name | hostname/ipaddress of interface>");
            System.err.println("-g <group name>, default is 224.0.0.0");
            System.err.println("-l <num> limit for number of sent/received packets, default is 0 (no limit)");
            System.err.println("-b <size> size of receive or send buffer");
            System.err.println("-ps <size> size of packets to be sent/received");
            System.err.println("-f <num> number of packets sent/s");
            System.err.println("-t <num> number of seconds between statistics output");
            System.err.println("-ttl <num> time-to-live for sent packets");
            System.err.println("-s runs as sender, default is to run as receiver");
            return;
        }
        new MulticastTester(args);
    }

    private int bufferSize = 1024 * 1024;

    private String groupName = "224.0.0.0";

    private String ifname = null;

    private long limit = 0;

    private int packetSize = 1250;

    private int port = 13000;

    private int frequency = 1;

    private int headerCount = 10;

    private boolean verbose = false;

    private final CountDownLatch endLatch = new CountDownLatch(1);

    private final CountDownLatch startLatch = new CountDownLatch(1);

    private String outfile;

    private int ttl = 2;

    public MulticastTester(String[] args) throws Exception {
        int statsInterval = 1;

        Mode mode = Mode.RECEIVE;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("-p".equals(arg)) {
                this.port = Integer.parseInt(args[++i]);
            }
            else if ("-i".equals(arg)) {
                this.ifname = args[++i];
            }
            else if ("-g".equals(arg)) {
                this.groupName = args[++i];
            }
            else if ("-l".equals(arg)) {
                this.limit = Integer.parseInt(args[++i]);
            }
            else if ("-b".equals(arg)) {
                this.bufferSize = NumberUtil.parseInt(args[++i]);
            }
            else if ("-ps".equals(arg)) {
                this.packetSize = Integer.parseInt(args[++i]);
            }
            else if ("-f".equals(arg)) {
                this.frequency = Integer.parseInt(args[++i]);
            }
            else if ("-t".equals(arg)) {
                statsInterval = Integer.parseInt(args[++i]);
            }
            else if ("-ttl".equals(arg)) {
                ttl = Integer.parseInt(args[++i]);
            }
            else if ("-h".equals(arg)) {
                headerCount = Integer.parseInt(args[++i]);
            }
            else if ("-s".equals(arg)) {
                mode = Mode.SEND;
            }
            else if ("-v".equals(arg)) {
                verbose = true;
            }
            else if ("-o".equals(arg)) {
                this.outfile = args[++i];
            }
        }

        PrintWriter pw = null;
        if (this.outfile != null) {
            pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(this.outfile))));
        }

        info("%16s = %-10s", "interface", this.ifname);
        info("%16s = %-10d", "port", this.port);
        info("%16s = %-10s", "group", this.groupName);
        info("%16s = %-10d", "limit", this.limit);
        info("%16s = %-10d", "buffer size", this.bufferSize);
        info("%16s = %-10d", "packet size", this.packetSize);
        info("%16s = %-10s", "mode", mode);
        info("%16s = %-10s", "verbose", verbose);
        if (mode == Mode.SEND) {
            info("%16s = %-10s", "target packets/s", this.frequency);
        }
        System.out.println("Mode: " + mode);

        final ExecutorService es = Executors.newFixedThreadPool(2);
        final Component c = mode == Mode.SEND ? new Sender() : new Receiver();
        final Future<?> future = es.submit(c);

        try {
            if (mode == Mode.SEND) {
                while (!endLatch.await(statsInterval, TimeUnit.SECONDS)) {
                    c.triggerLogStats();
                }
            }
            future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.close();
            }
            es.shutdown();
        }
    }

    private NetworkInterface getNetworkInterface() throws SocketException {
        if (ifname == null) {
            return null;
        }
        final NetworkInterface byName = NetworkInterface.getByName(ifname);
        if (byName != null) {
            return byName;
        }
        try {
            return NetworkInterface.getByInetAddress(InetAddress.getByName(ifname));
        } catch (UnknownHostException e) {
            System.err.println("neither interface nor address: '" + ifname + "'");
            return null;
        }
    }

    void info(String format, Object... args) {
        System.out.printf(format + "%n", args);
    }
}
